package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.util.PeriodicOut;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class GridShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + COLOR ;
	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

	GridBufferSubInstance[] xyInstance = new GridBufferSubInstance[2];
	GridBufferSubInstance[] xzInstance = new GridBufferSubInstance[2];
	GridBufferSubInstance[] yzInstance = new GridBufferSubInstance[2];
//	Vec3 xyProjVec = new Vec3( 1, 1, 0);
//	Vec3 xzProjVec = new Vec3( 1, 0, 1);
//	Vec3 yzProjVec = new Vec3( 0, 1, 1);
	Vec4 xyMoveVec = new Vec4( 1, 1, 0, 0);
	Vec4 xzMoveVec = new Vec4( 1, 0, 1, 0);
	Vec4 yzMoveVec = new Vec4( 0, 1, 1, 0);
	Vec4 gridScale = new Vec4( 1, 1, 1, 1);

//	GridBufferSubInstance[] xyInstance;
//	GridBufferSubInstance[] xzInstance;
//	GridBufferSubInstance[] yzInstance;

	public GridShaderPipeline() {
		this(OtherUtils.loadShader("Grid.vert"), OtherUtils.loadShader("Grid.frag"), OtherUtils.loadShader("Grid.glsl"));
//		currentMatrix.setIdentity();
////		geometryShader = OtherUtils.loadShader("Grid.glsl");
//		vertexShader = OtherUtils.loadShader("Grid.vert");
//		fragmentShader = OtherUtils.loadShader("Grid.frag");
//		load();
//		setupUniforms();
	}

	public GridShaderPipeline(String vertexShader, String fragmentShader, String geometryShader) {
		currentMatrix.setIdentity();
//		this.geometryShader = geometryShader;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		load();
		setupUniforms();

//		xyInstance = new GridBufferSubInstance[] {getLineSpread(new Vec3(-500, -500, 0), new Vec3(500, -500, 0), new Vec3(0, 10, 0), new Vec4(.5f, .5f, .5f, 1)),
//				getLineSpread(new Vec3(-500, -500, 0), new Vec3(-500, 500, 0), new Vec3(10, 0, 0), new Vec4(.5f, .5f, .5f, 1))};
		xyInstance[0] = getLineSpread(new Vec3(-1000, -1000, 0), new Vec3( 1000,-1000,  0), new Vec3( 0, 10,  0), new Vec4(.5f, .5f, .5f, 1));
		xyInstance[1] = getLineSpread(new Vec3(-1000, -1000, 0), new Vec3(-1000, 1000,  0), new Vec3(10,  0,  0), new Vec4(.5f, .5f, .5f, 1));
		xzInstance[0] = getLineSpread(new Vec3(-1000, 0, -1000), new Vec3( 1000, 0, -1000), new Vec3( 0,  0, 10), new Vec4(.5f, .5f, .5f, 1));
		xzInstance[1] = getLineSpread(new Vec3(-1000, 0, -1000), new Vec3(-1000, 0,  1000), new Vec3(10,  0,  0), new Vec4(.5f, .5f, .5f, 1));
		yzInstance[0] = getLineSpread(new Vec3(0, -1000, -1000), new Vec3(0,  1000, -1000), new Vec3( 0,  0, 10), new Vec4(.5f, .5f, .5f, 1));
		yzInstance[1] = getLineSpread(new Vec3(0, -1000, -1000), new Vec3(0, -1000,  1000), new Vec3( 0, 10,  0), new Vec4(.5f, .5f, .5f, 1));
//		xyInstance[0] = getLineSpread(new Vec3(-500, -500, 0), new Vec3(500, -500,  0), new Vec3( 0, 10,  0), new Vec4(.5f, .5f, .5f, 1));
//		xyInstance[1] = getLineSpread(new Vec3(-500, -500, 0), new Vec3(-500, 500,  0), new Vec3(10,  0,  0), new Vec4(.5f, .5f, .5f, 1));
//		xzInstance[0] = getLineSpread(new Vec3(-500, 0, -500), new Vec3( 500, 0, -500), new Vec3( 0,  0, 10), new Vec4( 1f, .5f, .5f, 1));
//		xzInstance[1] = getLineSpread(new Vec3(-500, 0, -500), new Vec3(-500, 0,  500), new Vec3( 10, 0,  0), new Vec4(.5f, .5f, 1f,  1));
//		yzInstance[0] = getLineSpread(new Vec3(0, -500, -500), new Vec3(0,  500, -500), new Vec3( 0,  0, 10), new Vec4(.5f,  1f, .5f, 1));
//		yzInstance[1] = getLineSpread(new Vec3(0, -500, -500), new Vec3(0, -500,  500), new Vec3( 0, 10,  0), new Vec4(.5f, .5f, 1f,  1));
	}


	Vec2 tempV2 = new Vec2();
	private GridBufferSubInstance getLineSpread(Vec3 lineStart, Vec3 lineEnd, Vec3 spreadDir, Vec4 color){
		GridBufferSubInstance instance = new GridBufferSubInstance(null, null);
		startInstance(instance);
		addVert(lineStart, spreadDir, color, tempV2, color, Vec3.ZERO);
		addVert(lineEnd, spreadDir, color, tempV2, color, Vec3.ZERO);
		endInstance();
		return instance;
	}
	protected void setupUniforms(){
//		createUniform("u_viewPos");
		createUniform("u_projection");
		createUniform("u_view");
		createUniform("u_moveVec");
		createUniform("u_gridScale");
//		createUniform("u_dist");
//		createUniform("u_color[0]");
//		createUniform("u_color[1]");
//		createUniform("u_color[2]");
//		createUniform("u_color[3]");
	}

	public void doRender() {
//		System.out.println("glEnd");
		//https://github.com/flowtsohg/mdx-m3-viewer/tree/827d1bda1731934fb8e1a5cf68d39786f9cb857d/src/viewer/handlers/w3x/shaders
		GL30.glBindVertexArray(glVertexArrayId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);


		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(NORMAL, STRIDE);
		enableAttribArray(COLOR, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;

		fillMatrixBuffer(pipelineMatrixBuffer, projectionMat);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);

		setUpAndDraw(xyInstance[0]);
		setUpAndDraw(xyInstance[1]);

//		if(!instances.isEmpty()){
//			for (BufferSubInstance instance : instances){
//				setUpAndDraw(instance);
//			}
//		} else {
//
//
////		GL11.glDrawArrays(glBeginType, 0, vertexCount);
////		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
////		GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCount);
//			GL31.glDrawArraysInstanced(GL11.GL_LINES, 0, 2, 100);
//			GL31.glDrawArraysInstanced(GL11.GL_LINES, 2, 2, 100);
//		}
		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
	}
	public void doRender(CameraManager cameraManager) {
//		System.out.println("glEnd");
		//https://github.com/flowtsohg/mdx-m3-viewer/tree/827d1bda1731934fb8e1a5cf68d39786f9cb857d/src/viewer/handlers/w3x/shaders
		GL30.glBindVertexArray(glVertexArrayId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);


		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(NORMAL, STRIDE);
		enableAttribArray(COLOR, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;


		getGridScale(cameraManager.sizeAdj());
		setupMoveVectors(cameraManager.getTarget(), cameraManager.sizeAdj());
//		setupMoveVectors(cameraManager.getTarget());

		fillMatrixBuffer(pipelineMatrixBuffer, projectionMat);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);
		glUniform("u_gridScale", gridScale);

//		if (!cameraManager.isOrtho() || 0.000001 < Math.abs(cameraManager.getCamBackward().dot(Vec3.Z_AXIS))) {
//		}
		glUniform("u_moveVec", xyMoveVec);
		setUpAndDraw(xyInstance[0]);
		setUpAndDraw(xyInstance[1]);
		if (cameraManager.isOrtho() && 0.999999 < Math.abs(cameraManager.getCamBackward().dot(Vec3.Y_AXIS))) {
			glUniform("u_moveVec", xzMoveVec);
//			System.out.println("draw XZ");
			setUpAndDraw(xzInstance[0]);
			setUpAndDraw(xzInstance[1]);
		}
		if (cameraManager.isOrtho() && 0.999999 < Math.abs(cameraManager.getCamBackward().dot(Vec3.X_AXIS))) {
			glUniform("u_moveVec", yzMoveVec);
//			System.out.println("draw YZ");
			setUpAndDraw(yzInstance[0]);
			setUpAndDraw(yzInstance[1]);
		}


//		if (!cameraManager.isOrtho() || 0.01 < Math.abs(cameraManager.getCamBackward().dot(Vec3.Z_AXIS))) {
//			setUpAndDraw(xyInstance[0]);
//			setUpAndDraw(xyInstance[1]);
//		}
//		setUpAndDraw(xyInstance[0]);
//		setUpAndDraw(xyInstance[1]);
//
//		setUpAndDraw(xzInstance[0]);
//		setUpAndDraw(xzInstance[1]);
//
//		setUpAndDraw(yzInstance[0]);
//		setUpAndDraw(yzInstance[1]);




//		if(!instances.isEmpty()){
//			for (BufferSubInstance instance : instances){
//				setUpAndDraw(instance);
//			}
//		} else {
//
//
////		GL11.glDrawArrays(glBeginType, 0, vertexCount);
////		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
////		GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCount);
//			GL31.glDrawArraysInstanced(GL11.GL_LINES, 0, 2, 100);
//			GL31.glDrawArraysInstanced(GL11.GL_LINES, 2, 2, 100);
//		}
		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
	}
	PeriodicOut ugg = new PeriodicOut(1000);
	float lineScaleMul = 1;
	float v = 1;
	private Vec4 getGridScale(double siseAdj) {
//		float cameraPxSize1 = (float) (1); // 1px
		float cameraPxSize1 = (float) (18.0/siseAdj); // 1px in world size
//		int gridLog = (int)Math.log10(cameraPxSize1*120);
//		float v = (int)Math.log10(cameraPxSize1);
//		float lineScaleMul = (float) (Math.pow(10, v-1));
		v = (int)Math.log10(cameraPxSize1);
		lineScaleMul = (float) (Math.pow(10, v-1));
		float lineScale2 = 1.0f / lineScaleMul;
//		System.out.println("PX_size: " + siseAdj + ", log10 =>  " + v + ", 10pow =>  " + lineScaleMul + ", 1div =>  " + lineScale2);
//		return (float) (Math.pow(10, v));
//		return (float) (Math.pow(10, v));
//		ugg.print(", ", "siseAdj: " + siseAdj, "cameraPxSize1: " + cameraPxSize1, "(int)Math.log10: " + v, "lineScaleMul: " + lineScaleMul, "lineScale2: " + lineScale2);
		return gridScale.set(lineScale2, lineScale2, lineScale2, 1);
	}

	private float getGridScale(CameraManager cameraManager) {
//		float cameraPxSize1 = (float) (1); // 1px
		float cameraPxSize1 = (float) (cameraManager.sizeAdj()); // 1px


		float approxGridSize = cameraPxSize1*10f;

		float currGridSize = 10f/approxGridSize * 10f;


//		int gridLog = (int)Math.log10(cameraPxSize1*120);
		double v = (int)Math.log10(cameraPxSize1);
		return (float) (1f/Math.pow(10, v));
	}
	private void setupMoveVectors(Vec3 target, double siseAdj){
//		float clampScale = 100*lineScaleMul;

		float cameraPxSize1 = (float) (18/siseAdj); // 1px in world size
		float v = (int)Math.log10(cameraPxSize1);
		float clampScale = (float)(100f / Math.pow(10, v-1));
//		ugg.print(", ", "target: " + target, "siseAdj: " + siseAdj, "cameraPxSize1: " + cameraPxSize1, "(int)Math.log10: " + v, "lineScale2: " + clampScale);

		float clampedX = (int)(target.x/clampScale) * clampScale;
		float clampedY = (int)(target.y/clampScale) * clampScale;
		float clampedZ = (int)(target.z/clampScale) * clampScale;
		xyMoveVec.set(clampedX, clampedY, 0, 0);
		xzMoveVec.set(clampedX, 0, clampedZ, 0);
		yzMoveVec.set(0, clampedY, clampedZ, 0);
	}
	private void setupMoveVectors(Vec3 target){
//		float clampScale = 100*lineScaleMul;
		float clampScale = (float) Math.pow(10, v+2);

		float clampedX = (int)(target.x/clampScale) * clampScale;
		float clampedY = (int)(target.y/clampScale) * clampScale;
		float clampedZ = (int)(target.z/clampScale) * clampScale;
		xyMoveVec.set(clampedX, clampedY, 0, 0);
		xzMoveVec.set(clampedX, 0, clampedZ, 0);
		yzMoveVec.set(0, clampedY, clampedZ, 0);
	}

	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		GL31.glDrawArraysInstanced(GL11.GL_LINES, instance.getOffset(), instance.getVertCount(), 200);
	}
	public void glEnableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			textureUsed = 1;
