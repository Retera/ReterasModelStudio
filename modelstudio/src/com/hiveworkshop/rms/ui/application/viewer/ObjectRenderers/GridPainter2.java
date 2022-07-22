package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class GridPainter2 {
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
	float[] lineHeapDir = new float[3];
	float[] lineHeapNeg = new float[3];

	Vec2 vec2Heap = new Vec2();
	Vec3 vec3Heap = new Vec3();
	Vec4 colorHeap = new Vec4(1f, 1f, 1f, .3f);

	Ray rayHeap = new Ray();
	Plane planeHeapXY = new Plane();

	Vec3 gridStart = new Vec3();
	Vec3 gridEnd = new Vec3();

//	Vec3[] lineStarts  = new Vec3[]{new Vec3(-100, -100, 0), new Vec3(-100, -100, 0)};
//	Vec3[] lineEnds    = new Vec3[]{new Vec3( 100, -100, 0), new Vec3(-100,  100, 0)};
//	Vec3[] lineSpreads = new Vec3[]{new Vec3( 0, 1, 0), new Vec3( 1, 0, 0)};
//	Vec4[] lineColors = new Vec4[]{new Vec4(1f, .5f, .5f, 1), new Vec4(.5f, 1f, .5f, 1)};

	Vec3[] lineStarts  = new Vec3[]{new Vec3(-100, -100, 0), new Vec3(-100, -100, 0)};
	Vec3[] lineEnds    = new Vec3[]{new Vec3( 100, -100, 0), new Vec3(-100,  100, 0)};
	Vec3[] lineSpreads = new Vec3[]{new Vec3( 0, 1, 0), new Vec3( 1, 0, 0)};
	Vec4[] lineColors = new Vec4[]{new Vec4(1f, .5f, .5f, 1), new Vec4(.5f, 1f, .5f, 1)};


	HalfGrid[] halfGrids = new HalfGrid[]{

			new HalfGrid(new Vec3(-500, -500, 0), new Vec3( 500, -500, 0),new Vec3( 0, 10, 0), new Vec4(.5f, .5f, .5f, 1)), // X_XY
			new HalfGrid(new Vec3(-500, -500, 0), new Vec3(-500,  500, 0),new Vec3( 10, 0, 0), new Vec4(.5f, .5f, .5f, 1)), // Y_XY
//			new HalfGrid(new Vec3(-100, -100, 0), new Vec3( 100, -100, 0),new Vec3( 0, 1, 0), new Vec4(1f, .5f, .5f, 1)), // X_XY
//			new HalfGrid(new Vec3(-100, -100, 0), new Vec3(-100,  100, 0),new Vec3( 1, 0, 0), new Vec4(.5f, 1f, .5f, 1)), // Y_XY

//			new HalfGrid(new Vec3(-100, 0, -100), new Vec3( 100, 0, -100),new Vec3( 0, 0, 1), new Vec4(1f, .5f, .5f, 1)), // X_XZ
//			new HalfGrid(new Vec3(-100, 0, -100), new Vec3(-100, 0,  100),new Vec3( 1, 0, 0), new Vec4(.5f, .5f, 1f, 1)), // Z_XZ
//
//			new HalfGrid(new Vec3(0, -100, -100), new Vec3(0,  100, -100),new Vec3( 0, 0, 1), new Vec4(.5f, 1f, .5f, 1)), // Y_YZ
//			new HalfGrid(new Vec3(0, -100, -100), new Vec3(0, -100,  100),new Vec3( 0, 1, 0), new Vec4(.5f, .5f, 1f, 1)), // Z_YZ
	};

//	Vec3[] lineStarts  = new Vec3[]{new Vec3(-100, 0, 0), new Vec3(-100, 0, 0), new Vec3(0, -100, 0), new Vec3(0, -100, 0)};
//	Vec3[] lineEnds    = new Vec3[]{new Vec3( 100, 0, 0), new Vec3( 100, 0, 0), new Vec3(0,  100, 0), new Vec3(0,  100, 0)};
//	Vec3[] lineSpreads = new Vec3[]{new Vec3( 0, 1, 0), new Vec3( 0, -1, 0), new Vec3( 1, 0, 0), new Vec3( -1, 0, 0)};
//	Vec4[] lineColors = new Vec4[]{new Vec4(1f, .5f, .5f, 1), new Vec4(1f, .5f, .5f, 1), new Vec4(.5f, 1f, .5f, 1), new Vec4(.5f, 1f, .5f, 1)};

//		colorHeap.set(1f, .5f, .5f, 1);
//
//		startHeap.set(-100, -100, 0);
//		endHeap.set(100, -100, 0);
//		pipeline.addVert(startHeap, Vec3.Y_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//		pipeline.addVert(endHeap, Vec3.Y_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
////		pipeline.addVert(startHeap, vec3Heap, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
////		pipeline.addVert(endHeap, vec3Heap, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//
////		addLinePoints(pipeline, startHeap, endHeap, gridDir1, gridDist, spread1);
//
//
//		colorHeap.set(.5f, 1f, .5f, 1);
//		vec3Heap.set(gridDir1).scale(spread1);
//		endHeap.set(startHeap).add(vec3Heap);
//		vec3Heap.set(gridDir2).scale(spread2);
//
//
//		startHeap.set(-100, -100, 0);
//		endHeap.set(-100, 100, 0);
//		pipeline.addVert(startHeap, Vec3.X_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//		pipeline.addVert(endHeap, Vec3.X_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

	public GridPainter2(CameraManager cameraHandler) {
		this.cameraHandler = cameraHandler;
	}
	public GridPainter2() {
	}

	long time = 0;
	public void fillGridBuffer(ShaderPipeline pipeline) {
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		pipeline.glEnableIfNeeded(GL11.GL_BLEND);
		pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
		pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
		pipeline.glDisableIfNeeded(GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		pipeline.prepare();

//		float cameraPxSize1 = (float) (cameraHandler.sizeAdj()); // 1px
		float cameraPxSize1 = (float) (1); // 1px
		int gridLog = (int)Math.log10(cameraPxSize1*120);
		double v = (int)Math.log10(cameraPxSize1);
		float lineScaleMul = (float) Math.pow(10, v);

//		findGridPoints();
		gridStart.set(-200, -200, 0);
		gridEnd.set(200, 200, 0);


		if(time < System.currentTimeMillis()){
			time = System.currentTimeMillis() + 5000;
		}

		colorHeap.set(1f, .5f, .5f, 1);

		if(gridStart.isValid() && gridEnd.isValid()){
//			for(int i = 0; i< lineStarts.length; i++){
//				makeLineSpread(pipeline, lineStarts[i], lineEnds[i], lineSpreads[i], lineColors[i]);
//			}
//			makeGridPoints(pipeline, gridStart, gridEnd, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//			makeGridPoints(pipeline, gridStart, gridEnd, Vec3.X_AXIS, Vec3.Y_AXIS, 1);
			for(HalfGrid hg : halfGrids){
				makeLineSpread(pipeline, hg.lineStart, hg.lineEnd, hg.lineSpread, hg.lineColor);
			}

		}
	}
	public void fillGridBuffer1(ShaderPipeline pipeline) {
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		pipeline.glEnableIfNeeded(GL11.GL_BLEND);
		pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
		pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
		pipeline.glDisableIfNeeded(GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		pipeline.prepare();

//		float cameraPxSize1 = (float) (cameraHandler.sizeAdj()); // 1px
		float cameraPxSize1 = (float) (1); // 1px
		int gridLog = (int)Math.log10(cameraPxSize1*120);
		double v = (int)Math.log10(cameraPxSize1);
		float lineScaleMul = (float) Math.pow(10, v);

//		findGridPoints();
		gridStart.set(-200, -200, 0);
		gridEnd.set(200, 200, 0);


//		gridStart
//				.minimize(pointTL)
//				.minimize(pointTR)
//				.minimize(pointBL)
//				.minimize(pointBR);
//		gridEnd
//				.maximize(pointTL)
//				.maximize(pointTR)
//				.maximize(pointBL)
//				.maximize(pointBR);

		if(time < System.currentTimeMillis()){
//			System.out.println("grid");
//			System.out.println("pointTL: " + pointTL);
//			System.out.println("pointTR: " + pointTR);
//			System.out.println("pointBL: " + pointBL);
//			System.out.println("pointBR: " + pointBR);
			time = System.currentTimeMillis() + 5000;
		}
//		colorHeap.set(1f, 1f, 1f, .3f);

		colorHeap.set(1f, .5f, .5f, 1);

//		if(pointTL.isValid()){
//			if(pointTL.distance(pointBR) > pointTR.distance(pointBL)){
//				makeGridPoints(pipeline, pointTL, pointBR, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//			} else {
//				makeGridPoints(pipeline, pointTR, pointBL, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//			}
//		}
		if(gridStart.isValid() && gridEnd.isValid()){
//			makeGridPoints(pipeline, gridStart, gridEnd, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
			makeGridPoints(pipeline, gridStart, gridEnd, Vec3.X_AXIS, Vec3.Y_AXIS, 1);
		}


//		makeGridPoints(pipeline, new Vec3(1000, 1000, 0), new Vec3(-1000, -1000, 0), Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//		makeGridPoints(pipeline, new Vec3(100*lineScaleMul, 100*lineScaleMul, 0), new Vec3(-100*lineScaleMul, -100*lineScaleMul, 0), Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);

	}

	private void findGridPoints() {
		planeHeapXY.set(Vec3.Z_AXIS, 0);
		gridStart.set(Vec3.ZERO);
		gridEnd.set(Vec3.ZERO);


		rayHeap.set(cameraHandler.getRayFromScreenSpace(-1, 1));
		float intersectTL = planeHeapXY.getIntersect(rayHeap);
		vec3Heap.set(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectTL);
		if(vec3Heap.isValid()){
			gridStart.minimize(vec3Heap);
			gridEnd.maximize(vec3Heap);
		}

		rayHeap.set(cameraHandler.getRayFromScreenSpace(1,1));
		float intersectTR = planeHeapXY.getIntersect(rayHeap);
		vec3Heap.set(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectTR);
		if(vec3Heap.isValid()){
			gridStart.minimize(vec3Heap);
			gridEnd.maximize(vec3Heap);
		}

		rayHeap.set(cameraHandler.getRayFromScreenSpace(-1,-1));
		float intersectBL = planeHeapXY.getIntersect(rayHeap);
		vec3Heap.set(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectBL);
		if(vec3Heap.isValid()){
			gridStart.minimize(vec3Heap);
			gridEnd.maximize(vec3Heap);
		}

		rayHeap.set(cameraHandler.getRayFromScreenSpace(1, -1));
		float intersectBR = planeHeapXY.getIntersect(rayHeap);
		vec3Heap.set(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectBR);
		if(vec3Heap.isValid()){
			gridStart.minimize(vec3Heap);
			gridEnd.maximize(vec3Heap);
		}
	}

	public void fillGridBuffer(ShaderPipeline pipeline, CameraManager cameraManager) {
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		pipeline.glEnableIfNeeded(GL11.GL_BLEND);
		pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
		pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
		pipeline.glDisableIfNeeded(GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		pipeline.prepare();

		float cameraPxSize1 = (float) (cameraHandler.sizeAdj()); // 1px
		int gridLog = (int)Math.log10(cameraPxSize1*120);
		double v = (int)Math.log10(cameraPxSize1);
		float lineScaleMul = (float) Math.pow(10, v);

		findGridPoints();


//		gridStart
//				.minimize(pointTL)
//				.minimize(pointTR)
//				.minimize(pointBL)
//				.minimize(pointBR);
//		gridEnd
//				.maximize(pointTL)
//				.maximize(pointTR)
//				.maximize(pointBL)
//				.maximize(pointBR);

		if(time < System.currentTimeMillis()){
//			System.out.println("grid");
//			System.out.println("pointTL: " + pointTL);
//			System.out.println("pointTR: " + pointTR);
//			System.out.println("pointBL: " + pointBL);
//			System.out.println("pointBR: " + pointBR);
			time = System.currentTimeMillis() + 5000;
		}
//		colorHeap.set(1f, 1f, 1f, .3f);

		colorHeap.set(1f, .5f, .5f, 1);

//		if(pointTL.isValid()){
//			if(pointTL.distance(pointBR) > pointTR.distance(pointBL)){
//				makeGridPoints(pipeline, pointTL, pointBR, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//			} else {
//				makeGridPoints(pipeline, pointTR, pointBL, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//			}
//		}
		if(gridStart.isValid() && gridEnd.isValid()){
			makeGridPoints(pipeline, gridStart, gridEnd, Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
		}


//		makeGridPoints(pipeline, new Vec3(1000, 1000, 0), new Vec3(-1000, -1000, 0), Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);
//		makeGridPoints(pipeline, new Vec3(100*lineScaleMul, 100*lineScaleMul, 0), new Vec3(-100*lineScaleMul, -100*lineScaleMul, 0), Vec3.X_AXIS, Vec3.Y_AXIS, gridLog);

	}


	private void drawDuoLine(ShaderPipeline pipeline, float[] lineSpacing, int index, int lhi){
		float lS = lineSpacing[index];//1
		for (lineHeapNeg[lhi] = lS, lineHeapPos[lhi] = lS; lineHeapNeg[lhi] < lineLength; lineHeapNeg[lhi] += lS, lineHeapPos[lhi] += lS) {
			pipeline.addVert(vec3Heap.set(lineHeapNeg), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			pipeline.addVert(vec3Heap.set(lineHeapPos), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			pipeline.addVert(vec3Heap.set(lineHeapDir), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		}
		for (lineHeapNeg[lhi] = -lS, lineHeapPos[lhi] = -lS; lineHeapNeg[lhi] > -lineLength; lineHeapNeg[lhi] -= lS, lineHeapPos[lhi] -= lS) {
			pipeline.addVert(vec3Heap.set(lineHeapNeg), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			pipeline.addVert(vec3Heap.set(lineHeapPos), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			pipeline.addVert(vec3Heap.set(lineHeapDir), Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		}
//		if(index<lineSpacing.length-1){
//			drawDuoLine(pipeline, lineSpacing, index+1, lhi);
//		}
	}



	Vec3 startHeap = new Vec3();
	Vec3 endHeap = new Vec3();

	private void addLinePoints(ShaderPipeline pipeline, Vec3 lineStart, Vec3 lineEnd, Vec3 spreadDir, float spread, float totSpread){
		int i = 0;
		for(float currSpread = 0; currSpread <= totSpread && i<200; currSpread += spread){
			vec3Heap.set(lineStart).addScaled(spreadDir, currSpread);
			pipeline.addVert(vec3Heap, Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			vec3Heap.set(lineEnd).addScaled(spreadDir, currSpread);
			pipeline.addVert(vec3Heap, Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//			vec3Heap.set(spreadDir).scale(spread);
//			pipeline.addVert(vec3Heap, Vec3.Z_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
			i++;
		}
	}

	private void makeGridPoints(ShaderPipeline pipeline, Vec3 gridStart, Vec3 gridEnd, Vec3 gridDir1, Vec3 gridDir2, int gridLog){
		float gridDist = (float) Math.pow(10, gridLog);
//		float gridDist = 100f;


//		startHeap.set(gridStart);
//		endHeap.set(gridEnd);
		startHeap.set(gridStart).minimize(gridEnd);
		endHeap.set(gridStart).maximize(gridEnd);
		vec3Heap.set(gridDir1).add(gridDir2);
		roundVec3(endHeap, gridDist, vec3Heap);
		roundVec3(startHeap, gridDist, vec3Heap.negate());


		vec3Heap.set(endHeap).sub(startHeap);   // diagonal
		float spread1 = vec3Heap.dot(gridDir1); // spread dir1 / line length dir2
		float spread2 = vec3Heap.dot(gridDir2); // spread dir2 / line length dir1


		// Line end
		vec3Heap.set(gridDir2).scale(spread2);
		endHeap.set(startHeap).add(vec3Heap);
		vec3Heap.set(gridDir1).scale(spread1);


		colorHeap.set(1f, .5f, .5f, 1);

		startHeap.set(-100, -100, 0);
		endHeap.set(100, -100, 0);
		pipeline.addVert(startHeap, Vec3.Y_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		pipeline.addVert(endHeap, Vec3.Y_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//		pipeline.addVert(startHeap, vec3Heap, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//		pipeline.addVert(endHeap, vec3Heap, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

//		addLinePoints(pipeline, startHeap, endHeap, gridDir1, gridDist, spread1);


		colorHeap.set(.5f, 1f, .5f, 1);
		vec3Heap.set(gridDir1).scale(spread1);
		endHeap.set(startHeap).add(vec3Heap);
		vec3Heap.set(gridDir2).scale(spread2);


		startHeap.set(-100, -100, 0);
		endHeap.set(-100, 100, 0);
		pipeline.addVert(startHeap, Vec3.X_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
		pipeline.addVert(endHeap, Vec3.X_AXIS, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

//		pipeline.addVert(startHeap, vec3Heap, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);
//		pipeline.addVert(endHeap, vec3Heap, colorHeap, vec2Heap, colorHeap, Vec3.ZERO);

//		addLinePoints(pipeline, startHeap, endHeap, gridDir2, gridDist, spread2);

	}


	private void makeLineSpread(ShaderPipeline pipeline, Vec3 lineStart, Vec3 lineEnd, Vec3 spreadDir, Vec4 color){
		GridBufferSubInstance instance = new GridBufferSubInstance(null, null);
		pipeline.startInstance(instance);
		pipeline.addVert(lineStart, spreadDir, color, vec2Heap, color, Vec3.ZERO);
		pipeline.addVert(lineEnd, spreadDir, color, vec2Heap, color, Vec3.ZERO);
		pipeline.endInstance();

	}


	private void roundVec3(Vec3 vec3, float prec, Vec3 adj){
		vec3.scale(1.0f / prec);
		vec3.x = Math.round(vec3.x) + adj.x;
		vec3.y = Math.round(vec3.y) + adj.y;
		vec3.z = Math.round(vec3.z) + adj.z;

		vec3.scale(prec);
	}


	private static class HalfGrid {
		Vec3 lineStart  = new Vec3();
		Vec3 lineEnd    = new Vec3();
		Vec3 lineSpread = new Vec3();
		Vec3 lineNorm = new Vec3();
		Vec4 lineColor  = new Vec4();

		HalfGrid(Vec3 lineStart,
		         Vec3 lineEnd,
		         Vec3 lineSpread,
		         Vec4 lineColor){
			this.lineStart = lineStart;
			this.lineEnd = lineEnd;
			this.lineSpread = lineSpread;
			this.lineColor = lineColor;
		}
		HalfGrid(Vec3 lineStart,
		         Vec3 lineEnd,
		         Vec3 lineSpread,
		         Vec3 lineNorm,
		         Vec4 lineColor){
			this.lineStart = lineStart;
			this.lineEnd = lineEnd;
			this.lineSpread = lineSpread;
			this.lineNorm = lineNorm;
			this.lineColor = lineColor;
		}
	}

}
