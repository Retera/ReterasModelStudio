package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.RenameBoneAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class RenameBoneChainPanel extends JPanel {

	private JButton doRenaming;

	public RenameBoneChainPanel() {
		super(new MigLayout("", "", ""));
		JTextField nameField = new JTextField(24);
		JTextField subfixField = new JTextField(24);
		JCheckBox doTypePrefix = new JCheckBox("Prefix with type");
		add(new JLabel("Name:"));
		add(nameField, "wrap");
		add(new JLabel("Subfix:"));
		add(subfixField, "wrap");
		add(doTypePrefix, "spanx 2, wrap");

		JCheckBox limitDepth = new JCheckBox("Limit depth");
		JSpinner depthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		depthSpinner.setEnabled(false);
		limitDepth.addActionListener(e -> depthSpinner.setEnabled(limitDepth.isSelected()));
		add(limitDepth, "spanx, split");
		add(depthSpinner, "wrap");

		doRenaming = new JButton("Rename");
		doRenaming.addActionListener(e -> doRenaming(nameField.getText(), subfixField.getText(), doTypePrefix.isSelected(), limitDepth.isSelected() ? (Integer) depthSpinner.getValue() : -1));
		add(doRenaming, "spanx");

	}

	public static void show(JComponent parent) {
		RenameBoneChainPanel animCopyPanel = new RenameBoneChainPanel();
		FramePopup.show(animCopyPanel, parent, "Rename Bone Chain");
	}

	private void doRenaming(String name, String subfix, boolean doTypePrefix, int depthLimit) {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
			Set<IdObject> selectedIdObjects = modelHandler.getModelView().getSelectedIdObjects();
			if (selectedIdObjects.size() == 1 && selectedIdObjects.stream().anyMatch(o -> o instanceof Bone)) {
				Bone bone = (Bone) selectedIdObjects.stream().findFirst().get();
				System.out.println("getClass: " + bone.getClass() + ", getSimpleName: " + bone.getClass().getSimpleName());
				Map<Integer, Bone> depthMap = new HashMap<>();
				fillDepthMap2(modelHandler, bone, depthLimit, 0, false, false, 0, depthMap);

				List<UndoAction> actions = new ArrayList<>();
				int countRenamedBones = 0;
				for (int depth : depthMap.keySet()) {
					Bone nodeToRename = depthMap.get(depth);

					String prefix = doTypePrefix ? nodeToRename.getClass().getSimpleName() + "_" : "";

					String indexString = depthMap.size() > 1 ? getIndexString(depth) : "";

					String newName = prefix + name + indexString + subfix;
					actions.add(new RenameBoneAction(newName, nodeToRename));
					countRenamedBones++;
				}

				modelHandler.getUndoManager().pushAction(new CompoundAction("Rename Bone Chain", actions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
				JOptionPane.showMessageDialog(this, "Renamed " + countRenamedBones + " bones/helpers!", "Renamed Bones", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Selection not valid." +
								"\nSelect one bone or helper to use this feature",
						"Invalid selection", JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}

	private String getIndexString(int key) {
		List<Integer> indexes2 = new ArrayList<>();
		for (int tempKey = key; tempKey > 0; ) {
			int subKey = tempKey % 300;
			if (subKey != 0) {
				indexes2.add(subKey);
				tempKey -= subKey;
			} else {
				tempKey /= 300;
			}
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = indexes2.size() - 1; i >= 0; i--) {
			stringBuilder.append("_").append(indexes2.get(i));
		}
		return stringBuilder.toString();

	}


	private void doRenaming(Bone bone, ModelHandler modelHandler, String name, String subfix, boolean doTypePrefix, int depthLimit) {
		Map<Integer, List<Bone>> depthMap = new HashMap<>();
		fillDepthMap(bone, depthLimit, 0, depthMap);

		List<UndoAction> actions = new ArrayList<>();
		int depthStringSize = ("" + depthMap.size()).length();
		int countRenamedBones = 0;
		for (int depth : depthMap.keySet()) {
			List<Bone> bones = depthMap.get(depth);
			int siblingStringSize = ("" + bones.size()).length();
			for (int i = 0; i <= bones.size(); i++) {
				Bone nodeToRename = bones.get(i);
				String prefix = doTypePrefix ? nodeToRename.getClass().getName() + " " : "";
				String depthString = ("0000" + depth).substring(4 - depthStringSize, 4);
				String siblingString = bones.size() > 1 ? ("0000" + i).substring(4 - siblingStringSize, 4) : "";
				String newName = prefix + name + " " + depthString + siblingString + subfix;
				actions.add(new RenameBoneAction(newName, nodeToRename));
				countRenamedBones++;
			}
		}

		modelHandler.getUndoManager().pushAction(new CompoundAction("Rename Bone Chain", actions, ModelStructureChangeListener.changeListener::nodesUpdated));
		JOptionPane.showMessageDialog(this, "Renamed " + countRenamedBones + " bones/helpers!", "Renamed Bones", JOptionPane.INFORMATION_MESSAGE);
	}

	private void fillDepthMap(Bone bone, int depthLimit, int currDepth, Map<Integer, List<Bone>> depthMap) {
		depthMap.computeIfAbsent(currDepth, k -> new ArrayList<>()).add(bone);
		System.out.println("added bone: " + bone.getName() + ", dl: " + depthLimit + ", currD: " + currDepth + ", children: " + bone.getChildrenNodes().size());
		for (IdObject child : bone.getChildrenNodes()) {
			if (child instanceof Bone && depthLimit != 0) {
				fillDepthMap((Bone) child, depthLimit - 1, currDepth + 1, depthMap);
			}
		}

	}

	private void fillDepthMap2(ModelHandler modelHandler, Bone bone, int depthLimit, int siblingIndex, boolean hasSibling, boolean parSibling, int parentKey, Map<Integer, Bone> depthMap) {
		int key;
		if (hasSibling || parSibling) {
			key = (parentKey * 300 + (siblingIndex + 1));
		} else {
			key = parentKey + 1;
		}
		depthMap.put(key, bone);
		List<IdObject> childrenNodes = bone.getChildrenNodes();
		childrenNodes.sort(Comparator.comparingInt(b -> b.getObjectId(modelHandler.getModel())));
		System.out.println("added bone: " + bone.getName() + ", dl: " + depthLimit + ", parentKey: " + parentKey + ", key: " + key + ", siblingIndex: " + siblingIndex + ", children: " + childrenNodes.size() + ", siblings: " + hasSibling);
		for (int i = 0; i < childrenNodes.size(); i++) {
			IdObject child = childrenNodes.get(i);
			if (child instanceof Bone && depthLimit != 0) {
				fillDepthMap2(modelHandler, (Bone) child, depthLimit - 1, i, childrenNodes.size() > 1, hasSibling, key, depthMap);
			}
		}

	}
}
