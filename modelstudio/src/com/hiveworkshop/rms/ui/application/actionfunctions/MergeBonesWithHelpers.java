package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParentAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetPivotAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class MergeBonesWithHelpers extends ActionFunction {
	public MergeBonesWithHelpers() {
		super(TextKey.MERGE_BONES_WITH_HELPERS, MergeBonesWithHelpers::mergeActionRes);
	}

	public static void mergeActionRes(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		ModelView modelView = modelHandler.getModelView();
		Set<Bone> bonesWOMotion = new HashSet<>();
		model.getBones().stream()
				.filter(b -> b.getAnimFlags().isEmpty() && modelView.isSelected(b) && b.getChildrenNodes().isEmpty() && b.getParent() instanceof Helper)
				.forEach(bonesWOMotion::add);

		List<UndoAction> undoActions = new ArrayList<>();
		List<IdObject> nodesToRemove = new ArrayList<>();
		Map<IdObject, IdObject> nodeToReplacement = new HashMap<>();
		Map<Bone, List<IdObject>> boneToChildren = new HashMap<>();

		for (Bone bone : bonesWOMotion) {
			IdObject parent = bone.getParent();
			long count = parent.getChildrenNodes().stream().filter(idObject -> idObject instanceof Bone && bonesWOMotion.contains(idObject)).count();
			if (count == 1) {
				nodesToRemove.add(parent);
				nodeToReplacement.put(parent, bone);
				System.out.println("moving " + bone.getName() + " to parent position");
				undoActions.add(new SetPivotAction(bone, new Vec3(parent.getPivotPoint()), null));


				ArrayList<AnimFlag<?>> animFlags = parent.getAnimFlags();
				for (AnimFlag<?> animFlag : animFlags) {
					undoActions.add(new AddAnimFlagAction<>(bone, animFlag.deepCopy(), null));
				}
				if ((bone.getName().toLowerCase().startsWith("mesh") || bone.getName().toLowerCase().startsWith("object"))
						&& (!parent.getName().toLowerCase().startsWith("mesh") && !parent.getName().toLowerCase().startsWith("object"))) {
					undoActions.add(new NameChangeAction(bone, parent.getName(), null));
				}

				List<IdObject> childList = new ArrayList<>(parent.getChildrenNodes());
				childList.remove(bone);
				boneToChildren.put(bone, childList);
			}
//				bone.setParent(parent.getParent());
		}
		System.out.println("\nfound " + bonesWOMotion.size() + " bones to merge and " + nodesToRemove.size() + " nodes to remove");

		undoActions.add(new RemoveSelectionUggAction(new SelectionBundle(nodesToRemove), modelView, null));
		undoActions.add(new DeleteNodesAction(nodesToRemove, null, model));

		for (Bone bone : bonesWOMotion) {
			IdObject parent = bone.getParent();
			if (nodesToRemove.contains(parent)) {
				IdObject newParent = parent.getParent();
				while (nodeToReplacement.containsKey(newParent)) {
					newParent = nodeToReplacement.get(newParent);
				}
				String newParentName = newParent == null ? "NULL" : newParent.getName();
				System.out.println("setting parent of " + bone.getName() + " to " + newParentName);
				undoActions.add(new SetParentAction(bone, newParent, null));
			}
//				bone.setParent(parent.getParent());
		}
		for (Bone bone : boneToChildren.keySet()) {
			System.out.println(bone.getClass().getSimpleName() + ": " + bone.getName() + ": stealing parents (" + bone.getParent().getClass().getSimpleName() + ": " + bone.getParent().getName() + ") children");
			List<IdObject> childList = boneToChildren.get(bone);
			for (IdObject child : childList) {
				undoActions.add(new SetParentAction(child, bone, null));
			}
		}
		modelHandler.getUndoManager().pushAction(
				new CompoundAction("Merge unnecessary Helpers", undoActions,
						ModelStructureChangeListener.changeListener::nodesUpdated)
						.redo());
	}
}