//			GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		}
		else if ((glEnum == GL11.GL_ALPHA_TEST) && (textureUnit == 0)) {
			alphaTest = 1;
		}
		else if (glEnum == GL11.GL_LIGHTING) {
			lightingEnabled = 1;
		}
	}

	public void glDisableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			textureUsed = 0;
			GL13.glActiveTexture(0);
		}
		else if ((glEnum == GL11.GL_ALPHA_TEST) && (textureUnit == 0)) {
			alphaTest = 0;
		}
		else if (glEnum == GL11.GL_LIGHTING) {
			lightingEnabled = 0;
		}
	}

	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres){
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(norm, 0);

		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, col);

		vertexCount++;

	}
//	private static final int STRIDE = POSITION + COLOR ;
//	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;
//
//
//	public GridShaderPipeline() {
//		currentMatrix.setIdentity();
////		geometryShader = OtherUtils.loadShader("Grid.glsl");
//		vertexShader = OtherUtils.loadShader("Grid.vert");
//		fragmentShader = OtherUtils.loadShader("Grid.frag");
//		load();
//		setupUniforms();
//	}
//
//
//	protected void setupUniforms(){
//		createUniform("u_viewPos");
//		createUniform("u_projection");
//		createUniform("u_view");
//	}
//
//	public void doRender() {
////		System.out.println("glEnd");
//		//https://github.com/flowtsohg/mdx-m3-viewer/tree/827d1bda1731934fb8e1a5cf68d39786f9cb857d/src/viewer/handlers/w3x/shaders
//		GL30.glBindVertexArray(glVertexArrayId);
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
//
//		pipelineVertexBuffer.position(vertexCount * STRIDE);
//		pipelineVertexBuffer.flip();
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);
//
//		enableAttribArray(POSITION, STRIDE);
//		enableAttribArray(COLOR, STRIDE);
//
//		GL11.glDisable(GL11.GL_CULL_FACE);
//		GL20.glUseProgram(shaderProgram);
//		textureUsed = 0;
//		alphaTest = 0;
//		lightingEnabled = 0;
//
//
//		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);
//		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
//		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
//		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
//		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);
//
//
////		GL11.glDrawArrays(glBeginType, 0, vertexCount);
////		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
//		GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCount);
//		pipelineVertexBuffer.clear();
//		GL20.glUseProgram(0);
//	}
//
//	public void glEnableIfNeeded(int glEnum) {
//		if (glEnum == GL11.GL_TEXTURE_2D) {
//			textureUsed = 1;
////			GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
//		}
//		else if ((glEnum == GL11.GL_ALPHA_TEST) && (textureUnit == 0)) {
//			alphaTest = 1;
//		}
//		else if (glEnum == GL11.GL_LIGHTING) {
//			lightingEnabled = 1;
//		}
//	}
//
//	public void glDisableIfNeeded(int glEnum) {
//		if (glEnum == GL11.GL_TEXTURE_2D) {
//			textureUsed = 0;
//			GL13.glActiveTexture(0);
//		}
//		else if ((glEnum == GL11.GL_ALPHA_TEST) && (textureUnit == 0)) {
//			alphaTest = 0;
//		}
//		else if (glEnum == GL11.GL_LIGHTING) {
//			lightingEnabled = 0;
//		}
//	}
//
//	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres){
//		int baseOffset = vertexCount * STRIDE;
//		currBufferOffset = 0;
//		ensureCapacity(baseOffset + STRIDE);
//		position.set(pos, 1);
//		normal.set(norm, 1).normalizeAsV3();
//		color.set(col);
//
//		addToBuffer(baseOffset, position);
//		addToBuffer(baseOffset, color);
//
//		vertexCount++;
//
//	}
}
