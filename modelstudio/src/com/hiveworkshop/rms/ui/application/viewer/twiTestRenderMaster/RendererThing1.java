package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.viewer.MouseListenerThing;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

public class RendererThing1 {

	public static void renderNormals(CameraManager cameraManager, ViewportCanvas viewportCanvas, ShaderPipeline normPipeline) {
		int width = viewportCanvas.getWidth();
		int height = viewportCanvas.getHeight();
		renderNormals(cameraManager, normPipeline, width, height);
	}

	public static void renderNormals(CameraManager cameraManager, ShaderPipeline normPipeline, int width, int height) {
		normPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		normPipeline.glViewport(width, height);
		normPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		normPipeline.glMatrixMode(GL11.GL_PROJECTION);
		normPipeline.glLoadIdentity();
		normPipeline.glMatrixMode(GL11.GL_MODELVIEW);
		normPipeline.glLoadIdentity();

		normPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
		normPipeline.doRender(GL11.GL_POINTS);
	}

	public static void render3DVerts(CameraManager cameraManager, ViewportCanvas viewportCanvas, ShaderPipeline vertPipeline) {
		int width = viewportCanvas.getWidth();
		int height = viewportCanvas.getHeight();
		render3DVerts(cameraManager, vertPipeline, width, height);
	}

	public static void render3DVerts(CameraManager cameraManager, ShaderPipeline vertPipeline, int width, int height) {
		vertPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		vertPipeline.glViewport(width, height);
		vertPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		vertPipeline.glMatrixMode(GL11.GL_PROJECTION);
		vertPipeline.glLoadIdentity();
		vertPipeline.glMatrixMode(GL11.GL_MODELVIEW);
		vertPipeline.glLoadIdentity();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		vertPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
		vertPipeline.doRender(GL11.GL_POINTS);
	}

	public static void renderNodes(CameraManager cameraManager, ViewportCanvas viewportCanvas, ShaderPipeline bonePipeline) {
		int width = viewportCanvas.getWidth();
		int height = viewportCanvas.getHeight();
		renderNodes(cameraManager, bonePipeline, width, height);
	}

	public static void renderNodes(CameraManager cameraManager, ShaderPipeline bonePipeline, int width, int height) {
		bonePipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		bonePipeline.glViewport(width, height);
		bonePipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		bonePipeline.glMatrixMode(GL11.GL_PROJECTION);
		bonePipeline.glLoadIdentity();
		bonePipeline.glMatrixMode(GL11.GL_MODELVIEW);
		bonePipeline.glLoadIdentity();

		bonePipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		bonePipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		bonePipeline.doRender(GL11.GL_POINTS);
	}

	public static void fillNodeBuffer(ShaderPipeline bonePipeline, ModelView modelView, RenderModel renderModel) {
		bonePipeline.prepare();

		Vec4 colorHeap = new Vec4(0.0f, 0.0f, 1.0f, 1.0f);
		for (IdObject v : modelView.getVisibleIdObjects()) {
			if(modelView.shouldRender(v)){
				RenderNode2 renderNode = renderModel.getRenderNode(v);

				bonePipeline.addVert(renderNode.getPivot(), Vec3.Z_AXIS, colorHeap, Vec2.ORIGIN, colorHeap, Vec3.ZERO, getSelectionStatus(v, modelView));
			}
		}
	}


	private static int getSelectionStatus(GeosetVertex vertex, ModelView modelView){
		if(modelView.getHighlightedGeoset() != null && modelView.getHighlightedGeoset() == vertex.getGeoset()) {
			return 0;
		} else if(modelView.isEditable(vertex)){
			if (modelView.isSelected(vertex)) {
				return 1;
			} else {
				return 2;
			}
		}
//		else if (!modelView.isHidden(vertex)){
//		}
		return 3;
	}

	private static int getSelectionStatus(IdObject idObject, ModelView modelView){
		if(modelView.getHighlightedNode() != null && modelView.getHighlightedNode() == idObject) {
			return 0;
		} else if(modelView.isEditable(idObject)){
			if (modelView.isSelected(idObject)) {
				return 1;
			} else {
				return 2;
			}
		}
//		else if (!modelView.isHidden(idObject)){
//		}
		return 3;
	}
	public static void paintSelectionBox(CameraManager cameraManager, ViewportCanvas viewportCanvas, ShaderPipeline selectionPipeline) {
//		CubePainter.paintCameraLookAt(cameraHandler);
		MouseListenerThing mouseAdapter = viewportCanvas.getMouseAdapter();
		if (mouseAdapter.isSelecting()) {
			selectionPipeline.glViewport(viewportCanvas.getWidth(), viewportCanvas.getHeight());
			selectionPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			selectionPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

			selectionPipeline.glMatrixMode(GL11.GL_PROJECTION);
			selectionPipeline.glLoadIdentity();
			selectionPipeline.glMatrixMode(GL11.GL_MODELVIEW);
			selectionPipeline.glLoadIdentity();

			selectionPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

			// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			selectionPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			selectionPipeline.doRender(GL11.GL_LINES);
		}
	}
	public static void fillSelectionBoxBuffer(MouseListenerThing mouseAdapter, ShaderPipeline selectionPipeline) {
		if (mouseAdapter.isSelecting()) {

			selectionPipeline.prepare();

			selectionPipeline.addVert(mouseAdapter.getStart(), Vec3.Z_AXIS, new Vec4(), Vec2.ORIGIN, new Vec4(1, 0,0,1), Vec3.ZERO);
			selectionPipeline.addVert(mouseAdapter.getEnd(), Vec3.Z_AXIS, new Vec4(), Vec2.ORIGIN, new Vec4(1, 0,0,1), Vec3.ZERO);

//			CubePainter.paintRekt(mouseAdapter.getStartPGeo(), mouseAdapter.getEndPGeo1(), mouseAdapter.getEndPGeo2(), mouseAdapter.getEndPGeo3(), cameraHandler);
//			System.out.println("is selecting!");
		}
	}
	public static void paintGrid(CameraManager cameraManager, ViewportCanvas viewportCanvas, ShaderPipeline pipeline) {
		int width = viewportCanvas.getWidth();
		int height = viewportCanvas.getHeight();
		paintGrid(cameraManager, pipeline, width, height);
	}

	public static void paintGrid(CameraManager cameraManager, ShaderPipeline pipeline, int width, int height) {
		pipeline.glViewport(width, height);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);

		pipeline.glMatrixMode(GL11.GL_PROJECTION);
		pipeline.glLoadIdentity();
		pipeline.glMatrixMode(GL11.GL_MODELVIEW);
		pipeline.glLoadIdentity();

		pipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

		// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		pipeline.doRender(GL11.GL_LINES);
	}
}
