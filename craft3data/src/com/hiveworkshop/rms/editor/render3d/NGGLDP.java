package com.hiveworkshop.rms.editor.render3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modelviewer.ViewerCamera;
import com.hiveworkshop.wc3.util.MathUtils;

/**
 * NGGLDP stands for "Not Good GL Design Practices". I am just trying to hack
 * this in for now based on the old legacy RMS code that was using sad, bad
 * fixed pipeline code (which can never render Reforged models accurately!)
 */
public class NGGLDP {

	static void uploadMatrix(final int shaderProgram, final String name, final Matrix4f matrix,
			final FloatBuffer buffer) {
		buffer.clear();
		buffer.put(matrix.m00).put(matrix.m01).put(matrix.m02).put(matrix.m03);
		buffer.put(matrix.m10).put(matrix.m11).put(matrix.m12).put(matrix.m13);
		buffer.put(matrix.m20).put(matrix.m21).put(matrix.m22).put(matrix.m23);
		buffer.put(matrix.m30).put(matrix.m31).put(matrix.m32).put(matrix.m33);
		buffer.flip();
		GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, name), false, buffer);
	}

	/** Uploads the scene lights as the u_light* uniform arrays shared by the shader pipelines. */
	static void uploadSceneLights(final int shaderProgram, final SceneLights lights, final FloatBuffer buffer) {
		final int mode = lights == null ? SceneLights.MODE_LEGACY : lights.mode;
		final int count = lights == null ? 0 : lights.count;
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightMode"), mode);
		GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightCount"), count);
		if ((mode == SceneLights.MODE_LEGACY) || (count == 0)) {
			return;
		}
		buffer.clear();
		for (int i = 0; i < SceneLights.MAX_LIGHTS; i++) {
			final boolean live = i < count;
			final boolean directional = live && (lights.type[i] == SceneLights.TYPE_DIRECTIONAL);
			final float[] source = directional ? lights.direction : lights.position;
			buffer.put(live ? source[i * 3] : 0).put(live ? source[(i * 3) + 1] : 0).put(live ? source[(i * 3) + 2] : 0)
					.put(live ? lights.type[i] : 0);
		}
		buffer.flip();
		GL20.glUniform4(GL20.glGetUniformLocation(shaderProgram, "u_lightPosType"), buffer);
		buffer.clear();
		for (int i = 0; i < SceneLights.MAX_LIGHTS; i++) {
			final boolean live = i < count;
			buffer.put(live ? lights.color[i * 3] : 0).put(live ? lights.color[(i * 3) + 1] : 0)
					.put(live ? lights.color[(i * 3) + 2] : 0).put(live ? lights.intensity[i] : 0);
		}
		buffer.flip();
		GL20.glUniform4(GL20.glGetUniformLocation(shaderProgram, "u_lightColorInt"), buffer);
		buffer.clear();
		for (int i = 0; i < SceneLights.MAX_LIGHTS; i++) {
			final boolean live = i < count;
			buffer.put(live ? lights.ambientColor[i * 3] : 0).put(live ? lights.ambientColor[(i * 3) + 1] : 0)
					.put(live ? lights.ambientColor[(i * 3) + 2] : 0).put(live ? lights.ambientIntensity[i] : 0);
		}
		buffer.flip();
		GL20.glUniform4(GL20.glGetUniformLocation(shaderProgram, "u_lightAmbColorInt"), buffer);
		buffer.clear();
		for (int i = 0; i < SceneLights.MAX_LIGHTS; i++) {
			final boolean live = i < count;
			buffer.put(live ? lights.attenuationStart[i] : 0).put(live ? lights.attenuationEnd[i] : 0).put(0).put(0);
		}
		buffer.flip();
		GL20.glUniform4(GL20.glGetUniformLocation(shaderProgram, "u_lightAtt"), buffer);
		buffer.clear();
		for (int i = 0; i < SceneLights.MAX_LIGHTS; i++) {
			final boolean live = i < count;
			buffer.put(live ? lights.quadraticFalloff[i] : 0).put(live ? lights.linearFalloff[i] : 0)
					.put(live ? lights.damping[i] : 0).put(live ? lights.shadowIntensity[i] : 0);
		}
		buffer.flip();
		GL20.glUniform4(GL20.glGetUniformLocation(shaderProgram, "u_lightFalloff"), buffer);
	}

	private static final FixedFunctionPipeline fixedFunctionPipeline = new FixedFunctionPipeline();

	public static Pipeline pipeline = null;

	public static void setPipeline(final Pipeline userPipeline) {
		pipeline = userPipeline;
		pipeline.onGlobalPipelineSet();
	}

	public static void fixedFunction() {
		pipeline = fixedFunctionPipeline;
	}

	public static final class ShaderSwitchingPipeline implements Pipeline {
		private final List<Pipeline> allShaderPipelines;
		private Pipeline currentPipeline;

		public ShaderSwitchingPipeline(final List<Pipeline> allShaderPipelines) {
			this.allShaderPipelines = allShaderPipelines;
		}

		@Override
		public void setCurrentPipeline(final int index) {
			currentPipeline = allShaderPipelines.get(index);
			currentPipeline.onGlobalPipelineSet();
		}

		@Override
		public int getCurrentPipelineIndex() {
			return allShaderPipelines.indexOf(currentPipeline);
		}

		@Override
		public void glBegin(final int type) {
			currentPipeline.glBegin(type);
		}

		@Override
		public void onGlobalPipelineSet() {
			if (currentPipeline != null) {
				currentPipeline.onGlobalPipelineSet();
			}
		}

		@Override
		public void glVertex3f(final float x, final float y, final float z) {
			currentPipeline.glVertex3f(x, y, z);
		}

		@Override
		public void glEnd() {
			currentPipeline.glEnd();
		}

		@Override
		public void glPolygonMode(final int face, final int mode) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glPolygonMode(face, mode);
			}
		}

		@Override
		public void glColor4f(final float r, final float g, final float b, final float a) {
			currentPipeline.glColor4f(r, g, b, a);
		}

		@Override
		public void glFresnelTeamColor1f(final float v) {
			currentPipeline.glFresnelTeamColor1f(v);
		}

		@Override
		public void glFresnelOpacity1f(final float v) {
			currentPipeline.glFresnelOpacity1f(v);
		}

		@Override
		public void glEmissiveGain1f(final float renderEmissiveGain) {
			currentPipeline.glEmissiveGain1f(renderEmissiveGain);
		}

		@Override
		public void glFresnelColor3f(final float r, final float g, final float b) {
			currentPipeline.glFresnelColor3f(r, g, b);
		}

		@Override
		public void glNormal3f(final float x, final float y, final float z) {
			currentPipeline.glNormal3f(x, y, z);
		}

		@Override
		public void glTexCoord2f(final float u, final float v) {
			currentPipeline.glTexCoord2f(u, v);
		}

		@Override
		public void glColor3f(final float r, final float g, final float b) {
			currentPipeline.glColor3f(r, g, b);
		}

		@Override
		public void glColor4ub(final byte r, final byte g, final byte b, final byte a) {
			currentPipeline.glColor4ub(r, g, b, a);
		}

		@Override
		public void glLight(final int light, final int pname, final FloatBuffer params) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glLight(light, pname, params);
			}
		}

		@Override
		public void glRotatef(final float angle, final float axisX, final float axisY, final float axisZ) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glRotatef(angle, axisX, axisY, axisZ);
			}
		}

		@Override
		public void glScalef(final float x, final float y, final float z) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glScalef(x, y, z);
			}
		}

		@Override
		public void glTranslatef(final float x, final float y, final float z) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glTranslatef(x, y, z);
			}
		}

		@Override
		public void glOrtho(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin,
				final float zMax) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glOrtho(xMin, xMax, yMin, yMax, zMin, zMax);
			}
		}

		@Override
		public void gluPerspective(final float fovY, final float aspect, final float nearClip, final float farClip) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.gluPerspective(fovY, aspect, nearClip, farClip);
			}
		}

		@Override
		public void glLightModel(final int lightModel, final FloatBuffer ambientColor) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glLightModel(lightModel, ambientColor);
			}
		}

		@Override
		public void glMatrixMode(final int mode) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glMatrixMode(mode);
			}
		}

		@Override
		public void glLoadIdentity() {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glLoadIdentity();
			}
		}

		@Override
		public void glEnableIfNeeded(final int glEnum) {
//			for (final Pipeline pipeline : allShaderPipelines) {
//				pipeline.onGlobalPipelineSet();
//				pipeline.glEnableIfNeeded(glEnum);
//			}
//			currentPipeline.onGlobalPipelineSet();
			currentPipeline.glEnableIfNeeded(glEnum);
		}

		@Override
		public void glShadeModel(final int glFlat) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glShadeModel(glFlat);
			}
		}

		@Override
		public void glDisableIfNeeded(final int glEnum) {
//			for (final Pipeline pipeline : allShaderPipelines) {
//				pipeline.onGlobalPipelineSet();
//				pipeline.glDisableIfNeeded(glEnum);
//			}
//			currentPipeline.onGlobalPipelineSet();
			currentPipeline.glDisableIfNeeded(glEnum);
		}

		@Override
		public void prepareToBindTexture() {
			currentPipeline.prepareToBindTexture();
		}

		@Override
		public void glTangent4f(final float x, final float y, final float z, final float w) {
			currentPipeline.glTangent4f(x, y, z, w);
		}

		@Override
		public void glActiveHDTexture(final int textureUnit) {
			currentPipeline.glActiveHDTexture(textureUnit);
		}

		@Override
		public void glViewport(final int x, final int y, final int w, final int h) {
			// NOTE maybe feels like this should apply to all, but currently as an
			// implementation detail we don't need the loop
			currentPipeline.glViewport(x, y, w, h);
		}

		@Override
		public void glCamera(final ViewerCamera viewerCamera, final boolean usingModelCamera) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glCamera(viewerCamera, usingModelCamera);
			}
		}

		@Override
		public void glSceneLights(final SceneLights lights) {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.glSceneLights(lights);
			}
		}

		@Override
		public void discard() {
			for (final Pipeline pipeline : allShaderPipelines) {
				pipeline.discard();
			}
		}

	}

	/**
	 * SimpleDiffuseShaderPipeline is only for classic SD models of Warcraft 3
	 */
	public static final class SimpleDiffuseShaderPipeline implements Pipeline {
		private static final int STRIDE = 4 /* position */ + 4 /* normal */ + 2 /* uv */ + 4 /* color */;
		private static final int STRIDE_BYTES = STRIDE * Float.BYTES;
		private static final String vertexShader = "#version 330 core\r\n" + //
				"\r\n" + //
				"layout (location = 0) in vec4 a_position;\r\n" + //
				"layout (location = 1) in vec4 a_normal;\r\n" + //
				"layout (location = 2) in vec2 a_uv;\r\n" + //
				"layout (location = 3) in vec4 a_color;\r\n" + //
				"\r\n" + //
				"out vec2 v_uv;\r\n" + //
				"out vec4 v_color;\r\n" + //
				"\r\n" + //
				"uniform mat4 u_projection;\r\n" + //
				"uniform vec3 u_lightDirection;\r\n" + //
				"uniform int u_lightingEnabled;\r\n" + //
				"uniform int u_usingModelCamera;\r\n" + //
				"uniform int u_lightMode;\r\n" + //
				"uniform int u_lightCount;\r\n" + //
				"uniform vec4 u_lightPosType[" + SceneLights.MAX_LIGHTS + "];\r\n" + //
				"uniform vec4 u_lightColorInt[" + SceneLights.MAX_LIGHTS + "];\r\n" + //
				"uniform vec4 u_lightAmbColorInt[" + SceneLights.MAX_LIGHTS + "];\r\n" + //
				"uniform vec4 u_lightAtt[" + SceneLights.MAX_LIGHTS + "];\r\n" + //
				"\r\n" + //
				// Warsmash's per-vertex light system: omni lights fall off with the
				// square of (distance / 64 + 1), directional lights are plain Lambert
				// plus their ambient term, ambient lights fall off with distance
				// between their attenuation start and end.
				"vec3 modelLightFactor(vec3 position, vec3 normal) {\r\n" + //
				"	vec3 lightFactor = vec3(0.0);\r\n" + //
				"	for (int i = 0; i < u_lightCount; i++) {\r\n" + //
				"		vec4 posType = u_lightPosType[i];\r\n" + //
				"		vec4 colorInt = u_lightColorInt[i];\r\n" + //
				"		vec4 ambColorInt = u_lightAmbColorInt[i];\r\n" + //
				"		vec2 att = u_lightAtt[i].xy;\r\n" + //
				"		if (posType.w > 1.5) {\r\n" + //
				"			float dist = length(position - posType.xyz);\r\n" + //
				"			if (dist <= att.y) {\r\n" + //
				"				float attenuationDist = clamp(dist - att.x, 0.001, att.y - att.x);\r\n" + //
				"				lightFactor += (1.0 / attenuationDist) * ambColorInt.a * ambColorInt.rgb;\r\n" + //
				"			}\r\n" + //
				"		} else if (posType.w > 0.5) {\r\n" + //
				"			vec3 contribution = colorInt.a * colorInt.rgb * clamp(dot(normal, posType.xyz), 0.0, 1.0);\r\n" + //
				"			lightFactor += clamp(contribution, 0.0, 1.0) + ambColorInt.a * ambColorInt.rgb;\r\n" + //
				"		} else {\r\n" + //
				"			vec3 delta = position - posType.xyz;\r\n" + //
				"			float dist = length(delta) / 64.0 + 1.0;\r\n" + //
				"			float falloff = 1.0 / (dist * dist);\r\n" + //
				"			vec3 contribution = (colorInt.a * falloff) * colorInt.rgb * clamp(dot(normal, normalize(-delta)), 0.0, 1.0);\r\n" + //
				"			lightFactor += clamp(contribution, 0.0, 1.0) + (ambColorInt.a * falloff) * ambColorInt.rgb;\r\n" + //
				"		}\r\n" + //
				"	}\r\n" + //
				"	return clamp(lightFactor, 0.0, 1.0);\r\n" + //
				"}\r\n" + //
				"\r\n" + //
				"void main() {\r\n" + //
				"		gl_Position = u_projection * a_position;\r\n" + //
				"		v_uv = a_uv;\r\n" + //
				"		v_color = a_color;\r\n" + //
				"		if(u_lightingEnabled != 0) {\r\n" + //
				"		    if(u_lightMode != 0) {\r\n" + //
				"			    v_color.rgb = v_color.rgb * modelLightFactor(a_position.xyz, normalize(a_normal.xyz));\r\n" + //
				"		    } else {\r\n" + //
				"			vec3 lightFactorContribution = vec3(clamp(dot(a_normal.xyz, u_lightDirection), 0.0, 1.0));\r\n"
				+ //
				"		    if(u_usingModelCamera != 0) {\r\n" + //
				"			    v_color.rgb = v_color.rgb * clamp(lightFactorContribution + 0.3f, 0.0, 1.0);\r\n" + //
				"		    } else {\r\n" + //
				"			    v_color.rgb = v_color.rgb * clamp(lightFactorContribution * 1.3 + vec3(0.5f, 0.5f, 0.5f), 0.0, 1.0);\r\n"
				+ //
				"		    }\r\n" + //
				"		    }\r\n" + //
				"		}\r\n" + //
				"}\r\n\0";
		private static final String fragmentShader = "#version 330 core\r\n" + //
				"\r\n" + //
				"uniform sampler2D u_texture;\r\n" + //
				"uniform int u_textureUsed;\r\n" + //
				"uniform int u_alphaTest;\r\n" + //
				"\r\n" + //
				"in vec2 v_uv;\r\n" + //
				"in vec4 v_color;\r\n" + //
				"\r\n" + //
				"out vec4 FragColor;\r\n" + //
				"\r\n" + //
				"void main() {\r\n" + // s
				"		vec4 color;\r\n" + //
				"		if(u_textureUsed != 0) {\r\n" + //
				"			vec4 texel = texture2D(u_texture, v_uv);\r\n" + //
				"			color = texel * v_color;\r\n" + //
				"		} else {\r\n" + //
				"			color = v_color;\r\n" + //
				"		}\r\n" + //
				"		if(u_alphaTest != 0 && color.a < 0.75) {\r\n" + //
				"			discard;\r\n" + //
				"		}\r\n" + //
				"		FragColor = color;\r\n" + //
				"}\r\n\0";
		private final Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
		private FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		private int vertexCount = 0;
		private int normalCount = 0;
		private int uvCount = 0;
		private int colorCount = 0;
		private int glBeginType;
		private int shaderProgram;
		private int vertexBufferObjectId, vertexArrayObjectId; // has nothing to do with "object id" of war3 models
		private boolean loaded = false;
		private final Matrix4f currentMatrix = new Matrix4f();
		{
			currentMatrix.setIdentity();
		}
		private int textureUsed = 0;
		private int alphaTest = 0;
		private int lightingEnabled = 1;

		public SimpleDiffuseShaderPipeline() {
			load();
		}

		private int createShader(final int shaderType, final String shaderSource) {
			final int shaderId = GL20.glCreateShader(shaderType);
			GL20.glShaderSource(shaderId, shaderSource);
			GL20.glCompileShader(shaderId);
			final int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
			if (compileStatus == GL11.GL_FALSE) {
				final String errorText = GL20.glGetShaderInfoLog(shaderId, 1024);
				System.err.println(errorText);
				throw new IllegalStateException(compileStatus + ": " + errorText);
			}
			return shaderId;
		}

		@Override
		public void glBegin(final int type) {
			pipelineVertexBuffer.clear();
			glBeginType = type;
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

		private void load() {
			final int vertexShaderId = createShader(GL20.GL_VERTEX_SHADER, vertexShader);
			final int fragmentShaderId = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);
			shaderProgram = GL20.glCreateProgram();
			GL20.glAttachShader(shaderProgram, vertexShaderId);
			GL20.glAttachShader(shaderProgram, fragmentShaderId);
			GL20.glLinkProgram(shaderProgram);
			final int linkStatus = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS);
			if (linkStatus == GL11.GL_FALSE) {
				final String errorText = GL20.glGetProgramInfoLog(shaderProgram, 1024);
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

		private void pushFloat(final int absoluteOffset, final float x) {
			ensureCapacity(absoluteOffset);
			pipelineVertexBuffer.put(absoluteOffset, x);
		}

		private void ensureCapacity(final int absoluteOffset) {
			if (pipelineVertexBuffer.capacity() <= absoluteOffset) {
				final FloatBuffer largerBuffer = ByteBuffer
						.allocateDirect(Math.max((absoluteOffset + 1) * 4, pipelineVertexBuffer.capacity() * 2 * 4))
						.order(ByteOrder.nativeOrder()).asFloatBuffer().clear();
				pipelineVertexBuffer.flip();
				largerBuffer.put(pipelineVertexBuffer);
				largerBuffer.clear();
				pipelineVertexBuffer = largerBuffer;
			}
		}

		@Override
		public void glVertex3f(final float x, final float y, final float z) {
			final int baseOffset = vertexCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			// world space; the shader applies the view-projection so lights can use the position
			pushFloat(baseOffset + 0, x);
			pushFloat(baseOffset + 1, y);
			pushFloat(baseOffset + 2, z);
			pushFloat(baseOffset + 3, 1);
			pushFloat(baseOffset + 10, color.x);
			pushFloat(baseOffset + 11, color.y);
			pushFloat(baseOffset + 12, color.z);
			pushFloat(baseOffset + 13, color.w);
			vertexCount++;
		}

		@Override
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
			if (usingModelCamera) {
				// this one emulates UI\MiscData.txt light
				// (used in WC3 portraits, so it'll be wrong on "main menu" background models)
				tempVec4.set(0.3f, -0.3f, 0.25f, 0.0f);
				GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_usingModelCamera"), 1);
			}
			else {
				// this one emulates DNC model light
				// (used in WC3 game world view)
				tempVec4.set(-24.1937f, 30.4879f, 444.411f, 0.0f);
				GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_usingModelCamera"), 0);
			}
//			Matrix4f.transform(currentMatrix, tempVec4, tempVec4);
			tempVec4.normalise();
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), tempVec4.x, tempVec4.y,
					tempVec4.z);
			uploadMatrix(shaderProgram, "u_projection", currentMatrix, pipelineMatrixBuffer);
			uploadSceneLights(shaderProgram, sceneLights, lightUploadBuffer);
			GL11.glDrawArrays(glBeginType, 0, vertexCount);
			vertexCount = 0;
			uvCount = 0;
			normalCount = 0;
			colorCount = 0;
			pipelineVertexBuffer.clear();
		}

		private final FloatBuffer pipelineMatrixBuffer = ByteBuffer.allocateDirect(16 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		private final FloatBuffer lightUploadBuffer = ByteBuffer.allocateDirect(SceneLights.MAX_LIGHTS * 4 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		@Override
		public void glPolygonMode(final int face, final int mode) {
			GL11.glPolygonMode(face, mode);
		}

		@Override
		public void glColor4f(final float r, final float g, final float b, final float a) {
			final int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, a);
			pushFloat(baseOffset + 10, color.x);
			pushFloat(baseOffset + 11, color.y);
			pushFloat(baseOffset + 12, color.z);
			pushFloat(baseOffset + 13, color.w);
			colorCount++;
		}

		@Override
		public void glNormal3f(final float x, final float y, final float z) {
			final int baseOffset = normalCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			tempVec4.set(x, y, z, 0);
			tempVec4.normalise();
			pushFloat(baseOffset + 4, tempVec4.x);
			pushFloat(baseOffset + 5, tempVec4.y);
			pushFloat(baseOffset + 6, tempVec4.z);
			pushFloat(baseOffset + 7, 0);
			normalCount++;
		}

		@Override
		public void glTexCoord2f(final float u, final float v) {
			final int baseOffset = uvCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 8, u);
			pushFloat(baseOffset + 9, v);
			uvCount++;
		}

		@Override
		public void glColor3f(final float r, final float g, final float b) {
			final int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, color.w);
			pushFloat(baseOffset + 10, color.x);
			pushFloat(baseOffset + 11, color.y);
			pushFloat(baseOffset + 12, color.z);
			colorCount++;
		}

		@Override
		public void glColor4ub(final byte r, final byte g, final byte b, final byte a) {
			final int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
			pushFloat(baseOffset + 10, color.x);
			pushFloat(baseOffset + 11, color.y);
			pushFloat(baseOffset + 12, color.z);
			pushFloat(baseOffset + 13, color.w);
			colorCount++;
		}

		@Override
		public void glLight(final int light, final int pname, final FloatBuffer params) {

		}

		private final Quaternion tempQuat = new Quaternion();
		private final Matrix4f tempMat4 = new Matrix4f();
		private boolean usingModelCamera;

		@Override
		public void glRotatef(final float angle, final float axisX, final float axisY, final float axisZ) {
			tempVec3.set(axisX, axisY, axisZ);
			tempVec3.normalise();
			tempVec4.set(tempVec3.x, tempVec3.y, tempVec3.z, (float) Math.toRadians(angle));
			tempQuat.setFromAxisAngle(tempVec4);
			tempQuat.normalise();
			MathUtils.fromQuat(tempQuat, tempMat4);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glCamera(final ViewerCamera viewerCamera, final boolean usingModelCamera) {
			this.usingModelCamera = usingModelCamera;
			Matrix4f.mul(viewerCamera.viewProjectionMatrix, currentMatrix, currentMatrix);
		}

		private final SceneLights sceneLights = new SceneLights();

		@Override
		public void glSceneLights(final SceneLights lights) {
			if (lights == null) {
				sceneLights.mode = SceneLights.MODE_LEGACY;
				sceneLights.count = 0;
			} else {
				sceneLights.copyFrom(lights);
			}
		}

		private final Vector3f tempVec3 = new Vector3f();
		private final Vector4f tempVec4 = new Vector4f();
		private int matrixMode;

		@Override
		public void glScalef(final float x, final float y, final float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.scale(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glTranslatef(final float x, final float y, final float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.translate(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glOrtho(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin,
				final float zMax) {
			MathUtils.setOrtho(currentMatrix, xMin, xMax, yMin, yMax, zMin, zMax);
		}

		@Override
		public void gluPerspective(final float fovY, final float aspect, final float nearClip, final float farClip) {
			MathUtils.setPerspective(currentMatrix, (float) Math.toRadians(fovY), aspect, nearClip, farClip);
			// When we are not using fixed function pipeline, notably Perspective cannot be
			// expressed as a matrix due to the math, so to emulate legacy behavior we will
			// set a flag and divide by negative Z factor later.
		}

		@Override
		public void glLightModel(final int lightModel, final FloatBuffer ambientColor) {

		}

		@Override
		public void glMatrixMode(final int mode) {
			matrixMode = mode;

		}

		@Override
		public void glLoadIdentity() {
			if (matrixMode == GL11.GL_PROJECTION) {
				currentMatrix.setIdentity();
			} // else if it is set to GL_MODELVIEW we should be in a different mode, but I was
				// lazy and only made 1 matrix and so we skip it....
		}

		@Override
		public void glEnableIfNeeded(final int glEnum) {
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

		@Override
		public void glShadeModel(final int mode) {
		}

		@Override
		public void glDisableIfNeeded(final int glEnum) {
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

		@Override
		public void prepareToBindTexture() {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			textureUsed = 1;
		}

		@Override
		public void onGlobalPipelineSet() {
			GL30.glBindVertexArray(vertexArrayObjectId);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		}

		@Override
		public void glTangent4f(final float x, final float y, final float z, final float w) {
			// tangents are not applicable to old style drawing
		}

		@Override
		public void glActiveHDTexture(final int textureUnit) {
			// TODO Auto-generated method stub

		}

		@Override
		public void glViewport(final int x, final int y, final int w, final int h) {
			GL11.glViewport(x, y, w, h);
		}

		@Override
		public void glFresnelColor3f(final float r, final float g, final float b) {
		}

		@Override
		public void glFresnelTeamColor1f(final float v) {
		}

		@Override
		public void glFresnelOpacity1f(final float v) {
		}

		@Override
		public void discard() {
			GL20.glDeleteProgram(shaderProgram);
		}

		@Override
		public void setCurrentPipeline(final int pipelineId) {
		}

		@Override
		public int getCurrentPipelineIndex() {
			return 0;
		}

		@Override
		public void glEmissiveGain1f(final float renderEmissiveGain) {
		}

	}

	/**
	 * HDDiffuseShaderPipeline is only for classic HD models of Reforged
	 */
	public static final class HDDiffuseShaderPipeline implements Pipeline {
		private static final int STRIDE = 4 /* position */ + 4 /* normal */ + 4 /* tangent */ + 2 /* uv */
				+ 4 /* color */ ;
		private static final int STRIDE_BYTES = STRIDE * Float.BYTES;
		private static final String vertexShader = """
				#version 330 core

				layout (location = 0) in vec4 a_position;
				layout (location = 1) in vec4 a_normal;
				layout (location = 2) in vec4 a_tangent;
				layout (location = 3) in vec2 a_uv;
				layout (location = 4) in vec4 a_color;

				uniform vec3 u_lightDirection;
				uniform vec3 u_viewPos;
				uniform mat4 u_projection;

				out vec2 v_uv;
				out vec4 v_color;
				out vec3 v_tangentLightPos;
				out vec3 v_tangentViewPos;
				out vec3 v_tangentFragPos;
				out vec3 v_worldPos;
				out vec3 v_worldNormal;
				out vec4 v_worldTangent;

				void main() {
					gl_Position = u_projection * a_position;
					v_uv = a_uv;
					v_color = a_color;
					v_worldPos = a_position.xyz;
					v_worldNormal = a_normal.xyz;
					v_worldTangent = a_tangent;
					vec3 tangent = normalize(a_tangent.xyz);
					vec3 mormal = normalize(a_normal.xyz);
					tangent = normalize(tangent - dot(tangent, mormal.xyz) * mormal.xyz);
					vec3 binormal = normalize(cross(mormal.xyz, tangent) * a_tangent.w);
					mat3 TBN = transpose(mat3(tangent, binormal, mormal.xyz));
					v_tangentLightPos = TBN * normalize(u_lightDirection).xyz;
					v_tangentViewPos = TBN * u_viewPos;
					v_tangentFragPos = TBN * (a_position).xyz;
				}
				""";
		/**
		 * The lit branch for {@code u_lightMode == 1} is a port of Warcraft III
		 * Reforged 3.0's HD mesh pixel shader as reconstructed in FernandoS27's
		 * Wc3Shaders (BSD-3-Clause, see licenses/): the hue-preserving team layer,
		 * GGX/Schlick specular with the shipped clamps and roughness remap, the
		 * main light with the split ambient/probe mix, clustered point lights with
		 * the exp-damped rational falloff, and the fresnel rim. The editor has no
		 * cube-map probe, so the environment-map texture stands in for it.
		 */
		private static final String fragmentShader = """
				#version 330 core

				uniform sampler2D u_textureDiffuse;
				uniform sampler2D u_textureNormal;
				uniform sampler2D u_textureORM;
				uniform sampler2D u_textureEmissive;
				uniform sampler2D u_textureTeamColor;
				uniform sampler2D u_textureReflections;
				uniform int u_textureUsed;
				uniform int u_alphaTest;
				uniform int u_lightingEnabled;
				uniform float u_fresnelTeamColor;
				uniform float u_emissiveGain;
				uniform vec4 u_fresnelColor;
				uniform vec2 u_viewportSize;
				uniform vec3 u_viewPos;
				uniform vec3 u_viewRight;
				uniform vec3 u_viewUp;
				uniform vec3 u_viewBack;
				uniform int u_lightMode;
				uniform int u_lightCount;
				uniform vec4 u_lightPosType[MAX_LIGHTS];
				uniform vec4 u_lightColorInt[MAX_LIGHTS];
				uniform vec4 u_lightAmbColorInt[MAX_LIGHTS];
				uniform vec4 u_lightAtt[MAX_LIGHTS];
				uniform vec4 u_lightFalloff[MAX_LIGHTS];
				uniform int u_mainLightEnabled;
				uniform vec3 u_mainLightDir;
				uniform vec3 u_mainLightColor;
				uniform vec3 u_mainAmbient;
				uniform float u_iblScale;

				in vec2 v_uv;
				in vec4 v_color;
				in vec3 v_tangentLightPos;
				in vec3 v_tangentViewPos;
				in vec3 v_tangentFragPos;
				in vec3 v_worldPos;
				in vec3 v_worldNormal;
				in vec4 v_worldTangent;

				out vec4 FragColor;

				const float PI = 3.14159265358979;
				const float INV_PI = 0.318309886183791;
				const float SAFE_EPS = 0.0001;
				const float HD_GGX_MIN_DENOM = 1.00000001e-07;
				const float HD_ROUGHNESS_SCALE = 0.899999976;
				const float HD_ROUGHNESS_BIAS = 0.0500000007;
				const float HD_IBL_SPLIT_UPPER = 0.675000012;
				const float HD_IBL_SPLIT_LOWER = 0.324999988;
				const float HD_ML_BLEND_SMOOTH = 0.949999988;
				const float HD_DBG_MIN_RADIANCE = 0.00392156886;
				const vec3 HD_FRESNEL_TARGET = vec3(0.0, 0.0, 0.999000967);

				vec3 srgbToLinear(vec3 c) {
					return mix(c / 12.92, pow((c + 0.055) / 1.055, vec3(2.4)), step(vec3(0.04045), c));
				}

				vec3 linearToSrgb(vec3 c) {
					c = max(c, vec3(0.0));
					return mix(c * 12.92, 1.055 * pow(c, vec3(1.0 / 2.4)) - 0.055, step(vec3(0.0031308), c));
				}

				// EnvSet truncates each light colour channel to a byte; no sRGB anywhere on the light path.
				vec3 engineLightColor(vec3 rgb) {
					return floor(clamp(rgb, 0.0, 1.0) * 255.0) / 255.0;
				}

				struct HDSurface {
					vec3 N;
					vec3 V;
					vec3 albedo;
					float metal;
					vec3 F0;
					vec3 diffColor;
					float rough;
					float a2;
					float kDirect;
					float kInv;
				};

				HDSurface hdBuildSurface(vec3 N, vec3 V, vec3 albedo, vec2 roughMetal) {
					HDSurface s;
					float roughness = clamp(roughMetal.x, 0.0, 1.0) * HD_ROUGHNESS_SCALE + HD_ROUGHNESS_BIAS;
					s.N = N;
					s.V = V;
					s.albedo = albedo;
					s.metal = roughMetal.y;
					s.rough = roughness;
					s.F0 = s.metal * s.albedo;
					s.diffColor = (s.albedo - s.metal * s.albedo) * INV_PI;
					float rr = s.rough * s.rough;
					s.a2 = rr * rr;
					float kSq = (1.0 + s.rough) * (1.0 + s.rough);
					s.kDirect = 0.125 * kSq;
					s.kInv = 1.0 - kSq * 0.125;
					return s;
				}

				float hdGGX(float ndh, float a2) {
					float dterm = ndh * ndh * a2 + (-ndh * ndh + 1.0);
					dterm = max(HD_GGX_MIN_DENOM, dterm);
					dterm = PI * (dterm * dterm);
					return a2 / dterm;
				}

				float hdVisibility(HDSurface s, float ndl, float ndv) {
					return clamp(1.0 / (4.0 * ((ndl * s.kInv + s.kDirect) * (ndv * s.kInv + s.kDirect))), 0.0, 1.0);
				}

				vec3 hdFresnel(HDSurface s, float vdh) {
					float t = 1.0 - vdh;
					float t5 = (t * t) * (t * t) * t;
					return (s.metal * -s.albedo + vec3(1.0)) * t5 + s.F0;
				}

				// Sphere-map lookup into the environment texture, in view space.
				vec3 envSample(vec3 worldDir) {
					vec3 v = vec3(dot(worldDir, u_viewRight), dot(worldDir, u_viewUp), dot(worldDir, u_viewBack));
					vec2 uv = vec2(0.5 + 0.5 * v.x, 0.5 - 0.5 * v.y);
					return srgbToLinear(texture(u_textureReflections, uv).rgb);
				}

				// Karis' analytic fit of the split-sum BRDF lookup (scale, bias).
				vec2 envBrdfApprox(float rough, float ndv) {
					vec4 c0 = vec4(-1.0, -0.0275, -0.572, 0.022);
					vec4 c1 = vec4(1.0, 0.0425, 1.04, -0.04);
					vec4 r = rough * c0 + c1;
					float a004 = min(r.x * r.x, exp2(-9.28 * ndv)) * r.x + r.y;
					return vec2(-1.04, 1.04) * a004 + r.zw;
				}

				// The main light, the probe and the ambient mix, in the shipped order of operations.
				vec3 hdMainLight(HDSurface s) {
					vec3 L = u_mainLightDir;
					vec3 H = normalize(s.V + L);
					float vdh = dot(H, s.V);
					float ndh = dot(s.N, H);
					float ndl = dot(s.N, L);
					float ndv = dot(s.N, s.V);
					vec3 directSpec = hdFresnel(s, vdh) * hdGGX(ndh, s.a2);
					directSpec = directSpec * hdVisibility(s, ndl, ndv);
					float shadowedNdl = max(0.0, ndl);
					// the probe stand-in: irradiance from the normal, radiance from the reflection
					vec3 irradiance = envSample(s.N);
					vec3 R = normalize(s.N * -(dot(-s.V, s.N) + dot(-s.V, s.N)) + -s.V);
					vec3 radiance = mix(envSample(R), irradiance, s.rough);
					vec2 brdf = envBrdfApprox(s.rough, max(ndv, 0.0));
					float weight = 1.0;
					vec3 lightColor = u_mainLightColor;
					float maxAmb = max(lightColor.x, max(lightColor.y, lightColor.z));
					float invAmb = 1.0 / (SAFE_EPS + maxAmb);
					float ambPlusIbl = maxAmb + weight;
					float iblScale = u_iblScale * (ambPlusIbl * weight);
					float maxIrr = max(irradiance.x, max(irradiance.y, irradiance.z));
					vec3 irrN = irradiance * (1.0 / (SAFE_EPS + maxIrr));
					vec3 ambDelta = lightColor * invAmb - irrN;
					vec3 upper = ambDelta * HD_IBL_SPLIT_UPPER + irrN;
					upper = upper * maxIrr;
					upper = upper * ambPlusIbl - lightColor;
					upper = weight * upper + lightColor;
					vec3 lower = ambDelta * HD_IBL_SPLIT_LOWER + irrN;
					lower = lower * maxIrr;
					vec3 lowerScaled = lower * iblScale;
					vec3 ambient = -lower * iblScale + upper;
					ambient = shadowedNdl * ambient + lowerScaled;
					ambient = u_mainAmbient + ambient;
					directSpec = directSpec * ambPlusIbl;
					directSpec = directSpec * shadowedNdl;
					vec3 iblSpec = (s.F0 * brdf.x + brdf.y) * iblScale;
					vec3 lit = directSpec * lightColor + iblSpec;
					lit = lit * radiance;
					return ambient * s.diffColor + lit;
				}

				// Every omni light of the model, with the 3.0 exp-damped rational falloff.
				vec3 hdPointLights(HDSurface s, vec3 worldPos, vec3 lit) {
					float ndv = dot(s.N, s.V);
					float gvLoop = ndv * s.kInv + s.kDirect;
					for (int i = 0; i < u_lightCount; i++) {
						vec4 posType = u_lightPosType[i];
						if (posType.w > 0.5) {
							continue;
						}
						vec4 falloffParams = u_lightFalloff[i];
						vec3 lightColor = engineLightColor(u_lightColorInt[i].rgb) * u_lightColorInt[i].a;
						vec3 toLight = posType.xyz - worldPos;
						float distSq = dot(toLight, toLight);
						float dist = sqrt(distSq);
						float falloff = exp(-falloffParams.z * distSq);
						float denom = dot(vec3(dist, 1.0, distSq), vec3(falloffParams.y, 1.0, falloffParams.x));
						float atten = falloff / denom;
						vec3 lightRadiance = lightColor * atten;
						float mag = sqrt(dot(lightRadiance, lightRadiance));
						if (mag < HD_DBG_MIN_RADIANCE) {
							continue;
						}
						vec3 Ldir = toLight / dist;
						vec3 H = normalize(s.V + Ldir);
						float vdh = dot(H, s.V);
						float ndh = dot(s.N, H);
						float ndl = dot(s.N, Ldir);
						vec3 spec = hdFresnel(s, vdh) * hdGGX(ndh, s.a2);
						float G = clamp(1.0 / (4.0 * ((ndl * s.kInv + s.kDirect) * gvLoop)), 0.0, 1.0);
						float weight = atten * max(0.0, ndl);
						vec3 contrib = spec * G + s.diffColor;
						contrib = contrib * lightColor;
						lit = contrib * weight + lit;
					}
					return lit;
				}

				vec3 decodeNormal(vec2 uv) {
					vec3 normalXYZ = texture(u_textureNormal, uv).xyz;
					vec2 normalXY = normalXYZ.yx * 2.0 - 1.0;
					return vec3(normalXY, sqrt(max(0.0, 1.0 - dot(normalXY, normalXY))));
				}

				void mainReforged() {
					vec4 texel = texture(u_textureDiffuse, v_uv);
					vec4 ormTexel = texture(u_textureORM, v_uv);
					vec3 albedo = srgbToLinear(texel.rgb);
					vec3 emissive = srgbToLinear(texture(u_textureEmissive, v_uv).rgb);
					// the hue-preserving team layer
					float teamWeight = ormTexel.a;
					vec3 teamRGB = srgbToLinear(texture(u_textureTeamColor, v_uv).rgb);
					float maxTeam = max(max(teamRGB.y, teamRGB.z), teamRGB.x);
					float invTeam = 1.0 / (SAFE_EPS + maxTeam);
					if (teamWeight > 0.0) {
						float sqrtW = sqrt(teamWeight);
						float smoothW = HD_ML_BLEND_SMOOTH * (teamWeight * teamWeight);
						float maxBase = max(max(albedo.y, albedo.z), albedo.x);
						vec3 baseN = albedo * (1.0 / (SAFE_EPS + maxBase));
						albedo = sqrtW * (teamRGB * invTeam - baseN) + baseN;
						float lum = smoothW * (min(maxTeam, maxBase) - maxBase) + maxBase;
						albedo = albedo * lum;
						emissive = teamWeight * (emissive * teamRGB - emissive) + emissive;
					}
					if (v_color.a == 1.0 && u_alphaTest != 0 && texel.a < 0.75) {
						discard;
					}
					if (texel.a * v_color.a == 0.0) {
						discard;
					}
					// shading basis in world space
					vec3 geoN = normalize(v_worldNormal);
					vec3 T = normalize(v_worldTangent.xyz);
					T = normalize(T - dot(T, geoN) * geoN);
					vec3 B = normalize(cross(geoN, T) * v_worldTangent.w);
					vec3 tsN = decodeNormal(v_uv);
					vec3 N = normalize(T * tsN.x + B * tsN.y + geoN * tsN.z);
					vec3 V = normalize(u_viewPos - v_worldPos);
					if (dot(geoN, V) < 0.0) {
						N = -N;
					}
					vec3 vertColor = srgbToLinear(v_color.rgb);
					vec3 shadedRGB;
					float ndv;
					if (u_lightingEnabled == 0) {
						shadedRGB = emissive * u_emissiveGain + albedo;
						shadedRGB = vertColor * shadedRGB;
						ndv = clamp(dot(V, N), 0.0, 1.0);
					} else {
						HDSurface surf = hdBuildSurface(N, V, albedo, ormTexel.gb);
						vec3 lit = vec3(0.0);
						if (u_mainLightEnabled != 0) {
							lit = hdMainLight(surf);
						}
						lit = hdPointLights(surf, v_worldPos, lit);
						lit = lit * ormTexel.r;
						shadedRGB = PI * lit;
						shadedRGB = emissive * u_emissiveGain + shadedRGB;
						shadedRGB = vertColor * shadedRGB;
						ndv = clamp(dot(N, V), 0.0, 1.0);
					}
					// fresnel rim
					float rim = 1.0 - ndv;
					float rim2 = rim * rim;
					float fresnelA = u_fresnelColor.w * rim2;
					float teamF2 = u_fresnelTeamColor * u_fresnelTeamColor;
					vec3 fresnelColorLinear = srgbToLinear(u_fresnelColor.rgb);
					float maxF = max(fresnelColorLinear.x, max(fresnelColorLinear.y, fresnelColorLinear.z));
					float invF = 1.0 / (SAFE_EPS + maxF);
					vec3 fresnelN = fresnelColorLinear * invF;
					vec3 fresnelTarget = (u_fresnelTeamColor > 0.0) ? teamRGB * invTeam : HD_FRESNEL_TARGET;
					float fresnelClamp = (u_fresnelTeamColor > 0.0) ? min(maxF, maxTeam) : min(1.0, maxF);
					vec3 fresnelCol = u_fresnelTeamColor * (fresnelTarget - fresnelN) + fresnelN;
					float fresnelInt = teamF2 * (fresnelClamp - maxF) + maxF;
					vec3 plainRGB = fresnelA * (fresnelCol * fresnelInt - shadedRGB) + shadedRGB;
					float plainAlpha = clamp(v_color.a * texel.a + fresnelA * texel.a, 0.0, 1.0);
					FragColor = vec4(linearToSrgb(plainRGB), plainAlpha);
				}

				void main() {
					if (u_lightMode != 0 && u_textureUsed != 0) {
						mainReforged();
						return;
					}
					vec4 color;
					vec4 ormTexel = texture(u_textureORM, v_uv);
					vec4 teamColorTexel = texture(u_textureTeamColor, v_uv);
					if(u_textureUsed != 0) {
						vec4 texel = texture(u_textureDiffuse, v_uv);
						color = vec4(texel.rgb * ((1.0 - ormTexel.a) + (teamColorTexel.rgb * ormTexel.a)), texel.a) * v_color;
					} else {
						color = v_color;
					}
					if(v_color.a == 1.0 && u_alphaTest != 0 && color.a < 0.75) {
						discard;
					}
					if(color.a == 0.0) {
						discard;
					}
					if(u_lightingEnabled != 0) {
						vec3 normalXYZ = texture(u_textureNormal, v_uv).xyz;
						vec2 normalXY = normalXYZ.yx * 2.0 - 1.0;
						vec3 normal = vec3(normalXY, sqrt(1.0 - dot(normalXY,normalXY)));
						vec4 emissiveTexel = texture(u_textureEmissive, v_uv);
						vec4 reflectionsTexel = clamp(0.2+2.0*texture(u_textureReflections, vec2(gl_FragCoord.x/u_viewportSize.x, -gl_FragCoord.y/u_viewportSize.y)), 0.0, 1.0);
						vec3 lightDir = normalize(v_tangentLightPos);
						float cosTheta = dot(lightDir, normal) * 0.5 + 0.5;
						float lambertFactor = clamp(cosTheta, 0.0, 1.0);
						vec3 diffuse = (clamp(lambertFactor, 0.0, 1.0)) * color.xyz;
						vec3 viewDir = normalize(v_tangentViewPos - v_tangentFragPos);
						vec3 reflectDir = reflect(-lightDir, normal);
						vec3 halfwayDir = normalize(lightDir + viewDir);
						float spec = pow(max(dot(normal, halfwayDir)*0.5 + 0.5, 0.0), 32.0);
						vec3 specular = vec3(max(-ormTexel.g+0.5, 0.0)+ormTexel.b) * spec * (reflectionsTexel.xyz * (1.0 - ormTexel.g) + ormTexel.g * color.xyz);
						vec3 fresnelColor = vec3(u_fresnelColor.rgb * (1.0 - u_fresnelTeamColor) + teamColorTexel.rgb *  u_fresnelTeamColor) * v_color.rgb;
						vec3 fresnel = fresnelColor*pow(1.0 - dot(normalize(v_tangentViewPos), normal), 1.0)*u_fresnelColor.a;
						FragColor = vec4(emissiveTexel.xyz * sqrt(u_emissiveGain) + specular + diffuse + fresnel, color.a);
					} else {
						FragColor = color;
					}
				}
				""".replace("MAX_LIGHTS", Integer.toString(SceneLights.MAX_LIGHTS));
		private final Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
		private final Vector3f fresnelColor = new Vector3f(0f, 0f, 0f);
		private FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		private final FloatBuffer pipelineMatrixBuffer = ByteBuffer.allocateDirect(16 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		private int vertexCount = 0;
		private int normalCount = 0;
		private int tangentCount = 0;
		private int uvCount = 0;
		private int colorCount = 0;
		private int fresnelColorCount = 0;
		private int glBeginType;
		private int shaderProgram;
		private int vertexBufferObjectId, vertexArrayObjectId; // has nothing to do with "object id" of war3 models
		private boolean loaded = false;
		private final Matrix4f currentMatrix = new Matrix4f();
		{
			currentMatrix.setIdentity();
		}
		private int textureUsed = 0;
		private int alphaTest = 0;
		private int lightingEnabled = 1;
		private float fresnelTeamColor;
		private float fresnelOpacity;
		private float renderEmissiveGain;
		private final FloatBuffer lightUploadBuffer = ByteBuffer.allocateDirect(SceneLights.MAX_LIGHTS * 4 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		/** WhiteoutFlakes' baseline sun when no day/night rig is loaded: toward (1, 0.35, 1.2). */
		private static final Vector3f BASELINE_SUN_DIR = new Vector3f(1f, 0.35f, 1.2f);
		static {
			BASELINE_SUN_DIR.normalise();
		}

		public HDDiffuseShaderPipeline() {
			load();
		}

		private int createShader(final int shaderType, final String shaderSource) {
			final int shaderId = GL20.glCreateShader(shaderType);
			GL20.glShaderSource(shaderId, shaderSource);
			GL20.glCompileShader(shaderId);
			final int compileStatus = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
			if (compileStatus == GL11.GL_FALSE) {
				final String errorText = GL20.glGetShaderInfoLog(shaderId, 1024);
				System.err.println(errorText);
				throw new IllegalStateException(compileStatus + ": " + errorText);
			}
			return shaderId;
		}

		@Override
		public void glBegin(final int type) {
			pipelineVertexBuffer.clear();
			glBeginType = type;
			vertexCount = 0;
			uvCount = 0;
			normalCount = 0;
			colorCount = 0;
			fresnelColorCount = 0;
			tangentCount = 0;
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

		private void load() {
			final int vertexShaderId = createShader(GL20.GL_VERTEX_SHADER, vertexShader);
			final int fragmentShaderId = createShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);
			shaderProgram = GL20.glCreateProgram();
			GL20.glAttachShader(shaderProgram, vertexShaderId);
			GL20.glAttachShader(shaderProgram, fragmentShaderId);
			GL20.glLinkProgram(shaderProgram);
			final int linkStatus = GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS);
			if (linkStatus == GL11.GL_FALSE) {
				final String errorText = GL20.glGetProgramInfoLog(shaderProgram, 1024);
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

		private void pushFloat(final int absoluteOffset, final float x) {
			ensureCapacity(absoluteOffset);
			pipelineVertexBuffer.put(absoluteOffset, x);
		}

		private void ensureCapacity(final int absoluteOffset) {
			if (pipelineVertexBuffer.capacity() <= absoluteOffset) {
				final int newSizeBytes = Math.max((absoluteOffset + 1) * 4, pipelineVertexBuffer.capacity() * 2 * 4);
				final FloatBuffer largerBuffer = ByteBuffer.allocateDirect(newSizeBytes).order(ByteOrder.nativeOrder())
						.asFloatBuffer().clear();
				pipelineVertexBuffer.flip();
				largerBuffer.put(pipelineVertexBuffer);
				largerBuffer.clear();
				pipelineVertexBuffer = largerBuffer;
			}
		}

		@Override
		public void glVertex3f(final float x, final float y, final float z) {
			final int baseOffset = vertexCount * STRIDE;
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

		@Override
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
			GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 8 * Float.BYTES);
			GL20.glEnableVertexAttribArray(3);
			GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, false, STRIDE_BYTES, 12 * Float.BYTES);
			GL20.glEnableVertexAttribArray(4);
			GL20.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, STRIDE_BYTES, 14 * Float.BYTES);
			GL20.glEnableVertexAttribArray(5);
			GL20.glVertexAttribPointer(5, 3, GL11.GL_FLOAT, false, STRIDE_BYTES, 18 * Float.BYTES);

			GL20.glUseProgram(shaderProgram);

			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureDiffuse"), 0);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureNormal"), 1);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureORM"), 2);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureEmissive"), 3);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureTeamColor"), 4);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureReflections"), 5);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_textureUsed"), textureUsed);
			textureUsed = 0;
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_alphaTest"), alphaTest);
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_lightingEnabled"), lightingEnabled);

			if (usingModelCamera) {
				// this one emulates UI\MiscData.txt light
				// (used in WC3 portraits, so it'll be wrong on "main menu" background models)
				GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), 0.3f, -0.3f, 0.25f);
			}
			else {
				// this one emulates DNC model light
				// (used in WC3 game world view)
				GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), -24.1937f, 30.4879f,
						444.411f);
			}

