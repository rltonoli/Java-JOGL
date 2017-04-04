#version 400 core

in vec4 attribute_Position; // vertex shader
in vec4 attribute_Color; // attributes
out vec4 varying_Color; // Outgoing varying data
				// sent to the fragment shader

void main(void) {
	varying_Color = attribute_Color;
	gl_Position = attribute_Position;
}