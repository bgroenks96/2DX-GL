package bg.x2d.physics;

import bg.x2d.geo.*;

public abstract class Force {
	
	public abstract Vector2f getVecf();
	public abstract Vector2d getVecd();
	public abstract double getNewtonForce(double mass);
	public abstract Vector2f applyTo(float time, float mass, Vector2f vec);
	public abstract Vector2d applyTo(double time, double mass, Vector2d vec);
}
