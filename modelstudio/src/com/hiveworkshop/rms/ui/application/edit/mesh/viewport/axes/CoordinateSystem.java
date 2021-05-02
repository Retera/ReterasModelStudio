package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import javax.swing.*;

public final class CoordinateSystem {
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
	}

	public CoordinateSystem setYFlip(int yFlip) {
		this.yFlip = yFlip;
		return this;
	}

	public CoordinateSystem setAspectRatio(double aspectRatio) {
		this.aspectRatio = aspectRatio;
		return this;
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

	public CoordinateSystem zoomIn(double amount) {
		zoom *= amount;
		return this;
	}
	public CoordinateSystem zoomOut(double amount) {
		zoom /= amount;
		return this;
	}

	public CoordinateSystem setPosition(double a, double b) {
		cameraX = a;
		cameraY = b;
		return this;
	}

	public CoordinateSystem translate(double a, double b) {
		cameraX += a / aspectRatio;
		cameraY += b;
		return this;
	}

	public CoordinateSystem translateZoomed(double a, double b) {
		cameraX += a / zoom / aspectRatio;
		cameraY += b / zoom;
		return this;
	}

	public CoordinateSystem setGeomPosition(double a, double b) {
		cameraX = geomX(a);
		cameraY = geomY(b);
		return this;
	}

	public CoordinateSystem setDimensions(byte dimension1, byte dimension2) {
		this.dimension1 = dimension1;
		this.dimension2 = dimension2;
		return this;
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

}
