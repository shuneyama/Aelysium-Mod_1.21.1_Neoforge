const int FOG_SHAPE_SPHERICAL = 0;
const int FOG_SHAPE_CYLINDRICAL = 1;

vec4 _linearFog(vec4 fragColor, float fragDistance, vec4 fogColor, float fogStart, float fogEnd) {
#ifdef USE_FOG
    if (fogStart >= 1.0e9) {
        return fragColor;
    }
    float band = fragDistance <= fogStart ? 0.0 : (fragDistance < fogEnd ? smoothstep(fogStart, fogEnd, fragDistance) : 1.0);
    float rain = clamp(fogColor.a - 1.0, 0.0, 1.0);
    float hazeStart = -160.0 * rain;
    float hazeEnd = 1024.0 - 256.0 * rain;
    float haze = clamp((fragDistance - hazeStart) / (hazeEnd - hazeStart), 0.0, 1.0);
    float factor = max(band, haze);
    vec3 blended = mix(fragColor.rgb, fogColor.rgb, factor * min(fogColor.a, 1.0));

    return vec4(blended, fragColor.a);
#else
    return fragColor;
#endif
}

float getFragDistance(int fogShape, vec3 position) {
    switch (fogShape) {
        case FOG_SHAPE_SPHERICAL: return length(position);
        case FOG_SHAPE_CYLINDRICAL: return max(length(position.xz), abs(position.y));
        default: return length(position);
    }
}
