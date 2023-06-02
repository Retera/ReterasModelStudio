package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.SmartMappingChooserPanel.Pairing;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Matrix;
import com.hiveworkshop.wc3.mdl.Named;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.VisibilitySource;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

/**
 * The panel to handle the import function.
 *
 * Eric Theller 6/11/2012
 */
public class ImportPanel extends JTabbedPane implements ActionListener, ListSelectionListener, ChangeListener {
	public static final ImageIcon animIcon = RMSIcons.animIcon;
	public static final ImageIcon boneIcon = RMSIcons.boneIcon;
	public static final ImageIcon geoIcon = RMSIcons.geoIcon;
	public static final ImageIcon objIcon = RMSIcons.objIcon;
	public static final ImageIcon greenIcon = RMSIcons.greenIcon;
	public static final ImageIcon redIcon = RMSIcons.redIcon;
	public static final ImageIcon orangeIcon = RMSIcons.orangeIcon;
	public static final ImageIcon cyanIcon = RMSIcons.cyanIcon;
	public static final ImageIcon redXIcon = RMSIcons.redXIcon;
	public static final ImageIcon greenArrowIcon = RMSIcons.greenArrowIcon;
	public static final ImageIcon moveUpIcon = RMSIcons.moveUpIcon;
	public static final ImageIcon moveDownIcon = RMSIcons.moveDownIcon;

	JFrame frame;

	EditableModel currentModel;
	EditableModel importedModel;

	// Geosets
	JPanel geosetsPanel = new JPanel();
	JButton importAllGeos, uncheckAllGeos;
	JTabbedPane geosetTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

	// Animation
	JPanel animPanel = new JPanel();
	JButton importAllAnims, timescaleAllAnims, uncheckAllAnims, renameAllAnims;
	JCheckBox clearExistingAnims;
	JTabbedPane animTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
	DefaultListModel existingAnims;

	// Bones
	JPanel bonesPanel = new JPanel();
	JButton importAllBones, uncheckAllBones, motionFromBones, uncheckUnusedBones;
	JCheckBox clearExistingBones;
	// JTabbedPane boneTabs = new
	// JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
	DefaultListModel<BonePanel> bonePanels = new DefaultListModel<>();
	private final Map<Bone, BonePanel> boneToPanel = new HashMap<>();
	JList boneTabs = new JList(bonePanels);
	JScrollPane boneTabsPane = new JScrollPane(boneTabs);
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
	ArrayList<BoneShell> oldBones;
	ArrayList<BoneShell> newBones;

	JCheckBox displayParents = new JCheckBox("Display parent names");
	JButton allMatrOriginal = new JButton("Reset all Matrices"),
			allMatrSameName = new JButton("Set all to available, original names"),
			applySmartMapping = new JButton("Apply Smart Mapping");

	// Objects
	JPanel objectsPanel = new JPanel();
	// JTabbedPane objectTabs = new
	// JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
	DefaultListModel<ObjectPanel> objectPanels = new DefaultListModel<>();
	JList objectTabs = new JList(objectPanels);
	JScrollPane objectTabsPane = new JScrollPane(objectTabs);
	CardLayout objectCardLayout = new CardLayout();
	JPanel objectPanelCards = new JPanel(objectCardLayout);
	MultiObjectPanel multiObjectPane;

	JButton importAllObjs, uncheckAllObjs;

	// Visibility
	JPanel visPanel = new JPanel();
	// DefaultListModel<VisibilityPane> visPanels = new
	// DefaultListModel<VisibilityPane>();
	JList visTabs = new JList();
	JScrollPane visTabsPane = new JScrollPane(visTabs);
	CardLayout visCardLayout = new CardLayout();
	JPanel visPanelCards = new JPanel(visCardLayout);
	MultiVisibilityPane multiVisPane;

	JButton allInvisButton, allVisButton, selSimButton;

	JButton okayButton, cancelButton;

	boolean importSuccess = false;
	boolean importStarted = false;
	boolean importEnded = false;

	private ModelStructureChangeListener callback;

	public ImportPanel(final EditableModel a, final EditableModel b, final ProgramPreferences preferences) {
		this(a, b, preferences, true);
	}

	public ImportPanel(final EditableModel currentModel, final EditableModel importedModel,
			final ProgramPreferences preferences, final boolean visibleOnStart) {
		super();
		if (currentModel.getName().equals(importedModel.getName())) {
			importedModel.setFileRef(new File(
					importedModel.getFile().getParent() + "/" + importedModel.getName() + " (Imported)" + ".mdl"));
			frame = new JFrame("Importing " + currentModel.getName() + " into itself");
		} else {
			frame = new JFrame("Importing " + importedModel.getName() + " into " + currentModel.getName());
		}
		currentModel.doSavePreps(preferences.isAlwaysUseMinimalMatricesInHD());
		try {
			frame.setIconImage(RMSIcons.MDLIcon.getImage());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null,
					"Error: Image files were not found! Due to bad programming, this might break the program!");
		}
		this.currentModel = currentModel;
		this.importedModel = importedModel;

		// Geoset Panel
		addTab("Geosets", geoIcon, geosetsPanel, "Controls which geosets will be imported.");

		final DefaultListModel materials = new DefaultListModel();
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

			geosetTabs.addTab(currentModel.getName() + " " + (i + 1), greenIcon, geoPanel,
					"Click to modify material data for this geoset.");
		}
		for (int i = 0; i < importedModel.getGeosets().size(); i++) {
			final GeosetPanel geoPanel = new GeosetPanel(true, importedModel, i, materials, materialsRenderer);

			geosetTabs.addTab(importedModel.getName() + " " + (i + 1), orangeIcon, geoPanel,
					"Click to modify importing and material data for this geoset.");
		}

		importAllGeos = new JButton("Import All");
		importAllGeos.addActionListener(this);
		geosetsPanel.add(importAllGeos);

		uncheckAllGeos = new JButton("Leave All");
		uncheckAllGeos.addActionListener(this);
		geosetsPanel.add(uncheckAllGeos);

		final GroupLayout geosetLayout = new GroupLayout(geosetsPanel);
		geosetLayout
				.setHorizontalGroup(geosetLayout
						.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(geosetLayout.createSequentialGroup()
								.addComponent(importAllGeos).addGap(8).addComponent(uncheckAllGeos))
						.addComponent(geosetTabs));
		geosetLayout
				.setVerticalGroup(geosetLayout.createSequentialGroup()
						.addGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(importAllGeos).addComponent(uncheckAllGeos))
						.addGap(8).addComponent(geosetTabs));
		geosetsPanel.setLayout(geosetLayout);

		// Animation Panel
		addTab("Animation", animIcon, animPanel, "Controls which animations will be imported.");

		existingAnims = new DefaultListModel();
		for (int i = 0; i < currentModel.getAnims().size(); i++) {
			existingAnims.addElement(new AnimShell(currentModel.getAnims().get(i)));
		}

		final AnimListCellRenderer animsRenderer = new AnimListCellRenderer();

		importAllAnims = new JButton("Import All");
		importAllAnims.addActionListener(this);
		animPanel.add(importAllAnims);

		timescaleAllAnims = new JButton("Time-scale All");
		timescaleAllAnims.addActionListener(this);
		animPanel.add(timescaleAllAnims);

		renameAllAnims = new JButton("Import and Rename All");
		renameAllAnims.addActionListener(this);
		animPanel.add(renameAllAnims);

		uncheckAllAnims = new JButton("Leave All");
		uncheckAllAnims.addActionListener(this);
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
				.addGroup(animLayout.createSequentialGroup().addComponent(importAllAnims).addGap(8)
						.addComponent(renameAllAnims).addGap(8).addComponent(timescaleAllAnims).addGap(8)
						.addComponent(uncheckAllAnims))
				.addComponent(clearExistingAnims).addComponent(animTabs));
		animLayout.setVerticalGroup(animLayout.createSequentialGroup()
				.addGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(importAllAnims)
						.addComponent(renameAllAnims).addComponent(timescaleAllAnims).addComponent(uncheckAllAnims))
				.addComponent(clearExistingAnims).addGap(8).addComponent(animTabs));
		animPanel.setLayout(animLayout);

		// Bone Panel
		addTab("Bones", boneIcon, bonesPanel, "Controls which bones will be imported.");
		existingBones = new DefaultListModel<BoneShell>();
		final ArrayList<Bone> currentMDLBones = currentModel.sortedIdObjects(Bone.class);
		final ArrayList<Helper> currentMDLHelpers = currentModel.sortedIdObjects(Helper.class);
		for (int i = 0; i < currentMDLBones.size(); i++) {
			existingBones.addElement(new BoneShell(currentMDLBones.get(i)));
		}
		for (int i = 0; i < currentMDLHelpers.size(); i++) {
			existingBones.addElement(new BoneShell(currentMDLHelpers.get(i)));
		}

		final ArrayList<Bone> importedMDLBones = importedModel.sortedIdObjects(Bone.class);
		final ArrayList<Helper> importedMDLHelpers = importedModel.sortedIdObjects(Helper.class);

		clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
		currentModelManager = new ModelViewManager(currentModel);
		importedModelManager = new ModelViewManager(importedModel);
		final BonePanelListCellRenderer bonePanelRenderer = new BonePanelListCellRenderer(currentModelManager,
				importedModelManager);
		boneShellRenderer = new BoneShellListCellRenderer(currentModelManager, importedModelManager);
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
		boneTabs.addListSelectionListener(this);
		boneTabs.setSelectedIndex(0);
		bonePanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		importAllBones = new JButton("Import All");
		importAllBones.addActionListener(this);
		bonesPanel.add(importAllBones);

		uncheckAllBones = new JButton("Leave All");
		uncheckAllBones.addActionListener(this);
		bonesPanel.add(uncheckAllBones);

		motionFromBones = new JButton("Motion From All");
		motionFromBones.addActionListener(this);
		bonesPanel.add(motionFromBones);

		uncheckUnusedBones = new JButton("Uncheck Unused");
		uncheckUnusedBones.addActionListener(this);
		bonesPanel.add(uncheckUnusedBones);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boneTabsPane, bonePanelCards);

		final GroupLayout boneLayout = new GroupLayout(bonesPanel);
		boneLayout.setHorizontalGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(boneLayout.createSequentialGroup().addComponent(importAllBones).addGap(8)
						.addComponent(motionFromBones).addGap(8).addComponent(uncheckUnusedBones).addGap(8)
						.addComponent(uncheckAllBones))
				.addComponent(clearExistingBones).addComponent(splitPane)
		// .addGroup(boneLayout.createSequentialGroup()
		// .addComponent(boneTabsPane)
		// .addComponent(bonePanelCards)
		// )
		);
		boneLayout.setVerticalGroup(boneLayout.createSequentialGroup()
				.addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(importAllBones)
						.addComponent(motionFromBones).addComponent(uncheckUnusedBones).addComponent(uncheckAllBones))
				.addComponent(clearExistingBones).addGap(8).addComponent(splitPane)
		// .addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
		// .addComponent(boneTabsPane)
		// .addComponent(bonePanelCards)
		// )
		);
		bonesPanel.setLayout(boneLayout);

		// Matrices Panel
		addTab("Matrices", greenIcon, geosetAnimPanel, "Controls which bones geosets are attached to.");
