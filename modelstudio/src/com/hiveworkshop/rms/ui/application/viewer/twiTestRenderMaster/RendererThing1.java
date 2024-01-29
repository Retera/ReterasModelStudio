package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.ui.application.viewer.MouseListenerThing;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

public class RendererThing1 {

	public static void renderGeosets(CameraManager cameraManager, ShaderPipeline pipeline,
	                                 int width, int height,
	                                 boolean wireFrame, boolean texture) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

		if (texture) {
			pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
		} else {
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		}
		if (wireFrame) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			pipeline.setPolygonMode(GL11.GL_LINE);
		} else {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			pipeline.setPolygonMode(GL11.GL_FILL);
		}

		pipeline.glViewport(width, height);


		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());

		pipeline.doRender(GL11.GL_TRIANGLES);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
	}
	public static void renderRibbons(CameraManager cameraManager, ShaderPipeline pipeline,
	                                 int width, int height,
	                                 boolean wireFrame, boolean texture) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

		if (texture) {
			pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
		} else {
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		}
		if (wireFrame) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			pipeline.setPolygonMode(GL11.GL_LINE);
		} else {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			pipeline.setPolygonMode(GL11.GL_FILL);
		}
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

		pipeline.glViewport(width, height);


		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());

		pipeline.doRender(GL11.GL_TRIANGLES);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
	}

	public static void renderParticles(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthMask(false);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		pipeline.glViewport(width, height);
		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		pipeline.doRender(GL11.GL_TRIANGLES);
	}

	public static void renderNormals(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glViewport(width, height);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		pipeline.doRender(GL11.GL_POINTS);
	}

	public static void render3DVerts(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glViewport(width, height);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);


		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		pipeline.setPolygonMode(GL11.GL_FILL);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		pipeline.doRender(GL11.GL_POINTS);
	}


	public static void renderNodes(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glViewport(width, height);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		pipeline.doRender(GL11.GL_TRIANGLES);
	}

	public static void renderCol(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glViewport(width, height);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		pipeline.doRender(GL11.GL_POINTS);
	}

	public static void renderCameras(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glViewport(width, height);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);


		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		pipeline.setPolygonMode(GL11.GL_FILL);


		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

//		pipeline.doRender(GL11.GL_POINTS);
		pipeline.doRender(GL11.GL_LINES);
	}

	public static void paintSelectionBox(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glViewport(width, height);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());

		// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		pipeline.doRender(GL11.GL_LINES);
	}

	public static void fillSelectionBoxBuffer(MouseListenerThing mouseAdapter, ShaderPipeline selectionPipeline) {
		selectionPipeline.prepare();
		selectionPipeline.setColor(new Vec4(1, 0,0,1));

		selectionPipeline.addVert(mouseAdapter.getStart());
		selectionPipeline.addVert(mouseAdapter.getEnd());
	}

	public static void paintGrid(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		pipeline.glViewport(width, height);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());

		// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01f);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		pipeline.doRender(GL11.GL_LINES, cameraManager);
	}
}
