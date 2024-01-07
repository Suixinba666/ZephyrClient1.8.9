#version 130

uniform sampler2D mixTexture;
uniform sampler2D stencilTexture;
uniform float factor;
uniform float alpha;

void main() {
    float s = 1.0f - texture2D(stencilTexture, gl_TexCoord[0].st).a;
    vec4 color = texture2D(mixTexture, gl_TexCoord[0].st);
    gl_FragColor = vec4(color.rgb * factor, color.a * s * alpha);
}