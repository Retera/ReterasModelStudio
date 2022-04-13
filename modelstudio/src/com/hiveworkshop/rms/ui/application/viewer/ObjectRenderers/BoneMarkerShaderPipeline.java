package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;


public class BoneMarkerShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = 4 /* position */ + 0 /* normal */ + 4 /* color */ ;
	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;


	public BoneMarkerShaderPipeline() {
		currentMatrix.setIdentity();
		geometryShader = OtherUtils.loadShader("Bone.glsl");
		vertexShader = OtherUtils.loadShader("Bone.vert");
		fragmentShader = OtherUtils.loadShader("Bone.frag");
//		geometryShader = OtherUtils.loadShader("NormalLines.glsl");
//		vertexShader = OtherUtils.loadShader("NormalLines.vert");
//		fragmentShader = OtherUtils.loadShader("NormalLines.frag");
		load();
	}

	public void glEnd() {
//		System.out.println("glEnd");
		//https://github.com/flowtsohg/mdx-m3-viewer/tree/827d1bda1731934fb8e1a5cf68d39786f9cb857d/src/viewer/handlers/w3x/shaders
		GL30.glBindVertexArray(vertexArrayObjectId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);

		// Vertex
		enableAttribArray(4, STRIDE);
		// Rotation
		enableAttribArray(4, STRIDE);
//		 Color
		enableAttribArray(4, STRIDE);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL20.glUseProgram(shaderProgram);
		textureUsed = 0;
		alphaTest = 0;
		lightingEnabled = 0;
//		GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "vertRad"), 20f);
//		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
//		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);
//		tempVec4.set(30.4879f, -24.1937f, 444.411f, 1.0f);
//		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y, tempVec4.z);
//		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), -24.1937f, 444.411f, 30.4879f);


//		tempVec3.unProject(Vec2.ONE, currentMatrix);
//		System.out.println("unprojected {1,1}: " + tempVec3 + ", 1 / x =  " + 1/tempVec3.x + ", " + "1 / y =  " + 1/tempVec3.y + ", " + "1 / z =  " + 1/tempVec3.z + " vp: {" + viewportWidth + ", " + viewportHeight + "}" + " vp/2: {" + viewportWidth/2f + ", " + viewportHeight/2f + "}");
//
//		tempVec4.set(0,0,0,1).transform(currentMatrix);
//		System.out.println("vec4 {1,1,0,1} transformed: " + tempVec4);
//		System.out.println("1 / viewportWidth =  " + 1/viewportWidth + ", " + "1 / viewportHeight =  " + 1/viewportHeight);
//		System.out.println("w / viewportWidth =  " + tempVec4.w/viewportWidth + ", " + "w / viewportHeight =  " + tempVec4.w/viewportHeight);



		tempVec4.set(0,0,0,1).transform(currentMatrix);
		GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "scale"), tempVec4.w/viewportWidth, tempVec4.w/viewportHeight);



//		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewPos"), 0, 0, -1);
		pipelineMatrixBuffer.clear();
		pipelineMatrixBuffer.put(currentMatrix.m00);
		pipelineMatrixBuffer.put(currentMatrix.m01);
		pipelineMatrixBuffer.put(currentMatrix.m02);
		pipelineMatrixBuffer.put(currentMatrix.m03);
		pipelineMatrixBuffer.put(currentMatrix.m10);
		pipelineMatrixBuffer.put(currentMatrix.m11);
		pipelineMatrixBuffer.put(currentMatrix.m12);
		pipelineMatrixBuffer.put(currentMatrix.m13);
		pipelineMatrixBuffer.put(currentMatrix.m20);
		pipelineMatrixBuffer.put(currentMatrix.m21);
		pipelineMatrixBuffer.put(currentMatrix.m22);
		pipelineMatrixBuffer.put(currentMatrix.m23);
		pipelineMatrixBuffer.put(currentMatrix.m30);
		pipelineMatrixBuffer.put(currentMatrix.m31);
		pipelineMatrixBuffer.put(currentMatrix.m32);
		pipelineMatrixBuffer.put(currentMatrix.m33);
		pipelineMatrixBuffer.flip();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false, pipelineMatrixBuffer);


