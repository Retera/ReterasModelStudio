package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class CameraRenderThing {
	private final Vec3 diffVec = new Vec3();
	private final Vec3 tempVec = new Vec3();
	private final Quat difRotR = new Quat();
	private final Quat rot90 = new Quat();
	private final Vec3[] normals;
	private final Vec3[] renderNormals;

	private final Vec3[] pointsPosBox;
	private final Vec3[] renderPointsPosBox;
	private final Vec3[] pointsTargetBox;
	private final Vec3[] renderPointsTargetBox;
	private final Vec3[] pointsFoVRect;
	private final Vec3[] renderPointsFoVRekt;
	private final Vec3[] pointsFarClipMark;
	private final Vec3[] renderPointsFarClipMark;
	private final Vec3[] pointsNearClipMark;
	private final Vec3[] renderPointsNearClipMark;

	private final Vec3 farClipPoint = new Vec3(0, 0, 10);
	private final Vec3 camPoint = new Vec3(0, 0, 10);
	private static final float sqRootTwo = (float) Math.sqrt(2);


	public CameraRenderThing(){
		float boxRadLength = 1.0f;
		float boxRadHeight = 1.0f;
		float boxRadWidth = 1.0f;
		float FovBoxSize = 1.0f;
		float markerSize = 1.0f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

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

		pointsPosBox = new Vec3[]{
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, down * boxRadHeight)};
		renderPointsPosBox = new Vec3[]{
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0)};

		pointsTargetBox = new Vec3[]{
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, down * boxRadHeight)};
		renderPointsTargetBox = new Vec3[]{
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0),
				new Vec3(0, 0, 0)};

		pointsFoVRect = new Vec3[]{
//				new Vec3(0, rght * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize),
//				new Vec3(0, rght * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize),
//				new Vec3(0, left * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize),
//				new Vec3(0, left * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize)};
				new Vec3(frnt * boxRadLength*FovBoxSize, rght * boxRadWidth*FovBoxSize, 0*uppp * boxRadHeight*FovBoxSize),
				new Vec3(back * boxRadLength*FovBoxSize, rght * boxRadWidth*FovBoxSize, 0*uppp * boxRadHeight*FovBoxSize),
				new Vec3(back * boxRadLength*FovBoxSize, left * boxRadWidth*FovBoxSize, 0*uppp * boxRadHeight*FovBoxSize),
				new Vec3(frnt * boxRadLength*FovBoxSize, left * boxRadWidth*FovBoxSize, 0*uppp * boxRadHeight*FovBoxSize)};
		renderPointsFoVRekt = new Vec3[]{
//				new Vec3(0, rght, uppp),
//				new Vec3(0, rght, uppp),
//				new Vec3(0, left, uppp),
//				new Vec3(0, left, uppp)};
				new Vec3(frnt, rght, uppp),
				new Vec3(back, rght, uppp),
				new Vec3(frnt, left, uppp),
				new Vec3(back, left, uppp)};

		pointsFarClipMark = new Vec3[]{
				new Vec3(frnt * boxRadLength*markerSize, rght * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize),
				new Vec3(back * boxRadLength*markerSize, rght * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize),
				new Vec3(frnt * boxRadLength*markerSize, left * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize),
				new Vec3(back * boxRadLength*markerSize, left * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize)};
