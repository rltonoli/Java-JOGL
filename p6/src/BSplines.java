import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

import java.util.Scanner;

public final class BSplines implements GLEventListener {
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

	// Parâmetros para construção da superfície
	private int order;
	private static float[] ctrlPoints;
	private static float[] knots;
	private static float d;
	private static int subdivs;
	private static int tResolution = 10000;
	private int VertNumb = 0; 
	
	
	public static void main(String[] args){
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		GLWindow glWindow = GLWindow.create(caps);

		glWindow.setTitle("JTestJGLU - JOGL");
		glWindow.setSize(width, height);
		glWindow.setUndecorated(false);
		glWindow.setPointerVisible(true);
		glWindow.setVisible(true);

		glWindow.addGLEventListener(new BSplines());
		Animator animator = new Animator();
		animator.add(glWindow);
		animator.start();
	}
	
	public void init(GLAutoDrawable drawable) {
		System.out.println("init");
		
		// Get order value
		order = 0;
		while (order<=1){
			order = GetAndTestInt("Order (k): ");
			if (order<=1) {System.out.println("Invalid input, order must be bigger than 1.");}
		}
		
		// Get number of control points
		int sizePoints = 0;
		while (sizePoints <= 0){
			sizePoints = GetAndTestInt("Number of Control Points: ");
			if (sizePoints <= 0) {System.out.println("Invalid input, hte number of control points must be bigger than 0.");}
		}
		ctrlPoints = new float[2*sizePoints];
		
		// Get control points, printing them
		float item = 0.0f;
		String question = null;
		for (int i=0; i<2*sizePoints; i+=2){
			if (i!=0){
				PrintCrtlPoints(i);
			}
			question = String.format("Control Point %d X: ", i/2+1);
			item = GetAndTest(question);
			ctrlPoints[i] = item;
			question = String.format("Control Point %d Y: ", i/2+1);
			item = GetAndTest(question);
			ctrlPoints[i+1] = item;
		}
		PrintCrtlPoints(ctrlPoints.length);
		
		// Get knot vector
		knots = new float[order+sizePoints];
		int fill = 0;
		float answer = 0.0f;
		String printthis = null;
		while (fill < knots.length){
			question = String.format("Knot element %d: ", fill+1);
			answer = GetAndTest(question);
			if (fill!=0){
				if (answer>=knots[fill-1]) {
					knots[fill] = answer;
					fill+=1;
				}
				else {
					System.out.println("New element must be bigger or equal than previous element. Try again.");
				}	
			}
			else {
				knots[fill] = answer;
				fill+=1;
			}
			for (int i=0; i<fill; i++){
				if (i==0){System.out.print("X = [");}
				printthis = String.format(" %.2f", knots[i]);
				System.out.print(printthis);
				if (i==fill-1){System.out.println(" ]");}
			}
		}
		
		// Normalizes knot vector
		float max = 0;
		for (int i=0; i<knots.length; i++) {
			if (i!=0){
				if (knots[i]>knots[i-1]){
					max = knots[i];
				}
			}
		}
		if (((max>1) || (max<1)) && (max!=0)) {
			for (int i=0; i<knots.length; i++) {
				knots[i] = knots[i]/max;
			}
		}
		
		// Get distance between curves
		d = GetAndTest("Curves' distance (d): ");
		if (d>0) {d=d*(-1);}
		
		// Get number of subdivisions
		subdivs = -1;
		while (subdivs<0){
			subdivs = GetAndTestInt("Subdivisions: ");
			if (subdivs<0) {System.out.println("Invalid input, subdvisions must be equal or bigger than 0.");}
		}
		

		// Calculates first BSpline
		float[] TheBSpline1 = BSpline(order, 0.0f);
		VertNumb = TheBSpline1.length/4;
		FloatBuffer TheBSplineFB1 = Buffers.newDirectFloatBuffer(TheBSpline1);
		
		// Calculates second BSpline
		float[] TheBSpline2 = ChangeDist(TheBSpline1, d);
		FloatBuffer TheBSplineFB2 = Buffers.newDirectFloatBuffer(TheBSpline2);
		
		// Calculates Subdivisions
		int step = (int) VertNumb/subdivs;
		while (step % 4 != 0){
			step--;
			
		}
		float[] Line = new float[8*(subdivs+1)];
		for (int i=0; i<Line.length-8; i+=8) {
			Line[i] = TheBSpline1[i*step/2]; Line[i+1] = TheBSpline1[i*step/2+1]; Line[i+2] = TheBSpline1[i*step/2+2]; Line[i+3] = TheBSpline1[i*step/2+3];
			Line[i+4] = TheBSpline2[i*step/2]; Line[i+5] = TheBSpline2[i*step/2+1]; Line[i+6] = TheBSpline2[i*step/2+2]; Line[i+7] = TheBSpline2[i*step/2+3];
		}
		Line[Line.length-8] = TheBSpline1[TheBSpline1.length-4]; Line[Line.length-7] = TheBSpline1[TheBSpline1.length-3]; Line[Line.length-6] = TheBSpline1[TheBSpline1.length-2]; Line[Line.length-5] = TheBSpline1[TheBSpline1.length-1];
		Line[Line.length-4] = TheBSpline2[TheBSpline2.length-4]; Line[Line.length-3] = TheBSpline2[TheBSpline2.length-3]; Line[Line.length-2] = TheBSpline2[TheBSpline2.length-2]; Line[Line.length-1] = TheBSpline2[TheBSpline2.length-1];
		FloatBuffer LineFB = Buffers.newDirectFloatBuffer(Line);

		
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
		gl.glGenVertexArrays(3, vao, 0); 
		gl.glGenBuffers(3, vbo, 0);
		
		gl.glBindVertexArray(vao[0]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, TheBSpline1.length * (Float.SIZE / Byte.SIZE), TheBSplineFB1, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[1]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[1]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, TheBSpline2.length * (Float.SIZE / Byte.SIZE), TheBSplineFB2, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(vao[2]);
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[2]); // create a buffer object and assign the allocated name
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, Line.length * (Float.SIZE / Byte.SIZE), LineFB, GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(vertexLoc, 4, GL4.GL_FLOAT, false, 0, 0);
		
		// Set Projection Matrix
		float[] projectionMatrix = glu.ortho(-30.0f, 30.0f, -10.0f, 10.0f, -100.0f, 100.0f);
		gl.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);

