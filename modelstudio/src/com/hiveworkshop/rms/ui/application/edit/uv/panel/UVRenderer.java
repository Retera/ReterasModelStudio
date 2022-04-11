package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.VertRendererThing;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class UVRenderer {
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

	public UVRenderer(CameraHandler cameraHandler, ProgramPreferences programPreferences) {
		this.cameraHandler = cameraHandler;
		this.programPreferences = programPreferences;
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
		vertRendererThing = new VertRendererThing((float) (cameraHandler.sizeAdj() * 4));


//		System.out.println("Geometry "+GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER));
//		System.out.println("Fragment "+GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER));
	}

	public UVRenderer updateModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
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

	public UVRenderer doRender() {

		renderGeosets(modelView.getVisibleGeosets());

		if (modelView.getHighlightedGeoset() != null) {
			drawHighlightedGeosets();
		}

//		if (show3dVerts) {
//			renderVertDots(formatVersion);
//		}
		return this;
	}

	private void drawHighlightedGeosets() {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if ((programPreferences != null) && (programPreferences.getHighlighTriangleColor() != null)) {
			final Color highlightTriangleColor = programPreferences.getHighlighTriangleColor();
			glColor3f(highlightTriangleColor.getRed() / 255f, highlightTriangleColor.getGreen() / 255f, highlightTriangleColor.getBlue() / 255f);
		} else {
			glColor3f(1f, 3f, 1f);
		}
		if (!modelView.getHighlightedGeoset().getVertices().isEmpty()) {
			renderMesh3(modelView.getHighlightedGeoset());
		}
	}

	private void renderGeosets(Iterable<Geoset> geosets) {
		GL11.glDepthMask(true);
		glShadeModel(GL11.GL_FLAT);
//		glDisable(GL_SHADE_MODEL);
//		if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
//			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		}
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glDisable(GL_CULL_FACE);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(false);

		for (Geoset geo : geosets) {
			if (modelView.shouldRender(geo)) {
				if (!geo.getVertices().isEmpty()) {
					renderMesh3(geo);
				}
			}
		}
	}

	private void renderMesh3(Geoset geo) {
		if (geo != null) {
			glBegin(GL11.GL_TRIANGLES);
			for (Triangle tri : geo.getTriangles()) {
//				if (programPreferences != null && !programPreferences.textureModels() && cameraHandler.isOrtho()) {
				if (programPreferences != null && cameraHandler.isOrtho()) {
					getTriAreaColor(tri);
				}
				GeosetVertex[] verts = tri.getVerts();
				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
					if (verts[0] != null && verts[1] != null && verts[2] != null) {
						paintVert(verts[0].getTverts().get(0));
						paintVert(verts[1].getTverts().get(0));
						paintVert(verts[2].getTverts().get(0));
//						paintVert(getUv(layer, verts[0]));
//						paintVert(getUv(layer, verts[1]));
//						paintVert(getUv(layer, verts[2]));

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

	private Vec2 getUv(Layer layer, GeosetVertex vertex) {
		int coordId = layer.getCoordId();
		if (coordId >= vertex.getTverts().size()) {
			coordId = vertex.getTverts().size() - 1;
		}
		return vertex.getTverts().get(coordId);
	}

	private void paintVert(Vec2 uv) {
		GL11.glNormal3f(0, 0, 1);
		GL11.glVertex3f(uv.x, uv.y, 0);
//		System.out.println(uv);
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
				vertRendererThing.updateSquareSize((float) (cameraHandler.sizeAdj()*4));
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
					float[] components;
					if (modelView.isEditable(renderVert.getVertex()) && modelView.isSelected(renderVert.getVertex())) {
						components = colorPrefs.getColorComponents(ColorThing.VERTEX_SELECTED);
					} else if (modelView.isEditable(renderVert.getVertex())) {
						components = colorPrefs.getColorComponents(ColorThing.VERTEX);
					} else {
						components = colorPrefs.getColorComponents(ColorThing.VERTEX_UNEDITABLE);
					}
					glColor4f(components[0], components[1], components[2], components[3]);
					vertRendererThing.transform(cameraHandler.getInverseCameraRotation(), renderVert.getRenderPos()).doGlGeom();
				}
			}
			glEnd();
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

		glColor4f(color[0], color[1], color[2], color[3]);
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
