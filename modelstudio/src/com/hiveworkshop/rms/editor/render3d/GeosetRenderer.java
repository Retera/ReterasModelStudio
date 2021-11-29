package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.application.viewer.VertRendererThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class GeosetRenderer {
	private final ProgramPreferences programPreferences;
	private CameraHandler cameraHandler;
	private final VertRendererThing vertRendererThing;
	private TextureThing textureThing;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;
	private EditorColorPrefs colorPrefs;
	private ModelView modelView;
	private int levelOfDetail = -1;

	boolean texLoaded = true;
	public GeosetRenderer(CameraHandler cameraHandler, ProgramPreferences programPreferences){
		this.cameraHandler = cameraHandler;
		this.programPreferences = programPreferences;
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
		vertRendererThing = new VertRendererThing(cameraHandler.getPixelSize());


//		System.out.println("Geometry "+GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER));
//		System.out.println("Fragment "+GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER));
	}
	public GeosetRenderer updateModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
		this.renderModel = renderModel;
		if(renderModel != null){
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

	public GeosetRenderer doRender(boolean renderTextures, boolean wireFrame, boolean showNormals, boolean show3dVerts){

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
		NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		if ((programPreferences != null) && (programPreferences.getHighlighTriangleColor() != null)) {
			final Color highlightTriangleColor = programPreferences.getHighlighTriangleColor();
			NGGLDP.pipeline.glColor3f(highlightTriangleColor.getRed() / 255f, highlightTriangleColor.getGreen() / 255f, highlightTriangleColor.getBlue() / 255f);
		} else {
			NGGLDP.pipeline.glColor3f(1f, 3f, 1f);
		}
		renderGeoset(modelView.getHighlightedGeoset(), true, formatVersion, true, renderTextures);
		renderGeoset(modelView.getHighlightedGeoset(), false, formatVersion, true, renderTextures);
	}

	//	private void renderGeosets(Iterable<Geoset> geosets, int formatVersion, boolean overriddenColors, boolean renderTextures) {
//		GL11.glDepthMask(true);
//		glShadeModel(GL11.GL_FLAT);
////		glDisable(GL_SHADE_MODEL);
//		if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
//			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		} else if (programPreferences.viewMode() == 0) {
//			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
//		}
//		if (renderTextures()) {
//			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
//			glEnable(GL11.GL_TEXTURE_2D);
////			glEnable(GL_SHADE_MODEL);
//			glShadeModel(GL_SMOOTH);
//		}
//		for (Geoset geo : geosets) {
//			if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
//				renderGeoset(geo, true, formatVersion, overriddenColors, renderTextures);
//			}
//		}
//		for (Geoset geo : geosets) {
//			if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
////			if (modelView.getEditableGeosets().contains(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
//				renderGeoset(geo, false, formatVersion, overriddenColors, renderTextures);
//			}
//		}
//	}
	private void renderGeosets(Iterable<Geoset> geosets, int formatVersion, boolean overriddenColors, boolean renderTextures, boolean wireFrame) {
		GL11.glDepthMask(true);
		NGGLDP.pipeline.glShadeModel(GL11.GL_FLAT);
//		glDisable(GL_SHADE_MODEL);
//		if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
//			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		}
		if (wireFrame) {
			NGGLDP.pipeline.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glDisable(GL_CULL_FACE);
			NGGLDP.pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
			NGGLDP.pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
		} else {
			NGGLDP.pipeline.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		}
		if (texLoaded && renderTextures) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			NGGLDP.pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
//			glEnable(GL_SHADE_MODEL);
			NGGLDP.pipeline.glShadeModel(GL_SMOOTH);
		}
		for (Geoset geo : geosets) {
			if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
				renderGeoset(geo, true, formatVersion, overriddenColors, renderTextures);
			}
		}
		for (Geoset geo : geosets) {
			if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
//			if (modelView.getEditableGeosets().contains(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
				renderGeoset(geo, false, formatVersion, overriddenColors, renderTextures);
			}
		}
	}

	private void renderGeoset(Geoset geo, boolean renderOpaque, int formatVersion, boolean overriddenColors, boolean renderTextures) {
		if (!correctLoD(geo, formatVersion) || geo.getVertices().isEmpty()) return;

		GeosetAnim geosetAnim = geo.getGeosetAnim();
		Vec3 renderColor = null;
		float geosetAnimVisibility = 1;
		if (geosetAnim != null) {
			geosetAnimVisibility = geosetAnim.getRenderVisibility(renderEnv);
			// do not show invisible geosets
			if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
				return;
			}
			renderColor = geosetAnim.getRenderColor(renderEnv);
		}

		Material material = geo.getMaterial();
		for (int i = 0; i < material.getLayers().size(); i++) {
			if (ModelUtils.isShaderStringSupported(formatVersion)
					&& (material.getShaderString() != null)
					&& (material.getShaderString().length() > 0)) {
				NGGLDP.pipeline.glActiveHDTexture(i);
			}
			Layer layer = material.getLayers().get(i);

			boolean opaqueLayer = (layer.getFilterMode() == FilterMode.NONE) || (layer.getFilterMode() == FilterMode.TRANSPARENT);

			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				Bitmap tex = layer.getRenderTexture(renderEnv, modelView.getModel());

				if (tex != null) {
					textureThing.bindLayerTexture(layer, tex, formatVersion, material);
				}

				if (overriddenColors) {
					NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				} else {
					setStandardColors(renderColor, geosetAnimVisibility * layer.getRenderVisibility(renderEnv), layer);
				}

				renderMesh3(geo, layer, renderTextures);
			}
		}
	}

	//, boolean renderTextures, boolean wireFrame
	private void renderMesh3(Geoset geo, Layer layer, boolean renderTextures) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (renderGeoset != null) {
			NGGLDP.pipeline.glBegin(GL11.GL_TRIANGLES);
			for (Triangle tri : geo.getTriangles()) {
//				if (programPreferences != null && !programPreferences.textureModels() && cameraHandler.isOrtho()) {
				if (programPreferences != null && !renderTextures && cameraHandler.isOrtho()) {
					getTriAreaColor(tri);
				}
				GeosetVertex[] verts = tri.getVerts();
				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
					RenderGeoset.RenderVert[] renderVert = new RenderGeoset.RenderVert[3];
					renderVert[0] = renderGeoset.getRenderVert(verts[0]);
					renderVert[1] = renderGeoset.getRenderVert(verts[1]);
					renderVert[2] = renderGeoset.getRenderVert(verts[2]);
					if (renderVert[0] != null && renderVert[1] != null && renderVert[2] != null) {
						paintVert(getUv(layer, verts[0]), renderVert[0].getRenderPos(), renderVert[0].getRenderNorm(), renderVert[0].getRenderTang());
						paintVert(getUv(layer, verts[1]), renderVert[1].getRenderPos(), renderVert[1].getRenderNorm(), renderVert[1].getRenderTang());
						paintVert(getUv(layer, verts[2]), renderVert[2].getRenderPos(), renderVert[2].getRenderNorm(), renderVert[2].getRenderTang());
//						System.out.println("RenderVert: " + renderVert[0].getRenderPos());
					}
				}
			}
			NGGLDP.pipeline.glEnd();
		}
	}



	private void setStandardColors(Vec3 renderColor, float alphaValue, Layer layer) {
		if (renderColor != null) {
			if (layer.getFilterMode() == FilterMode.ADDITIVE) {
				NGGLDP.pipeline.glColor4f(renderColor.x * alphaValue, renderColor.y * alphaValue, renderColor.z * alphaValue, alphaValue);
			} else {
				NGGLDP.pipeline.glColor4f(renderColor.x, renderColor.y, renderColor.z, alphaValue);
			}
		} else {
			NGGLDP.pipeline.glColor4f(1f, 1f, 1f, alphaValue);
		}
	}

	private Vec2 getUv(Layer layer, GeosetVertex vertex) {
		int coordId = layer.getCoordId();
		if (coordId >= vertex.getTverts().size()) {
			coordId = vertex.getTverts().size() - 1;
		}
		return vertex.getTverts().get(coordId);
	}

	private void paintVert(Vec2 uv, Vec3 vert, Vec3 normal, Vec4 tangent) {
		NGGLDP.pipeline.glNormal3f(normal.x, normal.y, normal.z);
		NGGLDP.pipeline.glTangent4f(tangent.x, tangent.y, tangent.z, tangent.w);
		NGGLDP.pipeline.glTexCoord2f(uv.x, uv.y);
		NGGLDP.pipeline.glVertex3f(vert.x, vert.y, vert.z);
	}

	private void renderNormals(int formatVersion) {
		GL11.glDepthMask(true);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		NGGLDP.pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);

		NGGLDP.pipeline.glBegin(GL11.GL_LINES);
		NGGLDP.pipeline.glColor3f(1f, 1f, 3f);

		for (Geoset geo : modelView.getVisibleGeosets()) {
			RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
			if (correctLoD(geo, formatVersion) && renderGeoset != null) {
				for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
					Vec3 renderPos = renderVert.getRenderPos();
					Vec3 renderNorm = renderVert.getRenderNorm();

					paintNormal(renderPos, renderNorm);
				}
			}
		}
		NGGLDP.pipeline.glEnd();
	}

	private void paintNormal(Vec3 vertexSumHeap, Vec3 normalSumHeap) {
		NGGLDP.pipeline.glNormal3f(normalSumHeap.x, normalSumHeap.y, normalSumHeap.z);
		NGGLDP.pipeline.glVertex3f(vertexSumHeap.x, vertexSumHeap.y, vertexSumHeap.z);

		float factor = (float) (6 / cameraHandler.getZoom());

		NGGLDP.pipeline.glNormal3f(normalSumHeap.x, normalSumHeap.y, normalSumHeap.z);
		NGGLDP.pipeline.glVertex3f(
				vertexSumHeap.x + (normalSumHeap.x * factor),
				vertexSumHeap.y + (normalSumHeap.y * factor),
				vertexSumHeap.z + (normalSumHeap.z * factor));
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
		NGGLDP.pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
		NGGLDP.pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
		NGGLDP.pipeline.glDisableIfNeeded(GL_SHADE_MODEL);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

//		glColor3f(255f, 1f, 255f);
		NGGLDP.pipeline.glColor4f(.7f, .0f, .0f, .4f);
		for (final Geoset geo : modelView.getEditableGeosets()) {
			if (correctLoD(geo, formatVersion) && modelView.shouldRender(geo)) {
				vertRendererThing.updateSquareSize(cameraHandler.getPixelSize());
				paintVertSquares2(geo);
			}
		}

		NGGLDP.pipeline.glEnableIfNeeded(GL_SHADE_MODEL);
	}

	public void paintVertSquares2(Geoset geo) {
//		float v = (float) ((cameraHandler.geomX(4) - cameraHandler.geomX(0)));//*cameraHandler.getZoom());
//		glBegin(GL11.GL_TRIANGLES);

//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		glPolygonMode(GL_COLOR, GL_FILL);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);

		if (renderGeoset != null) {
			NGGLDP.pipeline.glPolygonMode(GL_FRONT, GL_FILL);
			NGGLDP.pipeline.glBegin(GL11.GL_TRIANGLES);
			EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (renderVert != null && !modelView.isHidden(renderVert.getVertex())) {
					float[] components;
					if (modelView.isEditable(renderVert.getVertex()) && modelView.isSelected(renderVert.getVertex())) {
						components = colorPrefs.getColorComponents(ColorThing.VERTEX_SELECTED);
					} else if (modelView.isEditable(renderVert.getVertex())) {
						components = colorPrefs.getColorComponents(ColorThing.VERTEX);
					} else {
						components = colorPrefs.getColorComponents(ColorThing.VERTEX_UNEDITABLE);
					}
					NGGLDP.pipeline.glColor4f(components[0], components[1], components[2], components[3]);
					vertRendererThing.transform(cameraHandler.getInverseCameraRotation(), renderVert.getRenderPos()).doGlGeom();
				}
			}
			NGGLDP.pipeline.glEnd();
		}
	}

	private void getTriAreaColor(Triangle triangle) {
		float[] color;
//		if (triangle.getGeoset() == modelView.getHighlightedGeoset()) {
//			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA_HIGHLIGHTED);
////				GL11.glColor4f(.95f, .2f, .2f, 1f);
//		} else if (triFullySelected(triangle)) {
//			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA_SELECTED);
//		} else if (triFullyEditable(triangle)) {
//			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA);
////				GL11.glColor4f(1f, 1f, 1f, 1f);
//		} else {
//			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA_UNEDITABLE);
////				GL11.glColor4f(.5f, .5f, .5f, 1f);
//		}
		if (triangle.getGeoset() == modelView.getHighlightedGeoset()) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
//				GL11.glColor4f(.95f, .2f, .2f, 1f);
		} else if (triFullySelected(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
		} else if (triFullyEditable(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
//				GL11.glColor4f(1f, 1f, 1f, 1f);
		} else {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
//				GL11.glColor4f(.5f, .5f, .5f, 1f);
		}

		NGGLDP.pipeline.glColor4f(color[0], color[1], color[2], color[3]);
	}


	private boolean triFullySelected(Triangle triangle){
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}
	private boolean triFullyEditable(Triangle triangle){
		return modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2));
	}
	private boolean correctLoD(Geoset geo, int formatVersion) {
		if ((ModelUtils.isLevelOfDetailSupported(formatVersion)) && (geo.getLevelOfDetailName() != null) && (geo.getLevelOfDetailName().length() > 0) && levelOfDetail > -1) {
			return geo.getLevelOfDetail() == levelOfDetail;
		}
		return true;
	}

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
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
}
