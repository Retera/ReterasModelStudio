package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.SetGeosetAnimAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportFromUnit;
import com.hiveworkshop.rms.ui.application.actionfunctions.OpenFromInternal;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpliceMeshByRetera {
	JMenu skinSplice;

	public SpliceMeshByRetera(){
		JMenuItem skinSpliceFromFile = new JMenuItem("From File");
		skinSpliceFromFile.addActionListener(e -> spliceModel(new FileDialog().chooseModelFile(FileDialog.OPEN_WC_MODEL)));

		JMenuItem skinSpliceFromWorkspace = new JMenuItem("From Workspace");
		skinSpliceFromWorkspace.addActionListener(e -> spliceModel(getWorkspaceModel()));

		JMenuItem skinSpliceFromModel = new JMenuItem("From Model");
		skinSpliceFromModel.addActionListener(e -> spliceModel(OpenFromInternal.getInternalModel()));

		JMenuItem skinSpliceFromUnit = new JMenuItem("From Unit");
		skinSpliceFromUnit.addActionListener(e -> spliceModel(ImportFromUnit.getFileModel()));

		skinSplice = new JMenu("Skin Splice Mesh into Current");
		skinSplice.add(skinSpliceFromFile);
		skinSplice.add(skinSpliceFromWorkspace);
		skinSplice.add(skinSpliceFromModel);
		skinSplice.add(skinSpliceFromUnit);
	}

	public JMenu getMenu(){
		return skinSplice;
	}

	private EditableModel getWorkspaceModel() {
		List<EditableModel> optionNames = ProgramGlobals.getModelPanels().stream()
				.map(ModelPanel::getModel)
				.collect(Collectors.toList());

		EditableModel choice = (EditableModel) JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Choose a workspace item to import data from:", "Import from Workspace",
				JOptionPane.PLAIN_MESSAGE, null, optionNames.toArray(), optionNames.get(0));
		if (choice != null) {
			return TempStuffFromEditableModel.deepClone(choice, choice.getHeaderName());
		}
		return null;
	}

	private void spliceModel(EditableModel model) {
		if (model != null) {
			doSkinSpliceUI(model);
		}
	}

	protected void doSkinSpliceUI(EditableModel meshModel) {
		ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
		EditableModel animationModel = modelHandler.getModel();
		final Map<String, Bone> nameToNode = new HashMap<>();
		for (Bone bone : animationModel.getBones()) {
			nameToNode.put(bone.getName(), bone);
		}

		List<UndoAction> undoActions = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		for (final Geoset geo : meshModel.getGeosets()) {
			for (final GeosetVertex gv : geo.getVertices()) {
				if(gv.getSkinBones() != null){
					replaceHDBones(animationModel, nameToNode, warnings, gv);
				} else {
					replaceSDBones(animationModel, nameToNode, warnings, gv);
				}
			}
			undoActions.add(new AddGeosetAction(geo, animationModel, null));
			GeosetAnim geosetAnim = animationModel.getGeosetAnim(0);
			if(geosetAnim != null){
				undoActions.add(new SetGeosetAnimAction(animationModel, geo, geosetAnim.deepCopy(),  null));
			}
		}
		modelHandler.getUndoManager().pushAction(new CompoundAction("Splice Mesh", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());

	}

	private void replaceSDBones(EditableModel animationModel, Map<String, Bone> nameToNode, List<String> warnings, GeosetVertex gv) {
		List<Bone> bones = gv.getMatrix().getBones();

		for (int i = 0; i < bones.size(); i++) {
			IdObject bone = bones.get(i);
			if (bone != null) {
				Bone replacement = getReplacementBone(animationModel, nameToNode, warnings, bone);
				gv.setBone(i, replacement);

			}
		}
	}

	private void replaceHDBones(EditableModel animationModel, Map<String, Bone> nameToNode, List<String> warnings, GeosetVertex gv) {
		for (int i = 0; i < gv.getSkinBones().length; i++) {
			IdObject bone = gv.getSkinBones()[i].getBone();
			if (bone != null) {
				Bone replacement = getReplacementBone(animationModel, nameToNode, warnings, bone);
				gv.getSkinBones()[i].setBone(replacement);

			}
		}
	}

	private Bone getReplacementBone(EditableModel animationModel, Map<String, Bone> nameToNode, List<String> warnings, IdObject bone) {
		Pair<Integer, Bone> upDeptRep = getFirstMatchingParent(nameToNode, bone);
		if (upDeptRep.getSecond() == null) {
			warnings.add("Failed to replace: " + bone.getName());
			return animationModel.getBones().get(0);
		} else {
			Pair<Integer, Bone> downDeptRep = getChildBoneAtClosestDepth(upDeptRep.getFirst(), upDeptRep.getSecond());
			return downDeptRep.getSecond();
		}
	}

	private Pair<Integer, Bone> getChildBoneAtClosestDepth(int upwardDepth, Bone replacement) {
		List<IdObject> childrenNodes = replacement.getChildrenNodes();
		while (upwardDepth > 0
				&& childrenNodes.size() == 1
				&& childrenNodes.get(0) instanceof Bone) {
			replacement = (Bone) childrenNodes.get(0);
			upwardDepth--;
			childrenNodes = replacement.getChildrenNodes();
		}
		return new Pair<>(upwardDepth, replacement);
	}


	private Pair<Integer, Bone> getFirstMatchingParent(Map<String, Bone> nameToNode, IdObject bone) {
		int upwardDepth = 0;
		while (bone != null) {
			Bone replacement = nameToNode.get(bone.getName());
			if(replacement != null){
				return new Pair<>(upwardDepth, replacement);
			}
			bone = bone.getParent();
			upwardDepth++;
		}
		return new Pair<>(upwardDepth, null);
	}
}
