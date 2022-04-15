package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class GridPainter2 {
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	CameraHandler cameraHandler;
	float lineLength = 200;
	float lineSpacing = 100;
	float numberOfLines = 5;
	float subDivs = 10;
	float highlightEveryN = 5;
	float[] lineHeapPos = new float[3];
	float[] lineHeapNeg = new float[3];

	Vec2 vec2Heap = new Vec2();
	Vec3 vec3Heap = new Vec3();
	Vec4 colorHeap = new Vec4(1f, 1f, 1f, .3f);

	public GridPainter2(CameraHandler cameraHandler) {
		this.cameraHandler = cameraHandler;
	}

	public void paintGrid(ShaderPipeline pipeline) {
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		pipeline.glEnableIfNeeded(GL11.GL_BLEND);
		pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
		pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
		pipeline.glDisableIfNeeded(GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		pipeline.glBegin(GL11.GL_LINES);

		float cameraPxSize1 = (float) (cameraHandler.sizeAdj() * 2f); // 1px
		double v = (int)Math.log10(cameraPxSize1);
		float lineScaleMul = (float) Math.pow(10, v);
//		float lineScaleMul = (float) 1;

		float[] lineSpacingArr = new float[] {10*lineScaleMul, 50*lineScaleMul, 100*lineScaleMul};
		colorHeap.set(1f, 1f, 1f, .3f);

		//Grid floor X
		fillLineHeap(X, Y, Z);
		drawDuoLine(pipeline, lineSpacingArr, 0, Y);

		float upAngle = cameraHandler.getYAngle() % 180;
		float spinAngle = cameraHandler.getZAngle() % 180;
		boolean isSide = upAngle == 0 && spinAngle == 90;
		boolean isFront = upAngle == 0 && spinAngle == 0;
		if (cameraHandler.isOrtho() && isSide) {
			//Side Horizontal Lines
			zeroLineHeap(Y);
			drawDuoLine(pipeline, lineSpacingArr, 0, Z);
		}

		//Grid floor Y
		fillLineHeap(Y, X, Z);
		drawDuoLine(pipeline, lineSpacingArr, 0, X);

		if(cameraHandler.isOrtho() && isFront){
			//Front Horizontal Lines
			zeroLineHeap(X);
			drawDuoLine(pipeline, lineSpacingArr, 0, Z);
		}

		if(cameraHandler.isOrtho() && isSide){
			//Side Vertical Lines
			fillLineHeap(Z, X, Y);
			drawDuoLine(pipeline, lineSpacingArr, 0, X);
		}

		if(cameraHandler.isOrtho() && isFront){
			//Front Vertical Lines
			fillLineHeap(Z, X, Y);
//			zeroLineHeap(X);
			drawDuoLine(pipeline, lineSpacingArr, 0, Y);
		}

		colorHeap.set(1f, .5f, .5f, .7f);
		pipeline.addVert(vec3Heap.set(-lineLength, 0, 0), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		pipeline.addVert(vec3Heap.set(lineLength, 0, 0), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

		colorHeap.set(.5f, 1f, .5f, .7f);
		pipeline.addVert(vec3Heap.set(0, -lineLength, 0), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		pipeline.addVert(vec3Heap.set(0, lineLength, 0), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

		colorHeap.set(.5f, .5f, 1f, .7f);
		pipeline.addVert(vec3Heap.set(0, 0, -lineLength), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		pipeline.addVert(vec3Heap.set(0, 0, lineLength), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

		pipeline.glEnd();
	}

	private void fillLineHeap(int pos) {
		lineHeapPos[0] = 0;
		lineHeapPos[1] = 0;
		lineHeapPos[2] = 0;
		lineHeapNeg[0] = 0;
		lineHeapNeg[1] = 0;
		lineHeapNeg[2] = 0;

		lineHeapPos[pos] = lineLength;
		lineHeapNeg[pos] = -lineLength;
	}
	private void fillLineHeap2(int pos) {
		lineHeapPos[(pos+1)%3] = 0;
		lineHeapNeg[(pos+1)%3] = 0;
		lineHeapPos[(pos+2)%3] = 0;
		lineHeapNeg[(pos+2)%3] = 0;

		lineHeapPos[pos] = lineLength;
		lineHeapNeg[pos] = -lineLength;
	}
	private void fillLineHeap(int mainPos, int zero1, int zero2) {
		lineHeapPos[mainPos] = lineLength;
		lineHeapNeg[mainPos] = -lineLength;

		lineHeapPos[zero1] = 0;
		lineHeapNeg[zero1] = 0;

		lineHeapPos[zero2] = 0;
		lineHeapNeg[zero2] = 0;
	}
	private void setLineHeap(int pos, float value) {
		lineHeapPos[pos] = value;
		lineHeapNeg[pos] = -value;
	}
	private void setLineHeap(int pos) {
		lineHeapPos[pos] = lineLength;
		lineHeapNeg[pos] = -lineLength;
	}
	private void zeroLineHeap(int pos) {
		lineHeapPos[pos] = 0;
		lineHeapNeg[pos] = 0;
	}
	private void zeroLineHeap(int pos1, int pos2) {
		lineHeapPos[pos1] = 0;
		lineHeapNeg[pos1] = 0;
		lineHeapPos[pos2] = 0;
		lineHeapNeg[pos2] = 0;
	}

	private void drawDuoLine(ShaderPipeline pipeline, float[] lineSpacing, int index, int lhi){
		float lS = lineSpacing[index];//1
		for (lineHeapNeg[lhi] = lS, lineHeapPos[lhi] = lS; lineHeapNeg[lhi] < lineLength; lineHeapNeg[lhi] += lS, lineHeapPos[lhi] += lS) {
			pipeline.addVert(vec3Heap.set(lineHeapNeg), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			pipeline.addVert(vec3Heap.set(lineHeapPos), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		}
		for (lineHeapNeg[lhi] = -lS, lineHeapPos[lhi] = -lS; lineHeapNeg[lhi] > -lineLength; lineHeapNeg[lhi] -= lS, lineHeapPos[lhi] -= lS) {
			pipeline.addVert(vec3Heap.set(lineHeapNeg), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			pipeline.addVert(vec3Heap.set(lineHeapPos), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		}
		if(index<lineSpacing.length-1){
			drawDuoLine(pipeline, lineSpacing, index+1, lhi);
		}
	}


}
