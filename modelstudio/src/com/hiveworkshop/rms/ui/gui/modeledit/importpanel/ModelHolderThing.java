package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import java.util.*;

public class ModelHolderThing {
	public EditableModel receivingModel;
	public EditableModel donatingModel;

	// Geosets
	public IterableListModel<GeosetShell> recModGeoShells = new IterableListModel<>();
	public IterableListModel<GeosetShell> donModGeoShells = new IterableListModel<>();
	public IterableListModel<GeosetShell> allGeoShells = new IterableListModel<>();
	public JList<GeosetShell> geosetShellJList = new JList<>(allGeoShells);
	public IterableListModel<Material> recModMaterials = new IterableListModel<>();
	public IterableListModel<Material> donModMaterials = new IterableListModel<>();
	public IterableListModel<Material> allMaterials = new IterableListModel<>();

	// Animation
	public JCheckBox clearRecModAnims = new JCheckBox("Clear pre-existing animations");
	public JTabbedPane animTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
	public IterableListModel<AnimShell> recModAnims = new IterableListModel<>();
	public IterableListModel<AnimShell> donModAnims = new IterableListModel<>();
	public IterableListModel<AnimShell> animTabList = new IterableListModel<>();
	public JList<AnimShell> animJList = new JList<>(animTabList);

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

	public List<VisibilityShell> recModVisSourcesOld = new ArrayList<>();
	public List<VisibilityShell> donModVisSourcesNew = new ArrayList<>();
//	public IterableListModel<VisibilityShell> recModVisSourcesOld = new IterableListModel<>();
//	public IterableListModel<VisibilityShell> donModVisSourcesNew = new IterableListModel<>();

	public BoneShellListCellRenderer boneShellRenderer;
	//	public JList<VisibilityShell> recModVisibilityJList = new JList<>(recModVisibilityShells);
	BiMap<VisibilitySource, VisibilityShell> recModVisShellBiMap = new BiMap<>();

	public IterableListModel<VisibilityShell> futureVisComponents = new IterableListModel<>();
	public JList<VisibilityShell> visTabs = new JList<>(futureVisComponents);
	public ArrayList<VisibilityShell> allVisShellPanes = new ArrayList<>();


	public IterableListModel<VisibilityShell> visShellsComps = new IterableListModel<>();
	public JList<VisibilityShell> visShellJList = new JList<>(visShellsComps);
	public ArrayList<VisibilityShell> allVisShellss = new ArrayList<>();
	BiMap<VisibilitySource, VisibilityShell> donModVisShellBiMap = new BiMap<>();
	BiMap<VisibilitySource, VisibilityShell> allVisShellBiMap = new BiMap<>();

	VisibilityShell neverVisible = new VisibilityShell(false);
	VisibilityShell alwaysVisible = new VisibilityShell(true);
	VisibilityShell multipleVisible = new VisibilityShell(false).setMultiple();

	public ModelViewManager recModelManager;
	public ModelViewManager donModelManager;


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;

		initBoneHelperLists();
		initiateGeosetLists();
		initObjectLists();

