package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class CollisionMarkerShaderPipeline extends ShaderPipeline {
//	private static final int STRIDE = POSITION + ROTATION + SELECTION_STATUS;
//	private static final int STRIDE = POSITION + NORMAL + SELECTION_STATUS;
//	private static final int STRIDE = POSITION + NORMAL + POSITION + POSITION + SELECTION_STATUS + SELECTION_STATUS;
	private static final int STRIDE = POSITION + NORMAL + POSITION + POSITION + VEC2;


	public CollisionMarkerShaderPipeline() {
		currentMatrix.setIdentity();
		geometryShader = OtherUtils.loadShader("ShapeOutline.glsl");
		vertexShader = OtherUtils.loadShader("ShapeOutline.vert");
		fragmentShader = OtherUtils.loadShader("ShapeOutline.frag");
		load();
		setupUniforms();
	}

	public CollisionMarkerShaderPipeline(String vertexShader, String fragmentShader, String geometryShader) {
		currentMatrix.setIdentity();
		this.geometryShader = geometryShader;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		load();
		setupUniforms();
	}


	protected void setupUniforms(){
//		createUniform("scale");
		createUniform("u_viewPos");
		createUniform("u_projection");
		createUniform("u_view");
		createUniform("u_size");
		createUniform("show_node_dir");

		createUniform("u_vertColors[0]");
		createUniform("u_vertColors[1]");
		createUniform("u_vertColors[2]");
		createUniform("u_vertColors[3]");
	}

	public void doRender() {
		//https://github.com/flowtsohg/mdx-m3-viewer/tree/827d1bda1731934fb8e1a5cf68d39786f9cb857d/src/viewer/handlers/w3x/shaders
		GL30.glBindVertexArray(glVertexArrayId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);

		//POSITION + NORMAL + POSITION + POSITION + VEC2;
		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(NORMAL, STRIDE);
		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(POSITION, STRIDE);
		enableAttribArray(VEC2, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;



		float[] colorHig = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE_HIGHLIGHTED);
		float[] colorSel = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE_SELECTED);
		float[] colorEdi = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE);
		float[] colorVis = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.NODE_UNEDITABLE);
		glUniform("u_vertColors[0]", colorHig[0], colorHig[1], colorHig[2], colorHig[3]);
		glUniform("u_vertColors[1]", colorSel[0], colorSel[1], colorSel[2], colorSel[3]);
		glUniform("u_vertColors[2]", colorEdi[0], colorEdi[1], colorEdi[2], colorEdi[3]);
		glUniform("u_vertColors[3]", colorVis[0], colorVis[1], colorVis[2], colorVis[3]);

//		float u_size = ProgramGlobals.getPrefs().getNodeBoxSize();
		glUniform("u_size", ProgramGlobals.getPrefs().getNodeBoxSize()/5f);
		glUniform("show_node_dir", ProgramGlobals.getPrefs().showNodeForward() ? 1 : 0);


		tempVec4.set(0,0,0,1).transform(currentMatrix);
//		glUniform("scale", tempVec4.w/ viewPortSize.x, tempVec4.w/ viewPortSize.y);



		glUniform("u_viewPos", Vec3.NEGATIVE_Z_AXIS);
		fillMatrixBuffer(pipelineMatrixBuffer, projectionMat);
		GL20.glUniformMatrix4(getUniformLocation("u_projection"), false, pipelineMatrixBuffer);
		fillMatrixBuffer(pipelineViewMatrixBuffer, viewMat);
		GL20.glUniformMatrix4(getUniformLocation("u_view"), false, pipelineViewMatrixBuffer);


		GL11.glDrawArrays(GL11.GL_POINTS, 0, vertexCount);
		pipelineVertexBuffer.clear();
		GL20.glUseProgram(0);
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

//	PeriodicOut out = new PeriodicOut(2000);
	public void addVert(Vec3 pos, Vec3 norm, Vec4 vert1, Vec2 type_rad, Vec4 vert2, Vec3 fres, int selectionStatus){
		// need:
		// position vec3,
		// type int,
		// vert1 vec3, selectionStatus1 int, alt vec4
		// vert2 vec3, selectionStatus2 int, alt vec4
		// radius float
		int baseOffset = vertexCount * STRIDE;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);

		if(norm != null){
			normal.set(norm, 1);
		} else {
			normal.set(pos, 1);
		}


		if(fres == null){
			tempVec3.set(normal);
		} else {
			tempVec3.set(fres);
		}

		tempVec4.set(type_rad.x, type_rad.y, type_rad.x, type_rad.y);

		addToBuffer(baseOffset, position);
		addToBuffer(baseOffset, normal);
		addToBuffer(baseOffset, vert1);
		addToBuffer(baseOffset, vert2);
		addToBuffer(baseOffset, type_rad);


		vertexCount++;
	}

}
