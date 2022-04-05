package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.blp.GPUReadyTexture;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.BetterAWTGLCanvas;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class AnimatedPerspectiveViewport extends BetterAWTGLCanvas {
	public static final boolean LOG_EXCEPTIONS = true;
	private ModelHandler modelHandler;
	private RenderModel renderModel;
	private Timer paintTimer;

	private boolean texLoaded = false;

	private boolean wireframe;
	private SimpleDiffuseShaderPipeline pipeline;

	Class<? extends Throwable> lastThrownErrorClass;
	private final ProgramPreferences programPreferences;

	private long lastUpdateMillis = System.currentTimeMillis();
	private long lastExceptionTimeMillis = 0;

	private float backgroundRed, backgroundBlue, backgroundGreen;

	private int levelOfDetail;
	private final ViewerCamera viewerCamera;
	private final PortraitCameraManager cameraManager;

	MouseThingi mouseThingi;
	GeosetRenderThing geosetRenderThing;

	private TextureThing textureThing;

	private float animSpeed = 1.0f;
	private float xRatio;
	private float yRatio;

	private final Vec4 vertexHeap = new Vec4();
	private final Vec4 colorHeap = new Vec4();
	private final Vec4 normalHeap = new Vec4();
	private final Vec4 normalSumHeap = new Vec4();
	private final Mat4 skinBonesMatrixHeap = new Mat4();
	private final Vec3 screenDimension = new Vec3();
	private final Mat4 screenDimensionMat3Heap = new Mat4();

	boolean wantReload = false;
	boolean wantReloadAll = false;

	int popupCount = 0;

	public AnimatedPerspectiveViewport() throws LWJGLException {
		super();
		this.programPreferences = ProgramGlobals.getPrefs();
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		// Viewport border
		// setBorder(BorderFactory.createBevelBorder(1));
		setBackground(programPreferences == null ? new Color(80, 80, 80) : programPreferences.getPerspectiveBackgroundColor());
		setMinimumSize(new Dimension(200, 200));
		// add(Box.createHorizontalStrut(200));
		// add(Box.createVerticalStrut(200));
		// setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		viewerCamera = new ViewerCamera();
		cameraManager = new PortraitCameraManager();
		cameraManager.setupCamera(viewerCamera);

		mouseThingi = new MouseThingi(cameraManager);
		addMouseListener(mouseThingi);
		addMouseWheelListener(mouseThingi);

		geosetRenderThing = new GeosetRenderThing(cameraManager);

//
		loadBackgroundColors();
		paintTimer = new Timer(16, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}

		});
		paintTimer.start();
	}

	public void refreshStuff() {
		renderModel.refreshFromEditor(textureThing);
	}

	public ViewerCamera getViewerCamera() {
		return viewerCamera;
	}

	public PortraitCameraManager getPortraitCameraManager() {
		return cameraManager;
	}

	private void loadDefaultCameraFor(EditableModel model, final boolean loadDefaultAnimation) {
		ExtLog extents = null;
		List<CollisionShape> collisionShapes = model.getColliders();
		if (collisionShapes.size() > 0) {
			for (final CollisionShape shape : collisionShapes) {
				if ((shape != null) && (shape.getExtents() != null) && shape.getExtents().hasBoundsRadius()) {
					extents = shape.getExtents();
				}
			}
			final CollisionShape firstShape = collisionShapes.get(0);
			extents = firstShape.getExtents();
		}
		if (extents == null) {
			extents = model.getExtents();
		}
		Animation defaultAnimation = null;
		for (final Animation animation : model.getAnims()) {
			if ((defaultAnimation == null) || !defaultAnimation.getName().toLowerCase().contains("stand")
					|| (animation.getName().toLowerCase().contains("stand")
					&& (animation.getName().length() < defaultAnimation.getName().length()))) {
				defaultAnimation = animation;
				if ((animation.getExtents() != null) && (animation.getExtents().hasBoundsRadius()
						|| (animation.getExtents().getMinimumExtent() != null))) {
					extents = animation.getExtents();
				}
			}
		}
		if (loadDefaultAnimation) {
			renderModel.getTimeEnvironment().setSequence(defaultAnimation);
		}
		double boundsRadius = 64;
		if (extents != null) {
			if (extents.hasBoundsRadius() && (extents.getBoundsRadius() > 1)) {
				final double extBoundRadius = extents.getBoundsRadius();
				if (extBoundRadius > boundsRadius) {
					boundsRadius = extBoundRadius;
				}
			}
			if ((extents.getMaximumExtent() != null) && (extents.getMaximumExtent() != null)) {
				final double minMaxBoundRadius = extents.getMaximumExtent().distance(extents.getMinimumExtent()) / 2;
				if (minMaxBoundRadius > boundsRadius) {
					boundsRadius = minMaxBoundRadius;
				}
			}
			if ((boundsRadius > 10000) || (boundsRadius < 0.1)) {
				boundsRadius = 64;
			}
		}
		cameraManager.distance = (float) (boundsRadius * Math.sqrt(2)) * 2;
		cameraManager.target.set(0, 0, (float) boundsRadius / 2);

	}

	private void loadBackgroundColors() {
		if (getBackground() != null) {
			backgroundRed = getBackground().getRed() / 255f;
			backgroundGreen = getBackground().getGreen() / 255f;
			backgroundBlue = getBackground().getBlue() / 255f;
		}
	}

	public void setModel(ModelHandler modelHandler, RenderModel renderModel, boolean loadDefaultCamera) {
		renderModel.getTimeEnvironment().setSequence(null);
		this.modelHandler = modelHandler;
		this.renderModel = renderModel;
		if(modelHandler != null){
			textureThing = new TextureThing(modelHandler.getModel(), programPreferences);
			renderModel.setSpawnParticles((programPreferences.getRenderParticles() == null) || programPreferences.getRenderParticles());
			refreshStuff();
			if (modelHandler.getModel().getAnims().size() > 0) {
				renderModel.getTimeEnvironment().setSequence(modelHandler.getModel().getAnim(0));
			}
			if (loadDefaultCamera) {
				loadDefaultCamera();
			}
			reloadAllTextures();
		}
		geosetRenderThing.setModel(renderModel, modelHandler, textureThing);
	}

	public AnimatedPerspectiveViewport setWireFrame(boolean wireFrame) {
		this.wireframe = wireFrame;
		return this;
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
			textureThing.reMakeTextureMap();
			refreshStuff();
		}

	}

	public void setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	private SimpleDiffuseShaderPipeline getOrCreatePipeline() {
		if (pipeline == null) {
//			if ((modelView != null) && ModelUtils.isShaderStringSupported(model.getFormatVersion())
//					&& !model.getGeosets().isEmpty() && model.getGeoset(0).isHD()) {
//				pipeline = new NGGLDP.HDDiffuseShaderPipeline();
//			}
//			else {
//				pipeline = new NGGLDP.SimpleDiffuseShaderPipeline();
//			}
			pipeline = new SimpleDiffuseShaderPipeline();
		}
		return pipeline;
	}

	@Override
	public void initGL() {
		getOrCreatePipeline();
//		NGGLDP.setPipeline(getOrCreatePipeline());
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				forceReloadTextures();
			}
			// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
			// old library code and java std library code give me a metric for the
			// ridiculous ratio:
			xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
			yRatio = (float) (Display.getDisplayMode().getHeight() / Toolkit.getDefaultToolkit().getScreenSize().getHeight());
			// These ratios will be wrong and users will see corrupted visuals (bad scale,
			// only fits part of window,
			// etc) if they are using Windows 10 differing UI scale per monitor. I don't
			// think I have an API
			// to query that information yet, though.
		}
		catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	// public byte getPortFirstXYZ()
	// {
	// return m_d1;
	// }
	// public byte getPortSecondXYZ()
	// {
	// return m_d2;
	// }
	public BufferedImage getBufferedImage() {
		try {
			BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			// paintComponent(image.getGraphics(),5);
			Pbuffer buffer = new Pbuffer(getWidth(), getHeight(), new PixelFormat(), null, null);
			buffer.makeCurrent();
			ByteBuffer pixels = ByteBuffer.allocateDirect(getWidth() * getHeight() * 4);
			initGL();
			paintGL(false);
			GL11.glReadPixels(0, 0, getWidth(), getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
			final int[] array = new int[pixels.capacity() / 4];
			pixels.asIntBuffer().get(array);
			for (int i = 0; i < array.length; i++) {
				int rgba = array[i];
				int a = rgba & 0xFF;
				array[i] = (rgba >>> 8) | (a << 24);
			}
			image.getRaster().setDataElements(0, 0, getWidth(), getHeight(), array);
			buffer.releaseContext();
			return createFlipped(image);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static BufferedImage createFlipped(final BufferedImage image) {
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
		return createTransformed(image, at);
	}

	private static BufferedImage createTransformed(final BufferedImage image, final AffineTransform at) {
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.transform(at);
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
	}

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	@Override
	public void paintGL() {
		paintGL(true);
	}

	public void paintGL(final boolean autoRepainting) {
		viewerCamera.update();
		cameraManager.updateCamera();
//		NGGLDP.setPipeline(pipeline);
		setSize(getParent().getSize());
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload some.
			pipeline.discard();
			pipeline = null;
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
			texLoaded = true;
		}
		try {
			int formatVersion = modelHandler.getModel().getFormatVersion();
			if (renderModel.getTimeEnvironment().isLive()) {
				long currentTimeMillis = System.currentTimeMillis();
				if ((currentTimeMillis - lastExceptionTimeMillis) > 16) {
					renderModel.updateNodes(false, true);
					lastUpdateMillis = currentTimeMillis;
				}
			}

			if ((programPreferences != null) && (programPreferences.viewMode() == 0)) {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			} else if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
			pipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
			viewerCamera.viewport(0, 0, getWidth() * xRatio, getHeight() * yRatio);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
			pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			if (renderTextures()) {
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);

			pipeline.glMatrixMode(GL11.GL_PROJECTION);
			pipeline.glLoadIdentity();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			pipeline.glMatrixMode(GL11.GL_MODELVIEW);
			pipeline.glLoadIdentity();

			pipeline.glCamera(viewerCamera);

			setUpLights();

			geosetRenderThing.render(pipeline, formatVersion);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if ((programPreferences != null) && programPreferences.showNormals()) {
				geosetRenderThing.drawNormals(pipeline, formatVersion);
			}
			if (renderTextures()) {
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
//			renderModel.getParticleShader().use();
//			for (final RenderParticleEmitter2 particle : renderModel.getParticleEmitters2()) {
//				particle.render(renderModel, renderModel.getParticleShader());
//			}
//			for (final RenderRibbonEmitter emitter : renderModel.getRibbonEmitters()) {
//				emitter.render(renderModel, renderModel.getParticleShader());
//			}

			if (autoRepainting) {
				swapBuffers();
				boolean showing = isShowing();
				boolean running = paintTimer.isRunning();
				if (showing && !running) {
					paintTimer.restart();
				} else if (!showing && running) {
					paintTimer.stop();
				}
			}
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


	private void setUpLights() {
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


	private void setRenderColor(SimpleDiffuseShaderPipeline pipeline, GeosetAnim geosetAnim, float geosetAnimVisibility, Layer layer) {
		if (renderModel.getTimeEnvironment().getCurrentSequence() != null) {
			float layerVisibility = layer.getRenderVisibility(renderModel.getTimeEnvironment());
			float alphaValue = geosetAnimVisibility * layerVisibility;
			Vec3 renderColor = null;
			if (geosetAnim != null) {
				renderColor = geosetAnim.getRenderColor(renderModel.getTimeEnvironment());

			}
			if (renderColor != null) {
				if (layer.getFilterMode() == FilterMode.ADDITIVE) {
					pipeline.glColor4f(renderColor.z * alphaValue, renderColor.y * alphaValue, renderColor.x * alphaValue, alphaValue);
				} else {
					pipeline.glColor4f(renderColor.z, renderColor.y, renderColor.x, alphaValue);
				}
			} else {
				pipeline.glColor4f(1f, 1f, 1f, alphaValue);
			}

		} else {
			pipeline.glColor4f(1f, 1f, 1f, 1f);
		}
	}

//	public void bindLayer(final ParticleEmitter2 particle2, final Bitmap tex, final Integer texture) {
//		bindTexture(tex, texture);
//		switch (particle2.getFilterMode()) {
//			case BLEND:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//				break;
//			case ADDITIVE:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//				break;
//			case ALPHAKEY:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//				break;
//			case MODULATE:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
//				break;
//			case MODULATE2X:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
//				break;
//		}
//		if (particle2.getUnshaded()) {
//			pipeline.glDisableIfNeeded(GL11.GL_LIGHTING);
//		}
//		else {
//			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
//		}
//	}

	public static int loadTexture(SimpleDiffuseShaderPipeline pipeline, GPUReadyTexture texture, Bitmap bitmap, boolean alpha, int formatVersion) {
		if (texture == null) {
			return -1;
		}
		ByteBuffer buffer = texture.getBuffer();
		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		int textureID = GL11.glGenTextures(); // Generate texture ID
		pipeline.prepareToBindTexture();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, bitmap.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, bitmap.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
	}


//	private final class Particle2TextureInstance implements InternalResource, InternalInstance {
//		private final Bitmap bitmap;
//		private final ParticleEmitter2 particle;
//		private boolean loaded = false;
//
//		public Particle2TextureInstance(final Bitmap bitmap, final ParticleEmitter2 particle) {
//			this.bitmap = bitmap;
//			this.particle = particle;
//		}
//
//		@Override
//		public void setTransformation(final Vec3 worldLocation, final Quat rotation,
//		                              final Vec3 worldScale) {
//		}
//
//		@Override
//		public void setSequence(final int index) {
//
//		}
//
//		@Override
//		public void show() {
//		}
//
//		@Override
//		public void setPaused(final boolean paused) {
//
//		}
//
//		@Override
//		public void move(final Vec3 deltaPosition) {
//
//		}
//
//		@Override
//		public void hide() {
//		}
//
//		@Override
//		public void bind() {
//			if (!loaded) {
//				loadToTexMap((particle.getFilterModeReallyBadReallySlow() == MdlxParticleEmitter2.FilterMode.MODULATE)
//								&& (particle.getFilterModeReallyBadReallySlow() == MdlxParticleEmitter2.FilterMode.MODULATE2X),
//						bitmap);
//				loaded = true;
//			}
//			final Integer texture = textureMap.get(bitmap);
//			bindLayer(particle, bitmap, texture);
//		}
//
//		@Override
//		public InternalInstance addInstance() {
//			return this;
//		}
//
//	}
//
//	private final class RibbonEmitterMaterialInstance implements InternalResource, InternalInstance {
//		private final Material material;
//		private final RibbonEmitter ribbonEmitter;
//		private boolean loaded = false;
//
//		public RibbonEmitterMaterialInstance(final Material material, final RibbonEmitter ribbonEmitter) {
//			this.material = material;
//			this.ribbonEmitter = ribbonEmitter;
//		}
//
//		@Override
//		public void setTransformation(final Vec3 worldLocation, final Quat rotation,
//		                              final Vec3 worldScale) {
//		}
//
//		@Override
//		public void setSequence(final int index) {
//
//		}
//
//		@Override
//		public void show() {
//		}
//
//		@Override
//		public void setPaused(final boolean paused) {
//
//		}
//
//		@Override
//		public void move(final Vec3 deltaPosition) {
//
//		}
//
//		@Override
//		public void hide() {
//		}
//
//		@Override
//		public void bind() {
//			if (!loaded) {
//				for (int i = 0; i < material.getLayers().size(); i++) {
//					final Layer layer = material.getLayers().get(i);
//					if (layer.getTextureBitmap() != null) {
//						loadToTexMap(layer, layer.getTextureBitmap());
//					}
//					if (layer.getTextures() != null) {
//						for (final Bitmap tex : layer.getTextures()) {
//							loadToTexMap(layer, tex);
//						}
//					}
//				}
//				loaded = true;
//			}
//
//			// TODO support multi layer ribbon
//			final Layer layer = material.getLayers().get(0);
//			final Bitmap tex = layer.getRenderTexture(AnimatedPerspectiveViewport.this, model);
//			final Integer texture = textureMap.get(tex);
//			bindLayer(layer, tex, texture, 800, material);
//		}
//
//		@Override
//		public InternalInstance addInstance() {
//			return this;
//		}
//
//	}
//
//	@Override
//	public InternalResource allocateTexture(final Bitmap bitmap, final ParticleEmitter2 textureSource) {
//		return new Particle2TextureInstance(bitmap, textureSource);
//	}
//
//	@Override
//	public InternalResource allocateMaterial(final Material material, final RibbonEmitter ribbonEmitter) {
//		return new RibbonEmitterMaterialInstance(material, ribbonEmitter);
//	}

	public void setSpawnParticles(final boolean b) {
		renderModel.setSpawnParticles(b);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		// This clears the pipeline in case of shaders...
		// It clears out VBO/VAO so that it will auto create new ones.
		// Without this, for example, some stuff tries to use the
		// old VBO/VAO that are no longer valid and we get messed up drawing.
		// super in this case is calling destroy on OpenGL context entirely,
		// that's why. But this method was happening during the app lifecycle of
		// clearing and resetting the UI views when we open a new model.
		pipeline = null;
	}

	public PortraitCameraManager getCameraManager() {
		return cameraManager;
	}

	public void setCamera(final Camera camera) {
		cameraManager.setModelInstance(renderModel, camera);
	}

	public void loadDefaultCamera() {
		cameraManager.setModelInstance(null, null);
		loadDefaultCameraFor(modelHandler.getModel(), false);
	}

}