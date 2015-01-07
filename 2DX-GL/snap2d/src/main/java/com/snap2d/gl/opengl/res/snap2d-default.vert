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
 
// Standard vertex shader - receives vertex coordinate and texture
// coordinate data for rendering

uniform int tex_bound;

out vec4 color_vs, tex_vs;

void main() {
    vec4 vert = vec4(vert_coord.xy, 0, 1);
    transform(vert);
    color_vs = vert_color;
    if(tex_bound != 0)
        tex_vs = vec4(tex_coord.xy, 0, 1);
}