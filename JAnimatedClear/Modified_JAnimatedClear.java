//package br.unicamp.feec.dca.martino;

import com.jogamp.opengl.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.*;

/**
 * JAnimatedClear.java <BR>
 * author: JMario De Martino <P>
 *
 * Java adaptation of example (listing) 2.2 of the Book "OpenGL SuperBible 6th Edition".
 */

public final class Modified_JAnimatedClear implements GLEventListener {

	private GL4 gl;

	private static int width=600;
	private static int height=600;

	private static float inc = 0.005f;
	private float r = 1.0f;
	private float g = 0.0f;
	private float b = 0.0f;
	private float rinc = 0.0f; 
	private float ginc = inc; 
	private float binc = inc; 

	public static void main(String[] s) {
		System.out.println("main");

		// This demo is based on the GL4 GLProfile.
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));
		// The demo uses the NEWT (Native Windowing Toolkit) Programming Library
		GLWindow glWindow = GLWindow.create(caps);

		// Setup the GLWindow 
		glWindow.setTitle("Animated Simple Clear - JOGL");
		glWindow.setSize(width,height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		// Finally we connect the GLEventListener application code to the GLWindow.
		// GLWindow will call the GLEventListener init, reshape, display and dispose functions when needed.
		glWindow.addGLEventListener(new Modified_JAnimatedClear() /* GLEventListener */);
		Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
	}

	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		gl = drawable.getGL().getGL4();

//		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
//		System.err.println("INIT GL IS: " + gl.getClass().getName());
//		System.err.println("GL_VENDOR: " + gl.glGetString(GL4.GL_VENDOR));
//		System.err.println("GL_RENDERER: " + gl.glGetString(GL4.GL_RENDERER));
//		System.err.println("GL_VERSION: " + gl.glGetString(GL4.GL_VERSION));
	}

	public void display(GLAutoDrawable drawable) {
		// System.out.println("display");
		//float[] color = {r, g, b, 1.0f};
		float[] color = {g+b, r+b, r+g, 1.0f};
		gl.glClearBufferfv(GL4.GL_COLOR, 0, color, 0);
		r += rinc;
		g += ginc;
		b += binc;
		if( g >= 1.0 && rinc == 0.0f) {
			r = 0.0f;
			rinc = inc;
			g = 1.0f;
			ginc = 0.0f;
			b = 0.0f;
		} else if (b >= 1.0f && ginc == 0.0f) {
			r = 0.0f;
			g = 0.0f;
			ginc = inc;
			b = 1.0f;
			binc = 0.0f;
		} else if(r >= 1.0f && binc == 0.0f) {
			r = 1.0f;
			rinc = 0.0f;
			g = 0.0f;
			b = 0.0f;
			binc = inc;    			
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
		System.out.println("reshape - Window resized to width=" + z + " height=" + h);
		width = z;
		height = h;

		// Set viewport
		// Render to a square at the center of the window.
		gl.glViewport((width-height)/2,0,height,height);
	}

	public void dispose(GLAutoDrawable drawable){
		System.out.println("dispose: don´t forget to cleanup");
		System.exit(0);
	}
}