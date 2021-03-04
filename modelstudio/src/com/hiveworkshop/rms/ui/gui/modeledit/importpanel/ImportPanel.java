package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;
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
		mht.receivingModel.doSavePreps();
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

		// Listen all
		addChangeListener(mht.getDaChangeListener());

		final JPanel containerPanel = new JPanel(new MigLayout("gap 0, fill", "[grow]", "[grow]"));
		containerPanel.add(this, "wrap");
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

			for (int i = 0; i < mht.geosetTabs.getTabCount(); i++) {
				final GeosetPanel gp = (GeosetPanel) mht.geosetTabs.getComponentAt(i);
				gp.geoset.setMaterial(gp.getSelectedMaterial());
				if (gp.doImport.isSelected() && (gp.model == mht.donatingModel)) {
					mht.receivingModel.add(gp.geoset);
					if (gp.geoset.getGeosetAnim() != null) {
						mht.receivingModel.add(gp.geoset.getGeosetAnim());
					}
				}
			}

			final List<Animation> oldAnims = new ArrayList<>(mht.receivingModel.getAnims());
			final List<Animation> newAnims = new ArrayList<>();

			final List<AnimFlag<?>> recModFlags = mht.receivingModel.getAllAnimFlags();
			final List<AnimFlag<?>> donModFlags = mht.donatingModel.getAllAnimFlags();

			final List<EventObject> recModEventObjs = mht.receivingModel.getEvents();
			final List<EventObject> donModEventObjs = mht.donatingModel.getEvents();

			// note to self: remember to scale event objects with time
			final List<AnimFlag<?>> newImpFlags = new ArrayList<>();
			for (final AnimFlag<?> af : donModFlags) {
				if (!af.hasGlobalSeq()) {
					newImpFlags.add(AnimFlag.buildEmptyFrom(af));
				} else {
					newImpFlags.add(AnimFlag.createFromAnimFlag(af));
				}
			}
			final List<EventObject> newImpEventObjs = new ArrayList<>();
			for (EventObject e : donModEventObjs) {
				newImpEventObjs.add(EventObject.buildEmptyFrom(e));
			}
			final boolean clearAnims = mht.clearExistingAnims.isSelected();
			if (clearAnims) {
				for (final Animation anim : mht.receivingModel.getAnims()) {
					anim.clearData(recModFlags, recModEventObjs);
				}
				mht.receivingModel.getAnims().clear();
			}
			for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
				if (aniPanel.doImport.isSelected()) {
					final int type = aniPanel.importTypeBox.getSelectedIndex();
					final int animTrackEnd = mht.receivingModel.animTrackEnd();
					if (aniPanel.inReverse.isSelected()) {
						// reverse the animation
						aniPanel.anim.reverse(donModFlags, donModEventObjs);
					}
					switch (type) {
						case 0:
							aniPanel.anim.copyToInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300, donModFlags, donModEventObjs, newImpFlags, newImpEventObjs);
							aniPanel.anim.setInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300);
							mht.receivingModel.add(aniPanel.anim);
							newAnims.add(aniPanel.anim);
							break;
						case 1:
							aniPanel.anim.copyToInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300, donModFlags, donModEventObjs, newImpFlags, newImpEventObjs);
							aniPanel.anim.setInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300);
							aniPanel.anim.setName(aniPanel.newNameEntry.getText());
							mht.receivingModel.add(aniPanel.anim);
							newAnims.add(aniPanel.anim);
							break;
						case 2:
							// List<AnimShell> targets = aniPane.animList.getSelectedValuesList();
							// aniPanel.anim.setInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300, donModFlags,
							// donModEventObjs, newImpFlags, newImpEventObjs);
							// handled by animShells
							break;
						case 3:
							mht.donatingModel.buildGlobSeqFrom(aniPanel.anim, donModFlags);
							break;
					}
				}
			}
			final boolean clearBones = mht.clearExistingBones.isSelected();
			if (!clearAnims) {
				for (AnimShell animShell : mht.existingAnims) {
					if (animShell.importAnim != null) {
						animShell.importAnim.copyToInterval(animShell.anim.getStart(), animShell.anim.getEnd(), donModFlags, donModEventObjs, newImpFlags, newImpEventObjs);
						final Animation tempAnim = new Animation("temp", animShell.anim.getStart(), animShell.anim.getEnd());
						newAnims.add(tempAnim);
						if (!clearBones) {
							for (BoneShell bs : mht.existingBones) {
								if (bs.importBone != null) {
									if (mht.getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
										System.out.println(
												"Attempting to clear animation for " + bs.bone.getName() + " values " + animShell.anim.getStart() + ", " + animShell.anim.getEnd());
										bs.bone.clearAnimation(animShell.anim);
									}
								}
							}
						}
					}
				}
			}
			// Now, rebuild the old animflags with the new
			for (final AnimFlag<?> af : donModFlags) {
				af.setValuesTo(newImpFlags.get(donModFlags.indexOf(af)));
			}
			for (final EventObject e : donModEventObjs) {
				e.setValuesTo(newImpEventObjs.get(donModEventObjs.indexOf(e)));
			}

			if (clearBones) {
				for (final IdObject o : mht.receivingModel.getBones()) {
					mht.receivingModel.remove(o);
				}
				for (final IdObject o : mht.receivingModel.getHelpers()) {
					mht.receivingModel.remove(o);
				}
			}
			final List<IdObject> objectsAdded = new ArrayList<>();
			final List<Camera> camerasAdded = new ArrayList<>();
			for (BonePanel bonePanel : mht.bonePanels) {
				final Bone b = bonePanel.bone;
				final int type = bonePanel.importTypeBox.getSelectedIndex();
				// we will go through all bone shells for this
				// Fix cross-model referencing issue (force clean parent node's list of children)
				switch (type) {
					case 0 -> {
						mht.receivingModel.add(b);
						objectsAdded.add(b);
						final BoneShell mbs = bonePanel.futureBonesList.getSelectedValue();
						if (mbs != null) {
							b.setParent((mbs).bone);
						} else {
							b.setParent(null);
						}
					}
					case 1, 2 -> b.setParent(null);
				}
			}
			if (!clearBones) {
				for (int i = 0; i < mht.existingBones.size(); i++) {
					final BoneShell bs = mht.existingBones.get(i);
					if (bs.importBone != null) {
						if (mht.getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
							bs.bone.copyMotionFrom(bs.importBone);
						}
					}
				}
			}
			// IteratableListModel<BoneShell> bones = getFutureBoneList();
			boolean shownEmpty = false;
			Bone dummyBone = null;
			for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
				if (mht.geosetAnimTabs.isEnabledAt(i)) {
					final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
					for (int l = 0; l < bap.oldBoneRefs.size(); l++) {
						final MatrixShell ms = bap.oldBoneRefs.get(l);
						ms.matrix.getBones().clear();
						for (final BoneShell bs : ms.newBones) {
							if (mht.receivingModel.contains(bs.bone)) {
								if (bs.bone.getClass() == Helper.class) {
									JOptionPane.showMessageDialog(null,
											"Error: Holy fo shizzle my grizzle! A geoset is trying to attach to a helper, not a bone!");
								}
								ms.matrix.add(bs.bone);
							} else {
								System.out.println("Boneshaving " + bs.bone.getName() + " out of use");
							}
						}
						if (ms.matrix.size() == 0) {
							JOptionPane.showMessageDialog(null,
									"Error: A matrix was functionally destroyed while importing, and may take the program with it!");
						}
						if (ms.matrix.getBones().size() < 1) {
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
							if (!ms.matrix.getBones().contains(dummyBone)) {
								ms.matrix.getBones().add(dummyBone);
							}
						}
					}
				}
			}
			mht.receivingModel.updateObjectIds();
			for (final Geoset g : mht.receivingModel.getGeosets()) {
				g.applyMatricesToVertices(mht.receivingModel);
			}

			// Objects!
			for (ObjectPanel objectPanel : mht.objectPanels) {
				if (objectPanel.doImport.isSelected()) {
					if (objectPanel.object != null) {
						final BoneShell mbs = objectPanel.parentsList.getSelectedValue();
						if (mbs != null) {
							objectPanel.object.setParent(mbs.bone);
						} else {
							objectPanel.object.setParent(null);
						}
						// later make a name field?
						mht.receivingModel.add(objectPanel.object);
						objectsAdded.add(objectPanel.object);
					} else if (objectPanel.camera != null) {
						mht.receivingModel.add(objectPanel.camera);
						camerasAdded.add(objectPanel.camera);
					}
				} else {
					if (objectPanel.object != null) {
						objectPanel.object.setParent(null);
						// Fix cross-model referencing issue (force clean parent node's list of children)
					}
				}
			}

			final List<AnimFlag<?>> finalVisFlags = new ArrayList<>();
			for (VisibilityPanel vPanel : mht.visComponents) {
				final VisibilitySource temp = ((VisibilitySource) vPanel.sourceShell.source);
				final AnimFlag<?> visFlag = temp.getVisibilityFlag();// might be null
				final AnimFlag<?> newVisFlag;
				boolean tans = false;
				if (visFlag != null) {
					newVisFlag = AnimFlag.buildEmptyFrom(visFlag);
					tans = visFlag.tans();
				} else {
					newVisFlag = new FloatAnimFlag(temp.visFlagName());
				}
				// newVisFlag = new AnimFlag(temp.visFlagName());
				final Object oldSource = vPanel.oldSourcesBox.getSelectedItem();
				FloatAnimFlag flagOld = null;
				if (oldSource.getClass() == String.class) {
					if (oldSource == VisibilityPanel.NOTVISIBLE) {
						flagOld = new FloatAnimFlag("temp");
						for (final Animation a : oldAnims) {
							if (tans) {
								flagOld.addEntry(a.getStart(), 0f, 0f, 0f);
							} else {
								flagOld.addEntry(a.getStart(), 0f);
							}
						}
					}
				} else {
					flagOld = (FloatAnimFlag) ((VisibilitySource) ((VisibilityShell) oldSource).source).getVisibilityFlag();
				}
				final Object newSource = vPanel.newSourcesBox.getSelectedItem();
				FloatAnimFlag flagNew = null;
				if (newSource.getClass() == String.class) {
					if (newSource == VisibilityPanel.NOTVISIBLE) {
						flagNew = new FloatAnimFlag("temp");
						for (final Animation a : newAnims) {
							if (tans) {
								flagNew.addEntry(a.getStart(), 0f, 0f, 0f);
							} else {
								flagNew.addEntry(a.getStart(), 0f);
							}
						}
					}
				} else {
					flagNew = (FloatAnimFlag) ((VisibilitySource) ((VisibilityShell) newSource).source).getVisibilityFlag();
				}
				if ((vPanel.favorOld.isSelected() && (vPanel.sourceShell.model == mht.receivingModel) && !clearAnims) || (!vPanel.favorOld.isSelected() && (vPanel.sourceShell.model == mht.donatingModel))) {
					// this is an element favoring existing animations over imported
					for (final Animation a : oldAnims) {
						if (flagNew != null) {
							if (!flagNew.hasGlobalSeq()) {
								flagNew.deleteAnim(a);
								// All entries for visibility are deleted from imported
								// sources during existing animation times
							}
						}
					}
				} else {
					// this is an element not favoring existing over imported
					for (final Animation a : newAnims) {
						if (flagOld != null) {
							if (!flagOld.hasGlobalSeq()) {
								flagOld.deleteAnim(a);
								// All entries for visibility are deleted from
								// original-based sources during imported animation times
							}
						}
					}
				}
				if (flagOld != null) {
					newVisFlag.copyFrom(flagOld);
				}
				if (flagNew != null) {
					newVisFlag.copyFrom(flagNew);
				}
				finalVisFlags.add(newVisFlag);
			}
			for (int i = 0; i < mht.visComponents.size(); i++) {
				final VisibilityPanel vPanel = mht.visComponents.get(i);
				final VisibilitySource temp = ((VisibilitySource) vPanel.sourceShell.source);
				final AnimFlag<?> visFlag = finalVisFlags.get(i);// might be null
				if (visFlag.size() > 0) {
					temp.setVisibilityFlag(visFlag);
				} else {
					temp.setVisibilityFlag(null);
				}
			}

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
			final List<Geoset> geosetsAdded = new ArrayList<>();
			for (int i = 0; i < mht.geosetTabs.getTabCount(); i++) {
				final GeosetPanel gp = (GeosetPanel) mht.geosetTabs.getComponentAt(i);
				if (gp.doImport.isSelected() && (gp.model == mht.donatingModel)) {
					geosetsAdded.add(gp.geoset);
				}
			}
			if (callback != null) {
				callback.geosetsAdded(geosetsAdded);
				callback.nodesAdded(objectsAdded);
				callback.camerasAdded(camerasAdded);
			}
			for (final AnimFlag<?> flag : mht.receivingModel.getAllAnimFlags()) {
				flag.sort();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}

		importEnded = true;
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
		mht.setImportStatusForAllBones(1);
		mht.clearExistingAnims.doClick();
		mht.importAllObjs(false);
		mht.visibilityList();
		mht.selSimButton();

		if (singleAnimation) {
			// JOptionPane.showMessageDialog(null,"single trans");
			mht.uncheckAllAnims(false);
			for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
				if (aniPanel.anim.getName().equals(pickedAnim.getName())) {
					aniPanel.doImport.setSelected(true);
				}
			}
			mht.clearExistingAnims.doClick();// turn it back off
		}

		VisibilityShell corpseShell = null;
		// Try assuming it's a unit with a corpse; they'll tend to be that way

		// Iterate through new visibility sources, find a geoset with gutz material
		for (Object o : mht.donModVisSourcesNew) {
			if (o instanceof VisibilityShell) {
				final VisibilityShell vs = (VisibilityShell) o;
				if (vs.source instanceof Geoset) {
					final Geoset g = (Geoset) vs.source;
					if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath().equalsIgnoreCase("textures\\gutz.blp")) {
						corpseShell = vs;
						break;
					}
				}
			}
		}
		if (corpseShell != null) {
			for (VisibilityPanel vp : mht.visComponents) {
				if (vp.sourceShell.source instanceof Geoset) {
					final Geoset g = (Geoset) vp.sourceShell.source;
					if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath().equalsIgnoreCase("textures\\gutz.blp")) {
						vp.newSourcesBox.setSelectedItem(corpseShell);
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
		mht.uncheckAllAnims(false);
		mht.setImportStatusForAllBones(2);
		mht.importAllObjs(false);
		mht.visibilityList();
		mht.selSimButton();

		if (singleAnimation) {
			for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
				if (aniPanel.anim.getName().equals(visFromAnim.getName())) {
					aniPanel.doImport.doClick();
					aniPanel.importTypeBox.setSelectedItem(AnimPanel.TIMESCALE);

					for (AnimShell shell : mht.existingAnims) {
						if ((shell).anim.getName().equals(pickedAnim.getName())) {
							aniPanel.animList.setSelectedValue(shell, true);
							aniPanel.updateSelectionPicks();
							break;
						}
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Bug in anim transfer: attempted unnecessary 2-part transfer");
		}
		for (VisibilityPanel vp : mht.visComponents) {
			vp.favorOld.doClick();
		}

		if (!show) {
			applyImport();
		}
	}
}

