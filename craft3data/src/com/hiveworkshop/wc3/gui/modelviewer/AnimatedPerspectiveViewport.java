package com.hiveworkshop.wc3.gui.modelviewer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHT1;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LIGHT_MODEL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.rms.editor.render3d.NGGLDP;
import com.hiveworkshop.rms.editor.render3d.NGGLDP.Pipeline;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.GPUReadyTexture;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.ProgramPreferencesChangeListener;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.lwjgl.BetterAWTGLCanvas;
import com.hiveworkshop.wc3.gui.modelviewer.AnimationControllerListener.LoopType;
import com.hiveworkshop.wc3.gui.modelviewer.camera.PortraitCameraManager;
import com.hiveworkshop.wc3.gui.util.GlTextureRef;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.LayerShader;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.InternalInstance;
import com.hiveworkshop.wc3.mdl.render3d.InternalResource;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.render3d.RenderParticleEmitter2;
import com.hiveworkshop.wc3.mdl.render3d.RenderResourceAllocator;
import com.hiveworkshop.wc3.mdl.render3d.RenderRibbonEmitter;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.util.MathUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

public class AnimatedPerspectiveViewport extends BetterAWTGLCanvas implements MouseListener, ActionListener,
		MouseWheelListener, AnimatedRenderEnvironment, RenderResourceAllocator {
	public static final float NORMAL_RENDER_LENGTH = 0.025f;
	public static final boolean LOG_EXCEPTIONS = true;
	ModelView modelView;
	private RenderModel renderModel;
	Point lastClick;
	Point leftClickStart;
	Timer clickTimer = new Timer(16, this);
	Timer paintTimer;
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	boolean enabled = false;

	boolean texLoaded = false;

	JCheckBox wireframe;
	private NGGLDP.Pipeline pipeline;
	HashMap<Bitmap, GlTextureRef> textureMap = new HashMap<>();

	Class<? extends Throwable> lastThrownErrorClass;
	private final ProgramPreferences programPreferences;

	private int animationTime;
	private boolean live;
	private boolean looping = true;
	private Animation animation;
	private long lastUpdateMillis = System.currentTimeMillis();
	private long lastExceptionTimeMillis = 0;

	private float backgroundRed, backgroundBlue, backgroundGreen;

	private int levelOfDetail;
	private final ViewerCamera viewerCamera;
	private final PortraitCameraManager cameraManager;

	public AnimatedPerspectiveViewport(final ModelView modelView, final ProgramPreferences programPreferences,
			final boolean loadDefaultCamera) throws LWJGLException {
		super();
		this.programPreferences = programPreferences;
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		// Viewport border
		// setBorder(BorderFactory.createBevelBorder(1));
		setBackground(programPreferences == null ? new Color(80, 80, 80)
				: programPreferences.getPerspectiveBackgroundColor());
		setMinimumSize(new Dimension(200, 200));
		// add(Box.createHorizontalStrut(200));
		// add(Box.createVerticalStrut(200));
		// setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		this.modelView = modelView;
		viewerCamera = new ViewerCamera();
		cameraManager = new PortraitCameraManager();
		cameraManager.setupCamera(viewerCamera);
		if (loadDefaultCamera) {
			loadDefaultCameraFor(modelView, true);
		}
		this.renderModel = new RenderModel(modelView.getModel(), null);
		renderModel.setSpawnParticles(
				(programPreferences.getRenderParticles() == null) || programPreferences.getRenderParticles());
		renderModel.refreshFromEditor(this, viewerCamera, this);
		addMouseListener(this);
		addMouseWheelListener(this);

		if (programPreferences != null) {
			programPreferences.addChangeListener(new ProgramPreferencesChangeListener() {
				@Override
				public void preferencesChanged() {
					setBackground(programPreferences.getPerspectiveBackgroundColor() == null ? new Color(80, 80, 80)
							: programPreferences.getPerspectiveBackgroundColor());
					loadBackgroundColors();
				}
			});
		}
		loadBackgroundColors();
		paintTimer = new Timer(16, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				repaint();
				if (!isShowing()) {
					paintTimer.stop();
				}
			}

		});
		paintTimer.start();
	}

	private void loadDefaultCameraFor(final ModelView modelView, final boolean loadDefaultAnimation) {
		ExtLog extents = null;
		final ArrayList<CollisionShape> collisionShapes = modelView.getModel().sortedIdObjects(CollisionShape.class);
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
			extents = modelView.getModel().getExtents();
		}
		Animation defaultAnimation = null;
		for (final Animation animation : modelView.getModel().getAnims()) {
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
			this.animation = defaultAnimation;
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

	public void setModel(final ModelView modelView) {
		setAnimation(null);
		this.modelView = modelView;
		this.renderModel = new RenderModel(modelView.getModel(), null);
		renderModel.refreshFromEditor(this, viewerCamera, this);
		if (modelView.getModel().getAnims().size() > 0) {
			setAnimation(modelView.getModel().getAnim(0));
		}
		reloadAllTextures();
	}

	public void setAnimation(final Animation animation) {
		this.animation = animation;
		animationTime = 0;
		lastUpdateMillis = System.currentTimeMillis();
		if (animation != null) {
			renderModel.refreshFromEditor(this, viewerCamera, this);
		}
		else {
			renderModel.refreshFromEditor(this, viewerCamera, this);
		}
		if (loopType == LoopType.DEFAULT_LOOP) {
			final boolean loopingState = animation == null ? false : !animation.isNonLooping();
			looping = loopingState;
		}
	}

	public void setWireframeHandler(final JCheckBox nwireframe) {
		wireframe = nwireframe;
	}

	boolean wantReload = false;

	public void reloadTextures() {
		wantReload = true;
	}

	boolean wantReloadAll = false;

	public void reloadAllTextures() {
		wantReloadAll = true;
	}

	public void forceReloadTextures() {
		texLoaded = true;
		// for( Bitmap tex: textureMap.keySet())
		// {
		// //GL11.glDeleteTextures(textureMap.get(tex));
		// }
		// initGL();
		renderModel.refreshFromEditor(this, viewerCamera, this);

		deleteAllTextures();
		for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				final Layer layer = geo.getMaterial().getLayers().get(i);
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					final Bitmap shaderTexture = layer.getShaderTextures().get(shaderTextureTypeHD);
					if (shaderTexture != null) {
						loadToTexMap(layer, shaderTexture);
					}
				}
				if (layer.getTextures() != null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(layer, tex);
					}
				}
			}
		}
	}

	private void deleteAllTextures() {
		for (final GlTextureRef textureRef : textureMap.values()) {
			pipeline.setCurrentPipeline(textureRef.pipelineId);
			GL11.glDeleteTextures(textureRef.textureId);
		}
		textureMap.clear();
	}

	public void loadToTexMap(final Layer layer, final Bitmap tex) {
		pipeline.setCurrentPipeline(layer.getLayerShader().ordinal());
		loadToTexMap((layer.getFilterMode() == FilterMode.MODULATE) || (layer.getFilterMode() == FilterMode.MODULATE2X),
				tex);
	}

	public void loadToTexMap(boolean alpha, final Bitmap tex) {
		alpha = true;
		final int formatVersion = modelView.getModel().getFormatVersion();
		if (textureMap.get(tex) == null) {
			String path = tex.getPath();
			if (path.length() == 0) {
				if (tex.getReplaceableId() == 1) {
					path = "ReplaceableTextures\\TeamColor\\TeamColor" + Material.getTeamColorNumberString();
				}
				else if (tex.getReplaceableId() == 2) {
					path = "ReplaceableTextures\\TeamGlow\\TeamGlow" + Material.getTeamColorNumberString();
				}
				else if (tex.getReplaceableId() == 11) {
					path = "ReplaceableTextures\\Cliff\\Cliff0";
				}
				else if (tex.getReplaceableId() != 0) {
					path = "replaceabletextures\\lordaerontree\\lordaeronsummertree";
				}
				if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
						&& programPreferences.getAllowLoadingNonBlpTextures()) {
					path += ".blp";
				}
			}
			else if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
					&& programPreferences.getAllowLoadingNonBlpTextures()) {
			}
			else {
				path = path.substring(0, path.length() - 4);
			}
			Integer texture = null;
			try {
				final DataSource workingDirectory = modelView.getModel().getWrappedDataSource();
				if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
						&& programPreferences.getAllowLoadingNonBlpTextures()) {
					texture = loadTexture(BLPHandler.get().loadTexture(workingDirectory, path), tex, alpha,
							formatVersion);
				}
				else {
					texture = loadTexture(BLPHandler.get().loadTexture(workingDirectory, path + ".blp"), tex, alpha,
							formatVersion);
				}
			}
			catch (final Exception exc) {
				if (LOG_EXCEPTIONS) {
					exc.printStackTrace();
				}
			}
			if (texture != null) {
				textureMap.put(tex, new GlTextureRef(texture, pipeline.getCurrentPipelineIndex()));
			}
		}
	}

	public void addGeosets(final List<Geoset> geosets) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				final Layer layer = geo.getMaterial().getLayers().get(i);
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					final Bitmap shaderTexture = layer.getShaderTextures().get(shaderTextureTypeHD);
					if (shaderTexture != null) {
						loadToTexMap(layer, shaderTexture);
					}
				}
				if (layer.getTextures() != null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(layer, tex);
					}
				}
			}
		}
	}

	public void setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	public void setAnimationTime(final int trackTime) {
		this.animationTime = trackTime;
	}

	public void setLive(final boolean live) {
		this.live = live;
	}

	public void setLooping(final boolean looping) {
		this.looping = looping;
	}

	private Pipeline getOrCreatePipeline() {
		if (pipeline == null) {
			pipeline = new NGGLDP.ShaderSwitchingPipeline(
					Arrays.asList(new NGGLDP.SimpleDiffuseShaderPipeline(), new NGGLDP.HDDiffuseShaderPipeline()));
			pipeline.setCurrentPipeline(0);
		}
		return pipeline;
	}

	@Override
	public void initGL() {
		NGGLDP.setPipeline(getOrCreatePipeline());
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				texLoaded = true;
				deleteAllTextures();
				for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
					for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
						final Layer layer = geo.getMaterial().getLayers().get(i);
						for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
							final Bitmap shaderTexture = layer.getShaderTextures().get(shaderTextureTypeHD);
							if (shaderTexture != null) {
								loadToTexMap(layer, shaderTexture);
							}
						}
						if (layer.getTextures() != null) {
							for (final Bitmap tex : layer.getTextures()) {
								loadToTexMap(layer, tex);
							}
						}
					}
				}
				renderModel.refreshFromEditor(this, viewerCamera, this);
			}
			// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
			// old library code and java std library code give me a metric for the
			// ridiculous ratio:
			xRatio = (float) (Display.getDisplayMode().getWidth()
					/ Toolkit.getDefaultToolkit().getScreenSize().getWidth());
			yRatio = (float) (Display.getDisplayMode().getHeight()
					/ Toolkit.getDefaultToolkit().getScreenSize().getHeight());
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
			final int imageWidth = (int) (getWidth() * xRatio);
			final int imageHeight = (int) (getHeight() * yRatio);
			final BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			// paintComponent(image.getGraphics(),5);
			final Pbuffer buffer = new Pbuffer(imageWidth, imageHeight, new PixelFormat(), null, null);
			buffer.makeCurrent();
			final ByteBuffer pixels = ByteBuffer.allocateDirect(imageWidth * imageHeight * 4);
			initGL();
			paintGL(false);
			GL11.glReadPixels(0, 0, imageWidth, imageHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
			final int[] array = new int[pixels.capacity() / 4];
			pixels.asIntBuffer().get(array);
			for (int i = 0; i < array.length; i++) {
				final int rgba = array[i];
				final int a = rgba & 0xFF;
				array[i] = (rgba >>> 8) | (a << 24);
			}
			image.getRaster().setDataElements(0, 0, imageWidth, imageHeight, array);
			buffer.releaseContext();
			return createFlipped(image);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static BufferedImage createFlipped(final BufferedImage image) {
		final AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
		return createTransformed(image, at);
	}

	private static BufferedImage createTransformed(final BufferedImage image, final AffineTransform at) {
		final BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = newImage.createGraphics();
		g.transform(at);
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
	}

	boolean initialized = false;

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	private final Vector4f vertexHeap = new Vector4f();
	private final Vector4f appliedVertexHeap = new Vector4f();
	private final Vector4f vertexSumHeap = new Vector4f();
	private final Vector4f normalHeap = new Vector4f();
	private final Vector3f normalHeap3 = new Vector3f();
	private final Vector4f appliedNormalHeap = new Vector4f();
	private final Vector4f normalSumHeap = new Vector4f();
	private final Vector3f normalSumHeap3 = new Vector3f();
	private final Matrix4f skinBonesMatrixSumHeap = new Matrix4f();
	private final Matrix3f skinBonesMatrixSumHeap3 = new Matrix3f();
	private final Vector3f screenDimension = new Vector3f();
	private final Matrix3f screenDimensionMat3Heap = new Matrix3f();
	private final Quaternion quatHeap = new Quaternion();
	private final Vector3f vertexHeap3 = new Vector3f();
	private final Vector3f uvTranslationHeap3 = new Vector3f();
	private final Vector3f uvScaleHeap3 = new Vector3f();

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
		NGGLDP.setPipeline(pipeline);
		setSize(getParent().getSize());
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload
			// some.
			pipeline.discard();
			pipeline = null;
			try {
				initGL();// Re-overwrite textures
			}
			catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading textures:", e);
			}
		}
		else if (wantReload) {
			wantReload = false;
			try {
				forceReloadTextures();
			}
			catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading new texture:", e);
			}
		}
		else if (!texLoaded && ((programPreferences == null) || programPreferences.textureModels())) {
			forceReloadTextures();
			texLoaded = true;
		}
		try {
			final int formatVersion = modelView.getModel().getFormatVersion();
			if (live) {
				final long currentTimeMillis = System.currentTimeMillis();
				if ((currentTimeMillis - lastExceptionTimeMillis) > 16) {
					if ((animation != null) && (animation.length() > 0)) {
						if (looping) {
							animationTime = (int) ((animationTime
									+ (long) ((currentTimeMillis - lastUpdateMillis) * animSpeed))
									% animation.length());
						}
						else {
							animationTime = Math.min(animation.length(),
									(int) (animationTime + ((currentTimeMillis - lastUpdateMillis) * animSpeed)));
						}
					}
					renderModel.updateNodes(false, true);
					lastUpdateMillis = currentTimeMillis;
				}
			}

			if ((programPreferences != null) && (programPreferences.viewMode() == 0)) {
				NGGLDP.pipeline.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			}
			else if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
				NGGLDP.pipeline.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}
			NGGLDP.pipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
			viewerCamera.viewport(0, 0, getWidth() * xRatio, getHeight() * yRatio);
			GL11.glEnable(GL_DEPTH_TEST);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			NGGLDP.pipeline.glEnableIfNeeded(GL_COLOR_MATERIAL);
			NGGLDP.pipeline.glEnableIfNeeded(GL_LIGHTING);
			NGGLDP.pipeline.glEnableIfNeeded(GL_LIGHT0);
			NGGLDP.pipeline.glEnableIfNeeded(GL_LIGHT1);
			NGGLDP.pipeline.glEnableIfNeeded(GL_NORMALIZE);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			// System.out.println("max:
			// "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
			if (renderTextures()) {
				NGGLDP.pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);
			// glClearColor(0f, 0f, 0f, 1.0f);
			NGGLDP.pipeline.glMatrixMode(GL_PROJECTION);
			NGGLDP.pipeline.glLoadIdentity();
