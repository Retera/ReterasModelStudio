package com.hiveworkshop.wc3.util;

import java.awt.geom.Rectangle2D;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.mdl.Vertex;

public class MathUtils {
	public static final Vector3f VEC3_UNIT_X = new Vector3f(1, 0, 0);
	public static final Vector3f VEC3_UNIT_Y = new Vector3f(0, 1, 0);
	public static final Vector3f VEC3_UNIT_Z = new Vector3f(0, 0, 1);

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
		return min < b && b < max;
	}

	public static double lerp(final double a, final double b, final double t) {
		return a + t * (b - a);
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
		final double factor1 = factorTimes2 * (2 * t - 3) + 1;
		final double factor2 = factorTimes2 * (t - 2) + t;
		final double factor3 = factorTimes2 * (t - 1);
		final double factor4 = factorTimes2 * (3 - 2 * t);
		return a * factor1 + aOutTan * factor2 + bInTan * factor3 + b * factor4;
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

		return a * factor1 + aOutTan * factor2 + bInTan * factor3 + b * factor4;
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
		out.m30 = v.x + pivot.x - (out.m00 * pivot.x + out.m10 * pivot.y + out.m20 * pivot.z);
		out.m31 = v.y + pivot.y - (out.m01 * pivot.x + out.m11 * pivot.y + out.m21 * pivot.z);
		out.m32 = v.z + pivot.z - (out.m02 * pivot.x + out.m12 * pivot.y + out.m22 * pivot.z);
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

	static void toMatrix(final Quaternion q, final Matrix4f out) {
		final float x = q.x, y = q.y, z = q.z, w = q.w;
		final float xx = x * x;
		final float xy = x * y;
		final float xz = x * z;
		final float xw = x * w;
		final float yy = y * y;
		final float yz = y * z;
		final float yw = y * w;
		final float zz = z * z;
		final float zw = z * w;
		// Set matrix from quaternion
		out.m00 = 1 - 2 * (yy + zz);
		out.m01 = 2 * (xy - zw);
		out.m02 = 2 * (xz + yw);
		out.m03 = 0;
		out.m10 = 2 * (xy + zw);
		out.m11 = 1 - 2 * (xx + zz);
		out.m12 = 2 * (yz - xw);
		out.m13 = 0;
		out.m20 = 2 * (xz - yw);
		out.m21 = 2 * (yz + xw);
		out.m22 = 1 - 2 * (xx + yy);
		out.m23 = 0;
		out.m30 = 0;
		out.m31 = 0;
		out.m32 = 0;
		out.m33 = 1;

	}

	public static float randomInRange(final double min, final double max) {
		return (float) (min + Math.random() * (max - min));
	}

	public static int uint8ToUint24(final byte right, final byte bottom, final byte a) {
		return right << 16 & 0xFF0000 | bottom << 8 & 0xFF00 | a & 0xFF;
	}

	public static void setOrtho(final Matrix4f matrix, final float left, final float right, final float bottom,
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

	public static void setPerspective(final Matrix4f matrix, final float fovy, final float aspect, final float near,
			final float far) {
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

	public static void setPerspective(final Matrix4f matrix, final float left, final float right, final float bottom,
			final float top, final float near, final float far) {
		final float x = 2.0f * near / (right - left);
		final float y = 2.0f * near / (top - bottom);
		final float a = (right + left) / (right - left);
		final float b = (top + bottom) / (top - bottom);
		final float l_a1 = (far + near) / (near - far);
		final float l_a2 = 2 * far * near / (near - far);
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

	public static double areaOfTriangle(final double x1, final double y1, final double x2, final double y2,
			final double x3, final double y3) {
		final double a = dist(x1, y1, x2, y2);
		final double b = dist(x2, y2, x3, y3);
		final double c = dist(x1, y1, x3, y3);
		final double s = (a + b + c) / 2;
		return Math.sqrt(Math.max(0, s * (s - a) * (s - b) * (s - c)));
	}

	public static double dist(final double x1, final double y1, final double x2, final double y2) {
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static void reflect(final Vector3f left, final Vector3f right, final Vector3f output) {
		output.set(right);
		output.scale(2.0f * Vector3f.dot(right, left));
		Vector3f.sub(left, output, output);
	}

	private static final Vector3f tempVec = new Vector3f();
	private static final Vector3f tempVecX = new Vector3f();
	private static final Vector3f tempVecY = new Vector3f();
	private static final Vector3f tempVecZ = new Vector3f();
	private static final Matrix4f tempMat4 = new Matrix4f();

	public static void setToLookAt(final Matrix4f out, final Vector3f location, final Vector3f point,
			final Vector3f worldUp) {
		Vector3f.sub(point, location, tempVec);
//		l_vez.set(direction).nor();
		tempVecZ.set(tempVec).normalise();
//		l_vex.set(direction).nor();
		tempVecX.set(tempVec).normalise();
		Vector3f.cross(tempVecX, worldUp, tempVecX);
		if (tempVecX.lengthSquared() <= 0) {
			out.setIdentity();
			System.err.println("bad setToLookAt: " + location + ", " + point + ", " + worldUp);
			return;
		}
		tempVecX.normalise();
//		l_vex.crs(up).nor();
		Vector3f.cross(tempVecX, tempVecZ, tempVecY);
		tempVecY.normalise();
//		l_vey.set(l_vex).crs(l_vez).nor();
//		idt();
		out.setIdentity();
		out.m00 = tempVecX.x;
		out.m01 = tempVecX.y;
		out.m02 = tempVecX.z;
		out.m10 = tempVecY.x;
		out.m11 = tempVecY.y;
		out.m12 = tempVecY.z;
		out.m20 = -tempVecZ.x;
		out.m21 = -tempVecZ.y;
		out.m22 = -tempVecZ.z;

		tempMat4.setIdentity();
		tempMat4.translate(location);

		Matrix4f.mul(out, tempMat4, out);

	}

	public static void conjugate(final Quaternion q) {
		q.x = -q.x;
		q.y = -q.y;
		q.z = -q.z;
	}

	public static void unpackPlanes(final Vector4f[] planes, final Matrix4f m) {
		final float a00 = m.m00, a01 = m.m01, a02 = m.m02, a03 = m.m03, a10 = m.m10, a11 = m.m11, a12 = m.m12,
				a13 = m.m13, a20 = m.m20, a21 = m.m21, a22 = m.m22, a23 = m.m23, a30 = m.m30, a31 = m.m31, a32 = m.m32,
				a33 = m.m33;

		// Left clipping plane
		Vector4f plane = planes[0];
		plane.x = a30 + a00;
		plane.y = a31 + a01;
		plane.z = a32 + a02;
		plane.w = a33 + a03;

		// Right clipping plane
		plane = planes[1];
		plane.x = a30 - a00;
		plane.y = a31 - a01;
		plane.z = a32 - a02;
		plane.w = a33 - a03;

		// Top clipping plane
		plane = planes[2];
		plane.x = a30 - a10;
		plane.y = a31 - a11;
		plane.z = a32 - a12;
		plane.w = a33 - a13;

		// Bottom clipping plane
		plane = planes[3];
		plane.x = a30 + a10;
		plane.y = a31 + a11;
		plane.z = a32 + a12;
		plane.w = a33 + a13;

		// Near clipping plane
		plane = planes[4];
		plane.x = a30 + a20;
		plane.y = a31 + a21;
		plane.z = a32 + a22;
		plane.w = a33 + a23;

		// Far clipping plane
		plane = planes[5];
		plane.x = a30 - a20;
		plane.y = a31 - a21;
		plane.z = a32 - a22;
		plane.w = a33 - a23;

		normalizePlane(planes[0], planes[0]);
		normalizePlane(planes[1], planes[1]);
		normalizePlane(planes[2], planes[2]);
		normalizePlane(planes[3], planes[3]);
		normalizePlane(planes[4], planes[4]);
		normalizePlane(planes[5], planes[5]);
	}

	public static void normalizePlane(final Vector4f out, final Vector4f plane) {
		tempVec.set(plane.x, plane.y, plane.z);
		final float len = tempVec.length();

		out.x = plane.x / len;
		out.y = plane.y / len;
		out.z = plane.z / len;
		out.w = plane.w / len;
	}

	public static float distanceToPlane(final Vector4f plane, final Vector3f point) {
		return plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
	}

	private static Quaternion tempQuat1 = new Quaternion();
	private static Quaternion tempQuat2 = new Quaternion();

	public static Vector3f transform(final Quaternion rotation, final Vector3f vector) {
		tempQuat2.set(rotation);
		conjugate(tempQuat2);
		tempQuat1.set(vector.x, vector.y, vector.z, 0);
		Quaternion.mul(tempQuat1, tempQuat2, tempQuat2);
		Quaternion.mul(rotation, tempQuat2, tempQuat2);

		vector.x = tempQuat2.x;
		vector.y = tempQuat2.y;
		vector.z = tempQuat2.z;

		return vector;
	}

	/**
	 * Multiplies the vector v by the given matrix dividing by w, assuming the
	 * fourth (w) component of the vector is 1. This is mostly used to
	 * project/unproject vectors via a perspective projection matrix. (copied from
	 * LibGDX Vector3 class)
	 */
	public static void project(final Matrix4f m, final Vector3f v, final Vector3f out) {
		final float l_w = 1f / (v.x * m.m30 + v.y * m.m31 + v.z * m.m32 + m.m33);
		out.set((v.x * m.m00 + v.y * m.m01 + v.z * m.m02 + m.m03) * l_w,
				(v.x * m.m10 + v.y * m.m11 + v.z * m.m12 + m.m13) * l_w,
				(v.x * m.m20 + v.y * m.m21 + v.z * m.m22 + m.m23) * l_w);

	}

	private static final Vector4f heap = new Vector4f();

	public static Vector3f unproject(final Vector3f out, final Vector3f v, final Matrix4f inverseMatrix,
			final Rectangle2D.Float viewport) {
		final float x = 2 * (v.x - viewport.x) / viewport.width - 1;
		final float y = 2 * (v.y - viewport.y) / viewport.height - 1;
		final float z = 2 * v.z - 1;

		heap.set(x, y, z, 1);
		Matrix4f.transform(inverseMatrix, heap, heap);
		out.set(heap.x / heap.w, heap.y / heap.w, heap.z / heap.w);

		return out;
	}

	public static void copy(final Matrix4f in, final Matrix3f out) {
		out.m00 = in.m00;
		out.m01 = in.m01;
		out.m02 = in.m02;
		out.m10 = in.m10;
		out.m11 = in.m11;
		out.m12 = in.m12;
		out.m20 = in.m20;
		out.m21 = in.m21;
		out.m22 = in.m22;
	}
}