//		pointsFarClipMark = new Vec3[]{
//				new Vec3(frnt * boxRadLength*markerSize, rght * boxRadWidth*markerSize, down * boxRadHeight*markerSize),
//				new Vec3(back * boxRadLength*markerSize, rght * boxRadWidth*markerSize, down * boxRadHeight*markerSize),
//				new Vec3(frnt * boxRadLength*markerSize, left * boxRadWidth*markerSize, down * boxRadHeight*markerSize),
//				new Vec3(back * boxRadLength*markerSize, left * boxRadWidth*markerSize, down * boxRadHeight*markerSize)};
		renderPointsFarClipMark = new Vec3[]{
				new Vec3(frnt, rght, down),
				new Vec3(back, rght, down),
				new Vec3(frnt, left, down),
				new Vec3(back, left, down)};
		pointsNearClipMark = new Vec3[]{
				new Vec3(frnt * boxRadLength*markerSize, rght * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize),
				new Vec3(back * boxRadLength*markerSize, rght * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize),
				new Vec3(frnt * boxRadLength*markerSize, left * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize),
				new Vec3(back * boxRadLength*markerSize, left * boxRadWidth*markerSize, 0*down * boxRadHeight*markerSize)};
		renderPointsNearClipMark = new Vec3[]{
				new Vec3(frnt, rght, down),
				new Vec3(back, rght, down),
				new Vec3(frnt, left, down),
				new Vec3(back, left, down)};

	}
	public void paintCameras(ModelView modelView, RenderModel renderModel, Camera camera) {
		RenderNodeCamera renderNode = renderModel.getRenderNode(camera.getSourceNode());
		if (renderNode != null && modelView.shouldRender(camera)) {

			Vec3 renderPosNode = renderNode.getPivot(); // ToDo fix animated camera rotation stuff
			Vec3 targetPosition = renderNode.getTarget();
			transform2(renderPosNode, targetPosition, camera.getNearClip(), camera.getFarClip(), camera.getFieldOfView());

			float[] components = getColor(modelView, camera.getSourceNode());
			doGlGeomSource(components);

			components = getColor(modelView, camera.getTargetNode());
			doGlGeomTarget(components);

			components = getColor(modelView, camera);
			doGlGeomMarkers(components);

		}
	}

	private float[] getColor(ModelView modelView, CameraNode node) {
		EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();
		float[] components;
		if (modelView.getHighlightedCamera() == node.getParent()) {
			components = colorPrefs.getColorComponents(ColorThing.NODE_HIGHLIGHTED);
		} else if (!modelView.isEditable(node)) {
			components = colorPrefs.getColorComponents(ColorThing.NODE_UNEDITABLE);
		} else if (modelView.isSelected(node)) {
			components = colorPrefs.getColorComponents(ColorThing.NODE_SELECTED);
		} else {
			components = colorPrefs.getColorComponents(ColorThing.NODE);
		}
		return components;
	}

	private float[] getColor(ModelView modelView, Camera camera) {
		EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();
		float[] components;
		if (modelView.getHighlightedCamera() == camera) {
			components = colorPrefs.getColorComponents(ColorThing.NODE_HIGHLIGHTED);
		} else if (!modelView.isEditable(camera)) {
			components = colorPrefs.getColorComponents(ColorThing.NODE_UNEDITABLE);
		} else if (modelView.isSelected(camera)) {
			components = colorPrefs.getColorComponents(ColorThing.NODE_SELECTED);
		} else {
			components = colorPrefs.getColorComponents(ColorThing.NODE);
		}
		return components;
	}

	public CameraRenderThing transform2(Vec3 p1, Vec3 p2, double nearClip, double farClip, double fov) {
		diffVec.set(p2).sub(p1);
		if (diffVec.x == 0 && diffVec.y == 0 && diffVec.z == 0) {
			diffVec.set(Vec3.Z_AXIS).scale(0.01f);
		}

		difRotR.setFromAxisAngle(Vec3.Y_AXIS, (float) diffVec.radAngleTo(Vec3.Z_AXIS)).normalize();
		tempVec.set(diffVec.x, diffVec.y, 0);
		rot90.setFromAxisAngle(Vec3.Z_AXIS, (float) tempVec.radAngleTo2(Vec3.X_AXIS));


		difRotR.mulLeft(rot90);

		transform(difRotR, p1, p2, nearClip, farClip, fov);

		return this;
	}

	public CameraRenderThing transform(Quat rot, Vec3 pos, Vec3 target, double nearClip, double farClip, double fov){
		// camera position cube
		camPoint.set(pos);
		for(int i = 0; i< pointsPosBox.length; i++){
			renderPointsPosBox[i].set(pointsPosBox[i]).transform(rot).add(pos);
		}
		// camera target cube
		for (int i = 0; i < pointsTargetBox.length; i++) {
			renderPointsTargetBox[i].set(pointsTargetBox[i]).transform(rot).add(target);
		}
		// camera FoV Rectangle
		float fovAdj = (float) (farClip * Math.tan(fov/2)) * sqRootTwo;
		for (int i = 0; i < pointsFoVRect.length; i++) {
			renderPointsFoVRekt[i].set(pointsFoVRect[i]).scale(fovAdj).translate(0,0,farClip).transform(rot).add(pos);
		}
//		float fovAdj = (float) (fovBoxD.z * Math.tan(fov/2)) * sqRootTwo;
//		for (int i = 0; i < pointsFoVRect.length; i++) {
//			renderPointsFoVRekt[i].set(pointsFoVRect[i]).scale(fovAdj).add(fovBoxD).transform(rot).add(pos);
//		}
		farClipPoint.set(0,0,farClip).transform(rot).add(pos);
		for (int i = 0; i < pointsFarClipMark.length; i++) {
			renderPointsFarClipMark[i].set(pointsFarClipMark[i]).translate(0,0,farClip).transform(rot).add(pos);
		}

		float nearClipAdj = (float) (nearClip * Math.tan(fov/2)) * sqRootTwo;
		for (int i = 0; i < pointsNearClipMark.length; i++) {
//			renderPointsNearClipMark[i].set(pointsNearClipMark[i]).translate(0,0,nearClip).transform(rot).add(pos);
			renderPointsNearClipMark[i].set(pointsNearClipMark[i]).scale(nearClipAdj).translate(0,0,nearClip).transform(rot).add(pos);
		}

		for (int i = 0; i < normals.length; i++) {
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}


	public CameraRenderThing doGlGeomSource(float[] color) {
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
		glColor4f(color[0], color[1], color[2], color[3]);
		doPosBox();
		return this;
	}

	public CameraRenderThing doGlGeomTarget(float[] color) {
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
		glColor4f(color[0], color[1], color[2], color[3]);
		doTargetBox();
		return this;
	}

	public CameraRenderThing doGlGeomMarkers(float[] color) {
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glColor4f(color[0], color[1], color[2], color[3]/2f);
		doCameraMarkers();
		return this;
	}

	private void doPosBox() {
		glBegin(GL_TRIANGLES);
		//Front
		doGlTriQuad(renderPointsPosBox[6], renderPointsPosBox[4], renderPointsPosBox[7], renderPointsPosBox[5], renderNormals[0]);

		//Back
		doGlTriQuad(renderPointsPosBox[3], renderPointsPosBox[1], renderPointsPosBox[2], renderPointsPosBox[0], renderNormals[1]);

		//Left
		doGlTriQuad(renderPointsPosBox[0], renderPointsPosBox[4], renderPointsPosBox[2], renderPointsPosBox[6], renderNormals[2]);

		//Right
		doGlTriQuad(renderPointsPosBox[3], renderPointsPosBox[7], renderPointsPosBox[1], renderPointsPosBox[5], renderNormals[3]);

		//Up
		doGlTriQuad(renderPointsPosBox[1], renderPointsPosBox[5], renderPointsPosBox[0], renderPointsPosBox[4], renderNormals[4]);

		//Down
		doGlTriQuad(renderPointsPosBox[2], renderPointsPosBox[6], renderPointsPosBox[3], renderPointsPosBox[7], renderNormals[5]);
		glEnd();
	}

	private void doTargetBox() {
		glBegin(GL_TRIANGLES);
		//Front
		doGlTriQuad(renderPointsTargetBox[6], renderPointsTargetBox[4], renderPointsTargetBox[7], renderPointsTargetBox[5], renderNormals[0]);

		//Back
		doGlTriQuad(renderPointsTargetBox[3], renderPointsTargetBox[1], renderPointsTargetBox[2], renderPointsTargetBox[0], renderNormals[1]);

		//Left
		doGlTriQuad(renderPointsTargetBox[0], renderPointsTargetBox[4], renderPointsTargetBox[2], renderPointsTargetBox[6], renderNormals[2]);

		//Right
		doGlTriQuad(renderPointsTargetBox[3], renderPointsTargetBox[7], renderPointsTargetBox[1], renderPointsTargetBox[5], renderNormals[3]);

		//Up
		doGlTriQuad(renderPointsTargetBox[1], renderPointsTargetBox[5], renderPointsTargetBox[0], renderPointsTargetBox[4], renderNormals[4]);

		//Down
		doGlTriQuad(renderPointsTargetBox[2], renderPointsTargetBox[6], renderPointsTargetBox[3], renderPointsTargetBox[7], renderNormals[5]);
		glEnd();
	}

	private void doCameraMarkers() {

		glBegin(GL_LINE_LOOP);
		doGlLineLoop(renderPointsFarClipMark[0], renderPointsFarClipMark[1], renderPointsFarClipMark[2], renderPointsFarClipMark[3], renderNormals[0]);
		glEnd();

		glBegin(GL_LINE_LOOP);
		doGlLineLoop(renderPointsNearClipMark[0], renderPointsNearClipMark[1], renderPointsNearClipMark[2], renderPointsNearClipMark[3], renderNormals[0]);
		glEnd();

		glBegin(GL_LINE_LOOP);
		doGlLineLoop(renderPointsFoVRekt[1], renderPointsFoVRekt[0], renderPointsFoVRekt[3], renderPointsFoVRekt[2], renderNormals[0]);
		glEnd();

		glBegin(GL_LINES);
		for (Vec3 fovCorner : renderPointsFoVRekt){
			doGlLine(camPoint, fovCorner, renderNormals[0]);
		}
		glEnd();

		glColor4f(0f, 0f, 0f, .3f);
		glBegin(GL_LINES);
		doGlLine(camPoint, farClipPoint, renderNormals[0]);
		glEnd();

		boolean fillInFrustum = false;
		if(fillInFrustum){
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glBegin(GL_TRIANGLES);
			for (int i = 0; i < renderPointsFoVRekt.length; i++){
				glColor4f(0f, 0f, 0f, .3f);
				doGlTri(renderPointsFoVRekt[i], renderPointsFoVRekt[(i+1)%renderPointsFoVRekt.length], camPoint, renderNormals[0]);
			}
			for (Vec3 fovCorner : renderPointsFoVRekt){
				doGlLine(camPoint, fovCorner, renderNormals[0]);
			}
			glEnd();
		}
	}

	private static void doGlTriQuad(Vec3 LT, Vec3 LB, Vec3 RT, Vec3 RB, Vec3 normal) {
		GL11.glNormal3f(normal.x,normal.y,normal.z);

		GL11.glVertex3f(LT.x, LT.y, LT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RT.x, RT.y, RT.z);

		GL11.glVertex3f(RT.x, RT.y, RT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RB.x, RB.y, RB.z);
	}

	private static void doGlTri(Vec3 LT, Vec3 LB, Vec3 RT, Vec3 normal) {
		GL11.glNormal3f(normal.x,normal.y,normal.z);

		GL11.glVertex3f(LT.x, LT.y, LT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RT.x, RT.y, RT.z);
	}
	private static void doGlLineLoop(Vec3 LT, Vec3 LB, Vec3 RT, Vec3 RB, Vec3 normal) {
		GL11.glNormal3f(normal.x,normal.y,normal.z);

		GL11.glVertex3f(LT.x, LT.y, LT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RB.x, RB.y, RB.z);
		GL11.glVertex3f(RT.x, RT.y, RT.z);

	}
	private static void doGlLine(Vec3 lineStart, Vec3 lineEnd, Vec3 normal) {
		GL11.glNormal3f(normal.x,normal.y,normal.z);

		GL11.glVertex3f(lineStart.x, lineStart.y, lineStart.z);
		GL11.glVertex3f(lineEnd.x, lineEnd.y, lineEnd.z);

	}
}
