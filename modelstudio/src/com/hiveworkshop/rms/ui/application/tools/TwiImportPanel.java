package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public abstract class TwiImportPanel extends JPanel {
	ModelIconHandler iconHandler = new ModelIconHandler();
	EditableModel donModel;
	EditableModel recModel;
	ModelHandler recModelHandler;
	BoneChooser donBoneChooser;
	BoneChooser recBoneChooser;

	Animation prototypeAnim = new Animation("An Extra Empty Animation", 0, 1000);
	AnimShell prototypeAnimShell = new AnimShell(prototypeAnim);

	AnimListCellRenderer donRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> donAnimations = new IterableListModel<>();
	JList<AnimShell> donAnimList = new JList<>(donAnimations);

	AnimListCellRenderer recRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> recAnimations = new IterableListModel<>();
	JList<AnimShell> recAnimList = new JList<>(recAnimations);

	Bone chosenDonBone = null;
	Bone chosenRecBone = null;
	BoneOption boneOption = BoneOption.rebindGeometry;
	Integer boneChainDepth = -1;

	String donBoneButtonText = "Choose Bone Chain Start";
	String recBoneButtonText = "Choose Bone Chain Start";

	public TwiImportPanel(EditableModel donModel, ModelHandler recModelHandler) {
//		super(new MigLayout("fill, wrap 2, debug", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 0][grow 0][grow 0][grow 0]"));
//		super(new MigLayout("fill, debug", "[sg half][sg half]", ""));
//		super(new MigLayout("fill, debug", "[][]", ""));
		this.donModel = donModel;
		this.recModel = recModelHandler.getModel();
		this.recModelHandler = recModelHandler;

		donAnimList.setPrototypeCellValue(prototypeAnimShell);
		recAnimList.setPrototypeCellValue(prototypeAnimShell);

		donBoneChooser = new BoneChooser(donModel);
		recBoneChooser = new BoneChooser(recModel);

		fillLists(donModel, recModel);
	}

	protected JButton getButton(String text, Consumer<JButton> buttonConsumer, EditableModel model) {
		JButton button = new JButton(text);
		if(model != null){
			button.setIcon(iconHandler.getImageIcon(null, model));
		}
		button.addActionListener(e -> buttonConsumer.accept(button));
		return button;
	}

	protected void chooseDonBone(JButton chooseBone) {
		chosenDonBone = donBoneChooser.chooseBone(chosenDonBone, this);
		if (chosenDonBone != null) {
			chooseBone.setText(chosenDonBone.getName());
//			chooseBone.setIcon(iconHandler.getImageIcon(chosenDonBone, donModel));
		} else {
			chooseBone.setText(donBoneButtonText);
//			chooseBone.setIcon(null);
		}
		chooseBone.setIcon(iconHandler.getImageIcon(chosenDonBone, donModel));
		repaint();
	}
	protected void chooseRecBone(JButton chooseBone) {
		chosenRecBone = recBoneChooser.chooseBone(chosenRecBone, this);
		if (chosenRecBone != null) {
			chooseBone.setText(chosenRecBone.getName());
//			chooseBone.setIcon(iconHandler.getImageIcon(chosenRecBone, recModel));
		} else {
			chooseBone.setText(recBoneButtonText);
//			chooseBone.setIcon(null);
		}
		chooseBone.setIcon(iconHandler.getImageIcon(chosenRecBone, recModel));
		repaint();
	}
	protected void chooseRecBone(BoneChooser boneChooser, Bone oldBone, EditableModel model, JButton button, String nullText, Consumer<Bone> boneConsumer) {
		Bone bone = boneChooser.chooseBone(oldBone, this);
		if (bone != null) {
			button.setText(bone.getName());
			button.setIcon(iconHandler.getImageIcon(bone, model));
		} else {
			button.setText(nullText);
			button.setIcon(null);
		}
		boneConsumer.accept(bone);
		repaint();
	}

	protected JPanel getBoneOptionPanel(){
		SmartButtonGroup extraBonesOption = new SmartButtonGroup("Handle bones of geometry found outside of bone chain");
//		SmartButtonGroup extraBonesOption = new SmartButtonGroup();
		extraBonesOption.setButtonConst("");
		for(BoneOption option : BoneOption.values()){
			extraBonesOption.addJRadioButton(option.getText(), e -> boneOption = option).setToolTipText(option.getTooltip());
		}
		extraBonesOption.setSelectedIndex(BoneOption.rebindGeometry.ordinal());
//		extraBonesOption.addJRadioButton("Import", null);
//		extraBonesOption.addJRadioButton("Rebind Geometry", null);
//		extraBonesOption.addJRadioButton("Leave Geometry", null);

//		JPanel panel = new JPanel(new MigLayout("", ""));
//		panel.add(extraBonesOption.getButtonPanel());
//		return panel;
		return extraBonesOption.getButtonPanel();
	}

	protected void fillLists(EditableModel donModel, EditableModel recModel) {
		for (Animation animation : donModel.getAnims()) {
			donAnimations.addElement(new AnimShell(animation));
		}
		donAnimList.setCellRenderer(donRenderer);

		for (Animation animation : recModel.getAnims()) {
			recAnimations.addElement(new AnimShell(animation));
		}
		recAnimList.setCellRenderer(recRenderer);
	}

	protected JPanel getAnimMapPanel() {
//		JPanel animMapPanel = new JPanel(new MigLayout("ins 0, fill", "[50%][50%]", "[grow]"));
		JPanel animMapPanel = new JPanel(new MigLayout("ins 0, fill, wrap 2", "[sgx anim][sgx anim]", "[][grow][]"));
		animMapPanel.add(new JLabel("Map motion from:"), "");
		animMapPanel.add(new JLabel("Into existing animation:"), "wrap");

//		JPanel animMapPanelDon = new JPanel(new MigLayout("ins 0, fill", "[grow]", "[grow]"));
//		JPanel animMapPanelRec = new JPanel(new MigLayout("ins 0, fill", "[grow]", "[grow]"));
//		JScrollPane comp = new JScrollPane(donAnimList);
//		animMapPanelDon.add(comp, "growy, growx");
////		animMapPanelDon.add(new JScrollPane(donAnimList), "growy, growx");
//		animMapPanelRec.add(new JScrollPane(recAnimList), "growy, growx");
//		animMapPanel.add(animMapPanelDon, "growy, growx");
//		animMapPanel.add(animMapPanelRec, "growy, growx");

		animMapPanel.add(new JScrollPane(donAnimList), "growy, growx");
		animMapPanel.add(new JScrollPane(recAnimList), "growy, growx");

//		animMapPanel.add(new JScrollPane(donAnimList), "growy, growx, gpy 200");
//		animMapPanel.add(new JScrollPane(recAnimList), "growy, growx, gpy 200, wrap");

		donAnimList.addListSelectionListener(this::donAnimationSelectionChanged);
		recAnimList.addListSelectionListener(this::recAnimationSelectionChanged);

		return animMapPanel;
	}


	private void donAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() && donAnimList.getSelectedValue() != null) {
			recAnimList.setSelectedValue(null, false);
			scrollToRevealFirstChosen(donAnimList.getSelectedValue());
			recRenderer.setSelectedAnim(donAnimList.getSelectedValue());
		}
	}

	private void scrollToRevealFirstChosen(AnimShell animShell) {
		if(animShell.getImportType() == AnimShell.ImportType.TIMESCALE){
			for (int indexOfFirst = 0; indexOfFirst < recAnimations.getSize(); indexOfFirst++){
				if(recAnimations.get(indexOfFirst).getImportAnimShell() == animShell){
					Rectangle cellBounds = recAnimList.getCellBounds(indexOfFirst, indexOfFirst);
					if(cellBounds != null){
						recAnimList.scrollRectToVisible(cellBounds);
					}
					break;
				}
			}
		}
	}

	private void recAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			AnimShell donAnimShell = donAnimList.getSelectedValue();
			donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE);

			Set<AnimShell> donAnimsToCheck = new HashSet<>();
			for (AnimShell as : recAnimList.getSelectedValuesList()) {
				if (as.getImportAnimShell() == donAnimShell) {
					donAnimsToCheck.add(donAnimShell);
					as.setImportAnimShell(null);
				} else {
					if(as.getImportAnimShell() != null){
						donAnimsToCheck.add(as.getImportAnimShell());
					}
					as.setImportAnimShell(donAnimShell);
				}
			}
			recAnimList.setSelectedValue(null, false);

			for(AnimShell animShell : donAnimsToCheck){
				boolean isImp = false;
				for(AnimShell as : recAnimations){
					isImp = as.getImportAnimShell() == animShell;
					if(isImp){
						break;
					}
				}
				if (!isImp){
					animShell.setImportType(AnimShell.ImportType.IMPORTBASIC);
				}
			}
			donAnimList.repaint();
		}
	}

	protected Map<Sequence, Sequence> getRecToDonSequenceMap() {
		Map<Sequence, Sequence> recToDonSequenceMap = new HashMap<>(); // receiving animations to donating animations
		for (AnimShell animShell : recAnimations) {
			if (animShell.getImportAnimShell() != null && animShell.getImportAnimShell().getImportType() == AnimShell.ImportType.TIMESCALE) {
				recToDonSequenceMap.put(animShell.getAnim(), animShell.getImportAnimShell().getAnim());
			}
		}
		return recToDonSequenceMap;
	}

