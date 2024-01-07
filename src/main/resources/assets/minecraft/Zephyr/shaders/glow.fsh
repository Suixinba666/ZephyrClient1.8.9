#version 120

uniform sampler2D inTexture;
uniform int radius;
uniform float alpha;
uniform float[256] offset;
uniform vec2 offsetDir;
uniform vec2 texelSize;
uniform vec3 bloomColor;

void main() {
//    if (offsetDir.y > 0 && texture2D(inTexture, gl_TexCoord[0].st).a != 0.0) discard;
    float glow = texture2D(inTexture, gl_TexCoord[0].st).a * offset[0];

    for (float f = 1.0; f <= radius; f++) {
        glow += texture2D(inTexture, gl_TexCoord[0].st + f * texelSize * offsetDir).a * (offset[int(abs(f))]);
        glow += texture2D(inTexture, gl_TexCoord[0].st - f * texelSize * offsetDir).a * (offset[int(abs(f))]);
    }
    gl_FragColor = vec4(bloomColor, glow * alpha);
}