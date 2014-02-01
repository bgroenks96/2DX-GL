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
 
 // Applies uniform gamma correction to rendered fragments
 
#version 130
#define MAX_LIGHTS 30

uniform int tex_bound;

uniform sampler2D tex;
uniform int light_count;
uniform vec2 lights[MAX_LIGHTS];
uniform vec3 light_colors[MAX_LIGHTS];
uniform float radius[MAX_LIGHTS];
uniform float intensity[MAX_LIGHTS];
uniform float ambient;
uniform vec3 ambient_color;

in vec4 color, tex_out;
in float light_max_dist[MAX_LIGHTS];

out vec4 gl_FragColor;

void main() {
    vec4 rgba;
    if(tex_bound != 0) {
        vec2 uv = tex_out.xy;
        vec4 tex_frag = texture2D(tex, uv);
        rgba = tex_frag;
    } else
        rgba = color;
        
    vec3 light_sum = ambient_color * ambient;
    
    for(int i=0; i < light_count; i++) {
        float max_dist = light_max_dist[i];
        float r = radius[i];
        float v = intensity[i];
        float att = 0;
        vec2 coord = lights[i];
        vec2 diff = gl_FragCoord.xy - coord;
        float dist = length(diff);
        if(dist < max_dist) {
            att += v / pow(dist / r + 1, 2);
        }
        
        light_sum += light_colors[i] * att * (1-ambient);
    }
    
    gl_FragColor.rgb = rgba.rgb * light_sum;
    gl_FragColor.a = rgba.a;
}