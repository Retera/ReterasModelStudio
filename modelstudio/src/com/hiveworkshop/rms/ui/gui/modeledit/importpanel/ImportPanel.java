package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	JFrame frame;


	boolean importSuccess = false;
	boolean importStarted = false;
	boolean importEnded = false;

	private ModelStructureChangeListener callback;


	public ImportPanel(final EditableModel a, final EditableModel b) {
		this(a, b, true);
	}

	ModelHolderThing mht;

	public ImportPanel(final EditableModel receivingModel, final EditableModel donatingModel, final boolean visibleOnStart) {
		super();
		mht = new ModelHolderThing(receivingModel, donatingModel);
		if (mht.receivingModel.getName().equals(mht.donatingModel.getName())) {
			mht.donatingModel.setFileRef(new File(mht.donatingModel.getFile().getParent() + "/" + mht.donatingModel.getName() + " (Imported)" + ".mdl"));
			frame = new JFrame("Importing " + mht.receivingModel.getName() + " into itself");
		} else {
			frame = new JFrame("Importing " + mht.donatingModel.getName() + " into " + mht.receivingModel.getName());
		}
		TempSaveModelStuff.doSavePreps(mht.receivingModel);
		try {
			frame.setIconImage(RMSIcons.MDLIcon.getImage());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Image files were not found! Due to bad programming, this might break the program!");
		}

		// Geoset Panel
		GeosetEditPanel geosetEditPanel = new GeosetEditPanel(mht);
		addTab("Geosets", geoIcon, geosetEditPanel, "Controls which geosets will be imported.");
//		addTab("Geosets", geoIcon, GeosetEditPanel.makeGeosetPanel(mht), "Controls which geosets will be imported.");

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

		// Visibility Panel
		VisibilityEditPanel visibilityEditPanel = new VisibilityEditPanel(mht);
		addTab("Visibility", orangeIcon, visibilityEditPanel, "Controls the visibility of portions of the model.");

		final JPanel containerPanel = new JPanel(new MigLayout("gap 0, fill", "[grow]", "[grow][]"));
		containerPanel.add(this, "growx, growy, wrap");
		containerPanel.add(getFooterPanel());
		frame.setContentPane(containerPanel);

		frame.setBounds(0, 0, 1024, 780);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				cancelImport();
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// frame.pack();
		frame.setVisible(visibleOnStart);
	}

	public static void buildGlobSeqFrom(EditableModel model, Animation anim, List<AnimFlag<?>> flags) {
		GlobalSeq newSeq = new GlobalSeq(anim.getLength());
		for (AnimFlag<?> af : flags) {
			if (!af.hasGlobalSeq()) {
				AnimFlag<?> copy = af.deepCopy();
				copy.setGlobSeq(newSeq);
				copy.copyFrom(af, anim, newSeq);
				addFlagToParent(model, af, copy);
			}
		}
	}

	public static void addFlagToParent(EditableModel model, final AnimFlag<?> aflg, final AnimFlag<?> added)
	// aflg is the parent
	{
		// ADDS "added" TO THE PARENT OF "aflg"
		for (final Material m : model.getMaterials()) {
			for (final Layer layer : m.getLayers()) {
				if (layer.has(aflg.getName())) {
					layer.add(added);
				}
			}
		}

		if (model.getTexAnims() != null) {
			for (final TextureAnim textureAnim : model.getTexAnims()) {
				if (textureAnim.has(aflg.getName())) {
					textureAnim.add(added);
				}
			}
		}

		if (model.getGeosetAnims() != null) {
			for (final GeosetAnim geosetAnim : model.getGeosetAnims()) {
				if (geosetAnim.has(aflg.getName())) {
					geosetAnim.add(added);
				}
			}
		}

		for (final IdObject object : model.getAllObjects()) {
			if (object.has(aflg.getName())) {
				object.add(added);
			}
		}

		if (model.getCameras() != null) {
			for (final Camera x : model.getCameras()) {
//				if (x.getSourceNode().has(aflg.getName()) || x.targetAnimFlags.contains(aflg)) {
				if (x.getSourceNode().has(aflg.getName())) {
					x.getSourceNode().add(added);
				}
			}
		}
	}

	private JPanel getFooterPanel() {
		JPanel footerPanel = new JPanel(new MigLayout("gap 0", "[grow, left]8[grow, right]"));
		JButton okayButton = new JButton("Finish");
		okayButton.addActionListener(e -> applyImport());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> cancelImport());

		footerPanel.add(okayButton);
		footerPanel.add(cancelButton);

		return footerPanel;
	}


	private void applyImport() {
		doImport();
		frame.setVisible(false);
	}

	private void cancelImport() {
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
	public void setCallback(final ModelStructureChangeListener callback) {
		this.callback = callback;
	}

	public void doImport() {
		importStarted = true;
		try {
			// The engine for actually performing the model to model import.

			if (mht.receivingModel == mht.donatingModel) {
				JOptionPane.showMessageDialog(null, "The program has confused itself.");
			}

			List<Geoset> geosetsAdded = addChosenGeosets();
			List<Geoset> geosetsRemoved = getGeosetsRemoved();

			final List<Animation> oldAnims = new ArrayList<>(mht.receivingModel.getAnims());

			final List<AnimFlag<?>> recModFlags = mht.receivingModel.getAllAnimFlags();
			final List<AnimFlag<?>> donModFlags = mht.donatingModel.getAllAnimFlags();

			final List<EventObject> recModEventObjs = mht.receivingModel.getEvents();
			final List<EventObject> donModEventObjs = mht.donatingModel.getEvents();

			// note to self: remember to scale event objects with time
			final List<AnimFlag<?>> newImpFlags = new ArrayList<>();
			for (final AnimFlag<?> af : donModFlags) {
				if (!af.hasGlobalSeq()) {
					newImpFlags.add(af.getEmptyCopy());
				} else {
					newImpFlags.add(af.deepCopy());
				}
			}
			final List<EventObject> newImpEventObjs = new ArrayList<>();
			for (EventObject e : donModEventObjs) {
				newImpEventObjs.add(EventObject.buildEmptyFrom(e));
			}

			final boolean clearAnims = mht.clearRecModAnims.isSelected();
			if (clearAnims) {
				for (final Animation anim : mht.receivingModel.getAnims()) {
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
				mht.receivingModel.getAnims().clear();
			}


			final List<Animation> newAnims = getNewAnimations(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs);

//			if (!mht.clearRecModAnims.isSelected()) {
//				addNewAnimsIntoOldAnims(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, newAnims);
//			}

			// Now, rebuild the old animflags with the new
			for (final AnimFlag<?> af : donModFlags) {
				af.setValuesTo(newImpFlags.get(donModFlags.indexOf(af)));
			}
			for (final EventObject e : donModEventObjs) {
				e.setValuesTo(newImpEventObjs.get(donModEventObjs.indexOf(e)));
			}

			if (mht.clearExistingBones.isSelected()) {
				clearRecModBoneAndHelpers();
			}
			final List<IdObject> objectsAdded = new ArrayList<>();

			addChosenNewBones(objectsAdded);

			if (!mht.clearExistingBones.isSelected()) {
				copyMotionFromBones();
			}
			// IteratableListModel<BoneShell> bones = getFutureBoneList();
			aplyNewMatrixBones();

			// Objects!
			final List<Camera> camerasAdded = addChosenObjects(objectsAdded);

			setNewVisSources(oldAnims, clearAnims, newAnims);

			importSuccess = true;

			// TODO This is broken now, should fix it
			// Tell program to set visibility after import
			// MDLDisplay display = MainFrame.panel.displayFor(currentModel);
			// if( display != null )
			// {
			// display.setBeenSaved(false); // we edited the model
			// for( int i = 0; i < mht.geosetTabs.getTabCount(); i++ )
			// {
			// GeosetPanel gp = (GeosetPanel)mht.geosetTabs.getComponentAt(i);
			// if( gp.doImport.isSelected() && gp.model == importedModel )
			// {
			// display.makeGeosetEditable(gp.geoset, true);
			// display.makeGeosetVisible(gp.geoset, true);
			// }
			// }
			// MainFrame.panel.geoControl.repaint();
			// MainFrame.panel.geoControl.setMDLDisplay(display);
			// display.reloadTextures();//.mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
			// }


			if (callback != null) {
				callback.geosetsUpdated();
				callback.geosetsUpdated();
				callback.nodesUpdated();
				callback.camerasUpdated();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}

		importEnded = true;
	}

	private void setNewVisSources(List<Animation> oldAnims, boolean clearAnims, List<Animation> newAnims) {
		final List<AnimFlag<Float>> finalVisFlags = new ArrayList<>();
		for (VisibilityShell visibilityShell : mht.futureVisComponents) {
			VisibilitySource temp = ((VisibilitySource) visibilityShell.getSource());
			AnimFlag<Float> visFlag = temp.getVisibilityFlag();// might be null
			AnimFlag<Float> newVisFlag;

			boolean tans = false;
			if (visFlag != null) {
				newVisFlag = visFlag.getEmptyCopy();
				tans = visFlag.tans();
			} else {
				newVisFlag = new FloatAnimFlag(temp.visFlagName());
			}
			// newVisFlag = new AnimFlag(temp.visFlagName());

			FloatAnimFlag flagOld = getFloatAnimFlag(tans, oldAnims, visibilityShell.getOldVisSource());

			FloatAnimFlag flagNew = getFloatAnimFlag(tans, newAnims, visibilityShell.getNewVisSource());

			if ((visibilityShell.isFavorOld() && (visibilityShell.getModel() == mht.receivingModel) && !clearAnims) || (!visibilityShell.isFavorOld() && (visibilityShell.getModel() == mht.donatingModel))) {
				// this is an element favoring existing animations over imported
				clearAnims(oldAnims, flagNew);
			} else {
				// this is an element not favoring existing over imported
				clearAnims(newAnims, flagOld);
			}
			if (flagOld != null) {
				newVisFlag.copyFrom(flagOld);
			}
			if (flagNew != null) {
				newVisFlag.copyFrom(flagNew);
			}
			finalVisFlags.add(newVisFlag);
		}
		for (int i = 0; i < mht.futureVisComponents.size(); i++) {
			final VisibilityShell vPanel = mht.futureVisComponents.get(i);
			final VisibilitySource temp = ((VisibilitySource) vPanel.getSource());
			final AnimFlag<Float> visFlag = finalVisFlags.get(i);// might be null
			if (visFlag.size() > 0) {
				temp.setVisibilityFlag(visFlag);
			} else {
				temp.setVisibilityFlag(null);
			}
		}
	}

	private void clearAnims(List<Animation> anims, FloatAnimFlag flag) {
		for (final Animation a : anims) {
			if (flag != null) {
				if (!flag.hasGlobalSeq()) {
					flag.deleteAnim(a);
					// All entries for visibility are deleted from imported sources during existing animation times
				}
			}
		}
	}

	private FloatAnimFlag getFloatAnimFlag(boolean tans, List<Animation> anims, VisibilityShell source) {
		if (source != null) {
			if (source.isNeverVisible()) {
				FloatAnimFlag flagOld = new FloatAnimFlag("temp");

				Entry<Float> invisEntry = new Entry<>(0, 0f);
				if(tans) invisEntry.unLinearize();

				for (final Animation a : anims) {
					flagOld.setOrAddEntryT(a.getStart(), invisEntry.deepCopy().setTime(a.getStart()), a);
//					if (tans) {
//						flagOld.addEntry(a.getStart(), 0f, 0f, 0f);
//					} else {
//						flagOld.addEntry(a.getStart(), 0f);
//					}
				}
				return flagOld;
			} else if (!source.isAlwaysVisible()) {
				return (FloatAnimFlag) ((VisibilitySource) source.getSource()).getVisibilityFlag();
			}
		}
		return null;
	}

	private List<Camera> addChosenObjects(List<IdObject> objectsAdded) {
		final List<Camera> camerasAdded = new ArrayList<>();
		for (ObjectShell objectShell : mht.donModObjectShells) {
			if (objectShell.getShouldImport()) {
				if (objectShell.getIdObject() != null) {
					final BoneShell parentBs = objectShell.getNewParentBs();
					if (parentBs != null) {
						objectShell.getIdObject().setParent(parentBs.getBone());
					} else {
						objectShell.getIdObject().setParent(null);
					}
					// later make a name field?
					mht.receivingModel.add(objectShell.getIdObject());
					objectsAdded.add(objectShell.getIdObject());
				} else if (objectShell.getCamera() != null) {
					mht.receivingModel.add(objectShell.getCamera());
					camerasAdded.add(objectShell.getCamera());
				}
			} else {
				if (objectShell.getIdObject() != null) {
					objectShell.getIdObject().setParent(null);
					// Fix cross-model referencing issue (force clean parent node's list of children)
				}
			}
		}
		return camerasAdded;
	}

	private void aplyNewMatrixBones() {
		boolean shownEmpty = false;
		Bone dummyBone = null;
		for (GeosetShell geosetShell : mht.allGeoShells) {
			if (geosetShell.isDoImport()) {
				for (MatrixShell ms : geosetShell.getMatrixShells()) {
					ms.getMatrix().clear();
					for (final BoneShell bs : ms.getNewBones()) {
						if (mht.receivingModel.contains(bs.getBone())) {
							if (bs.getBone().getClass() == Helper.class) {
								JOptionPane.showMessageDialog(null,
										"Error: Holy fo shizzle my grizzle! A geoset is trying to attach to a helper, not a bone!");
							}
							ms.getMatrix().add(bs.getBone());
						} else {
							System.out.println("Boneshaving " + bs.getBone().getName() + " out of use");
						}
					}
					if (ms.getMatrix().size() == 0) {
						JOptionPane.showMessageDialog(null,
								"Error: A matrix was functionally destroyed while importing, and may take the program with it!");
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
				}
			}
		}
//			mht.receivingModel.updateObjectIds();
		for (final Geoset g : mht.receivingModel.getGeosets()) {
			g.applyMatricesToVertices(mht.receivingModel);
		}
	}

	private void copyMotionFromBones() {
		for (BoneShell bs : mht.recModBoneShells) {
			if (bs.getImportBoneShell() != null && bs.getImportBoneShell().getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
				bs.getBone().copyMotionFrom(bs.getImportBone());
			}
		}
	}

	private void addChosenNewBones(List<IdObject> objectsAdded) {
		for (BoneShell boneShell : mht.donModBoneShells) {
			// we will go through all bone shells for this
			// Fix cross-model referencing issue (force clean parent node's list of children)
			switch (boneShell.getImportStatus()) {
				case IMPORT -> {
					System.out.println("adding bone: " + boneShell);
					mht.receivingModel.add(boneShell.getBone());
					objectsAdded.add(boneShell.getBone());
					if (boneShell.getNewParentBs() != null) {
						boneShell.getBone().setParent(boneShell.getNewParentBs().getBone());
					} else {
						boneShell.getBone().setParent(null);
					}
				}
				case MOTIONFROM, DONTIMPORT -> boneShell.getBone().setParent(null);
			}
		}
	}

	private void clearRecModBoneAndHelpers() {
		for (final IdObject o : mht.receivingModel.getBones()) {
			mht.receivingModel.remove(o);
		}
		for (final IdObject o : mht.receivingModel.getHelpers()) {
			mht.receivingModel.remove(o);
		}
	}

	private List<Animation> getNewAnimations(List<AnimFlag<?>> donModFlags, List<EventObject> donModEventObjs, List<AnimFlag<?>> newImpFlags, List<EventObject> newImpEventObjs) {
		final List<Animation> newAnims = new ArrayList<>();
		for (AnimShell animShell : mht.animTabList) {
			if (animShell.getImportType() != AnimShell.ImportType.DONTIMPORT) {
				AnimShell.ImportType type = animShell.getImportType();
				int newStart = mht.receivingModel.animTrackEnd() + 300;

				Animation anim1 = animShell.getAnim();
				if (animShell.isReverse()) {
					// reverse the animation
					for (AnimFlag<?> af : donModFlags) {
						if (!af.hasGlobalSeq() && ((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
							af.timeScale2(anim1, -anim1.getLength(), anim1.getLength());
						}
					}
					for (EventObject e : donModEventObjs) {
						e.timeScale(anim1, -anim1.getLength(), anim1.getLength());
					}
				}
				switch (type) {
					case IMPORTBASIC:
					case CHANGENAME:

						//todo things here is probably broken...
						int length = anim1.getLength();
						anim1.setAnimStuff(newStart, length);
						animCopyToInterv1(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, anim1, anim1);
						if (type == AnimShell.ImportType.CHANGENAME) {
							anim1.setName(animShell.getName());
						}
						mht.receivingModel.add(anim1);
						newAnims.add(anim1);
						break;
					case TIMESCALE:
//						AnimShell importAnimShell = animShell.getImportAnimShell();
//
//						if (importAnimShell != null && importAnimShell.getImportType() == AnimShell.ImportType.TIMESCALE) {
//							importAnimShell.getAnim().copyToInterval(anim1.getStart(), anim1.getEnd(), anim1, donModFlags, donModEventObjs, newImpFlags, newImpEventObjs);
//
//							newAnims.add(new Animation("temp", anim1.getStart(), anim1.getEnd()));
//
//							if (!mht.clearExistingBones.isSelected()) {
//								for (BoneShell bs : mht.recModBoneShells) {
//									if (bs.getImportBoneShell() != null && bs.getImportBoneShell().getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
//										System.out.println("Attempting to clear animation for " + bs.getBone().getName() + " values " + anim1.getStart() + ", " + anim1.getEnd());
//										bs.getBone().clearAnimation(anim1);
//									}
//								}
//							}
//						}
						break;
					case GLOBALSEQ:
						buildGlobSeqFrom(mht.donatingModel, anim1, donModFlags);
						break;
				}
			}
		}
		if (!mht.clearRecModAnims.isSelected()) {
			addNewAnimsIntoOldAnims(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, newAnims);
		}
		return newAnims;
	}

	private void animCopyToInterv1(List<AnimFlag<?>> donModFlags, List<EventObject> donModEventObjs, List<AnimFlag<?>> newImpFlags, List<EventObject> newImpEventObjs, Animation anim1, Animation importAnim) {
//		importAnim.copyToInterval(start, start + length, anim1, donModFlags, donModEventObjs, newImpFlags, newImpEventObjs);
		for (final AnimFlag<?> af : newImpFlags) {
			if (!af.hasGlobalSeq()) {
				AnimFlag<?> source = donModFlags.get(newImpFlags.indexOf(af));
				af.copyFrom(source, importAnim, anim1);
			}
		}
		for (final EventObject e : newImpEventObjs) {
			if (!e.hasGlobalSeq()) {
				EventObject source = donModEventObjs.get(newImpEventObjs.indexOf(e));
				e.copyFrom(source, importAnim, anim1);
			}
		}
	}

	private void addNewAnimsIntoOldAnims(List<AnimFlag<?>> donModFlags, List<EventObject> donModEventObjs, List<AnimFlag<?>> newImpFlags, List<EventObject> newImpEventObjs, List<Animation> newAnims) {
		for (AnimShell animShell : mht.recModAnims) {

			AnimShell importAnimShell = animShell.getImportAnimShell();
			if (importAnimShell != null && importAnimShell.getImportType() == AnimShell.ImportType.TIMESCALE) {
				Animation anim1 = animShell.getAnim();
				int start = anim1.getStart();
				int length = anim1.getLength();

				Animation importAnim = importAnimShell.getAnim();
				animCopyToInterv1(donModFlags, donModEventObjs, newImpFlags, newImpEventObjs, anim1, importAnim);

				newAnims.add(new Animation("temp", start, start + length));

				if (!mht.clearExistingBones.isSelected()) {
					for (BoneShell bs : mht.recModBoneShells) {
						if (bs.getImportBoneShell() != null && bs.getImportBoneShell().getImportStatus() == BoneShell.ImportType.MOTIONFROM) {
							System.out.println("Attempting to clear animation for " + bs.getBone().getName() + " values " + start + ", " + start + length);
							bs.getBone().clearAnimation(anim1);
						}
					}
				}
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

				if (geoShell.getGeoset().getGeosetAnim() != null) {
					mht.receivingModel.add(geoShell.getGeoset().getGeosetAnim());
				}
			}
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

	public JFrame getParentFrame() {
		return frame;
	}

	// *********************Simple Import Functions****************
	public void animTransfer(final boolean singleAnimation, final Animation pickedAnim, final Animation visFromAnim, final boolean show) {
		mht.importAllGeos(false);
		mht.setImportStatusForAllBones(BoneShell.ImportType.MOTIONFROM);
		mht.clearRecModAnims.doClick();
		mht.importAllObjs(false);
		mht.visibilityList();
		mht.selSimButton();

		if (singleAnimation) {
			// JOptionPane.showMessageDialog(null,"single trans");
			mht.setImportTypeForAllAnims(AnimShell.ImportType.DONTIMPORT);
			for (AnimShell animShell : mht.animTabList) {
				if (animShell.getOldName().equals(pickedAnim.getName())) {
//					animShell.setDoImport(true);
					animShell.setImportType(AnimShell.ImportType.IMPORTBASIC);
				}
			}
			mht.clearRecModAnims.doClick();// turn it back off
		}

		VisibilityShell corpseShell = null;
		// Try assuming it's a unit with a corpse; they'll tend to be that way

		// Iterate through new visibility sources, find a geoset with gutz material
		for (VisibilityShell vs : mht.donModVisSourcesNew) {
			if (vs.source instanceof Geoset) {
				final Geoset g = (Geoset) vs.source;
				if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath().equalsIgnoreCase("textures\\gutz.blp")) {
					corpseShell = vs;
					break;
				}
			}
		}
		if (corpseShell != null) {
			for (VisibilityShell vs : mht.futureVisComponents) {
				if (vs.getSource() instanceof Geoset) {
					final Geoset g = (Geoset) vs.getSource();
					if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath().equalsIgnoreCase("textures\\gutz.blp")) {
						vs.setNewVisSource(corpseShell);
					}
				}
			}
		}

		if (!show) {
			applyImport();
		}
	}

	// *********************Simple Import Functions****************
	public void animTransferPartTwo(final boolean singleAnimation, final Animation pickedAnim, final Animation visFromAnim, final boolean show) {
		// This should be an import from self
		mht.importAllGeos(false);
//		mht.doImportAllAnims(false);
		mht.setImportTypeForAllAnims(AnimShell.ImportType.DONTIMPORT);
		mht.setImportStatusForAllBones(BoneShell.ImportType.DONTIMPORT);
		mht.importAllObjs(false);
		mht.visibilityList();
		mht.selSimButton();

		if (singleAnimation) {
			for (AnimShell animShell : mht.animTabList) {
				if (animShell.getOldName().equals(visFromAnim.getName())) {
//					animShell.setDoImport(true);
					animShell.setImportType(AnimShell.ImportType.TIMESCALE); // Time scale

					for (AnimShell shell : mht.recModAnims) {
						if (shell.getOldName().equals(pickedAnim.getName())) {
							animShell.setImportAnimShell(shell);
//							aniPanel.animList.setSelectedValue(shell, true);
//							aniPanel.updateSelectionPicks();
							break;
						}
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Bug in anim transfer: attempted unnecessary 2-part transfer");
		}
		for (VisibilityShell vs : mht.futureVisComponents) {
			vs.setFavorOld(false);
//			vp.favorOld.doClick();
		}

		if (!show) {
			applyImport();
		}
	}
}

