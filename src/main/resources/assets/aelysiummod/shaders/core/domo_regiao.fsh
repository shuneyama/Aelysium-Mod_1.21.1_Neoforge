#version 150

uniform float DistanciaFade;
uniform vec3 BaseColor;

in vec2 texCoord;
in vec4 vertexColor;
in vec3 localPos;
in vec3 localNormal;
in float viewDepth;

out vec4 fragColor;

float saturate(float x) { return clamp(x, 0.0, 1.0); }

void main() {
    vec3 n = normalize(localNormal);
    vec3 viewDir = normalize(-localPos);
    float fresnel = pow(1.0 - saturate(dot(n, viewDir)), 2.2);

    vec3 color = mix(BaseColor * 0.6, BaseColor * 1.3, fresnel);
    float alpha = saturate(0.28 + fresnel * 0.35);

    float distFade = smoothstep(DistanciaFade * 0.6, DistanciaFade, viewDepth);
    alpha *= (1.0 - distFade);

    if (alpha < 0.01) discard;

    fragColor = vec4(color, alpha) * vec4(1.0, 1.0, 1.0, vertexColor.a);
}
