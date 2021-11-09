package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class DrawPlaneAction2 implements GenericMoveAction {

	private final CameraHandler cameraHandler;
	private final Vec2 p1;
	private final Vec2 p2;
	Vec3 p1V3;
	Vec3 p2V3;
	Vec3 pDiffV3;
	private Mesh plane;
	private final Geoset planeGeoset;

	public DrawPlaneAction2(Vec2 p1, Vec2 p2, CameraHandler cameraHandler, Vec3 facingVector,
	                        int numberOfWidthSegments, int numberOfHeightSegments,
	                        Geoset planeGeoset) {
		this.p1 = p1;
		this.p2 = p2;
		this.planeGeoset = planeGeoset;
		this.cameraHandler = cameraHandler;

		makePlaneFromPoints(cameraHandler, numberOfWidthSegments, numberOfHeightSegments);
	}

	public DrawPlaneAction2(Vec2 p1, Vec2 p2, Vec3 p1V3, Vec3 p2V3, CameraHandler cameraHandler, Vec3 facingVector,
	                        int numberOfWidthSegments, int numberOfHeightSegments, Geoset planeGeoset) {
		this.p1 = p1;
		this.p2 = p2;
		this.planeGeoset = planeGeoset;
		this.cameraHandler = cameraHandler;
		this.p1V3 = p1V3;
		this.p2V3 = p2V3;

		makePlaneFromPoints2(cameraHandler, numberOfWidthSegments, numberOfHeightSegments);
	}

	public void makePlaneFromPoints2(CameraHandler cameraHandler,
	                                 int numberOfWidthSegments, int numberOfHeightSegments) {
		pDiffV3 = Vec3.getDiff(p2V3, p1V3);

		Quat trans = new Quat().setFromAxisAngle(0, 1, 0, (float) (Math.PI / 2.0));

		Vec3 flatPosV3 = new Vec3(p1V3).transform(cameraHandler.getViewPortAntiRotMat());


//		plane = ModelUtils.createPlane(cameraHandler.getViewPortAntiRotMat(), facingVector, 0, planeMin, planeMax, numberOfWidthSegments, numberOfHeightSegments);
//		plane = ModelUtils.createPlane3(numberOfWidthSegments, numberOfHeightSegments);
		plane = ModelUtils.createPlane3(3, 3);

		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.setGeoset(planeGeoset);
//			vertex.multiply(pDiffV3);
			vertex.rotate(Vec3.ZERO, trans);
//			vertex.rotate(p1V3, trans);
//			vertex.add(p1V3);
//			vertex.add(flatPosV3);
			vertex.getNormal().rotate(Vec3.ZERO, trans);
//			vertex.multiply(pDiffV3).add(p1V3);

			vertex.transform(cameraHandler.getViewPortAntiRotMat2());
			vertex.getNormal().transform(cameraHandler.getViewPortAntiRotMat2());
			vertex.multiply(pDiffV3).add(p1V3);

//			vertex.add(p1V3);
//			vertex.scale(p1V3, pDiffV3);

//			vertex.transform(trans);
//			vertex.getNormal().transform(cameraHandler.getViewPortAntiRotMat());
//			vertex.getNormal().transform(trans);
		}
		for (Triangle triangle : plane.getTriangles()) {
			triangle.setGeoset(planeGeoset);
		}
	}

	public void makePlaneFromPoints(CameraHandler cameraHandler,
	                                int numberOfWidthSegments, int numberOfHeightSegments) {

		Vec3 ugg = new Vec3(1, 0, 0).transform(cameraHandler.getViewPortAntiRotMat2());

		Vec3 p2V3 = new Vec3(p2.x, p2.y, 0);
		Vec3 pDiffV3 = new Vec3(p2.x - p1.x, p2.y - p1.y, 0);

		Quat trans = new Quat().setFromAxisAngle(0, 1, 0, (float) (Math.PI / 2.0));

//		p1V3 = new Vec3(p1.x, p1.y, 0).rotate(Vec3.ZERO, trans).transform(cameraHandler.getViewPortAntiRotMat2());
		p1V3 = new Vec3(p1.x, p1.y, 0).rotate(Vec3.ZERO, trans);
//		Quat trans1 = new Quat().setFromAxisAngle(ugg, (float) (Math.PI/2.0));
//		Quat trans1 = new Quat().setFromAxisAngle(ugg, (float) 0);
//		trans.mul(trans1);

//		plane = ModelUtils.createPlane(cameraHandler.getViewPortAntiRotMat(), facingVector, 0, planeMin, planeMax, numberOfWidthSegments, numberOfHeightSegments);
//		plane = ModelUtils.createPlane3(numberOfWidthSegments, numberOfHeightSegments);
		plane = ModelUtils.createPlane3(3, 3);

		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.setGeoset(planeGeoset);
			vertex.multiply(pDiffV3);
			vertex.rotate(Vec3.ZERO, trans);
//			vertex.rotate(p1V3, trans);
			vertex.add(p1V3);
			vertex.getNormal().rotate(Vec3.ZERO, trans);
//			vertex.multiply(pDiffV3).add(p1V3);

			vertex.transform(cameraHandler.getViewPortAntiRotMat2());
			vertex.getNormal().transform(cameraHandler.getViewPortAntiRotMat2());
//			vertex.add(p1V3);

//			vertex.transform(trans);
//			vertex.getNormal().transform(cameraHandler.getViewPortAntiRotMat());
//			vertex.getNormal().transform(trans);
		}
		for (Triangle triangle : plane.getTriangles()) {
			triangle.setGeoset(planeGeoset);
		}
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : plane.getVertices()) {
			planeGeoset.remove(vertex);
		}
		for (Triangle triangle : plane.getTriangles()) {
			planeGeoset.remove(triangle);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : plane.getVertices()) {
			planeGeoset.add(vertex);
		}
		for (Triangle triangle : plane.getTriangles()) {
			planeGeoset.add(triangle);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "create plane";
	}

	public Mesh getPlane() {
		return plane;
	}

	@Override
	public void updateTranslation(double deltaX, double deltaY, double deltaZ) {
		scalePlaneToPoints(deltaX, deltaY);
		p2.translate(deltaX, deltaY);
	}
