package bg.x2d.geo;

import java.awt.Polygon;
import java.awt.Rectangle;

import bg.x2d.gen.Generator;
import bg.x2d.gen.NumberGenerator;

/**
 * Generates a random polygon with n sides and inside of the specified Rectangle.
 * 
 * @author Brian
 * @since 2DX 1.0 (1st Edition)
 */
public class PolyGenerator implements Generator<Polygon> {
	
	private int x,y,width,height, n;
	private NumberGenerator<Integer> xgen, ygen;
	
	public PolyGenerator(Rectangle bounds, int nsides) {
		if(bounds !=null) {
			x = (int) Math.round(bounds.getX());
			y = (int) Math.round(bounds.getY());
			width = (int) Math.round(bounds.getWidth());
			height = (int) Math.round(bounds.getHeight());
		} else {
			x = 0;
			y = 0;
			width = 10;
			height = 10;
		}
		n = nsides;
		xgen = new NumberGenerator<Integer>(x,x+width);
		ygen = new NumberGenerator<Integer>(y,y+height);
	}

	@Override
	public Polygon generate() {
		Polygon p = new Polygon();
		
		for(int i=n;i>0;i--) {
			int x = xgen.generate();
			int y = ygen.generate();
			p.addPoint(x,y);
		}
		return p;
	}

}