//		addTab("Skin", orangeIcon, new JPanel(), "Edit SKIN chunk");

		final ParentToggleRenderer ptr = new ParentToggleRenderer(displayParents, currentModelManager,
				importedModelManager);

		displayParents.addChangeListener(this);

		allMatrOriginal.addActionListener(this);
		allMatrSameName.addActionListener(this);
		applySmartMapping.addActionListener(this);

		// Build the geosetAnimTabs list of GeosetPanels
		for (int i = 0; i < currentModel.getGeosets().size(); i++) {
			final BoneAttachmentPane geoPanel = new BoneAttachmentPane(currentModel, currentModel.getGeoset(i), ptr,
					this);

			geosetAnimTabs.addTab(currentModel.getName() + " " + (i + 1), greenIcon, geoPanel,
					"Click to modify animation data for Geoset " + i + " from " + currentModel.getName() + ".");
		}
		for (int i = 0; i < importedModel.getGeosets().size(); i++) {
			final BoneAttachmentPane geoPanel = new BoneAttachmentPane(importedModel, importedModel.getGeoset(i), ptr,
					this);

			geosetAnimTabs.addTab(importedModel.getName() + " " + (i + 1), orangeIcon, geoPanel,
					"Click to modify animation data for Geoset " + i + " from " + importedModel.getName() + ".");
		}
		geosetAnimTabs.addChangeListener(this);

		geosetAnimPanel.add(geosetAnimTabs);
		final GroupLayout gaLayout = new GroupLayout(geosetAnimPanel);
		gaLayout.setVerticalGroup(
				gaLayout.createSequentialGroup().addComponent(displayParents).addComponent(allMatrOriginal)
						.addComponent(allMatrSameName).addComponent(applySmartMapping).addComponent(geosetAnimTabs));
		gaLayout.setHorizontalGroup(gaLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(displayParents).addComponent(allMatrOriginal).addComponent(allMatrSameName)
				.addComponent(applySmartMapping).addComponent(geosetAnimTabs));
		geosetAnimPanel.setLayout(gaLayout);

		// Objects Panel
		addTab("Objects", objIcon, objectsPanel, "Controls which objects are imported.");
		getFutureBoneListExtended(false);

		// Build the objectTabs list of ObjectPanels
		final ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
		int panelid = 0;
		for (int i = 0; i < importedModel.getIdObjects().size(); i++) {
			final IdObject obj = importedModel.getIdObjects().get(i);
			if (obj.getClass() != Bone.class && obj.getClass() != Helper.class) {

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
		objectTabs.addListSelectionListener(this);
		objectTabs.setSelectedIndex(0);
		objectPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		importAllObjs = new JButton("Import All");
		importAllObjs.addActionListener(this);
		bonesPanel.add(importAllObjs);

		uncheckAllObjs = new JButton("Leave All");
		uncheckAllObjs.addActionListener(this);
		bonesPanel.add(uncheckAllObjs);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectTabsPane, objectPanelCards);

		final GroupLayout objectLayout = new GroupLayout(objectsPanel);
		objectLayout
				.setHorizontalGroup(objectLayout
						.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(objectLayout.createSequentialGroup()
								.addComponent(importAllObjs).addGap(8).addComponent(uncheckAllObjs))
						.addComponent(splitPane));
		objectLayout
				.setVerticalGroup(objectLayout.createSequentialGroup()
						.addGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(importAllObjs).addComponent(uncheckAllObjs))
						.addGap(8).addComponent(splitPane));
		objectsPanel.setLayout(objectLayout);

		// Visibility Panel
		addTab("Visibility", orangeIcon, visPanel, "Controls the visibility of portions of the model.");

		initVisibilityList();
		visibilityList();

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		for (final VisibilityShell vs : allVisShells) {
			final VisibilityPane vp = new VisibilityPane(vs, new DefaultComboBoxModel(visSourcesOld.toArray()),
					new DefaultComboBoxModel(visSourcesNew.toArray()), visRenderer);

			allVisShellPanes.add(vp);

			visPanelCards.add(vp, vp.title.getText());
		}

		multiVisPane = new MultiVisibilityPane(new DefaultComboBoxModel(visSourcesOld.toArray()),
				new DefaultComboBoxModel(visSourcesNew.toArray()), visRenderer);
		visPanelCards.add(blankPane, "blank");
		visPanelCards.add(multiVisPane, "multiple");
		visTabs.setModel(visComponents);
		visTabs.setCellRenderer(new VisPaneListCellRenderer(currentModel));
		visTabs.addListSelectionListener(this);
		visTabs.setSelectedIndex(0);
		visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		allInvisButton = new JButton("All Invisible in Exotic Anims");
		allInvisButton.addActionListener(this);
		allInvisButton.setToolTipText(
				"Forces everything to be always invisibile in animations other than their own original animations.");
		visPanel.add(allInvisButton);

		allVisButton = new JButton("All Visible in Exotic Anims");
		allVisButton.addActionListener(this);
		allVisButton.setToolTipText(
				"Forces everything to be always visibile in animations other than their own original animations.");
		visPanel.add(allVisButton);

		selSimButton = new JButton("Select Similar Options");
		selSimButton.addActionListener(this);
		selSimButton.setToolTipText("Similar components will be selected as visibility sources in exotic animations.");
		visPanel.add(selSimButton);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, visTabsPane, visPanelCards);

		final GroupLayout visLayout = new GroupLayout(visPanel);
		visLayout.setHorizontalGroup(
				visLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(allInvisButton)
						// .addGap(8)
						.addComponent(allVisButton).addComponent(selSimButton).addComponent(splitPane));
		visLayout.setVerticalGroup(visLayout.createSequentialGroup().addComponent(allInvisButton).addGap(8)
				.addComponent(allVisButton).addGap(8).addComponent(selSimButton).addGap(8).addComponent(splitPane));
		visPanel.setLayout(visLayout);

		// Listen all
		addChangeListener(this);

		okayButton = new JButton("Finish");
		okayButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		final JPanel finalPanel = new JPanel();
		final GroupLayout layout = new GroupLayout(finalPanel);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(this)
				.addGroup(layout.createSequentialGroup().addComponent(cancelButton).addComponent(okayButton)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(this).addGap(8)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(cancelButton)
						.addComponent(okayButton)));
		finalPanel.setLayout(layout);

		// Later add a Yes/No confirmation of "do you wish to cancel this
		// import?" when you close the window.
		frame.setContentPane(finalPanel);

		frame.setBounds(0, 0, 1024, 780);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				final Object[] options = { "Yes", "No" };
				final int n = JOptionPane.showOptionDialog(frame, "Really cancel this import?", "Confirmation",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (n == 0) {
					frame.setVisible(false);
					frame = null;
				}
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// frame.pack();
		frame.setVisible(visibleOnStart);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == importAllGeos) {
			for (int i = 0; i < geosetTabs.getTabCount(); i++) {
				final GeosetPanel geoPanel = (GeosetPanel) geosetTabs.getComponentAt(i);
				geoPanel.setSelected(true);
			}
		} else if (e.getSource() == uncheckAllGeos) {
			for (int i = 0; i < geosetTabs.getTabCount(); i++) {
				final GeosetPanel geoPanel = (GeosetPanel) geosetTabs.getComponentAt(i);
				geoPanel.setSelected(false);
			}
		} else if (e.getSource() == importAllAnims) {
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				aniPanel.setSelected(true);
			}
		} else if (e.getSource() == timescaleAllAnims) {
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				aniPanel.importTypeBox.setSelectedIndex(2);
			}
		} else if (e.getSource() == renameAllAnims) {
			final String newTagString = JOptionPane.showInputDialog(this,
					"Choose additional naming (i.e. swim or alternate)");
			if (newTagString != null) {
				for (int i = 0; i < animTabs.getTabCount(); i++) {
					final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
					aniPanel.importTypeBox.setSelectedIndex(1);
					final String oldName = aniPanel.anim.getName();
					String baseName = oldName;
					while (baseName.length() > 0 && baseName.contains(" ")) {
						final int lastSpaceIndex = baseName.lastIndexOf(' ');
						final String lastWord = baseName.substring(lastSpaceIndex + 1);
						boolean chunkHasInt = false;
						for (int animationId = 0; animationId < 10; animationId++) {
							if (lastWord.contains(Integer.toString(animationId))) {
								chunkHasInt = true;
							}
						}
						if (lastWord.contains("-") || chunkHasInt || lastWord.toLowerCase().contains("alternate")
								|| lastWord.length() <= 0) {
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
		} else if (e.getSource() == uncheckAllAnims) {
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				aniPanel.setSelected(false);
			}
		} else if (e.getSource() == importAllBones) {
			for (int i = 0; i < bonePanels.size(); i++) {
				final BonePanel bonePanel = bonePanels.get(i);
				bonePanel.setSelectedIndex(0);
			}
		} else if (e.getSource() == motionFromBones) {
			for (int i = 0; i < bonePanels.size(); i++) {
				final BonePanel bonePanel = bonePanels.get(i);
				bonePanel.setSelectedIndex(1);
			}
		} else if (e.getSource() == uncheckUnusedBones) {
			// Unselect all bones by iterating + setting to index 2 ("Do not
			// import" index)
			// Bones could be referenced by:
			// - A matrix
			// - Another bone
			// - An IdObject
			final ArrayList<BonePanel> usedBonePanels = new ArrayList<>();
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
				if (objectPanel.doImport.isSelected() && objectPanel.parentsList != null) {
					// System.out.println("Performing check on base:
					// "+objectPanel.object.getName());
					BoneShell shell = (BoneShell) objectPanel.parentsList.getSelectedValue();
					if (shell != null && shell.bone != null) {
						BonePanel current = getPanelOf(shell.bone);
						if (!usedBonePanels.contains(current)) {
							// System.out.println(" @adding base:
							// "+current.bone.getName());
							usedBonePanels.add(current);
						}

						boolean good = true;
						int k = 0;
						while (good) {
							if (current == null || current.getSelectedIndex() == 1) {
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
					final BoneAttachmentPane bap = (BoneAttachmentPane) geosetAnimTabs.getComponentAt(i);
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
								if (current == null || current.getSelectedIndex() == 1) {
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
							if (current == null || current.getSelectedIndex() == 1) {
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
		} else if (e.getSource() == uncheckAllBones) {
			for (int i = 0; i < bonePanels.size(); i++) {
				final BonePanel bonePanel = bonePanels.get(i);
				bonePanel.setSelectedIndex(2);
			}
		} else if (e.getSource() == importAllObjs) {
			for (int i = 0; i < objectPanels.size(); i++) {
				final ObjectPanel objectPanel = objectPanels.get(i);
				objectPanel.doImport.setSelected(true);
			}
		} else if (e.getSource() == uncheckAllObjs) {
			for (int i = 0; i < objectPanels.size(); i++) {
				final ObjectPanel objectPanel = objectPanels.get(i);
				objectPanel.doImport.setSelected(false);
			}
		} else if (e.getSource() == allVisButton) {
			for (int i = 0; i < allVisShellPanes.size(); i++) {
				final VisibilityPane vPanel = allVisShellPanes.get(i);
				if (vPanel.sourceShell.model == currentModel) {
					vPanel.newSourcesBox.setSelectedItem(VisibilityPane.VISIBLE);
				} else {
					vPanel.oldSourcesBox.setSelectedItem(VisibilityPane.VISIBLE);
				}
			}
		} else if (e.getSource() == allInvisButton) {
			for (int i = 0; i < allVisShellPanes.size(); i++) {
				final VisibilityPane vPanel = allVisShellPanes.get(i);
				if (vPanel.sourceShell.model == currentModel) {
					vPanel.newSourcesBox.setSelectedItem(VisibilityPane.NOTVISIBLE);
				} else {
					vPanel.oldSourcesBox.setSelectedItem(VisibilityPane.NOTVISIBLE);
				}
			}
		} else if (e.getSource() == selSimButton) {
			for (int i = 0; i < allVisShellPanes.size(); i++) {
				final VisibilityPane vPanel = allVisShellPanes.get(i);
				vPanel.selectSimilarOptions();
			}
		} else if (e.getSource() == okayButton) {
			doImport();
			frame.setVisible(false);
		} else if (e.getSource() == cancelButton) {
			final Object[] options = { "Yes", "No" };
			final int n = JOptionPane.showOptionDialog(frame, "Really cancel this import?", "Confirmation",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				frame.setVisible(false);
				frame = null;
			}
		} else if (e.getSource() == allMatrOriginal) {
			for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
				if (geosetAnimTabs.isEnabledAt(i)) {
					final BoneAttachmentPane bap = (BoneAttachmentPane) geosetAnimTabs.getComponentAt(i);
					bap.resetMatrices();
				}
			}
		} else if (e.getSource() == allMatrSameName) {
			for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
				if (geosetAnimTabs.isEnabledAt(i)) {
					final BoneAttachmentPane bap = (BoneAttachmentPane) geosetAnimTabs.getComponentAt(i);
					bap.setMatricesToSimilarNames();
				}
			}
		} else if (e.getSource() == applySmartMapping) {
			final SmartMappingChooserPanel smartMapChooserPanel = new SmartMappingChooserPanel(importedModelManager,
					currentModelManager, getFutureBoneListExtended(true));
			final int userResult = JOptionPane.showConfirmDialog(this, smartMapChooserPanel, "Apply Smart Mapping",
					JOptionPane.OK_CANCEL_OPTION);
			if (userResult == JOptionPane.OK_OPTION) {
				final DefaultListModel<Pairing> pairings = smartMapChooserPanel.getPairingListModel();
				final Map<Bone, Bone> pairingMap = new HashMap<>();
				for (int i = 0; i < pairings.size(); i++) {
					final Pairing pairing = pairings.get(i);
					pairingMap.put(pairing.importBone.bone, pairing.currentBone.bone);
				}
				for (int i = 0; i < geosetAnimTabs.getTabCount(); i++) {
					if (geosetAnimTabs.isEnabledAt(i)) {
						final BoneAttachmentPane bap = (BoneAttachmentPane) geosetAnimTabs.getComponentAt(i);
						bap.applySmartMapping(pairingMap);
					}
				}
			}
		}
	}

	// boolean listEnabled = true;
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (e.getSource() == boneTabs) {
			// boolean listEnabledNow = false;
			if (boneTabs.getSelectedValuesList().toArray().length < 1) {
				// listEnabledNow = listEnabled;
				boneCardLayout.show(bonePanelCards, "blank");
			} else if (boneTabs.getSelectedValuesList().toArray().length == 1) {
				// listEnabledNow = true;
				boneCardLayout.show(bonePanelCards, boneTabs.getSelectedIndex() + "");
				((BonePanel) boneTabs.getSelectedValue()).updateSelectionPicks();
			} else if (boneTabs.getSelectedValuesList().toArray().length > 1) {
				boneCardLayout.show(bonePanelCards, "multiple");
				// listEnabledNow = false;
				final Object[] selected = boneTabs.getSelectedValuesList().toArray();
				boolean dif = false;
				int tempIndex = -99;
				for (int i = 0; i < selected.length && !dif; i++) {
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
		} else if (e.getSource() == objectTabs) {
			if (objectTabs.getSelectedValuesList().toArray().length < 1) {
				objectCardLayout.show(objectPanelCards, "blank");
			} else if (objectTabs.getSelectedValuesList().toArray().length == 1) {
				getFutureBoneListExtended(false);
				objectCardLayout.show(objectPanelCards, objectTabs.getSelectedIndex() + "");// .title.getText()
			} else if (objectTabs.getSelectedValuesList().toArray().length > 1) {
				objectCardLayout.show(objectPanelCards, "multiple");
				final Object[] selected = objectTabs.getSelectedValuesList().toArray();
				boolean dif = false;
				boolean set = false;
				boolean selectedt = false;
				for (int i = 0; i < selected.length && !dif; i++) {
					final ObjectPanel temp = (ObjectPanel) selected[i];
					if (set == false) {
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
		} else if (e.getSource() == visTabs) {
			if (visTabs.getSelectedValuesList().toArray().length < 1) {
				visCardLayout.show(visPanelCards, "blank");
			} else if (visTabs.getSelectedValuesList().toArray().length == 1) {
				visCardLayout.show(visPanelCards, ((VisibilityPane) visTabs.getSelectedValue()).title.getText());
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

				for (int i = 0; i < selected.length && !dif; i++) {
					final VisibilityPane temp = (VisibilityPane) selected[i];
					if (set == false) {
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
					multiVisPane.favorOld.setSelected(selectedt);
				}
				if (difBoxOld) {
					multiVisPane.setMultipleOld();
				} else {
					multiVisPane.oldSourcesBox.setSelectedIndex(tempIndexOld);
				}
				if (difBoxNew) {
					multiVisPane.setMultipleNew();
				} else {
					multiVisPane.newSourcesBox.setSelectedIndex(tempIndexNew);
				}
			}
		}
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
			final BoneAttachmentPane geoPanel = (BoneAttachmentPane) geosetAnimTabs.getComponentAt(i);
			if (geoPanel.geoset == g) {
				geosetAnimTabs.setEnabledAt(i, flag);
			}
		}
	}

	/**
	 * Provides a Runnable to the ImportPanel that will be run after the import has
	 * ended successfully.
	 *
	 * @param callback
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
			final ArrayList<Bone> oldBonesRefs = currentModel.sortedIdObjects(Bone.class);
			for (final Bone b : oldBonesRefs) {
				final BoneShell bs = new BoneShell(b);
				bs.modelName = currentModel.getName();
				oldBones.add(bs);
			}
			final ArrayList<Bone> newBonesRefs = importedModel.sortedIdObjects(Bone.class);
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
			} else if (futureBoneList.contains(b)) {
				futureBoneList.removeElement(b);
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
			ArrayList<? extends Bone> oldHelpersRefs = currentModel.sortedIdObjects(Bone.class);
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
			ArrayList<? extends Bone> newHelpersRefs = importedModel.sortedIdObjects(Bone.class);
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
					totalAddTime += endTime - startTime;
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
					totalRemoveTime += endTime - startTime;
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
						totalAddTime += endTime - startTime;
						addCount++;
						futureBoneListExQuickLookupSet.add(b);
					}
				} else if (futureBoneListExQuickLookupSet.remove(b)) {
					final long startTime = System.nanoTime();
					futureBoneListEx.removeElement(b);
					final long endTime = System.nanoTime();
					totalRemoveTime += endTime - startTime;
					removeCount++;
				}
			}
		}
		if (addCount != 0) {
			System.out.println("average add time: " + totalAddTime / addCount);
			System.out.println("add count: " + addCount);
		}
		if (removeCount != 0) {
			System.out.println("average remove time: " + totalRemoveTime / removeCount);
			System.out.println("remove count: " + removeCount);
		}

		DefaultListModel<BoneShell> listModelToReturn;
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
	DefaultListModel<VisibilityPane> visComponents;

	ArrayList<VisibilityPane> allVisShellPanes = new ArrayList<>();
	private final BoneShellListCellRenderer boneShellRenderer;
	private final ModelViewManager currentModelManager;
	private final ModelViewManager importedModelManager;

	public VisibilityShell shellFromObject(final Object o) {
		for (final VisibilityShell v : allVisShells) {
			if (v.source == o) {
				return v;
			}
		}
		return null;
	}

	public VisibilityPane visPaneFromObject(final Object o) {
		for (final VisibilityPane vp : allVisShellPanes) {
			if (vp.sourceShell.source == o) {
				return vp;
			}
		}
		return null;
	}

	public void initVisibilityList() {
		visSourcesOld = new ArrayList();
		visSourcesNew = new ArrayList();
		allVisShells = new ArrayList<>();
		EditableModel model = currentModel;
		final ArrayList tempList = new ArrayList();
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
		visSourcesOld.add(VisibilityPane.NOTVISIBLE);
		visSourcesOld.add(VisibilityPane.VISIBLE);
		for (final Object o : importedModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				visSourcesNew.add(shellFromObject(o));
			} else {
				visSourcesNew.add(shellFromObject(((GeosetAnim) o).getGeoset()));
			}
		}
		visSourcesNew.add(VisibilityPane.NOTVISIBLE);
		visSourcesNew.add(VisibilityPane.VISIBLE);
		visComponents = new DefaultListModel<>();
	}

	public DefaultListModel<VisibilityPane> visibilityList() {
		final Object selection = visTabs.getSelectedValue();
		visComponents.clear();
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			for (final Layer l : gp.getSelectedMaterial().getLayers()) {
				final VisibilityPane vs = visPaneFromObject(l);
				if (!visComponents.contains(vs) && vs != null) {
					visComponents.addElement(vs);
				}
			}
		}
		for (int i = 0; i < geosetTabs.getTabCount(); i++) {
			final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
			if (gp.doImport.isSelected()) {
				final Geoset ga = gp.geoset;
				final VisibilityPane vs = visPaneFromObject(ga);
				if (!visComponents.contains(vs) && vs != null) {
					visComponents.addElement(vs);
				}
			}
		}
		// The current's
		final EditableModel model = currentModel;
		for (final Object l : model.sortedIdObjects(Light.class)) {
			final VisibilityPane vs = visPaneFromObject(l);
			if (!visComponents.contains(vs) && vs != null) {
				visComponents.addElement(vs);
			}
		}
		for (final Object a : model.sortedIdObjects(Attachment.class)) {
			final VisibilityPane vs = visPaneFromObject(a);
			if (!visComponents.contains(vs) && vs != null) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter.class)) {
			final VisibilityPane vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && vs != null) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitter2.class)) {
			final VisibilityPane vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && vs != null) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(RibbonEmitter.class)) {
			final VisibilityPane vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && vs != null) {
				visComponents.addElement(vs);
			}
		}
		for (final Object x : model.sortedIdObjects(ParticleEmitterPopcorn.class)) {
			final VisibilityPane vs = visPaneFromObject(x);
			if (!visComponents.contains(vs) && vs != null) {
				visComponents.addElement(vs);
			}
		}

		for (int i = 0; i < objectPanels.size(); i++) {
			final ObjectPanel op = objectPanels.get(i);
			if (op.doImport.isSelected() && op.object != null)// we don't touch
																// camera
																// "object"
																// panels (which
																// aren't
																// idobjects)
			{
				final VisibilityPane vs = visPaneFromObject(op.object);
				if (!visComponents.contains(vs) && vs != null) {
					visComponents.addElement(vs);
				}
			}
		}
		visTabs.setSelectedValue(selection, true);
		return visComponents;
	}

	public void setSelectedItem(final String what) {
		final Object[] selected = boneTabs.getSelectedValuesList().toArray();
		for (int i = 0; i < selected.length; i++) {
			final BonePanel temp = (BonePanel) selected[i];
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
			for (int i = 0; i < selected.length; i++) {
				final BonePanel temp = (BonePanel) selected[i];
				temp.setParent(list.getSelectedValue());
			}
		}
	}

	public void setObjGroupSelected(final boolean flag) {
		final Object[] selected = objectTabs.getSelectedValuesList().toArray();
		for (int i = 0; i < selected.length; i++) {
			final ObjectPanel temp = (ObjectPanel) selected[i];
			temp.doImport.setSelected(flag);
		}
	}

	public void setVisGroupSelected(final boolean flag) {
		final Object[] selected = visTabs.getSelectedValuesList().toArray();
		for (int i = 0; i < selected.length; i++) {
			final VisibilityPane temp = (VisibilityPane) selected[i];
			temp.favorOld.setSelected(flag);
		}
	}

	public void setVisGroupItemOld(final Object o) {
		final Object[] selected = visTabs.getSelectedValuesList().toArray();
		for (int i = 0; i < selected.length; i++) {
			final VisibilityPane temp = (VisibilityPane) selected[i];
			temp.oldSourcesBox.setSelectedItem(o);
		}
	}

	public void setVisGroupItemNew(final Object o) {
		final Object[] selected = visTabs.getSelectedValuesList().toArray();
		for (int i = 0; i < selected.length; i++) {
			final VisibilityPane temp = (VisibilityPane) selected[i];
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

			// ArrayList<Geoset> newGeosets = new ArrayList<Geoset>();//Just for
			// 3d update

			for (int i = 0; i < geosetTabs.getTabCount(); i++) {
				final GeosetPanel gp = (GeosetPanel) geosetTabs.getComponentAt(i);
				gp.geoset.setMaterial(gp.getSelectedMaterial());
				if (gp.doImport.isSelected() && gp.model == importedModel) {
					currentModel.add(gp.geoset);
					// newGeosets.add(gp.geoset);//Just for 3d update
					if (gp.geoset.getGeosetAnim() != null) {
						currentModel.add(gp.geoset.getGeosetAnim());
					}
				}
			}
			final ArrayList<Animation> oldAnims = new ArrayList<>();
			oldAnims.addAll(currentModel.getAnims());
			final ArrayList<Animation> newAnims = new ArrayList<>();
			final java.util.List<AnimFlag> curFlags = currentModel.getAllAnimFlags();
			final java.util.List<AnimFlag> impFlags = importedModel.getAllAnimFlags();
			final ArrayList curEventObjs = currentModel.sortedIdObjects(EventObject.class);
			final ArrayList impEventObjs = importedModel.sortedIdObjects(EventObject.class);
			// note to self: remember to scale event objects with time
			final ArrayList<AnimFlag> newImpFlags = new ArrayList<>();
			for (final AnimFlag af : impFlags) {
				if (!af.hasGlobalSeq()) {
					newImpFlags.add(AnimFlag.buildEmptyFrom(af));
				} else {
					newImpFlags.add(new AnimFlag(af));
				}
			}
			final ArrayList<EventObject> newImpEventObjs = new ArrayList<>();
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
						aniPanel.anim.copyToInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300,
								impFlags, impEventObjs, newImpFlags, newImpEventObjs);
						aniPanel.anim.setInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300);
						currentModel.add(aniPanel.anim);
						newAnims.add(aniPanel.anim);
						break;
					case 1:
						aniPanel.anim.copyToInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300,
								impFlags, impEventObjs, newImpFlags, newImpEventObjs);
						aniPanel.anim.setInterval(animTrackEnd + 300, animTrackEnd + aniPanel.anim.length() + 300);
						aniPanel.anim.setName(aniPanel.newNameEntry.getText());
						currentModel.add(aniPanel.anim);
						newAnims.add(aniPanel.anim);
						break;
					case 2:
						// List<AnimShell> targets =
						// aniPane.animList.getSelectedValuesList();
						// aniPanel.anim.setInterval(animTrackEnd+300,animTrackEnd
						// + aniPanel.anim.length() + 300, impFlags,
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
								final BoneShell bs = existingBones.get(p);
								if (bs.importBone != null) {
									if (getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1) {
										// JOptionPane.showMessageDialog(null,"Attempting
										// to clear animation for
										// "+bs.bone.getName()+" values
										// "+animShell.anim.getStart()+",
										// "+animShell.anim.getEnd());
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
				switch (type) {
				case 0:
					currentModel.add(b);
					objectsAdded.add(b);
					// b.setName(b.getName()+" "+importedModel.getName());
					final BoneShell mbs = (BoneShell) bonePanel.futureBonesList.getSelectedValue();
					if (mbs != null) {
						b.setParent(mbs.bone);
					} else {
						b.setParent(null);
					}
					break;
				case 1: // List targets =
						// bonePanel.boneList.getSelectedValuesList();
					// we will go through all bone shells for this
				case 2:
					// Fix cross-model referencing issue (force clean parent node's list of
					// children)
					b.setParent(null);
					break;
				}
			}
			if (!clearBones) {
				for (int i = 0; i < existingBones.size(); i++) {
					final BoneShell bs = existingBones.get(i);
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
					final BoneAttachmentPane bap = (BoneAttachmentPane) geosetAnimTabs.getComponentAt(i);
					for (int l = 0; l < bap.oldBoneRefs.size(); l++) {
						final MatrixShell ms = bap.oldBoneRefs.get(l);
						ms.matrix.getBones().clear();
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
								dummyBone.setPivotPoint(new Vertex(0, 0, 0));
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
						// objectPanel.object.setName(importedModel.getName()+"
						// "+objectPanel.object.getName());
						// later make a name field?
						currentModel.add(objectPanel.object);
						objectsAdded.add(objectPanel.object);
					} else if (objectPanel.camera != null) {
						// objectPanel.camera.setName(importedModel.getName()+"
						// "+objectPanel.camera.getName());
						currentModel.add(objectPanel.camera);
						camerasAdded.add(objectPanel.camera);
					}
				} else if (objectPanel.object != null) {
					objectPanel.object.setParent(null);
					// Fix cross-model referencing issue (force clean parent node's list of
					// children)
				}
			}

			final ArrayList<AnimFlag> finalVisFlags = new ArrayList<>();
			for (int i = 0; i < visComponents.size(); i++) {
				final VisibilityPane vPanel = visComponents.get(i);
				final VisibilitySource temp = (VisibilitySource) vPanel.sourceShell.source;
				final AnimFlag visFlag = temp.getVisibilityFlag();// might be
																	// null
				AnimFlag newVisFlag;
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
					if (oldSource == VisibilityPane.VISIBLE) {
						// empty for visible
					} else if (oldSource == VisibilityPane.NOTVISIBLE) {
						flagOld = new AnimFlag("temp");
						for (final Animation a : oldAnims) {
							test = true;
							if (tans) {
								flagOld.addEntry(new Integer(a.getStart()), new Double(0), new Double(0),
										new Double(0));
							} else {
								flagOld.addEntry(new Integer(a.getStart()), new Double(0));
							}
						}
					}
				} else {
					flagOld = ((VisibilitySource) ((VisibilityShell) oldSource).source).getVisibilityFlag();
				}
				final Object newSource = vPanel.newSourcesBox.getSelectedItem();
				AnimFlag flagNew = null;
				if (newSource.getClass() == String.class) {
					if (newSource == VisibilityPane.VISIBLE) {
						// empty for visible
					} else if (newSource == VisibilityPane.NOTVISIBLE) {
						flagNew = new AnimFlag("temp");
						for (final Animation a : newAnims) {
							if (tans) {
								flagNew.addEntry(new Integer(a.getStart()), new Double(0), new Double(0),
										new Double(0));
							} else {
								flagNew.addEntry(new Integer(a.getStart()), new Double(0));
							}
							// flagNew.times.add(new Integer(a.getStart()));
							// flagNew.values.add(new Double(0));
							// if( tans )
							// {
							// flagNew.inTans.add(new Double(0));
							// flagNew.outTans.add(new Double(0));
							// }
						}
					}
				} else {
					flagNew = ((VisibilitySource) ((VisibilityShell) newSource).source).getVisibilityFlag();
				}
				if (vPanel.favorOld.isSelected() && vPanel.sourceShell.model == currentModel && !clearAnims
						|| !vPanel.favorOld.isSelected() && vPanel.sourceShell.model == importedModel) {
					// this is an element favoring existing animations over
					// imported
					for (final Animation a : oldAnims) {
						if (flagNew != null) {
							if (!flagNew.hasGlobalSeq()) {
								flagNew.deleteAnim(a);// All entries for
														// visibility are
														// deleted from imported
														// sources during
														// existing animation
														// times
							}
						}
					}
				} else {
					// this is an element not favoring existing over imported
					for (final Animation a : newAnims) {
						if (flagOld != null) {
							if (!flagOld.hasGlobalSeq()) {
								flagOld.deleteAnim(a);// All entries for
														// visibility are
														// deleted from
														// original-based
														// sources during
														// imported animation
														// times
							}
						}
					}
				}
				if (flagOld != null) {
					newVisFlag.copyFrom(flagOld);
				} else {
				}
				if (flagNew != null) {
					newVisFlag.copyFrom(flagNew);
				} else {
				}
				finalVisFlags.add(newVisFlag);
			}
			for (int i = 0; i < visComponents.size(); i++) {
				final VisibilityPane vPanel = visComponents.get(i);
				final VisibilitySource temp = (VisibilitySource) vPanel.sourceShell.source;
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
				if (gp.doImport.isSelected() && gp.model == importedModel) {
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
		uncheckAllGeos.doClick();
		motionFromBones.doClick();
		clearExistingAnims.doClick();
		uncheckAllObjs.doClick();
		visibilityList();
		selSimButton.doClick();

		if (singleAnimation) {
			// JOptionPane.showMessageDialog(null,"single trans");
			uncheckAllAnims.doClick();
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				if (aniPanel.anim.getName().equals(pickedAnim.getName())) {
					aniPanel.doImport.setSelected(true);
				}
			}
			clearExistingAnims.doClick();// turn it back off
		}

		VisibilityShell corpseShell = null;// Try assuming it's a unit with a
											// corpse; they'll tend to be that
											// way

		// Iterate through new visibility sources, find a geoset with gutz
		// material
		for (int i = 0; i < visSourcesNew.size() && corpseShell == null; i++) {
			if (visSourcesNew.get(i) instanceof VisibilityShell) {
				final VisibilityShell vs = (VisibilityShell) visSourcesNew.get(i);
				if (vs.source instanceof Geoset) {
					final Geoset g = (Geoset) vs.source;
					if (g.getGeosetAnim() != null && g.getMaterial().firstLayer().firstTexture().getPath().toLowerCase()
							.equals("textures\\gutz.blp")) {
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
				final VisibilityPane vp = visComponents.get(i);
				// JOptionPane.showMessageDialog(null,"Visibility component: "+i
				// +", "+vp.sourceShell.source.getName());
				if (vp.sourceShell.source instanceof Geoset) {
					// System.err.println("Geoset at index: "+i);
					final Geoset g = (Geoset) vp.sourceShell.source;
					// JOptionPane.showMessageDialog(null,"Source file geo tex:
					// "+g.material.firstLayer().firstTexture().getPath());
					if (g.getGeosetAnim() != null && g.getMaterial().firstLayer().firstTexture().getPath().toLowerCase()
							.equals("textures\\gutz.blp")) {
						vp.newSourcesBox.setSelectedItem(corpseShell);
						// JOptionPane.showMessageDialog(null,"Source file
						// corpse set at index "+i);
					}
				}
			}
		}

		if (!show) {
			okayButton.doClick();
		}

		// visComponents.get(0).newSourcesBox.setSelectedItem
	}

	// *********************Simple Import Functions****************
	public void animTransferPartTwo(final boolean singleAnimation, final Animation pickedAnim,
			final Animation visFromAnim, final boolean show) {
		// This should be an import from self
		uncheckAllGeos.doClick();
		uncheckAllAnims.doClick();
		uncheckAllBones.doClick();
		uncheckAllObjs.doClick();
		visibilityList();
		selSimButton.doClick();

		if (singleAnimation) {
			for (int i = 0; i < animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
				if (aniPanel.anim.getName().equals(visFromAnim.getName())) {
					aniPanel.doImport.doClick();
					aniPanel.importTypeBox.setSelectedItem(AnimPanel.TIMESCALE);

					for (int d = 0; d < existingAnims.getSize(); d++) {
						final AnimShell shell = (AnimShell) existingAnims.get(d);
						if (shell.anim.getName().equals(pickedAnim.getName())) {
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
			final VisibilityPane vp = visComponents.get(i);
			// JOptionPane.showMessageDialog(null,"Visibility component: "+i +",
			// "+vp.sourceShell.source.getName());
			vp.favorOld.doClick();
		}

		if (!show) {
			okayButton.doClick();
		}

		// visComponents.get(0).newSourcesBox.setSelectedItem
	}
}

class GeosetPanel extends JPanel implements ChangeListener {
	// Geoset/Skin panel for controlling materials and geosets
	DefaultListModel materials;
	JList materialList;
	JScrollPane materialListPane;
	JCheckBox doImport;
	JLabel geoTitle;
	JLabel materialText;
	EditableModel model;
	Geoset geoset;
	int index;
	boolean isImported;
	MaterialListCellRenderer renderer;

	public GeosetPanel(final boolean imported, // Is this Geoset an imported
												// one, or an original?
			final EditableModel model, final int geoIndex, // which geoset is this for?
			// (starts with 0)
			final DefaultListModel materials, final MaterialListCellRenderer renderer) {
		this.materials = materials;
		this.model = model;
		this.renderer = renderer;
		index = geoIndex;
		geoset = model.getGeoset(geoIndex);
		isImported = imported;

		geoTitle = new JLabel(model.getName() + " " + (index + 1));
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import this Geoset");
		doImport.setSelected(true);
		if (imported) {
			doImport.addChangeListener(this);
		} else {
			doImport.setEnabled(false);
		}

		materialText = new JLabel("Material:");
		// Header for materials list

		materialList = new JList(materials);
		materialList.setCellRenderer(renderer);
		materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		materialList.setSelectedValue(geoset.getMaterial(), true);

		materialListPane = new JScrollPane(materialList);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout
				.createSequentialGroup().addGap(8).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(geoTitle).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(doImport).addComponent(materialText).addComponent(materialListPane)))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(geoTitle).addGap(16).addComponent(doImport)
				.addComponent(materialText).addComponent(materialListPane));
		setLayout(layout);
	}

	@Override
	public void paintComponent(final Graphics g) {
		renderer.setMaterial(geoset.getMaterial());
		super.paintComponent(g);
	}

	public void setSelected(final boolean flag) {
		if (isImported) {
			doImport.setSelected(flag);
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		materialText.setEnabled(doImport.isSelected());
		materialList.setEnabled(doImport.isSelected());
		materialListPane.setEnabled(doImport.isSelected());

		getImportPanel().informGeosetVisibility(geoset, doImport.isSelected());
	}

	ImportPanel impPanel;

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while (temp != null && temp.getClass() != ImportPanel.class) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}

	public Material getSelectedMaterial() {
		return (Material) materialList.getSelectedValue();
	}
}

class MaterialListCellRenderer extends DefaultListCellRenderer {
	EditableModel myModel;
	Object myMaterial;
	Font theFont = new Font("Arial", Font.BOLD, 32);
	HashMap<Material, ImageIcon> map = new HashMap<>();

	public MaterialListCellRenderer(final EditableModel model) {
		myModel = model;
	}

	public void setMaterial(final Object o) {
		myMaterial = o;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		String name = ((Material) value).getName();
		if (value == myMaterial) {
			name = name + " (Original)";
		}
		if (myModel.contains((Material) value)) {
			super.getListCellRendererComponent(list, name, index, iss, chf);

			ImageIcon myIcon = map.get(value);
			if (myIcon == null) {
				myIcon = new ImageIcon(Material.mergeImageScaled(ImportPanel.greenIcon.getImage(),
						((Material) value).getBufferedImage(myModel.getWrappedDataSource()), 64, 64, 48, 48));
				map.put((Material) value, myIcon);
			}

			setIcon(myIcon);
		} else {
			super.getListCellRendererComponent(list, "Import: " + name, index, iss, chf);
			ImageIcon myIcon = map.get(value);
			if (myIcon == null) {
				myIcon = new ImageIcon(Material.mergeImageScaled(ImportPanel.orangeIcon.getImage(),
						((Material) value).getBufferedImage(myModel.getWrappedDataSource()), 64, 64, 48, 48));
				map.put((Material) value, myIcon);
			}

			setIcon(myIcon);
		}
		setFont(theFont);
		return this;
	}
}

class AnimPanel extends JPanel implements ChangeListener, ItemListener, ListSelectionListener {
	// Animation panel for controlling which are imported

	// title
	JLabel title;

	// Import option
	JCheckBox doImport;

	// Import option
	JCheckBox inReverse;

	// The animation for this panel
	Animation anim;

	static final String IMPORTBASIC = "Import as-is";
	static final String CHANGENAME = "Change name to:";
	static final String TIMESCALE = "Time-scale into pre-existing:";
	static final String GLOBALSEQ = "Rebuild as global sequence";

	String[] animOptions = { IMPORTBASIC, CHANGENAME, TIMESCALE, GLOBALSEQ };

	JComboBox importTypeBox = new JComboBox(animOptions);

	JPanel cardPane = new JPanel();

	JPanel blankCardImp = new JPanel();
	JPanel blankCardGS = new JPanel();

	JPanel nameCard = new JPanel();
	JTextField newNameEntry = new JTextField("", 40);

	JPanel animListCard = new JPanel();
	DefaultListModel existingAnims;
	DefaultListModel listModel;
	JList animList;
	JScrollPane animListPane;

	public AnimPanel(final Animation anim, final DefaultListModel existingAnims, final AnimListCellRenderer renderer) {
		this.existingAnims = existingAnims;
		listModel = new DefaultListModel();
		for (int i = 0; i < existingAnims.size(); i++) {
			listModel.addElement(existingAnims.get(i));
		}
		this.anim = anim;

		title = new JLabel(anim.getName());
		title.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import this Sequence");
		doImport.setSelected(true);
		doImport.addChangeListener(this);

		inReverse = new JCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addChangeListener(this);

		importTypeBox.setEditable(false);
		importTypeBox.addItemListener(this);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		// Restricts users to pre-existing choices,
		// they cannot enter text in the box
		// (I think? that's an untested guess)

		// Combo box items:
		newNameEntry.setText(anim.getName());
		nameCard.add(newNameEntry);

		animList = new JList(listModel);
		animList.setCellRenderer(renderer);
		animList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// Use getSelectedValuesList().toArray() to request an array of selected
		// animations

		// Select any animation found that has the same name automatically
		// -- This iterates through the list of old animations and picks out
		// and like-named ones, so that the default selection is any animation
		// with the same name
		// (although this should stop after the first one is picked)
		animList.addListSelectionListener(this);
		for (int i = 0; i < existingAnims.size() && animList.getSelectedIndex() == -1; i++) {
			final Animation iAnim = ((AnimShell) listModel.get(i)).anim;
			if (iAnim.getName().toLowerCase().equals(anim.getName().toLowerCase())) {
				animList.setSelectedValue(listModel.get(i), true);
			}
		}

		animListPane = new JScrollPane(animList);
		animListCard.add(animListPane);

		final CardLayout cardLayout = new CardLayout();
		cardPane.setLayout(cardLayout);
		cardPane.add(blankCardImp, IMPORTBASIC);
		cardPane.add(nameCard, CHANGENAME);
		cardPane.add(animListPane, TIMESCALE);
		cardPane.add(blankCardGS, GLOBALSEQ);
		// cardLayout.show(cardPane,IMPORTBASIC);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(8).addGroup(layout
				.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(doImport)
						.addComponent(inReverse)
						.addGroup(layout.createSequentialGroup().addComponent(importTypeBox).addComponent(cardPane))))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16).addComponent(doImport)
				.addComponent(inReverse).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(importTypeBox).addComponent(cardPane)));
		setLayout(layout);
	}

	public void setSelected(final boolean flag) {
		doImport.setSelected(flag);
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		importTypeBox.setEnabled(doImport.isSelected());
		cardPane.setEnabled(doImport.isSelected());
		animList.setEnabled(doImport.isSelected());
		newNameEntry.setEnabled(doImport.isSelected());
		updateSelectionPicks();

	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
		// --
		// http://docs.oracle.com/javase/tutorial/uiswing/examples/layout/CardLayoutDemoProject/src/layout/CardLayoutDemo.java
		// -- http://docs.oracle.com/javase/tutorial/uiswing/layout/card.html
		// Thanks to the CardLayoutDemo.java at the above urls
		// in the JavaDocs for the example use of a CardLayout
		final CardLayout myLayout = (CardLayout) cardPane.getLayout();
		myLayout.show(cardPane, (String) e.getItem());
		updateSelectionPicks();
	}

	Object[] oldSelection = new Object[0];

	public void updateSelectionPicks() {
		listenSelection = false;
		// DefaultListModel newModel = new DefaultListModel();
		final Object[] selection = animList.getSelectedValuesList().toArray();
		listModel.clear();
		for (int i = 0; i < existingAnims.size(); i++) {
			final Animation temp = ((AnimShell) existingAnims.get(i)).importAnim;
			if (temp == null || temp == anim) {
				listModel.addElement(existingAnims.get(i));
			}
		}
		// for( int i = 0; i < existingAnims.size(); i++ )
		// {
		// newModel.addElement(existingAnims.get(i));
		// }
		// existingAnims.clear();
		// for( int i = 0; i < order.size(); i++ )
		// {
		// Object o = order.get(i);
		// if( newModel.contains(o) )
		// {
		// existingAnims.addElement(o);
		// }
		// }
		final int[] indices = new int[selection.length];
		for (int i = 0; i < selection.length; i++) {
			indices[i] = listModel.indexOf(selection[i]);
		}
		animList.setSelectedIndices(indices);
		listenSelection = true;

		Object[] newSelection;
		if (doImport.isSelected() && importTypeBox.getSelectedIndex() == 2) {
			newSelection = animList.getSelectedValuesList().toArray();
		} else {
			newSelection = new Object[0];
		}
		// ImportPanel panel = getImportPanel();
		for (final Object a : oldSelection) {
			((AnimShell) a).setImportAnim(null);
		}
		for (final Object a : newSelection) {
			((AnimShell) a).setImportAnim(anim);
		}
		// panel.addAnimPicks(oldSelection,this);
		// panel.removeAnimPicks(newSelection,this);
		oldSelection = newSelection;
	}

	boolean listenSelection = true;

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (listenSelection && e.getValueIsAdjusting()) {
			updateSelectionPicks();
		}
	}

	public ImportPanel getImportPanel() {
		Container temp = getParent();
		while (temp.getClass() != ImportPanel.class && temp != null) {
			temp = temp.getParent();
		}
		return (ImportPanel) temp;
	}

	public void reorderToModel(final DefaultListModel order) {
		// listenSelection = false;
		// DefaultListModel newModel = new DefaultListModel();
		// for( int i = 0; i < order.size(); i++ )
		// {
		// Object o = order.get(i);
		// if( this.existingAnims.contains(o) )
		// {
		// newModel.addElement(o);
		// }
		// }
		// this.existingAnims = newModel;
		// animList.setModel(existingAnims);
		// int [] indices = new int[oldSelection.length];
		// for( int i = 0; i < oldSelection.length; i++ )
		// {
		// indices[i] = existingAnims.indexOf(oldSelection[i]);
		// }
		// animList.setSelectedIndices(indices);
		// listenSelection = true;

		// listenSelection = false;
		// DefaultListModel newModel = new DefaultListModel();
		// Object [] selection = animList.getSelectedValuesList().toArray();
		// for( int i = 0; i < existingAnims.size(); i++ )
		// {
		// newModel.addElement(existingAnims.get(i));
		// }
		// existingAnims.clear();
		// for( int i = 0; i < order.size(); i++ )
		// {
		// Object o = order.get(i);
		// if( newModel.contains(o) )
		// {
		// existingAnims.addElement(o);
		// }
		// }
		// int [] indices = new int[selection.length];
		// for( int i = 0; i < selection.length; i++ )
		// {
		// indices[i] = existingAnims.indexOf(selection[i]);
		// }
		// animList.setSelectedIndices(indices);
		// listenSelection = true;
	}
}

class AnimListCellRenderer extends DefaultListCellRenderer {
	public AnimListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((AnimShell) value).anim.getName(), index, iss, chf);
		setIcon(ImportPanel.animIcon);
		return this;
	}
}

class AnimShell {
	Animation anim;
	Animation importAnim;

	public AnimShell(final Animation anim) {
		this.anim = anim;
	}

	public void setImportAnim(final Animation a) {
		importAnim = a;
	}
}

class BonePanel extends JPanel implements ListSelectionListener, ActionListener {
	Bone bone;

	JLabel title;

	static final String IMPORT = "Import this bone";
	static final String MOTIONFROM = "Import motion to pre-existing:";
	static final String LEAVE = "Do not import";

	String[] impOptions = { IMPORT, MOTIONFROM, LEAVE };

	JComboBox importTypeBox = new JComboBox(impOptions);

	// List for which bone to transfer motion
	DefaultListModel existingBones;
	DefaultListModel listModel;
	JList boneList;
	JScrollPane boneListPane;
	JPanel cardPanel;
	CardLayout cards = new CardLayout();
	JPanel dummyPanel = new JPanel();
	DefaultListModel<BoneShell> futureBones;
	JList futureBonesList;
	JScrollPane futureBonesListPane;
	JLabel parentTitle;

	protected BonePanel() {
		// This constructor is negative mojo
	}

	public BonePanel(final Bone whichBone, final DefaultListModel existingBonesList, final ListCellRenderer renderer,
			final ImportPanel thePanel) {
		bone = whichBone;
		existingBones = existingBonesList;
		impPanel = thePanel;
		listModel = new DefaultListModel();
		for (int i = 0; i < existingBonesList.size(); i++) {
			listModel.addElement(existingBonesList.get(i));
		}

		title = new JLabel(bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		importTypeBox.setEditable(false);
//		importTypeBox.addItemListener(this);
		importTypeBox.addActionListener(this);
		importTypeBox.setMaximumSize(new Dimension(200, 20));

		boneList = new JList(listModel);
		boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		boneList.setCellRenderer(renderer);
		boneList.addListSelectionListener(this);
		boneListPane = new JScrollPane(boneList);
		for (int i = 0; i < listModel.size(); i++) {
			final BoneShell bs = (BoneShell) listModel.get(i);
			if (bs.bone.getName().equals(bone.getName()) && bs.importBone == null
					&& (!(bs.bone.getName().contains("Mesh") || bs.bone.getName().contains("Object")
							|| bs.bone.getName().contains("Box"))
							|| bs.bone.getPivotPoint().equalLocs(bone.getPivotPoint()))) {
				boneList.setSelectedValue(bs, true);
				bs.setImportBone(bone);
				i = listModel.size();
				// System.out.println("GREAT BALLS OF FIRE");
			}
		}

		futureBones = getImportPanel().getFutureBoneListExtended(true);
		futureBonesList = new JList(futureBones);
		futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		futureBonesList.setCellRenderer(renderer);
		futureBonesListPane = new JScrollPane(futureBonesList);
		if (bone.getParent() != null) {
			parentTitle = new JLabel("Parent:      (Old Parent: " + bone.getParent().getName() + ")");
		} else {
			parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
		}

		add(importTypeBox);
		add(boneListPane);
		cardPanel = new JPanel(cards);
		cardPanel.add(boneListPane, "boneList");
		cardPanel.add(dummyPanel, "blank");
		cards.show(cardPanel, "blank");

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(8)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
								.addGroup(layout.createSequentialGroup().addComponent(importTypeBox)
										.addComponent(cardPanel)
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(parentTitle).addComponent(futureBonesListPane))))
						.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16).addGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(importTypeBox).addComponent(cardPanel)
				.addGroup(layout.createSequentialGroup().addComponent(parentTitle).addComponent(futureBonesListPane))));
		setLayout(layout);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateSelectionPicks();
		final boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
	}

	public void initList() {
		futureBones = getImportPanel().getFutureBoneListExtended(false);
		for (int i = 0; i < futureBones.size(); i++) {
			final BoneShell bs = futureBones.get(i);
			if (bs.bone == bone.getParent()) {
				futureBonesList.setSelectedValue(bs, true);
			}
		}
	}

	ImportPanel impPanel;

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while (temp != null && temp.getClass() != ImportPanel.class) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}

	public int getSelectedIndex() {
		return importTypeBox.getSelectedIndex();
	}

	public void setSelectedIndex(final int index) {
		importTypeBox.setSelectedIndex(index);
	}

	public void setSelectedValue(final String value) {
		importTypeBox.setSelectedItem(value);
	}

	public void setParent(final BoneShell pick) {
		futureBonesList.setSelectedValue(pick, true);
	}

	Object[] oldSelection = new Object[0];

	public void updateSelectionPicks() {
		listenSelection = false;
		// DefaultListModel newModel = new DefaultListModel();
		final Object[] selection = boneList.getSelectedValuesList().toArray();
		listModel.clear();
		for (int i = 0; i < existingBones.size(); i++) {
			final Bone temp = ((BoneShell) existingBones.get(i)).importBone;
			if (temp == null || temp == bone) {
				listModel.addElement(existingBones.get(i));
			}
		}
		// for( int i = 0; i < existingAnims.size(); i++ )
		// {
		// newModel.addElement(existingAnims.get(i));
		// }
		// existingAnims.clear();
		// for( int i = 0; i < order.size(); i++ )
		// {
		// Object o = order.get(i);
		// if( newModel.contains(o) )
		// {
		// existingAnims.addElement(o);
		// }
		// }
		final int[] indices = new int[selection.length];
		for (int i = 0; i < selection.length; i++) {
			indices[i] = listModel.indexOf(selection[i]);
		}
		boneList.setSelectedIndices(indices);
		listenSelection = true;

		Object[] newSelection;
		if (importTypeBox.getSelectedIndex() == 1) {
			newSelection = boneList.getSelectedValuesList().toArray();
		} else {
			newSelection = new Object[0];
		}
		// ImportPanel panel = getImportPanel();
		for (final Object a : oldSelection) {
			((BoneShell) a).setImportBone(null);
		}
		for (final Object a : newSelection) {
			((BoneShell) a).setImportBone(bone);
		}
		// panel.addAnimPicks(oldSelection,this);
		// panel.removeAnimPicks(newSelection,this);
		oldSelection = newSelection;
		// Object [] newSelection;
		// if( importTypeBox.getSelectedIndex() == 1 )
		// {
		// newSelection = boneList.getSelectedValuesList().toArray();
		// }
		// else
		// {
		// newSelection = new Object[0];
		// }
		// ImportPanel panel = getImportPanel();
		// panel.addBonePicks(oldSelection,this);
		// panel.removeBonePicks(newSelection,this);
		// // panel.reorderBonePicks(this);
		// oldSelection = newSelection;
		final long nanoStart = System.nanoTime();
		futureBones = getImportPanel().getFutureBoneListExtended(false);
		final long nanoEnd = System.nanoTime();
		System.out.println("updating future bone list took " + (nanoEnd - nanoStart) + " ns");
	}

	// public void reorderToModel(DefaultListModel order)
	// {
	// listenSelection = false;
	// DefaultListModel newModel = new DefaultListModel();
	// Object [] selection = boneList.getSelectedValuesList().toArray();
	// for( int i = 0; i < existingBones.size(); i++ )
	// {
	// newModel.addElement(existingBones.get(i));
	// }
	// existingBones.clear();
	// for( int i = 0; i < order.size(); i++ )
	// {
	// Object o = order.get(i);
	// if( newModel.contains(o) )
	// {
	// existingBones.addElement(o);
	// }
	// }
	// int [] indices = new int[selection.length];
	// for( int i = 0; i < selection.length; i++ )
	// {
	// indices[i] = existingBones.indexOf(selection[i]);
	// }
	// boneList.setSelectedIndices(indices);
	// listenSelection = true;
	// }

	boolean listenSelection = true;

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (listenSelection && e.getValueIsAdjusting()) {
			updateSelectionPicks();
		}
	}
}

class MultiBonePanel extends BonePanel {
	JButton setAllParent;

	public MultiBonePanel(final DefaultListModel existingBonesList, final ListCellRenderer renderer) {
		setAllParent = new JButton("Set Parent for All");
		setAllParent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				getImportPanel().setParentMultiBones();
			}
		});
		bone = null;
		existingBones = existingBonesList;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(this);
		importTypeBox.setMaximumSize(new Dimension(200, 20));

		boneList = new JList(existingBones);
		boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		boneList.setCellRenderer(renderer);
		boneListPane = new JScrollPane(boneList);

		add(importTypeBox);
		add(boneListPane);
		cardPanel = new JPanel(cards);
		cardPanel.add(boneListPane, "boneList");
		cardPanel.add(dummyPanel, "blank");
		boneList.setEnabled(false);
		cards.show(cardPanel, "blank");

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(8)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
						.addGroup(layout.createSequentialGroup().addComponent(importTypeBox).addComponent(cardPanel)
								.addComponent(setAllParent)))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(importTypeBox)
						.addComponent(cardPanel).addComponent(setAllParent)));
		setLayout(layout);
	}

	@Override
	public ImportPanel getImportPanel() {
		Container temp = getParent();
		while (temp.getClass() != ImportPanel.class && temp != null) {
			temp = temp.getParent();
		}
		return (ImportPanel) temp;
	}

	@Override
	public void setSelectedIndex(final int index) {
		listenForChange = false;
		importTypeBox.setSelectedIndex(index);
		listenForChange = true;
	}

	boolean listenForChange = true;

	@Override
	public void setSelectedValue(final String value) {
		listenForChange = false;
		importTypeBox.setSelectedItem(value);
		listenForChange = true;
	}

	public void setMultiTypes() {
		listenForChange = false;
		importTypeBox.setEditable(true);
		importTypeBox.setSelectedItem("Multiple selected");
		importTypeBox.setEditable(false);
		// boneListPane.setVisible(false);
		// boneList.setVisible(false);
		cards.show(cardPanel, "blank");
		revalidate();
		listenForChange = true;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final long nanoStart = System.nanoTime();
		final boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
		if (listenForChange) {
			getImportPanel().setSelectedItem((String) importTypeBox.getSelectedItem());
		}
		final long nanoEnd = System.nanoTime();
		System.out.println("MultiBonePanel.actionPerformed() took " + (nanoEnd - nanoStart) + " ns");
	}
}

class BoneShellListCellRenderer extends AbstractSnapshottingListCellRenderer2D<BoneShell> {
	public BoneShellListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
		super(modelDisplay, otherDisplay);
	}

	@Override
	protected ResettableVertexFilter<BoneShell> createFilter() {
		return new BoneShellFilter();
	}

	@Override
	protected BoneShell valueToType(final Object value) {
		return (BoneShell) value;
	}

	@Override
	protected boolean contains(final ModelView modelDisp, final BoneShell object) {
		return modelDisp.getModel().contains(object.bone);
	}

	private static final class BoneShellFilter implements ResettableVertexFilter<BoneShell> {
		private BoneShell boneShell;

		@Override
		public boolean isAccepted(final GeosetVertex vertex) {
			return vertex.isLinked(boneShell.bone);
		}

		@Override
		public ResettableVertexFilter<BoneShell> reset(final BoneShell matrix) {
			boneShell = matrix;
			return this;
		}

	}

	@Override
	protected Vertex getRenderVertex(final BoneShell value) {
		return value.bone.getPivotPoint();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		setBackground(new Color(220, 180, 255));
		super.getListCellRendererComponent(list, value, index, iss, chf);
		setText(((BoneShell) value).toString()/*
												 * ((BoneShell) value).bone.getClass().getSimpleName() + " \"" +
												 * ((BoneShell) value).bone.getName() + "\""
												 */);
		// setIcon(new
		// ImageIcon(Material.mergeImageScaled(ImportPanel.boneIcon.getImage(),
		// ((ImageIcon) getIcon()).getImage(), 64, 64, 64, 64)));
		return this;
	}
}

class BoneListCellRenderer extends DefaultListCellRenderer {
	public BoneListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list,
				((Bone) value).getClass().getSimpleName() + " \"" + ((Bone) value).getName() + "\"", index, iss, chf);
		setIcon(ImportPanel.boneIcon);
		return this;
	}
}

class ParentToggleRenderer extends BoneShellListCellRenderer {
	JCheckBox toggleBox;

	public ParentToggleRenderer(final JCheckBox toggleBox, final ModelView currentModelDisp,
			final ModelView importedModelDisp) {
		super(currentModelDisp, importedModelDisp);
		this.toggleBox = toggleBox;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {

		final BoneShellListCellRenderer comp = (BoneShellListCellRenderer) super.getListCellRendererComponent(list,
				value, index, iss, chf);
		if (toggleBox.isSelected()) {
			if (((BoneShell) value).bone.getParent() != null) {
				comp.setText(value.toString() + "; " + ((BoneShell) value).bone.getParent().getName());
			} else {
				comp.setText(value.toString() + "; (no parent)");
			}
		} else {
			super.getListCellRendererComponent(list, value, index, iss, chf);
		}
		return this;
	}
}

class BonePanelListCellRenderer extends AbstractSnapshottingListCellRenderer2D<Bone> {
	public BonePanelListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
		super(modelDisplay, otherDisplay);
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		setBackground(new Color(200, 255, 255));
		super.getListCellRendererComponent(list, ((BonePanel) value).bone, index, iss, chf);
		setText(((BonePanel) value).bone.getClass().getSimpleName() + " \"" + ((BonePanel) value).bone.getName()
				+ "\"");
		// setIcon(ImportPanel.cyanIcon);
		// setIcon(new
		// ImageIcon(Material.mergeImageScaled(ImportPanel.cyanIcon.getImage(),
		// ((ImageIcon) getIcon()).getImage(), 64, 64, 64, 64)));
		return this;
	}

	@Override
	protected ResettableVertexFilter<Bone> createFilter() {
		return new ResettableVertexFilter<Bone>() {
			private Bone bone;

			@Override
			public boolean isAccepted(final GeosetVertex vertex) {
				return vertex.isLinked(bone);
			}

			@Override
			public ResettableVertexFilter<Bone> reset(final Bone bone) {
				this.bone = bone;
				return this;
			}
		};
	}

	@Override
	protected Bone valueToType(final Object value) {
		return (Bone) value;
	}

	@Override
	protected Vertex getRenderVertex(final Bone value) {
		return value.getPivotPoint();
	}

	@Override
	protected boolean contains(final ModelView modelDisp, final Bone object) {
		return modelDisp.getModel().contains(object);
	}
}

class ObjPanelListCellRenderer extends DefaultListCellRenderer {
	public ObjPanelListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((ObjectPanel) value).title.getText(), index, iss, chf);
		setIcon(ImportPanel.redIcon);
		return this;
	}
}

class GeosetAnimationPanel extends JTabbedPane {
	// Geoset Animation panel for controlling bone attachments and visibility
	EditableModel model;
	Geoset geoset;
	boolean isImported;
	int index;
	BoneAttachmentPane bap;
	VisibilityPane vp;

	public GeosetAnimationPanel(final boolean imported, // Is this Geoset an
														// imported one, or an
														// original?
			final EditableModel model, final int geoIndex, final ImportPanel thePanel)// which
	// geoset
	// is
	// this
	// for?
	// (starts
	// with
	// 0)
	{
		this.model = model;
		impPanel = thePanel;
		index = geoIndex;
		geoset = model.getGeoset(geoIndex);
		isImported = imported;

		bap = new BoneAttachmentPane(model, geoset, null, getImportPanel());
		addTab("Bones", ImportPanel.boneIcon, bap, "Allows you to edit bone references.");

		// vp = new
		// VisibilityPane(thePanel.currentModel.m_geosets.size(),thePanel.currentModel.getName(),thePanel.importedModel.m_geosets.size(),thePanel.importedModel.getName(),geoIndex);
		// addTab("Visibility",ImportPanel.animIcon,vp,"Allows you to edit
		// visibility.");
	}

	public void refreshLists() {
		bap.refreshLists();
	}

	ImportPanel impPanel;

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while (temp != null && temp.getClass() != ImportPanel.class) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}

class BoneAttachmentPane extends JPanel implements ActionListener, ListSelectionListener {
	JLabel title;

	// Old bone refs (matrices)
	JLabel oldBoneRefsLabel;
	DefaultListModel<MatrixShell> oldBoneRefs;
	JList oldBoneRefsList;
	JScrollPane oldBoneRefsPane;

	// New refs
	JLabel newRefsLabel;
	DefaultListModel<BoneShell> newRefs;
	JList newRefsList;
	JScrollPane newRefsPane;
	JButton removeNewRef;
	JButton moveUp;
	JButton moveDown;

	// Bones (all available -- NEW AND OLD)
	JLabel bonesLabel;
	DefaultListModel<BoneShell> bones;
	JList bonesList;
	JScrollPane bonesPane;
	JButton useBone;

	EditableModel model;
	Geoset geoset;

	public BoneAttachmentPane(final EditableModel model, final Geoset whichGeoset, final ListCellRenderer renderer,
			final ImportPanel thePanel) {
		this.model = model;
		geoset = whichGeoset;
		impPanel = thePanel;

		bonesLabel = new JLabel("Bones");
		updateBonesList();
		// Built before oldBoneRefs, so that the MatrixShells can default to
		// using New Refs with the same name as their first bone
		bonesList = new JList(bones);
		bonesList.setCellRenderer(renderer);
		bonesPane = new JScrollPane(bonesList);

		useBone = new JButton("Use Bone(s)", ImportPanel.greenArrowIcon);
		useBone.addActionListener(this);

		oldBoneRefsLabel = new JLabel("Old Bone References");
		buildOldRefsList();
		oldBoneRefsList = new JList(oldBoneRefs);
		oldBoneRefsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		oldBoneRefsList.setCellRenderer(new MatrixShell2DListCellRenderer(new ModelViewManager(impPanel.currentModel),
//				new ModelViewManager(impPanel.importedModel)));
		oldBoneRefsList.addListSelectionListener(this);
		oldBoneRefsPane = new JScrollPane(oldBoneRefsList);

		newRefsLabel = new JLabel("New Refs");
		newRefs = new DefaultListModel<>();
		newRefsList = new JList(newRefs);
		newRefsList.setCellRenderer(renderer);
		newRefsPane = new JScrollPane(newRefsList);

		removeNewRef = new JButton("Remove", ImportPanel.redXIcon);
		removeNewRef.addActionListener(this);
		moveUp = new JButton(ImportPanel.moveUpIcon);
		moveUp.addActionListener(this);
		moveDown = new JButton(ImportPanel.moveDownIcon);
		moveDown.addActionListener(this);

		buildLayout();

		refreshNewRefsList();
	}

	public void buildLayout() {
		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(oldBoneRefsLabel)
						.addComponent(oldBoneRefsPane))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(newRefsLabel)
						.addComponent(newRefsPane).addComponent(removeNewRef))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(moveUp)
						.addComponent(moveDown))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(bonesLabel)
						.addComponent(bonesPane).addComponent(useBone)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(oldBoneRefsLabel)
						.addComponent(newRefsLabel).addComponent(bonesLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(oldBoneRefsPane)
						.addComponent(newRefsPane)
						.addGroup(layout.createSequentialGroup().addComponent(moveUp).addGap(16).addComponent(moveDown))
						.addComponent(bonesPane))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(removeNewRef)
						.addComponent(useBone)));
		setLayout(layout);
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		refreshLists();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == useBone) {
			for (final Object o : bonesList.getSelectedValuesList()) {
				if (!newRefs.contains(o)) {
					newRefs.addElement((BoneShell) o);
				}
			}
			refreshNewRefsList();
		} else if (e.getSource() == removeNewRef) {
			for (final Object o : newRefsList.getSelectedValuesList()) {
				int i = newRefsList.getSelectedIndex();
				newRefs.removeElement(o);
				if (i > newRefs.size() - 1) {
					i = newRefs.size() - 1;
				}
				newRefsList.setSelectedIndex(i);
			}
			refreshNewRefsList();
		} else if (e.getSource() == moveUp) {
			final int[] indices = newRefsList.getSelectedIndices();
			if (indices != null && indices.length > 0) {
				if (indices[0] > 0) {
					for (int i = 0; i < indices.length; i++) {
						final BoneShell bs = newRefs.get(indices[i]);
						newRefs.removeElement(bs);
						newRefs.add(indices[i] - 1, bs);
						indices[i] -= 1;
					}
				}
				newRefsList.setSelectedIndices(indices);
			}
		} else if (e.getSource() == moveDown) {
			final int[] indices = newRefsList.getSelectedIndices();
			if (indices != null && indices.length > 0) {
				if (indices[indices.length - 1] < newRefs.size() - 1) {
					for (int i = indices.length - 1; i >= 0; i--) {
						final BoneShell bs = newRefs.get(indices[i]);
						newRefs.removeElement(bs);
						newRefs.add(indices[i] + 1, bs);
						indices[i] += 1;
					}
				}
				newRefsList.setSelectedIndices(indices);
			}
		}
	}

	public void refreshLists() {
		updateBonesList();
		refreshNewRefsList();
	}

	MatrixShell currentMatrix = null;

	public void refreshNewRefsList() {
		// Does save the currently constructed matrix
		final java.util.List selection = newRefsList.getSelectedValuesList();
		if (currentMatrix != null) {
			currentMatrix.newBones.clear();
			for (final Object bs : newRefs.toArray()) {
				currentMatrix.newBones.add((BoneShell) bs);
			}
		}
		newRefs.clear();
		if (oldBoneRefsList.getSelectedValue() != null) {
			for (final BoneShell bs : ((MatrixShell) oldBoneRefsList.getSelectedValue()).newBones) {
				if (bones.contains(bs)) {
					newRefs.addElement(bs);
				}
			}
		}

		final int[] indices = new int[selection.size()];
		for (int i = 0; i < selection.size(); i++) {
			indices[i] = newRefs.indexOf(selection.get(i));
		}
		newRefsList.setSelectedIndices(indices);
		currentMatrix = (MatrixShell) oldBoneRefsList.getSelectedValue();
	}

	public void reloadNewRefsList() {
		// Does not save the currently constructed matrix
		final java.util.List selection = newRefsList.getSelectedValuesList();
		newRefs.clear();
		if (oldBoneRefsList.getSelectedValue() != null) {
			for (final BoneShell bs : ((MatrixShell) oldBoneRefsList.getSelectedValue()).newBones) {
				if (bones.contains(bs)) {
					newRefs.addElement(bs);
				}
			}
		}

		final int[] indices = new int[selection.size()];
		for (int i = 0; i < selection.size(); i++) {
			indices[i] = newRefs.indexOf(selection.get(i));
		}
		newRefsList.setSelectedIndices(indices);
		currentMatrix = (MatrixShell) oldBoneRefsList.getSelectedValue();
	}

	public void buildOldRefsList() {
		if (oldBoneRefs == null) {
			oldBoneRefs = new DefaultListModel<>();
		} else {
			oldBoneRefs.clear();
		}
		for (final Matrix m : geoset.getMatrix()) {
			final MatrixShell ms = new MatrixShell(m);
			// For look to find similarly named stuff and add it
			for (final Object bs : bones.toArray()) {
				// try {
				for (final Bone b : m.getBones()) {
					final String mName = b.getName();
					if (((BoneShell) bs).bone == b)// .getName().equals(mName) )
					{
						ms.newBones.add((BoneShell) bs);
					}
				}
				// }
				// catch (NullPointerException e)
				// {
				// System.out.println("We have a null in a matrix process,
				// probably not good but it was assumed that this might
				// happen.");
				// }
			}
			oldBoneRefs.addElement(ms);
		}
	}

	public void resetMatrices() {
		for (int i = 0; i < oldBoneRefs.size(); i++) {
			final MatrixShell ms = oldBoneRefs.get(i);
			ms.newBones.clear();
			final Matrix m = ms.matrix;
			// For look to find right stuff and add it
			for (final Object bs : bones.toArray()) {
				for (final Bone b : m.getBones()) {
					if (((BoneShell) bs).bone == b)// .getName().equals(mName) )
					{
						ms.newBones.add((BoneShell) bs);
					}
				}
			}
		}
		reloadNewRefsList();
	}

	public void applySmartMapping(final Map<Bone, Bone> pairingMap) {
		final Map<Bone, BoneShell> boneToFutureBoneShell = new HashMap<>();

		final int s = bones.size();
		for (int i = 0; i < s; i++) {
			final BoneShell boneShell = bones.get(i);
			boneToFutureBoneShell.put(boneShell.bone, boneShell);
		}

		for (int i = 0; i < oldBoneRefs.size(); i++) {
			final MatrixShell ms = oldBoneRefs.get(i);
			ms.newBones.clear();
			final Matrix m = ms.matrix;

			for (final Bone b : m.getBones()) {
				final Bone partner = pairingMap.get(b);
				if (partner != null) {
					ms.newBones.add(boneToFutureBoneShell.get(partner));
				}
			}
		}
		reloadNewRefsList();
	}

	public BoneShell getBoneShellFromFutureBonesList(final Bone bone) {
		final int s = bones.size();
		for (int i = 0; i < s; i++) {
			final BoneShell boneShell = bones.get(i);
			if (boneShell.bone == bone) {
				return boneShell;
			}
		}
		return null;
	}

	public void setMatricesToSimilarNames() {
		for (int i = 0; i < oldBoneRefs.size(); i++) {
			final MatrixShell ms = oldBoneRefs.get(i);
			ms.newBones.clear();
			final Matrix m = ms.matrix;
			// For look to find similarly named stuff and add it
			for (final Object bs : bones.toArray()) {
				for (final Bone b : m.getBones()) {
					final String mName = b.getName();
					if (((BoneShell) bs).bone.getName().equals(mName)) {
						ms.newBones.add((BoneShell) bs);
					}
				}
			}
		}
		reloadNewRefsList();
	}

	public void updateBonesList() {
		bones = getImportPanel().getFutureBoneList();
	}

	ImportPanel impPanel;

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while (temp != null && temp.getClass() != ImportPanel.class) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}

