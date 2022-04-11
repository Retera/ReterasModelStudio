package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.*;

import java.awt.geom.Rectangle2D;

public class ViewerCamera {
	private static final Vec3 vectorHeap = new Vec3();
	private static final Vec3 vectorHeap2 = new Vec3();
	private static final Vec3 vectorHeap3 = new Vec3();
	private static final Quat quatHeap = new Quat();
	private static final Mat4 matHeap = new Mat4();

	private static final Vec3 tempVec  = new Vec3();
	private static final Vec3 tempVecX = new Vec3();
	private static final Vec3 tempVecY = new Vec3();
	private static final Vec3 tempVecZ = new Vec3();
	private static final Mat4 tempMat4 = new Mat4();

	// rencered viewport
	public final Rectangle2D.Float rect =  new Rectangle2D.Float();

	// perspective values
	private float fov = 45;
	private float aspect = 1;

	// Orthogonal values
	private boolean isOrtho = false;
	private float leftClipPlane = 0f;
	private float rightClipPlane = 0f;
	private float bottomClipPlane = 0f;
	private float topClipPlane = 0f;

	// Shared values
	private float nearClipPlane = 0.001f;
	private float farClipPlane = 20000f;

	// World values
	public final Vec3 location = new Vec3();
	public final Quat rotation = new Quat();

	// Derived values.
	public Quat inverseRotation = new Quat();

	private final Mat4 viewMatrix = new Mat4();                  // World -> View
	private final Mat4 projectionMatrix = new Mat4();            // View -> Clip
	private final Mat4 viewProjectionMatrix = new Mat4();        // World -> Clip
	private final Mat4 inverseViewMatrix = new Mat4();           // View -> World
	private final Mat4 inverseViewProjectionMatrix = new Mat4(); // Clip -> World

	public final Vec3 directionX = new Vec3();
	public final Vec3 directionY = new Vec3();
	public final Vec3 directionZ = new Vec3();

	public final Vec3[] vectors;
	public final Vec3[] billboardedVectors;

	public final Vec4[] planes;
	private boolean dirty;

	public ViewerCamera() {

		// First four vectors are the corners of a 2x2 rectangle, the last three vectors
		// are the unit axes
		this.vectors = new Vec3[] {
				new Vec3(-1, -1, 0),
				new Vec3(-1,  1, 0),
				new Vec3( 1,  1, 0),
				new Vec3( 1, -1, 0),
				new Vec3( 1,  0, 0),
				new Vec3( 0,  1, 0),
				new Vec3( 0,  0, 1) };

		// First four vectors are the corners of a 2x2 rectangle billboarded to the
		// camera, the last three vectors are the unit axes billboarded
		this.billboardedVectors = new Vec3[] { new Vec3(), new Vec3(), new Vec3(), new Vec3(), new Vec3(), new Vec3(), new Vec3() };

		// Left, right, top, bottom, near, far
		this.planes = new Vec4[] { new Vec4(), new Vec4(), new Vec4(), new Vec4(), new Vec4(), new Vec4() };

		this.dirty = true;
	}

	public void perspective(float fov, float aspect, float near, float far) {
		this.isOrtho = false;
		this.fov = fov;
		this.aspect = aspect;
		this.nearClipPlane = near;
		this.farClipPlane = far;

		this.dirty = true;
	}

	public void ortho(float left, float right, float bottom, float top, float near, float far) {
		this.isOrtho = true;
		this.leftClipPlane = left;
		this.rightClipPlane = right;
		this.bottomClipPlane = bottom;
		this.topClipPlane = top;
		this.nearClipPlane = near;
		this.farClipPlane = far;

		this.dirty = true;
	}

	public ViewerCamera setOrtho() {
		float ortoFac = 4.0f * (150 / location.x);
		float w = (float) (rect.getWidth() / 2.0f / ortoFac);
		float h = (float) (rect.getHeight() / 2.0f / ortoFac);
		ortho(-w, w, -h, h, -6000.0f, 16000.0f);
		return this;
	}

	public ViewerCamera setPerspective() {
		perspective(70, aspect, nearClipPlane, farClipPlane);
		return this;
	}

	public boolean isOrtho() {
		return isOrtho;
	}

	public void viewport(Rectangle2D.Float viewport) {
		viewport(viewport.x, viewport.y, viewport.width, viewport.height);
	}

	public void viewport(float x, float y, float width, float height) {
		this.rect.setRect(x, y, width, height);

		this.aspect = width / height;

		this.dirty = true;
	}

	public float getAspect() {
		return this.aspect;
	}

