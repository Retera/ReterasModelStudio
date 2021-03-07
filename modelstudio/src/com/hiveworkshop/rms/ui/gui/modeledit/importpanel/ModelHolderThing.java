package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.util.*;

public class ModelHolderThing {
	public EditableModel receivingModel;
	public EditableModel donatingModel;

	// Geosets
	public IterableListModel<GeosetShell> recModGeoShells = new IterableListModel<>();
	public IterableListModel<GeosetShell> donModGeoShells = new IterableListModel<>();
	public IterableListModel<GeosetShell> allGeoShells = new IterableListModel<>();
	public IterableListModel<Material> recModMaterials = new IterableListModel<>();
	public IterableListModel<Material> donModMaterials = new IterableListModel<>();
	public IterableListModel<Material> allMaterials = new IterableListModel<>();

	// Animation
	public JCheckBox clearExistingAnims;
	public JTabbedPane animTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
	public IterableListModel<AnimShell> recModOrgAnims;

	// Bones
	public JCheckBox clearExistingBones;
	public IterableListModel<BoneShell> donModBoneShells = new IterableListModel<>();
	public JList<BoneShell> donModBoneShellJList = new JList<>(donModBoneShells);
	public IterableListModel<BoneShell> recModBoneShells = new IterableListModel<>();
	public ArrayList<BoneShell> recModBones = new ArrayList<>();
	public ArrayList<BoneShell> donModBones = new ArrayList<>();
	public ArrayList<BoneShell> recModHelpers = new ArrayList<>();
	public ArrayList<BoneShell> donModHelpers = new ArrayList<>();
	public IterableListModel<BoneShell> futureBoneListEx = new IterableListModel<>();
	public List<IterableListModel<BoneShell>> futureBoneListExFixableItems = new ArrayList<>();
	public IterableListModel<BoneShell> futureBoneList = new IterableListModel<>();
	BiMap<IdObject, BoneShell> recModBoneShellBiMap = new BiMap<>();
	BiMap<IdObject, BoneShell> donModBoneShellBiMap = new BiMap<>();

	public Set<BoneShell> futureBoneListExQuickLookupSet = new HashSet<>();


	// Objects
	public IterableListModel<ObjectShell> donModObjectShells = new IterableListModel<>();
	public JList<ObjectShell> donModObjectJList = new JList<>(donModObjectShells);
	public IterableListModel<ObjectShell> recModObjectShells = new IterableListModel<>();
//	public JList<ObjectShell> recModObjectJList = new JList<>(recModObjectShells);


	// Visibility

	public IterableListModel<VisibilityShell> donModVisibilityShells = new IterableListModel<>();
	//	public JList<VisibilityShell> donModVisibilityJList = new JList<>(donModVisibilityShells);
	public IterableListModel<VisibilityShell> recModVisibilityShells = new IterableListModel<>();
	//	public ArrayList<VisibilityShell> allVisShells = new ArrayList<>();
//	public ArrayList<Object> recModVisSourcesOld = new ArrayList<>();
	public IterableListModel<VisibilityShell> donModVisSourcesNew = new IterableListModel<>();
	//	public ArrayList<Object> donModVisSourcesNew = new ArrayList<>();
	public BoneShellListCellRenderer boneShellRenderer;
	//	public JList<VisibilityShell> recModVisibilityJList = new JList<>(recModVisibilityShells);
	BiMap<VisibilitySource, VisibilityShell> recModVisShellBiMap = new BiMap<>();

	public IterableListModel<VisibilityPanel> futureVisComponents = new IterableListModel<>();
	public JList<VisibilityPanel> visTabs = new JList<>(futureVisComponents);
	public ArrayList<VisibilityPanel> allVisShellPanes = new ArrayList<>();


	public IterableListModel<VisibilityShell> visShellsComps = new IterableListModel<>();
	public JList<VisibilityShell> visShellJList = new JList<>(visShellsComps);
	public ArrayList<VisibilityShell> allVisShellss = new ArrayList<>();
	BiMap<VisibilitySource, VisibilityShell> donModVisShellBiMap = new BiMap<>();
	BiMap<VisibilitySource, VisibilityShell> allVisShellBiMap = new BiMap<>();

	public ModelViewManager recModelManager;
	public ModelViewManager donModelManager;
	ChangeListener changeListener;


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;
		changeListener = getChangeListener();

		initBoneHelperLists();
		initiateGeosetLists();
		initObjectLists();

