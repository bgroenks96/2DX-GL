// transform.vert - Snap2D GLSL Vertex Transformation Utility
/*
 *  Copyright (C) 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */
#if __FILE__ == 0
    #version 130
#endif

uniform mat4 mOrtho;
uniform vec2 vTranslate;
uniform vec2 vScale;
uniform float vRotate;

void transform(vec4 vertex) {
    mat4 mTranslation = mat4( 1, 0, 0, vTranslate.x,
                           0, 1, 0, vTranslate.y,
                           0, 0, 1, 0,
                           0, 0, 0, 1);
    float c = cos(vRotate);
    float s = sin(vRotate);
    mat4 mRotation = mat4( c, -s, 0, 0,
                          s, c, 0, 0,
                          0, 0, (1-c)+c, 0,
                          0, 0, 0, 1 );
    mat4 mScale = mat4( vScale.x, 0, 0, 0,
                       0, vScale.y, 0, 0,
                       0, 0, 1, 0,
                       0, 0, 0, 1 );
    mat4 mvp = mOrtho * mTranslation * mRotation * mScale;
    gl_Position = mvp * vertex;
}

// --------------------------------------------------------------

