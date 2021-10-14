package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderGeoset {
	private final Geoset geoset;
	private final ModelView modelView;
	private final RenderModel renderModel;
	private final Map<GeosetVertex, RenderVert> renderVertexMap = new HashMap<>();
	private final ArrayList<RenderVert> renderVerts = new ArrayList<>();

//	private Vec3[] vertexBuffer;
//	private Vec3[] triangleBuffer;
//	private Vec3[] normalBuffer;
//	private Vec3[] texCoordsBuffer;

	List<RenderVert> editableNotSelectedVerts = new ArrayList<>();
	List<RenderVert> notEditableVerts = new ArrayList<>();
	List<RenderVert> normalMap = new ArrayList<>();
	List<RenderVert> selectedVerts = new ArrayList<>();
	List<RenderVert> highlightedVertsMap = new ArrayList<>();

	boolean isHD = false;

	Map<Matrix, Mat4> transformMapSD = new HashMap<>();
	Map<SkinBone[], Mat4> transformMapHD = new HashMap<>();

	public RenderGeoset(Geoset geoset, RenderModel renderModel, ModelView modelView) {
		this.geoset = geoset;
		this.renderModel = renderModel;
		this.modelView = modelView;
		rebuildVertexMap();
	}

	private void rebuildVertexMap() {
		checkHD();
//		renderVertexMap.removeIfKeyNotIn(geoset.getVertices());
		renderVerts.clear();
		renderVertexMap.clear();
		for (GeosetVertex vertex : geoset.getVertices()) {
			RenderVert renderVert = new RenderVert(vertex);
			renderVerts.add(renderVert);
//			renderVertexMap.computeIfAbsent(vertex, v -> new RenderVert(vertex));
			renderVertexMap.put(vertex, renderVert);
		}

	}

	public RenderGeoset updateTransforms(boolean forceAnimated){
		transformMapSD.clear();
		transformMapHD.clear();
		if(renderVerts.size() != geoset.getVertices().size()){
			rebuildVertexMap();
		}
		for(RenderVert renderVert : renderVerts){
			renderVert.update(getTransform(renderVert.vertex, forceAnimated));
		}
		return this;
	}
//	public BiMap<GeosetVertex, RenderVert> updateTransforms(){
//		for(RenderVert renderVert : renderVertexMap.valueSet()){
//			renderVert.update(getTransform(renderVert.vertex));
//		}
//		return renderVertexMap;
//	}

//	public BiMap<GeosetVertex, RenderVert> getRenderVertexMap() {
//		if(renderVerts.size() != geoset.getVertices().size()){
//			updateTransforms(false);
//		}
//		return renderVertexMap;
//	}

	public ArrayList<RenderVert> getRenderVerts() {
		return renderVerts;
	}

	public RenderVert getRenderVert(GeosetVertex vertex){
		if(renderVerts.size() != geoset.getVertices().size()){
			updateTransforms(false);
		}
		return renderVertexMap.get(vertex);
	}

	private Mat4 getTransform(GeosetVertex vertex, boolean forceAnimated) {
		if (renderModel.getTimeEnvironment().isLive() || forceAnimated) {
			if (isHD) {
				return processHdBones(renderModel, vertex.getSkinBones());
			} else {
				return processSdBones(renderModel, vertex.getMatrix().getBones());
			}
		}
		return null;
	}



	private void checkHD() {
		isHD = ModelUtils.isTangentAndSkinSupported(geoset.getParentModel())
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

	public static class RenderVert {
		GeosetVertex vertex;
		Vec3 renderPos = new Vec3();
		Vec3 renderNorm = new Vec3(0, 0, 1);
		Vec2 tVert = new Vec2(0, 0);

		public RenderVert(GeosetVertex vertex) {
			this.vertex = vertex;
			renderPos.set(vertex);
			if (vertex.getNormal() != null) {
				renderNorm.set(vertex.getNormal());
			}
			if (vertex.getTverts() != null && vertex.getTverts().get(0) != null) {
				tVert.set(vertex.getTVertex(0));
			}
		}

		public RenderVert update(Mat4 mat4) {
			renderPos.set(vertex);
			if (vertex.getNormal() != null) {
				renderNorm.set(vertex.getNormal());
			}
			if (mat4 != null) {
				renderPos.transform(mat4);
				if (vertex.getNormal() != null) {
//					mat4.printMatrix();
					renderNorm.transform(0, mat4).normalize();
				}
			}
			if (vertex.getTverts() != null && vertex.getTverts().get(0) != null) {
				tVert.set(vertex.getTVertex(0));
			}
			return this;
		}

		public GeosetVertex getVertex() {
			return vertex;
		}

		public Vec3 getRenderPos() {
			return renderPos;
		}

		public Vec3 getRenderNorm() {
			return renderNorm;
		}

		public Vec2 getTVert() {
			return tVert;
		}
	}


	Mat4 matrixSumHeap = new Mat4();
	public Mat4 processHdBones(RenderModel renderModel, SkinBone[] skinBones) {
		boolean foundValidBones = false;
//		Mat4 skinBonesMatrixSumHeap = new Mat4().setZero();
		matrixSumHeap.setZero();

		for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
			Bone bone = skinBones[boneIndex].getBone();
			if (bone == null) {
				continue;
			}
			foundValidBones = true;
			Mat4 worldMatrix = renderModel.getRenderNode(bone).getWorldMatrix();

//			skinBonesMatrixSumHeap.addScaled(worldMatrix,skinBones[boneIndex].getWeightFraction());
			matrixSumHeap.addScaled(worldMatrix,skinBones[boneIndex].getWeightFraction());
		}
		if (!foundValidBones) {
//			skinBonesMatrixSumHeap.setIdentity();
			matrixSumHeap.setIdentity();
		}
//		return skinBonesMatrixSumHeap;
		return matrixSumHeap;
	}

	public Mat4 processSdBones(RenderModel renderModel, List<Bone> bones) {
//		Mat4 bonesMatrixSumHeap = new Mat4().setZero();
		matrixSumHeap.setZero();
		if (bones.size() > 0) {
			for (Bone bone : bones) {
				matrixSumHeap.add(renderModel.getRenderNode(bone).getWorldMatrix());
//				bonesMatrixSumHeap.add(renderModel.getRenderNode(bone).getWorldMatrix());
			}
			return matrixSumHeap.uniformScale(1f / bones.size());
//			return bonesMatrixSumHeap.uniformScale(1f / bones.size());
		}
		return matrixSumHeap.setIdentity();
//		return bonesMatrixSumHeap.setIdentity();
	}
}
