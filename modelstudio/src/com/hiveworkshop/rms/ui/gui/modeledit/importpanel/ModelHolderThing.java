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
	//	public JList<GeosetShell> geosetShellJList = new JList<>(allGeoShells);
	public IterableListModel<MatrixShell> recModMatrixShells = new IterableListModel<>();
	public IterableListModel<MatrixShell> donModMatrixShells = new IterableListModel<>();
	public IterableListModel<MatrixShell> allMatrixShells = new IterableListModel<>();
	public Map<Matrix, MatrixShell> allMatrixShellMap = new HashMap<>();

	// Materials
	public IterableListModel<Material> recModMaterials = new IterableListModel<>();
	public IterableListModel<Material> donModMaterials = new IterableListModel<>();
	public IterableListModel<Material> allMaterials = new IterableListModel<>();

	// Animation
	public JCheckBox clearRecModAnims = new JCheckBox("Clear pre-existing animations");
	public IterableListModel<AnimShell> recModAnims = new IterableListModel<>();
	public IterableListModel<AnimShell> donModAnims = new IterableListModel<>();
	public IterableListModel<AnimShell> allAnimShells = new IterableListModel<>();
//	public JList<AnimShell> animJList = new JList<>(allAnimShells);

	// Bones
	public JCheckBox clearExistingBones;
	public IterableListModel<IdObjectShell<?>> donModBoneShells = new IterableListModel<>();
	//	public JList<BoneShell> donModBoneShellJList = new JList<>(donModBoneShells);
	public IterableListModel<IdObjectShell<?>> recModBoneShells = new IterableListModel<>();
	//	public JList<BoneShell> recModBoneShellJList = new JList<>(recModBoneShells);
	public IterableListModel<IdObjectShell<?>> allBoneShells = new IterableListModel<>();
	//	public JList<BoneShell> allBoneShellJList = new JList<>(allBoneShells);
	public ArrayList<IdObjectShell<?>> recModBones = new ArrayList<>();
	public ArrayList<IdObjectShell<?>> donModBones = new ArrayList<>();
	public IterableListModel<IdObjectShell<?>> futureBoneHelperList = new IterableListModel<>();
	public IterableListModel<IdObjectShell<?>> futureBoneList = new IterableListModel<>();
	BiMap<IdObject, IdObjectShell<?>> recModObjShellBiMap = new BiMap<>();
	BiMap<IdObject, IdObjectShell<?>> donModObjShellBiMap = new BiMap<>();


	// Objects
	public IterableListModel<IdObjectShell<?>> donModObjectShells = new IterableListModel<>();
	//	public JList<ObjectShell> donModObjectJList = new JList<>(donModObjectShells);
	public IterableListModel<IdObjectShell<?>> recModObjectShells = new IterableListModel<>();
	//	public JList<ObjectShell> recModObjectJList = new JList<>(recModObjectShells);
	public IterableListModel<IdObjectShell<?>> allObjectShells = new IterableListModel<>();
//	public JList<ObjectShell> allObjectJList = new JList<>(allObjectShells);

	// Cameras
	public IterableListModel<CameraShell> donModCameraShells = new IterableListModel<>();
	//	public JList<CameraShell> donModCameraJList = new JList<>(donModCameraShells);
	public IterableListModel<CameraShell> recModCameraShells = new IterableListModel<>();
	public JList<CameraShell> recModCameraJList = new JList<>(recModCameraShells);
	public IterableListModel<CameraShell> allCameraShells = new IterableListModel<>();
//	public JList<CameraShell> allCameraJList = new JList<>(allCameraShells);


	// Visibility

	public IterableListModel<VisibilityShell> donModVisibilityShells = new IterableListModel<>();
	public IterableListModel<VisibilityShell> recModVisibilityShells = new IterableListModel<>();

	public List<VisibilityShell> recModVisSourcesOld = new ArrayList<>();
	public List<VisibilityShell> donModVisSourcesNew = new ArrayList<>();

	public BoneShellListCellRenderer boneShellRenderer;

	public IterableListModel<VisibilityShell> futureVisComponents = new IterableListModel<>();
	public JList<VisibilityShell> visibilityShellJList = new JList<>(futureVisComponents);
	public ArrayList<VisibilityShell> allVisShells = new ArrayList<>();


	BiMap<TimelineContainer, VisibilityShell> recModVisShellBiMap = new BiMap<>();
	BiMap<TimelineContainer, VisibilityShell> donModVisShellBiMap = new BiMap<>();
	BiMap<TimelineContainer, VisibilityShell> allVisShellBiMap = new BiMap<>();

	VisibilityShell neverVisible = new VisibilityShell(false);
	VisibilityShell alwaysVisible = new VisibilityShell(true);
	VisibilityShell multipleVisible = new VisibilityShell(false).setMultiple();


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;

