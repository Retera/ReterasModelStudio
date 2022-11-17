package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.tools.SetHdSkinAction;
import com.hiveworkshop.rms.editor.actions.tools.SetMatrixAction3;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class FixUnboundVerts extends ActionFunction {
	public FixUnboundVerts(){
		super(TextKey.FIX_UNBOUND_VERTS, FixUnboundVerts::fixUnbound);
	}

	public static void fixUnbound(ModelHandler modelHandler){
		ModelView modelView = modelHandler.getModelView();
		List<GeosetVertex> boneLessVerts = new ArrayList<>();
		for(GeosetVertex vertex : modelView.getSelectedVertices()){
			if(!vertex.hasBones()){
				boneLessVerts.add(vertex);
			}
		}

		List<UndoAction> undoActions = new ArrayList<>();
		for(GeosetVertex vertex : boneLessVerts){
			//Map
			Map<Bone, Integer> skinBoneWeightMap = new HashMap<>();
			Map<Bone, Integer> matrixBoneWeightMap = new HashMap<>();
			for(Triangle triangle : vertex.getTriangles()){
				// This will currently count some vertices twice if they share two triangles.
				// I'm not sure if this is undesirable or not
				for(GeosetVertex vert : triangle.getVerts()){
					if(vert.getSkinBones() != null){
						for(SkinBone skinBone : vert.getSkinBones()){
							if (skinBone != null && skinBone.getBone() != null && 0 < skinBone.getWeight()){
								int w = skinBoneWeightMap.getOrDefault(skinBone.getBone(), 0) + skinBone.getWeight();
								skinBoneWeightMap.put(skinBone.getBone(), w);
							}
						}
					} else {
						for (int i = 0; i < vert.getBones().size(); i++){
							Bone bone = vert.getBones().get(i);
							// give bones listed first a slightly higher weight
							int bw = Math.round(255 * (vert.getBones().size()/((float) i+1+vert.getBones().size())));
							int w = matrixBoneWeightMap.getOrDefault(bone, 0) + bw;
							matrixBoneWeightMap.put(bone, w);
						}
					}
				}
			}

			if(!skinBoneWeightMap.isEmpty()){
				List<Pair<Bone, Integer>> weightPairs = new ArrayList<>();
				for (Bone bone : skinBoneWeightMap.keySet()){
					weightPairs.add(new Pair<>(bone, skinBoneWeightMap.get(bone)));
				}
				weightPairs.sort(Collections.reverseOrder(Comparator.comparingInt(Pair::getSecond)));

				Bone[] bones = new Bone[4];
				short[] weights = new short[4];
				int[] weightsI = new int[4];
				for(int i = 0; i < Math.min(weightPairs.size(), 4); i++){
					bones[i] = weightPairs.get(i).getFirst();
					weightsI[i] = weightPairs.get(i).getSecond();
				}
				float adj = Arrays.stream(weightsI).sum()/255.0f;
				for(int i = 0; i < weightsI.length; i++){
					weights[i] = (short) Math.round(weightsI[i]*adj);
				}
				undoActions.add(new SetHdSkinAction(Collections.singleton(vertex), bones, weights));
			} else if (!matrixBoneWeightMap.isEmpty()){
				List<Pair<Bone, Integer>> weightPairs = new ArrayList<>();
				for (Bone bone : matrixBoneWeightMap.keySet()){
					weightPairs.add(new Pair<>(bone, matrixBoneWeightMap.get(bone)));
				}
				weightPairs.sort(Comparator.comparingInt(Pair::getSecond));

				List<Bone> newBones = new ArrayList<>();
				for(int i = 0; i < Math.min(weightPairs.size(), 4); i++){
					newBones.add(weightPairs.get(i).getFirst());
				}
				undoActions.add(new SetMatrixAction3(Collections.singleton(vertex), newBones, Collections.emptySet()));

			} else {
				IdObject root = modelHandler.getModel().getObject("Root");
				if(!(root instanceof Bone)){
					root = modelHandler.getModel().getBones().get(0);
				}
				if (root != null){
					if (900 <= modelHandler.getModel().getFormatVersion()){
						Bone[] bones = new Bone[] {(Bone) root, null, null, null};
						short[] weights = new short[] {255, 0, 0, 0};
						undoActions.add(new SetHdSkinAction(Collections.singleton(vertex), bones, weights));
					} else {
						undoActions.add(new SetMatrixAction3(Collections.singleton(vertex), Collections.singleton((Bone) root), Collections.emptySet()));
					}
				}
			}
		}
		modelHandler.getUndoManager().pushAction(new CompoundAction("Fix loose verts", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
	}

	public static void doRemove(){
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if(modelPanel != null){
			EditableModel model = modelPanel.getModel();
			Set<Bone> usedBones = new HashSet<>();
			model.getGeosets().forEach(geoset -> usedBones.addAll(geoset.getBoneMap().keySet()));

			Set<Bone> modelOtherBones = new HashSet<>();

			for(IdObject idObject : model.getIdObjects()){
				if(idObject instanceof Bone && !usedBones.contains(idObject)){
					modelOtherBones.add((Bone) idObject);
				} else if (!(idObject instanceof Bone)){
					IdObject parent = idObject.getParent();
					if (parent != null) {
						if (parent instanceof Bone) {
							usedBones.add((Bone) parent);
							modelOtherBones.remove(parent);
						}
					}
				}
			}
			Set<IdObject> bonesToRemove = new HashSet<>();
			System.out.println("usedBones: " + usedBones.size());
			for(Bone bone : modelOtherBones){
				if (!childInSet(bone, usedBones)){
					bonesToRemove.add(bone);
				}
			}
			System.out.println("bonesToRemove: " + bonesToRemove.size());

			modelPanel.getUndoManager().pushAction(new DeleteNodesAction(bonesToRemove, ModelStructureChangeListener.changeListener, model).redo());
		}
	}

	private static boolean childInSet(Bone bone, Set<Bone> usedBones){
		for (IdObject idObject : bone.getChildrenNodes()){
			if(!(idObject instanceof Bone)){
				return true;
			} else if(usedBones.contains(idObject)){
				return true;
			} else if(childInSet((Bone)idObject, usedBones)){
				return true;
			}
		}
		return false;
	}
}
