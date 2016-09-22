package com.matrixeater.src;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
/**
 * The panel to handle the import function.
 * 
 * Eric Theller 
 * 6/11/2012
 */
public class ImportPanel extends JTabbedPane implements ActionListener, ListSelectionListener, ChangeListener
{
    static final ImageIcon animIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/anim_small.png"));
    static final ImageIcon boneIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Bone_small.png"));
    static final ImageIcon geoIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/geo_small.png"));
    static final ImageIcon objIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Obj_small.png"));
    static final ImageIcon greenIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Blank_small.png"));
    static final ImageIcon redIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankRed_small.png"));
    static final ImageIcon orangeIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankOrange_small.png"));
    static final ImageIcon cyanIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankCyan_small.png"));
    static final ImageIcon redXIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/redX.png"));
    static final ImageIcon greenArrowIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/greenArrow.png"));
    static final ImageIcon moveUpIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/moveUp.png"));
    static final ImageIcon moveDownIcon = new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/moveDown.png"));
    
    JFrame frame;
    
    MDL currentModel;
    MDL importedModel;
    
    //Geosets
    JPanel geosetsPanel = new JPanel();
    JButton importAllGeos, uncheckAllGeos;
    JTabbedPane geosetTabs = new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
    
    //Animation
    JPanel animPanel = new JPanel();
    JButton importAllAnims, uncheckAllAnims;
    JCheckBox clearExistingAnims;
    JTabbedPane animTabs = new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
    DefaultListModel existingAnims;
    
    //Bones
    JPanel bonesPanel = new JPanel();
    JButton importAllBones, uncheckAllBones, motionFromBones, uncheckUnusedBones;
    JCheckBox clearExistingBones;
    //JTabbedPane boneTabs = new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
    DefaultListModel<BonePanel> bonePanels = new DefaultListModel<BonePanel>();
    JList boneTabs = new JList(bonePanels);
    JScrollPane boneTabsPane = new JScrollPane(boneTabs);
//     DefaultListModel<BonePanel> oldBonePanels = new DefaultListModel<BonePanel>();
//     JList oldBoneTabs = new JList(oldBonePanels);
//     JScrollPane oldBoneTabsPane = new JScrollPane(oldBoneTabs);
    CardLayout boneCardLayout = new CardLayout();
    JPanel bonePanelCards = new JPanel(boneCardLayout);
    JPanel blankPane = new JPanel();
    MultiBonePanel multiBonePane;
    DefaultListModel existingBones;
    
    
    //Matrices
    JPanel geosetAnimPanel = new JPanel();
    JTabbedPane geosetAnimTabs = new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
    
    DefaultListModel<BoneShell> futureBoneList = new DefaultListModel<BoneShell>();
    ArrayList<BoneShell> oldBones;
    ArrayList<BoneShell> newBones;
    
    JCheckBox displayParents = new JCheckBox("Display parent names");
    JButton allMatrOriginal = new JButton("Reset all Matrices"), allMatrSameName = new JButton("Set all to available, original names");
    
    //Objects
    JPanel objectsPanel = new JPanel();
//     JTabbedPane objectTabs = new JTabbedPane(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
    DefaultListModel<ObjectPanel> objectPanels = new DefaultListModel<ObjectPanel>();
    JList objectTabs = new JList(objectPanels);
    JScrollPane objectTabsPane = new JScrollPane(objectTabs);
    CardLayout objectCardLayout = new CardLayout();
    JPanel objectPanelCards = new JPanel(objectCardLayout);
    MultiObjectPanel multiObjectPane;
    
    JButton importAllObjs, uncheckAllObjs;
    
    //Visibility
    JPanel visPanel = new JPanel();
//     DefaultListModel<VisibilityPane> visPanels = new DefaultListModel<VisibilityPane>();
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
    public ImportPanel(MDL a, MDL b)
    {
    	this(a, b, true);
    }
    
    public ImportPanel(MDL currentModel, MDL importedModel, boolean visibleOnStart)
    {
        super();
        if( currentModel.getName().equals(importedModel.getName()) )
        {
            importedModel.setFile(new File(importedModel.getFile().getParent()+"/"+importedModel.getName()+ " (Imported)"+".mdl"));
            frame = new JFrame("Importing "+currentModel.getName() +" into itself");
        }
        else
        {
            frame = new JFrame("Importing "+importedModel.getName()+" into "+currentModel.getName());
        }
        currentModel.doSavePreps();
        try {
            frame.setIconImage(MainPanel.MDLIcon.getImage());
        }
        catch( Exception e )
        {
            JOptionPane.showMessageDialog(null,"Error: Image files were not found! Due to bad programming, this might break the program!");
        }
        this.currentModel = currentModel;
        this.importedModel = importedModel;
        
        //Geoset Panel
        addTab("Geosets",geoIcon,geosetsPanel,"Controls which geosets will be imported.");
        
        DefaultListModel materials = new DefaultListModel();
        for( int i = 0; i < currentModel.m_materials.size(); i++ )
        {
            materials.addElement(currentModel.m_materials.get(i));
        }
        for( int i = 0; i < importedModel.m_materials.size(); i++ )
        {
            materials.addElement(importedModel.m_materials.get(i));
        }
        //A list of all materials available for use during this import, in
        // the form of a DefaultListModel
        
        MaterialListCellRenderer materialsRenderer = new MaterialListCellRenderer(currentModel);
        //All material lists will know which materials come from the out-of-model source (imported model)
        
        //Build the geosetTabs list of GeosetPanels
        for( int i = 0; i < currentModel.m_geosets.size(); i++ )
        {
            GeosetPanel geoPanel = new GeosetPanel( false, currentModel, i, materials, materialsRenderer );
            
            geosetTabs.addTab(currentModel.getName()+" "+(i+1),greenIcon,geoPanel,"Click to modify material data for this geoset.");
        }
        for( int i = 0; i < importedModel.m_geosets.size(); i++ )
        {
            GeosetPanel geoPanel = new GeosetPanel( true, importedModel, i, materials, materialsRenderer );
            
            geosetTabs.addTab(importedModel.getName()+" "+(i+1),orangeIcon,geoPanel,"Click to modify importing and material data for this geoset.");
        }
        
        importAllGeos = new JButton("Import All");
        importAllGeos.addActionListener(this);
        geosetsPanel.add(importAllGeos);
        
        uncheckAllGeos = new JButton("Leave All");
        uncheckAllGeos.addActionListener(this);
        geosetsPanel.add(uncheckAllGeos);
        
        GroupLayout geosetLayout = new GroupLayout(geosetsPanel);
        geosetLayout.setHorizontalGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(geosetLayout.createSequentialGroup()
                .addComponent(importAllGeos)
                .addGap(8)
                .addComponent(uncheckAllGeos)
            )
            .addComponent(geosetTabs)
        );
        geosetLayout.setVerticalGroup(geosetLayout.createSequentialGroup()
            .addGroup(geosetLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(importAllGeos)
                .addComponent(uncheckAllGeos)
            )
            .addGap(8)
            .addComponent(geosetTabs)
        );
        geosetsPanel.setLayout(geosetLayout);
        
        
        //Animation Panel
        addTab("Animation",animIcon,animPanel,"Controls which animations will be imported.");
        
        existingAnims = new DefaultListModel();
        for( int i = 0; i < currentModel.m_anims.size(); i++ )
        {
            existingAnims.addElement(new AnimShell(currentModel.m_anims.get(i)));
        }
        
        AnimListCellRenderer animsRenderer = new AnimListCellRenderer();
        
        importAllAnims = new JButton("Import All");
        importAllAnims.addActionListener(this);
        animPanel.add(importAllAnims);
        
        uncheckAllAnims = new JButton("Leave All");
        uncheckAllAnims.addActionListener(this);
        animPanel.add(uncheckAllAnims);
        
        clearExistingAnims = new JCheckBox("Clear pre-existing animations");
        
        
        //Build the animTabs list of AnimPanels
        for( int i = 0; i < importedModel.m_anims.size(); i++ )
        {
            Animation anim = importedModel.getAnim(i);
            AnimPanel iAnimPanel = new AnimPanel( anim, existingAnims, animsRenderer );
            
            animTabs.addTab(anim.getName(),orangeIcon,iAnimPanel,"Click to modify data for this animation sequence.");
        }
        animTabs.addChangeListener(this);
        
        animPanel.add(clearExistingAnims);
        animPanel.add(animTabs);
        
        GroupLayout animLayout = new GroupLayout(animPanel);
        animLayout.setHorizontalGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(animLayout.createSequentialGroup()
                .addComponent(importAllAnims)
                .addGap(8)
                .addComponent(uncheckAllAnims)
            )
            .addComponent(clearExistingAnims)
            .addComponent(animTabs)
        );
        animLayout.setVerticalGroup(animLayout.createSequentialGroup()
            .addGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(importAllAnims)
                .addComponent(uncheckAllAnims)
            )
            .addComponent(clearExistingAnims)
            .addGap(8)
            .addComponent(animTabs)
        );
        animPanel.setLayout(animLayout);
        
        //Bone Panel
        addTab("Bones", boneIcon, bonesPanel, "Controls which bones will be imported.");
        existingBones = new DefaultListModel();
        ArrayList<Bone> currentMDLBones = currentModel.sortedIdObjects(Bone.class);
        ArrayList<Helper> currentMDLHelpers = currentModel.sortedIdObjects(Helper.class);
        for( int i = 0; i < currentMDLBones.size(); i++ )
        {
            existingBones.addElement(new BoneShell(currentMDLBones.get(i)));
        }
        for( int i = 0; i < currentMDLHelpers.size(); i++ )
        {
            existingBones.addElement(new BoneShell(currentMDLHelpers.get(i)));
        }
        BoneShellListCellRenderer boneRenderer = new BoneShellListCellRenderer();
        
        ArrayList<Bone> importedMDLBones = importedModel.sortedIdObjects(Bone.class);
        ArrayList<Helper> importedMDLHelpers = importedModel.sortedIdObjects(Helper.class);
        
        clearExistingBones = new JCheckBox("Clear pre-existing bones and helpers");
        //Initialized up here for use with BonePanels
        
        BonePanelListCellRenderer bonePanelRenderer = new BonePanelListCellRenderer();
        for( int i = 0; i < importedMDLBones.size(); i++ )
        {
            Bone b = importedMDLBones.get(i);
            BonePanel bonePanel = new BonePanel(b,existingBones,boneRenderer,this);
//             boneTabs.addTab(b.getClass().getName() + " \"" + b.getName() + "\"", cyanIcon, bonePanel, "Controls import settings for this bone.");;
            bonePanelCards.add(bonePanel,i+"");//(bonePanel.title.getText()));
            bonePanels.addElement(bonePanel);
        }
        for( int i = 0; i < importedMDLHelpers.size(); i++ )
        {
            Bone b = importedMDLHelpers.get(i);
            BonePanel bonePanel = new BonePanel(b,existingBones,boneRenderer,this);
//             boneTabs.addTab(b.getClass().getName() + " \"" + b.getName() + "\"", cyanIcon, bonePanel, "Controls import settings for this bone.");;
            bonePanelCards.add(bonePanel,importedMDLBones.size()+i+"");//(bonePanel.title.getText()));
            bonePanels.addElement(bonePanel);
        }
        for( int i = 0; i < bonePanels.size(); i++ )
        {
            bonePanels.get(i).initList();
        }
        multiBonePane = new MultiBonePanel(existingBones,boneRenderer);
        bonePanelCards.add(blankPane,"blank");
        bonePanelCards.add(multiBonePane,"multiple");
        boneTabs.setCellRenderer(bonePanelRenderer);
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
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,boneTabsPane,bonePanelCards);
        
        GroupLayout boneLayout = new GroupLayout(bonesPanel);
        boneLayout.setHorizontalGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(boneLayout.createSequentialGroup()
                .addComponent(importAllBones)
                .addGap(8)
                .addComponent(motionFromBones)
                .addGap(8)
                .addComponent(uncheckUnusedBones)
                .addGap(8)
                .addComponent(uncheckAllBones)
            )
            .addComponent(clearExistingBones)
            .addComponent(splitPane)
//             .addGroup(boneLayout.createSequentialGroup()
//                 .addComponent(boneTabsPane)
//                 .addComponent(bonePanelCards)
//             )
        );
        boneLayout.setVerticalGroup(boneLayout.createSequentialGroup()
            .addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(importAllBones)
                .addComponent(motionFromBones)
                .addComponent(uncheckUnusedBones)
                .addComponent(uncheckAllBones)
            )
            .addComponent(clearExistingBones)
            .addGap(8)
            .addComponent(splitPane)
