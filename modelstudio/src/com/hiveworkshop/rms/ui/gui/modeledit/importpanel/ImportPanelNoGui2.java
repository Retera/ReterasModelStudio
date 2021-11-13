package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
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
	public static final ImageIcon animIcon = RMSIcons.animIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/anim_small.png"));
	public static final ImageIcon boneIcon = RMSIcons.boneIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Bone_small.png"));
	public static final ImageIcon geoIcon = RMSIcons.geoIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/geo_small.png"));
	public static final ImageIcon objIcon = RMSIcons.objIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Obj_small.png"));
	public static final ImageIcon greenIcon = RMSIcons.greenIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Blank_small.png"));
	public static final ImageIcon redIcon = RMSIcons.redIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankRed_small.png"));
	public static final ImageIcon orangeIcon = RMSIcons.orangeIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankOrange_small.png"));
	public static final ImageIcon cyanIcon = RMSIcons.cyanIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankCyan_small.png"));
	public static final ImageIcon redXIcon = RMSIcons.redXIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/redX.png"));
	public static final ImageIcon greenArrowIcon = RMSIcons.greenArrowIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/greenArrow.png"));
	public static final ImageIcon moveUpIcon = RMSIcons.moveUpIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/moveUp.png"));
	public static final ImageIcon moveDownIcon = RMSIcons.moveDownIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/moveDown.png"));

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
			// The engine for actually performing the model to model import.

			if (mht.receivingModel == mht.donatingModel) {
				JOptionPane.showMessageDialog(null, "The program has confused itself.");
			}

//			if (mht.clearExistingBones.isSelected()) {
//				clearRecModBoneAndHelpers();
//			}

			Map<BoneShell, IdObject> bonesAdded = addChosenNewBones(newModel);
			Map<ObjectShell, IdObject> objectsAdded = addChosenObjects(newModel, bonesAdded);
			Map<GeosetShell, Geoset> geosetsAdded = addChosenGeosets(newModel);
			Map<AnimShell, Animation> addedAnims = getAddedAnims(newModel);

			addChosenCameras(newModel);

			applyAnimations(bonesAdded, objectsAdded, geosetsAdded, addedAnims);

//			List<Animation> oldAnims = new ArrayList<>(mht.receivingModel.getAnims());
//
//			List<AnimFlag<?>> recModFlags = ModelUtils.getAllAnimFlags(mht.receivingModel);
//			List<AnimFlag<?>> donModFlags = ModelUtils.getAllAnimFlags(mht.donatingModel);
//
//			List<EventObject> recModEventObjs = mht.receivingModel.getEvents();
//			List<EventObject> donModEventObjs = mht.donatingModel.getEvents();
//
////			if (mht.clearRecModAnims.isSelected()) {
////				doClearAnims(recModFlags, recModEventObjs);
////			}
//
//
//			List<Animation> newAnims = getNewAnimations(donModFlags, donModEventObjs, newModel);
//
//			if (!mht.clearExistingBones.isSelected()) {
//				copyMotionFromBones();
//			}
			applyNewMatrixBones(geosetsAdded, bonesAdded, newModel);

