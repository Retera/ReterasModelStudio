package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.HashSet;
import java.util.Set;

public class RemoveUnusedBones extends ActionFunction {
	public RemoveUnusedBones(){
		super(TextKey.REMOVE_UNUSED_NODES, RemoveUnusedBones::doRemove);
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
