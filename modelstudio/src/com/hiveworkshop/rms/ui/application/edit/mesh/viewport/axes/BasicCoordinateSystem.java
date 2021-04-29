package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import javax.swing.*;

public final class BasicCoordinateSystem implements CoordinateSystem {
	private final byte dimension1;
	private final byte dimension2;
	//	private final int width;
//	private final int height;
	private final JComponent parent;
	private double cameraX = 0;
	private double cameraY = 0;
	private double zoom = 1;
	private int aspectRatio = 1;
	private int yFlip = -1;

	public BasicCoordinateSystem(byte dimension1, byte dimension2, JComponent parent) {
		this.dimension1 = dimension1;
		this.dimension2 = dimension2;
		this.parent = parent;
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

	public BasicCoordinateSystem setZoom(double zoom) {
		this.zoom = zoom;
		return this;
	}

	public BasicCoordinateSystem setPosition(double a, double b) {
		cameraX = a;
		cameraY = b;
		return this;
	}

	public BasicCoordinateSystem translate(double a, double b) {
		cameraX += a / aspectRatio;
		cameraY += b;
		return this;
	}

	@Override
	public double viewX(double x) {
		return (x + cameraX) * zoom * aspectRatio + parent.getWidth() / 2.0;
	}
//	public double viewX(double x) { return ((x + cameraX) * zoom * aspectRatio) + parent.getWidth() / 2.0;}

	@Override
	public double viewY(double y) {
		return ((y * yFlip + cameraY) * zoom) + parent.getHeight() / 2.0;
	}
//	public double viewY(double y) { return ((y * yFlip + cameraY) * zoom) + parent.getHeight() / 2.0;}

	@Override
	public double geomX(double x) {
		return (x - parent.getWidth() / 2.0) / aspectRatio / zoom - cameraX;
	}
//	public double geomX(double x) { return (x - parent.getWidth() / 2.0) / aspectRatio / zoom - cameraX;}

	@Override
	public double geomY(double y) {
		return yFlip * ((y - parent.getHeight() / 2.0) / zoom - cameraY);
	}
//	public double geomY(double y) { return yFlip * ((y - parent.getHeight() / 2.0) / zoom) - cameraY;}

	@Override
	public byte getPortFirstXYZ() {
		return dimension1;
	}

	@Override
	public byte getPortSecondXYZ() {
		return dimension2;
	}

}
