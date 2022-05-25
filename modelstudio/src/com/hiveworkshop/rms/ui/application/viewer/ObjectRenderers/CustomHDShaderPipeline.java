package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class CustomHDShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = POSITION + NORMAL + UV + TANGENT + FRESNEL_COLOR + SELECTION_STATUS;

	public CustomHDShaderPipeline(String vertexShader, String fragmentShader) {
		currentMatrix.setIdentity();
//		vertexShader = OtherUtils.loadShader("HDDiffuse.vert");
//		fragmentShader = OtherUtils.loadShader("HDDiffuse.frag");
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		load();
		setupUniforms();
	}

	protected void setupUniforms(){
		createUniform("u_textureDiffuse");
		createUniform("u_textureNormal");
		createUniform("u_textureORM");
		createUniform("u_textureEmissive");
		createUniform("u_textureTeamColor");
		createUniform("u_textureReflections");
		createUniform("u_textureUsed");
		createUniform("u_alphaTest");
		createUniform("u_lightingEnabled");
		createUniform("u_lightDirection");
		createUniform("u_viewPos");
		createUniform("u_viewportSize");
		createUniform("u_fresnelTeamColor");
		createUniform("u_fresnelColor");
		createUniform("u_geosetColor");
		createUniform("u_vertColors[0]");
		createUniform("u_vertColors[1]");
		createUniform("u_vertColors[2]");
		createUniform("u_vertColors[3]");
		createUniform("u_projection");
		createUniform("u_view");
		createUniform("u_uvTransform");
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
		enableAttribArray(TANGENT, STRIDE);
		enableAttribArray(FRESNEL_COLOR, STRIDE);
		enableAttribArray(SELECTION_STATUS, STRIDE);

		setUpConstantUniforms();

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

	private void setUpConstantUniforms(){
		glUniform("u_textureDiffuse", 0);
		glUniform("u_textureNormal", 1);
		glUniform("u_textureORM", 2);
		glUniform("u_textureEmissive", 3);
		glUniform("u_textureTeamColor", 4);
		glUniform("u_textureReflections", 5);
		glUniform("u_textureUsed", textureUsed);

		float[] colorHig = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX_HIGHLIGHTED);
		float[] colorSel = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX_SELECTED);
		float[] colorEdi = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX);
		float[] colorVis = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.VERTEX_UNEDITABLE);
		glUniform("u_vertColors[0]", colorHig[0], colorHig[1], colorHig[2], colorHig[3]);
		glUniform("u_vertColors[1]", colorSel[0], colorSel[1], colorSel[2], colorSel[3]);
		glUniform("u_vertColors[2]", colorEdi[0], colorEdi[1], colorEdi[2], colorEdi[3]);
		glUniform("u_vertColors[3]", colorVis[0], colorVis[1], colorVis[2], colorVis[3]);


		alphaTest = 0;
//		lightingEnabled = 0;
		glUniform("u_alphaTest", alphaTest);
		glUniform("u_lightingEnabled", lightingEnabled);
		tempVec3.set(30.4879f, -24.1937f, 444.411f);
		glUniform("u_lightDirection", tempVec3);

		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);
		glUniform("u_viewportSize", viewPortSize);

		fillMatrixBuffer(pipelineMatrixBuffer, projectionMat);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);
	}

	private void setUpAndDraw(BufferSubInstance instance) {
		instance.setUpInstance(this);
		glUniform("u_fresnelTeamColor", instance.getFresnelTeamColor());
		glUniform("u_fresnelColor", instance.getFresnelColor());
		glUniform("u_geosetColor", instance.getLayerColor());

		fillMatrixBuffer(uvTransformMatrixBuffer, instance.getUvTransform());
		GL20.glUniformMatrix4(getUniformLocation("u_uvTransform"), false, uvTransformMatrixBuffer);


//		System.out.println("start: " + instance.getOffset() + ", verts: " + instance.getVertCount());
		GL11.glDrawArrays(glBeginType, instance.getOffset(), instance.getVertCount());
	}

	private void setUpAndDraw() {
//		alphaTest = 0;
//		lightingEnabled = 0;
		glUniform("u_alphaTest", alphaTest);
		glUniform("u_lightingEnabled", lightingEnabled);
		tempVec3.set(30.4879f, -24.1937f, 444.411f);
		glUniform("u_lightDirection", tempVec3);
//		glUniform("u_lightDirection", -24.1937f, 444.411f, 30.4879f);


		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);
		glUniform("u_viewportSize", viewPortSize);
		glUniform("u_fresnelTeamColor", fresnelTeamColor);
		glUniform("u_fresnelColor", 0, 0, 0, 0);
		fillMatrixBuffer(pipelineMatrixBuffer, currentMatrix);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);


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
//		color.set(col);


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
//		addToBuffer(baseOffset, color);
		addToBuffer(baseOffset, tangent);
		addToBuffer(baseOffset, fres);
		addToBuffer(baseOffset, 0);

		vertexCount++;

	}

	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, int selectionStatus){
		int baseOffset = prepareAddVertex(STRIDE);
		position.set(pos, 1);
		normal.set(norm, 1).normalizeAsV3();
		tangent.set(tang).normalizeAsV3();


		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, uv);
		addToBuffer(baseOffset, tangent);
		addToBuffer(baseOffset, fres);
		addToBuffer(baseOffset, selectionStatus);

		vertexCount++;

	}
}
