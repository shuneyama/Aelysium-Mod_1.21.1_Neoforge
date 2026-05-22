#version 150
uniform sampler2D DiffuseSampler;
uniform float AberrationAmount;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 dir = texCoord - vec2(0.5);
    float dist = length(dir);
    vec2 offset = dir * (AberrationAmount + dist * AberrationAmount);
    vec2 clampedCoord = clamp(texCoord + offset, 0.001, 0.999);

    vec4 original = texture(DiffuseSampler, texCoord);
    vec4 shifted  = texture(DiffuseSampler, clampedCoord);

    vec3 cyan = vec3(0.03, 0.39, 0.49);
    vec3 diff = abs(shifted.rgb - original.rgb);
    vec3 ghost = diff * cyan * 1.5;
    vec3 result = original.rgb + ghost;

    float fogStrength = smoothstep(0.65, 1.0, dist * 1.2);
    vec3 fogColor = vec3(0.008, 0.251, 0.314);
    result = mix(result, fogColor, fogStrength * 0.15);

    fragColor = vec4(result, original.a);
}