//		GL11.glDrawArrays(glBeginType, 0, vertexCount);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, vertexCount);
		vertexCount = 0;
		uvCount = 0;
		normalCount = 0;
		colorCount = 0;
		fresnelColorCount = 0;
		tangentCount = 0;
		pipelineVertexBuffer.clear();
		textureUnit = 0;
		attributeArrayOffs = 0;
		attributeArrayIndex = 0;
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
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1);
		normal.set(Vec3.Z_AXIS).normalize();
		normal.set(norm).normalize();
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

	public void glVertex3f(float x, float y, float z) {
		int baseOffset = vertexCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 0, x);
		pushFloat(baseOffset + 1, y);
		pushFloat(baseOffset + 2, z);
		pushFloat(baseOffset + 3, 1);
		pushFloat(baseOffset + 14, color.x);
		pushFloat(baseOffset + 15, color.y);
		pushFloat(baseOffset + 16, color.z);
		pushFloat(baseOffset + 17, color.w);
		pushFloat(baseOffset + 18, fresnelColor.x);
		pushFloat(baseOffset + 19, fresnelColor.y);
		pushFloat(baseOffset + 20, fresnelColor.z);
		vertexCount++;
	}

//	public void glNormal3f(float x, float y, float z) {
//		normal.set(x, y, z).transform(0f, currentMatrix).normalize();
//		glNormal(normal);
//	}
//	public void glNormal3f(Vec3 norm) {
//		normal.set(norm).transform(0f, currentMatrix).normalize();
//		glNormal(normal);
//	}
//
//	public void glNormal3f(Vec4 norm) {
//		normal.set(norm).transform(0f, currentMatrix).normalize();
//		glNormal(normal);
//	}
//
//	private void glNormal(Vec3 normal) {
//		int baseOffset = normalCount * STRIDE;
//		ensureCapacity(baseOffset + STRIDE);
//		pushFloat(baseOffset + 4, normal.x);
//		pushFloat(baseOffset + 5, normal.y);
//		pushFloat(baseOffset + 6, normal.z);
//		pushFloat(baseOffset + 7, 1);
//		normalCount++;
//	}

	public void glTexCoord2f(Vec2 uv) {
		glTexCoord2f(uv.x, uv.y);
	}
	public void glTexCoord2f(float u, float v) {
		int baseOffset = uvCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 12, u);
		pushFloat(baseOffset + 13, v);
		uvCount++;
	}

//	public void glTangent4f(Vec4 vec4) {
//		// tangents are not applicable to old style drawing
//		tangent.set(vec4);
//		glTangent(tangent);
//	}
//
//	public void glTangent4f(float x, float y, float z, float w) {
//		tangent.set(x, y, z, w);
//		glTangent(tangent);
//	}
//
//	private void glTangent(Vec4 tangent) {
//		int baseOffset = tangentCount * STRIDE;
//		ensureCapacity(baseOffset + STRIDE);
//		pushFloat(baseOffset + 8, tangent.x);
//		pushFloat(baseOffset + 9, tangent.y);
//		pushFloat(baseOffset + 10, tangent.z);
//		pushFloat(baseOffset + 11, tangent.w);
//		tangentCount++;
//	}


//	public void glColor3f(float r, float g, float b) {
//		color.set(r, g, b, color.w);
//		glColor(color);
//	}
//
//	public void glColor4ub(byte r, byte g, byte b, byte a) {
//		color.set((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
//		glColor(color);
//	}
//
//	public void glColor4f(float r, float g, float b, float a) {
//		color.set(r, g, b, a);
//		glColor(color);
//	}
//
//	private void glColor(Vec4 color) {
//		int baseOffset = colorCount * STRIDE;
//		ensureCapacity(baseOffset + STRIDE);
//		pushFloat(baseOffset + 14, color.x);
//		pushFloat(baseOffset + 15, color.y);
//		pushFloat(baseOffset + 16, color.z);
//		pushFloat(baseOffset + 17, color.w);
//		colorCount++;
//	}

	public void glFresnelColor3f(Vec3 fres) {
		fresnelColor.set(fres);
		glFresnelColor(fresnelColor);
	}

	public void glFresnelColor3f(float r, float g, float b) {
		fresnelColor.set(r, g, b);
		glFresnelColor(fresnelColor);
	}

	private void glFresnelColor(Vec3 fresnelColor) {
//		int baseOffset = fresnelColorCount * STRIDE;
//		ensureCapacity(baseOffset + STRIDE);
//		pushFloat(baseOffset + 18, fresnelColor.x);
//		pushFloat(baseOffset + 19, fresnelColor.y);
//		pushFloat(baseOffset + 20, fresnelColor.z);
//		fresnelColorCount++;
	}

	public void glFresnelTeamColor1f(float v) {
		this.fresnelTeamColor = v;
	}

	public void glFresnelOpacity1f(float v) {
		this.fresnelOpacity = v;
	}
}
