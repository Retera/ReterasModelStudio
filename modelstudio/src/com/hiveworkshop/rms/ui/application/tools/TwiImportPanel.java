package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
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
	IdObjectChooserButton donBoneChooserButton;
	IdObjectChooserButton recBoneChooserButton;

	Animation prototypeAnim = new Animation("An Extra Empty Animation", 0, 1000);
	AnimShell prototypeAnimShell = new AnimShell(prototypeAnim);

	AnimShell asNewAnim = new AnimShell(new Animation("as New Anim", 0, 1));

	AnimListCellRenderer donRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> donAnimations = new IterableListModel<>();
	JList<AnimShell> donAnimList = new JList<>(donAnimations);

	AnimListCellRenderer recRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> recAnimations = new IterableListModel<>();
	JList<AnimShell> recAnimList = new JList<>(recAnimations);

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

		donBoneChooserButton = new IdObjectChooserButton(donModel, this);
		recBoneChooserButton = new IdObjectChooserButton(recModel, this);

		fillLists(donModel, recModel);
	}

	protected JPanel getBoneOptionPanel(){
		SmartButtonGroup extraBonesOption = new SmartButtonGroup("Handle bones of geometry found outside of bone chain");
//		SmartButtonGroup extraBonesOption = new SmartButtonGroup();
		extraBonesOption.setButtonConst("");
		for(BoneOption option : BoneOption.values()){
			extraBonesOption.addJRadioButton(option.getText(), e -> boneOption = option).setToolTipText(option.getTooltip());
		}
		extraBonesOption.setSelectedIndex(BoneOption.rebindGeometry.ordinal());
		return extraBonesOption.getButtonPanel();
	}

	protected void fillLists(EditableModel donModel, EditableModel recModel) {
		for (Animation animation : donModel.getAnims()) {
			AnimShell animShell = new AnimShell(animation);
			animShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
			donAnimations.addElement(animShell);
		}
		donAnimList.setCellRenderer(donRenderer);

		recAnimations.addElement(asNewAnim);
		for (Animation animation : recModel.getAnims()) {
			recAnimations.addElement(new AnimShell(animation));
		}
		recAnimList.setCellRenderer(recRenderer);
	}

	protected JPanel getAnimMapPanel() {
		JPanel animMapPanel = new JPanel(new MigLayout("ins 0, fill, wrap 2", "[sgx anim][sgx anim]", "[][grow][]"));
		JButton autoMatchAnimations = new JButton("Auto match animations");
		autoMatchAnimations.addActionListener(e -> matchAnimsByName());
		animMapPanel.add(autoMatchAnimations, "wrap");
		animMapPanel.add(new JLabel("Map motion from:"), "");
		animMapPanel.add(new JLabel("Into existing animation:"), "wrap");

		animMapPanel.add(new JScrollPane(donAnimList), "growy, growx");
		animMapPanel.add(new JScrollPane(recAnimList), "growy, growx");

//		animMapPanel.add(new JScrollPane(donAnimList), "growy, growx, gpy 200");
//		animMapPanel.add(new JScrollPane(recAnimList), "growy, growx, gpy 200, wrap");

		donAnimList.addListSelectionListener(this::donAnimationSelectionChanged);
		recAnimList.addListSelectionListener(this::recAnimationSelectionChanged);

		return animMapPanel;
	}

	private void matchAnimsByName() {
		Set<AnimShell> donAnimsToCheck = new HashSet<>();
		for (AnimShell recAnimShell : recAnimations) {
			for (AnimShell donAnimShell : donAnimations) {
				if (recAnimShell.getName().equals(donAnimShell.getName())) {
					recAnimShell.setAnimDataSrc(donAnimShell);
					donAnimsToCheck.add(donAnimShell);
					donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
					break;
				} else if (recAnimShell.getName().startsWith(donAnimShell.getName().split(" ")[0])) {
					if (recAnimShell.getAnimDataSrcAnim() == null) {
						recAnimShell.setAnimDataSrc(donAnimShell);
						donAnimsToCheck.add(donAnimShell);
						donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
					} else {
						int orgLength = recAnimShell.getAnim().getLength();
						int lengthDiffCurr = Math.abs(recAnimShell.getAnimDataSrcAnim().getLength() - orgLength);
						int lengthDiffNew = Math.abs(donAnimShell.getAnim().getLength() - orgLength);
						if (lengthDiffNew < lengthDiffCurr) {
							donAnimsToCheck.add(recAnimShell.getAnimDataSrc());
							recAnimShell.setAnimDataSrc(donAnimShell);
							donAnimsToCheck.add(donAnimShell);
							donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
						}
					}
				}
			}
		}
		fixImportType(donAnimsToCheck);
		recAnimList.repaint();

	}


	private void donAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() && donAnimList.getSelectedValue() != null) {
			recAnimList.setSelectedValue(null, false);
			scrollToRevealFirstChosen(donAnimList.getSelectedValue());
			recRenderer.setSelectedAnim(donAnimList.getSelectedValue());
		}
	}

	private void scrollToRevealFirstChosen(AnimShell animShell) {
		if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
			for (int indexOfFirst = 0; indexOfFirst < recAnimations.getSize(); indexOfFirst++) {
				if (recAnimations.get(indexOfFirst).getAnimDataSrc() == animShell) {
					Rectangle cellBounds = recAnimList.getCellBounds(indexOfFirst, indexOfFirst);
					if (cellBounds != null) {
						recAnimList.scrollRectToVisible(cellBounds);
					}
					break;
				}
			}
		}
	}

	// todo make use of "animShellsToTimeScaleInto" in AnimShell to facilitate
	//  importing into animation and importing animation at the same time
	private void recAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			AnimShell donAnimShell = donAnimList.getSelectedValue();

			Set<AnimShell> donAnimsToCheck = new HashSet<>();
			for (AnimShell as : recAnimList.getSelectedValuesList()) {
				if (as == asNewAnim && recAnimList.getSelectedValuesList().size() == 1) {
					if (donAnimShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
						donAnimShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
					} else {
						donAnimShell.setImportType(AnimShell.ImportType.IMPORT_BASIC);
					}
				} else if (as != asNewAnim){
					if (donAnimShell.getAnimDataDests().contains(as)) {
						donAnimShell.removeAnimDataDest(as);

					} else {
						donAnimShell.addAnimDataDest(as);
					}
				}
			}
			recAnimList.setSelectedValue(null, false);

			fixImportType(donAnimsToCheck);
		}
	}

	private void fixImportType(AnimShell donAnimShell) {
		if (donAnimShell.getAnimDataDests().isEmpty()) {
			donAnimShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
		} else if (donAnimShell.getAnimDataDests().size() == 1 && donAnimShell.getAnimDataDests().contains(asNewAnim)) {
			donAnimShell.setImportType(AnimShell.ImportType.IMPORT_BASIC);
		} else {
			donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
		}
	}

	private void recAnimationSelectionChanged1(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			AnimShell donAnimShell = donAnimList.getSelectedValue();
			donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);

			Set<AnimShell> donAnimsToCheck = new HashSet<>();
			for (AnimShell as : recAnimList.getSelectedValuesList()) {
				if (as.getAnimDataSrc() == donAnimShell) {
					donAnimsToCheck.add(donAnimShell);
					as.setAnimDataSrc(null);
				} else if (as != asNewAnim) {
					if (as.getAnimDataSrc() != null) {
						donAnimsToCheck.add(as.getAnimDataSrc());
					}
					as.setAnimDataSrc(donAnimShell);
				} else {
					if (asNewAnim.getAnimDataDests().contains(donAnimShell)) {
						System.out.println("import \"" + donAnimShell + "\"an new");
						asNewAnim.removeAnimDataDest(donAnimShell);
					} else {
						System.out.println("import \"" + donAnimShell + "\"an new");
						asNewAnim.addAnimDataDest(donAnimShell);
					}
					AnimShell.ImportType importType = donAnimShell.getImportType();
					if (importType != AnimShell.ImportType.IMPORT_BASIC) {
						donAnimShell.setImportType(AnimShell.ImportType.IMPORT_BASIC);
					} else {
						donAnimShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
					}
				}
			}
			recAnimList.setSelectedValue(null, false);

