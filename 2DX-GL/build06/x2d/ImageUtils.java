package bg.x2d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;

/**
 * Provides static convenience methods for performing geometric operations on images.
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class ImageUtils {
	
	public static AffineTransform rotateImage(Image img, Point location, double degrees) {
		AffineTransform affine = new AffineTransform();
		affine.rotate(degrees, location.getX()/2, location.getY());
		return affine;
	}
	
	public static AffineTransform rotateImage(Image img, Point location, Graphics2D g2d, double degrees) {
		AffineTransform affine = new AffineTransform();
		affine.rotate(degrees, location.getX()/2, location.getY());
		g2d.drawImage(img, affine, null);
		return affine;
	}
}
