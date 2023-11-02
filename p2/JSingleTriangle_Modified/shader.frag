#version 400 core
in vec4 varying_Color; // incomming varying data to the
		// fragment shader
		// sent from the vertex shader

void main (void) {
	gl_FragColor = varying_Color;
}