//			fixImportType(donAnimsToCheck);
			donAnimList.repaint();
		}
	}

	private void fixImportType(Set<AnimShell> donAnimsToCheck) {
		for (AnimShell animShell : donAnimsToCheck) {
			boolean isImp = false;
			for (AnimShell as : recAnimations) {
				isImp = as.getAnimDataSrc() == animShell;
				if (isImp) {
					break;
				}
			}
			if (!isImp) {
				animShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
			}
		}
		donAnimList.repaint();
	}

	protected Map<Sequence, Sequence> getRecToDonSequenceMap() {
		Map<Sequence, Sequence> recToDonSequenceMap = new HashMap<>(); // receiving animations to donating animations
//		for (AnimShell animShell : recAnimations) {
//			if (animShell.getAnimDataSrc() != null && animShell.getAnimDataSrc().getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
//				if (animShell == asNewAnim) {
//					recToDonSequenceMap.put(animShell.getAnimDataSrc().getAnim().deepCopy(), animShell.getAnimDataSrc().getAnim());
//				} else {
//					recToDonSequenceMap.put(animShell.getAnim(), animShell.getAnimDataSrc().getAnim());
//				}
//			}
//		}
		for (AnimShell animShell : donAnimations) {
			if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
				recToDonSequenceMap.put(animShell.getAnim().deepCopy(), animShell.getAnim());
			} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO){
				List<AnimShell> animDataDests = animShell.getAnimDataDests();
				for (AnimShell animDataDest : animDataDests){
					recToDonSequenceMap.put(animDataDest.getAnim(), animShell.getAnim());
				}
			}
		}
		return recToDonSequenceMap;
	}
