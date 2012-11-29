package bg.x2d.math;

import static java.lang.Math.*;

public class MathUtils {
	
	public static volatile boolean degrees;
	
	/**
	 * Computes the quadrant the given point lies in based on the origin (0, 0).
	 * @param x x-coord of the point
	 * @param y y-coord of the point
	 * @return int value 1, 2, 3 or 4 representing the point's quadrant based on the origin.
	 */
	public static int quadrant(double x, double y) {
		if(x >= 0 && y>= 0)
			return 1;
		else if(x <= 0 && y >= 0)
			return 2;
		else if(x <= 0 && y <= 0)
			return 3;
		else if(x >= 0 && y <= 0)
			return 4;
		else
			return 0;
	}
	
	/**
	 * Computes the terminal position of an angle using the given x, y coordinates drawn from the origin.
	 * The value returned from this method will be 0-2pi or 0-360 degrees.
	 * @param x
	 * @param y
	 * @return
	 */
	public static double terminal(double x, double y) {
		double ref = PI / 2;
		if(x > 0)
			ref = abs(atan(y / x));
		int quad = quadrant(x, y);
		return chk((quad * (PI / 2)) + ref);
	}
	
	/**
	 * Checks the 'degrees' boolean to see if the given angle should be converted to degrees.
	 * @param angle
	 * @return
	 */
	private static double chk(double angle) {
		if(degrees)
			return toDegrees(angle);
		else
			return angle;
	}
}
