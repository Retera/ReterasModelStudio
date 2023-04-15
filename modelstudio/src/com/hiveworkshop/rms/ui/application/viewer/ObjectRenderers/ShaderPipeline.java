package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ShaderPipeline {
	//https://www.khronos.org/files/opengles_shading_language.pdf
	protected static final int POSITION = 4;
	protected static final int NORMAL = 4;
	protected static final int TANGENT = 4;
	protected static final int UV = 2;
	protected static final int COLOR = 4;
	protected static final int FRESNEL_COLOR = 3;
	protected static final int ROTATION = 4;
	protected static final int SELECTION_STATUS = 1;
	protected static final int VEC2 = 2;
	protected static final int VEC3 = 3;
	protected static final int VEC4 = 4;
	protected static final int QUAT = 4;

	protected String vertexShader;
	protected String fragmentShader;
	protected String geometryShader;

	protected int matrixMode;
	protected final Vec4 color = new Vec4(1f, 1f, 1f, 1f);
	protected FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	protected final FloatBuffer pipelineMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	protected final FloatBuffer pipelineViewMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	protected final FloatBuffer uvTransformMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

	protected int vertexCount = 0;
	protected int attributeArrayOffs = 0;
	protected int attributeArrayIndex = 0;
	protected int currBufferOffset = 0;

	protected int glBeginType;
	protected int polygonMode;
	protected int shaderProgram;
	protected int glVertexBufferId;
	protected int glVertexArrayId;
	protected boolean loaded = false;

	protected final Mat4 currentMatrix = new Mat4();
	protected final Mat4 projectionMat = new Mat4();
	protected final Mat4 viewMat = new Mat4();
	protected int textureUsed = 0;
	protected int textureUnit;
	protected int alphaTest = 0;
	protected int lightingEnabled = 1;

	protected final Vec3 tempVec3 = new Vec3();
	protected final Vec4 tempVec4 = new Vec4();
	protected final Vec4 position = new Vec4();
	protected final Vec4 tangent = new Vec4();
	protected final Vec4 normal = new Vec4();

	protected final Quat tempQuat = new Quat();
	protected final Mat4 tempMat4 = new Mat4();

	protected float fresnelTeamColor = 0f;

	protected final Vec2 viewPortSize = new Vec2(1,1);

	protected Map<String, Integer> uniformMap = new HashMap<>();


	protected ArrayList<BufferSubInstance> instances = new ArrayList<>();
	protected BufferSubInstance currInstance;

	public ShaderPipeline(){
		currentMatrix.setIdentity();
	}

	protected void load() {
		createShaderProgram();

		glVertexArrayId = GL30.glGenVertexArrays();
		glVertexBufferId = GL15.glGenBuffers();
		GL30.glBindVertexArray(glVertexArrayId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
		loaded = true;
	}

	private void createShaderProgram() {
		shaderProgram = GL20.glCreateProgram();
		int vertexShaderId = createVertexShader();
		int fragmentShaderId = createFragmentShader();
		int geometryShaderId = createGeometryShader();
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
	}

	private int createVertexShader() {
		int vertexShaderId = -1;
		if(vertexShader != null && !vertexShader.isEmpty()){
			vertexShaderId = createShader(GL20.GL_VERTEX_SHADER, vertexShader);
			GL20.glAttachShader(shaderProgram, vertexShaderId);
		}
		return vertexShaderId;
	}

	private int createGeometryShader() {
		int geometryShaderId = -1;
		if(geometryShader != null && !geometryShader.isEmpty()){
			geometryShaderId = createShader(GL32.GL_GEOMETRY_SHADER, geometryShader);
			GL20.glAttachShader(shaderProgram, geometryShaderId);
		}
		return geometryShaderId;
	}

	private int createFragmentShader() {
		int fragmentShaderId = -1;
		if(fragmentShader != null && !fragmentShader.isEmpty()){
			fragmentShaderId = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);
			GL20.glAttachShader(shaderProgram, fragmentShaderId);
		}
		return fragmentShaderId;
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

	protected void createUniform(String uniformName){
		int uniformLocation = GL20.glGetUniformLocation(shaderProgram, uniformName);
		uniformMap.put(uniformName, uniformLocation);
//		if(uniformLocation != -1){
//			uniformMap.put(uniformName, uniformLocation);
//		}
	}

	protected Integer getUniformLocation(String uniformName){
		return uniformMap.get(uniformName);
	}

	protected void glUniform(String uniformName, int value){
		GL20.glUniform1i(getUniformLocation(uniformName), value);
	}
	protected void glUniform(String uniformName, float value){
		GL20.glUniform1f(getUniformLocation(uniformName), value);
	}
	protected void glUniform(String uniformName, float v0, float v1){
		GL20.glUniform2f(getUniformLocation(uniformName), v0, v1);
	}
	protected void glUniform(String uniformName, float v0, float v1, float v2){
		GL20.glUniform3f(getUniformLocation(uniformName), v0, v1, v2);
	}
	protected void glUniform(String uniformName, float v0, float v1, float v2, float v3){
		GL20.glUniform4f(getUniformLocation(uniformName), v0, v1, v2, v3);
	}
	protected void glUniform(String uniformName, Vec2 vec){
		GL20.glUniform2f(getUniformLocation(uniformName), vec.x, vec.y);
	}
	protected void glUniform(String uniformName, Vec3 vec){
		GL20.glUniform3f(getUniformLocation(uniformName), vec.x, vec.y, vec.z);
	}
	protected void glUniform(String uniformName, Vec4 vec){
		GL20.glUniform4f(getUniformLocation(uniformName), vec.x, vec.y, vec.z, vec.w);
	}


	// draw stuff
	public void doRender(int type){
		if (vertexCount > 0){
			attributeArrayOffs = 0;
			attributeArrayIndex = 0;
			glBeginType = type;
			switch (type) {
				case GL11.GL_TRIANGLES:
					break;
				case GL11.GL_TRIANGLE_STRIP:
					break;
				case GL11.GL_QUADS:
					break;
				case GL11.GL_LINE_STRIP:
					break;
				case GL11.GL_LINES:
					break;
				case GL11.GL_POINTS:
					break;
				default:
					throw new IllegalArgumentException(Integer.toString(type));
			}
			doRender();
		}
	}
	public void doRender(int type, CameraManager cameraManager){
		if (vertexCount > 0){
			attributeArrayOffs = 0;
			attributeArrayIndex = 0;
			glBeginType = type;
			switch (type) {
				case GL11.GL_TRIANGLES:
					break;
				case GL11.GL_TRIANGLE_STRIP:
					break;
				case GL11.GL_QUADS:
					break;
				case GL11.GL_LINE_STRIP:
					break;
				case GL11.GL_LINES:
					break;
				case GL11.GL_POINTS:
					break;
				default:
					throw new IllegalArgumentException(Integer.toString(type));
			}
			doRender(cameraManager);
		}
	}

	public ShaderPipeline setPolygonMode(int polygonMode) {
		this.polygonMode = polygonMode;
		return this;
	}

	public abstract void doRender();
	public void doRender(CameraManager cameraManager) {
	}

	// Prepare setup
	public void prepare() {
		pipelineVertexBuffer.clear();
		vertexCount = 0;
		attributeArrayOffs = 0;
		attributeArrayIndex = 0;
		instances.clear();
		currInstance = null;
		textureUnit = 0;
	}

	public void startInstance(BufferSubInstance instance){
		if(instance != null){
			instances.add(0, instance);
			instance.setOffset(vertexCount);
		}
		currInstance = instance;
	}
	public void overlappingInstance(BufferSubInstance instance){
		if(instance != null){
			instances.add(0, instance);
//			instance.setOffset(currInstance.getOffset());
//			instance.setVertCount(currInstance.getVertCount());
		}
	}
	public void endInstance(){
		if (currInstance != null) {
			currInstance.setEnd(vertexCount);
		}
		currInstance = null;
	}

	protected void fillMatrixBuffer(FloatBuffer buffer, Mat4 matrix) {
		buffer.clear();
		buffer.put(matrix.m00);
		buffer.put(matrix.m01);
		buffer.put(matrix.m02);
		buffer.put(matrix.m03);
		buffer.put(matrix.m10);
		buffer.put(matrix.m11);
		buffer.put(matrix.m12);
		buffer.put(matrix.m13);
		buffer.put(matrix.m20);
		buffer.put(matrix.m21);
		buffer.put(matrix.m22);
		buffer.put(matrix.m23);
		buffer.put(matrix.m30);
		buffer.put(matrix.m31);
		buffer.put(matrix.m32);
		buffer.put(matrix.m33);
		buffer.flip();
	}


	protected void enableAttribArray(int size, int stride) {
		GL20.glEnableVertexAttribArray(attributeArrayIndex);
		GL20.glVertexAttribPointer(attributeArrayIndex, size, GL11.GL_FLOAT, false, stride * Float.BYTES, (long) attributeArrayOffs * Float.BYTES);
		attributeArrayIndex++;
		attributeArrayOffs += size;
	}

	protected int prepareAddVertex(int stride){
		int baseOffset = vertexCount * stride;
		currBufferOffset = 0;
		ensureCapacity(baseOffset + stride);
		return baseOffset;
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

	protected void addToBuffer(int offset, Vec4 vec4){
		pipelineVertexBuffer.put(offset + currBufferOffset + 0, vec4.x);
		pipelineVertexBuffer.put(offset + currBufferOffset + 1, vec4.y);
		pipelineVertexBuffer.put(offset + currBufferOffset + 2, vec4.z);
		pipelineVertexBuffer.put(offset + currBufferOffset + 3, vec4.w);
		currBufferOffset +=4 ;
	}
	protected void addToBuffer(int offset, Vec3 vec3){
		pipelineVertexBuffer.put(offset + currBufferOffset + 0, vec3.x);
		pipelineVertexBuffer.put(offset + currBufferOffset + 1, vec3.y);
		pipelineVertexBuffer.put(offset + currBufferOffset + 2, vec3.z);
		currBufferOffset +=3 ;
	}
	protected void addToBuffer(int offset, Vec2 vec2){
		pipelineVertexBuffer.put(offset + currBufferOffset + 0, vec2.x);
		pipelineVertexBuffer.put(offset + currBufferOffset + 1, vec2.y);
		currBufferOffset +=2 ;
	}
	protected void addToBuffer(int offset, float i){
		pipelineVertexBuffer.put(offset + currBufferOffset + 0, i);
		currBufferOffset +=1 ;
	}
	protected void addToBuffer(int offset, int i){
		pipelineVertexBuffer.put(offset + currBufferOffset + 0, i);
		currBufferOffset +=1 ;
	}

	public void glPolygonMode(int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}

	public void glLight(int light, int pname, FloatBuffer params) {
		GL11.glLight(light, pname, params);
	}

	public void glLightModel(int lightModel, FloatBuffer ambientColor) {
		GL11.glLightModel(lightModel, ambientColor);
	}

	public void glSetViewProjectionMatrix(Mat4 projectionMatrix) {
		currentMatrix.setIdentity();
		tempMat4.set(projectionMatrix).mul(currentMatrix);
		currentMatrix.set(tempMat4);
	}

	public void glSetProjectionMatrix(Mat4 projectionMat) {
		this.projectionMat.set(projectionMat);
	}

	public void glSetViewMatrix(Mat4 viewMat) {
		this.viewMat.set(viewMat);
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
	public abstract void glDisableIfNeeded(int glEnum);
	public void onGlobalPipelineSet() {
		GL30.glBindVertexArray(glVertexArrayId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVertexBufferId);
	}

	public void prepareToBindTexture(){}

	public void glActiveHDTexture(int textureUnit) {
		this.textureUnit = textureUnit;
	}

	public void glViewport(int x, int y, int w, int h) {
		glViewport(w, h);
		GL11.glViewport(x, y, w, h);
	}
	public void glViewport(int w, int h) {
		viewPortSize.set(w, h);
	}

	public void setColor(Vec4 color){
		this.color.set(color);
	}

	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres){};
	public void addVert(Vec3 pos){};
	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, int selectionStatus) {};
	public void addVert(Vec3 pos, Vec3 norm, Vec4 tang, Vec2 uv, Vec4 col, Vec3 fres, float uniformScale) {};

	public void glFresnelTeamColor1f(float v) {
		this.fresnelTeamColor = v;
	}

	public void discard() {
		GL20.glDeleteProgram(shaderProgram);
	}
}
