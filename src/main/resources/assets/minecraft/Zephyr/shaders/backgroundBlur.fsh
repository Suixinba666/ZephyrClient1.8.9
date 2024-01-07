#version 130

uniform sampler2D inTexture;
uniform int radius;
uniform float[256] offset;
uniform float factor;
uniform float brightness;
uniform float saturation;
uniform float contrast;
uniform vec2 offsetDir;
uniform vec2 texelSize;

mat4 brightnessMatrix( float brightness )
{
    return mat4( 1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    brightness, brightness, brightness, 1 );
}

mat4 contrastMatrix( float contrast )
{
    float t = ( 1.0 - contrast ) / 2.0;

    return mat4( contrast, 0, 0, 0,
    0, contrast, 0, 0,
    0, 0, contrast, 0,
    t, t, t, 1 );

}

mat4 saturationMatrix( float saturation )
{
    vec3 luminance = vec3( 0.3086, 0.6094, 0.0820 );

    float oneMinusSat = 1.0 - saturation;

    vec3 red = vec3( luminance.x * oneMinusSat );
    red+= vec3( saturation, 0, 0 );

    vec3 green = vec3( luminance.y * oneMinusSat );
    green += vec3( 0, saturation, 0 );

    vec3 blue = vec3( luminance.z * oneMinusSat );
    blue += vec3( 0, 0, saturation );

    return mat4( red,     0,
    green,   0,
    blue,    0,
    0, 0, 0, 1 );
}

vec4 filtted(sampler2D tex, vec2 uv) {
    vec4 color = texture2DLod(tex, uv, 0) * brightnessMatrix(brightness) * contrastMatrix(contrast) * saturationMatrix(saturation);
    return vec4(color.rgb * factor, color.a);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    if (radius == 0 || radius == 1) {
        gl_FragColor = filtted(inTexture, uv);
        return;
    }
    vec4 color = filtted(inTexture, uv) * offset[0];

    for (int f = 1; f <= radius; f++) {
        color += filtted(inTexture, uv + f * texelSize * offsetDir) * (offset[int(abs(f))]);
        color += filtted(inTexture, uv - f * texelSize * offsetDir) * (offset[int(abs(f))]);
    }
    gl_FragColor = color;
}