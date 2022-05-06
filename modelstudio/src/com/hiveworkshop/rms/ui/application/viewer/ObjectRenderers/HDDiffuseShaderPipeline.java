package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class HDDiffuseShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + UV + COLOR + TANGENT + FRESNEL_COLOR;

	public HDDiffuseShaderPipeline() {
		currentMatrix.setIdentity();
		vertexShader = OtherUtils.loadShader("HDDiffuse.vert");
		fragmentShader = OtherUtils.loadShader("HDDiffuse.frag");
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

		GL20.glUseProgram(shaderProgram);

		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(NORMAL, STRIDE);
		enableAttribArray(UV, STRIDE);
		enableAttribArray(COLOR, STRIDE);
		enableAttribArray(TANGENT, STRIDE);
		enableAttribArray(FRESNEL_COLOR, STRIDE);

//		if(instances.isEmpty()){
//			if(sdInstances.isEmpty()){
////			System.out.println("no Instances!");
//				setUpAndDraw();
//			} else {
//				for (SdBufferSubInstance instance : sdInstances){
//					setUpAndDraw(instance);
//				}
//			}
//		} else {
////			System.out.println("has instances!");
//			for (HdBufferSubInstance instance : instances){
//				setUpAndDraw(instance);
//			}
//		}

		if(!instances.isEmpty()){
			for (BufferSubInstance instance : instances){
				setUpAndDraw(instance);
			}
		} else {
			setUpAndDraw();
		}
		textureUsed = 0;


		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
	}

	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureDiffuse"), 0);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureNormal"), 1);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureORM"), 2);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureEmissive"), 3);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureTeamColor"), 4);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureReflections"), 5);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureUsed"), textureUsed);
//		alphaTest = 0;
//		lightingEnabled = 0;
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);
		tempVec4.set(30.4879f, -24.1937f, 444.411f, 1.0f);
		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y, tempVec4.z);
//		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), -24.1937f, 444.411f, 30.4879f);


		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewPos"), 0, 0, -1);
		GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "u_viewportSize"), viewPortSize.x, viewPortSize.y);
		GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelTeamColor"), instance.getFresnelTeamColor());
		fresnelColor.set(instance.getFresnelColor());
		GL20.glUniform4f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelColor"), fresnelColor.x, fresnelColor.y, fresnelColor.z, instance.getFresnelOpacity());
//		fillPipelineMatrixBuffer();
		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(uvTransformMatrixBuffer, instance.getUvTransform());
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_uvTransform"), false, uvTransformMatrixBuffer);


//		System.out.println("start: " + instance.getOffset() + ", verts: " + instance.getVertCount());
		GL11.glDrawArrays(glBeginType, instance.getOffset(), instance.getVertCount());
	}

	private void setUpAndDraw() {
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureDiffuse"), 0);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureNormal"), 1);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureORM"), 2);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureEmissive"), 3);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureTeamColor"), 4);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureReflections"), 5);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureUsed"), textureUsed);
		textureUsed = 0;
//		alphaTest = 0;
//		lightingEnabled = 0;
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);
		tempVec4.set(30.4879f, -24.1937f, 444.411f, 1.0f);
		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y, tempVec4.z);
//		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), -24.1937f, 444.411f, 30.4879f);


		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewPos"), 0, 0, -1);
		GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "u_viewportSize"), viewPortSize.x, viewPortSize.y);
		GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelTeamColor"), fresnelTeamColor);
		GL20.glUniform4f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelColor"), fresnelColor.x, fresnelColor.y, fresnelColor.z, fresnelOpacity);
		fillPipelineMatrixBuffer();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false, pipelineMatrixBuffer);


		GL11.glDrawArrays(glBeginType, 0, vertexCount);
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
//		textureUsed = 1;
	}

	public void glActiveHDTexture(int textureUnit) {
		this.textureUnit = textureUnit;
//		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
	}


	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres){
		int baseOffset = prepareAddVertex(STRIDE);
//		int baseOffset = vertexCount * STRIDE;
//		currBufferOffset = 0;
//		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		tangent.set(tang).normalizeAsV3();
		color.set(col);
//		color.set(1f,0f,0,1f);


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, color);
		addToBuffer(baseOffset, tangent);
		addToBuffer(baseOffset, fres);

		vertexCount++;

	}


	public void glFresnelTeamColor1f(float v) {
		this.fresnelTeamColor = v;
	}

	public void glFresnelOpacity1f(float v) {
		this.fresnelOpacity = v;
	}
}
