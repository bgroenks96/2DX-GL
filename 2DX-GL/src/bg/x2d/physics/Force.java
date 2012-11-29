package bg.x2d.physics;

import bg.x2d.geo.*;

public abstract class Force {
	
	public abstract Vector2f getVecf();
	public abstract Vector2d getVecd();
	public abstract double getNewtonForce();
	public abstract Vector2f applyTo(float time, Vector2f vec);
}
