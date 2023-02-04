package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.SkinBone;

import java.util.*;

public class MergeSkinWeightsAction implements UndoAction {
	Map<Integer, List<GeosetVertex>> skinboneVertMap = new HashMap<>();
	Map<Integer, SkinBone[]> skinboneMap = new HashMap<>();
	Map<Integer, SkinBone[]> newSkinboneMap = new HashMap<>();



	public MergeSkinWeightsAction(Collection<GeosetVertex> vertices, Bone boneForZero, boolean replaceZero) {
		int numSkinBones = 0;
		int affectedVerts = 0;
		for(GeosetVertex vertex : vertices){
			SkinBone[] skinBones = vertex.getSkinBones();
			boolean skinProcessed = false;
			if (skinBones != null){
				for(int i = 0; i < skinBones.length-1 && !skinProcessed; i++){
					SkinBone skinBoneToCheck = skinBones[i];
					if(skinBoneToCheck != null && skinBoneToCheck.getBone() != null){
						for(int j = i+1; j < skinBones.length; j++){
							if (skinBones[j] != null && skinBoneToCheck.getBone() == skinBones[j].getBone()
									|| (replaceZero && skinBoneToCheck.getWeight() == 0 && skinBoneToCheck.getBone() != boneForZero
									|| replaceZero && skinBones[j].getWeight() == 0 && skinBones[j].getBone() != boneForZero)){
								int hashCode = Arrays.hashCode(skinBones);
								if(skinboneMap.get(hashCode) == null){
									skinboneMap.putIfAbsent(hashCode, getSkinBonesCopy(skinBones));
									SkinBone[] skinBonesCopy = getSkinBonesCopy(skinBones);
									mergeWeights(skinBonesCopy, boneForZero, replaceZero);
									sortBones(skinBonesCopy);
									newSkinboneMap.put(hashCode, skinBonesCopy);
									numSkinBones++;
								}
								skinboneVertMap.computeIfAbsent(hashCode, k -> new ArrayList<>()).add(vertex);
								affectedVerts++;
								skinProcessed = true;
							}
						}
					}
				}
			}
		}

//		System.out.println("num skinBones: " + numSkinBones + ", affVerts: " + affectedVerts);

	}

	private void mergeWeights(SkinBone[] skinBones, Bone boneForZero, boolean replaceZero) {
		for(int boneIndex = 0; boneIndex<skinBones.length; boneIndex++){
			SkinBone skinBoneToCheck = skinBones[boneIndex];
			Bone boneToCheck = skinBoneToCheck.getBone();
			for(int i = boneIndex+1; i < skinBones.length; i++){
				if(skinBones[i].getBone() == boneToCheck){
					skinBoneToCheck.setWeight((short) (skinBoneToCheck.getWeight() + skinBones[i].getWeight()));
					skinBones[i].setWeight((short) 0);
					if(replaceZero){
						skinBones[i].setBone(boneForZero);
					} else {
						skinBones[i].setBone(null);
					}
				}
			}
			if(skinBoneToCheck.getWeight() == 0 && replaceZero && skinBoneToCheck.getBone() != boneForZero){
				skinBoneToCheck.setBone(boneForZero);
			}
		}
	}

	private void sortBones(SkinBone[] skinBones) {
		for(int i = 0; i< skinBones.length; i++){
			for(int j = i+1; j< skinBones.length; j++){
				if(skinBones[i].getWeight() < skinBones[j].getWeight()){
					short tempWeight = skinBones[i].getWeight();
					Bone tempBone = skinBones[i].getBone();

					skinBones[i].setWeight(skinBones[j].getWeight());
					skinBones[i].setBone(skinBones[j].getBone());

					skinBones[j].setWeight(tempWeight);
					skinBones[j].setBone(tempBone);
				}
			}
		}
	}

	private SkinBone[] getSkinBonesCopy(SkinBone[] skinBones) {
		SkinBone[] skinBonesCopy = new SkinBone[4];
		for(int i = 0; i< skinBones.length; i++){
			if(skinBones[i] != null){
				skinBonesCopy[i] = skinBones[i].copy();
			} else {
				skinBonesCopy[i] = skinBones[i];
			}
		}
		return skinBonesCopy;
	}



	@Override
	public MergeSkinWeightsAction undo() {
		for(Integer hashcode : skinboneMap.keySet()) {
			SkinBone[] skinBones = skinboneMap.get(hashcode);
			Bone[] bones = new Bone[4];
			short[] weights = new short[4];
			for(int i = 0; i< skinBones.length; i++){
				if(skinBones[i] != null){
					bones[i] = skinBones[i].getBone();
					weights[i] = skinBones[i].getWeight();
				}
			}
			for(GeosetVertex vertex : skinboneVertMap.get(hashcode)){
				vertex.setSkinBones(bones, weights);
			}
		}
		return this;
	}

	@Override
	public MergeSkinWeightsAction redo() {
		for(Integer hashcode : newSkinboneMap.keySet()) {
			SkinBone[] skinBones = newSkinboneMap.get(hashcode);
			Bone[] bones = new Bone[4];
			short[] weights = new short[4];
			for(int i = 0; i< skinBones.length; i++){
				if(skinBones[i] != null){
					bones[i] = skinBones[i].getBone();
					weights[i] = skinBones[i].getWeight();
				}
			}
			for(GeosetVertex vertex : skinboneVertMap.get(hashcode)){
				vertex.setSkinBones(bones, weights);
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Merge Weights";
	}
}
