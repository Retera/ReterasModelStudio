package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;

public class SimpleDiffuseShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + UV + COLOR;

	public SimpleDiffuseShaderPipeline() {
		currentMatrix.setIdentity();
		vertexShader = OtherUtils.loadShader("simpleDiffuse.vert");
		fragmentShader = OtherUtils.loadShader("simpleDiffuse.frag");
		load();
	}

	public void doRender() {
		GL30.glBindVertexArray(glVertexArrayId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);

		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(NORMAL, STRIDE);
		enableAttribArray(UV, STRIDE);
		enableAttribArray(COLOR, STRIDE);

		GL20.glUseProgram(shaderProgram);


		if(!instances.isEmpty()){
			for (BufferSubInstance instance : instances){
				setUpAndDraw(instance);
			}
		} else {
			setUpAndDraw();
		}
		textureUsed = 0;
		pipelineVertexBuffer.clear();
	}

	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureDiffuse"), instance.getTextureSlot());
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureUsed"), textureUsed);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);
		tempVec4.set(30.4879f, -24.1937f, 444.411f, 1.0f);
		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y, tempVec4.z);
		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(uvTransformMatrixBuffer, instance.getUvTransform());
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_uvTransform"), false, uvTransformMatrixBuffer);

		GL11.glDrawArrays(glBeginType, instance.getOffset(), instance.getVertCount());
	}

	private void setUpAndDraw() {
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureDiffuse"), 0);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureUsed"), textureUsed);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);
		tempVec4.set(30.4879f, -24.1937f, 444.411f, 1.0f);
		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y, tempVec4.z);
		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false, pipelineMatrixBuffer);

		GL11.glDrawArrays(glBeginType, 0, vertexCount);
	}

	public void glEnableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			textureUsed = 1;
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
		}
		else if (glEnum == GL11.GL_ALPHA_TEST) {
			alphaTest = 1;
		}
		else if (glEnum == GL11.GL_LIGHTING) {
			lightingEnabled = 1;
		}
	}

	public void glShadeModel(int mode) {
	}

	public void glDisableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			textureUsed = 0;
			GL13.glActiveTexture(0);
		}
		else if (glEnum == GL11.GL_ALPHA_TEST) {
			alphaTest = 0;
		}
		else if (glEnum == GL11.GL_LIGHTING) {
			lightingEnabled = 0;
		}
	}

	public void prepareToBindTexture() {
//		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		textureUsed = 1;
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
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, color);

		vertexCount++;

	}

}