import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

public final class Sweep implements GLEventListener {
	private GL4 gl;
	private static JGLU glu = new JGLU();
	
	private static int width = 600;
	private static int height = 600;

	private int[] vao = new int[3];
    private int[] vbo = new int[3];
	
	private int shaderProgram;
	private int vertShader;
	private int fragShader;

	private float[] viewMatrix; // 4x4 matrix
	private int modelViewMatrixLocation;
	private int colorLocation;
	
	private float counter = 0.0f;
	private	float step = 0.01f;

	// Parâmetros Hermite
	private int CircResolution = 10;
	private int SweepResolution = 10;
	private float Radius = 1.50f;

	
	public static void main(String[] args){
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		GLWindow glWindow = GLWindow.create(caps);

		glWindow.setTitle("Hermite Surface Sweep");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		glWindow.addGLEventListener(new Sweep());
		Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
	}
	
	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		gl = drawable.getGL().getGL4();

		// Create GPU shader handles
		// OpenGL ES returns a index id to be stored for future reference.
		vertShader = loadShader(GL4.GL_VERTEX_SHADER, "./res/shader.vert");
		fragShader = loadShader(GL4.GL_FRAGMENT_SHADER, "./res/shader.frag");
		// Each shaderProgram must have one vertex shader and one fragment
		// shader.
		shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertShader);
		gl.glAttachShader(shaderProgram, fragShader);

		gl.glLinkProgram(shaderProgram);
		// Check link status.
		int[] linked = new int[1];
		gl.glGetProgramiv(shaderProgram, GL4.GL_LINK_STATUS, linked, 0);
		if (linked[0] != 0) {
			System.out.println("Shaders succesfully linked");
		} else {
			int[] logLength = new int[1];
			gl.glGetProgramiv(shaderProgram, GL4.GL_INFO_LOG_LENGTH,
					logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetProgramInfoLog(shaderProgram, logLength[0], (int[]) null,
					0, log, 0);

			System.err.println("Error linking shaders: " + new String(log));
			System.exit(1);
		}

		gl.glUseProgram(shaderProgram);
		
		// Get position of shader variables 
		int vertexLoc = gl.glGetAttribLocation(shaderProgram, "vertex");
		
		int projectionMatrixLocation = gl.glGetUniformLocation(shaderProgram,
				"u_projectionMatrix");
		
		modelViewMatrixLocation = gl.glGetUniformLocation(shaderProgram,
				"u_modelViewMatrix");
		
		colorLocation = gl.glGetUniformLocation(shaderProgram,
				"u_color");
		
		
		// TODO Calculate Hermite Sweep
		float[] Points = {
				/*P1*/     0.0f, 0.0f,  0.0f, 1.0f,
				/*P2*/     0.0f, 5.0f,  0.0f, 1.0f,
				/*P1dot*/  20.0f, 0.0f,  0.0f, 1.0f,
				/*P2dot*/  -20.0f, 5.0f,  0.0f, 1.0f
		};
		
		float[] circ = new float[4*(CircResolution+1)];
		float x=0.0f, y=0.0f, z=0.0f, w=1.0f;
		for (int i=0; i<=CircResolution; i++) {
			x= (float) (Radius*Math.cos((double) i*2*Math.PI/CircResolution));
			z= (float) (Radius*Math.sin((double) i*2*Math.PI/CircResolution));
			circ[4*i] = x; circ[4*i+1] = y; circ[4*i+2] = z; circ[4*i+3] = w; 
		}
		
		float[] aux1 = new float[4];
		float[] aux2 = new float[4];
		float[] vertices2 = new float[2*4*(CircResolution+1)*(SweepResolution+1)];
		for (int i=0; i<SweepResolution; i++) { // Each Step of the Sweep
			aux1 = HermiteCurve(Points, (float) i/SweepResolution);
			aux2 = HermiteCurve(Points, (float) (i+1)/SweepResolution);
			for (int j=0; j<=CircResolution; j++) { // Each point of the circle
				vertices2[i*(CircResolution+1)*8+j*8] = circ[4*j]+aux1[0]; vertices2[i*(CircResolution+1)*8+j*8+1] = circ[4*j+1]+aux1[1]; vertices2[i*(CircResolution+1)*8+j*8+2] = circ[4*j+2]+aux1[2]; vertices2[i*(CircResolution+1)*8+j*8+3] = circ[4*j+3];
				vertices2[i*(CircResolution+1)*8+j*8+4] = circ[4*j]+aux2[0]; vertices2[i*(CircResolution+1)*8+j*8+5] = circ[4*j+1]+aux2[1]; vertices2[i*(CircResolution+1)*8+j*8+6] = circ[4*j+2]+aux2[2]; vertices2[i*(CircResolution+1)*8+j*8+7] = circ[4*j+3];
			}
		}
		
		
		FloatBuffer vertices2FB = Buffers.newDirectFloatBuffer(vertices2);
		
		// Vertex-Array Object (VAO)
		gl.glGenVertexArrays(1, vao, 0); 
		gl.glGenBuffers(1, vbo, 0);
		
		gl.glBindVertexArray(vao[0]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertices2.length * (Float.SIZE / Byte.SIZE), vertices2FB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		
		// Set Projection Matrix
		float[] projectionMatrix = glu.ortho(-10.0f, 10.0f, -10.0f, 10.0f, 1.0f, 20.0f);
		gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);

		// Set view transformation 
		viewMatrix = glu.lookAt(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);

		// Set background color
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);		
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		// System.out.println("display");
		float[] modelview;
		float[] maux;
		float[] pilha;
		pilha= glu.matrixIdentity();
		counter += step;		
		// Clear screen
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
		gl.glBindVertexArray(vao[0]);
		gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_LINE);
		maux = glu.matrixRotate(counter, 1.0f, 0.0f, 0.0f);
		pilha = glu.matrixMultiply(maux, pilha);
		maux = glu.matrixRotate(counter, 0.0f, 1.0f, 1.0f);
		pilha = glu.matrixMultiply(maux, pilha);
		maux = glu.matrixTranslate(0.0f, 0.0f, -10.0f);
		pilha = glu.matrixMultiply(maux, pilha);
		modelview = glu.matrixMultiply(viewMatrix, pilha);
		gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelview, 0);
		gl.glUniform4f(colorLocation, 1.0f, 1.0f, 1.0f, 1.0f);

		// Draw the surface
		for (int i=0; i<=2*(SweepResolution+1); i++) {
			gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, i*2*(CircResolution+1), 2*(CircResolution+1));
		}

		pilha = glu.matrixIdentity();
		
		gl.glFlush();
	}
	
	public int loadShader(int type, String filename) {
		int shader;

		// Create GPU shader handle
		shader = gl.glCreateShader(type);

		// Read shader file
		String[] vlines = new String[1];
		vlines[0] = "";
		String line;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while ((line = reader.readLine()) != null) {
				vlines[0] += line + "\n"; // insert a newline character after
											// each line
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
		if (compiled[0] != 0) {
			System.out.println("Shader succesfully compiled");
		} else {
			int[] logLength = new int[1];
			gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shader, logLength[0], (int[]) null, 0, log, 0);

			System.err
					.println("Error compiling the shader: " + new String(log));
			System.exit(1);
		}

		return shader;
	}
	
	@Override
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
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		
	}
	
	
	// TODO Hermite Auxiliary Functions
	public static float[] HermiteCurve(float[] Points, float t){
		float[] vertice = new float[4];
		float term1 = (float) (2*Math.pow(t, 3) - 3*Math.pow(t, 2) + 1);
		float term2 = (float) (-2*Math.pow(t, 3) + 3*Math.pow(t, 2));
		float term3 = (float) (Math.pow(t, 3) - 2*Math.pow(t, 2) + t);
		float term4 = (float) (Math.pow(t, 3) - Math.pow(t, 2));
		vertice[0] = term1*Points[0] + term2*Points[4] + term3*Points[8]  + term4*Points[12];
		vertice[1] = term1*Points[1] + term2*Points[5] + term3*Points[9]  + term4*Points[13];
		vertice[2] = term1*Points[2] + term2*Points[6] + term3*Points[10] + term4*Points[14];
		vertice[3] = 1;
		return vertice;
	}
	public static float[] GetHermiteMatrix(){
		float[] Matrix  = {
					 2.0f, -3.0f,  0.0f, 1.0f,
					-2.0f, 3.0f ,  0.0f, 0.0f,
					 1.0f, -2.0f,  1.0f, 0.0f,
					 1.0f, -1.0f,  0.0f, 0.0f
			};
		return Matrix;
	}
	
}