//	protected Map<IdObject, IdObject> getChainMap(Bone donBone, Bone recBone, int depth){
//		Map<IdObject, IdObject> boneChainMap = new HashMap<>();
//		if(depth == -1) depth = 10000;
//
//		boneChainMap.put(recBone, donBone);
//
//		int currDepth = 0;
//		fillBoneChainMap(depth, boneChainMap, donBone, donModel, recBone, recModel, currDepth);
//		return boneChainMap;
//	}
//	protected Map<IdObject, IdObject> getChainMap1(Bone donBone, Bone recBone, int depth){
//		Map<IdObject, IdObject> boneChainMap = new HashMap<>();
//		if(depth == -1) depth = 10000;
//
//		boneChainMap.put(recBone, donBone);
//
//		int currDepth = 0;
//		fillBoneChainMap(depth, boneChainMap, donBone, recBone, currDepth);
//		return boneChainMap;
//	}
//	protected Map<IdObject, IdObject> getChainMapReverse(Bone donBone, Bone recBone, int depth){
//		Map<IdObject, IdObject> boneChainMap = new HashMap<>();
//		if(depth == -1) depth = 10000;
//
//		boneChainMap.put(donBone, recBone);
//
//		int currDepth = 0;
//		fillBoneChainMap(depth, boneChainMap, recBone, recModel, donBone, donModel, currDepth);
//		return boneChainMap;
//	}

