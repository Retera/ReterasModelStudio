package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.tools.ReplaceBonesAction;
import com.hiveworkshop.rms.editor.actions.tools.SetHdSkinAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.IdObjectListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.StringPadder;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class SkinningOptionPanel extends JPanel {
	ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	Map<Integer, List<GeosetVertex>> skinboneVertMap = new HashMap<>();
	Map<Integer, SkinBone[]> skinboneMap = new HashMap<>();
	Map<Integer, Bone[]> boneMap = new HashMap<>();
	Set<Matrix> matrixSet = new HashSet<>();
	Map<Matrix, Set<GeosetVertex>> matrixVertexMap = new HashMap<>();
	ModelHandler modelHandler;
	ModelView modelView;
	SkinningGroupsPanel skinSubPanel;
	Integer lessThanInt = 5;
	Integer adjustInt = 0;
	Integer snapStep = 5;

	public SkinningOptionPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		JTabbedPane tabbedPane = new JTabbedPane();
		collectUniqueSkinnings();

		skinSubPanel = new SkinningGroupsPanel(modelHandler)
				.fillSkinBonePanel(skinboneMap, skinboneVertMap)
				.fillMatrixPanel(matrixVertexMap);

		JPanel buttonPanel = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[][grow]"));
		buttonPanel.add(Button.create("Update From Viewport Selection", e -> remakeSkinSubPanel()), "");
		buttonPanel.add(Button.create("Merge Weights Of Same Bone", e -> mergeSameForAll()), "");
		buttonPanel.add(Button.create("Remove If Less Than", e -> removeIfLessThan(null)), "");
		buttonPanel.add(Button.create("Snap To Step", e -> snapToSteps()), "");

		JScrollPane scrollPane = new JScrollPane(skinSubPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		JPanel skinPanel = new JPanel(new MigLayout("fill", "[grow]", "[][grow]"));
		skinPanel.add(buttonPanel, "wrap");
		skinPanel.add(scrollPane, "growx, growy");
		tabbedPane.add("Skin", skinPanel);


		tabbedPane.add("Bones", getBoneAdjustmentsPanel());
		add(tabbedPane, "growx, growy");
		// Replace/Remove
		// Use group for selected (button) (button+comboBox?)
		// Replace with group... (comboBox)
		// Set/Change Bones and Weights
		// Increase influence %
		// Smooth %

	}

	private void remakeSkinSubPanel() {
		collectUniqueSkinnings();
		updateSkinSubPanel();
	}

	private void updateSkinSubPanel() {
		skinSubPanel
				.clear()
				.fillSkinBonePanel(skinboneMap, skinboneVertMap)
				.fillMatrixPanel(matrixVertexMap)
				.revalidate();
	}

	private void collectUniqueSkinnings() {

		System.out.println(modelView.getSelectedVertices().size() + " verts selected");
		skinboneVertMap.clear();
		skinboneMap.clear();
		boneMap.clear();
		matrixSet.clear();
		matrixVertexMap.clear();

		for (GeosetVertex vertex : modelView.getSelectedVertices()) {
			SkinBone[] skinBones = vertex.getSkinBones();

			if (skinBones != null) {
				int hashCode = Arrays.hashCode(skinBones);
				skinboneMap.putIfAbsent(hashCode, skinBones);
				skinboneVertMap.computeIfAbsent(hashCode, k -> new ArrayList<>()).add(vertex);

				Bone[] skinBoneBones = vertex.getSkinBoneBones();
				int hc = Arrays.hashCode(skinBoneBones);
				boneMap.putIfAbsent(hc, skinBoneBones);

			} else {
				matrixSet.add(vertex.getMatrix());

				Set<GeosetVertex> vertexSet = matrixVertexMap.get(vertex.getMatrix());
				if (vertexSet == null) {
					vertexSet = new HashSet<>();
					matrixVertexMap.put(new Matrix(vertex.getBones()), vertexSet);
				}
				vertexSet.add(vertex);
			}
		}

		System.out.println(skinboneMap.size() + " unique skinnings found using " + boneMap.size() + " unique bone combinations, " + matrixSet.size() + " matrices found");
	}


	public JPanel getBoneAdjustmentsPanel() {
		JPanel panel = new JPanel(new MigLayout("fill", "[grow][grow]"));
		JPanel listPanel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][grow]"));
		SearchableList<Bone> searchableList = getSearchableList(modelHandler.getModel().getBones());
		listPanel.add(searchableList.getSearchField(), "growx, wrap");
		listPanel.add(searchableList.getScrollableList(), "growx, growy");
		panel.add(listPanel, "growx, growy");

		JPanel boneThingPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		searchableList.addMultiSelectionListener((bones) -> {
			System.out.println("selected bones: " + bones);
			boneThingPanel.removeAll();
			boneThingPanel.add(getAdjustBonePanel(bones), "left, growx, growy");
			panel.revalidate();
		});

		panel.add(boneThingPanel, "growx, growy");
		return panel;
	}

	private SearchableList<Bone> getSearchableList(Collection<Bone> bones) {
		SearchableList<Bone> searchableList = new SearchableList<>(this::idObjectNameFiler);
		IdObjectListCellRenderer renderer = new IdObjectListCellRenderer(modelHandler.getModel(), null).setShowClass(false);
		searchableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		searchableList.setCellRenderer(renderer);

		searchableList.add(0, null);
		for (Bone bone : bones) {
			searchableList.add(bone);
		}

		return searchableList;
	}

	private boolean idObjectNameFiler(IdObject objectShell, String filterText) {
		return (objectShell != null ? objectShell.getName() : "none").toLowerCase().contains(filterText.toLowerCase());
	}

	public JPanel getAdjustBonePanel(Collection<Bone> bones) {
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel buttonPanel = new JPanel(new MigLayout("fill, ins 0"));

		buttonPanel.add(Button.create("Remove from selected", e -> replaceBones(bones, false)), "wrap");
		buttonPanel.add(Button.create("Replace for selected", e -> replaceBones(bones, true)), "wrap");
		buttonPanel.add(Button.create("Remove If Weight Less Than", e -> removeIfLessThan(bones)), "wrap");
		if (!skinboneMap.isEmpty()) {
			buttonPanel.add(Button.create("Adjust influence for selected", e -> adjustInfluence(bones)), "wrap");
		}

		String name = getString(bones);
		System.out.println("new panel for: " + name);

		JTextArea infoLabel = getInfoLabel(name);
		infoLabel.setBorder(BorderFactory.createTitledBorder("Selected"));
		panel.add(infoLabel, "bottom, growx, spanx, wrap");

		panel.add(buttonPanel, "top, wrap");
		return panel;
	}

	private JTextArea getInfoLabel(String text) {
		JTextArea infoLabel = new JTextArea(text);
		infoLabel.setEditable(false);
		infoLabel.setOpaque(false);
		infoLabel.setLineWrap(true);
		infoLabel.setWrapStyleWord(true);
		return infoLabel;
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

	private void adjustInfluence(Collection<Bone> bones) {
		SmartNumberSlider slider = new SmartNumberSlider("%", adjustInt , -100, 500, i -> adjustInt = i, false, false);
		int adjust_influence = JOptionPane.showConfirmDialog(this, slider, "Adjust influence", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (adjust_influence == JOptionPane.OK_OPTION && slider.getValue() != 0) {
			adjustInt = slider.getValue();
			float adjust = (100 + slider.getValue()) / 100f;
			adjustInfluenc(adjust, bones);
		}

	}

	public void adjustInfluenc(float adjust, Collection<Bone> selectedBones) {
		List<UndoAction> undoActions = new ArrayList<>();
		Set<Bone> bonesToAdj = new HashSet<>(selectedBones);
		for (Integer key : skinboneMap.keySet()) {
			SkinBone[] skinBones = skinboneMap.get(key);
			boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(sb -> bonesToAdj.contains(sb.getBone()));
			if (shouldAdjust) {
				Bone[] bones = new Bone[skinBones.length];
				short[] weights = new short[skinBones.length];

				for (int i = 0; i < skinBones.length; i++) {
					bones[i] = skinBones[i].getBone();
					weights[i] = skinBones[i].getWeight();
				}

				int[] newWeights = new int[weights.length];
				for (int i = 0; i < weights.length; i++) {
					if (bonesToAdj.contains(bones[i])) {
						newWeights[i] = (int) (MathUtils.clamp(weights[i] * adjust, 0, 10000) + .499f);
					} else {
						newWeights[i] = bones[i] != null ? weights[i] : 0;
					}
				}


				float totNewWeight = Arrays.stream(newWeights).sum();
				for (int i = 0; i < newWeights.length; i++) {
					float weightFraction = newWeights[i] / totNewWeight;
					weights[i] = (short) MathUtils.clamp((int) (255 * weightFraction + .499), 0, 255);
				}

				fixWeights(bones, weights);

				List<GeosetVertex> vertices = skinboneVertMap.get(key);
				undoActions.add(new SetHdSkinAction(vertices, bones, weights));

			}
		}
		modelHandler.getUndoManager().pushAction(new CompoundAction("Adjust skin wiegths", undoActions, changeListener::nodesUpdated).redo());

		updateSkinSubPanel();

	}

	public void replaceBones(Collection<Bone> bones, boolean showChooser) {
		System.out.println("replace for selected!");
		Map<IdObject, IdObject> replaceMap = new HashMap<>();
		IdObject idObject = null;
		IdObject orgBone = bones.size() == 1 ? bones.stream().findFirst().orElse(null) : null;
		if (showChooser) {
			IdObjectChooser idObjectChooser = new IdObjectChooser(modelHandler.getModel(), false).setClasses(Bone.class);
			idObject = idObjectChooser.chooseObject(orgBone, this);
		}
		if (!showChooser || idObject != orgBone) {
			for (Bone bone : bones) {
				if (bone != idObject) {
					replaceMap.put(bone, idObject);
				}
			}
		}
		if (!replaceMap.isEmpty()) {
			replaceBones(replaceMap);
		}
	}

	public void replaceBones(Map<IdObject, IdObject> bonesToReplace) {
		Map<IdObject, IdObject> boneReplacements = new HashMap<>();
		Set<GeosetVertex> affectedVerts = new HashSet<>();

		for (Integer key : skinboneMap.keySet()) {
			SkinBone[] skinBones = skinboneMap.get(key);
			boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(sb -> bonesToReplace.containsKey(sb.getBone()));
			if (shouldAdjust) {
				affectedVerts.addAll(skinboneVertMap.get(key));
				for (SkinBone skinBone : skinBones) {
					Bone bone = skinBone.getBone();

					IdObject orDefault = bonesToReplace.getOrDefault(bone, bone);
					String defName = orDefault == null ? "null" : ("\"" + orDefault.getName() + "\"");
					System.out.println("replacing \"" + bone.getName() + "\" with " + defName);
					boneReplacements.put(bone, orDefault);
				}
			}
		}

		for (Matrix matrix : matrixVertexMap.keySet()) {
			boolean shouldAdjust = matrix.getBones().stream().anyMatch(bonesToReplace::containsKey);
			if (shouldAdjust) {
				affectedVerts.addAll(matrixVertexMap.get(matrix));
				for (Bone bone : matrix.getBones()) {
					boneReplacements.put(bone, bonesToReplace.getOrDefault(bone, bone));
				}
			}
		}

		if (!boneReplacements.isEmpty()) {
			modelHandler.getUndoManager().pushAction(new ReplaceBonesAction(affectedVerts, boneReplacements, changeListener).redo());

			updateSkinSubPanel();
		}
	}

	private void mergeSameForAll() {
		List<UndoAction> undoActions = new ArrayList<>();
		for (Integer index : skinboneMap.keySet()) {
			SkinBone[] skinBones = skinboneMap.get(index);
			List<GeosetVertex> vertices = skinboneVertMap.get(index);
			Bone[] bones = new Bone[4];
			short[] weights = new short[4];

			for (int i = 0; i < 4; i++) {
				bones[i] = skinBones[i].getBone();
				weights[i] = skinBones[i].getWeight();
			}
			setNoInfluenceToNull(bones, weights);
			mergeIfSameBone(bones, weights);
			sortByWeight(bones, weights);

			if (isChangeMade(skinBones, bones, weights)) {
				undoActions.add(new SetHdSkinAction(vertices, bones, weights));
			}
		}

		if (!undoActions.isEmpty()) {
			modelHandler.getUndoManager().pushAction(new CompoundAction("Merge Weights of Same Bone", undoActions).redo());

			updateSkinSubPanel();
		}
	}


	private void removeIfLessThan(Collection<Bone> bones) {
		IntEditorJSpinner intEditorJSpinner = new IntEditorJSpinner(lessThanInt, 0, 255, weight -> lessThanInt = weight);
		int adjust_influence = JOptionPane.showConfirmDialog(this, intEditorJSpinner, "Remove if Less Than", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (adjust_influence == JOptionPane.OK_OPTION && intEditorJSpinner.getIntValue() != 0) {
			lessThanInt = intEditorJSpinner.getIntValue();
			removeIfLessThan(intEditorJSpinner.getIntValue(), bones);
		}
	}

	private void removeIfLessThan(int weightLimit, Collection<Bone> selectedBones) {

		List<UndoAction> undoActions = new ArrayList<>();
		Set<Bone> bonesToAdj = selectedBones == null ? null : new HashSet<>(selectedBones);
		Function<SkinBone, Boolean> shouldAdjFilter = selectedBones == null
				? sb -> sb.getWeight() != 0 && sb.getWeight() < weightLimit
				: sb -> sb.getWeight() != 0 && sb.getWeight() < weightLimit && bonesToAdj.contains(sb.getBone());
		for (Integer key : skinboneMap.keySet()) {
			SetHdSkinAction action = getRemoveIfLessAction(skinboneMap.get(key), skinboneVertMap.get(key), shouldAdjFilter);
			if (action != null) {
				undoActions.add(action);
			}
		}
		if (!undoActions.isEmpty()) {
			modelHandler.getUndoManager().pushAction(new CompoundAction("Adjust skin weights", undoActions, changeListener::nodesUpdated).redo());

			updateSkinSubPanel();
		}
	}

	private SetHdSkinAction getRemoveIfLessAction(SkinBone[] skinBones, List<GeosetVertex> vertices, Function<SkinBone, Boolean> shouldAdjFilter) {
		boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(shouldAdjFilter::apply);
		if (shouldAdjust) {
			Bone[] bones = new Bone[skinBones.length];
			short[] weights = new short[skinBones.length];

			for (int i = 0; i < skinBones.length; i++) {
				bones[i] = skinBones[i].getBone();
				weights[i] = skinBones[i].getWeight();
				if (shouldAdjFilter.apply(skinBones[i])) {
					weights[i] = 0;
				}
			}

			fixWeights(bones, weights);

			return new SetHdSkinAction(vertices, bones, weights);
		}
		return null;
	}

	private void snapToSteps() {
		IntEditorJSpinner intEditorJSpinner = new IntEditorJSpinner(snapStep, 0, 255, weight -> snapStep = weight);
		int adjust_influence = JOptionPane.showConfirmDialog(this, intEditorJSpinner, "Snap to Weight Step", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (adjust_influence == JOptionPane.OK_OPTION && intEditorJSpinner.getIntValue() != 0) {
			snapStep = intEditorJSpinner.getIntValue();
			snapToSteps(intEditorJSpinner.getIntValue());
		}

	}
	private void snapToSteps(int step) {
		int halfStep = step / 2;
		List<UndoAction> undoActions = new ArrayList<>();
		for (Integer key : skinboneMap.keySet()) {
			SkinBone[] skinBones = skinboneMap.get(key);
			List<GeosetVertex> vertices = skinboneVertMap.get(key);

			Bone[] bones = new Bone[skinBones.length];
			short[] weights = new short[skinBones.length];

			weights[0] -= 1;  // This makes the weight distribute better for some reason
			int weightDiff = 255;
			for (int i = 0; i < skinBones.length; i++) {
				bones[i] = skinBones[i].getBone();
				weights[i] = (short)(((skinBones[i].getWeight() + halfStep) / step) * step);
				weightDiff -= weights[i];
			}

			weights[0] += weightDiff % step;
			weightDiff += weightDiff % step;
			for (int i = 0; i < 4 && step <= weightDiff; i++) {
				weights[i] += step;
				weightDiff -= step;
			}
			for (int i = weights.length-1; 0 <= i && weightDiff <= -step; i--) {
				if (weights[i] != 0) {
					weights[i] -= step;
					weightDiff += step;
				}
			}

			fixWeights(bones, weights);

			undoActions.add(new SetHdSkinAction(vertices, bones, weights));
		}
		if (!undoActions.isEmpty()) {
			modelHandler.getUndoManager().pushAction(new CompoundAction("Snap Weights (step:" + step + ")", undoActions, changeListener::nodesUpdated).redo());

			updateSkinSubPanel();
		}
	}

	private void setNoInfluenceToNull(Bone[] bones, short[] weights) {
		for (int i = 0; i < 4; i++) {
			if (weights[i] == 0 && bones[i] != null) {
				bones[i] = null;
			}
		}
	}

	private void mergeIfSameBone(Bone[] bones, short[] weights) {
		for (int boneIndex = 0; boneIndex < bones.length; boneIndex++) {
			Bone boneToCheck = bones[boneIndex];
			for (int i = boneIndex + 1; i < bones.length; i++) {
				if (bones[i] != null && bones[i] == boneToCheck) {
					weights[boneIndex] += weights[i];
					weights[i] = 0;
					bones[i] = null;
				}
			}
		}
	}

	private void sortByWeight(Bone[] bones, short[] weights) {
		for (int i = 0; i < bones.length; i++) {
			for (int j = i + 1; j < bones.length; j++) {
				if (weights[i] < weights[j]) {
					short tempWeight = weights[i];
					Bone tempBone = bones[i];

					weights[i] = weights[j];
					bones[i] = bones[j];

					weights[j] = tempWeight;
					bones[j] = tempBone;
				}
			}
		}
	}
	private boolean isChangeMade(SkinBone[] skinBones, Bone[] bones, short[] weights) {
		for (int i = 0; i < skinBones.length; i++) {
			if (bones[i] != skinBones[i].getBone() || weights[i] != skinBones[i].getWeight()) {
				return true;
			}
		}
		return false;
	}

	private void fixWeights(Bone[] bones, short[] weights) {
		int totWeight = 0;
		for (short weight : weights) {
			totWeight += weight;
		}

		if (totWeight != 255) {
			int notNullBones = (int) Arrays.stream(bones).filter(Objects::nonNull).count();
			int extraWeight = totWeight - 255;

			for (int i = bones.length - 1; 0 <= i && extraWeight != 0; i--) {
				if (weights[i] != 0 && bones[i] != null) {
					short tempWeight = (short) MathUtils.clamp(weights[i] - extraWeight / (i + 1), 0, 255);
					extraWeight -= weights[i] - tempWeight;
					weights[i] = tempWeight;
				}
			}
			if (extraWeight != 0) {
				for (int i = 0; i < notNullBones && extraWeight != 0; i++) {
					short tempWeight = (short) MathUtils.clamp(weights[i] - extraWeight, 0, 255);
					extraWeight -= weights[i] - tempWeight;
					weights[i] = tempWeight;
				}
			}
		}
	}

	public static void showPanel(JComponent parent, ModelHandler modelHandler) {
		SkinningOptionPanel skinningOptionPanel = new SkinningOptionPanel(modelHandler);
		skinningOptionPanel.setPreferredSize(new Dimension(800, 650));
		skinningOptionPanel.revalidate();
		FramePopup.show(skinningOptionPanel, parent, "Edit Skinning");
	}

	// For experimenting with snapping
	public static void main(String[] args) {
		int step = 4;
		snap(step, 242, 7, 6, 0);
		snap(step, 244, 7, 4, 0);
		snap(step, 148, 63, 44, 0);
		snap(step, 228, 18, 9, 0);
		snap(step, 107, 82, 66, 0);
	}
	private static void snap(float step, int... skinBones) {
		float halfStep = (step+.5f)/2f;
		int[] weights = new int[4];


		StringBuilder sb = new StringBuilder("(step:").append(step).append(") \n");
		sb.append("\t[");
		for (int weight : skinBones) {
			sb.append(StringPadder.padStringStart(weight + "", " ", 3));
		}
		sb.append("]\n");

		skinBones[0] -= 1;
		for (int i = 0; i < 4; i++) {
			weights[i] = (int)(((int)((skinBones[i] + halfStep) / step)) * step);
		}

		int weightDiff = 255;
		for (int weight : weights) {
			weightDiff -= weight;
		}
		float stepDiff = weightDiff % step;

		weights[0] += stepDiff;
		weightDiff += stepDiff;
		for (int i = 0; i < 4 && step<=weightDiff; i++) {
			weights[i] += step;
			weightDiff -= step;
		}
		for (int i = weights.length-1; 0 <= i && weightDiff <= -step; i--) {
			if (weights[i] != 0) {
				weights[i] -= step;
				weightDiff += step;
			}
		}

		fixWeights2(weights);

		sb.append("\t[");
		for (int weight : weights) {
			sb.append(StringPadder.padStringStart(weight + "", " ", 3));
		}
		sb.append("]");
		System.out.println(sb);
	}
	private static void fixWeights2(int[] weights) {
		int totWeight = 0;
		for (int weight : weights) {
			totWeight += weight;
		}

		if (totWeight != 255) {
			int extraWeight = totWeight - 255;

			for (int i = 4 - 1; 0 <= i && extraWeight != 0; i--) {
				if (weights[i] != 0) {
					short tempWeight = (short) MathUtils.clamp(weights[i] - extraWeight / (i + 1), 0, 255);
					extraWeight -= weights[i] - tempWeight;
					weights[i] = tempWeight;
				}
			}
			if (extraWeight != 0) {
				for (int i = 0; i < 4 && extraWeight != 0; i++) {
					short tempWeight = (short) MathUtils.clamp(weights[i] - extraWeight, 0, 255);
					extraWeight -= weights[i] - tempWeight;
					weights[i] = tempWeight;
				}
			}
		}
	}
}