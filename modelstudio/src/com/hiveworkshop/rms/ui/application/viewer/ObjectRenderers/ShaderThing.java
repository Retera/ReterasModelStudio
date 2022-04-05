package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.ui.application.viewer.viewportcanvas.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;

public class ShaderThing {
	ShaderProgram shaderProgram;
	private static final String vertexShader = OtherUtils.loadShader("simpleDiffuse.vert");
	private static final String fragmentShader = OtherUtils.loadShader("simpleDiffuse.frag");
//	private static final String vertexShader = OtherUtils.loadShader("vertex_basic2.vert");
//	private static final String fragmentShader = OtherUtils.loadShader("fragment_basic2.frag");
	protected boolean alphaTest = false;
	boolean useTexture;
	protected boolean lightingEnabled = true;

	public void init() throws Exception {
		shaderProgram = new ShaderProgram();

		shaderProgram.createVertexShader(vertexShader);
		shaderProgram.createFragmentShader(fragmentShader);
//		shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
//		shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
		shaderProgram.link();
	}

	public void stuff(int glBeginType, int vertexCount, int textureId){
		shaderProgram.bind();
		shaderProgram.stuff(useTexture, alphaTest, lightingEnabled, textureId);
		GL11.glDrawArrays(glBeginType, 0, vertexCount);
		shaderProgram.unbind();
	}
	public void start(int textureId){
		shaderProgram.bind();
		shaderProgram.stuff(useTexture, alphaTest, lightingEnabled, textureId);
	}
	public void end(){
		shaderProgram.unbind();
	}


//	@Override
	public void glEnableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			useTexture = true;
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
		} else if (glEnum == GL11.GL_ALPHA_TEST) {
			alphaTest = true;
		} else if (glEnum == GL11.GL_LIGHTING) {
			lightingEnabled = true;
		}
	}

//	@Override
	public void glDisableIfNeeded(int glEnum) {
		if (glEnum == GL11.GL_TEXTURE_2D) {
			useTexture = false;
			GL13.glActiveTexture(0);
		} else if (glEnum == GL11.GL_ALPHA_TEST) {
			alphaTest = false;
		} else if (glEnum == GL11.GL_LIGHTING) {
			lightingEnabled = false;
		}
	}



	public void bindTexture(FloatBuffer buffer){
		shaderProgram.bindTexture(buffer);
	}
	public void bindPos(FloatBuffer buffer){
		shaderProgram.bindPos(buffer);
	}
	public void bindNorm(FloatBuffer buffer){
		shaderProgram.bindNorm(buffer);
	}
	public void bindColor(FloatBuffer buffer){
		shaderProgram.bindColor(buffer);
	}
}
