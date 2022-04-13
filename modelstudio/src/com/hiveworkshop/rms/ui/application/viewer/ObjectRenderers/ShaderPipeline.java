package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class ShaderPipeline {
	//https://www.khronos.org/files/opengles_shading_language.pdf
//	protected int STRIDE = 4 /* position */ + 4 /* normal */ + 2 /* uv */ + 4 /* color */;
//	protected int STRIDE_BYTES = STRIDE * Float.BYTES;
//	protected int STRIDE;
//	protected int STRIDE_BYTES = STRIDE * Float.BYTES;

	protected String vertexShader;
	protected String fragmentShader;
	protected String geometryShader;

	protected int matrixMode;
	protected final Vec4 color = new Vec4(1f, 1f, 1f, 1f);
	protected final Vec3 fresnelColor = new Vec3(0f, 0f, 0f);
	protected FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	protected final FloatBuffer pipelineMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

	protected int vertexCount = 0;
	protected int normalCount = 0;
	protected int uvCount = 0;
	protected int colorCount = 0;
	protected int tangentCount = 0;
	protected int fresnelColorCount = 0;
	protected int attributeArrayOffs = 0;
	protected int attributeArrayIndex = 0;

	protected int glBeginType;
	protected int shaderProgram;
	protected int vertexBufferObjectId;
	protected int vertexArrayObjectId; // has nothing to do with "object id" of war3 models
	protected boolean loaded = false;
	protected final Mat4 currentMatrix = new Mat4();
	protected int textureUsed = 0;
	protected int alphaTest = 0;
	protected int lightingEnabled = 1;

	protected final Vec3 tempVec3 = new Vec3();
	protected final Vec4 tempVec4 = new Vec4();
	protected final Vec4 position = new Vec4();
	protected final Vec4 tangent = new Vec4();
	protected final Vec3 normal = new Vec3();

	protected final Quat tempQuat = new Quat();
	protected final Mat4 tempMat4 = new Mat4();

	protected float fresnelTeamColor = 0f;
	protected float fresnelOpacity = 0f;

	protected int textureUnit;
	protected float viewportWidth;
	protected float viewportHeight;

	public ShaderPipeline(){
		currentMatrix.setIdentity();
	}

	protected void load() {
		shaderProgram = GL20.glCreateProgram();
		int vertexShaderId = -1;
		int fragmentShaderId = -1;
		int geometryShaderId = -1;
		if(vertexShader != null && !vertexShader.isEmpty()){
			vertexShaderId = createShader(GL20.GL_VERTEX_SHADER, vertexShader);
			GL20.glAttachShader(shaderProgram, vertexShaderId);
		}
		if(fragmentShader != null && !fragmentShader.isEmpty()){
			fragmentShaderId = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);
			GL20.glAttachShader(shaderProgram, fragmentShaderId);
		}
		if(geometryShader != null && !geometryShader.isEmpty()){
			geometryShaderId = createShader(GL32.GL_GEOMETRY_SHADER, geometryShader);
			GL20.glAttachShader(shaderProgram, geometryShaderId);
		}
		GL20.glLinkProgram(shaderProgram);
		int linkStatus = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS);
		if (linkStatus == GL11.GL_FALSE) {
			String errorText = GL20.glGetProgramInfoLog(shaderProgram, 1024);
			System.err.println(errorText);
			throw new IllegalStateException(linkStatus + ": " + errorText);
		}
		if(vertexShaderId != -1){
			GL20.glDeleteShader(vertexShaderId);
		}
		if(fragmentShaderId != -1){
			GL20.glDeleteShader(fragmentShaderId);
		}
		if(geometryShaderId != -1){
			GL20.glDeleteShader(geometryShaderId);
		}

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
//		System.out.println("glBegin");
		pipelineVertexBuffer.clear();
		glBeginType = type;
		vertexCount = 0;
		uvCount = 0;
		normalCount = 0;
		colorCount = 0;
		fresnelColorCount = 0;
		tangentCount = 0;
		attributeArrayOffs = 0;
		attributeArrayIndex = 0;
		switch (type) {
			case GL11.GL_TRIANGLES:
				break;
			case GL11.GL_QUADS:
				break;
			case GL11.GL_LINES:
				break;
			case GL11.GL_POINTS:
				break;
			default:
				throw new IllegalArgumentException(Integer.toString(type));
		}
	}


	protected void enableAttribArray(int size, int stride) {
		GL20.glEnableVertexAttribArray(attributeArrayIndex);
		GL20.glVertexAttribPointer(attributeArrayIndex, size, GL11.GL_FLOAT, false, stride * Float.BYTES, attributeArrayOffs * Float.BYTES);
		attributeArrayIndex++;
		attributeArrayOffs += size;
	}

	protected void pushFloat(int absoluteOffset, float x) {
		ensureCapacity(absoluteOffset);
		pipelineVertexBuffer.put(absoluteOffset, x);
	}

	protected void ensureCapacity(int absoluteOffset) {
		if (pipelineVertexBuffer.capacity() <= absoluteOffset) {
			int newSizeBytes = Math.max((absoluteOffset + 1) * 4, pipelineVertexBuffer.capacity() * 2 * 4);
			FloatBuffer largerBuffer = ByteBuffer
					.allocateDirect(newSizeBytes)
					.order(ByteOrder.nativeOrder())
					.asFloatBuffer()
					.clear();
			pipelineVertexBuffer.flip();
			largerBuffer.put(pipelineVertexBuffer);
			largerBuffer.clear();
			pipelineVertexBuffer = largerBuffer;
		}
	}

	public abstract void glEnd();


	public void glPolygonMode(int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}


	public void glLight(int light, int pname, FloatBuffer params) {
		GL11.glLight(light, pname, params);
	}

	public void glLightModel(int lightModel, FloatBuffer ambientColor) {
		GL11.glLightModel(lightModel, ambientColor);
	}

	public void glRotatef(float angle, float axisX, float axisY, float axisZ) {
//		tempVec3.set(axisX, axisY, axisZ);
//		tempVec3.normalize();
//		tempVec4.set(tempVec3.x, tempVec3.y, tempVec3.z, (float) Math.toRadians(angle));
//		tempQuat.setFromAxisAngle(tempVec4);
//		tempQuat.normalize();
//		tempMat4.fromQuat(tempQuat);
//		currentMatrix.mul(tempMat4);
	}

	public void glCamera(ViewerCamera viewerCamera) {
		Mat4 projectionMatrix = viewerCamera.getViewProjectionMatrix();
		glSetProjectionMatrix(projectionMatrix);
		glSetProjectionMatrix(viewerCamera.getViewProjectionMatrix());

	}

	public void glSetProjectionMatrix(Mat4 projectionMatrix) {
		currentMatrix.setIdentity();
		tempMat4.set(projectionMatrix).mul(currentMatrix);
		currentMatrix.set(tempMat4);
	}

	public void glScalef(float x, float y, float z) {
//		tempMat4.setIdentity();
//		tempVec3.set(x, y, z);
//		tempMat4.scale(tempVec3);
//		currentMatrix.mul(tempMat4);
	}

	public void glTranslatef(float x, float y, float z) {
//		tempMat4.setIdentity();
//		tempVec3.set(x, y, z);
//		tempMat4.translate(tempVec3);
//		currentMatrix.mul(tempMat4);
	}

	public void glOrtho(float xMin, float xMax, float yMin,
	                    float yMax, float zMin, float zMax) {
		currentMatrix.setOrtho(xMin, xMax, yMin, yMax, zMin, zMax);
	}

	public void gluPerspective(float fovY, float aspect, float nearClip, float farClip) {
//		currentMatrix.setPerspective((float) Math.toRadians(fovY), aspect, nearClip, farClip);
		// When we are not using fixed function pipeline, notably Perspective cannot be
		// expressed as a matrix due to the math, so to emulate legacy behavior we will
		// set a flag and divide by negative Z factor later.
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
	public abstract void glEnableIfNeeded(int glEnum);
	public abstract void glShadeModel(int mode);
	public abstract void glDisableIfNeeded(int glEnum);
	public abstract void prepareToBindTexture();

	public void onGlobalPipelineSet() {
		GL30.glBindVertexArray(vertexArrayObjectId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
	}

	public void glActiveHDTexture(int textureUnit) {
	}

	public void glViewport(int x, int y, int w, int h) {
		this.viewportWidth = w;
		this.viewportHeight = h;
		GL11.glViewport(x, y, w, h);
	}

	public abstract void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres);




	public void glFresnelColor3f(float r, float g, float b) {
	}

	public void glFresnelColor3f(Vec3 fres) {
	}

	public void glFresnelTeamColor1f(float v) {
	}

	public void glFresnelOpacity1f(float v) {
	}

	public void discard() {
		GL20.glDeleteProgram(shaderProgram);
	}
}
