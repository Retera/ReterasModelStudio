package com.hiveworkshop.rms.ui.application.viewer;

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
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glLightModel;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.util.glu.GLU.gluPerspective;

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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.InternalInstance;
import com.hiveworkshop.rms.editor.render3d.InternalResource;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.editor.render3d.RenderResourceAllocator;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.GPUReadyTexture;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.ui.application.viewer.AnimationControllerListener.LoopType;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.BetterAWTGLCanvas;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

public class AnimatedPerspectiveViewport extends BetterAWTGLCanvas implements MouseListener, ActionListener,
		MouseWheelListener, AnimatedRenderEnvironment, RenderResourceAllocator {
	public static final boolean LOG_EXCEPTIONS = true;
	ModelView modelView;
	private RenderModel renderModel;
	Vec3 cameraPos = new Vec3(0, 0, 0);
	Quat inverseCameraRotationQuat = new Quat();
	Quat inverseCameraRotationYSpin = new Quat();
	Quat inverseCameraRotationZSpin = new Quat();
	private final Vec4 axisHeap = new Vec4();
	double m_zoom = 1;
	Point lastClick;
	Point leftClickStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
	Timer paintTimer;
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	boolean enabled = false;

	boolean texLoaded = false;

	JCheckBox wireframe;
	HashMap<Bitmap, Integer> textureMap = new HashMap<>();

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
		if (loadDefaultCamera) {
			loadDefaultCameraFor(modelView);
		}
		renderModel = new RenderModel(modelView.getModel(), null);
		renderModel.setSpawnParticles(
				(programPreferences.getRenderParticles() == null) || programPreferences.getRenderParticles());
		renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
				inverseCameraRotationZSpin, this);
		addMouseListener(this);
		addMouseWheelListener(this);

		if (programPreferences != null) {
			programPreferences.addChangeListener(() -> {
				setBackground(programPreferences.getPerspectiveBackgroundColor() == null ? new Color(80, 80, 80)
						: programPreferences.getPerspectiveBackgroundColor());
				loadBackgroundColors();
			});
		}
		loadBackgroundColors();
		paintTimer = new Timer(16, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}
		});
		paintTimer.start();
	}

	private void loadDefaultCameraFor(final ModelView modelView) {
		ExtLog extents = null;
		final List<CollisionShape> collisionShapes = modelView.getModel().sortedIdObjects(CollisionShape.class);
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
		animation = defaultAnimation;
		if (extents != null) {
			double boundsRadius = 64;
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
			m_zoom = 128 / (boundsRadius * 2);
			cameraPos.y -= boundsRadius / 2;
		}
		yangle += 35;

		axisHeap.set(0, 1, 0, (float) Math.toRadians(yangle));
		inverseCameraRotationYSpin.setFromAxisAngle(axisHeap);
		axisHeap.set(0, 0, 1, (float) Math.toRadians(xangle));
		inverseCameraRotationZSpin.setFromAxisAngle(axisHeap);
		inverseCameraRotationYSpin.mul(inverseCameraRotationZSpin, inverseCameraRotationQuat);
		inverseCameraRotationQuat.x = -inverseCameraRotationQuat.x;
		inverseCameraRotationQuat.y = -inverseCameraRotationQuat.y;
		inverseCameraRotationQuat.z = -inverseCameraRotationQuat.z;
		inverseCameraRotationYSpin.x = -inverseCameraRotationYSpin.x;
		inverseCameraRotationYSpin.y = -inverseCameraRotationYSpin.y;
		inverseCameraRotationYSpin.z = -inverseCameraRotationYSpin.z;
		inverseCameraRotationZSpin.x = -inverseCameraRotationZSpin.x;
		inverseCameraRotationZSpin.y = -inverseCameraRotationZSpin.y;
		inverseCameraRotationZSpin.z = -inverseCameraRotationZSpin.z;
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
		renderModel = new RenderModel(modelView.getModel(), null);
		renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
				inverseCameraRotationZSpin, this);
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
			renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
					inverseCameraRotationZSpin, this);
		} else {
			renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
					inverseCameraRotationZSpin, this);
		}
		if (loopType == LoopType.DEFAULT_LOOP) {
			final boolean loopingState = animation != null && !animation.isNonLooping();
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
		renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
				inverseCameraRotationZSpin, this);

		deleteAllTextures();
		for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				if (ModelUtils.isShaderStringSupported(modelView.getModel().getFormatVersion())) {
					if ((geo.getMaterial().getShaderString() != null)
							&& (geo.getMaterial().getShaderString().length() > 0)) {
						if (i > 0) {
							break;
						}
					}
				}
				final Layer layer = geo.getMaterial().getLayers().get(i);
				if (layer.getTextureBitmap() != null) {
					loadToTexMap(layer, layer.getTextureBitmap());
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
		for (final Integer textureId : textureMap.values()) {
			GL11.glDeleteTextures(textureId);
		}
		textureMap.clear();
	}

	public void loadToTexMap(final Layer layer, final Bitmap tex) {
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
				} else if (tex.getReplaceableId() == 2) {
					path = "ReplaceableTextures\\TeamGlow\\TeamGlow" + Material.getTeamColorNumberString();
				} else if (tex.getReplaceableId() != 0) {
					path = "replaceabletextures\\lordaerontree\\lordaeronsummertree";
				}
				if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
						&& programPreferences.getAllowLoadingNonBlpTextures()) {
					path += ".blp";
				}
			} else {
				if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
						&& programPreferences.getAllowLoadingNonBlpTextures()) {
				} else {
					path = path.substring(0, path.length() - 4);
				}
			}
			Integer texture = null;
			try {
				final DataSource workingDirectory = modelView.getModel().getWrappedDataSource();
				if ((programPreferences.getAllowLoadingNonBlpTextures() != null)
						&& programPreferences.getAllowLoadingNonBlpTextures()) {
					texture = loadTexture(BLPHandler.get().loadTexture(workingDirectory, path), tex, alpha,
							formatVersion);
				} else {
					texture = loadTexture(BLPHandler.get().loadTexture(workingDirectory, path + ".blp"), tex, alpha,
							formatVersion);
				}
			} catch (final Exception exc) {
				if (LOG_EXCEPTIONS) {
					exc.printStackTrace();
				}
			}
			if (texture != null) {
				textureMap.put(tex, texture);
			}
		}
	}

	public void addGeosets(final List<Geoset> geosets) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				if (ModelUtils.isShaderStringSupported(modelView.getModel().getFormatVersion())) {
					if ((geo.getMaterial().getShaderString() != null)
							&& (geo.getMaterial().getShaderString().length() > 0)) {
						if (i > 0) {
							break;
						}
					}
				}
				final Layer layer = geo.getMaterial().getLayers().get(i);
				if (layer.getTextureBitmap() != null) {
					loadToTexMap(layer, layer.getTextureBitmap());
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
		animationTime = trackTime;
	}

	public void setLive(final boolean live) {
		this.live = live;
	}

	public void setLooping(final boolean looping) {
		this.looping = looping;
	}

	@Override
	public void initGL() {
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				texLoaded = true;
				deleteAllTextures();
				for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
					for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
						if (ModelUtils.isShaderStringSupported(modelView.getModel().getFormatVersion())) {
							if ((geo.getMaterial().getShaderString() != null)
									&& (geo.getMaterial().getShaderString().length() > 0)) {
								if (i > 0) {
									break;
								}
							}
						}
						final Layer layer = geo.getMaterial().getLayers().get(i);
						if (layer.getTextureBitmap() != null) {
							loadToTexMap(layer, layer.getTextureBitmap());
						}
						if (layer.getTextures() != null) {
							for (final Bitmap tex : layer.getTextures()) {
								loadToTexMap(layer, tex);
							}
						}
					}
				}
				renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
						inverseCameraRotationZSpin, this);
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
		} catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public void setPosition(final double a, final double b) {
		cameraPos.x = (float) a;
		cameraPos.y = (float) b;
	}

	public void translate(final double a, final double b) {
		cameraPos.x += a;
		cameraPos.y += b;
	}

	public void zoom(final double amount) {
		m_zoom *= 1 + amount;
	}

	public double getZoomAmount() {
		return m_zoom;
	}

	public Point2D.Double getDisplayOffset() {
		return new Point2D.Double(cameraPos.x, cameraPos.y);
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
			final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			// paintComponent(image.getGraphics(),5);
			final Pbuffer buffer = new Pbuffer(getWidth(), getHeight(), new PixelFormat(), null, null);
			buffer.makeCurrent();
			final ByteBuffer pixels = ByteBuffer.allocateDirect(getWidth() * getHeight() * 4);
			initGL();
			paintGL(false);
			GL11.glReadPixels(0, 0, getWidth(), getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
			final int[] array = new int[pixels.capacity() / 4];
			pixels.asIntBuffer().get(array);
			for (int i = 0; i < array.length; i++) {
				final int rgba = array[i];
				final int a = rgba & 0xFF;
				array[i] = (rgba >>> 8) | (a << 24);
			}
			image.getRaster().setDataElements(0, 0, getWidth(), getHeight(), array);
			buffer.releaseContext();
			return createFlipped(image);
		} catch (final Exception e) {
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
	private float xangle;
	private float yangle;

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	private final Vec4 vertexHeap = new Vec4();
	private final Vec4 appliedVertexHeap = new Vec4();
	private final Vec4 vertexSumHeap = new Vec4();
	private final Vec4 normalHeap = new Vec4();
	private final Vec4 appliedNormalHeap = new Vec4();
	private final Vec4 normalSumHeap = new Vec4();
	private final Mat4 skinBonesMatrixHeap = new Mat4();
	private final Mat4 skinBonesMatrixSumHeap = new Mat4();

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
		setSize(getParent().getSize());
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload
			// some.
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
			} catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading new texture:", e);
			}
		} else if (!texLoaded && ((programPreferences == null) || programPreferences.textureModels())) {
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
						} else {
							animationTime = Math.min(animation.length(),
									(int) (animationTime + ((currentTimeMillis - lastUpdateMillis) * animSpeed)));
						}
					}
					renderModel.updateNodes(false, true);
					lastUpdateMillis = currentTimeMillis;
				}
			}

			if ((programPreferences != null) && (programPreferences.viewMode() == 0)) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			} else if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}
			glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
			glEnable(GL_DEPTH_TEST);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			glEnable(GL_COLOR_MATERIAL);
			glEnable(GL_LIGHTING);
			glEnable(GL_LIGHT0);
			glEnable(GL_LIGHT1);
			glEnable(GL_NORMALIZE);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			// System.out.println("max:
			// "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
			if (renderTextures()) {
				glEnable(GL11.GL_TEXTURE_2D);
			}
			glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);
			// glClearColor(0f, 0f, 0f, 1.0f);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			gluPerspective(45f, (float) getWidth() / (float) getHeight(), 20.0f, 60000.0f);
			// GLU.gluOrtho2D(45f, (float)current_width/(float)current_height,
			// 1.0f, 600.0f);
			// glRotatef(angle, 0, 0, 0);
			// glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
			// gluOrtho2D(0.0f, (float) getWidth(), 0.0f, (float) getHeight());
			// GLU.
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			// GL11.glShadeModel(GL11.GL_SMOOTH);

			glTranslatef(0f + (cameraPos.x * (float) m_zoom), -70f - (cameraPos.y * (float) m_zoom),
					-200f - (cameraPos.z * (float) m_zoom));
			glRotatef(yangle, 1f, 0f, 0f);
			glRotatef(xangle, 0f, 1f, 0f);
			glScalef((float) m_zoom, (float) m_zoom, (float) m_zoom);

			final FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
			ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
			// float [] ambientColor = {0.2f, 0.2f, 0.2f, 1f};
			// FloatBuffer buffer =
			// ByteBuffer.allocateDirect(ambientColor.length*8).asFloatBuffer();
			// buffer.put(ambientColor).flip();
			glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);

			final FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
			lightColor0.put(0.8f).put(0.8f).put(0.8f).put(1f).flip();
			final FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
			lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
			glLight(GL_LIGHT0, GL_DIFFUSE, lightColor0);
			glLight(GL_LIGHT0, GL_POSITION, lightPos0);

			final FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
			lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
			final FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
			lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();

			glLight(GL_LIGHT1, GL_DIFFUSE, lightColor1);
			glLight(GL_LIGHT1, GL_POSITION, lightPos1);

			// glColor3f(1f,1f,0f);
			// glColorMaterial ( GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE ) ;
			// glEnable(GL_COLOR_MATERIAL);
			final List<Geoset> geosets = modelView.getModel().getGeosets();
			render(geosets, formatVersion);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			if ((programPreferences != null) && programPreferences.showNormals()) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_TEXTURE_2D);

				glBegin(GL11.GL_LINES);
				glColor3f(1f, 1f, 3f);
				// if( wireframe.isSelected() )
				for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
					if ((ModelUtils.isLevelOfDetailSupported(formatVersion)) && (geo.getLevelOfDetailName() != null)
							&& (geo.getLevelOfDetailName().length() > 0)) {
						if (geo.getLevelOfDetail() != levelOfDetail) {
							continue;
						}
					}
					if ((ModelUtils.isTangentAndSkinSupported(formatVersion)) && (geo.getVertices().size() > 0)
							&& (geo.getVertex(0).getSkinBones() != null)) {
						for (final Triangle tri : geo.getTriangles()) {
							for (final GeosetVertex v : tri.getVerts()) {
								vertexHeap.x = v.x;
								vertexHeap.y = v.y;
								vertexHeap.z = v.z;
								vertexHeap.w = 1;
								skinBonesMatrixSumHeap.setZero();
								final Bone[] skinBones = v.getSkinBones();
								final short[] skinBoneWeights = v.getSkinBoneWeights();
								boolean processedBones = false;
								for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
									final Bone skinBone = skinBones[boneIndex];
									if (skinBone == null) {
										continue;
									}
									processedBones = true;
									final Mat4 worldMatrix = renderModel.getRenderNode(skinBone).getWorldMatrix();
									skinBonesMatrixHeap.set(worldMatrix);

									skinBonesMatrixSumHeap.m00 += (skinBonesMatrixHeap.m00 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m01 += (skinBonesMatrixHeap.m01 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m02 += (skinBonesMatrixHeap.m02 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m03 += (skinBonesMatrixHeap.m03 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m10 += (skinBonesMatrixHeap.m10 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m11 += (skinBonesMatrixHeap.m11 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m12 += (skinBonesMatrixHeap.m12 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m13 += (skinBonesMatrixHeap.m13 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m20 += (skinBonesMatrixHeap.m20 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m21 += (skinBonesMatrixHeap.m21 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m22 += (skinBonesMatrixHeap.m22 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m23 += (skinBonesMatrixHeap.m23 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m30 += (skinBonesMatrixHeap.m30 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m31 += (skinBonesMatrixHeap.m31 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m32 += (skinBonesMatrixHeap.m32 * skinBoneWeights[boneIndex])
											/ 255f;
									skinBonesMatrixSumHeap.m33 += (skinBonesMatrixHeap.m33 * skinBoneWeights[boneIndex])
											/ 255f;
								}
								if (!processedBones) {
									skinBonesMatrixSumHeap.setIdentity();
								}
								skinBonesMatrixSumHeap.transform(vertexHeap, vertexSumHeap);
								if (v.getNormal() != null) {
									normalHeap.x = v.getNormal().x;
									normalHeap.y = v.getNormal().y;
									normalHeap.z = v.getNormal().z;
									normalHeap.w = 0;
									skinBonesMatrixSumHeap.transform(normalHeap, normalSumHeap);

									if (normalSumHeap.length() > 0) {
										normalSumHeap.normalize();
									} else {
										normalSumHeap.set(0, 1, 0, 0);
									}
									if (Float.isNaN(normalSumHeap.x) || Float.isNaN(normalSumHeap.y)
											|| Float.isNaN(normalSumHeap.z) || Float.isNaN(normalSumHeap.w)
											|| Float.isInfinite(normalSumHeap.x) || Float.isInfinite(normalSumHeap.y)
											|| Float.isInfinite(normalSumHeap.z) || Float.isInfinite(normalSumHeap.w)) {
										continue;
									}

									GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
									GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);

									GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
									GL11.glVertex3f(vertexSumHeap.y + (float) ((normalSumHeap.y * 6) / m_zoom),
											vertexSumHeap.z + (float) ((normalSumHeap.z * 6) / m_zoom),
											vertexSumHeap.x + (float) ((normalSumHeap.x * 6) / m_zoom));
								}
							}
						}
					} else {
						for (final Triangle tri : geo.getTriangles()) {
							for (final GeosetVertex v : tri.getVerts()) {

								vertexHeap.x = v.x;
								vertexHeap.y = v.y;
								vertexHeap.z = v.z;
								vertexHeap.w = 1;
								final int boneCount = v.getBones().size();
								if (boneCount > 0) {
									vertexSumHeap.set(0, 0, 0, 0);
									for (final Bone bone : v.getBones()) {
										renderModel.getRenderNode(bone).getWorldMatrix().transform(vertexHeap, appliedVertexHeap);
										vertexSumHeap.add(appliedVertexHeap);
									}
									vertexSumHeap.x /= boneCount;
									vertexSumHeap.y /= boneCount;
									vertexSumHeap.z /= boneCount;
									vertexSumHeap.w /= boneCount;
								} else {
									vertexSumHeap.set(vertexHeap);
								}
								if (v.getNormal() != null) {
									normalHeap.x = v.getNormal().x;
									normalHeap.y = v.getNormal().y;
									normalHeap.z = v.getNormal().z;
									normalHeap.w = 0;
									if (boneCount > 0) {
										normalSumHeap.set(0, 0, 0, 0);
										for (final Bone bone : v.getBones()) {
											renderModel.getRenderNode(bone).getWorldMatrix().transform(normalHeap, appliedNormalHeap);
											normalSumHeap.add(appliedNormalHeap);
										}
									} else {
										normalSumHeap.set(normalHeap);
									}

									if (normalSumHeap.length() > 0) {
										normalSumHeap.normalize();
									} else {
										normalSumHeap.set(0, 1, 0, 0);
									}

									GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
									GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);

									GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
									GL11.glVertex3f(vertexSumHeap.y + (float) ((normalSumHeap.y * 6) / m_zoom),
											vertexSumHeap.z + (float) ((normalSumHeap.z * 6) / m_zoom),
											vertexSumHeap.x + (float) ((normalSumHeap.x * 6) / m_zoom));
								}
							}
						}
					}
				}
				glEnd();
			}
			if (renderTextures()) {
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				glEnable(GL11.GL_TEXTURE_2D);
			}
			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			renderModel.getParticleShader().use();
			for (final RenderParticleEmitter2 particle : renderModel.getParticleEmitters2()) {
				particle.render(renderModel, renderModel.getParticleShader());
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
				} else if (!showing && running) {
					paintTimer.stop();
				}
			}
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

	public void render(final List<Geoset> geosets, final int formatVersion) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, true, formatVersion);
		}
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, false, formatVersion);
		}
	}

	public void render(final Geoset geo, final boolean renderOpaque, final int formatVersion) {
		if ((ModelUtils.isLevelOfDetailSupported(formatVersion)) && (geo.getLevelOfDetailName() != null)
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
			if (ModelUtils.isShaderStringSupported(formatVersion)) {
				if ((material.getShaderString() != null) && (material.getShaderString().length() > 0)) {
					if (i > 0) {
						break;
					}
				}
			}

			if (animation != null) {
				final float layerVisibility = layer.getRenderVisibility(this);
				final float alphaValue = geosetAnimVisibility * layerVisibility;
				if (/* geo.getMaterial().isConstantColor() && */ geosetAnim != null) {
					final Vec3 renderColor = geosetAnim.getRenderColor(this);
					if (renderColor != null) {
						if (layer.getFilterMode() == FilterMode.ADDITIVE) {
							GL11.glColor4f(renderColor.z * alphaValue, renderColor.y * alphaValue,
									renderColor.x * alphaValue, alphaValue);
						} else {
							GL11.glColor4f(renderColor.z * 1f, renderColor.y * 1f, renderColor.x * 1f, alphaValue);
						}
					} else {
						GL11.glColor4f(1f, 1f, 1f, alphaValue);
					}
				} else {
					GL11.glColor4f(1f, 1f, 1f, alphaValue);
				}
			} else {
				GL11.glColor4f(1f, 1f, 1f, 1f);
			}

			final FilterMode filterMode = layer.getFilterMode();
			final boolean opaqueLayer = (filterMode == FilterMode.NONE) || (filterMode == FilterMode.TRANSPARENT);
//			if (renderOpaque && (filterMode == FilterMode.ADDITIVE)) {
//				GL11.glColorMask(true, true, true, true);
//			} else {
//				GL11.glColorMask(false, false, false, false);
//			}
			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				final Bitmap tex = layer.getRenderTexture(this, modelView.getModel());
				final Integer texture = textureMap.get(tex);
				bindLayer(layer, tex, texture, formatVersion, material);
				glBegin(GL11.GL_TRIANGLES);
				if ((ModelUtils.isTangentAndSkinSupported(formatVersion)) && (geo.getVertices().size() > 0)
						&& (geo.getVertex(0).getSkinBones() != null)) {
					for (final Triangle tri : geo.getTriangles()) {
						for (final GeosetVertex v : tri.getVerts()) {
							vertexHeap.x = v.x;
							vertexHeap.y = v.y;
							vertexHeap.z = v.z;
							vertexHeap.w = 1;
							skinBonesMatrixSumHeap.setZero();
							final Bone[] skinBones = v.getSkinBones();
							final short[] skinBoneWeights = v.getSkinBoneWeights();
							vertexSumHeap.set(0, 0, 0, 0);
							boolean processedBones = false;
							for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
								final Bone skinBone = skinBones[boneIndex];
								if (skinBone == null) {
									continue;
								}
								processedBones = true;
								final Mat4 worldMatrix = renderModel.getRenderNode(skinBone).getWorldMatrix();
								skinBonesMatrixHeap.set(worldMatrix);

								skinBonesMatrixSumHeap.m00 += (skinBonesMatrixHeap.m00 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m01 += (skinBonesMatrixHeap.m01 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m02 += (skinBonesMatrixHeap.m02 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m03 += (skinBonesMatrixHeap.m03 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m10 += (skinBonesMatrixHeap.m10 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m11 += (skinBonesMatrixHeap.m11 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m12 += (skinBonesMatrixHeap.m12 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m13 += (skinBonesMatrixHeap.m13 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m20 += (skinBonesMatrixHeap.m20 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m21 += (skinBonesMatrixHeap.m21 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m22 += (skinBonesMatrixHeap.m22 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m23 += (skinBonesMatrixHeap.m23 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m30 += (skinBonesMatrixHeap.m30 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m31 += (skinBonesMatrixHeap.m31 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m32 += (skinBonesMatrixHeap.m32 * skinBoneWeights[boneIndex])
										/ 255f;
								skinBonesMatrixSumHeap.m33 += (skinBonesMatrixHeap.m33 * skinBoneWeights[boneIndex])
										/ 255f;
							}
							if (!processedBones) {
								skinBonesMatrixSumHeap.setIdentity();
							}
							skinBonesMatrixSumHeap.transform(vertexHeap, vertexSumHeap);
							if (v.getNormal() != null) {
								normalHeap.x = v.getNormal().x;
								normalHeap.y = v.getNormal().y;
								normalHeap.z = v.getNormal().z;
								normalHeap.w = 0;
								skinBonesMatrixSumHeap.transform(normalHeap, normalSumHeap);

								if (normalSumHeap.length() > 0) {
									normalSumHeap.normalize();
								} else {
									normalSumHeap.set(0, 1, 0, 0);
								}

								GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
							}
							int coordId = layer.getCoordId();
							if (coordId >= v.getTverts().size()) {
								coordId = v.getTverts().size() - 1;
							}
							GL11.glTexCoord2f(v.getTverts().get(coordId).x,
									v.getTverts().get(coordId).y);
							GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);
						}
					}
				} else {
					for (final Triangle tri : geo.getTriangles()) {
						for (final GeosetVertex v : tri.getVerts()) {

							vertexHeap.x = v.x;
							vertexHeap.y = v.y;
							vertexHeap.z = v.z;
							vertexHeap.w = 1;
							final int boneCount = v.getBones().size();
							if (boneCount > 0) {
								vertexSumHeap.set(0, 0, 0, 0);
								for (final Bone bone : v.getBones()) {
									renderModel.getRenderNode(bone).getWorldMatrix().transform(vertexHeap, appliedVertexHeap);
									vertexSumHeap.add(appliedVertexHeap);
								}
								vertexSumHeap.x /= boneCount;
								vertexSumHeap.y /= boneCount;
								vertexSumHeap.z /= boneCount;
								vertexSumHeap.w /= boneCount;
							} else {
								vertexSumHeap.set(vertexHeap);
							}
							if (v.getNormal() != null) {
								normalHeap.x = v.getNormal().x;
								normalHeap.y = v.getNormal().y;
								normalHeap.z = v.getNormal().z;
								normalHeap.w = 0;
								if (boneCount > 0) {
									normalSumHeap.set(0, 0, 0, 0);
									for (final Bone bone : v.getBones()) {
										renderModel.getRenderNode(bone).getWorldMatrix().transform(normalHeap, appliedNormalHeap);
										normalSumHeap.add(appliedNormalHeap);
									}
								} else {
									normalSumHeap.set(normalHeap);
								}

								if (normalSumHeap.length() > 0) {
									normalSumHeap.normalize();
								} else {
									normalSumHeap.set(0, 1, 0, 0);
								}

								GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
							}
							int coordId = layer.getCoordId();
							if (coordId >= v.getTverts().size()) {
								coordId = v.getTverts().size() - 1;
							}
							GL11.glTexCoord2f(v.getTverts().get(coordId).x,
									v.getTverts().get(coordId).y);
							GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);
						}
					}
				}
				// if( texture != null )
				// {
				// texture.release();
				// }
				glEnd();
			}
		}

	}

	public void bindLayer(final Layer layer, final Bitmap tex, final Integer texture, final int formatVersion,
			final Material parent) {
		if (texture != null) {
			// texture.bind();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		} else if (textureMap.size() > 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		}
		boolean depthMask = false;
		switch (layer.getFilterMode()) {
		case BLEND:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
		case ADDALPHA:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case MODULATE:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			break;
		case MODULATE2X:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			break;
		case NONE:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			depthMask = true;
			break;
		case TRANSPARENT:
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.75f);
			GL11.glDisable(GL11.GL_BLEND);
			depthMask = true;
			break;
		}
		if (layer.getTwoSided()
				|| ((ModelUtils.isShaderStringSupported(formatVersion)) && parent.getTwoSided())) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		} else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		if (layer.getNoDepthTest()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		if (layer.getNoDepthSet()) {
			GL11.glDepthMask(false);
		} else {
			GL11.glDepthMask(depthMask);
		}
//		GL11.glColorMask(layer.getFilterMode() == FilterMode.ADDITIVE, layer.getFilterMode() == FilterMode.ADDITIVE,
//				layer.getFilterMode() == FilterMode.ADDITIVE, layer.getFilterMode() == FilterMode.ADDITIVE);
		if (layer.getUnshaded()) {
			GL11.glDisable(GL_LIGHTING);
		} else {
			glEnable(GL_LIGHTING);
		}
	}

	public void bindLayer(final ParticleEmitter2 particle2, final Bitmap tex, final Integer texture) {
		if (texture != null) {
			// texture.bind();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		} else if (textureMap.size() > 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		}
		switch (particle2.getFilterMode()) {
		case BLEND:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case ALPHAKEY:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case MODULATE:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			break;
		case MODULATE2X:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			break;
		}
		if (particle2.getUnshaded()) {
			GL11.glDisable(GL_LIGHTING);
		} else {
			glEnable(GL_LIGHTING);
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
	public double convertX(final double x) {
		return ((x + cameraPos.x) * m_zoom) + (getWidth() / 2);
	}

	public double convertY(final double y) {
		return ((-y + cameraPos.y) * m_zoom) + (getHeight() / 2);
	}

	public double geomX(final double x) {
		return ((x - (getWidth() / 2)) / m_zoom) - cameraPos.x;
	}

	public double geomY(final double y) {
		return -(((y - (getHeight() / 2)) / m_zoom) - cameraPos.y);
	}

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

					cameraPos.x += ((int) mx - lastClick.x) / m_zoom;
					cameraPos.y += ((int) my - lastClick.y) / m_zoom;
					lastClick.x = (int) mx;
					lastClick.y = (int) my;
				}
				if (leftClickStart != null) {

					xangle += mx - leftClickStart.x;
					yangle += my - leftClickStart.y;

					axisHeap.set(0, 1, 0, (float) Math.toRadians(yangle));
					inverseCameraRotationYSpin.setFromAxisAngle(axisHeap);
					axisHeap.set(0, 0, 1, (float) Math.toRadians(xangle));
					inverseCameraRotationZSpin.setFromAxisAngle(axisHeap);
					inverseCameraRotationYSpin.mul(inverseCameraRotationZSpin, inverseCameraRotationQuat);
					inverseCameraRotationQuat.x = -inverseCameraRotationQuat.x;
					inverseCameraRotationQuat.y = -inverseCameraRotationQuat.y;
					inverseCameraRotationQuat.z = -inverseCameraRotationQuat.z;
					inverseCameraRotationYSpin.x = -inverseCameraRotationYSpin.x;
					inverseCameraRotationYSpin.y = -inverseCameraRotationYSpin.y;
					inverseCameraRotationYSpin.z = -inverseCameraRotationYSpin.z;
					inverseCameraRotationZSpin.x = -inverseCameraRotationZSpin.x;
					inverseCameraRotationZSpin.y = -inverseCameraRotationZSpin.y;
					inverseCameraRotationZSpin.z = -inverseCameraRotationZSpin.z;
					leftClickStart.x = (int) mx;
					leftClickStart.y = (int) my;
				}
				// MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

				if (actStart != null) {
					final Point actEnd = new Point((int) mx, (int) my);
					final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
					final Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
					// dispMDL.updateAction(convertedStart,convertedEnd,m_d1,m_d2);
					actStart = actEnd;
				}
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		clickTimer.setRepeats(true);
		clickTimer.start();
		mouseInBounds = true;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if ((leftClickStart == null) && (actStart == null) && (lastClick == null)) {
			clickTimer.stop();
		}
		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			lastClick = new Point(e.getXOnScreen(), e.getYOnScreen());
		} else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
			leftClickStart = new Point(e.getXOnScreen(), e.getYOnScreen());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			actStart = new Point(e.getX(), e.getY());
			final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			// dispMDL.startAction(convertedStart,m_d1,m_d2,MainFrame.panel.currentActionType());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
			cameraPos.x += (e.getXOnScreen() - lastClick.x) / m_zoom;
			cameraPos.y += (e.getYOnScreen() - lastClick.y) / m_zoom;
			lastClick = null;
		} else if (programPreferences.getThreeDCameraSpinButton().isButton(e) && (leftClickStart != null)) {
			final Point selectEnd = new Point(e.getX(), e.getY());
			final Rectangle2D.Double area = pointsToGeomRect(leftClickStart, selectEnd);
			// System.out.println(area);
			// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
			leftClickStart = null;
		} else if ((e.getButton() == MouseEvent.BUTTON3) && (actStart != null)) {
			final Point actEnd = new Point(e.getX(), e.getY());
			final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			final Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
			// dispMDL.finishAction(convertedStart,convertedEnd,m_d1,m_d2);
			actStart = null;
		}
		if (!mouseInBounds && (leftClickStart == null) && (actStart == null) && (lastClick == null)) {
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
		for (int i = 0; i < wr; i++) {
			if (neg) {
				// cameraPos.x -= (mx - getWidth() / 2)
				// * (1 / m_zoom - 1 / (m_zoom * 1.15));
				// cameraPos.y -= (my - getHeight() / 2)
				// * (1 / m_zoom - 1 / (m_zoom * 1.15));
				// cameraPos.z -= (getHeight() / 2)
				// * (1 / m_zoom - 1 / (m_zoom * 1.15));
				m_zoom *= 1.15;
			} else {
				m_zoom /= 1.15;
				// cameraPos.x -= (mx - getWidth() / 2)
				// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
				// cameraPos.y -= (my - getHeight() / 2)
				// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
				// cameraPos.z -= (getHeight() / 2)
				// * (1 / (m_zoom * 1.15) - 1 / m_zoom);
			}
		}
	}

	public Rectangle2D.Double pointsToGeomRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x), geomX(b.x)),
				Math.min(geomY(a.y), geomY(b.y)));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x), geomX(b.x)),
				Math.max(geomY(a.y), geomY(b.y)));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x,
				lowRight.y - topLeft.y);
		return temp;
	}

	public Rectangle2D.Double pointsToRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min(a.x, b.x), Math.min(a.y, b.y));
		final Point2D.Double lowRight = new Point2D.Double(Math.max(a.x, b.x), Math.max(a.y, b.y));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, lowRight.x - topLeft.x,
				lowRight.y - topLeft.y);
		return temp;
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
			final boolean loopingState = currentAnimation != null && !currentAnimation.isNonLooping();
			looping = loopingState;
			break;
		case NEVER_LOOP:
			looping = false;
			break;
		}
	}

	public void setAnimationSpeed(final float speed) {
		animSpeed = speed;

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
		public void setTransformation(final Vec3 worldLocation, final Quat rotation,
				final Vec3 worldScale) {
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
		public void move(final Vec3 deltaPosition) {

		}

		@Override
		public void hide() {
		}

		@Override
		public void bind() {
			if (!loaded) {
				loadToTexMap((particle.getFilterMode() == MdlxParticleEmitter2.FilterMode.MODULATE)
						&& (particle.getFilterMode() == MdlxParticleEmitter2.FilterMode.MODULATE2X),
						bitmap);
				loaded = true;
			}
			final Integer texture = textureMap.get(bitmap);
			bindLayer(particle, bitmap, texture);
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

	public void setSpawnParticles(final boolean b) {
		renderModel.setSpawnParticles(b);
	}
}