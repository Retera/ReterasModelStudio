package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixShell;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * The panel to handle the import function.
 *
 * Eric Theller 6/11/2012
 */
public class ImportPanel extends JTabbedPane implements ActionListener, ListSelectionListener, ChangeListener {
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

	EditableModel currentModel;
	EditableModel importedModel;

	// Geosets
//	JPanel geosetsPanel = new JPanel();
//	JButton importAllGeos, uncheckAllGeos;
	JTabbedPane geosetTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	// Animation
//	JPanel animPanel = new JPanel();
//	JButton importAllAnims, timescaleAllAnims, uncheckAllAnims, renameAllAnims;
	JCheckBox clearExistingAnims;
	JTabbedPane animTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
	DefaultListModel<AnimShell> existingAnims;

	// Bones
	JPanel bonesPanel = new JPanel();
	//	JButton importAllBones, uncheckAllBones, motionFromBones, uncheckUnusedBones;
	JCheckBox clearExistingBones;
	// JTabbedPane boneTabs = new
	// JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
	DefaultListModel<BonePanel> bonePanels = new DefaultListModel<>();
	private final Map<Bone, BonePanel> boneToPanel = new HashMap<>();
	JList<BonePanel> boneTabs = new JList<>(bonePanels);
	//	JScrollPane boneTabsPane = new JScrollPane(boneTabs);
	// DefaultListModel<BonePanel> oldBonePanels = new
	// DefaultListModel<BonePanel>();
	// JList oldBoneTabs = new JList(oldBonePanels);
	// JScrollPane oldBoneTabsPane = new JScrollPane(oldBoneTabs);
	CardLayout boneCardLayout = new CardLayout();
	JPanel bonePanelCards = new JPanel(boneCardLayout);
	JPanel blankPane = new JPanel();
	MultiBonePanel multiBonePane;
	DefaultListModel<BoneShell> existingBones;

	// Matrices
	JPanel geosetAnimPanel = new JPanel();
	JTabbedPane geosetAnimTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	DefaultListModel<BoneShell> futureBoneList = new DefaultListModel<>();
	List<BoneShell> oldBones;
	List<BoneShell> newBones;

	JCheckBox displayParents = new JCheckBox("Display parent names");
	JButton allMatrOriginal = new JButton("Reset all Matrices"),
			allMatrSameName = new JButton("Set all to available, original names");

	// Objects
//	JPanel objectsPanel = new JPanel();
	// JTabbedPane objectTabs = new
	// JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
	DefaultListModel<ObjectPanel> objectPanels = new DefaultListModel<>();
	JList<ObjectPanel> objectTabs = new JList<>(objectPanels);
	//	JScrollPane objectTabsPane = new JScrollPane(objectTabs);
	CardLayout objectCardLayout = new CardLayout();
	JPanel objectPanelCards = new JPanel(objectCardLayout);
	MultiObjectPanel multiObjectPane;

//	JButton importAllObjs, uncheckAllObjs;

	// Visibility
//	JPanel visPanel = new JPanel();
	// DefaultListModel<VisibilityPane> visPanels = new
	// DefaultListModel<VisibilityPane>();
	JList<VisibilityPanel> visTabs = new JList<>();
	JScrollPane visTabsPane = new JScrollPane(visTabs);
	CardLayout visCardLayout = new CardLayout();
	JPanel visPanelCards = new JPanel(visCardLayout);
	MultiVisibilityPanel multiVisPanel;

//	JButton allInvisButton, allVisButton, selSimButton;

//	JButton okayButton, cancelButton;

	boolean importSuccess = false;
	boolean importStarted = false;
	boolean importEnded = false;

	private ModelStructureChangeListener callback;

	public ImportPanel(final EditableModel a, final EditableModel b) {
		this(a, b, true);
	}

	DefaultListModel<VisibilityPanel> visComponents;
	ArrayList<VisibilityPanel> allVisShellPanes = new ArrayList<>();

