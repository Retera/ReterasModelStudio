package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.renderparts.RenderGeoset;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.BetterAWTGLCanvas;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class PerspectiveViewport extends BetterAWTGLCanvas {
	private static final int BYTES_PER_PIXEL = 4;
	private final float[] whiteDiffuse = {1f, 1f, 1f, 1f};
	private final float[] posSun = {0.0f, 10.0f, 0.0f, 1.0f};
	private RenderModel renderModel;
	private ModelView modelView;

	private final CameraHandler cameraHandler;
	private Timer paintTimer;
	private boolean mouseInBounds = false;
	private boolean texLoaded = false;

	private final TextureThing textureThing;
	private Class<? extends Throwable> lastThrownErrorClass;
	private boolean wantReload = false;
	private boolean wantReloadAll = false;
	private int popupCount = 0;
	private final TimeEnvironmentImpl renderEnv;
	private final ProgramPreferences programPreferences;

	private long lastExceptionTimeMillis = 0;
	private final Vec4 backgroundColor = new Vec4();
	private int levelOfDetail = -1;

	private float xRatio;
	private float yRatio;
	private final Timer clickTimer;

	ExtLog currentExt = new ExtLog(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0);
	ExtLog modelExtent = new ExtLog(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0);

	public PerspectiveViewport(ModelView modelView, RenderModel renderModel, TimeEnvironmentImpl renderEnvironment, boolean loadDefaultCamera) throws LWJGLException {
		super();
		this.programPreferences = ProgramGlobals.getPrefs();
		textureThing = new TextureThing(modelView, programPreferences);
		cameraHandler = new CameraHandler(this);
		renderEnv = renderEnvironment;

		MouseAdapter mouseAdapter = getMouseAdapter();
		addMouseListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);

		backgroundColor.set(programPreferences == null ? new float[]{80, 80, 80, 1} : programPreferences.getPerspectiveBackgroundColor().getColorComponents(new float[]{0, 255, 0, 1}));
		setBackground(backgroundColor.asFloatColor());
		System.out.println("(1) backgroundColor: " + backgroundColor);
//		setBackground(new Color(80, 80, 80, 0));
		setMinimumSize(new Dimension(200, 200));

		this.modelView = modelView;
		modelExtent.setMinMax(modelView.getModel().getExtents());
		setCurrentExtent();
		if (loadDefaultCamera) {
			ViewportHelpers.findDefaultAnimation(modelView, renderEnv);
			cameraHandler.loadDefaultCameraFor(ViewportHelpers.getBoundsRadius(renderEnv, modelExtent));
		}



		if (programPreferences != null) {
			programPreferences.addChangeListener(() -> updateBackgroundColor(programPreferences));
		}

		paintTimer = new Timer(16, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}
		});
		paintTimer.start();
		clickTimer = new Timer(16, e -> cameraHandler.clickTimerAction());
		shortcutKeyListener();

		this.renderModel = renderModel;
		this.renderModel.setCameraHandler(cameraHandler);
		this.renderModel.refreshFromEditor(getParticleTextureInstance());
	}

	public Particle2TextureInstance getParticleTextureInstance() {
		return new Particle2TextureInstance(textureThing, modelView, programPreferences);
	}

	public void setLevelOfDetail(int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	public void setModel(final ModelView modelView) {
		renderEnv.setAnimation(null);
		this.modelView = modelView;
		renderModel = new RenderModel(modelView.getModel(), modelView, renderEnv);
		renderModel.setCameraHandler(cameraHandler);
		modelExtent.setDefault().setMinMax(modelView.getModel().getExtents());
		setCurrentExtent();
		if (modelView.getModel().getAnims().size() > 0) {
			renderEnv.setAnimation(modelView.getModel().getAnim(0));
		}
		Vec3 maxExt = currentExt.getMaximumExtent();
		double boundsRadius = ViewportHelpers.getBoundsRadius(renderEnv, modelExtent);
		double radius;
		if (modelView.getModel().getAnims().size() < 2) {
			System.out.println("model? maxExt: " + maxExt.length() + ", boundsR: " + boundsRadius);
			radius = maxExt.length();
		} else {
			System.out.println("dodad? maxExt: " + maxExt.length() + ", boundsR: " + boundsRadius);
			radius = boundsRadius/2;
		}
		cameraHandler.resetZoom(radius);
		cameraHandler.setViewportCamera((int) -(radius / 6 ), 0, 0);

		reloadAllTextures();
	}

	protected void updateBackgroundColor(ProgramPreferences programPreferences) {
		Color backgroundColor = programPreferences.getPerspectiveBackgroundColor();
		this.backgroundColor.set(programPreferences == null ? new float[]{80, 80, 80, 1} : backgroundColor.getColorComponents(new float[]{255, 0, 0, 1}));
		setBackground(this.backgroundColor.asFloatColor());
		System.out.println("updateBackgroundColor: " + this.backgroundColor.asFloatColor() + "(" + this.backgroundColor + ")" );
//		setBackground(new Color(80, 80, 80, 0));
//		loadBackgroundColors();
	}

//	protected void loadBackgroundColors() {
//		backgroundRed = programPreferences.getPerspectiveBackgroundColor().getRed() / 255f;
//		backgroundGreen = programPreferences.getPerspectiveBackgroundColor().getGreen() / 255f;
//		backgroundBlue = programPreferences.getPerspectiveBackgroundColor().getBlue() / 255f;
//	}

	protected void shortcutKeyListener() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);

				if (e.getKeyCode() == KeyEvent.VK_NUMPAD7) {
					// Top view
					System.out.println("VK_NUMPAD7");
					cameraHandler.setCameraTop(currentExt.getMaximumExtent().length());
				}
				if (e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
					// Front view
					System.out.println("VK_NUMPAD1");
					cameraHandler.setCameraFront(currentExt.getMaximumExtent().length());
				}
				if (e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
					// Side view
					System.out.println("VK_NUMPAD3");
					cameraHandler.setCameraSide(currentExt.getMaximumExtent().length());
				}
				if (e.getKeyCode() == KeyEvent.VK_O) {
					// Orto Mode
					cameraHandler.toggleOrtho();
					System.out.println("VK_O");
				}
			}
		});
	}

	private void setCurrentExtent() {
		if ((renderEnv.getCurrentAnimation() != null) && renderEnv.getCurrentAnimation().getExtents() != null) {
			currentExt = renderEnv.getCurrentAnimation().getExtents();
		}
		if (currentExt.getMaximumExtent().distance(new Vec3(0, 0, 0)) < 1) {
			currentExt = modelExtent;
		}
	}

	public void translate(final double a, final double b) {
		cameraHandler.translate(a, b);
	}

	protected void forceReloadTextures() {
		texLoaded = true;
		textureThing.reMakeTextureMap();
		renderModel.refreshFromEditor(
//				Quat.getInverseRotation(cameraHandler.getInverseCameraRotation()),
//				cameraHandler.getInverseCameraRotationYSpin(),
//				cameraHandler.getInverseCameraRotationZSpin(),
				getParticleTextureInstance());
	}



	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	@Override
	public void initGL() {
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				forceReloadTextures();
			}
		} catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
		// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
		// old library code and java std library code give me a metric for the ridiculous ratio:
		xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		yRatio = (float) (Display.getDisplayMode().getHeight() / Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		// These ratios will be wrong and users will see corrupted visuals (bad scale, only fits part of window,
		// etc) if they are using Windows 10 differing UI scale per monitor. I don't think I have an API
		// to query that information yet, though.
	}

	@Override
	public void paintGL() {
		paintGL(true);
	}

	public void paintGL(final boolean autoRepainting) {
		setSize(getParent().getSize());
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.out.println("not rendering :O");
			if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 100) {
				System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			}
			return;
		}
		reloadIfNeeded();
		try {
			int formatVersion = modelView.getModel().getFormatVersion();
			if (renderEnv.isLive()) {
				renderEnv.updateAnimationTime();
				renderModel.updateNodes(false, programPreferences.getRenderParticles());
			}
			if(modelView.isGeosetsVisible()){
				renderModel.updateGeosets();
			}

			if ((programPreferences != null) && (programPreferences.viewMode() == 0)) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			} else if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}
			glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
			enableGlThings(GL_DEPTH_TEST, GL_COLOR_MATERIAL, GL_LIGHTING, GL_LIGHT0, GL_LIGHT1, GL_NORMALIZE);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, autoRepainting ? 1.0f : 1.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//			glMatrixMode(GL_MODELVIEW);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();

			cameraHandler.setUpCamera();

			FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
			ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
			glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);
			addLamp(0.8f, 40.0f, 200.0f, 80.0f, GL_LIGHT0);
			addLamp(0.2f, -100.0f, 200.5f, 0.5f, GL_LIGHT1);

			if (programPreferences != null && programPreferences.showPerspectiveGrid()) {
				paintGridFloor();
			}

			renderGeosets(modelView.getVisibleGeosets(), formatVersion, false);

			if (modelView.getHighlightedGeoset() != null) {
				drawHighlightedGeosets(formatVersion);
			}

			if ((programPreferences != null)) {
				if (programPreferences.showNormals()) {
					renderNormals(formatVersion);
				}
				if (programPreferences.show3dVerts()) {
					renderVertDots(formatVersion);
				}
				if (programPreferences.getRenderParticles()) {
					renderParticles();
				}
			}
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
			if (autoRepainting) {
				paintAndUpdate();
			}
		} catch (final Throwable e) {
			paintTimer.stop();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
				lastThrownErrorClass = e.getClass();
				popupCount++;
				if (popupCount < 10) {
					ExceptionPopup.display(e);
				}
			}
			System.out.println("Failed to render");
			throw new RuntimeException(e);
		}
	}


	private void reloadIfNeeded() {
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload some.
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
		}
	}

	private void paintAndUpdate() {
		try {
//			System.out.println("swapping buffers");
			swapBuffers();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		if (isShowing() && !paintTimer.isRunning()) {
			paintTimer.restart();
		} else if (!isShowing() && paintTimer.isRunning()) {
			paintTimer.stop();
		}
	}

	private void addLamp(float lightValue, float x, float y, float z, int glLight) {
		FloatBuffer lightColor = BufferUtils.createFloatBuffer(4);
		lightColor.put(lightValue).put(lightValue).put(lightValue).put(1f).flip();
		FloatBuffer lightPos = BufferUtils.createFloatBuffer(4);
		lightPos.put(x).put(y).put(z).put(1f).flip();
		glLight(glLight, GL_DIFFUSE, lightColor);
		glLight(glLight, GL_POSITION, lightPos);
	}

	private void renderParticles() {
		if (renderTextures()) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			glEnable(GL11.GL_TEXTURE_2D);
		}
		GL11.glDepthMask(false);
		enableGlThings(GL_BLEND, GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

//		renderModel.getParticleShader().use();
		for (final RenderParticleEmitter2 particle : renderModel.getRenderParticleEmitters2()) {
//			System.out.println("renderParticles");
			particle.render(renderModel, renderModel.getParticleShader());
		}
	}

	private void paintGridFloor() {
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		float lineLength = 200;
		float lineSpacing = 50;
		float numberOfLines = 5;
		glColor3f(255f, 1f, 255f);
		glColor4f(.7f, .7f, .7f, .4f);
		glBegin(GL11.GL_LINES);
		GL11.glNormal3f(0, 0, 0);
		float lineSpread = (numberOfLines + 1) * lineSpacing / 2;

		glColor4f(.7f, 1f, .7f, .4f);
		for (float x = -lineSpread + lineSpacing; x < lineSpread; x += lineSpacing) {
			GL11.glVertex3f(-lineLength, 0, x);
			GL11.glVertex3f(lineLength, 0, x);
		}

		glColor4f(1f, .7f, .7f, .4f);
		for (float y = -lineSpread + lineSpacing; y < lineSpread; y += lineSpacing) {
			GL11.glVertex3f(y, 0, -lineLength);
			GL11.glVertex3f(y, 0, lineLength);
		}
		glEnd();
	}
	private void paintVertCubes(Geoset geo) {
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glEnable(GL_SHADE_MODEL);
		GL11.glEnable(GL11.GL_CULL_FACE);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_CULL_FACE);
		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_SHADE_MODEL);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

