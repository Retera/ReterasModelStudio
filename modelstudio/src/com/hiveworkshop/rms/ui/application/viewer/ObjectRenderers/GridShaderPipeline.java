package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class GridShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + COLOR ;
	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;


	public GridShaderPipeline() {
		currentMatrix.setIdentity();
//		geometryShader = OtherUtils.loadShader("Grid.glsl");
		vertexShader = OtherUtils.loadShader("Grid.vert");
		fragmentShader = OtherUtils.loadShader("Grid.frag");
		load();
		setupUniforms();
	}

	public GridShaderPipeline(String vertexShader, String fragmentShader, String geometryShader) {
		currentMatrix.setIdentity();
//		this.geometryShader = geometryShader;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		load();
		setupUniforms();
	}


	protected void setupUniforms(){
//		createUniform("u_viewPos");
		createUniform("u_projection");
		createUniform("u_view");
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

		if(!instances.isEmpty()){
			for (BufferSubInstance instance : instances){
				setUpAndDraw(instance);
			}
		} else {


//		GL11.glDrawArrays(glBeginType, 0, vertexCount);
//		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
//		GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCount);
			GL31.glDrawArraysInstanced(GL11.GL_LINES, 0, 2, 100);
			GL31.glDrawArraysInstanced(GL11.GL_LINES, 2, 2, 100);
		}
		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
	}

	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		GL31.glDrawArraysInstanced(GL11.GL_LINES, instance.getOffset(), instance.getVertCount(), 100);
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
