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
}
