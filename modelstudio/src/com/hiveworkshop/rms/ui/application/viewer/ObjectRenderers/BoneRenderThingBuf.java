package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import static org.lwjgl.opengl.GL11.*;

public class BoneRenderThingBuf {
	private CameraHandler cameraHandler;
	private Vec3 diffVec = new Vec3();
	private Vec3 tempVec = new Vec3();
	Quat difRotR = new Quat();
	Quat rot90 = new Quat();
	private Vec3[] pointsJoint;
	private Vec3[] normals;
	private Vec3[] renderPointsJoint;
	private Vec3[] renderNormals;
	private Vec3[] pointsStemTop;
	private Vec3[] renderPointsStemTop;
	private Vec3[] pointsStemBot;
	private Vec3[] renderPointsStemBot;
	public BoneRenderThingBuf(CameraHandler cameraHandler){
		this.cameraHandler = cameraHandler;
		float boxRadLength = 1.0f;
		float boxRadHeight = 1.0f;
//			float boxRadHeight = 0f;
		float boxRadWidth = 1.0f;
		float stemTopSize = .4f;
		float stemBotSize = .15f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

		pointsJoint = new Vec3[]{
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, down * boxRadHeight)};
		renderPointsJoint = new Vec3[]{
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0)};
		normals = new Vec3[]{
				new Vec3(frnt, 0, 0),
				new Vec3(back, 0, 0),
				new Vec3(0, left, 0),
				new Vec3(0, rght, 0),
				new Vec3(0, 0, uppp),
				new Vec3(0, 0, down),
		};
		renderNormals = new Vec3[]{
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0)};

