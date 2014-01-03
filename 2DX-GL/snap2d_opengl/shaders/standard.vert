/*
 *  Copyright © 2012-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */
 
// Standard vertex shader - simply applies transform and assigns gl_FrontColor
// to the value of gl_Color for use in frag shader as well as gl_TexCoord.

uniform int tex_bound;
varying vec4 color;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    color = gl_Color;
    if(tex_bound != 0)
        gl_TexCoord[0] = gl_MultiTexCoord0;
}