 package bg.x2d.anim;

import java.awt.Graphics2D;
import java.util.concurrent.ExecutorService;


/**
 * Defines a standard for animation engines inside and outside of the 2DX library.<br>
 * Note: Java 5.0 or higher required as the java.util.concurrent class is utilized.
 *@since 2DX 1.0 (1st Edition)
 */

public interface Animator {

	public ExecutorService getThread();
	public void drawFrame(Graphics2D g);
}
