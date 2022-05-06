package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class BoneMarkerShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + ROTATION + COLOR;


	public BoneMarkerShaderPipeline() {
		currentMatrix.setIdentity();
		geometryShader = OtherUtils.loadShader("Bone.glsl");
		vertexShader = OtherUtils.loadShader("Bone.vert");
		fragmentShader = OtherUtils.loadShader("Bone.frag");
		load();
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
		enableAttribArray(ROTATION, STRIDE);
		enableAttribArray(COLOR, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;


		tempVec4.set(0,0,0,1).transform(currentMatrix);
		GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "scale"), tempVec4.w/ viewPortSize.x, tempVec4.w/ viewPortSize.y);



//		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewPos"), 0, 0, -1);
		fillPipelineMatrixBuffer();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false, pipelineMatrixBuffer);


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

	public void glShadeModel(int mode) {
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

	public void prepareToBindTexture() {
//		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		textureUsed = 1;
	}

	public void glActiveHDTexture(int textureUnit) {
		this.textureUnit = textureUnit;
//		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
	}


	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres){
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(Vec3.Z_AXIS, 1).normalize();
		normal.set(norm, 1).normalizeAsV3();
		tempQuat.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(45)).normalize();
		color.set(col);

		int index = 0;

//		pipelineVertexBuffer.put(baseOffset + 0, position.x);
//		pipelineVertexBuffer.put(baseOffset + 1, position.y);
//		pipelineVertexBuffer.put(baseOffset + 2, position.z);
//		pipelineVertexBuffer.put(baseOffset + 3, position.w);
////
////		pipelineVertexBuffer.put(baseOffset + 4, normal.x);
////		pipelineVertexBuffer.put(baseOffset + 5, normal.y);
////		pipelineVertexBuffer.put(baseOffset + 6, normal.z);
////		pipelineVertexBuffer.put(baseOffset + 7, 1);
//
//		pipelineVertexBuffer.put(baseOffset + 4, color.x);
//		pipelineVertexBuffer.put(baseOffset + 5, color.y);
//		pipelineVertexBuffer.put(baseOffset + 6, color.z);
//		pipelineVertexBuffer.put(baseOffset + 7, color.w);
		pipelineVertexBuffer.put(baseOffset + index++, position.x);
		pipelineVertexBuffer.put(baseOffset + index++, position.y);
		pipelineVertexBuffer.put(baseOffset + index++, position.z);
		pipelineVertexBuffer.put(baseOffset + index++, position.w);

		pipelineVertexBuffer.put(baseOffset + index++, tempQuat.x);
		pipelineVertexBuffer.put(baseOffset + index++, tempQuat.y);
		pipelineVertexBuffer.put(baseOffset + index++, tempQuat.z);
		pipelineVertexBuffer.put(baseOffset + index++, tempQuat.w);

		pipelineVertexBuffer.put(baseOffset + index++, color.x);
		pipelineVertexBuffer.put(baseOffset + index++, color.y);
		pipelineVertexBuffer.put(baseOffset + index++, color.z);
		pipelineVertexBuffer.put(baseOffset + index++, color.w);
		vertexCount++;

	}


	public void glFresnelTeamColor1f(float v) {
		this.fresnelTeamColor = v;
	}

	public void glFresnelOpacity1f(float v) {
		this.fresnelOpacity = v;
	}
}
