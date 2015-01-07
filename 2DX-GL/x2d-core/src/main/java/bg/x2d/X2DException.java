/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package bg.x2d;

/**
 * Generic exception thrown by classes in the 2DX software when internal errors
 * occur. This class should not be extended, nor thrown by objects outside of
 * the 2DX API itself.
 * 
 * @author Brian Groenke
 * @since 2DX 1.0 (1st Edition)
 */
public class X2DException extends Exception {

    private static final long serialVersionUID = 975059383220158740L;

    public X2DException() {

    }

    public X2DException(final String arg0) {

        super(arg0);
    }

    public X2DException(final Throwable arg0) {

        super(arg0);
    }

    public X2DException(final String arg0, final Throwable arg1) {

        super(arg0, arg1);
    }

}