//             .addGroup(boneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                 .addComponent(boneTabsPane)
//                 .addComponent(bonePanelCards)
//             )
        );
        bonesPanel.setLayout(boneLayout);
        
        //Matrices Panel
        addTab("Matrices",greenIcon,geosetAnimPanel,"Controls which bones geosets are attached to.");
        
        ParentToggleRenderer ptr = new ParentToggleRenderer(displayParents);
        
        displayParents.addChangeListener(this);
        
        allMatrOriginal.addActionListener(this);
        allMatrSameName.addActionListener(this);
        
        //Build the geosetAnimTabs list of GeosetPanels
        for( int i = 0; i < currentModel.m_geosets.size(); i++ )
        {
            BoneAttachmentPane geoPanel = new BoneAttachmentPane( currentModel, currentModel.getGeoset(i), ptr, this );
            
            geosetAnimTabs.addTab(currentModel.getName()+" "+(i+1),greenIcon,geoPanel,"Click to modify animation data for Geoset "+i+" from "+currentModel.getName()+".");
        }
        for( int i = 0; i < importedModel.m_geosets.size(); i++ )
        {
            BoneAttachmentPane geoPanel = new BoneAttachmentPane( importedModel, importedModel.getGeoset(i), ptr, this );
            
            geosetAnimTabs.addTab(importedModel.getName()+" "+(i+1),orangeIcon,geoPanel,"Click to modify animation data for Geoset "+i+" from "+importedModel.getName()+".");
        }
        geosetAnimTabs.addChangeListener(this);
        
        geosetAnimPanel.add(geosetAnimTabs);
        GroupLayout gaLayout = new GroupLayout(geosetAnimPanel);
        gaLayout.setVerticalGroup(gaLayout.createSequentialGroup()
            .addComponent(displayParents)
            .addComponent(allMatrOriginal)
            .addComponent(allMatrSameName)
            .addComponent(geosetAnimTabs)
            );
        gaLayout.setHorizontalGroup(gaLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(displayParents)
            .addComponent(allMatrOriginal)
            .addComponent(allMatrSameName)
            .addComponent(geosetAnimTabs)
            );
        geosetAnimPanel.setLayout(gaLayout);

        //Objects Panel
        addTab("Objects",objIcon,objectsPanel,"Controls which objects are imported.");
        getFutureBoneListExtended();
        
        
        //Build the objectTabs list of ObjectPanels
        ObjPanelListCellRenderer objectPanelRenderer = new ObjPanelListCellRenderer();
        int panelid = 0;
        for( int i = 0; i < importedModel.m_idobjects.size(); i++ )
        {
            IdObject obj = importedModel.m_idobjects.get(i);
            if( obj.getClass() != Bone.class && obj.getClass() != Helper.class )
            {
                
                ObjectPanel objPanel = new ObjectPanel( obj, futureBoneListEx );
                
                objectPanelCards.add(objPanel,panelid+"");//(objPanel.title.getText()));
                objectPanels.addElement(objPanel);
                panelid++;
//                 objectTabs.addTab(obj.getClass().getName()+" \""+obj.getName()+"\"",objIcon,objPanel,"Click to modify object import settings.");
            }
        }
        for( int i = 0; i < importedModel.m_cameras.size(); i++ )
        {
            Camera obj = importedModel.m_cameras.get(i);
            
            ObjectPanel objPanel = new ObjectPanel( obj );
            
            objectPanelCards.add(objPanel,panelid+"");//(objPanel.title.getText()));
            objectPanels.addElement(objPanel);
            panelid++;
        }
        multiObjectPane = new MultiObjectPanel(futureBoneListEx);
        objectPanelCards.add(blankPane,"blank");
        objectPanelCards.add(multiObjectPane,"multiple");
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
        
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,objectTabsPane,objectPanelCards);
        
        GroupLayout objectLayout = new GroupLayout(objectsPanel);
        objectLayout.setHorizontalGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(objectLayout.createSequentialGroup()
                .addComponent(importAllObjs)
                .addGap(8)
                .addComponent(uncheckAllObjs)
            )
            .addComponent(splitPane)
        );
        objectLayout.setVerticalGroup(objectLayout.createSequentialGroup()
            .addGroup(objectLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(importAllObjs)
                .addComponent(uncheckAllObjs)
            )
            .addGap(8)
            .addComponent(splitPane)
        );
        objectsPanel.setLayout(objectLayout);
        
        //Visibility Panel
        addTab("Visibility",orangeIcon,visPanel,"Controls the visibility of portions of the model.");
        
        initVisibilityList();
        visibilityList();
        
        VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
        for( VisibilityShell vs: allVisShells )
        {
            VisibilityPane vp = new VisibilityPane(vs,new DefaultComboBoxModel(visSourcesOld.toArray()),new DefaultComboBoxModel(visSourcesNew.toArray()),visRenderer);
            
            allVisShellPanes.add(vp);
                
            visPanelCards.add(vp,vp.title.getText());
        }
        
        multiVisPane = new MultiVisibilityPane(new DefaultComboBoxModel(visSourcesOld.toArray()),new DefaultComboBoxModel(visSourcesNew.toArray()),visRenderer);
        visPanelCards.add(blankPane,"blank");
        visPanelCards.add(multiVisPane,"multiple");
        visTabs.setModel(visComponents);
        visTabs.setCellRenderer(new VisPaneListCellRenderer(currentModel));
        visTabs.addListSelectionListener(this);
        visTabs.setSelectedIndex(0);
        visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));
        
        
        allInvisButton = new JButton("All Invisible in Exotic Anims");
        allInvisButton.addActionListener(this);
        allInvisButton.setToolTipText("Forces everything to be always invisibile in animations other than their own original animations.");
        visPanel.add(allInvisButton);
        
        allVisButton = new JButton("All Visible in Exotic Anims");
        allVisButton.addActionListener(this);
        allVisButton.setToolTipText("Forces everything to be always visibile in animations other than their own original animations.");
        visPanel.add(allVisButton);
        
        selSimButton = new JButton("Select Similar Options");
        selSimButton.addActionListener(this);
        selSimButton.setToolTipText("Similar components will be selected as visibility sources in exotic animations.");
        visPanel.add(selSimButton);
        
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,visTabsPane,visPanelCards);
        
        GroupLayout visLayout = new GroupLayout(visPanel);
        visLayout.setHorizontalGroup(visLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(allInvisButton)
//             .addGap(8)
            .addComponent(allVisButton)
            .addComponent(selSimButton)
            .addComponent(splitPane)
        );
        visLayout.setVerticalGroup(visLayout.createSequentialGroup()
            .addComponent(allInvisButton)
            .addGap(8)
            .addComponent(allVisButton)
            .addGap(8)
            .addComponent(selSimButton)
            .addGap(8)
            .addComponent(splitPane)
        );
        visPanel.setLayout(visLayout);
        
        
        
        
        //Listen all
        addChangeListener(this);
        
        
        okayButton = new JButton("Finish");
        okayButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        
        JPanel finalPanel = new JPanel();
        GroupLayout layout = new GroupLayout(finalPanel);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(this)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cancelButton)
                .addComponent(okayButton)
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(this)
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(cancelButton)
                .addComponent(okayButton)
                )
            );
        finalPanel.setLayout(layout);
        
        
        
        //Later add a Yes/No confirmation of "do you wish to cancel this import?" when you close the window.
        frame.setContentPane(finalPanel);
        
        
        
        frame.setBounds(0,0,1024,780);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                Object [] options = {"Yes","No"};
                int n = JOptionPane.showOptionDialog(frame,"Really cancel this import?","Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[1]);
                if( n == 0 )
                {
                    frame.setVisible(false);
                    frame = null;
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//         frame.pack();
        frame.setVisible(visibleOnStart);
    }
    public void actionPerformed(ActionEvent e)
    {
        if( e.getSource() == importAllGeos )
        {
            for( int i = 0; i < geosetTabs.getTabCount(); i++ )
            {
                GeosetPanel geoPanel = (GeosetPanel)geosetTabs.getComponentAt(i);
                geoPanel.setSelected(true);
            }
        }
        else if( e.getSource() == uncheckAllGeos )
        {
            for( int i = 0; i < geosetTabs.getTabCount(); i++ )
            {
                GeosetPanel geoPanel = (GeosetPanel)geosetTabs.getComponentAt(i);
                geoPanel.setSelected(false);
            }
        }
        else if( e.getSource() == importAllAnims )
        {
            for( int i = 0; i < animTabs.getTabCount(); i++ )
            {
                AnimPanel aniPanel = (AnimPanel)animTabs.getComponentAt(i);
                aniPanel.setSelected(true);
            }
        }
        else if( e.getSource() == uncheckAllAnims )
        {
            for( int i = 0; i < animTabs.getTabCount(); i++ )
            {
                AnimPanel aniPanel = (AnimPanel)animTabs.getComponentAt(i);
                aniPanel.setSelected(false);
            }
        }
        else if( e.getSource() == importAllBones )
        {
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                bonePanel.setSelectedIndex(0);
            }
        }
        else if( e.getSource() == motionFromBones )
        {
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                bonePanel.setSelectedIndex(1);
            }
        }
        else if( e.getSource() == uncheckUnusedBones )
        {
        	//Unselect all bones by iterating + setting to index 2 ("Do not import" index)
        	//Bones could be referenced by:
        	// - A matrix
        	// - Another bone
        	// - An IdObject
        	ArrayList<BonePanel> usedBonePanels = new ArrayList<BonePanel>();
//            for( int i = 0; i < bonePanels.size(); i++ )
//            {
//                BonePanel bonePanel = bonePanels.get(i);
//                if( bonePanel.getSelectedIndex() != 1 )
//                bonePanel.setSelectedIndex(2);
//            }
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                if( bonePanel.getSelectedIndex() == 0 )
                {
                }
            }
            for( int i = 0; i < objectPanels.size(); i++ )
            {
                ObjectPanel objectPanel = objectPanels.get(i);
                if( objectPanel.doImport.isSelected() && objectPanel.parentsList != null )
                {
//                    System.out.println("Performing check on base: "+objectPanel.object.getName());
                	BoneShell shell = (BoneShell)objectPanel.parentsList.getSelectedValue();
                	if( shell != null && shell.bone != null )
                	{
                        BonePanel current = getPanelOf(shell.bone);
                    	if( !usedBonePanels.contains(current) )
                    	{
//                            System.out.println("  @adding base: "+current.bone.getName());
                        	usedBonePanels.add(current);
                    	}
                        
                        boolean good = true;
                        int k = 0;
                        while( good )
                        {
                        	if( current == null || current.getSelectedIndex() == 1 )
                        		break;
                        	shell = (BoneShell)current.futureBonesList.getSelectedValue();
                        	//If shell is null, then the bone has "No Parent"
                        	//If current's selected index is not 2,
                        	if( shell == null )//current.getSelectedIndex() != 2
                        	{
                        		good = false;
                        	}
                        	else
                        	{
                            	current = getPanelOf(shell.bone);
                            	if( usedBonePanels.contains(current) )
                            	{
                            		good = false;
                            	}
                            	else
                            	{
//                                    System.out.println("  @Redirected + adding: "+current.bone.getName());
//                                	current.setSelectedIndex(0);
                                	usedBonePanels.add(current);
                            	}
                        	}
                        	k++;
                        	if( k > 1000 )
                        	{
                        		JOptionPane.showMessageDialog(null,"Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
                        		break;
                        	}
                        }
                	}
                }
            }
            for( int i = 0; i < geosetAnimTabs.getTabCount(); i++ )
            {
                if( geosetAnimTabs.isEnabledAt(i) )
                {
                    System.out.println("Performing check on geoset: "+i);
                    BoneAttachmentPane bap = (BoneAttachmentPane)geosetAnimTabs.getComponentAt(i);
                    for( int mk = 0; mk < bap.oldBoneRefs.size(); mk++ )
                    {
                    	MatrixShell ms = bap.oldBoneRefs.get(mk);
//                        System.out.println("Performing check on MatrixShell: "+ms);
                    	for( BoneShell bs: ms.newBones )
                    	{
                        	BoneShell shell = bs;
                            BonePanel current = getPanelOf(shell.bone);
                        	if( !usedBonePanels.contains(current) )
                        	{
//                                System.out.println("  @adding base: "+current.bone.getName());
                            	usedBonePanels.add(current);
                        	}
//                            System.out.println("Performing check on MatrixShell's sub: "+ms+": "+bs);
                            
                            boolean good = true;
                            int k = 0;
                            while( good )
                            {
                            	if( current == null || current.getSelectedIndex() == 1 )
                            		break;
                            	shell = (BoneShell)current.futureBonesList.getSelectedValue();
                            	//If shell is null, then the bone has "No Parent"
                            	//If current's selected index is not 2,
                            	if( shell == null )//current.getSelectedIndex() != 2
                            	{
                            		good = false;
                            	}
                            	else
                            	{
                                	current = getPanelOf(shell.bone);
                                	if( usedBonePanels.contains(current) )
                                	{
                                		good = false;
                                	}
                                	else
                                	{
//                                        System.out.println("  @Redirected + adding: "+current.bone.getName());
//                                    	current.setSelectedIndex(0);
                                    	usedBonePanels.add(current);
                                	}
                            	}
                            	k++;
                            	if( k > 1000 )
                            	{
                            		JOptionPane.showMessageDialog(null,"Unexpected error has occurred: IdObject to Bone parent loop, circular logic");
                            		break;
                            	}
                            }
                    	}
                    }
                }
            }
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                if( bonePanel.getSelectedIndex() != 1 )
            	{
                	if( usedBonePanels.contains(bonePanel) )
                	{
//                        System.out.println("Performing check on base: "+bonePanel.bone.getName());
                        BonePanel current = bonePanel;
                        boolean good = true;
                        int k = 0;
                        while( good )
                        {
                        	if( current == null || current.getSelectedIndex() == 1 )
                        		break;
                        	BoneShell shell = (BoneShell)current.futureBonesList.getSelectedValue();
                        	//If shell is null, then the bone has "No Parent"
                        	//If current's selected index is not 2,
                        	if( shell == null )//current.getSelectedIndex() != 2
                        	{
                        		good = false;
                        	}
                        	else
                        	{
                            	current = getPanelOf(shell.bone);
                            	if( usedBonePanels.contains(current) )
                            	{
                            		good = false;
                            	}
                            	else
                            	{
//                                    System.out.println("  @Redirected + adding: "+current.bone.getName());
//                                	current.setSelectedIndex(0);
                                	usedBonePanels.add(current);
                            	}
                        	}
                        	k++;
                        	if( k > 1000 )
                        	{
                        		JOptionPane.showMessageDialog(null,"Unexpected error has occurred: Bone parent loop, circular logic");
                        		break;
                        	}
                        }
                	}
            	}
            }
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                if( bonePanel.getSelectedIndex() != 1 )
            	{
                	if( usedBonePanels.contains(bonePanel) )
                	{
                        bonePanel.setSelectedIndex(0);
                	}
                	else
                	{
                        bonePanel.setSelectedIndex(2);
                	}
            	}
            }
        }
        else if( e.getSource() == uncheckAllBones )
        {
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                bonePanel.setSelectedIndex(2);
            }
        }
        else if( e.getSource() == importAllObjs )
        {
            for( int i = 0; i < objectPanels.size(); i++ )
            {
                ObjectPanel objectPanel = objectPanels.get(i);
                objectPanel.doImport.setSelected(true);
            }
        }
        else if( e.getSource() == uncheckAllObjs )
        {
            for( int i = 0; i < objectPanels.size(); i++ )
            {
                ObjectPanel objectPanel = objectPanels.get(i);
                objectPanel.doImport.setSelected(false);
            }
        }
        else if( e.getSource() == allVisButton )
        {
            for( int i = 0; i < allVisShellPanes.size(); i++ )
            {
                VisibilityPane vPanel = allVisShellPanes.get(i);
                if( vPanel.sourceShell.model == currentModel )
                {
                    vPanel.newSourcesBox.setSelectedItem(VisibilityPane.VISIBLE);
                }
                else
                {
                    vPanel.oldSourcesBox.setSelectedItem(VisibilityPane.VISIBLE);
                }
            }
        }
        else if( e.getSource() == allInvisButton )
        {
            for( int i = 0; i < allVisShellPanes.size(); i++ )
            {
                VisibilityPane vPanel = allVisShellPanes.get(i);
                if( vPanel.sourceShell.model == currentModel )
                {
                    vPanel.newSourcesBox.setSelectedItem(VisibilityPane.NOTVISIBLE);
                }
                else
                {
                    vPanel.oldSourcesBox.setSelectedItem(VisibilityPane.NOTVISIBLE);
                }
            }
        }
        else if( e.getSource() == selSimButton )
        {
            for( int i = 0; i < allVisShellPanes.size(); i++ )
            {
                VisibilityPane vPanel = allVisShellPanes.get(i);
                vPanel.selectSimilarOptions();
            }
        }
        else if( e.getSource() == okayButton )
        {
            doImport();
            frame.setVisible(false);
        }
        else if( e.getSource() == cancelButton )
        {
            Object [] options = {"Yes","No"};
            int n = JOptionPane.showOptionDialog(frame,"Really cancel this import?","Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[1]);
            if( n == 0 )
            {
                frame.setVisible(false);
                frame = null;
            }
        }
        else if( e.getSource() == allMatrOriginal )
        {
            for( int i = 0; i < geosetAnimTabs.getTabCount(); i++ )
            {
                if( geosetAnimTabs.isEnabledAt(i) )
                {
                    BoneAttachmentPane bap = (BoneAttachmentPane)geosetAnimTabs.getComponentAt(i);
                    bap.resetMatrices();
                }
            }
        }
        else if( e.getSource() == allMatrSameName )
        {
            for( int i = 0; i < geosetAnimTabs.getTabCount(); i++ )
            {
                if( geosetAnimTabs.isEnabledAt(i) )
                {
                    BoneAttachmentPane bap = (BoneAttachmentPane)geosetAnimTabs.getComponentAt(i);
                    bap.setMatricesToSimilarNames();
                }
            }
        }
    }
