package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static org.lwjgl.opengl.GL11.glColor4f;

public class GeosetRenderThing {
	public static final float NORMAL_RENDER_LENGTH = 0.025f;
	private int levelOfDetail = 0;
	private RenderModel renderModel;
	private ModelView modelView;
	private EditableModel model;
	private TextureThing textureThing;
	private final ProgramPreferences programPreferences;
	private EditorColorPrefs colorPrefs;

	private final Vec3 vertexHeap = new Vec3();
	private final Vec4 colorHeap = new Vec4();
	private final Vec3 fresnelColorHeap = new Vec3();
	private final Vec3 normalHeap = new Vec3();
	private final Vec3 normalHeap2 = new Vec3();
	private final Vec4 tangentHeap = new Vec4();
	private final Vec2 uvHeap = new Vec2();
	private final Mat4 skinBonesMatrixHeap = new Mat4();
	private Mat4 uvTransform = new Mat4();
	private float fresnelOpacity = 1;
	private float fresnelTeamColor = 0;

	private final Vec3 screenDimension = new Vec3();
	private final Mat4 screenDimensionMat3Heap = new Mat4();

	public GeosetRenderThing(){
		this.programPreferences = ProgramGlobals.getPrefs();
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
	}

	public GeosetRenderThing setModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing){
		this.renderModel = renderModel;
		this.textureThing = textureThing;
		if(modelView != null){
			this.modelView = modelView;
			model = modelView.getModel();
		} else {
			modelView = null;
			model = null;
		}
		return this;
	}

	public GeosetRenderThing setLod(int lod){
		levelOfDetail = lod;
		return this;
	}

	public void render(ShaderPipeline pipeline, boolean renderTextures) {
		if(renderModel != null){
			int formatVersion = model.getFormatVersion();
			for (Geoset geo : model.getGeosets()) {
				if(correctLoD(formatVersion, geo) && modelView.shouldRender(geo)){
					if(renderTextures){
						colorHeap.set(renderModel.getRenderGeoset(geo).getRenderColor());
					} else {
						colorHeap.set(1,1,1,1);
					}
					if(colorHeap.w > RenderModel.MAGIC_RENDER_SHOW_CONSTANT){
						render(pipeline, geo, formatVersion, renderTextures);
					} else {
						System.out.println("invis!");
					}
				}
			}
		}
	}

	public void renderVerts(ShaderPipeline pipeline, int formatVersion) {
		if(renderModel != null){
			for (Geoset geo : model.getGeosets()) {
				if(correctLoD(formatVersion, geo) && modelView.shouldRender(geo)){
					colorHeap.set(renderModel.getRenderGeoset(geo).getRenderColor());
					if(colorHeap.w > RenderModel.MAGIC_RENDER_SHOW_CONSTANT){
						renderVerts(pipeline, geo, formatVersion);
					} else {
						System.out.println("invis!");
					}
				}
			}
		}
	}

	PeriodicOut periodicOut = new PeriodicOut(1000);

	private void render(ShaderPipeline pipeline, Geoset geo, int formatVersion, boolean renderTextures) {
		Material material = geo.getMaterial();
//		System.out.println("\ngeoset: " + geo.getName());
		fresnelColorHeap.set(0f,0f,0f);
		for (int i = 0; i < material.getLayers().size(); i++) {
			Layer layer = material.getLayers().get(i);
			boolean hdTextureOnlyLayer = false;
			boolean isHD = false;
			boolean hdNoMetaDataLayer = false;
			boolean doSetUpFilterMode = true;
			if (ModelUtils.isShaderStringSupported(formatVersion) && material.getShaderString() != null && material.getShaderString().length() > 0) {
				pipeline.glActiveHDTexture(i);
				hdTextureOnlyLayer = i != (material.getLayers().size() - 1);
				hdNoMetaDataLayer = i != 0;
				isHD = true;
				doSetUpFilterMode = i == 0;
			}
			if (!isHD || i == 0) {
				pipeline.glBegin(GL11.GL_TRIANGLES);
			}

			if (doSetUpFilterMode) {
				setRenderColor(layer);
				if (hdTextureOnlyLayer) {
//				if (!hdNoMetaDataLayer) {
					// (this branch assures it's HD, if you hate this code paradigm change it to "isHD()" for the check)
					fresnelColorHeap.set(layer.getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ZERO));
//					fresnelColorHeap.set(layer.getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ONE));
					fresnelTeamColor = layer.getInterpolatedFloat(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, 0);
					fresnelOpacity = layer.getInterpolatedFloat(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_OPACITY, 0.0f);
//					pipeline.glFresnelColor3f(fresnelColorHeap);
					pipeline.glFresnelTeamColor1f(fresnelTeamColor);
					pipeline.glFresnelOpacity1f(fresnelOpacity);
				} else {
					fresnelColorHeap.set(0f,0f,0f);
				}
//				fresnelColorHeap.set(1f,1f,1f);
			}

			boolean twoSided = layer.getTwoSided() || (ModelUtils.isShaderStringSupported(formatVersion) && material.getTwoSided());
			Bitmap tex = layer.getRenderTexture(renderModel.getTimeEnvironment(), model);
//			periodicOut.print("texture: " + tex.getName() + ", geoset: " + geo.getName() + ", slot: " + i);
//			System.out.println("texture: " + tex.getName() + ", geoset: " + geo.getName() + ", slot: " + i);
			if(renderTextures){
				pipeline.prepareToBindTexture();
//				textureThing.bindLayerTexture(pipeline, layer, doSetUpFilterMode, i, twoSided, tex);
				textureThing.loadAndBindLayerTexture(pipeline, model, layer, doSetUpFilterMode, i, twoSided, tex);
			}
//			if (!hdTextureOnlyLayer) {
			if (!isHD || i == 0) {
//			if (!hdNoMetaDataLayer) {
//				pipeline.glBegin(GL11.GL_TRIANGLES);
//				drawGeo(pipeline, geo, layer);
			}
			if(!isHD || i == material.getLayers().size() - 1){
				drawGeo(pipeline, geo, layer, renderTextures);
				pipeline.glEnd();
			}
		}

	}

	Vec4 triColor = new Vec4();
	private void drawGeo(ShaderPipeline pipeline, Geoset geo, Layer layer, boolean renderTextures) {
		uvTransform = getUVTransform(layer);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		for (Triangle tri : geo.getTriangles()) {
			getTriRGBA(tri);
			triColor.set(colorHeap);
			for (GeosetVertex v : tri.getVerts()) {
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();
				Vec4 renderTang = renderVert.getRenderTang();

				getUV(layer.getCoordId(), v, uvTransform);

				if(!renderTextures){
					getFaceRGBA(v);
					colorHeap.addScaled(triColor, .25f).scale(.8f);
				}

				pipeline.addVert(renderPos, renderNorm, renderTang, uvHeap, colorHeap, fresnelColorHeap);
			}
		}
	}
	private void renderVerts(ShaderPipeline pipeline, Geoset geo, int formatVersion) {

//		pipeline.glBegin(GL11.GL_POINTS);
		pipeline.glBegin(GL11.GL_TRIANGLES);
		drawVVerts(pipeline, geo);
		pipeline.glEnd();

	}

	private void drawVVerts(ShaderPipeline pipeline, Geoset geo) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		for (GeosetVertex v : geo.getVertices()) {
			if(!modelView.isHidden(v)){
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();
				Vec4 renderTang = renderVert.getRenderTang();


				pipeline.addVert(renderPos, renderNorm, renderTang, uvHeap, colorHeap, fresnelColorHeap);
			}
		}