//		glColor3f(255f, 1f, 255f);
		glColor4f(.7f, .0f, .0f, .4f);

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		glBegin(GL11.GL_TRIANGLES);
		glBegin(GL_QUADS);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		float boxRad = .5f;
		float n = 1;
		float e = 1;
		float t = 1;
		float s = -n;
		float w = -e;
		float b = -t;


		Vec3 tnw_adj = new Vec3(n*boxRad,w*boxRad,t*boxRad);
		Vec3 bnw_adj = new Vec3(n*boxRad,w*boxRad,s*boxRad);
		Vec3 tsw_adj = new Vec3(s*boxRad,w*boxRad,t*boxRad);
		Vec3 bsw_adj = new Vec3(s*boxRad,w*boxRad,s*boxRad);
		Vec3 tne_adj = new Vec3(n*boxRad,e*boxRad,t*boxRad);
		Vec3 bne_adj = new Vec3(n*boxRad,e*boxRad,s*boxRad);
		Vec3 tse_adj = new Vec3(s*boxRad,e*boxRad,t*boxRad);
		Vec3 bse_adj = new Vec3(s*boxRad,e*boxRad,s*boxRad);

		// t (0,0, 1)
		// b (0,0,-1)
		// s (-1,0,0)
		// n ( 1,0,0)
		// e (0, 1,0)
		// w (0,-1,0)

		Vec3 tsw = new Vec3(0,0,0);
		Vec3 tnw = new Vec3(0,0,0);
		Vec3 bsw = new Vec3(0,0,0);
		Vec3 bnw = new Vec3(0,0,0);
		Vec3 tse = new Vec3(0,0,0);
		Vec3 tne = new Vec3(0,0,0);
		Vec3 bse = new Vec3(0,0,0);
		Vec3 bne = new Vec3(0,0,0);
		if (renderGeoset != null) {
			for (GeosetVertex vertex : geo.getVertices()) {
				if(modelView.isSelected(vertex)){
					glColor4f(1f, .0f, .0f, .7f);
				} else {
					glColor4f(.5f, .3f, .7f, .7f);
				}
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
				if(renderVert != null){
					Vec3 renderPos = renderVert.getRenderPos();

					tnw.set(renderPos).add(tnw_adj);
					tne.set(renderPos).add(tne_adj);
					tsw.set(renderPos).add(tsw_adj);
					tse.set(renderPos).add(tse_adj);
					bnw.set(renderPos).add(bnw_adj);
					bne.set(renderPos).add(bne_adj);
					bsw.set(renderPos).add(bsw_adj);
					bse.set(renderPos).add(bse_adj);


//				//Top
					GL11.glNormal3f(0, t, 0);
					GL11.glVertex3f(tnw.y, tnw.z, tnw.x);
					GL11.glVertex3f(tne.y, tne.z, tne.x);
					GL11.glVertex3f(tse.y, tse.z, tse.x);
					GL11.glVertex3f(tsw.y, tsw.z, tsw.x);
//
//				//Bottom
					GL11.glNormal3f(0, b, 0);
					GL11.glVertex3f(bsw.y, bsw.z, bsw.x);
					GL11.glVertex3f(bse.y, bse.z, bse.x);
					GL11.glVertex3f(bne.y, bne.z, bne.x);
					GL11.glVertex3f(bnw.y, bnw.z, bnw.x);
//
//				glColor4f(.7f, .7f, .0f, .7f);
//				//South
					GL11.glNormal3f(0, 0, s);
					GL11.glVertex3f(tsw.y, tsw.z, tsw.x);
					GL11.glVertex3f(tse.y, tse.z, tse.x);
					GL11.glVertex3f(bse.y, bse.z, bse.x);
					GL11.glVertex3f(bsw.y, bsw.z, bsw.x);
//
//				glColor4f(.0f, .7f, .7f, .7f);
//				//North
					GL11.glNormal3f(0, 0, n);
					GL11.glVertex3f(bnw.y, bnw.z, bnw.x);
					GL11.glVertex3f(bne.y, bne.z, bne.x);
					GL11.glVertex3f(tne.y, tne.z, tne.x);
					GL11.glVertex3f(tnw.y, tnw.z, tnw.x);
//
//				glColor4f(.7f, .0f, .0f, .7f);
//				//West
					GL11.glNormal3f(w, 0, 0);
					GL11.glVertex3f(bsw.y, bsw.z, bsw.x);
					GL11.glVertex3f(bnw.y, bnw.z, bnw.x);
					GL11.glVertex3f(tnw.y, tnw.z, tnw.x);
					GL11.glVertex3f(tsw.y, tsw.z, tsw.x);
//
//				glColor4f(0.f, .7f, .0f, .7f);
//				//East
					GL11.glNormal3f(e, 0, 0);
					GL11.glVertex3f(tse.y, tse.z, tse.x);
					GL11.glVertex3f(tne.y, tne.z, tne.x);
					GL11.glVertex3f(bne.y, bne.z, bne.x);
					GL11.glVertex3f(bse.y, bse.z, bse.x);

					// t (0,0, 1)
					// b (0,0,-1)
					// s (-1,0,0)
					// n ( 1,0,0)
					// e (0, 1,0)
					// w (0,-1,0)
				}
			}
		}
		glEnd();
		GL11.glEnable(GL_SHADE_MODEL);
	}

	private void setStandardColors(GeosetAnim geosetAnim, float geosetAnimVisibility, TimeEnvironmentImpl timeEnvironment, Layer layer) {
		if (timeEnvironment != null && timeEnvironment.getCurrentAnimation() != null) {
			float layerVisibility = layer.getRenderVisibility(timeEnvironment);
			float alphaValue = geosetAnimVisibility * layerVisibility;
			if (/* geo.getMaterial().isConstantColor() && */ geosetAnim != null) {
				Vec3 renderColor = geosetAnim.getRenderColor(timeEnvironment);
				if (renderColor != null) {
					if (layer.getFilterMode() == FilterMode.ADDITIVE) {
						GL11.glColor4f(renderColor.x * alphaValue, renderColor.y * alphaValue, renderColor.z * alphaValue, alphaValue);
					} else {
						GL11.glColor4f(renderColor.x * 1f, renderColor.y * 1f, renderColor.z * 1f, alphaValue);
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
	}

	private void drawHighlightedGeosets(int formatVersion) {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if ((programPreferences != null) && (programPreferences.getHighlighTriangleColor() != null)) {
			final Color highlightTriangleColor = programPreferences.getHighlighTriangleColor();
			glColor3f(highlightTriangleColor.getRed() / 255f, highlightTriangleColor.getGreen() / 255f, highlightTriangleColor.getBlue() / 255f);
		} else {
			glColor3f(1f, 3f, 1f);
		}
		renderGeosets(modelView.getHighlightedGeoset(), true, formatVersion, true);
		renderGeosets(modelView.getHighlightedGeoset(), false, formatVersion, true);
	}


	private void renderGeosets(Iterable<Geoset> geosets, int formatVersion, boolean overriddenColors) {
		GL11.glDepthMask(true);
		if (renderTextures()) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			glEnable(GL11.GL_TEXTURE_2D);
		}
		for (Geoset geo : geosets) {
			if (modelView.getEditableGeosets().contains(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
				renderGeosets(geo, true, formatVersion, overriddenColors);
			}
		}
		for (Geoset geo : geosets) {
			if (modelView.getEditableGeosets().contains(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
				renderGeosets(geo, false, formatVersion, overriddenColors);
			}
		}
	}

	private void renderGeosets(Geoset geo, boolean renderOpaque, int formatVersion, boolean overriddenColors) {
		if (!correctLoD(geo, formatVersion)) return;
		GeosetAnim geosetAnim = geo.getGeosetAnim();
		float geosetAnimVisibility = 1;

		TimeBoundProvider animation = renderEnv == null ? null : renderEnv.getCurrentAnimation();
		if ((animation != null) && (geosetAnim != null)) {
			geosetAnimVisibility = geosetAnim.getRenderVisibility(renderEnv);
			// do not show invisible geosets
			if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
				return;
			}
		}
		Material material = geo.getMaterial();
		for (int i = 0; i < material.getLayers().size(); i++) {
			if (ModelUtils.isShaderStringSupported(formatVersion)
					&& (material.getShaderString() != null)
					&& (material.getShaderString().length() > 0)
					&& (i > 0)) {
				break; // HD-materials is not supported
			}
			Layer layer = material.getLayers().get(i);

//			if (!overriddenColors) {
//				setStandardColors(geosetAnim, geosetAnimVisibility, renderEnv, layer);
//			}

//			FilterMode filterMode = layer.getFilterMode();
//			boolean opaqueLayer = (filterMode == FilterMode.NONE) || (filterMode == FilterMode.TRANSPARENT);
			boolean opaqueLayer = (layer.getFilterMode() == FilterMode.NONE) || (layer.getFilterMode() == FilterMode.TRANSPARENT);

			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				Bitmap tex = layer.getRenderTexture(renderEnv, modelView.getModel());

				textureThing.bindLayerTexture(layer, tex, formatVersion, material);

				if (overriddenColors) {
					GL11.glDisable(GL11.GL_ALPHA_TEST);
				} else {
					setStandardColors(geosetAnim, geosetAnimVisibility, renderEnv, layer);
				}
				glBegin(GL11.GL_TRIANGLES);

				renderMesh2(geo, layer);
				glEnd();
			}
		}
	}

	private void renderVertDots(int formatVersion) {
		GL11.glDepthMask(true);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
//		GL11.glDisable(GL11.GL_TEXTURE_2D);

		for (final Geoset geo : modelView.getVisibleGeosets()) {
			if (correctLoD(geo, formatVersion)) {
				paintVertCubes(geo);
			}
		}
	}

	private boolean correctLoD(Geoset geo, int formatVersion) {
		if ((ModelUtils.isLevelOfDetailSupported(formatVersion)) && (geo.getLevelOfDetailName() != null) && (geo.getLevelOfDetailName().length() > 0) && levelOfDetail > -1) {
			return geo.getLevelOfDetail() == levelOfDetail;
		}
		return true;
	}

	private void renderMesh2(Geoset geo, Layer layer) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (renderGeoset != null) {
			for (Triangle tri : geo.getTriangles()) {
				for (GeosetVertex vertex : tri.getVerts()) {
					if(renderGeoset.getRenderVert(vertex) != null){
						Vec3 renderPos = renderGeoset.getRenderVert(vertex).getRenderPos();
						Vec3 renderNorm = renderGeoset.getRenderVert(vertex).getRenderNorm();

						paintVert(layer, vertex, renderPos, renderNorm);
					}
				}
			}
		}
	}

	private void paintVert(Layer layer, GeosetVertex vertex, Vec3 vert, Vec3 normal) {
		GL11.glNormal3f(normal.y, normal.z, normal.x);
		int coordId = layer.getCoordId();
		if (coordId >= vertex.getTverts().size()) {
			coordId = vertex.getTverts().size() - 1;
		}
		GL11.glTexCoord2f(vertex.getTverts().get(coordId).x, vertex.getTverts().get(coordId).y);
		GL11.glVertex3f(vert.y, vert.z, vert.x);
	}



	private void renderNormals(int formatVersion) {
		GL11.glDepthMask(true);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		glBegin(GL11.GL_LINES);
		glColor3f(1f, 1f, 3f);

		for (Geoset geo : modelView.getVisibleGeosets()) {
			RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
			if (correctLoD(geo, formatVersion) && renderGeoset != null) {
				for(GeosetVertex vertex : geo.getVertices()){
					if(renderGeoset.getRenderVert(vertex) != null){
						Vec3 renderPos = renderGeoset.getRenderVert(vertex).getRenderPos();
						Vec3 renderNorm = renderGeoset.getRenderVert(vertex).getRenderNorm();

						paintNormal(renderPos, renderNorm);
					}
				}
			}
		}
		glEnd();
	}

	private void paintNormal(Vec3 vertexSumHeap, Vec3 normalSumHeap) {
		GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
		GL11.glVertex3f(vertexSumHeap.y, vertexSumHeap.z, vertexSumHeap.x);

		float factor = (float) (6 / cameraHandler.getZoom());

		GL11.glNormal3f(normalSumHeap.y, normalSumHeap.z, normalSumHeap.x);
		GL11.glVertex3f(
				vertexSumHeap.y + (normalSumHeap.y * factor),
				vertexSumHeap.z +  (normalSumHeap.z * factor),
				vertexSumHeap.x + (normalSumHeap.x * factor));
	}

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	public void reloadTextures() {
		wantReload = true;
	}

	public void reloadAllTextures() {
		wantReloadAll = true;
	}

	public void zoom(double amount) {
		cameraHandler.zoom(amount);
	}

	public double getZoomAmount() {
		return cameraHandler.getZoom();
	}

	private void enableGlThings(int... thing) {
		for (int t : thing) {
			glEnable(t);
		}
	}

	private void disableGlThings(int... thing) {
		for (int t : thing) {
			glDisable(t);
		}
	}

	public MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				clickTimer.setRepeats(true);
				clickTimer.start();
				mouseInBounds = true;
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if ((cameraHandler.getCameraSpinStartPoint() == null) && (cameraHandler.getActStart() == null) && (cameraHandler.getCameraPanStartPoint() == null)) {
					clickTimer.stop();
				}
				mouseInBounds = false;
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (programPreferences.getThreeDCameraPanButton().isButton(e)) {
					cameraHandler.startPan(e);
				} else if (programPreferences.getThreeDCameraSpinButton().isButton(e)) {
					cameraHandler.startSpinn(e);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					cameraHandler.startAct(e);
				}
//				System.out.println("camPos: " + cameraPos + ", invQ: " + inverseCameraRotation + ", invYspin: " + inverseCameraRotationYSpin + ", invZspin: " + inverseCameraRotationZSpin);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (programPreferences.getThreeDCameraPanButton().isButton(e) && (cameraHandler.getCameraPanStartPoint() != null)) {
					cameraHandler.finnishPan(e);
				} else if (programPreferences.getThreeDCameraSpinButton().isButton(e) && (cameraHandler.getCameraSpinStartPoint() != null)) {
					cameraHandler.finnishSpinn(e);
				} else if ((e.getButton() == MouseEvent.BUTTON3) && (cameraHandler.getActStart() != null)) {
					cameraHandler.finnishAct(e);
				}
				if (!mouseInBounds && (cameraHandler.getCameraSpinStartPoint() == null) && (cameraHandler.getActStart() == null) && (cameraHandler.getCameraPanStartPoint() == null)) {
					clickTimer.stop();
				}
				/*
				 * if( dispMDL != null ) dispMDL.refreshUndo();
				 */
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
				}
			}

			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
//				int wr = e.getWheelRotation();
//				cameraHandler.doZoom(wr);
				cameraHandler.doZoom3(e);
			}
		};
	}
}
