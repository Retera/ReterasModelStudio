package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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
			Map<GeosetShell, Geoset> newGeosetsMap = getNewGeosetsMap();
			for (Geoset geoset : newGeosetsMap.values()) {
				newModel.add(geoset);
				if (geoset.getGeosetAnim() != null) {
//					geoset.setGeosetAnim(null);
					newModel.add(geoset.getGeosetAnim());
				}
				if (!newModel.contains(geoset.getMaterial())) {
					Material material = geoset.getMaterial().deepCopy();
					geoset.setMaterial(material);
					newModel.add(material);
				}
			}

			Map<IdObjectShell<?>, IdObject> newObjectsMap = getNewIdObjectsMap();
			for (IdObject bone : newObjectsMap.values()) {
				newModel.add(bone);
			}
			collectIdObjectAnims(newObjectsMap);

			fixGeosetBones(newGeosetsMap, newObjectsMap);
//
//			for(Bitmap bitmap : mht.receivingModel.getTextures()){
//				newModel.add(bitmap);
//			}
//			for(Bitmap bitmap : mht.donatingModel.getTextures()){
//				newModel.add(bitmap);
//			}

			for (AnimShell animShell : mht.allAnimShells) {
				if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC
						|| animShell.getImportType() == AnimShell.ImportType.TIMESCALE_RECEIVE) {
					newModel.add(animShell.getAnim());
				}
			}

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
	private Map<IdObjectShell<?>, IdObject> getNewIdObjectsMap() {
		Map<IdObjectShell<?>, IdObject> newBoneMap = new HashMap<>();
		for (IdObjectShell<?> shell : mht.allBoneShells) {
			if (shell.getImportStatus() == IdObjectShell.ImportType.IMPORT
					|| shell.getImportStatus() == IdObjectShell.ImportType.RECEIVE_MOTION) {
				newBoneMap.put(shell, shell.getIdObject().copy()); // todo check instance of?
				if (!shell.getName().equals(shell.getIdObject().getName())) {
					System.out.println("shell: " + shell.getName() + " maps to " + shell.getIdObject().getName());
				}
			}
		}
		for (IdObjectShell<?> shell : mht.allObjectShells) {
			if (shell.getImportStatus() == IdObjectShell.ImportType.IMPORT
					|| shell.getImportStatus() == IdObjectShell.ImportType.RECEIVE_MOTION) {
				newBoneMap.put(shell, shell.getIdObject().copy()); // todo check instance of?
			}
		}
		for (IdObjectShell<?> idObjectShell : newBoneMap.keySet()) {
			IdObject newObject = newBoneMap.get(idObjectShell);
			newObject.setParent(newBoneMap.get(idObjectShell.getNewParentShell()));
			newObject.clearAnimFlags();
		}
		return newBoneMap;
	}

	private void collectIdObjectAnims(Map<IdObjectShell<?>, IdObject> newBoneMap) {
		for (IdObjectShell<?> idObjectShell : newBoneMap.keySet()) {
			IdObject newObject = newBoneMap.get(idObjectShell);

			for (AnimShell animShell : mht.allAnimShells) {
				if (animShell.getImportType() == AnimShell.ImportType.DONT_IMPORT) {
				} else {
					IdObjectShell<?> motionSrcShell = idObjectShell.getMotionSrcShell();

					if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
						if (motionSrcShell != null && animShell.isFromDonating() == motionSrcShell.isFromDonating()) {
							addIdObjectAnim(newObject, animShell, motionSrcShell, animShell.getAnim());
						} else if (animShell.isFromDonating() == idObjectShell.isFromDonating()) {
							addIdObjectAnim(newObject, animShell, idObjectShell, animShell.getAnim());
						}

					} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_RECEIVE) {
						AnimShell animDataSrc = animShell.getAnimDataSrc();
						// ToDo can be null! Match button not working!
						if (animDataSrc != null && motionSrcShell != null && animDataSrc.isFromDonating() == motionSrcShell.isFromDonating()) {
							addIdObjectAnim(newObject, animDataSrc, motionSrcShell, animShell.getAnim());
						} else if (animDataSrc != null && animDataSrc.isFromDonating() == idObjectShell.isFromDonating()) {
							addIdObjectAnim(newObject, animDataSrc, idObjectShell, animShell.getAnim());
						} else {
							if (motionSrcShell != null && animShell.isFromDonating() == motionSrcShell.isFromDonating()) {
								addIdObjectAnim(newObject, animShell, idObjectShell, animShell.getAnim());
							} else if (animShell.isFromDonating() == idObjectShell.isFromDonating()) {
								addIdObjectAnim(newObject, animShell, idObjectShell, animShell.getAnim());
							}
						}
					} else if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
					} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
					} else if (animShell.getImportType() == AnimShell.ImportType.GLOBALSEQ) {

					}
				}
			}
		}
	}


	private void collectVisAnims() {
		for (VisibilityShell visibilityShell : mht.allVisShells) {

			for (AnimShell animShell : mht.allAnimShells) {
				if (animShell.getImportType() == AnimShell.ImportType.DONT_IMPORT) {
				} else {

					if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {


					} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_RECEIVE) {

					} else if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
					} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
					} else if (animShell.getImportType() == AnimShell.ImportType.GLOBALSEQ) {

					}
				}
			}
		}
	}

	private void addIdObjectAnim(IdObject newBone, AnimShell animShell, IdObjectShell<?> motionSrcShell, Animation anim) {
		for (AnimFlag<?> srcFlag : motionSrcShell.getIdObject().getAnimFlags()) {
			if (srcFlag.hasSequence(animShell.getAnim())) {
				if (!newBone.has(srcFlag.getName())) {
					newBone.add(srcFlag.getEmptyCopy());
				}
				AnimFlag<?> destFlag = newBone.find(srcFlag.getName());
				AnimFlagUtils.copyFrom(destFlag, srcFlag, animShell.getAnim(), anim);
			}
		}
	}

	private Map<GeosetShell, Geoset> getNewGeosetsMap() {
		Map<GeosetShell, Geoset> newGeosetsMap = new HashMap<>();

		for (GeosetShell geoShell : mht.allGeoShells) {

			System.out.println("checking geoset: " + geoShell.getName() + ", should import: " + geoShell.isDoImport());
			if (geoShell.isDoImport()) {
				Geoset geoset = geoShell.getGeoset().deepCopy();
				geoset.setMaterial(geoShell.getMaterial());

				newGeosetsMap.put(geoShell, geoset);
			}
		}
		return newGeosetsMap;
	}

	private void fixGeosetBones(Map<GeosetShell, Geoset> newGeosetsMap, Map<IdObjectShell<?>, IdObject> newBoneMap) {
		// ToDo Fix MatrixShell for HD models!!
		for (GeosetShell geosetShell : newGeosetsMap.keySet()) {
			if (geosetShell.getName().contains("Face")) {
				System.out.println("doing Face!");
			}
			Map<Matrix, List<Bone>> matrixMap = new HashMap<>();
			Map<IdObject, IdObject> replacementBoneMap = new HashMap<>(); // <original bone, new Bone>
			Geoset geoset = newGeosetsMap.get(geosetShell);

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

//				for(int i = 0; i < matrixShell.getOrgBones().size(); i++){
//					IterableListModel<IdObjectShell<?>> shellNewBones = matrixShell.getNewBones();
//					if(!shellNewBones.isEmpty()) {
//						IdObjectShell<?> newShell = shellNewBones.get(Math.min(i, shellNewBones.size() - 1));
//						if(newShell != null){
//							replacementBoneMap.put(matrixShell.getOrgBones().get(i).getIdObject(), newBoneMap.get(newShell));
//						}
//					}
////					else if(replacementBoneMap.get(matrixShell.getOrgBones().get(i).getIdObject()) == null){
////						replacementBoneMap.put(matrixShell.getOrgBones().get(i).getIdObject(), newBoneMap.get(newBoneMap.keySet().stream().findAny().get()));
////					}


//				}
//				if (matrixShell.getNewBones().size() == matrixShell.getOrgBones().size()){
//				}

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
		model.getGeosetAnims().stream().filter(o -> o.owns(orgFlag)).forEach(o -> o.add(newGlobalSeqFlag));
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
				if (((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
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

	private void setNewVisSources(List<Animation> oldAnims, boolean clearAnims, List<Animation> newAnims) {
		final List<AnimFlag<Float>> finalVisFlags = new ArrayList<>();
		for (VisibilityShell visibilityShell : mht.futureVisComponents) {
			VisibilitySource temp = ((VisibilitySource) visibilityShell.getSource());
			AnimFlag<Float> visFlag = temp.getVisibilityFlag();// might be null
			AnimFlag<Float> newVisFlag;

			if (visFlag != null) {
				newVisFlag = visFlag.getEmptyCopy();
			} else {
				newVisFlag = new FloatAnimFlag(temp.visFlagName());
			}
			// newVisFlag = new AnimFlag(temp.visFlagName());

			FloatAnimFlag flagOld = getFloatAnimFlag(newVisFlag.tans(), oldAnims, visibilityShell.getDonModAnimsVisSource());
			FloatAnimFlag flagNew = getFloatAnimFlag(newVisFlag.tans(), newAnims, visibilityShell.getRecModAnimsVisSource());

			if (flagNew != null &&
					((visibilityShell.isFavorOld() && !visibilityShell.isFromDonating() && !clearAnims)
							|| !visibilityShell.isFavorOld() && (visibilityShell.isFromDonating()))) {
				// this is an element favoring existing animations over imported
				for (Animation a : oldAnims) {
					flagNew.deleteAnim(a);
				}
			} else if (flagOld != null) {
				// this is an element not favoring existing over imported
				for (Animation a : newAnims) {
					flagOld.deleteAnim(a);
				}
			}
			if (flagOld != null) {
				AnimFlagUtils.copyFrom(newVisFlag, flagOld);
			}
			if (flagNew != null) {
				AnimFlagUtils.copyFrom(newVisFlag, flagNew);
			}
			finalVisFlags.add(newVisFlag);
		}
		for (int i = 0; i < mht.futureVisComponents.size(); i++) {
			VisibilitySource visSource = ((VisibilitySource) mht.futureVisComponents.get(i).getSource());
			AnimFlag<Float> visFlag = finalVisFlags.get(i);// might be null
			if (visFlag.size() > 0) {
				visSource.setVisibilityFlag(visFlag);
			} else {
				visSource.setVisibilityFlag(null);
			}
		}
	}

	private FloatAnimFlag getFloatAnimFlag(boolean tans, List<Animation> anims, VisibilityShell source) {
		if (source != null) {
			if (source.isNeverVisible()) {
				FloatAnimFlag tempFlag = new FloatAnimFlag("temp");

				Entry<Float> invisEntry = new Entry<>(0, 0f);
				if (tans) invisEntry.unLinearize();

				for (Animation a : anims) {
					tempFlag.setOrAddEntryT(a.getStart(), invisEntry.deepCopy().setTime(a.getStart()), a);
				}
				return tempFlag;
			} else if (!source.isAlwaysVisible()) {
				return (FloatAnimFlag) ((VisibilitySource) source.getSource()).getVisibilityFlag();
			}
		}
		return null;
	}

	private List<Camera> addChosenCameras(EditableModel newModel) {
		List<Camera> camerasAdded = new ArrayList<>();
		for (CameraShell cameraShell : mht.allCameraShells) {
			if (cameraShell.getShouldImport() && cameraShell.getCamera() != null) {
				Camera camera = cameraShell.getCamera();
				newModel.add(camera);
				camerasAdded.add(camera);
			}
		}
		return camerasAdded;
	}

	private void applyNewMatrixBones(Map<GeosetShell, Geoset> geosetsAdded, Map<IdObjectShell<?>, IdObject> bonesAdded, EditableModel model) {
		Bone dummyBone = null;
		// ToDo this needs a map matrices to vertices or something to update vertex-bones correctly...
		for (GeosetShell geosetShell : geosetsAdded.keySet()) {
			Geoset geoset = geosetsAdded.get(geosetShell);
			geoset.reMakeMatrixList();
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

	private void copyMotionFromBones() {
		for (IdObjectShell<?> bs : mht.recModBoneShells) {
			if (bs.getMotionSrcShell() != null && bs.getMotionSrcShell().getImportStatus() == IdObjectShell.ImportType.MOTION_FROM) {
				copyMotionFrom3(bs.getIdObject(), bs.getMotionSrcShell().getIdObject());
			}
		}
	}
	public void copyMotionFrom3(IdObject receiving, IdObject donating) {
		for (AnimFlag<?> donFlag : donating.getAnimFlags()) {
			AnimFlag<?> recFlag = receiving.find(donFlag.getName());
			if(recFlag != null && (!donFlag.hasGlobalSeq() && !recFlag.hasGlobalSeq()
					|| donFlag.hasGlobalSeq() && donFlag.getGlobalSeq().equals(recFlag.getGlobalSeq()))){
				AnimFlagUtils.copyFrom(recFlag, donFlag);
			} else {
				receiving.add(donFlag.deepCopy());
			}
		}
	}

	private Map<IdObjectShell<?>, IdObject> addChosenNewBones(EditableModel newModel) {
		Map<IdObjectShell<?>, IdObject> bonesAdded = new HashMap<>();
		for (IdObjectShell<?> boneShell : mht.allBoneShells) {
			// we will go through all bone shells for this
			// Fix cross-model referencing issue (force clean parent node's list of children)
			if (boneShell.isFromDonating() || !mht.clearExistingBones.isSelected()) {
				switch (boneShell.getImportStatus()) {
					case IMPORT -> {
						System.out.println("adding bone: " + boneShell);
						Bone copy = (Bone) boneShell.getIdObject().copy(); // todo check instance of?
						copy.clearAnimFlags();
						newModel.add(copy);
						bonesAdded.put(boneShell, copy);
//						if (boneShell.getNewParentBs() != null) {
//							copy.setParent(boneShell.getNewParentBs().getBone());
//						} else {
//							copy.setParent(null);
//						}
					}
//					case MOTIONFROM -> {
//						Bone copy = boneShell.getBone().copy();
//						copy.clearAnimFlags();
//						bonesAdded.put(boneShell, copy);
//					}
//					case DONTIMPORT -> boneShell.getBone().setParent(null);

				}
			}
		}

		for (IdObjectShell<?> boneShell : bonesAdded.keySet()) {
			IdObject idObject = bonesAdded.get(boneShell);
			if (boneShell.getNewParentShell() != null) {
				idObject.setParent(bonesAdded.get(boneShell.getNewParentShell()));
			} else {
				idObject.setParent(null);
			}
		}

		return bonesAdded;
	}

	private Map<AnimShell, Animation> getAddedAnims(EditableModel newModel) {
		Map<AnimShell, Animation> addedAnims = new HashMap<>();
		for (AnimShell animShell : mht.allAnimShells) {
//			System.out.println("Anim: " + animShell.getName() + " should be dealt with " + animShell.getImportType() + " (" + animShell.getImportAnim() + ")");
			if (animShell.getImportType() != AnimShell.ImportType.DONT_IMPORT && animShell.getImportType() != AnimShell.ImportType.TIMESCALE_INTO) {
				Animation copy = animShell.getAnim().deepCopy();
				if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
					addedAnims.put(animShell, copy);
				} else if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
					copy.setName(animShell.getName());
				}
				newModel.add(copy);
				addedAnims.put(animShell, copy);
			}
		}
		return addedAnims;
	}


	private void animCopyToInterv1(List<AnimFlag<?>> animFlags, List<EventObject> eventObjects, List<AnimFlag<?>> newImpFlags, List<EventObject> newImpEventObjs, Animation anim1, Animation importAnim) {
//		importAnim.copyToInterval(start, start + length, anim1, animFlags, eventObjects, newImpFlags, newImpEventObjs);
		for (AnimFlag<?> af : newImpFlags) {
			if (!af.hasGlobalSeq()) {
				AnimFlag<?> source = animFlags.get(newImpFlags.indexOf(af));
				AnimFlagUtils.copyFrom(af, source, importAnim, anim1);
			}
		}
		for (EventObject e : newImpEventObjs) {
			if (!e.hasGlobalSeq()) {
				EventObject source = eventObjects.get(newImpEventObjs.indexOf(e));
				e.copyFrom(source, importAnim, anim1);
			}
		}
	}

	private void animCopyToInterv1(Map<AnimFlag<?>, AnimFlag<?>> flagMap, Map<EventObject, EventObject> eventMap, Animation anim1, Animation importAnim) {
		for (AnimFlag<?> source : flagMap.keySet()) {
			AnimFlag<?> af = flagMap.get(source);
			if (af != null && !af.hasGlobalSeq()) {
				AnimFlagUtils.copyFrom(af, source, importAnim, anim1);
			}
		}
		for (EventObject source : eventMap.keySet()) {
			EventObject e = eventMap.get(source);
			if (e != null && !e.hasGlobalSeq()) {
				e.copyFrom(source, importAnim, anim1);
			}
		}
	}

	private Map<GeosetShell, Geoset> addChosenGeosets(EditableModel newModel) {
		Map<GeosetShell, Geoset> geosetsAdded = new HashMap<>();
		Map<Material, Material> materials = new HashMap<>();

		for (GeosetShell geoShell : mht.allGeoShells) {

			System.out.println("checking geoset: " + geoShell.getName() + ", should import: " + geoShell.isDoImport());
			if (geoShell.isDoImport()) {
				Geoset geoset = geoShell.getGeoset().deepCopy();
				Material material = materials.computeIfAbsent(geoShell.getMaterial(), k -> geoShell.getMaterial().deepCopy());
				geoset.setMaterial(material);
				newModel.add(geoset);

				geosetsAdded.put(geoShell, geoset);

				if (geoset.getGeosetAnim() != null) {
					newModel.add(geoset.getGeosetAnim());
				}
			}
		}

		for (Material material : materials.values()) {
			newModel.add(material);
		}
		return geosetsAdded;
	}

	private List<Geoset> getGeosetsRemoved() {
		List<Geoset> geosetsRemoved = new ArrayList<>();

		for (GeosetShell geoShell : mht.recModGeoShells) {

			if (!geoShell.isDoImport()) {
				if (geoShell.getGeoset().getGeosetAnim() != null) {
					mht.receivingModel.remove(geoShell.getGeoset().getGeosetAnim());
				}
				geosetsRemoved.add(geoShell.getGeoset());
				mht.receivingModel.remove(geoShell.getGeoset());
			} else {
				geoShell.getGeoset().setMaterial(geoShell.getMaterial());
			}
		}
		return geosetsRemoved;
	}

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

