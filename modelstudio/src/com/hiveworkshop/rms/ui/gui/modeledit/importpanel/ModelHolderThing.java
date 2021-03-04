package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
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
	public JPanel bonesPanel = new JPanel();
	public JCheckBox clearExistingBones;
	public IterableListModel<BonePanel> bonePanels = new IterableListModel<>();
	public Map<Bone, BonePanel> boneToPanel = new HashMap<>();
	public JList<BonePanel> boneTabs = new JList<>(bonePanels);
	public CardLayout boneCardLayout = new CardLayout();
	public JPanel bonePanelCards = new JPanel(boneCardLayout);
	public JPanel blankPane = new JPanel();
	public MultiBonePanel multiBonePane;
	public IterableListModel<BoneShell> existingBones;

	// Matrices
	public JPanel geosetAnimPanel = new JPanel();
	public JTabbedPane geosetAnimTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	public IterableListModel<BoneShell> futureBoneList = new IterableListModel<>();
	public java.util.List<BoneShell> oldBones;
	public java.util.List<BoneShell> newBones;

	public JCheckBox displayParents = new JCheckBox("Display parent names");
	public JButton allMatrOriginal = new JButton("Reset all Matrices");
	public JButton allMatrSameName = new JButton("Set all to available, original names");

	// Objects
