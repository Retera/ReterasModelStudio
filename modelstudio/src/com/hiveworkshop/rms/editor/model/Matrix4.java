package com.hiveworkshop.rms.editor.model;

public class Matrix4 {
    public float m00 = 1.0f;
    public float m01 = 0.0f;
    public float m02 = 0.0f;
    public float m03 = 0.0f;
    public float m10 = 0.0f;
    public float m11 = 1.0f;
    public float m12 = 0.0f;
    public float m13 = 0.0f;
    public float m20 = 0.0f;
    public float m21 = 0.0f;
    public float m22 = 1.0f;
    public float m23 = 0.0f;
    public float m30 = 0.0f;
    public float m31 = 0.0f;
    public float m32 = 0.0f;
    public float m33 = 1.0f;

	public Matrix4() {
		
	}

	public Matrix4(final Matrix4 a) {
		set(a);
	}

	public Matrix4(final float[] a) {
        set(a);
	}

	public Matrix4(final float m00, final float m01, final float m02, final float m03,
                   final float m10, final float m11, final float m12, final float m13,
                   final float m20, final float m21, final float m22, final float m23,
                   final float m30, final float m31, final float m32, final float m33) {
		set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
	}

	public void set(final Matrix4 a) {
        set(a.m00, a.m01, a.m02, a.m03, a.m10, a.m11, a.m12, a.m13, a.m20, a.m21, a.m22, a.m23, a.m30, a.m31, a.m32, a.m33);
	}

