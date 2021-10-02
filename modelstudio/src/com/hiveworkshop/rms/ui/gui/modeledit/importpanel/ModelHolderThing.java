package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelHolderThing {
	public EditableModel receivingModel;
	public EditableModel donatingModel;

	// Geosets
	public IterableListModel<GeosetShell> recModGeoShells = new IterableListModel<>();
	public IterableListModel<GeosetShell> donModGeoShells = new IterableListModel<>();
	public IterableListModel<GeosetShell> allGeoShells = new IterableListModel<>();
	public JList<GeosetShell> geosetShellJList = new JList<>(allGeoShells);

	// Materials
	public IterableListModel<Material> recModMaterials = new IterableListModel<>();
	public IterableListModel<Material> donModMaterials = new IterableListModel<>();
	public IterableListModel<Material> allMaterials = new IterableListModel<>();

	// Animation
	public JCheckBox clearRecModAnims = new JCheckBox("Clear pre-existing animations");
	public IterableListModel<AnimShell> recModAnims = new IterableListModel<>();
	public IterableListModel<AnimShell> donModAnims = new IterableListModel<>();
	public IterableListModel<AnimShell> allAnimShells = new IterableListModel<>();
	public JList<AnimShell> animJList = new JList<>(allAnimShells);

	// Bones
	public JCheckBox clearExistingBones;
	public IterableListModel<BoneShell> donModBoneShells = new IterableListModel<>();
	public JList<BoneShell> donModBoneShellJList = new JList<>(donModBoneShells);
	public IterableListModel<BoneShell> recModBoneShells = new IterableListModel<>();
	public JList<BoneShell> recModBoneShellJList = new JList<>(recModBoneShells);
	public IterableListModel<BoneShell> allBoneShells = new IterableListModel<>();
	public JList<BoneShell> allBoneShellJList = new JList<>(allBoneShells);
	public ArrayList<BoneShell> recModBones = new ArrayList<>();
	public ArrayList<BoneShell> donModBones = new ArrayList<>();
	public IterableListModel<BoneShell> futureBoneHelperList = new IterableListModel<>();
	public IterableListModel<BoneShell> futureBoneList = new IterableListModel<>();
	BiMap<IdObject, BoneShell> recModBoneShellBiMap = new BiMap<>();
	BiMap<IdObject, BoneShell> donModBoneShellBiMap = new BiMap<>();


	// Objects
	public IterableListModel<ObjectShell> donModObjectShells = new IterableListModel<>();
	public JList<ObjectShell> donModObjectJList = new JList<>(donModObjectShells);
	public IterableListModel<ObjectShell> recModObjectShells = new IterableListModel<>();
	public JList<ObjectShell> recModObjectJList = new JList<>(recModObjectShells);
	public IterableListModel<ObjectShell> allObjectShells = new IterableListModel<>();
	public JList<ObjectShell> allObjectJList = new JList<>(allObjectShells);

	// Cameras
	public IterableListModel<CameraShell> donModCameraShells = new IterableListModel<>();
	public JList<CameraShell> donModCameraJList = new JList<>(donModCameraShells);
	public IterableListModel<CameraShell> recModCameraShells = new IterableListModel<>();
	public JList<CameraShell> recModCameraJList = new JList<>(recModCameraShells);
	public IterableListModel<CameraShell> allCameraShells = new IterableListModel<>();
	public JList<CameraShell> allCameraJList = new JList<>(allCameraShells);


	// Visibility

	public IterableListModel<VisibilityShell> donModVisibilityShells = new IterableListModel<>();
	public IterableListModel<VisibilityShell> recModVisibilityShells = new IterableListModel<>();

	public List<VisibilityShell> recModVisSourcesOld = new ArrayList<>();
	public List<VisibilityShell> donModVisSourcesNew = new ArrayList<>();

	public BoneShellListCellRenderer boneShellRenderer;

	public IterableListModel<VisibilityShell> futureVisComponents = new IterableListModel<>();
	public JList<VisibilityShell> visibilityShellJList = new JList<>(futureVisComponents);
	public ArrayList<VisibilityShell> allVisShells = new ArrayList<>();


	BiMap<VisibilitySource, VisibilityShell> recModVisShellBiMap = new BiMap<>();
	BiMap<VisibilitySource, VisibilityShell> donModVisShellBiMap = new BiMap<>();
	BiMap<VisibilitySource, VisibilityShell> allVisShellBiMap = new BiMap<>();

	VisibilityShell neverVisible = new VisibilityShell(false);
	VisibilityShell alwaysVisible = new VisibilityShell(true);
	VisibilityShell multipleVisible = new VisibilityShell(false).setMultiple();


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;

		initBoneHelperLists();
		initiateGeosetLists();
		initObjectLists();
		initAnimLists();

		boneShellRenderer = new BoneShellListCellRenderer(this.receivingModel, this.donatingModel);
	}

	public IterableListModel<BoneShell> getFutureBoneList() {
		futureBoneList.clear();
		ArrayList<BoneShell> motionFromBones = new ArrayList<>();
		ArrayList<BoneShell> dontImportBones = new ArrayList<>();

		if (!clearExistingBones.isSelected()) {
			for (BoneShell bs : recModBones) {
				switch (bs.getImportStatus()) {
					case IMPORT -> futureBoneList.addElement(bs);
					case MOTIONFROM -> motionFromBones.add(bs);
					case DONTIMPORT -> dontImportBones.add(bs);
				}
			}
		}

		for (BoneShell bs : donModBones) {
			switch (bs.getImportStatus()) {
				case IMPORT -> futureBoneList.addElement(bs);
				case MOTIONFROM -> motionFromBones.add(bs);
				case DONTIMPORT -> dontImportBones.add(bs);
			}
		}
		futureBoneList.addAll(dontImportBones);
		return futureBoneList;
	}

	public IterableListModel<BoneShell> getFutureBoneHelperList() {
		totalAddTime = 0;
		addCount = 0;
		totalRemoveTime = 0;
		removeCount = 0;

		ArrayList<BoneShell> motionFromBones = new ArrayList<>();
		ArrayList<BoneShell> dontImportBones = new ArrayList<>();

		futureBoneHelperList.clear();

		if (!clearExistingBones.isSelected()) {
			for (BoneShell bs : recModBoneShells) {
				switch (bs.getImportStatus()) {
					case IMPORT -> futureBoneHelperList.addElement(bs);
					case MOTIONFROM -> motionFromBones.add(bs);
					case DONTIMPORT -> dontImportBones.add(bs);
				}
			}
		}
		for (BoneShell bs : donModBoneShells) {
			switch (bs.getImportStatus()) {
				case IMPORT -> futureBoneHelperList.addElement(bs);
				case MOTIONFROM -> motionFromBones.add(bs);
				case DONTIMPORT -> dontImportBones.add(bs);
			}
		}

		futureBoneHelperList.addAll(dontImportBones);
		System.out.println("futureBoneListEx size: " + futureBoneHelperList.size());

		return futureBoneHelperList;
	}

	long totalAddTime;


	public void setImportTypeForAllAnims(AnimShell.ImportType type) {
		allAnimShells.forEach(shell -> shell.setImportType(type));
	}

	public void setImportTypeForAllDonAnims(AnimShell.ImportType type) {
		donModAnims.forEach(shell -> shell.setImportType(type));
	}

	public void setImportTypeForAllRecAnims(AnimShell.ImportType type) {
		recModAnims.forEach(shell -> shell.setImportType(type));
	}

	public void setImportStatusForAllDonBones(BoneShell.ImportType importType) {
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

	public void setImportAllDonObjs(boolean doImport) {
		donModObjectShells.forEach(shell -> shell.setShouldImport(doImport));
	}

	public void setImportAllDonCams(boolean b) {
		donModCameraShells.forEach(shell -> shell.setShouldImport(b));
	}

	public void selectSimilarVisSources() {
		// not sure this is correct
		for (final VisibilityShell visibilityShell : allVisShells) {
			for (VisibilityShell vs : donModVisSourcesNew) {
				if (visibilityShell.getSource().getName().equals(vs.getSource().getName())) {
//					System.out.println(visibilityShell.getSource().getName());
					visibilityShell.setNewVisSource(vs);
				}
			}
			for (VisibilityShell vs : recModVisSourcesOld) {
				if (visibilityShell.getSource().getName().equals(vs.getSource().getName())) {
//					System.out.println(visibilityShell.getSource().getName());
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



	private void initAnimLists() {
		for (Animation anim : receivingModel.getAnims()) {
			recModAnims.addElement(new AnimShell(anim, false));
		}

		for (Animation anim : donatingModel.getAnims()) {
			donModAnims.addElement(new AnimShell(anim, true));
		}
		allAnimShells.addAll(recModAnims);
		allAnimShells.addAll(donModAnims);
	}

	private void initBoneHelperLists() {
		BiMap<IdObject, BoneShell> recBoneBiMap = getBoneShellBiMap(receivingModel.getBones(), receivingModel, false);
		recModBoneShellBiMap.putAll(recBoneBiMap);
		recModBones.addAll(recBoneBiMap.values());

		BiMap<IdObject, BoneShell> recHelpBiMap = getBoneShellBiMap(receivingModel.getHelpers(), receivingModel, false);
		recModBoneShellBiMap.putAll(recHelpBiMap);

		recModBoneShells.addAll(recModBoneShellBiMap.values());
		recModBoneShellBiMap.values().forEach(bs -> bs.setParentBs(recModBoneShellBiMap));


		BiMap<IdObject, BoneShell> donBoneBiMap = getBoneShellBiMap(donatingModel.getBones(), donatingModel, true);
		donModBoneShellBiMap.putAll(donBoneBiMap);
		donModBones.addAll(donBoneBiMap.values());

		BiMap<IdObject, BoneShell> donHelpBiMap = getBoneShellBiMap(donatingModel.getHelpers(), donatingModel, true);
		donModBoneShellBiMap.putAll(donHelpBiMap);

		donModBoneShells.addAll(donModBoneShellBiMap.values());
		donModBoneShellBiMap.values().forEach(bs -> bs.setParentBs(donModBoneShellBiMap));

		allBoneShells.addAll(recModBoneShells);
		allBoneShells.addAll(donModBoneShells);
	}

	private BiMap<IdObject, BoneShell> getBoneShellBiMap(List<? extends Bone> objectList, EditableModel model, boolean isDonModel){
		BiMap<IdObject, BoneShell> biMap = new BiMap<>();
		for (Bone bone : objectList) {
			BoneShell bs = new BoneShell(bone, isDonModel, model.getName(), true);
			biMap.put(bone, bs);
		}
		return biMap;
	}

	private void initiateGeosetLists() {
		for (Geoset geoset : receivingModel.getGeosets()) {
			geoset.reMakeMatrixList();
			GeosetShell geoShell = new GeosetShell(geoset, receivingModel, false);
			geoShell.setMatrixShells(createMatrixShells(geoset, recModBoneShellBiMap, false));
			recModGeoShells.addElement(geoShell);
		}
		allGeoShells.addAll(recModGeoShells);

		for (Geoset geoset : donatingModel.getGeosets()) {
			geoset.reMakeMatrixList();
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
		for (Matrix matrix : geoset.getMatrices()) {
			ArrayList<BoneShell> orgBones = new ArrayList<>();
			// For look to find similarly named stuff and add it
			for (Bone bone : matrix.getBones()) {
				if (boneShells.get(bone) != null) {
					orgBones.add(boneShells.get(bone));
				}
			}

			MatrixShell ms = new MatrixShell(matrix, orgBones, isFromDonating);
			matrixShells.addElement(ms);
		}
		return matrixShells;
	}

	private void initObjectLists() {

		for (GeosetShell geoShell : donModGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				donModVisShellBiMap.put(x, new VisibilityShell(x, donatingModel, true));
			}
			donModVisShellBiMap.put(geoShell.getGeoset(), new VisibilityShell(geoShell.getGeoset(), donatingModel, true));
		}

		for (IdObject obj : donatingModel.getIdObjects()) {
			if (!(obj instanceof Bone)) {
				donModObjectShells.addElement(new ObjectShell(obj, true));
				if (!(obj instanceof CollisionShape || obj instanceof EventObject)) {
					donModVisShellBiMap.put(obj, new VisibilityShell(obj, donatingModel, true));
				}
			}
		}

		for (Camera obj : donatingModel.getCameras()) {
			donModCameraShells.addElement(new CameraShell(obj, true));
		}

		donModVisibilityShells.addAll(donModVisShellBiMap.values());
		allVisShellBiMap.putAll(donModVisShellBiMap);
		donModObjectShells.forEach(os -> os.setParentBs(donModBoneShellBiMap));


		for (GeosetShell geoShell : recModGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				recModVisShellBiMap.put(x, new VisibilityShell(x, receivingModel, false));
			}
			recModVisShellBiMap.put(geoShell.getGeoset(), new VisibilityShell(geoShell.getGeoset(), receivingModel, false));
		}

		for (IdObject obj : receivingModel.getIdObjects()) {
			if (!(obj instanceof Bone)) {
				recModObjectShells.addElement(new ObjectShell(obj, false));
				if (!(obj instanceof CollisionShape || obj instanceof EventObject)) {
					recModVisShellBiMap.put(obj, new VisibilityShell(obj, receivingModel, false));
				}
			}
		}

		for (Camera obj : receivingModel.getCameras()) {
			recModCameraShells.addElement(new CameraShell(obj, false));
		}

		recModVisibilityShells.addAll(recModVisShellBiMap.values());
		allVisShellBiMap.putAll(recModVisShellBiMap);
		recModObjectShells.forEach(os -> os.setParentBs(recModBoneShellBiMap));

		allObjectShells.addAll(recModObjectShells);
		allObjectShells.addAll(donModObjectShells);

		allCameraShells.addAll(recModCameraShells);
		allCameraShells.addAll(donModCameraShells);
	}

	public IterableListModel<VisibilityShell> visibilityList() {
		VisibilityShell selection = visibilityShellJList.getSelectedValue();
		futureVisComponents.clear();
		for (GeosetShell geoShell : allGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				VisibilityShell vs = visShellFromObject(x);
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.addElement(vs);
				}
			}
			if (geoShell.isDoImport()) {
				VisibilityShell vs = visShellFromObject(geoShell.getGeoset());
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.addElement(vs);
				}
			}
		}

		// The current's
		fetchAndAddVisComp(receivingModel.getLights());
		fetchAndAddVisComp(receivingModel.getAttachments());
		fetchAndAddVisComp(receivingModel.getParticleEmitters());
		fetchAndAddVisComp(receivingModel.getParticleEmitter2s());
		fetchAndAddVisComp(receivingModel.getRibbonEmitters());
		fetchAndAddVisComp(receivingModel.getPopcornEmitters());

		for (ObjectShell op : donModObjectShells) {
			if (op.getShouldImport()) {
				VisibilityShell vs = visShellFromObject(op.getIdObject());
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.addElement(vs);
				}
			}
		}
		visibilityShellJList.setSelectedValue(selection, true);
		return futureVisComponents;
	}

	public void fetchAndAddVisComp(List<? extends IdObject> idObjects) {
		for (IdObject x : idObjects) {
			VisibilityShell vs = visShellFromObject(x);
			if (vs != null && !futureVisComponents.contains(vs)) {
				futureVisComponents.addElement(vs);
			}
		}
	}

	public VisibilityShell visShellFromObject(VisibilitySource vs) {
		return allVisShellBiMap.get(vs);
	}
}
