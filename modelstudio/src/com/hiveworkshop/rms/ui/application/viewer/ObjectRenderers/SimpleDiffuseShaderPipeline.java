package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.*;

public class SimpleDiffuseShaderPipeline extends ShaderPipeline {
	private static final int STRIDE = 4 /* position */ + 4 /* normal */ + 2 /* uv */ + 4 /* color */;
	private static final int POS_OFFSET = 0;
	private static final int NORM_OFFSET = 4;
	private static final int UV_OFFSET = 8;
	private static final int COL_OFFSET = 10;
	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

	public SimpleDiffuseShaderPipeline() {
		currentMatrix.setIdentity();
		vertexShader = OtherUtils.loadShader("simpleDiffuse.vert");
		fragmentShader = OtherUtils.loadShader("simpleDiffuse.frag");
		load();
	}

	public void glEnd() {
		GL30.glBindVertexArray(vertexArrayObjectId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);

		// Vertex
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 0);
		// Normal
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 4 * Float.BYTES);
		// UV
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, STRIDE_BYTES, 8 * Float.BYTES);
		// Color
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 10 * Float.BYTES);

		GL20.glUseProgram(shaderProgram);

		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureDiffuse"), 0);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureUsed"), textureUsed);
		textureUsed = 0;
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);
		tempVec4.set(30.4879f, -24.1937f, 444.411f, 1.0f);
		tempVec4.transform(currentMatrix);
		tempVec4.normalize();
		GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y, tempVec4.z);
		GL11.glDrawArrays(glBeginType, 0, vertexCount);
		vertexCount = 0;
		uvCount = 0;
		normalCount = 0;
		colorCount = 0;
		pipelineVertexBuffer.clear();
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
		ensureCapacity(baseOffset + STRIDE);
		position.set(pos, 1).transform(currentMatrix);
		normal.set(norm).transform(0f, currentMatrix).normalize();
		color.set(col);


		pipelineVertexBuffer.put(baseOffset + 0, position.x);
		pipelineVertexBuffer.put(baseOffset + 1, position.y);
		pipelineVertexBuffer.put(baseOffset + 2, position.z);
		pipelineVertexBuffer.put(baseOffset + 3, position.w);

		pipelineVertexBuffer.put(baseOffset + 4, normal.x);
		pipelineVertexBuffer.put(baseOffset + 5, normal.y);
		pipelineVertexBuffer.put(baseOffset + 6, normal.z);
		pipelineVertexBuffer.put(baseOffset + 7, 1);

		pipelineVertexBuffer.put(baseOffset + 8, uv.x);
		pipelineVertexBuffer.put(baseOffset + 9, uv.y);

		pipelineVertexBuffer.put(baseOffset + 10, color.x);
		pipelineVertexBuffer.put(baseOffset + 11, color.y);
		pipelineVertexBuffer.put(baseOffset + 12, color.z);
		pipelineVertexBuffer.put(baseOffset + 13, color.w);
		vertexCount++;

	}

	public void glVertex3f(float x, float y, float z) {
		position.set(x, y, z, 1);
		position.transform(currentMatrix);
		glVertex(position);
		initVertColor(color);
	}

	public void glVertex3f(Vec3 pos) {
		position.set(pos, 1).transform(currentMatrix);
		glVertex(position);
		initVertColor(color);
	}

	public void glVertex3f(Vec4 pos) {
		position.set(pos);
		position.w = 1;
		position.transform(currentMatrix);
		glVertex(position);
		initVertColor(color);
	}

	private void initVertColor(Vec4 color) {
		int baseOffset = vertexCount * STRIDE - STRIDE;
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
	}

	private void glVertex(Vec4 position) {
		int baseOffset = vertexCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 0, position.x);
		pushFloat(baseOffset + 1, position.y);
		pushFloat(baseOffset + 2, position.z);
		pushFloat(baseOffset + 3, position.w);
		vertexCount++;
	}


	public void glNormal3f(float x, float y, float z) {
		normal.set(x, y, z).transform(0f, currentMatrix).normalize();
		glNormal(normal);
	}
	public void glNormal3f(Vec3 norm) {
		normal.set(norm).transform(0f, currentMatrix).normalize();
		glNormal(normal);
	}

	public void glNormal3f(Vec4 norm) {
		normal.set(norm).transform(0f, currentMatrix).normalize();
		glNormal(normal);
	}

	private void glNormal(Vec3 normal) {
		int baseOffset = normalCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 4, normal.x);
		pushFloat(baseOffset + 5, normal.y);
		pushFloat(baseOffset + 6, normal.z);
		pushFloat(baseOffset + 7, 1);
		normalCount++;
	}


	public void glTexCoord2f(Vec2 uv) {
		glTexCoord2f(uv.x, uv.y);
	}
	public void glTexCoord2f(float u, float v) {
		int baseOffset = uvCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 8, u);
		pushFloat(baseOffset + 9, v);
		uvCount++;
	}

	public void glTangent4f(float x, float y, float z, float w) {
		// tangents are not applicable to old style drawing
	}

	public void glTangent4f(Vec4 vec4) {
		// tangents are not applicable to old style drawing
	}

	public void glColor4f(float r, float g, float b, float a) {
		color.set(r, g, b, a);
		glColor(color);
	}

	public void glColor4f(float[] col) {
		color.set(col);
		glColor(color);
	}

	public void glColor3f(float r, float g, float b) {
		color.set(r, g, b, color.w);
		glColor(color);
	}

	public void glColor4ub(byte r, byte g, byte b, byte a) {
		color.set((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
		glColor(color);
	}
	public void glColor3f(Vec3 col) {
		color.set(col, color.w);
		glColor(color);
	}

	public void glColor4f(Vec4 col) {
		color.set(col);
		glColor(color);
	}

	private void glColor(Vec4 color) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glFresnelColor3f(float r, float g, float b) {
	}

	public void glFresnelColor3f(Vec3 fres) {
	}

	public void glFresnelTeamColor1f(float v) {
	}

	public void glFresnelOpacity1f(float v) {
	}

}