//			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), 0.0f, 0.0f, -10000f);

			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewPos"), cameraLocation.x, cameraLocation.y,
					cameraLocation.z);
			GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "u_viewportSize"), viewportWidth, viewportHeight);
			GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelTeamColor"), fresnelTeamColor);
			GL20.glUniform4f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelColor"), fresnelColor.x, fresnelColor.y,
					fresnelColor.z, fresnelOpacity);
			GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "u_emissiveGain"), renderEmissiveGain);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewRight"), viewRight.x, viewRight.y,
					viewRight.z);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewUp"), viewUp.x, viewUp.y, viewUp.z);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewBack"), viewBack.x, viewBack.y,
					viewBack.z);
			uploadSceneLights(shaderProgram, sceneLights, lightUploadBuffer);
			// The main light of the Reforged path is the viewer's baseline sun (the
			// game's own is the day/night rig); model directional and ambient lights
			// do not reach the HD pass in 3.0, only the omni lights do.
			GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "u_mainLightEnabled"), 1);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_mainLightDir"), BASELINE_SUN_DIR.x,
					BASELINE_SUN_DIR.y, BASELINE_SUN_DIR.z);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_mainLightColor"), 0.9f, 0.9f, 0.9f);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_mainAmbient"), 0.3f, 0.3f, 0.3f);
			GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "u_iblScale"), 0.15f);
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
//			pipelineMatrixBuffer.put(currentMatrix.m00);
//			pipelineMatrixBuffer.put(currentMatrix.m10);
//			pipelineMatrixBuffer.put(currentMatrix.m20);
//			pipelineMatrixBuffer.put(currentMatrix.m30);
//			pipelineMatrixBuffer.put(currentMatrix.m01);
//			pipelineMatrixBuffer.put(currentMatrix.m11);
//			pipelineMatrixBuffer.put(currentMatrix.m21);
//			pipelineMatrixBuffer.put(currentMatrix.m31);
//			pipelineMatrixBuffer.put(currentMatrix.m02);
//			pipelineMatrixBuffer.put(currentMatrix.m12);
//			pipelineMatrixBuffer.put(currentMatrix.m22);
//			pipelineMatrixBuffer.put(currentMatrix.m32);
//			pipelineMatrixBuffer.put(currentMatrix.m03);
//			pipelineMatrixBuffer.put(currentMatrix.m13);
//			pipelineMatrixBuffer.put(currentMatrix.m23);
//			pipelineMatrixBuffer.put(currentMatrix.m33);
			GL20.glUniformMatrix4(GL20.glGetUniformLocation(shaderProgram, "u_projection"), false,
					pipelineMatrixBuffer);
			GL11.glDrawArrays(glBeginType, 0, vertexCount);
			vertexCount = 0;
			uvCount = 0;
			normalCount = 0;
			colorCount = 0;
			fresnelColorCount = 0;
			tangentCount = 0;
			pipelineVertexBuffer.clear();
		}

		@Override
		public void glPolygonMode(final int face, final int mode) {
			GL11.glPolygonMode(face, mode);
		}

		@Override
		public void glColor4f(final float r, final float g, final float b, final float a) {
			final int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, a);
			pushFloat(baseOffset + 14, color.x);
			pushFloat(baseOffset + 15, color.y);
			pushFloat(baseOffset + 16, color.z);
			pushFloat(baseOffset + 17, color.w);
			colorCount++;
		}

		@Override
		public void glNormal3f(final float x, final float y, final float z) {
			final int baseOffset = normalCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 4, x);
			pushFloat(baseOffset + 5, y);
			pushFloat(baseOffset + 6, z);
			pushFloat(baseOffset + 7, 0);
			normalCount++;
		}

		@Override
		public void glTexCoord2f(final float u, final float v) {
			final int baseOffset = uvCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 12, u);
			pushFloat(baseOffset + 13, v);
			uvCount++;
		}

		@Override
		public void glColor3f(final float r, final float g, final float b) {
			final int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, color.w);
			pushFloat(baseOffset + 14, color.x);
			pushFloat(baseOffset + 15, color.y);
			pushFloat(baseOffset + 16, color.z);
			colorCount++;
		}

		@Override
		public void glFresnelTeamColor1f(final float v) {
			fresnelTeamColor = v;
		}

		@Override
		public void glFresnelOpacity1f(final float v) {
			fresnelOpacity = v;
		}

		@Override
		public void glEmissiveGain1f(final float renderEmissiveGain) {
			this.renderEmissiveGain = renderEmissiveGain;
		}

		@Override
		public void glFresnelColor3f(final float r, final float g, final float b) {
			final int baseOffset = fresnelColorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			fresnelColor.set(r, g, b);
			pushFloat(baseOffset + 18, r);
			pushFloat(baseOffset + 19, g);
			pushFloat(baseOffset + 20, b);
			fresnelColorCount++;
		}

		@Override
		public void glColor4ub(final byte r, final byte g, final byte b, final byte a) {
			final int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
			pushFloat(baseOffset + 14, color.x);
			pushFloat(baseOffset + 15, color.y);
			pushFloat(baseOffset + 16, color.z);
			pushFloat(baseOffset + 17, color.w);
			colorCount++;
		}

		@Override
		public void glLight(final int light, final int pname, final FloatBuffer params) {

		}

		private final Quaternion tempQuat = new Quaternion();
		private final Matrix4f tempMat4 = new Matrix4f();
		private boolean usingModelCamera;

		@Override
		public void glRotatef(final float angle, final float axisX, final float axisY, final float axisZ) {
			tempVec3.set(axisX, axisY, axisZ);
			tempVec3.normalise();
			tempVec4.set(tempVec3.x, tempVec3.y, tempVec3.z, (float) Math.toRadians(angle));
			tempQuat.setFromAxisAngle(tempVec4);
			tempQuat.normalise();
			MathUtils.fromQuat(tempQuat, tempMat4);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		private final SceneLights sceneLights = new SceneLights();

		@Override
		public void glSceneLights(final SceneLights lights) {
			if (lights == null) {
				sceneLights.mode = SceneLights.MODE_LEGACY;
				sceneLights.count = 0;
			} else {
				sceneLights.copyFrom(lights);
			}
		}

		private final Vector3f viewRight = new Vector3f(1, 0, 0);
		private final Vector3f viewUp = new Vector3f(0, 0, 1);
		private final Vector3f viewBack = new Vector3f(0, -1, 0);

		@Override
		public void glCamera(final ViewerCamera viewerCamera, final boolean usingModelCamera) {
			this.usingModelCamera = usingModelCamera;
			cameraLocation.set(viewerCamera.location);
			final Matrix4f view = viewerCamera.viewMatrix;
			viewRight.set(view.m00, view.m10, view.m20);
			viewUp.set(view.m01, view.m11, view.m21);
			viewBack.set(view.m02, view.m12, view.m22);
			Matrix4f.mul(viewerCamera.viewProjectionMatrix, currentMatrix, currentMatrix);
		}

		private final Vector3f tempVec3 = new Vector3f();
		private final Vector4f tempVec4 = new Vector4f();
		private int textureUnit;
		private int matrixMode;
		private int viewportWidth;
		private int viewportHeight;
		private final Vector3f cameraLocation = new Vector3f();

		@Override
		public void glScalef(final float x, final float y, final float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.scale(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glTranslatef(final float x, final float y, final float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.translate(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glOrtho(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin,
				final float zMax) {
			MathUtils.setOrtho(currentMatrix, xMin, xMax, yMin, yMax, zMin, zMax);
		}

		@Override
		public void gluPerspective(final float fovY, final float aspect, final float nearClip, final float farClip) {
			MathUtils.setPerspective(currentMatrix, (float) Math.toRadians(fovY), aspect, nearClip, farClip);
			// When we are not using fixed function pipeline, notably Perspective cannot be
			// expressed as a matrix due to the math, so to emulate legacy behavior we will
			// set a flag and divide by negative Z factor later.
		}

		@Override
		public void glLightModel(final int lightModel, final FloatBuffer ambientColor) {

		}

		@Override
		public void glMatrixMode(final int mode) {
			matrixMode = mode;

		}

		@Override
		public void glLoadIdentity() {
			if (matrixMode == GL11.GL_PROJECTION) {
				currentMatrix.setIdentity();
			} // else if it is set to GL_MODELVIEW we should be in a different mode, but I was
				// lazy and only made 1 matrix and so we skip it....
		}

		@Override
		public void glEnableIfNeeded(final int glEnum) {
			if (glEnum == GL11.GL_TEXTURE_2D) {
				textureUsed = 1;
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
			}
			else if ((glEnum == GL11.GL_ALPHA_TEST) && (textureUnit == 0)) {
				alphaTest = 1;
			}
			else if (glEnum == GL11.GL_LIGHTING) {
				lightingEnabled = 1;
			}
		}

		@Override
		public void glShadeModel(final int mode) {
		}

		@Override
		public void glDisableIfNeeded(final int glEnum) {
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

		@Override
		public void prepareToBindTexture() {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
			textureUsed = 1;
		}

		@Override
		public void onGlobalPipelineSet() {
			GL30.glBindVertexArray(vertexArrayObjectId);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObjectId);
		}

		@Override
		public void glTangent4f(final float x, final float y, final float z, final float w) {
			final int baseOffset = tangentCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 8, x);
			pushFloat(baseOffset + 9, y);
			pushFloat(baseOffset + 10, z);
			pushFloat(baseOffset + 11, w);
			tangentCount++;
		}

		@Override
		public void glActiveHDTexture(final int textureUnit) {
			this.textureUnit = textureUnit;
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		}

		@Override
		public void glViewport(final int x, final int y, final int w, final int h) {
			viewportWidth = w;
			viewportHeight = h;
			GL11.glViewport(x, y, w, h);
		}

		@Override
		public void discard() {
			GL20.glDeleteProgram(shaderProgram);
		}

		@Override
		public void setCurrentPipeline(final int pipelineId) {
		}

		@Override
		public int getCurrentPipelineIndex() {
			return 1;
		}

	}

	public static final class FixedFunctionPipeline implements Pipeline {
		@Override
		public void glBegin(final int type) {
			GL11.glBegin(type);
		}

		@Override
		public void glVertex3f(final float x, final float y, final float z) {
			GL11.glVertex3f(x, y, z);
		}

		@Override
		public void glEnd() {
			GL11.glEnd();
		}

		@Override
		public void glPolygonMode(final int face, final int mode) {
			GL11.glPolygonMode(face, mode);
		}

		@Override
		public void glColor4f(final float r, final float g, final float b, final float a) {
			GL11.glColor4f(r, g, b, a);
		}

		@Override
		public void glNormal3f(final float x, final float y, final float z) {
			GL11.glNormal3f(x, y, z);
		}

		@Override
		public void glTexCoord2f(final float u, final float v) {
			GL11.glTexCoord2f(u, v);
		}

		@Override
		public void glColor3f(final float r, final float g, final float b) {
			GL11.glColor3f(r, g, b);
		}

		@Override
		public void glFresnelColor3f(final float r, final float g, final float b) {
		}

		@Override
		public void glColor4ub(final byte r, final byte g, final byte b, final byte a) {
			GL11.glColor4ub(r, g, b, a);
		}

		@Override
		public void glLight(final int light, final int pname, final FloatBuffer params) {
			GL11.glLight(light, pname, params);
		}

		@Override
		public void glRotatef(final float a, final float b, final float c, final float d) {
			GL11.glRotatef(a, b, c, d);
		}

		@Override
		public void glScalef(final float x, final float y, final float z) {
			GL11.glScalef(x, y, z);
		}

		@Override
		public void glTranslatef(final float x, final float y, final float z) {
			GL11.glTranslatef(x, y, z);
		}

		@Override
		public void glOrtho(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin,
				final float zMax) {
			GL11.glOrtho(xMin, xMax, yMin, yMax, zMin, zMax);
		}

		@Override
		public void gluPerspective(final float fovY, final float aspect, final float nearClip, final float farClip) {
			GLU.gluPerspective(fovY, aspect, nearClip, farClip);
		}

		@Override
		public void glLightModel(final int lightModel, final FloatBuffer ambientColor) {
			GL11.glLightModel(lightModel, ambientColor);
		}

		@Override
		public void glMatrixMode(final int mode) {
			GL11.glMatrixMode(mode);
		}

		@Override
		public void glLoadIdentity() {
			GL11.glLoadIdentity();
		}

		@Override
		public void glEnableIfNeeded(final int glEnum) {
			GL11.glEnable(glEnum);
		}

		@Override
		public void glShadeModel(final int glFlat) {
			GL11.glShadeModel(glFlat);
		}

		@Override
		public void glDisableIfNeeded(final int glEnum) {
			GL11.glDisable(glEnum);
		}

		@Override
		public void prepareToBindTexture() {
		}

		@Override
		public void onGlobalPipelineSet() {
		}

		@Override
		public void glTangent4f(final float x, final float y, final float z, final float w) {
			// tangents are not applicable to old style drawing
		}

		@Override
		public void glActiveHDTexture(final int textureUnit) {
			// TODO Auto-generated method stub

		}

		@Override
		public void glViewport(final int x, final int y, final int w, final int h) {
			GL11.glViewport(x, y, w, h);
		}

		@Override
		public void glFresnelTeamColor1f(final float v) {
		}

		@Override
		public void glFresnelOpacity1f(final float v) {
		}

		@Override
		public void glCamera(final ViewerCamera viewerCamera, final boolean usingModelCamera) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void glSceneLights(final SceneLights lights) {
		}

		@Override
		public void discard() {
		}

		@Override
		public void setCurrentPipeline(final int pipelineId) {
		}

		@Override
		public int getCurrentPipelineIndex() {
			return 0;
		}

		@Override
		public void glEmissiveGain1f(final float renderEmissiveGain) {
		}

	}

	public static interface Pipeline {
		void glBegin(int type);

		void onGlobalPipelineSet();

		void glVertex3f(float x, float y, float z);

		void glEnd();

		void glPolygonMode(int face, int mode);

		void glColor4f(float r, float g, float b, float a);

		void glFresnelTeamColor1f(float v);

		void glFresnelOpacity1f(float v);

		void glEmissiveGain1f(float renderEmissiveGain);

		void glFresnelColor3f(float r, float g, float b);

		void glNormal3f(float x, float y, float z);

		void glTexCoord2f(float u, float v);

		void glColor3f(float r, float g, float b);

		void glColor4ub(byte r, byte g, byte b, byte a);

		void glLight(int light, int pname, FloatBuffer params);

		void glRotatef(float angle, float axisX, float axisY, float axisZ);

		void glScalef(float x, float y, float z);

		void glTranslatef(float x, float y, float z);

		void glOrtho(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax);

		void gluPerspective(float fovY, float aspect, float nearClip, float farClip);

		void glLightModel(int lightModel, FloatBuffer ambientColor);

		void glMatrixMode(int mode);

		void glLoadIdentity();

		void glEnableIfNeeded(int glEnum);

		void glShadeModel(int glFlat);

		void glDisableIfNeeded(int glEnum);

		void prepareToBindTexture();

		void glTangent4f(float x, float y, float z, float w);

		void glActiveHDTexture(int textureUnit);

		void glViewport(int x, int y, int w, int h);

		void glCamera(ViewerCamera viewerCamera, boolean usingModelCamera);

		/** The lights for the coming draws; null or MODE_LEGACY keeps the fixed viewer light. */
		void glSceneLights(SceneLights lights);

		void discard();

		void setCurrentPipeline(int pipelineId);

		int getCurrentPipelineIndex();
	}
}