//     boolean listEnabled = true;
    public void valueChanged(ListSelectionEvent e)
    {
        if( e.getSource() == boneTabs)
        {
    //         boolean listEnabledNow = false;
            if( boneTabs.getSelectedValuesList().toArray().length < 1 )
            {
    //             listEnabledNow = listEnabled;
                boneCardLayout.show(bonePanelCards,"blank");
            }
            else if( boneTabs.getSelectedValuesList().toArray().length == 1 )
            {
    //             listEnabledNow = true;
                boneCardLayout.show(bonePanelCards,(boneTabs.getSelectedIndex())+"");
                ((BonePanel)boneTabs.getSelectedValue()).updateSelectionPicks();
            }
            else if( boneTabs.getSelectedValuesList().toArray().length > 1 )
            {
                boneCardLayout.show(bonePanelCards,"multiple");
    //             listEnabledNow = false;
                Object [] selected = boneTabs.getSelectedValuesList().toArray();
                boolean dif = false;
                int tempIndex = -99;
                for( int i = 0; i < selected.length && !dif; i++ )
                {
                    BonePanel temp = (BonePanel)selected[i];
                    if( tempIndex == -99 )
                    tempIndex = temp.importTypeBox.getSelectedIndex();
                    if( tempIndex != temp.importTypeBox.getSelectedIndex() )
                    {
                        dif = true;
                    }
                }
                if( dif )
                {
                    multiBonePane.setMultiTypes();
                }
                else
                {
                    multiBonePane.setSelectedIndex(tempIndex);
                }
            }
    //         if( listEnabledNow != listEnabled )
    //         {
    //             for( int i = 0; i < bonePanels.size(); i++ )
    //             {
    //                 BonePanel bonePanel = bonePanels.get(i);
    //                 bonePanel.boneListPane.setEnabled(listEnabledNow);
    //                 bonePanel.boneList.setEnabled(listEnabledNow);
    //             }
    //             listEnabled = listEnabledNow;
    //         }
        }
        else if( e.getSource() == objectTabs)
        {
            if( objectTabs.getSelectedValuesList().toArray().length < 1 )
            {
                objectCardLayout.show(objectPanelCards,"blank");
            }
            else if( objectTabs.getSelectedValuesList().toArray().length == 1 )
            {
                getFutureBoneListExtended();
                objectCardLayout.show(objectPanelCards,(objectTabs.getSelectedIndex())+"");//.title.getText()
            }
            else if( objectTabs.getSelectedValuesList().toArray().length > 1 )
            {
                objectCardLayout.show(objectPanelCards,"multiple");
                Object [] selected = objectTabs.getSelectedValuesList().toArray();
                boolean dif = false;
                boolean set = false;
                boolean selectedt = false;
                for( int i = 0; i < selected.length && !dif; i++ )
                {
                    ObjectPanel temp = (ObjectPanel)selected[i];
                    if( set == false )
                    {
                        set = true;
                        selectedt = temp.doImport.isSelected();
                    }
                    else if( selectedt != temp.doImport.isSelected() )
                    {
                        dif = true;
                    }
                }
                if( !dif )
                {
                    multiObjectPane.doImport.setSelected(selectedt);
                }
            }
        }
        else if( e.getSource() == visTabs)
        {
            if( visTabs.getSelectedValuesList().toArray().length < 1 )
            {
                visCardLayout.show(visPanelCards,"blank");
            }
            else if( visTabs.getSelectedValuesList().toArray().length == 1 )
            {
                visCardLayout.show(visPanelCards,((VisibilityPane)visTabs.getSelectedValue()).title.getText());
            }
            else if( visTabs.getSelectedValuesList().toArray().length > 1 )
            {
                visCardLayout.show(visPanelCards,"multiple");
                Object [] selected = visTabs.getSelectedValuesList().toArray();
                
                
                
                boolean dif = false;
                boolean set = false;
                boolean selectedt = false;
                
                boolean difBoxOld = false;
                boolean difBoxNew = false;
                int tempIndexOld = -99;
                int tempIndexNew = -99;
                
                for( int i = 0; i < selected.length && !dif; i++ )
                {
                    VisibilityPane temp = (VisibilityPane)selected[i];
                    if( set == false )
                    {
                        set = true;
                        selectedt = temp.favorOld.isSelected();
                    }
                    else if( selectedt != temp.favorOld.isSelected() )
                    {
                        dif = true;
                    }
                    
                    
                    if( tempIndexOld == -99 )
                    tempIndexOld = temp.oldSourcesBox.getSelectedIndex();
                    if( tempIndexOld != temp.oldSourcesBox.getSelectedIndex() )
                    {
                        difBoxOld = true;
                    }
                    
                    
                    if( tempIndexNew == -99 )
                    tempIndexNew = temp.newSourcesBox.getSelectedIndex();
                    if( tempIndexNew != temp.newSourcesBox.getSelectedIndex() )
                    {
                        difBoxNew = true;
                    }
                }
                if( !dif )
                {
                    multiVisPane.favorOld.setSelected(selectedt);
                }
                if( difBoxOld )
                {
                    multiVisPane.setMultipleOld();
                }
                else
                {
                    multiVisPane.oldSourcesBox.setSelectedIndex(tempIndexOld);
                }
                if( difBoxNew )
                {
                    multiVisPane.setMultipleNew();
                }
                else
                {
                    multiVisPane.newSourcesBox.setSelectedIndex(tempIndexNew);
                }
            }
        }
    }
    /**public void addAnimPicks(Object [] whichAnims, AnimPanel source)
    {
        for( int i = 0; i < animTabs.getTabCount(); i++ )
        {
            AnimPanel aniPanel = (AnimPanel)animTabs.getComponentAt(i);
            if( aniPanel != source )
            {
                for( Object o: whichAnims )
                {
                    if( !aniPanel.existingAnims.contains(o) )
                    {
                        aniPanel.listenSelection = false;
                        aniPanel.existingAnims.addElement(o);
                        aniPanel.listenSelection = true;
//                         System.out.println(animTabs.getTitleAt(i)+" gained "+((Animation)o).getName());
                    }
                }
                aniPanel.reorderToModel(existingAnims);
            }
        }
    }
    public void removeAnimPicks(Object [] whichAnims, AnimPanel source)
    {
        for( int i = 0; i < animTabs.getTabCount(); i++ )
        {
            AnimPanel aniPanel = (AnimPanel)animTabs.getComponentAt(i);
            if( aniPanel != source )
            {
                for( Object o: whichAnims )
                {
                    aniPanel.listenSelection = false;
                    aniPanel.existingAnims.removeElement(o);
                    aniPanel.listenSelection = true;
//                     System.out.println(animTabs.getTitleAt(i)+" lost "+((Animation)o).getName());
                }
            }
        }
    }
    public void reorderAnimPicks(AnimPanel source)
    {
        for( int i = 0; i < animTabs.getTabCount(); i++ )
        {
            AnimPanel aniPanel = (AnimPanel)animTabs.getComponentAt(i);
            if( aniPanel != source )
            {
                aniPanel.reorderToModel(existingAnims);
            }
        }
    }
    public void addBonePicks(Object [] whichBones, BonePanel source)
    {
        for( int i = 0; i < bonePanels.size(); i++ )
        {
            BonePanel bonePanel = bonePanels.get(i);
            if( bonePanel != source )
            {
                for( Object o: whichBones )
                {
                    if( !bonePanel.existingBones.contains(o) )
                    {
                        bonePanel.listenSelection = false;
                        bonePanel.existingBones.addElement(o);
                        bonePanel.listenSelection = true;
//                         System.out.println(animTabs.getTitleAt(i)+" gained "+((Animation)o).getName());
                    }
                }
                bonePanel.reorderToModel(existingBones);
            }
        }
    }
    public void removeBonePicks(Object [] whichBones, BonePanel source)
    {
        for( int i = 0; i < bonePanels.size(); i++ )
        {
            BonePanel bonePanel = bonePanels.get(i);
            if( bonePanel != source )
            {
                for( Object o: whichBones )
                {
                    bonePanel.listenSelection = false;
                    bonePanel.existingBones.removeElement(o);
                    bonePanel.listenSelection = true;
//                     System.out.println(animTabs.getTitleAt(i)+" lost "+((Animation)o).getName());
                }
            }
        }
    }
    public void reorderBonePicks(BonePanel source)
    {
        for( int i = 0; i < bonePanels.size(); i++ )
        {
            BonePanel bonePanel = bonePanels.get(i);
            if( bonePanel != source )
            {
                bonePanel.reorderToModel(existingBones);
            }
        }
    }**/
    public void informGeosetVisibility(Geoset g, boolean flag)
    {
        for( int i = 0; i < geosetAnimTabs.getTabCount(); i++ )
        {
            BoneAttachmentPane geoPanel = (BoneAttachmentPane)geosetAnimTabs.getComponentAt(i);
            if( geoPanel.geoset == g )
            {
                geosetAnimTabs.setEnabledAt(i,flag);
            }
        }
    }
    public BonePanel getPanelOf(Bone b)
    {
        BonePanel out = null;
        for( int i = 0; i < bonePanels.size() && out == null; i++ )
        {
            BonePanel bp = bonePanels.get(i);
            if( bp.bone == b )
            {
                out = bp;
            }
        }
        return out;
    }
    public DefaultListModel<BoneShell> getFutureBoneList()
    {
        if( oldBones == null )
        {
            oldBones = new ArrayList<BoneShell>();
            newBones = new ArrayList<BoneShell>();
            ArrayList<Bone> oldBonesRefs = currentModel.sortedIdObjects(Bone.class);
            for( Bone b: oldBonesRefs )
            {
                BoneShell bs = new BoneShell(b);
                bs.modelName = currentModel.getName();
                oldBones.add(bs);
            }
            ArrayList<Bone> newBonesRefs = importedModel.sortedIdObjects(Bone.class);
            for( Bone b: newBonesRefs )
            {
                BoneShell bs = new BoneShell(b);
                bs.modelName = importedModel.getName();
                bs.panel = getPanelOf(b);
                newBones.add(bs);
            }
        }
        if( !clearExistingBones.isSelected() )
        {
            for( BoneShell b: oldBones )
            {
                if( !futureBoneList.contains(b) )
                {
                    futureBoneList.addElement(b);
                }
            }
        }
        else
        {
            for( BoneShell b: oldBones )
            {
                if( futureBoneList.contains(b) )
                {
                    futureBoneList.removeElement(b);
                }
            }
        }
        for( BoneShell b: newBones )
        {
            if( b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT )
            {
                if( !futureBoneList.contains(b) )
                {
                    futureBoneList.addElement(b);
                }
            }
            else
            {
                if( futureBoneList.contains(b) )
                {
                    futureBoneList.removeElement(b);
                }
            }
        }
        return futureBoneList;
    }
    DefaultListModel<BoneShell> futureBoneListEx = new DefaultListModel<BoneShell>();
    ArrayList<BoneShell> oldHelpers;
    ArrayList<BoneShell> newHelpers;
    public DefaultListModel<BoneShell> getFutureBoneListExtended()
    {
//         if( futureBoneList.size() > 0 )
//         {
//             if( oldHelpers == null )
//             {
//                 for( int i = 0; i < futureBoneList.size(); i++ )
//                 {
//                     futureBoneListEx.addElement(futureBoneList.get(i));
//                 }
//                 oldHelpers = new ArrayList<BoneShell>();
//                 newHelpers = new ArrayList<BoneShell>();
//                 ArrayList<Bone> oldBonesRefs = currentModel.sortedIdObjects(Helper.class);
//                 for( Bone b: oldBonesRefs )
//                 {
//                     BoneShell bs = new BoneShell(b);
//                     bs.modelName = currentModel.getName();
//                     oldBones.add(bs);
//                 }
//                 ArrayList<Bone> newBonesRefs = importedModel.sortedIdObjects(Helper.class);
//                 for( Bone b: newBonesRefs )
//                 {
//                     BoneShell bs = new BoneShell(b);
//                     bs.modelName = importedModel.getName();
//                     bs.panel = getPanelOf(b);
//                     newBones.add(bs);
//                 }
//             }
//             for( int i = 0; i < futureBoneListEx.size(); i++ )
//             {
//                 BoneShell bs = futureBoneListEx.get(i);
//                 if( (oldBones.contains(bs) || newBones.contains(bs)) && !futureBoneList.contains(bs) )
//                 {
//                     futureBoneListEx.removeElement(bs);
//                 }
//             }
//             for( int i = 0; i < futureBoneList.size(); i++ )
//             {
//                 BoneShell bs = futureBoneList.get(i);
//                 if( !futureBoneListEx.contains(bs) )
//                 futureBoneListEx.addElement(bs);
//             }
//             if( !clearExistingBones.isSelected() )
//             {
//                 for( BoneShell b: oldHelpers )
//                 {
//                     if( !futureBoneListEx.contains(b) )
//                     {
//                         futureBoneListEx.addElement(b);
//                     }
//                 }
//             }
//             else
//             {
//                 for( BoneShell b: oldHelpers )
//                 {
//                     if( futureBoneListEx.contains(b) )
//                     {
//                         futureBoneListEx.removeElement(b);
//                     }
//                 }
//             }
//             for( BoneShell b: newHelpers )
//             {
//                 if( b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT )
//                 {
//                     if( !futureBoneListEx.contains(b) )
//                     {
//                         futureBoneListEx.addElement(b);
//                     }
//                 }
//                 else
//                 {
//                     if( futureBoneListEx.contains(b) )
//                     {
//                         futureBoneListEx.removeElement(b);
//                     }
//                 }
//             }
//         }
        if( oldHelpers == null )
        {
            oldHelpers = new ArrayList<BoneShell>();
            newHelpers = new ArrayList<BoneShell>();
            ArrayList<Bone> oldHelpersRefs = currentModel.sortedIdObjects(Bone.class);
            for( Bone b: oldHelpersRefs )
            {
                BoneShell bs = new BoneShell(b);
                bs.modelName = currentModel.getName();
                bs.showClass = true;
                oldHelpers.add(bs);
            }
            oldHelpersRefs = currentModel.sortedIdObjects(Helper.class);
            for( Bone b: oldHelpersRefs )
            {
                BoneShell bs = new BoneShell(b);
                bs.modelName = currentModel.getName();
                bs.showClass = true;
                oldHelpers.add(bs);
            }
            ArrayList<Bone> newHelpersRefs = importedModel.sortedIdObjects(Bone.class);
            for( Bone b: newHelpersRefs )
            {
                BoneShell bs = new BoneShell(b);
                bs.modelName = importedModel.getName();
                bs.showClass = true;
                bs.panel = getPanelOf(b);
                newHelpers.add(bs);
            }
            newHelpersRefs = importedModel.sortedIdObjects(Helper.class);
            for( Bone b: newHelpersRefs )
            {
                BoneShell bs = new BoneShell(b);
                bs.modelName = importedModel.getName();
                bs.showClass = true;
                bs.panel = getPanelOf(b);
                newHelpers.add(bs);
            }
        }
        if( !clearExistingBones.isSelected() )
        {
            for( BoneShell b: oldHelpers )
            {
                if( !futureBoneListEx.contains(b) )
                {
                    futureBoneListEx.addElement(b);
                }
            }
        }
        else
        {
            for( BoneShell b: oldHelpers )
            {
                if( futureBoneListEx.contains(b) )
                {
                    futureBoneListEx.removeElement(b);
                }
            }
        }
        for( BoneShell b: newHelpers )
        {
            b.panel = getPanelOf(b.bone);
            if( b.panel != null )
            {
                if( b.panel.importTypeBox.getSelectedItem() == BonePanel.IMPORT )
                {
                    if( !futureBoneListEx.contains(b) )
                    {
                        futureBoneListEx.addElement(b);
                    }
                }
                else
                {
                    if( futureBoneListEx.contains(b) )
                    {
                        futureBoneListEx.removeElement(b);
                    }
                }
            }
        }
        
        return futureBoneListEx;
    }
    ArrayList<VisibilityShell> allVisShells;
    ArrayList<Object> visSourcesOld;
    ArrayList<Object> visSourcesNew;
    DefaultListModel<VisibilityPane> visComponents;
    
    ArrayList<VisibilityPane> allVisShellPanes = new ArrayList<VisibilityPane>();
    public VisibilityShell shellFromObject(Object o)
    {
        for( VisibilityShell v: allVisShells )
        {
            if( v.source == o )
            {
                return v;
            }
        }
        return null;
    }
    public VisibilityPane visPaneFromObject(Object o)
    {
        for( VisibilityPane vp: allVisShellPanes )
        {
            if( vp.sourceShell.source == o )
            {
                return vp;
            }
        }
        return null;
    }
    public void initVisibilityList()
    {
        visSourcesOld = new ArrayList();
        visSourcesNew = new ArrayList();
        allVisShells = new ArrayList<VisibilityShell>();
        MDL model = currentModel;
        ArrayList tempList = new ArrayList();
        for( Material mat: model.m_materials )
        {
            for( Layer lay: mat.layers )
            {
                VisibilityShell vs = new VisibilityShell(lay,model);
                if( !tempList.contains(lay) )
                {
                    tempList.add(lay);
                    allVisShells.add(vs);
                }
            }
        }
        for( Geoset ga: model.m_geosets )
        {
            VisibilityShell vs = new VisibilityShell(ga,model);
            if( !tempList.contains(ga) )
            {
                tempList.add(ga);
                allVisShells.add(vs);
            }
        }
        for( Object l: model.sortedIdObjects(Light.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)l,model);
            if( !tempList.contains(l) )
            {
                tempList.add(l);
                allVisShells.add(vs);
            }
        }
        for( Object a: model.sortedIdObjects(Attachment.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)a,model);
            if( !tempList.contains(a) )
            {
                tempList.add(a);
                allVisShells.add(vs);
            }
        }
        for( Object x: model.sortedIdObjects(ParticleEmitter.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)x,model);
            if( !tempList.contains(x) )
            {
                tempList.add(x);
                allVisShells.add(vs);
            }
        }
        for( Object x: model.sortedIdObjects(ParticleEmitter2.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)x,model);
            if( !tempList.contains(x) )
            {
                tempList.add(x);
                allVisShells.add(vs);
            }
        }
        for( Object x: model.sortedIdObjects(RibbonEmitter.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)x,model);
            if( !tempList.contains(x) )
            {
                tempList.add(x);
                allVisShells.add(vs);
            }
        }
        model = importedModel;
        for( Material mat: model.m_materials )
        {
            for( Layer lay: mat.layers )
            {
                VisibilityShell vs = new VisibilityShell(lay,model);
                if( !tempList.contains(lay) )
                {
                    tempList.add(lay);
                    allVisShells.add(vs);
                }
            }
        }
        for( Geoset ga: model.m_geosets )
        {
            VisibilityShell vs = new VisibilityShell(ga,model);
            if( !tempList.contains(ga) )
            {
                tempList.add(ga);
                allVisShells.add(vs);
            }
        }
        for( Object l: model.sortedIdObjects(Light.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)l,model);
            if( !tempList.contains(l) )
            {
                tempList.add(l);
                allVisShells.add(vs);
            }
        }
        for( Object a: model.sortedIdObjects(Attachment.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)a,model);
            if( !tempList.contains(a) )
            {
                tempList.add(a);
                allVisShells.add(vs);
            }
        }
        for( Object x: model.sortedIdObjects(ParticleEmitter.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)x,model);
            if( !tempList.contains(x) )
            {
                tempList.add(x);
                allVisShells.add(vs);
            }
        }
        for( Object x: model.sortedIdObjects(ParticleEmitter2.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)x,model);
            if( !tempList.contains(x) )
            {
                tempList.add(x);
                allVisShells.add(vs);
            }
        }                
        for( Object x: model.sortedIdObjects(RibbonEmitter.class) )
        {
            VisibilityShell vs = new VisibilityShell((Named)x,model);
            if( !tempList.contains(x) )
            {
                tempList.add(x);
                allVisShells.add(vs);
            }
        }
        
        System.out.println("allVisShells:");
        for( VisibilityShell vs: allVisShells )
        {
            System.out.println(vs.source.getName());
        }
        
        System.out.println("new/old:");
        for( Object o: currentModel.getAllVisibilitySources() )
        {
            if( o.getClass() != GeosetAnim.class )
            {
                visSourcesOld.add(shellFromObject(o));
                System.out.println(shellFromObject(o).source.getName());
            }
            else
            {
                visSourcesOld.add(shellFromObject(((GeosetAnim)o).geoset));
                System.out.println(shellFromObject(((GeosetAnim)o).geoset).source.getName());
            }
        }
        visSourcesOld.add(VisibilityPane.NOTVISIBLE);
        visSourcesOld.add(VisibilityPane.VISIBLE);
        for( Object o: importedModel.getAllVisibilitySources() )
        {
            if( o.getClass() != GeosetAnim.class )
            {
                visSourcesNew.add(shellFromObject(o));
                System.out.println(shellFromObject(o).source.getName());
            }
            else
            {
                visSourcesNew.add(shellFromObject(((GeosetAnim)o).geoset));
                System.out.println(shellFromObject(((GeosetAnim)o).geoset).source.getName());
            }
        }
        visSourcesNew.add(VisibilityPane.NOTVISIBLE);
        visSourcesNew.add(VisibilityPane.VISIBLE);
        visComponents = new DefaultListModel<VisibilityPane>();
    }
    public DefaultListModel<VisibilityPane> visibilityList()
    {
        Object selection = visTabs.getSelectedValue();
        visComponents.clear();
        for( int i = 0; i < geosetTabs.getTabCount(); i++ )
        {
            GeosetPanel gp = (GeosetPanel)geosetTabs.getComponentAt(i);
            for( Layer l: gp.getSelectedMaterial().layers )
            {
                VisibilityPane vs = visPaneFromObject(l);
                if( !visComponents.contains(vs) && vs != null )
                {
                    visComponents.addElement(vs);
                }
            }
        }
        for( int i = 0; i < geosetTabs.getTabCount(); i++ )
        {
            GeosetPanel gp = (GeosetPanel)geosetTabs.getComponentAt(i);
            if( gp.doImport.isSelected() )
            {
                Geoset ga = gp.geoset;
                VisibilityPane vs = visPaneFromObject(ga);
                if( !visComponents.contains(vs) && vs != null )
                {
                    visComponents.addElement(vs);
                }
            }
        }
        //The current's 
        MDL model = currentModel;
        for( Object l: model.sortedIdObjects(Light.class) )
        {
            VisibilityPane vs = visPaneFromObject(l);
            if( !visComponents.contains(vs) && vs != null )
            {
                visComponents.addElement(vs);
            }
        }
        for( Object a: model.sortedIdObjects(Attachment.class) )
        {
            VisibilityPane vs = visPaneFromObject(a);
            if( !visComponents.contains(vs) && vs != null )
            {
                visComponents.addElement(vs);
            }
        }
        for( Object x: model.sortedIdObjects(ParticleEmitter.class) )
        {
            VisibilityPane vs = visPaneFromObject(x);
            if( !visComponents.contains(vs) && vs != null )
            {
                visComponents.addElement(vs);
            }
        }
        for( Object x: model.sortedIdObjects(ParticleEmitter2.class) )
        {
            VisibilityPane vs = visPaneFromObject(x);
            if( !visComponents.contains(vs) && vs != null )
            {
                visComponents.addElement(vs);
            }
        }
        for( Object x: model.sortedIdObjects(RibbonEmitter.class) )
        {
            VisibilityPane vs = visPaneFromObject(x);
            if( !visComponents.contains(vs) && vs != null )
            {
                visComponents.addElement(vs);
            }
        }
        
        for( int i = 0; i < objectPanels.size(); i++ )
        {
            ObjectPanel op = objectPanels.get(i);
            if( op.doImport.isSelected() && op.object != null )//we don't touch camera "object" panels (which aren't idobjects)
            {
                VisibilityPane vs = visPaneFromObject(op.object);
                if( !visComponents.contains(vs) && vs != null )
                {
                    visComponents.addElement(vs);
                }
            }
        }
        visTabs.setSelectedValue(selection,true);
        return visComponents;
    }
    public void setSelectedItem(String what)
    {
        Object [] selected = boneTabs.getSelectedValuesList().toArray();
        for( int i = 0; i < selected.length; i++ )
        {
            BonePanel temp = (BonePanel)selected[i];
            temp.setSelectedValue(what);
        }
    }
    /**
     * The method run when the user pushes the "Set Parent for All" button in the MultiBone panel.
     */
    public void setParentMultiBones()
    {
    	JList<BoneShell> list = new JList<BoneShell>(getFutureBoneListExtended());
    	int x = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Set Parent for All Selected Bones", JOptionPane.OK_CANCEL_OPTION);
    	if( x == JOptionPane.OK_OPTION);
    	{
            Object [] selected = boneTabs.getSelectedValuesList().toArray();
            for( int i = 0; i < selected.length; i++ )
            {
                BonePanel temp = (BonePanel)selected[i];
                temp.setParent(list.getSelectedValue());
            }
    	}
    }
    public void setObjGroupSelected(boolean flag)
    {
        Object [] selected = objectTabs.getSelectedValuesList().toArray();
        for( int i = 0; i < selected.length; i++ )
        {
            ObjectPanel temp = (ObjectPanel)selected[i];
            temp.doImport.setSelected(flag);
        }
    }
    public void setVisGroupSelected(boolean flag)
    {
        Object [] selected = visTabs.getSelectedValuesList().toArray();
        for( int i = 0; i < selected.length; i++ )
        {
            VisibilityPane temp = (VisibilityPane)selected[i];
            temp.favorOld.setSelected(flag);
        }
    }
    public void setVisGroupItemOld(Object o)
    {
        Object [] selected = visTabs.getSelectedValuesList().toArray();
        for( int i = 0; i < selected.length; i++ )
        {
            VisibilityPane temp = (VisibilityPane)selected[i];
            temp.oldSourcesBox.setSelectedItem(o);
        }
    }
    public void setVisGroupItemNew(Object o)
    {
        Object [] selected = visTabs.getSelectedValuesList().toArray();
        for( int i = 0; i < selected.length; i++ )
        {
            VisibilityPane temp = (VisibilityPane)selected[i];
            temp.newSourcesBox.setSelectedItem(o);
        }
    }
    public void stateChanged(ChangeEvent e)
    {
        ((AnimPanel)animTabs.getSelectedComponent()).updateSelectionPicks();
        getFutureBoneList();
        getFutureBoneListExtended();
        visibilityList();
//         ((BoneAttachmentPane)geosetAnimTabs.getSelectedComponent()).refreshLists();
        repaint();
    }
    
    public void doImport()
    {
        importStarted = true;
    	try {

            //AFTER WRITING THREE THOUSAND LINES OF INTERFACE, FINALLLLLLLLLLLLYYYYYYYYY
            //The engine for actually performing the model to model import.
            
            if( currentModel == importedModel )
            {
                JOptionPane.showMessageDialog(null,"The program has confused itself.");
            }
            
            //ArrayList<Geoset> newGeosets = new ArrayList<Geoset>();//Just for 3d update
            
            for( int i = 0; i < geosetTabs.getTabCount(); i++ )
            {
                GeosetPanel gp = (GeosetPanel)geosetTabs.getComponentAt(i);
                gp.geoset.setMaterial(gp.getSelectedMaterial());
                if( gp.doImport.isSelected() && gp.model == importedModel )
                {
                    currentModel.add(gp.geoset);
                    //newGeosets.add(gp.geoset);//Just for 3d update
                    if( gp.geoset.geosetAnim != null )
                    currentModel.add(gp.geoset.geosetAnim);
                }
            }
            ArrayList<Animation> oldAnims = new ArrayList<Animation>();
            oldAnims.addAll(currentModel.m_anims);
            ArrayList<Animation> newAnims = new ArrayList<Animation>();
            java.util.List<AnimFlag> curFlags = currentModel.getAllAnimFlags();
            java.util.List<AnimFlag> impFlags = importedModel.getAllAnimFlags();
            ArrayList curEventObjs = currentModel.sortedIdObjects(EventObject.class);
            ArrayList impEventObjs = importedModel.sortedIdObjects(EventObject.class);
            //note to self: remember to scale event objects with time
            ArrayList<AnimFlag> newImpFlags = new ArrayList<AnimFlag>();
            for( AnimFlag af: impFlags )
            {
                if( !af.hasGlobalSeq )
                {
                    newImpFlags.add(AnimFlag.buildEmptyFrom(af));
                }
                else
                {
                    newImpFlags.add(new AnimFlag(af));
                }
            }
            ArrayList<EventObject> newImpEventObjs = new ArrayList<EventObject>();
            for( Object e: impEventObjs )
            {
                newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject)e));
            }
            boolean clearAnims = clearExistingAnims.isSelected();
            if( clearAnims )
            {
            	for( Animation anim: currentModel.m_anims )
            	{
            		anim.clearData(curFlags, curEventObjs);
            	}
                currentModel.m_anims.clear();
            }
            for( int i = 0; i < animTabs.getTabCount(); i++ )
            {
                AnimPanel aniPanel = (AnimPanel)animTabs.getComponentAt(i);
                if( aniPanel.doImport.isSelected() )
                {
                    int type = aniPanel.importTypeBox.getSelectedIndex();
                    int animTrackEnd = currentModel.animTrackEnd();
                    if( aniPanel.inReverse.isSelected() )
                    {
                        //reverse the animation
                        aniPanel.anim.reverse(impFlags, impEventObjs);
                    }
                    switch (type)
                    {
                        case 0: aniPanel.anim.copyToInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300, impFlags, impEventObjs, newImpFlags, newImpEventObjs);
                                aniPanel.anim.setInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300);
                                currentModel.add(aniPanel.anim);
                                newAnims.add(aniPanel.anim);
                            break;
                        case 1: 
                                aniPanel.anim.copyToInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300, impFlags, impEventObjs, newImpFlags, newImpEventObjs);
                                aniPanel.anim.setInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300);
                                aniPanel.anim.setName(aniPanel.newNameEntry.getText());
                                currentModel.add(aniPanel.anim);
                                newAnims.add(aniPanel.anim);
                            break;
                        case 2: 