//	public JPanel objectsPanel = new JPanel();
	public IterableListModel<ObjectPanel> objectPanels = new IterableListModel<>();
	public JList<ObjectPanel> objectTabs = new JList<>(objectPanels);
	public CardLayout objectCardLayout = new CardLayout();
	public JPanel objectPanelCards = new JPanel(objectCardLayout);
	public MultiObjectPanel multiObjectPane;


	// Visibility
	public JList<VisibilityPanel> visTabs = new JList<>();
	public JScrollPane visTabsPane = new JScrollPane(visTabs);
	public CardLayout visCardLayout = new CardLayout();
	public JPanel visPanelCards = new JPanel(visCardLayout);
	public MultiVisibilityPanel multiVisPanel;

	public IterableListModel<VisibilityPanel> visComponents;
	public ArrayList<VisibilityPanel> allVisShellPanes = new ArrayList<>();

	public IterableListModel<BoneShell> futureBoneListEx = new IterableListModel<>();
	public List<IterableListModel<BoneShell>> futureBoneListExFixableItems = new ArrayList<>();
	public ArrayList<BoneShell> oldHelpers;
	public ArrayList<BoneShell> newHelpers;
	public Set<BoneShell> futureBoneListExQuickLookupSet = new HashSet<>();
	public ArrayList<VisibilityShell> allVisShells;
	public ArrayList<Object> visSourcesOld;
	public ArrayList<Object> visSourcesNew;
	public BoneShellListCellRenderer boneShellRenderer;


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;
	}

	public void initVisibilityList() {
		visSourcesOld = new ArrayList<>();
		visSourcesNew = new ArrayList<>();
		allVisShells = new ArrayList<>();
		EditableModel model = receivingModel;
		final List tempList = new ArrayList();
		for (final Material mat : model.getMaterials()) {
			for (final Layer lay : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(lay, model);
				if (!tempList.contains(lay)) {
					tempList.add(lay);
					allVisShells.add(vs);
				}
			}
		}
		for (final Geoset ga : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(ga, model);
			if (!tempList.contains(ga)) {
				tempList.add(ga);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getLights()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getAttachments()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitter2s()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getRibbonEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getPopcornEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		model = donatingModel;
		for (final Material mat : model.getMaterials()) {
			for (final Layer x : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(x, model);
				if (!tempList.contains(x)) {
					tempList.add(x);
					allVisShells.add(vs);
				}
			}
		}
		for (final Geoset x : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getLights()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getAttachments()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitter2s()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getRibbonEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.getPopcornEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}

		System.out.println("allVisShells:");
		for (final VisibilityShell vs : allVisShells) {
			System.out.println(vs.source.getName());
		}

		System.out.println("new/old:");
		for (final Object o : receivingModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				visSourcesOld.add(shellFromObject(o));
				System.out.println(shellFromObject(o).source.getName());
			} else {
				visSourcesOld.add(shellFromObject(((GeosetAnim) o).getGeoset()));
				System.out.println(shellFromObject(((GeosetAnim) o).getGeoset()).source.getName());
			}
		}
		visSourcesOld.add(VisibilityPanel.NOTVISIBLE);
		visSourcesOld.add(VisibilityPanel.VISIBLE);
		for (final Object o : donatingModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				visSourcesNew.add(shellFromObject(o));
			} else {
				visSourcesNew.add(shellFromObject(((GeosetAnim) o).getGeoset()));
			}
		}
		visSourcesNew.add(VisibilityPanel.NOTVISIBLE);
		visSourcesNew.add(VisibilityPanel.VISIBLE);
		visComponents = new IterableListModel<>();
	}

	public IterableListModel<VisibilityPanel> visibilityList() {
		final Object selection = visTabs.getSelectedValue();
		visComponents.clear();
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			for (final Layer l : gp.getSelectedMaterial().getLayers()) {
				final VisibilityPanel vs = visPaneFromObject(l);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			if (gp.doImport.isSelected()) {
				final Geoset ga = gp.geoset;
				final VisibilityPanel vs = visPaneFromObject(ga);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		// The current's
		final EditableModel model = receivingModel;
		for (final Object x : model.getLights()) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.getAttachments()) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.getParticleEmitters()) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.getParticleEmitter2s()) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.getRibbonEmitters()) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.getPopcornEmitters()) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}

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

	public IterableListModel<BoneShell> getFutureBoneList() {
		if (oldBones == null) {
			oldBones = new ArrayList<>();
			newBones = new ArrayList<>();
			final List<Bone> oldBonesRefs = receivingModel.getBones();
			for (final Bone b : oldBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = receivingModel.getName();
				oldBones.add(bs);
			}
			final List<Bone> newBonesRefs = donatingModel.getBones();
			for (final Bone b : newBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = donatingModel.getName();
				bs.panel = getPanelOf(b);
				newBones.add(bs);
			}
		}
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : oldBones) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			}
		} else {
			for (final BoneShell b : oldBones) {
				if (futureBoneList.contains(b)) {
					futureBoneList.removeElement(b);
				}
			}
		}
		for (final BoneShell b : newBones) {
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
		long totalAddTime = 0;
		long addCount = 0;
		long totalRemoveTime = 0;
		long removeCount = 0;
		if (oldHelpers == null) {
			oldHelpers = new ArrayList<>();
			newHelpers = new ArrayList<>();
			List<? extends Bone> oldHelpersRefs = receivingModel.getBones();
			for (final Bone b : oldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = receivingModel.getName();
				bs.showClass = true;
				oldHelpers.add(bs);
			}
			oldHelpersRefs = receivingModel.getHelpers();
			for (final Bone b : oldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = receivingModel.getName();
				bs.showClass = true;
				oldHelpers.add(bs);
			}
			List<? extends Bone> newHelpersRefs = donatingModel.getBones();
			for (final Bone b : newHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = donatingModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				newHelpers.add(bs);
			}
			newHelpersRefs = donatingModel.getHelpers();
			for (final Bone b : newHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = donatingModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				newHelpers.add(bs);
			}
		}
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : oldHelpers) {
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
			for (final BoneShell b : oldHelpers) {
				if (futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += (endTime - startTime);
					removeCount++;
				}
			}
		}
		for (final BoneShell b : newHelpers) {
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

	public VisibilityShell shellFromObject(final Object o) {
		for (final VisibilityShell v : allVisShells) {
			if (v.source == o) {
				return v;
			}
		}
		return null;
	}


	public void timescaleAllAnims() {
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
			aniPanel.importTypeBox.setSelectedIndex(2);
		}
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

	public void importAllBones(int selsctionIndex) {
		for (BonePanel bonePanel : bonePanels) {
			bonePanel.setSelectedIndex(selsctionIndex);
		}
	}

	public void importAllObjs(boolean b) {
		for (ObjectPanel objectPanel : objectPanels) {
			objectPanel.doImport.setSelected(b);
		}
	}

	public void allVisButton(String visible) {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			if (vPanel.sourceShell.model == receivingModel) {
				vPanel.newSourcesBox.setSelectedItem(visible);
			} else {
				vPanel.oldSourcesBox.setSelectedItem(visible);
			}
		}
	}

	public void selSimButton() {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			vPanel.selectSimilarOptions();
		}
	}

	public void informGeosetVisibility(final Geoset g, final boolean flag) {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			final BoneAttachmentPanel geoPanel = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
			if (geoPanel.geoset == g) {
				geosetAnimTabs.setEnabledAt(i, flag);
			}
		}
	}

	public void allMatrOriginal() {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				bap.resetMatrices();
			}
		}
	}

	public void allMatrSameName() {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				bap.setMatricesToSimilarNames();
			}
		}
	}

	public void setSelectedItem(final String what) {
		for (BonePanel temp : boneTabs.getSelectedValuesList()) {
			temp.setSelectedValue(what);
		}
	}

	/**
	 * The method run when the user pushes the "Set Parent for All" button in the
	 * MultiBone panel.
	 */
	public void setParentMultiBones() {
		final JList<BoneShell> list = new JList<>(getFutureBoneListExtended(true));
		list.setCellRenderer(boneShellRenderer);
		final int x = JOptionPane.showConfirmDialog(null, new JScrollPane(list), "Set Parent for All Selected Bones", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			for (BonePanel temp : boneTabs.getSelectedValuesList()) {
				temp.setParent(list.getSelectedValue());
			}
		}
	}

	public void setObjGroupSelected(final boolean flag) {
		for (ObjectPanel temp : objectTabs.getSelectedValuesList()) {
			temp.doImport.setSelected(flag);
		}
	}

	public void setVisGroupSelected(final boolean flag) {
		for (VisibilityPanel temp : visTabs.getSelectedValuesList()) {
			temp.favorOld.setSelected(flag);
		}
	}

	public void setVisGroupItemOld(final Object o) {
		for (VisibilityPanel temp : visTabs.getSelectedValuesList()) {
			temp.oldSourcesBox.setSelectedItem(o);
		}
	}

	public void setVisGroupItemNew(final Object o) {
		for (VisibilityPanel temp : visTabs.getSelectedValuesList()) {
			temp.newSourcesBox.setSelectedItem(o);
		}
	}

	public BonePanel getPanelOf(final Bone b) {
		return boneToPanel.get(b);
	}
}
