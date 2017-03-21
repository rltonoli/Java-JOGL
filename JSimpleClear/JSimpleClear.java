// package br.unicamp.feec.dca.martino;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.newt.opengl.GLWindow;

/**
 * JSimpleClear.java <BR>
 * author: JMario De Martino <P>
 *
 * Java adaptation of example (listing) 2.1 of the Book "OpenGL SuperBible 6th Edition"
 */

public final class JSimpleClear implements GLEventListener {

	private GL4 gl;

	private static int width = 600;
	private static int height = 600;

	public static void main(String[] s) {
		System.out.println("main");
		
		// This demo is based on the GL4 GLProfile.
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));
		// The demo uses the NEWT (Native Windowing Toolkit) Programming Library
		GLWindow glWindow = GLWindow.create(caps);

		// Setup the GLWindow
		glWindow.setTitle("Simple Clear - JOGL");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		// Finally we connect the GLEventListener application code to the GLWindow.
		// GLWindow will call the GLEventListener init, reshape, display and dispose functions when needed.
		glWindow.addGLEventListener(new JSimpleClear() /* GLEventListener */);
		Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
	}

	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		gl = drawable.getGL().getGL4();

		/* Show the capabilities of the OpenGL implementation */
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl.getClass().getName());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL4.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL4.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL4.GL_VERSION));
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
		System.out.println("reshape - Window resized to width=" + z + " height=" + h);
		width = z;
		height = h;

		// Set viewport
		// Render to a square at the center of the window.
		gl.glViewport((width-height)/2, 0, height, height);
	}

	public void display(GLAutoDrawable drawable) {
		System.out.println("display");
		float[] red = {1.0f, 0.0f, 0.0f, 1.0f};
		gl.glClearBufferfv(GL4.GL_COLOR, 0, red, 0);
	}

	public void dispose(GLAutoDrawable drawable){
		System.out.println("dispose");
		System.exit(0);
	}
}
