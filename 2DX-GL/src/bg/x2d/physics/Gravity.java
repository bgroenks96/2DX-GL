package bg.x2d.physics;

import bg.x2d.geo.*;

public class Gravity extends Force {
	
	public static final float STANDARD = 9.8f;
	
	private float ga;
	private Vector2f vecf;
	private Vector2d vecd;
	
	public Gravity(float mpss) {
		ga = mpss;
		initVectors();
	}
	
	private void initVectors() {
		vecf = new Vector2f(0, ga);
		vecd = new Vector2d(0, ga);
	}
	
	public Gravity() {
		this(STANDARD);
	}

	@Override
	public Vector2f getVecf() {
		return vecf;
	}

	@Override
	public Vector2d getVecd() {
		return vecd;
	}

	/**
	 * F = ma
	 * The newton force exerted by gravity depends on the mass of the object.
	 * Therefore, the force (in N) is determined by multiplying the given
	 * mass value by the value of gravity's acceleration.
	 * @param mass mass of the object in Kg (required).
	 * @return the force exerted on the object wit the given mass in Newtons (N).
	 */
	@Override
	public double getNewtonForce(double mass) {
		return ga * mass;
	}

	/**
	 * Applies the forces of this Gravity object to the given Vector2f.
	 * @param time seconds to accelerate the given vector
	 * @param mass of the object in Kg.  Irrelevant for gravitational force.
	 * @param vec velocity vector to be accelerated by Gravity.
	 * @return the modified Vector2f object (allows for chain calls).
	 */
	@Override
	public Vector2f applyTo(float time, float mass, Vector2f vec) {
		vec.add(vecf.multNew(time));
		return vec;
	}

	/**
	 * Applies the forces of this Gravity object to the given Vector2d.
	 * @param time seconds to accelerate the given vector (assumed to be
	 * velocity).
	 * @param mass of the object in Kg.  Irrelevant for gravitational force.
	 * @param vec velocity vector to be accelerated by Gravity.
	 * @return the modified Vector2d object (allows for chain calls).
	 */
	@Override
	public Vector2d applyTo(double time, double mass, Vector2d vec) {
		vec.add(vecd.multNew(time));
		return vec;
	}

}
