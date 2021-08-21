package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

public class BoneRenderThing {
	private static final Vec3 zAxis = new Vec3(0, 0, 1);
	private Vec3 diffVec = new Vec3();
	private Vec3 tempVec = new Vec3();
	Quat difRotR = new Quat();
	Quat rot90 = new Quat();
	private Vec3[] points;
	private Vec3[] normals;
	private Vec3[] renderPoints;
	private Vec3[] renderNormals;
	public BoneRenderThing(){
		float boxRadLength = 1.5f;
		float boxRadHeight = 1.5f;
//			float boxRadHeight = 0f;
		float boxRadWidth = 1.5f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

		points = new Vec3[]{
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(back * boxRadLength, left * boxRadWidth, down * boxRadHeight)};
		renderPoints = new Vec3[]{
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

	}

	public BoneRenderThing transform(Quat rot, Vec3 p1, Vec3 p2){
		for(int i = 0; i<points.length; i++){
			renderPoints[i].set(points[i]).transform(rot);
			if(i%2 == 0){
				renderPoints[i].add(p1);
			} else {
				renderPoints[i].add(p2);
			}
		}

		for(int i = 0; i<normals.length; i++){
			renderNormals[i].set(normals[i]).transform(rot);
		}

		return this;
	}
	public BoneRenderThing transform2(Vec3 p1, Vec3 p2){
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
	public BoneRenderThing transform2(Vec3 p1, Vec3 p2, Vec3 scale){
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

	public BoneRenderThing doGlGeom() {
		//Front
		doGlQuad(renderPoints[6], renderPoints[4], renderPoints[7], renderPoints[5], renderNormals[0]);

		//Back
		doGlQuad(renderPoints[3], renderPoints[1], renderPoints[2], renderPoints[0], renderNormals[1]);

		//Left
		doGlQuad(renderPoints[0], renderPoints[4], renderPoints[2], renderPoints[6], renderNormals[2]);

		//Right
		doGlQuad(renderPoints[3], renderPoints[7], renderPoints[1], renderPoints[5], renderNormals[3]);

		//Up
		doGlQuad(renderPoints[1], renderPoints[5], renderPoints[0], renderPoints[4], renderNormals[4]);

		//Down
		doGlQuad(renderPoints[2], renderPoints[6], renderPoints[3], renderPoints[7], renderNormals[5]);
		return this;
	}

	private static void doGlQuad(Vec3 RT, Vec3 LT, Vec3 RB, Vec3 LB, Vec3 normal) {
		GL11.glNormal3f(normal.x,normal.y,normal.z);
		GL11.glVertex3f(RT.x, RT.y, RT.z);
		GL11.glVertex3f(LT.x, LT.y, LT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RB.x, RB.y, RB.z);
	}
}
