package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class NormalLinesShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL;


	public NormalLinesShaderPipeline() {
		currentMatrix.setIdentity();
		geometryShader = OtherUtils.loadShader("NormalLines.glsl");
		vertexShader = OtherUtils.loadShader("NormalLines.vert");
		fragmentShader = OtherUtils.loadShader("NormalLines.frag");
		load();
		setupUniforms();
	}


	protected void setupUniforms(){
		createUniform("u_color");
		createUniform("u_viewPos");
		createUniform("u_projection");
		createUniform("u_view");
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
//		enableAttribArray(COLOR, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;


		glUniform("u_color", color);
		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);
		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);


//		GL11.glDrawArrays(glBeginType, 0, vertexCount);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, vertexCount);
		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
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
		normal.set(norm, 1).normalizeAsV3();
		color.set(col);


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);

		vertexCount++;

	}
	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, int selectionStatus){
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		color.set(col);


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);

		vertexCount++;
	}
}
