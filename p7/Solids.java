import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

public final class Solids implements GLEventListener {
	private GL4 gl;
	private JGLU glu = new JGLU();
	
	private static int width = 600;
	private static int height = 600;

	private int[] vao = new int[5];
    private int[] vbo = new int[5];
	
	private int shaderProgram;
	private int vertShader;
	private int fragShader;

	private float[] viewMatrix; // 4x4 matrix
	private int modelViewMatrixLocation;
	private int colorLocation;
	
	private float counter = 0.0f;
	private	float step = 0.01f;
		
	// Parâmetros dos sólidos
	private float CubeL = 3.0f;
	private float ToroidRadius = 8.0f;
	private float ToroidThick = 2.0f;
	private int ToroidCircRes = 10;
	private int ToroidRevRes = 30;
	private int ConeRevRes = 15;
	private int ConeHeightRes = 10;
	private float ConeHeight = 6.0f;
	private float ConeRadius = 3.0f;
	private float SphereRadius = 3.0f;
	private int SphereResolution = 12;
	private float CylinderRadius = 3.0f;
	private float CylinderHeight = 6.0f;
	private int CylinderRes = 20;
	
	
	
	public static void main(String[] args){
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		GLWindow glWindow = GLWindow.create(caps);

		glWindow.setTitle("JTestJGLU - JOGL");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		glWindow.addGLEventListener(new Solids());
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
		float[] Cube = drawCube(CubeL);
		float[] Toroid = drawToroid(ToroidRadius, ToroidThick);
		float[] Cone = drawCone(ConeRadius, ConeHeight);
		float[] Sphere = drawSphere(SphereRadius);
		float[] Cylinder  = drawCylinder(CylinderRadius, CylinderHeight);
		FloatBuffer CubeFB = Buffers.newDirectFloatBuffer(Cube);
		FloatBuffer ToroidFB = Buffers.newDirectFloatBuffer(Toroid);
		FloatBuffer ConeFB = Buffers.newDirectFloatBuffer(Cone);
		FloatBuffer SphereFB = Buffers.newDirectFloatBuffer(Sphere);
		FloatBuffer CylinderFB = Buffers.newDirectFloatBuffer(Cylinder);
		
		// Vertex-Array Object (VAO)
		gl.glGenVertexArrays(5, vao, 0); 
		gl.glGenBuffers(5, vbo, 0);
		
		gl.glBindVertexArray(vao[0]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Cube.length * (Float.SIZE / Byte.SIZE), CubeFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[1]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[1]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Toroid.length * (Float.SIZE / Byte.SIZE), ToroidFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[2]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[2]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Cone.length * (Float.SIZE / Byte.SIZE), ConeFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[3]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[3]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Sphere.length * (Float.SIZE / Byte.SIZE), SphereFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[4]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[4]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Cylinder.length * (Float.SIZE / Byte.SIZE), CylinderFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		// Set Projection Matrix
		float[] projectionMatrix = glu.ortho(-20.0f, 20.0f, -20.0f, 20.0f, 1.0f, 50.0f);
		gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);

		// Set view transformation 
		viewMatrix = glu.lookAt(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);

		// Set background color
		gl.glClearColor(0.5f, 0.5f, 1.0f, 1.0f);		
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
		
		for (int i=0; i<5; i++){
			gl.glBindVertexArray(vao[i]);
			gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_LINE);

			// Set object color
			
			
			
			maux = glu.matrixRotate(counter, 1.0f, 0.0f, 0.0f);
			pilha = glu.matrixMultiply(maux, pilha);
		
			
			
			if (i==0){ // Cube
				maux = glu.matrixRotate(counter, 0.0f, 1.0f, -1.0f);
				pilha = glu.matrixMultiply(maux, pilha);
				
				gl.glUniform4f(colorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
				maux = glu.matrixTranslate(12.0f, 12.0f, -10.0f);
			}
			else if (i==1){ // Toroid
				maux = glu.matrixRotate(counter, 1.0f, 0.0f, 1.0f);
				pilha = glu.matrixMultiply(maux, pilha);
				
				gl.glUniform4f(colorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
				maux = glu.matrixTranslate(0.0f, 0.0f, -10.0f);
			}
			else if (i==2){ //Cone
				maux = glu.matrixRotate(counter, 1.0f, 1.0f, -1.0f);
				pilha = glu.matrixMultiply(maux, pilha);
				
				maux = glu.matrixRotate(counter, 0.0f, 1.0f, -1.0f);
				pilha = glu.matrixMultiply(maux, pilha);
				
				gl.glUniform4f(colorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
				maux = glu.matrixTranslate(-10.0f, -10.0f, -10.0f);
			}
			else if (i==3) { //Sphere
				maux = glu.matrixRotate(counter, 0.0f, 1.0f, -1.0f);
				pilha = glu.matrixMultiply(maux, pilha);
				
				gl.glUniform4f(colorLocation, 1.0f, 0.2f, 0.2f, 1.0f);
				maux = glu.matrixTranslate(-10.0f, 10.0f, -10.0f);
			}
			
			else if (i==4) { //Cylinder
				maux = glu.matrixRotate(counter, 1.0f, 0.0f, -1.0f);
				pilha = glu.matrixMultiply(maux, pilha);
				
				gl.glUniform4f(colorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
				maux = glu.matrixTranslate(10.0f, -10.0f, -10.0f);
			}
			pilha = glu.matrixMultiply(maux, pilha);
			modelview = glu.matrixMultiply(viewMatrix, pilha);
			
			
			
			gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelview, 0);
		
			
			if (i==0) gl.glDrawArrays(GL4.GL_TRIANGLES, 0, 12*3);
			else if (i==1) {
				for (int j=0; j<=ToroidRevRes; j++){
					gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, j*2*(ToroidCircRes+1), 2*(ToroidCircRes+1));
				}
			}
			else if (i==2) {
				for (int j=0; j<ConeRevRes; j++){
					gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, j*2*(ConeHeightRes+1), 2*(ConeHeightRes+1));
				}
				gl.glDrawArrays(GL4.GL_TRIANGLE_FAN, ConeRevRes*2*(ConeHeightRes+1), ConeRevRes+1);
			}
			
			else if (i==3){
				for (int j=0; j<2*SphereResolution; j++){
					gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP,j*2*(SphereResolution), 4*(SphereResolution));
				}
			}
			else if (i==4){
				gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 2*(CylinderRes+1));
				gl.glDrawArrays(GL4.GL_TRIANGLE_FAN, 2*(CylinderRes+1)+1, CylinderRes+1);
				gl.glDrawArrays(GL4.GL_TRIANGLE_FAN, 2*(CylinderRes+1)+CylinderRes+3, CylinderRes+1);
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

	public float[] drawCube(float L) {
		if (L<0) L=L*(-1);
		if (L==0) return null;
		float H= (float) L/2;
		float[] vertices = {
			    -H,-H,-H, 1.0f, // triangle 1 : begin
			    -H,-H, H, 1.0f,
			    -H, H, H, 1.0f,// triangle 1 : end
			    H, H,-H, 1.0f,// triangle 2 : begin
			    -H,-H,-H, 1.0f,
			    -H, H,-H, 1.0f, // triangle 2 : end
			    H,-H, H, 1.0f,
			    -H,-H,-H, 1.0f,
			    H,-H,-H, 1.0f,
			    H, H,-H, 1.0f,
			    H,-H,-H, 1.0f,
			    -H,-H,-H, 1.0f,
			    -H,-H,-H, 1.0f,
			    -H, H, H, 1.0f,
			    -H, H,-H, 1.0f,
			    H,-H, H, 1.0f,
			    -H,-H, H, 1.0f,
			    -H,-H,-H, 1.0f,
			    -H, H, H, 1.0f,
			    -H,-H, H, 1.0f,
			    H,-H, H, 1.0f,
			    H, H, H, 1.0f,
			    H,-H,-H, 1.0f,
			    H, H,-H, 1.0f,
			    H,-H,-H, 1.0f,
			    H, H, H, 1.0f,
			    H,-H, H, 1.0f,
			    H, H, H, 1.0f,
			    H, H,-H, 1.0f,
			    -H, H,-H, 1.0f,
			    H, H, H, 1.0f,
			    -H, H,-H, 1.0f,
			    -H, H, H, 1.0f,
			    H, H, H, 1.0f,
			    -H, H, H, 1.0f,
			    H,-H, H, 1.0f
			};
		return vertices;
	}

	public float[] drawToroid(float Radius, float Thick) {
		if (Radius<Thick) {
			float change = Radius;
			Radius = Thick;
			Thick = change;
		}
		if ((Radius==0) || (Thick==0)) return null;
		float[] vertices = new float[2*4*(ToroidCircRes+1)*(ToroidRevRes)];
		float[] aux = new float[4];
		float x=0.0f, y=0.0f, z=0.0f, w=1.0f;
		float cos, sin;
		for (int i=0; i<ToroidRevRes; i++){
			for (int j=0; j<=ToroidCircRes; j++){
				
				// Circle Position centered in Radius
				x= (float) (Thick*Math.cos((double) j*2*Math.PI/ToroidCircRes));
				y= (float) (Thick*Math.sin((double) j*2*Math.PI/ToroidCircRes));
				aux[0] = x+Radius; aux[1] = y; aux[2] = z; aux[3] = w; 
				
				// Position of the Revolution
				cos = (float) (Math.cos((double) i*2*Math.PI/ToroidRevRes));
				sin = (float) (Math.sin((double) i*2*Math.PI/ToroidRevRes));
				
				// Hermite lines
				vertices[i*(ToroidCircRes+1)*8+j*8] = aux[0]*cos; vertices[i*(ToroidCircRes+1)*8+j*8+1] = aux[1]; vertices[i*(ToroidCircRes+1)*8+j*8+2] = aux[0]*sin; vertices[i*(ToroidCircRes+1)*8+j*8+3] = aux[3];
				// Lines between ToroidCircRess
				
				// Point of the next revolution for the triangle strip
				cos = (float) (Math.cos((double) (i+1)*2*Math.PI/ToroidRevRes));
				sin = (float) (Math.sin((double) (i+1)*2*Math.PI/ToroidRevRes));
				vertices[i*(ToroidCircRes+1)*8+j*8+4] = aux[0]*cos; vertices[i*(ToroidCircRes+1)*8+j*8+5] = aux[1]; vertices[i*(ToroidCircRes+1)*8+j*8+6] = aux[0]*sin; vertices[i*(ToroidCircRes+1)*8+j*8+7] = aux[3];

			}
		}
		return vertices;
	}

	public float[] drawCone(float Radius, float Height){
		if ((Radius==0) || (Height==0)) return null;
		float[] vertices = new float[2*4*(ConeHeightRes+1)*(ConeRevRes)+4*(ConeRevRes+1)];
		float[] aux = new float[4*(ConeRevRes+1)];
		float x=0.0f, y=0.0f, z=0.0f, w=1.0f;
		aux[0]=0.0f;aux[1]=0.0f;aux[2]=0.0f;aux[3]=1.0f;
		int count=1;
		for (int i=0; i<ConeRevRes; i++){
			
			for (int j=0; j<=ConeHeightRes; j++){
				y = Height*j/ConeHeightRes;
				x= (float) (((Height-y)/Height)*Radius*Math.cos((double) i*2*Math.PI/ConeRevRes));
				z= (float) (((Height-y)/Height)*Radius*Math.sin((double) i*2*Math.PI/ConeRevRes)); 
				vertices[i*(ConeHeightRes+1)*8+j*8] = x; vertices[i*(ConeHeightRes+1)*8+j*8+1] = y; vertices[i*(ConeHeightRes+1)*8+j*8+2] = z; vertices[i*(ConeHeightRes+1)*8+j*8+3] = w;

				if (y==0.0f) {
					aux[4*count] = x; aux[4*count+1] = y; aux[4*count+2] = z; aux[4*count+3] = w;
					count++;
				}

				x= (float) (((Height-y)/Height)*Radius*Math.cos((double) (i+1)*2*Math.PI/ConeRevRes));
				z= (float) (((Height-y)/Height)*Radius*Math.sin((double) (i+1)*2*Math.PI/ConeRevRes)); 
				vertices[i*(ToroidCircRes+1)*8+j*8+4] = x; vertices[i*(ToroidCircRes+1)*8+j*8+5] = y; vertices[i*(ToroidCircRes+1)*8+j*8+6] = z; vertices[i*(ToroidCircRes+1)*8+j*8+7] = w;
			}
		}
		
		for (int i=0; i<=ConeRevRes; i++){
			vertices[2*4*(ConeHeightRes+1)*(ConeRevRes)+4*i] = aux[4*i]; vertices[2*4*(ConeHeightRes+1)*(ConeRevRes)+4*i+1] = aux[4*i+1]; vertices[2*4*(ConeHeightRes+1)*(ConeRevRes)+4*i+2] = aux[4*i+2]; vertices[2*4*(ConeHeightRes+1)*(ConeRevRes)+4*i+3] = aux[4*i+3];
		}		
		return vertices;
		
		
	}

	public float[] drawSphere(float Radius){
		float x=0.0f, y=0.0f, z=0.0f, w=1.0f;
		int res = SphereResolution;
		float[] vertices = new float[8*(res+1)*(2*res+1)];
		for (int i=0; i<res; i++){
			for (int j=0; j<=2*res; j++){
				
				x = (float) (Radius*Math.cos((double) i*2*Math.PI/res)*Math.sin((double) j*Math.PI/(2*res)));
				y = (float) (Radius*Math.cos((double) j*Math.PI/(2*res)));	
				z = (float) (Radius*Math.sin((double) i*2*Math.PI/res)*Math.sin((double) j*Math.PI/(2*res)));
				
				vertices[i*(2*res+1)*8+j*8] = x; vertices[i*(2*res+1)*8+j*8+1] = y; vertices[i*(2*res+1)*8+j*8+2] = z; vertices[i*(2*res+1)*8+j*8+3] = w;
				x = (float) (Radius*Math.cos((double) (i+1)*2*Math.PI/res)*Math.sin((double) j*Math.PI/(2*res)));
				y = (float) (Radius*Math.cos((double) j*Math.PI/(2*res)));
				z = (float) (Radius*Math.sin((double) (i+1)*2*Math.PI/res)*Math.sin((double) j*Math.PI/(2*res)));
				
				vertices[i*(2*res+1)*8+j*8+4] = x; vertices[i*(2*res+1)*8+j*8+5] = y; vertices[i*(2*res+1)*8+j*8+6] = z; vertices[i*(2*res+1)*8+j*8+7] = w;
			}
		}
		

		return vertices;
	}
	
	public float[] drawCylinder(float Radius, float Height){
		if ((Radius==0) || (Height==0)) return null;
		float[] vertices = new float[8*(CylinderRes+1)+8*(CylinderRes+2)+4];
		float x=0.0f, y=0.0f, z=0.0f, w=1.0f;
		int count=0;
		for (int i=0; i<=CylinderRes; i++){
			y = 0;
			x= (float) (Radius*Math.cos((double) i*2*Math.PI/CylinderRes));
			z= (float) (Radius*Math.sin((double) i*2*Math.PI/CylinderRes)); 
			vertices[i*8] = x;vertices[i*8+1] = y;vertices[i*8+2] = z;vertices[i*8+3] = w;
			
			y = Height;
			
			vertices[i*8+4] = x;vertices[i*8+5] = y;vertices[i*8+6] = z;vertices[i*8+7] = w;
		}
		for (int i=0; i<=CylinderRes; i++){
			
		}
		
		count = (CylinderRes+1)*8+4;
		vertices[count] = 0.0f; vertices[count+1] = 0.0f; vertices[count+2] = 0.0f; vertices[count+3] = 1.0f;
		count+=4;
		y=0.0f;
		for (int i=0; i<=CylinderRes; i++){

			x= (float) (Radius*Math.cos((double) i*2*Math.PI/CylinderRes));
			z= (float) (Radius*Math.sin((double) i*2*Math.PI/CylinderRes)); 
			vertices[count+i*4] = x; vertices[count+i*4+1] = y; vertices[count+i*4+2] =z; vertices[count+i*4+3] = w;
		}		
		y=Height;
		count+=(CylinderRes+1)*4;
		vertices[count] = 0.0f; vertices[count+1] = y; vertices[count+2] = 0.0f; vertices[count+3] = 1.0f;
		count+=4;
		for (int i=0; i<=CylinderRes; i++){
			x= (float) (Radius*Math.cos((double) i*2*Math.PI/CylinderRes));
			z= (float) (Radius*Math.sin((double) i*2*Math.PI/CylinderRes)); 
			vertices[count+i*4] = x; vertices[count+i*4+1] = y; vertices[count+i*4+2] =z; vertices[count+i*4+3] = w;
		}	
		return vertices;
	}

}



















