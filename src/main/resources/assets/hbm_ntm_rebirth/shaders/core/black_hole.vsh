#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

out vec2 texCoord;

void main()
{
    gl_Position = vec4(Position, 1.0);
    texCoord = Position.xy * 0.5 + 0.5;
}
