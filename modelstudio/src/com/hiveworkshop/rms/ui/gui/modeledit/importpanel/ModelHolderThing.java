package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.util.*;

public class ModelHolderThing {
	public EditableModel receivingModel;
	public EditableModel donatingModel;

	// Geosets
	public JTabbedPane geosetTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	// Animation
	public JCheckBox clearExistingAnims;
	public JTabbedPane animTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
	public IterableListModel<AnimShell> existingAnims;

	// Bones
	public JCheckBox clearExistingBones;
	public IterableListModel<BonePanel> bonePanels = new IterableListModel<>();
	public Map<Bone, BonePanel> boneToPanel = new HashMap<>();
	public JList<BonePanel> boneTabs = new JList<>(bonePanels);

	public IterableListModel<BoneShell> existingBones;

	// Matrices
	public JTabbedPane geosetAnimTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	public IterableListModel<BoneShell> futureBoneList = new IterableListModel<>();
	public ArrayList<BoneShell> recModOldBones;
	public ArrayList<BoneShell> donModNewBones;

	// Objects
//	public JPanel objectsPanel = new JPanel();
	public IterableListModel<ObjectPanel> objectPanels = new IterableListModel<>();
	public JList<ObjectPanel> objectTabs = new JList<>(objectPanels);


	// Visibility
	public JList<VisibilityPanel> visTabs = new JList<>();

	public IterableListModel<VisibilityPanel> visComponents;
	public ArrayList<VisibilityPanel> allVisShellPanes = new ArrayList<>();

	public IterableListModel<BoneShell> futureBoneListEx = new IterableListModel<>();
	public List<IterableListModel<BoneShell>> futureBoneListExFixableItems = new ArrayList<>();

	public ArrayList<BoneShell> recModOldHelpers;
	public ArrayList<BoneShell> donModNewHelpers;

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

		for (ObjectPanel op : objectPanels) {
			if (op.doImport.isSelected() && (op.object != null))
			// we don't touch camera "object" panels (which aren't idobjects)
			{
				final VisibilityPanel vs = visPaneFromObject(op.object);
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
		if (recModOldBones == null) {
			recModOldBones = new ArrayList<>();
			donModNewBones = new ArrayList<>();
			final List<Bone> recOldBonesRefs = receivingModel.getBones();
			for (final Bone b : recOldBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = receivingModel.getName();
				recModOldBones.add(bs);
			}
			final List<Bone> donNewBonesRefs = donatingModel.getBones();
			for (final Bone b : donNewBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = donatingModel.getName();
				bs.panel = getPanelOf(b);
				donModNewBones.add(bs);
			}
		}
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : recModOldBones) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			}
		} else {
			for (final BoneShell b : recModOldBones) {
				if (futureBoneList.contains(b)) {
					futureBoneList.removeElement(b);
				}
			}
		}
		for (final BoneShell b : donModNewBones) {
			if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			} else {
				if (futureBoneList.contains(b)) {
					futureBoneList.removeElement(b);
				}
			}
		}
		return futureBoneList;
	}

	public IterableListModel<BoneShell> getFutureBoneListExtended(final boolean newSnapshot) {
		totalAddTime = 0;
		addCount = 0;
		totalRemoveTime = 0;
		removeCount = 0;
		if (recModOldHelpers == null) {
			recModOldHelpers = new ArrayList<>();
			donModNewHelpers = new ArrayList<>();
			List<? extends Bone> RecOldBonesRefs = receivingModel.getBones();
			for (final Bone b : RecOldBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = receivingModel.getName();
				bs.showClass = true;
				recModOldHelpers.add(bs);
			}
			List<? extends Bone> recOldHelpersRefs = receivingModel.getHelpers();
			for (final Bone b : recOldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = receivingModel.getName();
				bs.showClass = true;
				recModOldHelpers.add(bs);
			}
			List<? extends Bone> donNewBonesRefs = donatingModel.getBones();
			for (final Bone b : donNewBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = donatingModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				donModNewHelpers.add(bs);
			}
			List<? extends Bone> donNewHelpersRefs = donatingModel.getHelpers();
			for (final Bone b : donNewHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = donatingModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				donModNewHelpers.add(bs);
			}
		}
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : recModOldHelpers) {
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
			for (final BoneShell b : recModOldHelpers) {
				if (futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += (endTime - startTime);
					removeCount++;
				}
			}
		}
		for (final BoneShell b : donModNewHelpers) {
			b.panel = getPanelOf(b.bone);
			if (b.panel != null) {
				if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
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
		// We CANT call clear, we have to preserve
		// the parent list
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
		for (BonePanel bonePanel : bonePanels) {
			bonePanel.setSelectedIndex(selsctionIndex);
		}
	}

	public void importAllObjs(boolean b) {
		for (ObjectPanel objectPanel : objectPanels) {
			objectPanel.doImport.setSelected(b);
		}
	}

	public void selSimButton() {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			vPanel.selectSimilarOptions();
		}
	}

	public BonePanel getPanelOf(final Bone b) {
		return boneToPanel.get(b);
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
}