//		initBoneHelperLists();
		initIdObjectListsLists();
		initiateGeosetLists();
		initCameraLists();
//		initObjectLists();
		initVisLists();
		initAnimLists();

		boneShellRenderer = new BoneShellListCellRenderer(this.receivingModel, this.donatingModel);
	}

	public IterableListModel<IdObjectShell<?>> getFutureBoneList() {
		futureBoneList.clear();
		ArrayList<IdObjectShell<?>> motionFromBones = new ArrayList<>();
		ArrayList<IdObjectShell<?>> receiveMotionBones = new ArrayList<>();
		ArrayList<IdObjectShell<?>> dontImportBones = new ArrayList<>();

		if (!clearExistingBones.isSelected()) {
			for (IdObjectShell<?> bs : recModBones) {
				switch (bs.getImportStatus()) {
					case IMPORT -> futureBoneList.addElement(bs);
					case MOTION_FROM -> motionFromBones.add(bs);
//					case RECEIVE_MOTION -> receiveMotionBones.add(bs);
					case RECEIVE_MOTION -> futureBoneList.addElement(bs);
					case DONT_IMPORT -> dontImportBones.add(bs);
				}
			}
		}

		for (IdObjectShell<?> bs : donModBones) {
			switch (bs.getImportStatus()) {
				case IMPORT -> futureBoneList.addElement(bs);
				case MOTION_FROM -> motionFromBones.add(bs);
//				case RECEIVE_MOTION -> receiveMotionBones.add(bs);
				case RECEIVE_MOTION -> futureBoneList.addElement(bs);
				case DONT_IMPORT -> dontImportBones.add(bs);
			}
		}
//		futureBoneList.addAll(receiveMotionBones);
		futureBoneList.addAll(dontImportBones);
		return futureBoneList;
	}

	public IterableListModel<IdObjectShell<?>> getFutureBoneHelperList() {
		totalAddTime = 0;
		addCount = 0;
		totalRemoveTime = 0;
		removeCount = 0;

		ArrayList<IdObjectShell<?>> motionFromBones = new ArrayList<>();
		ArrayList<IdObjectShell<?>> dontImportBones = new ArrayList<>();

		futureBoneHelperList.clear();

		if (!clearExistingBones.isSelected()) {
			for (IdObjectShell<?> bs : recModBoneShells) {
				switch (bs.getImportStatus()) {
					case IMPORT -> futureBoneHelperList.addElement(bs);
					case MOTION_FROM -> motionFromBones.add(bs);
					case DONT_IMPORT -> dontImportBones.add(bs);
				}
			}
		}
		for (IdObjectShell<?> bs : donModBoneShells) {
			switch (bs.getImportStatus()) {
				case IMPORT -> futureBoneHelperList.addElement(bs);
				case MOTION_FROM -> motionFromBones.add(bs);
				case DONT_IMPORT -> dontImportBones.add(bs);
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

	public void setImportStatusForAllDonBones(IdObjectShell.ImportType importType) {
		Map<String, IdObjectShell<?>> nameMap = new HashMap<>();
		if (importType == IdObjectShell.ImportType.MOTION_FROM) {
			for (IdObjectShell<?> boneShell : recModBoneShells) {
				nameMap.put(boneShell.getName(), boneShell);
			}
		}
		for (IdObjectShell<?> boneShell : donModBoneShells) {
			boneShell.setImportStatus(importType);
			if (importType == IdObjectShell.ImportType.MOTION_FROM && nameMap.containsKey(boneShell.getName())) {
				nameMap.get(boneShell.getName()).setMotionSrcShell(boneShell);
			} else if (importType != IdObjectShell.ImportType.MOTION_FROM) {
				boneShell.setMotionSrcShell(null);
			}
		}
	}

	public void setImportStatusForAllRecBones(IdObjectShell.ImportType importType) {
		Map<String, IdObjectShell<?>> nameMap = new HashMap<>();
		if (importType == IdObjectShell.ImportType.MOTION_FROM) {
			for (IdObjectShell<?> boneShell : donModBoneShells) {
				nameMap.put(boneShell.getName(), boneShell);
			}
		}
		for (IdObjectShell<?> boneShell : recModBoneShells) {
			boneShell.setImportStatus(importType);
			if (importType == IdObjectShell.ImportType.MOTION_FROM && nameMap.containsKey(boneShell.getName())) {
				nameMap.get(boneShell.getName()).setMotionSrcShell(boneShell);
			} else if (importType != IdObjectShell.ImportType.MOTION_FROM) {
				boneShell.setMotionSrcShell(null);
			}
		}
	}

	public void setImportAllDonObjs(boolean doImport) {
		donModObjectShells.forEach(shell -> shell.setShouldImport(doImport));
	}

	public void setImportAllRecObjs(boolean doImport) {
		recModObjectShells.forEach(shell -> shell.setShouldImport(doImport));
	}

	public void setImportAllDonCams(boolean b) {
		donModCameraShells.forEach(shell -> shell.setShouldImport(b));
	}

	public void setImportAllRecCams(boolean b) {
		recModCameraShells.forEach(shell -> shell.setShouldImport(b));
	}

	public void selectSimilarVisSources() {
		// not sure this is correct
		for (final VisibilityShell visibilityShell : allVisShells) {
			for (VisibilityShell vs : donModVisSourcesNew) {
				if (visibilityShell.getSource().getName().equals(vs.getSource().getName())) {
//					System.out.println(visibilityShell.getSource().getName());
					visibilityShell.setRecModAnimsVisSource(vs);
				}
			}
			for (VisibilityShell vs : recModVisSourcesOld) {
				if (visibilityShell.getSource().getName().equals(vs.getSource().getName())) {
//					System.out.println(visibilityShell.getSource().getName());
					visibilityShell.setRecModAnimsVisSource(vs);
				}
			}
		}
	}

	long addCount;
	long totalRemoveTime;
	long removeCount;

	public void setImportAllGeos(boolean b) {
		for (GeosetShell geoShell : allGeoShells) {
//			if (geoShell.isFromDonating()) {
//				geoShell.setDoImport(b);
//			}
			geoShell.setDoImport(b);
		}
	}

	public void setImportAllRecGeos(boolean b) {
		for (GeosetShell geoShell : recModGeoShells) {
			geoShell.setDoImport(b);
//			if (!geoShell.isFromDonating()) {
//				geoShell.setDoImport(b);
//			}
		}
	}

	public void setImportAllDonGeos(boolean b) {
		for (GeosetShell geoShell : donModGeoShells) {
			geoShell.setDoImport(b);
//			if (geoShell.isFromDonating()) {
//				geoShell.setDoImport(b);
//			}
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
		BiMap<IdObject, IdObjectShell<? extends IdObject>> recBoneBiMap = getIdObjectShellBiMap(receivingModel.getBones(), receivingModel, false);
		recModObjShellBiMap.putAll(recBoneBiMap);
		recModBones.addAll(recBoneBiMap.values());
		recModBoneShells.addAll(recBoneBiMap.values());

		BiMap<IdObject, IdObjectShell<? extends IdObject>> recHelpBiMap = getIdObjectShellBiMap(receivingModel.getHelpers(), receivingModel, false);
		recModObjShellBiMap.putAll(recHelpBiMap);
		recModBoneShells.addAll(recHelpBiMap.values());

//		recModObjShellBiMap.values().forEach(bs -> bs.setParentBs(recModObjShellBiMap));


		BiMap<IdObject, IdObjectShell<? extends IdObject>> donBoneBiMap = getIdObjectShellBiMap(donatingModel.getBones(), donatingModel, true);
		donModObjShellBiMap.putAll(donBoneBiMap);
		donModBones.addAll(donBoneBiMap.values());
		donModBoneShells.addAll(donBoneBiMap.values());

		BiMap<IdObject, IdObjectShell<? extends IdObject>> donHelpBiMap = getIdObjectShellBiMap(donatingModel.getHelpers(), donatingModel, true);
		donModObjShellBiMap.putAll(donHelpBiMap);
		donModBoneShells.addAll(donHelpBiMap.values());

//		donModObjShellBiMap.values().forEach(bs -> bs.setParentBs(donModObjShellBiMap));

		allBoneShells.addAll(recModBoneShells);
		allBoneShells.addAll(donModBoneShells);
	}

	private BiMap<IdObject, IdObjectShell<? extends IdObject>> getIdObjectShellBiMap(List<? extends IdObject> objectList, EditableModel model, boolean isDonModel) {
		BiMap<IdObject, IdObjectShell<? extends IdObject>> biMap = new BiMap<>();
		for (IdObject bone : objectList) {
			IdObjectShell<? extends IdObject> bs = new IdObjectShell<>(bone, isDonModel, model.getName(), true);
			biMap.put(bone, bs);
		}
		return biMap;
	}

	private void initIdObjectListsLists() {
		for (IdObject idObject : receivingModel.getIdObjects()) {
			IdObjectShell<?> shell = new IdObjectShell<>(idObject, false, receivingModel.getName(), true);
			if (idObject instanceof Helper) {
				recModBoneShells.addElement(shell);
			} else if (idObject instanceof Bone) {
				recModBones.add(shell);
				recModBoneShells.addElement(shell);
			} else {
				recModObjectShells.addElement(shell);
			}
			recModObjShellBiMap.put(idObject, shell);
		}
		for (IdObject idObject : donatingModel.getIdObjects()) {
			IdObjectShell<?> shell = new IdObjectShell<>(idObject, true, donatingModel.getName(), true);
			if (idObject instanceof Helper) {
				donModBoneShells.addElement(shell);
			} else if (idObject instanceof Bone) {
				donModBones.add(shell);
				donModBoneShells.addElement(shell);
			} else {
				donModObjectShells.addElement(shell);
			}
			donModObjShellBiMap.put(idObject, shell);
		}

		allBoneShells.addAll(recModBoneShells);
		allBoneShells.addAll(donModBoneShells);
		allObjectShells.addAll(recModObjectShells);
		allObjectShells.addAll(donModObjectShells);
		donModObjShellBiMap.values().forEach(os -> os.setParentBs(donModObjShellBiMap));
		recModObjShellBiMap.values().forEach(os -> os.setParentBs(recModObjShellBiMap));
	}

	private void initiateGeosetLists() {
		// ToDo Fix MatrixShell for HD models!!

		for (Geoset geoset : receivingModel.getGeosets()) {
			GeosetShell geoShell = new GeosetShell(geoset, receivingModel, false);
			geoShell.setMatrixShells(createMatrixShells(geoset, recModObjShellBiMap, receivingModel, false));
			recModGeoShells.addElement(geoShell);
			geoset.reMakeMatrixList();
		}
		allGeoShells.addAll(recModGeoShells);

		for (Geoset geoset : donatingModel.getGeosets()) {
			GeosetShell geoShell = new GeosetShell(geoset, donatingModel, true);
			geoShell.setMatrixShells(createMatrixShells(geoset, donModObjShellBiMap, donatingModel, true));
			donModGeoShells.addElement(geoShell);
			geoset.reMakeMatrixList();
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

	private void initCameraLists() {
		for (Camera obj : receivingModel.getCameras()) {
			recModCameraShells.addElement(new CameraShell(obj, false));
		}

		for (Camera obj : donatingModel.getCameras()) {
			donModCameraShells.addElement(new CameraShell(obj, true));
		}

		allCameraShells.addAll(recModCameraShells);
		allCameraShells.addAll(donModCameraShells);
	}

	// ToDo Fix MatrixShell for HD models!!
	private IterableListModel<MatrixShell> createMatrixShells(Geoset geoset, BiMap<IdObject, IdObjectShell<?>> boneShells, EditableModel model, boolean isFromDonating) {
		IterableListModel<MatrixShell> matrixShells = new IterableListModel<>();
		if (geoset.isHD()) {
			for (Bone bone : geoset.getBoneMap().keySet()) {
				ArrayList<IdObjectShell<?>> orgBones = new ArrayList<>();
				orgBones.add(boneShells.get(bone));
				MatrixShell ms = allMatrixShellMap.computeIfAbsent(new Matrix(bone), matrix -> new MatrixShell(matrix, orgBones, isFromDonating, true));
				matrixShells.addElement(ms);
			}
		} else {
			for (Matrix matrix : geoset.collectMatrices()) {
				ArrayList<IdObjectShell<?>> orgBones = new ArrayList<>();
				// For look to find similarly named stuff and add it
				for (Bone bone : matrix.getBones()) {
					if (boneShells.get(bone).getIdObject() instanceof Bone) {
						orgBones.add(boneShells.get(bone));
					}
				}

				MatrixShell ms = new MatrixShell(matrix, orgBones, isFromDonating);
				matrixShells.addElement(ms);
			}
		}
		return matrixShells;
	}

	private void initObjectLists() {

		for (GeosetShell geoShell : donModGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				donModVisShellBiMap.put(x, new VisibilityShell(x, donatingModel.getName(), true));
			}
			donModVisShellBiMap.put(geoShell.getGeoset(), new VisibilityShell(geoShell.getGeoset(), donatingModel.getName(), true));
		}

		for (IdObject obj : donatingModel.getIdObjects()) {
			if (!(obj instanceof Bone)) {
				donModObjectShells.addElement(new IdObjectShell<>(obj, true));
				if (!(obj instanceof CollisionShape || obj instanceof EventObject)) {
					donModVisShellBiMap.put(obj, new VisibilityShell(obj, donatingModel.getName(), true));
				}
			}
		}

		for (Camera obj : donatingModel.getCameras()) {
			donModCameraShells.addElement(new CameraShell(obj, true));
		}

		donModVisibilityShells.addAll(donModVisShellBiMap.values());
		allVisShellBiMap.putAll(donModVisShellBiMap);
		donModObjectShells.forEach(os -> os.setParentBs(donModObjShellBiMap));


		for (GeosetShell geoShell : recModGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				recModVisShellBiMap.put(x, new VisibilityShell(x, receivingModel.getName(), false));
			}
			recModVisShellBiMap.put(geoShell.getGeoset(), new VisibilityShell(geoShell.getGeoset(), receivingModel.getName(), false));
		}

		for (IdObject obj : receivingModel.getIdObjects()) {
			if (!(obj instanceof Bone)) {
				recModObjectShells.addElement(new IdObjectShell<>(obj, false));
				if (!(obj instanceof CollisionShape || obj instanceof EventObject)) {
					recModVisShellBiMap.put(obj, new VisibilityShell(obj, receivingModel.getName(), false));
				}
			}
		}

		for (Camera obj : receivingModel.getCameras()) {
			recModCameraShells.addElement(new CameraShell(obj, false));
		}

		recModVisibilityShells.addAll(recModVisShellBiMap.values());
		allVisShellBiMap.putAll(recModVisShellBiMap);
		recModObjectShells.forEach(os -> os.setParentBs(recModObjShellBiMap));

		allObjectShells.addAll(recModObjectShells);
		allObjectShells.addAll(donModObjectShells);

		allCameraShells.addAll(recModCameraShells);
		allCameraShells.addAll(donModCameraShells);
	}

	private void initVisLists() {

		for (GeosetShell geoShell : donModGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				donModVisShellBiMap.put(x, new VisibilityShell(x, geoShell.getModelName(), geoShell.isFromDonating()));
			}
			donModVisShellBiMap.put(geoShell.getGeoset(), new VisibilityShell(geoShell.getGeoset(), geoShell.getModelName(), geoShell.isFromDonating()));
		}

		for (IdObjectShell<?> obj : donModObjectShells) {
			if (!(obj.getIdObject() instanceof CollisionShape || obj.getIdObject() instanceof EventObject)) {
				donModVisShellBiMap.put(obj.getIdObject(), new VisibilityShell(obj.getIdObject(), donatingModel.getName(), true));
			}
		}

		donModVisibilityShells.addAll(donModVisShellBiMap.values());
		allVisShellBiMap.putAll(donModVisShellBiMap);


		for (GeosetShell geoShell : recModGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				recModVisShellBiMap.put(x, new VisibilityShell(x, geoShell.getModelName(), geoShell.isFromDonating()));
			}
			recModVisShellBiMap.put(geoShell.getGeoset(), new VisibilityShell(geoShell.getGeoset(), geoShell.getModelName(), geoShell.isFromDonating()));
		}

		for (IdObjectShell<?> obj : recModObjectShells) {
			if (!(obj.getIdObject() instanceof CollisionShape || obj.getIdObject() instanceof EventObject)) {
				recModVisShellBiMap.put(obj.getIdObject(), new VisibilityShell(obj.getIdObject(), receivingModel.getName(), true));
			}
		}

		recModVisibilityShells.addAll(recModVisShellBiMap.values());
		allVisShellBiMap.putAll(recModVisShellBiMap);
	}

	public IterableListModel<VisibilityShell> visibilityList() {
		VisibilityShell selection = visibilityShellJList.getSelectedValue();
		futureVisComponents.clear();
		for (GeosetShell geoShell : allGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				VisibilityShell vs = allVisShellBiMap.get(x);
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.addElement(vs);
				}
			}
			if (geoShell.isDoImport()) {
				VisibilityShell vs = allVisShellBiMap.get(geoShell.getGeoset());
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

		for (IdObjectShell<?> op : donModObjectShells) {
			if (op.getShouldImport()) {
				VisibilityShell vs = allVisShellBiMap.get(op.getIdObject());
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
			VisibilityShell vs = allVisShellBiMap.get(x);
			if (vs != null && !futureVisComponents.contains(vs)) {
				futureVisComponents.addElement(vs);
			}
		}
	}

	public VisibilityShell visShellFromObject(TimelineContainer vs) {
		return allVisShellBiMap.get(vs);
	}
}
