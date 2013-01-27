/*
 * Copyright © 2011-2013 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d.anim;

import java.awt.geom.*;

/**
 * Holds data used by Segments when running in an ongoing animation.
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class Transform {

	public double rotation;
	public Point2D.Double rotationAnchor = new Point2D.Double();
}
