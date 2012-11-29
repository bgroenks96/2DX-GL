package bg.x2d.geo;

import static bg.x2d.math.MathUtils.*;
import static java.lang.Math.*;

import java.awt.geom.*;

public class Vector2d {
	
	public double x, y, mag, angle;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param angle
	 */
	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
		
		init();
	}
	
	public Vector2d(Vector2d copy) {
		x = copy.x;
		y = copy.y;
		mag = copy.mag;
		angle = copy.angle;
	}
	
	private void init() {
		mag = sqrt(pow(x, 2) + pow(y, 2));
		angle = terminal(x, y);
	}
	
	public double degs() {
		return toDegrees(angle);
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2d add(Vector2d arg) {
		x += arg.x;
		y += arg.y;
		init();
		return this;
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2d sub(Vector2d arg) {
		x -= arg.x;
		y -= arg.y;
		init();
		return this;
	}
	
	public Vector2d mult(double factor) {
		x *= factor;
		y *= factor;
		init();
		return this;
	}
	
	public Vector2d div(double factor) {
		x /= factor;
		y /= factor;
		init();
		return this;
	}
	
	public Vector2d addNew(Vector2d arg) {
		return new Vector2d(x += arg.x, y+= arg.y);
	}
	
	public Vector2d subNew(Vector2d arg) {
		return new Vector2d(x -= arg.x, y-= arg.y);
	}
	
	public Vector2d multNew(double factor) {
		return new Vector2d(this).mult(factor);
	}
	
	public Vector2d divNew(double factor) {
		return new Vector2d(this).div(factor);
	}
	
	/**
	 * 
	 * @param arg
	 * @return
	 */
	public double dot(Vector2d arg) {
		return (mag * arg.mag * cos(angleBetween(arg)));
	}
	
	public double det(Vector2d arg) {
		return (x * arg.y - y * arg.x);
	}
	
	public double cross(Vector2d arg) {
		return det(arg);
	}
	
	public double angleBetween(Vector2d vec) {
		return abs(vec.angle - angle);
	}
	
	public Vector2d interpolate(Vector2d vec, double alpha) {
		return this.mult(1 - alpha).add(vec.multNew(alpha));
	}
	
	/**
	 * Rotates this vector by given radians.
	 * @param angle rotation value IN RADIANS
	 * @return this vector for chain calls.
	 */
	public Vector2d rotate(float rads) {
		Point2D p = GeoUtils.rotatePoint(new PointLD(x,y), new PointLD(0,0), toDegrees(rads));
		this.x = p.getX();
		this.y = p.getY();
		init();
		return this;
	}
	
	/**
	 * 
	 * @param theta
	 * @return
	 */
	public Vector2d rotateNew(float theta) {
		Point2D p = GeoUtils.rotatePoint(new PointLD(x,y), new PointLD(0,0), toDegrees(theta));
		double x = p.getX();
		double y = p.getY();
		return new Vector2d(x, y);
	}
	
	public Vector2d negate() {
		x = -x;
		y = -y;
		init();
		return this;
	}
	
	public Vector2d negateNew() {
		double x = -this.x;
		double y = -this.y;
		return new Vector2d(x,y);
	}
	
	public final void normalize() {
		double norm;
		norm = (1.0/Math.sqrt(this.x * this.x + this.y * this.y));
		this.x *= norm;
		this.y *= norm;
	}
}
