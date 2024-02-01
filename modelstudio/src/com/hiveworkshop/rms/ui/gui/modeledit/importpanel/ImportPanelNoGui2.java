package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.*;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * The panel to handle the import function.
 *
 * Eric Theller 6/11/2012
 */
public class ImportPanelNoGui2 extends JTabbedPane {
	boolean importSuccess = false;
	boolean importStarted = false;
	boolean importEnded = false;

	private ModelStructureChangeListener changeListener1;

	ModelHolderThing mht;

	public ImportPanelNoGui2(EditableModel receivingModel, EditableModel donatingModel) {
		this(new ModelHolderThing(receivingModel, donatingModel));
	}

	public ImportPanelNoGui2(ModelHolderThing mht) {
		super();
		this.mht = mht;
		if (mht.receivingModel.getName().equals(mht.donatingModel.getName())) {
			mht.donatingModel.setFileRef(new File(mht.donatingModel.getFile().getParent() + "/" + mht.donatingModel.getName() + " (Imported)" + ".mdl"));
		}
		TempSaveModelStuff.doSavePreps(mht.receivingModel);
	}

	public EditableModel doImport() {
		importStarted = true;
		EditableModel newModel = new EditableModel("Imp");
		newModel.setFormatVersion(mht.receivingModel.getFormatVersion());
		newModel.setBlendTime(mht.receivingModel.getBlendTime());
		newModel.setExtents(mht.receivingModel.getExtents());
		try {
			Map<AnimShell, Animation> animsToAdd = getAnimsToAdd();
			animsToAdd.values().forEach(newModel::add);

			Map<Material, Material> materialMap = getMaterialMap();
			materialMap.values().forEach(newModel::add);

			Map<IdObjectShell<?>, IdObject> newObjectsMap = getNewIdObjectsMap(animsToAdd);
			newObjectsMap.values().forEach(newModel::add);

			newObjectsMap.values().stream()
					.filter(o -> o instanceof RibbonEmitter).map(r -> (RibbonEmitter) r)
					.forEach(r -> r.setMaterial(materialMap.get(r.getMaterial())));

			Map<GeosetShell, Geoset> newGeosetsMap = getNewGeosetsMap(materialMap, newObjectsMap);
			newGeosetsMap.values().forEach(newModel::add);

			System.out.println("VisShell mappings: " + mht.allVisShellBiMap.size());
			for (TimelineContainer tc : mht.allVisShellBiMap.keys()) {
				VisibilityShell<?> visibilityShell = mht.allVisShellBiMap.get(tc);
				if (tc instanceof Named) {
					System.out.println("VisShell for \"" + ((Named) tc).getName() + "\": " + visibilityShell);
				} else if (tc != null) {
					System.out.println("VisShell for \"" + tc.getClass().getSimpleName() + "\": " + visibilityShell);
				}
			}

			newGeosetsMap.keySet().forEach(g -> doVisStuff(g.getGeoset(), newGeosetsMap.get(g), animsToAdd));
//			newGeosetsMap.values().forEach(g -> doVisStuff(g, animsToAdd));

			materialMap.values().forEach(m -> m.getLayers().forEach(l -> doVisStuff(l, animsToAdd)));


			for (CameraShell cameraShell : mht.allCameraShells) {
				if (cameraShell.getShouldImport()) {
					newModel.add(cameraShell.getCamera().deepCopy());
				}
			}

			importSuccess = true;


			if (changeListener1 != null) {
				changeListener1.geosetsUpdated();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}

		importEnded = true;
		return newModel;
	}

	/**
	 * Goes through all BoneShells and creates copies of all bones to be imported,
	 * after which it sets the parents of these bones from the copies and
	 * adds/removes/replaces animation data
	 *
	 * @return A map from BoneShell to a copy of the bone to import
	 */
	private Map<IdObjectShell<?>, IdObject> getNewIdObjectsMap(Map<AnimShell, Animation> animsToAdd) {
		Map<IdObjectShell<?>, IdObject> newBoneMap = new HashMap<>();
		for (IdObjectShell<?> shell : mht.allBoneShells) {
			if (shell.getShouldImport()) {
				newBoneMap.put(shell, shell.getIdObject().copy()); // todo check instance of?
				if (!shell.getName().equals(shell.getIdObject().getName())) {
					System.out.println("shell: " + shell.getName() + " maps to " + shell.getIdObject().getName());
				}
			}
		}
		for (IdObjectShell<?> shell : mht.allObjectShells) {
			if (shell.getShouldImport()) {
				newBoneMap.put(shell, shell.getIdObject().copy()); // todo check instance of?
			}
		}
		for (IdObjectShell<?> idObjectShell : newBoneMap.keySet()) {
			IdObject newObject = newBoneMap.get(idObjectShell);
			newObject.setParent(newBoneMap.get(idObjectShell.getNewParentShell()));
			newObject.clearAnimFlags();
			copyAnims(newObject, idObjectShell, animsToAdd);
		}
		return newBoneMap;
	}

	private Map<AnimShell, Animation> getAnimsToAdd() {
		Map<AnimShell, Animation> animationMap = new LinkedHashMap<>();
		for (AnimShell animShell : mht.allAnimShells) {
			if (animShell.isDoImport()) {
				animationMap.put(animShell, animShell.getAnim().deepCopy());
			}
		}
		return animationMap;
	}

	private void copyAnims(IdObject newObject, IdObjectShell<?> idObjectShell, Map<AnimShell, Animation> animsToAdd) {
		boolean prioSelf = idObjectShell.isPrioritizeMotionFromSelf();
		for (AnimShell animShell : animsToAdd.keySet()) {
			Animation anim = animsToAdd.get(animShell);

			IdObjectShell<?> node = getNodeToUse(idObjectShell, animShell);

			AnimShell animSrc = animShell.getAnimDataSrc();

			if (sameModel(node, animSrc)) {
				addIdObjectAnim(newObject, animSrc, node, anim);
			} else if (sameModel(node, animShell)) {
				addIdObjectAnim(newObject, animShell, node, anim);
			}

		}
	}
	private IdObjectShell<?> getNodeToUse(IdObjectShell<?> idObjectShell, AnimShell animShell) {
		boolean prioSelf = idObjectShell.isPrioritizeMotionFromSelf();
		IdObjectShell<?> prioNode = prioSelf ? idObjectShell : idObjectShell.getMotionSrcShell();
		IdObjectShell<?> altNode = prioSelf ? idObjectShell.getMotionSrcShell() : idObjectShell;

		AnimShell animSrc = animShell.getAnimDataSrc();
		if (sameModel(prioNode, animSrc) || sameModel(prioNode, animShell)) {
			return prioNode;
		} else if (sameModel(altNode, animSrc) || sameModel(altNode, animShell)) {
			return altNode;
		} else {
			return idObjectShell;
		}
	}
	private boolean sameModel(IdObjectShell<?> idObjectShell, AnimShell animShell) {
		return idObjectShell != null && animShell != null && idObjectShell.isFromDonating() == animShell.isFromDonating();
	}

	private void addIdObjectAnim(IdObject newBone, AnimShell animShell, IdObjectShell<?> motionSrcShell, Animation anim) {
		for (AnimFlag<?> srcFlag : motionSrcShell.getIdObject().getAnimFlags()) {
			if (srcFlag.hasSequence(animShell.getAnim())) {
				if (!newBone.has(srcFlag.getName())) {
					newBone.add(srcFlag.getEmptyCopy());
				}
				AnimFlag<?> destFlag = newBone.find(srcFlag.getName());
				AnimFlagUtils.copyFrom(destFlag, srcFlag, animShell.getAnim(), anim);
			} else if (srcFlag.hasGlobalSeq()) {
				if (!newBone.has(srcFlag.getName())) {
					newBone.add(srcFlag.getEmptyCopy());
				}
				GlobalSeq globalSeq = srcFlag.getGlobalSeq();
				AnimFlag<?> destFlag = newBone.find(srcFlag.getName());
				AnimFlagUtils.copyFrom(destFlag, srcFlag, globalSeq, globalSeq.deepCopy());
				break;
			}
		}
	}

	private Map<GeosetShell, Geoset> getNewGeosetsMap(Map<Material, Material> materialMap, Map<IdObjectShell<?>, IdObject> newBoneMap) {
		Map<GeosetShell, Geoset> newGeosetsMap = new HashMap<>();

		for (GeosetShell geoShell : mht.allGeoShells) {

			System.out.println("checking geoset: " + geoShell.getName() + ", should import: " + geoShell.isDoImport());
			if (geoShell.isDoImport()) {
				Geoset geoset = geoShell.getGeoset().deepCopy();
				geoset.setMaterial(materialMap.get(geoShell.getMaterial()));

				newGeosetsMap.put(geoShell, geoset);

				if (geoShell.hasSkinBones()) {
					Map<IdObject, IdObject> replacementBoneMap = getReplacementBoneMap(geoShell, newBoneMap);

					for (GeosetVertex vertex : geoset.getVertices()) {
						vertex.replaceBones(replacementBoneMap);
					}
				} else {
					Map<Matrix, List<Bone>> matrixMap = getMatrixListMap(geoShell, newBoneMap);

					for (GeosetVertex vertex : geoset.getVertices()) {
						List<Bone> newBones = matrixMap.get(vertex.getMatrix());
						vertex.clearBoneAttachments();
						vertex.addBoneAttachments(newBones);
					}
				}
			}
		}
		return newGeosetsMap;
	}

	private Map<Matrix, List<Bone>> getMatrixListMap(GeosetShell geosetShell, Map<IdObjectShell<?>, IdObject> newBoneMap) {
		Map<Matrix, List<Bone>> matrixMap = new HashMap<>();
		System.out.println("geo matrixes: " + geosetShell.getMatrixShells().size());
		for (MatrixShell matrixShell : geosetShell.getMatrixShells()) {
//			System.out.println("matrixShell: " + matrixShell);
//			System.out.println("matrixShell-NewBones: " + matrixShell.getNewBones().size());
//			System.out.println("matrixShell-OldBones: " + matrixShell.getOrgBones().size());
			List<Bone> newBones = new ArrayList<>();
			for (IdObjectShell<?> shell : matrixShell.getNewBones()) {
				if (newBoneMap.get(shell) instanceof Bone) {
					newBones.add((Bone) newBoneMap.get(shell));
				}
			}
			matrixMap.put(matrixShell.getMatrix(), newBones);
			if (matrixShell.getOrgBones().size() == 0) {
				System.out.println("no org bones!");
			}
			if (matrixShell.getNewBones().size() == 0) {
				System.out.println("no new bones!");
			}
			if (newBones.size() == 0) {
				System.out.println("new bones empty!");
			}
		}
		return matrixMap;
	}

	private Map<IdObject, IdObject> getReplacementBoneMap(GeosetShell geosetShell, Map<IdObjectShell<?>, IdObject> newBoneMap) {
		Map<IdObject, IdObject> replacementBoneMap = new HashMap<>(); // <original bone, new Bone>
		for (MatrixShell matrixShell : geosetShell.getMatrixShells()) {
			IdObjectShell<?> replacementBone = matrixShell.getHdBoneToUse();
			IdObjectShell<?> orgBone = matrixShell.getHdBoneToMapFrom();
			if (matrixShell.isHd() && replacementBone != null && orgBone != null && newBoneMap.get(replacementBone) != null) {
				replacementBoneMap.put(orgBone.getIdObject(), newBoneMap.get(replacementBone));
			} else {
				for (IdObjectShell<?> boneShell : matrixShell.getOrgBones()) {
					if (newBoneMap.get(boneShell) != null) {
						replacementBoneMap.put(boneShell.getIdObject(), newBoneMap.get(boneShell));
					} else {
						System.out.println("couldn't find replacement bone for: " + boneShell.getName());
					}
				}
				for (IdObjectShell<?> boneShell : matrixShell.getNewBones()) {
					if (newBoneMap.get(boneShell) != null) {
						replacementBoneMap.put(boneShell.getIdObject(), newBoneMap.get(boneShell));
					} else {
						System.out.println("couldn't find replacement bone for: " + boneShell.getName());
					}
				}
			}
		}
		return replacementBoneMap;
	}



	private void doVisStuff(TimelineContainer visDest, Map<AnimShell, Animation> animsToAdd) {
		VisibilityShell<?> visibilityShell = mht.allVisShellBiMap.get(visDest);

		if (visibilityShell != null) {
			System.out.println(visDest + " vis binding");
			for (AnimShell animShell : animsToAdd.keySet()) {
				Animation anim = animsToAdd.get(animShell);

				VisibilityShell<?> visToUse = getVisToUse(visibilityShell, animShell);

				AnimShell animSrc = animShell.getAnimDataSrc();
				if (sameModel(visToUse, animSrc)) {
					addVisAnim(visDest, animSrc, visToUse, anim);
				} else if (sameModel(visToUse, animShell)) {
					addVisAnim(visDest, animShell, visToUse, anim);
				}

			}
		} else {
			System.out.println(visDest + " had no vis binding");
		}


	}
	private void doVisStuff(TimelineContainer orgObj, TimelineContainer visDest, Map<AnimShell, Animation> animsToAdd) {
		VisibilityShell<?> visibilityShell = mht.allVisShellBiMap.get(orgObj);

		if (visibilityShell != null) {
			System.out.println(visDest + " vis binding");
			for (AnimShell animShell : animsToAdd.keySet()) {
				Animation anim = animsToAdd.get(animShell);

				VisibilityShell<?> visToUse = getVisToUse(visibilityShell, animShell);

				AnimShell animSrc = animShell.getAnimDataSrc();
				if (sameModel(visToUse, animSrc)) {
					addVisAnim(visDest, animSrc, visToUse, anim);
				} else if (sameModel(visToUse, animShell)) {
					addVisAnim(visDest, animShell, visToUse, anim);
				}

			}
		} else {
			System.out.println(visDest + " had no vis binding");
		}


	}

	private VisibilityShell<?> getVisToUse(VisibilityShell<?> visibilityShell, AnimShell animShell) {
		boolean prioSelf = visibilityShell.isFavorOld();
		VisibilityShell<?> prioVis = prioSelf ? visibilityShell : visibilityShell.getVisSource();
		VisibilityShell<?> altVis = prioSelf ? visibilityShell.getVisSource() : visibilityShell;

		AnimShell animSrc = animShell.getAnimDataSrc();
		if (sameModel(prioVis, animSrc) || sameModel(prioVis, animShell)) {
			return prioVis;
		} else if (sameModel(altVis, animSrc) || sameModel(altVis, animShell)) {
			return altVis;
		} else {
			return visibilityShell;
		}
	}
	private boolean sameModel(VisibilityShell<?> idObjectShell, AnimShell animShell) {
		return idObjectShell != null && animShell != null && idObjectShell.isFromDonating() == animShell.isFromDonating();
	}

	private void addVisAnim(TimelineContainer visDest, AnimShell animShell, VisibilityShell<?> motionSrcShell, Animation anim) {
		AnimFlag<Float> visibilityFlag = motionSrcShell.getSource().getVisibilityFlag();
		if (visibilityFlag != null && visibilityFlag.hasSequence(animShell.getAnim())) {
			if (!visDest.has(visDest.visFlagName())) {
				AnimFlag<Float> emptyCopy = visibilityFlag.getEmptyCopy();
				emptyCopy.setName(visDest.visFlagName());
				visDest.add(emptyCopy);
			}
			AnimFlag<?> destFlag = visDest.find(visDest.visFlagName());
			AnimFlagUtils.copyFrom(destFlag, visibilityFlag, animShell.getAnim(), anim);
		}
	}

	private Map<Material, Material> getMaterialMap() {
		Map<Material, Material> materialMap = new HashMap<>();
		for (GeosetShell geoShell : mht.allGeoShells) {
			if (geoShell.isDoImport()) {
				materialMap.computeIfAbsent(geoShell.getMaterial(), k -> geoShell.getMaterial().deepCopy());
			}
		}
		for (IdObjectShell<?> idObjectShell : mht.allObjectShells) {
			if (idObjectShell.getShouldImport() && idObjectShell.getIdObject() instanceof RibbonEmitter ribbon) {
				materialMap.computeIfAbsent(ribbon.getMaterial(), k -> ribbon.getMaterial().deepCopy());

			}
		}
		return materialMap;
	}

	private void fixGeosetBones(Map<GeosetShell, Geoset> newGeosetsMap, Map<IdObjectShell<?>, IdObject> newBoneMap) {
		// ToDo Fix MatrixShell for HD models!!
		for (GeosetShell geosetShell : newGeosetsMap.keySet()) {
			if (geosetShell.getName().contains("Face")) {
				System.out.println("doing Face!");
			}
			Geoset geoset = newGeosetsMap.get(geosetShell);
			Map<Matrix, List<Bone>> matrixMap = new HashMap<>();
			Map<IdObject, IdObject> replacementBoneMap = new HashMap<>(); // <original bone, new Bone>

			System.out.println("importing geoset: " + geosetShell.getName() + ", MatShells: " + geosetShell.getMatrixShells().size());
			for (MatrixShell matrixShell : geosetShell.getMatrixShells()) {
				List<Bone> newBones = new ArrayList<>();
				matrixShell.getNewBones().forEach(bs -> newBones.add((Bone) newBoneMap.get(bs)));
				matrixMap.put(matrixShell.getMatrix(), newBones);
				if (matrixShell.getOrgBones().size() == 0) {
					System.out.println("no org bones!");
				}
				if (matrixShell.getNewBones().size() == 0) {
					System.out.println("no new bones!");
				}
				if (newBones.size() == 0) {
					System.out.println("new bones empty!");
				}
				IdObjectShell<?> replacementBone = matrixShell.getHdBoneToUse();
				IdObjectShell<?> orgBone = matrixShell.getHdBoneToMapFrom();
				if (matrixShell.isHd() && replacementBone != null && orgBone != null && newBoneMap.get(replacementBone) != null) {
					replacementBoneMap.put(orgBone.getIdObject(), newBoneMap.get(replacementBone));
				} else {
					for (IdObjectShell<?> boneShell : matrixShell.getOrgBones()) {
						if (newBoneMap.get(boneShell) != null) {
							replacementBoneMap.put(boneShell.getIdObject(), newBoneMap.get(boneShell));
						} else {
							System.out.println("couldn't find replacement bone for: " + boneShell.getName());
						}
					}
					for (IdObjectShell<?> boneShell : matrixShell.getNewBones()) {
						if (newBoneMap.get(boneShell) != null) {
							replacementBoneMap.put(boneShell.getIdObject(), newBoneMap.get(boneShell));
						} else {
							System.out.println("couldn't find replacement bone for: " + boneShell.getName());
						}
					}
				}

			}
//			System.out.println("replacement bone map size: " + replacementBoneMap.size() + ", newBones size: " + );
			int newBonesSet = 0;
			int bonesReplaced = 0;
			int verts = 0;
			for (GeosetVertex vertex : geoset.getVertices()) {
				verts++;
				List<Bone> newBones = matrixMap.get(vertex.getMatrix());
				if (newBones != null) {
					vertex.clearBoneAttachments();
					vertex.addBoneAttachments(newBones);
					newBonesSet++;
				} else {
					vertex.replaceBones(replacementBoneMap);
					bonesReplaced++;
				}
			}

			System.out.println("for " + verts + " vertices " + newBonesSet + " bone attatchments were added and " + bonesReplaced + " bone replacements done");
		}
	}

	private void setNewEventTracks(Map<AnimShell, Animation> addedAnims, EventObject eventObject) {
		for (AnimShell animShell : addedAnims.keySet()) {
			Animation animation = addedAnims.get(animShell);
			Animation oldAnim = animShell.getAnim();
			Animation impAnim = animShell.getAnimDataSrcAnim();

			boolean reverse = false;

			TreeSet<Integer> entryMapCopy = null;
			if (eventObject.getEventTrack(oldAnim) != null) {
				reverse = animShell.isReverse();
				entryMapCopy = eventObject.getEventTrack(oldAnim);
			} else if (impAnim != null && eventObject.getEventTrack(impAnim) != null) {
				reverse = animShell.getAnimDataSrc().isReverse();
				entryMapCopy = eventObject.getEventTrack(impAnim);
			}
			if (entryMapCopy != null) {
				eventObject.setSequence(animation, entryMapCopy);
				if (reverse) {
					eventObject.timeScale(animation, -oldAnim.getLength(), oldAnim.getLength());
				} else {
					eventObject.timeScale(animation, oldAnim.getLength(), 0);
				}
			}
		}
	}

	private <Q> AnimFlag<Q> getNewAnimFlag(Map<AnimShell, Animation> addedAnims, AnimFlag<Q> animFlag) {
		AnimFlag<Q> copy = animFlag.getEmptyCopy();
		for (AnimShell animShell : addedAnims.keySet()) {
			Animation animation = addedAnims.get(animShell);
			Animation oldAnim = animShell.getAnim();
			Animation impAnim = animShell.getAnimDataSrcAnim();

			boolean reverse = false;
			TreeMap<Integer, Entry<Q>> entryMapCopy = null;
			if (animFlag.getEntryMap(oldAnim) != null) {
				reverse = animShell.isReverse();
				entryMapCopy = animFlag.getSequenceEntryMapCopy(oldAnim);
			} else if (impAnim != null && animFlag.getEntryMap(impAnim) != null) {
				reverse = animShell.getAnimDataSrc().isReverse();
				entryMapCopy = animFlag.getSequenceEntryMapCopy(impAnim);
			}
			if (entryMapCopy != null) {
				copy.addEntryMap(animation, entryMapCopy);
				if (reverse) {
					AnimFlagUtils.timeScale2(copy, animation, -oldAnim.getLength(), oldAnim.getLength());
				} else {
					AnimFlagUtils.timeScale2(copy, animation, oldAnim.getLength(), 0);
				}
			}
		}
		return copy;
	}

	public static void buildGlobSeqFrom(EditableModel model, Animation anim, List<AnimFlag<?>> flags) {
		GlobalSeq newSeq = new GlobalSeq(anim.getLength());
		for (AnimFlag<?> af : flags) {
			if (!af.hasGlobalSeq()) {
				AnimFlag<?> newGlobalSeqFlag = af.deepCopy();
				newGlobalSeqFlag.setGlobSeq(newSeq);
				AnimFlagUtils.copyFrom(newGlobalSeqFlag, af, anim, newSeq);
				addFlagToParent(model, af, newGlobalSeqFlag);
			}
		}
	}

	public static void addFlagToParent(EditableModel model, AnimFlag<?> orgFlag, AnimFlag<?> newGlobalSeqFlag) {
		// orgFlag is the original flag that should exist in the parent
		// ADDS "newGlobalSeqFlag" TO THE PARENT OF "orgFlag"
		for (Material material : model.getMaterials()) {
			material.getLayers().stream().filter(layer -> layer.owns(orgFlag)).forEach(layer -> layer.add(newGlobalSeqFlag));
		}

		model.getTexAnims().stream().filter(o -> o.owns(orgFlag)).forEach(o -> o.add(newGlobalSeqFlag));
		model.getGeosets().stream().filter(o -> o.owns(orgFlag)).forEach(o -> o.add(newGlobalSeqFlag));
		model.getIdObjects().stream().filter(o -> o.owns(orgFlag)).forEach(o -> o.add(newGlobalSeqFlag));
		model.getCameras().stream().filter(o -> o.getSourceNode().owns(orgFlag)).forEach(o -> o.getSourceNode().add(newGlobalSeqFlag));

	}

	/**
	 * Provides a Runnable to the ImportPanel that will be run after the import has
	 * ended successfully.
	 */
	public void setModelChangeListener(final ModelStructureChangeListener changeListener) {
		this.changeListener1 = changeListener;
	}

	private void doClearAnims(List<AnimFlag<?>> recModFlags, List<EventObject> recModEventObjs) {
		for (Animation anim : mht.receivingModel.getAnims()) {
			for (AnimFlag<?> af : recModFlags) {
				if (af.getName().equals(MdlUtils.TOKEN_SCALING)
						|| af.getName().equals(MdlUtils.TOKEN_ROTATION)
						|| af.getName().equals(MdlUtils.TOKEN_TRANSLATION)) {
					// !af.hasGlobalSeq && was above before
					af.deleteAnim(anim);
				}
			}
			for (EventObject e : recModEventObjs) {
				e.deleteAnim(anim);
			}
		}
		mht.receivingModel.clearAnimations();
	}


	private void applyNewMatrixBones(Map<GeosetShell, Geoset> geosetsAdded, Map<IdObjectShell<?>, IdObject> bonesAdded, EditableModel model) {
		Bone dummyBone = null;
		// ToDo this needs a map matrices to vertices or something to update vertex-bones correctly...
		for (GeosetShell geosetShell : geosetsAdded.keySet()) {
			Geoset geoset = geosetsAdded.get(geosetShell);
			Map<Matrix, List<GeosetVertex>> matrixVertexMap = getMatrixListMap(geoset);

			for (MatrixShell matrixShell : geosetShell.getMatrixShells()) {
				System.out.println("new bones: " + matrixShell.getNewBones().size());
				List<GeosetVertex> vertexList = matrixVertexMap.get(matrixShell.getMatrix());
				Set<Bone> matrixBones = new HashSet<>();
				for (IdObjectShell<?> bs : matrixShell.getNewBones()) {
					IdObject idObject = bonesAdded.get(bs);
					if (idObject instanceof Bone) {
						matrixBones.add((Bone) idObject);
					}
				}

//				if (matrix.getBones().size() < 1) {
				if (matrixBones.size() < 1) {
					dummyBone = getDummyBone(dummyBone);
					matrixBones.add(dummyBone);
				}
				if (vertexList != null) {
					for (GeosetVertex vertex : vertexList) {
						vertex.clearBoneAttachments();
						for (Bone bone : matrixBones) {
							vertex.addBoneAttachment(bone);
						}
					}
				} else {
					System.out.println("couldn't find vertices for Matrix " + matrixShell.getMatrix());
				}
			}
			geoset.reMakeMatrixList();
		}
		if (dummyBone != null) {
			model.add(dummyBone);
		}

	}

	private Map<Matrix, List<GeosetVertex>> getMatrixListMap(Geoset geoset) {
		Map<Matrix, List<GeosetVertex>> matrixVertexMap = new HashMap<>();
		for (GeosetVertex vertex : geoset.getVertices()) {
			matrixVertexMap.computeIfAbsent(vertex.getMatrix(), k -> new ArrayList<>()).add(vertex);
		}
		return matrixVertexMap;
	}

	private Bone getDummyBone(Bone dummyBone) {
		if (dummyBone == null) {
			dummyBone = new Bone();
			dummyBone.setName("Bone_MatrixEaterDummy" + (int) (Math.random() * 2000000000));
			dummyBone.setPivotPoint(new Vec3(0, 0, 0));
		}
		return dummyBone;
	}

//	public void applyMatricesToVertices(Geoset geoset, EditableModel mdlr) {
////		System.out.println("applyMatricesToVertices");
//		for (GeosetVertex gv : geoset.getVertices()) {
//			gv.clearBoneAttachments(); //Todo check if this is broken
//			int vertexGroup = gv.getVertexGroup();
//			Matrix mx = geoset.getMatrix(vertexGroup);
//			if (((vertexGroup == -1) || (mx == null))) {
//				if (!ModelUtils.isTangentAndSkinSupported(mdlr.getFormatVersion())) {
//					throw new IllegalStateException("You have empty vertex groupings but FormatVersion is 800. Did you load HD mesh into an SD model?");
//				}
//			} else {
////				mx.updateIds(mdlr);
//				mx.cureBones(mdlr);
//				for (Bone bone : mx.getBones()) {
//					gv.addBoneAttachment(bone);
//				}
//			}
//		}
//	}


	public boolean importSuccessful() {
		return importSuccess;
	}

	public boolean importStarted() {
		return importStarted;
	}

	public boolean importEnded() {
		return importEnded;
	}
}

