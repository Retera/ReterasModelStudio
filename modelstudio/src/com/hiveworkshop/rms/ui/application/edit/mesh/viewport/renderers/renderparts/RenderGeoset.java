package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.renderparts;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.BiMap;
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
	private final BiMap<GeosetVertex, RenderVert> renderVertexMap = new BiMap<>();
	private final BiMap<Integer, GeosetVertex> vertexMap = new BiMap<>();

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
	Map<GeosetVertex.SkinBone[], Mat4> transformMapHD = new HashMap<>();

	public RenderGeoset(Geoset geoset, RenderModel renderModel, ModelView modelView) {
		this.geoset = geoset;
		this.renderModel = renderModel;
		this.modelView = modelView;
		rebuildVertexMap();
	}

	private void rebuildVertexMap() {
		vertexMap.clear();
		checkHD();
//		renderVertexMap.removeIfKeyNotIn(geoset.getVertices());
		renderVertexMap.clear();
		for (GeosetVertex vertex : geoset.getVertices()) {
			renderVertexMap.computeIfAbsent(vertex, v -> new RenderVert(vertex));
//			vertexMap.put(vertexMap.size(), renderVertexMap.get(vertex));
			vertexMap.put(vertexMap.size(), vertex);
		}

	}

	public RenderGeoset updateTransforms(boolean forceAnimated){
		transformMapSD.clear();
		transformMapHD.clear();
		if(renderVertexMap.size() != geoset.getVertices().size()){
			rebuildVertexMap();
		}
		for(RenderVert renderVert : renderVertexMap.valueSet()){
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

	public BiMap<GeosetVertex, RenderVert> getRenderVertexMap() {
		if(renderVertexMap.size() != geoset.getVertices().size()){
			updateTransforms(false);
		}
		return renderVertexMap;
	}

	public RenderVert getRenderVert(GeosetVertex vertex){
		if(renderVertexMap.size() != geoset.getVertices().size()){
			updateTransforms(false);
		}
		return renderVertexMap.get(vertex);
	}

	private Mat4 getTransform(GeosetVertex vertex, boolean forceAnimated) {
		if (renderModel.getAnimatedRenderEnvironment().isLive() || forceAnimated) {
			if (isHD) {
				GeosetVertex.SkinBone[] skinBones = vertex.getSkinBones();
				return transformMapHD.computeIfAbsent(skinBones, k -> ModelUtils.processHdBones(renderModel, skinBones));
			} else {
				Matrix matrix = vertex.getMatrix();
				return transformMapSD.computeIfAbsent(matrix, k -> ModelUtils.processSdBones(renderModel, matrix.getBones()));
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
}