//			NGGLDP.pipeline.gluPerspective(45f, (float) getWidth() / (float) getHeight(), 20.0f, 60000.0f);
			// GLU.gluOrtho2D(45f, (float)current_width/(float)current_height,
			// 1.0f, 600.0f);
			// glRotatef(angle, 0, 0, 0);
			// glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
			// gluOrtho2D(0.0f, (float) getWidth(), 0.0f, (float) getHeight());
			// GLU.
			GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			NGGLDP.pipeline.glMatrixMode(GL_MODELVIEW);
			NGGLDP.pipeline.glLoadIdentity();
			// GL11.glShadeModel(GL11.GL_SMOOTH);

			NGGLDP.pipeline.glCamera(viewerCamera, cameraManager.modelCamera != null);

			final FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
			ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
			// float [] ambientColor = {0.2f, 0.2f, 0.2f, 1f};
			// FloatBuffer buffer =
			// ByteBuffer.allocateDirect(ambientColor.length*8).asFloatBuffer();
			// buffer.put(ambientColor).flip();
			NGGLDP.pipeline.glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);

			final FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
			lightColor0.put(0.8f).put(0.8f).put(0.8f).put(1f).flip();
			final FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
			lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
			NGGLDP.pipeline.glLight(GL_LIGHT0, GL_DIFFUSE, lightColor0);
			NGGLDP.pipeline.glLight(GL_LIGHT0, GL_POSITION, lightPos0);

			final FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
			lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
			final FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
			lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();

			NGGLDP.pipeline.glLight(GL_LIGHT1, GL_DIFFUSE, lightColor1);
			NGGLDP.pipeline.glLight(GL_LIGHT1, GL_POSITION, lightPos1);

			// glColor3f(1f,1f,0f);
			// glColorMaterial ( GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE ) ;
			// glEnable(GL_COLOR_MATERIAL);
			final ArrayList<Geoset> geosets = modelView.getModel().getGeosets();
			render(geosets, formatVersion);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if ((programPreferences != null) && programPreferences.showNormals()) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
				NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);

				NGGLDP.pipeline.glBegin(GL11.GL_LINES);
				NGGLDP.pipeline.glColor3f(1f, 1f, 3f);
				// if( wireframe.isSelected() )
				for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
					if (ModelUtils.isLevelOfDetailSupported(formatVersion) && (geo.getLevelOfDetailName() != null)
							&& (geo.getLevelOfDetailName().length() > 0)) {
						if (geo.getLevelOfDetail() != levelOfDetail) {
							continue;
						}
					}
					for (final Triangle tri : geo.getTriangles()) {
						for (final GeosetVertex v : tri.getVerts()) {
							vertexHeap.x = (float) v.x;
							vertexHeap.y = (float) v.y;
							vertexHeap.z = (float) v.z;
							vertexHeap.w = 1;
							skinBonesMatrixSumHeap.setZero();
							final List<GeosetVertexBoneLink> boneAttachments = v.getLinks();
							boolean processedBones = false;
							float sumWeight = 0;
							for (int boneIndex = 0; boneIndex < boneAttachments.size(); boneIndex++) {
								final GeosetVertexBoneLink link = boneAttachments.get(boneIndex);
								final Bone skinBone = link.bone;
								if (skinBone == null) {
									continue;
								}
								processedBones = true;
								final Matrix4f worldMatrix = renderModel.getRenderNode(skinBone).getWorldMatrix();
								sumWeight += link.weight;
								skinBonesMatrixSumHeap.m00 += (worldMatrix.m00 * link.weight);
								skinBonesMatrixSumHeap.m01 += (worldMatrix.m01 * link.weight);
								skinBonesMatrixSumHeap.m02 += (worldMatrix.m02 * link.weight);
								skinBonesMatrixSumHeap.m03 += (worldMatrix.m03 * link.weight);
								skinBonesMatrixSumHeap.m10 += (worldMatrix.m10 * link.weight);
								skinBonesMatrixSumHeap.m11 += (worldMatrix.m11 * link.weight);
								skinBonesMatrixSumHeap.m12 += (worldMatrix.m12 * link.weight);
								skinBonesMatrixSumHeap.m13 += (worldMatrix.m13 * link.weight);
								skinBonesMatrixSumHeap.m20 += (worldMatrix.m20 * link.weight);
								skinBonesMatrixSumHeap.m21 += (worldMatrix.m21 * link.weight);
								skinBonesMatrixSumHeap.m22 += (worldMatrix.m22 * link.weight);
								skinBonesMatrixSumHeap.m23 += (worldMatrix.m23 * link.weight);
								skinBonesMatrixSumHeap.m30 += (worldMatrix.m30 * link.weight);
								skinBonesMatrixSumHeap.m31 += (worldMatrix.m31 * link.weight);
								skinBonesMatrixSumHeap.m32 += (worldMatrix.m32 * link.weight);
								skinBonesMatrixSumHeap.m33 += (worldMatrix.m33 * link.weight);
							}
							if (!processedBones) {
								skinBonesMatrixSumHeap.setIdentity();
							}
							else if (sumWeight > 0) {
								skinBonesMatrixSumHeap.m00 /= sumWeight;
								skinBonesMatrixSumHeap.m01 /= sumWeight;
								skinBonesMatrixSumHeap.m02 /= sumWeight;
								skinBonesMatrixSumHeap.m03 /= sumWeight;
								skinBonesMatrixSumHeap.m10 /= sumWeight;
								skinBonesMatrixSumHeap.m11 /= sumWeight;
								skinBonesMatrixSumHeap.m12 /= sumWeight;
								skinBonesMatrixSumHeap.m13 /= sumWeight;
								skinBonesMatrixSumHeap.m20 /= sumWeight;
								skinBonesMatrixSumHeap.m21 /= sumWeight;
								skinBonesMatrixSumHeap.m22 /= sumWeight;
								skinBonesMatrixSumHeap.m23 /= sumWeight;
								skinBonesMatrixSumHeap.m30 /= sumWeight;
								skinBonesMatrixSumHeap.m31 /= sumWeight;
								skinBonesMatrixSumHeap.m32 /= sumWeight;
								skinBonesMatrixSumHeap.m33 /= sumWeight;
							}
							Matrix4f.transform(skinBonesMatrixSumHeap, vertexHeap, vertexSumHeap);
							if (v.getNormal() != null) {
								normalHeap.x = (float) v.getNormal().x;
								normalHeap.y = (float) v.getNormal().y;
								normalHeap.z = (float) v.getNormal().z;
								normalHeap.w = 0;
								Matrix4f.transform(skinBonesMatrixSumHeap, normalHeap, normalSumHeap);

								if (normalSumHeap.length() > 0) {
									normalSumHeap.normalise();
								}
								else {
									normalSumHeap.set(0, 0, 1, 0);
								}
								if (Float.isNaN(normalSumHeap.x) || Float.isNaN(normalSumHeap.y)
										|| Float.isNaN(normalSumHeap.z) || Float.isNaN(normalSumHeap.w)
										|| Float.isInfinite(normalSumHeap.x) || Float.isInfinite(normalSumHeap.y)
										|| Float.isInfinite(normalSumHeap.z) || Float.isInfinite(normalSumHeap.w)) {
									continue;
								}

								NGGLDP.pipeline.glNormal3f(normalSumHeap.x, normalSumHeap.y, normalSumHeap.z);
								NGGLDP.pipeline.glVertex3f(vertexSumHeap.x, vertexSumHeap.y, vertexSumHeap.z);

								NGGLDP.pipeline.glNormal3f(normalSumHeap.x, normalSumHeap.y, normalSumHeap.z);
								NGGLDP.pipeline.glVertex3f(
										vertexSumHeap.x
												+ (normalSumHeap.x * NORMAL_RENDER_LENGTH * cameraManager.distance),
										vertexSumHeap.y
												+ (normalSumHeap.y * NORMAL_RENDER_LENGTH * cameraManager.distance),
										vertexSumHeap.z
												+ (normalSumHeap.z * NORMAL_RENDER_LENGTH * cameraManager.distance));
							}
						}
					}
				}
				NGGLDP.pipeline.glEnd();
			}
			if (renderTextures()) {
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				NGGLDP.pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			renderModel.getParticleShader().use();
			for (final RenderParticleEmitter2 particle : renderModel.getParticleEmitters2()) {
				particle.render(renderModel, renderModel.getParticleShader());
			}
			for (final RenderRibbonEmitter emitter : renderModel.getRibbonEmitters()) {
				emitter.render(renderModel, renderModel.getParticleShader());
			}

			// System.out.println("max:
			// "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));

			// glPopMatrix();
			if (autoRepainting) {
				swapBuffers();
				final boolean showing = isShowing();
				final boolean running = paintTimer.isRunning();
				if (showing && !running) {
					paintTimer.restart();
				}
				else if (!showing && running) {
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

	int popupCount = 0;
	// public void paintGL() {
	// super.paintGL();
	// try {
	// if( !initialized )
	// {
	// initGL();
	// initialized = true;
	// }
	// System.out.println("printingGL");
	//// makeCurrent();
	// GL11.glBegin(GL11.GL_QUADS);
	// GL11.glColor3f(0f,1f,1f);
	// for (Geoset geo : dispMDL.getMDL().getGeosets()) {
	// for (Triangle tri : geo.m_triangle) {
	// for (Vertex v : tri.m_verts) {
	// GL11.glVertex3f((float) v.x, (float) v.y, (float) v.z);
	// }
	// }
	// }
	// GL11.glEnd();
	//// swapBuffers();
	// } catch (Exception e) {
	// // Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public void render(final ArrayList<Geoset> geosets, final int formatVersion) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, true, formatVersion);
		}
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, false, formatVersion);
		}
	}

	public void render(final Geoset geo, final boolean renderOpaque, final int formatVersion) {
		if (ModelUtils.isLevelOfDetailSupported(formatVersion) && (geo.getLevelOfDetailName() != null)
				&& (geo.getLevelOfDetailName().length() > 0)) {
			if (geo.getLevelOfDetail() != levelOfDetail) {
				return;
			}
		}
		final GeosetAnim geosetAnim = geo.getGeosetAnim();
		float geosetAnimVisibility = 1;
		if ((animation != null) && (geosetAnim != null)) {
			geosetAnimVisibility = geosetAnim.getRenderVisibility(this);
			if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
				return;
			}
		}
		final Material material = geo.getMaterial();
		for (int i = 0; i < material.getLayers().size(); i++) {
			final Layer layer = material.getLayers().get(i);
			final boolean isHD = layer.getLayerShader() == LayerShader.HD;

			final FilterMode filterMode = layer.getFilterMode();
			final boolean opaqueLayer = (filterMode == FilterMode.NONE) || ((filterMode == FilterMode.TRANSPARENT)
					&& ((layer.getLayerShader() != LayerShader.HD) || (layer.getRenderVisibility(this) > 0.99)));

			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				pipeline.setCurrentPipeline(layer.getLayerShader().ordinal());
				if (isHD) {
					boolean first = true;
					for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
						final Bitmap shaderTexture = layer.getRenderTexture(this, modelView.getModel(),
								shaderTextureTypeHD);
						if (shaderTexture != null) {
							NGGLDP.pipeline.glActiveHDTexture(shaderTextureTypeHD.ordinal());

							if (first) {
								first = false;
								bindLayer(layer, shaderTexture, textureMap.get(shaderTexture), formatVersion, material);
							}
							else {
								bindTexture(shaderTexture, textureMap.get(shaderTexture));
							}
						}
					}
				}
				else {
					final Bitmap tex = layer.getRenderTexture(this, modelView.getModel(), ShaderTextureTypeHD.Diffuse);
					final GlTextureRef texture = textureMap.get(tex);
					bindLayer(layer, tex, texture, formatVersion, material);
				}
			}

			if (animation != null) {
				final float layerVisibility = layer.getRenderVisibility(this);
				final float alphaValue = geosetAnimVisibility * layerVisibility;
				if (/* geo.getMaterial().isConstantColor() && */ geosetAnim != null) {
					final Vector3f renderColor = geosetAnim.getRenderColor(this);
					if (renderColor != null) {
						if (layer.getFilterMode() == FilterMode.ADDITIVE) {
							NGGLDP.pipeline.glColor4f(renderColor.z * alphaValue, renderColor.y * alphaValue,
									renderColor.x * alphaValue, alphaValue);
						}
						else {
							NGGLDP.pipeline.glColor4f(renderColor.z * 1f, renderColor.y * 1f, renderColor.x * 1f,
									alphaValue);
						}
					}
					else {
						NGGLDP.pipeline.glColor4f(1f, 1f, 1f, alphaValue);
					}
				}
				else {
					NGGLDP.pipeline.glColor4f(1f, 1f, 1f, alphaValue);
				}

			}
			else {
				NGGLDP.pipeline.glColor4f(1f, 1f, 1f, 1f);
			}
			if (isHD) {
				final Vertex fresnelColor = layer.getRenderFresnelColor(this);
				if (fresnelColor != null) {
					NGGLDP.pipeline.glFresnelColor3f((float) fresnelColor.z, (float) fresnelColor.y,
							(float) fresnelColor.x);
				}
				else {
					NGGLDP.pipeline.glFresnelColor3f(1f, 1f, 1f);
				}
				NGGLDP.pipeline.glFresnelTeamColor1f(layer.getRenderFresnelTeamColor(this));
				NGGLDP.pipeline.glFresnelOpacity1f(layer.getRenderFresnelOpacity(this));
				NGGLDP.pipeline.glEmissiveGain1f(layer.getRenderEmissiveGain(this));
			}
