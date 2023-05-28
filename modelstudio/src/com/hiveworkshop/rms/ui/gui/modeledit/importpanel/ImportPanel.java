package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.*;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The panel to handle the import function.
 *
 * Eric Theller 6/11/2012
 */
public class ImportPanel extends JTabbedPane {
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

	private JFrame frame;

	boolean importSuccess = false;
	boolean importStarted = false;
	boolean importEnded = false;

	private ModelStructureChangeListener changeListener1;

	ModelHolderThing mht;

	public ImportPanel(final EditableModel receivingModel, final EditableModel donatingModel, final boolean visibleOnStart) {
		super();
		mht = new ModelHolderThing(receivingModel, donatingModel);

		String receivingModelName = mht.receivingModel.getName();
		String donatingModelName = mht.donatingModel.getName();


		if (receivingModelName.equals(donatingModelName)) {
			mht.donatingModel.setFileRef(new File(mht.donatingModel.getFile().getParent() + "/" + donatingModelName + " (Imported)" + ".mdl"));
		}
		TempSaveModelStuff.doSavePreps(mht.receivingModel);

		makeTabs();


		if (receivingModelName.equals(donatingModelName)) {
			frame = getFrame(receivingModelName, "itself");
		} else {
			frame = getFrame(donatingModelName, receivingModelName);
		}
		// frame.pack();
		frame.setVisible(visibleOnStart);
	}

	private JFrame getFrame(String name1, String name2) {
		JFrame frame = new JFrame("Importing " + name1 + " into " + name2);
		try {
			frame.setIconImage(RMSIcons.MDLIcon.getImage());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Image files were not found! Due to bad programming, this might break the program!");
		}

		JPanel containerPanel = new JPanel(new MigLayout("gap 0, fill", "[grow]", "[grow][]"));
		containerPanel.add(this, "growx, growy, wrap");
		containerPanel.add(getFooterPanel(frame));
		frame.setContentPane(containerPanel);

		frame.setBounds(0, 0, 1024, 780);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				cancelImport(frame);
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		return frame;
	}

	private void makeTabs() {
		// Geoset Panel
		GeosetEditPanel geosetEditPanel = new GeosetEditPanel(mht);
		addTab("Geosets", geoIcon, geosetEditPanel, "Controls which geosets will be imported.");

		// Animation Panel
		AnimEditPanel animEditPanel = new AnimEditPanel(mht);
		addTab("Animation", animIcon, animEditPanel, "Controls which animations will be imported.");

		// Bone Panel
		BoneEditPanel boneEditPanel = new BoneEditPanel(mht);
		addTab("Bones", boneIcon, boneEditPanel, "Controls which bones will be imported.");

		// Matrices Panel // Build the geosetAnimTabs list of GeosetPanels
		BoneAttachmentEditPanel boneAttachmentEditPanel = new BoneAttachmentEditPanel(mht);
		addTab("Matrices", greenIcon, boneAttachmentEditPanel, "Controls which bones geosets are attached to.");

		// Objects Panel
		ObjectEditPanel objectEditPanel = new ObjectEditPanel(mht);
		addTab("Objects", objIcon, objectEditPanel, "Controls which objects are imported.");

		// Objects Panel
		CameraEditPanel cameraEditPanel = new CameraEditPanel(mht);
		addTab("Cameras", objIcon, cameraEditPanel, "Controls which cameras are imported.");

		// Visibility Panel
		VisibilityEditPanel visibilityEditPanel = new VisibilityEditPanel(mht);
		addTab("Visibility", orangeIcon, visibilityEditPanel, "Controls the visibility of portions of the model.");
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

	private JPanel getFooterPanel(JFrame frame) {
		JPanel footerPanel = new JPanel(new MigLayout("gap 0", "[grow, left]8[grow, right]"));
		JButton okayButton = new JButton("Finish");
		okayButton.addActionListener(e -> applyImport(frame));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> cancelImport(frame));

		footerPanel.add(okayButton);
		footerPanel.add(cancelButton);

		return footerPanel;
	}


	private void applyImport(JFrame frame) {
		doImport();
		frame.setVisible(false);
		frame.dispose();
	}

	private void cancelImport(JFrame frame) {
		final Object[] options = {"Yes", "No"};
		final int n = JOptionPane.showOptionDialog(frame, "Really cancel this import?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0) {
			frame.setVisible(false);
			frame = null;
		}
	}

