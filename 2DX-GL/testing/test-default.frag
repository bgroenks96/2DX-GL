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
#version 150
 
// Applies uniform gamma correction to rendered fragments
 
vec3 gamma_func(vec3 rgb);
 
uniform float gamma;

in vec4 color;
out vec4 frag_out;

void main() {
    frag_out.rgb = gamma_func(color.rgb);
    frag_out.a = color.a;
}

vec3 gamma_func(vec3 rgb) {
    return pow(rgb, vec3(1.0/gamma));
}