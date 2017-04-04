//package br.unicamp.feec.dca.martino;

import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

public final class JAnimatedTriangle implements GLEventListener {
	
    private GL4 gl;
    
    private static int width=600;
    private static int height=600;
    private float step = 0.0f;
    
    private int shaderProgram;
    private int vertShader;
    private int fragShader;
    
	public static void main(String[] s) {
		
		// This demo is based on the GL4 GLProfile 
    	GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));
    	
		// The demo uses the NEWT (Native Windowing Toolkit) Programming Library
    	GLWindow glWindow = GLWindow.create(caps);

    	// Setup the GLWindow
    	glWindow.setTitle("Animated Triangle - JOGL");
    	glWindow.setSize(width,height);
    	glWindow.setUndecorated(false);
    	glWindow.setPointerVisible(true);
    	glWindow.setVisible(true);

		// Finally we connect the GLEventListener application code to the GLWindow.
		// GLWindow will call the GLEventListener init, reshape, display and dispose functions when needed.
    	glWindow.addGLEventListener(new JAnimatedTriangle());
    	Animator animator = new Animator();
    	animator.add(glWindow);
    	animator.start();
    }
	
    public void init(GLAutoDrawable drawable) {
    	System.out.println("init");

    	gl = drawable.getGL().getGL4();
    	
       	// Create GPU shader handles
    	vertShader = loadShader(GL4.GL_VERTEX_SHADER, "./src/shader.vert");
    	fragShader = loadShader(GL4.GL_FRAGMENT_SHADER, "./src/shader.frag");
    	//Each shaderProgram must have one vertex shader and one fragment shader.
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
    }
    
    public void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
    	System.out.println("reshape - Window resized to width=" + z + " height=" + h);
    	width = z;
    	height = h;

    	// Optional: Set viewport
    	// Render to a square at the center of the window.
    	gl.glViewport((width-height)/2, 0, height, height);
    }
	
    public void display(GLAutoDrawable drawable) {
    	System.out.println("display "+ step);
    	float[] bg = {0.0f, 0.0f, 1.0f, 1.0f};
    	gl.glClearBufferfv(GL4.GL_COLOR, 0, bg, 0);
    	
    	float[] offset = {(float) Math.sin(step) * 0.5f, 
    					  (float) Math.cos(step) *
    					  0.6f, 
    					  0.0f, 
    					  0.0f};
    	int offsetLoc = gl.glGetAttribLocation(shaderProgram, "offset");
    	gl.glVertexAttrib4fv(offsetLoc, offset, 0);
    	
    	float[] color = {1.0f, 
    					(float) (Math.sin(step) + 1) * 0.5f, 
    					(float) (Math.cos(step) + 1) * 0.5f, 
    					1.0f};
    	int colorLoc = gl.glGetAttribLocation(shaderProgram, "color");
    	gl.glVertexAttrib4fv(colorLoc, color, 0);
    	gl.glDrawArrays(GL4.GL_TRIANGLES, 0, 3);
    	
    	step += 0.01;
    	}
    
    public void dispose(GLAutoDrawable drawable) {
		System.out.println("Dispose");
		System.out.println("cleanup, remember to release shaders");
		gl.glUseProgram(0);
		gl.glDetachShader(shaderProgram, vertShader);
		gl.glDeleteShader(vertShader);
		gl.glDetachShader(shaderProgram, fragShader);
		gl.glDeleteShader(fragShader);
		gl.glDeleteProgram(shaderProgram);
		System.exit(0);
    	System.exit(0);
    }
    
    public int loadShader(int type, String filename ) {
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