	/**
	 * Provides a Runnable to the ImportPanel that will be run after the import has
	 * ended successfully.
	 */
	public void setModelChangeListener(final ModelStructureChangeListener changeListener) {
		this.changeListener1 = changeListener;
	}

	public void doImport() {
		importStarted = true;
		try {
			// The engine for actually performing the model to model import.

			if (mht.receivingModel == mht.donatingModel) {
				JOptionPane.showMessageDialog(null, "The program has confused itself.");
			}

			addChosenGeosets();
			getGeosetsRemoved();

			List<Animation> oldAnims = new ArrayList<>(mht.receivingModel.getAnims());

			List<AnimFlag<?>> recModFlags = ModelUtils.getAllAnimFlags(mht.receivingModel);
			List<AnimFlag<?>> donModFlags = ModelUtils.getAllAnimFlags(mht.donatingModel);

			List<EventObject> recModEventObjs = mht.receivingModel.getEvents();
			List<EventObject> donModEventObjs = mht.donatingModel.getEvents();

			if (mht.clearRecModAnims.isSelected()) {
				doClearAnims(recModFlags, recModEventObjs);
			}


			List<Animation> newAnims = getNewAnimations(donModFlags, donModEventObjs);

			if (mht.clearExistingBones.isSelected()) {
				clearRecModBoneAndHelpers();
			}

			addChosenNewBones();

			if (!mht.clearExistingBones.isSelected()) {
				copyMotionFromBones();
			}
			// IteratableListModel<BoneShell> bones = getFutureBoneList();
			applyNewMatrixBones();

			// Objects!
			addChosenObjects();
			addChosenCameras();

			setNewVisSources(oldAnims, mht.clearRecModAnims.isSelected(), newAnims);

			importSuccess = true;


			if (changeListener1 != null) {
				changeListener1.geosetsUpdated();
				changeListener1.geosetsUpdated();
				changeListener1.nodesUpdated();
				changeListener1.camerasUpdated();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}

		importEnded = true;
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

	private void setNewVisSources(List<Animation> oldAnims, boolean clearAnims, List<Animation> newAnims) {
		final List<AnimFlag<Float>> finalVisFlags = new ArrayList<>();
		for (VisibilityShell<?> visibilityShell : mht.futureVisComponents) {
			TimelineContainer temp = visibilityShell.getSource();
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
					((visibilityShell.isFavorOld() && (!visibilityShell.isFromDonating()) && !clearAnims)
							|| (!visibilityShell.isFavorOld() && (visibilityShell.isFromDonating())))) {
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
			TimelineContainer visSource = mht.futureVisComponents.get(i).getSource();
			AnimFlag<Float> visFlag = finalVisFlags.get(i);// might be null
			if (visFlag.size() > 0) {
				visSource.setVisibilityFlag(visFlag);
			} else {
				visSource.setVisibilityFlag(null);
			}
		}
	}

	private FloatAnimFlag getFloatAnimFlag(boolean tans, List<Animation> anims, VisibilityShell<?> source) {
		if (source != null) {
			if (source.isNeverVisible()) {
				return getNeverVisFlag(tans, anims);
			} else if (!source.isAlwaysVisible()) {
				return (FloatAnimFlag) source.getSource().getVisibilityFlag();
			}
		}
		return null;
	}

	private FloatAnimFlag getNeverVisFlag(boolean tans, List<Animation> anims) {
		FloatAnimFlag tempFlag = new FloatAnimFlag("temp");
		for (Animation a : anims) {
			tempFlag.setOrAddEntryT(0, new Entry<>(0, 0f), a);
		}
		if (tans) tempFlag.unLinearize();
		return tempFlag;
	}

	private List<IdObject> addChosenObjects() {
		List<IdObject> objectsAdded = new ArrayList<>();
		for (IdObjectShell<?> objectShell : mht.donModObjectShells) {
			if (objectShell.getShouldImport() && objectShell.getIdObject() != null) {
				IdObjectShell<?> parentBs = objectShell.getNewParentShell();
				if (parentBs != null) {
					objectShell.getIdObject().setParent(parentBs.getIdObject());
				} else {
					objectShell.getIdObject().setParent(null);
				}
				// later make a name field?
				mht.receivingModel.add(objectShell.getIdObject());
				objectsAdded.add(objectShell.getIdObject());
			} else if (objectShell.getIdObject() != null) {
				objectShell.getIdObject().setParent(null);
				// Fix cross-model referencing issue (force clean parent node's list of children)

			}
		}

		return objectsAdded;
	}

	private List<Camera> addChosenCameras() {
		List<Camera> camerasAdded = new ArrayList<>();
		for (CameraShell cameraShell : mht.donModCameraShells) {
			if (cameraShell.getShouldImport() && cameraShell.getCamera() != null) {
				mht.receivingModel.add(cameraShell.getCamera());
				camerasAdded.add(cameraShell.getCamera());
			}
		}
		return camerasAdded;
	}

	private void applyNewMatrixBones() {
		boolean shownEmpty = false;
		boolean shownDestroyed = false;
		Bone dummyBone = null;
		// ToDo this needs a map matrices to vertices or something to update vertex-bones correctly...
		for (GeosetShell geosetShell : mht.allGeoShells) {
			if (geosetShell.isDoImport()) {
				Map<Matrix, List<GeosetVertex>> matrixVertexMap = new HashMap<>();
				for (GeosetVertex vertex : geosetShell.getGeoset().getVertices()){
					matrixVertexMap.computeIfAbsent(vertex.getMatrix(), k -> new ArrayList<>()).add(vertex);
				}
				for (MatrixShell ms : geosetShell.getMatrixShells()) {
					List<GeosetVertex> vertexList = matrixVertexMap.get(ms.getMatrix());
					ms.getMatrix().clear();
					for (final IdObjectShell<?> bs : ms.getNewBones()) {
						if (bs.getIdObject() instanceof Bone && mht.receivingModel.contains(bs.getIdObject())) {
							if (bs.getIdObject().getClass() == Helper.class) {
								JOptionPane.showMessageDialog(null,
										"Error: Holy fo shizzle my grizzle! A geoset is trying to attach to a helper, not a bone!");
							}
							ms.getMatrix().add((Bone) bs.getIdObject());
						} else {
							System.out.println("Boneshaving " + bs.getIdObject().getName() + " out of use");
						}
					}
					if (ms.getMatrix().size() == 0 && !shownDestroyed) {
						JOptionPane.showMessageDialog(null,
								"Error: A matrix was functionally destroyed while importing, and may take the program with it!");
						shownDestroyed = true;
					}
					if (ms.getMatrix().getBones().size() < 1) {
						if (dummyBone == null) {
							dummyBone = new Bone();
							dummyBone.setName("Bone_MatrixEaterDummy" + (int) (Math.random() * 2000000000));
							dummyBone.setPivotPoint(new Vec3(0, 0, 0));
							if (!mht.receivingModel.contains(dummyBone)) {
								mht.receivingModel.add(dummyBone);
							}
						}
						if (!shownEmpty) {
							JOptionPane.showMessageDialog(null,
									"Warning: You left some matrices empty. This was detected, and a dummy bone at { 0, 0, 0 } has been generated for them named "
											+ dummyBone.getName()
											+ "\nMultiple geosets may be attached to this bone, and the error will only be reported once for your convenience.");
							shownEmpty = true;
						}
						if (!ms.getMatrix().getBones().contains(dummyBone)) {
							ms.getMatrix().getBones().add(dummyBone);
						}
					}
					ms.getMatrix().cureBones(mht.receivingModel.getBones());
					if(vertexList != null){
						for(GeosetVertex vertex : vertexList){
							vertex.clearBoneAttachments();
							for (Bone bone : ms.getMatrix().getBones()) {
								vertex.addBoneAttachment(bone);
							}
						}
					} else {
						System.out.println("couldn't find vertices for Matrix " + ms.getMatrix());
					}
				}
			}
		}
////			mht.receivingModel.updateObjectIds();
//		for (Geoset g : mht.receivingModel.getGeosets()) {
//			applyMatricesToVertices(g, mht.receivingModel);
//		}
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

	private List<IdObject> addChosenNewBones() {
		List<IdObject> objectsAdded = new ArrayList<>();
		for (IdObjectShell<?> boneShell : mht.donModBoneShells) {
			// we will go through all bone shells for this
			// Fix cross-model referencing issue (force clean parent node's list of children)
			switch (boneShell.getImportStatus()) {
				case IMPORT -> {
					System.out.println("adding bone: " + boneShell);
					mht.receivingModel.add(boneShell.getIdObject());
					objectsAdded.add(boneShell.getIdObject());
					if (boneShell.getNewParentShell() != null) {
						boneShell.getIdObject().setParent(boneShell.getNewParentShell().getIdObject());
					} else {
						boneShell.getIdObject().setParent(null);
					}
				}
				case MOTION_FROM, DONT_IMPORT -> boneShell.getIdObject().setParent(null);
			}
		}
		return objectsAdded;
	}

	private void clearRecModBoneAndHelpers() {
		for (IdObject o : mht.receivingModel.getBones()) {
			mht.receivingModel.remove(o);
		}
		for (IdObject o : mht.receivingModel.getHelpers()) {
			mht.receivingModel.remove(o);
		}
	}

	private List<Animation> getNewAnimations(List<AnimFlag<?>> donModFlags, List<EventObject> donModEventObjs) {


		// note to self: remember to scale event objects with time
//		List<AnimFlag<?>> newImpFlags = new ArrayList<>();
		Map<AnimFlag<?>, AnimFlag<?>> flagMap = new HashMap<>();
		for (AnimFlag<?> af : donModFlags) {
			if (!af.hasGlobalSeq()) {
//				newImpFlags.add(af.getEmptyCopy());
				flagMap.put(af, af.getEmptyCopy());
			} else {
//				newImpFlags.add(af.deepCopy());
				flagMap.put(af, af.deepCopy());
			}
		}
//		List<EventObject> newImpEventObjs = new ArrayList<>();
		Map<EventObject, EventObject> eventMap = new HashMap<>();
		for (EventObject e : donModEventObjs) {
//			newImpEventObjs.add(EventObject.buildEmptyFrom(e));
			eventMap.put(e, EventObject.buildEmptyFrom(e));
		}


		List<Animation> newAnims = new ArrayList<>();
		for (AnimShell animShell : mht.allAnimShells) {
			if (animShell.isDoImport()) {
				int newStart = ModelUtils.animTrackEnd(mht.receivingModel) + 300;

				Animation anim1 = animShell.getAnim();
				if (animShell.isReverse()) {
					reverseAnim(donModFlags, donModEventObjs, anim1);
				}
				//todo things here is probably broken...
				anim1.setStart(newStart);
//						animCopyToInterv1(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, anim1, anim1);
				animCopyToInterv1(flagMap, eventMap, anim1, anim1);

				anim1.setName(animShell.getName());
				if(!mht.receivingModel.contains(anim1)){
					mht.receivingModel.add(anim1);
					newAnims.add(anim1);
				}
			}
			if(!animShell.getAnimDataDests().isEmpty()) {
				if (!mht.clearRecModAnims.isSelected()) {

					Animation anim1 = animShell.getAnim();
					for (AnimShell recAnimShell : mht.recModAnims) {
						AnimShell importAnimShell = recAnimShell.getAnimDataSrc();
						if (importAnimShell == animShell) {
							Animation importAnim = importAnimShell.getAnim();
							animCopyToInterv1(flagMap, eventMap, anim1, importAnim);

							newAnims.add(new Animation("temp", anim1.getStart(), anim1.getEnd()));

							if (!mht.clearExistingBones.isSelected()) {
								for (IdObjectShell<?> bs : mht.recModBoneShells) {
									if (bs.getMotionSrcShell() != null && bs.getMotionSrcShell().getImportStatus() == IdObjectShell.ImportType.MOTION_FROM) {
										System.out.println("Attempting to clear animation for " + bs.getIdObject().getName() + " values " + anim1.getStart() + ", " + anim1.getEnd());
										bs.getIdObject().clearAnimation(anim1);
									}
								}
							}
						}
					}
				}
			}
			if(false){
				Animation anim1 = animShell.getAnim();
				buildGlobSeqFrom(mht.donatingModel, anim1, donModFlags);
			}
		}


//		for (AnimShell animShell : mht.allAnimShells) {
//			if (animShell.getImportType() != AnimShell.ImportType.DONT_IMPORT) {
//				int newStart = ModelUtils.animTrackEnd(mht.receivingModel) + 300;
//
//				Animation anim1 = animShell.getAnim();
//				if (animShell.isReverse()) {
//					reverseAnim(donModFlags, donModEventObjs, anim1);
//				}
//				switch (animShell.getImportType()) {
//					case IMPORT_BASIC:
//					case CHANGE_NAME:
//
//						//todo things here is probably broken...
//						anim1.setStart(newStart);
////						animCopyToInterv1(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, anim1, anim1);
//						animCopyToInterv1(flagMap, eventMap, anim1, anim1);
//						if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
//							anim1.setName(animShell.getName());
//						}
//						if(!mht.receivingModel.contains(anim1)){
//							mht.receivingModel.add(anim1);
//							newAnims.add(anim1);
//						}
//						break;
//					case TIMESCALE_INTO:
//						if (!mht.clearRecModAnims.isSelected()) {
//							for (AnimShell recAnimShell : mht.recModAnims) {
//								AnimShell importAnimShell = recAnimShell.getAnimDataSrc();
//								if (importAnimShell == animShell) {
//									Animation importAnim = importAnimShell.getAnim();
//									animCopyToInterv1(flagMap, eventMap, anim1, importAnim);
//
//									newAnims.add(new Animation("temp", anim1.getStart(), anim1.getEnd()));
//
//									if (!mht.clearExistingBones.isSelected()) {
//										for (IdObjectShell<?> bs : mht.recModBoneShells) {
//											if (bs.getMotionSrcShell() != null && bs.getMotionSrcShell().getImportStatus() == IdObjectShell.ImportType.MOTION_FROM) {
//												System.out.println("Attempting to clear animation for " + bs.getIdObject().getName() + " values " + anim1.getStart() + ", " + anim1.getEnd());
//												bs.getIdObject().clearAnimation(anim1);
//											}
//										}
//									}
//								}
//							}
//						}
////						AnimShell importAnimShell = animShell.getImportAnimShell();
////
////						if (importAnimShell != null && importAnimShell.getImportType() == AnimShell.ImportType.TIMESCALE) {
////
////							Animation importAnim = importAnimShell.getAnim();
////							animCopyToInterv1(flagMap, eventMap, anim1, importAnim);
////
////							newAnims.add(new Animation("temp", anim1.getStart(), anim1.getEnd()));
////
////							if (!mht.clearExistingBones.isSelected()) {
////								for (BoneShell bs : mht.recModBoneShells) {
////									if (bs.getImportBoneShell() != null && bs.getImportBoneShell().getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
////										System.out.println("Attempting to clear animation for " + bs.getBone().getName() + " values " + anim1.getStart() + ", " + anim1.getEnd());
////										bs.getBone().clearAnimation(anim1);
////									}
////								}
////							}
////						}
//						break;
//					case GLOBALSEQ:
//						buildGlobSeqFrom(mht.donatingModel, anim1, donModFlags);
//						break;
//				}
//			}
//		}


//		if (!mht.clearRecModAnims.isSelected()) {
//			addNewAnimsIntoOldAnims(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, newAnims);
//		}
//		if (!mht.clearRecModAnims.isSelected()) {
//			for (AnimShell animShell : mht.recModAnims) {
//
//				AnimShell importAnimShell = animShell.getImportAnimShell();
//				if (importAnimShell != null && importAnimShell.getImportType() == AnimShell.ImportType.TIMESCALE) {
//					Animation anim1 = animShell.getAnim();
//
//					Animation importAnim = importAnimShell.getAnim();
//					animCopyToInterv1(flagMap, eventMap, anim1, importAnim);
//
//					newAnims.add(new Animation("temp", anim1.getStart(), anim1.getEnd()));
//
//					if (!mht.clearExistingBones.isSelected()) {
//						for (BoneShell bs : mht.recModBoneShells) {
//							if (bs.getImportBoneShell() != null && bs.getImportBoneShell().getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
//								System.out.println("Attempting to clear animation for " + bs.getBone().getName() + " values " + anim1.getStart() + ", " + anim1.getLength());
//								bs.getBone().clearAnimation(anim1);
//							}
//						}
//					}
//				}
//			}
//		}

		// Now, rebuild the old animflags with the new
		for (AnimFlag<?> af : donModFlags) {
//			af.setValuesTo(newImpFlags.get(donModFlags.indexOf(af)));
			AnimFlagUtils.setValuesTo(af, flagMap.get(af));
		}
		for (EventObject e : donModEventObjs) {
//			e.setValuesTo(newImpEventObjs.get(donModEventObjs.indexOf(e)));
			e.setValuesTo(eventMap.get(e));
		}

		return newAnims;
	}

	private void addNewAnimsIntoOldAnims(List<AnimFlag<?>> donModFlags, List<EventObject> donModEventObjs, List<AnimFlag<?>> newImpFlags, List<EventObject> newImpEventObjs, List<Animation> newAnims) {
		for (AnimShell animShell : mht.recModAnims) {

			AnimShell importAnimShell = animShell.getAnimDataSrc();
			if (importAnimShell != null) {
				Animation anim1 = animShell.getAnim();

				Animation importAnim = importAnimShell.getAnim();
				animCopyToInterv1(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, anim1, importAnim);

				newAnims.add(new Animation("temp", anim1.getStart(), anim1.getEnd()));

				if (!mht.clearExistingBones.isSelected()) {
					for (IdObjectShell<?> bs : mht.recModBoneShells) {
						if (bs.getMotionSrcShell() != null && bs.getMotionSrcShell().getImportStatus() == IdObjectShell.ImportType.MOTION_FROM) {
							System.out.println("Attempting to clear animation for " + bs.getIdObject().getName() + " values " + anim1.getStart() + ", " + anim1.getLength());
							bs.getIdObject().clearAnimation(anim1);
						}
					}
				}
			}
		}
	}

	private void reverseAnim(List<AnimFlag<?>> donModFlags, List<EventObject> donModEventObjs, Animation anim1) {
		// reverse the animation
		int length = anim1.getLength();
		for (AnimFlag<?> af : donModFlags) {
			if (!af.hasGlobalSeq() && (af.getName().equals(MdlUtils.TOKEN_SCALING)
					|| af.getName().equals(MdlUtils.TOKEN_ROTATION)
					|| af.getName().equals(MdlUtils.TOKEN_TRANSLATION))) {
				AnimFlagUtils.timeScale2(af, anim1, -length, length);
			}
		}
		for (EventObject e : donModEventObjs) {
			e.timeScale(anim1, -length, length);
		}
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
		for (AnimFlag<?> source : flagMap.keySet()){
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

	private List<Geoset> addChosenGeosets() {
		List<Geoset> geosetsAdded = new ArrayList<>();

		for (GeosetShell geoShell : mht.donModGeoShells) {

			if (geoShell.isDoImport()) {
				geoShell.getGeoset().setMaterial(geoShell.getMaterial());
				mht.receivingModel.add(geoShell.getGeoset());

				geosetsAdded.add(geoShell.getGeoset());
			}
		}
		return geosetsAdded;
	}

	private List<Geoset> getGeosetsRemoved() {
		List<Geoset> geosetsRemoved = new ArrayList<>();

		for (GeosetShell geoShell : mht.recModGeoShells) {

			if (!geoShell.isDoImport()) {
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

	public JFrame getParentFrame() {
		return frame;
	}

	// *********************Simple Import Functions****************
	public void animTransfer(boolean singleAnimation, Animation pickedAnim, Animation visFromAnim, boolean show) {
		mht.clearRecModAnims.setSelected(true);
		prepareModelHolderThing(IdObjectShell.ImportType.MOTION_FROM, singleAnimation);

		if (singleAnimation) {
			for (AnimShell animShell : mht.allAnimShells) {
				if (animShell.getOldName().equals(pickedAnim.getName())) {
					animShell.setDoImport(false);
				}
			}
			mht.clearRecModAnims.setSelected(false);
		}


		// Try assuming it's a unit with a corpse; they'll tend to be that way
		// Iterate through new visibility sources, find a geoset with gutz material
		for (VisibilityShell<?> donVis : mht.donModVisibilityShells) {
			if (isGutz(donVis)) {
				for (VisibilityShell<?> impVis : mht.futureVisComponents) {
					if (isGutz(impVis)) {
						impVis.setRecModAnimsVisSource(donVis);
					}
				}
				break;
			}
		}

		if (!show) {
			applyImport(frame);
		}
	}
//	public void animTransfer_org(boolean singleAnimation, Animation pickedAnim, Animation visFromAnim, boolean show) {
//		mht.clearRecModAnims.setSelected(true);
//		ugg(BoneShell.ImportType.MOTIONFROM, singleAnimation);
//
//		if (singleAnimation) {
//			importSingleAnim2(pickedAnim);
//			mht.clearRecModAnims.setSelected(false);
//		}
//
//
//		// Try assuming it's a unit with a corpse; they'll tend to be that way
//		// Iterate through new visibility sources, find a geoset with gutz material
//		for (VisibilityShell<?> donVis : mht.donModVisSourcesNew) {
//			if (isGutz(donVis)) {
//				for (VisibilityShell<?> impVis : mht.futureVisComponents) {
//					if (isGutz(impVis)) {
//						impVis.setNewVisSource(donVis);
//					}
//				}
//				break;
//			}
//		}
//
//		if (!show) {
//			applyImport(frame);
//		}
//	}
//private void importSingleAnim2(Animation pickedAnim) {
//	// JOptionPane.showMessageDialog(null,"single trans");
//	for (AnimShell animShell : mht.allAnimShells) {
//		if (animShell.getOldName().equals(pickedAnim.getName())) {
//			animShell.setImportType(AnimShell.ImportType.IMPORTBASIC);
//		}
//	}
////			mht.clearRecModAnims.doClick();// turn it back off
//}

	private void prepareModelHolderThing(IdObjectShell.ImportType importType, boolean singleAnim) {
//		mht.setImportAllGeos(false);
		mht.donModGeoShells.forEach(g -> g.setDoImport(false));

		mht.setImportStatusForAllDonBones(importType);

		mht.donModObjectShells.forEach(shell -> shell.setShouldImport(false));
//		mht.setImportAllDonObjs(false);
		mht.visibilityList();
		mht.selectSimilarVisSources();
		if (singleAnim) {
			mht.donModAnims.forEach(shell -> shell.setDoImport(false));
		}
	}

	private boolean isGutz(VisibilityShell<?> donVis) {
		boolean isGeoset = donVis.getSource() instanceof Geoset;
		if(isGeoset){
			boolean hasGeoAnim = ((Geoset) donVis.getSource()).hasAnim();
			if(hasGeoAnim){
				Bitmap bitmap = ((Geoset) donVis.getSource()).getMaterial().firstLayer().firstTexture();
				return bitmap.getPath().equalsIgnoreCase("textures\\gutz.blp");
			}
		}
		return false;
	}

	public void animTransferPartTwo(Animation pickedAnim, AnimShell visFromAnim, boolean show) {
		// This should be an import from self
		// This seems to be a stupid hack to put back lost stuff...
		prepareModelHolderThing(IdObjectShell.ImportType.DONT_IMPORT, true);

		for (AnimShell animShell : mht.allAnimShells) {
//		for (AnimShell animShell : mht.donModAnims) {
			if (visFromAnim == null || animShell.getOldName().equals(visFromAnim.getName())) {

				for (AnimShell shell : mht.allAnimShells) {
//				for (AnimShell shell : mht.recModAnims) {
					if (shell.getOldName().equals(pickedAnim.getName())) {
						animShell.setAnimDataSrc(shell);
						break;
					}
				}
			}
		}

		for (VisibilityShell<?> vs : mht.futureVisComponents) {
			vs.setFavorOld(false);
		}

		if (!show) {
			applyImport(frame);
		}
	}
//	public void animTransferPartTwo(Animation pickedAnim, Animation visFromAnim, boolean show) {
//		// This should be an import from self
//		// This seems to be a stupid hack to put back lost stuff...
//		prepareModelHolderThing(BoneShell.ImportType.DONTIMPORT, true);
//
//		importSingleAnim1(pickedAnim, visFromAnim);
//
//		for (VisibilityShell vs : mht.futureVisComponents) {
//			vs.setFavorOld(false);
//		}
//
//		if (!show) {
//			applyImport(frame);
//		}
//	}
//
//
//	private void importSingleAnim1(Animation pickedAnim, Animation visFromAnim) {
//		for (AnimShell animShell : mht.allAnimShells) {
////		for (AnimShell animShell : mht.donModAnims) {
//			if (animShell.getOldName().equals(visFromAnim.getName())) {
//				animShell.setImportType(AnimShell.ImportType.TIMESCALE); // Time scale
//
//				for (AnimShell shell : mht.allAnimShells) {
////				for (AnimShell shell : mht.recModAnims) {
//					if (shell.getOldName().equals(pickedAnim.getName())) {
//						animShell.setImportAnimShell(shell);
//						break;
//					}
//				}
//			}
//		}
//	}
}

