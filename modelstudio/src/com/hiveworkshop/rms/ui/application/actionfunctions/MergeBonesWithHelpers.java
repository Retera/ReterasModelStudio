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
import java.util.stream.Collectors;

public class MergeBonesWithHelpers extends ActionFunction {
	public MergeBonesWithHelpers() {
		super(TextKey.MERGE_BONES_WITH_HELPERS, MergeBonesWithHelpers::mergeActionRes);
	}

	public static void mergeActionRes(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		ModelView modelView = modelHandler.getModelView();

		Set<Bone> bonesToReplace = getBonesToReplace(model.getBones(), modelView);
		List<UndoAction> undoActions = new ArrayList<>();
		List<IdObject> nodesToRemove = new ArrayList<>();
		Map<IdObject, IdObject> nodeToReplacement = new HashMap<>();
		Map<Bone, List<IdObject>> boneToChildren = new HashMap<>();

		for (Bone bone : bonesToReplace) {
			IdObject parent = bone.getParent();
			nodesToRemove.add(parent);
			nodeToReplacement.put(parent, bone);
			System.out.println("moving \"" + bone.getName() + "\" to parent position");
			undoActions.add(new SetPivotAction(bone, new Vec3(parent.getPivotPoint()), null));

			ArrayList<AnimFlag<?>> animFlags = parent.getAnimFlags();
			for (AnimFlag<?> animFlag : animFlags) {
				undoActions.add(new AddAnimFlagAction<>(bone, animFlag.deepCopy(), null));
			}
			if (isBadName(bone.getName()) && !isBadName(parent.getName())) {
				undoActions.add(new NameChangeAction(bone, parent.getName(), null));
			}

			List<IdObject> childList = new ArrayList<>(parent.getChildrenNodes());
			childList.remove(bone);
			boneToChildren.put(bone, childList);
		}
		System.out.println("\nfound " + bonesToReplace.size() + " bones to merge and " + nodesToRemove.size() + " nodes to remove");

		undoActions.add(new RemoveSelectionUggAction(new SelectionBundle(nodesToRemove), modelView, null));
		undoActions.add(new DeleteNodesAction(nodesToRemove, modelView, null));

		for (Bone bone : bonesToReplace) {
			IdObject parent = bone.getParent();
			if (nodesToRemove.contains(parent)) {
				IdObject newParent = parent.getParent();
				while (nodeToReplacement.containsKey(newParent)) {
					newParent = nodeToReplacement.get(newParent);
				}
				String newParentName = newParent == null ? "NULL" : newParent.getName();
				System.out.println("setting parent of \"" + bone.getName() + "\" to \"" + newParentName + "\"");
				undoActions.add(new SetParentAction(bone, newParent, null));
			}
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

	private static TreeSet<Bone> getBonesToReplace(Collection<Bone> bones, ModelView modelView) {
		Set<Bone> bonesWOMotion = bones.stream()
				.filter(b -> b.getAnimFlags().isEmpty() && modelView.isSelected(b) && b.getParent() instanceof Helper)
				.collect(Collectors.toSet());

		EditableModel model = modelView.getModel();
		TreeSet<Bone> bonesToReplace = new TreeSet<>(Comparator.comparingInt(model::getObjectId));
		TreeSet<Bone> temp = new TreeSet<>(Comparator.comparingInt(model::getObjectId));
		for (Bone bone : bonesWOMotion) {
			temp.clear();
			IdObject parent = bone.getParent();
			parent.getChildrenNodes().stream().filter(idObject -> idObject instanceof Bone && bonesWOMotion.contains(idObject)).forEach(b -> temp.add((Bone) b));
			if (temp.size() == 1) {
				bonesToReplace.addAll(temp);
			} else if (temp.stream().filter(b -> b.getChildrenNodes().isEmpty()).count() == 1) {
				temp.stream().filter(b -> b.getChildrenNodes().isEmpty()).forEach(bonesToReplace::add);
			} else if (temp.stream().filter(modelView::isSelected).count() == 1) {
				temp.stream().filter(modelView::isSelected).forEach(bonesToReplace::add);
			}
		}
		return bonesToReplace;
	}

	private static boolean isBadName(String name) {
		String lowCaseName = name.toLowerCase();
		return lowCaseName.startsWith("mesh") || lowCaseName.startsWith("object") || lowCaseName.startsWith("box");
	}
}
