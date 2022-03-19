package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.event.MouseWheelEvent;

public final class CoordinateSystem {
	private final Mat4 viewPortAntiRotMat = new Mat4();
	private final Quat inverseCameraRotation = new Quat();
	private Vec3 axis1 = new Vec3();
	private Vec3 axis2 = new Vec3();
	private final double ZOOM_FACTOR = 1.15;

	private byte dimension1;
	private byte dimension2;
	private final JComponent parent;
	private double cameraX = 0;
	private double cameraY = 0;
	private double zoom = 1;
	private double aspectRatio = 1;
	private int yFlip = -1;

	public CoordinateSystem(byte dimension1, byte dimension2, JComponent parent) {
		this.dimension1 = dimension1;
		this.dimension2 = dimension2;
		this.parent = parent;
		axis1.setCoord(dimension1, 1);
		axis2.setCoord(dimension2, 1);
		Quat rot1 = new Quat().setFromAxisAngle(axis1, 0).normalize().invertRotation();
		Quat rot2 = new Quat().setFromAxisAngle(axis2, 0).normalize().invertRotation();

		inverseCameraRotation.set(rot1).mul(rot2).normalize().invertRotation();
		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation);
	}

	public CoordinateSystem setYFlip(int yFlip) {
		this.yFlip = yFlip;
		return this;
	}

	public CoordinateSystem setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
		return this;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public double getCameraX() {
		return cameraX;
	}

	public double getCameraY() {
		return cameraY;
	}

	public double getZoom() {
		return zoom;
	}

	public CoordinateSystem setZoom(double zoom) {
		this.zoom = zoom;
		return this;
	}

	public CoordinateSystem doZoom(MouseWheelEvent e) {
		int wr = e.getWheelRotation();

		double mouseX = e.getX();
		double mouseY = e.getY();
		return doZoom(wr, mouseX, mouseY);
	}

	// xRatio = [0.0, 1.0]
	public CoordinateSystem doZoom(double wRatio, double hRatio, boolean zoomIn) {
		double mouseX = wRatio * parent.getWidth();
		double mouseY = hRatio * parent.getHeight();

		return doZoom(zoomIn ? -1 : 1, mouseX, mouseY);
	}
	private CoordinateSystem doZoom(int wr, double mouseX, double mouseY) {
		int dir = wr < 0 ? -1 : 1;
		double w = mouseX - (parent.getWidth() / 2.0);
		double h = mouseY - (parent.getHeight() / 2.0);

		for (int i = 0; i < wr * dir; i++) {
			double zoomAdjust = (ZOOM_FACTOR - 1) * dir / zoom;

			if (dir == -1) {
				cameraX += w * zoomAdjust / ZOOM_FACTOR / aspectRatio;
				cameraY += h * zoomAdjust / ZOOM_FACTOR;
				zoom *= ZOOM_FACTOR;
			} else {

				cameraX += w * zoomAdjust  / aspectRatio;
				cameraY += h * zoomAdjust ;
				zoom /= ZOOM_FACTOR;
			}
		}
		return this;
	}

	public CoordinateSystem doZoom1(MouseWheelEvent e) {
		int wr = e.getWheelRotation();

		int dir = wr < 0 ? -1 : 1;

		double mouseX = e.getX();
		double mouseY = e.getY();
		System.out.println("mouseX: " + mouseX + ", mouseY: " + mouseY);

		double w = mouseX - (parent.getWidth() / 2.0);
		double h = mouseY - (parent.getHeight() / 2.0);
		double wRatio = mouseX / parent.getWidth();
		double hRatio = mouseY / parent.getHeight();

		for (int i = 0; i < wr * dir; i++) {
			double zoomAdjust = (ZOOM_FACTOR - 1) * dir / ZOOM_FACTOR;

			cameraX += w * zoomAdjust / zoom / aspectRatio;
			cameraY += h * zoomAdjust / zoom;
//
//			cameraX += w * (ZOOM_FACTOR - 1) * dir / ZOOM_FACTOR / zoom / aspectRatio;
//			cameraY += h * (ZOOM_FACTOR - 1) * dir / ZOOM_FACTOR / zoom;

			if (dir == -1) {

				zoom *= ZOOM_FACTOR;
			} else {
				zoom /= ZOOM_FACTOR;
			}
		}
		return this;
	}

	public CoordinateSystem zoomIn(double amount) {
		zoom *= amount;
		return this;
	}

	public CoordinateSystem zoomOut(double amount) {
		zoom /= amount;
		return this;
	}

	public CoordinateSystem setPosition(double x, double y) {
		cameraX = x;
		cameraY = y;
		return this;
	}

	public CoordinateSystem translate(double x, double y) {
		cameraX += x / aspectRatio;
		cameraY += y;
		return this;
	}

	public CoordinateSystem translateZoomed(double x, double y) {
		cameraX += x / zoom / aspectRatio;
		cameraY += y / zoom;
		return this;
	}

	public CoordinateSystem setGeomPosition(double x, double y) {
		cameraX = geomX(x);
		cameraY = geomY(y);
		return this;
	}

	public CoordinateSystem setDimensions(byte dimension1, byte dimension2) {
		this.dimension1 = dimension1;
		this.dimension2 = dimension2;

		axis1.setCoord(dimension1, 1);
		axis2.setCoord(dimension2, 1);
		Quat rot1 = new Quat().setFromAxisAngle(axis1, 0).normalize().invertRotation();
		Quat rot2 = new Quat().setFromAxisAngle(axis2, 0).normalize().invertRotation();

		inverseCameraRotation.set(rot1).mul(rot2).normalize().invertRotation();
		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation);
		return this;
	}

	public Vec2 viewV(double x, double y) {
		double x_view = (x + cameraX) * zoom * aspectRatio + parent.getWidth() / 2.0;
		double y_view = ((y * yFlip + cameraY) * zoom) + parent.getHeight() / 2.0;
		return new Vec2(x_view, y_view);
	}

	public Vec2 viewV(Vec2 vec2) {
		double x_view = (vec2.x + cameraX) * zoom * aspectRatio + parent.getWidth() / 2.0;
		double y_view = ((vec2.y * yFlip + cameraY) * zoom) + parent.getHeight() / 2.0;
		return new Vec2(x_view, y_view);
	}

	public Vec2 convertToViewVec2(Vec3 vertex) {
		double x = viewX(vertex.getCoord(dimension1));
		double y = viewY(vertex.getCoord(dimension2));
		return new Vec2(x, y);
	}

	public Vec2 geomVec2(Vec2 point) {
		return new Vec2(geomX(point.x), geomY(point.y));
	}

	public double viewX(double x) {
		return (x + cameraX) * zoom * aspectRatio + parent.getWidth() / 2.0;
	}

	public double viewY(double y) {
		return ((y * yFlip + cameraY) * zoom) + parent.getHeight() / 2.0;
	}

	public double geomX(double x) {
		return (x - parent.getWidth() / 2.0) / aspectRatio / zoom - cameraX;
	}

	public double geomY(double y) {
		return yFlip * ((y - parent.getHeight() / 2.0) / zoom - cameraY);
	}

	public byte getPortFirstXYZ() {
		return dimension1;
	}

	public byte getPortSecondXYZ() {
		return dimension2;
	}

	public byte getUnusedXYZ() {
		if (dimension1 < 0) {
			dimension1 = (byte) (-dimension1 - 1);
		}
		if (dimension2 < 0) {
			dimension2 = (byte) (-dimension2 - 1);
		}
		return (byte) (3 - dimension1 - dimension2);
	}

	public Mat4 getViewPortAntiRotMat() {
//		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation.invertRotation());
//		inverseCameraRotation.invertRotation();
		return viewPortAntiRotMat;
	}

	public Quat viewportRotation() {
		return switch (getUnusedXYZ()) {
			case 0 -> new Quat().setFromAxisAngle(Vec3.X_AXIS, 0);
			case 1 -> new Quat().setFromAxisAngle(Vec3.Y_AXIS, 0);
			case 2 -> new Quat().setFromAxisAngle(Vec3.Z_AXIS, 0);
			case -1 -> new Quat().setFromAxisAngle(Vec3.NEGATIVE_X_AXIS, 0);
			case -2 -> new Quat().setFromAxisAngle(Vec3.NEGATIVE_Y_AXIS, 0);
			case -3 -> new Quat().setFromAxisAngle(Vec3.NEGATIVE_Z_AXIS, 0);
			default -> throw new IllegalStateException("Unexpected value: " + getUnusedXYZ());
		};
//		return switch (dim) {
//			case 0 -> centerX;
//			case 1 -> centerY;
//			case -1 -> -centerX;
//			case -2 -> -centerY;
//			case -3 -> -centerZ;
//			case 2 -> centerZ;
//			default -> centerZ;
//		};
	}

}
