package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

public class VertRendererThing {
	Vec3 normal = new Vec3(1,0,0);
	Vec3 renderNormal = new Vec3();
	Vec3[] points;
	Vec3[] renderPoints;


	public VertRendererThing(float v){
		float boxRadLength = .001f;
		float boxRadHeight = .5f * v;
		float boxRadWidth = .5f * v;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

		points = new Vec3[]{
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
		};
		renderPoints = new Vec3[]{
				new Vec3(),
				new Vec3(),
				new Vec3(),
				new Vec3(),
		};
	}

	public VertRendererThing doGlGeom() {
//		doGlQuad(renderPoints[0], renderPoints[1], renderPoints[2], renderPoints[3], renderNormal);
		doGlTriQuad(renderPoints[0], renderPoints[1], renderPoints[2], renderPoints[3], renderNormal);
		return this;
	}

	public VertRendererThing transform(Mat4 wM, Vec3 renderPos) {
		for(int i = 0; i<points.length; i++){
			renderPoints[i].set(points[i]).transform(wM).add(renderPos);
		}
		renderNormal.set(normal).transform(wM);
		return this;
	}
	public VertRendererThing transform(Quat rot, Vec3 renderPos) {
		for(int i = 0; i<points.length; i++){
			renderPoints[i].set(points[i]).transform(rot).add(renderPos);
		}
		renderNormal.set(normal).transform(rot);
		return this;
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