//	protected Map<Sequence, Sequence> getDonToRecSequenceMap() {
//		Map<Sequence, Sequence> donToRecSequenceMap = new HashMap<>(); // donating animations to receiving animations
//		for (AnimShell animShell : recAnimations) {
//			if (animShell.getAnimDataSrc() != null && animShell.getAnimDataSrc().getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
//				if (animShell == asNewAnim) {
//					donToRecSequenceMap.put(animShell.getAnimDataSrc().getAnim().deepCopy(), animShell.getAnimDataSrc().getAnim());
//				} else {
//					donToRecSequenceMap.put(animShell.getAnim(), animShell.getAnimDataSrc().getAnim());
//				}
//			}
//		}
//		for (AnimShell animShell : donAnimations) {
//			if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
//				donToRecSequenceMap.put(animShell.getAnim(), animShell.getAnim().deepCopy());
//			} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO){
//				List<AnimShell> animDataDests = animShell.getAnimDataDests();
//				for(a)
//				donToRecSequenceMap.put(animShell.getAnim(), animDataDests.getAnim());
//			}
//		}
//		return donToRecSequenceMap;
//	}

	protected Map<Sequence, Sequence> getRecToDonSequenceMap1() {
		Map<Sequence, Sequence> recToDonSequenceMap = new HashMap<>(); // receiving animations to donating animations
		for (AnimShell animShell : recAnimations) {
			if (animShell.getAnimDataSrc() != null && animShell.getAnimDataSrc().getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
				if (animShell == asNewAnim) {
					recToDonSequenceMap.put(animShell.getAnimDataSrc().getAnim().deepCopy(), animShell.getAnimDataSrc().getAnim());
				} else {
					recToDonSequenceMap.put(animShell.getAnim(), animShell.getAnimDataSrc().getAnim());
				}
			}
		}
		for (AnimShell animShell : donAnimations) {
			if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
				recToDonSequenceMap.put(animShell.getAnim().deepCopy(), animShell.getAnim());
			}
		}
		return recToDonSequenceMap;
	}

	protected Map<IdObject, IdObject> getChainMap(Bone mapToBone, EditableModel mapToModel, Bone mapFromBone, EditableModel mapFromModel, int depth, boolean presentParent){
		return new BoneChainMapWizard(this, mapToModel, mapFromModel).getChainMap(mapToBone, mapFromBone, depth, presentParent);
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
	                                                     EditableModel modelForPanel, boolean presentParent) {
		Map<IdObject, IdObject> boneChainSubMap = new HashMap<>();
		JPanel objectMappingPanel = new JPanel(new MigLayout());
		for (IdObject idObjectForPanel : idObjectsForPanel) {
			JComboBox<IdObjectShell<?>> boneChooserBox = getBoneChooserBox(idObjectForPanel, idObjectsForComboBox, modelForComboBox, (o -> boneChainSubMap.put(idObjectForPanel, o)), presentParent);
			if (boneChooserBox.getModel().getSize() == 2) {
				boneChainSubMap.put(idObjectForPanel, boneChooserBox.getModel().getElementAt(1).getIdObject());
			} else if (boneChooserBox.getModel().getSize() > 2 || presentParent) {
				objectMappingPanel.add(new JLabel(iconHandler.getImageIcon(idObjectForPanel, modelForPanel)));
				objectMappingPanel.add(new JLabel(idObjectForPanel.getName()));
				objectMappingPanel.add(boneChooserBox, "wrap");
			}
		}
		if (objectMappingPanel.getComponentCount() > 0) {
			JScrollPane scrollPane = new JScrollPane(objectMappingPanel);
			scrollPane.setMaximumSize(ScreenInfo.getSmallWindow());
			JOptionPane.showConfirmDialog(this, scrollPane, "Map Bones", JOptionPane.OK_CANCEL_OPTION);
		}

		return boneChainSubMap;
	}

	private JComboBox<IdObjectShell<?>> getBoneChooserBox(IdObject idObjectDest, List<IdObject> idObjectsForComboBox, EditableModel modelForComboBox, Consumer<IdObject> idObjectConsumer, boolean presentParent) {
		JComboBox<IdObjectShell<?>> comboBox = new JComboBox<>();
		comboBox.addItem(new IdObjectShell<>(null));
//		if (presentParent && idObjectDest.getParent() != null) {
//			comboBox.addItem(new IdObjectShell<>(idObjectDest.getParent()));
//		}
		if (presentParent && !idObjectsForComboBox.isEmpty() && idObjectsForComboBox.get(0).getParent() != null) {
			comboBox.addItem(new IdObjectShell<>(idObjectsForComboBox.get(0).getParent()));
		}
		IdObjectShell<?> sameNameObject = null;
		int lastMatch = 20;
		String destName = idObjectDest.getName();
		for (IdObject idObject : idObjectsForComboBox) {
			if (idObject instanceof Bone && idObjectDest instanceof Bone || idObject.getClass() == idObjectDest.getClass()) {
				IdObjectShell<?> newObjectShell = new IdObjectShell<>(idObject);
				comboBox.addItem(newObjectShell);
				String name = idObject.getName();
				if (name.equals(destName)) {
					sameNameObject = newObjectShell;
					lastMatch = Math.abs(sameNameObject.getIdObject().getName().compareTo(destName));
				} else if (-lastMatch < name.compareTo(destName) && name.compareTo(destName) < lastMatch) {
					String[] namsSplit = name.split("_");
					String[] destSplit = destName.split("_");
					boolean match = true;
					for (int i = 0; i < namsSplit.length && i < destSplit.length; i++) {
						if (!namsSplit[i].equals(destSplit[i])) {
							match = false;
							break;
						}
					}
					if (match) {
						sameNameObject = newObjectShell;
						lastMatch = Math.abs(sameNameObject.getIdObject().getName().compareTo(destName));
					}
				}
			}
		}
		comboBox.setRenderer(new ObjectShellListCellRenderer(modelForComboBox, null));
		comboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				idObjectConsumer.accept(((IdObjectShell<?>) e.getItem()).getIdObject());
			}
		});
		if (sameNameObject == null) {
			comboBox.setSelectedIndex(0);
		} else {
			comboBox.setSelectedItem(sameNameObject);
		}
		return comboBox;
	}


	protected Set<Geoset> getNewGeosets(Set<Bone> selectedBones) {
		Set<Bone> extraBones = new HashSet<>();
		Set<Geoset> newGeosets = getCopiedGeosets(selectedBones, extraBones);

		for (Geoset newGeoset : newGeosets) {
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			if (boneOption == BoneOption.importBonesExtra) {
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

	private Set<Geoset> getCopiedGeosets(Set<Bone> selectedBones, Set<Bone> extraBones) {
		Set<Geoset> newGeosets = new HashSet<>();
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


	protected boolean isBones(IdObject idObject1, IdObject idObject2) {
		return idObject1 instanceof Bone && idObject2 instanceof Bone;
	}

	private void mergeUnnecessaryBonesWithHelpers(EditableModel model) {
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
