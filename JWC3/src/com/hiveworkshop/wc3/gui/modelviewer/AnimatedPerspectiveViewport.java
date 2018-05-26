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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
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
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.ProgramPreferencesChangeListener;
import com.hiveworkshop.wc3.gui.lwjgl.BetterAWTGLCanvas;
import com.hiveworkshop.wc3.gui.modelviewer.AnimationControllerListener.LoopType;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class AnimatedPerspectiveViewport extends BetterAWTGLCanvas
		implements MouseListener, ActionListener, MouseWheelListener, AnimatedRenderEnvironment {
	ModelView modelView;
	private RenderModel renderModel;
	Vertex cameraPos = new Vertex(0, 0, 0);
	Quaternion inverseCameraRotationQuat = new Quaternion();
	Quaternion inverseCameraRotationYSpin = new Quaternion();
	Quaternion inverseCameraRotationZSpin = new Quaternion();
	private final Vector4f axisHeap = new Vector4f();
	double m_zoom = 1;
	Point lastClick;
	Point leftClickStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
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

	public AnimatedPerspectiveViewport(final ModelView modelView, final ProgramPreferences programPreferences)
			throws LWJGLException {
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
		this.renderModel = new RenderModel(modelView.getModel());
		renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
				inverseCameraRotationZSpin);
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
		this.renderModel = new RenderModel(modelView.getModel());
		renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
				inverseCameraRotationZSpin);
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
					inverseCameraRotationZSpin);
		} else {
			renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
					inverseCameraRotationZSpin);
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

		for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				final Layer layer = geo.getMaterial().getLayers().get(i);
				if (layer.getTextureBitmap() == null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(layer, tex, false);
					}
				} else {
					loadToTexMap(layer, layer.getTextureBitmap(), false);
				}
			}
		}
	}

	public void loadToTexMap(final Layer layer, final Bitmap tex, final boolean force) {
		if (force || textureMap.get(tex) == null) {
			String path = tex.getPath();
			if (path.length() == 0) {
				if (tex.getReplaceableId() == 1) {
					path = "ReplaceableTextures\\TeamColor\\TeamColor" + Material.getTeamColorNumberString();
				} else if (tex.getReplaceableId() == 2) {
					path = "ReplaceableTextures\\TeamGlow\\TeamGlow" + Material.getTeamColorNumberString();
				}
			} else {
				path = path.substring(0, path.length() - 4);
			}
			Integer texture = null;
			try {
				final File workingDirectory = modelView.getModel().getWorkingDirectory();
				texture = loadTexture(BLPHandler.get()
						.getTexture(workingDirectory == null ? "" : workingDirectory.getPath(), path + ".blp"), tex,
						layer.getFilterMode() != FilterMode.NONE);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// new
				// FileInputStream(new
				// File(dispMDL.getMDL().getFile().getParent()+"\\"+path+".tga"))).getTextureID();

				// try { } catch (FileNotFoundException e) {
				// // Auto-generated catch block
				// e.printStackTrace();
				// } catch (IOException e) {
				// // Auto-generated catch block
				// e.printStackTrace();
				// }
			}
			if (texture != null) {
				textureMap.put(tex, texture);
				// textureMapCID.put(tex,
				// geo.getMaterial().getLayers().get(i).getCoordId());
				// texture.bind();
			}
		}
	}

	public void addGeosets(final List<Geoset> geosets) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				final Layer layer = geo.getMaterial().getLayers().get(i);
				if (layer.getTextureBitmap() == null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(layer, tex, true);
					}
				} else {
					loadToTexMap(layer, layer.getTextureBitmap(), true);
				}
			}
		}
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

	@Override
	public void initGL() {
		try {
			if (programPreferences == null || programPreferences.textureModels()) {
				texLoaded = true;
				for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
					for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
						final Layer layer = geo.getMaterial().getLayers().get(i);
						if (layer.getTextureBitmap() == null) {
							for (final Bitmap tex : layer.getTextures()) {
								loadToTexMap(layer, tex, true);
							}
						} else {
							loadToTexMap(layer, layer.getTextureBitmap(), true);
						}
					}
				}
				renderModel.refreshFromEditor(this, inverseCameraRotationQuat, inverseCameraRotationYSpin,
						inverseCameraRotationZSpin);
			}
		} catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
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
		m_zoom *= (1 + amount);
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
	private float xangle;
	private float yangle;

	public boolean renderTextures() {
		return texLoaded && (programPreferences == null || programPreferences.textureModels());
	}

	private final Vector4f vertexHeap = new Vector4f();
	private final Vector4f appliedVertexHeap = new Vector4f();
	private final Vector4f vertexSumHeap = new Vector4f();
	private final Vector4f normalHeap = new Vector4f();
	private final Vector4f appliedNormalHeap = new Vector4f();
	private final Vector4f normalSumHeap = new Vector4f();

	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	@Override
	public void paintGL() {
		// setSize(getParent().getSize());
		if (System.currentTimeMillis() - lastExceptionTimeMillis < 5000) {
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
		} else if (!texLoaded && (programPreferences == null || programPreferences.textureModels())) {
			forceReloadTextures();
			texLoaded = true;
		}
		try {
			if (live) {
				final long currentTimeMillis = System.currentTimeMillis();
				if (currentTimeMillis - lastExceptionTimeMillis > 16) {
					if (animation != null && animation.length() > 0) {
						if (looping) {
							animationTime = (int) ((animationTime + (currentTimeMillis - lastUpdateMillis))
									% animation.length());
						} else {
							animationTime = Math.min(animation.length(),
									(int) ((animationTime + (currentTimeMillis - lastUpdateMillis))));
						}
					}
					renderModel.updateNodes(false);
					lastUpdateMillis = currentTimeMillis;
				}
			}

			if (programPreferences != null && programPreferences.viewMode() == 0) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			} else if (programPreferences == null || programPreferences.viewMode() == 1) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}
			glViewport(0, 0, getWidth(), getHeight());
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
			glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 1.0f);
			// glClearColor(0f, 0f, 0f, 1.0f);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			gluPerspective(45f, (float) getWidth() / (float) getHeight(), 1.0f, 600.0f);
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

			glTranslatef(0f + (float) cameraPos.x * (float) m_zoom, -70f - (float) cameraPos.y * (float) m_zoom,
					-200f - (float) cameraPos.z * (float) m_zoom);
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
			final ArrayList<Geoset> geosets = modelView.getModel().getGeosets();
			render(geosets);
			if (programPreferences != null && programPreferences.showNormals()) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_TEXTURE_2D);

				glBegin(GL11.GL_LINES);
				glColor3f(1f, 1f, 3f);
				// if( wireframe.isSelected() )
				for (final Geoset geo : modelView.getModel().getGeosets()) {// .getMDL().getGeosets()
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
									Matrix4f.transform(renderModel.getRenderNode(bone).getWorldMatrix(), vertexHeap,
											appliedVertexHeap);
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
										Matrix4f.transform(renderModel.getRenderNode(bone).getWorldMatrix(), normalHeap,
												appliedNormalHeap);
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

								GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
								GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);

								GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
								GL11.glVertex3f(vertexSumHeap.y + (float) (normalSumHeap.y * 6 / m_zoom),
										vertexSumHeap.z + (float) (normalSumHeap.z * 6 / m_zoom),
										vertexSumHeap.x + (float) (normalSumHeap.x * 6 / m_zoom));
							}
						}
					}
				}
				glEnd();
			}
			// System.out.println("max:
			// "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			// glPopMatrix();
			swapBuffers();
			repaint();
		} catch (final Throwable e) {
			e.printStackTrace();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if (lastThrownErrorClass == null || lastThrownErrorClass != e.getClass()) {
				lastThrownErrorClass = e.getClass();
				JOptionPane.showMessageDialog(null, "Rendering failed because of this exact reason:\n"
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			throw new RuntimeException(e);
		}
	}
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

	public void render(final ArrayList<Geoset> geosets) {
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, true);
		}
		for (final Geoset geo : geosets) {// .getMDL().getGeosets()
			render(geo, false);
		}
	}

	public void render(final Geoset geo, final boolean renderOpaque) {
		final GeosetAnim geosetAnim = geo.getGeosetAnim();
		float geosetAnimVisibility = 1;
		if (animation != null && geosetAnim != null) {
			geosetAnimVisibility = geosetAnim.getRenderVisibility(this);
			if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
				return;
			}
		}
		for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
			final Layer layer = geo.getMaterial().getLayers().get(i);

			if (animation != null) {
				final float layerVisibility = layer.getRenderVisibility(this);
				final float alphaValue = geosetAnimVisibility * layerVisibility;
				if (/* geo.getMaterial().isConstantColor() && */ geosetAnim != null) {
					final Vector3f renderColor = geosetAnim.getRenderColor(this);
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
			final boolean opaqueLayer = filterMode == FilterMode.NONE || filterMode == FilterMode.TRANSPARENT;
			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				final Bitmap tex = layer.getRenderTexture(this);
				final Integer texture = textureMap.get(tex);
				bindLayer(layer, tex, texture);
				glBegin(GL11.GL_TRIANGLES);
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
								Matrix4f.transform(renderModel.getRenderNode(bone).getWorldMatrix(), vertexHeap,
										appliedVertexHeap);
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
									Matrix4f.transform(renderModel.getRenderNode(bone).getWorldMatrix(), normalHeap,
											appliedNormalHeap);
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
						GL11.glTexCoord2f((float) v.getTverts().get(v.getTverts().size() - 1 - layer.getCoordId()).x,
								(float) v.getTverts().get(v.getTverts().size() - 1 - layer.getCoordId()).y);
						GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);
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

	public void bindLayer(final Layer layer, final Bitmap tex, final Integer texture) {
		if (texture != null) {
			// texture.bind();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
					tex.getWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
					tex.getWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		}
		boolean depthMask = false;
		switch (layer.getFilterMode()) {
		case BLEND:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			break;
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
		return (x + cameraPos.x) * m_zoom + getWidth() / 2;
	}

	public double convertY(final double y) {
		return ((-y + cameraPos.y) * m_zoom) + getHeight() / 2;
	}

	public double geomX(final double x) {
		return (x - getWidth() / 2) / m_zoom - cameraPos.x;
	}

	public double geomY(final double y) {
		return -((y - getHeight() / 2) / m_zoom - cameraPos.y);
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
			final double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);// MainFrame.frame.getX()-8);
			final double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);// MainFrame.frame.getY()-30);
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

				xangle += (mx - leftClickStart.x);
				yangle += (my - leftClickStart.y);

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
			repaint();
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
		if (leftClickStart == null && actStart == null && lastClick == null) {
			clickTimer.stop();
		}
		mouseInBounds = false;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			lastClick = new Point(e.getXOnScreen(), e.getYOnScreen());
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			leftClickStart = new Point(e.getXOnScreen(), e.getYOnScreen());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			actStart = new Point(e.getX(), e.getY());
			final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			// dispMDL.startAction(convertedStart,m_d1,m_d2,MainFrame.panel.currentActionType());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			cameraPos.x += (e.getXOnScreen() - lastClick.x) / m_zoom;
			cameraPos.y += (e.getYOnScreen() - lastClick.y) / m_zoom;
			lastClick = null;
		} else if (e.getButton() == MouseEvent.BUTTON1 && leftClickStart != null) {
			final Point selectEnd = new Point(e.getX(), e.getY());
			final Rectangle2D.Double area = pointsToGeomRect(leftClickStart, selectEnd);
			// System.out.println(area);
			// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
			leftClickStart = null;
		} else if (e.getButton() == MouseEvent.BUTTON3 && actStart != null) {
			final Point actEnd = new Point(e.getX(), e.getY());
			final Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x), geomY(actStart.y));
			final Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x), geomY(actEnd.y));
			// dispMDL.finishAction(convertedStart,convertedEnd,m_d1,m_d2);
			actStart = null;
		}
		if (!mouseInBounds && leftClickStart == null && actStart == null && lastClick == null) {
			clickTimer.stop();
			repaint();
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
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, (lowRight.x - (topLeft.x)),
				((lowRight.y) - (topLeft.y)));
		return temp;
	}

	public Rectangle2D.Double pointsToRect(final Point a, final Point b) {
		final Point2D.Double topLeft = new Point2D.Double(Math.min((a.x), (b.x)), Math.min((a.y), (b.y)));
		final Point2D.Double lowRight = new Point2D.Double(Math.max((a.x), (b.x)), Math.max((a.y), (b.y)));
		final Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y, (lowRight.x - (topLeft.x)),
				((lowRight.y) - (topLeft.y)));
		return temp;
	}

	private static final int BYTES_PER_PIXEL = 4;
	private LoopType loopType;

	public static int loadTexture(final BufferedImage image, final Bitmap bitmap, final boolean alpha) {

		final int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
		// 4
		// for
		// RGBA,
		// 3
		// for
		// RGB

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
															// Only for RGBA
			}
		}

		buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS

		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		final int textureID = GL11.glGenTextures(); // Generate texture ID
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				bitmap.getWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				bitmap.getWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, buffer);

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
}