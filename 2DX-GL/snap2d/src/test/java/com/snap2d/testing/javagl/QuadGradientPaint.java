package com.snap2d.testing.javagl;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.WeakHashMap;

/*
 * Copied from de.lessvoid.nifty.java2d.renderer - Nifty Java2D batch render system
 */
public class QuadGradientPaint implements Paint {

    private final Color c0, c1, c2, c3;

    public QuadGradientPaint(final de.lessvoid.nifty.tools.Color c0,
                             final de.lessvoid.nifty.tools.Color c1,
                             final de.lessvoid.nifty.tools.Color c2,
                             final de.lessvoid.nifty.tools.Color c3) {

        this.c0 = convertNiftyColorToAwt(c0);
        this.c1 = convertNiftyColorToAwt(c1);
        this.c2 = convertNiftyColorToAwt(c2);
        this.c3 = convertNiftyColorToAwt(c3);
    }

    @Override
    public int getTransparency() {

        return Transparency.TRANSLUCENT;
    }

    @Override
    public PaintContext createContext(final ColorModel cm,
                                      final Rectangle deviceBounds,
                                      final Rectangle2D userBounds,
                                      final AffineTransform xform,
                                      final RenderingHints hints) {

        return new QuadGradientPaintContext(cm, deviceBounds, userBounds, xform, hints);
    }

    private static WeakHashMap<ColorModel, Raster> rasterCache = new WeakHashMap<ColorModel, Raster>();

    private class QuadGradientPaintContext implements PaintContext {

        ColorModel cm;
        Rectangle deviceBounds;
        Rectangle2D userBounds;

        Raster saved;

        public QuadGradientPaintContext(final ColorModel cm,
                                        final Rectangle deviceBounds,
                                        final Rectangle2D userBounds,
                                        final AffineTransform xform,
                                        final RenderingHints hints) {

            this.cm = cm;
            this.deviceBounds = deviceBounds;
            this.userBounds = userBounds;
        }

        @Override
        public void dispose() {

            if (saved != null) {
                rasterCache.put(cm, saved);
                saved = null;
            }
        }

        @Override
        public ColorModel getColorModel() {

            return cm;
        }

        @Override
        public Raster getRaster(final int x, final int y, final int w, final int h) {

            /*
             * checkRasterCache(); if (saved == null || w < saved.getWidth() ||
             * h < saved.getHeight()) { saved = createRaster(x, y, w, h); }
             * return saved;
             */
            return createRaster(x, y, w, h);
        }

        private Raster createRaster(final int x, final int y, final int w, final int h) {

            WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
            // final double radius = 1.5*Point.distance(0, 0,
            // userBounds.getCenterX(), userBounds.getCenterY());
            float[] rgba = new float[4];
            float[] hsbf = new float[3]; // final
            float[] hsb0 = Color.RGBtoHSB(c0.getRed(), c0.getGreen(), c0.getRed(), null);
            float[] hsb1 = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getRed(), null);
            float[] hsb2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getRed(), null);
            float[] hsb3 = Color.RGBtoHSB(c3.getRed(), c3.getGreen(), c3.getRed(), null);
            // get space width
            float uwt = (float) userBounds.getWidth();
            float uht = (float) userBounds.getHeight();
            for (int j = y; j < y + h; j++ ) {
                for (int i = x; i < x + w; i++ ) {
                    // if (!deviceBounds.contains(i, j) ||
                    // !userBounds.contains(i, j))
                    // continue;
                    for (int ind = 0; ind < hsbf.length; ind++ ) {
                        float hsbC0 = hsb0[ind];
                        float hsbC1 = hsb1[ind];
                        float hsbC2 = hsb2[ind];
                        float hsbC3 = hsb3[ind];
                        hsbf[ind] = interpolateComponentHSB(hsbC0, hsbC1, hsbC2, hsbC3, i, j, uwt, uht);
                    }
                    int rgbaValue = Color.HSBtoRGB(hsbf[0], hsbf[1], hsbf[2]);
                    Color rgbaColor = new Color(rgbaValue);
                    rgba[0] = rgbaColor.getRed();
                    rgba[1] = rgbaColor.getGreen();
                    rgba[2] = rgbaColor.getBlue();
                    rgba[3] = rgbaColor.getAlpha();
                    raster.setPixel(i - x, j - y, rgba);
                }
            }
            return raster;
        }

        private float interpolateComponentHSB(final float c0,
                                              final float c1,
                                              final float c2,
                                              final float c3,
                                              final float x,
                                              final float y,
                                              final float w,
                                              final float h) {

            return (c1 * y / h + c0 * (1 - y / h)) * (x / w) + (c2 * y / h + c3 * (1 - y / h)) * (1 - x / w);
        }

        private void checkRasterCache() {

            if (saved == null) {
                saved = rasterCache.get(cm);
            }
        }
    }

    private static Color convertNiftyColorToAwt(final de.lessvoid.nifty.tools.Color niftyColor) {

        return new Color(niftyColor.getRed(), niftyColor.getGreen(), niftyColor.getBlue(), niftyColor.getAlpha());
    }
}