class ObjectPanel extends JPanel {
	JLabel title;

	IdObject object;
	Camera camera;
	JCheckBox doImport;
	JLabel parentLabel;
	JLabel oldParentLabel;
	DefaultListModel<BoneShell> parents;
	JList parentsList;
	JScrollPane parentsPane;

	protected ObjectPanel() {

	}

	public ObjectPanel(final IdObject whichObject, final DefaultListModel<BoneShell> possibleParents) {
		object = whichObject;

		title = new JLabel(object.getClass().getSimpleName() + " \"" + object.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		parentLabel = new JLabel("Parent:");
		if (object.getParent() != null) {
			oldParentLabel = new JLabel("(Old Parent: " + object.getParent().getName() + ")");
		} else {
			oldParentLabel = new JLabel("(Old Parent: {no parent})");
		}

		parents = possibleParents;
		parentsList = new JList(parents);
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i = 0; i < parents.size(); i++) {
			final BoneShell bs = parents.get(i);
			if (bs.bone == object.getParent()) {
				parentsList.setSelectedValue(bs, true);
			}
		}

		parentsPane = new JScrollPane(parentsList);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(doImport)
						.addComponent(oldParentLabel)
						.addGroup(layout.createSequentialGroup().addComponent(parentLabel).addComponent(parentsPane))));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16).addComponent(doImport)
				.addComponent(oldParentLabel).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(parentLabel).addComponent(parentsPane)));
		setLayout(layout);
	}

	public ObjectPanel(final Camera c) {
		camera = c;

		title = new JLabel(c.getClass().getSimpleName() + " \"" + c.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		parentLabel = new JLabel("Parent:");
		oldParentLabel = new JLabel("(Cameras don't have parents)");

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(doImport)
						.addComponent(oldParentLabel)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16).addComponent(doImport)
				.addComponent(oldParentLabel));
		setLayout(layout);
	}
}

