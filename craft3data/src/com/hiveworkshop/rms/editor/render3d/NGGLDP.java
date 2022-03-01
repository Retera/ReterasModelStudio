package com.hiveworkshop.rms.editor.render3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

import com.hiveworkshop.wc3.util.MathUtils;

/**
 * NGGLDP stands for "Not Good GL Design Practices". I am just trying to hack
 * this in for now based on the old legacy RMS code that was using sad, bad
 * fixed pipeline code (which can never render Reforged models accurately!)
 */
public class NGGLDP {
	private static final FixedFunctionPipeline fixedFunctionPipeline = new FixedFunctionPipeline();

	public static Pipeline pipeline = null;

	public static void setPipeline(Pipeline userPipeline) {
		pipeline = userPipeline;
		pipeline.onGlobalPipelineSet();
	}

	public static void fixedFunction() {
		pipeline = fixedFunctionPipeline;
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
				"uniform vec3 u_lightDirection;\r\n" + //
				"uniform int u_lightingEnabled;\r\n" + //
				"\r\n" + //
				"void main() {\r\n" + //
				"		gl_Position = a_position;\r\n" + //
				"		v_uv = a_uv;\r\n" + //
				"		v_color = a_color;\r\n" + //
				"		if(u_lightingEnabled != 0) {\r\n" + //
				"			vec3 lightFactorContribution = vec3(clamp(dot(a_normal.xyz, u_lightDirection), 0.0, 1.0));\r\n"
				+ //
				"			if(lightFactorContribution.r > 1.0 || lightFactorContribution.g > 1.0 || lightFactorContribution.b > 1.0) {\r\n"
				+ //
				"				lightFactorContribution = clamp(lightFactorContribution, 0.0, 1.0);\r\n" + //
				"			}\r\n" + //
				"			v_color.rgb = v_color.rgb * clamp(lightFactorContribution + vec3(0.3f, 0.3f, 0.3f), 0.0, 1.0);\r\n"
				+ //
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
		private Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
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
		private Matrix4f currentMatrix = new Matrix4f();
		{
			currentMatrix.setIdentity();
		}
		private int textureUsed = 0;
		private int alphaTest = 0;
		private int lightingEnabled = 1;
		private boolean perspectiveActive = false;

		public SimpleDiffuseShaderPipeline() {
			load();
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

		@Override
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

		private void pushFloat(int absoluteOffset, float x) {
			ensureCapacity(absoluteOffset);
			pipelineVertexBuffer.put(absoluteOffset, x);
		}

		private void ensureCapacity(int absoluteOffset) {
			if (pipelineVertexBuffer.capacity() <= absoluteOffset) {
				FloatBuffer largerBuffer = ByteBuffer
						.allocateDirect(Math.max((absoluteOffset + 1) * 4, pipelineVertexBuffer.capacity() * 2 * 4))
						.order(ByteOrder.nativeOrder()).asFloatBuffer().clear();
				pipelineVertexBuffer.flip();
				largerBuffer.put(pipelineVertexBuffer);
				largerBuffer.clear();
				pipelineVertexBuffer = largerBuffer;
			}
		}

		@Override
		public void glVertex3f(float x, float y, float z) {
			int baseOffset = vertexCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			tempVec4.set(x, y, z, 1);
			Matrix4f.transform(currentMatrix, tempVec4, tempVec4);
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
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), 0.0683413f, -0.0542323f,
					0.996187f);
			GL11.glDrawArrays(glBeginType, 0, vertexCount);
			vertexCount = 0;
			uvCount = 0;
			normalCount = 0;
			colorCount = 0;
			pipelineVertexBuffer.clear();
		}

		@Override
		public void glPolygonMode(int face, int mode) {
			GL11.glPolygonMode(face, mode);
		}

		@Override
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