		// Set view transformation 
		viewMatrix = glu.lookAt(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);

		// Set background color
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);		
	}
	
	@Override
	// TODO Dislay
	public void display(GLAutoDrawable drawable) {
		// System.out.println("display");
		float[] modelview;
		float[] maux;
		float[] pilha;
		pilha= glu.matrixIdentity();
		
		counter += step;
		@SuppressWarnings("unused")
		float cos = (float) Math.cos(counter);
		@SuppressWarnings("unused")
		float sin = (float) Math.sin(counter);
		
		// Clear screen
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
		
		for (int i=0; i<3; i++){
			gl.glBindVertexArray(vao[i]);
			//gl.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_FILL);

			// Set object color
			
			maux = glu.matrixRotate(counter, 1.0f, 0.0f, 0.0f);
			pilha = glu.matrixMultiply(maux, pilha);
			
			maux = glu.matrixRotate(counter, 0.0f, 1.0f, 0.0f);
			pilha = glu.matrixMultiply(maux, pilha);
		
			//maux = glu.matrixTranslate(2*cos, 2*sin, 0.0f);
			//pilha = glu.matrixMultiply(maux, pilha);
			
			
			maux = glu.matrixTranslate(0.0f, 0.0f, -3.0f);
			pilha = glu.matrixMultiply(maux, pilha);
			
			
			if (i==0){ // BSpline z=0
				gl.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
			}
			else if (i==1){ // BSpline z=d
				gl.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
			}
			else{ //Divisions
				gl.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
			}
			
			
			modelview = glu.matrixMultiply(viewMatrix, pilha);
			
			
			
			gl.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelview, 0);
		
			
			if (i==0) gl.glDrawArrays(GL4.GL_LINE_STRIP, 0, VertNumb);
			else if (i==1) gl.glDrawArrays(GL4.GL_LINE_STRIP, 0, VertNumb);
			else gl.glDrawArrays(GL4.GL_LINE_STRIP, 0, 2*(subdivs+1));
			
			
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
	
	// Tests if str is an integer
	public static boolean isInt(String str) {  
		try {  
			Integer.parseInt(str);  
		}  
		catch(NumberFormatException nfe) {  
			return false;  
		}
	  return true;  
	}
	
	// Tests if str is a float
	public static boolean isFloat(String str) {  
		try {  
			Float.parseFloat(str);  
		}  
		catch(NumberFormatException nfe) {  
			return false;  
		}
	  return true;  
	}
	
	// Prints question and receives user input, testing if is an integer
	public static int GetAndTestInt(String question) {
		int n=0;
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		String userInput = null;
		boolean test = false;
		while (test != true) {
			System.out.println(question);
			userInput = reader.next(); // Scans the next token of the input as an int.
			test = isInt(userInput);
		}
		n = Integer.parseInt(userInput);
		return n;
	}
	
	// Prints question and receives user input, testing if is a float
	public static float GetAndTest(String question) {
		float n=0;
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		String userInput = null;
		boolean test = false;
		while (test != true) {
			System.out.println(question);
			userInput = reader.next(); // Scans the next token of the input as a float.
			test = isFloat(userInput);
		}
		n = Float.parseFloat(userInput);
		return n;
	}
	
	// Prints the array ctrlPoints
	public static void PrintCrtlPoints(int index){
		String aux; 
		System.out.print("B = |");
		for (int j=0; j<index;j+=2){
			aux = String.format(" %.2f", ctrlPoints[j]);
			System.out.print(aux);
		}
		System.out.println(" |");
		System.out.print("    |");
		for (int j=1; j<index;j+=2){
			aux = String.format(" %.2f", ctrlPoints[j]);
			System.out.print(aux);
		}
		System.out.println(" |");
	}
	
	// Recursive function, returns Nik at the position t
	public static float Nik(int i, int k, float t){
		float NValue = 0.0f;
		if (k == 1) {
			if ((knots[i-1]<=t) && (t<knots[i])){
				NValue = 1;
			}
		}
		else {
			// Avoids divisions by zero
			if (((knots[i+k-2] - knots[i-1]) == 0) &&  ((knots[i-1+k] - knots[i])==0)) {
				NValue = 0.0f;
			}
			else{
				if (((knots[i+k-2] - knots[i-1]) == 0) ||  ((knots[i-1+k] - knots[i])==0)) {
					if ((knots[i+k-2] - knots[i-1]) == 0) {
						NValue = ((knots[i-1+k] - t)/(knots[i-1+k] - knots[i]))*Nik(i+1,k-1,t);
					}
					else if ((knots[i-1+k] - knots[i])==0){
						NValue = ((t - knots[i-1]) / (knots[i+k-2] - knots[i-1]))*Nik(i,k-1,t);
					}
				}
				else {
					NValue = ((t - knots[i-1]) / (knots[i+k-2] - knots[i-1]))*Nik(i,k-1,t) + ((knots[i-1+k] - t)/(knots[i-1+k] - knots[i]))*Nik(i+1,k-1,t);
					}
			}
		}
		return NValue;
	}
	
	// Returns a float array that describes a BSpline curve with order k 
	public static float[] BSpline(int k, float d) {
		float[] auxVertices = new float[2*tResolution]; // Array that will hold the curve points
		float N=0.0f;
		float NSum=0.0f;
		int invalidPoints = 0;
		for (int vertPoint=0; vertPoint<2*tResolution; vertPoint+=2){ // For each t
			for (int cPoint=0; cPoint<ctrlPoints.length; cPoint+=2) { // For each control point
				N = Nik(cPoint/2+1,k,(float) (vertPoint/2)/tResolution);
				auxVertices[vertPoint] += ctrlPoints[cPoint]*N;
				auxVertices[vertPoint+1] += ctrlPoints[cPoint+1]*N;
				NSum+=N;
			}
			// Check if N sums to 1, if not, this point will later be discarded
			if (NSum != 1.0f) {
				invalidPoints++;
				auxVertices[vertPoint] = 0.0f/0.0f; // Creates Not a Number
				auxVertices[vertPoint+1] = 0.0f/0.0f;
			}
			NSum=0;
		} 
		
		// Discards points with Sum of N != 1
		float[] vertices = new float[4*(tResolution-invalidPoints)];
		int counter = 0;
		for (int i=0; i<2*tResolution; i+=2){
			if (auxVertices[i] == auxVertices[i]) {
				vertices[counter] = auxVertices[i];
				vertices[counter+1] = auxVertices[i+1];
				vertices[counter+2] = d;
				vertices[counter+3] = 1.0f;
				counter+=4;
			}
		}
		return vertices;
	}
	
	// Return a copy of array with z=d
	public static float[] ChangeDist(float[] array, float d) {
		float[] vertices = new float[array.length];
		for (int i=0;i<array.length;i+=4){
			vertices[i] = array[i];
			vertices[i+1] = array[i+1];
			vertices[i+2] = d;
			vertices[i+3] = array[i+3];
		}
		return vertices;
	}
}
