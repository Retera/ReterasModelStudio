package com.hiveworkshop.rms.editor.render3d;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.hiveworkshop.wc3.gui.lwjgl.BetterAWTGLCanvas;

public class GLTestCanvas extends BetterAWTGLCanvas {
	private static final String vertexShader = "#version 330 core\n"  + //
		    "layout (location = 0) in vec3 aPos;\n"+ //
		    "void main()\n"+ //
		    "{\n"+ //
		    "   gl_Position = vec4(aPos, 1.0);\n"+ //
		    "}\0";
	private static final String fragmentShader = "#version 330 core\n"+ //
		    "out vec4 FragColor;\n"+ //
		    "uniform vec4 ourColor;\n"+ //
		    "void main()\n"+ //
		    "{\n"+ //
		    "   FragColor = ourColor;\n"+ //
		    "}\n\0";

	private FloatBuffer pipelineVertexBuffer = ByteBuffer.allocateDirect(1024 * 4).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
	private int vbo, vao; // has nothing to do with "object id" of war3 models
	private int shaderProgram;
	private float xRatio;
	private float yRatio;

	public GLTestCanvas() throws LWJGLException {
		setPreferredSize(new Dimension(800, 600));
		repaint();
	}
	
	@Override
	protected void initGL() {

		xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		yRatio = (float) (Display.getDisplayMode().getHeight() / Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		

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

		pipelineVertexBuffer.clear();
		if(true) {
			pipelineVertexBuffer.put(0.5f);
			pipelineVertexBuffer.put(-0.5f);
			pipelineVertexBuffer.put(0.0f);

			pipelineVertexBuffer.put(-0.5f);
			pipelineVertexBuffer.put(-0.5f);
			pipelineVertexBuffer.put(0.0f);

			pipelineVertexBuffer.put(0.0f);
			pipelineVertexBuffer.put(0.5f);
			pipelineVertexBuffer.put(0.0f);
		} else {
			for(int i = 0; i < 9; i++) {
				pipelineVertexBuffer.put((float)(Math.random() * 500- 250));
			}
		}
		
		pipelineVertexBuffer.flip();

		vao = GL30.glGenVertexArrays();
		vbo = GL15.glGenBuffers();
		GL30.glBindVertexArray(vao);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pipelineVertexBuffer, GL15.GL_STATIC_DRAW);

		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
		GL20.glEnableVertexAttribArray(0);

		GL30.glBindVertexArray(vao);
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
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}
	
	@Override
	protected void paintGL() {
		GL11.glClearColor(0f, 0f, 0.3f, 0f);
		glClear(GL_COLOR_BUFFER_BIT);
		
		
		GL20.glUseProgram(shaderProgram);


		int vertexColorLocation = GL20.glGetUniformLocation(shaderProgram, "ourColor");
		GL20.glUniform4f(vertexColorLocation, 0.0f, (float)Math.random(), 0.0f, 1.0f);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
		
		
		
		try {
			swapBuffers();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		repaint();
	}

}
