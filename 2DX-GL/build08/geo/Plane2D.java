/**
 * 
 */
package bg.x2d.geo;

import java.awt.Component;
import java.awt.geom.Point2D;

/**
 * A configurable coordinate plane that translates its point locations to the underlying pixel grid.  Plane2D allows custom coordinate grids to be created with
 * a specific scale, size and type.  1,2, or 4-quadrant planes can be used, also allowing use of negative numbers in any given point.
 * The process is backed by a 2-dimensional Coordinate array.
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class Plane2D {
	
	public Plane2D(int x, int y, int width, int height, int ppu) {
		new Coordinate(5.0,5.0);
	}
	
	public Plane2D(Component c, int ppu) {
		
	}
	
	
	public static class Coordinate extends Point2D.Double {
		
		private static final long serialVersionUID = -5246275691815572910L;
		
		public Coordinate(double x, double y) {
			super(x,y);
		}
		
	}

}
