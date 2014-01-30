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
 
#define MAX_LIGHTS 20

uniform int tex_bound;

uniform sampler2D tex;
uniform int light_count;
uniform vec2 lights[MAX_LIGHTS];
uniform vec3 light_colors[MAX_LIGHTS];
uniform float radius[MAX_LIGHTS];
uniform float intensity[MAX_LIGHTS];
uniform float ambient;

const float min_lum = 0.001;

in vec4 vert_color;
in vec2 vert_coord, tex_coord;

out vec4 color, tex_out;
out float light_max_dist[MAX_LIGHTS];

void main() {
    vec4 vert = vec4(vert_coord.xy, 0, 1);
    transform(vert);
    color = vert_color;
    tex_out = vec4(tex_coord.xy, 0, 1);
        
    for(int i=0; i < light_count; i++) {
        light_max_dist[i] = radius[i] * (sqrt(intensity[i]/min_lum) - 1);
    }
}