//                             List<AnimShell> targets = aniPane.animList.getSelectedValuesList();
//                             aniPanel.anim.setInterval(animTrackEnd+300,animTrackEnd + aniPanel.anim.length() + 300, impFlags, impEventObjs, newImpFlags, newImpEventObjs);
                            //handled by animShells
                            break;
                        case 3:
                            importedModel.buildGlobSeqFrom(aniPanel.anim,impFlags);
                            break;
                    }
                }
            }
            boolean clearBones = clearExistingBones.isSelected();
            if( !clearAnims )
            {
                for( int i = 0; i < existingAnims.size(); i++ )
                {
                    Object o = existingAnims.get(i);
                    AnimShell animShell = (AnimShell)o;
                    if( animShell.importAnim != null )
                    {
                        animShell.importAnim.copyToInterval(animShell.anim.getStart(),animShell.anim.getEnd(), impFlags, impEventObjs, newImpFlags, newImpEventObjs);
                        Animation tempAnim = new Animation("temp",animShell.anim.getStart(),animShell.anim.getEnd());
                        newAnims.add(tempAnim);
                        if( !clearBones )
                        {
                            for( int p = 0; p < existingBones.size(); p++ )
                            {
                                BoneShell bs = (BoneShell)existingBones.get(p);
                                if( bs.importBone != null )
                                {
                                    if( getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1 )
                                    {
//                                         JOptionPane.showMessageDialog(null,"Attempting to clear animation for "+bs.bone.getName()+" values "+animShell.anim.getStart()+", "+animShell.anim.getEnd());
                                        System.out.println("Attempting to clear animation for "+bs.bone.getName()+" values "+animShell.anim.getStart()+", "+animShell.anim.getEnd());
                                        bs.bone.clearAnimation(animShell.anim);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //Now, rebuild the old animflags with the new
            for( AnimFlag af: impFlags )
            {
                af.setValuesTo(newImpFlags.get(impFlags.indexOf(af)));
            }
            for( Object e: impEventObjs )
            {
                ((EventObject)e).setValuesTo(newImpEventObjs.get(impEventObjs.indexOf(e)));
            }
            
            if( clearBones )
            {
                for( Object o: currentModel.sortedIdObjects(Bone.class) )
                {
                    currentModel.remove((IdObject)o);
                }
                for( Object o: currentModel.sortedIdObjects(Helper.class) )
                {
                    currentModel.remove((IdObject)o);
                }
            }
            for( int i = 0; i < bonePanels.size(); i++ )
            {
                BonePanel bonePanel = bonePanels.get(i);
                Bone b = bonePanel.bone;
                int type = bonePanel.importTypeBox.getSelectedIndex();
                switch (type)
                {
                    case 0: currentModel.add(b);
//                             b.setName(b.getName()+" "+importedModel.getName());
                            BoneShell mbs = (BoneShell)bonePanel.futureBonesList.getSelectedValue();
                            if( mbs != null )
                            {
                                b.parent = (IdObject)(mbs).bone;
                            }
                            else
                            {
                                b.parent = null;
                            }
                        break;
                    case 1: //List targets = bonePanel.boneList.getSelectedValuesList();
                        // we will go through all bone shells for this
                        break;
                    case 2: 
                        break;
                }
            }
            if( !clearBones )
            {
                for( int i = 0; i < existingBones.size(); i++ )
                {
                    BoneShell bs = (BoneShell)existingBones.get(i);
                    if( bs.importBone != null )
                    {
                        if( getPanelOf(bs.importBone).importTypeBox.getSelectedIndex() == 1 )
                        {
                            bs.bone.copyMotionFrom(bs.importBone);
                        }
                    }
                }
            }
//             DefaultListModel<BoneShell> bones = getFutureBoneList();
            for( int i = 0; i < geosetAnimTabs.getTabCount(); i++ )
            {
                if( geosetAnimTabs.isEnabledAt(i) )
                {
                    BoneAttachmentPane bap = (BoneAttachmentPane)geosetAnimTabs.getComponentAt(i);
                    for( int l = 0; l < bap.oldBoneRefs.size(); l++ )
                    {
                        MatrixShell ms = bap.oldBoneRefs.get(l);
                        if( ms.newBones.size() > 0 ) // don't make dumb empty matrices when things get confused
                        ms.matrix.bones.clear();
                        else
                        {
                            JOptionPane.showMessageDialog(null,"Warning: You left some matrices empty. Zero values will be inserted.");
                            ms.matrix.bones.clear();
                            ms.matrix.bones.add(bap.model.getBone(0));
                        }
                        for( BoneShell bs: ms.newBones )
                        {
                            if( currentModel.contains(bs.bone) )
                            {
                                if( bs.bone.getClass() == Helper.class )
                                JOptionPane.showMessageDialog(null,"Error: Holy fo shizzle my grizzle! A geoset is trying to attach to a helper, not a bone!");
                                ms.matrix.add(bs.bone);
                            }
                            else
                            {
                                System.out.println("Boneshaving "+bs.bone.getName()+" out of use");
                            }
                        }
                        if( ms.matrix.size() == 0 )
                        {
                            JOptionPane.showMessageDialog(null,"Error: A matrix was functionally destroyed while importing, and may take the program with it!");
                        }
                        //ms.matrix.bones = ms.newBones;
                    }
                }
            }
            currentModel.updateObjectIds();
            for( Geoset g: currentModel.m_geosets )
            {
                g.applyMatricesToVertices(currentModel);
            }
            
            //Objects!
            for( int i = 0; i < objectPanels.size(); i++ )
            {
                ObjectPanel objectPanel = objectPanels.get(i);
                if( objectPanel.doImport.isSelected() )
                {
                    if( objectPanel.object != null )
                    {
                        BoneShell mbs = (BoneShell)objectPanel.parentsList.getSelectedValue();
                        if( mbs != null )
                        {
                            objectPanel.object.setParent((IdObject)mbs.bone);
                        }
                        else
                        {
                            objectPanel.object.setParent(null);
                        }
//                         objectPanel.object.setName(importedModel.getName()+" "+objectPanel.object.getName());
                        //later make a name field?
                        currentModel.add(objectPanel.object);
                    }
                    else if( objectPanel.camera != null )
                    {
//                         objectPanel.camera.setName(importedModel.getName()+" "+objectPanel.camera.getName());
                        currentModel.add(objectPanel.camera);
                    }
                }
            }
            
            ArrayList<AnimFlag> finalVisFlags = new ArrayList<AnimFlag>();
            for( int i = 0; i < visComponents.size(); i++ )
            {
                VisibilityPane vPanel = visComponents.get(i);
                VisibilitySource temp = ((VisibilitySource)vPanel.sourceShell.source);
                AnimFlag visFlag = temp.getVisibilityFlag();//might be null
                AnimFlag newVisFlag;
                boolean tans = false;
                if( visFlag != null )
                {
                    newVisFlag = AnimFlag.buildEmptyFrom(visFlag);
                    tans = visFlag.tans();
                }
                else
                {
                    newVisFlag = new AnimFlag(temp.visFlagName());
                }
//                     newVisFlag = new AnimFlag(temp.visFlagName());
                Object oldSource = vPanel.oldSourcesBox.getSelectedItem();
                AnimFlag flagOld = null;
                boolean test = false;
                if( oldSource.getClass() == String.class )
                {
                    if( oldSource == VisibilityPane.VISIBLE )
                    {
                        //empty for visible
                    }
                    else if( oldSource == VisibilityPane.NOTVISIBLE )
                    {
                        System.out.println("Add invis o: "+vPanel.sourceShell.model.getName());
                        flagOld = new AnimFlag("temp");
                        for( Animation a: oldAnims )
                        {
                            test = true;
                            System.out.println("Adding 0 value");
                            flagOld.times.add(new Integer(a.getStart()));
                            flagOld.values.add(new Double(0));
                            if( tans )
                            {
                                flagOld.inTans.add(new Double(0));
                                flagOld.outTans.add(new Double(0));
                            }
                        }
                    }
                }
                else
                {
                    flagOld = (((VisibilitySource)((VisibilityShell)oldSource).source).getVisibilityFlag());
                }
                Object newSource = vPanel.newSourcesBox.getSelectedItem();
                AnimFlag flagNew = null;
                if( newSource.getClass() == String.class )
                {
                    if( newSource == VisibilityPane.VISIBLE )
                    {
                        //empty for visible
                    }
                    else if( newSource == VisibilityPane.NOTVISIBLE )
                    {
                        System.out.println("Add invis n: "+vPanel.sourceShell.model.getName());
                        flagNew = new AnimFlag("temp");
                        for( Animation a: newAnims )
                        {
                            flagNew.times.add(new Integer(a.getStart()));
                            flagNew.values.add(new Double(0));
                            if( tans )
                            {
                                flagNew.inTans.add(new Double(0));
                                flagNew.outTans.add(new Double(0));
                            }
                        }
                    }
                }
                else
                {
                    flagNew = (((VisibilitySource)((VisibilityShell)newSource).source).getVisibilityFlag());
                }
                if( (vPanel.favorOld.isSelected() && vPanel.sourceShell.model == currentModel && !clearAnims) || (!vPanel.favorOld.isSelected() && vPanel.sourceShell.model == importedModel ) )
                {
                    //this is an element favoring existing animations over imported
                    for( Animation a: oldAnims )
                    {
                        if( flagNew != null )
                        {
                            if( !flagNew.hasGlobalSeq )
                            flagNew.deleteAnim(a);//All entries for visibility are deleted from imported sources during existing animation times
                        }
                    }
                }
                else
                {
                    //this is an element not favoring existing over imported
                    for( Animation a: newAnims )
                    {
                        if( flagOld != null )
                        {
                            System.out.println("Deletin' my homies");
                            if( !flagOld.hasGlobalSeq )
                            flagOld.deleteAnim(a);//All entries for visibility are deleted from original-based sources during imported animation times
                        }
                    }
                }
                if( flagOld != null )
                {
//                     if( test )
                    System.out.println("Copyin' my bro");
                    newVisFlag.copyFrom(flagOld);
                }
                else
                {
                    System.out.println("A: "+vPanel.sourceShell.model.getName());
                }
                if( flagNew != null )
                {
                    newVisFlag.copyFrom(flagNew);
                }
                else
                {
                    System.out.println("B: "+vPanel.sourceShell.model.getName());
                }
                finalVisFlags.add(newVisFlag);
            }
            for( int i = 0; i < visComponents.size(); i++ )
            {
                VisibilityPane vPanel = visComponents.get(i);
                VisibilitySource temp = ((VisibilitySource)vPanel.sourceShell.source);
                AnimFlag visFlag = finalVisFlags.get(i);//might be null
                if( visFlag.size() > 0 )
                {
                    temp.setVisibilityFlag(visFlag);
                }
                else
                {
                    temp.setVisibilityFlag(null);
                }
            }
            
            importSuccess = true;
            
            //Tell program to set visibility after import
            MDLDisplay display = MainFrame.panel.displayFor(currentModel);
            if( display != null )
            {
                for( int i = 0; i < geosetTabs.getTabCount(); i++ )
                {
                    GeosetPanel gp = (GeosetPanel)geosetTabs.getComponentAt(i);
                    if( gp.doImport.isSelected() && gp.model == importedModel )
                    {
                        display.makeGeosetEditable(gp.geoset, true);
                        display.makeGeosetVisible(gp.geoset, true);
                    }
                }
                MainFrame.panel.geoControl.repaint();
                MainFrame.panel.geoControl.setMDLDisplay(display);
                display.mpanel.perspArea.reloadTextures();//addGeosets(newGeosets);
            }
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		ExceptionPopup.display(e);
    	}
        
        importEnded = true;
    }
    
    public boolean importSuccessful()
    {
    	return importSuccess;
    }
    public boolean importStarted()
    {
    	return importStarted;
    }
    public boolean importEnded()
    {
    	return importEnded;
    }
    public JFrame getParentFrame()
    {
    	return frame;
    }
    
    //*********************Simple Import Functions****************
    public void animTransfer()
    {
    	uncheckAllGeos.doClick();
    	motionFromBones.doClick();
    	clearExistingAnims.doClick();
    	uncheckAllObjs.doClick();
    	selSimButton.doClick();
    	
    	VisibilityShell corpseShell = null;//Try assuming it's a unit with a corpse; they'll tend to be that way
    	
    	//Iterate through new visibility sources, find a geoset with gutz material
    	for( int i = 0; i < visSourcesNew.size() && corpseShell == null; i++ )
    	{
        	if( visSourcesNew.get(i) instanceof VisibilityShell )
        	{
        		VisibilityShell vs = (VisibilityShell)visSourcesNew.get(i);
        		if( vs.source instanceof Geoset )
        		{
        			Geoset g = (Geoset)vs.source;
        			if( g.geosetAnim != null && g.material.firstLayer().firstTexture().getPath().equals("Textures\\gutz.blp"))
        			{
        				corpseShell = vs;
        			}
        		}
        	}
    	}
    	if( corpseShell != null )
    	{
    		for(int i = 0; i < visComponents.size(); i++ )
    		{
    			VisibilityPane vp = visComponents.get(i);
    			if( vp.sourceShell.source instanceof Geoset)
    			{
            		Geoset g = (Geoset)vp.sourceShell.source;
            		if( g.geosetAnim != null && g.material.firstLayer().firstTexture().getPath().equals("Textures\\gutz.blp"))
            		{
            			vp.newSourcesBox.setSelectedItem(corpseShell);
            		}
    			}
    		}
    	}
    	
    	okayButton.doClick();
    	
    	//visComponents.get(0).newSourcesBox.setSelectedItem
    }
}
class GeosetPanel extends JPanel implements ChangeListener
{
    //Geoset/Skin panel for controlling materials and geosets
    DefaultListModel materials;
    JList materialList;
    JScrollPane materialListPane;
    JCheckBox doImport;
    JLabel geoTitle;
    JLabel materialText;
    MDL model;
    Geoset geoset;
    int index;
    boolean isImported;
    MaterialListCellRenderer renderer;
    public GeosetPanel( boolean imported,//Is this Geoset an imported one, or an original?
                        MDL model,
                        int geoIndex,//which geoset is this for? (starts with 0)
                        DefaultListModel materials,
                        MaterialListCellRenderer renderer)
    {
        this.materials = materials;
        this.model = model;
        this.renderer = renderer;
        index = geoIndex;
        geoset = model.getGeoset(geoIndex);
        isImported = imported;
        
        geoTitle = new JLabel(model.getName()+" "+(index+1));
        geoTitle.setFont(new Font("Arial",Font.BOLD,26));
        
        doImport = new JCheckBox("Import this Geoset");
        doImport.setSelected(true);
        if( imported )
        {
            doImport.addChangeListener(this);
        }
        else
        {
            doImport.setEnabled(false);
        }
        
        materialText = new JLabel("Material:");
        //Header for materials list
        
        materialList = new JList(materials);
        materialList.setCellRenderer(renderer);
        materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialList.setSelectedValue(geoset.material,true);
        
        materialListPane = new JScrollPane(materialList);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(geoTitle)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(doImport)
                    .addComponent(materialText)
                    .addComponent(materialListPane)
                    )
                )
            .addGap(8)
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(geoTitle)
            .addGap(16)
            .addComponent(doImport)
            .addComponent(materialText)
            .addComponent(materialListPane)
            );
        setLayout(layout);
    }
    public void paintComponent(Graphics g)
    {
        renderer.setMaterial(geoset.material);
        super.paintComponent(g);
    }
    public void setSelected(boolean flag)
    {
        if( isImported )
        {
            doImport.setSelected(flag);
        }
    }
    public void stateChanged(ChangeEvent e)
    {
        materialText.setEnabled(doImport.isSelected());
        materialList.setEnabled(doImport.isSelected());
        materialListPane.setEnabled(doImport.isSelected());
        
        getImportPanel().informGeosetVisibility(geoset,doImport.isSelected());
    }
    ImportPanel impPanel;
    public ImportPanel getImportPanel()
    {
        if( impPanel == null )
        {
            Container temp = getParent();
            while( temp != null && temp.getClass() != ImportPanel.class )
            {
                temp = temp.getParent();
            }
            impPanel = (ImportPanel)temp;
        }
        return impPanel;
    }
    public Material getSelectedMaterial()
    {
        return (Material)materialList.getSelectedValue();
    }
}
class MaterialListCellRenderer extends DefaultListCellRenderer
{
    MDL myModel;
    Object myMaterial;
	Font theFont = new Font("Arial",Font.BOLD,32);
	HashMap<Material,ImageIcon> map = new HashMap<Material,ImageIcon>();
    public MaterialListCellRenderer(MDL model)
    {
        myModel = model;
    }
    public void setMaterial(Object o)
    {
        myMaterial = o;
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        String name = ((Material)value).getName();
        if( value == myMaterial )
        {
            name = name + " (Original)";
        }
        if( myModel.contains( (Material)value ) )
        {
            super.getListCellRendererComponent( list, name, index, iss, chf );
            
            ImageIcon myIcon = map.get((Material)value);
            if( myIcon == null )
            {
            	myIcon = new ImageIcon(Material.mergeImageScaled(ImportPanel.greenIcon.getImage(),((Material)value).getBufferedImage(),64,64,48,48));
            	map.put((Material)value, myIcon);
            }
            
            
            setIcon( myIcon );
        }
        else
        {
            super.getListCellRendererComponent( list, "Import: "+name, index, iss, chf );
            ImageIcon myIcon = map.get((Material)value);
            if( myIcon == null )
            {
            	myIcon = new ImageIcon(Material.mergeImageScaled(ImportPanel.orangeIcon.getImage(),((Material)value).getBufferedImage(),64,64,48,48));
            	map.put((Material)value, myIcon);
            }
            
            
            setIcon( myIcon );
        }
        setFont(theFont);
        return this;
    }
}

class AnimPanel extends JPanel implements ChangeListener, ItemListener, ListSelectionListener
{
    //Animation panel for controlling which are imported
    
    //title
    JLabel title;
    
    //Import option
    JCheckBox doImport;
    
    //Import option
    JCheckBox inReverse;
    
    //The animation for this panel
    Animation anim;
    
    static final String IMPORTBASIC = "Import as-is";
    static final String CHANGENAME = "Change name to:";
    static final String TIMESCALE = "Time-scale into pre-existing:";
    static final String GLOBALSEQ = "Rebuild as global sequence";
    
    String [] animOptions = { IMPORTBASIC, CHANGENAME, TIMESCALE, GLOBALSEQ };
    
    JComboBox importTypeBox = new JComboBox(animOptions);
    
    JPanel cardPane = new JPanel();
    
    JPanel blankCardImp = new JPanel();
    JPanel blankCardGS = new JPanel();
    
    JPanel nameCard = new JPanel();
        JTextField newNameEntry = new JTextField("",8);
    
    JPanel animListCard = new JPanel();
        DefaultListModel existingAnims;
        DefaultListModel listModel;
        JList animList;
        JScrollPane animListPane;
    
    public AnimPanel(   Animation anim,
                        DefaultListModel existingAnims,
                        AnimListCellRenderer renderer)
    {
        this.existingAnims = existingAnims;
        listModel = new DefaultListModel();
        for( int i = 0; i < existingAnims.size(); i++ )
        {
            this.listModel.addElement(existingAnims.get(i));
        }
        this.anim = anim;
        
        title = new JLabel(anim.getName());
        title.setFont(new Font("Arial",Font.BOLD,26));
        
        doImport = new JCheckBox("Import this Sequence");
        doImport.setSelected(true);
        doImport.addChangeListener(this);
        
        inReverse = new JCheckBox("Reverse");
        inReverse.setSelected(false);
        inReverse.addChangeListener(this);
        
        importTypeBox.setEditable(false);
        importTypeBox.addItemListener(this);
        importTypeBox.setMaximumSize(new Dimension(200,20));
        //Restricts users to pre-existing choices,
        // they cannot enter text in the box
        // (I think? that's an untested guess)
        
        //Combo box items:
        newNameEntry.setText(anim.getName());
        nameCard.add(newNameEntry);
        
        animList = new JList(this.listModel);
        animList.setCellRenderer(renderer);
        animList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //Use getSelectedValuesList().toArray() to request an array of selected animations
        
        //Select any animation found that has the same name automatically
        // -- This iterates through the list of old animations and picks out
        // and like-named ones, so that the default selection is any animation with the same name
        // (although this should stop after the first one is picked)
        animList.addListSelectionListener(this);
        for( int i = 0; i < existingAnims.size() && animList.getSelectedIndex() == -1; i++ )
        {
            Animation iAnim = ((AnimShell)listModel.get(i)).anim;
            if( iAnim.getName().toLowerCase().equals(anim.getName().toLowerCase()) )
            animList.setSelectedValue(listModel.get(i),true);
        }
        
        animListPane = new JScrollPane(animList);
        animListCard.add(animListPane);
        
        CardLayout cardLayout = new CardLayout();
        cardPane.setLayout(cardLayout);
        cardPane.add(blankCardImp,IMPORTBASIC);
        cardPane.add(nameCard,CHANGENAME);
        cardPane.add(animListPane,TIMESCALE);
        cardPane.add(blankCardGS,GLOBALSEQ);
//         cardLayout.show(cardPane,IMPORTBASIC);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(title)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(doImport)
                    .addComponent(inReverse)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(importTypeBox)
                        .addComponent(cardPane)
                        )
                    )
                )
            .addGap(8)
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addComponent(doImport)
            .addComponent(inReverse)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(importTypeBox)
                .addComponent(cardPane)
                )
            );
        setLayout(layout);
    }
    public void setSelected(boolean flag)
    {
        doImport.setSelected(flag);
    }
    public void stateChanged(ChangeEvent e)
    {
        importTypeBox.setEnabled(doImport.isSelected());
        cardPane.setEnabled(doImport.isSelected());
        animList.setEnabled(doImport.isSelected());
        newNameEntry.setEnabled(doImport.isSelected());
        updateSelectionPicks();
        
    }
    public void itemStateChanged(ItemEvent e)
    {
        // -- http://docs.oracle.com/javase/tutorial/uiswing/examples/layout/CardLayoutDemoProject/src/layout/CardLayoutDemo.java
        // -- http://docs.oracle.com/javase/tutorial/uiswing/layout/card.html
        //Thanks to the CardLayoutDemo.java at the above urls
        // in the JavaDocs for the example use of a CardLayout
        CardLayout myLayout = (CardLayout)cardPane.getLayout();
        myLayout.show(cardPane,(String)e.getItem());
        updateSelectionPicks();
    }
    Object [] oldSelection = new Object[0];
    public void updateSelectionPicks()
    {
        listenSelection = false;
//         DefaultListModel newModel = new DefaultListModel();
        Object [] selection = animList.getSelectedValuesList().toArray();
        listModel.clear();
        for( int i = 0; i < existingAnims.size(); i++ )
        {
            Animation temp = ((AnimShell)existingAnims.get(i)).importAnim;
            if( temp == null || temp == this.anim )
            listModel.addElement(existingAnims.get(i));
        }
//         for( int i = 0; i < existingAnims.size(); i++ )
//         {
//             newModel.addElement(existingAnims.get(i));
//         }
//         existingAnims.clear();
//         for( int i = 0; i < order.size(); i++ )
//         {
//             Object o = order.get(i);
//             if( newModel.contains(o) )
//             {
//                 existingAnims.addElement(o);
//             }
//         }
        int [] indices = new int[selection.length];
        for( int i = 0; i < selection.length; i++ )
        {
            indices[i] = listModel.indexOf(selection[i]);
        }
        animList.setSelectedIndices(indices);
        listenSelection = true;

        Object [] newSelection;
        if( doImport.isSelected() && importTypeBox.getSelectedIndex() == 2 )
        {
            newSelection = animList.getSelectedValuesList().toArray();
        }
        else
        {
            newSelection = new Object[0];
        }
//         ImportPanel panel = getImportPanel();
        for( Object a: oldSelection )
        {
            ((AnimShell)a).setImportAnim(null);
        }
        for( Object a: newSelection )
        {
            ((AnimShell)a).setImportAnim(this.anim);
        }
//         panel.addAnimPicks(oldSelection,this);
//         panel.removeAnimPicks(newSelection,this);
        oldSelection = newSelection;
    }
    boolean listenSelection = true;
    public void valueChanged(ListSelectionEvent e)
    {
        if( listenSelection && e.getValueIsAdjusting() )
        {
            updateSelectionPicks();
        }
    }
    public ImportPanel getImportPanel()
    {
        Container temp = getParent();
        while( temp.getClass() != ImportPanel.class && temp != null )
        {
            temp = temp.getParent();
        }
        return (ImportPanel)temp;
    }
    public void reorderToModel(DefaultListModel order)
    {
//         listenSelection = false;
//         DefaultListModel newModel = new DefaultListModel();
//         for( int i = 0; i < order.size(); i++ )
//         {
//             Object o = order.get(i);
//             if( this.existingAnims.contains(o) )
//             {
//                 newModel.addElement(o);
//             }
//         }
//         this.existingAnims = newModel;
//         animList.setModel(existingAnims);
//         int [] indices = new int[oldSelection.length];
//         for( int i = 0; i < oldSelection.length; i++ )
//         {
//             indices[i] = existingAnims.indexOf(oldSelection[i]);
//         }
//         animList.setSelectedIndices(indices);
//         listenSelection = true;
        
//         listenSelection = false;
//         DefaultListModel newModel = new DefaultListModel();
//         Object [] selection = animList.getSelectedValuesList().toArray();
//         for( int i = 0; i < existingAnims.size(); i++ )
//         {
//             newModel.addElement(existingAnims.get(i));
//         }
//         existingAnims.clear();
//         for( int i = 0; i < order.size(); i++ )
//         {
//             Object o = order.get(i);
//             if( newModel.contains(o) )
//             {
//                 existingAnims.addElement(o);
//             }
//         }
//         int [] indices = new int[selection.length];
//         for( int i = 0; i < selection.length; i++ )
//         {
//             indices[i] = existingAnims.indexOf(selection[i]);
//         }
//         animList.setSelectedIndices(indices);
//         listenSelection = true;
    }
}
class AnimListCellRenderer extends DefaultListCellRenderer
{
    public AnimListCellRenderer()
    {
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        super.getListCellRendererComponent( list, ((AnimShell)value).anim.getName(), index, iss, chf );
        setIcon( ImportPanel.animIcon );
        return this;
    }
}
class AnimShell
{
    Animation anim;
    Animation importAnim;
    public AnimShell(Animation anim)
    {
        this.anim = anim;
    }
    public void setImportAnim(Animation a)
    {
        importAnim = a;
    }
}
class BonePanel extends JPanel implements ItemListener, ListSelectionListener
{
    Bone bone;
    
