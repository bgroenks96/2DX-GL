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
#version 130
 
// Applies uniform gamma correction to rendered fragments
 
vec3 gamma_func(vec3 rgb);
 
uniform sampler2D tex;
uniform float gamma;
uniform int tex_bound;

varying vec4 color, tex_coord;

void main() {
    if(tex_bound != 0) {
        vec2 uv = tex_coord.xy;
        vec4 tex_frag = texture2D(tex, uv);
        gl_FragColor.rgb = gamma_func(tex_frag.rgb);
        gl_FragColor.a = tex_frag.a;
    } else {
        gl_FragColor.rgb = gamma_func(color.rgb);
        gl_FragColor.a = color.a;
    }
}

vec3 gamma_func(vec3 rgb) {
    return pow(rgb, vec3(1.0/gamma));
}