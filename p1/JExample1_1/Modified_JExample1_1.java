// package br.unicamp.feec.dca.martino;

import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;


/**
 * JExample1_1.java <BR>
 * author: JMario De Martino <P>
 *
 * Java implementation of Example 1.1 of the OpenGL Red Book 8th Edition
 */

public class Modified_JExample1_1 implements GLEventListener {

	private GL4 gl;
	private static int width = 600;
	private static int height = 600;
	
	private int vPosition = 0;

	private final int NUMVAOS = 1; 
	private int[] vaos = new int[NUMVAOS]; 

	private final int NUMBUFFERS = 1;
	private int[] myBuffers = new int[NUMBUFFERS];

	private int numVertices;

	private int shaderProgram;
	private int vertShader;
	private int fragShader;

	public static void main(String[] args) {
		
		System.out.println("main");
		
		// This demo is based on the GL4 GLProfile
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));
		
		// The demo uses the NEWT (Native Windowing Toolkit) Programming Library
		GLWindow glWindow = GLWindow.create(caps);
		
		// Setup the GLWindow
		glWindow.setTitle("Example 1.1 Red Book 8th Edition - adapted to JOGL");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		// Finally we connect the GLEventListener application code to the GLWindow.
		// GLWindow will call the GLEventListener init, reshape, display and dispose functions when needed.
		glWindow.addGLEventListener(new Modified_JExample1_1());
		Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
		
	}

	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		gl = drawable.getGL().getGL4();
		
		System.out.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.out.println("INIT GL IS: " + gl.getClass().getName());
		System.out.println("GL_VENDOR: " + gl.glGetString(GL4.GL_VENDOR));
		System.out.println("GL_RENDERER: " + gl.glGetString(GL4.GL_RENDERER));
		System.out.println("GL_VERSION: " + gl.glGetString(GL4.GL_VERSION));
			
		gl.glGenVertexArrays(NUMVAOS, vaos, 0);
		gl.glBindVertexArray(vaos[0]);

		float[] vertices = {  
				-0.75f, -0.75f, 
				 0f, -0.75f,
				 0f,  0.75f,
				 0.75f, -0.75f,
				 0f,  -0.75f,
				 0f,  0.75f
		};
		
		FloatBuffer verticesFB = Buffers.newDirectFloatBuffer(vertices);
		numVertices = vertices.length / 2;

		gl.glGenBuffers(NUMBUFFERS, myBuffers, 0);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, myBuffers[0]);
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertices.length * (Float.SIZE / Byte.SIZE), verticesFB, GL4.GL_STATIC_DRAW);
		verticesFB = null; // It is OK to release CPU vertices memory after transfer to GPU

		/* The following lines of code until gl.glUseProgram 
		 * is an adaptation of the LoadShaders presented in the red book
		 * Here the function loadShader loads only one shader.
		 * The shader attachment, linking, and installation are done in the code that follows
		 * and not in the LoadShader function.
		 */
		vertShader = loadShader(GL4.GL_VERTEX_SHADER, "./src/triangles.vert");
		fragShader = loadShader(GL4.GL_FRAGMENT_SHADER, "./src/triangles.frag");
		// Each shaderProgram must have one vertex shader and one fragment shader.
		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertShader);
		gl.glAttachShader(shaderProgram, fragShader);

		gl.glLinkProgram(shaderProgram);
		
		// Check link status.
		int[] linked = new int[1];
		gl.glGetProgramiv(shaderProgram, GL4.GL_LINK_STATUS, linked, 0);
		if(linked[0]!=0){System.out.println("Shaders succesfully linked");}
		else {
			int[] logLength = new int[1];
			gl.glGetProgramiv(shaderProgram, GL4.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetProgramInfoLog(shaderProgram, logLength[0], (int[])null, 0, log, 0);

			System.err.println("Error linking shaders: " + new String(log));
			System.exit(1);
		}
		
		gl.glUseProgram(shaderProgram);

		gl.glVertexAttribPointer(vPosition, 2, GL4.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(vPosition);
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		System.out.println("Reshape " + x +"/"  + y +" " + w +"x"+ h);
	}
	
	public void display(GLAutoDrawable drawable) {
		System.out.println("display");
//    	System.out.println("display");

		// Clear screen
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT);

		gl.glBindVertexArray(vaos[0]);

		gl.glDrawArrays(GL4.GL_TRIANGLES, 0, numVertices);		

		gl.glFlush();
	}

	public void dispose(GLAutoDrawable drawable) {
		System.out.println("Dispose");
		System.out.println("cleanup, remember to release shaders");
		gl.glUseProgram(shaderProgram);
		gl.glDetachShader(shaderProgram, vertShader);
		gl.glDeleteShader(vertShader);
		gl.glDetachShader(shaderProgram, fragShader);
		gl.glDeleteShader(fragShader);
		gl.glDeleteProgram(shaderProgram);
		System.exit(0);
	}

	public int loadShader(int type, String filename ) {
		System.out.println("loadShader");
		int shader;

		// Create GPU shader handle
		shader = gl.glCreateShader(type);

		// Read shader file
		String[] vlines = new String[1];
		vlines[0] = "";
		String line;

		try{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while((line = reader.readLine()) != null) {
				vlines[0] += line + "\n";  // insert a newline character after each line
			}
			reader.close();
		} catch (Exception e) {
			System.err.println("Fail reading shader file");
		}

		gl.glShaderSource(shader, vlines.length, vlines, null);

		// Compile shader
		gl.glCompileShader(shader);

		// Check compile status.
		int[] compiled = new int[1];
		gl.glGetShaderiv(shader, GL4.GL_COMPILE_STATUS, compiled, 0);
		if(compiled[0]!=0){System.out.println("Shader succesfully compiled");}
		else {
			int[] logLength = new int[1];
			gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shader, logLength[0], (int[])null, 0, log, 0);

			System.err.println("Error compiling the shader: " + new String(log));
			System.exit(1);
		}

		return shader;
	}

}