    JLabel title;
    
    static final String IMPORT = "Import this bone";
    static final String MOTIONFROM = "Import motion to pre-existing:";
    static final String LEAVE = "Do not import";
    
    String [] impOptions = { IMPORT, MOTIONFROM, LEAVE };
    
    JComboBox importTypeBox = new JComboBox(impOptions);
    
    //List for which bone to transfer motion
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
    protected BonePanel()
    {
        //This constructor is negative mojo
    }
    public BonePanel( Bone whichBone, DefaultListModel existingBonesList, ListCellRenderer renderer, ImportPanel thePanel )
    {
        bone = whichBone;
        existingBones = existingBonesList;
        impPanel = thePanel;
        listModel = new DefaultListModel();
        for( int i = 0; i < existingBonesList.size(); i++ )
        {
            listModel.addElement(existingBonesList.get(i));
        }
        
        title = new JLabel(bone.getClass().getSimpleName()+" \""+bone.getName()+"\"");
        title.setFont( new Font("Arial",Font.BOLD,26));
        
        importTypeBox.setEditable(false);
        importTypeBox.addItemListener(this);
        importTypeBox.setMaximumSize(new Dimension(200,20));
        
        boneList = new JList(listModel);
        boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        boneList.setCellRenderer(renderer);
        boneList.addListSelectionListener(this);
        boneListPane = new JScrollPane(boneList);
        for( int i = 0; i < listModel.size(); i++ )
        {
            BoneShell bs = (BoneShell)listModel.get(i);
            if( bs.bone.getName().equals(bone.getName()) && bs.importBone == null )
            {
                boneList.setSelectedValue(bs,true);
                bs.setImportBone(bone);
                i = listModel.size();
//                 System.out.println("GREAT BALLS OF FIRE");
            }
        }
        
        futureBones = getImportPanel().getFutureBoneListExtended();
        futureBonesList = new JList(futureBones);
        futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        futureBonesListPane = new JScrollPane(futureBonesList);
        if( bone.parent != null )
        {
            parentTitle = new JLabel("Parent:      (Old Parent: "+bone.parent.getName()+")");
        }
        else
        {
            parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
        }
        
        add(importTypeBox);
        add(boneListPane);
        cardPanel = new JPanel(cards);
        cardPanel.add(boneListPane,"boneList");
        cardPanel.add(dummyPanel,"blank");
        cards.show(cardPanel,"blank");
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(title)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(importTypeBox)
                    .addComponent(cardPanel)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(parentTitle)
                        .addComponent(futureBonesListPane)
                        )
                )
            )
            .addGap(8)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(importTypeBox)
                .addComponent(cardPanel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(parentTitle)
                    .addComponent(futureBonesListPane)
                    )
            )
        );
        setLayout(layout);
    }
    public void initList()
    {
        futureBones = getImportPanel().getFutureBoneListExtended();
        for( int i = 0; i < futureBones.size(); i++ )
        {
            BoneShell bs = futureBones.get(i);
            if( bs.bone == bone.parent )
            {
                futureBonesList.setSelectedValue((Object)bs,true);
            }
        }
    }
    ImportPanel impPanel;
    public ImportPanel getImportPanel()
    {
        if( impPanel == null )
        {
            Container temp = getParent();
            while( temp != null && temp.getClass() != ImportPanel.class )
            {
                temp = temp.getParent();
            }
            impPanel = (ImportPanel)temp;
        }
        return impPanel;
    }
    public int getSelectedIndex()
    {
    	return importTypeBox.getSelectedIndex();
    }
    public void setSelectedIndex(int index)
    {
        importTypeBox.setSelectedIndex(index);
    }
    public void setSelectedValue(String value)
    {
        importTypeBox.setSelectedItem(value);
    }
    public void setParent(BoneShell pick)
    {
    	futureBonesList.setSelectedValue(pick, true);
    }
    Object [] oldSelection = new Object[0];
    public void updateSelectionPicks()
    {
        listenSelection = false;
//         DefaultListModel newModel = new DefaultListModel();
        Object [] selection = boneList.getSelectedValuesList().toArray();
        listModel.clear();
        for( int i = 0; i < existingBones.size(); i++ )
        {
            Bone temp = ((BoneShell)existingBones.get(i)).importBone;
            if( temp == null || temp == this.bone )
            listModel.addElement(existingBones.get(i));
        }
//         for( int i = 0; i < existingAnims.size(); i++ )
//         {
//             newModel.addElement(existingAnims.get(i));
//         }
//         existingAnims.clear();
//         for( int i = 0; i < order.size(); i++ )
//         {
//             Object o = order.get(i);
//             if( newModel.contains(o) )
//             {
//                 existingAnims.addElement(o);
//             }
//         }
        int [] indices = new int[selection.length];
        for( int i = 0; i < selection.length; i++ )
        {
            indices[i] = listModel.indexOf(selection[i]);
        }
        boneList.setSelectedIndices(indices);
        listenSelection = true;

        Object [] newSelection;
        if( importTypeBox.getSelectedIndex() == 1 )
        {
            newSelection = boneList.getSelectedValuesList().toArray();
        }
        else
        {
            newSelection = new Object[0];
        }
//         ImportPanel panel = getImportPanel();
        for( Object a: oldSelection )
        {
            ((BoneShell)a).setImportBone(null);
        }
        for( Object a: newSelection )
        {
            ((BoneShell)a).setImportBone(this.bone);
        }
//         panel.addAnimPicks(oldSelection,this);
//         panel.removeAnimPicks(newSelection,this);
        oldSelection = newSelection;
//         Object [] newSelection;
//         if( importTypeBox.getSelectedIndex() == 1 )
//         {
//             newSelection = boneList.getSelectedValuesList().toArray();
//         }
//         else
//         {
//             newSelection = new Object[0];
//         }
//         ImportPanel panel = getImportPanel();
//         panel.addBonePicks(oldSelection,this);
//         panel.removeBonePicks(newSelection,this);
// //         panel.reorderBonePicks(this);
//         oldSelection = newSelection;
        futureBones = getImportPanel().getFutureBoneListExtended();
    }
