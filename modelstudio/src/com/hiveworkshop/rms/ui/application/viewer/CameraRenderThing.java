package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class CameraRenderThing {
	private Vec3 diffVec = new Vec3();
	private Vec3 tempVec = new Vec3();
	Quat difRotR = new Quat();
	Quat rot90 = new Quat();
	private Vec3[] normals;
	private Vec3[] renderNormals;

	private Vec3[] pointsPosBox;
	private Vec3[] renderPointsPosBox;
	private Vec3[] pointsTargetBox;
	private Vec3[] renderPointsTargetBox;
	private Vec3[] pointsFoVRect;
	private Vec3[] renderPointsFoVRekt;
	private Vec3[] pointsFarClipMark;
	private Vec3[] renderPointsFarClipMark;
	private Vec3[] pointsNearClipMark;
	private Vec3[] renderPointsNearClipMark;


//		Vec3 start = camera.position;
//		Vec3 end = camera.targetPosition;
//
//		g2.translate(end.x, end.y);
//		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
//		double zoom = CoordSysUtils.getZoom(coordinateSystem);
//		int size = (int) (20 * zoom);
//		double dist = start.distance(end);
//
//		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawRect((int) dist - size, -size, size * 2, size * 2);
//
//		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawLine(0, 0, size, size);
//		g2.drawLine(0, 0, size, -size);
//
//		g2.drawLine(0, 0, (int) dist, 0);
	public CameraRenderThing(){
		float boxRadLength = 1.0f;
		float boxRadHeight = 1.0f;
//			float boxRadHeight = 0f;
		float boxRadWidth = 1.0f;
		float FovBoxSize = 5.0f;
		float markerSize = 3.0f;
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
				new Vec3(frnt * boxRadLength*FovBoxSize, rght * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize),
				new Vec3(back * boxRadLength*FovBoxSize, rght * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize),
				new Vec3(frnt * boxRadLength*FovBoxSize, left * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize),
				new Vec3(back * boxRadLength*FovBoxSize, left * boxRadWidth*FovBoxSize, uppp * boxRadHeight*FovBoxSize)};
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
		RenderNode renderNode = renderModel.getRenderNode(camera);
		if (renderNode != null && modelView.shouldRender(camera)) {

			Vec3 renderPosNode = renderNode.getPivot(); // ToDo fix animated camera stuff
			transform2(camera.getPosition(), camera.getTargetPosition(), camera.getNearClip(), camera.getFarClip());

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

			doGlGeom(components);
		}
	}

	public CameraRenderThing transform(Quat rot, Vec3 pos, Vec3 target, double nearClip, double farClip){
		// camera position cube
		for(int i = 0; i< pointsPosBox.length; i++){
			renderPointsPosBox[i].set(pointsPosBox[i]).transform(rot).add(pos);
		}
		// camera target cube
		for (int i = 0; i < pointsTargetBox.length; i++) {
			renderPointsTargetBox[i].set(pointsTargetBox[i]).transform(rot).add(target);
		}
		// camera FoV Rectangle
		for (int i = 0; i < pointsFoVRect.length; i++) {
			renderPointsFoVRekt[i].set(pointsFoVRect[i]).transform(rot).add(pos);
		}
		for (int i = 0; i < pointsFarClipMark.length; i++) {
//			renderPointsFarClipMark[i].set(pointsFarClipMark[i]).transform(rot).add(pos);
			renderPointsFarClipMark[i].set(pointsFarClipMark[i]).translate(0,0,farClip).transform(rot).add(pos);
		}
		for (int i = 0; i < pointsNearClipMark.length; i++) {
//			renderPointsNearClipMark[i].set(pointsNearClipMark[i]).transform(rot).add(pos);
			renderPointsNearClipMark[i].set(pointsNearClipMark[i]).translate(0,0,nearClip).transform(rot).add(pos);
		}

		for (int i = 0; i < normals.length; i++) {
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}

	public CameraRenderThing transform(Vec3 pos, Vec3 target, double nearClip, double farClip){
		for(int i = 0; i< pointsPosBox.length; i++){
			renderPointsPosBox[i].set(pointsPosBox[i]).add(pos);
		}
		for (int i = 0; i < pointsTargetBox.length; i++) {
			renderPointsTargetBox[i].set(pointsTargetBox[i]).add(target);
		}
		for (int i = 0; i < pointsFoVRect.length; i++) {
			renderPointsFoVRekt[i].set(pointsFoVRect[i]).add(pos);
		}
		for (int i = 0; i < pointsFarClipMark.length; i++) {
//			renderPointsFarClipMark[i].set(pointsFarClipMark[i]).translate(farClip,0,0).add(pos);
			renderPointsFarClipMark[i].set(pointsFarClipMark[i]).add(pos);
		}
		for (int i = 0; i < pointsNearClipMark.length; i++) {
			renderPointsNearClipMark[i].set(pointsNearClipMark[i]).add(pos);
//			renderPointsNearClipMark[i].set(pointsNearClipMark[i]).translate(nearClip,0,0).add(pos);
		}

		for (int i = 0; i < normals.length; i++) {
			renderNormals[i].set(normals[i]);
		}

		return this;
	}

	public CameraRenderThing transform(Quat rot, Vec3 p1, Vec3 p2, float scale) {
		for (int i = 0; i < pointsPosBox.length; i++) {
			renderPointsPosBox[i].set(pointsPosBox[i]).scale(scale).transform(rot).add(p1);
		}
		for (int i = 0; i < pointsFoVRect.length; i++) {
			renderPointsFoVRekt[i].set(pointsFoVRect[i]).scale(scale).transform(rot).add(p2);
		}
		for (int i = 0; i < pointsFarClipMark.length; i++) {
			renderPointsFarClipMark[i].set(pointsFarClipMark[i]).scale(scale).transform(rot).add(p1);
		}

		for (int i = 0; i < normals.length; i++) {
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}

	public CameraRenderThing transform2(Vec3 p1, Vec3 p2) {
		diffVec.set(p2).sub(p1);
		float scale = 1;
//		System.out.println("dist to par: " + diffVec.length());
		if (diffVec.x == 0 && diffVec.y == 0 && diffVec.z == 0) {
			diffVec.set(Vec3.Z_AXIS).scale(0.01f);
		}
//		else {
//			scale = diffVec.length() / 10;
//		}
		tempVec.set(Vec3.Z_AXIS).cross(diffVec).normalize();

		difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
		rot90.setFromAxisAngle(tempVec, (float) (Math.PI / 2)).normalize();
		difRotR.mul(rot90).normalize();

		transform(difRotR, p1, p2, scale);
//		transform(difRotR, p1, p2);

		return this;
	}
	public CameraRenderThing transform2(Vec3 p1, Vec3 p2, double nearClip, double farClip) {
		diffVec.set(p2).sub(p1);
		if (diffVec.x == 0 && diffVec.y == 0 && diffVec.z == 0) {
			diffVec.set(Vec3.Z_AXIS).scale(0.01f);
		}
		tempVec.set(Vec3.Z_AXIS).cross(diffVec).normalize();

		difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
		rot90.setFromAxisAngle(tempVec, (float) (Math.PI / 2)).normalize();
		difRotR.mul(rot90).normalize();
		difRotR.setAsRotBetween(diffVec, Vec3.Z_AXIS);

		transform(difRotR, p1, p2, nearClip, farClip);

		return this;
	}
	public CameraRenderThing transform2(Vec3 p1, Vec3 p2, float nodeSize) {
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

	public CameraRenderThing doGlGeom(float[] color) {
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
//		glBegin(GL_QUADS);
		glColor4f(color[0], color[1], color[2], color[3]);
//		glBegin(GL_TRIANGLE_STRIP);
		glBegin(GL_TRIANGLES);
		doPosBox();
		doTargetBox();
		doStem2();
		glEnd();
//		glBegin(GL_TRIANGLE_STRIP);
//		doStem2();
//		glEnd();
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		glBegin(GL_TRIANGLES);
//		glColor4f(color[0], color[1], color[2], color[3]);
//		doStem();
//		glEnd();
		return this;
	}

	private void doPosBox() {
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
	}

	private void doTargetBox() {
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
	}

	private void doStem2() {
//		renderPointsStemTop = new Vec3[]{
//				new Vec3( 1, -1, 1),// F R 0
//				new Vec3(-1, -1, 1),// B R 1
//				new Vec3( 1,  1, 1),// F L 2
//				new Vec3(-1,  1, 1) // B L 3
//		};
		//Down
//		glColor4f(0,0,1,1);
//		doGlQuad(renderPointsStemBot[2], renderPointsStemBot[0], renderPointsStemBot[3], renderPointsStemBot[1], renderNormals[0]);

		doGlTriQuad(renderPointsFarClipMark[0], renderPointsFarClipMark[1], renderPointsFarClipMark[2], renderPointsFarClipMark[3], renderNormals[0]);
		doGlTriQuad(renderPointsNearClipMark[0], renderPointsNearClipMark[1], renderPointsNearClipMark[2], renderPointsNearClipMark[3], renderNormals[0]);

		//Up
//		glColor4f(1,1,0,1);
//		doGlQuad(renderPointsStemTop[3], renderPointsStemTop[1], renderPointsStemTop[2], renderPointsStemTop[0], renderNormals[1]);

//		// FoV
//		doGlTriQuad(renderPointsFoVRekt[1], renderPointsFoVRekt[0], renderPointsFoVRekt[3], renderPointsFoVRekt[2], renderNormals[1]);


//		//Front
////		glColor4f(1,0,0,1);
////		doGlQuad(renderPointsStemTop[0], renderPointsStemBot[0], renderPointsStemTop[2], renderPointsStemBot[2], renderNormals[2]);
////		doGlQuad(renderPointsStemTop[0], renderPointsStemTop[2], renderPointsStemBot[0], renderPointsStemBot[2], renderNormals[2]);
//		doGlTriQuad(renderPointsFoVRekt[0], renderPointsFarClipMark[0], renderPointsFoVRekt[2], renderPointsFarClipMark[2], renderNormals[2]);
//		//Back
////		glColor4f(0,1,1,1);
//		doGlTriQuad(renderPointsFoVRekt[3], renderPointsFarClipMark[3], renderPointsFoVRekt[1], renderPointsFarClipMark[1], renderNormals[3]);
//		//Right
////		glColor4f(0,1,0,1);
//		doGlTriQuad(renderPointsFoVRekt[1], renderPointsFarClipMark[1], renderPointsFoVRekt[0], renderPointsFarClipMark[0], renderNormals[4]);
//		//Left
////		glColor4f(1,0,1,1);
//		doGlTriQuad(renderPointsFoVRekt[2], renderPointsFarClipMark[2], renderPointsFoVRekt[3], renderPointsFarClipMark[3], renderNormals[5]);
	}


//	pointsStem =
	private void doStem() {

//		Vec3[] ugg = new Vec3[]{
//				new Vec3( 1, -1, 1),// F R 0
//				new Vec3(-1, -1, 1),// B R 1
//				new Vec3( 1,  1, 1),// F L 2
//				new Vec3(-1,  1, 1),// B L 3
//				new Vec3( 0, 0, -1)};
		//Front
//		doGlTri(renderPointsStem[0], renderPointsStem[2], renderPointsStem[4], renderNormals[0]);
//
//		//Back
//		doGlTri(renderPointsStem[3], renderPointsStem[1], renderPointsStem[4], renderNormals[1]);
//
//		//Left
//		doGlTri(renderPointsStem[3], renderPointsStem[2], renderPointsStem[4], renderNormals[2]);
//
//		//Right
//		doGlTri(renderPointsStem[0], renderPointsStem[1], renderPointsStem[4], renderNormals[3]);
	}

	private static void doGlQuad(Vec3 RT, Vec3 LT, Vec3 RB, Vec3 LB, Vec3 normal) {
		GL11.glNormal3f(normal.x,normal.y,normal.z);
		GL11.glVertex3f(RT.x, RT.y, RT.z);
		GL11.glVertex3f(LT.x, LT.y, LT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RB.x, RB.y, RB.z);
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
}
