/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package bg.x2d;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
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
	private ImageUtils() {}

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
	public static AffineTransform rotateImage(Image img, Point location,
			double degrees) {
		AffineTransform affine = new AffineTransform();
		affine.setToTranslation(location.getX(), location.getY());
		affine.rotate(Math.toRadians(degrees), img.getWidth(null) / 2,
				img.getHeight(null) / 2);
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
	public static AffineTransform rotateImage(Image img, Point location,
			Graphics2D g2d, double degrees) {
		AffineTransform affine = new AffineTransform();
		affine.setToTranslation(location.getX(), location.getY());
		affine.rotate(Math.toRadians(degrees), img.getWidth(null) / 2,
				img.getHeight(null) / 2);
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
	public static AffineTransform rotateImage(Image img, Point location,
			double theta, boolean radians) {
		AffineTransform affine = new AffineTransform();
		affine.setToTranslation(location.getX(), location.getY());
		if (radians) {
			affine.rotate(theta, img.getWidth(null) / 2,
					img.getHeight(null) / 2);
		} else {
			affine.rotate(Math.toRadians(theta), img.getWidth(null) / 2,
					img.getHeight(null) / 2);
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
	public static AffineTransform rotateImage(Image img, Point location,
			Graphics2D g2d, double theta, boolean radians) {
		AffineTransform affine = new AffineTransform();
		affine.setToTranslation(location.getX(), location.getY());
		if (radians) {
			affine.rotate(theta, img.getWidth(null) / 2,
					img.getHeight(null) / 2);
		} else {
			affine.rotate(Math.toRadians(theta), img.getWidth(null) / 2,
					img.getHeight(null) / 2);
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
	public static AffineTransform scaleImage(Image img, Point location,
			Dimension newSize) {
		AffineTransform at = new AffineTransform();
		at.setToTranslation(location.getX(), location.getY());
		at.scale(newSize.getWidth() / img.getWidth(null), newSize.getHeight()
				/ img.getHeight(null));
		return at;
	}

	/**
	 * Takes any generic Image and scales it to a new size. The returned Image
	 * is a casted BufferedImage upon which the scaled image is drawn upon. This
	 * method calls upon the Image class's <code>getScaledInstance(...)</code>
	 * method to scale the passed Image object. Smooth scaling is used and the
	 * BufferedImage created is of TYPE_INT_ARGB.
	 * 
	 * @param img
	 *            Any generic Image or Image subclass to be scaled (result image
	 *            may not be castable).
	 * @param newSize
	 *            the dimensions for the new Image.
	 * @return an Image casted from a BufferedImage object.
	 */
	public static Image scaleImage(Image img, Dimension newSize) {
		BufferedImage bi = new BufferedImage((int) newSize.getWidth(),
				(int) newSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Image scaled = img.getScaledInstance(bi.getWidth(), bi.getHeight(),
				Image.SCALE_SMOOTH);
		bi.getGraphics().drawImage(scaled, 0, 0, null);
		return bi;
	}

	/**
	 * Takes any BufferedImage and scales it to a new size. This method draws
	 * the returned Image from <code>getScaledInstance(..)</code> to a new,
	 * resized BufferedImage.
	 * 
	 * @param bi
	 *            The BufferedImage to be scaled.
	 * @param newSize
	 *            The new size for the BufferedImage.
	 * @param imgType
	 *            The new BufferedImage type (defined in the BufferedImage
	 *            class)
	 * @param scaleMode
	 *            The method that should be used when scaling (defined in Image
	 *            class)
	 * @return the scaled BufferedImage
	 */
	public static BufferedImage scaleBufferedImage(BufferedImage bi,
			Dimension newSize, int imgType, int scaleMode) {
		BufferedImage newImage = new BufferedImage((int) newSize.getWidth(),
				(int) newSize.getHeight(), imgType);
		Image scaled = bi.getScaledInstance(newImage.getWidth(),
				newImage.getHeight(), scaleMode);
		newImage.getGraphics().drawImage(scaled, 0, 0, null);
		return newImage;
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
	public static BufferedImage convertBufferedImage(BufferedImage bi,
			int outType) {
		BufferedImage newImage = new BufferedImage(bi.getWidth(),
				bi.getHeight(), outType);
		newImage.createGraphics().drawRenderedImage(bi, null);
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
	public static BufferedImage convertImage(Image img, int outType) {
		BufferedImage newImage = new BufferedImage(img.getWidth(null),
				img.getHeight(null), outType);
		newImage.createGraphics().drawImage(img, 0, 0, null);
		return newImage;
	}
	
	/**
	 * Convenience method to create a native platform compatible BufferedImage.  A native image will, in most cases, boost performance.
	 * @param wt width of the image
	 * @param ht height of the image
	 * @param g Graphics object to obtain the device configuration from
	 * @return the newly created BufferedImage object
	 */
	public static BufferedImage getNativeImage(int wt, int ht, Graphics g) {
		return ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(wt, ht, Transparency.TRANSLUCENT);
	}
	
	/**
	 * Convenience method to create a native platform compatible BufferedImage.  A native image will, in most cases, boost performance.
	 * @param wt width of the image
	 * @param ht height of the image
	 * @param g Graphics object to obtain the device configuration from
	 * @return the newly created BufferedImage object
	 */
	public static BufferedImage getNativeImage(Image orig, Graphics g) {
		BufferedImage nimg = ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(orig.getWidth(null), orig.getHeight(null), Transparency.OPAQUE);
		Graphics2D g2 = nimg.createGraphics();
		g2.drawImage(orig, 0, 0, null);
		g2.dispose();
		return nimg;
	}
	
	static int nativeType = Integer.MIN_VALUE;
	
	public static int getNativeImageType(Graphics g) {
		if(nativeType != Integer.MIN_VALUE)
				return nativeType;
		
		BufferedImage nat = ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(1, 1, Transparency.TRANSLUCENT);
		return (nativeType = nat.getType());
	}
	
	/**
	 * Convenience method for creating a new compatible VolatileImage using the given Graphics context.
	 * @param wt the width of the new image
	 * @param ht the height of the new image
	 * @param g the Graphics object to obtain device configuration data from.
	 * @return the newly created, fully compatible VolatileImage.
	 * @see VolatileImage
	 */
	public static VolatileImage createVolatileImage(int wt, int ht, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		return g2d.getDeviceConfiguration().createCompatibleVolatileImage(wt, ht);
	}
	
	/**
	 * Convenience method that calls the validate(...) method on the given VolatileImage using the GraphicsConfiguration returned by
	 * <code>((Graphics2D)g).getDeviceConfiguration()</code>.
	 * @param vi the VolatileImage to be validated.
	 * @param g the Graphics object to use for the GraphicsConfiguration.
	 * @return an integer value specified by the {@link GraphicsConfiguration} class.
	 * @see VolatileImage
	 */
	public static int validateVI(VolatileImage vi, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		return vi.validate(g2d.getDeviceConfiguration());
	}
}
