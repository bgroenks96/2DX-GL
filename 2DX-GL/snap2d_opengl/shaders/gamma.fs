/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */
 
 // Applies uniform gamma correction to rendered fragments
 
uniform sampler2D tex;
uniform float gamma;
uniform int tex_bound;

void main() {
    vec2 uv = gl_TexCoord[0].xy;
    vec3 color = (tex_bound) ? texture2D(sceneBuffer, uv).rgb : gl_FrontColor.rgb;
    gl_FragColor.rgb = pow(color, vec3(1.0 / gamma));
    gl_FragColor.a = 1.0;
}