package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelHolderThing {
	public EditableModel receivingModel;
	public EditableModel donatingModel;

	// Geosets
	public JTabbedPane geosetTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

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

	// Matrices
	public JTabbedPane geosetAnimTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	// Objects
	public IterableListModel<ObjectShell> donModObjectShells = new IterableListModel<>();
	public JList<ObjectShell> donModObjectJList = new JList<>(donModObjectShells);


	// Visibility
	public JList<VisibilityPanel> visTabs = new JList<>();

	public IterableListModel<VisibilityPanel> visComponents;
	public ArrayList<VisibilityPanel> allVisShellPanes = new ArrayList<>();

	public Set<BoneShell> futureBoneListExQuickLookupSet = new HashSet<>();
	public ArrayList<VisibilityShell> allVisShells;

	public ArrayList<Object> recModVisSourcesOld;
	public ArrayList<Object> donModVisSourcesNew;
	public BoneShellListCellRenderer boneShellRenderer;

	public ModelViewManager recModelManager;
	public ModelViewManager donModelManager;
	ChangeListener changeListener;


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;
		changeListener = getChangeListener();

		initLists();

		recModelManager = new ModelViewManager(receivingModel);
		donModelManager = new ModelViewManager(donatingModel);
		boneShellRenderer = new BoneShellListCellRenderer(recModelManager, donModelManager);
	}

	long totalAddTime;
	long addCount;
	long totalRemoveTime;
	long removeCount;

	public IterableListModel<VisibilityPanel> visibilityList() {
		VisibilityPanel selection = visTabs.getSelectedValue();
		visComponents.clear();
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			for (final Layer x : gp.getSelectedMaterial().getLayers()) {
				final VisibilityPanel vs = visPaneFromObject(x);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			if (gp.doImport.isSelected()) {
				final Geoset x = gp.geoset;
				final VisibilityPanel vs = visPaneFromObject(x);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		// The current's
		final EditableModel model = receivingModel;
		createAndAddVisComp(model.getLights());
		createAndAddVisComp(model.getAttachments());
		createAndAddVisComp(model.getParticleEmitters());
		createAndAddVisComp(model.getParticleEmitter2s());
		createAndAddVisComp(model.getRibbonEmitters());
		createAndAddVisComp(model.getPopcornEmitters());

		for (ObjectShell op : donModObjectShells) {
			if (op.getShouldImport() && (op.getIdObject() != null))
			// we don't touch camera "object" panels (which aren't idobjects)
			{
				final VisibilityPanel vs = visPaneFromObject(op.getIdObject());
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		visTabs.setSelectedValue(selection, true);
		return visComponents;
	}

	public void createAndAddVisComp(List<? extends IdObject> idObjects) {
		for (IdObject x : idObjects) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
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
//		for (final BoneShell b : donModBones) {
//			if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
//				if (!futureBoneList.contains(b)) {
//					futureBoneList.addElement(b);
//				}
//			} else {
//				futureBoneList.removeElement(b);
//			}
//		}
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

	public VisibilityPanel visPaneFromObject(final Object o) {
		for (final VisibilityPanel vp : allVisShellPanes) {
			if (vp.sourceShell.source == o) {
				return vp;
			}
		}
		return null;
	}


	public void importAllGeos(boolean b) {
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel geoPanel = (GeosetPanel) geosetTabs.getComponentAt(i);
			geoPanel.setSelected(b);
		}
	}

	public void uncheckAllAnims(boolean b) {
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
			aniPanel.setSelected(b);
		}
	}

	public void setImportStatusForAllBones(int selsctionIndex) {
		for (BoneShell bonePanel : donModBoneShells) {
			bonePanel.setImportStatus(selsctionIndex);
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

	public BoneShell getPanelOf(final Bone b) {
		return donModBoneShellBiMap.get(b);
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

	private void initLists() {

		for (Bone bone : receivingModel.getBones()) {
			BoneShell bs = new BoneShell(bone);
			bs.setModelName(receivingModel.getName());
			bs.setShowClass(true);
			recModBones.add(bs);
			recModBoneShellBiMap.put(bone, bs);
		}
		recModBoneShells.addAll(recModBones);

		for (Helper helper : receivingModel.getHelpers()) {
			BoneShell bs = new BoneShell(helper);
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
			BoneShell bs = new BoneShell(bone);
			bs.setModelName(donatingModel.getName());
			bs.setShowClass(true);
			donModBones.add(bs);
			donModBoneShellBiMap.put(bone, bs);
		}
		donModBoneShells.addAll(donModBones);

		for (Helper helper : donatingModel.getHelpers()) {
			BoneShell bs = new BoneShell(helper);
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

}
