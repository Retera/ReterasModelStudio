package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.HdBufferSubInstance;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SdBufferSubInstance;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glColor4f;

public class GeosetRenderThing {
	public static final float NORMAL_RENDER_LENGTH = 0.025f;
	private final EditorColorPrefs colorPrefs;
	private int levelOfDetail = 0;
	private RenderModel renderModel;
	private ModelView modelView;
	private EditableModel model;
	private TextureThing textureThing;

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

	public GeosetRenderThing() {
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
	}

	public GeosetRenderThing setModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
		this.renderModel = renderModel;
		this.textureThing = textureThing;
		if (modelView != null) {
			this.modelView = modelView;
			model = modelView.getModel();
		} else {
			this.modelView = null;
			model = null;
		}
		return this;
	}

	public GeosetRenderThing setLod(int lod) {
		levelOfDetail = lod;
		return this;
	}

	private List<Geoset> sortedGeos = new ArrayList<>();
	private List<Geoset> opaqueGeos = new ArrayList<>();
	private List<Geoset> transperentGeos = new ArrayList<>();
	public void render(ShaderPipeline pipeline, boolean renderTextures) {
		if (renderModel != null) {
			opaqueGeos.clear();
			transperentGeos.clear();
			sortedGeos.clear();
			int formatVersion = model.getFormatVersion();
			pipeline.prepare();
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
				if (renderTextures) {
					colorHeap.set(renderModel.getRenderGeoset(geo).getRenderColor());
				} else {
					colorHeap.set(1,1,1,1);
				}
				if (colorHeap.w > RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
					renderInst(pipeline, geo, formatVersion, renderTextures);
				}
			}
		}
	}

	PeriodicOut periodicOut = new PeriodicOut(1000);

	private void renderInst(ShaderPipeline pipeline, Geoset geo, int formatVersion, boolean renderTextures) {
		Material material = geo.getMaterial();
//		System.out.println("\ngeoset: " + geo.getName());
		fresnelColorHeap.set(0f,0f,0f);


		int numLayers = material.getLayers().size();
		if (ModelUtils.isShaderStringSupported(formatVersion) && material.getShaderString() != null && material.getShaderString().length() > 0) {
//			pipeline.glBegin(GL11.GL_TRIANGLES);
			HdBufferSubInstance instance = new HdBufferSubInstance(model, textureThing);
			instance.setRenderTextures(renderTextures);
			instance.setMaterial(material, 0, renderModel.getTimeEnvironment());
			pipeline.startInstance(instance);

			Layer diffuseLayer = material.getLayer(0);
			setRenderColor(diffuseLayer);

			drawGeo(pipeline, geo, material.getLayer(numLayers - 1), renderTextures);
			pipeline.endInstance();
//			pipeline.glEnd();
		} else if (numLayers != 0) {
			Layer lastAddedLayer = null;
			SdBufferSubInstance lastInstance = null;

//			for (int i = 0; i < numLayers; i++) {
			for (int i = numLayers-1; i >=0; i--) {
				Layer layer = material.getLayers().get(i);
				if (0.1 < layer.getRenderVisibility(renderModel.getTimeEnvironment())) {
					SdBufferSubInstance instance = new SdBufferSubInstance(model, textureThing);
					instance.setRenderTextures(renderTextures);
					instance.setMaterial(material, i, renderModel.getTimeEnvironment());
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



	}


	Vec4 triColor = new Vec4();
	private void drawGeo(ShaderPipeline pipeline, Geoset geo, Layer layer, boolean renderTextures) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		layerColorHeap.set(renderGeoset.getRenderColor());
		for (Triangle tri : geo.getTriangles()) {
			getTriRGBA(tri);
			triColor.set(colorHeap);
			for (GeosetVertex v : tri.getVerts()) {
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(v);
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();
				Vec4 renderTang = renderVert.getRenderTang();

				getUV(layer.getCoordId(), v);

				if (!renderTextures) {
					getFaceRGBA(v);
//					colorHeap.addScaled(triColor, .25f).scale(.8f); // (4/4 + 1/4) * 4/5 = 4/4
					colorHeap.addScaled(triColor, .5625f).scale(.64f); // (16/16 + 9/16) * 16/25 = 16/16
//					colorHeap.add(triColor).scale(.5f);
				} else {
					colorHeap.set(layerColorHeap);
				}

				pipeline.addVert(renderPos, renderNorm, renderTang, uvHeap, colorHeap, fresnelColorHeap, getSelectionStatus(v));
			}
		}
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
			// if ( wireframe.isSelected() )
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

				pipeline.addVert(renderPos, renderNorm, tangentHeap, uvHeap, colorHeap, Vec3.ZERO);
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
			// if ( wireframe.isSelected() )
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

	private Vec2 getUV(int coordId, GeosetVertex vertex) {
		List<Vec2> uvs = vertex.getTverts();
		if (coordId >= uvs.size()) {
			coordId = uvs.size() - 1;
		}
		uvHeap.set(uvs.get(coordId));
		return uvHeap;
	}

	Vec3 uvCenter = new Vec3(1,1,1);
	private Mat4 getUVTransform(Layer layer) {
		if (layer.getTextureAnim() != null) {
			uvTransform.setIdentity();
			uvTransform.fromRotationTranslationScaleOrigin(
					layer.getTextureAnim().getInterpolatedQuat(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_ROTATION, Quat.IDENTITY),
					layer.getTextureAnim().getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_TRANSLATION, Vec3.ZERO),
					layer.getTextureAnim().getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_SCALING, Vec3.ONE),
					uvCenter
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


	private boolean triFullySelected(Triangle triangle) {
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}
	private boolean triFullyEditable(Triangle triangle) {
		return modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2));
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
//		else if (!modelView.isHidden(vertex)) {
//		}
		return 3;
	}
}
