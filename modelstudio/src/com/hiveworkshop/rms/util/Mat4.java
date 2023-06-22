package com.hiveworkshop.rms.util;

public class Mat4 {
	public static Mat4 IDENTITY = new Mat4();
//	public float
//			m00 = 1.0f, m01 = 0.0f, m02 = 0.0f, m03 = 0.0f,
//			m10 = 0.0f, m11 = 1.0f, m12 = 0.0f, m13 = 0.0f,
//			m20 = 0.0f, m21 = 0.0f, m22 = 1.0f, m23 = 0.0f,
//			m30 = 0.0f, m31 = 0.0f, m32 = 0.0f, m33 = 1.0f;
	public float
		m00 = 1.0f, m10 = 0.0f, m20 = 0.0f, m30 = 0.0f,
		m01 = 0.0f, m11 = 1.0f, m21 = 0.0f, m31 = 0.0f,
		m02 = 0.0f, m12 = 0.0f, m22 = 1.0f, m32 = 0.0f,
		m03 = 0.0f, m13 = 0.0f, m23 = 0.0f, m33 = 1.0f;

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
				a[0],   a[1],  a[2],  a[3],
				a[4],   a[5],  a[6],  a[7],
				a[8],   a[9], a[10], a[11],
				a[12], a[13], a[14], a[15]);
		return this;
	}


	public Mat4 setFromBindPose(final float[] a) {
		set(
				a[0], a[ 1], a[ 2], m03,
				a[3], a[ 4], a[ 5], m13,
				a[6], a[ 7], a[ 8], m23,
				a[9], a[10], a[11], m33);
		return this;
	}

	public float[] getBindPose() {
		return new float[] {
				m00, m01, m02,
				m10, m11, m12,
				m20, m21, m22,
				m30, m31, m32};

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
	public Mat4 scale(float v) {
		m00 *= v;
		m01 *= v;
		m02 *= v;
		m03 *= v;
		m10 *= v;
		m11 *= v;
		m12 *= v;
		m13 *= v;
		m20 *= v;
		m21 *= v;
		m22 *= v;
		m23 *= v;
		m30 *= v;
		m31 *= v;
		m32 *= v;
		m33 *= v;
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

	public Mat4 mul(final Mat4 a) {
		float tmp00 = m00 * a.m00 + m10 * a.m01 + m20 * a.m02 + m30 * a.m03;
		float tmp01 = m01 * a.m00 + m11 * a.m01 + m21 * a.m02 + m31 * a.m03;
		float tmp02 = m02 * a.m00 + m12 * a.m01 + m22 * a.m02 + m32 * a.m03;
		float tmp03 = m03 * a.m00 + m13 * a.m01 + m23 * a.m02 + m33 * a.m03;
		float tmp10 = m00 * a.m10 + m10 * a.m11 + m20 * a.m12 + m30 * a.m13;
		float tmp11 = m01 * a.m10 + m11 * a.m11 + m21 * a.m12 + m31 * a.m13;
		float tmp12 = m02 * a.m10 + m12 * a.m11 + m22 * a.m12 + m32 * a.m13;
		float tmp13 = m03 * a.m10 + m13 * a.m11 + m23 * a.m12 + m33 * a.m13;
		float tmp20 = m00 * a.m20 + m10 * a.m21 + m20 * a.m22 + m30 * a.m23;
		float tmp21 = m01 * a.m20 + m11 * a.m21 + m21 * a.m22 + m31 * a.m23;
		float tmp22 = m02 * a.m20 + m12 * a.m21 + m22 * a.m22 + m32 * a.m23;
		float tmp23 = m03 * a.m20 + m13 * a.m21 + m23 * a.m22 + m33 * a.m23;
		float tmp30 = m00 * a.m30 + m10 * a.m31 + m20 * a.m32 + m30 * a.m33;
		float tmp31 = m01 * a.m30 + m11 * a.m31 + m21 * a.m32 + m31 * a.m33;
		float tmp32 = m02 * a.m30 + m12 * a.m31 + m22 * a.m32 + m32 * a.m33;
		float tmp33 = m03 * a.m30 + m13 * a.m31 + m23 * a.m32 + m33 * a.m33;
		return set(
				tmp00, tmp01, tmp02, tmp03,
				tmp10, tmp11, tmp12, tmp13,
				tmp20, tmp21, tmp22, tmp23,
				tmp30, tmp31, tmp32, tmp33);
	}

	public Mat4 translate(Vec3 a) {
		m30 += m00 * a.x + m10 * a.y + m20 * a.z;
		m31 += m01 * a.x + m11 * a.y + m21 * a.z;
		m32 += m02 * a.x + m12 * a.y + m22 * a.z;
		m33 += m03 * a.x + m13 * a.y + m23 * a.z;
		return this;
	}
	public Mat4 translateScaled(Vec3 a, float scale) {
		m30 += (m00 * a.x + m10 * a.y + m20 * a.z) * scale;
		m31 += (m01 * a.x + m11 * a.y + m21 * a.z) * scale;
		m32 += (m02 * a.x + m12 * a.y + m22 * a.z) * scale;
		m33 += (m03 * a.x + m13 * a.y + m23 * a.z) * scale;
		return this;
	}

	public Mat4 setLocation(Vec3 loc) {
		m30 = loc.x;
		m31 = loc.y;
		m32 = loc.z;
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

	public Mat4 addScaled(Mat4 matToAdd, float scale) {
		m00 += (matToAdd.m00 * scale);
		m01 += (matToAdd.m01 * scale);
		m02 += (matToAdd.m02 * scale);
		m03 += (matToAdd.m03 * scale);
		m10 += (matToAdd.m10 * scale);
		m11 += (matToAdd.m11 * scale);
		m12 += (matToAdd.m12 * scale);
		m13 += (matToAdd.m13 * scale);
		m20 += (matToAdd.m20 * scale);
		m21 += (matToAdd.m21 * scale);
		m22 += (matToAdd.m22 * scale);
		m23 += (matToAdd.m23 * scale);
		m30 += (matToAdd.m30 * scale);
		m31 += (matToAdd.m31 * scale);
		m32 += (matToAdd.m32 * scale);
		m33 += (matToAdd.m33 * scale);
		return this;
	}

	public Mat4 invert1() {
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

		float tmp00 = (m11 * b11) - (m12 * b10) + (m13 * b09) * det;
		float tmp01 = (m02 * b10) - (m01 * b11) - (m03 * b09) * det;
		float tmp02 = (m31 * b05) - (m32 * b04) + (m33 * b03) * det;
		float tmp03 = (m22 * b04) - (m21 * b05) - (m23 * b03) * det;
		float tmp10 = (m12 * b08) - (m10 * b11) - (m13 * b07) * det;
		float tmp11 = (m00 * b11) - (m02 * b08) + (m03 * b07) * det;
		float tmp12 = (m32 * b02) - (m30 * b05) - (m33 * b01) * det;
		float tmp13 = (m20 * b05) - (m22 * b02) + (m23 * b01) * det;
		float tmp20 = (m10 * b10) - (m11 * b08) + (m13 * b06) * det;
		float tmp21 = (m01 * b08) - (m00 * b10) - (m03 * b06) * det;
		float tmp22 = (m30 * b04) - (m31 * b02) + (m33 * b00) * det;
		float tmp23 = (m21 * b02) - (m20 * b04) - (m23 * b00) * det;
		float tmp30 = (m11 * b07) - (m10 * b09) - (m12 * b06) * det;
		float tmp31 = (m00 * b09) - (m01 * b07) + (m02 * b06) * det;
		float tmp32 = (m31 * b01) - (m30 * b03) - (m32 * b00) * det;
		float tmp33 = (m20 * b03) - (m21 * b01) + (m22 * b00) * det;
		return set(
				tmp00, tmp01, tmp02, tmp03,
				tmp10, tmp11, tmp12, tmp13,
				tmp20, tmp21, tmp22, tmp23,
				tmp30, tmp31, tmp32, tmp33);
	}

	public Mat4 invert() {
		float determinant = determinant();

		if (determinant != 0) {
			/*
			 * m00 m01 m02 m03
			 * m10 m11 m12 m13
			 * m20 m21 m22 m23
			 * m30 m31 m32 m33
			 */
//			if (dest == null)
//				dest = new Matrix4f();
			float determinant_inv = 1f/determinant;

			// first row
			float t00 =  determinant3x3(m11, m12, m13, m21, m22, m23, m31, m32, m33);
			float t01 = -determinant3x3(m10, m12, m13, m20, m22, m23, m30, m32, m33);
			float t02 =  determinant3x3(m10, m11, m13, m20, m21, m23, m30, m31, m33);
			float t03 = -determinant3x3(m10, m11, m12, m20, m21, m22, m30, m31, m32);
			// second row
			float t10 = -determinant3x3(m01, m02, m03, m21, m22, m23, m31, m32, m33);
			float t11 =  determinant3x3(m00, m02, m03, m20, m22, m23, m30, m32, m33);
			float t12 = -determinant3x3(m00, m01, m03, m20, m21, m23, m30, m31, m33);
			float t13 =  determinant3x3(m00, m01, m02, m20, m21, m22, m30, m31, m32);
			// third row
			float t20 =  determinant3x3(m01, m02, m03, m11, m12, m13, m31, m32, m33);
			float t21 = -determinant3x3(m00, m02, m03, m10, m12, m13, m30, m32, m33);
			float t22 =  determinant3x3(m00, m01, m03, m10, m11, m13, m30, m31, m33);
			float t23 = -determinant3x3(m00, m01, m02, m10, m11, m12, m30, m31, m32);
			// fourth row
			float t30 = -determinant3x3(m01, m02, m03, m11, m12, m13, m21, m22, m23);
			float t31 =  determinant3x3(m00, m02, m03, m10, m12, m13, m20, m22, m23);
			float t32 = -determinant3x3(m00, m01, m03, m10, m11, m13, m20, m21, m23);
			float t33 =  determinant3x3(m00, m01, m02, m10, m11, m12, m20, m21, m22);

			// transpose and divide by the determinant
			m00 = t00*determinant_inv;
			m11 = t11*determinant_inv;
			m22 = t22*determinant_inv;
			m33 = t33*determinant_inv;
			m01 = t10*determinant_inv;
			m10 = t01*determinant_inv;
			m20 = t02*determinant_inv;
			m02 = t20*determinant_inv;
			m12 = t21*determinant_inv;
			m21 = t12*determinant_inv;
			m03 = t30*determinant_inv;
			m30 = t03*determinant_inv;
			m13 = t31*determinant_inv;
			m31 = t13*determinant_inv;
			m32 = t23*determinant_inv;
			m23 = t32*determinant_inv;
			return this;
		} else
			return this;
	}

	private static float determinant3x3(float t00, float t01, float t02,
	                                    float t10, float t11, float t12,
	                                    float t20, float t21, float t22) {
		return   t00 * (t11 * t22 - t12 * t21)
				+ t01 * (t12 * t20 - t10 * t22)
				+ t02 * (t10 * t21 - t11 * t20);
	}

	public float determinant() {
		float f =
				m00
						* ((m11 * m22 * m33 + m12 * m23 * m31 + m13 * m21 * m32)
						- m13 * m22 * m31
						- m11 * m23 * m32
						- m12 * m21 * m33);
		f -= m01
				* ((m10 * m22 * m33 + m12 * m23 * m30 + m13 * m20 * m32)
				- m13 * m22 * m30
				- m10 * m23 * m32
				- m12 * m20 * m33);
		f += m02
				* ((m10 * m21 * m33 + m11 * m23 * m30 + m13 * m20 * m31)
				- m13 * m21 * m30
				- m10 * m23 * m31
				- m11 * m20 * m33);
		f -= m03
				* ((m10 * m21 * m32 + m11 * m22 * m30 + m12 * m20 * m31)
				- m12 * m21 * m30
				- m10 * m22 * m31
				- m11 * m20 * m32);
		return f;
	}

	// copied from ghostwolf and
	// https://www.blend4web.com/api_doc/libs_gl-matrix2.js.html
	public Mat4 fromRotationTranslationScaleOrigin(final Quat rot, final Vec3 transl, final Vec3 scale, final Vec3 pivot) {
		final float xx = rot.x * rot.x * 2;
		final float xy = rot.x * rot.y * 2;
		final float xz = rot.x * rot.z * 2;
		final float yy = rot.y * rot.y * 2;
		final float yz = rot.y * rot.z * 2;
		final float zz = rot.z * rot.z * 2;
		final float wx = rot.w * rot.x * 2;
		final float wy = rot.w * rot.y * 2;
		final float wz = rot.w * rot.z * 2;

		m00 = (1 - (yy + zz))   * scale.x;
		m01 = (xy + wz)         * scale.x;
		m02 = (xz - wy)         * scale.x;
		m03 = 0;
		m10 = (xy - wz)         * scale.y;
		m11 = (1 - (xx + zz))   * scale.y;
		m12 = (yz + wx)         * scale.y;
		m13 = 0;
		m20 = (xz + wy)         * scale.z;
		m21 = (yz - wx)         * scale.z;
		m22 = (1 - (xx + yy))   * scale.z;
		m23 = 0;
		m30 = (transl.x + pivot.x) - ((m00 * pivot.x) + (m10 * pivot.y) + (m20 * pivot.z));
		m31 = (transl.y + pivot.y) - ((m01 * pivot.x) + (m11 * pivot.y) + (m21 * pivot.z));
		m32 = (transl.z + pivot.z) - ((m02 * pivot.x) + (m12 * pivot.y) + (m22 * pivot.z));
		m33 = 1;

		return this;
	}

	public Mat4 fromRotationTranslationScaleOrigin11(Vec3 pivot) {
		final Quat rot = new Quat();
		final Vec3 loc = new Vec3();
		final Vec3 scale = new Vec3();

		float xx = rot.x * rot.x * 2;
		float xy = rot.x * rot.y * 2;
		float xz = rot.x * rot.z * 2;
		float yy = rot.y * rot.y * 2;
		float yz = rot.y * rot.z * 2;
		float zz = rot.z * rot.z * 2;
		float wx = rot.w * rot.x * 2;
		float wy = rot.w * rot.y * 2;
		float wz = rot.w * rot.z * 2;

		m00 = (1 - (yy + zz))   * scale.x;
		m01 = (xy + wz)         * scale.x;
		m02 = (xz - wy)         * scale.x;

		m10 = (xy - wz)         * scale.y;
		m11 = (1 - (xx + zz))   * scale.y;
		m12 = (yz + wx)         * scale.y;

		m20 = (xz + wy)         * scale.z;
		m21 = (yz - wx)         * scale.z;
		m22 = (1 - (xx + yy))   * scale.z;

		m30 = (loc.x + pivot.x) - ((m00 * pivot.x) + (m10 * pivot.y) + (m20 * pivot.z));
		m31 = (loc.y + pivot.y) - ((m01 * pivot.x) + (m11 * pivot.y) + (m21 * pivot.z));
		m32 = (loc.z + pivot.z) - ((m02 * pivot.x) + (m12 * pivot.y) + (m22 * pivot.z));


		loc.x = ((m00 * pivot.x) + (m10 * pivot.y) + (m20 * pivot.z)) - pivot.x;
		loc.y = ((m01 * pivot.x) + (m11 * pivot.y) + (m21 * pivot.z)) - pivot.y;
		loc.z = ((m02 * pivot.x) + (m12 * pivot.y) + (m22 * pivot.z)) - pivot.z;

		scale.z = m20 / (xz + wy);
		scale.z = m21 / (yz - wx);
		scale.z = m22 / (1 - (xx + yy));

		scale.y = m10 / (xy - wz);
		scale.y = m11 / (1 - (xx + zz));
		scale.y = m12 / (yz + wx);

		scale.x = m00 / (1 - (yy + zz));
		scale.x = m01 / (xy + wz);
		scale.x = m02 / (xz - wy);


		xx = (1 - m22 * (yz - wx) / m21) - yy;
		xx = (1 - m11 * (yz + wx) / m12) - zz;

		yy = (1 - m22 * (xz + wy) / m20) - xx;
		yy = (1 - m00 * (xz - wy) / m02) - zz;

		zz = (1 - m00 * (xy + wz) / m01) - yy;
		zz = (1 - m11 * (xy - wz) / m10) - xx;


		xx = m22 * ((xz + wy) / m20 - (yz - wx) / m21) + xx;
		xx = m11 * ((xy - wz) / m10 - (yz + wx) / m12) + xx;

		yy = m22 * ((yz - wx) / m21 - (xz + wy) / m20) + yy;
		yy = m00 * ((xy + wz) / m01 - (xz - wy) / m02) + yy;

		zz = m00 * ((xz - wy) / m02 - (xy + wz) / m01) + zz;
		zz = m11 * ((yz + wx) / m12 - (xy - wz) / m10) + zz;

		return this;
	}

	public Vec3 getBackLocation(final Vec3 pivot) {
		Vec3 loc = new Vec3();

		loc.x = m30 + ((m00 * pivot.x) + (m10 * pivot.y) + (m20 * pivot.z)) - pivot.x;
		loc.y = m31 + ((m01 * pivot.x) + (m11 * pivot.y) + (m21 * pivot.z)) - pivot.y;
		loc.z = m32 + ((m02 * pivot.x) + (m12 * pivot.y) + (m22 * pivot.z)) - pivot.z;

		return loc;
	}

	public Quat getBackRotation(Vec3 loc, Vec3 scale, Vec3 pivot) {
		Quat rot = new Quat();
//		float xx = rot.x * rot.x * 2;
//		float xy = rot.x * rot.y * 2;
//		float xz = rot.x * rot.z * 2;
//		float yy = rot.y * rot.y * 2;
//		float yz = rot.y * rot.z * 2;
//		float zz = rot.z * rot.z * 2;
//		float wx = rot.w * rot.x * 2;
//		float wy = rot.w * rot.y * 2;
//		float wz = rot.w * rot.z * 2;
//
//		m00 = (1 - (yy + zz))   * scale.x;
//		m01 = (xy + wz)         * scale.x;
//		m02 = (xz - wy)         * scale.x;
//		m10 = (xy - wz)         * scale.y;
//		m11 = (1 - (xx + zz))   * scale.y;
//		m12 = (yz + wx)         * scale.y;
//		m20 = (xz + wy)         * scale.z;
//		m21 = (yz - wx)         * scale.z;
//		m22 = (1 - (xx + yy))   * scale.z;


		float t00 = m00 / scale.x;
		float t01 = m01 / scale.x;
		float t02 = m02 / scale.x;
		float t10 = m10 / scale.y;
		float t11 = m11 / scale.y;
		float t12 = m12 / scale.y;
		float t20 = m20 / scale.z;
		float t21 = m21 / scale.z;
		float t22 = m22 / scale.z;
//
//		t00 = 1 - yy - zz  ;
//		t01 = xy + wz        ;
//		t02 = xz - wy        ;
//		t10 = xy - wz        ;
//		t11 = 1 - xx - zz ;
//		t12 = yz + wx        ;
//		t20 = xz + wy        ;
//		t21 = yz - wx        ;
//		t22 = 1 - xx - yy  ;
//
//		zz = t22 - t11 + yy;
//		xx = 1 - t22 - yy;
//
//		yy = 1 - t00 - zz  ;

//		zz = 1 - yy - t00;
//
//		yy = 1 - xx - t22;
//
//		xx = 1 - t11 - zz;
//
//		yy = t11 + zz - t22;
//
//		zz  = (1 - t11 + t22 - t00)/2;


		float zz = (t22 - t11 + 1 - t00) / 2;
		float xx = 1 - t11 - zz;
//		float yy = 1 - t00 - zz;
		float yy = t11 + zz - t22;


		rot.x = (float) Math.sqrt(xx / 2);
		rot.y = (float) Math.sqrt(yy / 2);
		rot.z = (float) Math.sqrt(zz / 2);

		return rot;
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

	public Mat4 setOrtho(Vec3 nearLeftBottom, Vec3 farRightTop) {
		return setOrtho(nearLeftBottom.y, farRightTop.y, nearLeftBottom.z, farRightTop.z, nearLeftBottom.x, farRightTop.x);
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

	public void printMatrix(){
		String s = "" +
		"| " + m00 + "\t" + m01 + "\t" + m02 + "\t" + m03 + " |\n" +
		"| " + m10 + "\t" + m11 + "\t" + m12 + "\t" + m13 + " |\n" +
		"| " + m20 + "\t" + m21 + "\t" + m22 + "\t" + m23 + " |\n" +
		"| " + m30 + "\t" + m31 + "\t" + m32 + "\t" + m33 + " |";
		System.out.println(s);
	}

	public Mat4 setAsProjection(Vec4 v){
		m00 = v.x * v.x;
		m01 = v.y * v.x;
		m02 = v.z * v.x;
		m03 = v.w * v.x;
		m10 = v.x * v.y;
		m11 = v.y * v.y;
		m12 = v.z * v.y;
		m13 = v.w * v.y;
		m20 = v.x * v.z;
		m21 = v.y * v.z;
		m22 = v.z * v.z;
		m23 = v.w * v.z;
		m30 = v.x * v.w;
		m31 = v.y * v.w;
		m32 = v.z * v.w;
		m33 = v.w * v.w;
		return this;
	}


	public Mat4 transpose() {
		float m00 = this.m00;
		float m01 = this.m10;
		float m02 = this.m20;
		float m03 = this.m30;
		float m10 = this.m01;
		float m11 = this.m11;
		float m12 = this.m21;
		float m13 = this.m31;
		float m20 = this.m02;
		float m21 = this.m12;
		float m22 = this.m22;
		float m23 = this.m32;
		float m30 = this.m03;
		float m31 = this.m13;
		float m32 = this.m23;
		float m33 = this.m33;

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

	public Mat4 set(Vec3 right, Vec3 up, Vec3 dir){
		setIdentity();
//		m00 = right.x;
//		m01 = right.y;
//		m02 = right.z;
//		m10 = up.x;
//		m11 = up.y;
//		m12 = up.z;
//		m20 = dir.x;
//		m21 = dir.y;
//		m22 = dir.z;
		m00 = right.x;
		m10 = right.y;
		m20 = right.z;
		m01 = up.x;
		m11 = up.y;
		m21 = up.z;
		m02 = dir.x;
		m12 = dir.y;
		m22 = dir.z;

		return this;
	}

	public Mat4 setOne() {
		m00 = 1.0f;
		m01 = 1.0f;
		m02 = 1.0f;
		m03 = 1.0f;
		m10 = 1.0f;
		m11 = 1.0f;
		m12 = 1.0f;
		m13 = 1.0f;
		m20 = 1.0f;
		m21 = 1.0f;
		m22 = 1.0f;
		m23 = 1.0f;
		m30 = 1.0f;
		m31 = 1.0f;
		m32 = 1.0f;
		m33 = 1.0f;
		return this;
	}
}
