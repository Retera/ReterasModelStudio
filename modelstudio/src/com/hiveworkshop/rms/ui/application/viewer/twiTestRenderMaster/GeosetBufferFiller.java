package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.HdBufferSubInstance;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GeosetBufferFiller {
	public static final float NORMAL_RENDER_LENGTH = 0.025f;
	private int levelOfDetail = 0;
	private RenderModel renderModel;
	private ModelView modelView;
	private EditableModel model;
	private TextureThing textureThing;
	private EditorColorPrefs colorPrefs;

	private final Vec3 vertexHeap = new Vec3();
	private final Vec4 layerColorHeap = new Vec4();
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

	public GeosetBufferFiller() {
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
	}

	public GeosetBufferFiller setModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
		this.renderModel = renderModel;
		this.textureThing = textureThing;
		this.modelView = modelView;
		if (renderModel != null) {
			this.model = renderModel.getModel();
		} else {
			this.model = null;
		}
		return this;
	}

	public GeosetBufferFiller setLod(int lod) {
		levelOfDetail = lod;
		return this;
	}

	private List<Geoset> sortedGeos = new ArrayList<>();
	private List<Geoset> opaqueGeos = new ArrayList<>();
	private List<Geoset> transperentGeos = new ArrayList<>();
	public void fillBuffer(ShaderManager shaderManager, boolean renderTextures) {


		opaqueGeos.clear();
		transperentGeos.clear();
		sortedGeos.clear();
		if (renderModel != null) {
			int formatVersion = model.getFormatVersion();
			boolean shaderStringSupported = ModelUtils.isShaderStringSupported(formatVersion);
			shaderManager.getPipeline(ShaderManager.PipelineType.MESH).prepare();

			for (Geoset geo : model.getGeosets()) {
				if (correctLoD(formatVersion, geo) && modelView.shouldRender(geo)) {
					if (geo.isOpaque()) {
						opaqueGeos.add(geo);
					} else {
						transperentGeos.add(geo);
					}
				}
			}
			sortedGeos.addAll(transperentGeos);
			sortedGeos.addAll(opaqueGeos);

			for (Geoset geo : sortedGeos) {
				if (correctLoD(formatVersion, geo) && modelView.shouldRender(geo)) {
//					Material material = geo.getMaterial();
//					boolean hd = shaderStringSupported && material.getShaderString() != null && material.getShaderString().length() > 0;
					ShaderPipeline pipeline = shaderManager.getPipeline(ShaderManager.PipelineType.MESH);
//					System.out.println("pipeline: " + pipeline.getClass());
					if (renderTextures) {
						colorHeap.set(renderModel.getRenderGeoset(geo).getRenderColor());
					} else {
						colorHeap.set(1,1,1,1);
					}
					if (colorHeap.w > RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
						renderInst(pipeline, geo, formatVersion, renderTextures);
//					} else {
//						System.out.println("invis!");
					}
				}
			}
		}
	}

	PeriodicOut periodicOut = new PeriodicOut(1000);


	private void renderInst(ShaderPipeline pipeline, Geoset geo, int formatVersion, boolean renderTextures) {
		Material material = geo.getMaterial();
//		System.out.println("\ngeoset: " + geo.getName());
		fresnelColorHeap.set(0f,0f,0f);

		// the geoset/layer/render color here is probably wrong...

		int numLayers = material.getLayers().size();
		Layer lastAddedLayer = null;
		HdBufferSubInstance lastInstance = null;

//		for (int i = 0; i < numLayers; i++) {
		for (int i = numLayers-1; 0 <= i; i--) {
			Layer layer = material.getLayers().get(i);
			if (0 < layer.getRenderVisibility(renderModel.getTimeEnvironment())) {
				HdBufferSubInstance instance = new HdBufferSubInstance(model, textureThing);
				instance.setRenderTextures(renderTextures);
				instance.setMaterial(material, i, renderModel.getTimeEnvironment());
				instance.setLayerColor(renderModel.getRenderGeoset(geo).getRenderColor());
				instance.setOpaque(geo.isOpaque());
				if (lastAddedLayer == null || layer.getCoordId() != lastAddedLayer.getCoordId()) {
					lastAddedLayer = layer;
					pipeline.startInstance(instance);
					setRenderColor(layer);
					lastInstance = instance;

					drawGeo(pipeline, geo, layer, renderTextures);
					pipeline.endInstance();
				} else {
					instance.setOffset(lastInstance.getOffset());
					instance.setVertCount(lastInstance.getVertCount());
					pipeline.overlappingInstance(instance);
				}
			}
		}



	}


	Vec4 triColor = new Vec4();
	private void drawGeo(ShaderPipeline pipeline, Geoset geo, Layer layer, boolean renderTextures) {
		Mat4 uvTransform = getUVTransform(layer);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		layerColorHeap.set(renderGeoset.getRenderColor());
		for (Triangle tri : geo.getTriangles()) {
			triColor.set(getTriRGBA(tri));
			for (GeosetVertex v : tri.getVerts()) {
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				if (renderVert != null) {
					Vec3 renderPos = renderVert.getRenderPos();
					Vec3 renderNorm = renderVert.getRenderNorm();
					Vec4 renderTang = renderVert.getRenderTang();

					getUV(layer.getCoordId(), v, uvTransform);

					if (!renderTextures) {
						colorHeap.set(getFaceRGBA(v));
//					    colorHeap.addScaled(triColor, .25f).scale(.8f); // (4/4 + 1/4) * 4/5 = 4/4
						colorHeap.addScaled(triColor, .5625f).scale(.64f); // (16/16 + 9/16) * 16/25 = 16/16
//					    colorHeap.add(triColor).scale(.5f);
					} else {
						colorHeap.set(layerColorHeap);
					}

					int selectionStatus = getSelectionStatus(v);

					pipeline.addVert(renderPos, renderNorm, renderTang, uvHeap, colorHeap, fresnelColorHeap, selectionStatus);
				}
			}
		}
	}

	private int getSelectionStatus(GeosetVertex vertex) {
		if (modelView.getHighlightedGeoset() != null && modelView.getHighlightedGeoset() == vertex.getGeoset()) {
			return 0;
		} else if (modelView.isEditable(vertex)) {
			if (modelView.isSelected(vertex)) {
				return 1;
			} else {
				return 2;
			}
		}
		return 3;
	}

	public void fillNormalsBuffer(ShaderPipeline pipeline) {
		if (renderModel != null) {
			// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			pipeline.prepare();
//			pipeline.glColor3f(1f, 1f, 3f);
			colorHeap.set(.7f, .7f, 1f, 1f);
			for (Geoset geo : model.getGeosets()) {
				if (correctLoD(model.getFormatVersion(), geo) && modelView.shouldRender(geo)) {
					fillNormalsBuffer(pipeline, geo);
				}
			}
		}
	}


	private void fillNormalsBuffer(ShaderPipeline pipeline, Geoset geo) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		for (GeosetVertex v : geo.getVertices()) {
			if (!modelView.isHidden(v)) {
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();

				pipeline.addVert(renderPos, renderNorm, tangentHeap, uvHeap, colorHeap, fresnelColorHeap, getSelectionStatus(v));
			}
		}
	}

	public void fillVertsBuffer(ShaderPipeline pipeline) {
		if (renderModel != null) {
			// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			pipeline.prepare();
//			pipeline.glColor3f(1f, 1f, 3f);
			colorHeap.set(1f, .0f, 1f, 1f);
			for (Geoset geo : model.getGeosets()) {
				if (correctLoD(model.getFormatVersion(), geo) && modelView.shouldRender(geo)) {
					fillVertsBuffer(pipeline, geo);
				}
			}
		}
	}


	private void fillVertsBuffer(ShaderPipeline pipeline, Geoset geo) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		for (GeosetVertex v : geo.getVertices()) {
			if (!modelView.isHidden(v)) {
				getVertRGBA(v);
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();

				pipeline.addVert(renderPos, renderNorm, tangentHeap, uvHeap, colorHeap, fresnelColorHeap, getSelectionStatus(v));
			}
		}
	}

	private Vec4 setRenderColor(Layer layer) {
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
		return colorHeap;
//		colorHeap.set(1,0,1,1);

	}

	private Vec2 getUV(int coordId, GeosetVertex vertex, Mat4 uvTransform) {
		List<Vec2> uvs = vertex.getTverts();
		if (coordId >= uvs.size()) {
			coordId = uvs.size() - 1;
		}
		uvHeap.set(uvs.get(coordId));
		return uvHeap;
	}

	private Mat4 getUVTransform(Layer layer) {
		if (layer.getTextureAnim() != null) {
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

	private void resetColorHeap() {
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
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED));
		} else if (modelView.isEditable(vertex) && modelView.isSelected(vertex)) {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED));
		} else if (modelView.isEditable(vertex)) {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE));
		} else {
			rgba = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE));
		}
		return rgba;
	}

	private float[] getTriRGBA(Triangle triangle) {
		float[] color;
		if (triangle.getGeoset() == modelView.getHighlightedGeoset()) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED));
		} else if (triFullySelected(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED));
		} else if (triFullyEditable(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE));
		} else {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
//			colorHeap.set(colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE));
		}
		return color;
	}


	private boolean triFullySelected(Triangle triangle) {
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}
	private boolean triFullyEditable(Triangle triangle) {
		return modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2));
	}
}