//     public void reorderToModel(DefaultListModel order)
//     {
//         listenSelection = false;
//         DefaultListModel newModel = new DefaultListModel();
//         Object [] selection = boneList.getSelectedValuesList().toArray();
//         for( int i = 0; i < existingBones.size(); i++ )
//         {
//             newModel.addElement(existingBones.get(i));
//         }
//         existingBones.clear();
//         for( int i = 0; i < order.size(); i++ )
//         {
//             Object o = order.get(i);
//             if( newModel.contains(o) )
//             {
//                 existingBones.addElement(o);
//             }
//         }
//         int [] indices = new int[selection.length];
//         for( int i = 0; i < selection.length; i++ )
//         {
//             indices[i] = existingBones.indexOf(selection[i]);
//         }
//         boneList.setSelectedIndices(indices);
//         listenSelection = true;
//     }
    public void itemStateChanged(ItemEvent e)
    {
//         boneListPane.setVisible(e.getItem() == MOTIONFROM);
//         boneList.setVisible(e.getItem() == MOTIONFROM);
        updateSelectionPicks();
        if( e.getItem() == MOTIONFROM )
        {
            cards.show(cardPanel,"boneList");
        }
        else
        {
            cards.show(cardPanel,"blank");
        }
        revalidate();
    }
    boolean listenSelection = true;
    public void valueChanged(ListSelectionEvent e)
    {
        if( listenSelection && e.getValueIsAdjusting() )
        updateSelectionPicks();
    }
}
class MultiBonePanel extends BonePanel
{
    JButton setAllParent;
    public MultiBonePanel( DefaultListModel existingBonesList, ListCellRenderer renderer )
    {
    	setAllParent = new JButton("Set Parent for All");
    	setAllParent.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e)
    		{
    			getImportPanel().setParentMultiBones();
    		}
    	});
        bone = null;
        this.existingBones = existingBonesList;
        
        title = new JLabel("Multiple Selected");
        title.setFont( new Font("Arial",Font.BOLD,26));
        
        importTypeBox.setEditable(false);
        importTypeBox.addItemListener(this);
        importTypeBox.setMaximumSize(new Dimension(200,20));
        
        boneList = new JList(existingBones);
        boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        boneList.setCellRenderer(renderer);
        boneListPane = new JScrollPane(boneList);
        
        add(importTypeBox);
        add(boneListPane);
        cardPanel = new JPanel(cards);
        cardPanel.add(boneListPane,"boneList");
        cardPanel.add(dummyPanel,"blank");
        boneList.setEnabled(false);
        cards.show(cardPanel,"blank");
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(title)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(importTypeBox)
                    .addComponent(cardPanel)
                    .addComponent(setAllParent)
                )
            )
            .addGap(8)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(importTypeBox)
                .addComponent(cardPanel)
                .addComponent(setAllParent)
            )
        );
        setLayout(layout);
    }
    public ImportPanel getImportPanel()
    {
        Container temp = getParent();
        while( temp.getClass() != ImportPanel.class && temp != null )
        {
            temp = temp.getParent();
        }
        return (ImportPanel)temp;
    }
    public void setSelectedIndex(int index)
    {
        listenForChange = false;
        importTypeBox.setSelectedIndex(index);
        listenForChange = true;
    }
    boolean listenForChange = true;
    public void setSelectedValue(String value)
    {
        listenForChange = false;
        importTypeBox.setSelectedItem(value);
        listenForChange = true;
    }
    public void setMultiTypes()
    {
        listenForChange = false;
        importTypeBox.setEditable(true);
        importTypeBox.setSelectedItem("Multiple selected");
        importTypeBox.setEditable(false);
//         boneListPane.setVisible(false);
//         boneList.setVisible(false);
        cards.show(cardPanel,"blank");
        revalidate();
        listenForChange = true;
    }
    public void itemStateChanged(ItemEvent e)
    {
//         boneListPane.setVisible(e.getItem() == MOTIONFROM);
//         boneList.setVisible(e.getItem() == MOTIONFROM);
        if( e.getItem() == MOTIONFROM )
        {
            cards.show(cardPanel,"boneList");
        }
        else
        {
            cards.show(cardPanel,"blank");
        }
        revalidate();
        if( listenForChange )
        {
            getImportPanel().setSelectedItem((String)e.getItem());
        }
    }
}
class BoneShellListCellRenderer extends DefaultListCellRenderer
{
    public BoneShellListCellRenderer()
    {
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        super.getListCellRendererComponent( list, ((BoneShell)value).bone.getClass().getSimpleName()+" \""+((BoneShell)value).bone.getName()+"\"", index, iss, chf );
        setIcon( ImportPanel.boneIcon );
        return this;
    }
}
class BoneListCellRenderer extends DefaultListCellRenderer
{
    public BoneListCellRenderer()
    {
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        super.getListCellRendererComponent( list, ((Bone)value).getClass().getSimpleName()+" \""+((Bone)value).getName()+"\"", index, iss, chf );
        setIcon( ImportPanel.boneIcon );
        return this;
    }
}
class ParentToggleRenderer extends DefaultListCellRenderer
{
    JCheckBox toggleBox;
    public ParentToggleRenderer(JCheckBox toggleBox)
    {
        this.toggleBox = toggleBox;
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        if( toggleBox.isSelected() )
        {
            if( ((BoneShell)value).bone.parent != null )
            {
                super.getListCellRendererComponent( list, value.toString()+"; "+((BoneShell)value).bone.parent.getName(), index, iss, chf );
            }
            else
            {
                super.getListCellRendererComponent( list, value.toString()+"; (no parent)", index, iss, chf );
            }
        }
        else
        {
            super.getListCellRendererComponent( list, value, index, iss, chf );
        }
        return this;
    }
}
class BoneShell
{
    Bone bone;
    Bone importBone;
    String modelName;
    BonePanel panel;
    boolean showClass = false;
    public BoneShell(Bone b)
    {
        bone = b;
    }
    public void setImportBone(Bone b)
    {
        importBone = b;
    }
    public String toString()
    {
        if( showClass )
        {
            if( modelName == null )
            {
                return bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"";
            }
            else
            {
                return modelName + ": " + bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"";
            }
        }
        else
        {
            if( modelName == null )
            {
                return bone.getName();
            }
            else
            {
                return modelName + ": " + bone.getName();
            }
        }
    }
}
class BonePanelListCellRenderer extends DefaultListCellRenderer
{
    public BonePanelListCellRenderer()
    {
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        super.getListCellRendererComponent( list, ((BonePanel)value).bone.getClass().getSimpleName()+" \""+((BonePanel)value).bone.getName()+"\"", index, iss, chf );
        setIcon( ImportPanel.cyanIcon );
        return this;
    }
}
class ObjPanelListCellRenderer extends DefaultListCellRenderer
{
    public ObjPanelListCellRenderer()
    {
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        super.getListCellRendererComponent( list, ((ObjectPanel)value).title.getText(), index, iss, chf );
        setIcon( ImportPanel.redIcon );
        return this;
    }
}
class GeosetAnimationPanel extends JTabbedPane
{
    //Geoset Animation panel for controlling bone attachments and visibility
    MDL model;
    Geoset geoset;
    boolean isImported;
    int index;
    BoneAttachmentPane bap;
    VisibilityPane vp;
    public GeosetAnimationPanel( boolean imported,//Is this Geoset an imported one, or an original?
                        MDL model,
                        int geoIndex,
                        ImportPanel thePanel)//which geoset is this for? (starts with 0)
    {
        this.model = model;
        impPanel = thePanel;
        index = geoIndex;
        geoset = model.getGeoset(geoIndex);
        isImported = imported;
        
        bap = new BoneAttachmentPane(model,geoset,null,getImportPanel());
        addTab("Bones",ImportPanel.boneIcon,bap,"Allows you to edit bone references.");
        
//         vp = new VisibilityPane(thePanel.currentModel.m_geosets.size(),thePanel.currentModel.getName(),thePanel.importedModel.m_geosets.size(),thePanel.importedModel.getName(),geoIndex);
//         addTab("Visibility",ImportPanel.animIcon,vp,"Allows you to edit visibility.");
    }
    public void refreshLists()
    {
        bap.refreshLists();
    }
    ImportPanel impPanel;
    public ImportPanel getImportPanel()
    {
        if( impPanel == null )
        {
            Container temp = getParent();
            while( temp != null && temp.getClass() != ImportPanel.class )
            {
                temp = temp.getParent();
            }
            impPanel = (ImportPanel)temp;
        }
        return impPanel;
    }
}
class MatrixShell
{
    Matrix matrix;
    ArrayList<BoneShell> newBones = new ArrayList<BoneShell>();
    public MatrixShell(Matrix m)
    {
        matrix = m;
        newBones = new ArrayList<BoneShell>();
    }
    public String toString()
    {
        return matrix.getName();
    }
}
class BoneAttachmentPane extends JPanel implements ActionListener, ListSelectionListener
{
    JLabel title;
    
