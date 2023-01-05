package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class VertRendererThingBuf {
	Vec3 normal = new Vec3(Vec3.X_AXIS);
	Vec3 renderNormal = new Vec3();
	Vec3 baseSize = new Vec3(1, 1, 1);
	Vec3[] orgPoints;
	Vec3[] points;
	Vec3[] renderPoints;


	public VertRendererThingBuf(float v) {
		float boxRadLength = .001f;
		float boxRadHeight = .5f * v;
		float boxRadWidth = .5f * v;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

		orgPoints = new Vec3[] {
				new Vec3(frnt, left, uppp),
				new Vec3(frnt, rght, uppp),
				new Vec3(frnt, rght, down),
				new Vec3(frnt, left, down),
		};
		points = new Vec3[] {
				new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight),
				new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight),
				new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight),
		};
		renderPoints = new Vec3[] {
				new Vec3(),
				new Vec3(),
				new Vec3(),
				new Vec3(),
		};
	}

	public VertRendererThingBuf updateSquareSize(float v) {
		baseSize.set(.001f, .5f * v, .5f * v);
		for (int i = 0; i < points.length; i++) {
			points[i].set(orgPoints[i]).multiply(baseSize);
		}
		return this;
	}

	public VertRendererThingBuf doGlGeom(VertexBuffers renderBuffers, float[] rgba) {
//		doGlQuad(renderPoints[0], renderPoints[1], renderPoints[2], renderPoints[3], renderNormal);
		doGlTriQuad(renderBuffers, renderPoints[0], renderPoints[1], renderPoints[2], renderPoints[3], renderNormal, rgba);
		return this;
	}

	public VertRendererThingBuf transform(Mat4 wM, Vec3 renderPos) {
		for (int i = 0; i < points.length; i++) {
			renderPoints[i].set(points[i]).transform(wM).add(renderPos);
		}
		renderNormal.set(normal).transform(wM);
		return this;
	}
	public VertRendererThingBuf transform(Quat rot, Vec3 renderPos) {
		for(int i = 0; i<points.length; i++){
			renderPoints[i].set(points[i]).transform(rot).add(renderPos);
		}
		renderNormal.set(normal).transform(rot);
		return this;
	}


	private static void doGlTriQuad(VertexBuffers buffers, Vec3 LT, Vec3 LB, Vec3 RT, Vec3 RB, Vec3 normal, float[] color) {
		buffers.setMultiple(LT, normal, color);
		buffers.setMultiple(LB, normal, color);
		buffers.setMultiple(RT, normal, color);

		buffers.setMultiple(LT, normal, color);
		buffers.setMultiple(RT, normal, color);
		buffers.setMultiple(RB, normal, color);
//		buffers.setMultiple(RT, normal, color);
//		buffers.setMultiple(LB, normal, color);
//		buffers.setMultiple(RB, normal, color);

	}
}
