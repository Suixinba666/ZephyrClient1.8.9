#version 120

uniform sampler2D inTexture, textureToCheck;
uniform vec2 texelSize, direction;
uniform float radius, factor;
uniform float weights[256];

#define offset texelSize * direction

void main() {
//    if (direction.y > 0 && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
    vec4 blr = texture2D(inTexture, gl_TexCoord[0].st) * weights[0];
    float check = 1.0f - texture2D(textureToCheck, gl_TexCoord[0].st).a;

    for (float f = 1.0; f <= radius; f++) {
        blr += texture2D(inTexture, gl_TexCoord[0].st + f * offset) * (weights[int(abs(f))]);
        blr += texture2D(inTexture, gl_TexCoord[0].st - f * offset) * (weights[int(abs(f))]);
    }

    gl_FragColor = vec4(blr.rgb * factor, blr.a);
}