class MultiObjectPanel extends ObjectPanel implements ChangeListener {
	public MultiObjectPanel(final DefaultListModel<BoneShell> possibleParents) {
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import these objects (click to apply to all)");
		doImport.setSelected(true);
		doImport.addChangeListener(this);
		parentLabel = new JLabel("Parent:");
		oldParentLabel = new JLabel("(Old parent can only be displayed for a single object)");

		parents = possibleParents;
		parentsList = new JList(parents);
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentsPane = new JScrollPane(parentsList);
		parentsPane.setEnabled(false);
		parentsList.setEnabled(false);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(doImport)
						.addComponent(oldParentLabel)
						.addGroup(layout.createSequentialGroup().addComponent(parentLabel).addComponent(parentsPane))));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16).addComponent(doImport)
				.addComponent(oldParentLabel).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(parentLabel).addComponent(parentsPane)));
		setLayout(layout);
	}

	boolean oldVal = true;

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (doImport.isSelected() != oldVal) {
			getImportPanel().setObjGroupSelected(doImport.isSelected());
			oldVal = doImport.isSelected();
		}
	}

	ImportPanel impPanel;

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while (temp != null && temp.getClass() != ImportPanel.class) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}

class VisibilityShell {
	Named source;
	EditableModel model;

	public VisibilityShell(final Named n, final EditableModel whichModel) {
		source = n;
		model = whichModel;
	}
}

