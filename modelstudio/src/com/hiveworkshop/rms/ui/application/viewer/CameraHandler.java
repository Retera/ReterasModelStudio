package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class CameraHandler {
	private final Vec3 X_AXIS = new Vec3(1, 0, 0);
	private final Vec3 Y_AXIS = new Vec3(0, 1, 0);
	private final Vec3 Z_AXIS = new Vec3(0, 0, 1);
	private final double ZOOM_FACTOR = 1.07;
	private final Vec3 cameraPos = new Vec3(0, 0, 0);
	private final Vec3 glCamTrans = new Vec3();
	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotXSpinX = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotXSpinZ = new Quat();
	private final Quat inverseCameraRotYSpinX = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotYSpinZ = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotZSpinY = new Quat();
	private final Quat inverseCameraRotZSpinZ = new Quat();
	private final Quat inverseCameraRot_XY_mulZX = new Quat();
	private double m_zoom = 1;
	private Vec2 cameraPanStartPoint2;
	private Vec2 cameraSpinStartPoint2;

	private Vec2 actStart;

	Mat4 viewPortAntiRotMat = new Mat4();

	ViewportActivityManager activityManager;

	private float xRatio;
	private float yRatio;
	private float xAngle;
	private float yAngle;
	private float zAngle;
	private boolean ortho = false;
	private boolean allowToggleOrtho = true;
	private boolean allowRotation = true;

	private final PerspectiveViewport viewport;

	public CameraHandler(PerspectiveViewport viewport) {
		this.viewport = viewport;
	}

	public CameraHandler resetZoom() {
		m_zoom = .2f;
		return this;
	}

	public CameraHandler loadDefaultCameraFor(double boundsRadius) {
		m_zoom = .2f;

//		System.out.println("boundsRadius: " + boundsRadius + " (zoom : 128/" + boundsRadius + "/1.3=" + m_zoom + ", X-pos: " + boundsRadius + "/4=" + (boundsRadius / 4) + ")");

		setViewportCamera((int) -(boundsRadius / 3), (int) (boundsRadius / 20), 0, 20, -25);
		return this;
	}

	public CameraHandler setRatios(float xRatio, float yRatio){
		this.xRatio = xRatio;
		this.yRatio = yRatio;
		return this;
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

	public void setViewportCamera(int dist, int height, int rX, int rY, int rZ) {
		cameraPanStartPoint2 = null;
		cameraSpinStartPoint2 = null;
		actStart = null;

		xAngle = rX;
		yAngle = rY;
		zAngle = rZ;

		calculateCameraRotation();

		cameraPos.set(dist, 0, height);
	}

	public void setPosition(double a, double b) {
		cameraPos.x = (float) a;
		cameraPos.y = (float) b;
	}

	private void calculateCameraRotation() {
		inverseCameraRotXSpinX.setFromAxisAngle(X_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotXSpinX.invertRotation();
		inverseCameraRotXSpinY.setFromAxisAngle(X_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotXSpinY.invertRotation();
		inverseCameraRotXSpinZ.setFromAxisAngle(X_AXIS, (float) Math.toRadians(zAngle)).normalize();
		inverseCameraRotXSpinZ.invertRotation();

		inverseCameraRotYSpinX.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotYSpinX.invertRotation();
		inverseCameraRotYSpinY.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotYSpinY.invertRotation();
		inverseCameraRotYSpinZ.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(zAngle)).normalize();
		inverseCameraRotYSpinZ.invertRotation();

		inverseCameraRotZSpinX.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotZSpinX.invertRotation();
		inverseCameraRotZSpinY.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotZSpinY.invertRotation();
		inverseCameraRotZSpinZ.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(zAngle)).normalize();
		inverseCameraRotZSpinZ.invertRotation();

//		inverseCameraRotation.set(getInverseCameraRotXSpinZ()).normalize();
		inverseCameraRotation.set(getInverseCameraRotZSpinZ()).mul(getInverseCameraRotYSpinY()).normalize();

//		inverseCameraRotation.set(getInverseCameraRotZSpinY()).mul(getInverseCameraRotXSpinZ()).normalize();
//		inverseCameraRotation.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(yAngle)).mul(getInverseCameraRotXSpinZ()).invertRotation().normalize();


//		inverseCameraRotation.setFromAxisAngle(X_AXIS, (float) Math.toRadians(0)).mul(getInverseCameraRotXSpinZ()).mul(getInverseCameraRotXSpinZ()).normalize();


//		inverseCameraRotation.setIdentity().mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinZ()).normalize();
//		inverseCameraRotation.set(getInverseCameraRotZSpinZ()).mul(getInverseCameraRotYSpinY()).normalize();
//		inverseCameraRotation.set(inverseCameraRotYSpinZ).mul(inverseCameraRotXSpinX).normalize();
//		inverseCameraRotation.setFromAxisAngle(Z_AXIS, 0).normalize();

		inverseCameraRot_XY_mulZX.set(inverseCameraRotXSpinY).mul(inverseCameraRotZSpinX).normalize();
		inverseCameraRot_XY_mulZX.invertRotation();
	}
//	private void calculateCameraRotation() {
//		inverseCameraRotXSpinY.setFromAxisAngle(X_AXIS, (float) Math.toRadians(yAngle)).normalize();
//		inverseCameraRotXSpinY.invertRotation();
//
//		inverseCameraRotXSpinX.setFromAxisAngle(X_AXIS, (float) Math.toRadians(xAngle)).normalize();
//		inverseCameraRotXSpinX.invertRotation();
//
//		inverseCameraRotYSpinX.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(xAngle)).normalize();
//		inverseCameraRotYSpinX.invertRotation();
//
//		inverseCameraRotZSpinY.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(yAngle)).normalize();
//		inverseCameraRotZSpinY.invertRotation();
//
//
//		inverseCameraRotYSpinY.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(yAngle)).normalize();
//		inverseCameraRotYSpinY.invertRotation();
//		inverseCameraRotZSpinX.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
//		inverseCameraRotZSpinX.invertRotation();
//
//		inverseCameraRotation.set(inverseCameraRotZSpinX).mul(inverseCameraRotYSpinY).normalize();
//
//		inverseCameraRot_XY_mulZX.set(inverseCameraRotXSpinY).mul(inverseCameraRotZSpinX).normalize();
//		inverseCameraRot_XY_mulZX.invertRotation();
//	}

	public Quat getInverseCameraRotation() {
//		return inverseCameraRotation.invertRotation();
		return inverseCameraRotation;
	}

	public Quat getInverseCameraRot_XY_mulZX() {
		return inverseCameraRot_XY_mulZX;
	}

	public Quat getInverseCameraRotXSpinX() {
		return inverseCameraRotXSpinX;
	}

	public Quat getInverseCameraRotXSpinY() {
		return inverseCameraRotXSpinY;
	}

	public Quat getInverseCameraRotXSpinZ() {
		return inverseCameraRotXSpinZ;
	}

	public Quat getInverseCameraRotYSpinX() {
		return inverseCameraRotYSpinX;
	}

	public Quat getInverseCameraRotYSpinY() {
		return inverseCameraRotYSpinY;
	}

	public Quat getInverseCameraRotYSpinZ() {
		return inverseCameraRotYSpinZ;
	}

	public Quat getInverseCameraRotZSpinX() {
		return inverseCameraRotZSpinX;
	}

	public Quat getInverseCameraRotZSpinY() {
		return inverseCameraRotZSpinY;
	}

	public Quat getInverseCameraRotZSpinZ() {
		return inverseCameraRotZSpinZ;
	}

	public void setUpCamera() {
		if (ortho) {
			float ortoFac = 4.0f * (-150 / cameraPos.x);
			float w = viewport.getWidth() / 2.0f / ortoFac;
			float h = viewport.getHeight() / 2.0f / ortoFac;
			glOrtho(-w, w, -h, h, -6000.0f, 16000.0f);
		} else {
			gluPerspective(45f, (float) viewport.getWidth() / (float) viewport.getHeight(), 5.0f, 16000.0f);
		}

//		glCamTrans.set(cameraPos);

		// Rotating camera to have +Z up and +X as forward (pointing into camera)
		glRotatef(-90, 1f, 0f, 0f);
		glRotatef(-90, 0f, 0f, 1f);

		glTranslatef(cameraPos.x, -cameraPos.y, -cameraPos.z);

		glRotatef(xAngle, 1f, 0f, 0f);
		glRotatef(yAngle, 0f, 1f, 0f);
		glRotatef(zAngle, 0f, 0f, 1f);
		glScalef((float) m_zoom, (float) m_zoom, (float) m_zoom);
	}

	public Vec3 getActualCameraPos() {
		Vec3 statCamPos = new Vec3(0f, -70f, -200f);
		Vec3 dynCamPos = Vec3.getScaled(cameraPos, (float) m_zoom).sub(statCamPos);
		dynCamPos.rotate(0, 0, 0, Math.toRadians(yAngle), (byte) 1, (byte) 2);
		dynCamPos.rotate(0, 0, 0, Math.toRadians(yAngle), (byte) 0, (byte) 2);
		return dynCamPos;
	}

	public Vec3 getCameraPos() {
		return cameraPos;
	}

	public void setCameraSide(double modelRadius) {
		System.out.println("SIDE, modelRadius: " + modelRadius + ", xAngl: " + xAngle + ", yAng: " + yAngle + ", zAng: " + zAngle + ", zoom: " + m_zoom + ", pos: " + cameraPos + ", glTrans: " + glCamTrans);
//		resetZoom();
//		setViewportCamera((int) -(modelRadius / 3), (int) (modelRadius / 9), 0, 0, 90);
		setViewportCamera((int) cameraPos.x, (int) (modelRadius / 9), 0, 0, 90);
	}

	public void setCameraFront(double modelRadius) {
		System.out.println("FRONT, modelRadius: " + modelRadius + ", xAngl: " + xAngle + ", yAng: " + yAngle + ", zAng: " + zAngle + ", zoom: " + m_zoom + ", pos: " + cameraPos + ", glTrans: " + glCamTrans
//				+ "\ninvCS: " + inverseCameraRotation.toAxisWithAngle() + " (" + (inverseCameraRotation.toAxisWithAngle().w*57.3) + ") " + new Vec3().wikiToEuler(getInverseCameraRotation()).scale(57.3f)
//				+ "\ninvYS: " + inverseCameraRotYSpinY.toAxisWithAngle() + " (" + (inverseCameraRotYSpinY.toAxisWithAngle().w*57.3) + ")"
//				+ "\ninvZS: " + inverseCameraRotZSpinX.toAxisWithAngle() + " (" + (inverseCameraRotZSpinX.toAxisWithAngle().w*57.3) + ")"
		);
//		resetZoom();
//		setViewportCamera((int) -(modelRadius / 3), (int) (modelRadius / 9), 0, 0, 0);
		setViewportCamera((int) cameraPos.x, (int) (modelRadius / 9), 0, 0, 0);
	}

	public void setCameraTop(double modelRadius) {
		System.out.println("TOP, modelRadius: " + modelRadius + ", xAngl: " + xAngle + ", yAng: " + yAngle + ", zAng: " + zAngle + ", zoom: " + m_zoom + ", pos: " + cameraPos + ", glTrans: " + glCamTrans);
//		resetZoom();
//		setViewportCamera((int) -(modelRadius * .54), 00, 90, 0);
//		setViewportCamera((int) -(modelRadius / 3), 0, 0, 90, 0);
		setViewportCamera((int) cameraPos.x, 0, 0, 90, 0);
	}


	public void clickTimerAction() {
		final int xoff = 0;
		final int yoff = 0;
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo != null) {
			final double mx = pointerInfo.getLocation().x - xoff;
			final double my = pointerInfo.getLocation().y - yoff;

			if (cameraPanStartPoint2 != null) {
				panCamera((int) mx, (int) my);
			}
			if (cameraSpinStartPoint2 != null && allowRotation) {
				rotateCamera(mx, my);
			}
			// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

			if (actStart != null) {
				somethingSelect((int) mx, (int) my);


			}
		}
	}

	private void somethingSelect(int mx, int my) {
		Point locationOnScreen = viewport.getLocationOnScreen();
		Vec2 actEnd = new Vec2(mx - locationOnScreen.x, my - locationOnScreen.y);
		Vec2 convertedStart = new Vec2(geomX(actStart.x), geomY(actStart.y));
		Vec2 convertedEnd = new Vec2(geomX(actEnd.x), geomY(actEnd.y));
		// dispMDL.updateAction(convertedStart,convertedEnd,m_d1,m_d2);
		actStart = actEnd;
	}

	private void rotateCamera(double mx, double my) {
		// rotate Viewport Camera
		zAngle += mx - cameraSpinStartPoint2.x;
		yAngle += my - cameraSpinStartPoint2.y;

		calculateCameraRotation();

		cameraSpinStartPoint2.x = (int) mx;
		cameraSpinStartPoint2.y = (int) my;
	}

	public void rotate(double right, double up) {
		zAngle += right;
		yAngle += up;
		calculateCameraRotation();
	}

	private void panCamera(int mx, int my) {
		// translate Viewport Camera
		double cameraPointX = (mx - cameraPanStartPoint2.x);// / m_zoom;
		double cameraPointY = (my - cameraPanStartPoint2.y);// / m_zoom;

		translate(-cameraPointX, cameraPointY);
//		Vec3 vertexHeap = new Vec3(0, -cameraPointX, cameraPointY);
//		cameraPos.add(vertexHeap);
		cameraPanStartPoint2.x = mx;
		cameraPanStartPoint2.y = my;
	}

	public void translate(double left, double up) {
//		cameraPos.y += left;
//		cameraPos.z += up;
//		cameraPos.y += left * (1f / m_zoom);
//		cameraPos.z += up * (1f / m_zoom);
//		cameraPos.y += left * (1f / m_zoom) * cameraPos.x / 600f;
//		cameraPos.z += up * (1f / m_zoom) * cameraPos.x / 600f;
		cameraPos.y -= left * cameraPos.x / 600f;
		cameraPos.z -= up * cameraPos.x / 600f;
	}


	public CameraHandler setActivityManager(ViewportActivityManager activityManager) {
		this.activityManager = activityManager;
		return this;
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

	public void doZoom(int wr) {
		final boolean neg = wr < 0;

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			if (neg) {
//				 cameraPos.x -= (mx - viewport.getWidth() / 2f) * (1 / m_zoom - 1 / (m_zoom * ZOOM_FACTOR));
//				 cameraPos.y -= (my - viewport.getHeight() / 2f) * (1 / m_zoom - 1 / (m_zoom * ZOOM_FACTOR));
//				 cameraPos.z -= (viewport.getHeight() / 2f) * (1 / m_zoom - 1 / (m_zoom * ZOOM_FACTOR));
				m_zoom *= ZOOM_FACTOR;
			} else {
				m_zoom /= ZOOM_FACTOR;
				// cameraPos.x -= (mx - viewport.getWidth() / 2) * (1 / (m_zoom * ZOOM_FACTOR) - 1 / m_zoom);
				// cameraPos.y -= (my - viewport.getHeight() / 2) * (1 / (m_zoom * ZOOM_FACTOR) - 1 / m_zoom);
				// cameraPos.z -= (viewport.getHeight() / 2) * (1 / (m_zoom * ZOOM_FACTOR) - 1 / m_zoom);
			}
		}
	}
	public void doZoom2(MouseWheelEvent e) {
		int wr = e.getWheelRotation();

		int dir = wr < 0 ? -1 : 1;
		boolean neg = wr < 0;

		double mouseX = e.getX();
		double mouseY = e.getY();

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			double zoomAdj = (ZOOM_FACTOR - 1) * dir / (m_zoom * ZOOM_FACTOR);
			double w = mouseX - viewport.getWidth() / 2f;
			double h = mouseY - viewport.getHeight() / 2f;
			if (neg) {
				cameraPos.x -= w * zoomAdj;
				cameraPos.y -= h * zoomAdj;
				cameraPos.z -= (viewport.getHeight() / 2f) * zoomAdj;

				m_zoom *= ZOOM_FACTOR;
			} else {
				m_zoom /= ZOOM_FACTOR;

				cameraPos.x -= w * zoomAdj;
				cameraPos.y -= h * zoomAdj;
				cameraPos.z -= (viewport.getHeight() / 2f) * zoomAdj;
			}
		}
	}
	public void doZoom3(MouseWheelEvent e) {
		int wr = e.getWheelRotation();

		double mouseX = e.getX();
		double mouseY = e.getY();

		int dir = wr < 0 ? -1 : 1;

		for (int i = 0; i < wr*dir; i++) {
//			double w = mouseX - viewport.getWidth() / 2f;
//			double h = mouseY - viewport.getHeight() / 2f;
//
//			double zoomAdj = (ZOOM_FACTOR - 1)*dir / (m_zoom * ZOOM_FACTOR);
//			cameraPos.x += w * zoomAdj;
//			cameraPos.y += h * zoomAdj;
//			cameraPos.z += (viewport.getHeight() / 2f) * zoomAdj;

			if (dir == -1) {
				m_zoom *= ZOOM_FACTOR;
			} else {
				m_zoom /= ZOOM_FACTOR;

			}
		}
	}

	public void doZoom4(MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		int dir = wr < 0 ? -1 : 1;

		for (int i = 0; i < wr * dir; i++) {
			if (dir == -1) {
				m_zoom *= ZOOM_FACTOR;
				cameraPos.y *= ZOOM_FACTOR;
				cameraPos.z *= ZOOM_FACTOR;
			} else {
				m_zoom /= ZOOM_FACTOR;
				cameraPos.y /= ZOOM_FACTOR;
				cameraPos.z /= ZOOM_FACTOR;
			}
		}
	}

	public Vec2 getActStart() {
		return actStart;
	}

	public Vec2 getCameraPanStartPoint() {
		return cameraPanStartPoint2;
	}

	public Vec2 getCameraSpinStartPoint() {
		return cameraSpinStartPoint2;
	}

	public void finnishAct(MouseEvent e) {
		if ((actStart != null)) {
			Vec2 actEnd = new Vec2(e.getX(), e.getY());
			Vec2 convertedStart = new Vec2(geomX(actStart.x), geomY(actStart.y));
			Vec2 convertedEnd = new Vec2(geomX(actEnd.x), geomY(actEnd.y));
			System.out.println("act Finished! " + actStart + " to " + actEnd);
			// dispMDL.finishAction(convertedStart,convertedEnd,m_d1,m_d2);
			actStart = null;

			if (activityManager != null) {
				activityManager.mouseReleased(e, this);
			}
		}
	}

	public void rot(float rx, float ry, float rz) {
		xAngle += rx;
		yAngle += ry;
		zAngle += rz;
	}

	public void finnishSpinn(MouseEvent e) {
//		Vec2 selectEnd = new Vec2(e.getX(), e.getY());
//		Rectangle2D.Double area = pointsToGeomRect(cameraSpinStartPoint2, selectEnd);
//		// System.out.println(area);
//		// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
		cameraSpinStartPoint2 = null;
	}

	public void finnishPan(MouseEvent e) {
//		cameraPos.y += (e.getXOnScreen() - cameraPanStartPoint2.x) / m_zoom;
//		cameraPos.z += (e.getYOnScreen() - cameraPanStartPoint2.y) / m_zoom;
		cameraPanStartPoint2 = null;
	}

	public void startAct(MouseEvent e) {
		System.out.println("actStart");
		actStart = new Vec2(e.getX(), e.getY());
		Vec2 convertedStart = new Vec2(geomX(actStart.x), geomY(actStart.y));
		if (activityManager != null) {
			activityManager.mousePressed(e, this);
		}
		// dispMDL.startAction(convertedStart,m_d1,m_d2,MainFrame.panel.currentActionType());
	}

	public void startSpinn(MouseEvent e) {
		cameraSpinStartPoint2 = new Vec2(e.getXOnScreen(), e.getYOnScreen());
	}

	public void startPan(MouseEvent e) {
		cameraPanStartPoint2 = new Vec2(e.getXOnScreen(), e.getYOnScreen());
	}

	public Rectangle2D.Double pointsToGeomRect(Vec2 p1, Vec2 p2) {
		Vec2 min = new Vec2(p1).minimize(p2);
		Vec2 max = new Vec2(p1).maximize(p2).sub(min);
		return new Rectangle2D.Double(min.x, min.y, max.x, max.y);
	}

	public double getZoom() {
		return m_zoom;
	}

	public void zoom(double amount) {
		m_zoom *= 1 + amount;
	}

	public Vec2 getDisplayOffset() {
		return new Vec2(cameraPos.x, cameraPos.y);
	}

	public double convertX(double x) {
		return ((x + cameraPos.x) * m_zoom) + (viewport.getWidth() / 2.0);
	}

	public double uggX(double x) {
		return (((x * m_zoom * -1) - cameraPos.y) * 600f / cameraPos.x) + ( viewport.getWidth() / 2.0);
	}

	public double convertY(double y) {
		return ((-y + cameraPos.y) * m_zoom) + (viewport.getHeight() / 2.0);
	}

	public double geomX(double x) {
		return (-(x - (viewport.getWidth() / 2.0)) * cameraPos.x / 600f + cameraPos.y) / m_zoom;
	}

	public double geomY(double y) {

		return ((y - (viewport.getHeight() / 2.0)) * cameraPos.x / 600f + cameraPos.z) / m_zoom;
	}