//		pointsStemTop = new Vec3[]{
//				new Vec3(frnt * boxRadLength*stemTopSize, rght * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
//				new Vec3(back * boxRadLength*stemTopSize, rght * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
//				new Vec3(frnt * boxRadLength*stemTopSize, left * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
//				new Vec3(back * boxRadLength*stemTopSize, left * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
//				new Vec3(0,0,down * boxRadHeight*stemTopSize)};
		pointsStemTop = new Vec3[]{
				new Vec3(frnt * boxRadLength*stemTopSize, rght * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
				new Vec3(back * boxRadLength*stemTopSize, rght * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
				new Vec3(frnt * boxRadLength*stemTopSize, left * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize),
				new Vec3(back * boxRadLength*stemTopSize, left * boxRadWidth*stemTopSize, uppp * boxRadHeight*stemTopSize)};
		renderPointsStemTop = new Vec3[]{
				new Vec3(frnt, rght, uppp),
				new Vec3(back, rght, uppp),
				new Vec3(frnt, left, uppp),
				new Vec3(back, left, uppp)};

		pointsStemBot = new Vec3[]{
				new Vec3(frnt * boxRadLength*stemBotSize, rght * boxRadWidth*stemBotSize, down * boxRadHeight*stemBotSize),
				new Vec3(back * boxRadLength*stemBotSize, rght * boxRadWidth*stemBotSize, down * boxRadHeight*stemBotSize),
				new Vec3(frnt * boxRadLength*stemBotSize, left * boxRadWidth*stemBotSize, down * boxRadHeight*stemBotSize),
				new Vec3(back * boxRadLength*stemBotSize, left * boxRadWidth*stemBotSize, down * boxRadHeight*stemBotSize)};
		renderPointsStemBot = new Vec3[]{
				new Vec3(frnt, rght, down),
				new Vec3(back, rght, down),
				new Vec3(frnt, left, down),
				new Vec3(back, left, down)};

	}

	public int getQuadsPerObj(){
		return 12;
	}

	public void paintBones(VertexBuffers renderBuffers, ModelView modelView, RenderModel renderModel, IdObject idObject) {
		RenderNode2 renderNode = renderModel.getRenderNode(idObject);
		if (renderNode != null && modelView.shouldRender(idObject)) {

			Vec3 renderPosNode = renderNode.getPivot();
			float nodeSize = (float) (cameraHandler.sizeAdj() * idObject.getClickRadius() / 2f);
			if (renderNode.hasParent()) {
				RenderNode2 parentNode = renderModel.getRenderNode(idObject.getParent());
				transform2(renderPosNode, parentNode.getPivot(), nodeSize);
			} else {
				transform2(renderPosNode, renderPosNode, nodeSize);
			}

			EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();


			float[] rgba = getRGBA(modelView, idObject, colorPrefs);


			doJoint(renderBuffers, rgba);
			doStem2(renderBuffers, rgba);

//			doGlGeom(renderBuffers, rgba);
		}
	}

	public float[] getRGBA(ModelView modelView, IdObject idObject, EditorColorPrefs colorPrefs) {
		float[] rgba;
		if (modelView.getHighlightedNode() == idObject) {
			rgba = colorPrefs.getColorComponents(ColorThing.NODE_HIGHLIGHTED);
		} else if (!modelView.isEditable(idObject)) {
			rgba = colorPrefs.getColorComponents(ColorThing.NODE_UNEDITABLE);
		} else if (modelView.isSelected(idObject)) {
			rgba = colorPrefs.getColorComponents(ColorThing.NODE_SELECTED);
		} else {
			rgba = colorPrefs.getColorComponents(ColorThing.NODE);
		}
		return rgba;
	}

	public BoneRenderThingBuf transform(Quat rot, Vec3 p1, Vec3 p2){
		for(int i = 0; i< pointsJoint.length; i++){
			renderPointsJoint[i].set(pointsJoint[i]).transform(rot).add(p1);
		}
		for (int i = 0; i < pointsStemTop.length; i++) {
			renderPointsStemTop[i].set(pointsStemTop[i]).transform(rot).add(p2);
		}
		for (int i = 0; i < pointsStemBot.length; i++) {
			renderPointsStemBot[i].set(pointsStemBot[i]).transform(rot).add(p1);
		}

		for (int i = 0; i < normals.length; i++) {
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}

	public BoneRenderThingBuf transform(Quat rot, Vec3 p1, Vec3 p2, float scale) {
		for (int i = 0; i < pointsJoint.length; i++) {
			renderPointsJoint[i].set(pointsJoint[i]).scale(scale).transform(rot).add(p1);
		}
		for (int i = 0; i < pointsStemTop.length; i++) {
			renderPointsStemTop[i].set(pointsStemTop[i]).scale(scale).transform(rot).add(p2);
		}
		for (int i = 0; i < pointsStemBot.length; i++) {
			renderPointsStemBot[i].set(pointsStemBot[i]).scale(scale).transform(rot).add(p1);
		}

		for (int i = 0; i < normals.length; i++) {
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}

	public BoneRenderThingBuf transform2(Vec3 p1, Vec3 p2, float nodeSize) {
		diffVec.set(p2).sub(p1);
//		System.out.println("dist to par: " + diffVec.length());
		if (diffVec.x == 0 && diffVec.y == 0 && diffVec.z == 0) {
			diffVec.set(Vec3.Z_AXIS).scale(0.01f);
		}
//		else {
//			nodeSize = diffVec.length() / 10;
//		}
		tempVec.set(Vec3.Z_AXIS).cross(diffVec).normalize();

		difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
		rot90.setFromAxisAngle(tempVec, (float) (Math.PI / 2)).normalize();
		difRotR.mul(rot90).normalize();

		transform(difRotR, p1, p2, nodeSize);
//		transform(difRotR, p1, p2);
		return this;
	}

	public BoneRenderThingBuf doGlGeom(VertexBuffers buffers, float[] color) {
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
		glColor4f(color[0], color[1], color[2], color[3]);
		glBegin(GL_TRIANGLES);
		doJoint(buffers, color);
		doStem2(buffers, color);
		glEnd();
		return this;
	}

	private void doJoint(VertexBuffers buffers, float[] color) {
		//Front
		doGlTriQuad(buffers, renderPointsJoint[6], renderPointsJoint[4], renderPointsJoint[7], renderPointsJoint[5], renderNormals[0], color);
		//Back
		doGlTriQuad(buffers, renderPointsJoint[3], renderPointsJoint[1], renderPointsJoint[2], renderPointsJoint[0], renderNormals[1], color);
		//Left
		doGlTriQuad(buffers, renderPointsJoint[0], renderPointsJoint[4], renderPointsJoint[2], renderPointsJoint[6], renderNormals[2], color);
		//Right
		doGlTriQuad(buffers, renderPointsJoint[3], renderPointsJoint[7], renderPointsJoint[1], renderPointsJoint[5], renderNormals[3], color);
		//Up
		doGlTriQuad(buffers, renderPointsJoint[1], renderPointsJoint[5], renderPointsJoint[0], renderPointsJoint[4], renderNormals[4], color);
		//Down
		doGlTriQuad(buffers, renderPointsJoint[2], renderPointsJoint[6], renderPointsJoint[3], renderPointsJoint[7], renderNormals[5], color);

	}

	private void doStem2(VertexBuffers buffers, float[] color) {

		//Down
		doGlTriQuad(buffers, renderPointsStemBot[0], renderPointsStemBot[1], renderPointsStemBot[2], renderPointsStemBot[3], renderNormals[0], color);
		//Up
		doGlTriQuad(buffers, renderPointsStemTop[1], renderPointsStemTop[0], renderPointsStemTop[3], renderPointsStemTop[2], renderNormals[1], color);
		//Front
		doGlTriQuad(buffers, renderPointsStemTop[0], renderPointsStemBot[0], renderPointsStemTop[2], renderPointsStemBot[2], renderNormals[2], color);
		//Back
		doGlTriQuad(buffers, renderPointsStemTop[3], renderPointsStemBot[3], renderPointsStemTop[1], renderPointsStemBot[1], renderNormals[3], color);
		//Right
		doGlTriQuad(buffers, renderPointsStemTop[1], renderPointsStemBot[1], renderPointsStemTop[0], renderPointsStemBot[0], renderNormals[4], color);
		//Left
		doGlTriQuad(buffers, renderPointsStemTop[2], renderPointsStemBot[2], renderPointsStemTop[3], renderPointsStemBot[3], renderNormals[5], color);


	}

	private static void doGlTriQuad(VertexBuffers buffers, Vec3 LT, Vec3 LB, Vec3 RT, Vec3 RB, Vec3 normal, float[] color) {
		buffers.setMultiple(LT, normal, color);
		buffers.setMultiple(LB, normal, color);
		buffers.setMultiple(RT, normal, color);

		buffers.setMultiple(RT, normal, color);
		buffers.setMultiple(LB, normal, color);
		buffers.setMultiple(RB, normal, color);

	}
}
