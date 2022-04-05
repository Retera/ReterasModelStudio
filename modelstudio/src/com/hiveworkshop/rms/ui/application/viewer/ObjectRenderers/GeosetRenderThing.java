package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

public class GeosetRenderThing {
	public static final float NORMAL_RENDER_LENGTH = 0.025f;
	private int levelOfDetail = 0;
	private SimpleDiffuseShaderPipeline pipeline;
	private RenderModel renderModel;
	private ModelHandler modelHandler;
	private TextureThing textureThing;
	private PortraitCameraManager cameraManager;

	private final Vec4 vertexHeap = new Vec4();
	private final Vec4 colorHeap = new Vec4();
	private final Vec4 normalHeap = new Vec4();
	private final Vec4 normalSumHeap = new Vec4();
	private final Mat4 skinBonesMatrixHeap = new Mat4();
	private final Vec3 screenDimension = new Vec3();
	private final Mat4 screenDimensionMat3Heap = new Mat4();

	public GeosetRenderThing(PortraitCameraManager cameraManager){
		this.cameraManager = cameraManager;
	}

	public GeosetRenderThing setModel(RenderModel renderModel, ModelHandler modelHandler, TextureThing textureThing){
		this.renderModel = renderModel;
		this.modelHandler = modelHandler;
		this.textureThing = textureThing;
		return this;
	}

	public GeosetRenderThing setLod(int lod){
		levelOfDetail = lod;
		return this;
	}

	public void render(SimpleDiffuseShaderPipeline pipeline, int formatVersion) {
		if(renderModel != null){
			for (Geoset geo : modelHandler.getModel().getGeosets()) {
				if(correctLoD(formatVersion, geo)){
					colorHeap.set(renderModel.getRenderGeoset(geo).getRenderColor());
					if(colorHeap.w > RenderModel.MAGIC_RENDER_SHOW_CONSTANT){
						render(pipeline, geo, formatVersion);
					}
				}
			}
		}
	}

	private void render(SimpleDiffuseShaderPipeline pipeline, Geoset geo, int formatVersion) {
		Material material = geo.getMaterial();
		for (int i = 0; i < material.getLayers().size(); i++) {
			Layer layer = material.getLayers().get(i);
			boolean hdTextureOnlyLayer = false;
			boolean hdNoMetaDataLayer = false;
			if (ModelUtils.isShaderStringSupported(formatVersion) && material.getShaderString() != null && material.getShaderString().length() > 0) {
				pipeline.glActiveHDTexture(i);
				hdTextureOnlyLayer = i != (material.getLayers().size() - 1);
				hdNoMetaDataLayer = i != 0;
			}

			if (!hdNoMetaDataLayer) {
				setRenderColor(layer);
				pipeline.glColor4f(colorHeap);
				if (hdTextureOnlyLayer) {
					// (this branch assures it's HD, if you hate this code paradigm change it to "isHD()" for the check)
					Vec3 fresnelColor = layer.getInterpolatedVector(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ONE);
					if (fresnelColor != null) {
						pipeline.glFresnelColor3f(fresnelColor);
					}
					else {
//						pipeline.glFresnelColor3f(0f, 0f, 0f);
						pipeline.glFresnelColor3f(1f, 1f, 1f);
					}
					pipeline.glFresnelTeamColor1f(layer.getInterpolatedFloat(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, 0));
					pipeline.glFresnelOpacity1f(layer.getInterpolatedFloat(renderModel.getTimeEnvironment(), MdlUtils.TOKEN_FRESNEL_OPACITY, 1.0f));
				}
			}

			textureThing.bindLayerTexture(pipeline, renderModel, modelHandler.getModel(), formatVersion, material, layer, hdNoMetaDataLayer);
			if (hdTextureOnlyLayer) {
				continue;
			}
			pipeline.glBegin(GL11.GL_TRIANGLES);
			drawGeo(pipeline, geo, layer);
			pipeline.glEnd();
		}

	}

	private void drawGeo(SimpleDiffuseShaderPipeline pipeline, Geoset geo, Layer layer) {
		for (Triangle tri : geo.getTriangles()) {
			for (GeosetVertex v : tri.getVerts()) {
				setUpVertStuff(v);

				pipeline.glNormal3f(normalSumHeap);

				if (v.getTangent() != null) {
					normalHeap.set(v.getTangent());
					normalHeap.w = 0;
					normalSumHeap.set(normalHeap).transform(skinBonesMatrixHeap);

					normalSumHeap.w = v.getTangent().w;

					pipeline.glTangent4f(normalSumHeap);
				}

				int coordId = layer.getCoordId();
				if (coordId >= v.getTverts().size()) {
					coordId = v.getTverts().size() - 1;
				}
				pipeline.glTexCoord2f(v.getTverts().get(coordId));
				pipeline.glVertex3f(vertexHeap);
			}
		}
	}

	public void drawNormals(SimpleDiffuseShaderPipeline pipeline, int formatVersion) {
		if(renderModel != null){
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);

			pipeline.glBegin(GL11.GL_LINES);
			pipeline.glColor3f(1f, 1f, 3f);
			// if( wireframe.isSelected() )
			for (final Geoset geo : modelHandler.getModel().getGeosets()) {
				if (correctLoD(formatVersion, geo)) {
					drawNormals(pipeline, geo);
				}
			}
			pipeline.glEnd();
		}
	}


	private void setUpVertStuff(GeosetVertex v) {
		setUpBoneMatrix(v);

		if(v.getNormal() == null){
			normalHeap.set(Vec3.Z_AXIS, 0);
		} else {
			normalHeap.set(v.getNormal(), 0);
		}

		vertexHeap.set(v, 1).transform(skinBonesMatrixHeap);
		normalSumHeap.set(normalHeap).transform(skinBonesMatrixHeap);

		if (normalSumHeap.length() > 0 && normalSumHeap.isValid()) {
			normalSumHeap.normalize();
		} else {
			normalSumHeap.set(0, 0, 1, 0);
		}
	}


	private void drawNormals(SimpleDiffuseShaderPipeline pipeline, Geoset geo) {
		for (Triangle tri : geo.getTriangles()) {
			for (GeosetVertex v : tri.getVerts()) {
				setUpVertStuff(v);

				pipeline.glNormal3f(normalSumHeap);
				pipeline.glVertex3f(vertexHeap);

				float normalLength = NORMAL_RENDER_LENGTH * cameraManager.distance;
				vertexHeap.addScaled(normalSumHeap, normalLength);
				pipeline.glNormal3f(normalSumHeap);
				pipeline.glVertex3f(vertexHeap);
			}
		}
	}


	private void setUpBoneMatrix(GeosetVertex v) {
		skinBonesMatrixHeap.setZero();
		SkinBone[] skinBones = v.getSkinBones();
		boolean processedBones = false;
		if(skinBones == null){
			for (Bone bone : v.getBones()) {
				processedBones = true;
				Mat4 worldMatrix = renderModel.getRenderNode(bone).getWorldMatrix();
				skinBonesMatrixHeap.add(worldMatrix);
			}
			skinBonesMatrixHeap.scale(1.0f/v.getBones().size());
		} else {
			for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
				SkinBone skinBone = skinBones[boneIndex];
				if (skinBone != null) {
					processedBones = true;
					Mat4 worldMatrix = renderModel.getRenderNode(skinBone.getBone()).getWorldMatrix();
					skinBonesMatrixHeap.addScaled(worldMatrix, skinBone.getWeightFraction());
				}
			}
		}
		if (!processedBones) {
			skinBonesMatrixHeap.setIdentity();
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
}
