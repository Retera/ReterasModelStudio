package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.render3d.NGGLDP;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseWheelEvent;


public class CameraHandler {
	private final double ZOOM_FACTOR = 1.07;
	//	private final Vec3 cameraPos1 = new Vec3(0, 0, 0);
	private final Vec3 cameraPos = new Vec3(0, 0, 0);
	//	private final Vec3 cameraLookAt = new Vec3(0, 0, 0);
	private final Vec3 glCamTrans = new Vec3();
	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotZSpinZ = new Quat();

//	private final Quat inverseCameraRotation = new Quat();
//	private final Quat inverseCameraRotXSpinX = new Quat();
//	private final Quat inverseCameraRotXSpinY = new Quat();
//	private final Quat inverseCameraRotXSpinZ = new Quat();
//	private final Quat inverseCameraRotYSpinX = new Quat();
//	private final Quat inverseCameraRotYSpinY = new Quat();
//	private final Quat inverseCameraRotYSpinZ = new Quat();
//	private final Quat inverseCameraRotZSpinX = new Quat();
//	private final Quat inverseCameraRotZSpinY = new Quat();
//	private final Quat inverseCameraRotZSpinZ = new Quat();
//	private final Quat inverseCameraRot_XY_mulZX = new Quat();

	private double m_zoom = 1;

	private final Mat4 viewPortAntiRotMat = new Mat4();

	private ViewportActivityManager activityManager;
	private float xAngle;
	private float yAngle;
	private float zAngle;
	private boolean ortho = false;
	private boolean allowToggleOrtho = true;
	private boolean allowRotation = true;

	private final Component viewport;

	public CameraHandler(Component viewport) {
		this.viewport = viewport;
	}

	public CameraHandler resetZoom() {
		m_zoom = .2f;
		return this;
	}

	public CameraHandler loadDefaultCameraFor(double boundsRadius) {
		m_zoom = .2f;

//		System.out.println("boundsRadius: " + boundsRadius + " (zoom : 128/" + boundsRadius + "/1.3=" + m_zoom + ", X-pos: " + boundsRadius + "/4=" + (boundsRadius / 4) + ")");

		setViewportCamera((int) (boundsRadius / 3), (int) (boundsRadius / 20), 0, 20, -25);
//		System.out.println("X dist: " + (int) -(boundsRadius / 3) + ", X dist: " + (int) (boundsRadius / 20));
//		setViewportCamera((int) 0, (int) 0, 0, 0, 0);
		return this;
	}

	public void setUpCamera() {
		if (ortho) {
			float ortoFac = 4.0f * (150 / cameraPos.x);
			float w = viewport.getWidth() / 2.0f / ortoFac;
			float h = viewport.getHeight() / 2.0f / ortoFac;
			NGGLDP.pipeline.glOrtho(-w, w, -h, h, -6000.0f, 16000.0f);
		} else {
			NGGLDP.pipeline.gluPerspective(45f, (float) viewport.getWidth() / (float) viewport.getHeight(), 5f, 16000.0f);
			
		}

//		glCamTrans.set(cameraPos);

		// Rotating camera to have +Z up and +X as forward (pointing into camera)
		NGGLDP.pipeline.glRotatef(-90, 1f, 0f, 0f);
		NGGLDP.pipeline.glRotatef(-90, 0f, 0f, 1f);


//		GLU.gluLookAt(cameraPos.x, -cameraPos.y, -cameraPos.z, cameraLookAt.x, -cameraLookAt.y, -cameraLookAt.z, 0,0,1);

		NGGLDP.pipeline.glTranslatef(-cameraPos.x, -cameraPos.y, -cameraPos.z);
//
		NGGLDP.pipeline.glRotatef(xAngle, 1f, 0f, 0f);
		NGGLDP.pipeline.glRotatef(yAngle, 0f, 1f, 0f);
		NGGLDP.pipeline.glRotatef(zAngle, 0f, 0f, 1f);
		NGGLDP.pipeline.glScalef((float) m_zoom, (float) m_zoom, (float) m_zoom);
	}

	public void setViewportCamera(int dist, int height, int rX, int rY, int rZ) {
		xAngle = rX;
		yAngle = rY;
		zAngle = rZ;

//		cameraLookAt.set(0,0,height);
		cameraPos.set(dist, 0, height);
//		cameraPos1.set(dist, 0, 0);
		calculateCameraRotation();

	}

	public void zoom(double amount) {
		m_zoom *= 1 + amount;
	}

