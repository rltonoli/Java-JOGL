public class JGLU {

	public float[] matrixIdentity() {
		// Retorna a matrix identidade
		float[] matrix = {
				1.0f, 0f, 0f, 0f,
				0f, 1.0f, 0f, 0f,
				0f, 0f, 1.0f, 0f,
				0f, 0f, 0f, 1.0f
		};
		return matrix;
	}
	
	public float[] matrixMultiply(float[] m0, float[] m1){
		float[] matrix = new float[16];
		// Multiplica as matrizes m0 por m1 e retorna a matriz resultante
		matrix[0] = m0[0]*m1[0] + m0[4]*m1[1] + m0[8]*m1[2] + m0[12]*m1[3];
		matrix[1] = m0[1]*m1[0] + m0[5]*m1[1] + m0[9]*m1[2] + m0[13]*m1[3];
		matrix[2] = m0[2]*m1[0] + m0[6]*m1[1] + m0[10]*m1[2] + m0[14]*m1[3];
		matrix[3] = m0[3]*m1[0] + m0[7]*m1[1] + m0[11]*m1[2] + m0[15]*m1[3];
		matrix[4] = m0[0]*m1[4] + m0[4]*m1[5] + m0[8]*m1[6] + m0[12]*m1[7];
		matrix[5] = m0[1]*m1[4] + m0[5]*m1[5] + m0[9]*m1[6] + m0[13]*m1[7];
		matrix[6] = m0[2]*m1[4] + m0[6]*m1[5] + m0[10]*m1[6] + m0[14]*m1[7];
		matrix[7] = m0[3]*m1[4] + m0[7]*m1[5] + m0[11]*m1[6] + m0[15]*m1[7];
		matrix[8] = m0[0]*m1[8] + m0[4]*m1[9] + m0[8]*m1[10] + m0[12]*m1[11];
		matrix[9] = m0[1]*m1[8] + m0[5]*m1[9] + m0[9]*m1[10] + m0[13]*m1[11];
		matrix[10] = m0[2]*m1[8] + m0[6]*m1[9] + m0[10]*m1[10] + m0[14]*m1[11];
		matrix[11] = m0[3]*m1[8] + m0[7]*m1[9] + m0[11]*m1[10] + m0[15]*m1[11];
		matrix[12] = m0[0]*m1[12] + m0[4]*m1[13] + m0[8]*m1[14] + m0[12]*m1[15];
		matrix[13] = m0[1]*m1[12] + m0[5]*m1[13] + m0[9]*m1[14] + m0[13]*m1[15];
		matrix[14] = m0[2]*m1[12] + m0[6]*m1[13] + m0[10]*m1[14] + m0[14]*m1[15];
		matrix[15] = m0[3]*m1[12] + m0[7]*m1[13] + m0[11]*m1[14] + m0[15]*m1[15];
		return matrix;
	}
	
	public float[] matrixTranslate(float tx, float ty, float tz) {
		// Retorna uma matriz de translação
		float[] matrix = {
				1.0f, 0f, 0f, 0f,
				0f, 1.0f, 0f, 0f,
				0f, 0f, 1.0f, 0f,
				tx, ty, tz, 1.0f
		};
		return matrix;
	}

	
	public float[] matrixRotate(float angle, float x, float y, float z) {
		float[] matrix = new float[16];
		// Retorna a matriz de rotação
		
		// Normaliza o vetor (x,y,z)
		float length = (float) Math.sqrt(x*x+y*y+z*z);
		if (length != 0) {
			x = x/length;
			y = y/length;
			z = z/length;
		} 
		else { // Se o comprimento for 0 (x=0, y=0 e z=0) retorna a matriz identidade
			System.out.println("Invalid operation");
			matrix = matrixIdentity();
			return matrix;
		}
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);
		
		
		matrix[0] = x*x*(1-c) + c;
		matrix[1] = x*y*(1-c) + z*s;
		matrix[2] = x*z*(1-c) - y*s;
		matrix[3] = 0;
		matrix[4] = x*y*(1-c) - z*s;
		matrix[5] = y*y*(1-c) + c;
		matrix[6] = y*z*(1-c) + x*s;
		matrix[7] = 0;
		matrix[8] = x*z*(1-c) + y*s;
		matrix[9] = y*z*(1-c) - x*s;
		matrix[10] = z*z*(1-c) + c;
		matrix[11] = 0;
		matrix[12] = 0;
		matrix[13] = 0;
		matrix[14] = 0;
		matrix[15] = 1;
		
		return matrix;
	}
	
	public float[] matrixScale(float x, float y, float z) {
		// Retorna a matrz de escala
		float[] matrix = {
				x, 0f, 0f, 0f,
				0f, y, 0f, 0f,
				0f, 0f, z, 0f,
				0f, 0f, 0f, 1.0f
		};
		return matrix;
	}
	
	public float[] lookAt(float ex , float ey , float ez , float cx , float cy , float cz, float ux , float uy , float uz){
		float[] matrix = new float[16];
		float dx, dy, dz, lx, ly, lz, ox, oy, oz, d_length, l_length;
		
		// Cálculo do vetor d (para onde a câmera aponta, direção) normalizado
		d_length = (float) Math.sqrt(Math.pow(cx-ex, 2) + Math.pow(cy-ey, 2) + Math.pow(cz-ez, 2));
		if (d_length==0) {System.out.println("Invalid operation"); matrix = matrixIdentity(); return matrix;}
		dx = (cx-ex)/d_length;
		dy = (cy-ey)/d_length;
		dz = (cz-ez)/d_length;
		
		// Cálculo do vetor l (completa o sistema ortonormal composto pelos vetores de direção e orientação) normalizado
		l_length = (float) Math.sqrt(Math.pow((dy*uz-dz*uy), 2) + Math.pow((dz*ux-dx*uz), 2) + Math.pow((dx*uy-dy*ux), 2));
		if (l_length==0) {System.out.println("Invalid operation"); matrix = matrixIdentity(); return matrix;}
		lx = (dy*uz - dz*uy)/l_length;
		ly = (dz*ux - dx*uz)/l_length;
		lz = (dx*uy - dy*ux)/l_length;
		
		// Cálculo do vetor o (orientação da câmera, já normalizado)
		ox = ly*dz-lz*dy;
		oy = lz*dx-lx*dz;
		oz = lx*dy-ly*dx;
		
		// Montagem da matrix
		matrix[0] = lx;
		matrix[1] = ox;
		matrix[2] = -dx;
		matrix[3] = 0;
		matrix[4] = ly;
		matrix[5] = oy;
		matrix[6] = -dy;
		matrix[7] = 0;
		matrix[8] = lz;
		matrix[9] = oz;
		matrix[10] = -dz;
		matrix[11] = 0;
		matrix[12] = -(lx*ex + ly*ey + lz*ez);
		matrix[13] = -(ox*ex + oy*ey + oz*ez);
		matrix[14] = dx*ex + dy*ey + dz*ez;
		matrix[15] = 1;
		
		return matrix;
	}
	
	public float[] ortho(float l, float r, float b, float t, float n, float f) {
		float[] matrix = new float[16];
		// Matriz de projeção ortográfica
		
		if ((l == r) || (b == t) || (n == f) || ((r == 0) && (l == 0)) || ((b == 0) && (t == 0)) || ((n == 0) && (f == 0)))   {
			// Se houver alguma divisão por zero, retorna a matriz identidade
			System.out.println("Invalid operation"); matrix = matrixIdentity(); return matrix;
		} 
		else {
			matrix[0] = 2/(r-l);
			matrix[1] = 0;
			matrix[2] = 0;
			matrix[3] = 0;
			matrix[4] = 0;
			matrix[5] = 2/(t-b);
			matrix[6] = 0;
			matrix[7] = 0;
			matrix[8] = 0;
			matrix[9] = 0;
			matrix[10] = -2/(f-n);
			matrix[11] = 0;
			matrix[12] = -(r+l)/(r-l);
			matrix[13] = -(t+b)/(t-b);
			matrix[14] = -(f+n)/(f-n);
			matrix[15] = 1;
		}
		
		return matrix;
	}
	
	public float[] frustum(float l, float r, float b, float t, float n, float f) {
		float[] matrix = new float[16]; 
		// Matriz de projeção perspectiva frustum
		
		if ((l == r) || (b == t) || (n == f) || ((r == 0) && (l == 0)) || ((b == 0) && (t == 0)) || ((n == 0) && (f == 0)))   {
			// Se houver alguma divisão por zero, retorna a matriz identidade
			System.out.println("Invalid operation"); matrix = matrixIdentity(); return matrix;
		} 
		else {
			matrix[0] = 2*n/(r-l);
			matrix[1] = 0;
			matrix[2] = 0;
			matrix[3] = 0;
			matrix[4] = 0;
			matrix[5] = 2*n/(t-b);
			matrix[6] = 0;
			matrix[7] = 0;
			matrix[8] = (r+l)/(r-l);
			matrix[9] = (t+b)/(t-b);
			matrix[10] = -(f+n)/(f-n);
			matrix[11] = -1;
			matrix[12] = 0;
			matrix[13] = 0;
			matrix[14] = -2*f*n/(f-n);
			matrix[15] = 0;
		}
		return matrix;
	}
	
	float[] quaternionCopy(float[] quaternion) {
		float[] new_quaternion = new float[4]; 
		new_quaternion = quaternion;
		return new_quaternion;
	}
	
	float quaternionNorm(float[] quaternion) {
		float norma = (float) Math.sqrt(Math.pow(quaternion[0], 2)+Math.pow(quaternion[1], 2)+Math.pow(quaternion[2], 2)+Math.pow(quaternion[3], 2));
		return norma;
	}
	
	void quaternionNormalize(float[] quaternion) {
		float norma = quaternionNorm(quaternion);
		if (norma!=0) {
			for (int i=0; i<4; i++) {
				quaternion[i] = quaternion[i]/norma;
			}
		}
	}
	
	float[] quaternionAdd(float[] quaternion1, float[] quaternion2) {
		float[] new_quaternion = new float[4];
		for (int i=0; i<4; i++) {
			new_quaternion[i] = quaternion1[i] + quaternion2[i];
		}
		return new_quaternion;
	}
	
	float[] quaternionMultiply(float[] quaternion1, float[] quaternion2) {
		float[] new_quaternion = new float[4];
		float[] auxiliar = new float[4];
		// p0*q0 - (p1*q1 + p2*q2 + p3*q3) -> Parte escalar
		new_quaternion[0] = quaternion1[0]*quaternion2[0] - quaternionEscalar(quaternion1,quaternion2);
		// p0*q1 + q0*p1 + (p2*q3 - p3*q2) -> i
		auxiliar = quaternionVetorial(quaternion1, quaternion2);
		new_quaternion[1] = quaternion1[0]*quaternion2[1] + quaternion2[0]*quaternion1[1] + auxiliar[1];
		// p0*q2 + q0*p2 + (p3*q1 - p1*q3) -> j
		new_quaternion[2] = quaternion1[0]*quaternion2[2] + quaternion2[0]*quaternion1[2] + auxiliar[2];
		// p0*q3 + q0*p3 + (p1*q2 - p2*q1) -> k
		new_quaternion[3] = quaternion1[0]*quaternion2[3] + quaternion2[0]*quaternion1[3] + auxiliar[3];	
		return new_quaternion;
	}
	
	float[] quaternionConjugate(float[] quaternion) {
		float[] conjugate = new float[4];
		conjugate[0] = quaternion[0];
		conjugate[1] = -quaternion[1];
		conjugate[2] = -quaternion[2];
		conjugate[3] = -quaternion[3];
		return conjugate;
	}
	
	float[] quaternionRotation(float angle, float x, float y, float z){
		float[] rotation = new float[4];
		float length = (float) Math.sqrt(x*x+y*y+z*z);
		if (length != 0) {
			x = x/length;
			y = y/length;
			z = z/length;
		}
		rotation[0] = (float) Math.cos(angle/2);
		rotation[1] = (float) Math.sin(angle/2) * x;
		rotation[2] = (float) Math.sin(angle/2) * y;
		rotation[3] = (float) Math.sin(angle/2) * z;
		return rotation;
	}
	
	float[] quaternionRotationX(float angle){
		float[] rotation = new float[4];
		rotation = quaternionRotation(angle,1,0,0);
		return rotation;
	}
	
	float[] quaternionRotationY(float angle){
		float[] rotation = new float[4];
		rotation = quaternionRotation(angle,0,1,0);
		return rotation;
	}
	
	float[] quaternionRotationZ(float angle){
		float[] rotation = new float[4];
		rotation = quaternionRotation(angle,0,0,1);
		return rotation;
	}
	
	float[] quaternion2Matrix(float[] quaternion){
		float[] matrix = new float[16];
		// Assume que é um quaternio de rotação, com vetor u normalizado
		// Para facilitar o debug vou passar para variáveis q0, q1, q2 e q3
		float q0 = quaternion[0];
		float q1 = quaternion[1];
		float q2 = quaternion[2];
		float q3 = quaternion[3];
		matrix[0] = 2*q0*q0 - 1 + 2*q1*q1; //-> 1x1
		matrix[1] = 2*q1*q2 + 2*q0*q3;
		matrix[2] = 2*q1*q3 - 2*q0*q2;
		matrix[3] = 0;
		matrix[4] = 2*q1*q2 - 2*q0*q3;
		matrix[5] = 2*q0*q0 - 1 + 2*q2*q2; //-> 2x2
		matrix[6] = 2*q2*q3 + 2*q0*q1;
		matrix[7] = 0;
		matrix[8] = 2*q1*q3 + 2*q0*q2;
		matrix[9] = 2*q2*q3 - 2*q0*q1;
		matrix[10] = 2*q0*q0 - 1 + 2*q3*q3; //-> 3x3
		matrix[11] = 0;
		matrix[12] = 0;
		matrix[13] = 0;
		matrix[14] = 0;
		matrix[15] = 1;
		return matrix;
	}
	
	float[] matrix2QuaternionAula(float[] matrix) {
		float[] quaternion = new float[4];
		float angle = (float) Math.acos((matrix[0] + matrix[5] + matrix[10] - 1)/2);
		
		float r1 = matrix[4]*matrix[9] - (matrix[5] - 1)*matrix[8];
		float r2 = matrix[1]*matrix[8] - (matrix[0] - 1)*matrix[9];
		float r3 = (matrix[0] - 1)*(matrix[5] - 1) - matrix[4]*matrix[1];
		matrixPrint(matrix);
		System.out.println(matrix[0] + matrix[5] + matrix[10]);
		System.out.format("angle1: %f, r1: %f, r2: %f, r3: %f%n%n", angle*180/3.1415,r1,r2,r3);
		float length = (float) Math.sqrt(r1*r1+r2*r2+r3*r3);
		if (length != 0) {
			r1 = r1/length;
			r2 = r2/length;
			r3 = r3/length;
		}
		quaternion = quaternionRotation(angle, r1, r2, r3);
		return quaternion;
	}
	
	float[] matrix2Quaternion(float[] matrix) {
		float[] quaternion = new float[4];
		float Tr = matrix[0] + matrix[5] + matrix[10] + 1;
		float angle = 0, S=0, r1=0, r2=0, r3=0;
		if (Tr>0) {
			S = 0.5f/((float)Math.sqrt(Tr));
			angle = 2* (float) Math.acos(0.25f/S);
			r1 = Math.abs((matrix[9] - matrix[6])*S);
			r2 = Math.abs((matrix[2] - matrix[8])*S);
			r3 = Math.abs((matrix[4] - matrix[1])*S);
		} else {
			if ((matrix[0] > matrix[5]) && (matrix[0] > matrix[10])) { 
				  S = 2* (float) Math.sqrt(1.0 + matrix[0] - matrix[5] - matrix[10]); // S=4*qx
				  
				  r1 = 0.5f * S;
				  r2 = (matrix[1] + matrix[4]) / S; 
				  r3 = (matrix[2] + matrix[8]) / S; 
				  angle = 2* (float) Math.acos((matrix[6] + matrix[9])/S);
				} else if ((matrix[5] > matrix[0]) && (matrix[5] > matrix[10])) { 
				  S = 2* (float) Math.sqrt(1.0 + matrix[5] - matrix[0] - matrix[10]); // S=4*qy
				  
				  r1 = (matrix[1] + matrix[4]) / S; 
				  r2 = 0.5f * S;
				  r3 = (matrix[6] + matrix[9]) / S;
				  angle = 2* (float) Math.acos((matrix[2] + matrix[8]) / S);
				} else { 
				  S =  2* (float)Math.sqrt(1.0 + matrix[10] - matrix[0] - matrix[5]); // S=4*qz
				  
				  r1 = (matrix[2] + matrix[8]) / S;
				  r2 = (matrix[6] + matrix[9]) / S;
				  r3 = 0.5f * S;
				  angle = 2* (float) Math.acos((matrix[1] + matrix[4]) / S);
				}
		}
		
		float length = (float) Math.sqrt(r1*r1+r2*r2+r3*r3);
		if (length != 0) {
			r1 = r1/length;
			r2 = r2/length;
			r3 = r3/length;
		}
		quaternion = quaternionRotation(angle, r1, r2, r3);
		return quaternion;
	}
	
	// FUNÇÕES ADICIONAIS PARA MINIMIZAR REDUNDÂNCIA OU AJUDAR NO DEBUG
	
	// Retorna vetor resultante de produto vetorial de q1, q2 e q3 de dois quaternios. 
	// Não mexe no escalar, não utiliza escalar, só usa os vetores.
	float[] quaternionVetorial(float[] quaternion1, float[] quaternion2) {
		float[] vec = new float[4];
		vec[0] = 0;
		vec[1] = quaternion1[2]*quaternion2[3] - quaternion1[3]*quaternion2[2];
		vec[2] = quaternion1[3]*quaternion2[1] - quaternion1[1]*quaternion2[3];
		vec[3] = quaternion1[1]*quaternion2[2] - quaternion1[2]*quaternion2[1];
		return vec;
	}
	
	// Faz produto escalar de q1, q2 e q3 de dois quaternios
	float quaternionEscalar(float[] quaternion1, float[] quaternion2){
		float result = quaternion1[1]*quaternion2[1]+quaternion1[2]*quaternion2[2]+quaternion1[3]*quaternion2[3];
		return result;
	}
	
	void quaternionPrint(float[] quaternion){
		System.out.format("(%f, %f, %f, %f)%n",quaternion[0],quaternion[1],quaternion[2],quaternion[3]);
	}
	
	void matrixPrint(float[] matrix){
		System.out.format("|%f, %f, %f, %f|%n",matrix[0],matrix[4],matrix[8],matrix[12]);
		System.out.format("|%f, %f, %f, %f|%n",matrix[1],matrix[5],matrix[9],matrix[13]);
		System.out.format("|%f, %f, %f, %f|%n",matrix[2],matrix[6],matrix[10],matrix[14]);
		System.out.format("|%f, %f, %f, %f|%n%n",matrix[3],matrix[7],matrix[11],matrix[15]);
	}
}



