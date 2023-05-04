package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.util.BiMap;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelHolderThing {
	public EditableModel receivingModel;
	public EditableModel donatingModel;

	// Geosets
	public List<GeosetShell> recModGeoShells = new ArrayList<>();
	public List<GeosetShell> donModGeoShells = new ArrayList<>();
	public List<GeosetShell> allGeoShells = new ArrayList<>();
	public List<MatrixShell> recModMatrixShells = new ArrayList<>();
	public List<MatrixShell> donModMatrixShells = new ArrayList<>();
	public List<MatrixShell> allMatrixShells = new ArrayList<>();
	public Map<Matrix, MatrixShell> allMatrixShellMap = new HashMap<>();

	// Materials
	public List<Material> recModMaterials = new ArrayList<>();
	public List<Material> donModMaterials = new ArrayList<>();
	public List<Material> allMaterials = new ArrayList<>();

	// Animation
	public JCheckBox clearRecModAnims = new JCheckBox("Clear pre-existing animations");
	public List<AnimShell> recModAnims = new ArrayList<>();
	public List<AnimShell> donModAnims = new ArrayList<>();
	public List<AnimShell> allAnimShells = new ArrayList<>();

	// Bones
	public JCheckBox clearExistingBones;
	public List<IdObjectShell<?>> donModBoneShells = new ArrayList<>();
	public List<IdObjectShell<?>> recModBoneShells = new ArrayList<>();
	public List<IdObjectShell<?>> allBoneShells = new ArrayList<>();
	public ArrayList<IdObjectShell<?>> recModBones = new ArrayList<>();
	public ArrayList<IdObjectShell<?>> donModBones = new ArrayList<>();
	public List<IdObjectShell<?>> futureBoneHelperList = new ArrayList<>();
	public List<IdObjectShell<?>> futureBoneList = new ArrayList<>();
	BiMap<IdObject, IdObjectShell<?>> recModObjShellBiMap = new BiMap<>();
	BiMap<IdObject, IdObjectShell<?>> donModObjShellBiMap = new BiMap<>();


	// Objects
	public List<IdObjectShell<?>> donModObjectShells = new ArrayList<>(); // No Bones or Helpers
	public List<IdObjectShell<?>> recModObjectShells = new ArrayList<>(); // No Bones or Helpers
	public List<IdObjectShell<?>> allObjectShells = new ArrayList<>(); // No Bones or Helpers

	// Cameras
	public List<CameraShell> donModCameraShells = new ArrayList<>();
	public List<CameraShell> recModCameraShells = new ArrayList<>();
	public List<CameraShell> allCameraShells = new ArrayList<>();


	// Visibility

	public List<VisibilityShell<?>> donModVisibilityShells = new ArrayList<>();
	public List<VisibilityShell<?>> recModVisibilityShells = new ArrayList<>();

	public BoneShellListCellRenderer boneShellRenderer;

	public List<VisibilityShell<?>> futureVisComponents = new ArrayList<>();
	public ArrayList<VisibilityShell<?>> allVisShells = new ArrayList<>();


	BiMap<TimelineContainer, VisibilityShell<?>> recModVisShellBiMap = new BiMap<>();
	BiMap<TimelineContainer, VisibilityShell<?>> donModVisShellBiMap = new BiMap<>();
	BiMap<TimelineContainer, VisibilityShell<?>> allVisShellBiMap = new BiMap<>();

	VisibilityShell<Attachment> recAlwaysVis;
	VisibilityShell<Attachment> recNeverVis;
	VisibilityShell<Attachment> donAlwaysVis;
	VisibilityShell<Attachment> donNeverVis;


	public ModelHolderThing(EditableModel receivingModel, EditableModel donatingModel) {
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;

		recAlwaysVis = new VisibilityShell<>(getVisAttach(false, false), receivingModel.getName(), false);
		recNeverVis = new VisibilityShell<>(getVisAttach(false, true), receivingModel.getName(), false);
		donAlwaysVis = new VisibilityShell<>(getVisAttach(true, false), donatingModel.getName(), true);
		donNeverVis = new VisibilityShell<>(getVisAttach(true, true), donatingModel.getName(), true);
//		initBoneHelperLists();
		initIdObjectListsLists();
		initiateGeosetLists();
		initCameraLists();
//		initObjectLists();
		initVisLists();
		initAnimLists();

		boneShellRenderer = new BoneShellListCellRenderer(this.receivingModel, this.donatingModel);
	}

	public List<IdObjectShell<?>> getFutureBoneList() {
		futureBoneList.clear();
		ArrayList<IdObjectShell<?>> motionFromBones = new ArrayList<>();
		ArrayList<IdObjectShell<?>> receiveMotionBones = new ArrayList<>();
		ArrayList<IdObjectShell<?>> dontImportBones = new ArrayList<>();

		if (!clearExistingBones.isSelected()) {
			for (IdObjectShell<?> bs : recModBones) {
				switch (bs.getImportStatus()) {
					case IMPORT -> futureBoneList.add(bs);
					case MOTION_FROM -> motionFromBones.add(bs);
//					case RECEIVE_MOTION -> receiveMotionBones.add(bs);
					case RECEIVE_MOTION -> futureBoneList.add(bs);
					case DONT_IMPORT -> dontImportBones.add(bs);
				}
			}
		}

		for (IdObjectShell<?> bs : donModBones) {
			switch (bs.getImportStatus()) {
				case IMPORT -> futureBoneList.add(bs);
				case MOTION_FROM -> motionFromBones.add(bs);
//				case RECEIVE_MOTION -> receiveMotionBones.add(bs);
				case RECEIVE_MOTION -> futureBoneList.add(bs);
				case DONT_IMPORT -> dontImportBones.add(bs);
			}
		}
//		futureBoneList.addAll(receiveMotionBones);
		futureBoneList.addAll(dontImportBones);
		return futureBoneList;
	}

	public List<IdObjectShell<?>> getFutureBoneHelperList() {
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
					case IMPORT -> futureBoneHelperList.add(bs);
					case MOTION_FROM -> motionFromBones.add(bs);
					case DONT_IMPORT -> dontImportBones.add(bs);
				}
			}
		}
		for (IdObjectShell<?> bs : donModBoneShells) {
			switch (bs.getImportStatus()) {
				case IMPORT -> futureBoneHelperList.add(bs);
				case MOTION_FROM -> motionFromBones.add(bs);
				case DONT_IMPORT -> dontImportBones.add(bs);
			}
		}

		futureBoneHelperList.addAll(dontImportBones);
		System.out.println("futureBoneListEx size: " + futureBoneHelperList.size());

		return futureBoneHelperList;
	}

	long totalAddTime;

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

	public void selectSimilarVisSources() {
		// not sure this is correct
		for (final VisibilityShell<?> visibilityShell : allVisShells) {
			for (VisibilityShell<?> vs : donModVisibilityShells) {
				if (visibilityShell.getNameSource().getName().equals(vs.getNameSource().getName())) {
//					System.out.println(visibilityShell.getSource().getName());
					visibilityShell.setRecModAnimsVisSource(vs);
				}
			}
			for (VisibilityShell<?> vs : recModVisibilityShells) {
				if (visibilityShell.getNameSource().getName().equals(vs.getNameSource().getName())) {
//					System.out.println(visibilityShell.getSource().getName());
					visibilityShell.setRecModAnimsVisSource(vs);
				}
			}
		}
	}

	long addCount;
	long totalRemoveTime;
	long removeCount;


	private void initAnimLists() {
		for (Animation anim : receivingModel.getAnims()) {
			recModAnims.add(new AnimShell(anim, false));
		}

		for (Animation anim : donatingModel.getAnims()) {
			donModAnims.add(new AnimShell(anim, true));
		}
		allAnimShells.addAll(recModAnims);
		allAnimShells.addAll(donModAnims);
	}

	private void initIdObjectListsLists() {
		for (IdObject idObject : receivingModel.getIdObjects()) {
			IdObjectShell<?> shell = new IdObjectShell<>(idObject, false, receivingModel.getName(), true);
			if (idObject instanceof Helper) {
				recModBoneShells.add(shell);
			} else if (idObject instanceof Bone) {
				recModBones.add(shell);
				recModBoneShells.add(shell);
			} else {
				recModObjectShells.add(shell);
			}
			recModObjShellBiMap.put(idObject, shell);
		}
		for (IdObject idObject : donatingModel.getIdObjects()) {
			IdObjectShell<?> shell = new IdObjectShell<>(idObject, true, donatingModel.getName(), true);
			if (idObject instanceof Helper) {
				donModBoneShells.add(shell);
			} else if (idObject instanceof Bone) {
				donModBones.add(shell);
				donModBoneShells.add(shell);
			} else {
				donModObjectShells.add(shell);
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
			recModGeoShells.add(geoShell);
			geoset.reMakeMatrixList();
		}
		allGeoShells.addAll(recModGeoShells);

		for (Geoset geoset : donatingModel.getGeosets()) {
			GeosetShell geoShell = new GeosetShell(geoset, donatingModel, true);
			geoShell.setMatrixShells(createMatrixShells(geoset, donModObjShellBiMap, donatingModel, true));
			donModGeoShells.add(geoShell);
			geoset.reMakeMatrixList();
		}
		allGeoShells.addAll(donModGeoShells);

		recModMaterials.addAll(receivingModel.getMaterials());
		allMaterials.addAll(recModMaterials);

		donModMaterials.addAll(donatingModel.getMaterials());
		allMaterials.addAll(donModMaterials);
	}

	private void initCameraLists() {
		for (Camera obj : receivingModel.getCameras()) {
			recModCameraShells.add(new CameraShell(obj, false));
		}

		for (Camera obj : donatingModel.getCameras()) {
			donModCameraShells.add(new CameraShell(obj, true));
		}

		allCameraShells.addAll(recModCameraShells);
		allCameraShells.addAll(donModCameraShells);
	}

	// ToDo Fix MatrixShell for HD models!!
	private List<MatrixShell> createMatrixShells(Geoset geoset, BiMap<IdObject, IdObjectShell<?>> boneShells, EditableModel model, boolean isFromDonating) {
		List<MatrixShell> matrixShells = new ArrayList<>();
		if (geoset.isHD()) {
			for (Bone bone : geoset.getBoneMap().keySet()) {
				ArrayList<IdObjectShell<?>> orgBones = new ArrayList<>();
				orgBones.add(boneShells.get(bone));
				MatrixShell ms = allMatrixShellMap.computeIfAbsent(new Matrix(bone), matrix -> new MatrixShell(matrix, orgBones, isFromDonating, true));
				matrixShells.add(ms);
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
				matrixShells.add(ms);
			}
		}
		return matrixShells;
	}

	private void initVisLists() {
		for (Material material : recModMaterials) {
			for (Layer x : material.getLayers()) {
				recModVisibilityShells.add(new VisibilityShell<>(x, material.getName(), false));
			}
		}

		for (GeosetShell geoShell : recModGeoShells) {
			recModVisibilityShells.add(new VisibilityShell<>(geoShell.getGeoset(), geoShell.getModelName(), geoShell.isFromDonating()));
		}

		for (IdObjectShell<?> obj : recModObjectShells) {
			if (!(obj.getIdObject() instanceof CollisionShape || obj.getIdObject() instanceof EventObject)) {
				recModVisibilityShells.add(new VisibilityShell<>(obj.getIdObject(), receivingModel.getName(), obj.isFromDonating()));
			}
		}

		allVisShells.addAll(recModVisibilityShells);
		recModVisibilityShells.forEach(v -> recModVisShellBiMap.put(v.getSource(), v));
		allVisShellBiMap.putAll(recModVisShellBiMap);
		recModVisibilityShells.add(0, recAlwaysVis);
		recModVisibilityShells.add(1, recNeverVis);


		for (Material material : donModMaterials) {
			for (Layer x : material.getLayers()) {
				donModVisibilityShells.add(new VisibilityShell<>(x, material.getName(), true));
			}
		}

		for (GeosetShell geoShell : donModGeoShells) {
			donModVisibilityShells.add(new VisibilityShell<>(geoShell.getGeoset(), geoShell.getModelName(), geoShell.isFromDonating()));
		}

		for (IdObjectShell<?> obj : donModObjectShells) {
			if (!(obj.getIdObject() instanceof CollisionShape || obj.getIdObject() instanceof EventObject)) {
				donModVisibilityShells.add(new VisibilityShell<>(obj.getIdObject(), donatingModel.getName(), obj.isFromDonating()));
			}
		}

		allVisShells.addAll(donModVisibilityShells);
		donModVisibilityShells.forEach(v -> donModVisShellBiMap.put(v.getSource(), v));
		allVisShellBiMap.putAll(donModVisShellBiMap);
		donModVisibilityShells.add(0, donAlwaysVis);
		donModVisibilityShells.add(1, donNeverVis);
	}

	private Attachment getVisAttach(boolean donMod, boolean neverVis){
		FloatAnimFlag visFlag = new FloatAnimFlag(MdlUtils.TOKEN_VISIBILITY);
		String name = neverVis ? "Never Visible" : "Always Visible";
		Attachment attachment = new Attachment(name);
		if(neverVis){
			List<Animation> animations = donMod ? donatingModel.getAnims() : receivingModel.getAnims();
			for(Animation animation : animations){
				visFlag.addEntry(0, 0f, animation);
			}
			attachment.add(visFlag);
		}
		return attachment;
	}

	public List<VisibilityShell<?>> visibilityList() {
		futureVisComponents.clear();
		for (GeosetShell geoShell : allGeoShells) {
			for (Layer x : geoShell.getMaterial().getLayers()) {
				VisibilityShell<?> vs = allVisShellBiMap.get(x);
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.add(vs);
				}
			}
			if (geoShell.isDoImport()) {
				VisibilityShell<?> vs = allVisShellBiMap.get(geoShell.getGeoset());
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.add(vs);
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
				VisibilityShell<?> vs = allVisShellBiMap.get(op.getIdObject());
				if (vs != null && !futureVisComponents.contains(vs)) {
					futureVisComponents.add(vs);
				}
			}
		}
		return futureVisComponents;
	}

	public void fetchAndAddVisComp(List<? extends IdObject> idObjects) {
		for (IdObject x : idObjects) {
			VisibilityShell<?> vs = allVisShellBiMap.get(x);
			if (vs != null && !futureVisComponents.contains(vs)) {
				futureVisComponents.add(vs);
			}
		}
	}
}