	public void set(final float[] a) {
        set(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9], a[10], a[11], a[12], a[13], a[14], a[15]);
	}

	public void set(final float m00, final float m01, final float m02, final float m03,
                    final float m10, final float m11, final float m12, final float m13,
                    final float m20, final float m21, final float m22, final float m23,
                	final float m30, final float m31, final float m32, final float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
	}
	
  public static Vertex4 transform(final Matrix4 a, final Vertex4 b, final Vertex4 out) {
    out.x = a.m00 * b.x + a.m10 * b.y + a.m20 * b.z + a.m30 * b.w;
    out.y = a.m01 * b.x + a.m11 * b.y + a.m21 * b.z + a.m31 * b.w;
    out.z = a.m02 * b.x + a.m12 * b.y + a.m22 * b.z + a.m32 * b.w;
    out.w = a.m03 * b.x + a.m13 * b.y + a.m23 * b.z + a.m33 * b.w;

    return out;
}
    
	public void setIdentity() {
        setIdentity(this);
    }
    
    public static Matrix4 setIdentity(final Matrix4 out) {
        out.m00 = 1.0f;
		out.m01 = 0.0f;
		out.m02 = 0.0f;
		out.m03 = 0.0f;
		out.m10 = 0.0f;
		out.m11 = 1.0f;
		out.m12 = 0.0f;
		out.m13 = 0.0f;
		out.m20 = 0.0f;
		out.m21 = 0.0f;
		out.m22 = 1.0f;
		out.m23 = 0.0f;
		out.m30 = 0.0f;
		out.m31 = 0.0f;
		out.m32 = 0.0f;
        out.m33 = 1.0f;
        
        return out;
    }

	public void setZero() {
		setZero(this);
    }
    
    public static Matrix4 setZero(Matrix4 m) {
		m.m00 = 0.0f;
		m.m01 = 0.0f;
		m.m02 = 0.0f;
		m.m03 = 0.0f;
		m.m10 = 0.0f;
		m.m11 = 0.0f;
		m.m12 = 0.0f;
		m.m13 = 0.0f;
		m.m20 = 0.0f;
		m.m21 = 0.0f;
		m.m22 = 0.0f;
		m.m23 = 0.0f;
		m.m30 = 0.0f;
		m.m31 = 0.0f;
		m.m32 = 0.0f;
		m.m33 = 0.0f;

		return m;
	}

	public static Matrix4 mul(final Matrix4 a, final Matrix4 b, final Matrix4 out) {
        out.m00 = a.m00 * b.m00 + a.m10 * b.m01 + a.m20 * b.m02 + a.m30 * b.m03;
		out.m01 = a.m01 * b.m00 + a.m11 * b.m01 + a.m21 * b.m02 + a.m31 * b.m03;
		out.m02 = a.m02 * b.m00 + a.m12 * b.m01 + a.m22 * b.m02 + a.m32 * b.m03;
		out.m03 = a.m03 * b.m00 + a.m13 * b.m01 + a.m23 * b.m02 + a.m33 * b.m03;
		out.m10 = a.m00 * b.m10 + a.m10 * b.m11 + a.m20 * b.m12 + a.m30 * b.m13;
		out.m11 = a.m01 * b.m10 + a.m11 * b.m11 + a.m21 * b.m12 + a.m31 * b.m13;
		out.m12 = a.m02 * b.m10 + a.m12 * b.m11 + a.m22 * b.m12 + a.m32 * b.m13;
		out.m13 = a.m03 * b.m10 + a.m13 * b.m11 + a.m23 * b.m12 + a.m33 * b.m13;
		out.m20 = a.m00 * b.m20 + a.m10 * b.m21 + a.m20 * b.m22 + a.m30 * b.m23;
		out.m21 = a.m01 * b.m20 + a.m11 * b.m21 + a.m21 * b.m22 + a.m31 * b.m23;
		out.m22 = a.m02 * b.m20 + a.m12 * b.m21 + a.m22 * b.m22 + a.m32 * b.m23;
		out.m23 = a.m03 * b.m20 + a.m13 * b.m21 + a.m23 * b.m22 + a.m33 * b.m23;
		out.m30 = a.m00 * b.m30 + a.m10 * b.m31 + a.m20 * b.m32 + a.m30 * b.m33;
		out.m31 = a.m01 * b.m30 + a.m11 * b.m31 + a.m21 * b.m32 + a.m31 * b.m33;
		out.m32 = a.m02 * b.m30 + a.m12 * b.m31 + a.m22 * b.m32 + a.m32 * b.m33;
        out.m33 = a.m03 * b.m30 + a.m13 * b.m31 + a.m23 * b.m32 + a.m33 * b.m33;
        
        return out;
	}
    
    public static Matrix4 translate(Vertex a, Matrix4 b, Matrix4 out) {
		out.m30 += b.m00 * a.x + b.m10 * a.y + b.m20 * a.z;
		out.m31 += b.m01 * a.x + b.m11 * a.y + b.m21 * a.z;
		out.m32 += b.m02 * a.x + b.m12 * a.y + b.m22 * a.z;
		out.m33 += b.m03 * a.x + b.m13 * a.y + b.m23 * a.z;

		return out;
    }
    
    
    public Matrix4 translate(Vertex vec, Matrix4 dest) {
		return translate(vec, this, dest);
    }
    
    public Matrix4 translate(Vertex vec) {
		return translate(vec, this);
	}

	public static Matrix4 scale(Vertex a, Matrix4 b, Matrix4 out) {
        float x = (float) a.x;
        float y = (float) a.y;
        float z = (float) a.z;

		out.m00 = b.m00 * x;
		out.m01 = b.m01 * x;
		out.m02 = b.m02 * x;
		out.m03 = b.m03 * x;
		out.m10 = b.m10 * y;
		out.m11 = b.m11 * y;
		out.m12 = b.m12 * y;
		out.m13 = b.m13 * y;
		out.m20 = b.m20 * z;
		out.m21 = b.m21 * z;
		out.m22 = b.m22 * z;
        out.m23 = b.m23 * z;
        
		return out;
    }
    
    public Matrix4 scale(Vertex vec, Matrix4 dest) {
		return scale(vec, this, dest);
    }
    
    public Matrix4 scale(Vertex vec) {
		return scale(vec, this);
	}

	public static Matrix4 invert(final Matrix4 a, final Matrix4 out) {
        float a00 = a.m00;
        float a01 = a.m01;
        float a02 = a.m02;
        float a03 = a.m03;
        float a10 = a.m10;
        float a11 = a.m11;
        float a12 = a.m12;
        float a13 = a.m13;
        float a20 = a.m20;
        float a21 = a.m21;
        float a22 = a.m22;
        float a23 = a.m23;
        float a30 = a.m30;
        float a31 = a.m31;
        float a32 = a.m32;
        float a33 = a.m33;
      
        float b00 = a00 * a11 - a01 * a10;
        float b01 = a00 * a12 - a02 * a10;
        float b02 = a00 * a13 - a03 * a10;
        float b03 = a01 * a12 - a02 * a11;
        float b04 = a01 * a13 - a03 * a11;
        float b05 = a02 * a13 - a03 * a12;
        float b06 = a20 * a31 - a21 * a30;
        float b07 = a20 * a32 - a22 * a30;
        float b08 = a20 * a33 - a23 * a30;
        float b09 = a21 * a32 - a22 * a31;
        float b10 = a21 * a33 - a23 * a31;
        float b11 = a22 * a33 - a23 * a32;
      
        // Calculate the determinant
        float det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;
      
        if (det == 0f) {
          return null;
        }

        det = 1f / det;
      
        out.m00 = (a11 * b11) - (a12 * b10) + (a13 * b09) * det;
        out.m01 = (a02 * b10) - (a01 * b11) - (a03 * b09) * det;
        out.m02 = (a31 * b05) - (a32 * b04) + (a33 * b03) * det;
        out.m03 = (a22 * b04) - (a21 * b05) - (a23 * b03) * det;
        out.m10 = (a12 * b08) - (a10 * b11) - (a13 * b07) * det;
        out.m11 = (a00 * b11) - (a02 * b08) + (a03 * b07) * det;
        out.m12 = (a32 * b02) - (a30 * b05) - (a33 * b01) * det;
        out.m13 = (a20 * b05) - (a22 * b02) + (a23 * b01) * det;
        out.m20 = (a10 * b10) - (a11 * b08) + (a13 * b06) * det;
        out.m21 = (a01 * b08) - (a00 * b10) - (a03 * b06) * det;
        out.m22 = (a30 * b04) - (a31 * b02) + (a33 * b00) * det;
        out.m23 = (a21 * b02) - (a20 * b04) - (a23 * b00) * det;
        out.m30 = (a11 * b07) - (a10 * b09) - (a12 * b06) * det;
        out.m31 = (a00 * b09) - (a01 * b07) + (a02 * b06) * det;
        out.m32 = (a31 * b01) - (a30 * b03) - (a32 * b00) * det;
        out.m33 = (a20 * b03) - (a21 * b01) + (a22 * b00) * det;
      
        return out;
    }
}
