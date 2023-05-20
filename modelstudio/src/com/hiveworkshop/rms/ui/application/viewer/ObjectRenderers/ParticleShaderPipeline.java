package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class ParticleShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + UV + COLOR + 1;

	public ParticleShaderPipeline(String vert, String frag, String glsl) {
		currentMatrix.setIdentity();
		vertexShader = vert;
		fragmentShader = frag;
		geometryShader = glsl;
		load();
		setupUniforms();
	}

	public ParticleShaderPipeline() {
		currentMatrix.setIdentity();
		vertexShader = OtherUtils.loadShader("Particle.vert");
		fragmentShader = OtherUtils.loadShader("Particle.frag");
		geometryShader = OtherUtils.loadShader("Particle.glsl");
		load();
		setupUniforms();
	}

	protected void setupUniforms(){
		createUniform("u_texture");
		createUniform("u_alphaTest");
		createUniform("u_projection");
		createUniform("u_view");
		createUniform("u_transform");
		createUniform("u_flipBookSize");
	}

	public void doRender() {
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
		enableAttribArray(1, STRIDE); // Scale

//		setUpConstantUniforms();

		for (BufferSubInstance instance : instances){
			setUpAndDraw(instance);
		}
		textureUsed = 0;


		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
	}

//	private void setUpConstantUniforms(){
//		glUniform("u_alphaTest", alphaTest);
//
//		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
//		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
//	}
	protected final FloatBuffer antiRotBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	protected final Mat4 antiRotMat = new Mat4().setIdentity();
	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		glUniform("u_texture", 0);
		glUniform("u_alphaTest", alphaTest);
		glUniform("u_flipBookSize", instance.getFlipBookSize());
		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);
		fillMatrixBuffer(antiRotBuffer, antiRotMat);
		GL20.glUniformMatrix4(getUniformLocation("u_transform"), false, antiRotBuffer);

		GL11.glDrawArrays(GL11.GL_POINTS, 0, vertexCount);
	}

	public void glEnableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			textureUsed = 1;
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
		int baseOffset = prepareAddVertex(STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		tangent.set(tang).normalizeAsV3();
		color.set(col);

		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, color);

		vertexCount++;

	}

	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, int selectionStatus){
		int baseOffset = prepareAddVertex(STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		tangent.set(tang).normalizeAsV3();
		color.set(col);


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, color);
		addToBuffer(baseOffset, 1);



		vertexCount++;

	}
	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, float uniformScale){
		int baseOffset = prepareAddVertex(STRIDE);
		position.set(pos, 1);                   // Loc
		normal.set(norm, 1).normalizeAsV3();
//		tangent.set(tang).normalizeAsV3();
		tangent.set(tang);                      // [[Tail loc] or [0,0,0], isTail]
		color.set(col);                         // Color


		addToBuffer(baseOffset, position);
//		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, tang);          // [[Tail loc], isTail(=1)] or [0,0,0, isTail(=0)]
		addToBuffer(baseOffset, uv);            // [UV indexX, UV indexY]
		addToBuffer(baseOffset, color);
		addToBuffer(baseOffset, uniformScale);  // scale


		vertexCount++;

	}
}
