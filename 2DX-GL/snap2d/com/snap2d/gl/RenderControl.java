/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.gl;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import bg.x2d.ImageUtils;
import bg.x2d.Local;
import bg.x2d.utils.ConfigLogHandler;

import com.snap2d.ThreadManager;
import com.snap2d.gl.GraphicsConfig.Property;
import com.snap2d.gl.spi.RenderController;

/**
 * Acts as a rendering handle to a Display. This class handles the core game
 * update/render thread for the Java2D renderer.
 * 
 * @author Brian Groenke
 * @since Snap2D 1.0
 * @see com.snap2d.gl.Display
 */
public class RenderControl implements RenderController {

    public static final String CL_LIB_LOC = Local.getNativeLibraryLocation() + "/opencl";

    public static final int POSITION_LAST = RenderController.POSITION_LAST;
    public static final Color CANVAS_BACK = Color.WHITE;

    public static int stopTimeout = 2000;

    private static final long RESIZE_TIMER = (long) 1.0E8;

    private static final Logger log = Logger.getLogger(RenderControl.class.getCanonicalName());
    {
        log.setLevel(Level.CONFIG);
        log.addHandler(new ConfigLogHandler(""));
    }

    /**
     * Determines whether or not auto-resizing should be used. True by default.
     */
    public volatile boolean auto = true;

    /**
     * True if hardware acceleration (VolatileImage) should be used, false
     * otherwise.
     */
    public volatile boolean accelerated = true;

    protected Canvas canvas;
    protected volatile BufferedImage pri;
    protected volatile VolatileImage accBuff;
    protected volatile int[] pixelData;
    protected volatile long lastResizeFinish;
    protected volatile boolean applyGamma, updateGamma, scheduledResize;

    protected List <Renderable> rtasks = new ArrayList <Renderable>(), delQueue = new Vector <Renderable>();
    protected List <QueuedRenderable> addQueue = new Vector <QueuedRenderable>();
    protected RenderLoop loop;
    protected ThreadManager exec = new ThreadManager();
    protected AutoResize autoResize;
    protected Future <?> taskCallback;
    protected Dimension initSize;
    protected int buffs;
    protected float gamma = 1.0f;

    protected GammaTable gammaTable = new GammaTable(gamma);
    protected Map <RenderingHints.Key, Object> renderOps;

    private final Semaphore loopChk = new Semaphore(1, true);
    private final GraphicsConfig config;

