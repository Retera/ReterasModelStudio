package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class GeosetRenderer {
	private final ProgramPreferences programPreferences;
	private CameraManager cameraHandler;
	private final VertRendererThing vertRendererThing;
	private TextureThing textureThing;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;
	private EditorColorPrefs colorPrefs;
	private ModelView modelView;
	private int levelOfDetail = -1;

	private Vec2 uvTemp = new Vec2();
	private Mat4 uvTransform = new Mat4();

	boolean texLoaded = true;
	public GeosetRenderer(CameraManager cameraHandler, ProgramPreferences programPreferences) {
		this.cameraHandler = cameraHandler;
		this.programPreferences = programPreferences;
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
		vertRendererThing = new VertRendererThing((float) (cameraHandler.sizeAdj()*4));


//		System.out.println("Geometry "+GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER));
//		System.out.println("Fragment "+GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER));
	}
	public GeosetRenderer updateModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
		this.renderModel = renderModel;
		if (renderModel != null) {
			renderEnv = renderModel.getTimeEnvironment();
			this.modelView = modelView;
			this.textureThing = textureThing;
		} else {
			renderEnv = null;
			this.modelView = null;
			this.textureThing = null;
		}
		return this;
	}

	public GeosetRenderer doRender(boolean renderTextures, boolean wireFrame, boolean showNormals, boolean show3dVerts) {

		int formatVersion = modelView.getModel().getFormatVersion();
		renderGeosets(modelView.getVisibleGeosets(), formatVersion, false, renderTextures, wireFrame);

		if (modelView.getHighlightedGeoset() != null) {
			drawHighlightedGeosets(formatVersion, renderTextures);
		}

		if (showNormals) {
			renderNormals(formatVersion);
		}
		if (show3dVerts) {
			renderVertDots(formatVersion);
		}
		return this;
	}

	private void drawHighlightedGeosets(int formatVersion, boolean renderTextures) {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if (colorPrefs != null) {
			final Color highlightTriangleColor = colorPrefs.getColor(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
			glColor3f(highlightTriangleColor.getRed() / 255f, highlightTriangleColor.getGreen() / 255f, highlightTriangleColor.getBlue() / 255f);
		} else {
			glColor3f(1f, 3f, 1f);
		}
		renderGeoset(modelView.getHighlightedGeoset(), true, formatVersion, true, renderTextures);
		renderGeoset(modelView.getHighlightedGeoset(), false, formatVersion, true, renderTextures);
	}

	// ToDo investigate why transparent Geosets don't render when renderTextures is false

	private List<Geoset> sortedGeos = new ArrayList<>();
	private List<Geoset> opaqueGeos = new ArrayList<>();
	private List<Geoset> transperentGeos = new ArrayList<>();
	private void renderGeosets(Iterable<Geoset> geosets, int formatVersion, boolean overriddenColors, boolean renderTextures, boolean wireFrame) {
		GL11.glDepthMask(true);
		glShadeModel(GL11.GL_FLAT);
//		glDisable(GL_SHADE_MODEL);
//		if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
//			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		}
		if (wireFrame) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glDisable(GL_CULL_FACE);
			glDisable(GL_ALPHA_TEST);
			glDisable(GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
		} else {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glEnable(GL_CULL_FACE);
			glEnable(GL_ALPHA_TEST);
		}
		if (texLoaded && renderTextures) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			glEnable(GL11.GL_TEXTURE_2D);
//			glEnable(GL_SHADE_MODEL);
			glShadeModel(GL_SMOOTH);
		} else {

			glDisable(GL_TEXTURE_2D);
			glShadeModel(GL11.GL_FLAT);
		}

		opaqueGeos.clear();
		transperentGeos.clear();
		sortedGeos.clear();
		for (Geoset geo : geosets) {
			if (correctLoD(geo, formatVersion) && modelView.shouldRender(geo)) {
				if (geo.isOpaque()) {
					opaqueGeos.add(geo);
				} else {
					transperentGeos.add(geo);
				}
			}
		}
		sortedGeos.addAll(transperentGeos);
		sortedGeos.addAll(opaqueGeos);

		for (Geoset geo : opaqueGeos) {
			if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
				renderGeoset(geo, true, formatVersion, overriddenColors, renderTextures);
			}
		}
		for (Geoset geo : transperentGeos) {
			if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
//			if (modelView.getEditableGeosets().contains(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
				renderGeoset(geo, false, formatVersion, overriddenColors, renderTextures);
			}
		}
	}

	private void renderGeoset(Geoset geo, boolean renderOpaque, int formatVersion, boolean overriddenColors, boolean renderTextures) {
		if (!correctLoD(geo, formatVersion) || geo.getVertices().isEmpty()) return;

		float geosetAnimVisibility = geo.getRenderVisibility(renderEnv);
		if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
			return;
		}
		Vec3 renderColor = geo.getRenderColor(renderEnv);

		Material material = geo.getMaterial();
		List<Layer> layers = material.getLayers();
		for (int i = 0; i < layers.size(); i++) {
			if (ModelUtils.isShaderStringSupported(formatVersion)
					&& (material.getShaderString() != null)
					&& (material.getShaderString().length() > 0)
					&& (i > 0)) {
				break; // HD-materials is not supported
			}
			Layer layer = layers.get(i);

			boolean opaqueLayer = isOpaqueLayer(geo, renderTextures, layer);

			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				if (!modelView.isEditable(geo) && !renderOpaque && (geo.getName().contains("Pelvis")) && !renderTextures) {
					System.out.println("should render geo " + geo.getName() + ": " + (!renderOpaque && !opaqueLayer) + ", has renderGeo: " + renderModel.getRenderGeoset(geo));
				}

				Bitmap tex = layer.getRenderTexture(renderEnv);

				if (tex != null) {
					textureThing.bindLayerTexture(layer, tex, formatVersion, material, 0);
				}

				if (overriddenColors) {
					GL11.glDisable(GL11.GL_ALPHA_TEST);
				} else {
					setStandardColors(renderColor, geosetAnimVisibility * layer.getRenderVisibility(renderEnv), layer);
				}

				renderMesh3(geo, layer, renderTextures);
			}
		}
	}

	private boolean isOpaqueLayer(Geoset geo, boolean renderTextures, Layer layer) {
		boolean notEditable = !modelView.isEditable(geo);
		boolean dontTexture = !renderTextures;
		return (layer.getFilterMode() == FilterMode.NONE || layer.getFilterMode() == FilterMode.TRANSPARENT) && !(!renderTextures && !modelView.isEditable(geo));
	}

	//, boolean renderTextures, boolean wireFrame
	private void renderMesh3(Geoset geo, Layer layer, boolean renderTextures) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (renderGeoset != null) {
			Mat4 uvTransform = getUVTransform(layer);
			glBegin(GL11.GL_TRIANGLES);
			for (Triangle tri : geo.getTriangles()) {
//				if (programPreferences != null && !programPreferences.textureModels() && cameraHandler.isOrtho()) {
				if (colorPrefs != null && !renderTextures && cameraHandler.isOrtho()) {
					getTriAreaColor(tri);
				}
				GeosetVertex[] verts = tri.getVerts();
				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
					RenderGeoset.RenderVert[] renderVert = new RenderGeoset.RenderVert[3];
					renderVert[0] = renderGeoset.getRenderVert(verts[0]);
					renderVert[1] = renderGeoset.getRenderVert(verts[1]);
					renderVert[2] = renderGeoset.getRenderVert(verts[2]);
					if (renderVert[0] != null && renderVert[1] != null && renderVert[2] != null) {
						paintVert(getUv(layer, verts[0], uvTransform), renderVert[0].getRenderPos(), renderVert[0].getRenderNorm());
						paintVert(getUv(layer, verts[1], uvTransform), renderVert[1].getRenderPos(), renderVert[1].getRenderNorm());
						paintVert(getUv(layer, verts[2], uvTransform), renderVert[2].getRenderPos(), renderVert[2].getRenderNorm());
//						System.out.println("RenderVert: " + renderVert[0].getRenderPos());
					}
				}
			}
			glEnd();
		}
	}



	private void setStandardColors(Vec3 renderColor, float alphaValue, Layer layer) {
		if (renderColor != null) {
			if (layer.getFilterMode() == FilterMode.ADDITIVE) {
				GL11.glColor4f(renderColor.x * alphaValue, renderColor.y * alphaValue, renderColor.z * alphaValue, alphaValue);
			} else {
				GL11.glColor4f(renderColor.x, renderColor.y, renderColor.z, alphaValue);
			}
		} else {
			GL11.glColor4f(1f, 1f, 1f, alphaValue);
		}
	}

	private Vec2 getUv(Layer layer, GeosetVertex vertex, Mat4 uvTransform) {
		int coordId = layer.getCoordId();
		if (vertex.getTverts().size() <= coordId) {
			coordId = vertex.getTverts().size() - 1;
		}
		if (uvTransform != null) {
			uvTemp.set(vertex.getTverts().get(coordId));
			uvTemp.transform2(uvTransform);
			return uvTemp;
		}
		return vertex.getTverts().get(coordId);
	}

	private Mat4 getUVTransform(Layer layer) {
		if (layer.getTextureAnim() != null) {
			uvTransform.setIdentity();

			uvTransform.fromRotationTranslationScale(
					layer.getTextureAnim().getInterpolatedQuat(renderEnv, MdlUtils.TOKEN_ROTATION, Quat.IDENTITY),
					layer.getTextureAnim().getInterpolatedVector(renderEnv, MdlUtils.TOKEN_TRANSLATION, Vec3.ZERO),
					layer.getTextureAnim().getInterpolatedVector(renderEnv, MdlUtils.TOKEN_SCALING, Vec3.ONE)
			);
			return uvTransform;
		}
		return null;
	}

	private void paintVert(Vec2 uv, Vec3 vert, Vec3 normal) {
		GL11.glNormal3f(normal.x, normal.y, normal.z);
		GL11.glTexCoord2f(uv.x, uv.y);
		GL11.glVertex3f(vert.x, vert.y, vert.z);
	}

	private void renderNormals(int formatVersion) {
		GL11.glDepthMask(true);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		glBegin(GL11.GL_LINES);
		glColor3f(1f, 1f, 3f);

		float sizeFactor = (float) (6.0 * 4.0 * cameraHandler.sizeAdj());

		for (Geoset geo : modelView.getVisibleGeosets()) {
			RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
			if (correctLoD(geo, formatVersion) && renderGeoset != null) {
				for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
					Vec3 renderPos = renderVert.getRenderPos();
					Vec3 renderNorm = renderVert.getRenderNorm();

					paintNormal(renderPos, renderNorm, sizeFactor);
				}
			}
		}
		glEnd();
	}

	private void paintNormal(Vec3 vertexSumHeap, Vec3 normalSumHeap, float sizeFactor) {
		GL11.glNormal3f(normalSumHeap.x, normalSumHeap.y, normalSumHeap.z);
		GL11.glVertex3f(vertexSumHeap.x, vertexSumHeap.y, vertexSumHeap.z);

		GL11.glNormal3f(normalSumHeap.x, normalSumHeap.y, normalSumHeap.z);
		GL11.glVertex3f(
				vertexSumHeap.x + (normalSumHeap.x * sizeFactor),
				vertexSumHeap.y + (normalSumHeap.y * sizeFactor),
				vertexSumHeap.z + (normalSumHeap.z * sizeFactor));
	}




	private void renderVertDots(int formatVersion) {
		GL11.glDepthMask(true);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
//		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glDepthMask(true);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glEnable(GL_SHADE_MODEL);
		GL11.glEnable(GL11.GL_CULL_FACE);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_CULL_FACE);
		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_SHADE_MODEL);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