		recModelManager = new ModelViewManager(receivingModel);
		donModelManager = new ModelViewManager(donatingModel);
		boneShellRenderer = new BoneShellListCellRenderer(recModelManager, donModelManager);
	}

	public IterableListModel<BoneShell> getFutureBoneList() {
		futureBoneList.clear();
		ArrayList<BoneShell> motionFromBones = new ArrayList<>();
		ArrayList<BoneShell> dontImportBones = new ArrayList<>();

		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : recModBones) {
				if (b.getImportStatus() == BoneShell.ImportType.IMPORT) {
					futureBoneList.addElement(b);
				} else if (b.getImportStatus() == BoneShell.ImportType.DONTIMPORT) {
					dontImportBones.add(b);
				} else if (b.getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
					motionFromBones.add(b);
				}
			}
		}

		for (final BoneShell b : donModBones) {
			if (b.getImportStatus() == BoneShell.ImportType.IMPORT) {
				futureBoneList.addElement(b);
			} else if (b.getImportStatus() == BoneShell.ImportType.DONTIMPORT) {
				dontImportBones.add(b);
			} else if (b.getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
				motionFromBones.add(b);
			}
		}
		futureBoneList.addAll(dontImportBones);
		return futureBoneList;
	}

	public IterableListModel<BoneShell> getFutureBoneListExtended(final boolean newSnapshot) {
		totalAddTime = 0;
		addCount = 0;
		totalRemoveTime = 0;
		removeCount = 0;

		ArrayList<BoneShell> motionFromBones = new ArrayList<>();
		ArrayList<BoneShell> dontImportBones = new ArrayList<>();

		futureBoneListEx.clear();

		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : recModBoneShells) {
				if (b.getImportStatus() == BoneShell.ImportType.IMPORT) {
					futureBoneListEx.addElement(b);
				} else if (b.getImportStatus() == BoneShell.ImportType.DONTIMPORT) {
					dontImportBones.add(b);
				} else if (b.getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
					motionFromBones.add(b);
				}
			}
		}

		for (final BoneShell b : donModBoneShells) {
			if (b.getImportStatus() == BoneShell.ImportType.IMPORT) {
				futureBoneListEx.addElement(b);
			} else if (b.getImportStatus() == BoneShell.ImportType.DONTIMPORT) {
				dontImportBones.add(b);
			} else if (b.getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
				motionFromBones.add(b);
			}
		}

		futureBoneListEx.addAll(dontImportBones);

		return futureBoneListEx;
	}

	long totalAddTime;


	public void setImportTypeForAllAnims(AnimShell.ImportType type) {
		for (AnimShell animShell : animTabList) {
			animShell.setImportType(type);
		}
	}

	public void setImportStatusForAllBones(BoneShell.ImportType importType) {
		Map<String, BoneShell> nameMap = new HashMap<>();
		if (importType == BoneShell.ImportType.MOTIONFROM) {
			for (BoneShell boneShell : recModBoneShells) {
				nameMap.put(boneShell.getName(), boneShell);
			}
		}
		for (BoneShell boneShell : donModBoneShells) {
			boneShell.setImportStatus(importType);
			if (importType == BoneShell.ImportType.MOTIONFROM && nameMap.containsKey(boneShell.getName())) {
				nameMap.get(boneShell.getName()).setImportBoneShell(boneShell);
			} else if (importType != BoneShell.ImportType.MOTIONFROM) {
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
		// not sure this is correct
		for (final VisibilityShell visibilityShell : allVisShellPanes) {
			for (VisibilityShell vs : donModVisSourcesNew) {
				if (visibilityShell.getSource().getName().equals(vs.getSource().getName())) {
					System.out.println(visibilityShell.source.getName());
					visibilityShell.setNewVisSource(vs);
				}
			}
			for (VisibilityShell vs : recModVisSourcesOld) {
				if (visibilityShell.getSource().getName().equals(vs.getSource().getName())) {
					System.out.println(visibilityShell.source.getName());
					visibilityShell.setNewVisSource(vs);
				}
			}
		}
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

	public IterableListModel<VisibilityShell> visibilityList() {
		VisibilityShell selection = visTabs.getSelectedValue();
		futureVisComponents.clear();
		List<VisibilityShell> geosetVisSources = new ArrayList<>();
//		List<VisibilityPanel> geosetVisSources = new ArrayList<>();
		for (GeosetShell geoShell : allGeoShells) {
			for (final Layer x : geoShell.getMaterial().getLayers()) {
//				VisibilityPanel vs = visPaneFromObject(x);
				VisibilityShell vs = visShellFromObject(x);
				if (!futureVisComponents.contains(vs) && (vs != null)) {
					futureVisComponents.addElement(vs);
				}
			}
			if (geoShell.isDoImport()) {
				VisibilityShell vs = visShellFromObject(geoShell.getGeoset());
				if (!futureVisComponents.contains(vs) && !geosetVisSources.contains(vs) && (vs != null)) {
					geosetVisSources.add(vs);
				}
			}
		}
		futureVisComponents.addAll(geosetVisSources);

		// The current's
		fetchAndAddVisComp(receivingModel.getLights());
		fetchAndAddVisComp(receivingModel.getAttachments());
		fetchAndAddVisComp(receivingModel.getParticleEmitters());
		fetchAndAddVisComp(receivingModel.getParticleEmitter2s());
		fetchAndAddVisComp(receivingModel.getRibbonEmitters());
		fetchAndAddVisComp(receivingModel.getPopcornEmitters());

		for (ObjectShell op : donModObjectShells) {
			if (op.getShouldImport() && (op.getIdObject() != null))
			// we don't touch camera "object" panels (which aren't idobjects)
			{
				VisibilityShell vs = visShellFromObject(op.getIdObject());
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
			VisibilityShell vs = visShellFromObject(x);
			if (!futureVisComponents.contains(vs) && (vs != null)) {
				futureVisComponents.addElement(vs);
			}
		}
	}

	public VisibilityShell visShellFromObject(VisibilitySource vs) {
		return allVisShellBiMap.get(vs);
	}
}