//		for (Triangle tri : geo.getTriangles()) {
//			for (GeosetVertex v : tri.getVerts()) {
//				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
//				Vec3 renderPos = renderVert.getRenderPos();
//				Vec3 renderNorm = renderVert.getRenderNorm();
//				Vec4 renderTang = renderVert.getRenderTang();
//
//
//				pipeline.addVert(renderPos, renderNorm, renderTang, uvHeap, colorHeap, fresnelColorHeap);
//			}
//		}
//
	}

	public void drawNormals(ShaderPipeline pipeline) {
		if(renderModel != null){
			// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			pipeline.glBegin(GL11.GL_POINTS);
//			pipeline.glColor3f(1f, 1f, 3f);
			colorHeap.set(.7f, .7f, 1f, 1f);
			// if( wireframe.isSelected() )
			for (Geoset geo : model.getGeosets()) {
				if (correctLoD(model.getFormatVersion(), geo) && modelView.shouldRender(geo)) {
					drawNormals(pipeline, geo);
				}
			}
			pipeline.glEnd();
		}
	}


	private void drawNormals(ShaderPipeline pipeline, Geoset geo) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		for (GeosetVertex v : geo.getVertices()) {
			if(!modelView.isHidden(v)){
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();

				pipeline.addVert(renderPos, renderNorm, tangentHeap, uvHeap, colorHeap, fresnelColorHeap);
			}
		}
	}

	public void drawVerts(ShaderPipeline pipeline) {
		if(renderModel != null){
			// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			pipeline.glBegin(GL11.GL_POINTS);
//			pipeline.glColor3f(1f, 1f, 3f);
			colorHeap.set(1f, .0f, 1f, 1f);
			// if( wireframe.isSelected() )
			for (Geoset geo : model.getGeosets()) {
				if (correctLoD(model.getFormatVersion(), geo) && modelView.shouldRender(geo)) {
					drawVerts(pipeline, geo);
				}
			}
			pipeline.glEnd();
		}
	}


	private void drawVerts(ShaderPipeline pipeline, Geoset geo) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		for (GeosetVertex v : geo.getVertices()) {
			if(!modelView.isHidden(v)){
				getVertRGBA(v);
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();

				pipeline.addVert(renderPos, renderNorm, tangentHeap, uvHeap, colorHeap, fresnelColorHeap);
			}
		}
	}

	private void setRenderColor(Layer layer) {
		if (renderModel.getTimeEnvironment().getCurrentSequence() != null) {
			float layerVisibility = layer.getRenderVisibility(renderModel.getTimeEnvironment());

			if (layer.getFilterMode() == FilterMode.ADDITIVE) {
				float alphaValue = colorHeap.w * layerVisibility;
				colorHeap.scale(alphaValue);
				colorHeap.w = alphaValue;
			} else {
				colorHeap.w *= layerVisibility;
			}

		} else {
			resetColorHeap();
		}
//		colorHeap.set(1,0,1,1);

	}

	private Vec2 getUV(int coordId, GeosetVertex vertex, Mat4 uvTransform) {
		List<Vec2> uvs = vertex.getTverts();
		if (coordId >= uvs.size()) {
			coordId = uvs.size() - 1;
		}
		uvHeap.set(uvs.get(coordId));
		if(uvTransform != null){
			uvHeap.transform2(uvTransform);
		}
		return uvHeap;
	}

	private Mat4 getUVTransform(Layer layer) {
		if(layer.getTextureAnim() != null){
			uvTransform.setIdentity();

			uvTransform.fromRotationTranslationScale(
					layer.getTextureAnim().getInterpolatedQuat(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_ROTATION, Quat.IDENTITY),
					layer.getTextureAnim().getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_TRANSLATION, Vec3.ZERO),
					layer.getTextureAnim().getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_SCALING, Vec3.ONE)
			);
			return uvTransform;
		}
		return null;
	}

	private void resetColorHeap(){
		colorHeap.set(1f, 1f, 1f, 1f);
	}

	private boolean correctLoD(int formatVersion, Geoset geo) {
		return !(ModelUtils.isLevelOfDetailSupported(formatVersion)
				&& geo.getLevelOfDetailName() != null
				&& geo.getLevelOfDetailName().length() > 0
				&& geo.getLevelOfDetail() != levelOfDetail);
	}


	private float[] getVertRGBA(GeosetVertex vertex) {
		float[] rgba;
		if (modelView.isEditable(vertex) && modelView.isSelected(vertex)) {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX_SELECTED);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.VERTEX_SELECTED));
		} else if (modelView.isEditable(vertex)) {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.VERTEX));
		} else {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX_UNEDITABLE);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.VERTEX_UNEDITABLE));
		}
		return rgba;
	}
	private float[] getFaceRGBA(GeosetVertex vertex) {
		float[] rgba;
		if (vertex.getGeoset() == modelView.getHighlightedGeoset()) {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED));
		} else if (modelView.isEditable(vertex) && modelView.isSelected(vertex)) {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED));
		} else if (modelView.isEditable(vertex)) {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE));
		} else {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE));
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
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED));
		} else if (triFullySelected(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED));
		} else if (triFullyEditable(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE));
		} else {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE));
		}
		return color;
	}


	private boolean triFullySelected(Triangle triangle){
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}
	private boolean triFullyEditable(Triangle triangle){
		return modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2));
	}
}
