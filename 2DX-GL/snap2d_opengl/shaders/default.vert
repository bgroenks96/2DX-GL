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
 
// Standard vertex shader - receives vertex coordinate and texture
// coordinate data for rendering

#version 120 

uniform int tex_bound;

attribute vec2 vert_coord, tex_coord;

varying vec4 color;

void main() {
    vec4 pos = vec4(vert_coord.xy, 0, 1);
    gl_Position = gl_ModelViewProjectionMatrix * pos;
    color = gl_Color;
    if(tex_bound != 0)
        gl_TexCoord[0] = vec4(tex_coord.xy, 0, 1);
}