#version 150

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (fogStart >= 1.0e9) {
        return inColor;
    }

    float band = vertexDistance <= fogStart ? 0.0 : (vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0);
    float rain = clamp(fogColor.a - 1.0, 0.0, 1.0);
    float hazeStart = -160.0 * rain;
    float hazeEnd = 1024.0 - 256.0 * rain;
    float haze = clamp((vertexDistance - hazeStart) / (hazeEnd - hazeStart), 0.0, 1.0);
    return vec4(mix(inColor.rgb, fogColor.rgb, max(band, haze) * min(fogColor.a, 1.0)), inColor.a);
}

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 1.0;
    } else if (vertexDistance >= fogEnd) {
        return 0.0;
    }

    return smoothstep(fogEnd, fogStart, vertexDistance);
}

float fog_distance(vec3 pos, int shape) {
    if (shape == 0) {
        return length(pos);
    } else {
        float distXZ = length(pos.xz);
        float distY = abs(pos.y);
        return max(distXZ, distY);
    }
}