    /**
     * Creates a RenderControl object that can be used to render data to a
     * Display. A Canvas object is created internally with a managed
     * BufferStrategy. RenderControl renders everything to a VolatileImage back
     * buffer before drawing to the Canvas, so creating the BufferStrategy with
     * 1-2 buffers is recommended for best performance.
     * 
     * @param buffs
     *            the number of buffers the BufferStrategy should be created
     *            with.
     */
    protected RenderControl(final int buffs, final GraphicsConfig config) {

        this.config = config;
        this.canvas = new Canvas();
        this.buffs = buffs;

        renderOps = new HashMap <RenderingHints.Key, Object>();
        loop = new RenderLoop();
        autoResize = new AutoResize();

        canvas.setIgnoreRepaint(true);
        canvas.addComponentListener(autoResize);
        canvas.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {

                setRenderActive(true);
            }

            @Override
            public void focusLost(final FocusEvent e) {

                setRenderActive(false);
            }
        });

        printInitReport();
    }

    @Override
    public void startRenderLoop() {

        taskCallback = exec.submitJob(loop);
    }

    @Override
    public void stopRenderLoop() {

        loop.running = false;
        loop.active = false;

        long st = System.currentTimeMillis();
        while (!taskCallback.isDone()) {
            if (System.currentTimeMillis() - st > stopTimeout) {
                taskCallback.cancel(true);
                break;
            }
        }
    }

    /**
     * If true, the rendering loop will actively render to the screen and
     * execute update logic. Otherwise, the loop will sleep until it is stopped
     * or set to active again.
     * 
     * @param active
     *            true to enable active rendering/updates, false to disable
     *            rendering/updates
     */
    @Override
    public void setRenderActive(final boolean active) {

        try {
            loopChk.acquire();
            loop.active = active;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            loopChk.release();
        }
    }

    /**
     * If true, the rendering loop will continue to run, but update ticks will
     * be skipped. False by default.
     * 
     * @param noUpdate
     *            true if updates should be disabled, false to enable.
     */
    @Override
    public void setDisableUpdates(final boolean noUpdate) {

        loop.noUpdate = noUpdate;
    }

    @Override
    public boolean isUpdating() {

        return !loop.noUpdate;
    }

    /**
     * Enable/disable hardware accelerated rendering on the back buffer. True by
     * default.
     * 
     * @param accelerated
     */
    public void setUseHardwareAcceleration(final boolean accelerated) {

        this.accelerated = accelerated;
    }

    /**
     * Fully releases system resources used by this RenderControl object and
     * clears all registered Renderables. Note that once this method is called,
     * the object is unusable and should be released for garbage collection.
     * Continued use of a disposed RenderControl object will cause errors.
     */
    public void dispose() {

        stopRenderLoop();
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs != null) {
            bs.dispose();
        }
        rtasks.clear();
        addQueue.clear();
        delQueue.clear();
        renderOps.clear();
        // pri.flush();
        if (accBuff != null) {
            accBuff.flush();
        }

        // nullify references to potentially significant resource holders to
        // ensure that they are
        // available
        // for garbage collection.
        pixelData = null;
        canvas = null;
        loop = null;
        // pri = null;
        accBuff = null;
        autoResize = null;
        taskCallback = null;
    }

    /**
     * Gets the last recorded number of frames rendered per second.
     * 
     * @return
     */
    @Override
    public int getCurrentFPS() {

        return loop.fps;
    }

    /**
     * Gets the last recorded number of updates (ticks) per second.
     * 
     * @return
     */
    @Override
    public int getCurrentTPS() {

        return loop.tps;
    }

    /**
     * Sets the frame rate that the rendering algorithm will target when
     * interpolating.
     * 
     * @param fps
     *            frames per second
     */
    @Override
    public void setTargetFPS(final int fps) {

        loop.setTargetFPS(fps);
    }

    /**
     * Sets the frequency per second at which the Renderable.update method is
     * called.
     * 
     * @param tps
     *            ticks per second
     */
    @Override
    public void setTargetTPS(final int tps) {

        loop.setTargetTPS(tps);
    }

    /**
     * Sets the max number of times updates can be issued before a render must
     * occur. If animations are "chugging" or skipping, it may help to set this
     * value to a very low value (1-2). Higher values will prevent the game
     * updates from freezing.
     * 
     * @param maxUpdates
     *            max number of updates to be sent before rendering.
     */
    @Override
    public void setMaxUpdates(final int maxUpdates) {

        loop.setMaxUpdates(maxUpdates);
    }

    @Deprecated
    /**
     * Sets the gamma value that will be applied to all pixels rendered on screen.
     * 
     * @param gamma
     *            a gamma value (0.0 > gamma < 1.0 = darker; gamma > 1.0 = brighter)
     */
    public void setGamma(final float gamma) {

        if (gamma >= 0) {
            this.gamma = gamma;
            updateGamma = true;
        }
    }

    @Deprecated
    /**
     * No longer supported.
     * Enables/disables gamma correction on the rendered image.
     * 
     * @param enabled
     */
    public void setGammaCorrectionEnabled(final boolean enabled) {

        applyGamma = enabled;
    }

    @Deprecated
    /**
     * No longer supported.
     * @return true if gamma correction is enabled, false otherwise.
     */
    public boolean isGammaEnabled() {

        return applyGamma;
    }

    /**
     * @return true if hardware acceleration is enabled, false otherwise.
     */
    public boolean isHardwareAccelerated() {

        return accelerated;
    }

    /**
     * Checks to see if this RenderControl has a loop that is actively
     * rendering/updating.
     * 
     * @return true if active, false otherwise.
     */
    @Override
    public boolean isRenderActive() {

        return loop.active;
    }

    /**
     * Checks to see if this RenderControl has a currently running loop.
     * 
     * @return true if a loop is running, false otherwise.
     */
    public boolean isRunning() {

        return loop.running;
    }

    /**
     * Registers the Renderable object with this RenderControl to be rendered on
     * screen. The render(Graphics2D,float) method will be called to draw to the
     * Graphics context.
     * 
     * @param r
     *            the Renderable object to be called when rendering.
     * @param pos
     *            the position in the rendering queue to be placed. 0 is the
     *            first to be rendered on each frame and LAST is provided as a
     *            convenience field to insert at position size - 1 (aka the end
     *            of the queue, thus last to be rendered on each frame).
     */
    public synchronized void addRenderable(final Renderable r, int pos) {

        if (pos == POSITION_LAST) {
            pos = (addQueue.size() == 0) ? rtasks.size() : rtasks.size() + addQueue.size();
        }
        QueuedRenderable qr = new QueuedRenderable();
        qr.pos = pos;
        qr.r = r;
        addQueue.add(qr);
    }

    /**
     * Removes the Renderable object from the queue, if it exists.
     * 
     * @param r
     *            removes the Renderable from the queue.
     */
    public synchronized void removeRenderable(final Renderable r) {

        delQueue.add(r);
    }

    public synchronized boolean isRegistered(final Renderable r) {

        return rtasks.contains(r);
    }

    public synchronized int getIndexInQueue(final Renderable r) {

        try {
            loopChk.acquire();
            int ind = rtasks.indexOf(r);
            loopChk.release();
            return ind;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized Renderable[] getRenderQueue() {

        Renderable[] queueArr = new Renderable[getQueueSize()];
        try {
            loopChk.acquire();
            rtasks.toArray(queueArr);
            loopChk.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return queueArr;
    }

    /**
     * Fetches List.size() for the rendering queue.
     * 
     * @return
     */
    public int getQueueSize() {

        return rtasks.size();
    }

    public void setRenderOp(final Key key, final Object value) {

        renderOps.put(key, value);
    }

    public void setRenderOps(final Map <Key, ?> hints) {

        renderOps.putAll(hints);
    }

    public Object getRenderOpValue(final Key key) {

        return renderOps.get(key);
    }

    /**
     * Blends a translucent pixel with an opaque destination value. Currently
     * unused.
     * 
     * @param srcA
     * @param srcValue
     * @param dstValue
     * @return
     */
    protected int blend(final float srcA, final int srcValue, final int dstValue) {

        float srcR = ( (srcValue & 0x00ff0000) >>> 16) / 255f;
        float srcG = ( (srcValue & 0x0000ff00) >>> 8) / 255f;
        float srcB = ( (srcValue & 0x000000ff)) / 255f;

        // float dstA = ((dstValue & 0xff000000) >>> 24) / 255f;
        float dstR = ( (dstValue & 0x00ff0000) >>> 16) / 255f;
        float dstG = ( (dstValue & 0x0000ff00) >>> 8) / 255f;
        float dstB = ( (dstValue & 0x000000ff)) / 255f;

        srcR *= srcA;
        srcG *= srcA;
        srcB *= srcA;

        // final output
        float R = srcR + dstR * (1 - srcA);
        float G = srcG + dstG * (1 - srcA);
        float B = srcB + dstB * (1 - srcA);

        return ((int) (R * 255) << 16) | ((int) (G * 255) << 8) | (int) (B * 255);
    }

    ExecutorService renderPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new RenderThreadFactory());
    ArrayList <RenderRow> rowCache = new ArrayList <RenderRow>();

    /**
     * Internal method that is called by RenderLoop to draw rendered data to the
     * screen. The back buffer's data is copied to the main image which, if
     * hardware acceleration is enabled, is drawn to a VolatileImage. Otherwise,
     * the BufferedImage itself is drawn.
     */
    protected void render(final Renderable[] renderables, final float interpolation) {

        // If the component was being resized, cancel rendering until finished
        // (prevents
        // flickering).
        if (System.nanoTime() - lastResizeFinish < RESIZE_TIMER) {
            return;
        }
        if (canvas.getParent() == null) {
            return;
        }
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(buffs);
            canvas.requestFocus();
            return;
        }

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setRenderingHints(renderOps);
        try {

            // Check the status of the VolatileImage and update/re-create it if
            // necessary.
            if (accBuff == null || accBuff.getWidth() != canvas.getWidth() || accBuff.getHeight() != canvas.getHeight()) {
                accBuff = ImageUtils.createVolatileImage(canvas.getWidth(), canvas.getHeight());
                accBuff.setAccelerationPriority( (accelerated) ? 1.0f : 0.0f);
            }
            int stat = 0;
            do {
                if ( (stat = ImageUtils.validateVI(accBuff, g)) != VolatileImage.IMAGE_OK) {
                    if (stat == VolatileImage.IMAGE_INCOMPATIBLE) {
                        accBuff = ImageUtils.createVolatileImage(canvas.getWidth(), canvas.getHeight());
                        accBuff.setAccelerationPriority( (accelerated) ? 1.0f : 0.0f);
                    }
                }

                Graphics2D img = accBuff.createGraphics();
                img.setColor(CANVAS_BACK);
                img.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                for (Renderable r : renderables) {
                    r.render(img, interpolation);
                }
                img.dispose();
            } while (accBuff.contentsLost());

            g.drawImage(accBuff, 0, 0, null);
        } finally {
            g.dispose();
        }

        if (!bs.contentsLost() && canvas.getParent() != null) {
            bs.show();
        }
    }

    /**
     * Not currently used - replaced by faster, direct Volatile Image rendering.
     * As of build 102, the Snap2D Java2D rendering engine no longer supports
     * per-pixel post processing.
     * 
     * @author Brian Groenke
     */
    protected class RenderRow implements Runnable {

        int y;

        RenderRow(final int y) {

            this.y = y;
        }

        @Override
        public void run() {

            int priWidth = pri.getWidth();
            for (int x = 0; x < priWidth; x++ ) {

                if (x >= priWidth || x < 0) {
                    continue;
                }

                int pos = y * priWidth + x;
                if (pos < 0 || pos >= pixelData.length) {
                    continue;
                }

                // get foreground pixels (source)
                int srcValue = pixelData[y * priWidth + x];
                int b = srcValue & 0xFF;
                int g = srcValue >> 8 & 0xFF;
                int r = srcValue >> 16 & 0xFF;
        int a = srcValue >> 24 & 0xFF;
        if (applyGamma) {
            a = gammaTable.applyGamma(a);
            r = gammaTable.applyGamma(r);
            g = gammaTable.applyGamma(g);
            b = gammaTable.applyGamma(b);
        }
        srcValue = a;
        srcValue = (srcValue << 8) + r;
        srcValue = (srcValue << 8) + g;
        srcValue = (srcValue << 8) + b;
        pixelData[pos] = srcValue;
            }
        }

    }

    /*
     * private void initOpenCL() throws IOException {
     * if(!Boolean.getBoolean("com.snap2d.gl.opencl")) return; else if
     * (JavaCL.listPlatforms() == null || JavaCL.listPlatforms().length == 0) {
     * System.out.println("[Snap2D] No supported OpenCL drivers detected");
     * return; }
     * 
     * clContext = JavaCL.createBestContext(); String progSrc =
     * IOUtils.readText(ClassLoader .getSystemResource(CL_PROG));
     * clContext.setCacheBinaries(true); clProg =
     * clContext.createProgram(progSrc); renderKernel =
     * clProg.createKernel(KERNEL1); clQueue = clContext.createDefaultQueue();
     * 
     * gPtr = Pointer.allocateInts(256).order(clContext.getByteOrder());
     * gPtr.setInts(gammaTable.getTable()); gBuff =
     * clContext.createBuffer(Usage.Input, gPtr, false);
     * 
     * CLPlatform platform = JavaCL.getBestDevice().getPlatform();
     * System.out.println("[Snap2D] Initialized " + platform.getName() + " " +
     * platform.getVersion()); }
     */

    /**
     * Loop where render/update logic is executed.
     * 
     * @author Brian Groenke
     */
    protected class RenderLoop implements Runnable {

        // Default values
        private final double TARGET_FPS = 60, TARGET_TIME_BETWEEN_RENDERS = 1000000000.0 / TARGET_FPS, TICK_HERTZ = 30,
                TIME_BETWEEN_UPDATES = 1000000000.0 / TICK_HERTZ, MAX_UPDATES_BEFORE_RENDER = 3;

        private final long SLEEP_WHILE_INACTIVE = 100;

        private double targetFPS = TARGET_FPS, targetTimeBetweenRenders = TARGET_TIME_BETWEEN_RENDERS,
                tickHertz = TICK_HERTZ, timeBetweenUpdates = TIME_BETWEEN_UPDATES,
                maxUpdates = MAX_UPDATES_BEFORE_RENDER;

        volatile int fps, tps;
        volatile boolean running, active, noUpdate, printFrames;

        @Override
        public void run() {

            Thread.currentThread().setName("snap2d-render_loop");

            exec.newDaemon(new Runnable() {

                @Override
                public void run() {

                    Thread.currentThread().setName("snap2d-sleeper_thread");
                    try {
                        if (Local.getPlatform().toLowerCase().contains("windows")
                                && Boolean.getBoolean(Property.SNAP2D_WINDOWS_HIGH_RES_TIMER.getProperty())) {
                            System.out.println("[Snap2D] started windows sleeper daemon");
                            Thread.sleep(Long.MAX_VALUE);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            exec.newDaemon(new Runnable() {

                @Override
                public void run() {

                    Thread.currentThread().setName("snap2d-fps_out_thread");
                    while (running) {
                        try {
                            Thread.sleep(800);
                            boolean print = Boolean.getBoolean(Property.SNAP2D_PRINT_RENDER_STAT.getProperty());
                            while (!printFrames) {
                                ;
                            }
                            String printStr = fps + " fps " + tps + " ticks";
                            if (print) {
                                System.err.println("snap2d-renderer: " + printStr);
                            } else {
                                log.fine(printStr);
                            }
                            printFrames = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });

            double lastUpdateTime = System.nanoTime();
            double lastRenderTime = System.nanoTime();
            int lastSecondTime = (int) (lastUpdateTime / 1000000000);
            int frameCount = 0, ticks = 0;
            running = true;
            active = true;
            Renderable[] renderables = new Renderable[0];
            System.runFinalization();
            System.gc();
            while (running) {
                try {

                    if (addQueue.size() > 0) {

                        for (QueuedRenderable qr : addQueue) {
                            rtasks.add(qr.pos, qr.r);
                        }
                        addQueue.clear();

                        renderables = rtasks.toArray(new Renderable[rtasks.size()]);
                    }

                    if (delQueue.size() > 0) {
                        for (Renderable r : delQueue) {
                            rtasks.remove(r);
                        }
                        delQueue.clear();

                        renderables = rtasks.toArray(new Renderable[rtasks.size()]);
                    }

                    if (scheduledResize) {
                        autoResize.resize();
                        scheduledResize = false;
                    }

                    if (updateGamma) {
                        gammaTable.setGamma(gamma);
                        updateGamma = false;
                    }

                    double now = System.nanoTime();
                    if (active && (canvas.getWidth() > 0 && canvas.getHeight() > 0)) {

                        if (initSize == null) {
                            initSize = new Dimension(canvas.getWidth(), canvas.getHeight());
                        }

                        int updateCount = 0;

                        while (now - lastUpdateTime > timeBetweenUpdates && updateCount < maxUpdates && !noUpdate) {

                            for (Renderable r : renderables) {
                                r.update((long) now, (long) lastUpdateTime);
                            }

                            lastUpdateTime += timeBetweenUpdates;
                            updateCount++ ;
                            ticks++ ;
                        }

                        if (now - lastUpdateTime > timeBetweenUpdates && !noUpdate) {
                            lastUpdateTime = now - timeBetweenUpdates;
                        }

                        float interpolation = Math.min(1.0f, (float) ( (now - lastUpdateTime) / timeBetweenUpdates));
                        /*
                         * Graphics2D g = buff.createGraphics(); for(Renderable
                         * r:renderables) r.render(g, interpolation);
                         * g.dispose(); render();
                         */
                        render(renderables, interpolation);
                        lastRenderTime = now;
                        frameCount++ ;

                        int thisSecond = (int) (now / 1000000000);
                        if (thisSecond > lastSecondTime) {
                            fps = frameCount;
                            tps = ticks;
                            printFrames = true;
                            frameCount = 0;
                            ticks = 0;
                            lastSecondTime = thisSecond;
                        }
                    }

                    if (!active) {
                        fps = 0;
                        tps = 0;
                        printFrames = true;
                    }

                    loopChk.release();
                    while (now - lastRenderTime < targetTimeBetweenRenders
                            && (now - lastUpdateTime < timeBetweenUpdates || noUpdate)) {
                        Thread.yield();
                        now = System.nanoTime();
                    }
                    loopChk.acquire();

                    if (!active) {
                        // preserve CPU if loop is currently is currently
                        // inactive.
                        // the constant can be lowered to reduce latency when
                        // re-focusing.
                        Thread.sleep(SLEEP_WHILE_INACTIVE);
                    }
                } catch (Exception e) {
                    System.err.println("Snap2D: error in rendering loop: " + e.toString()
                            + "\nTerminating loop execution...");
                    CrashReportWindow crashDisp = new CrashReportWindow();
                    crashDisp.dumpToLog("Unhandled error detected in rendering loop - aborting execution", e);
                    crashDisp.setVisible(true);
                    running = false;
                }
            }
        }

        protected void setTargetFPS(final int fps) {

            if (fps < 0) {
                return;
            }
            targetFPS = fps;
            targetTimeBetweenRenders = 1000000000.0 / targetFPS;
        }

        protected void setTargetTPS(final int tps) {

            if (tps < 0) {
                return;
            }
            tickHertz = tps;
            timeBetweenUpdates = 1000000000.0 / tickHertz;
        }

        protected void setMaxUpdates(final int max) {

            if (max > 0) {
                maxUpdates = max;
            }
        }
    }

    protected class QueuedRenderable {

        Renderable r;
        int pos;
    }

    protected class AutoResize extends ComponentAdapter {

        private volatile int wt, ht;

        @Override
        public void componentResized(final ComponentEvent e) {

            wt = e.getComponent().getWidth();
            ht = e.getComponent().getHeight();
            scheduledResize = true;
        }

        protected void resize() {

            if (wt <= 0) {
                wt = 1;
            }

            if (ht <= 0) {
                ht = 1;
            }

            if (auto) {
                Iterator <Renderable> itr = rtasks.listIterator();
                while (itr.hasNext()) {
                    Renderable r = itr.next();
                    r.onResize(initSize, new Dimension(wt, ht));
                }

                lastResizeFinish = System.nanoTime();
            }
        }

    }

    protected static class RenderThreadFactory implements ThreadFactory {

        volatile static int poolNum;

        RenderThreadFactory() {

            poolNum++ ;
        }

        int nthreads;

        @Override
        public Thread newThread(final Runnable arg0) {

            Thread t = new Thread(arg0);
            t.setName("snap2d_render_pool-" + poolNum + "_0" + nthreads);
            t.setDaemon(true);
            t.setPriority(Thread.MAX_PRIORITY);
            nthreads++ ;
            return t;
        }
    }

    private void printInitReport() {

        if (!Boolean.getBoolean(Property.SNAP2D_PRINT_J2D_CONFIG.getProperty())) {
            return;
        }
        log.info("initialized Java2D graphics pipeline");
        for (Property p : config.configMap.keySet()) {
            log.config(p.getProperty() + "=" + config.get(p));
        }
    }
}

/*
 * // If acceleration is now disabled but was previously enabled, release system
 * // resources held by // VolatileImage and set the reference to null. if
 * (accBuff != null) { accBuff.flush(); accBuff = null; }
 * 
 * Graphics2D g2 = pri.createGraphics(); for (Renderable r : renderables) {
 * r.render(g2, interpolation); } g2.dispose();
 * 
 * Future<?> finalRow = null; for (int y = 0; y < priHeight; y++) {
 * 
 * if (y >= priHeight || y < 0) { continue; }
 * 
 * if (y >= rowCache.size()) { rowCache.add(new RenderRow(y)); } Future<?> task
 * = renderPool.submit(rowCache.get(y)); if (y == priHeight - 1) { finalRow =
 * task; } }
 * 
 * while (!finalRow.isDone()) { ; }
 * 
 * g.drawImage(pri, 0, 0, null);
 */