	public void doZoom(MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		int dir = wr < 0 ? -1 : 1;

		for (int i = 0; i < wr * dir; i++) {
			if (dir == -1) {
				m_zoom *= ZOOM_FACTOR;
				cameraPos.y *= ZOOM_FACTOR;
				cameraPos.z *= ZOOM_FACTOR;
//				cameraPos.scale(cameraLookAt, (float) ZOOM_FACTOR);
			} else {
				m_zoom /= ZOOM_FACTOR;
				cameraPos.y /= ZOOM_FACTOR;
				cameraPos.z /= ZOOM_FACTOR;
//				cameraPos.scale(cameraLookAt, (float) (1/ZOOM_FACTOR));
			}
		}
	}

	public void setPosition(double a, double b) {
		cameraPos.x = (float) a;
		cameraPos.y = (float) b;
	}

	public void translate(double right, double up) {
//		cameraPos.y += right;
//		cameraPos.z += up;
//		cameraPos.y += right * (1f / m_zoom);
//		cameraPos.z += up * (1f / m_zoom);
//		cameraPos.y += right * (1f / m_zoom) * cameraPos.x / 600f;
//		cameraPos.z += up * (1f / m_zoom) * cameraPos.x / 600f;
		cameraPos.y += right * cameraPos.x / 600f;
		cameraPos.z += up * cameraPos.x / 600f;

//		cameraLookAt.y -= right * cameraPos.x / 600f;
//		cameraLookAt.z -= up * cameraPos.x / 600f;

	}

	public void translate2(double x, double y, double z) {
//		cameraPos.y += left;
//		cameraPos.z += up;
//		cameraPos.y += left * (1f / m_zoom);
//		cameraPos.z += up * (1f / m_zoom);
//		cameraPos.y += left * (1f / m_zoom) * cameraPos.x / 600f;
//		cameraPos.z += up * (1f / m_zoom) * cameraPos.x / 600f;
		cameraPos.x += x;
		cameraPos.y += y;
		cameraPos.z += z;
		System.out.println(cameraPos);

//		cameraLookAt.y -= left * cameraPos.x / 600f;
//		cameraLookAt.z -= up * cameraPos.x / 600f;

	}

	public void rotate(double right, double up) {
		if (allowRotation) {
			zAngle += right;
			yAngle += up;


//			cameraPos.rotate(cameraPos, Math.toRadians(up), (byte) 0, (byte) 2);
//			cameraPos.rotate(cameraPos, Math.toRadians(right), (byte) 1, (byte) 2);
			calculateCameraRotation();
		} else {
			translate(-right, up);
		}
	}

	public void setCameraRotation(float right, float up) {
		if (allowRotation) {
			zAngle = right;
			yAngle = up;
			calculateCameraRotation();
		}
	}

	public void rot(float rx, float ry, float rz) {
		xAngle += rx;
		yAngle += ry;
		zAngle += rz;
		System.out.println(""
				+ "xAngle: " + xAngle
				+ ", yAngle: " + yAngle
				+ ", zAngle: " + zAngle);
		calculateCameraRotation();

//		cameraPos.rotate(cameraPos, Math.toRadians(ry), (byte) 0, (byte) 2);
//		cameraPos.rotate(cameraPos, Math.toRadians(rz), (byte) 1, (byte) 2);
	}

	public double getZoom() {
		return m_zoom;
	}

	public float getXAngle() {
		return xAngle;
	}

	public float getYAngle() {
		return yAngle;
	}

	public float getZAngle() {
		return zAngle;
	}

	public Vec2 getDisplayOffset() {
		return new Vec2(cameraPos.x, cameraPos.y);
	}

	public double convertX(double x) {
		return ((x + cameraPos.x) * m_zoom) + (viewport.getWidth() / 2.0);
	}

	public double uggX(double x) {
		return (((x * m_zoom * -1) - cameraPos.y) * 600f / cameraPos.x) + (viewport.getWidth() / 2.0);
	}

	public double convertY(double y) {
		return ((-y + cameraPos.y) * m_zoom) + (viewport.getHeight() / 2.0);
	}

	public double geomXifYZplane(double x) {
		return ((x - (viewport.getWidth() / 2.0)) * cameraPos.x / 600f + cameraPos.y) / m_zoom;
	}

	public double geomYifYZplane(double y) {

		return (-(y - (viewport.getHeight() / 2.0)) * cameraPos.x / 600f + cameraPos.z) / m_zoom;
	}