class VisPaneListCellRenderer extends DefaultListCellRenderer {
	EditableModel current;

	public VisPaneListCellRenderer(final EditableModel whichModel) {
		current = whichModel;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((VisibilityPane) value).sourceShell.model.getName() + ": "
				+ ((VisibilityPane) value).sourceShell.source.getName(), index, iss, chf);
		if (current == ((VisibilityPane) value).sourceShell.model) {
			setIcon(ImportPanel.greenIcon);
		} else {
			setIcon(ImportPanel.orangeIcon);
		}
		return this;
	}
}

class VisShellBoxCellRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer {
	public VisShellBoxCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		if (value == null) {
			return getListCellRendererComponent(list, "NULL ERROR", index, iss, chf);
		}
		if (value.getClass() != String.class) {
			super.getListCellRendererComponent(list,
					((VisibilityShell) value).model.getName() + ": " + ((VisibilityShell) value).source.getName(),
					index, iss, chf);
		} else {
			super.getListCellRendererComponent(list, value, index, iss, chf);
		}
		return this;
	}
}

class VisibilityPane extends JPanel {
	static final String NOTVISIBLE = "Not visible";
	static final String VISIBLE = "Always visible";
	JLabel oldAnimsLabel;
	JComboBox oldSourcesBox;
	JLabel newAnimsLabel;
	JComboBox newSourcesBox;
	JCheckBox favorOld;
	VisibilityShell sourceShell;

