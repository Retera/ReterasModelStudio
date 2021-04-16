package com.hiveworkshop.wc3.gui.modeledit;

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
import static org.lwjgl.opengl.GL11.glColor4f;
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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
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
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.GPUReadyTexture;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.ProgramPreferencesChangeListener;
import com.hiveworkshop.wc3.gui.animedit.BasicTimeBoundProvider;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.lwjgl.BetterAWTGLCanvas;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.InternalInstance;
import com.hiveworkshop.wc3.mdl.render3d.InternalResource;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.render3d.RenderParticleEmitter2;
import com.hiveworkshop.wc3.mdl.render3d.RenderResourceAllocator;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.util.MathUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

public class PerspectiveViewport extends BetterAWTGLCanvas
		implements MouseListener, ActionListener, MouseWheelListener, RenderResourceAllocator {
	public static final boolean LOG_EXCEPTIONS = true;
	ModelView modelView;
	Vertex cameraPos = new Vertex(0, 0, 0);
	Quaternion inverseCameraRotationQuat = new Quaternion();
	Quaternion inverseCameraRotationYSpin = new Quaternion();
	Quaternion inverseCameraRotationZSpin = new Quaternion();
	Matrix4f matrixHeap = new Matrix4f();
	private final Vector4f axisHeap = new Vector4f();
	double m_zoom = 1;
	Point lastClick;
	Point leftClickStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	JMenuItem reAssignMatrix;
	JMenuItem cogBone;
	boolean enabled = false;

	boolean texLoaded = false;

	JCheckBox wireframe;
	HashMap<Bitmap, Integer> textureMap = new HashMap<>();

	Class<? extends Throwable> lastThrownErrorClass;
	private final ProgramPreferences programPreferences;
	private final RenderModel editorRenderModel;

	private float backgroundRed, backgroundBlue, backgroundGreen;
	Runnable repaintRunnable = new Runnable() {
		@Override
		public void run() {
			editorRenderModel.updateNodes(true, true);
		}
	};
	Timer paintTimer;

	public PerspectiveViewport(final ModelView modelView, final ProgramPreferences programPreferences,
			final RenderModel editorRenderModel) throws LWJGLException {
		super();
		this.programPreferences = programPreferences;
		this.editorRenderModel = editorRenderModel;
		editorRenderModel.setAllowInanimateParticles(true);
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		// Viewport border
		// setBorder(BorderFactory.createBevelBorder(1));
		setBackground((programPreferences == null) || (programPreferences.getPerspectiveBackgroundColor() == null)
				? new Color(80, 80, 80)
				: programPreferences.getPerspectiveBackgroundColor());
		setMinimumSize(new Dimension(200, 200));
		// add(Box.createHorizontalStrut(200));
		// add(Box.createVerticalStrut(200));
		// setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		this.modelView = modelView;
		addMouseListener(this);
		addMouseWheelListener(this);

		contextMenu = new JPopupMenu();
		reAssignMatrix = new JMenuItem("Re-assign Matrix");
		reAssignMatrix.addActionListener(this);
		contextMenu.add(reAssignMatrix);
		cogBone = new JMenuItem("Auto-Center Bone(s)");
		cogBone.addActionListener(this);
		contextMenu.add(cogBone);

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

	private void loadBackgroundColors() {
		backgroundRed = programPreferences.getPerspectiveBackgroundColor().getRed() / 255f;
		backgroundGreen = programPreferences.getPerspectiveBackgroundColor().getGreen() / 255f;
		backgroundBlue = programPreferences.getPerspectiveBackgroundColor().getBlue() / 255f;
	}

	public void setWireframeHandler(final JCheckBox nwireframe) {
		wireframe = nwireframe;
	}

	private final float[] whiteDiffuse = { 1f, 1f, 1f, 1f };
	private final float[] posSun = { 0.0f, 10.0f, 0.0f, 1.0f };

	boolean wantReload = false;

	public void reloadTextures() {
		wantReload = true;
	}

	boolean wantReloadAll = false;
	private float xRatio;
	private float yRatio;

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
					loadToTexMap(layer.getTextureBitmap());
				}
				if (layer.getTextures() != null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(tex);
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

	public void loadToTexMap(final Bitmap tex) {
		if (textureMap.get(tex) == null) {
			String path = tex.getPath();
			if (path.length() == 0) {
				if (tex.getReplaceableId() == 1) {
					path = "ReplaceableTextures\\TeamColor\\TeamColor" + Material.getTeamColorNumberString();
				} else if (tex.getReplaceableId() == 2) {
					path = "ReplaceableTextures\\TeamGlow\\TeamGlow" + Material.getTeamColorNumberString();
				} else if (tex.getReplaceableId() == 11) {
					path = "ReplaceableTextures\\Cliff\\Cliff0";
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
					texture = loadTexture(BLPHandler.get().loadTexture(workingDirectory, path), tex);
				} else {
					texture = loadTexture(BLPHandler.get().loadTexture(workingDirectory, path + ".blp"), tex);
				}
			} catch (final Exception exc) {
				if (LOG_EXCEPTIONS) {
					exc.printStackTrace();
				}
				try {
				} catch (final Exception exc2) {
					if (LOG_EXCEPTIONS) {
						exc2.printStackTrace();
					}
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
					loadToTexMap(layer.getTextureBitmap());
				}
				if (layer.getTextures() != null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(tex);
					}
				}
			}
		}
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
							loadToTexMap(layer.getTextureBitmap());
						}
						if (layer.getTextures() != null) {
							for (final Bitmap tex : layer.getTextures()) {
								loadToTexMap(tex);
							}
						}
					}
				}
			}
		} catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
		// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
		// old library code and java std library code give me a metric for the
		// ridiculous ratio:
		xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		yRatio = (float) (Display.getDisplayMode().getHeight()
				/ Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		// These ratios will be wrong and users will see corrupted visuals (bad scale,
		// only fits part of window,
		// etc) if they are using Windows 10 differing UI scale per monitor. I don't
		// think I have an API
		// to query that information yet, though.

	}

	public void setPosition(final double a, final double b) {
		cameraPos.x = a;
		cameraPos.y = b;
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
			final ByteBuffer pixels = ByteBuffer.allocate(getWidth() * getHeight() * 4);
			initGL();
			paintGL();
			GL11.glReadPixels(0, 0, getWidth(), getHeight(), 1, GL11.GL_4_BYTES, pixels);
			image.getRaster().setDataElements(0, 0, getWidth(), getHeight(), pixels);
			return image;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	boolean initialized = false;
	int current_height;
	int current_width;
	private float xangle;
	private float yangle;

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	private final Vector4f vertexHeap = new Vector4f();
	private final Vector4f appliedVertexHeap = new Vector4f();
	private final Vector4f vertexSumHeap = new Vector4f();
	private final Vector4f normalHeap = new Vector4f();
	private final Vector4f appliedNormalHeap = new Vector4f();
	private final Vector4f normalSumHeap = new Vector4f();
	private final Matrix4f skinBonesMatrixHeap = new Matrix4f();
	private final Matrix4f skinBonesMatrixSumHeap = new Matrix4f();

	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	@Override
	public void paintGL() {
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
			initContext(0, 0, 0);
			if ((getWidth() != current_width) || (getHeight() != current_height)) {
				current_width = getWidth();
				current_height = getHeight();
				glViewport(0, 0, (int) (current_width * xRatio), (int) (current_height * yRatio));
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
			if (renderTextures()) {
				glEnable(GL11.GL_TEXTURE_2D);
			}
			GL11.glEnable(GL11.GL_BLEND);
			glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 1.0f);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			gluPerspective(45f, (float) current_width / (float) current_height, 20.0f, 600.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			glTranslatef(0f, -70f, -200f);
			glRotatef(yangle, 1f, 0f, 0f);
			glRotatef(xangle, 0f, 1f, 0f);
			glTranslatef((float) cameraPos.y * (float) m_zoom, (float) cameraPos.z * (float) m_zoom,
					(float) cameraPos.x * (float) m_zoom);
			glScalef((float) m_zoom, (float) m_zoom, (float) m_zoom);

			final FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
			ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
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
			glColor4f(0.5882352941176471f, 0.5882352941176471f, 1f, 0.3f);
			// glPushMatrix();
			// glTranslatef(getWidth() / 2.0f, getHeight() / 2.0f, 0.0f);
			// glRotatef(2*angle, 0f, 0f, -1.0f);
			// glRectf(-50.0f, -50.0f, 50.0f, 50.0f);
			for (final Geoset geo : modelView.getVisibleGeosets()) {// .getMDL().getGeosets()
				if (!modelView.getEditableGeosets().contains(geo) && (modelView.getHighlightedGeoset() != geo)) {
					render(geo, true, false, true, formatVersion);
				}
			}
			for (final Geoset geo : modelView.getVisibleGeosets()) {// .getMDL().getGeosets()
				if (!modelView.getEditableGeosets().contains(geo) && (modelView.getHighlightedGeoset() != geo)) {
					render(geo, false, false, true, formatVersion);
				}
			}
			glColor3f(1f, 1f, 1f);
			render(modelView.getEditableGeosets(), formatVersion);
			GL11.glDepthMask(true);
			// System.out.println("max:
			// "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
			if (modelView.getHighlightedGeoset() != null) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				if ((programPreferences != null) && (programPreferences.getHighlighTriangleColor() != null)) {
					final Color highlighTriangleColor = programPreferences.getHighlighTriangleColor();
					glColor3f(highlighTriangleColor.getRed() / 255f, highlighTriangleColor.getGreen() / 255f,
							highlighTriangleColor.getBlue() / 255f);
				} else {
					glColor3f(1f, 3f, 1f);
				}
				render(modelView.getHighlightedGeoset(), true, true, true, formatVersion);
				render(modelView.getHighlightedGeoset(), false, true, true, formatVersion);
			}

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
					if ((ModelUtils.isTangentAndSkinSupported(formatVersion)) && (geo.getVertices().size() > 0)
							&& (geo.getVertex(0).getSkinBones() != null)) {
						for (final Triangle tri : geo.getTriangles()) {
							for (final GeosetVertex v : tri.getVerts()) {
								vertexHeap.x = (float) v.x;
								vertexHeap.y = (float) v.y;
								vertexHeap.z = (float) v.z;
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
									final Matrix4f worldMatrix = editorRenderModel.getRenderNode(skinBone)
											.getWorldMatrix();
									skinBonesMatrixHeap.load(worldMatrix);

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
								Matrix4f.transform(skinBonesMatrixSumHeap, vertexHeap, vertexSumHeap);
								if (v.getNormal() != null) {
									normalHeap.x = (float) v.getNormal().x;
									normalHeap.y = (float) v.getNormal().y;
									normalHeap.z = (float) v.getNormal().z;
									normalHeap.w = 0;
									Matrix4f.transform(skinBonesMatrixSumHeap, normalHeap, normalSumHeap);

									if (normalSumHeap.length() > 0) {
										normalSumHeap.normalise();
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

								vertexHeap.x = (float) v.x;
								vertexHeap.y = (float) v.y;
								vertexHeap.z = (float) v.z;
								vertexHeap.w = 1;
								final int boneCount = v.getBones().size();
								if (boneCount > 0) {
									vertexSumHeap.set(0, 0, 0, 0);
									for (final Bone bone : v.getBones()) {
										Matrix4f.transform(editorRenderModel.getRenderNode(bone).getWorldMatrix(),
												vertexHeap, appliedVertexHeap);
										Vector4f.add(vertexSumHeap, appliedVertexHeap, vertexSumHeap);
									}
									vertexSumHeap.x /= boneCount;
									vertexSumHeap.y /= boneCount;
									vertexSumHeap.z /= boneCount;
									vertexSumHeap.w /= boneCount;
								} else {
									vertexSumHeap.set(vertexHeap);
								}
								if (v.getNormal() != null) {
									normalHeap.x = (float) v.getNormal().x;
									normalHeap.y = (float) v.getNormal().y;
									normalHeap.z = (float) v.getNormal().z;
									normalHeap.w = 0;
									if (boneCount > 0) {
										normalSumHeap.set(0, 0, 0, 0);
										for (final Bone bone : v.getBones()) {
											Matrix4f.transform(editorRenderModel.getRenderNode(bone).getWorldMatrix(),
													normalHeap, appliedNormalHeap);
											Vector4f.add(normalSumHeap, appliedNormalHeap, normalSumHeap);
										}
									} else {
										normalSumHeap.set(normalHeap);
									}

									if (normalSumHeap.length() > 0) {
										normalSumHeap.normalise();
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
			editorRenderModel.getParticleShader().use();
			for (final RenderParticleEmitter2 particle : editorRenderModel.getParticleEmitters2()) {
				particle.render(editorRenderModel, editorRenderModel.getParticleShader());
			}

			// glPopMatrix();
			swapBuffers();
			repaintRunnable.run();
			final boolean showing = isShowing();
			final boolean running = paintTimer.isRunning();
			if (showing && !running) {
				paintTimer.restart();
			} else if (!showing && running) {
				paintTimer.stop();
			}
		} catch (final Throwable e) {
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

	public void bindLayer(final Layer layer, final Bitmap tex, final Integer texture) {
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
		if (layer.isTwoSided()) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		} else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		if (layer.isNoDepthTest()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		if (layer.isNoDepthSet()) {
			GL11.glDepthMask(false);
		} else {
			GL11.glDepthMask(depthMask);
		}
		if (layer.isUnshaded()) {
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
		switch (particle2.getFilterModeReallyBadReallySlow()) {
		case Blend:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case Additive:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case AlphaKey:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			break;
		case Modulate:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			break;
		case Modulate2x:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			break;
		}
		if (particle2.isUnshaded()) {
			GL11.glDisable(GL_LIGHTING);
		} else {
			glEnable(GL_LIGHTING);
		}
	}

	public void render(final Iterable<Geoset> geosets, final int formatVersion) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, true, false, false, formatVersion);
		}
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, false, false, false, formatVersion);
		}
	}

	public void render(final Geoset geo, final boolean renderOpaque, final boolean overriddenMaterials,
			final boolean overriddenColors, final int formatVersion) {
		final GeosetAnim geosetAnim = geo.getGeosetAnim();
		float geosetAnimVisibility = 1;
		final AnimatedRenderEnvironment timeEnvironment = editorRenderModel.getAnimatedRenderEnvironment();
		final BasicTimeBoundProvider animation = timeEnvironment == null ? null : timeEnvironment.getCurrentAnimation();
		if ((animation != null) && (geosetAnim != null)) {
			geosetAnimVisibility = geosetAnim.getRenderVisibility(timeEnvironment);
			if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
				return;
			}
		}
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

			if (!overriddenColors) {
				if (animation != null) {
					final float layerVisibility = layer.getRenderVisibility(timeEnvironment);
					if (/* geo.getMaterial().isConstantColor() && */ geosetAnim != null) {
						final Vector3f renderColor = geosetAnim.getRenderColor(timeEnvironment);
						if (renderColor != null) {
							GL11.glColor4f(renderColor.z * 1f, renderColor.y * 1f, renderColor.x * 1f,
									geosetAnimVisibility * layerVisibility);
						} else {
							GL11.glColor4f(1f, 1f, 1f, geosetAnimVisibility * layerVisibility);
						}
					} else {
						GL11.glColor4f(1f, 1f, 1f, geosetAnimVisibility * layerVisibility);
					}
				} else {
					GL11.glColor4f(1f, 1f, 1f, 1f);
				}
			}

			final FilterMode filterMode = layer.getFilterMode();
			final boolean opaqueLayer = (filterMode == FilterMode.NONE) || (filterMode == FilterMode.TRANSPARENT);
			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				if (!overriddenMaterials) {
					final Bitmap tex = layer.getRenderTexture(timeEnvironment, modelView.getModel());
					final Integer texture = textureMap.get(tex);
					bindLayer(layer, tex, texture);
				}
				if (overriddenColors) {
					GL11.glDisable(GL11.GL_ALPHA_TEST);
				}
				glBegin(GL11.GL_TRIANGLES);
				if ((ModelUtils.isTangentAndSkinSupported(formatVersion)) && (geo.getVertices().size() > 0)
						&& (geo.getVertex(0).getSkinBones() != null)) {
					for (final Triangle tri : geo.getTriangles()) {
						for (final GeosetVertex v : tri.getVerts()) {
							vertexHeap.x = (float) v.x;
							vertexHeap.y = (float) v.y;
							vertexHeap.z = (float) v.z;
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
								final Matrix4f worldMatrix = editorRenderModel.getRenderNode(skinBone).getWorldMatrix();
								skinBonesMatrixHeap.load(worldMatrix);

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
							Matrix4f.transform(skinBonesMatrixSumHeap, vertexHeap, vertexSumHeap);
							if (v.getNormal() != null) {
								normalHeap.x = (float) v.getNormal().x;
								normalHeap.y = (float) v.getNormal().y;
								normalHeap.z = (float) v.getNormal().z;
								normalHeap.w = 0;
								Matrix4f.transform(skinBonesMatrixSumHeap, normalHeap, normalSumHeap);

								if (normalSumHeap.length() > 0) {
									normalSumHeap.normalise();
								} else {
									normalSumHeap.set(0, 1, 0, 0);
								}

								GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
							}
							int coordId = layer.getCoordId();
							if (coordId >= v.getTverts().size()) {
								coordId = v.getTverts().size() - 1;
							}
							GL11.glTexCoord2f((float) v.getTverts().get(coordId).x,
									(float) v.getTverts().get(coordId).y);
							GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);
						}
					}
				} else {
					for (final Triangle tri : geo.getTriangles()) {
						for (final GeosetVertex v : tri.getVerts()) {

							vertexHeap.x = (float) v.x;
							vertexHeap.y = (float) v.y;
							vertexHeap.z = (float) v.z;
							vertexHeap.w = 1;
							final int boneCount = v.getBones().size();
							if (boneCount > 0) {
								vertexSumHeap.set(0, 0, 0, 0);
								for (final Bone bone : v.getBones()) {
									Matrix4f.transform(editorRenderModel.getRenderNode(bone).getWorldMatrix(),
											vertexHeap, appliedVertexHeap);
									Vector4f.add(vertexSumHeap, appliedVertexHeap, vertexSumHeap);
								}
								vertexSumHeap.x /= boneCount;
								vertexSumHeap.y /= boneCount;
								vertexSumHeap.z /= boneCount;
								vertexSumHeap.w /= boneCount;
							} else {
								vertexSumHeap.set(vertexHeap);
							}
							if (v.getNormal() != null) {
								normalHeap.x = (float) v.getNormal().x;
								normalHeap.y = (float) v.getNormal().y;
								normalHeap.z = (float) v.getNormal().z;
								normalHeap.w = 0;
								if (boneCount > 0) {
									normalSumHeap.set(0, 0, 0, 0);
									for (final Bone bone : v.getBones()) {
										Matrix4f.transform(editorRenderModel.getRenderNode(bone).getWorldMatrix(),
												normalHeap, appliedNormalHeap);
										Vector4f.add(normalSumHeap, appliedNormalHeap, normalSumHeap);
									}
								} else {
									normalSumHeap.set(normalHeap);
								}

								if (normalSumHeap.length() > 0) {
									normalSumHeap.normalise();
								} else {
									normalSumHeap.set(0, 1, 0, 0);
								}

								GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
							}
							int coordId = layer.getCoordId();
							if (coordId >= v.getTverts().size()) {
								coordId = v.getTverts().size() - 1;
							}
							final int highestTvertexIndx = v.getTverts().size() - 1;
							GL11.glTexCoord2f((float) v.getTverts().get(coordId).x,
									(float) v.getTverts().get(coordId).y);
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
			final double mx = MouseInfo.getPointerInfo().getLocation().x - xoff;// MainFrame.frame.getX()-8);
			final double my = MouseInfo.getPointerInfo().getLocation().y - yoff;// MainFrame.frame.getY()-30);
			// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse,
			// "+lastClick.x+","+lastClick.y+" as last.");
			// System.out.println(xoff+" and "+mx);
			if (lastClick != null) {
				MathUtils.fromQuat(inverseCameraRotationQuat, matrixHeap);
				vertexHeap.x = 0;
				vertexHeap.y = (float) (((int) mx - lastClick.x) / m_zoom);
				vertexHeap.z = -(float) (((int) my - lastClick.y) / m_zoom);
				vertexHeap.w = 1;
				Matrix4f.transform(matrixHeap, vertexHeap, vertexHeap);
				cameraPos.x += vertexHeap.x;
				cameraPos.y += vertexHeap.y;
				cameraPos.z += vertexHeap.z;
				lastClick.x = (int) mx;
				lastClick.y = (int) my;
				// rip it from magos's code:
				// final double dx = ((int) mx - lastClick.x) / m_zoom;
				// final double dy = ((int) my - lastClick.y) / m_zoom;
				// final double yaw = xangle;
				// final double pitch = yangle;
				// final double x_x = dx * Math.sin(yaw);
				// final double x_y = -dy * Math.sin(pitch) * Math.cos(yaw);
				// final double y_x = -dx * Math.cos(yaw);
				// final double y_y = -dy * Math.sin(pitch) * Math.sin(yaw);
				// final double z_x = 0;
				// final double z_y = dy * Math.cos(pitch);
				// cameraPos.x += (x_x + x_y);
				// cameraPos.y += (y_x + y_y);
				// cameraPos.z += (z_x + z_y);
				// lastClick.x = (int) mx;
				// lastClick.y = (int) my;
			}
			if (leftClickStart != null) {

				xangle += mx - leftClickStart.x;
				yangle += my - leftClickStart.y;

				axisHeap.set(0, 1, 0, (float) Math.toRadians(yangle));
				inverseCameraRotationYSpin.setFromAxisAngle(axisHeap);
				axisHeap.set(0, 0, 1, (float) Math.toRadians(xangle));
				inverseCameraRotationZSpin.setFromAxisAngle(axisHeap);
				Quaternion.mul(inverseCameraRotationYSpin, inverseCameraRotationZSpin, inverseCameraRotationQuat);
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
		} else if (e.getSource() == reAssignMatrix) {
			// MatrixPopup matrixPopup = new MatrixPopup(dispMDL.getMDL());
			// String[] words = { "Accept", "Cancel" };
			// int i = JOptionPane.showOptionDialog(MainFrame.panel,
			// matrixPopup,
			// "Rebuild Matrix", JOptionPane.PLAIN_MESSAGE,
			// JOptionPane.YES_NO_OPTION, null, words, words[1]);
			// if (i == 0) {
			// // JOptionPane.showMessageDialog(null,"action approved");
			// dispMDL.setMatrix(matrixPopup.newRefs);
			// }
		} else if (e.getSource() == cogBone) {
			// modelView.cogBones();
			JOptionPane.showMessageDialog(this,
					"Please use other viewport, this action is not implemented for this viewport.");
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
		if (programPreferences.getThreeDCameraPanButton().isButton(e) && (lastClick != null)) {
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
		if (e.getButton() == MouseEvent.BUTTON3) {

			// if( actEnd.equals(actStart) )
			// {
			// actStart = null;

			// JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			// ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
			// contextMenu.show(this, e.getX(), e.getY());
			// }
		}
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

	public static int loadTexture(final GPUReadyTexture image, final Bitmap bitmap) {
		if (image == null) {
			return -1;
		}

		final ByteBuffer buffer = image.getBuffer();
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
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
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
			if (!loaded) {
				loadToTexMap(bitmap);
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

	public void setViewportBackground(final Color background) {
		setBackground(background);
	}
}