import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

public final class JConics implements GLEventListener {
	private GL4 gl;
	private JGLU glu = new JGLU();
	
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
	
	// Para alterar os parâmetros das cônicas:
	private float Parabola_Focus = 0.8f;
	private float Parabola_End = 10;
	private int Parabola_Triangles = 30;

	private float Ellipse_a = 7;
	private float Ellipse_b = 3;
	private int Ellipse_Triangles = 30;
	
	private float Hyperbola_a = 3;
	private float Hyperbola_b = 2;
	private float Hyperbola_End = 9;
	private int Hyperbola_Triangles = 30;
	
	private int numVertices_Par = Parabola_Triangles+2;
	private int numVertices_Ell = Ellipse_Triangles+2;
	private int numVertices_Hyp = Hyperbola_Triangles+2;
	
	public static void main(String[] args){
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		GLWindow glWindow = GLWindow.create(caps);

		glWindow.setTitle("JTestJGLU - JOGL");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		glWindow.addGLEventListener(new JConics());
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


		// Vertex data
		float[] Parabola = drawParabola(Parabola_Focus, Parabola_End, Parabola_Triangles);
		float[] Ellipse = drawEllipse(Ellipse_a, Ellipse_b, Ellipse_Triangles);
		float[] Hyperbola = drawHyperbola(Hyperbola_a, Hyperbola_b, Hyperbola_End, Hyperbola_Triangles);
		FloatBuffer ParabolaFB = Buffers.newDirectFloatBuffer(Parabola);
		FloatBuffer EllipseFB = Buffers.newDirectFloatBuffer(Ellipse);
		FloatBuffer HyperbolaFB = Buffers.newDirectFloatBuffer(Hyperbola);
		
		// Vertex-Array Object (VAO)
		gl.glGenVertexArrays(3, vao, 0); 
		gl.glGenBuffers(3, vbo, 0);
		
		gl.glBindVertexArray(vao[0]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Parabola.length * (Float.SIZE / Byte.SIZE), ParabolaFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[1]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[1]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Ellipse.length * (Float.SIZE / Byte.SIZE), EllipseFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[2]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[2]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Hyperbola.length * (Float.SIZE / Byte.SIZE), HyperbolaFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		// Set Projection Matrix
		float[] projectionMatrix = glu.ortho(-20.0f, 20.0f, -20.0f, 20.0f, 1.0f, 10.0f);
		gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);

		// Set view transformation 
		viewMatrix = glu.lookAt(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);

		// Set background color
		gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);		
	}

	public float[] drawParabola(float focus, float end, int triangles) {
		
		if (triangles<2){System.out.println("Invalid argument. Minimun triangles number must be at least 2."); triangles=2;}
		int Points = triangles + 1;
		float[] vertices = new float[(triangles+2)*4];
		if (focus<0){focus=(-1)*focus;}
		if (end<0){end=(-1)*end;}
		float ymax = (float) Math.sqrt(4*focus*end);
		float ymin = -ymax;
		float tmax = ymax/(2*focus);
		float tmin = ymin/(2*focus);
		float step = (tmax-tmin)/(Points-1);
		float x=end;
		float y=0;
		vertices[0]=x;vertices[1]=0.0f;vertices[2]=0.0f;vertices[3]=1.0f;
		y = ymin;
		x = end;
		vertices[4]=x;vertices[5]=y;vertices[6]=0.0f;vertices[7]=1.0f;
		for (int i = 2; i <= Points; i++){
			x = x + y*step + focus*step*step;
			y = y + 2*focus*step;
			vertices[i*4    ] = x; vertices[i*4 + 1] = y;	vertices[i*4 + 2] = 0.0f; vertices[i*4 + 3] = 1.0f;
		}
		return vertices;
	}
	
	public float[] drawEllipse(float a, float b, int triangles) {
		
		if (triangles<4){System.out.println("Invalid argument. Minimun triangles number must be at least 4."); triangles=4;}
		int Points = triangles + 1;
		float[] vertices = new float[(triangles+2)*4];
		if (a<0){a=(-1)*a;}
		if (b<0){b=(-1)*b;}
		float step = (float) (2*Math.PI/(Points-1));
		float t = 0;
		float x=0;
		float y=0;
		vertices[0]=0.0f;vertices[1]=0.0f;vertices[2]=0.0f;vertices[3]=1.0f;
		for (int i = 1; i <= Points; i++){
			x = (float) (a*Math.cos(t));
			y = (float) (b*Math.sin(t));
			t += step;
			vertices[i*4    ] = x; vertices[i*4 + 1] = y;	vertices[i*4 + 2] = 0.0f; vertices[i*4 + 3] = 1.0f;
		}
		return vertices;
	}

	public float[] drawHyperbola(float a,float b, float end, int triangles) {
		
		if (triangles<2){System.out.println("Invalid argument. triangles must be at least 2.");triangles=2;}
		int Points = triangles + 1;
		float[] vertices = new float[(triangles+2)*4];
		if (a<0){a=(-1)*a;}
		if (b<0){b=(-1)*b;}
		if (end<0){end=(-1)*end;}
		float ymax = (float) Math.sqrt((b*b*end*end/(a*a)) - b*b); // y2 = b2*x2/a2 - b2
		float ymin = -ymax;
		float tmax = (float) Math.atan(ymax/b);
		float tmin = -tmax;
		float step = (tmax-tmin)/(Points-1);
		float x=end;
		float y=0;
		vertices[0]=x;vertices[1]=0.0f;vertices[2]=0.0f;vertices[3]=1.0f;
		y = ymin;
		x = end;
		vertices[4]=x;vertices[5]=y;vertices[6]=0.0f;vertices[7]=1.0f;
		for (int i = 2; i <= Points; i++){
			x = (float)  ( (b*x) / ( b*Math.cos(step) - y*Math.sin(step) ) );
			y = (float)  ( ( b*y + b*b*Math.tan(step) ) / (b - y*Math.tan(step) ) );
			vertices[i*4    ] = x; vertices[i*4 + 1] = y;	vertices[i*4 + 2] = 0.0f; vertices[i*4 + 3] = 1.0f;
		}
		return vertices;
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
		
		for (int i=0; i<3; i++){
			gl.glBindVertexArray(vao[i]);
			gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);

			// Set object color
			
			maux = glu.matrixRotate(counter, 0.0f, 0.0f, -1.0f);
			pilha = glu.matrixMultiply(maux, pilha);
		
			
			
			if (i==0){ // Parabola
				gl.glUniform4f(colorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
				maux = glu.matrixTranslate(12.0f, 12.0f, -3.0f);
			}
			else if (i==1){ // Ellipse
				gl.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
				maux = glu.matrixTranslate(0.0f, 0.0f, -3.0f);
			}
			else{ //Hyperbola
				gl.glUniform4f(colorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
				maux = glu.matrixTranslate(-10.0f, -10.0f, -3.0f);
			}
			pilha = glu.matrixMultiply(maux, pilha);
			modelview = glu.matrixMultiply(viewMatrix, pilha);
			
			
			
			gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelview, 0);
		
			
			if (i==0) gl.glDrawArrays(GL4.GL_TRIANGLE_FAN, 0, numVertices_Par);
			else if (i==1) gl.glDrawArrays(GL4.GL_TRIANGLE_FAN, 0, numVertices_Ell);
			else gl.glDrawArrays(GL4.GL_TRIANGLE_FAN, 0, numVertices_Hyp);
			
			
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
	
	
}