	public ImportPanel(final EditableModel currentModel, final EditableModel importedModel, final boolean visibleOnStart) {
		super();
		if (currentModel.getName().equals(importedModel.getName())) {
			importedModel.setFileRef(new File(importedModel.getFile().getParent() + "/" + importedModel.getName() + " (Imported)" + ".mdl"));
			frame = new JFrame("Importing " + currentModel.getName() + " into itself");
		} else {
			frame = new JFrame("Importing " + importedModel.getName() + " into " + currentModel.getName());
		}
		currentModel.doSavePreps();
		try {
			frame.setIconImage(RMSIcons.MDLIcon.getImage());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Image files were not found! Due to bad programming, this might break the program!");
		}
		this.currentModel = currentModel;
		this.importedModel = importedModel;
		final ModelViewManager currentModelManager = new ModelViewManager(currentModel);
		final ModelViewManager importedModelManager = new ModelViewManager(importedModel);

		// Geoset Panel
		makeGeosetPanel(currentModel, importedModel);

		// Animation Panel
		makeAnimationPanel(currentModel, importedModel);

		// Bone Panel
		boneShellRenderer = new BoneShellListCellRenderer(currentModelManager, importedModelManager);
		final BonePanelListCellRenderer bonePanelRenderer = new BonePanelListCellRenderer(currentModelManager, importedModelManager);

		makeBonePanel(currentModel, importedModel, bonePanelRenderer);

		// Matrices Panel
		final ParentToggleRenderer ptr = makeMatricesPanle(currentModelManager, importedModelManager);

		// Build the geosetAnimTabs list of GeosetPanels
		MakeGeosetAnimPanel(currentModel, importedModel, ptr);

		// Objects Panel
		makeObjecsPanel(importedModel);

		// Visibility Panel
		makeVisPanel(currentModel);

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

	private void makeGeosetPanel(EditableModel currentModel, EditableModel importedModel) {
		JPanel geosetsPanel = new JPanel();
		addTab("Geosets", geoIcon, geosetsPanel, "Controls which geosets will be imported.");

		final DefaultListModel<Material> materials = new DefaultListModel<>();
		for (int i = 0; i < currentModel.getMaterials().size(); i++) {
			materials.addElement(currentModel.getMaterials().get(i));
		}
		for (int i = 0; i < importedModel.getMaterials().size(); i++) {
			materials.addElement(importedModel.getMaterials().get(i));
		}
		// A list of all materials available for use during this import, in
		// the form of a DefaultListModel

		final MaterialListCellRenderer materialsRenderer = new MaterialListCellRenderer(currentModel);
		// All material lists will know which materials come from the
		// out-of-model source (imported model)

		// Build the geosetTabs list of GeosetPanels
		for (int i = 0; i < currentModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(false, currentModel, i, materials, materialsRenderer);

			geosetTabs.addTab(currentModel.getName() + " " + (i + 1), greenIcon, geoPanel, "Click to modify material data for this geoset.");
		}
		for (int i = 0; i < importedModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(true, importedModel, i, materials, materialsRenderer);

			geosetTabs.addTab(importedModel.getName() + " " + (i + 1), orangeIcon, geoPanel, "Click to modify importing and material data for this geoset.");
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
				.addComponent(geosetTabs));
		geosetLayout.setVerticalGroup(geosetLayout.createSequentialGroup()
				.addGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllGeos)
						.addComponent(uncheckAllGeos)).addGap(8)
				.addComponent(geosetTabs));
		geosetsPanel.setLayout(geosetLayout);
	}

	private void makeAnimationPanel(EditableModel currentModel, EditableModel importedModel) {
		JPanel animPanel = new JPanel();
		addTab("Animation", animIcon, animPanel, "Controls which animations will be imported.");

		existingAnims = new DefaultListModel();
		for (int i = 0; i < currentModel.getAnims().size(); i++) {
			existingAnims.addElement(new AnimShell(currentModel.getAnims().get(i)));
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

		clearExistingAnims = new JCheckBox("Clear pre-existing animations");

		// Build the animTabs list of AnimPanels
		for (int i = 0; i < importedModel.getAnims().size(); i++) {
			final Animation anim = importedModel.getAnim(i);
			final AnimPanel iAnimPanel = new AnimPanel(anim, existingAnims, animsRenderer);

			animTabs.addTab(anim.getName(), orangeIcon, iAnimPanel,
					"Click to modify data for this animation sequence.");
		}
		animTabs.addChangeListener(this);

		animPanel.add(clearExistingAnims);
		animPanel.add(animTabs);

		final GroupLayout animLayout = new GroupLayout(animPanel);
		animLayout.setHorizontalGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(animLayout.createSequentialGroup()
						.addComponent(importAllAnims).addGap(8)
						.addComponent(renameAllAnims).addGap(8)
						.addComponent(timescaleAllAnims).addGap(8)
						.addComponent(uncheckAllAnims))
				.addComponent(clearExistingAnims)
				.addComponent(animTabs));
		animLayout.setVerticalGroup(animLayout.createSequentialGroup()
				.addGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllAnims)
						.addComponent(renameAllAnims)
						.addComponent(timescaleAllAnims)
						.addComponent(uncheckAllAnims))
				.addComponent(clearExistingAnims).addGap(8)
				.addComponent(animTabs));
		animPanel.setLayout(animLayout);
	}

	private void makeBonePanel(EditableModel currentModel, EditableModel importedModel, BonePanelListCellRenderer bonePanelRenderer) {
		addTab("Bones", boneIcon, bonesPanel, "Controls which bones will be imported.");
		existingBones = new DefaultListModel<>();
		final List<Bone> currentMDLBones = currentModel.sortedIdObjects(Bone.class);
		final List<Helper> currentMDLHelpers = currentModel.sortedIdObjects(Helper.class);
		for (Bone currentMDLBone : currentMDLBones) {
			existingBones.addElement(new BoneShell(currentMDLBone));
		}
		for (Helper currentMDLHelper : currentMDLHelpers) {
			existingBones.addElement(new BoneShell(currentMDLHelper));
		}

		final List<Bone> importedMDLBones = importedModel.sortedIdObjects(Bone.class);
		final List<Helper> importedMDLHelpers = importedModel.sortedIdObjects(Helper.class);

		clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
		// Initialized up here for use with BonePanels

		for (int i = 0; i < importedMDLBones.size(); i++) {
			final Bone b = importedMDLBones.get(i);
			final BonePanel bonePanel = new BonePanel(b, existingBones, boneShellRenderer, this);
			// boneTabs.addTab(b.getClass().getName() + " \"" + b.getName() +
			// "\"", cyanIcon, bonePanel, "Controls import settings for this
			// bone.");;
			bonePanelCards.add(bonePanel, i + "");// (bonePanel.title.getText()));
			bonePanels.addElement(bonePanel);
			boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < importedMDLHelpers.size(); i++) {
			final Bone b = importedMDLHelpers.get(i);
			final BonePanel bonePanel = new BonePanel(b, existingBones, boneShellRenderer, this);
			// boneTabs.addTab(b.getClass().getName() + " \"" + b.getName() +
			// "\"", cyanIcon, bonePanel, "Controls import settings for this
			// bone.");;
			bonePanelCards.add(bonePanel, importedMDLBones.size() + i + "");// (bonePanel.title.getText()));
			bonePanels.addElement(bonePanel);
			boneToPanel.put(b, bonePanel);
		}
		for (int i = 0; i < bonePanels.size(); i++) {
			bonePanels.get(i).initList();
		}
		multiBonePane = new MultiBonePanel(existingBones, boneShellRenderer);
		bonePanelCards.add(blankPane, "blank");
		bonePanelCards.add(multiBonePane, "multiple");
		boneTabs.setCellRenderer(bonePanelRenderer);// bonePanelRenderer);
		boneTabs.addListSelectionListener(e -> boneTabsValueChanged());
		boneTabs.setSelectedIndex(0);
		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton importAllBones = new JButton("Import All");
		importAllBones.addActionListener(e -> importAllBones(0));
		bonesPanel.add(importAllBones);

		JButton uncheckAllBones = new JButton("Leave All");
		uncheckAllBones.addActionListener(e -> importAllBones(2));
		bonesPanel.add(uncheckAllBones);

		JButton motionFromBones = new JButton("Motion From All");
		motionFromBones.addActionListener(e -> importAllBones(1));
		bonesPanel.add(motionFromBones);

		JButton uncheckUnusedBones = new JButton("Uncheck Unused");
		uncheckUnusedBones.addActionListener(e -> uncheckUnusedBones());
		bonesPanel.add(uncheckUnusedBones);

		JScrollPane boneTabsPane = new JScrollPane(boneTabs);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneTabsPane, bonePanelCards);

		final GroupLayout boneLayout = new GroupLayout(bonesPanel);
		boneLayout.setHorizontalGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(boneLayout.createSequentialGroup()
								.addComponent(importAllBones).addGap(8)
								.addComponent(motionFromBones).addGap(8)
								.addComponent(uncheckUnusedBones).addGap(8)
								.addComponent(uncheckAllBones))
						.addComponent(clearExistingBones)
						.addComponent(splitPane)
				// .addGroup(boneLayout.createSequentialGroup()
				// .addComponent(boneTabsPane)
				// .addComponent(bonePanelCards)
				// )
		);
		boneLayout.setVerticalGroup(boneLayout.createSequentialGroup()
						.addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(importAllBones)
								.addComponent(motionFromBones)
								.addComponent(uncheckUnusedBones)
								.addComponent(uncheckAllBones))
						.addComponent(clearExistingBones).addGap(8)
						.addComponent(splitPane)
				// .addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				// .addComponent(boneTabsPane)
				// .addComponent(bonePanelCards)
				// )
		);
		bonesPanel.setLayout(boneLayout);
	}

	private ParentToggleRenderer makeMatricesPanle(ModelViewManager currentModelManager, ModelViewManager importedModelManager) {
		addTab("Matrices", greenIcon, geosetAnimPanel, "Controls which bones geosets are attached to.");
//		addTab("Skin", orangeIcon, new JPanel(), "Edit SKIN chunk");

		final ParentToggleRenderer ptr = new ParentToggleRenderer(displayParents, currentModelManager,
				importedModelManager);

		displayParents.addChangeListener(this);

		allMatrOriginal.addActionListener(e -> allMatrOriginal());
		allMatrSameName.addActionListener(e -> allMatrSameName());
		return ptr;
	}

	private void MakeGeosetAnimPanel(EditableModel currentModel, EditableModel importedModel, ParentToggleRenderer ptr) {
		for (int i = 0; i < currentModel.getGeosets().size(); i++) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(currentModel, currentModel.getGeoset(i), ptr,
					this);

			geosetAnimTabs.addTab(currentModel.getName() + " " + (i + 1), greenIcon, geoPanel,
					"Click to modify animation data for Geoset " + i + " from " + currentModel.getName() + ".");
		}
		for (int i = 0; i < importedModel.getGeosets().size(); i++) {
			final BoneAttachmentPanel geoPanel = new BoneAttachmentPanel(importedModel, importedModel.getGeoset(i), ptr,
					this);

			geosetAnimTabs.addTab(importedModel.getName() + " " + (i + 1), orangeIcon, geoPanel,
					"Click to modify animation data for Geoset " + i + " from " + importedModel.getName() + ".");
		}
		geosetAnimTabs.addChangeListener(this);

		geosetAnimPanel.add(geosetAnimTabs);
		final GroupLayout gaLayout = new GroupLayout(geosetAnimPanel);
		gaLayout.setVerticalGroup(gaLayout.createSequentialGroup()
				.addComponent(displayParents)
				.addComponent(allMatrOriginal)
				.addComponent(allMatrSameName)
				.addComponent(geosetAnimTabs));
		gaLayout.setHorizontalGroup(gaLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(displayParents)
				.addComponent(allMatrOriginal)
				.addComponent(allMatrSameName)
				.addComponent(geosetAnimTabs));
		geosetAnimPanel.setLayout(gaLayout);
	}

	private void makeObjecsPanel(EditableModel importedModel) {
		JPanel objectsPanel = new JPanel();
		JSplitPane splitPane;
		addTab("Objects", objIcon, objectsPanel, "Controls which objects are imported.");
		getFutureBoneListExtended(false);

		// Build the objectTabs list of ObjectPanels
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
		int panelid = 0;
		for (int i = 0; i < importedModel.getIdObjects().size(); i++) {
			final IdObject obj = importedModel.getIdObjects().get(i);
			if ((obj.getClass() != Bone.class) && (obj.getClass() != Helper.class)) {

				final ObjectPanel objPanel = new ObjectPanel(obj, getFutureBoneListExtended(true));

				objectPanelCards.add(objPanel, panelid + "");// (objPanel.title.getText()));
				objectPanels.addElement(objPanel);
				panelid++;
				// objectTabs.addTab(obj.getClass().getName()+"
				// \""+obj.getName()+"\"",objIcon,objPanel,"Click to modify
				// object import settings.");
			}
		}
		for (int i = 0; i < importedModel.getCameras().size(); i++) {
			final Camera obj = importedModel.getCameras().get(i);

			final ObjectPanel objPanel = new ObjectPanel(obj);

			objectPanelCards.add(objPanel, panelid + "");// (objPanel.title.getText()));
			objectPanels.addElement(objPanel);
			panelid++;
		}
		multiObjectPane = new MultiObjectPanel(getFutureBoneListExtended(true));
		objectPanelCards.add(blankPane, "blank");
		objectPanelCards.add(multiObjectPane, "multiple");
		objectTabs.setCellRenderer(objectPanelRenderer);
		objectTabs.addListSelectionListener(e -> objectTabsValueChanged());
		objectTabs.setSelectedIndex(0);
		objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton importAllObjs = new JButton("Import All");
		importAllObjs.addActionListener(e -> importAllObjs(true));
		bonesPanel.add(importAllObjs);

		JButton uncheckAllObjs = new JButton("Leave All");
		uncheckAllObjs.addActionListener(e -> importAllObjs(false));
		bonesPanel.add(uncheckAllObjs);

		JScrollPane objectTabsPane = new JScrollPane(objectTabs);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectTabsPane, objectPanelCards);

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
	}

	private void makeVisPanel(EditableModel currentModel) {
		JPanel visPanel = new JPanel();
		JSplitPane splitPane;
		addTab("Visibility", orangeIcon, visPanel, "Controls the visibility of portions of the model.");

		initVisibilityList();
		visibilityList();

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		for (final VisibilityShell vs : allVisShells) {
			final VisibilityPanel vp = new VisibilityPanel(vs, new DefaultComboBoxModel<>(visSourcesOld.toArray()),
					new DefaultComboBoxModel<>(visSourcesNew.toArray()), visRenderer);

			allVisShellPanes.add(vp);

			visPanelCards.add(vp, vp.title.getText());
		}

		multiVisPanel = new MultiVisibilityPanel(new DefaultComboBoxModel<>(visSourcesOld.toArray()),
				new DefaultComboBoxModel<>(visSourcesNew.toArray()), visRenderer);
		visPanelCards.add(blankPane, "blank");
		visPanelCards.add(multiVisPanel, "multiple");
		visTabs.setModel(visComponents);
		visTabs.setCellRenderer(new VisPaneListCellRenderer(currentModel));
		visTabs.addListSelectionListener(e -> visTabsValueChanged());
		visTabs.setSelectedIndex(0);
		visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JButton allInvisButton = new JButton("All Invisible in Exotic Anims");
		allInvisButton.addActionListener(e -> allVisButton(VisibilityPanel.NOTVISIBLE));
		allInvisButton.setToolTipText(
				"Forces everything to be always invisibile in animations other than their own original animations.");
		visPanel.add(allInvisButton);

		JButton allVisButton = new JButton("All Visible in Exotic Anims");
		allVisButton.addActionListener(e -> allVisButton(VisibilityPanel.VISIBLE));
		allVisButton.setToolTipText(
				"Forces everything to be always visibile in animations other than their own original animations.");
		visPanel.add(allVisButton);

		JButton selSimButton = new JButton("Select Similar Options");
		selSimButton.addActionListener(e -> selSimButton());
		selSimButton.setToolTipText("Similar components will be selected as visibility sources in exotic animations.");
		visPanel.add(selSimButton);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, visTabsPane, visPanelCards);

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

	@Override
	public void actionPerformed(final ActionEvent e) {
	}

	private void renameAllAnims() {
		final String newTagString = JOptionPane.showInputDialog(this,
				"Choose additional naming (i.e. swim or alternate)");
		if (newTagString != null) {
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
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
					if (lastWord.contains("-") || chunkHasInt || lastWord.toLowerCase().contains("alternate")
							|| (lastWord.length() <= 0)) {
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
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
			aniPanel.importTypeBox.setSelectedIndex(2);
		}
	}

	private void importAllGeos(boolean b) {
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel geoPanel = (GeosetPanel) geosetTabs.getComponentAt(i);
			geoPanel.setSelected(b);
		}
	}

	private void uncheckAllAnims(boolean b) {
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
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
		// for( int i = 0; i < bonePanels.size(); i++ )
		// {
		// BonePanel bonePanel = bonePanels.get(i);
		// if( bonePanel.getSelectedIndex() != 1 )
		// bonePanel.setSelectedIndex(2);
		// }
		for (int i = 0; i < bonePanels.size(); i++) {
			final BonePanel bonePanel = bonePanels.get(i);
			if (bonePanel.getSelectedIndex() == 0) {
			}
		}
		for (int i = 0; i < objectPanels.size(); i++) {
			final ObjectPanel objectPanel = objectPanels.get(i);
			if (objectPanel.doImport.isSelected() && (objectPanel.parentsList != null)) {
				// System.out.println("Performing check on base:
				// "+objectPanel.object.getName());
				BoneShell shell = (BoneShell) objectPanel.parentsList.getSelectedValue();
				if ((shell != null) && (shell.bone != null)) {
					BonePanel current = getPanelOf(shell.bone);
					if (!usedBonePanels.contains(current)) {
						// System.out.println(" @adding base:
						// "+current.bone.getName());
						usedBonePanels.add(current);
					}

					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getSelectedIndex() == 1)) {
							break;
						}
						shell = (BoneShell) current.futureBonesList.getSelectedValue();
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
								// System.out.println(" @Redirected +
								// adding: "+current.bone.getName());
								// current.setSelectedIndex(0);
								usedBonePanels.add(current);
							}
						}
						k++;
						if (k > 1000) {
							JOptionPane.showMessageDialog(null,
									"Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
							break;
						}
					}
				}
			}
		}
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				System.out.println("Performing check on geoset: " + i);
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				for (int mk = 0; mk < bap.oldBoneRefs.size(); mk++) {
					final MatrixShell ms = bap.oldBoneRefs.get(mk);
					// System.out.println("Performing check on MatrixShell:
					// "+ms);
					for (final BoneShell bs : ms.newBones) {
						BoneShell shell = bs;
						BonePanel current = getPanelOf(shell.bone);
						if (!usedBonePanels.contains(current)) {
							// System.out.println(" @adding base:
							// "+current.bone.getName());
							usedBonePanels.add(current);
						}
						// System.out.println("Performing check on
						// MatrixShell's sub: "+ms+": "+bs);

						boolean good = true;
						int k = 0;
						while (good) {
							if ((current == null) || (current.getSelectedIndex() == 1)) {
								break;
							}
							shell = (BoneShell) current.futureBonesList.getSelectedValue();
							// If shell is null, then the bone has "No
							// Parent"
							// If current's selected index is not 2,
							if (shell == null)// current.getSelectedIndex()
							// != 2
							{
								good = false;
							} else {
								current = getPanelOf(shell.bone);
								if (usedBonePanels.contains(current)) {
									good = false;
								} else {
									// System.out.println(" @Redirected +
									// adding: "+current.bone.getName());
									// current.setSelectedIndex(0);
									usedBonePanels.add(current);
								}
							}
							k++;
							if (k > 1000) {
								JOptionPane.showMessageDialog(null,
										"Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
								break;
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < bonePanels.size(); i++) {
			final BonePanel bonePanel = bonePanels.get(i);
			if (bonePanel.getSelectedIndex() != 1) {
				if (usedBonePanels.contains(bonePanel)) {
					// System.out.println("Performing check on base:
					// "+bonePanel.bone.getName());
					BonePanel current = bonePanel;
					boolean good = true;
					int k = 0;
					while (good) {
						if ((current == null) || (current.getSelectedIndex() == 1)) {
							break;
						}
						final BoneShell shell = (BoneShell) current.futureBonesList.getSelectedValue();
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
								// System.out.println(" @Redirected +
								// adding: "+current.bone.getName());
								// current.setSelectedIndex(0);
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
		for (int i = 0; i < bonePanels.size(); i++) {
			final BonePanel bonePanel = bonePanels.get(i);
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
		for (int i = 0; i < bonePanels.size(); i++) {
			final BonePanel bonePanel = bonePanels.get(i);
			bonePanel.setSelectedIndex(selsctionIndex);
		}
	}

	private void importAllObjs(boolean b) {
		for (int i = 0; i < objectPanels.size(); i++) {
			final ObjectPanel objectPanel = objectPanels.get(i);
			objectPanel.doImport.setSelected(b);
		}
	}

	private void allVisButton(String visible) {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			if (vPanel.sourceShell.model == currentModel) {
				vPanel.newSourcesBox.setSelectedItem(visible);
			} else {
				vPanel.oldSourcesBox.setSelectedItem(visible);
			}
		}
	}

	private void selSimButton() {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			vPanel.selectSimilarOptions();
		}
	}

	private void okayButton() {
		doImport();
		frame.setVisible(false);
	}

	private void cancelButton() {
		final Object[] options = {"Yes", "No"};
		final int n = JOptionPane.showOptionDialog(frame, "Really cancel this import?", "Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0) {
			frame.setVisible(false);
			frame = null;
		}
	}

	private void allMatrOriginal() {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				bap.resetMatrices();
			}
		}
	}

	private void allMatrSameName() {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			if (geosetAnimTabs.isEnabledAt(i)) {
				final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
				bap.setMatricesToSimilarNames();
			}
		}
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
	}

	private void boneTabsValueChanged() {
		// boolean listEnabledNow = false;
		if (boneTabs.getSelectedValuesList().toArray().length < 1) {
			// listEnabledNow = listEnabled;
			boneCardLayout.show(bonePanelCards, "blank");
		} else if (boneTabs.getSelectedValuesList().toArray().length == 1) {
			// listEnabledNow = true;
			boneCardLayout.show(bonePanelCards, (boneTabs.getSelectedIndex()) + "");
			((BonePanel) boneTabs.getSelectedValue()).updateSelectionPicks();
		} else if (boneTabs.getSelectedValuesList().toArray().length > 1) {
			boneCardLayout.show(bonePanelCards, "multiple");
			// listEnabledNow = false;
			final Object[] selected = boneTabs.getSelectedValuesList().toArray();
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
				multiBonePane.setMultiTypes();
			} else {
				multiBonePane.setSelectedIndex(tempIndex);
			}
		}
		// if( listEnabledNow != listEnabled )
		// {
		// for( int i = 0; i < bonePanels.size(); i++ )
		// {
		// BonePanel bonePanel = bonePanels.get(i);
		// bonePanel.boneListPane.setEnabled(listEnabledNow);
		// bonePanel.boneList.setEnabled(listEnabledNow);
		// }
		// listEnabled = listEnabledNow;
		// }
	}

	/**
	 * public void addAnimPicks(Object [] whichAnims, AnimPanel source) { for( int i
	 * = 0; i < animTabs.getTabCount(); i++ ) { AnimPanel aniPanel =
	 * (AnimPanel)animTabs.getComponentAt(i); if( aniPanel != source ) { for( Object
	 * o: whichAnims ) { if( !aniPanel.existingAnims.contains(o) ) {
	 * aniPanel.listenSelection = false; aniPanel.existingAnims.addElement(o);
	 * aniPanel.listenSelection = true; //
	 * System.out.println(animTabs.getTitleAt(i)+" gained "
	 * +((Animation)o).getName()); } } aniPanel.reorderToModel(existingAnims); } } }
	 * public void removeAnimPicks(Object [] whichAnims, AnimPanel source) { for(
	 * int i = 0; i < animTabs.getTabCount(); i++ ) { AnimPanel aniPanel =
	 * (AnimPanel)animTabs.getComponentAt(i); if( aniPanel != source ) { for( Object
	 * o: whichAnims ) { aniPanel.listenSelection = false;
	 * aniPanel.existingAnims.removeElement(o); aniPanel.listenSelection = true; //
	 * System.out.println(animTabs.getTitleAt(i)+" lost "
	 * +((Animation)o).getName()); } } } } public void reorderAnimPicks(AnimPanel
	 * source) { for( int i = 0; i < animTabs.getTabCount(); i++ ) { AnimPanel
	 * aniPanel = (AnimPanel)animTabs.getComponentAt(i); if( aniPanel != source ) {
	 * aniPanel.reorderToModel(existingAnims); } } } public void addBonePicks(Object
	 * [] whichBones, BonePanel source) { for( int i = 0; i < bonePanels.size(); i++
	 * ) { BonePanel bonePanel = bonePanels.get(i); if( bonePanel != source ) { for(
	 * Object o: whichBones ) { if( !bonePanel.existingBones.contains(o) ) {
	 * bonePanel.listenSelection = false; bonePanel.existingBones.addElement(o);
	 * bonePanel.listenSelection = true; //
	 * System.out.println(animTabs.getTitleAt(i)+" gained "
	 * +((Animation)o).getName()); } } bonePanel.reorderToModel(existingBones); } }
	 * } public void removeBonePicks(Object [] whichBones, BonePanel source) { for(
	 * int i = 0; i < bonePanels.size(); i++ ) { BonePanel bonePanel =
	 * bonePanels.get(i); if( bonePanel != source ) { for( Object o: whichBones ) {
	 * bonePanel.listenSelection = false; bonePanel.existingBones.removeElement(o);
	 * bonePanel.listenSelection = true; //
	 * System.out.println(animTabs.getTitleAt(i)+" lost "
	 * +((Animation)o).getName()); } } } } public void reorderBonePicks(BonePanel
	 * source) { for( int i = 0; i < bonePanels.size(); i++ ) { BonePanel bonePanel
	 * = bonePanels.get(i); if( bonePanel != source ) {
	 * bonePanel.reorderToModel(existingBones); } } }
	 **/
	public void informGeosetVisibility(final Geoset g, final boolean flag) {
		for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
			final BoneAttachmentPanel geoPanel = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
			if (geoPanel.geoset == g) {
				geosetAnimTabs.setEnabledAt(i, flag);
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
//		BonePanel out = null;
//		for (int i = 0; i < bonePanels.size() && out == null; i++) {
//			final BonePanel bp = bonePanels.get(i);
//			if (bp.bone == b) {
//				out = bp;
//			}
//		}
		return boneToPanel.get(b);
	}

	public DefaultListModel<BoneShell> getFutureBoneList() {
		if (oldBones == null) {
			oldBones = new ArrayList<>();
			newBones = new ArrayList<>();
			final List<Bone> oldBonesRefs = currentModel.sortedIdObjects(Bone.class);
			for (final Bone b : oldBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = currentModel.getName();
				oldBones.add(bs);
			}
			final List<Bone> newBonesRefs = importedModel.sortedIdObjects(Bone.class);
			for (final Bone b : newBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = importedModel.getName();
				bs.panel = getPanelOf(b);
				newBones.add(bs);
			}
		}
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : oldBones) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			}
		} else {
			for (final BoneShell b : oldBones) {
				if (futureBoneList.contains(b)) {
					futureBoneList.removeElement(b);
				}
			}
		}
		for (final BoneShell b : newBones) {
			if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
				if (!futureBoneList.contains(b)) {
					futureBoneList.addElement(b);
				}
			} else {
				if (futureBoneList.contains(b)) {
					futureBoneList.removeElement(b);
				}
			}
		}
		return futureBoneList;
	}

	DefaultListModel<BoneShell> futureBoneListEx = new DefaultListModel<>();
	List<DefaultListModel<BoneShell>> futureBoneListExFixableItems = new ArrayList<>();
	ArrayList<BoneShell> oldHelpers;
	ArrayList<BoneShell> newHelpers;
	private final Set<BoneShell> futureBoneListExQuickLookupSet = new HashSet<>();

	public DefaultListModel<BoneShell> getFutureBoneListExtended(final boolean newSnapshot) {
		// if( futureBoneList.size() > 0 )
		// {
		// if( oldHelpers == null )
		// {
		// for( int i = 0; i < futureBoneList.size(); i++ )
		// {
		// futureBoneListEx.addElement(futureBoneList.get(i));
		// }
		// oldHelpers = new ArrayList<BoneShell>();
		// newHelpers = new ArrayList<BoneShell>();
		// ArrayList<Bone> oldBonesRefs =
		// currentModel.sortedIdObjects(Helper.class);
		// for( Bone b: oldBonesRefs )
		// {
		// BoneShell bs = new BoneShell(b);
		// bs.modelName = currentModel.getName();
		// oldBones.add(bs);
		// }
		// ArrayList<Bone> newBonesRefs =
		// importedModel.sortedIdObjects(Helper.class);
		// for( Bone b: newBonesRefs )
		// {
		// BoneShell bs = new BoneShell(b);
		// bs.modelName = importedModel.getName();
		// bs.panel = getPanelOf(b);
		// newBones.add(bs);
		// }
		// }
		// for( int i = 0; i < futureBoneListEx.size(); i++ )
		// {
		// BoneShell bs = futureBoneListEx.get(i);
		// if( (oldBones.contains(bs) || newBones.contains(bs)) &&
		// !futureBoneList.contains(bs) )
		// {
		// futureBoneListEx.removeElement(bs);
		// }
		// }
		// for( int i = 0; i < futureBoneList.size(); i++ )
		// {
		// BoneShell bs = futureBoneList.get(i);
		// if( !futureBoneListEx.contains(bs) )
		// futureBoneListEx.addElement(bs);
		// }
		// if( !clearExistingBones.isSelected() )
		// {
		// for( BoneShell b: oldHelpers )
		// {
		// if( !futureBoneListEx.contains(b) )
		// {
		// futureBoneListEx.addElement(b);
		// }
		// }
		// }
		// else
		// {
		// for( BoneShell b: oldHelpers )
		// {
		// if( futureBoneListEx.contains(b) )
		// {
		// futureBoneListEx.removeElement(b);
		// }
		// }
		// }
		// for( BoneShell b: newHelpers )
		// {
		// if( b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT )
		// {
		// if( !futureBoneListEx.contains(b) )
		// {
		// futureBoneListEx.addElement(b);
		// }
		// }
		// else
		// {
		// if( futureBoneListEx.contains(b) )
		// {
		// futureBoneListEx.removeElement(b);
		// }
		// }
		// }
		// }
		long totalAddTime = 0;
		long addCount = 0;
		long totalRemoveTime = 0;
		long removeCount = 0;
		if (oldHelpers == null) {
			oldHelpers = new ArrayList<>();
			newHelpers = new ArrayList<>();
			List<? extends Bone> oldHelpersRefs = currentModel.sortedIdObjects(Bone.class);
			for (final Bone b : oldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = currentModel.getName();
				bs.showClass = true;
				oldHelpers.add(bs);
			}
			oldHelpersRefs = currentModel.sortedIdObjects(Helper.class);
			for (final Bone b : oldHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = currentModel.getName();
				bs.showClass = true;
				oldHelpers.add(bs);
			}
			List<? extends Bone> newHelpersRefs = importedModel.sortedIdObjects(Bone.class);
			for (final Bone b : newHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = importedModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				newHelpers.add(bs);
			}
			newHelpersRefs = importedModel.sortedIdObjects(Helper.class);
			for (final Bone b : newHelpersRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = importedModel.getName();
				bs.showClass = true;
				bs.panel = getPanelOf(b);
				newHelpers.add(bs);
			}
		}
		if (!clearExistingBones.isSelected()) {
			for (final BoneShell b : oldHelpers) {
				if (!futureBoneListExQuickLookupSet.contains(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.addElement(b);
					final long endTime = System.nanoTime();
					totalAddTime += (endTime - startTime);
					addCount++;
					futureBoneListExQuickLookupSet.add(b);
				}
			}
		} else {
			for (final BoneShell b : oldHelpers) {
				if (futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += (endTime - startTime);
					removeCount++;
				}
			}
		}
		for (final BoneShell b : newHelpers) {
			b.panel = getPanelOf(b.bone);
			if (b.panel != null) {
				if (b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT) {
					if (!futureBoneListExQuickLookupSet.contains(b)) {
						final long startTime = System.nanoTime();
						futureBoneListEx.addElement(b);
						final long endTime = System.nanoTime();
						totalAddTime += (endTime - startTime);
						addCount++;
						futureBoneListExQuickLookupSet.add(b);
					}
				} else {
					if (futureBoneListExQuickLookupSet.remove(b)) {
						final long startTime = System.nanoTime();
						futureBoneListEx.removeElement(b);
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

		final DefaultListModel<BoneShell> listModelToReturn;
		if (newSnapshot || futureBoneListExFixableItems.isEmpty()) {
			final DefaultListModel<BoneShell> futureBoneListReplica = new DefaultListModel<>();
			futureBoneListExFixableItems.add(futureBoneListReplica);
			listModelToReturn = futureBoneListReplica;
		} else {
			listModelToReturn = futureBoneListExFixableItems.get(0);
		}
		// We CANT call clear, we have to preserve
		// the parent list
		for (final DefaultListModel<BoneShell> model : futureBoneListExFixableItems) {
			// clean things that should not be there
			for (int i = model.getSize() - 1; i >= 0; i--) {
				final BoneShell previousElement = model.get(i);
				if (!futureBoneListExQuickLookupSet.contains(previousElement)) {
					model.remove(i);
				}
			}
			// add back things who should be there
			for (int i = 0; i < futureBoneListEx.getSize(); i++) {
				final BoneShell elementAt = futureBoneListEx.getElementAt(i);
				if (!model.contains(elementAt)) {
					model.addElement(elementAt);
				}
			}
		}
//		for(DefaultListModel<BoneShell> model: futureBoneListExFixableItems) {
//			model.clear();
//			for(int i = 0; i < futureBoneListEx.getSize(); i++) {
//				model.addElement(futureBoneListEx.getElementAt(i));
//			}
//		}
		return listModelToReturn;
//		return futureBoneListEx;
	}

	ArrayList<VisibilityShell> allVisShells;
	ArrayList<Object> visSourcesOld;
	ArrayList<Object> visSourcesNew;

	private void objectTabsValueChanged() {
		if (objectTabs.getSelectedValuesList().toArray().length < 1) {
			objectCardLayout.show(objectPanelCards, "blank");
		} else if (objectTabs.getSelectedValuesList().toArray().length == 1) {
			getFutureBoneListExtended(false);
			objectCardLayout.show(objectPanelCards, (objectTabs.getSelectedIndex()) + "");// .title.getText()
		} else if (objectTabs.getSelectedValuesList().toArray().length > 1) {
			objectCardLayout.show(objectPanelCards, "multiple");
			final Object[] selected = objectTabs.getSelectedValuesList().toArray();
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
				multiObjectPane.doImport.setSelected(selectedt);
			}
		}
	}

	private void visTabsValueChanged() {
		if (visTabs.getSelectedValuesList().toArray().length < 1) {
			visCardLayout.show(visPanelCards, "blank");
		} else if (visTabs.getSelectedValuesList().toArray().length == 1) {
			visCardLayout.show(visPanelCards, visTabs.getSelectedValue().title.getText());
		} else if (visTabs.getSelectedValuesList().toArray().length > 1) {
			visCardLayout.show(visPanelCards, "multiple");
			final Object[] selected = visTabs.getSelectedValuesList().toArray();

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
				multiVisPanel.favorOld.setSelected(selectedt);
			}
			if (difBoxOld) {
				multiVisPanel.setMultipleOld();
			} else {
				multiVisPanel.oldSourcesBox.setSelectedIndex(tempIndexOld);
			}
			if (difBoxNew) {
				multiVisPanel.setMultipleNew();
			} else {
				multiVisPanel.newSourcesBox.setSelectedIndex(tempIndexNew);
			}
		}
	}
	private final BoneShellListCellRenderer boneShellRenderer;

	public VisibilityShell shellFromObject(final Object o) {
		for (final VisibilityShell v : allVisShells) {
			if (v.source == o) {
				return v;
			}
		}
		return null;
	}

	public VisibilityPanel visPaneFromObject(final Object o) {
		for (final VisibilityPanel vp : allVisShellPanes) {
			if (vp.sourceShell.source == o) {
				return vp;
			}
		}
		return null;
	}

	public void initVisibilityList() {
		visSourcesOld = new ArrayList<>();
		visSourcesNew = new ArrayList<>();
		allVisShells = new ArrayList<>();
		EditableModel model = currentModel;
		final List tempList = new ArrayList();
		for (final Material mat : model.getMaterials()) {
			for (final Layer lay : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(lay, model);
				if (!tempList.contains(lay)) {
					tempList.add(lay);
					allVisShells.add(vs);
				}
			}
		}
		for (final Geoset ga : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(ga, model);
			if (!tempList.contains(ga)) {
				tempList.add(ga);
				allVisShells.add(vs);
			}
		}
		for (final Object l : model.sortedIdObjects(Light.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) l, model);
			if (!tempList.contains(l)) {
				tempList.add(l);
				allVisShells.add(vs);
			}
		}
		for (final Object a : model.sortedIdObjects(Attachment.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) a, model);
			if (!tempList.contains(a)) {
				tempList.add(a);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter2.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(RibbonEmitter.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitterPopcorn.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		model = importedModel;
		for (final Material mat : model.getMaterials()) {
			for (final Layer lay : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(lay, model);
				if (!tempList.contains(lay)) {
					tempList.add(lay);
					allVisShells.add(vs);
				}
			}
		}
		for (final Geoset ga : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(ga, model);
			if (!tempList.contains(ga)) {
				tempList.add(ga);
				allVisShells.add(vs);
			}
		}
		for (final Object l : model.sortedIdObjects(Light.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) l, model);
			if (!tempList.contains(l)) {
				tempList.add(l);
				allVisShells.add(vs);
			}
		}
		for (final Object a : model.sortedIdObjects(Attachment.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) a, model);
			if (!tempList.contains(a)) {
				tempList.add(a);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter2.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(RibbonEmitter.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitterPopcorn.class)) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}

		System.out.println("allVisShells:");
		for (final VisibilityShell vs : allVisShells) {
			System.out.println(vs.source.getName());
		}

		System.out.println("new/old:");
		for (final Object o : currentModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				visSourcesOld.add(shellFromObject(o));
				System.out.println(shellFromObject(o).source.getName());
			} else {
				visSourcesOld.add(shellFromObject(((GeosetAnim) o).getGeoset()));
				System.out.println(shellFromObject(((GeosetAnim) o).getGeoset()).source.getName());
			}
		}
		visSourcesOld.add(VisibilityPanel.NOTVISIBLE);
		visSourcesOld.add(VisibilityPanel.VISIBLE);
		for (final Object o : importedModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				visSourcesNew.add(shellFromObject(o));
			} else {
				visSourcesNew.add(shellFromObject(((GeosetAnim) o).getGeoset()));
			}
		}
		visSourcesNew.add(VisibilityPanel.NOTVISIBLE);
		visSourcesNew.add(VisibilityPanel.VISIBLE);
		visComponents = new DefaultListModel<>();
	}

	public DefaultListModel<VisibilityPanel> visibilityList() {
		final Object selection = visTabs.getSelectedValue();
		visComponents.clear();
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			for (final Layer l : gp.getSelectedMaterial().getLayers()) {
				final VisibilityPanel vs = visPaneFromObject(l);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			if (gp.doImport.isSelected()) {
				final Geoset ga = gp.geoset;
				final VisibilityPanel vs = visPaneFromObject(ga);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		// The current's
		final EditableModel model = currentModel;
		for (final Object l : model.sortedIdObjects(Light.class)) {
			final VisibilityPanel vs = visPaneFromObject(l);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object a : model.sortedIdObjects(Attachment.class)) {
			final VisibilityPanel vs = visPaneFromObject(a);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter.class)) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter2.class)) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(RibbonEmitter.class)) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitterPopcorn.class)) {
			final VisibilityPanel vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && (vs != null)) {
				visComponents.addElement(vs);
			}
		}

		for (int i = 0; i < objectPanels.size(); i++) {
			final ObjectPanel op = objectPanels.get(i);
			if (op.doImport.isSelected() && (op.object != null))
				// we don't touch camera "object" panels (which aren't idobjects)
			{
				final VisibilityPanel vs = visPaneFromObject(op.object);
				if (!visComponents.contains(vs) && (vs != null)) {
					visComponents.addElement(vs);
				}
			}
		}
		visTabs.setSelectedValue(selection, true);
		return visComponents;
	}

	public void setSelectedItem(final String what) {
		final Object[] selected = boneTabs.getSelectedValuesList().toArray();
		for (Object o : selected) {
			final BonePanel temp = (BonePanel) o;
			temp.setSelectedValue(what);
		}
	}

	/**
	 * The method run when the user pushes the "Set Parent for All" button in the
	 * MultiBone panel.
	 */
	public void setParentMultiBones() {
		final JList<BoneShell> list = new JList<>(getFutureBoneListExtended(true));
		list.setCellRenderer(boneShellRenderer);
		final int x = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Set Parent for All Selected Bones",
				JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			final Object[] selected = boneTabs.getSelectedValuesList().toArray();
			for (Object o : selected) {
				final BonePanel temp = (BonePanel) o;
				temp.setParent(list.getSelectedValue());
			}
		}
	}

	public void setObjGroupSelected(final boolean flag) {
		final Object[] selected = objectTabs.getSelectedValuesList().toArray();
		for (Object o : selected) {
			final ObjectPanel temp = (ObjectPanel) o;
			temp.doImport.setSelected(flag);
		}
	}

	public void setVisGroupSelected(final boolean flag) {
		final Object[] selected = visTabs.getSelectedValuesList().toArray();
		for (Object o : selected) {
			final VisibilityPanel temp = (VisibilityPanel) o;
			temp.favorOld.setSelected(flag);
		}
	}

	public void setVisGroupItemOld(final Object o) {
		final Object[] selected = visTabs.getSelectedValuesList().toArray();
		for (Object value : selected) {
			final VisibilityPanel temp = (VisibilityPanel) value;
			temp.oldSourcesBox.setSelectedItem(o);
		}
	}

	public void setVisGroupItemNew(final Object o) {
		final Object[] selected = visTabs.getSelectedValuesList().toArray();
		for (Object value : selected) {
			final VisibilityPanel temp = (VisibilityPanel) value;
			temp.newSourcesBox.setSelectedItem(o);
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		((AnimPanel) animTabs.getSelectedComponent()).updateSelectionPicks();
		getFutureBoneList();
		getFutureBoneListExtended(false);
		visibilityList();
		// ((BoneAttachmentPane)geosetAnimTabs.getSelectedComponent()).refreshLists();
		repaint();
	}

	public void doImport() {
		importStarted = true;
		try {

			// AFTER WRITING THREE THOUSAND LINES OF INTERFACE,
			// FINALLLLLLLLLLLLYYYYYYYYY
			// The engine for actually performing the model to model import.

			if (currentModel == importedModel) {
				JOptionPane.showMessageDialog(null, "The program has confused itself.");
			}

			// ArrayList<Geoset> newGeosets = new ArrayList<Geoset>();//Just for 3d update

			for (int i = 0; i < geosetTabs.getTabCount(); i++) {
				final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
				gp.geoset.setMaterial(gp.getSelectedMaterial());
				if (gp.doImport.isSelected() && (gp.model == importedModel)) {
					currentModel.add(gp.geoset);
					// newGeosets.add(gp.geoset);//Just for 3d update
					if (gp.geoset.getGeosetAnim() != null) {
						currentModel.add(gp.geoset.getGeosetAnim());
					}
				}
			}
			final List<Animation> oldAnims = new ArrayList<>(currentModel.getAnims());
			final List<Animation> newAnims = new ArrayList<>();
			final java.util.List<AnimFlag> curFlags = currentModel.getAllAnimFlags();
			final java.util.List<AnimFlag> impFlags = importedModel.getAllAnimFlags();
			final List<EventObject> curEventObjs = currentModel.sortedIdObjects(EventObject.class);
			final List<EventObject> impEventObjs = importedModel.sortedIdObjects(EventObject.class);
			// note to self: remember to scale event objects with time
			final List<AnimFlag> newImpFlags = new ArrayList<>();
			for (final AnimFlag af : impFlags) {
				if (!af.hasGlobalSeq()) {
					newImpFlags.add(AnimFlag.buildEmptyFrom(af));
				} else {
					newImpFlags.add(new AnimFlag(af));
				}
			}
			final List<EventObject> newImpEventObjs = new ArrayList<>();
			for (final Object e : impEventObjs) {
				newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
			}
			final boolean clearAnims = clearExistingAnims.isSelected();
			if (clearAnims) {
				for (final Animation anim : currentModel.getAnims()) {
					anim.clearData(curFlags, curEventObjs);
				}
				currentModel.getAnims().clear();
			}
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				if (aniPanel.doImport.isSelected()) {
					final int type = aniPanel.importTypeBox.getSelectedIndex();
					final int animTrackEnd = currentModel.animTrackEnd();
					if (aniPanel.inReverse.isSelected()) {
						// reverse the animation
						aniPanel.anim.reverse(impFlags, impEventObjs);
					}
					switch (type) {
						case 0:
							aniPanel.anim.copyToInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300, impFlags, impEventObjs, newImpFlags, newImpEventObjs);
							aniPanel.anim.setInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300);
							currentModel.add(aniPanel.anim);
							newAnims.add(aniPanel.anim);
							break;
						case 1:
							aniPanel.anim.copyToInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300, impFlags, impEventObjs, newImpFlags, newImpEventObjs);
							aniPanel.anim.setInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300);
							aniPanel.anim.setName(aniPanel.newNameEntry.getText());
							currentModel.add(aniPanel.anim);
							newAnims.add(aniPanel.anim);
							break;
						case 2:
							// List<AnimShell> targets = aniPane.animList.getSelectedValuesList();
							// aniPanel.anim.setInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300, impFlags,
							// impEventObjs, newImpFlags, newImpEventObjs);
							// handled by animShells
							break;
						case 3:
							importedModel.buildGlobSeqFrom(aniPanel.anim, impFlags);
							break;
					}
				}
			}
			final boolean clearBones = clearExistingBones.isSelected();
			if (!clearAnims) {
				for (int i = 0; i < existingAnims.size(); i++) {
					final Object o = existingAnims.get(i);
					final AnimShell animShell = (AnimShell) o;
					if (animShell.importAnim != null) {
						animShell.importAnim.copyToInterval(animShell.anim.getStart(), animShell.anim.getEnd(),
								impFlags, impEventObjs, newImpFlags, newImpEventObjs);
						final Animation tempAnim = new Animation("temp", animShell.anim.getStart(),
								animShell.anim.getEnd());
						newAnims.add(tempAnim);
						if (!clearBones) {
							for (int p = 0; p < existingBones.size(); p++) {
								final BoneShell bs = (BoneShell) existingBones.get(p);
								if (bs.importBone != null) {
									if (getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
										// JOptionPane.showMessageDialog(null,"Attempting to clear animation for
										// "+bs.bone.getName()+" values "+animShell.anim.getStart()+", "+animShell.anim.getEnd());
										System.out.println(
												"Attempting to clear animation for " + bs.bone.getName() + " values "
														+ animShell.anim.getStart() + ", " + animShell.anim.getEnd());
										bs.bone.clearAnimation(animShell.anim);
									}
								}
							}
						}
					}
				}
			}
			// Now, rebuild the old animflags with the new
			for (final AnimFlag af : impFlags) {
				af.setValuesTo(newImpFlags.get(impFlags.indexOf(af)));
			}
			for (final Object e : impEventObjs) {
				((EventObject) e).setValuesTo(newImpEventObjs.get(impEventObjs.indexOf(e)));
			}

			if (clearBones) {
				for (final IdObject o : currentModel.sortedIdObjects(Bone.class)) {
					currentModel.remove(o);
				}
				for (final IdObject o : currentModel.sortedIdObjects(Helper.class)) {
					currentModel.remove(o);
				}
			}
			final List<IdObject> objectsAdded = new ArrayList<>();
			final List<Camera> camerasAdded = new ArrayList<>();
			for (int i = 0; i < bonePanels.size(); i++) {
				final BonePanel bonePanel = bonePanels.get(i);
				final Bone b = bonePanel.bone;
				final int type = bonePanel.importTypeBox.getSelectedIndex();
				// b.setName(b.getName()+" "+importedModel.getName());
				// bonePanel.boneList.getSelectedValuesList();
				// we will go through all bone shells for this
				// Fix cross-model referencing issue (force clean parent node's list of
				// children)
				switch (type) {
					case 0 -> {
						currentModel.add(b);
						objectsAdded.add(b);
						final BoneShell mbs = (BoneShell) bonePanel.futureBonesList.getSelectedValue();
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
				for (int i = 0; i < existingBones.size(); i++) {
					final BoneShell bs = (BoneShell) existingBones.get(i);
					if (bs.importBone != null) {
						if (getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
							bs.bone.copyMotionFrom(bs.importBone);
						}
					}
				}
			}
			// DefaultListModel<BoneShell> bones = getFutureBoneList();
			boolean shownEmpty = false;
			Bone dummyBone = null;
			for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
				if (geosetAnimTabs.isEnabledAt(i)) {
					final BoneAttachmentPanel bap = (BoneAttachmentPanel) geosetAnimTabs.getComponentAt(i);
					for (int l = 0; l < bap.oldBoneRefs.size(); l++) {
						final MatrixShell ms = bap.oldBoneRefs.get(l);
						if (ms.newBones.size() > 0) {
							ms.matrix.getBones().clear();
						} else {
							// JOptionPane.showMessageDialog(null,"Warning: You left some matrices empty. Zero values will be inserted.");
							ms.matrix.getBones().clear();
							// ms.matrix.bones.add(bap.model.getBone(0));
						}
						for (final BoneShell bs : ms.newBones) {
							if (currentModel.contains(bs.bone)) {
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
								if (!currentModel.contains(dummyBone)) {
									currentModel.add(dummyBone);
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
						// ms.matrix.bones = ms.newBones;
					}
				}
			}
			currentModel.updateObjectIds();
			for (final Geoset g : currentModel.getGeosets()) {
				g.applyMatricesToVertices(currentModel);
			}

			// Objects!
			for (int i = 0; i < objectPanels.size(); i++) {
				final ObjectPanel objectPanel = objectPanels.get(i);
				if (objectPanel.doImport.isSelected()) {
					if (objectPanel.object != null) {
						final BoneShell mbs = (BoneShell) objectPanel.parentsList.getSelectedValue();
						if (mbs != null) {
							objectPanel.object.setParent(mbs.bone);
						} else {
							objectPanel.object.setParent(null);
						}
						// objectPanel.object.setName(importedModel.getName()+" "+objectPanel.object.getName());
						// later make a name field?
						currentModel.add(objectPanel.object);
						objectsAdded.add(objectPanel.object);
					} else if (objectPanel.camera != null) {
						// objectPanel.camera.setName(importedModel.getName()+" "+objectPanel.camera.getName());
						currentModel.add(objectPanel.camera);
						camerasAdded.add(objectPanel.camera);
					}
				} else {
					if (objectPanel.object != null) {
						objectPanel.object.setParent(null);
						// Fix cross-model referencing issue (force clean parent node's list of children)
					}
				}
			}

			final List<AnimFlag> finalVisFlags = new ArrayList<>();
			for (int i = 0; i < visComponents.size(); i++) {
				final VisibilityPanel vPanel = visComponents.get(i);
				final VisibilitySource temp = ((VisibilitySource) vPanel.sourceShell.source);
				final AnimFlag visFlag = temp.getVisibilityFlag();// might be
				// null
				final AnimFlag newVisFlag;
				boolean tans = false;
				if (visFlag != null) {
					newVisFlag = AnimFlag.buildEmptyFrom(visFlag);
					tans = visFlag.tans();
				} else {
					newVisFlag = new AnimFlag(temp.visFlagName());
				}
				// newVisFlag = new AnimFlag(temp.visFlagName());
				final Object oldSource = vPanel.oldSourcesBox.getSelectedItem();
				AnimFlag flagOld = null;
				boolean test = false;
				if (oldSource.getClass() == String.class) {
					if (oldSource == VisibilityPanel.VISIBLE) {
						// empty for visible
					} else if (oldSource == VisibilityPanel.NOTVISIBLE) {
						flagOld = new AnimFlag("temp");
						for (final Animation a : oldAnims) {
							test = true;
							if (tans) {
								flagOld.addEntry(a.getStart(), (float) 0, (float) 0,
										(float) 0);
							} else {
								flagOld.addEntry(a.getStart(), (float) 0);
							}
						}
					}
				} else {
					flagOld = (((VisibilitySource) ((VisibilityShell) oldSource).source).getVisibilityFlag());
				}
				final Object newSource = vPanel.newSourcesBox.getSelectedItem();
				AnimFlag flagNew = null;
				if (newSource.getClass() == String.class) {
					if (newSource == VisibilityPanel.VISIBLE) {
						// empty for visible
					} else if (newSource == VisibilityPanel.NOTVISIBLE) {
						flagNew = new AnimFlag("temp");
						for (final Animation a : newAnims) {
							if (tans) {
								flagNew.addEntry(a.getStart(), (float) 0, (float) 0,
										(float) 0);
							} else {
								flagNew.addEntry(a.getStart(), (float) 0);
							}
							// flagNew.times.add(Integer.valueOf(a.getStart()));
							// flagNew.values.add(Float.valueOf(0));
							// if( tans )
							// {
							// flagNew.inTans.add(Float.valueOf(0));
							// flagNew.outTans.add(Float.valueOf(0));
							// }
						}
					}
				} else {
					flagNew = (((VisibilitySource) ((VisibilityShell) newSource).source).getVisibilityFlag());
				}
				if ((vPanel.favorOld.isSelected() && (vPanel.sourceShell.model == currentModel) && !clearAnims)
						|| (!vPanel.favorOld.isSelected() && (vPanel.sourceShell.model == importedModel))) {
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
			for (int i = 0; i < visComponents.size(); i++) {
				final VisibilityPanel vPanel = visComponents.get(i);
				final VisibilitySource temp = ((VisibilitySource) vPanel.sourceShell.source);
				final AnimFlag visFlag = finalVisFlags.get(i);// might be null
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
			// for( int i = 0; i < geosetTabs.getTabCount(); i++ )
			// {
			// GeosetPanel gp = (GeosetPanel)geosetTabs.getComponentAt(i);
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
			for (int i = 0; i < geosetTabs.getTabCount(); i++) {
				final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
				if (gp.doImport.isSelected() && (gp.model == importedModel)) {
					geosetsAdded.add(gp.geoset);
				}
			}
			if (callback != null) {
				callback.geosetsAdded(geosetsAdded);
				callback.nodesAdded(objectsAdded);
				callback.camerasAdded(camerasAdded);
			}
			for (final AnimFlag flag : currentModel.getAllAnimFlags()) {
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
	public void animTransfer(final boolean singleAnimation, final Animation pickedAnim, final Animation visFromAnim,
			final boolean show) {
		importAllGeos(false);
		importAllBones(1);
		clearExistingAnims.doClick();
		importAllObjs(false);
		visibilityList();
		selSimButton();

		if (singleAnimation) {
			// JOptionPane.showMessageDialog(null,"single trans");
			uncheckAllAnims(false);
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				if (aniPanel.anim.getName().equals(pickedAnim.getName())) {
					aniPanel.doImport.setSelected(true);
				}
			}
			clearExistingAnims.doClick();// turn it back off
		}

		VisibilityShell corpseShell = null;
		// Try assuming it's a unit with a corpse; they'll tend to be that way

		// Iterate through new visibility sources, find a geoset with gutz material
		for (int i = 0; (i < visSourcesNew.size()) && (corpseShell == null); i++) {
			if (visSourcesNew.get(i) instanceof VisibilityShell) {
				final VisibilityShell vs = (VisibilityShell) visSourcesNew.get(i);
				if (vs.source instanceof Geoset) {
					final Geoset g = (Geoset) vs.source;
					if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath()
							.toLowerCase().equals("textures\\gutz.blp")) {
						corpseShell = vs;
						// JOptionPane.showMessageDialog(null,"Anim file corpse
						// found at index "+i);
					}
				}
			}
		}
		// JOptionPane.showMessageDialog(null,"AutoTrans corpse visibility:");
		// JOptionPane.showMessageDialog(null,(corpseShell != null) + ",
		// "+visComponents.size());
		if (corpseShell != null) {
			for (int i = 0; i < visComponents.getSize(); i++) {
				final VisibilityPanel vp = visComponents.get(i);
				// JOptionPane.showMessageDialog(null,"Visibility component: "+i
				// +", "+vp.sourceShell.source.getName());
				if (vp.sourceShell.source instanceof Geoset) {
					// System.err.println("Geoset at index: "+i);
					final Geoset g = (Geoset) vp.sourceShell.source;
					// JOptionPane.showMessageDialog(null,"Source file geo tex:
					// "+g.material.firstLayer().firstTexture().getPath());
					if ((g.getGeosetAnim() != null) && g.getMaterial().firstLayer().firstTexture().getPath()
							.toLowerCase().equals("textures\\gutz.blp")) {
						vp.newSourcesBox.setSelectedItem(corpseShell);
						// JOptionPane.showMessageDialog(null,"Source file
						// corpse set at index "+i);
					}
				}
			}
		}

		if (!show) {
			okayButton();
		}

		// visComponents.get(0).newSourcesBox.setSelectedItem
	}

	// *********************Simple Import Functions****************
	public void animTransferPartTwo(final boolean singleAnimation, final Animation pickedAnim,
			final Animation visFromAnim, final boolean show) {
		// This should be an import from self
		importAllGeos(false);
		uncheckAllAnims(false);
		importAllBones(2);
		importAllObjs(false);
		visibilityList();
		selSimButton();

		if (singleAnimation) {
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				if (aniPanel.anim.getName().equals(visFromAnim.getName())) {
					aniPanel.doImport.doClick();
					aniPanel.importTypeBox.setSelectedItem(AnimPanel.TIMESCALE);

					for (int d = 0; d < existingAnims.getSize(); d++) {
						final AnimShell shell = (AnimShell) existingAnims.get(d);
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
		for (int i = 0; i < visComponents.getSize(); i++) {
			final VisibilityPanel vp = visComponents.get(i);
			// JOptionPane.showMessageDialog(null,"Visibility component: "+i +",
			// "+vp.sourceShell.source.getName());
			vp.favorOld.doClick();
		}

		if (!show) {
			okayButton();
		}

		// visComponents.get(0).newSourcesBox.setSelectedItem
	}
}

