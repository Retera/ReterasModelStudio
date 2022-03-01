package com.hiveworkshop.wc3.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.mdl.Vertex;

public class MathUtils {
	/**
	 * Returns true if the value of <code>b</code> falls between the values
	 * <code>a</code> and <code>c</code>.
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static boolean isBetween(final double a, final double b, final double c) {
		final double min = Math.min(a, c);
		final double max = Math.max(a, c);
		return (min < b) && (b < max);
	}

	public static double lerp(final double a, final double b, final double t) {
		return a + (t * (b - a));
	}

	public static Vertex lerp(final Vertex out, final Vertex a, final Vertex b, final double t) {
		out.setTo(MathUtils.lerp(a.x, b.x, t), MathUtils.lerp(a.y, b.y, t), MathUtils.lerp(a.z, b.z, t));
		return out;
	}

	public static Vector4f lerp(final Vector4f out, final Vector4f a, final Vector4f b, final double t) {
		out.set((float) MathUtils.lerp(a.x, b.x, t), (float) MathUtils.lerp(a.y, b.y, t),
				(float) MathUtils.lerp(a.z, b.z, t), (float) MathUtils.lerp(a.w, b.w, t));
		return out;
	}

	public static double hermite(final double a, final double aOutTan, final double bInTan, final double b,
			final double t) {
		final double factorTimes2 = t * t;
		final double factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final double factor2 = (factorTimes2 * (t - 2)) + t;
		final double factor3 = factorTimes2 * (t - 1);
		final double factor4 = factorTimes2 * (3 - (2 * t));
		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}

	public static double bezier(final double a, final double aOutTan, final double bInTan, final double b,
			final double t) {
		final double invt = 1 - t;
		final double factorSquared = t * t;
		final double inverseFactorSquared = invt * invt;
		final double factor1 = inverseFactorSquared * invt;
		final double factor2 = 3 * t * inverseFactorSquared;
		final double factor3 = 3 * factorSquared * invt;
		final double factor4 = factorSquared * t;

		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}

	// copied from ghostwolf and
	// https://www.blend4web.com/api_doc/libs_gl-matrix2.js.html
	public static void fromRotationTranslationScaleOrigin(final Quaternion q, final Vector3f v, final Vector3f s,
			final Matrix4f out, final Vector3f pivot) {
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
		out.m00 = (1 - (yy + zz)) * sx;
		out.m01 = (xy + wz) * sx;
		out.m02 = (xz - wy) * sx;
		out.m03 = 0;
		out.m10 = (xy - wz) * sy;
		out.m11 = (1 - (xx + zz)) * sy;
		out.m12 = (yz + wx) * sy;
		out.m13 = 0;
		out.m20 = (xz + wy) * sz;
		out.m21 = (yz - wx) * sz;
		out.m22 = (1 - (xx + yy)) * sz;
		out.m23 = 0;
		out.m30 = (v.x + pivot.x) - ((out.m00 * pivot.x) + (out.m10 * pivot.y) + (out.m20 * pivot.z));
		out.m31 = (v.y + pivot.y) - ((out.m01 * pivot.x) + (out.m11 * pivot.y) + (out.m21 * pivot.z));
		out.m32 = (v.z + pivot.z) - ((out.m02 * pivot.x) + (out.m12 * pivot.y) + (out.m22 * pivot.z));
		out.m33 = 1;
	}

	// copied from
	// https://www.blend4web.com/api_doc/libs_gl-matrix2.js.html
	public static void fromRotationTranslationScale(final Quaternion q, final Vector3f v, final Vector3f s,
			final Matrix4f out) {
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
		out.m00 = (1 - (yy + zz)) * sx;
		out.m01 = (xy + wz) * sx;
		out.m02 = (xz - wy) * sx;
		out.m03 = 0;
		out.m10 = (xy - wz) * sy;
		out.m11 = (1 - (xx + zz)) * sy;
		out.m12 = (yz + wx) * sy;
		out.m13 = 0;
		out.m20 = (xz + wy) * sz;
		out.m21 = (yz - wx) * sz;
		out.m22 = (1 - (xx + yy)) * sz;
		out.m23 = 0;
		out.m30 = v.x;
		out.m31 = v.y;
		out.m32 = v.z;
		out.m33 = 1;
	}

	public static void fromQuat(final Quaternion q, final Matrix4f out) {
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
		out.m00 = 1 - yy - zz;
		out.m01 = yx + wz;
		out.m02 = zx - wy;
		out.m03 = 0;
		out.m10 = yx - wz;
		out.m11 = 1 - xx - zz;
		out.m12 = zy + wx;
		out.m13 = 0;
		out.m20 = zx + wy;
		out.m21 = zy - wx;
		out.m22 = 1 - xx - yy;
		out.m23 = 0;
		out.m30 = 0;
		out.m31 = 0;
		out.m32 = 0;
		out.m33 = 1;

	}

	public static float randomInRange(final double min, final double max) {
		return (float) (min + (Math.random() * (max - min)));
	}

	public static int uint8ToUint24(final byte right, final byte bottom, final byte a) {
		return ((right << 16) & 0xFF0000) | ((bottom << 8) & 0xFF00) | (a & 0xFF);
	}

	public static void setOrtho(Matrix4f matrix, final float left, final float right, final float bottom,
			final float top, final float near, final float far) {
		final float lr = 1 / (left - right);
		final float bt = 1 / (bottom - top);
		final float nf = 1 / (near - far);

		matrix.m00 = -2 * lr;
		matrix.m01 = 0;
		matrix.m02 = 0;
		matrix.m03 = 0;
		matrix.m10 = 0;
		matrix.m11 = -2 * bt;
		matrix.m12 = 0;
		matrix.m13 = 0;
		matrix.m20 = 0;
		matrix.m21 = 0;
		matrix.m22 = 2 * nf;
		matrix.m23 = 0;
		matrix.m30 = (left + right) * lr;
		matrix.m31 = (top + bottom) * bt;
		matrix.m32 = (far + near) * nf;
		matrix.m33 = 1;
	}

	public static void setPerspective(Matrix4f matrix, final float fovy, final float aspect, final float near, final float far) {
		final float f = 1 / (float) Math.tan(fovy / 2);

		matrix.m00 = f / aspect;
		matrix.m01 = 0;
		matrix.m02 = 0;
		matrix.m03 = 0;
		matrix.m10 = 0;
		matrix.m11 = f;
		matrix.m12 = 0;
		matrix.m13 = 0;
		matrix.m20 = 0;
		matrix.m21 = 0;
		matrix.m23 = -1;
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m33 = 0;

		if (!Float.isInfinite(far)) {
			final float nf = 1 / (near - far);

			matrix.m22 = (far + near) * nf;
			matrix.m32 = 2 * far * near * nf;
		} else {
			matrix.m22 = -1;
			matrix.m32 = -2 * near;
		}
	}

	public static void setPerspective(Matrix4f matrix,final float left, final float right, final float bottom, final float top, final float near, final float far) {
		float x = 2.0f * near / (right - left);
		float y = 2.0f * near / (top - bottom);
		float a = (right + left) / (right - left);
		float b = (top + bottom) / (top - bottom);
		float l_a1 = (far + near) / (near - far);
		float l_a2 = (2 * far * near) / (near - far);
		matrix.m00 = x;
		matrix.m01 = 0;
		matrix.m02 = 0;
		matrix.m03 = 0;
		matrix.m10 = 0;
		matrix.m11 = y;
		matrix.m12 = 0;
		matrix.m13 = 0;
		matrix.m20 = a;
		matrix.m21 = b;
		matrix.m22 = l_a1;
		matrix.m23 = -1;
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = l_a2;
		matrix.m33 = 0;
	}
}
