//package br.unicamp.feec.dca.martino;


import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;


public final class JTestGLU implements GLEventListener {
	private GL4 gl;
	private JGLU glu = new JGLU();
	
	private static int width = 600;
	private static int height = 600;

	private int[] vao = new int[1];
    private int[] vbo = new int[1];
	private int numVertices;
	
	private int shaderProgram;
	private int vertShader;
	private int fragShader;

	private float[] viewMatrix; // 4x4 matrix
	private int modelViewMatrixLocation;
	private int colorLocation;
	
	private float angle = 0.0f;
	private	float step = -0.01f;

	public static void main(String[] args) {
		
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		GLWindow glWindow = GLWindow.create(caps);

		glWindow.setTitle("JTestJGLU - JOGL");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		glWindow.addGLEventListener(new JTestGLU());
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

		// Vertex-Array Object (VAO)
		gl.glGenVertexArrays(1, vao, 0); // allocate vertex-array object name
		gl.glBindVertexArray(vao[0]); // create and bind a vertex object to the name
		
		// Create triangle
		// Vertex data
    	float[] vertices = {  
    			0.0f, 0.0f, 0.0f, 1.0f,
    			0.5f, 0.0f, 0.0f, 1.0f,
    			0.0f,  0.5f, 0.0f, 1.0f 
    			};

    	FloatBuffer verticesFB = Buffers.newDirectFloatBuffer(vertices);
    	numVertices = vertices.length / 2;
  
		// Vertex-buffer Object (VBO)
    	gl.glGenBuffers(1, vbo, 0); // allocate vertex-buffer object name
    	gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]); // create a buffer object and assign the allocated name

    	// Transfer data to VBO, this perform the copy of data from CPU -> GPU memory
    	gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertices.length * (Float.SIZE / Byte.SIZE), verticesFB, GL4.GL_STATIC_DRAW);
    	verticesFB = null; // It is OK to release CPU vertices memory after transfer to GPU
    	
    	// Associate Vertex attribute with the last bound VBO
        gl.glVertexAttribPointer(vertexLoc/* the vertex attribute */, 4 /* four positions used for each vertex */,
                                 GL4.GL_FLOAT, false /* normalized? */, 0 /* stride */,
                                 0 /* The bound VBO data offset */);

        gl.glEnableVertexAttribArray(0);
	
		// Set Projection Matrix
		float[] projectionMatrix = glu.ortho(-1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 10.0f);
		gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);

		// Set view transformation 
		viewMatrix = glu.lookAt(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
		
		// Set background color
		gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		System.out.println("Reshape ");
	}

	public void display(GLAutoDrawable drawable) {
		// System.out.println("display");
		float[] modelview;
		float[] maux;
		float[] pilha;
		pilha= glu.matrixIdentity();
		
		angle += step*10;
		
		float x = (float) Math.cos(angle) * 0.5f;
		float y = (float) Math.sin(angle) * 0.5f;
		
		// Clear screen
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

		gl.glBindVertexArray(vao[0]);
		gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);
		
		// Set triangle color
		gl.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
		
		float[] quaternion1 = new float[4];
		float[] quaternion2 = new float[4];
		float[] quaternion3 = new float[4];
		
		// 1.a) Teste do quaternion copy
		//quaternion1[0] = 1; quaternion1[1] = 2; quaternion1[2] = 3; quaternion1[3] = 4;
		//quaternion2 = glu.quaternionCopy(quaternion1);
		//glu.quaternionPrint(quaternion1);
		//glu.quaternionPrint(quaternion2);
		
		// 1.b) Teste do cálculo da norma
		//quaternion1[0] = 1; quaternion1[1] = 2; quaternion1[2] = 3; quaternion1[3] = 4;
		//float norma = glu.quaternionNorm(quaternion1);
		//glu.quaternionPrint(quaternion1);
		//System.out.format("Norma esperada: %f%n", Math.sqrt(1+4+9+16));
		//System.out.format("Resultado do cálculo da norma: %f%n", norma);
		
		// 1.c) Teste da normalização do quaternio
		// quaternion1[0] = 1; quaternion1[1] = 2; quaternion1[2] = 3; quaternion1[3] = 4;
		//glu.quaternionNormalize(quaternion1);
		//System.out.format("Quaternio esperado: (%f,%f,%f,%f)%n", 1/Math.sqrt(1+4+9+16),2/Math.sqrt(1+4+9+16),3/Math.sqrt(1+4+9+16),4/Math.sqrt(1+4+9+16));
		//glu.quaternionPrint(quaternion1);
		
		// 1.d) Teste de adição de quaternios
		//quaternion1[0] = 1; quaternion1[1] = 2; quaternion1[2] = 3; quaternion1[3] = 4;
		//quaternion2[0] = 1; quaternion2[1] = 2; quaternion2[2] = 3; quaternion2[3] = 4;
		//quaternion3 = glu.quaternionAdd(quaternion1, quaternion2);
		//glu.quaternionPrint(quaternion3);
		
		// 1.e) Teste de multiplicação de quaternios (Igual dado em aula)
		//quaternion1[0] = 2; quaternion1[1] = -1; quaternion1[2] = 2; quaternion1[3] = 3;
		//quaternion2[0] = 3; quaternion2[1] = 1; quaternion2[2] = -2; quaternion2[3] = 1;
		//quaternion3 = glu.quaternionMultiply(quaternion1, quaternion2);
		//System.out.print("Resultado q1*q2: ");
		//glu.quaternionPrint(quaternion3); // Espera-se (8, 7, 6, 11)
		//quaternion3 = glu.quaternionMultiply(quaternion2, quaternion1);
		//System.out.print("Resultado q2*q1: ");
		//glu.quaternionPrint(quaternion3); // Espera-se (8, -9, -2, 11)
		
		// 1.f) Teste do cálculo do conjugado
		//quaternion1[0] = 1; quaternion1[1] = 2; quaternion1[2] = 3; quaternion1[3] = 4;
		//quaternion2 = glu.quaternionConjugate(quaternion1);
		//glu.quaternionPrint(quaternion1);
		//glu.quaternionPrint(quaternion2);
		
		// 1.h, i, j) Teste das rotações em X, Y e Z
		//quaternion1 = glu.quaternionRotationX((float) Math.PI/3);
		//quaternion2 = glu.quaternionRotationY((float) Math.PI/2);
		//quaternion3 = glu.quaternionRotationZ((float) Math.PI*2/3);
		//System.out.print("Quaternio 1: ");
		//glu.quaternionPrint(quaternion1);
		//System.out.print("Quaternio 2: ");
		//glu.quaternionPrint(quaternion2);
		//System.out.print("Quaternio 3: ");
		//glu.quaternionPrint(quaternion3);
		
		// 1.g) Teste de rotação em torno de eixo arbitrário
		// Primeiramente nos eixos principais
		//quaternion1 = glu.quaternionRotation((float) Math.PI/3,1,0,0);
		//quaternion2 = glu.quaternionRotation((float) Math.PI/2,0,1,0);
		//quaternion3 = glu.quaternionRotation((float) Math.PI*2/3,0,0,1);
		//System.out.print("Quaternio 1: ");
		//glu.quaternionPrint(quaternion1);
		//System.out.print("Quaternio 2: ");
		//glu.quaternionPrint(quaternion2);
		//System.out.print("Quaternio 3: ");
		//glu.quaternionPrint(quaternion3);
		// Agora com eixo "misto"
		//quaternion1 = glu.quaternionRotation((float) Math.PI/3,1,1,0);
		//quaternion2 = glu.quaternionRotation((float) Math.PI/2,0,1,1);
		//quaternion3 = glu.quaternionRotation((float) Math.PI*2/3,1,0,1);
		//System.out.print("Quaternio 1: ");
		//glu.quaternionPrint(quaternion1);
		//System.out.print("Quaternio 2: ");
		//glu.quaternionPrint(quaternion2);
		//System.out.print("Quaternio 3: ");
		//glu.quaternionPrint(quaternion3);
		
		// Teste da transformação de quaternion em matrix
		//quaternion1 = glu.quaternionRotation((float) Math.PI/6,1,0,0);
		//quaternion2 = glu.quaternionRotation((float) Math.PI/4,0,1,0);
		//quaternion3 = glu.quaternionRotation((float) Math.PI/3,0,0,1);
		//maux = glu.quaternion2Matrix(quaternion1);
		//System.out.println("Matriz 1: ");
		//glu.matrixPrint(maux);
		//maux = glu.quaternion2Matrix(quaternion2);
		//System.out.println("Matriz 2: ");
		//glu.matrixPrint(maux);
		//maux = glu.quaternion2Matrix(quaternion3);
		//System.out.println("Matriz 3: ");
		//glu.matrixPrint(maux);
		
		// 1.k) Teste da transformação de matrix em quaternios
		//maux = glu.matrixRotate((float) Math.PI/6, 1, 0, 0);
		//quaternion1 = glu.matrix2Quaternion(maux);
		//System.out.print("Quaternio 1: ");
		//glu.quaternionPrint(quaternion1);
		//maux = glu.matrixRotate((float) Math.PI/4, 0, 1, 0);
		//quaternion2 = glu.matrix2Quaternion(maux);
		//System.out.print("Quaternio 2: ");
		//glu.quaternionPrint(quaternion2);
		//maux = glu.matrixRotate((float) Math.PI/3, 0, 0, 1);
		//quaternion3 = glu.matrix2Quaternion(maux);
		//System.out.print("Quaternio 3: ");
		//glu.quaternionPrint(quaternion3);
		
		// 1.l e teste final) Testando se transformando para quaternion e depois de volta para matriz funciona tudo normal
		//float[] quat = new float[4];
		//quat = glu.matrix2Quaternion(pilha);
		//quaternion1 = glu.quaternionRotation(angle,1.0f,1.0f,0);
		//quat = glu.quaternionMultiply(quaternion1,quat);
		//quaternion2 = glu.quaternionRotation(angle/10,0,0,1.0f);
		//quat = glu.quaternionMultiply(quaternion2,quat);
		//pilha = glu.quaternion2Matrix(quat);
	
		maux = glu.matrixTranslate(0.0f, 0.0f, -3.0f);
		pilha = glu.matrixMultiply(maux, pilha);
		
		modelview = glu.matrixMultiply(viewMatrix, pilha);
		gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelview, 0);
		
		// Draw triangle
		gl.glDrawArrays(GL4.GL_TRIANGLES, 0, numVertices);	
	
		gl.glFlush();
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
	
	

}


