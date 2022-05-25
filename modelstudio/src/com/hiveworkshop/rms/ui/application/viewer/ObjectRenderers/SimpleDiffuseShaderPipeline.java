package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;

public class SimpleDiffuseShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + UV + TANGENT + SELECTION_STATUS;

	public SimpleDiffuseShaderPipeline() {
		currentMatrix.setIdentity();
		vertexShader = OtherUtils.loadShader("simpleDiffuse.vert");
		fragmentShader = OtherUtils.loadShader("simpleDiffuse.frag");
		load();
		setupUniforms();
	}
	public SimpleDiffuseShaderPipeline(String vertexShader, String fragmentShader) {
		currentMatrix.setIdentity();
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		load();
		setupUniforms();
	}


	protected void setupUniforms(){
		createUniform("u_textureDiffuse");
		createUniform("u_textureUsed");
		createUniform("u_alphaTest");
		createUniform("u_lightingEnabled");
		createUniform("u_lightDirection");
		createUniform("u_viewPos");
		createUniform("u_projection");
		createUniform("u_view");
		createUniform("u_uvTransform");
		createUniform("u_geosetColor");

		createUniform("a_selectionStatus");
		createUniform("u_vertColors[0]");
		createUniform("u_vertColors[1]");
		createUniform("u_vertColors[2]");
		createUniform("u_vertColors[3]");
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
		enableAttribArray(TANGENT, STRIDE);
		enableAttribArray(SELECTION_STATUS, STRIDE);

		GL20.glUseProgram(shaderProgram);
		setUpConstantUniforms();


		if(!instances.isEmpty()){
			for (BufferSubInstance instance : instances){
				if(textureUsed == 0 && instance.getTextureSlot() == 0){
					setUpAndDraw(instance);
				} else {
					setUpAndDraw(instance);
				}
			}
		} else {
			setUpAndDraw();
		}
//		textureUsed = 0;
		pipelineVertexBuffer.clear();
	}

	private void setUpConstantUniforms(){
		glUniform("u_textureUsed", textureUsed);
		glUniform("u_lightingEnabled", lightingEnabled);
		tempVec3.set(30.4879f, -24.1937f, 444.411f);
		glUniform("u_lightDirection", tempVec3);
		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);
		fillMatrixBuffer(pipelineMatrixBuffer, projectionMat);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);


		float[] colorHig = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX_HIGHLIGHTED);
		float[] colorSel = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX_SELECTED);
		float[] colorEdi = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX);
		float[] colorVis = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX_UNEDITABLE);
		glUniform("u_vertColors[0]", colorHig[0], colorHig[1], colorHig[2], colorHig[3]);
		glUniform("u_vertColors[1]", colorSel[0], colorSel[1], colorSel[2], colorSel[3]);
		glUniform("u_vertColors[2]", colorEdi[0], colorEdi[1], colorEdi[2], colorEdi[3]);
		glUniform("u_vertColors[3]", colorVis[0], colorVis[1], colorVis[2], colorVis[3]);
	}

	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		if(textureUsed == 0){
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		glUniform("u_textureDiffuse", instance.getTextureSlot());
		glUniform("u_geosetColor", instance.getLayerColor());
		glUniform("u_alphaTest", alphaTest);
		fillMatrixBuffer(uvTransformMatrixBuffer, instance.getUvTransform());
		GL20.glUniformMatrix4(getUniformLocation("u_uvTransform"), false, uvTransformMatrixBuffer);

		GL11.glDrawArrays(glBeginType, instance.getOffset(), instance.getVertCount());
	}

	private void setUpAndDraw() {
		glUniform("u_textureDiffuse", 0);
		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);

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


	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres){
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		tangent.set(tang).normalizeAsV3();


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, tangent);
		addToBuffer(baseOffset, 0);

		vertexCount++;

	}

	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, int selectionStatus){
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		tangent.set(tang).normalizeAsV3();


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, tangent);
		addToBuffer(baseOffset, selectionStatus);

		vertexCount++;

	}

}