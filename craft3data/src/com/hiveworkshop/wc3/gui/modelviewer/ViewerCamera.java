package com.hiveworkshop.wc3.gui.modelviewer;

import java.awt.geom.Rectangle2D;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.util.MathUtils;

/**
 * A camera class, ported from Warsmash which was ported from mdx-m3-viewer
 */
public class ViewerCamera {
	private static final Vector3f vectorHeap = new Vector3f();
	private static final Vector3f vectorHeap2 = new Vector3f();
	private static final Vector3f vectorHeap3 = new Vector3f();
	private static final Quaternion quatHeap = new Quaternion();
	private static final Matrix4f matHeap = new Matrix4f();

	public final Rectangle2D.Float rect;

	private boolean isPerspective;
	private float fov;
	private float aspect;

	public boolean isOrtho;
	private float leftClipPlane;
	private float rightClipPlane;
	private float bottomClipPlane;
	private float topClipPlane;

	private float nearClipPlane;
	private float farClipPlane;

	public final Vector3f location;
	public final Quaternion rotation;

	public Quaternion inverseRotation;
	/**
	 * World -> View.
	 */
	public final Matrix4f viewMatrix;
	/**
	 * View -> Clip.
	 */
	private final Matrix4f projectionMatrix;
	/**
	 * World -> Clip.
	 */
	public final Matrix4f viewProjectionMatrix;
	/**
	 * View -> World.
	 */
	private final Matrix4f inverseViewMatrix;
	/**
	 * Clip -> World.
	 */
	private final Matrix4f inverseViewProjectionMatrix;
	public final Vector3f directionX;
	public final Vector3f directionY;
	public final Vector3f directionZ;
	public final Vector3f[] vectors;
	public final Vector3f[] billboardedVectors;

	public final Vector4f[] planes;
	private boolean dirty;

	public ViewerCamera() {
		// rencered viewport
		this.rect = new Rectangle2D.Float();

		// perspective values
		this.isPerspective = true;
		this.fov = 0;
		this.aspect = 0;

		// Orthogonal values
		this.isOrtho = false;
		this.leftClipPlane = 0f;
		this.rightClipPlane = 0f;
		this.bottomClipPlane = 0f;
		this.topClipPlane = 0f;

		// Shared values
		this.nearClipPlane = 0f;
		this.farClipPlane = 0f;

		// World values
		this.location = new Vector3f();
		this.rotation = new Quaternion();

		// Derived values.
		this.inverseRotation = new Quaternion();
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		this.viewProjectionMatrix = new Matrix4f();
		this.inverseViewMatrix = new Matrix4f();
		this.inverseViewProjectionMatrix = new Matrix4f();
		this.directionX = new Vector3f();
		this.directionY = new Vector3f();
		this.directionZ = new Vector3f();

		// First four vectors are the corners of a 2x2 rectangle, the last three vectors
		// are the unit axes
		this.vectors = new Vector3f[] { new Vector3f(-1, -1, 0), new Vector3f(-1, 1, 0), new Vector3f(1, 1, 0),
				new Vector3f(1, -1, 0), new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1) };

