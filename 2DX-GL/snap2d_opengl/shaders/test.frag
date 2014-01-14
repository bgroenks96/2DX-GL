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
 
#version 120

vec3 gamma_func(vec3 rgb);
 
uniform sampler2D tex;
uniform float gamma;
uniform int tex_bound;
uniform int light_count;
uniform vec2 lights[20];
uniform vec3 light_colors[20];
uniform float radius;
uniform float intensity;
uniform float ambient;

const float min_lum = 0.001;

/*
 *const vec2 lights[2] = vec2[2]( vec2(300,300), vec2(900, 600) );
 *const vec3 light_colors[2] = vec3[2]( vec3(1,1,1), vec3(0.9,0.6,0.2) );
 *const float radius = 100, intensity = 1, min_lum = 0.001, ambient = 0;
 */

varying vec4 color;

void main() {
    vec4 rgba;
    if(tex_bound != 0) {
        vec2 uv = gl_TexCoord[0].xy;
        vec4 tex_frag = texture2D(tex, uv);
        rgba = tex_frag;
    } else
        rgba = color;
        
    float dist_max = radius * (sqrt(intensity/min_lum) - 1);
    vec3 ambient_color = vec3(1,1,1);  // ambient color
    vec3 light_sum = ambient_color * ambient;
    
    int i;
    for(i=0; i < light_count; i++) {
        float att = 0;
        vec2 coord = lights[i];
        vec2 diff = gl_FragCoord.xy - coord;
        float dist = length(diff);
        if (dist < dist_max) {
            float lum = intensity / pow(dist / radius + 1, 2);
            att = (lum - min_lum) / (1 - min_lum);
        }
        
        light_sum += light_colors[i] * att;
    }
    
    gl_FragColor.rgb = rgba.rgb * clamp(light_sum, 0, 1);
    gl_FragColor.a = rgba.a;
}



vec3 gamma_func(vec3 rgb) {
    return pow(rgb, vec3(1.0/gamma));
}