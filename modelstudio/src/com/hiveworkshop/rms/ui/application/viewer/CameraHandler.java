package com.hiveworkshop.rms.ui.application.viewer;

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
	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotZSpinY = new Quat();
	private final Quat inverseCameraRot_XY_mulZX = new Quat();
	private final Quat inverseCameraRotXSpinX = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotYSpinX = new Quat();
	private double m_zoom = 1;
	private Vec2 cameraPanStartPoint2;
	private Vec2 cameraSpinStartPoint2;

	private Vec2 actStart2;

	private float xRatio;
	private float yRatio;
	private float xAngle;
	private float yAngle;
	private boolean ortho = false;

	private final PerspectiveViewport viewport;
	public CameraHandler(PerspectiveViewport viewport){
		this.viewport = viewport;
	}

	public CameraHandler resetZoom(double modelRadius) {
		m_zoom = 128 / modelRadius;
		return this;
	}

	public CameraHandler loadDefaultCameraFor(double boundsRadius) {
		m_zoom = 128 / (boundsRadius * 1.3);
		cameraPos.y -= boundsRadius / 4;
		yAngle += 35;

		calculateCameraRotation();
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
		ortho = !ortho;
		return this;
	}

	public void setViewportCamera(int dist, int rX, int rY) {
		cameraPanStartPoint2 = null;
		cameraSpinStartPoint2 = null;
		actStart2 = null;

		xAngle = rX;
		yAngle = rY;

		calculateCameraRotation();

		Vec3 vertexHeap = new Vec3(-0, dist, -0);
		cameraPos.set(vertexHeap);
	}

	public void setPosition(double a, double b) {
		cameraPos.x = (float) a;
		cameraPos.y = (float) b;
	}

	public void translate(double a, double b) {
		cameraPos.x += a;
		cameraPos.y += b;
	}

	private void calculateCameraRotation() {
		inverseCameraRotXSpinY.setFromAxisAngle(X_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotXSpinY.invertRotation();

		inverseCameraRotXSpinX.setFromAxisAngle(X_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotXSpinX.invertRotation();

		inverseCameraRotYSpinX.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotYSpinX.invertRotation();

		inverseCameraRotZSpinY.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotZSpinY.invertRotation();


		inverseCameraRotYSpinY.setFromAxisAngle(Y_AXIS, (float) Math.toRadians(yAngle)).normalize();
		inverseCameraRotYSpinY.invertRotation();
		inverseCameraRotZSpinX.setFromAxisAngle(Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotZSpinX.invertRotation();

		inverseCameraRotation.set(inverseCameraRotZSpinX).mul(inverseCameraRotYSpinY).normalize();

		inverseCameraRot_XY_mulZX.set(inverseCameraRotXSpinY).mul(inverseCameraRotZSpinX).normalize();
		inverseCameraRot_XY_mulZX.invertRotation();
	}

	public Quat getInverseCameraRotation() {
//		return inverseCameraRotation.invertRotation();
		return inverseCameraRotation;
	}
	public Quat getInverseCameraRotZSpinY() {
		return inverseCameraRotZSpinY;
	}
	public Quat getInverseCameraRotXSpinX() {
		return inverseCameraRotXSpinX;
	}
	public Quat getInverseCameraRot_XY_mulZX() {
		return inverseCameraRot_XY_mulZX;
	}

	public Quat getInverseCameraRotYSpinY() {
		return inverseCameraRotYSpinY;
	}

	public Quat getInverseCameraRotZSpinX() {
		return inverseCameraRotZSpinX;
	}

	public Quat getInverseCameraRotXSpinY() {
		return inverseCameraRotXSpinY;
	}

	public Quat getInverseCameraRotYSpinX() {
		return inverseCameraRotYSpinX;
	}

	public void setUpCamera() {
//		System.out.println("setting up camera");
		if (ortho) {
			float ortoFac = 4.0f;
			float w = viewport.getWidth() / 2.0f / ortoFac;
			float h = viewport.getHeight() / 2.0f / ortoFac;
			glOrtho(-w, w, -h, h, -6000.0f, 16000.0f);
		} else {
			gluPerspective(45f, (float) viewport.getWidth() / (float) viewport.getHeight(), 5.0f, 16000.0f);
		}

		Vec3 statCamPos = new Vec3(0f, -70f, -200f);
		Vec3 dynCamPos = Vec3.getScaled(cameraPos, (float) m_zoom).sub(statCamPos);

//		System.out.println(dynCamPos);

		glTranslatef(dynCamPos.x, -dynCamPos.y, -dynCamPos.z);
		glRotatef(yAngle, 1f, 0f, 0f);
		glRotatef(xAngle, 0f, 1f, 0f);
		glScalef((float) m_zoom, (float) m_zoom, (float) m_zoom);
	}

	public void setCameraSide(double modelRadius) {
		resetZoom(modelRadius);
		setViewportCamera((int) -(modelRadius / 6), 90, 0);
	}

	public Vec3 getCameraPos(){
		Vec3 statCamPos = new Vec3(0f, -70f, -200f);
		Vec3 dynCamPos = Vec3.getScaled(cameraPos, (float) m_zoom).sub(statCamPos);
		dynCamPos.rotate(0,0,0, Math.toRadians(yAngle), (byte)1,(byte)2);
		dynCamPos.rotate(0,0,0, Math.toRadians(yAngle), (byte)0,(byte)2);
		return dynCamPos;
	}
	public void setCameraFront(double modelRadius) {
		System.out.println("xAngl: " + xAngle + ", yAng: " + yAngle
				+ "\ninvCS: " + inverseCameraRotation.toAxisWithAngle() + " (" + (inverseCameraRotation.toAxisWithAngle().w*57.3) + ") " + new Vec3().wikiToEuler(getInverseCameraRotation()).scale(57.3f)
				+ "\ninvYS: " + inverseCameraRotYSpinY.toAxisWithAngle() + " (" + (inverseCameraRotYSpinY.toAxisWithAngle().w*57.3) + ")"
				+ "\ninvZS: " + inverseCameraRotZSpinX.toAxisWithAngle() + " (" + (inverseCameraRotZSpinX.toAxisWithAngle().w*57.3) + ")"
		);
		resetZoom(modelRadius);
		setViewportCamera((int) -(modelRadius / 6), 0, 0);
	}

	public void setCameraTop(double modelRadius) {
		resetZoom(modelRadius);
		setViewportCamera((int) -(modelRadius * .54), 0, 90);
	}



	public void clickTimerAction() {
		final int xoff = 0;
		final int yoff = 0;
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo != null) {
			final double mx = pointerInfo.getLocation().x - xoff;
			final double my = pointerInfo.getLocation().y - yoff;

			if (cameraPanStartPoint2 != null) {
				// translate Viewport Camera
				double cameraPointX = ((int) mx - cameraPanStartPoint2.x) / m_zoom;
				double cameraPointY = ((int) my - cameraPanStartPoint2.y) / m_zoom;

				Vec3 vertexHeap = new Vec3(cameraPointX, cameraPointY, 0);
				cameraPos.add(vertexHeap);
				cameraPanStartPoint2.x = (int) mx;
				cameraPanStartPoint2.y = (int) my;
			}
			if (cameraSpinStartPoint2 != null) {
				// rotate Viewport Camera
				xAngle += mx - cameraSpinStartPoint2.x;
				yAngle += my - cameraSpinStartPoint2.y;

				calculateCameraRotation();

				cameraSpinStartPoint2.x = (int) mx;
				cameraSpinStartPoint2.y = (int) my;
			}
			// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

//			if (actStart != null) {
//				final Point actEnd = new Point((int) mx, (int) my);
//				final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
//				final Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
//				// dispMDL.updateAction(convertedStart,convertedEnd,m_d1,m_d2);
//				actStart = actEnd;
//			}
		}
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
			double w = mouseX - viewport.getWidth() / 2f;
			double h = mouseY - viewport.getHeight() / 2f;

			double zoomAdj = (ZOOM_FACTOR - 1)*dir / (m_zoom * ZOOM_FACTOR);
			cameraPos.x += w * zoomAdj;
			cameraPos.y += h * zoomAdj;
			cameraPos.z += (viewport.getHeight() / 2f) * zoomAdj;

			if (dir == -1) {
				m_zoom *= ZOOM_FACTOR;
			} else {
				m_zoom /= ZOOM_FACTOR;

			}
		}
	}

	public Vec2 getActStart() {
		return actStart2;
	}

	public Vec2 getCameraPanStartPoint() {
		return cameraPanStartPoint2;
	}

	public Vec2 getCameraSpinStartPoint() {
		return cameraSpinStartPoint2;
	}

	public void finnishAct(MouseEvent e) {
		if ((actStart2 != null)) {
			Vec2 actEnd = new Vec2(e.getX(), e.getY());
			Vec2 convertedStart = new Vec2(geomX(actStart2.x), geomY(actStart2.y));
			Vec2 convertedEnd = new Vec2(geomX(actEnd.x), geomY(actEnd.y));
			// dispMDL.finishAction(convertedStart,convertedEnd,m_d1,m_d2);
			actStart2 = null;
		}
	}

	public void finnishSpinn(MouseEvent e) {
		Vec2 selectEnd = new Vec2(e.getX(), e.getY());
		Rectangle2D.Double area = pointsToGeomRect(cameraSpinStartPoint2, selectEnd);
		// System.out.println(area);
		// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
		cameraSpinStartPoint2 = null;
	}

	public void finnishPan(MouseEvent e) {
		cameraPos.x += (e.getXOnScreen() - cameraPanStartPoint2.x) / m_zoom;
		cameraPos.y += (e.getYOnScreen() - cameraPanStartPoint2.y) / m_zoom;
		cameraPanStartPoint2 = null;
	}

	public void startAct(MouseEvent e) {
		actStart2 = new Vec2(e.getX(), e.getY());
		Vec2 convertedStart = new Vec2(geomX(actStart2.x), geomY(actStart2.y));
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

	public double convertY(double y) {
		return ((-y + cameraPos.y) * m_zoom) + (viewport.getHeight() / 2.0);
	}

	public double geomX(double x) {
		return ((x - (viewport.getWidth() / 2.0)) / m_zoom) - cameraPos.x;
	}

	public double geomY(double y) {
		return -(((y - (viewport.getHeight() / 2.0)) / m_zoom) - cameraPos.y);
	}
}