//	public void updateTranslation2(double newP2X, double newP2Y) {
////		p2.translate(deltaX, deltaY);
////		p2.translate(deltaX, deltaY);
//		scalePlaneToPoints(p1, p2, newP2X, newP2Y);
//	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
////		p2.translate(delta.x, delta.y);
//		scalePlaneToPoints(delta.x, delta.y);

//		Vec3 scaleV3 = new Vec3(delta).add(p2V3).divide(p2V3);
//		Vec3 scaleV3 = new Vec3(delta).divide(p2V3).translate(1,1,1);

//		Vec3 pDiffV3New = new Vec3(p2V3).add(delta).sub(p1V3);

//		Vec3 scaleV3 = new Vec3(delta).divide(pDiffV3).translate(1,1,1);
//		Vec3 scaleV3 = new Vec3(pDiffV3New).divide(pDiffV3);
//		Vec3 scaleV3 = new Vec3(p2V3).add(delta).sub(p1V3).divide(pDiffV3);
		Vec3 scaleV3 = new Vec3(delta).divide(pDiffV3).translate(1, 1, 1);
		;


//		scaleV3.transform(cameraHandler.getViewPortAntiRotMat2());
		for (GeosetVertex vertex : plane.getVertices()) {
//			vertex.transform(cameraHandler.getViewPortAntiRotMat()).scale(Vec3.ZERO, scaleV3).transform(cameraHandler.getViewPortAntiRotMat2());
//			vertex.sub(p1V3).transform(cameraHandler.getViewPortAntiRotMat()).scale(Vec3.ZERO, scaleV3).transform(cameraHandler.getViewPortAntiRotMat2()).add(p1V3);
//			vertex.transform(cameraHandler.getViewPortAntiRotMat()).scale(p1V3, scaleV3).transform(cameraHandler.getViewPortAntiRotMat2());
			vertex.scale(p1V3, scaleV3);
		}
		p2V3.add(delta);
//		pDiffV3.set(p2V3).sub(p1V3);
		pDiffV3.add(delta);
		return this;
	}

	public void scalePlaneToPoints1(double deltaX, double deltaY) {
//		Vec2 min = new Vec2(p1).minimize(p2);
//		Vec2 max = new Vec2(p1).maximize(p2);
//		Vec2 p22 = new Vec2(p2).translate(deltaX, deltaY);
////		Vec2 scale = Vec2.getDif(max, min).div(Vec2.getDif(planeMax, planeMin));
		Vec2 scale = new Vec2(deltaX, deltaY).div(p2);
//
//		Vec3 p1V3 = new Vec3(0, p1.x, p1.y).transform(cameraHandler.getViewPortAntiRotMat());
		Vec3 p1V3 = new Vec3(p1.x, p1.y, 0);
////		Vec3 planeMinV3 = new Vec3(planeMin.x, planeMin.y, 0).transform(cameraHandler.getViewPortAntiRotMat2());
////		Vec3 planeMaxV3 = new Vec3(planeMax.x, planeMax.y, 0).transform(cameraHandler.getViewPortAntiRotMat2());
////		Vec3 minV3 = new Vec3(min.x, min.y, 0).transform(cameraHandler.getViewPortAntiRotMat2());
////		Vec3 maxV3 = new Vec3(max.x, max.y, 0).transform(cameraHandler.getViewPortAntiRotMat2());
//		Vec3 scaleV3 = new Vec3( scale.x, scale.y, 0).transform(cameraHandler.getViewPortAntiRotMat());
//		Vec3 scaleV3 = new Vec3( scale.x, scale.y, 0);
		Vec3 scaleV3 = new Vec3((deltaX / p2.x) + 1, (deltaY / p2.y) + 1, 0);
		for (GeosetVertex vertex : plane.getVertices()) {
//			shiftPlanePoint(vertex, min, scale);
//			vertex.scale(p1V3, scaleV3);
			vertex.scale(Vec3.ZERO, scaleV3);
//			vertex.sub(planeMinV3).multiply(scaleV3).add(minV3);
		}
//
//		planeMin = min;
//		planeMax = max;
	}

	public void scalePlaneToPoints(double deltaX, double deltaY) {
		Vec3 scaleV3 = new Vec3(0, (deltaX / (p2.x - p1.x)) + 1, (deltaY / (p2.y - p1.y)) + 1);
//		scaleV3.transform(cameraHandler.getViewPortAntiRotMat2());
		for (GeosetVertex vertex : plane.getVertices()) {
//			vertex.transform(cameraHandler.getViewPortAntiRotMat()).scale(Vec3.ZERO, scaleV3).transform(cameraHandler.getViewPortAntiRotMat2());
//			vertex.sub(p1V3).transform(cameraHandler.getViewPortAntiRotMat()).scale(Vec3.ZERO, scaleV3).transform(cameraHandler.getViewPortAntiRotMat2()).add(p1V3);
			vertex.transform(cameraHandler.getViewPortAntiRotMat()).scale(p1V3, scaleV3).transform(cameraHandler.getViewPortAntiRotMat2());
		}
	}

	public void scalePlaneToPoints2(double deltaX, double deltaY) {
		Vec3 scaleV3 = new Vec3((deltaX / p2.x) + 1, (deltaY / p2.y) + 1, 0);
		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.scale(Vec3.ZERO, scaleV3);
		}
	}
}
