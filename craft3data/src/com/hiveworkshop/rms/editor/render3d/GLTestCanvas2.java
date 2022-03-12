package com.hiveworkshop.rms.editor.render3d;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class GLTestCanvas2 extends com.hiveworkshop.wc3.gui.lwjgl.BetterAWTGLCanvas {
	private float xRatio;
	private float yRatio;

	public GLTestCanvas2() throws LWJGLException {
		setPreferredSize(new Dimension(800, 600));
		repaint();
	}

	@Override
	protected void initGL() {
		NGGLDP.pipeline = new NGGLDP.SimpleDiffuseShaderPipeline();
		xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		yRatio = (float) (Display.getDisplayMode().getHeight()
				/ Toolkit.getDefaultToolkit().getScreenSize().getHeight());

	}

	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	private long lastMs;
	private float time;

	@Override
	protected void paintGL() {
		GL11.glClearColor(0f, 0f, 0.3f, 0f);
		glClear(GL_COLOR_BUFFER_BIT);
		NGGLDP.pipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));

		long currentTimeMillis = System.currentTimeMillis();
		if (lastMs != 0) {
			time += (currentTimeMillis - lastMs) / 1000f * 390f;
		}
		lastMs = currentTimeMillis;
		System.out.println(time);

		NGGLDP.pipeline.glLoadIdentity();
		NGGLDP.pipeline.gluPerspective(45f, (float) getWidth() / (float) getHeight(), -1.0f, 1.0f);
		NGGLDP.pipeline.glTranslatef(0.5f, 0, 0);
		NGGLDP.pipeline.glRotatef(time, 0.0f, 0, 1f);
		NGGLDP.pipeline.glScalef(0.5f, 0.5f, 0.5f);
		NGGLDP.pipeline.glBegin(GL11.GL_TRIANGLES);
		NGGLDP.pipeline.glVertex3f(-0.5f, -0.5f, 0.0f);
		NGGLDP.pipeline.glColor3f(1f, 0, 0);
		NGGLDP.pipeline.glVertex3f(0.5f, -0.5f, 0.0f);
		NGGLDP.pipeline.glColor3f(0f, 1f, 0f);
		NGGLDP.pipeline.glVertex3f(0.0f, 0.5f, 0.0f);
		NGGLDP.pipeline.glColor3f(0f, 0, 1f);

		NGGLDP.pipeline.glScalef(2f, 2f, 2f);
		NGGLDP.pipeline.glRotatef(-time, 0.0f, 0, 1f);
		NGGLDP.pipeline.glTranslatef(-0.5f, 0, 0);

		NGGLDP.pipeline.glVertex3f(-0.5f, -0.5f, 0.0f);
		NGGLDP.pipeline.glColor3f(1f, 1, 0);
		NGGLDP.pipeline.glVertex3f(0.5f, -0.5f, 0.0f);
		NGGLDP.pipeline.glColor3f(0f, 1f, 1f);
		NGGLDP.pipeline.glVertex3f(0.0f, 0.5f, 0.0f);
		NGGLDP.pipeline.glColor3f(1f, 0, 1f);
		
		
		NGGLDP.pipeline.glEnd();

		try {
			swapBuffers();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		repaint();
	}

}
