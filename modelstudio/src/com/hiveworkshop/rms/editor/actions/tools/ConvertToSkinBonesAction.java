package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.editor.model.SkinBone;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConvertToSkinBonesAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Geoset geoset;
	private final Map<Matrix, Set<GeosetVertex>> matrixVertexMap;
	private final Map<Matrix, SkinBone[]> matrixSkinBoneMap;

	public ConvertToSkinBonesAction(Geoset geoset, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		matrixVertexMap = collectMatrices();
		matrixSkinBoneMap = createSkinBones();
	}

	private Map<Matrix, Set<GeosetVertex>> collectMatrices() {
		Map<Matrix, Set<GeosetVertex>> matrixVertexMap = new HashMap<>();

		for(GeosetVertex vertex : geoset.getVertices()){
			if(vertex.getSkinBones() == null){
				if(!matrixVertexMap.containsKey(vertex.getMatrix())){
					matrixVertexMap.put(new Matrix(vertex.getBones()), new HashSet<>());
				}
				matrixVertexMap.get(vertex.getMatrix()).add(vertex);
			}

		}
		return matrixVertexMap;
	}

	private Map<Matrix, SkinBone[]> createSkinBones() {
		Map<Matrix, SkinBone[]> matrixSkinBoneMap = new HashMap<>();
		for(Matrix matrix : matrixVertexMap.keySet()){
			matrixSkinBoneMap.put(matrix, matrixToSkinBones(matrix));
		}
		return matrixSkinBoneMap;
	}

	@Override
	public UndoAction undo() {
		for(Matrix matrix : matrixVertexMap.keySet()){
			for(GeosetVertex vertex : matrixVertexMap.get(matrix)){
				vertex.removeSkinBones();
				vertex.setBones(matrix.getBones());
			}
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for(GeosetVertex vertex : geoset.getVertices()){
			vertex.initSkinBones();
			SkinBone[] skinBones = matrixSkinBoneMap.get(vertex.getMatrix());
			vertex.clearBoneAttachments();
			for(int i = 0; i < skinBones.length; i++){
				vertex.setSkinBone(skinBones[i].copy(), i);
			}
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}



	public SkinBone[] matrixToSkinBones(Matrix matrix) {
		int bonesNum = Math.min(4, matrix.size());
		short weight = 255;
		if (bonesNum > 0) {
			weight = (short) (255 / bonesNum);
		}

		SkinBone[] skinBones = new SkinBone[4];
		for (int i = 0; i < 4; i++) {
			if (i < bonesNum) {
				skinBones[i] = new SkinBone(weight, matrix.get(i));
			} else {
				skinBones[i] = new SkinBone((short) 0);
			}
		}
		if(!matrix.isEmpty()){
			skinBones[0].setWeight((short) (weight + (255 % bonesNum)));
		} else {
			skinBones[0].setWeight((short) 255);
		}

		return skinBones;
	}

	@Override
	public String actionName() {
		return "Convert Geoset Matrices to SkinBones";
	}
}
