import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

public final class BezierSurface implements GLEventListener {
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

	// Parâmetros Bezier
	private int LineResolution = 30;

	
	public static void main(String[] args){
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		GLWindow glWindow = GLWindow.create(caps);

		glWindow.setTitle("Bezier Surface");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		glWindow.addGLEventListener(new BezierSurface());
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

		//THIS IS A BACKUP
//		float[] CPoints = {
//				/*B00*/ -15.0f, 0.0f,  15.0f, 1.0f, // 0
//				/*B10*/  -5.0f, 5.0f,  15.0f, 1.0f, // 1
//				/*B20*/   5.0f, 5.0f,  15.0f, 1.0f, // 2
//				/*B30*/  15.0f, 0.0f,  15.0f, 1.0f, // 3
//				/*B01*/ -15.0f, 5.0f,   5.0f, 1.0f, // 4
//				/*B11*/  -5.0f, 5.0f,   5.0f, 1.0f, // 5
//				/*B21*/   5.0f, 5.0f,   5.0f, 1.0f, // 6 
//				/*B31*/  15.0f, 5.0f,   5.0f, 1.0f, // 7
//				/*B02*/ -15.0f, 5.0f,  -5.0f, 1.0f, // 8
//				/*B12*/  -5.0f, 5.0f,  -5.0f, 1.0f, // 9
//				/*B22*/   5.0f, 5.0f,  -5.0f, 1.0f, // 10
//				/*B32*/  15.0f, 5.0f,  -5.0f, 1.0f, // 11
//				/*B03*/ -15.0f, 0.0f, -15.0f, 1.0f, // 12
//				/*B13*/  -5.0f, 5.0f, -15.0f, 1.0f, // 13
//				/*B23*/   5.0f, 5.0f, -15.0f, 1.0f, // 14
//				/*B33*/  15.0f, 0.0f, -15.0f, 1.0f // 15
//		};
		
		
		
		// TODO Calculate Bezier Surface
		float[] CPoints = {
				/*B00*/ -15.0f, -15.0f,  15.0f, 1.0f, // 0
				/*B10*/  -5.0f, 5.0f,  15.0f, 1.0f, // 1
				/*B20*/   5.0f, 5.0f,  15.0f, 1.0f, // 2
				/*B30*/  15.0f, -15.0f,  15.0f, 1.0f, // 3
				/*B01*/ -15.0f, 5.0f,   5.0f, 1.0f, // 4
				/*B11*/  -5.0f, -15.0f,   5.0f, 1.0f, // 5
				/*B21*/   5.0f, -15.0f,   5.0f, 1.0f, // 6 
				/*B31*/  15.0f, 5.0f,   5.0f, 1.0f, // 7
				/*B02*/ -15.0f, 5.0f,  -5.0f, 1.0f, // 8
				/*B12*/  -5.0f, -15.0f,  -5.0f, 1.0f, // 9
				/*B22*/   5.0f, -15.0f,  -5.0f, 1.0f, // 10
				/*B32*/  15.0f, 5.0f,  -5.0f, 1.0f, // 11
				/*B03*/ -15.0f, -15.0f, -15.0f, 1.0f, // 12
				/*B13*/  -5.0f, 5.0f, -15.0f, 1.0f, // 13
				/*B23*/   5.0f, 5.0f, -15.0f, 1.0f, // 14
				/*B33*/  15.0f, -15.0f, -15.0f, 1.0f // 15
		};
		
		// Vertices to draw control points
		float[] CPointsDraw = new float[16*4*2];
		for (int i=0; i<16*4; i++) {
			CPointsDraw[i] = CPoints[i];
		}
		for (int i=0; i<4; i++) {
			CPointsDraw[16*4+i*16]    = CPoints[i*4]   ; CPointsDraw[16*4+i*16+1]  = CPoints[i*4+1] ; CPointsDraw[16*4+i*16+2]  = CPoints[i*4+2] ; CPointsDraw[16*4+i*16+3]  = CPoints[i*4+3];
			CPointsDraw[16*4+i*16+4]  = CPoints[i*4+16]; CPointsDraw[16*4+i*16+5]  = CPoints[i*4+17]; CPointsDraw[16*4+i*16+6]  = CPoints[4*i+18]; CPointsDraw[16*4+i*16+7]  = CPoints[4*i+19];
			CPointsDraw[16*4+i*16+8]  = CPoints[i*4+32]; CPointsDraw[16*4+i*16+9]  = CPoints[i*4+33]; CPointsDraw[16*4+i*16+10] = CPoints[4*i+34]; CPointsDraw[16*4+i*16+11] = CPoints[4*i+35];
			CPointsDraw[16*4+i*16+12] = CPoints[i*4+48]; CPointsDraw[16*4+i*16+13] = CPoints[i*4+49]; CPointsDraw[16*4+i*16+14] = CPoints[4*i+50]; CPointsDraw[16*4+i*16+15] = CPoints[4*i+51];
			
		}		
		FloatBuffer CPointsDrawFB = Buffers.newDirectFloatBuffer(CPointsDraw);
		
		
		// Vertices to draw surface
		float[] aux = new float[4];
		int resolution = LineResolution;
		float[] vertices = new float[6*4*resolution*resolution];
		int count = 0;
		for(int v=0; v<resolution; v++) {
			for(int s=0; s<resolution; s++) {
				aux = BezierSurfaceCalculator(CPoints, (float) s/resolution, (float) v/resolution);
				vertices[count*4] = aux[0]; vertices[count*4+1] = aux[1]; vertices[count*4+2] = aux[2]; vertices[count*4+3] = aux[3];
				count++;
				
				aux = BezierSurfaceCalculator(CPoints, (float) s/resolution, (float) (v+1)/resolution);
				vertices[count*4] = aux[0]; vertices[count*4+1] = aux[1]; vertices[count*4+2] = aux[2]; vertices[count*4+3] = aux[3];
				count++;
				
				aux = BezierSurfaceCalculator(CPoints, (float) (s+1)/resolution, (float) (v+1)/resolution);
				vertices[count*4] = aux[0]; vertices[count*4+1] = aux[1]; vertices[count*4+2] = aux[2]; vertices[count*4+3] = aux[3];
				count++;
				
				aux = BezierSurfaceCalculator(CPoints, (float) s/resolution, (float) v/resolution);
				vertices[count*4] = aux[0]; vertices[count*4+1] = aux[1]; vertices[count*4+2] = aux[2]; vertices[count*4+3] = aux[3];
				count++;
				
				aux = BezierSurfaceCalculator(CPoints, (float) (s+1)/resolution, (float) v/resolution);
				vertices[count*4] = aux[0]; vertices[count*4+1] = aux[1]; vertices[count*4+2] = aux[2]; vertices[count*4+3] = aux[3];
				count++;
				
				aux = BezierSurfaceCalculator(CPoints, (float) (s+1)/resolution, (float) (v+1)/resolution);
				vertices[count*4] = aux[0]; vertices[count*4+1] = aux[1]; vertices[count*4+2] = aux[2]; vertices[count*4+3] = aux[3];
				count++;
			}
			
		}
		FloatBuffer verticesFB = Buffers.newDirectFloatBuffer(vertices);
		
		
		// Vertex-Array Object (VAO)
		gl.glGenVertexArrays(2, vao, 0); 
		gl.glGenBuffers(2, vbo, 0);
		
		gl.glBindVertexArray(vao[0]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertices.length * (Float.SIZE / Byte.SIZE), verticesFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[1]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[1]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, CPointsDraw.length * (Float.SIZE / Byte.SIZE), CPointsDrawFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		// Set Projection Matrix
		float[] projectionMatrix = glu.ortho(-50.0f, 50.0f, -50.0f, 50.0f, -50.0f, 50.0f);
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
		
		for (int i=0; i<2; i++){
			gl.glBindVertexArray(vao[i]);
			gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_LINE);

			maux = glu.matrixRotate(counter, 1.0f, 1.0f, 0.0f);
			pilha = glu.matrixMultiply(maux, pilha);
		
			maux = glu.matrixRotate(counter, 0.0f, 0.0f, 1.0f);
			pilha = glu.matrixMultiply(maux, pilha);
			
			if (i==0){ // Surface
				gl.glUniform4f(colorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
				maux = glu.matrixTranslate(0.0f, 0.0f, -15.0f);
			}
			else if (i==1){ // Control Points
				gl.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
				maux = glu.matrixTranslate(0.0f, 0.0f, -3.0f);
			}
			pilha = glu.matrixMultiply(maux, pilha);
			modelview = glu.matrixMultiply(viewMatrix, pilha);
			
			
			
			gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelview, 0);
		
			
			if (i==0) gl.glDrawArrays(GL4.GL_TRIANGLES, 0, LineResolution*LineResolution*6);
			
			else if (i==1) {
				for (int j=0; j<8; j++) {
					gl.glDrawArrays(GL4.GL_LINE_STRIP, j*4, 4);
				}
			}
			
			
			pilha = glu.matrixIdentity();
		}
		
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
	
	
	// TODO Bezier Auxiliary Functions
	public static float[] BezierSurfaceCalculator(float[] CPoints, float s, float v){
		// Control Points need to be already transposed
		float[] vertices = new float[4];
		float[] CPointsAux = new float[16];
		float[] aux16 = new float[16];
		float[] aux4 = new float[4];
		float[] Bezier = GetBezierMatrix();
		
		// For X, Y and Z
		for (int i=0; i<3; i++){
			// Copy values from control points matrix of the "i" coordinate
			for (int j=0; j<16; j++) {
				CPointsAux[j] = CPoints[j*4+i];
			}
			aux16 = glu.matrixMultiply(Bezier, CPointsAux);
			aux16 = glu.matrixMultiply(aux16, Bezier);
			
			aux4[0] = aux16[0]*v*v*v + aux16[4]*v*v + aux16[8]*v  + aux16[12];
			aux4[1] = aux16[1]*v*v*v + aux16[5]*v*v + aux16[9]*v  + aux16[13];
			aux4[2] = aux16[2]*v*v*v + aux16[6]*v*v + aux16[10]*v + aux16[14];
			aux4[3] = aux16[3]*v*v*v + aux16[7]*v*v + aux16[11]*v + aux16[15];
			
			vertices[i] = aux4[0]*s*s*s + aux4[1]*s*s + aux4[2]*s + aux4[3];
			
		}
		
		vertices[3] = 1.0f;
		
		
		return vertices;
	}
	                                                
	
	public static float[] GetBezierMatrix(){
		float[] Matrix  = {
					-1.0f, 3.0f , -3.0f, 1.0f,
					3.0f , -6.0f,  3.0f, 0.0f,
					-3.0f,  3.0f,  0.0f, 0.0f,
					 1.0f,  0.0f,  0.0f, 0.0f
			};
		return Matrix;
	}
	
}