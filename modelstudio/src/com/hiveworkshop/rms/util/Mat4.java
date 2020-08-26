package com.hiveworkshop.rms.util;

public class Mat4 {
    public float m00 = 1.0f, m01 = 0.0f, m02 = 0.0f, m03 = 0.0f,
                 m10 = 0.0f, m11 = 1.0f, m12 = 0.0f, m13 = 0.0f,
                 m20 = 0.0f, m21 = 0.0f, m22 = 1.0f, m23 = 0.0f,
                 m30 = 0.0f, m31 = 0.0f, m32 = 0.0f, m33 = 1.0f;

	public Mat4() {
		
	}

	public Mat4(final Mat4 a) {
		set(a);
	}

	public Mat4(final float[] a) {
        set(a);
	}

	public Mat4(final float m00, final float m01, final float m02, final float m03,
                 final float m10, final float m11, final float m12, final float m13,
                 final float m20, final float m21, final float m22, final float m23,
                 final float m30, final float m31, final float m32, final float m33) {
		set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
	}

	public void set(final Mat4 a) {
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
	
  public Vec4 transform(final Vec4 a, final Vec4 out) {
    float x = a.x;
    float y = a.y;
    float z = a.z;
    float w = a.w;

    out.x = (m00 * x) + (m10 * y) + (m20 * z) + (m30 * w);
    out.y = (m01 * x) + (m11 * y) + (m21 * z) + (m31 * w);
    out.z = (m02 * x) + (m12 * y) + (m22 * z) + (m32 * w);
    out.w = (m03 * x) + (m13 * y) + (m23 * z) + (m33 * w);

    return out;
  }

  public Vec3 transform(final Vec3 a, final Vec3 out) {
    float x = a.x;
    float y = a.y;
    float z = a.z;

    out.x = (m00 * x) + (m10 * y) + (m20 * z) + m30;
    out.y = (m01 * x) + (m11 * y) + (m21 * z) + m31;
    out.z = (m02 * x) + (m12 * y) + (m22 * z) + m32;

    return out;
  }

  public Vec4 transform(final Vec4 a) {
    return transform(a, a);
  }
  
  public Vec3 transform(final Vec3 a) {
    return transform(a, a);
  }
    
  public Mat4 setIdentity() {
    m00 = 1.0f;
    m01 = 0.0f;
    m02 = 0.0f;
    m03 = 0.0f;
    m10 = 0.0f;
    m11 = 1.0f;
    m12 = 0.0f;
    m13 = 0.0f;
    m20 = 0.0f;
    m21 = 0.0f;
    m22 = 1.0f;
    m23 = 0.0f;
    m30 = 0.0f;
    m31 = 0.0f;
    m32 = 0.0f;
    m33 = 1.0f;

    return this;
  }

  public Mat4 setZero() {
    m00 = 0.0f;
    m01 = 0.0f;
    m02 = 0.0f;
    m03 = 0.0f;
    m10 = 0.0f;
    m11 = 0.0f;
    m12 = 0.0f;
    m13 = 0.0f;
    m20 = 0.0f;
    m21 = 0.0f;
    m22 = 0.0f;
    m23 = 0.0f;
    m30 = 0.0f;
    m31 = 0.0f;
    m32 = 0.0f;
    m33 = 0.0f;

    return this;
  }

  public Mat4 mul(final Mat4 a, final Mat4 out) {
    out.m00 = m00 * a.m00 + m10 * a.m01 + m20 * a.m02 + m30 * a.m03;
    out.m01 = m01 * a.m00 + m11 * a.m01 + m21 * a.m02 + m31 * a.m03;
    out.m02 = m02 * a.m00 + m12 * a.m01 + m22 * a.m02 + m32 * a.m03;
    out.m03 = m03 * a.m00 + m13 * a.m01 + m23 * a.m02 + m33 * a.m03;
    out.m10 = m00 * a.m10 + m10 * a.m11 + m20 * a.m12 + m30 * a.m13;
    out.m11 = m01 * a.m10 + m11 * a.m11 + m21 * a.m12 + m31 * a.m13;
    out.m12 = m02 * a.m10 + m12 * a.m11 + m22 * a.m12 + m32 * a.m13;
    out.m13 = m03 * a.m10 + m13 * a.m11 + m23 * a.m12 + m33 * a.m13;
    out.m20 = m00 * a.m20 + m10 * a.m21 + m20 * a.m22 + m30 * a.m23;
    out.m21 = m01 * a.m20 + m11 * a.m21 + m21 * a.m22 + m31 * a.m23;
    out.m22 = m02 * a.m20 + m12 * a.m21 + m22 * a.m22 + m32 * a.m23;
    out.m23 = m03 * a.m20 + m13 * a.m21 + m23 * a.m22 + m33 * a.m23;
    out.m30 = m00 * a.m30 + m10 * a.m31 + m20 * a.m32 + m30 * a.m33;
    out.m31 = m01 * a.m30 + m11 * a.m31 + m21 * a.m32 + m31 * a.m33;
    out.m32 = m02 * a.m30 + m12 * a.m31 + m22 * a.m32 + m32 * a.m33;
    out.m33 = m03 * a.m30 + m13 * a.m31 + m23 * a.m32 + m33 * a.m33;

    return out;
  }

  public Mat4 mul(final Mat4 a) {
    return mul(a, this);
  }
    
  public Mat4 translate(Vec3 a, Mat4 out) {
    float x = a.x;
    float y = a.y;
    float z = a.z;

    out.m30 += m00 * x + m10 * y + m20 * z;
    out.m31 += m01 * x + m11 * y + m21 * z;
    out.m32 += m02 * x + m12 * y + m22 * z;
    out.m33 += m03 * x + m13 * y + m23 * z;

    return out;
  }

  public Mat4 translate(Vec3 a) {
    return translate(a, this);
  }

  public Mat4 scale(Vec3 a, Mat4 out) {
    float x = (float) a.x;
    float y = (float) a.y;
    float z = (float) a.z;

    out.m00 = m00 * x;
    out.m01 = m01 * x;
    out.m02 = m02 * x;
    out.m03 = m03 * x;
    out.m10 = m10 * y;
    out.m11 = m11 * y;
    out.m12 = m12 * y;
    out.m13 = m13 * y;
    out.m20 = m20 * z;
    out.m21 = m21 * z;
    out.m22 = m22 * z;
    out.m23 = m23 * z;

    return out;
  }
    
  public Mat4 scale(Vec3 a) {
    return scale(a, this);
  }

	public Mat4 invert(final Mat4 out) {
        float b00 = m00 * m11 - m01 * m10;
        float b01 = m00 * m12 - m02 * m10;
        float b02 = m00 * m13 - m03 * m10;
        float b03 = m01 * m12 - m02 * m11;
        float b04 = m01 * m13 - m03 * m11;
        float b05 = m02 * m13 - m03 * m12;
        float b06 = m20 * m31 - m21 * m30;
        float b07 = m20 * m32 - m22 * m30;
        float b08 = m20 * m33 - m23 * m30;
        float b09 = m21 * m32 - m22 * m31;
        float b10 = m21 * m33 - m23 * m31;
        float b11 = m22 * m33 - m23 * m32;
      
        // Calculate the determinant
        float det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;
      
        if (det == 0f) {
          return null;
        }

        det = 1f / det;
      
        out.m00 = (m11 * b11) - (m12 * b10) + (m13 * b09) * det;
        out.m01 = (m02 * b10) - (m01 * b11) - (m03 * b09) * det;
        out.m02 = (m31 * b05) - (m32 * b04) + (m33 * b03) * det;
        out.m03 = (m22 * b04) - (m21 * b05) - (m23 * b03) * det;
        out.m10 = (m12 * b08) - (m10 * b11) - (m13 * b07) * det;
        out.m11 = (m00 * b11) - (m02 * b08) + (m03 * b07) * det;
        out.m12 = (m32 * b02) - (m30 * b05) - (m33 * b01) * det;
        out.m13 = (m20 * b05) - (m22 * b02) + (m23 * b01) * det;
        out.m20 = (m10 * b10) - (m11 * b08) + (m13 * b06) * det;
        out.m21 = (m01 * b08) - (m00 * b10) - (m03 * b06) * det;
        out.m22 = (m30 * b04) - (m31 * b02) + (m33 * b00) * det;
        out.m23 = (m21 * b02) - (m20 * b04) - (m23 * b00) * det;
        out.m30 = (m11 * b07) - (m10 * b09) - (m12 * b06) * det;
        out.m31 = (m00 * b09) - (m01 * b07) + (m02 * b06) * det;
        out.m32 = (m31 * b01) - (m30 * b03) - (m32 * b00) * det;
        out.m33 = (m20 * b03) - (m21 * b01) + (m22 * b00) * det;
      
        return out;
    }

    public Mat4 invert() {
      return invert(this);
    }

  // copied from ghostwolf and
	// https://www.blend4web.com/api_doc/libs_gl-matrix2.js.html
  public Mat4 fromRotationTranslationScaleOrigin(final Quat q, final Vec3 v, final Vec3 s, final Vec3 pivot) {
    final float x = q.x;
    final float y = q.y;
    final float z = q.z;
    final float w = q.w;
    final float x2 = x + x;
    final float y2 = y + y;
    final float z2 = z + z;
    final float xx = x * x2;
    final float xy = x * y2;
    final float xz = x * z2;
    final float yy = y * y2;
    final float yz = y * z2;
    final float zz = z * z2;
    final float wx = w * x2;
    final float wy = w * y2;
    final float wz = w * z2;
    final float sx = s.x;
    final float sy = s.y;
    final float sz = s.z;

    m00 = (1 - (yy + zz)) * sx;
    m01 = (xy + wz) * sx;
    m02 = (xz - wy) * sx;
    m03 = 0;
    m10 = (xy - wz) * sy;
    m11 = (1 - (xx + zz)) * sy;
    m12 = (yz + wx) * sy;
    m13 = 0;
    m20 = (xz + wy) * sz;
    m21 = (yz - wx) * sz;
    m22 = (1 - (xx + yy)) * sz;
    m23 = 0;
    m30 = (v.x + pivot.x) - ((m00 * pivot.x) + (m10 * pivot.y) + (m20 * pivot.z));
    m31 = (v.y + pivot.y) - ((m01 * pivot.x) + (m11 * pivot.y) + (m21 * pivot.z));
    m32 = (v.z + pivot.z) - ((m02 * pivot.x) + (m12 * pivot.y) + (m22 * pivot.z));
    m33 = 1;

    return this;
  }

  // copied from
  // https://www.blend4web.com/api_doc/libs_gl-matrix2.js.html
  public Mat4 fromRotationTranslationScale(final Quat q, final Vec3 v, final Vec3 s) {
    final float x = q.x;
    final float y = q.y;
    final float z = q.z;
    final float w = q.w;
    final float x2 = x + x;
    final float y2 = y + y;
    final float z2 = z + z;
    final float xx = x * x2;
    final float xy = x * y2;
    final float xz = x * z2;
    final float yy = y * y2;
    final float yz = y * z2;
    final float zz = z * z2;
    final float wx = w * x2;
    final float wy = w * y2;
    final float wz = w * z2;
    final float sx = s.x;
    final float sy = s.y;
    final float sz = s.z;

    m00 = (1 - (yy + zz)) * sx;
    m01 = (xy + wz) * sx;
    m02 = (xz - wy) * sx;
    m03 = 0;
    m10 = (xy - wz) * sy;
    m11 = (1 - (xx + zz)) * sy;
    m12 = (yz + wx) * sy;
    m13 = 0;
    m20 = (xz + wy) * sz;
    m21 = (yz - wx) * sz;
    m22 = (1 - (xx + yy)) * sz;
    m23 = 0;
    m30 = v.x;
    m31 = v.y;
    m32 = v.z;
    m33 = 1;

    return this;
  }

  public Mat4 fromQuat(final Quat q) {
    final float x = q.x, y = q.y, z = q.z, w = q.w;
    final float x2 = x + x;
    final float y2 = y + y;
    final float z2 = z + z;
    final float xx = x * x2;
    final float yx = y * x2;
    final float yy = y * y2;
    final float zx = z * x2;
    final float zy = z * y2;
    final float zz = z * z2;
    final float wx = w * x2;
    final float wy = w * y2;
    final float wz = w * z2;

    m00 = 1 - yy - zz;
    m01 = yx + wz;
    m02 = zx - wy;
    m03 = 0;
    m10 = yx - wz;
    m11 = 1 - xx - zz;
    m12 = zy + wx;
    m13 = 0;
    m20 = zx + wy;
    m21 = zy - wx;
    m22 = 1 - xx - yy;
    m23 = 0;
    m30 = 0;
    m31 = 0;
    m32 = 0;
    m33 = 1;

    return this;
  }
}