		@Override
		public void glNormal3f(float x, float y, float z) {
			int baseOffset = normalCount * STRIDE;
			tempVec4.set(x, y, z, 1);
			Matrix4f.transform(currentMatrix, tempVec4, tempVec4);
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 4, tempVec4.x);
			pushFloat(baseOffset + 5, tempVec4.y);
			pushFloat(baseOffset + 6, tempVec4.z);
			pushFloat(baseOffset + 7, 1);
			normalCount++;
		}

		@Override
		public void glTexCoord2f(float u, float v) {
			int baseOffset = uvCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 8, u);
			pushFloat(baseOffset + 9, v);
			uvCount++;
		}

		@Override
		public void glColor3f(float r, float g, float b) {
			int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, color.w);
			pushFloat(baseOffset + 10, color.x);
			pushFloat(baseOffset + 11, color.y);
			pushFloat(baseOffset + 12, color.z);
			colorCount++;
		}

		@Override
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

		@Override
		public void glLight(int light, int pname, FloatBuffer params) {

		}

		private Quaternion tempQuat = new Quaternion();
		private Matrix4f tempMat4 = new Matrix4f();

		@Override
		public void glRotatef(float angle, float axisX, float axisY, float axisZ) {
			tempVec3.set(axisX, axisY, axisZ);
			tempVec3.normalise();
			tempVec4.set(tempVec3.x, tempVec3.y, tempVec3.z, (float) Math.toRadians(angle));
			tempQuat.setFromAxisAngle(tempVec4);
			tempQuat.normalise();
			MathUtils.fromQuat(tempQuat, tempMat4);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		private Vector3f tempVec3 = new Vector3f();
		private Vector4f tempVec4 = new Vector4f();
		private int matrixMode;

		@Override
		public void glScalef(float x, float y, float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.scale(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glTranslatef(float x, float y, float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.translate(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glOrtho(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
			perspectiveActive = false;
			MathUtils.setOrtho(currentMatrix, xMin, xMax, yMin, yMax, zMin, zMax);
		}

		@Override
		public void gluPerspective(float fovY, float aspect, float nearClip, float farClip) {
			MathUtils.setPerspective(currentMatrix, (float) Math.toRadians(fovY), aspect, nearClip, farClip);
			// When we are not using fixed function pipeline, notably Perspective cannot be
			// expressed as a matrix due to the math, so to emulate legacy behavior we will
			// set a flag and divide by negative Z factor later.
			perspectiveActive = NGGLDP.pipeline != NGGLDP.fixedFunctionPipeline;
		}

		@Override
		public void glLightModel(int lightModel, FloatBuffer ambientColor) {

		}

		@Override
		public void glMatrixMode(int mode) {
			this.matrixMode = mode;

		}

		@Override
		public void glLoadIdentity() {
			if (matrixMode == GL11.GL_PROJECTION) {
				perspectiveActive = false;
				currentMatrix.setIdentity();
			} // else if it is set to GL_MODELVIEW we should be in a different mode, but I was
				// lazy and only made 1 matrix and so we skip it....
		}

		@Override
		public void glEnableIfNeeded(int glEnum) {
			if (glEnum == GL11.GL_TEXTURE_2D) {
				textureUsed = 1;
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
			} else if (glEnum == GL11.GL_ALPHA_TEST) {
				alphaTest = 1;
			} else if (glEnum == GL11.GL_LIGHTING) {
				lightingEnabled = 1;
			}
		}

		@Override
		public void glShadeModel(int mode) {
		}

		@Override
		public void glDisableIfNeeded(int glEnum) {
			if (glEnum == GL11.GL_TEXTURE_2D) {
				textureUsed = 0;
				GL13.glActiveTexture(0);
			} else if (glEnum == GL11.GL_ALPHA_TEST) {
				alphaTest = 0;
			} else if (glEnum == GL11.GL_LIGHTING) {
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
		public void glTangent4f(float x, float y, float z, float w) {
			// tangents are not applicable to old style drawing
		}

		@Override
		public void glActiveHDTexture(int textureUnit) {
			// TODO Auto-generated method stub

		}

		@Override
		public void glViewport(int x, int y, int w, int h) {
			GL11.glViewport(x, y, w, h);
		}

		@Override
		public void glFresnelColor3f(float r, float g, float b) {
		}

		@Override
		public void glFresnelTeamColor1f(float v) {
		}

		@Override
		public void glFresnelOpacity1f(float v) {
		}

	}

	/**
	 * HDDiffuseShaderPipeline is only for classic HD models of Reforged
	 */
	public static final class HDDiffuseShaderPipeline implements Pipeline {
		private static final int STRIDE = 4 /* position */ + 4 /* normal */ + 4 /* tangent */ + 2 /* uv */
				+ 4 /* color */ ;
		private static final int STRIDE_BYTES = STRIDE * Float.BYTES;
		private static final String vertexShader = "#version 330 core\r\n" + //
				"\r\n" + //
				"layout (location = 0) in vec4 a_position;\r\n" + //
				"layout (location = 1) in vec4 a_normal;\r\n" + //
				"layout (location = 2) in vec4 a_tangent;\r\n" + //
				"layout (location = 3) in vec2 a_uv;\r\n" + //
				"layout (location = 4) in vec4 a_color;\r\n" + //
				"\r\n" + //
				"uniform vec3 u_lightDirection;\r\n" + //
				"uniform vec3 u_viewPos;\r\n" + //
				"uniform mat4 u_projection;\r\n" + //
				"\r\n" + //
				"out vec2 v_uv;\r\n" + //
				"out vec4 v_color;\r\n" + //
				"out vec3 v_tangentLightPos;\r\n" + //
				"out vec3 v_tangentViewPos;\r\n" + //
				"out vec3 v_tangentFragPos;\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"void main() {\r\n" + //
				"		gl_Position = u_projection * a_position;\r\n" + //
				"		v_uv = a_uv;\r\n" + //
				"		v_color = a_color;\r\n" + //
				"		vec3 tangent = a_tangent.xyz;\r\n" + //
				// this is supposed to re-orthogonalize per
				// https://learnopengl.com/Advanced-Lighting/Normal-Mapping although I'm
				// undecided if wc3 needs it
				"		tangent = normalize(tangent - dot(tangent, a_normal.xyz) * a_normal.xyz);\r\n" + //
				"		vec3 binormal = normalize(cross(a_normal.xyz, tangent) * a_tangent.w);\r\n" + //
				"		mat3 mv = mat3(u_projection);\r\n" + //
				"		mat3 TBN = transpose(mat3(normalize(mv*tangent), normalize(mv*binormal), normalize(mv*a_normal.xyz)));\r\n"
				+ //
				"		v_tangentLightPos = TBN * (mv * u_lightDirection).xyz;\r\n" + //
				"		v_tangentViewPos = TBN * u_viewPos;\r\n" + //
				"		v_tangentFragPos = TBN * (u_projection * a_position).xyz;\r\n" + //
				"}\r\n\0";
		private static final String fragmentShader = "#version 330 core\r\n" + //
				"\r\n" + //
				"uniform sampler2D u_textureDiffuse;\r\n" + //
				"uniform sampler2D u_textureNormal;\r\n" + //
				"uniform sampler2D u_textureORM;\r\n" + //
				"uniform sampler2D u_textureEmissive;\r\n" + //
				"uniform sampler2D u_textureTeamColor;\r\n" + //
				"uniform sampler2D u_textureReflections;\r\n" + //
				"uniform int u_textureUsed;\r\n" + //
				"uniform int u_alphaTest;\r\n" + //
				"uniform int u_lightingEnabled;\r\n" + //
				"uniform float u_fresnelTeamColor;\r\n" + //
				"uniform vec4 u_fresnelColor;\r\n" + //
				"uniform vec2 u_viewportSize;\r\n" //
				+ "" //
//				+ "float GeometrySchlickGGX(float NdotV, float k)\r\n" // Source: https://learnopengl.com/PBR/Theory
//				+ "{\r\n" //
//				+ "    float nom   = NdotV;\r\n" //
//				+ "    float denom = NdotV * (1.0 - k) + k;\r\n" //
//				+ "	\r\n" //
//				+ "    return nom / denom;\r\n" //
//				+ "}\r\n" //
//				+ "  \r\n" //
//				+ "float GeometrySmith(vec3 N, vec3 V, vec3 L, float k)\r\n" //
//				+ "{\r\n" //
//				+ "    float NdotV = max(dot(N, V), 0.0);\r\n" //
//				+ "    float NdotL = max(dot(N, L), 0.0);\r\n" //
//				+ "    float ggx1 = GeometrySchlickGGX(NdotV, k);\r\n" //
//				+ "    float ggx2 = GeometrySchlickGGX(NdotL, k);\r\n" //
//				+ "	\r\n" //
//				+ "    return ggx1 * ggx2;\r\n" //
//				+ "}\r\n" //
//				+ "" //
//				+ ""
//				+ "\r\n"
//				+ "float DistributionGGX(vec3 N, vec3 H, float a)\r\n"
//				+ "{\r\n"
//				+ "    float a2     = a*a;\r\n"
//				+ "    float NdotH  = max(dot(N, H), 0.0);\r\n"
//				+ "    float NdotH2 = NdotH*NdotH;\r\n"
//				+ "	\r\n"
//				+ "    float nom    = a2;\r\n"
//				+ "    float denom  = (NdotH2 * (a2 - 1.0) + 1.0);\r\n"
//				+ "    denom        = PI * denom * denom;\r\n"
//				+ "	\r\n"
//				+ "    return nom / denom;\r\n"
//				+ "}\r\n"
				+ "" + "" //
				+ "" + //
				"\r\n" + //
				"in vec2 v_uv;\r\n" + //
				"in vec4 v_color;\r\n" + //
				"in vec3 v_tangentLightPos;\r\n" + //
				"in vec3 v_tangentViewPos;\r\n" + //
				"in vec3 v_tangentFragPos;\r\n" + //
				"\r\n" + //
				"out vec4 FragColor;\r\n" + //
				"\r\n" + //
				"void main() {\r\n" + // s
				"		vec4 color;\r\n" + //
				"		vec4 ormTexel = texture2D(u_textureORM, v_uv);\r\n" + //
				"		vec4 teamColorTexel = texture2D(u_textureTeamColor, v_uv);\r\n" + //
				"		if(u_textureUsed != 0) {\r\n" + //
				"			vec4 texel = texture2D(u_textureDiffuse, v_uv);\r\n" + //
				"			color = vec4(texel.rgb * ((1.0 - ormTexel.a) + (teamColorTexel.rgb * ormTexel.a)), texel.a) * v_color;\r\n"
				+ //
				"		} else {\r\n" + //
				"			color = v_color;\r\n" + //
				"		}\r\n" + //
				"		if(u_alphaTest != 0 && color.a < 0.75) {\r\n" + //
				"			discard;\r\n" + //
				"		}\r\n" + //
				"		if(u_lightingEnabled != 0) {\r\n" + //
				"			vec2 normalXY = texture2D(u_textureNormal, v_uv).xy * 2.0 - 1.0;\r\n" + //
				"			vec3 normal = vec3(normalXY, sqrt(1.0 - dot(normalXY,normalXY)));\r\n" + //
				"			vec4 emissiveTexel = texture2D(u_textureEmissive, v_uv);\r\n" + //
				"			vec4 reflectionsTexel = clamp(0.2+2.0*texture2D(u_textureReflections, vec2(gl_FragCoord.x/u_viewportSize.x, -gl_FragCoord.y/u_viewportSize.y)), 0.0, 1.0);\r\n"
				+ //
				"			vec3 lightDir = normalize(v_tangentViewPos);\r\n" + //
				"			float cosTheta = dot(lightDir, normal);\r\n" + //
				"			float lambertFactor = clamp(cosTheta, 0.0, 1.0);\r\n" + //
				"			vec3 diffuse = (clamp(lambertFactor * (ormTexel.r) + 0.1, 0.0, 1.0)) * color.xyz;\r\n" + //
				"			vec3 viewDir = normalize(v_tangentViewPos - v_tangentFragPos);\r\n" + //
				"			vec3 reflectDir = reflect(-lightDir, normal);\r\n" + //
				"			vec3 halfwayDir = normalize(lightDir + viewDir);\r\n" + //
				"			float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);\r\n" + //
				"			vec3 specular = vec3(max(ormTexel.b-0.5, 0.0)) * spec /* * reflectionsTexel.xyz*/;\r\n" + //
				"			vec3 fresnelColor = vec3(u_fresnelColor.rgb * (1.0 - u_fresnelTeamColor) + teamColorTexel.rgb *  u_fresnelTeamColor) * v_color.rgb;\r\n" + //
				"			vec3 fresnel = fresnelColor*pow(1.0 - cosTheta, 1.0)*u_fresnelColor.a;\r\n" + //
				"			FragColor = vec4(emissiveTexel.xyz + specular + diffuse + fresnel, color.a);\r\n" + //
				"		} else {\r\n" + //
				"			FragColor = color;\r\n" + //
				"		}\r\n" + //
				"}\r\n\0";
		private Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
		private Vector3f fresnelColor = new Vector3f(0f, 0f, 0f);
		private FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		private FloatBuffer pipelineMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
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
		private Matrix4f currentMatrix = new Matrix4f();
		{
			currentMatrix.setIdentity();
		}
		private int textureUsed = 0;
		private int alphaTest = 0;
		private int lightingEnabled = 1;
		private boolean perspectiveActive = false;
		private float fresnelTeamColor;
		private float fresnelOpacity;

		public HDDiffuseShaderPipeline() {
			load();
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

		@Override
		public void glBegin(int type) {
			pipelineVertexBuffer.clear();
			this.glBeginType = type;
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

		private void pushFloat(int absoluteOffset, float x) {
			ensureCapacity(absoluteOffset);
			pipelineVertexBuffer.put(absoluteOffset, x);
		}

		private void ensureCapacity(int absoluteOffset) {
			if (pipelineVertexBuffer.capacity() <= absoluteOffset) {
				int newSizeBytes = Math.max((absoluteOffset + 1) * 4, pipelineVertexBuffer.capacity() * 2 * 4);
				FloatBuffer largerBuffer = ByteBuffer.allocateDirect(newSizeBytes).order(ByteOrder.nativeOrder())
						.asFloatBuffer().clear();
				pipelineVertexBuffer.flip();
				largerBuffer.put(pipelineVertexBuffer);
				largerBuffer.clear();
				pipelineVertexBuffer = largerBuffer;
			}
		}

		@Override
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
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_lightDirection"), -24.1937f, 444.411f,
					30.4879f);
			GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "u_viewPos"), 0, 0, -1);
			GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "u_viewportSize"), viewportWidth, viewportHeight);
			GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelTeamColor"), fresnelTeamColor);
			GL20.glUniform4f(GL20.glGetUniformLocation(shaderProgram, "u_fresnelColor"), fresnelColor.x, fresnelColor.y, fresnelColor.z, fresnelOpacity);
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
		public void glPolygonMode(int face, int mode) {
			GL11.glPolygonMode(face, mode);
		}

		@Override
		public void glColor4f(float r, float g, float b, float a) {
			int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, a);
			pushFloat(baseOffset + 14, color.x);
			pushFloat(baseOffset + 15, color.y);
			pushFloat(baseOffset + 16, color.z);
			pushFloat(baseOffset + 17, color.w);
			colorCount++;
		}

		@Override
		public void glNormal3f(float x, float y, float z) {
			int baseOffset = normalCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 4, x);
			pushFloat(baseOffset + 5, y);
			pushFloat(baseOffset + 6, z);
			pushFloat(baseOffset + 7, 1);
			normalCount++;
		}

		@Override
		public void glTexCoord2f(float u, float v) {
			int baseOffset = uvCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 12, u);
			pushFloat(baseOffset + 13, v);
			uvCount++;
		}

		@Override
		public void glColor3f(float r, float g, float b) {
			int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set(r, g, b, color.w);
			pushFloat(baseOffset + 14, color.x);
			pushFloat(baseOffset + 15, color.y);
			pushFloat(baseOffset + 16, color.z);
			colorCount++;
		}

		@Override
		public void glFresnelTeamColor1f(float v) {
			this.fresnelTeamColor = v;
		}

		@Override
		public void glFresnelOpacity1f(float v) {
			this.fresnelOpacity = v;
		}

		@Override
		public void glFresnelColor3f(float r, float g, float b) {
			int baseOffset = fresnelColorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			fresnelColor.set(r, g, b);
			pushFloat(baseOffset + 18, r);
			pushFloat(baseOffset + 19, g);
			pushFloat(baseOffset + 20, b);
			fresnelColorCount++;
		}

		@Override
		public void glColor4ub(byte r, byte g, byte b, byte a) {
			int baseOffset = colorCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			color.set((r & 0xFF) / 255f, (g & 0xFF) / 255f, (b & 0xFF) / 255f, (a & 0xFF) / 255f);
			pushFloat(baseOffset + 14, color.x);
			pushFloat(baseOffset + 15, color.y);
			pushFloat(baseOffset + 16, color.z);
			pushFloat(baseOffset + 17, color.w);
			colorCount++;
		}

		@Override
		public void glLight(int light, int pname, FloatBuffer params) {

		}

		private Quaternion tempQuat = new Quaternion();
		private Matrix4f tempMat4 = new Matrix4f();

		@Override
		public void glRotatef(float angle, float axisX, float axisY, float axisZ) {
			tempVec3.set(axisX, axisY, axisZ);
			tempVec3.normalise();
			tempVec4.set(tempVec3.x, tempVec3.y, tempVec3.z, (float) Math.toRadians(angle));
			tempQuat.setFromAxisAngle(tempVec4);
			tempQuat.normalise();
			MathUtils.fromQuat(tempQuat, tempMat4);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		private Vector3f tempVec3 = new Vector3f();
		private Vector4f tempVec4 = new Vector4f();
		private int textureUnit;
		private int matrixMode;
		private int viewportWidth;
		private int viewportHeight;

		@Override
		public void glScalef(float x, float y, float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.scale(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glTranslatef(float x, float y, float z) {
			tempMat4.setIdentity();
			tempVec3.set(x, y, z);
			tempMat4.translate(tempVec3);
			Matrix4f.mul(currentMatrix, tempMat4, currentMatrix);
		}

		@Override
		public void glOrtho(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
			perspectiveActive = false;
			MathUtils.setOrtho(currentMatrix, xMin, xMax, yMin, yMax, zMin, zMax);
		}

		@Override
		public void gluPerspective(float fovY, float aspect, float nearClip, float farClip) {
			MathUtils.setPerspective(currentMatrix, (float) Math.toRadians(fovY), aspect, nearClip, farClip);
			// When we are not using fixed function pipeline, notably Perspective cannot be
			// expressed as a matrix due to the math, so to emulate legacy behavior we will
			// set a flag and divide by negative Z factor later.
			perspectiveActive = NGGLDP.pipeline != NGGLDP.fixedFunctionPipeline;
		}

		@Override
		public void glLightModel(int lightModel, FloatBuffer ambientColor) {

		}

		@Override
		public void glMatrixMode(int mode) {
			this.matrixMode = mode;

		}

		@Override
		public void glLoadIdentity() {
			perspectiveActive = false;
			if (matrixMode == GL11.GL_PROJECTION) {
				perspectiveActive = false;
				currentMatrix.setIdentity();
			} // else if it is set to GL_MODELVIEW we should be in a different mode, but I was
				// lazy and only made 1 matrix and so we skip it....
		}

		@Override
		public void glEnableIfNeeded(int glEnum) {
			if (glEnum == GL11.GL_TEXTURE_2D) {
				textureUsed = 1;
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
			} else if (glEnum == GL11.GL_ALPHA_TEST && textureUnit == 0) {
				alphaTest = 1;
			} else if (glEnum == GL11.GL_LIGHTING) {
				lightingEnabled = 1;
			}
		}

		@Override
		public void glShadeModel(int mode) {
		}

		@Override
		public void glDisableIfNeeded(int glEnum) {
			if (glEnum == GL11.GL_TEXTURE_2D) {
				textureUsed = 0;
				GL13.glActiveTexture(0);
			} else if (glEnum == GL11.GL_ALPHA_TEST && textureUnit == 0) {
				alphaTest = 0;
			} else if (glEnum == GL11.GL_LIGHTING) {
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
		public void glTangent4f(float x, float y, float z, float w) {
			int baseOffset = tangentCount * STRIDE;
			ensureCapacity(baseOffset + STRIDE);
			pushFloat(baseOffset + 8, x);
			pushFloat(baseOffset + 9, y);
			pushFloat(baseOffset + 10, z);
			pushFloat(baseOffset + 11, w);
			tangentCount++;
		}

		@Override
		public void glActiveHDTexture(int textureUnit) {
			this.textureUnit = textureUnit;
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		}

		@Override
		public void glViewport(int x, int y, int w, int h) {
			this.viewportWidth = w;
			this.viewportHeight = h;
			GL11.glViewport(x, y, w, h);
		}

	}

	public static final class FixedFunctionPipeline implements Pipeline {
		@Override
		public void glBegin(int type) {
			GL11.glBegin(type);
		}

		@Override
		public void glVertex3f(float x, float y, float z) {
			GL11.glVertex3f(x, y, z);
		}

		@Override
		public void glEnd() {
			GL11.glEnd();
		}

		@Override
		public void glPolygonMode(int face, int mode) {
			GL11.glPolygonMode(face, mode);
		}

		@Override
		public void glColor4f(float r, float g, float b, float a) {
			GL11.glColor4f(r, g, b, a);
		}

		@Override
		public void glNormal3f(float x, float y, float z) {
			GL11.glNormal3f(x, y, z);
		}

		@Override
		public void glTexCoord2f(float u, float v) {
			GL11.glTexCoord2f(u, v);
		}

		@Override
		public void glColor3f(float r, float g, float b) {
			GL11.glColor3f(r, g, b);
		}

		@Override
		public void glFresnelColor3f(float r, float g, float b) {
		}

		@Override
		public void glColor4ub(byte r, byte g, byte b, byte a) {
			GL11.glColor4ub(r, g, b, a);
		}

		@Override
		public void glLight(int light, int pname, FloatBuffer params) {
			GL11.glLight(light, pname, params);
		}

		@Override
		public void glRotatef(float a, float b, float c, float d) {
			GL11.glRotatef(a, b, c, d);
		}

		@Override
		public void glScalef(float x, float y, float z) {
			GL11.glScalef(x, y, z);
		}

		@Override
		public void glTranslatef(float x, float y, float z) {
			GL11.glTranslatef(x, y, z);
		}

		@Override
		public void glOrtho(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
			GL11.glOrtho(xMin, xMax, yMin, yMax, zMin, zMax);
		}

		@Override
		public void gluPerspective(float fovY, float aspect, float nearClip, float farClip) {
			GLU.gluPerspective(fovY, aspect, nearClip, farClip);
		}

		@Override
		public void glLightModel(int lightModel, FloatBuffer ambientColor) {
			GL11.glLightModel(lightModel, ambientColor);
		}

		@Override
		public void glMatrixMode(int mode) {
			GL11.glMatrixMode(mode);
		}

		@Override
		public void glLoadIdentity() {
			GL11.glLoadIdentity();
		}

		@Override
		public void glEnableIfNeeded(int glEnum) {
			GL11.glEnable(glEnum);
		}

		@Override
		public void glShadeModel(int glFlat) {
			GL11.glShadeModel(glFlat);
		}

		@Override
		public void glDisableIfNeeded(int glEnum) {
			GL11.glDisable(glEnum);
		}

		@Override
		public void prepareToBindTexture() {
		}

		@Override
		public void onGlobalPipelineSet() {
		}

		@Override
		public void glTangent4f(float x, float y, float z, float w) {
			// tangents are not applicable to old style drawing
		}

		@Override
		public void glActiveHDTexture(int textureUnit) {
			// TODO Auto-generated method stub

		}

		@Override
		public void glViewport(int x, int y, int w, int h) {
			GL11.glViewport(x, y, w, h);
		}

		@Override
		public void glFresnelTeamColor1f(float v) {
		}

		@Override
		public void glFresnelOpacity1f(float v) {
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

	}
}
