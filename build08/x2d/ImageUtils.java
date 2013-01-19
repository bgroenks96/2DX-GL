package bg.x2d;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Provides static convenience methods for performing geometric operations on images.
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class ImageUtils {
	
	public static AffineTransform rotateImage(Image img, Point location, double degrees) {
		AffineTransform affine = new AffineTransform();
		affine.setToTranslation(location.getX(), location.getY());
		affine.rotate(Math.toRadians(degrees), img.getWidth(null)/2, img.getHeight(null)/2);
		return affine;
	}
	
	public static AffineTransform rotateImage(Image img, Point location, Graphics2D g2d, double degrees) {
		AffineTransform affine = new AffineTransform();
		affine.setToTranslation(location.getX(), location.getY());
		affine.rotate(Math.toRadians(degrees), img.getWidth(null)/2, img.getHeight(null)/2);
		g2d.drawImage(img, affine, null);
		return affine;
	}
	
	/**
	 * Performs a AffineTransform scaling operation relative to the generic Image provided.  The returned AffineTransform can be used to draw a scaled version of the Image on screen.
	 * High quality is not guaranteed.
	 * @param img The Image to be scaled.
	 * @param location The location the Image should be drawn at.
	 * @param newSize The dimensions the scaled Image should be.
	 * @return a scaled and translated AffineTransform that can be used to draw a scaled version of the Image.
	 */
	public static AffineTransform scaleImage(Image img, Point location, Dimension newSize) {
		AffineTransform at = new AffineTransform();
		at.setToTranslation(location.getX(), location.getY());
		at.scale((double)newSize.getWidth()/(double)img.getWidth(null), (double)newSize.getHeight()/(double)img.getHeight(null));
		return at;
	}
	
	/**
	 * Takes any generic Image and scales it to a new size.  The returned Image is a casted BufferedImage upon which the scaled image is drawn upon.
	 * This method calls upon the Image class's <code>getScaledInstance(...)</code> method to scale the passed Image object.
	 * Smooth scaling is used and the BufferedImage created is of TYPE_INT_ARGB.
	 * @param img Any generic Image or Image subclass to be scaled (result image may not be castable).
	 * @param newSize the dimensions for the new Image.
	 * @return an Image casted from a BufferedImage object.
	 */
	public static Image scaleImage(Image img, Dimension newSize) {
		BufferedImage bi = new BufferedImage((int)newSize.getWidth(),(int)newSize.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Image scaled = img.getScaledInstance(bi.getWidth(),bi.getHeight(),Image.SCALE_SMOOTH);
		bi.getGraphics().drawImage(scaled, 0, 0, null);
		return (Image) bi;
	}
	
	public static BufferedImage scaleBufferedImage(BufferedImage bi, Dimension newSize, int imgType, int scaleMode) {
		BufferedImage newImage = new BufferedImage((int)newSize.getWidth(),(int)newSize.getHeight(),imgType);
		Image scaled = bi.getScaledInstance(newImage.getWidth(),newImage.getHeight(),scaleMode);
		newImage.getGraphics().drawImage(scaled, 0, 0, null);
		return newImage;
	}
}
