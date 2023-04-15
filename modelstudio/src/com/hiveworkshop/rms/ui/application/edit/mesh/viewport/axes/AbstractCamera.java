package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.event.MouseWheelEvent;

public abstract class AbstractCamera {

	public abstract Component getComponent();
	public abstract Mat4 getInvViewProjectionMat();
	public abstract Mat4 getViewProjectionMatrix();
	public abstract SelectionBoxHelper getSelectionBoxHelper(Vec2 topRight, Vec2 bottomLeft);
	public abstract double sizeAdj();
	public abstract void doZoom(MouseWheelEvent e);
	public abstract void zoom(double v);
	public abstract void translate(double right, double up);
	public abstract void setPosition(double right, double up);
	public abstract void rotate(double right, double up);
	public abstract void setCameraRotation(float right, float up);
	public abstract void setCameraRotation(float right, float up, float tilt);
	public abstract Vec3 getGeoPoint(double viewX, double viewY);
	public abstract void resetCamera();
	public abstract void resetZoom();
	public AbstractCamera toggleOrtho(){return this;}
	public void moveTargetDepth(double depth){}
	public void setTargetPoint(Vec3 vec3){}

}