	JLabel title;

	protected VisibilityPane() {
		// for use in multi pane
	}

	public VisibilityPane(final VisibilityShell sourceShell, final DefaultComboBoxModel oldSources,
			final DefaultComboBoxModel newSources, final ListCellRenderer renderer) {
		this.sourceShell = sourceShell;
		title = new JLabel(sourceShell.model.getName() + ": " + sourceShell.source.getName());
		title.setFont(new Font("Arial", Font.BOLD, 26));

		oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		oldSourcesBox = new JComboBox(oldSources);
		oldSourcesBox.setEditable(false);
		oldSourcesBox.setMaximumSize(new Dimension(1000, 25));
		oldSourcesBox.setRenderer(renderer);
		boolean didContain = false;
		for (int i = 0; i < oldSources.getSize() && !didContain; i++) {
			if (sourceShell == oldSources.getElementAt(i)) {
				didContain = true;
			}
		}
		if (didContain) {
			oldSourcesBox.setSelectedItem(sourceShell);
		} else {
			oldSourcesBox.setSelectedItem(VISIBLE);
		}

		newAnimsLabel = new JLabel("Imported animation visibility from: ");
		newSourcesBox = new JComboBox(newSources);
		newSourcesBox.setEditable(false);
		newSourcesBox.setMaximumSize(new Dimension(1000, 25));
		newSourcesBox.setRenderer(renderer);
		didContain = false;
		for (int i = 0; i < newSources.getSize() && !didContain; i++) {
			if (sourceShell == newSources.getElementAt(i)) {
				didContain = true;
			}
		}
		if (didContain) {
			newSourcesBox.setSelectedItem(sourceShell);
		} else {
			newSourcesBox.setSelectedItem(VISIBLE);
		}

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(oldAnimsLabel)
						.addComponent(oldSourcesBox).addComponent(newAnimsLabel).addComponent(newSourcesBox)
						.addComponent(favorOld)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16)
				.addComponent(oldAnimsLabel).addComponent(oldSourcesBox).addComponent(newAnimsLabel)
				.addComponent(newSourcesBox).addComponent(favorOld));
		setLayout(layout);
	}

	public void selectSimilarOptions() {
		final VisibilityShell temp = null;
		final ListModel oldSources = oldSourcesBox.getModel();
		for (int i = 0; i < oldSources.getSize(); i++) {
			if (!(oldSources.getElementAt(i) instanceof String)) {
				if (sourceShell.source.getName()
						.equals(((VisibilityShell) oldSources.getElementAt(i)).source.getName())) {
					System.out.println(sourceShell.source.getName());
					oldSourcesBox.setSelectedItem(oldSources.getElementAt(i));
				}
			}
		}
		final ListModel newSources = newSourcesBox.getModel();
		for (int i = 0; i < newSources.getSize(); i++) {
			if (!(newSources.getElementAt(i) instanceof String)) {
				if (sourceShell.source.getName()
						.equals(((VisibilityShell) newSources.getElementAt(i)).source.getName())) {
					System.out.println(sourceShell.source.getName());
					newSourcesBox.setSelectedItem(newSources.getElementAt(i));
				}
			}
		}
	}
}

