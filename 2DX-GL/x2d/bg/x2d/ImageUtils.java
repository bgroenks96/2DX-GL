/*
 * Copyright © 2011-2013 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 * Provides static convenience methods for performing geometric operations on
 * images.
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class ImageUtils {

    // Construction is NOT allowed
    private ImageUtils() {

    }

    /**
     * Returns a rotated AffineTransform object that can be used to rotate an
     * image.
     * 
     * @param img
     *            The Image to be rotated
     * @param location
     *            The location of the Image in the 2D coordinate space
     * @param degrees
     *            The number of degrees to rotate
     * @return the rotated (and translated) AffineTransform
     */
    public static AffineTransform rotateImage(final Image img, final Point location, final double degrees) {

        AffineTransform affine = new AffineTransform();
        affine.setToTranslation(location.getX(), location.getY());
        affine.rotate(Math.toRadians(degrees), img.getWidth(null) / 2, img.getHeight(null) / 2);
        return affine;
    }

    /**
     * Draws a rotated version of the given Image to the specified Graphics2D
     * object.
     * 
     * @param img
     *            The Image to be rotated
     * @param location
     *            The location of the Image in the 2D coordinate space
     * @param g2d
     *            The Graphics2D object on which to draw the new rotated image.
     * @param degrees
     *            The number of degrees to rotate
     * @return the rotated (and translated) AffineTransform
     */
    public static AffineTransform rotateImage(final Image img, final Point location, final Graphics2D g2d,
            final double degrees) {

        AffineTransform affine = new AffineTransform();
        affine.setToTranslation(location.getX(), location.getY());
        affine.rotate(Math.toRadians(degrees), img.getWidth(null) / 2, img.getHeight(null) / 2);
        g2d.drawImage(img, affine, null);
        return affine;
    }

    /**
     * Returns a rotated AffineTransform object that can be used to rotate an
     * image.
     * 
     * @param img
     *            The Image to be rotated
     * @param location
     *            The location of the Image in the 2D coordinate space
     * @param theta
     *            Rotation value
     * @param radians
     *            true if theta should be interpreted as a radian value, false
     *            if theta should be interpreted as a degree value.
     * @return the rotated (and translated) AffineTransform
     */
    public static AffineTransform rotateImage(final Image img, final Point location, final double theta,
            final boolean radians) {

        AffineTransform affine = new AffineTransform();
        affine.setToTranslation(location.getX(), location.getY());
        if (radians) {
            affine.rotate(theta, img.getWidth(null) / 2, img.getHeight(null) / 2);
        } else {
            affine.rotate(Math.toRadians(theta), img.getWidth(null) / 2, img.getHeight(null) / 2);
        }
        return affine;
    }

    /**
     * Draws a rotated version of the given Image to the specified Graphics2D
     * object.
     * 
     * @param img
     *            The Image to be rotated
     * @param location
     *            The location of the Image in the 2D coordinate space
     * @param g2d
     *            The Graphics2D object on which to draw the new rotated image.
     * @param theta
     *            Rotation value
     * @param radians
     *            true if theta should be interpreted as a radian value, false
     *            if theta should be interpreted as a degree value.
     * @return the rotated (and translated) AffineTransform
     */
    public static AffineTransform rotateImage(final Image img, final Point location, final Graphics2D g2d,
            final double theta, final boolean radians) {

        AffineTransform affine = new AffineTransform();
        affine.setToTranslation(location.getX(), location.getY());
        if (radians) {
            affine.rotate(theta, img.getWidth(null) / 2, img.getHeight(null) / 2);
        } else {
            affine.rotate(Math.toRadians(theta), img.getWidth(null) / 2, img.getHeight(null) / 2);
        }
        g2d.drawImage(img, affine, null);
        return affine;
    }

    /**
     * Performs a AffineTransform scaling operation relative to the generic
     * Image provided. The returned AffineTransform can be used to draw a scaled
     * version of the Image on screen. High quality is not guaranteed.
     * 
     * @param img
     *            The Image to be scaled.
     * @param location
     *            The location the Image should be drawn at.
     * @param newSize
     *            The dimensions the scaled Image should be.
     * @return a scaled and translated AffineTransform that can be used to draw
     *         a scaled version of the Image.
     */
    public static AffineTransform scaleImage(final Image img, final Point location, final Dimension newSize) {

        AffineTransform at = new AffineTransform();
        at.setToTranslation(location.getX(), location.getY());
        at.scale(newSize.getWidth() / img.getWidth(null), newSize.getHeight() / img.getHeight(null));
        return at;
    }

    /**
     * Takes any generic Image and scales it to a new size. The returned Image
     * is a BufferedImage upon which the scaled image is drawn upon. This method
     * calls
     * <code>scaleImage(img, newSize, BufferedImage.TYPE_INT_ARGB, highQuality)</code>
     * .
     * 
     * @param img
     *            Any generic Image or Image subclass to be scaled (doesn't
     *            affect the BufferedImage type of the returned image).
     * @param newSize
     *            the dimensions for the new Image.
     * @return a BufferedImage with the scaled Image drawn onto it.
     */
    public static BufferedImage scaleImage(final Image img, final Dimension newSize, final ScaleQuality quality) {

        return scaleImage(img, newSize, BufferedImage.TYPE_INT_ARGB, quality);
    }

    /**
     * Legacy method. Calls standard image scaling method with
     * ScaleQuality.NORM. <code>scaleImage(Image,Dimension,ScaleQuality)</code>
     * should be preferred.
     * 
     * @param img
     * @param newSize
     * @return
     */
    public static Image scaleImage(final Image img, final Dimension newSize) {

        return scaleImage(img, newSize, ScaleQuality.NORM);
    }

    /**
     * Takes any Image and scales it to a new size. This method draws the image
     * onto a BufferedImage of the specified size. Various rendering operations
     * are set depending on the value of <code>quality</cdoe>.
     * 
     * @param bi
     *            The Image to be scaled.
     * @param newSize
     *            The new size for the image
     * @param imgType
     *            The new BufferedImage type (defined in the BufferedImage
     *            class)
     * @param quality
     *            The scaling quality of the newly rendered image.
     * @return the image scaled and drawn onto a BufferedImage
     */
    public static BufferedImage scaleImage(final Image img, final Dimension newSize, final int imgType,
            final ScaleQuality quality) {

        BufferedImage bi = new BufferedImage((int) newSize.getWidth(), (int) newSize.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        switch (quality) {
        case HIGH:
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            break;
        case SPEED:
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        case NORM:
            break;
        }
        g.drawImage(img, 0, 0, bi.getWidth(), bi.getHeight(), null);
        g.dispose();
        return bi;
    }

    /**
     * Legacy method. Provided for backwards compatibility.
     * 
     * @param scaleType
     *            Note that this argument is ignored in the modern
     *            implementation of this method.
     * @return BufferedImage scaled using the preferred
     *         <code>scaleImage(Image,Dimension,int,ScaleQuality)</code> method.
     */
    public static BufferedImage scaleBufferedImage(final BufferedImage img, final Dimension newSize, final int imgType,
            final int scaleType) {

        return scaleImage(img, newSize, imgType, ScaleQuality.NORM);
    }

    /**
     * Converts a BufferedImage to the specified type by drawing it onto a new
     * BufferedImage. The data and dimensions of the given image are preserved.
     * 
     * @param bi
     *            The BufferedImage to convert.
     * @param outType
     *            The type of BufferedImage that should be returned.
     * @return the new BufferedImage of type <code>outType</code> that is a copy
     *         of <code>bi</code>
     */
    public static BufferedImage convertBufferedImage(final BufferedImage bi, final int outType) {

        BufferedImage newImage = new BufferedImage(bi.getWidth(), bi.getHeight(), outType);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return newImage;
    }

    /**
     * Converts any Image to a BufferedImage of the specified type. The data and
     * dimensions of the given Image are preserved.
     * 
     * @param img
     *            The Image to convert.
     * @param outType
     *            The type of BufferedImage that should be returned.
     * @return the new BufferedImage of type <code>outType</code> that is a copy
     *         of <code>img</code>
     */
    public static BufferedImage convertImage(final Image img, final int outType) {

        BufferedImage newImage = new BufferedImage(img.getWidth(null), img.getHeight(null), outType);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return newImage;
    }

    /**
     * Convenience method to create a native platform compatible BufferedImage.
     * A native image will, in most cases, boost performance.
     * 
     * @param wt
     *            width of the image
     * @param ht
     *            height of the image
     * @param g
     *            Graphics object to obtain the device configuration from
     * @return the newly created BufferedImage object
     */
    public static BufferedImage getNativeImage(final int wt, final int ht) {

        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(wt, ht, Transparency.TRANSLUCENT);
    }

    /**
     * Convenience method to create a native platform compatible BufferedImage.
     * A native image will, in most cases, boost performance.
     * 
     * @param wt
     *            width of the image
     * @param ht
     *            height of the image
     * @param g
     *            Graphics object to obtain the device configuration from
     * @return the newly created BufferedImage object
     */
    public static BufferedImage getNativeImage(final Image orig) {

        GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration c = d.getDefaultConfiguration();
        BufferedImage nimg = c.createCompatibleImage(orig.getWidth(null), orig.getHeight(null),
                Transparency.TRANSLUCENT);
        Graphics2D g2 = nimg.createGraphics();
        g2.drawImage(orig, 0, 0, null);
        g2.dispose();
        return nimg;
    }

    static int nativeType = Integer.MIN_VALUE;

    public static int getNativeImageType() {

        if (nativeType != Integer.MIN_VALUE) {
            return nativeType;
        }

        GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration c = d.getDefaultConfiguration();
        BufferedImage nat = c.createCompatibleImage(1, 1, Transparency.TRANSLUCENT);
        return (nativeType = nat.getType());
    }

    /**
     * Convenience method for creating a new compatible VolatileImage using the
     * given Graphics context.
     * 
     * @param wt
     *            the width of the new image
     * @param ht
     *            the height of the new image
     * @return the newly created, fully compatible VolatileImage.
     * @see VolatileImage
     */
    public static VolatileImage createVolatileImage(final int wt, final int ht) {

        GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration c = d.getDefaultConfiguration();
        return c.createCompatibleVolatileImage(wt, ht);
    }

    /**
     * Convenience method that calls the validate(...) method on the given
     * VolatileImage using the GraphicsConfiguration returned by
     * <code>((Graphics2D)g).getDeviceConfiguration()</code>.
     * 
     * @param vi
     *            the VolatileImage to be validated.
     * @param g
     *            the Graphics object to use for the GraphicsConfiguration.
     * @return an integer value specified by the {@link GraphicsConfiguration}
     *         class.
     * @see VolatileImage
     */
    public static int validateVI(final VolatileImage vi, final Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        return vi.validate(g2d.getDeviceConfiguration());
    }

    public enum ScaleQuality {
        HIGH, NORM, SPEED;
    }
}
