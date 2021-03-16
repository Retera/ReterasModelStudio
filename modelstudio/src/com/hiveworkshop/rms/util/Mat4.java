package com.hiveworkshop.rms.util;

public class Mat4 {
	public float
			m00 = 1.0f, m01 = 0.0f, m02 = 0.0f, m03 = 0.0f,
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
		set(
				m00, m01, m02, m03,
				m10, m11, m12, m13,
				m20, m21, m22, m23,
				m30, m31, m32, m33);
	}

	public static Mat4 getProd(final Mat4 a, final Mat4 b) {
		return new Mat4(a).mul(b);
	}

	public static Mat4 getInverted(final Mat4 mat) {
		return new Mat4(mat).invert();
	}

	public Mat4 set(final Mat4 a) {
		set(
				a.m00, a.m01, a.m02, a.m03,
				a.m10, a.m11, a.m12, a.m13,
				a.m20, a.m21, a.m22, a.m23,
				a.m30, a.m31, a.m32, a.m33);
		return this;
	}

	public Mat4 set(final float[] a) {
		set(
				a[0], a[1], a[2], a[3],
				a[4], a[5], a[6], a[7],
				a[8], a[9], a[10], a[11],
				a[12], a[13], a[14], a[15]);
		return this;
	}

	public Mat4 set(final float m00, final float m01, final float m02, final float m03,
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
		return this;
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

	public static Mat4 uniformScale(float v, Mat4 matrixToScale) {
		matrixToScale.m00 *= v;
		matrixToScale.m01 *= v;
		matrixToScale.m02 *= v;
		matrixToScale.m03 *= v;
		matrixToScale.m10 *= v;
		matrixToScale.m11 *= v;
		matrixToScale.m12 *= v;
		matrixToScale.m13 *= v;
		matrixToScale.m20 *= v;
		matrixToScale.m21 *= v;
		matrixToScale.m22 *= v;
		matrixToScale.m23 *= v;
		matrixToScale.m30 *= v;
		matrixToScale.m31 *= v;
		matrixToScale.m32 *= v;
		matrixToScale.m33 *= v;
		return matrixToScale;
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

	public Mat4 mul(final Mat4 a) {
		Mat4 temp = new Mat4();
		temp.m00 = m00 * a.m00 + m10 * a.m01 + m20 * a.m02 + m30 * a.m03;
		temp.m01 = m01 * a.m00 + m11 * a.m01 + m21 * a.m02 + m31 * a.m03;
		temp.m02 = m02 * a.m00 + m12 * a.m01 + m22 * a.m02 + m32 * a.m03;
		temp.m03 = m03 * a.m00 + m13 * a.m01 + m23 * a.m02 + m33 * a.m03;
		temp.m10 = m00 * a.m10 + m10 * a.m11 + m20 * a.m12 + m30 * a.m13;
		temp.m11 = m01 * a.m10 + m11 * a.m11 + m21 * a.m12 + m31 * a.m13;
		temp.m12 = m02 * a.m10 + m12 * a.m11 + m22 * a.m12 + m32 * a.m13;
		temp.m13 = m03 * a.m10 + m13 * a.m11 + m23 * a.m12 + m33 * a.m13;
		temp.m20 = m00 * a.m20 + m10 * a.m21 + m20 * a.m22 + m30 * a.m23;
		temp.m21 = m01 * a.m20 + m11 * a.m21 + m21 * a.m22 + m31 * a.m23;
		temp.m22 = m02 * a.m20 + m12 * a.m21 + m22 * a.m22 + m32 * a.m23;
		temp.m23 = m03 * a.m20 + m13 * a.m21 + m23 * a.m22 + m33 * a.m23;
		temp.m30 = m00 * a.m30 + m10 * a.m31 + m20 * a.m32 + m30 * a.m33;
		temp.m31 = m01 * a.m30 + m11 * a.m31 + m21 * a.m32 + m31 * a.m33;
		temp.m32 = m02 * a.m30 + m12 * a.m31 + m22 * a.m32 + m32 * a.m33;
		temp.m33 = m03 * a.m30 + m13 * a.m31 + m23 * a.m32 + m33 * a.m33;
		return set(temp);
	}

	public Mat4 translate(Vec3 a) {
		m30 += m00 * a.x + m10 * a.y + m20 * a.z;
		m31 += m01 * a.x + m11 * a.y + m21 * a.z;
		m32 += m02 * a.x + m12 * a.y + m22 * a.z;
		m33 += m03 * a.x + m13 * a.y + m23 * a.z;
		return this;
	}

	public Mat4 uniformScale(float v) {
		return uniformScale(v, this);
	}

	/**
	 * @param v value to scale by
	 * @return a uniformly scaled copy of the matrix
	 */
	public Mat4 getUniformlyScaled(float v) {
		Mat4 mat4 = new Mat4(this);
		return uniformScale(v, mat4);
	}

	public Mat4 scale(Vec3 a) {
		m00 = m00 * a.x;
		m01 = m01 * a.x;
		m02 = m02 * a.x;
		m03 = m03 * a.x;
		m10 = m10 * a.y;
		m11 = m11 * a.y;
		m12 = m12 * a.y;
		m13 = m13 * a.y;
		m20 = m20 * a.z;
		m21 = m21 * a.z;
		m22 = m22 * a.z;
		m23 = m23 * a.z;
		return this;
	}

	public Mat4 add(Mat4 matToAdd) {
		m00 += matToAdd.m00;
		m01 += matToAdd.m01;
		m02 += matToAdd.m02;
		m03 += matToAdd.m03;
		m10 += matToAdd.m10;
		m11 += matToAdd.m11;
		m12 += matToAdd.m12;
		m13 += matToAdd.m13;
		m20 += matToAdd.m20;
		m21 += matToAdd.m21;
		m22 += matToAdd.m22;
		m23 += matToAdd.m23;
		m30 += matToAdd.m30;
		m31 += matToAdd.m31;
		m32 += matToAdd.m32;
		m33 += matToAdd.m33;
		return this;
	}

	public Mat4 invert() {
		Mat4 temp = new Mat4();
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

		temp.m00 = (m11 * b11) - (m12 * b10) + (m13 * b09) * det;
		temp.m01 = (m02 * b10) - (m01 * b11) - (m03 * b09) * det;
		temp.m02 = (m31 * b05) - (m32 * b04) + (m33 * b03) * det;
		temp.m03 = (m22 * b04) - (m21 * b05) - (m23 * b03) * det;
		temp.m10 = (m12 * b08) - (m10 * b11) - (m13 * b07) * det;
		temp.m11 = (m00 * b11) - (m02 * b08) + (m03 * b07) * det;
		temp.m12 = (m32 * b02) - (m30 * b05) - (m33 * b01) * det;
		temp.m13 = (m20 * b05) - (m22 * b02) + (m23 * b01) * det;
		temp.m20 = (m10 * b10) - (m11 * b08) + (m13 * b06) * det;
		temp.m21 = (m01 * b08) - (m00 * b10) - (m03 * b06) * det;
		temp.m22 = (m30 * b04) - (m31 * b02) + (m33 * b00) * det;
		temp.m23 = (m21 * b02) - (m20 * b04) - (m23 * b00) * det;
		temp.m30 = (m11 * b07) - (m10 * b09) - (m12 * b06) * det;
		temp.m31 = (m00 * b09) - (m01 * b07) + (m02 * b06) * det;
		temp.m32 = (m31 * b01) - (m30 * b03) - (m32 * b00) * det;
		temp.m33 = (m20 * b03) - (m21 * b01) + (m22 * b00) * det;
		return set(temp);
	}

	// copied from ghostwolf and
	// https://www.blend4web.com/api_doc/libs_gl-matrix2.js.html
	public Mat4 fromRotationTranslationScaleOrigin(final Quat q, final Vec3 v, final Vec3 s, final Vec3 pivot) {
		final float xx = q.x * q.x * 2;
		final float xy = q.x * q.y * 2;
		final float xz = q.x * q.z * 2;
		final float yy = q.y * q.y * 2;
		final float yz = q.y * q.z * 2;
		final float zz = q.z * q.z * 2;
		final float wx = q.w * q.x * 2;
		final float wy = q.w * q.y * 2;
		final float wz = q.w * q.z * 2;

		m00 = (1 - (yy + zz)) * s.x;
		m01 = (xy + wz) * s.x;
		m02 = (xz - wy) * s.x;
		m03 = 0;
		m10 = (xy - wz) * s.y;
		m11 = (1 - (xx + zz)) * s.y;
		m12 = (yz + wx) * s.y;
		m13 = 0;
		m20 = (xz + wy) * s.z;
		m21 = (yz - wx) * s.z;
		m22 = (1 - (xx + yy)) * s.z;
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
		final float xx = q.x * q.x * 2;
		final float xy = q.x * q.y * 2;
		final float xz = q.x * q.z * 2;
		final float yy = q.y * q.y * 2;
		final float yz = q.y * q.z * 2;
		final float zz = q.z * q.z * 2;
		final float wx = q.w * q.x * 2;
		final float wy = q.w * q.y * 2;
		final float wz = q.w * q.z * 2;

		m00 = (1 - (yy + zz)) * s.x;
		m01 = (xy + wz) * s.x;
		m02 = (xz - wy) * s.x;
		m03 = 0;
		m10 = (xy - wz) * s.y;
		m11 = (1 - (xx + zz)) * s.y;
		m12 = (yz + wx) * s.y;
		m13 = 0;
		m20 = (xz + wy) * s.z;
		m21 = (yz - wx) * s.z;
		m22 = (1 - (xx + yy)) * s.z;
		m23 = 0;
		m30 = v.x;
		m31 = v.y;
		m32 = v.z;
		m33 = 1;

		return this;
	}

	public Mat4 fromQuat(final Quat q) {
		final float xx = q.x * q.x * 2;
		final float yx = q.y * q.x * 2;
		final float yy = q.y * q.y * 2;
		final float zx = q.z * q.x * 2;
		final float zy = q.z * q.y * 2;
		final float zz = q.z * q.z * 2;
		final float wx = q.w * q.x * 2;
		final float wy = q.w * q.y * 2;
		final float wz = q.w * q.z * 2;

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

	public Mat4 setPerspective(final float fovy, final float aspect, final float near, final float far) {
		final float f = 1 / (float) Math.tan(fovy / 2);

		m00 = f / aspect;
		m01 = 0;
		m02 = 0;
		m03 = 0;
		m10 = 0;
		m11 = f;
		m12 = 0;
		m13 = 0;
		m20 = 0;
		m21 = 0;
		m23 = -1;
		m30 = 0;
		m31 = 0;
		m33 = 0;

		if (!Float.isInfinite(far)) {
			final float nf = 1 / (near - far);

			m22 = (far + near) * nf;
			m32 = 2 * far * near * nf;
		} else {
			m22 = -1;
			m32 = -2 * near;
		}

		return this;
	}

	public Mat4 setOrtho(final float left, final float right, final float bottom, final float top, final float near, final float far) {
		final float lr = 1 / (left - right);
		final float bt = 1 / (bottom - top);
		final float nf = 1 / (near - far);

		m00 = -2 * lr;
		m01 = 0;
		m02 = 0;
		m03 = 0;
		m10 = 0;
		m11 = -2 * bt;
		m12 = 0;
		m13 = 0;
		m20 = 0;
		m21 = 0;
		m22 = 2 * nf;
		m23 = 0;
		m30 = (left + right) * lr;
		m31 = (top + bottom) * bt;
		m32 = (far + near) * nf;
		m33 = 1;

		return this;
	}

	public float[] toFloatArray() {
		return new float[] {
				m00, m01, m02, m03,
				m10, m11, m12, m13,
				m20, m21, m22, m23,
				m30, m31, m32, m33};
	}
}