class MultiVisibilityPane extends VisibilityPane implements ChangeListener, ItemListener {
	public MultiVisibilityPane(final DefaultComboBoxModel oldSources, final DefaultComboBoxModel newSources,
			final ListCellRenderer renderer) {
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		oldAnimsLabel = new JLabel("Existing animation visibility from: ");
		oldSourcesBox = new JComboBox(oldSources);
		oldSourcesBox.setEditable(false);
		oldSourcesBox.setMaximumSize(new Dimension(1000, 25));
		oldSourcesBox.setRenderer(renderer);
		oldSourcesBox.addItemListener(this);

		newAnimsLabel = new JLabel("Imported animation visibility from: ");
		newSourcesBox = new JComboBox(newSources);
		newSourcesBox.setEditable(false);
		newSourcesBox.setMaximumSize(new Dimension(1000, 25));
		newSourcesBox.setRenderer(renderer);
		newSourcesBox.addItemListener(this);

		favorOld = new JCheckBox("Favor component's original visibility when combining");
		favorOld.setSelected(true);
		favorOld.addChangeListener(this);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(title)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(oldAnimsLabel)
						.addComponent(oldSourcesBox).addComponent(newAnimsLabel).addComponent(newSourcesBox)
						.addComponent(favorOld)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(title).addGap(16)
				.addComponent(oldAnimsLabel).addComponent(oldSourcesBox).addComponent(newAnimsLabel)
				.addComponent(newSourcesBox).addComponent(favorOld));
		setLayout(layout);
	}

	boolean oldVal = true;

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (favorOld.isSelected() != oldVal) {
			getImportPanel().setVisGroupSelected(favorOld.isSelected());
			oldVal = favorOld.isSelected();
		}
	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
		if (e.getSource() == oldSourcesBox) {
			getImportPanel().setVisGroupItemOld(oldSourcesBox.getSelectedItem());
		}
		if (e.getSource() == newSourcesBox) {
			getImportPanel().setVisGroupItemNew(newSourcesBox.getSelectedItem());
		}
	}

	public void setMultipleOld() {
		oldSourcesBox.setEditable(true);
		oldSourcesBox.setSelectedItem("Multiple selected");
		oldSourcesBox.setEditable(false);
	}

	public void setMultipleNew() {
		newSourcesBox.setEditable(true);
		newSourcesBox.setSelectedItem("Multiple selected");
		newSourcesBox.setEditable(false);
	}

	ImportPanel impPanel;

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while (temp != null && temp.getClass() != ImportPanel.class) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}
}