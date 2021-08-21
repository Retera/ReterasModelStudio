package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class BoneRenderThing2 {
	private static final Vec3 zAxis = new Vec3(0, 0, 1);
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
	public BoneRenderThing2(){
		float boxRadLength = 1.5f;
		float boxRadHeight = 1.5f;
//			float boxRadHeight = 0f;
		float boxRadWidth = 1.5f;
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

	public BoneRenderThing2 transform(Quat rot, Vec3 p1, Vec3 p2){
		for(int i = 0; i< pointsJoint.length; i++){
			renderPointsJoint[i].set(pointsJoint[i]).transform(rot).add(p1);
		}
		for(int i = 0; i< pointsStemTop.length; i++){
			renderPointsStemTop[i].set(pointsStemTop[i]).transform(rot).add(p2);
		}
		for(int i = 0; i< pointsStemBot.length; i++){
			renderPointsStemBot[i].set(pointsStemBot[i]).transform(rot).add(p1);
		}

		for(int i = 0; i<normals.length; i++){
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}
	public BoneRenderThing2 transform2(Vec3 p1, Vec3 p2){
		diffVec.set(p2).sub(p1);
		if(diffVec.x==0 && diffVec.y == 0 && diffVec.z == 0){
			diffVec.set(zAxis).scale(0.01f);
		}
		tempVec.set(zAxis).cross(diffVec).normalize();

		difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
		rot90.setFromAxisAngle(tempVec, (float) (Math.PI/2)).normalize();
		difRotR.mul(rot90).normalize();

		transform(difRotR, p1, p2);

		return this;
	}
	public BoneRenderThing2 transform2(Vec3 p1, Vec3 p2, Vec3 scale){
		diffVec.set(p2).sub(p1);
		if(diffVec.x==0 && diffVec.y == 0 && diffVec.z == 0){
			diffVec.set(zAxis).scale(0.01f);
		}
		tempVec.set(zAxis).cross(diffVec).normalize();

		difRotR.setFromAxisAngle(tempVec, (float) (diffVec.getAngleToZaxis())).normalize();
		rot90.setFromAxisAngle(tempVec, (float) (Math.PI/2)).normalize();
		difRotR.mul(rot90).normalize();

		transform(difRotR, p1, p2);

		return this;
	}

	public BoneRenderThing2 doGlGeom(float[] color) {
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
//		glBegin(GL_QUADS);
		glColor4f(color[0], color[1], color[2], color[3]);
//		glBegin(GL_TRIANGLE_STRIP);
		glBegin(GL_TRIANGLES);
		doJoint();
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

	private void doJoint() {
		//Front
		doGlTriQuad(renderPointsJoint[6], renderPointsJoint[4], renderPointsJoint[7], renderPointsJoint[5], renderNormals[0]);

		//Back
		doGlTriQuad(renderPointsJoint[3], renderPointsJoint[1], renderPointsJoint[2], renderPointsJoint[0], renderNormals[1]);

		//Left
		doGlTriQuad(renderPointsJoint[0], renderPointsJoint[4], renderPointsJoint[2], renderPointsJoint[6], renderNormals[2]);

		//Right
		doGlTriQuad(renderPointsJoint[3], renderPointsJoint[7], renderPointsJoint[1], renderPointsJoint[5], renderNormals[3]);

		//Up
		doGlTriQuad(renderPointsJoint[1], renderPointsJoint[5], renderPointsJoint[0], renderPointsJoint[4], renderNormals[4]);

		//Down
		doGlTriQuad(renderPointsJoint[2], renderPointsJoint[6], renderPointsJoint[3], renderPointsJoint[7], renderNormals[5]);
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
		doGlTriQuad(renderPointsStemBot[0], renderPointsStemBot[1], renderPointsStemBot[2], renderPointsStemBot[3], renderNormals[0]);

		//Up
//		glColor4f(1,1,0,1);
//		doGlQuad(renderPointsStemTop[3], renderPointsStemTop[1], renderPointsStemTop[2], renderPointsStemTop[0], renderNormals[1]);
		doGlTriQuad(renderPointsStemTop[1], renderPointsStemTop[0], renderPointsStemTop[3], renderPointsStemTop[2], renderNormals[1]);
		//Front
//		glColor4f(1,0,0,1);
//		doGlQuad(renderPointsStemTop[0], renderPointsStemBot[0], renderPointsStemTop[2], renderPointsStemBot[2], renderNormals[2]);
//		doGlQuad(renderPointsStemTop[0], renderPointsStemTop[2], renderPointsStemBot[0], renderPointsStemBot[2], renderNormals[2]);
		doGlTriQuad(renderPointsStemTop[0], renderPointsStemBot[0], renderPointsStemTop[2], renderPointsStemBot[2], renderNormals[2]);
		//Back
//		glColor4f(0,1,1,1);
		doGlTriQuad(renderPointsStemTop[3], renderPointsStemBot[3], renderPointsStemTop[1], renderPointsStemBot[1], renderNormals[3]);
		//Right
//		glColor4f(0,1,0,1);
		doGlTriQuad(renderPointsStemTop[1], renderPointsStemBot[1], renderPointsStemTop[0], renderPointsStemBot[0], renderNormals[4]);
		//Left
//		glColor4f(1,0,1,1);
		doGlTriQuad(renderPointsStemTop[2], renderPointsStemBot[2], renderPointsStemTop[3], renderPointsStemBot[3], renderNormals[5]);
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
