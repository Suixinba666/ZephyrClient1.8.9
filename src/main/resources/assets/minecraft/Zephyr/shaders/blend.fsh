#version 130

uniform sampler2D mixTexture;
uniform sampler2D stencilTexture;
uniform float brightness;
uniform float saturation;
uniform float contrast;

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

void main() {
    vec4 oc = brightnessMatrix( brightness ) *
              contrastMatrix( contrast ) *
              saturationMatrix( saturation ) *
              texture2D(mixTexture, gl_TexCoord[0].st);
    float s = texture2D(stencilTexture, gl_TexCoord[0].st).a;
    gl_FragColor = vec4(oc.rgb, oc.a * s);
}