//	protected void fillBoneChainMap(int depth, Map<IdObject, IdObject> boneChainMap, IdObject currDonBone, IdObject currRecBone, int currDepth) {
//		if (currDepth < depth){
//			currDepth++;
//			List<IdObject> recChilds = currRecBone.getChildrenNodes();
//			List<IdObject> donChilds = currDonBone.getChildrenNodes();
//			if (recChilds.size() == 1 && donChilds.size() == 1){
//				if(isBones(recChilds.get(0), donChilds.get(0))){
//					boneChainMap.put(recChilds.get(0), donChilds.get(0));
//					fillBoneChainMap(depth, boneChainMap, donChilds.get(0), recChilds.get(0), currDepth);
//				} else if(recChilds.get(0).getClass() == donChilds.get(0).getClass()){
//					boneChainMap.put(recChilds.get(0), donChilds.get(0));
//				}
//			} else if (!(recChilds.isEmpty() || donChilds.isEmpty())) {
//				Map<IdObject, IdObject> boneChainSubMap = getBoneChainSubMap(donChilds, recChilds);
//				boneChainMap.putAll(boneChainSubMap);
//				for(IdObject idObject : boneChainSubMap.keySet()){
//					if (boneChainSubMap.get(idObject) != null && isBones(idObject, boneChainSubMap.get(idObject))){
//						fillBoneChainMap(depth, boneChainMap, boneChainSubMap.get(idObject), idObject, currDepth);
//					}
//				}
//			}
//		}
//	}
//	protected Map<IdObject, IdObject> getBoneChainSubMap(List<IdObject> donIdObjects, List<IdObject> recIdObjects){
//		Map<IdObject, IdObject> boneChainSubMap = new HashMap<>();
//		JPanel objectMappingPanel = new JPanel(new MigLayout());
//		for(IdObject idObject : recIdObjects){
//			JComboBox<ObjectShell> boneChooserBox = getBoneChooserBox(idObject, donIdObjects, donModel, (o -> boneChainSubMap.put(idObject, o)));
//			if(boneChooserBox.getModel().getSize() == 2){
//				boneChainSubMap.put(idObject, boneChooserBox.getModel().getElementAt(1).getIdObject());
//			} else if (boneChooserBox.getModel().getSize() > 2){
//				objectMappingPanel.add(new JLabel(iconHandler.getImageIcon(idObject, recModel)));
//				objectMappingPanel.add(new JLabel(idObject.getName()));
//				objectMappingPanel.add(boneChooserBox, "wrap");
//			}
//		}
//		JOptionPane.showConfirmDialog(this, objectMappingPanel, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
//
//		return boneChainSubMap;
//	}

	protected Map<IdObject, IdObject> getChainMap(Bone mapToBone, EditableModel mapToModel, Bone mapFromBone, EditableModel mapFromModel, int depth, boolean presentParent){
		Map<IdObject, IdObject> boneChainMap = new HashMap<>();
		if(depth == -1) depth = 10000;

		boneChainMap.put(mapFromBone, mapToBone);

		int currDepth = 0;
		fillBoneChainMap(depth, boneChainMap, mapToBone, mapToModel, mapFromBone, mapFromModel, currDepth, presentParent);
		return boneChainMap;
	}

	protected void fillBoneChainMap(int depth,
	                                Map<IdObject, IdObject> boneChainMap,
	                                IdObject mapToBone, EditableModel mapToModel,
	                                IdObject mapFromBone, EditableModel mapFromModel,
	                                int currDepth, boolean presentParent) {
		if (currDepth < depth){
			currDepth++;
			List<IdObject> mapFromChilds = mapFromBone.getChildrenNodes();
			List<IdObject> mapToChilds = mapToBone.getChildrenNodes();
			if (mapFromChilds.size() == 1 && mapToChilds.size() == 1){
				if(isBones(mapFromChilds.get(0), mapToChilds.get(0))){
					boneChainMap.put(mapFromChilds.get(0), mapToChilds.get(0));
					fillBoneChainMap(depth, boneChainMap, mapToChilds.get(0), mapToModel, mapFromChilds.get(0), mapFromModel, currDepth, presentParent);
				} else if(mapFromChilds.get(0).getClass() == mapToChilds.get(0).getClass()){
					boneChainMap.put(mapFromChilds.get(0), mapToChilds.get(0));
				}
			} else if (!(mapFromChilds.isEmpty() || mapToChilds.isEmpty())) {
				Map<IdObject, IdObject> boneChainSubMap = getBoneChainSubMap(mapToChilds, mapToModel, mapFromChilds, mapFromModel, presentParent);
				boneChainMap.putAll(boneChainSubMap);
				for(IdObject idObject : boneChainSubMap.keySet()){
					if (boneChainSubMap.get(idObject) != null && isBones(idObject, boneChainSubMap.get(idObject))){
						fillBoneChainMap(depth, boneChainMap, boneChainSubMap.get(idObject), mapToModel, idObject, mapFromModel, currDepth, presentParent);
					}
				}
			}
		}
	}

	protected Map<IdObject, IdObject> getBoneChainSubMap(List<IdObject> idObjectsForComboBox,
	                                                     EditableModel modelForComboBox,
	                                                     List<IdObject> idObjectsForPanel,
	                                                     EditableModel modelForPanel, boolean presentParent){
		Map<IdObject, IdObject> boneChainSubMap = new HashMap<>();
		JPanel objectMappingPanel = new JPanel(new MigLayout());
		for(IdObject idObjectForPanel : idObjectsForPanel){
			JComboBox<ObjectShell> boneChooserBox = getBoneChooserBox(idObjectForPanel, idObjectsForComboBox, modelForComboBox, (o -> boneChainSubMap.put(idObjectForPanel, o)), presentParent);
			if (boneChooserBox.getModel().getSize() <= 2){
				boneChainSubMap.put(idObjectForPanel, boneChooserBox.getModel().getElementAt(1).getIdObject());
			} else if (boneChooserBox.getModel().getSize() > 2 || presentParent) {
				objectMappingPanel.add(new JLabel(iconHandler.getImageIcon(idObjectForPanel, modelForPanel)));
				objectMappingPanel.add(new JLabel(idObjectForPanel.getName()));
				objectMappingPanel.add(boneChooserBox, "wrap");
			}
		}
		if(objectMappingPanel.getComponentCount() > 0){
			JScrollPane scrollPane = new JScrollPane(objectMappingPanel);
			scrollPane.setMaximumSize(ScreenInfo.getSmallWindow());
			JOptionPane.showConfirmDialog(this, scrollPane, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		}

		return boneChainSubMap;
	}

	private JComboBox<ObjectShell> getBoneChooserBox(IdObject idObjectDest, List<IdObject> idObjectsForComboBox, EditableModel modelForComboBox, Consumer<IdObject> idObjectConsumer, boolean presentParent){
		JComboBox<ObjectShell> comboBox = new JComboBox<>();
		comboBox.addItem(new ObjectShell(null));
		if (presentParent && idObjectDest.getParent() != null){
			comboBox.addItem(new ObjectShell(idObjectDest.getParent()));
		}
		ObjectShell sameNameObject = null;
		int lastMatch = 20;
		String destName = idObjectDest.getName();
		for (IdObject idObject : idObjectsForComboBox){
			if(idObject instanceof Bone && idObjectDest instanceof Bone || idObject.getClass() == idObjectDest.getClass()){
				ObjectShell newObjectShell = new ObjectShell(idObject);
				comboBox.addItem(newObjectShell);
				String name = idObject.getName();
				if(name.equals(destName)){
					sameNameObject = newObjectShell;
					lastMatch = Math.abs(sameNameObject.getIdObject().getName().compareTo(destName));
				} else if(-lastMatch < name.compareTo(destName) && name.compareTo(destName) < lastMatch){
					String[] namsSplit = name.split("_");
					String[] destSplit = destName.split("_");
					boolean match = true;
					for(int i = 0; i<namsSplit.length && i< destSplit.length; i++){
						if(!namsSplit[i].equals(destSplit[i])){
							match = false;
							break;
						}
					}
					if(match){
						sameNameObject = newObjectShell;
						lastMatch = Math.abs(sameNameObject.getIdObject().getName().compareTo(destName));
					}
				}
			}
		}
		comboBox.setRenderer(new ObjectShellListCellRenderer(modelForComboBox, null));
		comboBox.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED){
				idObjectConsumer.accept(((ObjectShell) e.getItem()).getIdObject());
			}
		});
		if(sameNameObject == null){
			comboBox.setSelectedIndex(0);
		} else {
			comboBox.setSelectedItem(sameNameObject);
		}
		return comboBox;
	}


	protected Set<Geoset> getNewGeosets1(Set<Bone> selectedBones) {
		Set<Geoset> newGeosets = new HashSet<>();
		Set<Bone> extraBones = new HashSet<>();
		for (Geoset donGeoset : donModel.getGeosets()) {
			Geoset newGeoset = donGeoset.deepCopy();
			Set<GeosetVertex> vertexSet = new HashSet<>();
			for (Bone bone : selectedBones) {
				List<GeosetVertex> vertices = newGeoset.getBoneMap().get(bone);
				if (vertices != null) {
					vertexSet.addAll(vertices);
				}
			}
			if (!vertexSet.isEmpty()) {
				Set<GeosetVertex> verticesToPurge = new HashSet<>();
				for (GeosetVertex vertex : vertexSet) {
					switch (boneOption){
						case importBones, importBonesExtra -> {
							if (vertex.getSkinBones() != null) {
								for (SkinBone skinBone : vertex.getSkinBones()) {
									if (skinBone != null && skinBone.getBone() != null) {
										extraBones.add(skinBone.getBone());
									}
								}
							} else {
								extraBones.addAll(vertex.getBones());
							}
						}
						case rebindGeometry -> {
							if (vertex.getSkinBones() != null) {
								short extraWeight = 0;
								for (SkinBone skinBone : vertex.getSkinBones()) {
									if (skinBone != null && skinBone.getBone() != null && !selectedBones.contains(skinBone.getBone())) {
										extraWeight += skinBone.getWeight();
										skinBone.setBone(null);
										skinBone.setWeight((short) 0);
									}
								}
								for (SkinBone skinBone : vertex.getSkinBones()) {
									if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0){
										skinBone.setWeight((short) (skinBone.getWeight() + extraWeight));
										break;
									}
								}

							} else {
								Set<Bone> vertBones = new HashSet<>(vertex.getBones());
								vertBones.removeAll(selectedBones);
								vertex.removeBones(vertBones);
							}
						}
						case leaveGeometry -> {
							if (vertex.getSkinBones() != null) {
								for (SkinBone skinBone : vertex.getSkinBones()) {
									if (skinBone != null && skinBone.getBone() != null && !selectedBones.contains(skinBone.getBone())) {
										verticesToPurge.add(vertex);
										break;
									}
								}

							} else {
								if(!selectedBones.containsAll(vertex.getBones())){
									verticesToPurge.add(vertex);
								}
							}
						}
					}
				}
				selectedBones.addAll(extraBones);
				if(boneOption == BoneOption.importBonesExtra) {
					for (Bone bone : selectedBones) {
						List<GeosetVertex> vertices = newGeoset.getBoneMap().get(bone);
						if (vertices != null) {
							for(GeosetVertex vertex : vertices){
								if(selectedBones.containsAll(vertex.getAllBones())){
									vertexSet.add(vertex);
								}
							}
						}
					}
				}

				vertexSet.removeAll(verticesToPurge);
				Set<Triangle> trianglesToRemove = new HashSet<>();
				Set<GeosetVertex> verticesToCull = new HashSet<>();
				for (Triangle triangle : newGeoset.getTriangles()) {
					List<GeosetVertex> triVerts = Arrays.asList(triangle.getVerts());
					if (!vertexSet.containsAll(triVerts)) {
						trianglesToRemove.add(triangle);
						verticesToCull.addAll(triVerts);
					}
				}

				verticesToCull.removeAll(vertexSet);
				newGeoset.remove(verticesToCull);

				trianglesToRemove.forEach(newGeoset::removeExtended);

				newGeoset.setParentModel(recModel);
				newGeosets.add(newGeoset);
			}

		}
		return newGeosets;
	}


	protected Set<Geoset> getNewGeosets(Set<Bone> selectedBones) {
		Set<Geoset> newGeosets = new HashSet<>();
		Set<Bone> extraBones = new HashSet<>();
		for (Geoset donGeoset : donModel.getGeosets()) {
			Geoset newGeoset = donGeoset.deepCopy();
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			if (!vertexSet.isEmpty()) {
				switch (boneOption) {
					case importBones, importBonesExtra -> extraBones.addAll(getExtraBones(vertexSet));
					case rebindGeometry -> removeBonesNotInSet(selectedBones, vertexSet);
					case leaveGeometry -> removeVerticesNotFullyCovered(selectedBones, vertexSet);
				}

				newGeoset.setParentModel(recModel);
				newGeosets.add(newGeoset);
			}
		}

		for (Geoset newGeoset : newGeosets){
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			if(boneOption == BoneOption.importBonesExtra){
				vertexSet.addAll(getVertexSet(extraBones, newGeoset));
			}

			Set<Triangle> trianglesToRemove = new HashSet<>();
			Set<GeosetVertex> verticesToCull = new HashSet<>();
			for (Triangle triangle : newGeoset.getTriangles()) {
				List<GeosetVertex> triVerts = Arrays.asList(triangle.getVerts());
				if (!vertexSet.containsAll(triVerts)) {
					trianglesToRemove.add(triangle);
					verticesToCull.addAll(triVerts);
				}
			}

			verticesToCull.removeAll(vertexSet);
			newGeoset.remove(verticesToCull);

			trianglesToRemove.forEach(newGeoset::removeExtended);
		}
		selectedBones.addAll(extraBones);
		return newGeosets;
	}

	private Set<Bone> getExtraBones(Set<GeosetVertex> vertexSet) {
		Set<Bone> extraBones = new HashSet<>();
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null) {
						extraBones.add(skinBone.getBone());
					}
				}
			} else {
				extraBones.addAll(vertex.getBones());
			}
		}
		return extraBones;
	}

	private Set<GeosetVertex> getVertexSet(Set<Bone> selectedBones, Geoset newGeoset) {
		Set<GeosetVertex> vertexSet = new HashSet<>();
		for (Bone bone : selectedBones) {
			List<GeosetVertex> vertices = newGeoset.getBoneMap().get(bone);
			if (vertices != null) {
				vertexSet.addAll(vertices);
			}
		}
		return vertexSet;
	}

	private void removeVerticesNotFullyCovered(Set<Bone> selectedBones, Set<GeosetVertex> vertexSet) {
		Set<GeosetVertex> verticesToPurge = new HashSet<>();
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && !selectedBones.contains(skinBone.getBone())) {
						verticesToPurge.add(vertex);
						break;
					}
				}

			} else {
				if(!selectedBones.containsAll(vertex.getBones())){
					verticesToPurge.add(vertex);
				}
			}
		}

		vertexSet.removeAll(verticesToPurge);
	}

	private void removeBonesNotInSet(Set<Bone> selectedBones, Set<GeosetVertex> vertexSet) {
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				short extraWeight = 0;
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && !selectedBones.contains(skinBone.getBone())) {
						extraWeight += skinBone.getWeight();
						skinBone.setBone(null);
						skinBone.setWeight((short) 0);
					}
				}
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0){
						skinBone.setWeight((short) (skinBone.getWeight() + extraWeight));
						break;
					}
				}

			} else {
				Set<Bone> vertBones = new HashSet<>(vertex.getBones());
				vertBones.removeAll(selectedBones);
				vertex.removeBones(vertBones);
			}
		}
	}

	private boolean containsAll(Triangle triangle, Set<GeosetVertex> vertexSet) {
		return vertexSet.contains(triangle.get(0))
				&& vertexSet.contains(triangle.get(1))
				&& vertexSet.contains(triangle.get(2));
	}


	private boolean isBones(IdObject idObject1, IdObject idObject2) {
		return idObject1 instanceof Bone && idObject2 instanceof Bone;
	}

	private void mergeUnnecessaryBonesWithHelpers(EditableModel model){
		Set<Bone> bonesWOMotion = new HashSet<>();
		model.getBones().stream()
				.filter(b -> b.getAnimFlags().isEmpty() && b.getChildrenNodes().isEmpty() && b.getParent() instanceof Helper)
				.forEach(bonesWOMotion::add);

//		Set<IdObject> decomParents = new HashSet<>();
		for (Bone bone : bonesWOMotion){
			IdObject parent = bone.getParent();
			bone.setPivotPoint(parent.getPivotPoint());
			bone.setAnimFlags(parent.getAnimFlags());
//			List<IdObject> childList = new ArrayList<>(parent.getChildrenNodes());
//			for(IdObject child : childList){
//				child.setParent(parent.getParent());
//			}
			bone.setParent(parent.getParent());
//			decomParents.add(parent);
		}

//		for (IdObject parent)
	}

	protected enum BoneOption {
		rebindGeometry("Rebind Geometry", "Remove bones not in bone chain from bone bindings"),
		leaveGeometry("Leave Geometry", "Remove vertices bound to bones not in bone chain"),
		importBones("Import Bones", "Import bones which has vertices to be imported bound to them"),
		importBonesExtra("Import Bones and Extra Geometry", "Import bones which has vertices to be imported bound to them, and extra vertices bound only to these bones");
		String text;
		String tooltip;
		BoneOption(String text, String tooltip){
			this.text = text;
			this.tooltip = tooltip;
		}

		public String getText() {
			return text;
		}

		public String getTooltip() {
			return tooltip;
		}
	}
}