    //Old bone refs (matrices)
    JLabel oldBoneRefsLabel;
    DefaultListModel<MatrixShell> oldBoneRefs;
    JList oldBoneRefsList;
    JScrollPane oldBoneRefsPane;
    
    //New refs
    JLabel newRefsLabel;
    DefaultListModel<BoneShell> newRefs;
    JList newRefsList;
    JScrollPane newRefsPane;
    JButton removeNewRef;
    JButton moveUp;
    JButton moveDown;
    
    
    
    //Bones (all available -- NEW AND OLD)
    JLabel bonesLabel;
    DefaultListModel<BoneShell> bones;
    JList bonesList;
    JScrollPane bonesPane;
    JButton useBone;
    
    
    MDL model;
    Geoset geoset;
    public BoneAttachmentPane( MDL model, Geoset whichGeoset, ListCellRenderer renderer, ImportPanel thePanel )
    {
        this.model = model;
        this.geoset = whichGeoset;
        impPanel = thePanel;
        
        
        bonesLabel = new JLabel("Bones");
        updateBonesList();
        //Built before oldBoneRefs, so that the MatrixShells can default to using New Refs with the same name as their first bone
        bonesList = new JList(bones);
        bonesList.setCellRenderer(renderer);
        bonesPane = new JScrollPane(bonesList);
        
        useBone = new JButton("Use Bone(s)",ImportPanel.greenArrowIcon);
        useBone.addActionListener(this);
        
        oldBoneRefsLabel = new JLabel("Old Bone References");
        buildOldRefsList();
        oldBoneRefsList = new JList(oldBoneRefs);
        oldBoneRefsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        oldBoneRefsList.addListSelectionListener(this);
        oldBoneRefsPane = new JScrollPane(oldBoneRefsList);
        
        
        newRefsLabel = new JLabel("New Refs");
        newRefs = new DefaultListModel<BoneShell>();
        newRefsList = new JList(newRefs);
        newRefsList.setCellRenderer(renderer);
        newRefsPane = new JScrollPane(newRefsList);
        
        removeNewRef = new JButton("Remove",ImportPanel.redXIcon);
        removeNewRef.addActionListener(this);
        moveUp = new JButton(ImportPanel.moveUpIcon);
        moveUp.addActionListener(this);
        moveDown = new JButton(ImportPanel.moveDownIcon);
        moveDown.addActionListener(this);
        
        buildLayout();
        
        refreshNewRefsList();
    }
    public void buildLayout()
    {
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(oldBoneRefsLabel)
                .addComponent(oldBoneRefsPane)
                )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(newRefsLabel)
                .addComponent(newRefsPane)
                .addComponent(removeNewRef)
                )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(moveUp)
                .addComponent(moveDown)
                )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(bonesLabel)
                .addComponent(bonesPane)
                .addComponent(useBone)
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(oldBoneRefsLabel)
                .addComponent(newRefsLabel)
                .addComponent(bonesLabel)
                )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(oldBoneRefsPane)
                .addComponent(newRefsPane)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(moveUp)
                    .addGap(16)
                    .addComponent(moveDown)
                    )
                .addComponent(bonesPane)
                )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(removeNewRef)
                .addComponent(useBone)
                )
            );
        setLayout(layout);
    }
    public void valueChanged(ListSelectionEvent e)
    {
        refreshLists();
    }
    public void actionPerformed(ActionEvent e)
    {
        if( e.getSource() == useBone )
        {
            for( Object o: bonesList.getSelectedValuesList() )
            {
                if( !newRefs.contains(o) )
                {
                    newRefs.addElement((BoneShell)o);
                }
            }
            refreshNewRefsList();
        }
        else if( e.getSource() == removeNewRef )
        {
            for( Object o: newRefsList.getSelectedValuesList() )
            {
                int i = newRefsList.getSelectedIndex();
                newRefs.removeElement(o);
                if( i > newRefs.size()-1 )
                {
                    i = newRefs.size()-1;
                }
                newRefsList.setSelectedIndex(i);
            }
            refreshNewRefsList();
        }
        else if( e.getSource() == moveUp )
        {
            int [] indices = newRefsList.getSelectedIndices();
            if( indices != null && indices.length > 0 )
            {
                if( indices[0] > 0 )
                {
                    for( int i = 0; i < indices.length; i++ )
                    {
                        BoneShell bs = newRefs.get(indices[i]);
                        newRefs.removeElement(bs);
                        newRefs.add(indices[i]-1,bs);
                        indices[i]-=1;
                    }
                }
                newRefsList.setSelectedIndices(indices);
            }
        }
        else if( e.getSource() == moveDown )
        {
            int [] indices = newRefsList.getSelectedIndices();
            if( indices != null && indices.length > 0 )
            {
                if( indices[indices.length-1] < newRefs.size()-1 )
                {
                    for( int i = indices.length-1; i >= 0; i-- )
                    {
                        BoneShell bs = newRefs.get(indices[i]);
                        newRefs.removeElement(bs);
                        newRefs.add(indices[i]+1,bs);
                        indices[i]+=1;
                    }
                }
                newRefsList.setSelectedIndices(indices);
            }
        }
    }
    public void refreshLists()
    {
        updateBonesList();
        refreshNewRefsList();
    }
    MatrixShell currentMatrix = null;
    public void refreshNewRefsList()
    {
        //Does save the currently constructed matrix
        java.util.List selection = newRefsList.getSelectedValuesList();
        if( currentMatrix != null )
        {
            currentMatrix.newBones.clear();
            for( Object bs: newRefs.toArray() )
            {
                currentMatrix.newBones.add((BoneShell)bs);
            }
        }
        newRefs.clear();
        if( oldBoneRefsList.getSelectedValue() != null )
        {
            for( BoneShell bs: ((MatrixShell)oldBoneRefsList.getSelectedValue()).newBones )
            {
                if( bones.contains(bs) )
                newRefs.addElement(bs);
            }
        }
        
        int [] indices = new int[selection.size()];
        for( int i = 0; i < selection.size(); i++ )
        {
            indices[i] = newRefs.indexOf(selection.get(i));
        }
        newRefsList.setSelectedIndices(indices);
        currentMatrix = (MatrixShell)oldBoneRefsList.getSelectedValue();
    }
    public void reloadNewRefsList()
    {
        //Does not save the currently constructed matrix
        java.util.List selection = newRefsList.getSelectedValuesList();
        newRefs.clear();
        if( oldBoneRefsList.getSelectedValue() != null )
        {
            for( BoneShell bs: ((MatrixShell)oldBoneRefsList.getSelectedValue()).newBones )
            {
                if( bones.contains(bs) )
                newRefs.addElement(bs);
            }
        }
        
        int [] indices = new int[selection.size()];
        for( int i = 0; i < selection.size(); i++ )
        {
            indices[i] = newRefs.indexOf(selection.get(i));
        }
        newRefsList.setSelectedIndices(indices);
        currentMatrix = (MatrixShell)oldBoneRefsList.getSelectedValue();
    }
    public void buildOldRefsList()
    {
        if( oldBoneRefs == null )
        {
            oldBoneRefs = new DefaultListModel<MatrixShell>();
        }
        else
        {
            oldBoneRefs.clear();
        }
        for( Matrix m: geoset.m_matrix )
        {
            MatrixShell ms = new MatrixShell(m);
            //For look to find similarly named stuff and add it
            for( Object bs: bones.toArray() )
            {
//                 try {
                    for( Bone b: m.bones )
                    {
                        String mName = b.getName();
                        if( ((BoneShell)bs).bone == b )//.getName().equals(mName) )
                        {
                            ms.newBones.add((BoneShell)bs);
                        }
                    }
//                 }
//                 catch (NullPointerException e)
//                 {
//                     System.out.println("We have a null in a matrix process, probably not good but it was assumed that this might happen.");
//                 }
            }
            oldBoneRefs.addElement(ms);
        }
    }
    public void resetMatrices()
    {
        for( int i = 0; i < oldBoneRefs.size(); i++ )
        {
            MatrixShell ms = oldBoneRefs.get(i);
            ms.newBones.clear();
            Matrix m = ms.matrix;
            //For look to find right stuff and add it
            for( Object bs: bones.toArray() )
            {
                    for( Bone b: m.bones )
                    {
                        if( ((BoneShell)bs).bone == b )//.getName().equals(mName) )
                        {
                            ms.newBones.add((BoneShell)bs);
                        }
                    }
            }
        }
        reloadNewRefsList();
    }
    public void setMatricesToSimilarNames()
    {
        for( int i = 0; i < oldBoneRefs.size(); i++ )
        {
            MatrixShell ms = oldBoneRefs.get(i);
            ms.newBones.clear();
            Matrix m = ms.matrix;
            //For look to find similarly named stuff and add it
            for( Object bs: bones.toArray() )
            {
                    for( Bone b: m.bones )
                    {
                        String mName = b.getName();
                        if( ((BoneShell)bs).bone.getName().equals(mName) )
                        {
                            ms.newBones.add((BoneShell)bs);
                        }
                    }
            }
        }
        reloadNewRefsList();
    }
    public void updateBonesList()
    {
        bones = getImportPanel().getFutureBoneList();
    }
    ImportPanel impPanel;
    public ImportPanel getImportPanel()
    {
        if( impPanel == null )
        {
            Container temp = getParent();
            while( temp != null && temp.getClass() != ImportPanel.class )
            {
                temp = temp.getParent();
            }
            impPanel = (ImportPanel)temp;
        }
        return impPanel;
    }
}
class ObjectPanel extends JPanel
{
    JLabel title;
    