//			if (renderOpaque && (filterMode == FilterMode.ADDITIVE)) {
//				GL11.glColorMask(true, true, true, true);
//			} else {
//				GL11.glColorMask(false, false, false, false);
//			}
			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				NGGLDP.pipeline.glBegin(GL11.GL_TRIANGLES);
				for (final Triangle tri : geo.getTriangles()) {
					for (final GeosetVertex v : tri.getVerts()) {
						vertexHeap.x = (float) v.x;
						vertexHeap.y = (float) v.y;
						vertexHeap.z = (float) v.z;
						vertexHeap.w = 1;
						skinBonesMatrixSumHeap.setZero();
						final List<GeosetVertexBoneLink> boneAttachments = v.getLinks();
						boolean processedBones = false;
						float sumWeight = 0;
						for (int boneIndex = 0; boneIndex < boneAttachments.size(); boneIndex++) {
							final GeosetVertexBoneLink link = boneAttachments.get(boneIndex);
							final Bone skinBone = link.bone;
							if (skinBone == null) {
								continue;
							}
							processedBones = true;
							final Matrix4f worldMatrix = renderModel.getRenderNode(skinBone).getWorldMatrix();

							sumWeight += link.weight;
							skinBonesMatrixSumHeap.m00 += (worldMatrix.m00 * link.weight);
							skinBonesMatrixSumHeap.m01 += (worldMatrix.m01 * link.weight);
							skinBonesMatrixSumHeap.m02 += (worldMatrix.m02 * link.weight);
							skinBonesMatrixSumHeap.m03 += (worldMatrix.m03 * link.weight);
							skinBonesMatrixSumHeap.m10 += (worldMatrix.m10 * link.weight);
							skinBonesMatrixSumHeap.m11 += (worldMatrix.m11 * link.weight);
							skinBonesMatrixSumHeap.m12 += (worldMatrix.m12 * link.weight);
							skinBonesMatrixSumHeap.m13 += (worldMatrix.m13 * link.weight);
							skinBonesMatrixSumHeap.m20 += (worldMatrix.m20 * link.weight);
							skinBonesMatrixSumHeap.m21 += (worldMatrix.m21 * link.weight);
							skinBonesMatrixSumHeap.m22 += (worldMatrix.m22 * link.weight);
							skinBonesMatrixSumHeap.m23 += (worldMatrix.m23 * link.weight);
							skinBonesMatrixSumHeap.m30 += (worldMatrix.m30 * link.weight);
							skinBonesMatrixSumHeap.m31 += (worldMatrix.m31 * link.weight);
							skinBonesMatrixSumHeap.m32 += (worldMatrix.m32 * link.weight);
							skinBonesMatrixSumHeap.m33 += (worldMatrix.m33 * link.weight);
						}
						if (!processedBones) {
							skinBonesMatrixSumHeap.setIdentity();
						}
						else if (sumWeight > 0) {
							skinBonesMatrixSumHeap.m00 /= sumWeight;
							skinBonesMatrixSumHeap.m01 /= sumWeight;
							skinBonesMatrixSumHeap.m02 /= sumWeight;
							skinBonesMatrixSumHeap.m03 /= sumWeight;
							skinBonesMatrixSumHeap.m10 /= sumWeight;
							skinBonesMatrixSumHeap.m11 /= sumWeight;
							skinBonesMatrixSumHeap.m12 /= sumWeight;
							skinBonesMatrixSumHeap.m13 /= sumWeight;
							skinBonesMatrixSumHeap.m20 /= sumWeight;
							skinBonesMatrixSumHeap.m21 /= sumWeight;
							skinBonesMatrixSumHeap.m22 /= sumWeight;
							skinBonesMatrixSumHeap.m23 /= sumWeight;
							skinBonesMatrixSumHeap.m30 /= sumWeight;
							skinBonesMatrixSumHeap.m31 /= sumWeight;
							skinBonesMatrixSumHeap.m32 /= sumWeight;
							skinBonesMatrixSumHeap.m33 /= sumWeight;
						}
						Matrix4f.transform(skinBonesMatrixSumHeap, vertexHeap, vertexSumHeap);
						skinBonesMatrixSumHeap3.m00 = skinBonesMatrixSumHeap.m00;
						skinBonesMatrixSumHeap3.m01 = skinBonesMatrixSumHeap.m01;
						skinBonesMatrixSumHeap3.m02 = skinBonesMatrixSumHeap.m02;
						skinBonesMatrixSumHeap3.m10 = skinBonesMatrixSumHeap.m10;
						skinBonesMatrixSumHeap3.m11 = skinBonesMatrixSumHeap.m11;
						skinBonesMatrixSumHeap3.m12 = skinBonesMatrixSumHeap.m12;
						skinBonesMatrixSumHeap3.m20 = skinBonesMatrixSumHeap.m20;
						skinBonesMatrixSumHeap3.m21 = skinBonesMatrixSumHeap.m21;
						skinBonesMatrixSumHeap3.m22 = skinBonesMatrixSumHeap.m22;
						if (v.getNormal() != null) {
							normalHeap3.x = (float) v.getNormal().x;
							normalHeap3.y = (float) v.getNormal().y;
							normalHeap3.z = (float) v.getNormal().z;
							Matrix3f.transform(skinBonesMatrixSumHeap3, normalHeap3, normalSumHeap3);

							if (normalSumHeap3.length() > 0) {
								normalSumHeap3.normalise();
							}
							else {
								normalSumHeap3.set(0, 0, 1);
							}
							NGGLDP.pipeline.glNormal3f(normalSumHeap3.x, normalSumHeap3.y, normalSumHeap3.z);
						}
						if (v.getTangent() != null) {
							normalHeap3.set(v.getTangent()[0], v.getTangent()[1], v.getTangent()[2]);
							Matrix3f.transform(skinBonesMatrixSumHeap3, normalHeap3, normalSumHeap3);

							NGGLDP.pipeline.glTangent4f(normalSumHeap3.x, normalSumHeap3.y, normalSumHeap3.z,
									v.getTangent()[3]);
						}
						else {
							NGGLDP.pipeline.glTangent4f(0, 0, 1, 1);
						}
						int coordId = layer.getCoordId();
						if (coordId >= v.getTverts().size()) {
							coordId = v.getTverts().size() - 1;
						}
						final TextureAnim textureAnim = layer.getTextureAnim();
						if (textureAnim != null) {
							vertexHeap.set((float) v.getTverts().get(coordId).x, (float) v.getTverts().get(coordId).y,
									0, 1);
							final QuaternionRotation renderRotation = textureAnim.getRenderRotation(this);
							quatHeap.set((float) renderRotation.a, (float) renderRotation.b, (float) renderRotation.c,
									(float) renderRotation.d);
							final Vertex renderTranslation = textureAnim.getRenderTranslation(this);
							uvTranslationHeap3.set((float) renderTranslation.x, (float) renderTranslation.y,
									(float) renderTranslation.z);
							final Vertex renderScale = textureAnim.getRenderScale(this);
							uvScaleHeap3.set((float) renderScale.x, (float) renderScale.y, (float) renderScale.z);
							MathUtils.fromRotationTranslationScale(quatHeap, uvTranslationHeap3, uvScaleHeap3,
									skinBonesMatrixSumHeap);
							Matrix4f.transform(skinBonesMatrixSumHeap, vertexHeap, vertexHeap);
							NGGLDP.pipeline.glTexCoord2f(vertexHeap.x, vertexHeap.y);
						}
						else {
							NGGLDP.pipeline.glTexCoord2f((float) v.getTverts().get(coordId).x,
									(float) v.getTverts().get(coordId).y);
						}
						NGGLDP.pipeline.glVertex3f(vertexSumHeap.x, vertexSumHeap.y, vertexSumHeap.z);
					}
				}
				// if( texture != null )
				// {
				// texture.release();
				// }
				NGGLDP.pipeline.glEnd();
			}
		}

	}

	public void bindLayer(final Layer layer, final Bitmap tex, final GlTextureRef texture, final int formatVersion,
			final Material parent) {
		bindTexture(tex, texture);
		boolean depthMask = false;
		switch (layer.getFilterMode()) {
		case BLEND:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
		case ADDALPHA:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case MODULATE:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			break;
		case MODULATE2X:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			break;
		case NONE:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			depthMask = true;
			break;
		case TRANSPARENT:
			NGGLDP.pipeline.glEnableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.75f);
			GL11.glDisable(GL11.GL_BLEND);
			depthMask = true;
			break;
		}
		if (layer.isTwoSided()
				|| (ModelUtils.isShaderStringSupported(formatVersion) && parent.getFlags().contains("TwoSided"))) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		if (layer.isNoDepthTest()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
		else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		if (layer.isNoDepthSet()) {
			GL11.glDepthMask(false);
		}
		else {
			GL11.glDepthMask(depthMask);
		}
//		GL11.glColorMask(layer.getFilterMode() == FilterMode.ADDITIVE, layer.getFilterMode() == FilterMode.ADDITIVE,
//				layer.getFilterMode() == FilterMode.ADDITIVE, layer.getFilterMode() == FilterMode.ADDITIVE);
		if (layer.isUnshaded()) {
			NGGLDP.pipeline.glDisableIfNeeded(GL_LIGHTING);
		}
		else {
			NGGLDP.pipeline.glEnableIfNeeded(GL_LIGHTING);
		}
	}

	private void bindTexture(final Bitmap tex, final GlTextureRef texture) {
		NGGLDP.pipeline.prepareToBindTexture();
		if (texture != null) {
			// texture.bind();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.textureId);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		}
		else if (textureMap.size() > 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		}
	}

	public void bindLayer(final ParticleEmitter2 particle2, final Bitmap tex, final GlTextureRef texture) {
		bindTexture(tex, texture);
		switch (particle2.getFilterModeReallyBadReallySlow()) {
		case Blend:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case Additive:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case AlphaKey:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case Modulate:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			break;
		case Modulate2x:
			NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			break;
		}
		if (particle2.isUnshaded()) {
			NGGLDP.pipeline.glDisableIfNeeded(GL_LIGHTING);
		}
		else {
			NGGLDP.pipeline.glEnableIfNeeded(GL_LIGHTING);
		}
	}

	// public void paintComponent(Graphics g)
	// {
	// paintComponent(g,1);
	// }
	// public void paintComponent(Graphics g, int vertexSize)
	// {
	// super.paintComponent(g);
	// dispMDL.drawGeosets(g,this,vertexSize);
	// dispMDL.drawPivots(g,this,vertexSize);
	// switch((int)m_d1)
	// {
	// case 0: g.setColor( new Color( 0, 255, 0 ) ); break;
	// case 1: g.setColor( new Color( 255, 0, 0 ) ); break;
	// case 2: g.setColor( new Color( 0, 0, 255 ) ); break;
	// }
	// //g.setColor( new Color( 255, 0, 0 ) );
	// g.drawLine((int)Math.round(convertX(0)),(int)Math.round(convertY(0)),(int)Math.round(convertX(5)),(int)Math.round(convertY(0)));
	//
	// switch((int)m_d2)
	// {
	// case 0: g.setColor( new Color( 0, 255, 0 ) ); break;
	// case 1: g.setColor( new Color( 255, 0, 0 ) ); break;
	// case 2: g.setColor( new Color( 0, 0, 255 ) ); break;
	// }
	// //g.setColor( new Color( 255, 0, 0 ) );
	// g.drawLine((int)Math.round(convertX(0)),(int)Math.round(convertY(0)),(int)Math.round(convertX(0)),(int)Math.round(convertY(5)));
	//
	// //Visual effects from user controls
	// int xoff = 0;
	// int yoff = 0;
	// Component temp = this;
	// while( temp != null )
	// {
	// xoff+=temp.getX();
	// yoff+=temp.getY();
	// if( temp.getClass() == ModelPanel.class )
	// {
	// temp = MainFrame.panel;
	// }
	// else
	// {
	// temp = temp.getParent();
	// }
	// }
	//
	// try {
	// double mx =
	// (MouseInfo.getPointerInfo().getLocation().x-xoff);//MainFrame.frame.getX()-8);
	// double my =
	// (MouseInfo.getPointerInfo().getLocation().y-yoff);//MainFrame.frame.getY()-30);
	//
	// //SelectionBox:
	// if( selectStart != null )
	// {
	// Point sEnd = new Point((int)mx,(int)my);
	// Rectangle2D.Double r = pointsToRect(selectStart,sEnd);
	// g.setColor(MDLDisplay.selectColor);
	// ((Graphics2D)g).draw(r);
	// }
	// }
	// catch (Exception exc)
	// {
	// JOptionPane.showMessageDialog(null,"Error retrieving mouse coordinates.
	// (Probably not a major issue. Due to sleep mode?)");
	// }
	// }

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == clickTimer) {
			final int xoff = 0;
			final int yoff = 0;
			// Component temp = this;
			// while (temp != null) {
			// xoff += temp.getX();
			// yoff += temp.getY();
			// if (temp.getClass() == ModelPanel.class) {
			// temp = MainFrame.panel;
			// } else {
			// temp = temp.getParent();
			// }
			// }
			final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				final double mx = pointerInfo.getLocation().x - xoff;// MainFrame.frame.getX()-8);
				final double my = pointerInfo.getLocation().y - yoff;// MainFrame.frame.getY()-30);
				// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
				// "+lastClick.x+","+lastClick.y+" as last.");
				// System.out.println(xoff+" and "+mx);

				if (lastClick != null) {
					final double dx = mx - lastClick.x;
					final double dy = my - lastClick.y;

					applyPan(dx, dy);

					lastClick.x = (int) mx;
					lastClick.y = (int) my;
				}
				if (leftClickStart != null) {

					final double dx = mx - leftClickStart.x;
					final double dy = my - leftClickStart.y;
					cameraManager.horizontalAngle -= Math.toRadians(dy);
					cameraManager.verticalAngle -= Math.toRadians(dx);
					leftClickStart.x = (int) mx;
					leftClickStart.y = (int) my;
				}
				// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

			}
		}
	}

	private void applyPan(final double dx, final double dy) {
		this.screenDimension.set(-1, 0, 0);
		MathUtils.copy(cameraManager.camera.viewProjectionMatrix, screenDimensionMat3Heap);
		screenDimensionMat3Heap.transpose();
		Matrix3f.transform(screenDimensionMat3Heap, screenDimension, screenDimension);
		screenDimension.normalise();
		screenDimension.scale((float) dx * 0.008f * cameraManager.distance);
		Vector3f.add(cameraManager.target, screenDimension, cameraManager.target);
		this.screenDimension.set(0, 1, 0);
		MathUtils.copy(cameraManager.camera.viewProjectionMatrix, screenDimensionMat3Heap);
		screenDimensionMat3Heap.transpose();
		Matrix3f.transform(screenDimensionMat3Heap, screenDimension, screenDimension);
		screenDimension.normalise();
		screenDimension.scale((float) dy * 0.008f * cameraManager.distance);
		Vector3f.add(cameraManager.target, screenDimension, cameraManager.target);
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		clickTimer.setRepeats(true);
		clickTimer.start();
		mouseInBounds = true;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if ((leftClickStart == null) && (lastClick == null)) {
			clickTimer.stop();
		}
		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			lastClick = new Point(e.getXOnScreen(), e.getYOnScreen());
		}
		else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
			leftClickStart = new Point(e.getXOnScreen(), e.getYOnScreen());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			final double dx = e.getXOnScreen() - lastClick.x;
			final double dy = e.getYOnScreen() - lastClick.y;
			applyPan(dx, dy);
			lastClick = null;
		}
		else if (programPreferences.getThreeDCameraSpinButton().isButton(e) && (leftClickStart != null)) {
			final Point selectEnd = new Point(e.getX(), e.getY());
			// System.out.println(area);
			// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
			leftClickStart = null;
		}
		if (!mouseInBounds && (leftClickStart == null) && (lastClick == null)) {
			clickTimer.stop();
		}
		/*
		 * if( dispMDL != null ) dispMDL.refreshUndo();
		 */
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		final boolean neg = wr < 0;

		if (neg) {
			wr = -wr;
		}
		if (e.isControlDown()) {
			for (int i = 0; i < wr; i++) {
				if (neg) {
					cameraManager.setFarClip(cameraManager.getFarClip() / 1.15f);
				}
				else {
					cameraManager.setFarClip(cameraManager.getFarClip() * 1.15f);
				}
			}
		}
		else {
			for (int i = 0; i < wr; i++) {
				if (neg) {
					// cameraPos.x -= (mx - getWidth() / 2)
					// * (1 / m_zoom - 1 / (m_zoom * 1.15));
					// cameraPos.y -= (my - getHeight() / 2)
					// * (1 / m_zoom - 1 / (m_zoom * 1.15));
					// cameraPos.z -= (getHeight() / 2)
					// * (1 / m_zoom - 1 / (m_zoom * 1.15));
					cameraManager.distance /= 1.15;
				}
				else {
					cameraManager.distance *= 1.15;
					// cameraPos.x -= (mx - getWidth() / 2)
					// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
					// cameraPos.y -= (my - getHeight() / 2)
					// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
					// cameraPos.z -= (getHeight() / 2)
					// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
				}
			}
		}
	}

	private static final int BYTES_PER_PIXEL = 4;
	private LoopType loopType;
	private float animSpeed = 1.0f;
	private float xRatio;
	private float yRatio;

	public static int loadTexture(final GPUReadyTexture texture, final Bitmap bitmap, final boolean alpha,
			final int formatVersion) {
		if (texture == null) {
			return -1;
		}
		final ByteBuffer buffer = texture.getBuffer();
		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		final int textureID = GL11.glGenTextures(); // Generate texture ID
		NGGLDP.pipeline.prepareToBindTexture();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				bitmap.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				bitmap.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0,
				GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
	}

	@Override
	public int getAnimationTime() {
		return animationTime;
	}

	@Override
	public Animation getCurrentAnimation() {
		return animation;
	}

	@Override
	public int getGlobalSeqTime(final int globalSeqLength) {
		if (globalSeqLength == 0) {
			return 0;
		}
		return (int) (lastUpdateMillis % globalSeqLength);
	}

	public void setLoopType(final LoopType loopType) {
		this.loopType = loopType;
		final Animation currentAnimation = animation;
		switch (loopType) {
		case ALWAYS_LOOP:
			looping = true;
			break;
		case DEFAULT_LOOP:
			final boolean loopingState = currentAnimation == null ? false : !currentAnimation.isNonLooping();
			looping = loopingState;
			break;
		case NEVER_LOOP:
			looping = false;
			break;
		}
	}

	public void setAnimationSpeed(final float speed) {
		this.animSpeed = speed;

	}

	private final class Particle2TextureInstance implements InternalResource, InternalInstance {
		private final Bitmap bitmap;
		private final ParticleEmitter2 particle;
		private boolean loaded = false;

		public Particle2TextureInstance(final Bitmap bitmap, final ParticleEmitter2 particle) {
			this.bitmap = bitmap;
			this.particle = particle;
		}

		@Override
		public void setTransformation(final Vector3f worldLocation, final Quaternion rotation,
				final Vector3f worldScale) {
		}

		@Override
		public void setSequence(final int index) {

		}

		@Override
		public void show() {
		}

		@Override
		public void setPaused(final boolean paused) {

		}

		@Override
		public void move(final Vector3f deltaPosition) {

		}

		@Override
		public void hide() {
		}

		@Override
		public void bind() {
			pipeline.setCurrentPipeline(0);
			if (!loaded) {
				loadToTexMap((particle.getFilterModeReallyBadReallySlow() == ParticleEmitter2.FilterMode.Modulate)
						&& (particle.getFilterModeReallyBadReallySlow() == ParticleEmitter2.FilterMode.Modulate2x),
						bitmap);
				loaded = true;
			}
			final GlTextureRef texture = textureMap.get(bitmap);
			bindLayer(particle, bitmap, texture);
		}

		@Override
		public InternalInstance addInstance() {
			return this;
		}

	}

	private final class RibbonEmitterMaterialInstance implements InternalResource, InternalInstance {
		private final Material material;
		private final RibbonEmitter ribbonEmitter;
		private boolean loaded = false;

		public RibbonEmitterMaterialInstance(final Material material, final RibbonEmitter ribbonEmitter) {
			this.material = material;
			this.ribbonEmitter = ribbonEmitter;
		}

		@Override
		public void setTransformation(final Vector3f worldLocation, final Quaternion rotation,
				final Vector3f worldScale) {
		}

		@Override
		public void setSequence(final int index) {

		}

		@Override
		public void show() {
		}

		@Override
		public void setPaused(final boolean paused) {

		}

		@Override
		public void move(final Vector3f deltaPosition) {

		}

		@Override
		public void hide() {
		}

		@Override
		public void bind() {
			pipeline.setCurrentPipeline(0);
			if (!loaded) {
				for (int i = 0; i < material.getLayers().size(); i++) {
					final Layer layer = material.getLayers().get(i);
					for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
						final Bitmap shaderTexture = layer.getShaderTextures().get(shaderTextureTypeHD);
						if (shaderTexture != null) {
							loadToTexMap(layer, shaderTexture);
						}
					}
					if (layer.getTextures() != null) {
						for (final Bitmap tex : layer.getTextures()) {
							loadToTexMap(layer, tex);
						}
					}
				}
				loaded = true;
			}

			// TODO support multi layer ribbon
			final Layer layer = material.getLayers().get(0);
			final Bitmap tex = layer.getRenderTexture(AnimatedPerspectiveViewport.this, modelView.getModel(),
					ShaderTextureTypeHD.Diffuse);
			final GlTextureRef texture = textureMap.get(tex);
			bindLayer(layer, tex, texture, 800, material);
		}

		@Override
		public InternalInstance addInstance() {
			return this;
		}

	}

	@Override
	public InternalResource allocateTexture(final Bitmap bitmap, final ParticleEmitter2 textureSource) {
		return new Particle2TextureInstance(bitmap, textureSource);
	}

	@Override
	public InternalResource allocateMaterial(final Material material, final RibbonEmitter ribbonEmitter) {
		return new RibbonEmitterMaterialInstance(material, ribbonEmitter);
	}

	public void setSpawnParticles(final boolean b) {
		renderModel.setSpawnParticles(b);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		// This clears the pipeline in case of shaders... It clears out VBO/VAO so that
		// it will auto create new ones.
		// Without this, for example, some stuff tries to use the old VBO/VAO that are
		// no longer valid and we get
		// messed up drawing. super in this case is calling destroy on OpenGL context
		// entirely, that's why.
		// But this method was happening during the app lifecycle of clearing and
		// resetting the UI views when we
		// open a new model.
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
		loadDefaultCameraFor(modelView, false);
	}

	public Camera createCameraFromCurrentView() {

		return new Camera("Camera01",
				new Vertex(viewerCamera.location.x, viewerCamera.location.y, viewerCamera.location.z),
				new Vertex(viewerCamera.location.x - viewerCamera.directionZ.x,
						viewerCamera.location.y - viewerCamera.directionZ.y,
						viewerCamera.location.z - viewerCamera.directionZ.z),
				(viewerCamera.getFov() * 4.0f) / 3.0f, viewerCamera.getFarClipPlane(), viewerCamera.getNearClipPlane());
	}

}