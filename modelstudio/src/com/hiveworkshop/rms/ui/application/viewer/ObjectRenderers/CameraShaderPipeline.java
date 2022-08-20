package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class CameraShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + SELECTION_STATUS ;
	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;


	public CameraShaderPipeline() {
		currentMatrix.setIdentity();
		vertexShader = OtherUtils.loadShader("Camera.vert");
		fragmentShader = OtherUtils.loadShader("Camera.frag");
		load();
		setupUniforms();
	}

	public CameraShaderPipeline(String vertexShader, String fragmentShader, String geometryShader) {
		currentMatrix.setIdentity();
//		this.geometryShader = geometryShader;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		load();
		setupUniforms();
	}


	protected void setupUniforms(){
		createUniform("u_projection");
		createUniform("u_view");
		createUniform("u_vertColors[0]");
		createUniform("u_vertColors[1]");
		createUniform("u_vertColors[2]");
		createUniform("u_vertColors[3]");
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
		enableAttribArray(SELECTION_STATUS, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;

		fillMatrixBuffer(pipelineMatrixBuffer, projectionMat);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);

		float[] colorHig = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE_HIGHLIGHTED);
		float[] colorSel = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE_SELECTED);
		float[] colorEdi = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE);
		float[] colorVis = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE_UNEDITABLE);

		glUniform("u_vertColors[0]", colorHig[0], colorHig[1], colorHig[2], colorHig[3]);
		glUniform("u_vertColors[1]", colorSel[0], colorSel[1], colorSel[2], colorSel[3]);
		glUniform("u_vertColors[2]", colorEdi[0], colorEdi[1], colorEdi[2], colorEdi[3]);
		glUniform("u_vertColors[3]", colorVis[0], colorVis[1], colorVis[2], colorVis[3]);


		if(!instances.isEmpty()){
			for (BufferSubInstance instance : instances){
				setUpAndDraw(instance);
			}
		} else {


			GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCount);
//			GL31.glDrawArraysInstanced(GL11.GL_LINES, 0, 2, 100);
//			GL31.glDrawArraysInstanced(GL11.GL_LINES, 2, 2, 100);
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

	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, int selectionStatus){
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(norm, 0);

		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, selectionStatus);

		vertexCount++;

	}
}
