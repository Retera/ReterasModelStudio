package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.tools.ReplaceBonesAction;
import com.hiveworkshop.rms.editor.actions.tools.SetHdSkinAction;
import com.hiveworkshop.rms.editor.actions.tools.SetMatrixAction3;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.SkinPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.IdObjectListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.util.SearchableList;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.StringPadder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class SkinningOptionPanel extends JPanel {
	ModelHandler modelHandler;
	ModelView modelView;
	Map<Integer, List<GeosetVertex>> skinboneVertMap = new HashMap<>();
	Map<Integer, SkinBone[]> skinboneMap = new HashMap<>();
	Map<Integer, Bone[]> boneMap = new HashMap<>();
//	Map<Integer, Short[]> weightMap = new HashMap<>();
	Set<Matrix> matrixSet = new HashSet<>();
	Map<Matrix, Set<GeosetVertex>> matrixVertexMap = new HashMap<>();
	ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	public SkinningOptionPanel(ModelHandler modelHandler){
		super(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
//		collectUniqueSkinnings();
		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel skinSubPanel = new JPanel(new MigLayout("fill, gap 0, ins 0"));
		fillSkinSubPanel(skinSubPanel);

		JPanel skinPanel = new JPanel(new MigLayout("fill", "[grow]", "[][grow]"));
		JButton load = new JButton("update from selection");
		load.addActionListener(e -> fillSkinSubPanel(skinSubPanel));
		skinPanel.add(load, "wrap");


		skinPanel.add(skinSubPanel, "growx, growy");
		JScrollPane scrollPane = new JScrollPane(skinPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		tabbedPane.add("Skin", scrollPane);

		tabbedPane.add("Bones", getBoneAdjustmentsPanel());
		add(tabbedPane, "growx, growy");
		// Replace/Remove
		// Use group for selected (button) (button+comboBox?)
		// Replace with group... (comboBox)
		// Set/Change Bones and Weights
		// Increase influence %
		// Smooth %

	}

	private void fillSkinSubPanel(JPanel skinSubPanel) {
		skinSubPanel.removeAll();
		collectUniqueSkinnings();
		if(!skinboneMap.isEmpty()){
			System.out.println("adding SkinBonePanel");
			skinSubPanel.add(getSkinBonePanel(), "wrap");
		}
		if(!matrixVertexMap.isEmpty()){
			System.out.println("adding MatrixPanel");
			skinSubPanel.add(getMatrixPanel(), "wrap");
		}
		skinSubPanel.revalidate();
	}

	public JPanel getSkinBonePanel(){
		JPanel panel = new JPanel(new MigLayout("fill"));
		for(Integer key : skinboneMap.keySet()){
			JPanel sbInfoPanel = new JPanel(new MigLayout("gap 0, ins 0, wrap 1"));
			sbInfoPanel.add(new JLabel(skinboneVertMap.get(key).size() + " vertices:"));
			for (String sbS : getHDDescription(key)){
				sbInfoPanel.add(new JLabel(sbS));
			}
			JPanel sbPanel = new JPanel();
			sbPanel.add(sbInfoPanel);
			JButton edit = new JButton("Edit");
			edit.addActionListener(e -> changeSkinBoneSetup(skinboneVertMap.get(key), skinboneMap.get(key)));
			sbInfoPanel.add(edit);
			JButton useForSelected = new JButton("Use for selected");
			useForSelected.addActionListener(e -> useForVertices(modelView.getSelectedVertices(), skinboneMap.get(key)));
			sbInfoPanel.add(useForSelected);
			panel.add(sbInfoPanel, "wrap");
		}
		return panel;
	}

	public String[] getHDDescription(Integer sbId){
		SkinBone[] skinBones = skinboneMap.get(sbId);
		String[] strings = new String[skinBones.length];

		for (int i = 0; i < skinBones.length; i++) {
			if (skinBones[i] == null) {
				strings[i] = "null";
			} else {
				String nameString = skinBones[i].getBone() == null ? "null" : skinBones[i].getBone().getName();
				nameString = StringPadder.padStringEnd(nameString + ":", " ", 29);
				String weightString = StringPadder.padStringStart(skinBones[i].getWeight() + "", " ", 3);

				strings[i] = nameString + weightString;
			}
		}
		return strings;
	}

	public JPanel getMatrixPanel(){
		JPanel panel = new JPanel(new MigLayout("fill"));
		for(Matrix matrix : matrixVertexMap.keySet()){
			JPanel sbInfoPanel = new JPanel(new MigLayout("gap 0, ins 0"));
			sbInfoPanel.add(new JLabel(getMatricesDescription(matrix)));

			JPanel sbPanel = new JPanel();
			sbPanel.add(sbInfoPanel);
			JButton edit = new JButton("Edit");
			edit.addActionListener(e -> changeMatrixSetup(matrix));
			sbInfoPanel.add(edit);
			JButton useForSelected = new JButton("Use for selected");
			useForSelected.addActionListener(e -> useForVertices(modelView.getSelectedVertices(), matrix.getBones()));
			sbInfoPanel.add(useForSelected);
			panel.add(sbInfoPanel, "wrap");
		}
		return panel;
	}




	public String getMatricesDescription(Matrix matrix) {
		StringBuilder boneList = new StringBuilder();

		int numBones = matrix.size();
		if(numBones>0){
			boneList.append("Matrix:{");
			for (int i = 0; i < numBones; i++) {
				if (i == (numBones - 2)) {
					boneList.append(matrix.get(i).getName()).append(" and ");
				} else if (i == (numBones - 1)) {
					boneList.append(matrix.get(i).getName());
				} else {
					boneList.append(matrix.get(i).getName()).append(", ");
				}
			}
			boneList.append("}: ");
			boneList.append(matrixVertexMap.get(matrix).size());
			boneList.append(" vertices");
		}
		return boneList.toString();
	}

	public void changeSkinBoneSetup(Collection<GeosetVertex> vertices, SkinBone[] skinBones){
		SkinPopup skinPopup = new SkinPopup(modelHandler.getModel(), skinBones);
		String[] words = {"Accept", "Cancel"};
		int i = JOptionPane.showOptionDialog(this, skinPopup, "Rebuild Skin", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == 0) {
			Bone[] bones = skinPopup.getBones();
			short[] weights = skinPopup.getSkinWeights();
			modelHandler.getUndoManager().pushAction(new SetHdSkinAction(vertices, bones, weights).redo());
		}
	}

	public void changeMatrixSetup(Matrix matrix){
		MatrixPopup matrixPopup = new MatrixPopup(modelHandler.getModel(), matrixVertexMap.get(matrix));
		String[] words = {"Accept", "Cancel"};
		int i = JOptionPane.showOptionDialog(this, matrixPopup, "Reassign Matrix", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == 0) {
			modelHandler.getUndoManager()
					.pushAction(new SetMatrixAction3(matrixVertexMap.get(matrix), matrixPopup.getNewBoneList(), matrixPopup.getBonesNotInAll()).redo());
		}
	}

	public void useForVertices(Collection<GeosetVertex> vertices, SkinBone[] skinBones){
		Bone[] bones = new Bone[skinBones.length];
		short[] weights = new short[skinBones.length];
		for(int i = 0; i<skinBones.length; i++){
			bones[i] = skinBones[i].getBone();
			weights[i] = skinBones[i].getWeight();
		}

		Set<GeosetVertex> affectedVerts = vertices.stream().filter(gv -> gv.getSkinBones() != null).collect(Collectors.toSet());

		modelHandler.getUndoManager().pushAction(new SetHdSkinAction(affectedVerts, bones, weights).redo());
	}

	public void useForVertices(Collection<GeosetVertex> vertices, Collection<Bone> bones){
		modelHandler.getUndoManager().pushAction(new SetMatrixAction3(vertices, bones, Collections.emptySet()).redo());
	}

	public void selectVerticesSubset(Collection<GeosetVertex> vertices){
		if (!modelView.sameSelection(vertices, Collections.emptySet(), Collections.emptySet())) {
			SelectionBundle bundle = new SelectionBundle(vertices);
			UndoAction action = new SetSelectionUggAction(bundle, modelView, "select skin vertices", ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public void selectVertices(SkinBone[] skinBones){
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
			UndoAction action = new SetSelectionUggAction(bundle, modelView, "select skin vertices", ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public void selectVertices(Matrix matrix){
		Set<GeosetVertex> vertices = new LinkedHashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (modelView.isEditable(vertex)) {
					if (matrix.equals(vertex.getMatrix())) {
						vertices.add(vertex);
					}
				}
			}
		}
		if (!vertices.isEmpty() && !modelView.sameSelection(vertices, Collections.emptySet(), Collections.emptySet())) {
			SelectionBundle bundle = new SelectionBundle(vertices);
			UndoAction action = new SetSelectionUggAction(bundle, modelView, "select skin vertices", ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public void replaceBones1(Map<IdObject, IdObject> bonesToReplace){
		Map<IdObject, IdObject> boneReplacements = new HashMap<>();
		Set<GeosetVertex> affectedVerts = new HashSet<>();

		for (Integer key : skinboneMap.keySet()) {
			SkinBone[] skinBones = skinboneMap.get(key);
			boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(sb -> bonesToReplace.containsKey(sb.getBone()));
			if (shouldAdjust) {
				affectedVerts.addAll(skinboneVertMap.get(key));
				for (SkinBone skinBone : skinBones) {
					Bone bone = skinBone.getBone();
					boneReplacements.put(bone, bonesToReplace.getOrDefault(bone, bone));
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

		modelHandler.getUndoManager().pushAction(new ReplaceBonesAction(affectedVerts, boneReplacements, changeListener).redo());
	}

	public void replaceBones(Collection<GeosetVertex> selection, Map<IdObject, IdObject> boneReplacements){
		modelHandler.getUndoManager().pushAction(new ReplaceBonesAction(selection, boneReplacements, changeListener).redo());
	}

	public void adjustInfluence(Collection<GeosetVertex> selection, Map<IdObject, Float> boneAdjustments){
		List<UndoAction> undoActions = new ArrayList<>();
		for(Integer key : skinboneMap.keySet()){
			SkinBone[] skinBones = skinboneMap.get(key);
			boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(sb -> boneAdjustments.containsKey(sb.getBone()));
			if(shouldAdjust){
				Bone[] bones = new Bone[skinBones.length];
				short[] weights = new short[skinBones.length];
				Short[] newWeights = new Short[skinBones.length];
				int notAdjustedWeights = 0;
				short extraWeight = 0;

				// calculate how much weight is redistributed and how many weights to spread the weight disparity over
				for (int i = 0; i < skinBones.length; i++){
					Bone bone = skinBones[i].getBone();
					short weight = skinBones[i].getWeight();
					bones[i] = bone;
					weights[i] = weight;
					Float adjust = boneAdjustments.get(bone);
					if(adjust != null){
						newWeights[i] = (short) MathUtils.clamp(weight * (1 + adjust), 0, 255);
						extraWeight += newWeights[i] - weight;
					} else {
						notAdjustedWeights++;
					}
				}

				// Spread the weight disparity
				for (int i = 0; i < skinBones.length; i++){
					if (newWeights[i] == null) {
						newWeights[i] = (short) MathUtils.clamp(weights[i] - extraWeight/notAdjustedWeights, 0, 255);
						extraWeight -= newWeights[i] - weights[i];
						notAdjustedWeights--;
					}
				}

				// Fix any left over extra weight (spreads any disparity if all weights were adjusted)
				for (int i = 0; i < skinBones.length; i++){
					weights[i] = (short) MathUtils.clamp(newWeights[i] - extraWeight/(skinBones.length-i), 0, 255);
					extraWeight -= weights[i] - newWeights[i];
				}


				List<GeosetVertex> vertices = skinboneVertMap.get(key);
				undoActions.add(new SetHdSkinAction(vertices, bones, weights));

//				for (int i = 0; i < skinBones.length; i++){
//					if (newWeights[i] != null) {
//						weights[i] = newWeights[i];
//					} else {
//						short orgWeight = weights[i];
//						weights[i] = (short) MathUtils.clamp(orgWeight - extraWeight/notAdjustedWeights, 0, 255);
//						extraWeight -= weights[i] - orgWeight;
//						notAdjustedWeights--;
//					}
//				}
//
//
//				for (int i = 0; i < skinBones.length; i++){
//					short orgWeight = weights[i];
//					weights[i] = (short) MathUtils.clamp(orgWeight - extraWeight/(skinBones.length-i), 0, 255);
//					extraWeight -= weights[i] - orgWeight;
//					notAdjustedWeights--;
//				}
//
//
//				List<GeosetVertex> vertices = skinboneVertMap.get(key);
//				undoActions.add(new SetHdSkinAction(vertices, bones, weights));
			}
		}
		modelHandler.getUndoManager().pushAction(new CompoundAction("Adjust skin wiegths", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated));

	}
//
//	public void increaseInfluence(Collection<GeosetVertex> selection, Map<IdObject, Float> boneAdjustments){
//		List<UndoAction> undoActions = new ArrayList<>();
//		for(Integer key : skinboneMap.keySet()){
//			SkinBone[] skinBones = skinboneMap.get(key);
//			boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(sb -> boneAdjustments.containsKey(sb.getBone()));
//			if(shouldAdjust){
//				Bone[] bones = new Bone[skinBones.length];
//				short[] weights = new short[skinBones.length];
////				Short[] adjWeights = new Short[skinBones.length];
//				Short[] adjWeights = new Short[skinBones.length];
//				int notAdjustedWeights = 0;
//				short extraWeight = 0;
//
//				// calculate how much weight is redistributed and how many weights to spread the weight disparity over
//				for (int i = 0; i < skinBones.length; i++){
//					Bone bone = skinBones[i].getBone();
//					short weight = skinBones[i].getWeight();
//					bones[i] = bone;
//					weights[i] = weight;
//					Float adjust = boneAdjustments.get(bone);
//					if(adjust != null){
//						adjWeights[i] = (short) MathUtils.clamp(weight * (1 + adjust), 0, 255);
//						extraWeight += adjWeights[i] - weight;
//					} else {
//						notAdjustedWeights++;
//					}
//				}
//
//				// Spread the weight disparity
//				for (int i = 0; i < skinBones.length; i++){
//					if (adjWeights[i] == null) {
//						short orgWeight = weights[i];
//						weights[i] = (short) MathUtils.clamp(orgWeight - extraWeight/notAdjustedWeights, 0, 255);
//						extraWeight -= weights[i] - orgWeight;
//						notAdjustedWeights--;
//					} else {
//						weights[i] = adjWeights[i];
//					}
//				}
//
//
//				for (int i = 0; i < skinBones.length; i++){
//					short orgWeight = weights[i];
//					weights[i] = (short) MathUtils.clamp(orgWeight - extraWeight/(skinBones.length-i), 0, 255);
//					extraWeight -= weights[i] - orgWeight;
//					notAdjustedWeights--;
//				}
//
//
//				List<GeosetVertex> vertices = skinboneVertMap.get(key);
//				undoActions.add(new SetHdSkinAction(vertices, bones, weights));
//			}
//		}
//
//		for (GeosetVertex vertex : selection) {
//			SkinBone[] skinBones = vertex.getSkinBones();
//			if(skinBones != null){
//				boolean shouldAdjust = Arrays.stream(skinBones).anyMatch(sb -> boneAdjustments.containsKey(sb.getBone()));
//				if(shouldAdjust){
////					undoActions.add(new SetHdSkinAction())
//				}
//			}
//		}
//	}

	private void collectUniqueSkinnings(){

		System.out.println(modelView.getSelectedVertices().size() + " verts selected");
		skinboneVertMap.clear();
		skinboneMap.clear();
		boneMap.clear();
		matrixSet.clear();
		matrixVertexMap.clear();

		for(GeosetVertex vertex : modelView.getSelectedVertices()){
			SkinBone[] skinBones = vertex.getSkinBones();

			if (skinBones != null){
				int hashCode = Arrays.hashCode(skinBones);
				skinboneMap.putIfAbsent(hashCode, skinBones);
				skinboneVertMap.computeIfAbsent(hashCode, k -> new ArrayList<>()).add(vertex);

				Bone[] skinBoneBones = vertex.getSkinBoneBones();
				int hc = Arrays.hashCode(skinBoneBones);
				boneMap.putIfAbsent(hc, skinBoneBones);

			} else {
				matrixSet.add(vertex.getMatrix());

				Set<GeosetVertex> vertexSet = matrixVertexMap.get(vertex.getMatrix());
				if(vertexSet == null){
					vertexSet = new HashSet<>();
					matrixVertexMap.put(new Matrix(vertex.getBones()), vertexSet);
				}
				vertexSet.add(vertex);
			}
		}

		System.out.println(skinboneMap.size() + " unique skinnings found using " + boneMap.size() + " unique bone combinations, " + matrixSet.size() + " matrices found");
	}


	public JPanel getBoneAdjustmentsPanel(){
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
			boneThingPanel.add(getAdjustBonePanel(bones));
			panel.revalidate();
		});
//		searchableList.addSelectionListener1((bone) -> {
//			System.out.println("selected bone: " + bone);
//			boneThingPanel.removeAll();
//			boneThingPanel.add(getAdjustBonePanel(bone));
//			panel.revalidate();
////			SwingUtilities.invokeLater(panel::revalidate);
////			if(panel.getParent() != null){
////				panel.getParent().repaint();
////			}
////			boneThingPanel.repaint();
//		});
		panel.add(boneThingPanel, "growx, growy");
		return panel;
	}

	public JPanel getAdjustBonePanel(Bone bone){
		JPanel panel = new JPanel(new MigLayout("fill"));
		String name = bone == null ? "none" : bone.getName();
		System.out.println("new panel for: " + name);
		panel.add(new JLabel(name), "wrap");
		JButton removeFromSelected = new JButton("Remove from selected");
		removeFromSelected.addActionListener(e -> removeFromSelected(bone));
		panel.add(removeFromSelected, "wrap");
		JButton replaceForSelected = new JButton("Replace for selected");
		removeFromSelected.addActionListener(e -> replaceForSelected(bone));
		panel.add(replaceForSelected, "wrap");
		if(!skinboneMap.isEmpty()){
			panel.add(new JButton("Adjust influence for selected"), "wrap");
		}
		return panel;
	}

	public void removeFromSelected(Bone bone){
		Map<IdObject, IdObject> toRemoveMap = new HashMap<>();
		toRemoveMap.put(bone, null);
		replaceBones1(toRemoveMap);
	}

	public void replaceForSelected(Bone bone){
		Map<IdObject, IdObject> replaceMap = new HashMap<>();
		IdObjectChooser idObjectChooser = new IdObjectChooser(modelHandler.getModel(), false).setClasses(Bone.class);
		IdObject idObject = idObjectChooser.chooseObject(bone, this);
		if(idObject != null){
			replaceMap.put(bone, idObject);
			replaceBones1(replaceMap);
		}
	}

	public JPanel getAdjustBonePanel(Collection<Bone> bones){
		JPanel panel = new JPanel(new MigLayout("fill"));

		StringBuilder nameBuilder = new StringBuilder();
		int bonesLeft = bones.size();
		for (Bone bone : bones){
			if(bone != null){
//				nameBuilder.append(bone.getName());
				if (bonesLeft == 2) {
					nameBuilder.append(bone.getName()).append(" and ");
				} else if (bonesLeft == 1) {
					nameBuilder.append(bone.getName());
				} else {
					nameBuilder.append(bone.getName()).append(", ");
				}
			}
			bonesLeft--;
		}

		String name = nameBuilder.toString();
		System.out.println("new panel for: " + name);
		panel.add(new JLabel(name), "wrap");
		JButton removeFromSelected = new JButton("Remove from selected");
		removeFromSelected.addActionListener(e -> removeFromSelected(bones));
		panel.add(removeFromSelected, "wrap");
		JButton replaceForSelected = new JButton("Replace for selected");
		replaceForSelected.addActionListener(e -> replaceForSelected(bones));
		panel.add(replaceForSelected, "wrap");
		if(!skinboneMap.isEmpty()){
			panel.add(new JButton("Adjust influence for selected"), "wrap");
		}
		return panel;
	}
	public void removeFromSelected(Collection<Bone> bones){
		System.out.println("remove from selected!");
		Map<IdObject, IdObject> toRemoveMap = new HashMap<>();
		for (Bone bone : bones){
			toRemoveMap.put(bone, null);
		}
		replaceBones1(toRemoveMap);
	}

	public void replaceForSelected(Collection<Bone> bones){
		System.out.println("replace for selected!");
		Map<IdObject, IdObject> replaceMap = new HashMap<>();
		IdObjectChooser idObjectChooser = new IdObjectChooser(modelHandler.getModel(), false).setClasses(Bone.class);
		IdObject idObject = idObjectChooser.chooseObject(null, this);
		if(idObject != null){
			for (Bone bone : bones){
				replaceMap.put(bone, idObject);
			}
			replaceBones1(replaceMap);
		}
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



	public static void showPanel(JComponent parent, ModelHandler modelHandler) {
		SkinningOptionPanel skinningOptionPanel = new SkinningOptionPanel(modelHandler);
		skinningOptionPanel.setPreferredSize(new Dimension(800, 650));
		skinningOptionPanel.revalidate();
//		FramePopup.show(skinningOptionPanel, ProgramGlobals.getMainPanel(), "Edit Textures");
		FramePopup.show(skinningOptionPanel, parent, "Edit Skinning");
	}
}
