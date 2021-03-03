package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.IterableListModel;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
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
public class ImportPanel extends JTabbedPane implements ChangeListener {
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
		final ModelViewManager currentModelManager = new ModelViewManager(mht.receivingModel);
		final ModelViewManager importedModelManager = new ModelViewManager(mht.donatingModel);

		// Geoset Panel

		addTab("Geosets", geoIcon, makeGeosetPanel(), "Controls which geosets will be imported.");

		// Animation Panel

		addTab("Animation", animIcon, makeAnimationPanel(), "Controls which animations will be imported.");

		// Bone Panel
		mht.boneShellRenderer = new BoneShellListCellRenderer(currentModelManager, importedModelManager);
		final BonePanelListCellRenderer bonePanelRenderer = new BonePanelListCellRenderer(currentModelManager, importedModelManager);


		addTab("Bones", boneIcon, makeBonePanel(bonePanelRenderer), "Controls which bones will be imported.");

		// Matrices Panel
		final ParentToggleRenderer ptr = makeMatricesPanle(currentModelManager, importedModelManager);

		// Build the geosetAnimTabs list of GeosetPanels
		makeGeosetAnimPanel(ptr);

		// Objects Panel

		addTab("Objects", objIcon, makeObjecsPanel(), "Controls which objects are imported.");

		// Visibility Panel

		addTab("Visibility", orangeIcon, makeVisPanel(), "Controls the visibility of portions of the model.");

		// Listen all
		addChangeListener(this);

		makeFinalPanel();