		// First four vectors are the corners of a 2x2 rectangle billboarded to the
		// camera, the last three vectors are the unit axes billboarded
		this.billboardedVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
				new Vector3f(), new Vector3f(), new Vector3f() };

		// Left, right, top, bottom, near, far
		this.planes = new Vector4f[] { new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(),
				new Vector4f() };

		this.dirty = true;
	}

	public void perspective(final float fov, final float aspect, final float near, final float far) {
		this.isPerspective = true;
		this.isOrtho = false;
		this.fov = fov;
		this.aspect = aspect;
		this.nearClipPlane = near;
		this.farClipPlane = far;

		this.dirty = true;
	}

	public void ortho(final float left, final float right, final float bottom, final float top, final float near,
			final float far) {
		this.isPerspective = false;
		this.isOrtho = true;
		this.leftClipPlane = left;
		this.rightClipPlane = right;
		this.bottomClipPlane = bottom;
		this.topClipPlane = top;
		this.nearClipPlane = near;
		this.farClipPlane = far;

		this.dirty = true;
	}

	public void viewport(final Rectangle2D.Float viewport) {
		viewport(viewport.x, viewport.y, viewport.width, viewport.height);
	}

	public void viewport(final float x, final float y, final float width, final float height) {
		this.rect.setRect(x, y, width, height);

		this.aspect = width / height;

		this.dirty = true;
	}

	public float getAspect() {
		return this.aspect;
	}

	public void setLocation(final Vector3f location) {
		this.location.set(location);

		this.dirty = true;
	}

	public void move(final Vector3f offset) {
		Vector3f.add(location, offset, location);

		this.dirty = true;
	}

	public void setRotation(final Quaternion rotation) {
		this.rotation.set(rotation);

		this.dirty = true;
	}

	public void rotate(final Quaternion rotation) {
		Quaternion.mul(this.rotation, rotation, this.rotation);

		this.dirty = true;
	}

	public void setRotationAngles(final float horizontalAngle, final float verticalAngle) {
		this.rotation.setIdentity();
//		this.rotateAngles(horizontalAngle, verticalAngle);
		throw new UnsupportedOperationException(
				"Ghostwolf called a function that does not exist, so I did not know what to do here");
	}

	public void face(final Vector3f point, final Vector3f worldUp) {
		MathUtils.setToLookAt(matHeap, this.location, point, worldUp);
		this.rotation.setFromMatrix(matHeap);

		this.dirty = true;
	}

	public void moveToAndFace(final Vector3f location, final Vector3f target, final Vector3f worldUp) {
		this.location.set(location);
		this.face(target, worldUp);
	}

	public void reset() {
		this.location.set(0, 0, 0);
		this.rotation.setIdentity();

		this.dirty = true;
	}

	public void update() {
		if (this.dirty) {
			final Vector3f location = this.location;
			final Quaternion rotation = this.rotation;
			final Quaternion inverseRotation = this.inverseRotation;
			final Matrix4f viewMatrix = this.viewMatrix;
			final Matrix4f projectionMatrix = this.projectionMatrix;
			final Matrix4f viewProjectionMatrix = this.viewProjectionMatrix;
			final Vector3f[] vectors = this.vectors;
			final Vector3f[] billboardedVectors = this.billboardedVectors;

			if (this.isPerspective) {
				MathUtils.setPerspective(projectionMatrix, this.fov, this.aspect, this.nearClipPlane,
						this.farClipPlane);
			}
			else {
				MathUtils.setOrtho(projectionMatrix, this.leftClipPlane, this.rightClipPlane, this.bottomClipPlane,
						this.topClipPlane, this.nearClipPlane, this.farClipPlane);
			}

			MathUtils.fromQuat(rotation, viewMatrix);
			vectorHeap.set(location).scale(-1);
			viewMatrix.translate(vectorHeap);
			inverseRotation.set(rotation);
			MathUtils.conjugate(inverseRotation);

			// World projection matrix
			// World space -> NDC space
			Matrix4f.mul(projectionMatrix, viewMatrix, viewProjectionMatrix);

			// Recalculate the camera's frustum planes
			MathUtils.unpackPlanes(this.planes, viewProjectionMatrix);

			// Inverse world matrix
			// Camera space -> world space
			Matrix4f.invert(viewMatrix, this.inverseViewMatrix);

			this.directionX.set(MathUtils.VEC3_UNIT_X);
			MathUtils.transform(inverseRotation, this.directionX);
			this.directionY.set(MathUtils.VEC3_UNIT_Y);
			MathUtils.transform(inverseRotation, this.directionY);
			this.directionZ.set(MathUtils.VEC3_UNIT_Z);
			MathUtils.transform(inverseRotation, this.directionZ);

			// Inverse world projection matrix
			// NDC space -> World space
			Matrix4f.invert(viewProjectionMatrix, inverseViewProjectionMatrix);

			for (int i = 0; i < 7; i++) {
				billboardedVectors[i].set(vectors[i]);
				MathUtils.transform(inverseRotation, billboardedVectors[i]);
			}
			this.dirty = false;
		}
	}

	public boolean testSphere(final Vector3f center, final float radius) {
		for (final Vector4f plane : this.planes) {
			if (MathUtils.distanceToPlane(plane, center) <= -radius) {
				return false;
			}
		}
		return true;
	}

	public Vector3f cameraToWorld(final Vector3f out, final Vector3f v) {
		MathUtils.project(this.inverseViewMatrix, v, out);
		return out;
	}

	public Vector3f worldToCamera(final Vector3f out, final Vector3f v) {
		MathUtils.project(this.viewMatrix, v, out);
		return out;
	}

	public Vector2f worldToScreen(final Vector2f out, final Vector3f v, final float screenHeightCheese) {
		final Rectangle2D.Float viewport = this.rect;

		MathUtils.project(this.viewProjectionMatrix, v, vectorHeap);

		out.x = Math.round(((vectorHeap.x + 1) / 2) * viewport.width);
		out.y = ((screenHeightCheese - viewport.y - viewport.height) + (viewport.height))
				- Math.round(((vectorHeap.y + 1) / 2) * viewport.height);

		return out;
	}

	public float[] screenToWorldRay(final float[] out, final Vector2f v) {
		final Vector3f a = vectorHeap;
		final Vector3f b = vectorHeap2;
		final Vector3f c = vectorHeap3;
		final float x = v.x;
		final float y = v.y;
		final Rectangle2D.Float viewport = this.rect;

		// Intersection on the near-plane
		c.set(x, y, 0);
		MathUtils.unproject(a, c, this.inverseViewProjectionMatrix, viewport);

		// Intersection on the far-plane
		c.set(x, y, 1);
		MathUtils.unproject(b, c, this.inverseViewProjectionMatrix, viewport);

		out[0] = a.x;
		out[1] = a.y;
		out[2] = a.z;
		out[3] = b.x;
		out[4] = b.y;
		out[5] = b.z;

		return out;
	}
}
