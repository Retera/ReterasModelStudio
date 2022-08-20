package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.MouseListenerThing;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.GridPainter2;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.application.viewer.ViewportHelpers;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class BufferFiller {
	private ModelView modelView;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;

	private boolean texLoaded = false;

	private final ShaderManager shaderManager = new ShaderManager();

	Class<? extends Throwable> lastThrownErrorClass;
	private ProgramPreferences programPreferences;

	private long lastExceptionTimeMillis = 0;

	float[] backgroundColor = new float[4];
//	private float backgroundRed, backgroundBlue, backgroundGreen;

	private int levelOfDetail;

	GeosetBufferFiller geosetBufferFiller;
	NodeBufferFiller nodeBufferFiller;
	GridPainter2 gridPainter2;
	ParticleBufferFiller particleBufferFiller;
	CameraBufferFiller cameraBufferFiller;

	private TextureThing textureThing;

	private float xRatio;
	private float yRatio;

	boolean wantReload = false;
	boolean wantReloadAll = false;

	int popupCount = 0;

	Color bgColor;

	boolean isHD = true;

	public BufferFiller(ModelView modelView, RenderModel renderModel, boolean loadDefaultSequence){
		this.programPreferences = ProgramGlobals.getPrefs();

		bgColor = programPreferences == null ? new Color(80, 80, 80) : programPreferences.getPerspectiveBackgroundColor();


		geosetBufferFiller = new GeosetBufferFiller();
		nodeBufferFiller = new NodeBufferFiller();
		gridPainter2 = new GridPainter2();
		particleBufferFiller = new ParticleBufferFiller();
		cameraBufferFiller = new CameraBufferFiller();
		setModel(modelView, renderModel, loadDefaultSequence);
	}

	public ShaderManager getShaderManager() {
		return shaderManager;
	}

	public BufferFiller setHD(boolean HD) {
		isHD = HD;
		return this;
	}

	public boolean isHD() {
		return isHD;
	}

	public void setModel(ModelView modelView, RenderModel renderModel, boolean loadDefaultSequence) {
		this.renderModel = renderModel;
		this.modelView = modelView;
		if(renderModel != null){
			renderModel.getTimeEnvironment().setSequence(null);
			EditableModel model = renderModel.getModel();
			textureThing = new TextureThing(programPreferences);
			renderEnv = renderModel.getTimeEnvironment();

			if (loadDefaultSequence) {
				renderEnv.setSequence(ViewportHelpers.findDefaultAnimation(model));
			}

			renderModel.refreshFromEditor();
			reloadAllTextures();
		} else {
			textureThing = null;
			renderEnv = null;
			modelView = null;
		}
		geosetBufferFiller.setModel(renderModel, modelView, textureThing);
		nodeBufferFiller.setModel(renderModel, modelView);
		particleBufferFiller.setModel(textureThing, renderModel);
		cameraBufferFiller.setModel(renderModel, modelView);
	}

	long nextUpdate = 0;
	int updateInterval = 16;
	private void setUpdateInterval(int updateInterval){
		this.updateInterval = updateInterval;
	}
	private void runUpdate(){
		long millis = System.currentTimeMillis();
		if(nextUpdate < millis){
			nextUpdate = millis + updateInterval;
			if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
				System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
				return;
			}
			textureThing.clearQued();
			reloadIfNeeded();
			updateRenderModel();
			fillBuffers();
		}
	}
	public void forceUpdate(){
		long millis = System.currentTimeMillis();
		nextUpdate = millis + updateInterval*3L;
		renderModel.updateAnimationTime().updateNodes2(programPreferences.getRenderParticles());
		if (modelView.isGeosetsVisible()) {
			renderModel.updateGeosets();
		}
		fillBuffers();
	}


	public void initGL() {
		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline(isHD);
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				forceReloadTextures();
			}
			// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
			// old library code and java std library code give me a metric for the
			// ridiculous ratio:
			DisplayMode displayMode = Display.getDisplayMode();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			xRatio = (float) (displayMode.getWidth() / screenSize.getWidth());
			yRatio = (float) (displayMode.getHeight() / screenSize.getHeight());