	public double geomDist(double x1) {
//		return geomX(x1) - geomX(0);
		return x1 * cameraPos.x / 600f / m_zoom;
	}

	public Vec3 getGeoPoint(double viewX, double viewY) {
		double x_real = (viewX - (viewport.getWidth() / 2.0)) * cameraPos.x / 600f + cameraPos.y;
		double y_real = -(viewY - (viewport.getHeight() / 2.0)) * cameraPos.x / 600f + cameraPos.z;

		Vec3 vec3 = new Vec3(0, x_real, y_real);
		vec3.scale((float) (1f / m_zoom));

		vec3.transform(getViewPortAntiRotMat2());
//		System.out.println("GeoMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", geoP: " + vec3 + ", camPos: " + cameraPos);

		return vec3;
	}

	//	public Vec2 getCameraSpacePoint(double viewX, double viewY) {
	public Vec2 getPoint_ifYZplane(double viewX, double viewY) {
		double x_real = (viewX - (viewport.getWidth() / 2.0)) * cameraPos.x / 600f + cameraPos.y;
		double y_real = -(viewY - (viewport.getHeight() / 2.0)) * cameraPos.x / 600f + cameraPos.z;

		Vec2 vec2 = new Vec2(x_real, y_real);
		vec2.scale((float) (1f / m_zoom));
//		vec2.transform(getViewPortAntiRotMat2());

//		System.out.println("CamSpaceMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", CamP: " + vec2 + ", camPos: " + cameraPos);

		return vec2;
	}

	public float getPixelSize() {
		return (float) ((geomXifYZplane(4) - geomXifYZplane(0)));
	}

	public CameraHandler setAllowRotation(boolean allowRotation) {
		this.allowRotation = allowRotation;
		return this;
	}

	public Vec3 getActualCameraPos() {
		Vec3 statCamPos = new Vec3(0f, -70f, -200f);
		Vec3 dynCamPos = Vec3.getScaled(cameraPos, (float) m_zoom).sub(statCamPos);
		dynCamPos.rotate(0, 0, 0, Math.toRadians(yAngle), (byte) 0, (byte) 2);
		dynCamPos.rotate(0, 0, 0, Math.toRadians(zAngle), (byte) 1, (byte) 2);
		return dynCamPos;
	}

	public Vec3 getCameraPos() {
		return cameraPos;
	}

	public boolean isOrtho() {
		return ortho;
	}

	public CameraHandler setOrtho(boolean ortho) {
		this.ortho = ortho;
		return this;
	}

	public CameraHandler toggleOrtho() {
		if (allowToggleOrtho) {
			ortho = !ortho;
			System.out.println("ortho: " + ortho);
		}
		return this;
	}

	public CameraHandler setAllowToggleOrtho(boolean allowToggleOrtho) {
		this.allowToggleOrtho = allowToggleOrtho;
		return this;
	}


	public CameraHandler setActivityManager(ViewportActivityManager activityManager) {
		this.activityManager = activityManager;
		return this;
	}

	private void calculateCameraRotation() {
		inverseCameraRotXSpinY.setFromAxisAngle(Vec3.X_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotXSpinY.invertRotation();
		inverseCameraRotYSpinY.setFromAxisAngle(Vec3.Y_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotYSpinY.invertRotation();
		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotZSpinX.invertRotation();
		inverseCameraRotZSpinZ.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(zAngle)).normalize();
		inverseCameraRotZSpinZ.invertRotation();
		inverseCameraRotation.set(getInverseCameraRotZSpinZ()).mul(getInverseCameraRotYSpinY()).normalize();

	}

	public Quat getInverseCameraRotation() {
		return inverseCameraRotation;
	}

	public Quat getInverseCameraRotXSpinY() {
		return inverseCameraRotXSpinY;
	}

	public Quat getInverseCameraRotYSpinY() {
		return inverseCameraRotYSpinY;
	}

	public Quat getInverseCameraRotZSpinX() {
		return inverseCameraRotZSpinX;
	}

	public Quat getInverseCameraRotZSpinZ() {
		return inverseCameraRotZSpinZ;
	}

	public Mat4 getViewPortAntiRotMat() {
		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation.invertRotation());
		inverseCameraRotation.invertRotation();
		return viewPortAntiRotMat;
	}

	public Mat4 getViewPortAntiRotMat2() {
		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation);
		return viewPortAntiRotMat;
	}

	public Vec3 getCameraLookAt() {
		return Vec3.ZERO;
	}
}