//			setNewVisSources(oldAnims, mht.clearRecModAnims.isSelected(), newAnims);

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

	private void applyAnimations(Map<BoneShell, IdObject> bonesAdded, Map<ObjectShell, IdObject> objectsAdded, Map<GeosetShell, Geoset> geosetsAdded, Map<AnimShell, Animation> addedAnims) {
		for (BoneShell boneShell : bonesAdded.keySet()) {
			IdObject idObject = bonesAdded.get(boneShell);
			ArrayList<AnimFlag<?>> animFlags;
			if (boneShell.getImportBone() != null) {
				animFlags = boneShell.getImportBone().getAnimFlags();
			} else {
				animFlags = boneShell.getBone().getAnimFlags();
			}
			for (AnimFlag<?> animFlag : animFlags) {
				idObject.add(getNewAnimFlag(addedAnims, animFlag));
			}
		}
		for (ObjectShell objectShell : objectsAdded.keySet()) {
			IdObject idObject = objectsAdded.get(objectShell);
			ArrayList<AnimFlag<?>> animFlags = objectShell.getIdObject().getAnimFlags();
			for (AnimFlag<?> animFlag : animFlags) {
				idObject.add(getNewAnimFlag(addedAnims, animFlag));
			}
			if (idObject instanceof EventObject) {
				setNewEventTracks(addedAnims, (EventObject) idObject);
			}
		}
//		for (GeosetShell geosetShell : geosetsAdded.keySet()){
//			Geoset geoset = geosetsAdded.get(geosetShell);
//			GeosetAnim geosetAnim = geosetShell.getGeoset().getGeosetAnim();
//			if(geosetAnim != null){
//				ArrayList<AnimFlag<?>> animFlags = geosetAnim.getAnimFlags();
//				GeosetAnim copyAnim = geoset.getGeosetAnim();
//				copyAnim.clearAnimFlags();
//				for (AnimFlag<?> animFlag : animFlags){
//					copyAnim.add(extracted(addedAnims, animFlag));
//				}
//			}
//		}

	}

	private void setNewEventTracks(Map<AnimShell, Animation> addedAnims, EventObject eventObject) {
		for (AnimShell animShell : addedAnims.keySet()) {
			Animation animation = addedAnims.get(animShell);
			Animation oldAnim = animShell.getAnim();
			Animation impAnim = animShell.getImportAnim();

			boolean reverse = false;

			TreeSet<Integer> entryMapCopy = null;
			if (eventObject.getEventTrack(oldAnim) != null) {
				reverse = animShell.isReverse();
				entryMapCopy = eventObject.getEventTrack(oldAnim);
			} else if (impAnim != null && eventObject.getEventTrack(impAnim) != null) {
				reverse = animShell.getImportAnimShell().isReverse();
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
			Animation impAnim = animShell.getImportAnim();

			boolean reverse = false;
			TreeMap<Integer, Entry<Q>> entryMapCopy = null;
			if (animFlag.getEntryMap(oldAnim) != null) {
				reverse = animShell.isReverse();
				entryMapCopy = animFlag.getSequenceEntryMapCopy(oldAnim);
			} else if (impAnim != null && animFlag.getEntryMap(impAnim) != null) {
				reverse = animShell.getImportAnimShell().isReverse();
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

			FloatAnimFlag flagOld = getFloatAnimFlag(newVisFlag.tans(), oldAnims, visibilityShell.getOldVisSource());
			FloatAnimFlag flagNew = getFloatAnimFlag(newVisFlag.tans(), newAnims, visibilityShell.getNewVisSource());

			if (flagNew != null &&
					((visibilityShell.isFavorOld() && (visibilityShell.getModel() == mht.receivingModel) && !clearAnims)
							|| (!visibilityShell.isFavorOld() && (visibilityShell.getModel() == mht.donatingModel)))) {
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

	private Map<ObjectShell, IdObject> addChosenObjects(EditableModel newModel, Map<BoneShell, IdObject> bonesAdded) {
		Map<ObjectShell, IdObject> objectsAdded = new HashMap<>();
		for (ObjectShell objectShell : mht.allObjectShells) {
			if (objectShell.getShouldImport() && objectShell.getIdObject() != null) {
				BoneShell parentBs = objectShell.getNewParentBs();
				IdObject copy = objectShell.getIdObject().copy();
				copy.clearAnimFlags();
				if (parentBs != null) {
					copy.setParent(bonesAdded.get(parentBs));
				} else {
					copy.setParent(null);
				}
				// later make a name field?
				newModel.add(copy);
				objectsAdded.put(objectShell, copy);
			}
//			else if (objectShell.getIdObject() != null) {
////				objectShell.getIdObject().setParent(null);
//				// Fix cross-model referencing issue (force clean parent node's list of children)
//
//			}
		}

		return objectsAdded;
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

	private void applyNewMatrixBones(Map<GeosetShell, Geoset> geosetsAdded, Map<BoneShell, IdObject> bonesAdded, EditableModel model) {
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
				for (BoneShell bs : matrixShell.getNewBones()) {
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
		for (BoneShell bs : mht.recModBoneShells) {
			if (bs.getImportBoneShell() != null && bs.getImportBoneShell().getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
				bs.getBone().copyMotionFrom(bs.getImportBone());
			}
		}
	}

	private Map<BoneShell, IdObject> addChosenNewBones(EditableModel newModel) {
		Map<BoneShell, IdObject> bonesAdded = new HashMap<>();
		for (BoneShell boneShell : mht.allBoneShells) {
			// we will go through all bone shells for this
			// Fix cross-model referencing issue (force clean parent node's list of children)
			if (boneShell.isFromDonating() || !mht.clearExistingBones.isSelected()) {
				switch (boneShell.getImportStatus()) {
					case IMPORT -> {
						System.out.println("adding bone: " + boneShell);
						Bone copy = boneShell.getBone().copy();
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

		for (BoneShell boneShell : bonesAdded.keySet()) {
			IdObject idObject = bonesAdded.get(boneShell);
			if (boneShell.getNewParentBs() != null) {
				idObject.setParent(bonesAdded.get(boneShell.getNewParentBs()));
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
			if (animShell.getImportType() != AnimShell.ImportType.DONTIMPORT && animShell.getImportType() != AnimShell.ImportType.TIMESCALE) {
				Animation copy = animShell.getAnim().deepCopy();
				if (animShell.getImportType() == AnimShell.ImportType.IMPORTBASIC) {
					addedAnims.put(animShell, copy);
				} else if (animShell.getImportType() == AnimShell.ImportType.CHANGENAME) {
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