		frame.setBounds(0, 0, 1024, 780);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				cancelButton();
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// frame.pack();
		frame.setVisible(visibleOnStart);
	}

	private JPanel makeGeosetPanel() {
		JPanel geosetsPanel = new JPanel();

		final IterableListModel<Material> materials = new IterableListModel<>();
		for (Material material : mht.receivingModel.getMaterials()) {
			materials.addElement(material);
		}
		for (Material material : mht.donatingModel.getMaterials()) {
			materials.addElement(material);
		}
		// A list of all materials available for use during this import, in
		// the form of a IterableListModel

		final MaterialListCellRenderer materialsRenderer = new MaterialListCellRenderer(mht.receivingModel);
		// All material lists will know which materials come from the
		// out-of-model source (imported model)

		// Build the geosetTabs list of GeosetPanels
		for (int i = 0; i < mht.receivingModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(false, mht.receivingModel, i, materials, materialsRenderer);

			mht.geosetTabs.addTab(mht.receivingModel.getName() + " " + (i + 1), greenIcon, geoPanel, "Click to modify material data for this geoset.");
		}
		for (int i = 0; i < mht.donatingModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(true, mht.donatingModel, i, materials, materialsRenderer);

			mht.geosetTabs.addTab(mht.donatingModel.getName() + " " + (i + 1), orangeIcon, geoPanel, "Click to modify importing and material data for this geoset.");
		}

		JButton importAllGeos = new JButton("Import All");
		importAllGeos.addActionListener(e -> importAllGeos(true));
		geosetsPanel.add(importAllGeos);

		JButton uncheckAllGeos = new JButton("Leave All");
		uncheckAllGeos.addActionListener(e -> importAllGeos(false));
		geosetsPanel.add(uncheckAllGeos);

		final GroupLayout geosetLayout = new GroupLayout(geosetsPanel);
		geosetLayout.setHorizontalGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(geosetLayout.createSequentialGroup()
						.addComponent(importAllGeos).addGap(8)
						.addComponent(uncheckAllGeos))
				.addComponent(mht.geosetTabs));
		geosetLayout.setVerticalGroup(geosetLayout.createSequentialGroup()
				.addGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllGeos)
						.addComponent(uncheckAllGeos)).addGap(8)
				.addComponent(mht.geosetTabs));
		geosetsPanel.setLayout(geosetLayout);

		return geosetsPanel;
	}

	private JPanel makeAnimationPanel() {
		JPanel animPanel = new JPanel();

		mht.existingAnims = new IterableListModel<>();
		for (int i = 0; i < mht.receivingModel.getAnims().size(); i++) {
			mht.existingAnims.addElement(new AnimShell(mht.receivingModel.getAnims().get(i)));
		}

		final AnimListCellRenderer animsRenderer = new AnimListCellRenderer();

		JButton importAllAnims = new JButton("Import All");
		importAllAnims.addActionListener(e -> uncheckAllAnims(true));
		animPanel.add(importAllAnims);

		JButton timescaleAllAnims = new JButton("Time-scale All");
		timescaleAllAnims.addActionListener(e -> timescaleAllAnims());
		animPanel.add(timescaleAllAnims);

		JButton renameAllAnims = new JButton("Import and Rename All");
		renameAllAnims.addActionListener(e -> renameAllAnims());
		animPanel.add(renameAllAnims);

		JButton uncheckAllAnims = new JButton("Leave All");
		uncheckAllAnims.addActionListener(e -> uncheckAllAnims(false));
		animPanel.add(uncheckAllAnims);

		mht.clearExistingAnims = new JCheckBox("Clear pre-existing animations");

		// Build the animTabs list of AnimPanels
		for (int i = 0; i < mht.donatingModel.getAnims().size(); i++) {
			final Animation anim = mht.donatingModel.getAnim(i);
			final AnimPanel iAnimPanel = new AnimPanel(anim, mht.existingAnims, animsRenderer);

			mht.animTabs.addTab(anim.getName(), orangeIcon, iAnimPanel,
					"Click to modify data for this animation sequence.");
		}
		mht.animTabs.addChangeListener(this);

		animPanel.add(mht.clearExistingAnims);
		animPanel.add(mht.animTabs);

		final GroupLayout animLayout = new GroupLayout(animPanel);
		animLayout.setHorizontalGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(animLayout.createSequentialGroup()
						.addComponent(importAllAnims).addGap(8)
						.addComponent(renameAllAnims).addGap(8)
						.addComponent(timescaleAllAnims).addGap(8)
						.addComponent(uncheckAllAnims))
				.addComponent(mht.clearExistingAnims)
				.addComponent(mht.animTabs));
		animLayout.setVerticalGroup(animLayout.createSequentialGroup()
				.addGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllAnims)
						.addComponent(renameAllAnims)
						.addComponent(timescaleAllAnims)
						.addComponent(uncheckAllAnims))
				.addComponent(mht.clearExistingAnims).addGap(8)
				.addComponent(mht.animTabs));
		animPanel.setLayout(animLayout);

		return animPanel;
	}

	private JPanel makeBonePanel(BonePanelListCellRenderer bonePanelRenderer) {
		mht.existingBones = new IterableListModel<>();
		final List<Bone> recModBones = mht.receivingModel.getBones();
		final List<Helper> recModHelpers = mht.receivingModel.getHelpers();
		for (Bone currentMDLBone : recModBones) {
			mht.existingBones.addElement(new BoneShell(currentMDLBone));
		}
		for (Helper currentMDLHelper : recModHelpers) {
			mht.existingBones.addElement(new BoneShell(currentMDLHelper));
		}

		final List<Bone> donModBones = mht.donatingModel.getBones();

		final List<Helper> donModHelpers = mht.donatingModel.getHelpers();

		mht.clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
		// Initialized up here for use with BonePanels

		for (int i = 0; i < donModBones.size(); i++) {
			final Bone b = donModBones.get(i);
			final BonePanel bonePanel = new BonePanel(b, mht.existingBones, mht.boneShellRenderer, this);

			mht.bonePanelCards.add(bonePanel, i + "");// (bonePanel.title.getText()));
			mht.bonePanels.addElement(bonePanel);
			mht.boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < donModHelpers.size(); i++) {
			final Bone b = donModHelpers.get(i);
			final BonePanel bonePanel = new BonePanel(b, mht.existingBones, mht.boneShellRenderer, this);

			mht.bonePanelCards.add(bonePanel, donModBones.size() + i + "");// (bonePanel.title.getText()));
			mht.bonePanels.addElement(bonePanel);
			mht.boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < mht.bonePanels.size(); i++) {
			mht.bonePanels.get(i).initList();
		}
		mht.multiBonePane = new MultiBonePanel(mht.existingBones, mht.boneShellRenderer);
		mht.bonePanelCards.add(mht.blankPane, "blank");
		mht.bonePanelCards.add(mht.multiBonePane, "multiple");
		mht.boneTabs.setCellRenderer(bonePanelRenderer);// bonePanelRenderer);
		mht.boneTabs.addListSelectionListener(e -> boneTabsValueChanged());
		mht.boneTabs.setSelectedIndex(0);
		mht.bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton importAllBones = new JButton("Import All");
		importAllBones.addActionListener(e -> importAllBones(0));
		mht.bonesPanel.add(importAllBones);

		JButton uncheckAllBones = new JButton("Leave All");
		uncheckAllBones.addActionListener(e -> importAllBones(2));
		mht.bonesPanel.add(uncheckAllBones);

		JButton motionFromBones = new JButton("Motion From All");
		motionFromBones.addActionListener(e -> importAllBones(1));
		mht.bonesPanel.add(motionFromBones);

		JButton uncheckUnusedBones = new JButton("Uncheck Unused");
		uncheckUnusedBones.addActionListener(e -> uncheckUnusedBones());
		mht.bonesPanel.add(uncheckUnusedBones);

		JScrollPane boneTabsPane = new JScrollPane(mht.boneTabs);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneTabsPane, mht.bonePanelCards);

		final GroupLayout boneLayout = new GroupLayout(mht.bonesPanel);
		boneLayout.setHorizontalGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(boneLayout.createSequentialGroup()
						.addComponent(importAllBones).addGap(8)
						.addComponent(motionFromBones).addGap(8)
						.addComponent(uncheckUnusedBones).addGap(8)
						.addComponent(uncheckAllBones))
				.addComponent(mht.clearExistingBones)
				.addComponent(splitPane)
		);
		boneLayout.setVerticalGroup(boneLayout.createSequentialGroup()
				.addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllBones)
						.addComponent(motionFromBones)
						.addComponent(uncheckUnusedBones)
						.addComponent(uncheckAllBones))
				.addComponent(mht.clearExistingBones).addGap(8)
				.addComponent(splitPane)
		);
		mht.bonesPanel.setLayout(boneLayout);
		return mht.bonesPanel;
	}

	private ParentToggleRenderer makeMatricesPanle(ModelViewManager currentModelManager, ModelViewManager importedModelManager) {
		addTab("Matrices", greenIcon, mht.geosetAnimPanel, "Controls which bones geosets are attached to.");
//		addTab("Skin", orangeIcon, new JPanel(), "Edit SKIN chunk");

		final ParentToggleRenderer ptr = new ParentToggleRenderer(mht.displayParents, currentModelManager, importedModelManager);

		mht.displayParents.addChangeListener(this);

		mht.allMatrOriginal.addActionListener(e -> allMatrOriginal());
		mht.allMatrSameName.addActionListener(e -> allMatrSameName());
		return ptr;
	}

	private JPanel makeGeosetAnimPanel(ParentToggleRenderer ptr) {
		for (int i = 0; i < mht.receivingModel.getGeosets().size(); i++) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(mht.receivingModel, mht.receivingModel.getGeoset(i), ptr, this);

			mht.geosetAnimTabs.addTab(mht.receivingModel.getName() + " " + (i + 1), greenIcon, geoPanel, "Click to modify animation data for Geoset " + i + " from " + mht.receivingModel.getName() + ".");
		}
		for (int i = 0; i < mht.donatingModel.getGeosets().size(); i++) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(mht.donatingModel, mht.donatingModel.getGeoset(i), ptr, this);

			mht.geosetAnimTabs.addTab(mht.donatingModel.getName() + " " + (i + 1), orangeIcon, geoPanel, "Click to modify animation data for Geoset " + i + " from " + mht.donatingModel.getName() + ".");
		}
		mht.geosetAnimTabs.addChangeListener(this);

		mht.geosetAnimPanel.add(mht.geosetAnimTabs);
		final GroupLayout gaLayout = new GroupLayout(mht.geosetAnimPanel);
		gaLayout.setVerticalGroup(gaLayout.createSequentialGroup()
				.addComponent(mht.displayParents)
				.addComponent(mht.allMatrOriginal)
				.addComponent(mht.allMatrSameName)
				.addComponent(mht.geosetAnimTabs));
		gaLayout.setHorizontalGroup(gaLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(mht.displayParents)
				.addComponent(mht.allMatrOriginal)
				.addComponent(mht.allMatrSameName)
				.addComponent(mht.geosetAnimTabs));
		mht.geosetAnimPanel.setLayout(gaLayout);

		return mht.geosetAnimPanel;
	}

	private JPanel makeObjecsPanel() {
		JPanel objectsPanel = new JPanel();
		JSplitPane splitPane;
		getFutureBoneListExtended(false);

		// Build the objectTabs list of ObjectPanels
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
		int panelid = 0;
		for (int i = 0; i < mht.donatingModel.getIdObjects().size(); i++) {
			final IdObject obj = mht.donatingModel.getIdObjects().get(i);
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {

				final ObjectPanel objPanel = new ObjectPanel(obj, getFutureBoneListExtended(true));

				mht.objectPanelCards.add(objPanel, panelid + "");
				mht.objectPanels.addElement(objPanel);
				panelid++;
			}
		}
		for (int i = 0; i < mht.donatingModel.getCameras().size(); i++) {
			final Camera obj = mht.donatingModel.getCameras().get(i);

			final ObjectPanel objPanel = new ObjectPanel(obj);

			mht.objectPanelCards.add(objPanel, panelid + "");// (objPanel.title.getText()));
			mht.objectPanels.addElement(objPanel);
			panelid++;
		}
		mht.multiObjectPane = new MultiObjectPanel(getFutureBoneListExtended(true));
		mht.objectPanelCards.add(mht.blankPane, "blank");
		mht.objectPanelCards.add(mht.multiObjectPane, "multiple");
		mht.objectTabs.setCellRenderer(objectPanelRenderer);
		mht.objectTabs.addListSelectionListener(e -> objectTabsValueChanged());
		mht.objectTabs.setSelectedIndex(0);
		mht.objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton importAllObjs = new JButton("Import All");
		importAllObjs.addActionListener(e -> importAllObjs(true));
		mht.bonesPanel.add(importAllObjs);

		JButton uncheckAllObjs = new JButton("Leave All");
		uncheckAllObjs.addActionListener(e -> importAllObjs(false));
		mht.bonesPanel.add(uncheckAllObjs);

		JScrollPane objectTabsPane = new JScrollPane(mht.objectTabs);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectTabsPane, mht.objectPanelCards);

		final GroupLayout objectLayout = new GroupLayout(objectsPanel);
		objectLayout.setHorizontalGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(objectLayout.createSequentialGroup()
						.addComponent(importAllObjs).addGap(8)
						.addComponent(uncheckAllObjs))
				.addComponent(splitPane));
		objectLayout.setVerticalGroup(objectLayout.createSequentialGroup()
				.addGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllObjs)
						.addComponent(uncheckAllObjs)).addGap(8)
				.addComponent(splitPane));
		objectsPanel.setLayout(objectLayout);
		return objectsPanel;
	}

	private JPanel makeVisPanel() {
		JPanel visPanel = new JPanel();
		JSplitPane splitPane;

		mht.initVisibilityList();
		mht.visibilityList();

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		for (final VisibilityShell vs : mht.allVisShells) {
			final VisibilityPanel vp = new VisibilityPanel(vs, new DefaultComboBoxModel<>(mht.visSourcesOld.toArray()), new DefaultComboBoxModel<>(mht.visSourcesNew.toArray()), visRenderer);

			mht.allVisShellPanes.add(vp);

			mht.visPanelCards.add(vp, vp.title.getText());
		}

		mht.multiVisPanel = new MultiVisibilityPanel(new DefaultComboBoxModel<>(mht.visSourcesOld.toArray()), new DefaultComboBoxModel<>(mht.visSourcesNew.toArray()), visRenderer);
		mht.visPanelCards.add(mht.blankPane, "blank");
		mht.visPanelCards.add(mht.multiVisPanel, "multiple");
		mht.visTabs.setModel(mht.visComponents);
		mht.visTabs.setCellRenderer(new VisPaneListCellRenderer(mht.receivingModel));
		mht.visTabs.addListSelectionListener(e -> visTabsValueChanged());
		mht.visTabs.setSelectedIndex(0);
		mht.visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton allInvisButton = new JButton("All Invisible in Exotic Anims");
		allInvisButton.addActionListener(e -> allVisButton(VisibilityPanel.NOTVISIBLE));
		allInvisButton.setToolTipText("Forces everything to be always invisibile in animations other than their own original animations.");
		visPanel.add(allInvisButton);

		JButton allVisButton = new JButton("All Visible in Exotic Anims");
		allVisButton.addActionListener(e -> allVisButton(VisibilityPanel.VISIBLE));
		allVisButton.setToolTipText("Forces everything to be always visibile in animations other than their own original animations.");
		visPanel.add(allVisButton);

		JButton selSimButton = new JButton("Select Similar Options");
		selSimButton.addActionListener(e -> selSimButton());
		selSimButton.setToolTipText("Similar components will be selected as visibility sources in exotic animations.");
		visPanel.add(selSimButton);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mht.visTabsPane, mht.visPanelCards);

		final GroupLayout visLayout = new GroupLayout(visPanel);
		visLayout.setHorizontalGroup(visLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(allInvisButton)// .addGap(8)
				.addComponent(allVisButton)
				.addComponent(selSimButton)
				.addComponent(splitPane));
		visLayout.setVerticalGroup(visLayout.createSequentialGroup()
				.addComponent(allInvisButton).addGap(8)
				.addComponent(allVisButton).addGap(8)
				.addComponent(selSimButton).addGap(8)
				.addComponent(splitPane));
		visPanel.setLayout(visLayout);
		return visPanel;
	}

	private void makeFinalPanel() {
		JButton okayButton = new JButton("Finish");
		okayButton.addActionListener(e -> okayButton());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> cancelButton());

		final JPanel finalPanel = new JPanel();
		final GroupLayout layout = new GroupLayout(finalPanel);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(this)
				.addGroup(layout.createSequentialGroup()
						.addComponent(cancelButton)
						.addComponent(okayButton)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(this).addGap(8)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(cancelButton)
						.addComponent(okayButton)));
		finalPanel.setLayout(layout);

		// Later add a Yes/No confirmation of "do you wish to cancel this
		// import?" when you close the window.
		frame.setContentPane(finalPanel);
	}


	private void renameAllAnims() {
		final String newTagString = JOptionPane.showInputDialog(this,
				"Choose additional naming (i.e. swim or alternate)");
		if (newTagString != null) {
			for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
				aniPanel.importTypeBox.setSelectedIndex(1);
				final String oldName = aniPanel.anim.getName();
				String baseName = oldName;
				while ((baseName.length() > 0) && baseName.contains(" ")) {
					final int lastSpaceIndex = baseName.lastIndexOf(' ');
					final String lastWord = baseName.substring(lastSpaceIndex + 1);
					boolean chunkHasInt = false;
					for (int animationId = 0; animationId < 10; animationId++) {
						if (lastWord.contains(Integer.toString(animationId))) {
							chunkHasInt = true;
						}
					}
					if (lastWord.contains("-") || chunkHasInt || lastWord.toLowerCase().contains("alternate") || (lastWord.length() <= 0)) {
						baseName = baseName.substring(0, baseName.lastIndexOf(' '));
					} else {
						break;
					}
				}
				final String afterBase = oldName.substring(Math.min(oldName.length(), baseName.length() + 1));
				final String newName = baseName + " " + newTagString + " " + afterBase;
				aniPanel.newNameEntry.setText(newName);
			}
		}
	}

	private void timescaleAllAnims() {
		for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
			aniPanel.importTypeBox.setSelectedIndex(2);
		}
	}

	private void importAllGeos(boolean b) {
		for (int i = 0; i < mht.geosetTabs.getTabCount(); i++) {
			final GeosetPanel geoPanel = (GeosetPanel) mht.geosetTabs.getComponentAt(i);
			geoPanel.setSelected(b);
		}
	}

	private void uncheckAllAnims(boolean b) {
		for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
			aniPanel.setSelected(b);
		}
	}

	private void uncheckUnusedBones() {
		// Unselect all bones by iterating + setting to index 2 ("Do not
		// import" index)
		// Bones could be referenced by:
		// - A matrix
		// - Another bone
		// - An IdObject
		final List<BonePanel> usedBonePanels = new ArrayList<>();
		for (BonePanel bonePanel : mht.bonePanels) {
			if (bonePanel.getSelectedIndex() == 0) {
			}
		}
		for (ObjectPanel objectPanel : mht.objectPanels) {
			if (objectPanel.doImport.isSelected() && (objectPanel.parentsList != null)) {
				BoneShell shell = objectPanel.parentsList.getSelectedValue();
				if ((shell != null) && (shell.bone != null)) {
					BonePanel current = getPanelOf(shell.bone);
					if (!usedBonePanels.contains(current)) {
						usedBonePanels.add(current);
					}

					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getSelectedIndex() == 1)) {
							break;
						}
						shell = current.futureBonesList.getSelectedValue();
						// If shell is null, then the bone has "No Parent"
						// If current's selected index is not 2,
						if (shell == null)// current.getSelectedIndex() != 2
						{
							good = false;
						} else {
							current = getPanelOf(shell.bone);
							if (usedBonePanels.contains(current)) {
								good = false;
							} else {
								usedBonePanels.add(current);
							}
						}
						k++;
						if (k > 1000) {
							JOptionPane.showMessageDialog(null, "Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
							break;
						}
					}
				}
			}
		}
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				for (int mk = 0; mk < bap.oldBoneRefs.size(); mk++) {
					final MatrixShell ms = bap.oldBoneRefs.get(mk);
					for (final BoneShell bs : ms.newBones) {
						BoneShell shell = bs;
						BonePanel current = getPanelOf(shell.bone);
						if (!usedBonePanels.contains(current)) {
							usedBonePanels.add(current);
						}

						boolean good = true;
						int k = 0;
						while (good) {
							if ((current == null) || (current.getSelectedIndex() == 1)) {
								break;
							}
							shell = current.futureBonesList.getSelectedValue();
							// If shell is null, then the bone has "No Parent"
							// If current's selected index is not 2,
							if (shell == null)// current.getSelectedIndex() != 2
							{
								good = false;
							} else {
								current = getPanelOf(shell.bone);
								if (usedBonePanels.contains(current)) {
									good = false;
								} else {
									usedBonePanels.add(current);
								}
							}
							k++;
							if (k > 1000) {
								JOptionPane.showMessageDialog(null, "Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
								break;
							}
						}
					}
				}
			}
		}
		for (BonePanel bonePanel : mht.bonePanels) {
			if (bonePanel.getSelectedIndex() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					BonePanel current = bonePanel;
					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getSelectedIndex() == 1)) {
							break;
						}
						final BoneShell shell = current.futureBonesList.getSelectedValue();
						// If shell is null, then the bone has "No Parent"
						// If current's selected index is not 2,
						if (shell == null)// current.getSelectedIndex() != 2
						{
							good = false;
						} else {
							current = getPanelOf(shell.bone);
							if (usedBonePanels.contains(current)) {
								good = false;
							} else {
								usedBonePanels.add(current);
							}
						}
						k++;
						if (k > 1000) {
							JOptionPane.showMessageDialog(null,
									"Unexpected error has occurred: Bone parent loop, circular logic");
							break;
						}
					}
				}
			}
		}
		for (BonePanel bonePanel : mht.bonePanels) {
			if (bonePanel.getSelectedIndex() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					bonePanel.setSelectedIndex(0);
				} else {
					bonePanel.setSelectedIndex(2);
				}
			}
		}
	}

	private void importAllBones(int selsctionIndex) {
		for (BonePanel bonePanel : mht.bonePanels) {
			bonePanel.setSelectedIndex(selsctionIndex);
		}
	}

	private void importAllObjs(boolean b) {
		for (ObjectPanel objectPanel : mht.objectPanels) {
			objectPanel.doImport.setSelected(b);
		}
	}

	private void allVisButton(String visible) {
		for (final VisibilityPanel vPanel : mht.allVisShellPanes) {
			if (vPanel.sourceShell.model == mht.receivingModel) {
				vPanel.newSourcesBox.setSelectedItem(visible);
			} else {
				vPanel.oldSourcesBox.setSelectedItem(visible);
			}
		}
	}

	private void selSimButton() {
		for (final VisibilityPanel vPanel : mht.allVisShellPanes) {
			vPanel.selectSimilarOptions();
		}
	}

	private void okayButton() {
		doImport();
		frame.setVisible(false);
	}

	private void cancelButton() {
		final Object[] options = {"Yes", "No"};
		final int n = JOptionPane.showOptionDialog(frame, "Really cancel this import?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0) {
			frame.setVisible(false);
			frame = null;
		}
	}

	private void allMatrOriginal() {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				bap.resetMatrices();
			}
		}
	}

	private void allMatrSameName() {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			if (mht.geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
				bap.setMatricesToSimilarNames();
			}
		}
	}

	private void boneTabsValueChanged() {
		// boolean listEnabledNow = false;
		if (mht.boneTabs.getSelectedValuesList().toArray().length < 1) {
			// listEnabledNow = listEnabled;
			mht.boneCardLayout.show(mht.bonePanelCards, "blank");
		} else if (mht.boneTabs.getSelectedValuesList().toArray().length == 1) {
			// listEnabledNow = true;
			mht.boneCardLayout.show(mht.bonePanelCards, (mht.boneTabs.getSelectedIndex()) + "");
			mht.boneTabs.getSelectedValue().updateSelectionPicks();
		} else if (mht.boneTabs.getSelectedValuesList().toArray().length > 1) {
			mht.boneCardLayout.show(mht.bonePanelCards, "multiple");
			// listEnabledNow = false;
			final Object[] selected = mht.boneTabs.getSelectedValuesList().toArray();
			boolean dif = false;
			int tempIndex = -99;
			for (int i = 0; (i < selected.length) && !dif; i++) {
				final BonePanel temp = (BonePanel) selected[i];
				if (tempIndex == -99) {
					tempIndex = temp.importTypeBox.getSelectedIndex();
				}
				if (tempIndex != temp.importTypeBox.getSelectedIndex()) {
					dif = true;
				}
			}
			if (dif) {
				mht.multiBonePane.setMultiTypes();
			} else {
				mht.multiBonePane.setSelectedIndex(tempIndex);
			}
		}
	}

	public void informGeosetVisibility(final Geoset g, final boolean flag) {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			final BoneAttachmentPanel geoPanel = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
			if (geoPanel.geoset == g) {
				mht.geosetAnimTabs.setEnabledAt(i, flag);
			}
		}
	}

	/**
	 * Provides a Runnable to the ImportPanel that will be run after the import has
	 * ended successfully.
	 */
	public void setCallback(final ModelStructureChangeListener callback) {
		this.callback = callback;
	}

	public BonePanel getPanelOf(final Bone b) {
		return mht.boneToPanel.get(b);
	}

	public IterableListModel<BoneShell> getFutureBoneList() {
		if (mht.oldBones == null) {
			mht.oldBones = new ArrayList<>();
			mht.newBones = new ArrayList<>();
			final List<Bone> oldBonesRefs = mht.receivingModel.getBones();
			for (final Bone b : oldBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = mht.receivingModel.getName();
				mht.oldBones.add(bs);
			}
			final List<Bone> newBonesRefs = mht.donatingModel.getBones();
			for (final Bone b : newBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = mht.donatingModel.getName();
				bs.panel = getPanelOf(b);
				mht.newBones.add(bs);
			}
		}
		if (!mht.clearExistingBones.isSelected()) {
			for (final BoneShell b : mht.oldBones) {
				if (!mht.futureBoneList.contains(b)) {
					mht.futureBoneList.addElement(b);
				}
			}
		} else {
			for (final BoneShell b : mht.oldBones) {
				if (mht.futureBoneList.contains(b)) {
					mht.futureBoneList.removeElement(b);
				}
			}
		}
		for (final BoneShell b : mht.newBones) {
			if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
				if (!mht.futureBoneList.contains(b)) {
					mht.futureBoneList.addElement(b);
				}
			} else {
				if (mht.futureBoneList.contains(b)) {
					mht.futureBoneList.removeElement(b);
				}
			}
		}
		return mht.futureBoneList;
	}


	public IterableListModel<BoneShell> getFutureBoneListExtended(final boolean newSnapshot) {
		long totalAddTime = 0;
		long addCount = 0;
		long totalRemoveTime = 0;
		long removeCount = 0;
		if (mht.oldHelpers == null) {
			mht.oldHelpers = new ArrayList<>();
			mht.newHelpers = new ArrayList<>();
			List<? extends Bone> oldHelpersRefs = mht.receivingModel.getBones();
			for (final Bone b : oldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = mht.receivingModel.getName();
				bs.showClass = true;
				mht.oldHelpers.add(bs);
			}
			oldHelpersRefs = mht.receivingModel.getHelpers();
			for (final Bone b : oldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = mht.receivingModel.getName();
				bs.showClass = true;
				mht.oldHelpers.add(bs);
			}
			List<? extends Bone> newHelpersRefs = mht.donatingModel.getBones();
			for (final Bone b : newHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = mht.donatingModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				mht.newHelpers.add(bs);
			}
			newHelpersRefs = mht.donatingModel.getHelpers();
			for (final Bone b : newHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = mht.donatingModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				mht.newHelpers.add(bs);
			}
		}
		if (!mht.clearExistingBones.isSelected()) {
			for (final BoneShell b : mht.oldHelpers) {
				if (!mht.futureBoneListExQuickLookupSet.contains(b)) {
					final long startTime = System.nanoTime();
					mht.futureBoneListEx.addElement(b);
					final long endTime = System.nanoTime();
					totalAddTime += (endTime - startTime);
					addCount++;
					mht.futureBoneListExQuickLookupSet.add(b);
				}
			}
		} else {
			for (final BoneShell b : mht.oldHelpers) {
				if (mht.futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					mht.futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += (endTime - startTime);
					removeCount++;
				}
			}
		}
		for (final BoneShell b : mht.newHelpers) {
			b.panel = getPanelOf(b.bone);
			if (b.panel != null) {
				if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
					if (!mht.futureBoneListExQuickLookupSet.contains(b)) {
						final long startTime = System.nanoTime();
						mht.futureBoneListEx.addElement(b);
						final long endTime = System.nanoTime();
						totalAddTime += (endTime - startTime);
						addCount++;
						mht.futureBoneListExQuickLookupSet.add(b);
					}
				} else {
					if (mht.futureBoneListExQuickLookupSet.remove(b)) {
						final long startTime = System.nanoTime();
						mht.futureBoneListEx.removeElement(b);
						final long endTime = System.nanoTime();
						totalRemoveTime += (endTime - startTime);
						removeCount++;
					}
				}
			}
		}
		if (addCount != 0) {
			System.out.println("average add time: " + (totalAddTime / addCount));
			System.out.println("add count: " + addCount);
		}
		if (removeCount != 0) {
			System.out.println("average remove time: " + (totalRemoveTime / removeCount));
			System.out.println("remove count: " + removeCount);
		}

		final IterableListModel<BoneShell> listModelToReturn;
		if (newSnapshot || mht.futureBoneListExFixableItems.isEmpty()) {
			final IterableListModel<BoneShell> futureBoneListReplica = new IterableListModel<>();
			mht.futureBoneListExFixableItems.add(futureBoneListReplica);
			listModelToReturn = futureBoneListReplica;
		} else {
			listModelToReturn = mht.futureBoneListExFixableItems.get(0);
		}
		// We CANT call clear, we have to preserve
		// the parent list
		for (final IterableListModel<BoneShell> model : mht.futureBoneListExFixableItems) {
			// clean things that should not be there
			for (BoneShell previousElement : model) {
				if (!mht.futureBoneListExQuickLookupSet.contains(previousElement)) {
					model.remove(previousElement);
				}
			}
			// add back things who should be there
			for (BoneShell elementAt : mht.futureBoneListEx) {
				if (!model.contains(elementAt)) {
					model.addElement(elementAt);
				}
			}
		}
		return listModelToReturn;
	}


	private void objectTabsValueChanged() {
		if (mht.objectTabs.getSelectedValuesList().toArray().length < 1) {
			mht.objectCardLayout.show(mht.objectPanelCards, "blank");
		} else if (mht.objectTabs.getSelectedValuesList().toArray().length == 1) {
			getFutureBoneListExtended(false);
			mht.objectCardLayout.show(mht.objectPanelCards, (mht.objectTabs.getSelectedIndex()) + "");// .title.getText()
		} else if (mht.objectTabs.getSelectedValuesList().toArray().length > 1) {
			mht.objectCardLayout.show(mht.objectPanelCards, "multiple");
			final Object[] selected = mht.objectTabs.getSelectedValuesList().toArray();
			boolean dif = false;
			boolean set = false;
			boolean selectedt = false;
			for (int i = 0; (i < selected.length) && !dif; i++) {
				final ObjectPanel temp = (ObjectPanel) selected[i];
				if (!set) {
					set = true;
					selectedt = temp.doImport.isSelected();
				} else if (selectedt != temp.doImport.isSelected()) {
					dif = true;
				}
			}
			if (!dif) {
				mht.multiObjectPane.doImport.setSelected(selectedt);
			}
		}
	}
	private void visTabsValueChanged() {
		if (mht.visTabs.getSelectedValuesList().toArray().length < 1) {
			mht.visCardLayout.show(mht.visPanelCards, "blank");
		} else if (mht.visTabs.getSelectedValuesList().toArray().length == 1) {
			mht.visCardLayout.show(mht.visPanelCards, mht.visTabs.getSelectedValue().title.getText());
		} else if (mht.visTabs.getSelectedValuesList().toArray().length > 1) {
			mht.visCardLayout.show(mht.visPanelCards, "multiple");
			final Object[] selected = mht.visTabs.getSelectedValuesList().toArray();

			boolean dif = false;
			boolean set = false;
			boolean selectedt = false;

			boolean difBoxOld = false;
			boolean difBoxNew = false;
			int tempIndexOld = -99;
			int tempIndexNew = -99;

			for (int i = 0; (i < selected.length) && !dif; i++) {
				final VisibilityPanel temp = (VisibilityPanel) selected[i];
				if (!set) {
					set = true;
					selectedt = temp.favorOld.isSelected();
				} else if (selectedt != temp.favorOld.isSelected()) {
					dif = true;
				}

				if (tempIndexOld == -99) {
					tempIndexOld = temp.oldSourcesBox.getSelectedIndex();
				}
				if (tempIndexOld != temp.oldSourcesBox.getSelectedIndex()) {
					difBoxOld = true;
				}

				if (tempIndexNew == -99) {
					tempIndexNew = temp.newSourcesBox.getSelectedIndex();
				}
				if (tempIndexNew != temp.newSourcesBox.getSelectedIndex()) {
					difBoxNew = true;
				}
			}
			if (!dif) {
				mht.multiVisPanel.favorOld.setSelected(selectedt);
			}
			if (difBoxOld) {
				mht.multiVisPanel.setMultipleOld();
			} else {
				mht.multiVisPanel.oldSourcesBox.setSelectedIndex(tempIndexOld);
			}
			if (difBoxNew) {
				mht.multiVisPanel.setMultipleNew();
			} else {
				mht.multiVisPanel.newSourcesBox.setSelectedIndex(tempIndexNew);
			}
		}
	}

	public void setSelectedItem(final String what) {
		for (BonePanel temp : mht.boneTabs.getSelectedValuesList()) {
			temp.setSelectedValue(what);
		}
	}

	/**
	 * The method run when the user pushes the "Set Parent for All" button in the
	 * MultiBone panel.
	 */
	public void setParentMultiBones() {
		final JList<BoneShell> list = new JList<>(getFutureBoneListExtended(true));
		list.setCellRenderer(mht.boneShellRenderer);
		final int x = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Set Parent for All Selected Bones", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			for (BonePanel temp : mht.boneTabs.getSelectedValuesList()) {
				temp.setParent(list.getSelectedValue());
			}
		}
	}

	public void setObjGroupSelected(final boolean flag) {
		for (ObjectPanel temp : mht.objectTabs.getSelectedValuesList()) {
			temp.doImport.setSelected(flag);
		}
	}

	public void setVisGroupSelected(final boolean flag) {
		for (VisibilityPanel temp : mht.visTabs.getSelectedValuesList()) {
			temp.favorOld.setSelected(flag);
		}
	}

	public void setVisGroupItemOld(final Object o) {
		for (VisibilityPanel temp : mht.visTabs.getSelectedValuesList()) {
			temp.oldSourcesBox.setSelectedItem(o);
		}
	}

	public void setVisGroupItemNew(final Object o) {
		for (VisibilityPanel temp : mht.visTabs.getSelectedValuesList()) {
			temp.newSourcesBox.setSelectedItem(o);
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		((AnimPanel) mht.animTabs.getSelectedComponent()).updateSelectionPicks();
		getFutureBoneList();
		getFutureBoneListExtended(false);
		mht.visibilityList();
		repaint();
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
									if (getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
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
						if (getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
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
		importAllGeos(false);
		importAllBones(1);
		mht.clearExistingAnims.doClick();
		importAllObjs(false);
		mht.visibilityList();
		selSimButton();

		if (singleAnimation) {
			// JOptionPane.showMessageDialog(null,"single trans");
			uncheckAllAnims(false);
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
		for (int i = 0; (i < mht.visSourcesNew.size()) && (corpseShell == null); i++) {
			if (mht.visSourcesNew.get(i) instanceof VisibilityShell) {
				final VisibilityShell vs = (VisibilityShell) mht.visSourcesNew.get(i);
				if (vs.source instanceof Geoset) {
					final Geoset g = (Geoset) vs.source;
					if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath().equalsIgnoreCase("textures\\gutz.blp")) {
						corpseShell = vs;
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
			okayButton();
		}
	}

	// *********************Simple Import Functions****************
	public void animTransferPartTwo(final boolean singleAnimation, final Animation pickedAnim, final Animation visFromAnim, final boolean show) {
		// This should be an import from self
		importAllGeos(false);
		uncheckAllAnims(false);
		importAllBones(2);
		importAllObjs(false);
		mht.visibilityList();
		selSimButton();

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
			okayButton();
		}
	}
}

