package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.tools.SetHdSkinAction;
import com.hiveworkshop.rms.editor.actions.tools.SetMatrixAction3;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.SkinPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.StringPadder;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class SkinningGroupsPanel extends JPanel {
	ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	ModelHandler modelHandler;
	ModelView modelView;

	public SkinningGroupsPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, gap 0, ins 0"));
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
	}

	public SkinningGroupsPanel clear() {
		removeAll();
		return this;
	}

	public SkinningGroupsPanel fillSkinBonePanel(Map<Integer, SkinBone[]> skinBoneMap, Map<Integer, List<GeosetVertex>> skinBoneVertMap) {
		if (skinBoneMap != null && skinBoneVertMap != null && !skinBoneMap.isEmpty()) {
			System.out.println("adding SkinBonePanel (" + skinBoneMap.size() + " groups)");
			JPanel skinBonePanel = new JPanel(new MigLayout("fill", "[sg1]"));
			for (Integer key : skinBoneMap.keySet()) {
				List<GeosetVertex> vertices = skinBoneVertMap.get(key);
				SkinBone[] skinBones = skinBoneMap.get(key);
				JPanel sbWrapperPanel = getSBSubPanel(vertices, skinBones);
				skinBonePanel.add(sbWrapperPanel, "wrap");
			}
			add(skinBonePanel, "wrap");
		}
		return this;
	}

	public SkinningGroupsPanel fillMatrixPanel(Map<Matrix, Set<GeosetVertex>> matrixVertexMap) {
		if (matrixVertexMap != null && !matrixVertexMap.isEmpty()) {
			System.out.println("adding MatrixPanel (" + matrixVertexMap.size() + " groups)");

			JPanel matrixPanel = new JPanel(new MigLayout("fill"));
			for (Matrix matrix : matrixVertexMap.keySet()) {
				JPanel sbInfoPanel = getMatrixSubPanel(matrix, matrixVertexMap.get(matrix));
				matrixPanel.add(sbInfoPanel, "wrap");
			}
			add(matrixPanel, "wrap");
		}
		return this;
	}

	private JPanel getSBSubPanel(List<GeosetVertex> vertices, SkinBone[] skinBones) {
		JPanel sbWrapperPanel = new JPanel(new MigLayout("gap 0, ins 0", "10[]10[]10", "2[][]2"));
		sbWrapperPanel.setBorder(BorderFactory.createTitledBorder(""));
		JPanel sbInfoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill, wrap 2", "[grow, left]10[right]"));
		JPanel sbButtonsPanel = new JPanel(new MigLayout("gap 0, ins 0, wrap 1"));
		sbWrapperPanel.add(new JLabel(vertices.size() + " vertices:"), "wrap");

		for (String sbS : getHDDescription2(skinBones)) {
			sbInfoPanel.add(new JLabel(sbS));
		}

		sbButtonsPanel.add(Button.create("Edit", e -> changeSkinBoneSetup(vertices, skinBones)));
		sbButtonsPanel.add(Button.setTooltip(Button.create("Use for Selected", e -> useForVertices(modelView.getSelectedVertices(), skinBones)), "Use this skinning for all currently selected vertices"));
		sbButtonsPanel.add(Button.setTooltip(Button.create("Select Vertices (Whole model)", e -> selectVertices(skinBones)), "Finds and selects all editable vertices using this exact skinning"));
		sbButtonsPanel.add(Button.setTooltip(Button.create("Select Vertices (Subset of selection)", e -> selectVerticesSubset(vertices)), "Isolates and selects vertices using this skinning from the original selection"));
		sbWrapperPanel.add(sbInfoPanel);
		sbWrapperPanel.add(sbButtonsPanel);
		return sbWrapperPanel;
	}

	public String[] getHDDescription2(SkinBone[] skinBones) {
		String[] strings = new String[skinBones.length*2];

		for (int i = 0; i < skinBones.length; i++) {
			Bone bone = skinBones[i] == null ? null : skinBones[i].getBone();
			short weight = skinBones[i] == null ? 0 : skinBones[i].getWeight();

			strings[i * 2]     = bone == null ? "null" : bone.getName();
			strings[i * 2 + 1] = StringPadder.padStringStart(weight + "", " ", 3);
		}

		return strings;
	}


	private JPanel getMatrixSubPanel(Matrix matrix, Set<GeosetVertex> vertices) {
		JPanel sbInfoPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		sbInfoPanel.add(new JLabel(getMatricesDescription(matrix, vertices.size())));

		JPanel sbPanel = new JPanel();
		sbPanel.add(sbInfoPanel);
		sbInfoPanel.add(Button.create("Edit", e -> changeMatrixSetup(vertices)));
		sbInfoPanel.add(Button.setTooltip(Button.create("Use for Selected", e -> useForVertices(modelView.getSelectedVertices(), matrix.getBones())), "Use this matrix for all currently selected vertices"));
		return sbInfoPanel;
	}

	public String getMatricesDescription(Matrix matrix, int size) {
		String name = getString(matrix.getBones());

		if (!name.isBlank()) {
			return "Matrix:{"
					+ name
					+ "}: "
					+ size
					+ " vertices";
		}
		return "";
	}

	private String getString(Collection<Bone> bones) {
		List<String> nameList = bones.stream().filter(Objects::nonNull).map(IdObject::getName).toList();
		String boneString = String.join(", ", nameList);
		if (1 < nameList.size()) {
			String last = nameList.get(nameList.size() - 1);
			boneString = boneString.replaceFirst(", " + last + "$", " and " + last);
		}
		return boneString;
	}

	public void changeSkinBoneSetup(Collection<GeosetVertex> vertices, SkinBone[] skinBones) {
		SkinPopup skinPopup = new SkinPopup(modelHandler.getModel(), skinBones);
		String[] words = {"Accept", "Cancel"};
		int i = JOptionPane.showOptionDialog(this, skinPopup, "Edit Skin", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == JOptionPane.OK_OPTION) {
			Bone[] bones = skinPopup.getBones();
			short[] weights = skinPopup.getSkinWeights();
			modelHandler.getUndoManager().pushAction(new SetHdSkinAction(vertices, bones, weights).redo());
		}
	}

	public void changeMatrixSetup(Set<GeosetVertex> vertices) {
		MatrixPopup matrixPopup = new MatrixPopup(modelHandler.getModel(), vertices);
		String[] words = {"Accept", "Cancel"};
		int i = JOptionPane.showOptionDialog(this, matrixPopup, "Edit Matrix", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == JOptionPane.OK_OPTION) {
			SetMatrixAction3 undoAction = new SetMatrixAction3(vertices, matrixPopup.getNewBoneList(), matrixPopup.getBonesNotInAll());
			modelHandler.getUndoManager().pushAction(undoAction.redo());
		}
	}

	public void useForVertices(Collection<GeosetVertex> vertices, SkinBone[] skinBones) {
		Bone[] bones = new Bone[skinBones.length];
		short[] weights = new short[skinBones.length];
		for (int i = 0; i < skinBones.length; i++) {
			bones[i] = skinBones[i].getBone();
			weights[i] = skinBones[i].getWeight();
		}

		Set<GeosetVertex> affectedVerts = vertices.stream().filter(gv -> gv.getSkinBones() != null).collect(Collectors.toSet());

		modelHandler.getUndoManager().pushAction(new SetHdSkinAction(affectedVerts, bones, weights).redo());
	}

	public void useForVertices(Collection<GeosetVertex> vertices, Collection<Bone> bones) {
		modelHandler.getUndoManager().pushAction(new SetMatrixAction3(vertices, bones, Collections.emptySet()).redo());
	}

	public void selectVerticesSubset(Collection<GeosetVertex> vertices) {
		if (!modelView.sameSelection(vertices, Collections.emptySet(), Collections.emptySet())) {
			SelectionBundle bundle = new SelectionBundle(vertices);
			UndoAction action = new SetSelectionUggAction(bundle, modelView, "select skin vertices", changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public void selectVertices(SkinBone[] skinBones) {
		Set<GeosetVertex> vertices = new LinkedHashSet<>();
		int hashCode = Arrays.hashCode(skinBones);
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (modelView.isEditable(vertex)) {
					if (vertex.getSkinBones() != null && hashCode == Arrays.hashCode(vertex.getSkinBones())) {
						vertices.add(vertex);
					}
				}
			}
		}
		if (!vertices.isEmpty() && !modelView.sameSelection(vertices, Collections.emptySet(), Collections.emptySet())) {
			SelectionBundle bundle = new SelectionBundle(vertices);
			UndoAction action = new SetSelectionUggAction(bundle, modelView, "select skin vertices", changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}
}
