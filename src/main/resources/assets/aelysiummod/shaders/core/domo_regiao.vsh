#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Raio;
uniform vec3 Deslocamento;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 localPos;
out vec3 localNormal;
out float viewDepth;

void main() {
    texCoord = UV0;
    vertexColor = Color;
    vec3 posicao = Position * Raio + Deslocamento;
    localPos = posicao;
    localNormal = normalize(Color.rgb * 2.0 - 1.0);

    vec4 viewPos = ModelViewMat * vec4(posicao, 1.0);
    viewDepth = -viewPos.z;

    gl_Position = ProjMat * viewPos;
}
