package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class CameraBufferFiller {
	private final Vec3 diffVec = new Vec3();
	private final Vec3 tempVec = new Vec3();
	private final Quat difRotR = new Quat();
	private final Quat rot90 = new Quat();

	private final Vec3 farClipPoint = new Vec3(0, 0, 10);
	private final Vec3 camPoint = new Vec3(0, 0, 10);
	private static final float sqRootTwo = (float) Math.sqrt(2);


	private RenderModel renderModel;
	private ModelView modelView;

	Quad xPlane = new Quad(1, null, 1, -1.0, 1, -1.0);
	Quad xRenderPlane = new Quad();
	Quad yPlane = new Quad(1, -1.0, 1, null, 1, -1.0);
	Quad yRenderPlane = new Quad();
	Quad zPlane = new Quad(1, -1.0, 1, -1.0, 1, null);
	Quad zRenderPlane = new Quad();

	Vec3[] planePoints = {
			new Vec3(-1,-1,  1),
			new Vec3(-1, 1,  1),
			new Vec3(-1, 1, -1),
			new Vec3(-1,-1, -1),
	};
	Vec3[] renderPlanePoints = {
			new Vec3(1, 1, -1),
			new Vec3(1,-1, -1),
			new Vec3(1, 1,  1),
			new Vec3(1,-1,  1),
	};

	Vec3[] upMarker = {
			new Vec3(),
			new Vec3(),
			new Vec3(),
	};


	public CameraBufferFiller(){

	}


	public CameraBufferFiller setModel(RenderModel renderModel, ModelView modelView){
		this.renderModel = renderModel;
		this.modelView = modelView;
		return this;
	}

	public void fillBuffer(ShaderPipeline pipeline){
		pipeline.prepare();
//		Vec2 selStat = new Vec2();

		for (Camera camera : modelView.getVisibleCameras()) {
			RenderNodeCamera renderNode = renderModel.getRenderNode(camera);
			if(renderNode != null){
				Vec3 pivot = renderNode.getPivot();
				Vec3 target = renderNode.getTarget();
				Quat rot = calcRot(pivot, target, renderNode.getLocalRotation());
				ugg(pipeline, camera, rot, pivot, target, renderNode.getNearClip(), renderNode.getFarClip(), renderNode.getFoV());
			}
		}
	}


	private Quat calcRot(Vec3 pos, Vec3 target, Quat rot){
		diffVec.set(target).sub(pos);
		if (diffVec.x == 0 && diffVec.y == 0 && diffVec.z == 0) {
			diffVec.set(Vec3.Z_AXIS).scale(0.01f);
		}

		difRotR.setFromAxisAngle(Vec3.Y_AXIS, (float) (diffVec.radAngleTo(Vec3.Z_AXIS) + (Math.PI / 2.0))).normalize();
		tempVec.set(diffVec.x, diffVec.y, 0);
		rot90.setFromAxisAngle(Vec3.Z_AXIS, (float) tempVec.radAngleTo2(Vec3.X_AXIS));


		difRotR.mulLeft(rot90).mulLeft(rot);


		return difRotR;
	}


	public void ugg(ShaderPipeline pipeline, Camera camera, Quat rot, Vec3 pos, Vec3 target, double nearClip, double farClip, double fov){
		double tanHalfFoV = Math.tan(fov / 2);
		float targetSideDist = (float) (pos.distance(target) * tanHalfFoV);
		float farClipSideDist = (float) (farClip * tanHalfFoV);
		float nearClipSideDist = (float) (nearClip * tanHalfFoV);

		int sourceSel = getSelectionStatus(camera.getSourceNode());
		int targetSel = getSelectionStatus(camera.getTargetNode());



		upMarker[0].set(-nearClip,  nearClipSideDist, -nearClipSideDist).transform(rot).add(pos);
		upMarker[1].set(-nearClip, -nearClipSideDist, -nearClipSideDist).transform(rot).add(pos);
		upMarker[2].set(-nearClip,     0,  -nearClipSideDist*1.5).transform(rot).add(pos);
		doGlLineLoopS(pipeline, Vec3.ZERO, sourceSel, upMarker);

		// add nearPlane Square
		Vec3 nearAdj = new Vec3(nearClip,  nearClipSideDist,  nearClipSideDist);

		for(int i = 0; i< renderPlanePoints.length; i++){
			renderPlanePoints[i].set(planePoints[i]).multiply(nearAdj).transform(rot).add(pos);
		}
		doGlLineLoopS(pipeline, Vec3.ZERO, sourceSel, renderPlanePoints);

//		xRenderPlane.set(xPlane).mul(nearAdj).rot(rot).add(pos);
//		doGlLineLoopS(pipeline, 0, xRenderPlane);

		// add targetMarker Square
		Vec3 targAdj = new Vec3(pos.distance(target), targetSideDist,  targetSideDist);

		for(int i = 0; i< renderPlanePoints.length; i++){
			renderPlanePoints[i].set(planePoints[i]).multiply(targAdj).transform(rot).add(pos);
		}
		doGlLineLoopS(pipeline, Vec3.ZERO, targetSel, renderPlanePoints);

		doGLLine(pipeline, Vec3.ZERO, targetSel, renderPlanePoints[0], renderPlanePoints[2]);
		doGLLine(pipeline, Vec3.ZERO, targetSel, renderPlanePoints[1], renderPlanePoints[3]);



		// add farPlane Square
		Vec3 farAdj = new Vec3(farClip, farClipSideDist,  farClipSideDist);

		for(int i = 0; i< renderPlanePoints.length; i++){
			renderPlanePoints[i].set(planePoints[i]).multiply(farAdj).transform(rot).add(pos);
		}
		doGlLineLoopS(pipeline, Vec3.ZERO, sourceSel, renderPlanePoints);

		doGLLine(pipeline, Vec3.ZERO, sourceSel, renderPlanePoints[0], renderPlanePoints[2]);
		doGLLine(pipeline, Vec3.ZERO, sourceSel, renderPlanePoints[1], renderPlanePoints[3]);



//		xRenderPlane.set(xPlane).mul(farAdj).rot(rot).add(pos);
//		doGlLineLoopS(pipeline, 0, xRenderPlane);
		// add frustum Lines
		for(Vec3 farPoint : renderPlanePoints){
			doGLLine(pipeline, Vec3.ZERO, sourceSel, pos, farPoint);
		}

//		doGLLine(pipeline, Vec3.ZERO, 0, pos, xRenderPlane.p0);
//		doGLLine(pipeline, Vec3.ZERO, 0, pos, xRenderPlane.p1);
//		doGLLine(pipeline, Vec3.ZERO, 0, pos, xRenderPlane.p2);
//		doGLLine(pipeline, Vec3.ZERO, 0, pos, xRenderPlane.p3);

		// add targetLine
		doGLLine(pipeline, Vec3.ZERO, targetSel, pos, target);


		// add uppMarker Lines

		// add CamBox
		// add TargetBox


	}

	private int getSelectionStatus(CameraNode cameraNode){
		if(modelView.getHighlightedCamera() != null && modelView.getHighlightedCamera() == cameraNode.getParent()) {
			return 0;
		} else if(modelView.isEditable(cameraNode)){
			if (modelView.isSelected(cameraNode)) {
				return 1;
			} else {
				return 2;
			}
		}
//		else if (!modelView.isHidden(vertex)){
//		}
		return 3;
	}

	private static void doGlLineLoopS(ShaderPipeline pipeline, Vec3 normal, int selectionStatus, Vec3... points) {
		for(int i = 0; i<points.length; i++){
			doGLLine(pipeline, normal, selectionStatus, points[i], points[(i + 1) % points.length]);
		}
	}
	private static void doGlLineLoopS(ShaderPipeline pipeline, int selectionStatus, Quad quad) {
		doGLLine(pipeline, quad.norm, selectionStatus, quad.p0, quad.p1);
		doGLLine(pipeline, quad.norm, selectionStatus, quad.p1, quad.p2);
		doGLLine(pipeline, quad.norm, selectionStatus, quad.p2, quad.p3);
		doGLLine(pipeline, quad.norm, selectionStatus, quad.p3, quad.p0);
	}


	private static void doGlTriQuad(ShaderPipeline pipeline, int selectionStatus, Quad quad) {
		pipeline.addVert(quad.p0, quad.norm, null, null, null, null, selectionStatus);
		pipeline.addVert(quad.p1, quad.norm, null, null, null, null, selectionStatus);
		pipeline.addVert(quad.p2, quad.norm, null, null, null, null, selectionStatus);

		pipeline.addVert(quad.p2, quad.norm, null, null, null, null, selectionStatus);
		pipeline.addVert(quad.p1, quad.norm, null, null, null, null, selectionStatus);
		pipeline.addVert(quad.p3, quad.norm, null, null, null, null, selectionStatus);
	}

	private static void doGLLine(ShaderPipeline pipeline, Vec3 normal, int selectionStatus, Vec3 lineStart, Vec3 lineEnd) {
		pipeline.addVert(lineStart, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(lineEnd, normal, null, null, null, null, selectionStatus);
	}



	private static void doGlTriQuadS(ShaderPipeline pipeline, Vec3 LT, Vec3 LB, Vec3 RT, Vec3 RB, Vec3 normal, int selectionStatus) {
		pipeline.addVert(LT, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(LB, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(RT, normal, null, null, null, null, selectionStatus);

		pipeline.addVert(RT, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(LB, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(RB, normal, null, null, null, null, selectionStatus);
	}

	private static void doGlTriS(ShaderPipeline pipeline, Vec3 LT, Vec3 LB, Vec3 RT, Vec3 normal, int selectionStatus) {
		pipeline.addVert(LT, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(LB, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(RT, normal, null, null, null, null, selectionStatus);
	}

	private static void doGlLineLoopS(ShaderPipeline pipeline, Vec3 LT, Vec3 LB, Vec3 RT, Vec3 RB, Vec3 normal, int selectionStatus) {
		pipeline.addVert(LT, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(LB, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(RB, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(RT, normal, null, null, null, null, selectionStatus);
	}

	private static void doGlLineS(ShaderPipeline pipeline, Vec3 lineStart, Vec3 lineEnd, Vec3 normal, int selectionStatus) {
		pipeline.addVert(lineStart, normal, null, null, null, null, selectionStatus);
		pipeline.addVert(lineEnd, normal, null, null, null, null, selectionStatus);
	}

	private static class Quad {
		Vec3 p0;
		Vec3 p1;
		Vec3 p2;
		Vec3 p3;
		Vec3 norm;

		Quad(){
			p0 = new Vec3(1, 1, -1);
			p1 = new Vec3(1,-1, -1);
			p2 = new Vec3(1, 1,  1);
			p3 = new Vec3(1,-1,  1);
			norm = new Vec3();
		}
		Quad(double x0, Double x1, double y0, Double y1, double z0, Double z1){
			if(x1 == null){
				p0 = new Vec3(x0, y0, z1);
				p1 = new Vec3(x0, y1, z1);
				p2 = new Vec3(x0, y0, z0);
				p3 = new Vec3(x0, y1, z0);
				norm = new Vec3(x0,0, 0).normalize();
			} else if(y1 == null){
				p0 = new Vec3(x0, y0, z1);
				p1 = new Vec3(x1, y0, z1);
				p2 = new Vec3(x0, y0, z0);
				p3 = new Vec3(x1, y0, z0);
				norm = new Vec3(0, y0,0).normalize();
			} else if(z1 == null){
				p0 = new Vec3(x1, y0, z0);
				p1 = new Vec3(x1, y1, z0);
				p2 = new Vec3(x0, y0, z0);
				p3 = new Vec3(x0, y1, z0);
				norm = new Vec3(0,0, z0).normalize();
			}
		}

		Quad set(Quad quad){
			p0.set(quad.p0);
			p1.set(quad.p1);
			p2.set(quad.p2);
			p3.set(quad.p3);
			norm.set(quad.norm);
			return this;
		}
		Quad mul(Vec3 v){
			p0.multiply(v);
			p1.multiply(v);
			p2.multiply(v);
			p3.multiply(v);
			return this;
		}
		Quad rot(Quat q){
			p0.transform(q);
			p1.transform(q);
			p2.transform(q);
			p3.transform(q);
			norm.transform(q);
			return this;
		}
		Quad add(Vec3 v){
			p0.add(v);
			p1.add(v);
			p2.add(v);
			p3.add(v);
			return this;
		}
		Quad mulAll(Vec3 v){
			p0.multiply(v);
			p1.multiply(v);
			p2.multiply(v);
			p3.multiply(v);
			norm.multiply(v);
			return this;
		}
		Quad flipp(){
			p0.scale(-1);
			p1.scale(-1);
			p2.scale(-1);
			p3.scale(-1);
			norm.scale(-1);
			return this;
		}
	}
}
