package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SimpleDiffuseShaderPipeline {
	private static final int STRIDE = 4 /* position */ + 4 /* normal */ + 2 /* uv */ + 4 /* color */;
	private static final int STRIDE_BYTES = STRIDE * Float.BYTES;
	private static String vertexShader;
	private static String fragmentShader;
//private static final String vertexShader = "#version 330 core\r\n" + //
//		"\r\n" + //
//		"layout (location = 0) in vec4 a_position;\r\n" + //
//		"layout (location = 1) in vec4 a_normal;\r\n" + //
//		"layout (location = 2) in vec2 a_uv;\r\n" + //
//		"layout (location = 3) in vec4 a_color;\r\n" + //
//		"\r\n" + //
//		"out vec2 v_uv;\r\n" + //
//		"out vec4 v_color;\r\n" + //
//		"\r\n" + //
//		"uniform vec3 u_lightDirection;\r\n" + //
//		"uniform int u_lightingEnabled;\r\n" + //
//		"\r\n" + //
//		"void main() {\r\n" + //
//		"		gl_Position = a_position;\r\n" + //
//		"		v_uv = a_uv;\r\n" + //
//		"		v_color = a_color;\r\n" + //
//		"		if(u_lightingEnabled != 0) {\r\n" + //
//		"			vec3 lightFactorContribution = vec3(clamp(dot(a_normal.xyz, u_lightDirection), 0.0, 1.0));\r\n"
//		+ //
//		"			v_color.rgb = v_color.rgb * clamp(lightFactorContribution * 1.3 + vec3(0.5f, 0.5f, 0.5f), 0.0, 1.0);\r\n"
//		+ //
//		"		}\r\n" + //
//		"}\r\n\0";
//	private static final String fragmentShader = "#version 330 core\r\n" + //
//			"\r\n" + //
//			"uniform sampler2D u_texture;\r\n" + //
//			"uniform int u_textureUsed;\r\n" + //
//			"uniform int u_alphaTest;\r\n" + //
//			"\r\n" + //
//			"in vec2 v_uv;\r\n" + //
//			"in vec4 v_color;\r\n" + //
//			"\r\n" + //
//			"out vec4 FragColor;\r\n" + //
//			"\r\n" + //
//			"void main() {\r\n" + // s
//			"		vec4 color;\r\n" + //
//			"		if(u_textureUsed != 0) {\r\n" + //
//			"			vec4 texel = texture2D(u_texture, v_uv);\r\n" + //
//			"			color = texel * v_color;\r\n" + //
//			"		} else {\r\n" + //
//			"			color = v_color;\r\n" + //
//			"		}\r\n" + //
//			"		if(u_alphaTest != 0 && color.a < 0.75) {\r\n" + //
//			"			discard;\r\n" + //
//			"		}\r\n" + //
//			"		FragColor = color;\r\n" + //
//			"}\r\n\0";
	private final Vec4 color = new Vec4(1f, 1f, 1f, 1f);
	private FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private int vertexCount = 0;
	private int normalCount = 0;
	private int uvCount = 0;
	private int colorCount = 0;
	private int glBeginType;
	private int shaderProgram;
	private int vertexBufferObjectId;
	private int vertexArrayObjectId; // has nothing to do with "object id" of war3 models
	private boolean loaded = false;
	private final Mat4 currentMatrix = new Mat4();
	private int textureUsed = 0;
	private int alphaTest = 0;
	private int lightingEnabled = 1;

	private final Vec3 tempVec3 = new Vec3();
	private final Vec4 tempVec4 = new Vec4();

	private final Quat tempQuat = new Quat();
	private final Mat4 tempMat4 = new Mat4();
	private int matrixMode;

	//	private static final String vertexShader = OtherUtils.loadShader("simpleDiffuse.vert");
//	private static final String fragmentShader = OtherUtils.loadShader("simpleDiffuse.frag");
	public SimpleDiffuseShaderPipeline() {
		currentMatrix.setIdentity();
//		vertexShader = OtherUtils.loadShader("simpleDiffuse.vert");
//		fragmentShader = OtherUtils.loadShader("simpleDiffuse.frag");
		vertexShader = OtherUtils.loadShader("vertex_basic.vert");
		fragmentShader = OtherUtils.loadShader("fragment_basic.frag");
		load();
	}

	private void load() {
		int vertexShaderId = createShader(GL20.GL_VERTEX_SHADER, vertexShader);
		int fragmentShaderId = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);
		shaderProgram = GL20.glCreateProgram();
		GL20.glAttachShader(shaderProgram, vertexShaderId);
		GL20.glAttachShader(shaderProgram, fragmentShaderId);
		GL20.glLinkProgram(shaderProgram);
		int linkStatus = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS);
		if (linkStatus == GL11.GL_FALSE) {
			String errorText = GL20.glGetProgramInfoLog(shaderProgram, 1024);
			System.err.println(errorText);
			throw new IllegalStateException(linkStatus + ": " + errorText);
		}
		GL20.glDeleteShader(vertexShaderId);
		GL20.glDeleteShader(fragmentShaderId);

		vertexArrayObjectId = GL30.glGenVertexArrays();
		vertexBufferObjectId = GL15.glGenBuffers();
		GL30.glBindVertexArray(vertexArrayObjectId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		loaded = true;

		// GL20.glGetAttribLocation(shaderProgram, "a_position") ?
	}

	private int createShader(int shaderType, String shaderSource) {
		int shaderId = GL20.glCreateShader(shaderType);
		GL20.glShaderSource(shaderId, shaderSource);
		GL20.glCompileShader(shaderId);
		int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
		if (compileStatus == GL11.GL_FALSE) {
			String errorText = GL20.glGetShaderInfoLog(shaderId, 1024);
			System.err.println(errorText);
			throw new IllegalStateException(compileStatus + ": " + errorText);
		}
		return shaderId;
	}

	public void glBegin(int type) {
		pipelineVertexBuffer.clear();
		this.glBeginType = type;
		vertexCount = 0;
		uvCount = 0;
		normalCount = 0;
		colorCount = 0;
		switch (type) {
			case GL11.GL_TRIANGLES:
				break;
			case GL11.GL_QUADS:
				break;
			case GL11.GL_LINES:
				break;
			default:
				throw new IllegalArgumentException(Integer.toString(type));
		}
	}

	private void pushFloat(int absoluteOffset, float x) {
		ensureCapacity(absoluteOffset);
		pipelineVertexBuffer.put(absoluteOffset, x);
	}

	private void ensureCapacity(int absoluteOffset) {
		if (pipelineVertexBuffer.capacity() <= absoluteOffset) {
			FloatBuffer largerBuffer = ByteBuffer
					.allocateDirect(Math.max((absoluteOffset + 1) * 4, pipelineVertexBuffer.capacity() * 2 * 4))
					.order(ByteOrder.nativeOrder())
					.asFloatBuffer()
					.clear();
			pipelineVertexBuffer.flip();
			largerBuffer.put(pipelineVertexBuffer);
			largerBuffer.clear();
			pipelineVertexBuffer = largerBuffer;
		}
	}

	public void glEnd() {
		GL30.glBindVertexArray(vertexArrayObjectId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);

		pipelineVertexBuffer.position(vertexCount * STRIDE);
		pipelineVertexBuffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_DYNAMIC_DRAW);

		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 4 * Float.BYTES);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, STRIDE_BYTES, 8 * Float.BYTES);
		GL20.glEnableVertexAttribArray(3);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 10 * Float.BYTES);

		GL20.glUseProgram(shaderProgram);

		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_texture"), 0);
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

	public void glPolygonMode(int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}

	public void glVertex3f(float x, float y, float z) {
		int baseOffset = vertexCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		tempVec4.set(x, y, z, 1);
		tempVec4.transform(currentMatrix);
		pushFloat(baseOffset + 0, tempVec4.x);
		pushFloat(baseOffset + 1, tempVec4.y);
		pushFloat(baseOffset + 2, tempVec4.z);
		pushFloat(baseOffset + 3, tempVec4.w);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		vertexCount++;
	}

	public void glColor4f(float r, float g, float b, float a) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(r, g, b, a);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glColor4f(float[] col) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(col);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glNormal3f(float x, float y, float z) {
		int baseOffset = normalCount * STRIDE;
		tempVec4.set(x, y, z, 0);
		tempVec4.transform(currentMatrix);
		tempVec4.normalize();
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 4, tempVec4.x);
		pushFloat(baseOffset + 5, tempVec4.y);
		pushFloat(baseOffset + 6, tempVec4.z);
		pushFloat(baseOffset + 7, 1);
		normalCount++;
	}

	public void glTexCoord2f(float u, float v) {
		int baseOffset = uvCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 8, u);
		pushFloat(baseOffset + 9, v);
		uvCount++;
	}

	public void glColor3f(float r, float g, float b) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(r, g, b, color.w);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		colorCount++;
	}

	public void glColor4ub(byte r, byte g, byte b, byte a) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glVertex3f(Vec3 pos) {
		int baseOffset = vertexCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		tempVec4.set(pos, 1);
		tempVec4.transform(currentMatrix);
		pushFloat(baseOffset + 0, tempVec4.x);
		pushFloat(baseOffset + 1, tempVec4.y);
		pushFloat(baseOffset + 2, tempVec4.z);
		pushFloat(baseOffset + 3, tempVec4.w);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		vertexCount++;
	}

	public void glVertex3f(Vec4 pos) {
		int baseOffset = vertexCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		tempVec4.set(pos);
		tempVec4.w = 1;
		tempVec4.transform(currentMatrix);
		pushFloat(baseOffset + 0, tempVec4.x);
		pushFloat(baseOffset + 1, tempVec4.y);
		pushFloat(baseOffset + 2, tempVec4.z);
		pushFloat(baseOffset + 3, tempVec4.w);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		vertexCount++;
	}

	public void glColor4f(Vec4 col) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(col);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glNormal3f(Vec3 norm) {
		int baseOffset = normalCount * STRIDE;
		tempVec4.set(norm, 0);
		tempVec4.transform(currentMatrix);
		tempVec4.normalize();
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 4, tempVec4.x);
		pushFloat(baseOffset + 5, tempVec4.y);
		pushFloat(baseOffset + 6, tempVec4.z);
		pushFloat(baseOffset + 7, 1);
		normalCount++;
	}

	public void glNormal3f(Vec4 norm) {
		int baseOffset = normalCount * STRIDE;
		tempVec4.set(norm);
		tempVec4.w = 0;
		tempVec4.transform(currentMatrix);
		tempVec4.normalize();
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 4, tempVec4.x);
		pushFloat(baseOffset + 5, tempVec4.y);
		pushFloat(baseOffset + 6, tempVec4.z);
		pushFloat(baseOffset + 7, 1);
		normalCount++;
	}

	public void glTexCoord2f(Vec2 uv) {
		int baseOffset = uvCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		pushFloat(baseOffset + 8, uv.x);
		pushFloat(baseOffset + 9, uv.y);
		uvCount++;
	}

	public void glColor3f(Vec3 col) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(col, color.w);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		colorCount++;
	}

	public void glLight(int light, int pname, FloatBuffer params) {

	}

	public void glRotatef(float angle, float axisX, float axisY, float axisZ) {
		tempVec3.set(axisX, axisY, axisZ);
		tempVec3.normalize();
		tempVec4.set(tempVec3.x, tempVec3.y, tempVec3.z, (float) Math.toRadians(angle));
		tempQuat.setFromAxisAngle(tempVec4);
		tempQuat.normalize();
		tempMat4.fromQuat(tempQuat);
//		Mat4.mul(currentMatrix, tempMat4, currentMatrix);
		currentMatrix.mul(tempMat4);
	}

	public void glCamera(ViewerCamera viewerCamera) {
		tempMat4.set(viewerCamera.viewProjectionMatrix).mul(currentMatrix);
		currentMatrix.set(tempMat4);

	}

	public void glScalef(float x, float y, float z) {
		tempMat4.setIdentity();
		tempVec3.set(x, y, z);
		tempMat4.scale(tempVec3);
//		Mat4.mul(currentMatrix, tempMat4, currentMatrix);
		currentMatrix.mul(tempMat4);
	}

	public void glTranslatef(float x, float y, float z) {
		tempMat4.setIdentity();
		tempVec3.set(x, y, z);
		tempMat4.translate(tempVec3);
//		Mat4.mul(currentMatrix, tempMat4, currentMatrix);
		currentMatrix.mul(tempMat4);
	}

	public void glOrtho(float xMin, float xMax, float yMin,
	                    float yMax, float zMin, float zMax) {
		currentMatrix.setOrtho(xMin, xMax, yMin, yMax, zMin, zMax);
	}

	public void gluPerspective(float fovY, float aspect, float nearClip, float farClip) {
		currentMatrix.setPerspective((float) Math.toRadians(fovY), aspect, nearClip, farClip);
		// When we are not using fixed function pipeline, notably Perspective cannot be
		// expressed as a matrix due to the math, so to emulate legacy behavior we will
		// set a flag and divide by negative Z factor later.
	}

	public void glLightModel(int lightModel, FloatBuffer ambientColor) {

	}

	public void glMatrixMode(int mode) {
		this.matrixMode = mode;

	}

	public void glLoadIdentity() {
		if (matrixMode == GL11.GL_PROJECTION) {
			currentMatrix.setIdentity();
		} // else if it is set to GL_MODELVIEW we should be in a different mode, but I was
		// lazy and only made 1 matrix and so we skip it....
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
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		textureUsed = 1;
	}

	public void onGlobalPipelineSet() {
		GL30.glBindVertexArray(vertexArrayObjectId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
	}

	public void glTangent4f(float x, float y, float z, float w) {
		// tangents are not applicable to old style drawing
	}

	public void glTangent4f(Vec4 vec4) {
		// tangents are not applicable to old style drawing
	}

	public void glActiveHDTexture(int textureUnit) {
		// TODO Auto-generated method stub

	}

	public void glViewport(int x, int y, int w, int h) {
		GL11.glViewport(x, y, w, h);
	}

	public void glFresnelColor3f(float r, float g, float b) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(r, g, b, 1f);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glFresnelColor3f(Vec3 fres) {
		int baseOffset = colorCount * STRIDE;
		ensureCapacity(baseOffset + STRIDE);
		color.set(fres, 1f);
		pushFloat(baseOffset + 10, color.x);
		pushFloat(baseOffset + 11, color.y);
		pushFloat(baseOffset + 12, color.z);
		pushFloat(baseOffset + 13, color.w);
		colorCount++;
	}

	public void glFresnelTeamColor1f(float v) {
	}

	public void glFresnelOpacity1f(float v) {
	}

	public void discard() {
		GL20.glDeleteProgram(shaderProgram);
	}

}