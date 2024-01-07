#version 130

uniform sampler2D inTexture;
uniform float radius;
uniform vec2 offsetDir;
uniform vec2 texelSize;

const float PI = 3.14159265358;

float calculateGaussianValue(float x, float sigma) {
    return exp(-0.5 * x * x / (sigma * sigma));
}

void main() {
    vec2 uv = gl_TexCoord[0].st;
    if (radius <= 1) {
        gl_FragColor = texture2D(inTexture, uv);
        return;
    }

    vec4 color = vec4(0.0);
    float total =  0;
    for (int f = 0; f <= int(radius + 0.5); f++) {
        float gaussian = calculateGaussianValue(f, radius / 2.0);
        total += gaussian;
        color += texture2D(inTexture, uv + f * texelSize * offsetDir) * gaussian;
        color += texture2D(inTexture, uv - f * texelSize * offsetDir) * gaussian;
    }
    gl_FragColor = color / total / 2.0f;
}