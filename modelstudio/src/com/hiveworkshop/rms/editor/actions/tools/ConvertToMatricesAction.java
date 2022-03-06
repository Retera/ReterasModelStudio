package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.SkinBone;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class ConvertToMatricesAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Geoset geoset;
	private final Map<SkinBoneWrapper, Set<GeosetVertex>> skinBoneVertexMap;
	private final Map<SkinBoneWrapper, List<Bone>> skinBoneMatrixMap;

	public ConvertToMatricesAction(Geoset geoset, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		skinBoneVertexMap = collectSkinBones();
		skinBoneMatrixMap = createMatrices();
	}

	private Map<SkinBoneWrapper, Set<GeosetVertex>> collectSkinBones() {
		SkinBoneWrapper tempWrapper = new SkinBoneWrapper(null);
		Map<SkinBoneWrapper, Set<GeosetVertex>> matrixVertexMap = new HashMap<>();

		for(GeosetVertex vertex : geoset.getVertices()){
			tempWrapper.skinBones = vertex.getSkinBones();
			if(!matrixVertexMap.containsKey(tempWrapper)){
				matrixVertexMap.put(new SkinBoneWrapper(vertex.getSkinBones()), new HashSet<>());
			}
			matrixVertexMap.get(tempWrapper).add(vertex);

		}
		return matrixVertexMap;
	}


	private Map<SkinBoneWrapper, List<Bone>> createMatrices() {
		Map<SkinBoneWrapper, List<Bone>> matrixSkinBoneMap = new HashMap<>();
		for(SkinBoneWrapper skinBoneWrapper : skinBoneVertexMap.keySet()){
			matrixSkinBoneMap.put(skinBoneWrapper, skinBonesToMatrix(skinBoneWrapper.skinBones));
		}
		return matrixSkinBoneMap;
	}

	@Override
	public UndoAction undo() {
		for(SkinBoneWrapper skinBonesWrapper : skinBoneVertexMap.keySet()){
			for(GeosetVertex vertex : skinBoneVertexMap.get(skinBonesWrapper)){
				vertex.clearBoneAttachments();
				vertex.initSkinBones();
				for(int i = 0; i < skinBonesWrapper.skinBones.length; i++){
					vertex.setSkinBone(skinBonesWrapper.skinBones[i].copy(), i);
				}
			}
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		SkinBoneWrapper tempWrapper = new SkinBoneWrapper(null);
		int i = 0;
		for(GeosetVertex vertex : geoset.getVertices()){
			tempWrapper.skinBones = vertex.removeSkinBones();
			vertex.setBones(skinBoneMatrixMap.get(tempWrapper));
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	public List<Bone> skinBonesToMatrix(SkinBone[] skinBones) {
		List<Bone> matrix = new ArrayList<>();
//		for(SkinBone skinBone : skinBones){
//			if(skinBone != null){
//				matrix.add(skinBone.getBone());
//			}
//		}
		boolean fallback = false;
		for (SkinBone skinBone : skinBones) {
			if (skinBone != null && skinBone.getBone() != null) {
				fallback = true;
				if (skinBone.getWeight() > 110) {
					matrix.add(skinBone.getBone());
				}
			}
		}
		if (matrix.isEmpty() && fallback) {
			for (SkinBone skinBone : skinBones) {
				if (skinBone != null && skinBone.getBone() != null) {
					matrix.add(skinBone.getBone());
				}
			}
		}
		return matrix;
	}

	@Override
	public String actionName() {
		return "Convert Geoset SkinBones to Matrices";
	}

	private static class SkinBoneWrapper {
		SkinBone[] skinBones;
		SkinBoneWrapper(SkinBone[] skinBones){
			this.skinBones = skinBones;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SkinBoneWrapper that = (SkinBoneWrapper) o;
			return Arrays.equals(skinBones, that.skinBones);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(skinBones);
		}
	}
}
