package bg.x2d.physics;

import bg.x2d.geo.*;

public interface PhysicsNode {
	
	public double x = 0, y = 0;
	
	public double getVelocity();
	public void setVelocity(Vector2f vec);
	public void setVelocity(Vector2d vec);
	public Vector2f collideWith(Vector2f vec);
	public Vector2d collideWith(Vector2d vec);
	public Vector2f applyForces(float time, Force... f);
	public Vector2d applyForces(double time, Force... f);
}
