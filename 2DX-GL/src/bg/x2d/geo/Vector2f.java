package bg.x2d.geo;

import static java.lang.Math.*;

import java.awt.geom.*;

/**
 * 
 * @author brian
 * @since 2DX 1.0
 */
public class Vector2f {

	public volatile float x, y, mag, angle;

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;

		init();
	}

	public Vector2f(Vector2f copy) {
		x = copy.x;
		y = copy.y;
		mag = copy.mag;
		angle = copy.angle;
	}

	private void init() {
		mag = (float) sqrt(pow(x, 2) + pow(y, 2));
		angle = (float) GeoUtils.terminal(x, y);
	}

	public float degs() {
		return (float) toDegrees(angle);
	}

	/**
	 * 
	 * @param arg
	 * @return
	 */
	public Vector2f add(Vector2f arg) {
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
	public Vector2f sub(Vector2f arg) {
		x -= arg.x;
		y -= arg.y;
		init();
		return this;
	}

	public Vector2f mult(float factor) {
		x *= factor;
		y *= factor;
		init();
		return this;
	}

	public Vector2f div(float factor) {
		x /= factor;
		y /= factor;
		init();
		return this;
	}

	public Vector2f addNew(Vector2f arg) {
		return new Vector2f(x += arg.x, y+= arg.y);
	}

	public Vector2f subNew(Vector2f arg) {
		return new Vector2f(x -= arg.x, y-= arg.y);
	}

	public Vector2f multNew(float factor) {
		return new Vector2f(this).mult(factor);
	}

	public Vector2f divNew(float factor) {
		return new Vector2f(this).div(factor);
	}

	/**
	 * 
	 * @param arg
	 * @return
	 */
	public float dot(Vector2f arg) {
		return (float) (mag * arg.mag * cos(angleBetween(arg)));
	}

	public float det(Vector2f arg) {
		return (float) (x * arg.y - y * arg.x);
	}

	public float cross(Vector2f arg) {
		return det(arg);
	}

	public final void normalize() {
		float norm;
		norm = (float) 
				(1.0/Math.sqrt(this.x * this.x + this.y * this.y));
		this.x *= norm;
		this.y *= norm;
	}

	public float angleBetween(Vector2f vec) {
		return abs(vec.angle - angle);
	}

	public Vector2f interpolate(Vector2f vec, float alpha) {
		return this.mult(1 - alpha).add(vec.multNew(alpha));
	}

	/**
	 * Rotates this vector by given radians.
	 * @param angle rotation value IN RADIANS
	 * @return this vector for chain calls.
	 */
	public Vector2f rotate(float rads) {
		Point2D p = GeoUtils.rotatePoint(new PointLD(x,y), new PointLD(0,0), toDegrees(rads));
		this.x = (float) p.getX();
		this.y = (float) p.getY();
		init();
		return this;
	}

	/**
	 * 
	 * @param theta
	 * @return
	 */
	public Vector2f rotateNew(float theta) {
		Point2D p = GeoUtils.rotatePoint(new PointLD(x,y), new PointLD(0,0), toDegrees(theta));
		float x = (float) p.getX();
		float y = (float) p.getY();
		return new Vector2f(x, y);
	}

	public Vector2f negate() {
		x = -x;
		y = -y;
		init();
		return this;
	}

	public Vector2f negateNew() {
		float x = -this.x;
		float y = -this.y;
		return new Vector2f(x,y);
	}
	
	public Point2D.Float applyTo(Point2D.Float p, float multiplier) {
		return new Point2D.Float(p.x + (x * multiplier), p.y + (y * multiplier));
	}
}