//		glColor3f(255f, 1f, 255f);
		glColor4f(.7f, .0f, .0f, .4f);
		for (final Geoset geo : modelView.getEditableGeosets()) {
			if (correctLoD(geo, formatVersion) && modelView.shouldRender(geo)) {
				vertRendererThing.updateSquareSize((float) (cameraHandler.sizeAdj() * 4));
				paintVertSquares2(geo);
			}
		}

		GL11.glEnable(GL_SHADE_MODEL);
	}

	public void paintVertSquares2(Geoset geo) {
//		float v = (float) ((cameraHandler.geomX(4) - cameraHandler.geomX(0)));//*cameraHandler.getZoom());
//		glBegin(GL11.GL_TRIANGLES);

//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		glPolygonMode(GL_COLOR, GL_FILL);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);

		if (renderGeoset != null) {
			glPolygonMode(GL_FRONT, GL_FILL);
			glBegin(GL11.GL_TRIANGLES);
			EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (renderVert != null && !modelView.isHidden(renderVert.getVertex())) {
					float[] rgba = getVertRGBA(colorPrefs, renderVert);
					glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
					vertRendererThing.transform(cameraHandler.getInverseCameraRotation(), renderVert.getRenderPos()).doGlGeom();
				}
			}
			glEnd();
		}
	}

	private float[] getVertRGBA(EditorColorPrefs colorPrefs, RenderGeoset.RenderVert renderVert) {
		float[] rgba;
		if (modelView.isEditable(renderVert.getVertex()) && modelView.isSelected(renderVert.getVertex())) {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX_SELECTED);
		} else if (modelView.isEditable(renderVert.getVertex())) {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX);
		} else {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX_UNEDITABLE);
		}
		return rgba;
	}

	private void getTriAreaColor(Triangle triangle) {
		float[] color = getTriRGBA(triangle);

		glColor4f(color[0], color[1], color[2], color[3]);
	}

	private float[] getTriRGBA(Triangle triangle) {
		float[] color;
		if (triangle.getGeoset() == modelView.getHighlightedGeoset()) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
		} else if (triFullySelected(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
		} else if (triFullyEditable(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
		} else {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
		}
		return color;
	}


	private boolean triFullySelected(Triangle triangle) {
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}
	private boolean triFullyEditable(Triangle triangle) {
		return modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2));
	}
	private boolean correctLoD(Geoset geo, int formatVersion) {
		if ((ModelUtils.isLevelOfDetailSupported(formatVersion)) && (geo.getLevelOfDetailName() != null) && (geo.getLevelOfDetailName().length() > 0) && levelOfDetail > -1) {
			return geo.getLevelOfDetail() == levelOfDetail;
		}
		return true;
	}

//	public boolean renderTextures() {
//		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
//	}

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
}
