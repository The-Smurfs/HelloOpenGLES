#version 300 es

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec4 aColor;
layout(location = 2) in vec2 aTexCoord;

uniform mat4 uMVPMatrix;

out vec4 outColor;
out vec2 outTexCoord;

void main()
{
    outColor = aColor;
    outTexCoord = aTexCoord;
    gl_Position = uMVPMatrix*vec4(aPosition, 1.0);
}