//	public double geomDistX(double x1, double x2) {
//		return (-(x1 - (viewport.getWidth() / 2.0)) * cameraPos.x / 600f + cameraPos.y) / m_zoom;
//	}
//
//	public double geomDistY(double y1, double y2) {
//		return (y1 - y2) * cameraPos.x / 600f / m_zoom;
//	}

	public Vec3 getGeoPoint(double viewX, double viewY) {
		double x_real = -(viewX - (viewport.getWidth() / 2.0)) * cameraPos.x / 600f + cameraPos.y;
		double y_real = (viewY - (viewport.getHeight() / 2.0)) * cameraPos.x / 600f + cameraPos.z;

		Vec3 vec3 = new Vec3(0, x_real, y_real);
		vec3.scale((float) (1f / m_zoom));

//		System.out.println("Mouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + "vec3: " + vec3 + ", camPos: " + cameraPos);
		vec3.transform(getViewPortAntiRotMat2());

		return vec3;
	}
//
//	public double geomX(double x) {
//		return ((x - (viewport.getWidth() / 2.0)) / m_zoom) - cameraPos.x;
//	}
//
//	public double geomY(double y) {
//		return -(((y - (viewport.getHeight() / 2.0)) / m_zoom) - cameraPos.y);
//	}

	public CameraHandler setAllowRotation(boolean allowRotation) {
		this.allowRotation = allowRotation;
		return this;
	}

	public CameraHandler setAllowToggleOrtho(boolean allowToggleOrtho) {
		this.allowToggleOrtho = allowToggleOrtho;
		return this;
	}

	public boolean isOrtho() {
		return ortho;
	}
}