    IdObject object;
    Camera camera;
    JCheckBox doImport;
    JLabel parentLabel;
    JLabel oldParentLabel;
    DefaultListModel<BoneShell> parents;
    JList parentsList;
    JScrollPane parentsPane;
    
    protected ObjectPanel()
    {
        
    }
    public ObjectPanel(IdObject whichObject, DefaultListModel<BoneShell> possibleParents )
    {
        object = whichObject;
        
        title = new JLabel(object.getClass().getSimpleName()+" \""+object.getName()+"\"");
        title.setFont(new Font("Arial",Font.BOLD,26));
        
        doImport = new JCheckBox("Import this object");
        doImport.setSelected(true);
        parentLabel = new JLabel("Parent:");
        if( object.parent != null )
        {
            oldParentLabel = new JLabel("(Old Parent: "+object.parent.getName()+")");
        }
        else
        {
            oldParentLabel = new JLabel("(Old Parent: {no parent})");
        }
        
        parents = possibleParents;
        parentsList = new JList(parents);
        parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for( int i = 0; i < parents.size(); i++ )
        {
            BoneShell bs = parents.get(i);
            if( bs.bone == object.parent )
            {
                parentsList.setSelectedValue((Object)bs,true);
            }
        }
        
        parentsPane = new JScrollPane(parentsList);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(title)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(doImport)
                .addComponent(oldParentLabel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(parentLabel)
                    .addComponent(parentsPane)
                    )
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addComponent(doImport)
            .addComponent(oldParentLabel)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(parentLabel)
                .addComponent(parentsPane)
                )
            );
        setLayout(layout);
    }
    public ObjectPanel(Camera c)
    {
        camera = c;
        
        title = new JLabel(c.getClass().getSimpleName()+" \""+c.getName()+"\"");
        title.setFont(new Font("Arial",Font.BOLD,26));
        
        doImport = new JCheckBox("Import this object");
        doImport.setSelected(true);
        parentLabel = new JLabel("Parent:");
        oldParentLabel = new JLabel("(Cameras don't have parents)");
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(title)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(doImport)
                .addComponent(oldParentLabel)
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addComponent(doImport)
            .addComponent(oldParentLabel)
            );
        setLayout(layout);
    }
}
class MultiObjectPanel extends ObjectPanel implements ChangeListener
{
    public MultiObjectPanel(DefaultListModel<BoneShell> possibleParents )
    {
        title = new JLabel("Multiple Selected");
        title.setFont(new Font("Arial",Font.BOLD,26));
        
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
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(title)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(doImport)
                .addComponent(oldParentLabel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(parentLabel)
                    .addComponent(parentsPane)
                    )
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addComponent(doImport)
            .addComponent(oldParentLabel)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(parentLabel)
                .addComponent(parentsPane)
                )
            );
        setLayout(layout);
    }
    boolean oldVal = true;
    public void stateChanged(ChangeEvent e)
    {
        if( doImport.isSelected() != oldVal )
        {
            getImportPanel().setObjGroupSelected(doImport.isSelected());
            oldVal = doImport.isSelected();
        }
    }
    ImportPanel impPanel;
    public ImportPanel getImportPanel()
    {
        if( impPanel == null )
        {
            Container temp = getParent();
            while( temp != null && temp.getClass() != ImportPanel.class )
            {
                temp = temp.getParent();
            }
            impPanel = (ImportPanel)temp;
        }
        return impPanel;
    }
}
class VisibilityShell
{
    Named source;
    MDL model;
    public VisibilityShell(Named n, MDL whichModel)
    {
        source = n;
        model = whichModel;
    }
}
class VisPaneListCellRenderer extends DefaultListCellRenderer
{
    MDL current;
    public VisPaneListCellRenderer(MDL whichModel)
    {
        current = whichModel;
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        super.getListCellRendererComponent( list, ((VisibilityPane)value).sourceShell.model.getName()+": "+((VisibilityPane)value).sourceShell.source.getName(), index, iss, chf );
        if( current == ((VisibilityPane)value).sourceShell.model )
        {
            setIcon( ImportPanel.greenIcon );
        }
        else
        {
            setIcon( ImportPanel.orangeIcon );
        }
        return this;
    }
}
class VisShellBoxCellRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer
{
    public VisShellBoxCellRenderer()
    {
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        if( value == null )
        {
            return getListCellRendererComponent( list, "NULL ERROR", index, iss, chf );
        }
        if( value.getClass() != String.class )
        {
            super.getListCellRendererComponent( list, ((VisibilityShell)value).model.getName()+": "+((VisibilityShell)value).source.getName(), index, iss, chf );
        }
        else
        {
            super.getListCellRendererComponent( list, value, index, iss, chf );
        }
        return this;
    }
}
class VisibilityPane extends JPanel
{
    static final String NOTVISIBLE = "Not visible";
    static final String VISIBLE = "Always visible";
    JLabel oldAnimsLabel;
    JComboBox oldSourcesBox;
    JLabel newAnimsLabel;
    JComboBox newSourcesBox;
    JCheckBox favorOld;
    VisibilityShell sourceShell;
    
    JLabel title;
    protected VisibilityPane()
    {
        //for use in multi pane
    }
    public VisibilityPane(VisibilityShell sourceShell, DefaultComboBoxModel oldSources, DefaultComboBoxModel newSources, ListCellRenderer renderer )
    {
        this.sourceShell = sourceShell;
        title = new JLabel(sourceShell.model.getName()+": "+sourceShell.source.getName());
        title.setFont(new Font("Arial",Font.BOLD,26));
        
        oldAnimsLabel = new JLabel("Existing animation visibility from: ");
        oldSourcesBox = new JComboBox(oldSources);
        oldSourcesBox.setEditable(false);
        oldSourcesBox.setMaximumSize(new Dimension(1000,25));
        oldSourcesBox.setRenderer(renderer);
        boolean didContain = false;
        for( int i = 0; i < oldSources.getSize() && !didContain; i++ )
        {
            if( sourceShell == oldSources.getElementAt(i) )
            {
                didContain = true;
            }
        }
        if( didContain )
        {
            oldSourcesBox.setSelectedItem(sourceShell);
        }
        else
        {
            oldSourcesBox.setSelectedItem(VISIBLE);
        }
        
        newAnimsLabel = new JLabel("Imported animation visibility from: ");
        newSourcesBox = new JComboBox(newSources);
        newSourcesBox.setEditable(false);
        newSourcesBox.setMaximumSize(new Dimension(1000,25));
        newSourcesBox.setRenderer(renderer);
        didContain = false;
        for( int i = 0; i < newSources.getSize() && !didContain; i++ )
        {
            if( sourceShell == newSources.getElementAt(i) )
            {
                didContain = true;
            }
        }
        if( didContain )
        {
            newSourcesBox.setSelectedItem(sourceShell);
        }
        else
        {
            newSourcesBox.setSelectedItem(VISIBLE);
        }
        
        favorOld = new JCheckBox("Favor component's original visibility when combining");
        favorOld.setSelected(true);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(title)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(oldAnimsLabel)
                .addComponent(oldSourcesBox)
                .addComponent(newAnimsLabel)
                .addComponent(newSourcesBox)
                .addComponent(favorOld)
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addComponent(oldAnimsLabel)
            .addComponent(oldSourcesBox)
            .addComponent(newAnimsLabel)
            .addComponent(newSourcesBox)
            .addComponent(favorOld)
            );
        setLayout(layout);
    }
    public void selectSimilarOptions()
    {
        VisibilityShell temp = null;
        ListModel oldSources = oldSourcesBox.getModel();
        for( int i = 0; i < oldSources.getSize(); i++ )
        {
            if( ! (oldSources.getElementAt(i) instanceof String) )
            if( sourceShell.source.getName().equals(((VisibilityShell)oldSources.getElementAt(i)).source.getName()) )
            {
                System.out.println(sourceShell.source.getName());
                oldSourcesBox.setSelectedItem(oldSources.getElementAt(i));
            }
        }
        ListModel newSources = newSourcesBox.getModel();
        for( int i = 0; i < newSources.getSize(); i++ )
        {
            if( ! (newSources.getElementAt(i) instanceof String) )
            if( sourceShell.source.getName().equals(((VisibilityShell)newSources.getElementAt(i)).source.getName()) )
            {
                System.out.println(sourceShell.source.getName());
                newSourcesBox.setSelectedItem(newSources.getElementAt(i));
            }
        }
    }
}
class MultiVisibilityPane extends VisibilityPane implements ChangeListener, ItemListener
{
    public MultiVisibilityPane(DefaultComboBoxModel oldSources, DefaultComboBoxModel newSources, ListCellRenderer renderer )
    {
        title = new JLabel("Multiple Selected");
        title.setFont(new Font("Arial",Font.BOLD,26));
        
        oldAnimsLabel = new JLabel("Existing animation visibility from: ");
        oldSourcesBox = new JComboBox(oldSources);
        oldSourcesBox.setEditable(false);
        oldSourcesBox.setMaximumSize(new Dimension(1000,25));
        oldSourcesBox.setRenderer(renderer);
        oldSourcesBox.addItemListener(this);
        
        newAnimsLabel = new JLabel("Imported animation visibility from: ");
        newSourcesBox = new JComboBox(newSources);
        newSourcesBox.setEditable(false);
        newSourcesBox.setMaximumSize(new Dimension(1000,25));
        newSourcesBox.setRenderer(renderer);
        newSourcesBox.addItemListener(this);
        
        favorOld = new JCheckBox("Favor component's original visibility when combining");
        favorOld.setSelected(true);
        favorOld.addChangeListener(this);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(title)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(oldAnimsLabel)
                .addComponent(oldSourcesBox)
                .addComponent(newAnimsLabel)
                .addComponent(newSourcesBox)
                .addComponent(favorOld)
                )
            );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addGap(16)
            .addComponent(oldAnimsLabel)
            .addComponent(oldSourcesBox)
            .addComponent(newAnimsLabel)
            .addComponent(newSourcesBox)
            .addComponent(favorOld)
            );
        setLayout(layout);
    }
    boolean oldVal = true;
    public void stateChanged(ChangeEvent e)
    {
        if( favorOld.isSelected() != oldVal )
        {
            getImportPanel().setVisGroupSelected(favorOld.isSelected());
            oldVal = favorOld.isSelected();
        }
    }
    public void itemStateChanged(ItemEvent e)
    {
        if( e.getSource() == oldSourcesBox )
        getImportPanel().setVisGroupItemOld(oldSourcesBox.getSelectedItem());
        if( e.getSource() == newSourcesBox )
        getImportPanel().setVisGroupItemNew(newSourcesBox.getSelectedItem());
    }
    public void setMultipleOld()
    {
        oldSourcesBox.setEditable(true);
        oldSourcesBox.setSelectedItem("Multiple selected");
        oldSourcesBox.setEditable(false);
    }
    public void setMultipleNew()
    {
        newSourcesBox.setEditable(true);
        newSourcesBox.setSelectedItem("Multiple selected");
        newSourcesBox.setEditable(false);
    }
    ImportPanel impPanel;
    public ImportPanel getImportPanel()
    {
        if( impPanel == null )
        {
            Container temp = getParent();
            while( temp != null && temp.getClass() != ImportPanel.class )
            {
                temp = temp.getParent();
            }
            impPanel = (ImportPanel)temp;
        }
        return impPanel;
    }
}