		recModelManager = new ModelViewManager(receivingModel);
		donModelManager = new ModelViewManager(donatingModel);
		boneShellRenderer = new BoneShellListCellRenderer(recModelManager, donModelManager);
	}

	public IterableListModel<BoneShell> getFutureBoneList() {
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : recModBones) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			}
		} else {
			futureBoneList.removeAll(recModBones);
//			for (final BoneShell b : recModBones) {
//				if (futureBoneList.contains(b)) {
//					futureBoneList.removeElement(b);
//				}
//			}
		}
		for (final BoneShell b : donModBones) {
			if (b.getImportStatus() == 0) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			} else {
				futureBoneList.removeElement(b);
			}
		}
		return futureBoneList;
	}

	public IterableListModel<BoneShell> getFutureBoneListExtended(final boolean newSnapshot) {
		totalAddTime = 0;
		addCount = 0;
		totalRemoveTime = 0;
		removeCount = 0;

		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : recModBoneShells) {
				if (!futureBoneListExQuickLookupSet.contains(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.addElement(b);
					final long endTime = System.nanoTime();
					totalAddTime += (endTime - startTime);
					addCount++;
					futureBoneListExQuickLookupSet.add(b);
				}
			}
		} else {
			for (final BoneShell b : recModBoneShells) {
				if (futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += (endTime - startTime);
					removeCount++;
				}
			}
		}
		for (final BoneShell b : donModBoneShells) {
			if (b.getImportStatus() == 0) {
				if (!futureBoneListExQuickLookupSet.contains(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.addElement(b);
					final long endTime = System.nanoTime();
					totalAddTime += (endTime - startTime);
					addCount++;
					futureBoneListExQuickLookupSet.add(b);
				}
			} else {
				if (futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += (endTime - startTime);
					removeCount++;
				}
			}

		}

		if (addCount != 0) {
			System.out.println("average add time: " + (totalAddTime / addCount));
			System.out.println("add count: " + addCount);
		}
		if (removeCount != 0) {
			System.out.println("average remove time: " + (totalRemoveTime / removeCount));
			System.out.println("remove count: " + removeCount);
		}

		final IterableListModel<BoneShell> listModelToReturn;
		if (newSnapshot || futureBoneListExFixableItems.isEmpty()) {
			final IterableListModel<BoneShell> futureBoneListReplica = new IterableListModel<>();
			futureBoneListExFixableItems.add(futureBoneListReplica);
			listModelToReturn = futureBoneListReplica;
		} else {
			listModelToReturn = futureBoneListExFixableItems.get(0);
		}
		// We CANT call clear, we have to preserve the parent list
		for (final IterableListModel<BoneShell> model : futureBoneListExFixableItems) {
			// clean things that should not be there
			for (BoneShell previousElement : model) {
				if (!futureBoneListExQuickLookupSet.contains(previousElement)) {
					model.remove(previousElement);
				}
			}
			// add back things who should be there
			for (BoneShell elementAt : futureBoneListEx) {
				if (!model.contains(elementAt)) {
					model.addElement(elementAt);
				}
			}
		}
		return listModelToReturn;
	}

	long totalAddTime;

	public void uncheckAllAnims(boolean b) {
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
			aniPanel.setSelected(b);
		}
	}

	public void setImportStatusForAllBones(int selectionIndex) {
		Map<String, BoneShell> nameMap = new HashMap<>();
		if (selectionIndex == 1) {
			for (BoneShell boneShell : recModBoneShells) {
				nameMap.put(boneShell.getName(), boneShell);
			}
		}
		for (BoneShell boneShell : donModBoneShells) {
			boneShell.setImportStatus(selectionIndex);
			if (selectionIndex == 1 && nameMap.containsKey(boneShell.getName())) {
				nameMap.get(boneShell.getName()).setImportBoneShell(boneShell);
			} else if (selectionIndex != 1) {
				boneShell.setImportBoneShell(null);
			}
		}
	}

	public void importAllObjs(boolean b) {
		for (ObjectShell objectPanel : donModObjectShells) {
			objectPanel.setShouldImport(b);
		}
	}

	public void selSimButton() {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			vPanel.selectSimilarOptions();
		}
	}


	public ChangeListener getChangeListener() {
		return e -> {
			((AnimPanel) animTabs.getSelectedComponent()).updateSelectionPicks();
			getFutureBoneList();
			getFutureBoneListExtended(false);
			visibilityList();
//				repaint();
		};
	}

	public ChangeListener getDaChangeListener() {
		return changeListener;
	}

	long addCount;
	long totalRemoveTime;
	long removeCount;

	public void importAllGeos(boolean b) {
		for (GeosetShell geoShell : allGeoShells) {
			if (geoShell.isFromDonating()) {
				geoShell.setDoImport(b);
			}
		}
	}

	private void initBoneHelperLists() {

		for (Bone bone : receivingModel.getBones()) {
			BoneShell bs = new BoneShell(bone, false);
			bs.setModelName(receivingModel.getName());
			bs.setShowClass(true);
			recModBones.add(bs);
			recModBoneShellBiMap.put(bone, bs);
		}
		recModBoneShells.addAll(recModBones);

		for (Helper helper : receivingModel.getHelpers()) {
			BoneShell bs = new BoneShell(helper, false);
			bs.setModelName(receivingModel.getName());
			bs.setShowClass(true);
			recModHelpers.add(bs);
			recModBoneShellBiMap.put(helper, bs);
		}
		recModBoneShells.addAll(recModHelpers);

		for (BoneShell bs : recModBoneShellBiMap.values()) {
			bs.setParentBs(recModBoneShellBiMap);
		}


		for (Bone bone : donatingModel.getBones()) {
			BoneShell bs = new BoneShell(bone, true);
			bs.setModelName(donatingModel.getName());
			bs.setShowClass(true);
			donModBones.add(bs);
			donModBoneShellBiMap.put(bone, bs);
		}
		donModBoneShells.addAll(donModBones);

		for (Helper helper : donatingModel.getHelpers()) {
			BoneShell bs = new BoneShell(helper, true);
			bs.setModelName(donatingModel.getName());
			bs.setShowClass(true);
			donModHelpers.add(bs);
			donModBoneShellBiMap.put(helper, bs);
		}
		donModBoneShells.addAll(donModHelpers);

		for (BoneShell bs : donModBoneShellBiMap.values()) {
			bs.setParentBs(donModBoneShellBiMap);
		}
	}

	private void initiateGeosetLists() {
		for (Geoset geoset : receivingModel.getGeosets()) {
			GeosetShell geoShell = new GeosetShell(geoset, receivingModel, false);
			geoShell.setMatrixShells(createMatrixShells(geoset, recModBoneShellBiMap, false));
			recModGeoShells.addElement(geoShell);
		}
		allGeoShells.addAll(recModGeoShells);

		for (Geoset geoset : donatingModel.getGeosets()) {
			GeosetShell geoShell = new GeosetShell(geoset, donatingModel, true);
			geoShell.setMatrixShells(createMatrixShells(geoset, donModBoneShellBiMap, true));
			donModGeoShells.addElement(geoShell);
		}
		allGeoShells.addAll(donModGeoShells);

		for (Material material : receivingModel.getMaterials()) {
			recModMaterials.addElement(material);
		}
		allMaterials.addAll(recModMaterials);

		for (Material material : donatingModel.getMaterials()) {
			donModMaterials.addElement(material);
		}
		allMaterials.addAll(donModMaterials);
	}

	private IterableListModel<MatrixShell> createMatrixShells(Geoset geoset, BiMap<IdObject, BoneShell> boneShells, boolean isFromDonating) {
		IterableListModel<MatrixShell> matrixShells = new IterableListModel<>();
		for (final Matrix matrix : geoset.getMatrix()) {
			ArrayList<BoneShell> orgBones = new ArrayList<>();
			// For look to find similarly named stuff and add it
			for (Bone bone : matrix.getBones()) {
				if (boneShells.get(bone) != null) {
					orgBones.add(boneShells.get(bone));
				}
			}

			final MatrixShell ms = new MatrixShell(matrix, orgBones, isFromDonating);
			matrixShells.addElement(ms);
		}
		return matrixShells;
	}

	private void initObjectLists() {

		List<VisibilityShell> donGeosetVisSources = new ArrayList<>();
		for (GeosetShell geoShell : donModGeoShells) {
			for (final Layer x : geoShell.getMaterial().getLayers()) {
				VisibilityShell vs = new VisibilityShell(x, donatingModel, true);
				donModVisibilityShells.addElement(vs);
				donModVisShellBiMap.put(x, vs);
				allVisShellBiMap.put(x, vs);

			}
			VisibilityShell vs = new VisibilityShell(geoShell.getGeoset(), donatingModel, true);
			donGeosetVisSources.add(vs);
			donModVisShellBiMap.put(geoShell.getGeoset(), vs);
			allVisShellBiMap.put(geoShell.getGeoset(), vs);
		}
		donModVisibilityShells.addAll(donGeosetVisSources);

		for (IdObject obj : donatingModel.getIdObjects()) {
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {
				ObjectShell objectShell = new ObjectShell(obj, true);
				donModObjectShells.addElement(objectShell);
				if (obj.getClass() != CollisionShape.class && obj.getClass() != EventObject.class) {
					VisibilityShell vs = new VisibilityShell(obj, donatingModel, true);
					donModVisibilityShells.addElement(vs);
					donModVisShellBiMap.put(obj, vs);
					allVisShellBiMap.put(obj, vs);
				}
			}
		}
		for (Camera obj : donatingModel.getCameras()) {
			ObjectShell objectShell = new ObjectShell(obj, true);
			donModObjectShells.addElement(objectShell);
		}
		for (ObjectShell os : donModObjectShells) {
			os.setParentBs(donModBoneShellBiMap);
		}

		List<VisibilityShell> recGeosetVisSources = new ArrayList<>();
		for (GeosetShell geoShell : recModGeoShells) {
			for (final Layer x : geoShell.getMaterial().getLayers()) {
				VisibilityShell vs = new VisibilityShell(x, receivingModel, true);
				recModVisibilityShells.addElement(vs);
				recModVisShellBiMap.put(x, vs);
				allVisShellBiMap.put(x, vs);
			}
			VisibilityShell vs = new VisibilityShell(geoShell.getGeoset(), receivingModel, true);
			recGeosetVisSources.add(vs);
			recModVisShellBiMap.put(geoShell.getGeoset(), vs);
			allVisShellBiMap.put(geoShell.getGeoset(), vs);
		}
		recModVisibilityShells.addAll(recGeosetVisSources);

		for (IdObject obj : receivingModel.getIdObjects()) {
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {
				ObjectShell objectShell = new ObjectShell(obj, true);
				recModObjectShells.addElement(objectShell);
				if (obj.getClass() != CollisionShape.class && obj.getClass() != EventObject.class) {
					VisibilityShell vs = new VisibilityShell(obj, receivingModel, true);
					recModVisibilityShells.addElement(vs);
					recModVisShellBiMap.put(obj, vs);
					allVisShellBiMap.put(obj, vs);
				}
			}
		}
		for (Camera obj : receivingModel.getCameras()) {
			ObjectShell objectShell = new ObjectShell(obj, true);
			recModObjectShells.addElement(objectShell);
		}

		for (ObjectShell os : recModObjectShells) {
			os.setParentBs(recModBoneShellBiMap);
		}

	}

	public IterableListModel<VisibilityPanel> visibilityList() {
		VisibilityPanel selection = visTabs.getSelectedValue();
		futureVisComponents.clear();
		List<VisibilityPanel> geosetVisSources = new ArrayList<>();
		for (GeosetShell geoShell : allGeoShells) {
			for (final Layer x : geoShell.getMaterial().getLayers()) {
				VisibilityPanel vs = visPaneFromObject(x);
				if (!futureVisComponents.contains(vs) && (vs != null)) {
					futureVisComponents.addElement(vs);
				}
			}
			if (geoShell.isDoImport()) {
				VisibilityPanel vs = visPaneFromObject(geoShell.getGeoset());
				if (!futureVisComponents.contains(vs) && !geosetVisSources.contains(vs) && (vs != null)) {
					geosetVisSources.add(vs);
				}
			}
		}
		futureVisComponents.addAll(geosetVisSources);

		// The current's
		final EditableModel model = receivingModel;
		fetchAndAddVisComp(model.getLights());
		fetchAndAddVisComp(model.getAttachments());
		fetchAndAddVisComp(model.getParticleEmitters());
		fetchAndAddVisComp(model.getParticleEmitter2s());
		fetchAndAddVisComp(model.getRibbonEmitters());
		fetchAndAddVisComp(model.getPopcornEmitters());

		for (ObjectShell op : donModObjectShells) {
			if (op.getShouldImport() && (op.getIdObject() != null))
			// we don't touch camera "object" panels (which aren't idobjects)
			{
				final VisibilityPanel vs = visPaneFromObject(op.getIdObject());
				if (!futureVisComponents.contains(vs) && (vs != null)) {
					futureVisComponents.addElement(vs);
				}
			}
		}
		visTabs.setSelectedValue(selection, true);
		return futureVisComponents;
	}

	public void fetchAndAddVisComp(List<? extends IdObject> idObjects) {
		for (IdObject x : idObjects) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!futureVisComponents.contains(vs) && (vs != null)) {
				futureVisComponents.addElement(vs);
			}
		}
	}

	public VisibilityPanel visPaneFromObject(final Object o) {
		for (final VisibilityPanel vp : allVisShellPanes) {
			if (vp.sourceShell.source == o) {
				return vp;
			}
		}
		return null;
	}

	public VisibilityShell visShellFromObject(VisibilitySource vs) {
		return allVisShellBiMap.get(vs);
	}
}
