package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class GeosetRendererBuf {
	private final ProgramPreferences programPreferences;
	private CameraHandler cameraHandler;
	private final VertRendererThingBuf vertRendererThing;
	private TextureThing textureThing;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;
	private EditorColorPrefs colorPrefs;
	private ModelView modelView;
	private int levelOfDetail = -1;
	private float[] rgba = new float[4];

	VertexBuffers vertexBuffers = new VertexBuffers();
//	VertexBuffers dotBuffers = new VertexBuffers();

	boolean texLoaded = true;
	public GeosetRendererBuf(CameraHandler cameraHandler, ProgramPreferences programPreferences){
		this.cameraHandler = cameraHandler;
		this.programPreferences = programPreferences;
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
		vertRendererThing = new VertRendererThingBuf(cameraHandler.getPixelSize());


//		System.out.println("Geometry "+GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER));
//		System.out.println("Fragment "+GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER));
	}
	public GeosetRendererBuf updateModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
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

	public GeosetRendererBuf doRender(boolean renderTextures, boolean wireFrame, boolean showNormals, boolean show3dVerts){
		vertexBuffers.resetIndex();
		int formatVersion = modelView.getModel().getFormatVersion();

		for (Geoset geo : modelView.getVisibleGeosets()) {
			if(modelView.getHighlightedGeoset() == geo){
				drawHighlightedGeosets(formatVersion, renderTextures);
			} else {
				renderGeoset(geo, formatVersion, false, renderTextures, wireFrame);
			}

			if (showNormals) {
				glDepthMask(true);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
				glDisable(GL_TEXTURE_2D);
				renderNormals(geo, formatVersion);
			}
			if (show3dVerts) {
				GL11.glDepthMask(true);
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_CULL_FACE);

				glDisable(GL_ALPHA_TEST);
				glDisable(GL_TEXTURE_2D);
				glDisable(GL_SHADE_MODEL);
//				disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_SHADE_MODEL);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glEnable(GL11.GL_BLEND);
				renderVertDots(geo, formatVersion);
			}
		}
		return this;
	}

	private void drawHighlightedGeosets(int formatVersion, boolean renderTextures) {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if ((programPreferences != null) && (programPreferences.getHighlighTriangleColor() != null)) {
			final Color highlightTriangleColor = programPreferences.getHighlighTriangleColor();
			glColor3f(highlightTriangleColor.getRed() / 255f, highlightTriangleColor.getGreen() / 255f, highlightTriangleColor.getBlue() / 255f);
		} else {
			glColor3f(1f, 3f, 1f);
		}
		renderGeoset(modelView.getHighlightedGeoset(), true, formatVersion, true, renderTextures);
		renderGeoset(modelView.getHighlightedGeoset(), false, formatVersion, true, renderTextures);
	}

	// ToDo investigate why transparent Geosets don't render when renderTextures is false
	private void renderGeoset(Geoset geo, int formatVersion, boolean overriddenColors, boolean renderTextures, boolean wireFrame) {
		GL11.glDepthMask(true);
		glShadeModel(GL11.GL_FLAT);

		if (wireFrame) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glDisable(GL_CULL_FACE);
			glDisable(GL_ALPHA_TEST);
			glDisable(GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
		} else {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		}
		if (texLoaded && renderTextures) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			glEnable(GL11.GL_TEXTURE_2D);
			glShadeModel(GL_SMOOTH);
		}
		if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
			renderGeoset(geo, true, formatVersion, overriddenColors, renderTextures);
		}
		if (modelView.shouldRender(geo) || (modelView.getHighlightedGeoset() != geo && overriddenColors)) {
			renderGeoset(geo, false, formatVersion, overriddenColors, renderTextures);
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
//				System.out.println("Wont render");
				return;
			}
			renderColor = geosetAnim.getRenderColor(renderEnv);
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

			boolean opaqueLayer = isOpaqueLayer(geo, renderTextures, layer);

			if ((renderOpaque && opaqueLayer) || (!renderOpaque && !opaqueLayer)) {
				if(!modelView.isEditable(geo) && !renderOpaque && (geo.getName().contains("Pelvis")) && !renderTextures){
					System.out.println("should render geo " + geo.getName() + ": " + (!renderOpaque && !opaqueLayer) + ", has renderGeo: " + renderModel.getRenderGeoset(geo));
				}

				Bitmap tex = layer.getRenderTexture(renderEnv, modelView.getModel());

				if (tex != null) {
					textureThing.bindLayerTexture(layer, tex, formatVersion, material);
				}

				if (overriddenColors) {
					getStandardColors(null, 1f, FilterMode.NONE);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
				} else {
					getStandardColors(renderColor, geosetAnimVisibility * layer.getRenderVisibility(renderEnv), layer.getFilterMode());
				}

				renderMesh3(geo, layer, renderTextures);
			}
		}
	}

	private boolean isOpaqueLayer(Geoset geo, boolean renderTextures, Layer layer) {
		boolean opaqueFilterMode = layer.getFilterMode() == FilterMode.NONE || layer.getFilterMode() == FilterMode.TRANSPARENT;
		boolean notTex_notEd = !renderTextures && !modelView.isEditable(geo);
		return opaqueFilterMode && !notTex_notEd;
	}


	private void renderMesh3(Geoset geo, Layer layer, boolean renderTextures) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (renderGeoset != null) {
			vertexBuffers.resetIndex();
			vertexBuffers.ensureCapacity(geo.getTriangles().size() * 3);

			int renderedVertices = 0;
			float[] color;
			for (Triangle tri : geo.getTriangles()) {
				if (programPreferences != null && !renderTextures && cameraHandler.isOrtho()) {
					color = getTriAreaColor(tri);
				} else {
					color = rgba;
				}
				GeosetVertex[] verts = tri.getVerts();
				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
					for(GeosetVertex vertex : tri.getVerts()){
						RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
						Vec3 renderPos = renderVert.getRenderPos();
						Vec3 renderNorm = renderVert.getRenderNorm();

						int coordId = layer.getCoordId();
						var uvs = vertex.getTverts();
						if (coordId >= uvs.size()) {
							coordId = uvs.size() - 1;
						}

						Vec2 texcoord = uvs.get(coordId);
						vertexBuffers.setMultiple(renderPos, renderNorm, texcoord, color);
						renderedVertices++;
					}
				}
			}


			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glEnableClientState(GL_NORMAL_ARRAY);
			glEnableClientState(GL_COLOR_ARRAY);

			glVertexPointer(3, 0, vertexBuffers.getPositions());
			glTexCoordPointer(2, 0, vertexBuffers.getTexCoords());
			glNormalPointer(0, vertexBuffers.getNormals());
			glColorPointer(4, 0, vertexBuffers.getColors());

			glDrawArrays(GL_TRIANGLES, 0, renderedVertices);

			glDisableClientState(GL_VERTEX_ARRAY);
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			glDisableClientState(GL_NORMAL_ARRAY);
			glDisableClientState(GL_COLOR_ARRAY);
		}
	}


	private float[] getStandardColors(Vec3 renderColor, float alphaValue, FilterMode filterMode) {
		if (renderColor != null) {
			if (filterMode == FilterMode.ADDITIVE) {
				rgba[0] = renderColor.x * alphaValue;
				rgba[1] = renderColor.y * alphaValue;
				rgba[2] = renderColor.z * alphaValue;
			} else {
				rgba[0] = renderColor.x;
				rgba[1] = renderColor.y;
				rgba[2] = renderColor.z;
			}
		} else {
			rgba[0] = 1f;
			rgba[1] = 1f;
			rgba[2] = 1f;
		}
		rgba[3] = alphaValue;
		return rgba;
	}



	private void renderNormals(Geoset geo, int formatVersion) {
		glColor3f(1f, 1f, 3f);
		rgba[0] = 1f;
		rgba[1] = 1f;
		rgba[2] = 3f;
		rgba[3] = 1f;

		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (correctLoD(geo, formatVersion) && renderGeoset != null) {

			int renderedVertices = 0;
			Vec3 normEndPos = new Vec3();

			vertexBuffers.ensureCapacity(geo.getVertices().size() * 2);
			float sizeFactor = 6 * cameraHandler.getPixelSize();

			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();
				normEndPos.set(renderNorm).scale(sizeFactor).add(renderPos);

				vertexBuffers.setMultiple(renderPos, renderNorm, rgba);
				vertexBuffers.setMultiple(normEndPos, renderNorm, rgba);
				renderedVertices++;
			}

			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_COLOR_ARRAY);

			glVertexPointer(3, 0, vertexBuffers.getPositions());
			glColorPointer(3, 0, vertexBuffers.getColors());

			glDrawArrays(GL_LINES, 0, renderedVertices * 2);

			glDisableClientState(GL_VERTEX_ARRAY);
			glDisableClientState(GL_COLOR_ARRAY);
		}
	}


	private void renderVertDots(Geoset geo, int formatVersion) {

		if (correctLoD(geo, formatVersion) && modelView.shouldRender(geo)) {
			vertRendererThing.updateSquareSize(cameraHandler.getPixelSize());
			RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);

			if (renderGeoset != null) {
				int renderedVertices = 0;
				vertexBuffers.resetIndex();
				vertexBuffers.ensureCapacity(geo.getVertices().size() * 6);

//				glPolygonMode(GL_FRONT, GL_FILL);
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
				glDisable(GL_DEPTH_TEST);

				EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();
				for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
					if (renderVert != null && !modelView.isHidden(renderVert.getVertex())) {
						float[] rgba = getRGBA(colorPrefs, renderVert);
						VertRendererThingBuf thingBuf = vertRendererThing.transform(cameraHandler.getInverseCameraRotation(), renderVert.getRenderPos());
						thingBuf.doGlGeom(vertexBuffers, rgba);
						renderedVertices++;
					}
				}


				glEnableClientState(GL_VERTEX_ARRAY);
				glEnableClientState(GL_COLOR_ARRAY);

				glVertexPointer(3, 0, vertexBuffers.getPositions());
				glColorPointer(4, 0, vertexBuffers.getColors());

				glDrawArrays(GL_TRIANGLES, 0, renderedVertices * 6);

				glDisableClientState(GL_VERTEX_ARRAY);
				glDisableClientState(GL_COLOR_ARRAY);

			}
		}

	}

	private float[] getRGBA(EditorColorPrefs colorPrefs, RenderGeoset.RenderVert renderVert) {
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

	private float[] getTriAreaColor(Triangle triangle) {
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