	public void setLocation(Vec3 location) {
		this.location.set(location);

		this.dirty = true;
	}

	public void move(Vec3 offset) {
		location.add(offset);

		this.dirty = true;
	}

	public void setCameraRotation(float right, float up) {
		Quat xRot = new Quat(Vec3.X_AXIS, (float) Math.toRadians(up));
		Quat yRot = new Quat(Vec3.Y_AXIS, (float) Math.toRadians(right));

		setRotation(xRot.mul(yRot));
	}

	public void setRotation(Quat rotation) {
		this.rotation.set(rotation);

		this.dirty = true;
	}

	public void rotate(Quat rotation) {
		this.rotation.mul(rotation);

		this.dirty = true;
	}

	public void setRotationAngles(float horizontalAngle, float verticalAngle) {
		this.rotation.setIdentity();
//		this.rotateAngles(horizontalAngle, verticalAngle);
		throw new UnsupportedOperationException("Ghostwolf called a function that does not exist, so I did not know what to do here");
	}

	public void face(Vec3 point, Vec3 worldUp) {
		setToLookAt(matHeap, this.location, point, worldUp);
		this.rotation.setFromMat(matHeap);

		this.dirty = true;
	}

	public static void setToLookAt(Mat4 out, Vec3 location, Vec3 point, Vec3 worldUp) {
		tempVec.set(point).sub(location).normalize();
		tempVecZ.set(tempVec);
		tempVecX.set(tempVec);
		tempVecX.cross(worldUp);
		if (tempVecX.lengthSquared() <= 0) {
			out.setIdentity();
			System.err.println("bad setToLookAt: " + location + ", " + point + ", " + worldUp);
			return;
		}
		tempVecX.normalize();
		tempVecY.set(tempVecX).cross(tempVecZ).normalize();

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

		out.mul(tempMat4);
	}

	public void moveToAndFace(Vec3 location, Vec3 target, Vec3 worldUp) {
		this.location.set(location);
		this.face(target, worldUp);
	}

	public void reset() {
		this.location.set(0, 0, 0);
		this.rotation.setIdentity();

		this.dirty = true;
	}

