package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class DrawPlaneAction2 implements GenericMoveAction {

	private final Mat4 viewPortAntiRotMat2;
	private final Vec2 p1;
	private final Vec2 p2;
	Vec3 p1V3;
	Vec3 p2V3;
	Vec3 pDiffV3;
	private Mesh plane;
	private final Geoset planeGeoset;

	public DrawPlaneAction2(Vec2 p1, Vec2 p2, Mat4 viewPortAntiRotMat2, Vec3 facingVector,
	                        int numberOfWidthSegments, int numberOfHeightSegments,
	                        Geoset planeGeoset) {
		this.p1 = p1;
		this.p2 = p2;
		this.p1V3 = new Vec3(0, p1.x, p1.y).transform(viewPortAntiRotMat2);
		this.p2V3 = new Vec3(0, p2.x, p2.y).transform(viewPortAntiRotMat2);
//		this.p1V3 = new Vec3(0, p1.x, p1.y).transformInverted(viewPortAntiRotMat2);
//		this.p2V3 = new Vec3(0, p2.x, p2.y).transformInverted(viewPortAntiRotMat2);
		pDiffV3 = new Vec3(p1V3).sub(p2V3);
		this.planeGeoset = planeGeoset;
		this.viewPortAntiRotMat2 = viewPortAntiRotMat2;

//		makePlaneFromPoints(numberOfWidthSegments, numberOfHeightSegments);
		makePlaneFromPoints2(viewPortAntiRotMat2, numberOfWidthSegments, numberOfHeightSegments);
	}

	public DrawPlaneAction2(Vec2 p1, Vec2 p2, Vec3 p1V3, Vec3 p2V3, Mat4 viewPortAntiRotMat2, Vec3 facingVector,
	                        int numberOfWidthSegments, int numberOfHeightSegments, Geoset planeGeoset) {
		this.p1 = p1;
		this.p2 = p2;
		this.planeGeoset = planeGeoset;
		this.p1V3 = p1V3;
		this.p2V3 = p2V3;


		this.viewPortAntiRotMat2 = viewPortAntiRotMat2;
		makePlaneFromPoints2(viewPortAntiRotMat2, numberOfWidthSegments, numberOfHeightSegments);
	}

	public void makePlaneFromPoints2(Mat4 viewPortAntiRotMat2,
	                                 int numberOfWidthSegments, int numberOfHeightSegments) {

//		plane = ModelUtils.createPlane3(numberOfWidthSegments, numberOfHeightSegments);
		Quat trans = new Quat(Vec3.Y_AXIS, (float) Math.toRadians(-90));
		plane = ModelUtils.createPlane3(3, 3);
//		pDiffV3.set(p2V3).sub(p1V3);

		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.setGeoset(planeGeoset);

//			// Rotate to face up..?
//			vertex.transform(trans);
			vertex.getNormal().transform(trans);

			// rotate to face camera
			vertex.transform(viewPortAntiRotMat2);
			vertex.getNormal().transform(viewPortAntiRotMat2);
//			vertex.transformInverted(viewPortAntiRotMat2);
//			vertex.getNormal().transformInverted(viewPortAntiRotMat2);

			// scale and move
//			vertex.multiply(pDiffV3).add(p1V3);
			vertex.scale(10).add(p1V3);
//			vertex.scale(10);
		}
		for (Triangle triangle : plane.getTriangles()) {
			triangle.setGeoset(planeGeoset);
		}
	}

	public void makePlaneFromPoints(int numberOfWidthSegments, int numberOfHeightSegments) {
		pDiffV3.set(p2V3).sub(p1V3);
		Vec3 pDiffV3 = new Vec3(p2.x - p1.x, p2.y - p1.y, 0);

		Quat trans = new Quat().setFromAxisAngle(0, 1, 0, (float) (Math.PI / 2.0));

		p1V3 = new Vec3(p1.x, p1.y, 0).rotate(Vec3.ZERO, trans);

//		plane = ModelUtils.createPlane3(numberOfWidthSegments, numberOfHeightSegments);
		plane = ModelUtils.createPlane3(3, 3);

		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.setGeoset(planeGeoset);
			vertex.multiply(pDiffV3);

			// Rotate to face up..?
			vertex.rotate(Vec3.ZERO, trans);
			vertex.getNormal().rotate(Vec3.ZERO, trans);

			vertex.add(p1V3);

			vertex.transform(viewPortAntiRotMat2);
			vertex.getNormal().transform(viewPortAntiRotMat2);

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
//		scalePlaneToPoints(deltaX, deltaY, viewPortAntiRotMat2);
//		p2.translate(deltaX, deltaY);
	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
//		Vec3 scaleV3 = new Vec3(delta).divide(pDiffV3).add(Vec3.ONE);
//
//		for (GeosetVertex vertex : plane.getVertices()) {
//			vertex.scale(p1V3, scaleV3);
//		}
//		p2V3.add(delta);
//		pDiffV3.add(delta);
		return this;
	}

	public void scalePlaneToPoints1(double deltaX, double deltaY) {
		Vec2 scale = new Vec2(deltaX, deltaY).div(p2);
		Vec3 p1V3 = new Vec3(p1.x, p1.y, 0);

		Vec3 scaleV3 = new Vec3((deltaX / p2.x) + 1, (deltaY / p2.y) + 1, 0);
		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.scale(Vec3.ZERO, scaleV3);
		}
	}

	public void scalePlaneToPoints(double deltaX, double deltaY, Mat4 viewPortAntiRotMat) {
		Vec3 scaleV3 = new Vec3(0, (deltaX / (p2.x - p1.x)) + 1, (deltaY / (p2.y - p1.y)) + 1);
		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.transformInverted(viewPortAntiRotMat).scale(p1V3, scaleV3).transform(viewPortAntiRotMat);
		}
	}

	public void scalePlaneToPoints2(double deltaX, double deltaY) {
		Vec3 scaleV3 = new Vec3((deltaX / p2.x) + 1, (deltaY / p2.y) + 1, 0);
		for (GeosetVertex vertex : plane.getVertices()) {
			vertex.scale(Vec3.ZERO, scaleV3);
		}
	}
}