//			System.out.println("Display.getDisplayMode(): " + displayMode.getWidth() + "x" + displayMode.getHeight());
//			System.out.println("Toolkit.getDefaultToolkit(): " + screenSize.getWidth() + "x" + screenSize.getHeight());

			// These ratios will be wrong and users will see corrupted visuals (bad scale, only fits part of window,
			// etc) if they are using Windows 10 differing UI scale per monitor. I don't think I have an API
			// to query that information yet, though.
		}
		catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
	}
	public void paintCanvas(ViewportSettings viewportSettings, CameraManager cameraManager, MouseListenerThing mouseAdapter, Component viewportCanvas) {
		runUpdate();
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		try {

			paintCanvas(cameraManager, viewportSettings, mouseAdapter, viewportCanvas);

		}
		catch (final Throwable e) {
			e.printStackTrace();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
				lastThrownErrorClass = e.getClass();
				popupCount++;
				if (popupCount < 10) {
					ExceptionPopup.display(e);
				}
			}
			throw new RuntimeException(e);
		}
	}

	private void fillBuffers(){

		programPreferences.getPerspectiveBackgroundColor().getColorComponents(backgroundColor);
		if(renderModel != null){
			geosetBufferFiller.fillBuffer(shaderManager.getOrCreatePipeline(isHD), true);

			nodeBufferFiller.fillBuffer(shaderManager.getOrCreateBoneMarkerShaderPipeline());

			geosetBufferFiller.fillNormalsBuffer(shaderManager.getOrCreateNormPipeline());
			geosetBufferFiller.fillVertsBuffer(shaderManager.getOrCreateVertPipeline());

			if (programPreferences.showPerspectiveGrid()) {
				gridPainter2.fillGridBuffer(shaderManager.getOrCreateGridPipeline());
			}

			cameraBufferFiller.fillBuffer(shaderManager.getOrCreateCameraShaderPipeline());

//			if (programPreferences != null && programPreferences.getRenderParticles()) {
//				particleBufferFiller.fillParticleHeap();
//			}
		}
	}

	public void paintCanvas(CameraManager cameraManager, ViewportSettings viewportSettings, MouseListenerThing mouseAdapter, Component viewportCanvas) {
		paintCanvas(cameraManager, viewportSettings, mouseAdapter, viewportCanvas.getWidth(), viewportCanvas.getHeight());
	}
	public void paintCanvas(CameraManager cameraManager, ViewportSettings viewportSettings, MouseListenerThing mouseAdapter, int width, int height) {

		cameraManager.updateCamera();


		// https://learnopengl.com/Advanced-OpenGL/Advanced-GLSL "Uniform buffer objects"
		GL11.glViewport(0, 0, width, height);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDepthMask(true);


		clearViewport();
		setUpLights();


		if(renderModel != null){
			ShaderPipeline pipeline = shaderManager.getOrCreatePipeline(isHD);
			RendererThing1.renderGeosets(cameraManager, pipeline, width, height, viewportSettings.isWireFrame(), viewportSettings.isRenderTextures());

			if(viewportSettings.isShowNodes()){
				ShaderPipeline boneMarkerShaderPipeline = shaderManager.getOrCreateBoneMarkerShaderPipeline();
				RendererThing1.renderNodes(cameraManager, boneMarkerShaderPipeline, width, height);
			}

			RendererThing1.renderCameras(cameraManager, shaderManager.getOrCreateCameraShaderPipeline(), width, height);


			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);


			if (viewportSettings.isShowNormals()) {
				RendererThing1.renderNormals(cameraManager, shaderManager.getOrCreateNormPipeline(), width, height);
			}
			if (viewportSettings.isShow3dVerts()) {
				RendererThing1.render3DVerts(cameraManager, shaderManager.getOrCreateVertPipeline(), width, height);
			}


			if (mouseAdapter != null && mouseAdapter.isSelecting()) {
				ShaderPipeline selectionPipeline = shaderManager.getOrCreateSelectionPipeline();
				RendererThing1.fillSelectionBoxBuffer(mouseAdapter, selectionPipeline);
				RendererThing1.paintSelectionBox(cameraManager, selectionPipeline, width, height);
			}

			if (programPreferences.showPerspectiveGrid()) {
				RendererThing1.paintGrid(cameraManager, shaderManager.getOrCreateGridPipeline(), width, height);
			}

			if (programPreferences != null && programPreferences.getRenderParticles()) {
				renderParticles(cameraManager, width, height);
			}
		}
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	private void clearViewport() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], 0.0f);
	}

	private void renderParticles(CameraManager cameraManager, int width, int height) {
		ShaderPipeline pipeline = shaderManager.getOrCreateParticleShaderPipeline();
		pipeline.glViewport(width, height);
		pipeline.glSetViewProjectionMatrix(cameraManager.getViewProjectionMatrix());
		pipeline.glSetViewMatrix(cameraManager.getViewMat());
		pipeline.glSetProjectionMatrix(cameraManager.getProjectionMat());
		for(RenderParticleEmitter2 emitter2 : renderModel.getRenderParticleEmitters2()) {
			pipeline.prepare();
			particleBufferFiller.fillParticleHeap(pipeline, emitter2);
			pipeline.doRender(GL11.GL_TRIANGLES);
		}
	}

	public ByteBuffer paintGL2(CameraManager cameraManager, ViewportSettings viewportSettings, int width, int height) {
		try {
			paintCanvas(cameraManager, viewportSettings, null, width, height);

			ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);
			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);

			return pixels;
		} catch (final Throwable e) {
			e.printStackTrace();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
				lastThrownErrorClass = e.getClass();
				popupCount++;
				if (popupCount < 10) {
					ExceptionPopup.display(e);
				}
			}
			throw new RuntimeException(e);
		}
	}

	private void setUpLights() {
		FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
		ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ambientColor);

		FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
		lightColor0.put(0.8f).put(0.8f).put(0.8f).put(1f).flip();
		FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
		lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightColor0);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos0);

		FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
		lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
		FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
		lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();

		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, lightColor1);
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPos1);
	}

	private void setUpLights1(ShaderPipeline pipeline) {
		FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
		ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
		pipeline.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ambientColor);

		FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
		lightColor0.put(0.8f).put(0.8f).put(0.8f).put(1f).flip();
		FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
		lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
		pipeline.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightColor0);
		pipeline.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos0);

		FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
		lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
		FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
		lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();

		pipeline.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, lightColor1);
		pipeline.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPos1);
	}

	private void updateRenderModel() {
		if (renderEnv.isLive()) {
//			renderEnv.updateAnimationTime();
//			renderModel.updateNodes(false, programPreferences.getRenderParticles());
			renderModel.updateAnimationTime().updateNodes2(programPreferences.getRenderParticles());
		} else if (ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
//			renderModel.updateNodes(false, programPreferences.getRenderParticles());
//			renderModel.updateNodes2(programPreferences.getRenderParticles());
			renderModel.updateNodes(programPreferences.getRenderParticles());
		}
		if (modelView.isGeosetsVisible()) {
			renderModel.updateGeosets();
		}
	}



	private void reloadIfNeeded() {
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload some.
			shaderManager.discardPipelines();
			try {
				initGL();// Re-overwrite textures
			} catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading textures:", e);
			}
		} else if (wantReload) {
			wantReload = false;
			try {
				forceReloadTextures();
			}
			catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading new texture:", e);
			}
		} else if (!texLoaded && ((programPreferences == null) || programPreferences.textureModels())) {
			forceReloadTextures();
//			doReloadTextures();
			texLoaded = true;
		}
	}
	public void reloadTextures() {
		wantReload = true;
	}

	public void reloadAllTextures() {
		wantReloadAll = true;
	}

	public void forceReloadTextures() {
		texLoaded = true;
		if (textureThing != null && renderModel != null) {
//			textureThing.reMakeTextureMap(renderModel.getModel());
			textureThing.clearTextureMap();

			renderModel.refreshFromEditor();
		}

	}
	public void doReloadTextures() {
		texLoaded = true;
		if (textureThing != null) {
			textureThing.clearTextureMap();
		}

	}

	public void setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
		geosetBufferFiller.setLod(levelOfDetail);
	}

	public void clearTextureMap(){
		System.out.println("clearing textureMap for " + modelView.getModel().getName());
		texLoaded = false;
		textureThing.queClear();
//		textureThing.clearTextureMap();
	}
}