	public void update() {
		if (dirty) {
			if (!isOrtho) {
				projectionMatrix.setPerspective(fov, aspect, nearClipPlane, farClipPlane);
			} else {
				projectionMatrix.setOrtho(leftClipPlane, rightClipPlane, bottomClipPlane, topClipPlane, nearClipPlane, farClipPlane);
			}

			vectorHeap.set(location).scale(-1);
			viewMatrix.fromQuat(rotation).translate(vectorHeap);
//			MathUtils.conjugate(inverseRotation);

			// World projection matrix
			// World space -> NDC space
			viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);

			// Recalculate the camera's frustum planes
			unpackPlanes(this.planes, viewProjectionMatrix);
//			viewProjectionMatrix.unpackPlanes(this.planes)

			// Inverse world matrix
			// Camera space -> world space
			inverseViewMatrix.set(viewMatrix).invert();

			inverseRotation.set(rotation).invertRotation();
			directionX.set(Vec3.X_AXIS);
			directionX.transform(inverseRotation);
			directionY.set(Vec3.Y_AXIS);
			directionY.transform(inverseRotation);
			directionZ.set(Vec3.Z_AXIS);
			directionZ.transform(inverseRotation);

			// Inverse world projection matrix
			// NDC space -> World space
			inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();

			for (int i = 0; i < 7; i++) {
				billboardedVectors[i].set(vectors[i]);
				billboardedVectors[i].transform(inverseRotation);
			}
			this.dirty = false;
		}
	}

	public Mat4 getViewProjectionMatrix() {
		return viewProjectionMatrix;
	}

	public boolean testSphere(Vec3 center, float radius) {
		for (Vec4 plane : this.planes) {
			if (distanceToPlane(plane, center) <= -radius) {
				return false;
			}
		}
		return true;
	}

	public Vec3 cameraToWorld(Vec3 out, Vec3 v) {
		project(this.inverseViewMatrix, v, out);

		return out;
	}

	public Vec3 worldToCamera(Vec3 out, Vec3 v) {
		project(this.viewMatrix, v, out);
		return out;
	}

	public Vec2 worldToScreen(Vec2 out, Vec3 v, float screenHeightCheese) {
		Rectangle2D.Float viewport = this.rect;

		project(this.viewProjectionMatrix, v, vectorHeap);

		out.x = Math.round(((vectorHeap.x + 1) / 2) * viewport.width);
		out.y = ((screenHeightCheese - viewport.y - viewport.height) + (viewport.height))
				- Math.round(((vectorHeap.y + 1) / 2) * viewport.height);

		return out;
	}

	public float[] screenToWorldRay(float[] out, Vec2 v) {
		Vec3 a = vectorHeap;
		Vec3 b = vectorHeap2;
		Vec3 c = vectorHeap3;
		float x = v.x;
		float y = v.y;
		Rectangle2D.Float viewport = this.rect;

		// Intersection on the near-plane
		c.set(x, y, 0);
		unproject(a, c, this.inverseViewProjectionMatrix, viewport);

		// Intersection on the far-plane
		c.set(x, y, 1);
		unproject(b, c, this.inverseViewProjectionMatrix, viewport);

		out[0] = a.x;
		out[1] = a.y;
		out[2] = a.z;
		out[3] = b.x;
		out[4] = b.y;
		out[5] = b.z;

		return out;
	}

	/**
	 * Multiplies the vector v by the given matrix dividing by w, assuming the
	 * fourth (w) component of the vector is 1. This is mostly used to
	 * project/unproject vectors via a perspective projection matrix. (copied from
	 * LibGDX Vector3 class)
	 */
	public static void project(Mat4 m, Vec3 v, Vec3 out) {
		float l_w = 1f / ((v.x * m.m30) + (v.y * m.m31) + (v.z * m.m32) + m.m33);
		out.set(((v.x * m.m00) + (v.y * m.m01) + (v.z * m.m02) + m.m03) * l_w,
				((v.x * m.m10) + (v.y * m.m11) + (v.z * m.m12) + m.m13) * l_w,
				((v.x * m.m20) + (v.y * m.m21) + (v.z * m.m22) + m.m23) * l_w);

	}
	private static final Vec4 heap = new Vec4();

	public static Vec3 unproject(Vec3 out, Vec3 v, Mat4 inverseMatrix, Rectangle2D.Float viewport) {
		float x = ((2 * (v.x - viewport.x)) / viewport.width) - 1;
		float y = ((2 * (v.y - viewport.y)) / viewport.height) - 1;
		float z = (2 * v.z) - 1;

		heap.set(x, y, z, 1);
		heap.transform(inverseMatrix);
		out.set(heap.x / heap.w, heap.y / heap.w, heap.z / heap.w);

		return out;
	}

	public static float distanceToPlane(Vec4 plane, Vec3 point) {
		return (plane.x * point.x) + (plane.y * point.y) + (plane.z * point.z) + plane.w;
	}


	public static void unpackPlanes(Vec4[] planes, Mat4 m) {
		// Left clipping plane
		Vec4 plane = planes[0];
		plane.x = m.m30 + m.m00;
		plane.y = m.m31 + m.m01;
		plane.z = m.m32 + m.m02;
		plane.w = m.m33 + m.m03;

		// Right clipping plane
		plane = planes[1];
		plane.x = m.m30 - m.m00;
		plane.y = m.m31 - m.m01;
		plane.z = m.m32 - m.m02;
		plane.w = m.m33 - m.m03;

		// Top clipping plane
		plane = planes[2];
		plane.x = m.m30 - m.m10;
		plane.y = m.m31 - m.m11;
		plane.z = m.m32 - m.m12;
		plane.w = m.m33 - m.m13;

		// Bottom clipping plane
		plane = planes[3];
		plane.x = m.m30 + m.m10;
		plane.y = m.m31 + m.m11;
		plane.z = m.m32 + m.m12;
		plane.w = m.m33 + m.m13;

		// Near clipping plane
		plane = planes[4];
		plane.x = m.m30 + m.m20;
		plane.y = m.m31 + m.m21;
		plane.z = m.m32 + m.m22;
		plane.w = m.m33 + m.m23;

		// Far clipping plane
		plane = planes[5];
		plane.x = m.m30 - m.m20;
		plane.y = m.m31 - m.m21;
		plane.z = m.m32 - m.m22;
		plane.w = m.m33 - m.m23;

		normalizePlane(planes[0], planes[0]);
		normalizePlane(planes[1], planes[1]);
		normalizePlane(planes[2], planes[2]);
		normalizePlane(planes[3], planes[3]);
		normalizePlane(planes[4], planes[4]);
		normalizePlane(planes[5], planes[5]);
	}

	public static void normalizePlane(Vec4 out, Vec4 plane) {
		tempVec.set(plane.x, plane.y, plane.z);
		float len = tempVec.length();

		out.x = plane.x / len;
		out.y = plane.y / len;
		out.z = plane.z / len;
		out.w = plane.w / len;
	}
}