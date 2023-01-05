package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class GridPainter {
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	CameraManager cameraHandler;
	float lineLength = 200;
	float lineSpacing = 100;
	float numberOfLines = 5;
	float subDivs = 10;
	float highlightEveryN = 5;
	float[] lineHeapPos = new float[3];
	float[] lineHeapNeg = new float[3];

	public GridPainter(CameraManager cameraHandler) {
		this.cameraHandler = cameraHandler;
	}

	public void paintGrid() {
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		glBegin(GL11.GL_LINES);
		GL11.glNormal3f(0, 0, 0);

		float cameraPxSize1 = (float) (cameraHandler.sizeAdj() * 2f); // 1px
		double v = (int)Math.log10(cameraPxSize1);
		float lineScaleMul = (float) Math.pow(10, v);

		float[] lineSpacingArr = new float[] {10*lineScaleMul, 50*lineScaleMul, 100*lineScaleMul};
		glColor4f(1f, 1f, 1f, .3f);

		//Grid floor X
		fillLineHeap(X, Y, Z);
		drawUggDuoArr(lineSpacingArr, 0, Y);

		float upAngle = cameraHandler.getYAngle() % 180;
		float spinAngle = cameraHandler.getZAngle() % 180;
		boolean isSide = upAngle == 0 && spinAngle == 90;
		boolean isFront = upAngle == 0 && spinAngle == 0;
		if (cameraHandler.isOrtho() && isSide) {
			//Side Horizontal Lines
			zeroLineHeap(Y);
			drawUggDuoArr(lineSpacingArr, 0, Z);
		}

		//Grid floor Y
		fillLineHeap(Y, X, Z);
		drawUggDuoArr(lineSpacingArr, 0, X);

		if(cameraHandler.isOrtho() && isFront){
			//Front Horizontal Lines
			zeroLineHeap(X);
			drawUggDuoArr(lineSpacingArr, 0, Z);
		}

		if(cameraHandler.isOrtho() && isSide){
			//Side Vertical Lines
			fillLineHeap(Z, X, Y);
			drawUggDuoArr(lineSpacingArr, 0, X);
		}

		if(cameraHandler.isOrtho() && isFront){
			//Front Vertical Lines
			fillLineHeap(Z, X, Y);
//			zeroLineHeap(X);
			drawUggDuoArr(lineSpacingArr, 0, Y);
		}

		glColor4f(1f, .5f, .5f, .7f);
		GL11.glVertex3f(-lineLength, 0, 0);
		GL11.glVertex3f(lineLength, 0, 0);

		glColor4f(.5f, 1f, .5f, .7f);
		GL11.glVertex3f(0, -lineLength, 0);
		GL11.glVertex3f(0, lineLength, 0);

		glColor4f(.5f, .5f, 1f, .7f);
		GL11.glVertex3f(0, 0, -lineLength);
		GL11.glVertex3f(0, 0, lineLength);
		glEnd();
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

	private void drawUggDuoArr(float[] lineSpacing, int index, int lhi){
		float lS = lineSpacing[index];//1
		for (lineHeapNeg[lhi] = lS, lineHeapPos[lhi] = lS; lineHeapNeg[lhi] < lineLength; lineHeapNeg[lhi] += lS, lineHeapPos[lhi] += lS) {
			GL11.glVertex3f(lineHeapNeg[0],  lineHeapNeg[1], lineHeapNeg[2]);
			GL11.glVertex3f(lineHeapPos[0],  lineHeapPos[1], lineHeapPos[2]);
		}
		for (lineHeapNeg[lhi] = -lS, lineHeapPos[lhi] = -lS; lineHeapNeg[lhi] > -lineLength; lineHeapNeg[lhi] -= lS, lineHeapPos[lhi] -= lS) {
			GL11.glVertex3f(lineHeapNeg[0], lineHeapNeg[1], lineHeapNeg[2]);
			GL11.glVertex3f(lineHeapPos[0], lineHeapPos[1], lineHeapPos[2]);
		}
		if(index<lineSpacing.length-1){
			drawUggDuoArr(lineSpacing, index+1, lhi);
		}
	}
	private void drawUgg2(int[] lineSpacing, int index){
		for (float currDist = 0; currDist < lineLength; currDist += lineSpacing[index]) {
			GL11.glVertex3f(-lineLength, currDist, 0);
			GL11.glVertex3f(lineLength, currDist, 0);
			GL11.glVertex3f(-lineLength, -currDist, 0);
			GL11.glVertex3f(lineLength, -currDist, 0);
		}
		if(index<lineSpacing.length-1){
			drawUgg2(lineSpacing, index+1);
		}
	}
	private void drawUgg1(int lineSpacing, int recursions){
		for (float y = -lineLength; y < lineLength; y += lineSpacing) {
			GL11.glVertex3f(-lineLength, y, 0);
			GL11.glVertex3f(lineLength, y, 0);
		}
		if(recursions>0){
			drawUgg1(lineSpacing*10, recursions-1);
		}
	}

}
