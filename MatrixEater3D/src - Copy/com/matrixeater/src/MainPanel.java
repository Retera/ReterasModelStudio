package com.matrixeater.src;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import com.matrixeater.data.DirectorySelector;
import com.matrixeater.data.SaveProfile;
import com.matrixeater.imp.ImportPanelSimple;
/**
 * Write a description of class MainPanel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MainPanel extends JPanel implements ActionListener, MouseListener, ChangeListener
{
    ModeButton selectButton,addButton,deselectButton,moveButton,rotateButton,scaleButton,extrudeButton,extendButton,snapButton,deleteButton,cloneButton, xButton, yButton, zButton;
    ArrayList<ModeButton> buttons = new ArrayList<ModeButton>();
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, modelMenu, mirrorSubmenu, viewMenu, aboutMenu;
    JCheckBoxMenuItem mirrorFlip, showNormals, textureModels, showVertexModifyControls;
    ArrayList geoItems = new ArrayList();
    JMenuItem open, save, showController, mergeGeoset, saveAs, importButton, importButtonS, newDirectory, creditsButton, nullmodelButton, selectAll, invertSelect, expandSelection, mirrorX, mirrorY, mirrorZ, insideOut, showMatrices, editUVs;
    UndoMenuItem undo;
    RedoMenuItem redo;
    
    JMenu viewMode;
    JRadioButtonMenuItem wireframe, solid;
    ButtonGroup viewModes;
    
    JFileChooser fc;
    FileFilter filter;
    File filterFile;
    File currentFile;
    ImportPanel importPanel;
    static final ImageIcon MDLIcon = new ImageIcon(MainPanel.class.getResource("ImageBin/MDLIcon_16.png"));
    int c = 0;
    boolean loading;
    JTabbedPane tabbedPane;
    ViewController geoControl;
    JTextField [] mouseCoordDisplay = new JTextField[3];
    int selectionType = 0;
    boolean cheatShift = false;
    boolean cheatAlt = false;
    int actionType = 3;
    Color activeRColor1 = new Color(200,255,200);
    Color activeRColor2 = new Color(60,170,0);
    Color activeColor1 = new Color(255,200,200);
    Color activeColor2 = new Color(170,60,0);
    Color activeBColor1 = new Color(200,200,255);
    Color activeBColor2 = new Color(0,60,170);
    boolean cloneOn = false;
    SaveProfile profile = SaveProfile.get();
    public boolean showNormals()
    {
    	return showNormals.isSelected();
    }
    public boolean showVMControls()
    {
    	return showVertexModifyControls.isSelected();
    }
    public boolean textureModels()
    {
    	return textureModels.isSelected();
    }
    public int viewMode()
    {
    	if( wireframe.isSelected() ){
    		return 0;
    	}
    	else if( solid.isSelected() ){
    		return 1;
    	}
    	return -1;
    }
    public void setCloneOn(boolean flag)
    {
        cloneOn = flag;
        if( cloneOn )
        {
            cloneButton.setColors(activeBColor1,activeBColor2);
        }
        else
        {
            cloneButton.resetColors();
        }
    }
    
    boolean [] dimLocks = new boolean[3];
    public ModeButton getDLockButton(int x)
    {
    	switch (x)
    	{
    	case 0:
    		return zButton;
    	case 1:
    		return xButton;
    	case 2:
    		return yButton;
    	}
    	return null;
    }
    public void setDimLock(int x, boolean flag)
    {
        dimLocks[x] = flag;
        if( dimLocks[x] )
        {
            getDLockButton(x).setColors(activeBColor1,activeBColor2);
        }
        else
        {
        	getDLockButton(x).resetColors();
        }
    }
    public boolean getDimLock(int dim)
    {
    	return dimLocks[dim];
    }
    
    JMenuItem contextClose;
    int contextClickedTab = 0;
    JPopupMenu contextMenu;
    AbstractAction undoAction = new AbstractAction("Undo")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().undo();
                    refreshUndo();
                    repaint();
                    geoControl.repaint();
                    if(mpanel.getMDLDisplay().uvpanel != null)
                    	mpanel.getMDLDisplay().uvpanel.repaint();
                }
            };
    AbstractAction redoAction = new AbstractAction("Redo")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().redo();
                    refreshUndo();
                    repaint();
                    geoControl.repaint();
                    if(mpanel.getMDLDisplay().uvpanel != null)
                    	mpanel.getMDLDisplay().uvpanel.repaint();
                }
            };
    AbstractAction deleteAction = new AbstractAction("Delete")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().delete();
                    repaint();
                    geoControl.repaint();
                    if(mpanel.getMDLDisplay().uvpanel != null)
                    	mpanel.getMDLDisplay().uvpanel.repaint();
                }
            };
    AbstractAction selectAllAction = new AbstractAction("Select All")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().selectAll();
                    repaint();
                }
            };
    AbstractAction invertSelectAction = new AbstractAction("Invert Selection")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().invertSelection();
                    repaint();
                }
            };
    AbstractAction expandSelectionAction = new AbstractAction("Expand Selection")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().expandSelection();
                    repaint();
                }
            };
    AbstractAction mirrorXAction = new AbstractAction("Mirror X")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().mirror((byte)1,mirrorFlip.isSelected());
                    repaint();
                }
            };
    AbstractAction mirrorYAction = new AbstractAction("Mirror Y")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().mirror((byte)2,mirrorFlip.isSelected());
                    repaint();
                }
            };
    AbstractAction mirrorZAction = new AbstractAction("Mirror Z")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().mirror((byte)0,mirrorFlip.isSelected());
                    repaint();
                }
            };
    AbstractAction insideOutAction = new AbstractAction("Inside Out")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().insideOut();
                    repaint();
                }
            };
    AbstractAction viewMatricesAction = new AbstractAction("View Matrices")
            {
                public void actionPerformed(ActionEvent e)
                {
                    ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
                    if( mpanel != null )
                    mpanel.getMDLDisplay().viewMatrices();
                    repaint();
                }
            };
    public MainPanel()
    {
        super();
        
//         testArea = new PerspDisplayPanel("Graphic Test",2,0);
//         //botArea.setViewport(0,1);
//         add(testArea);
        
        selectButton = new ModeButton("Select");
        addButton = new ModeButton("Add");
        deselectButton = new ModeButton("Deselect");
        JLabel [] divider = new JLabel[3];
        for( int i = 0; i < divider.length; i++ )
        {
            divider[i] = new JLabel("----------");
        }
        for( int i = 0; i < mouseCoordDisplay.length; i++ )
        {
            mouseCoordDisplay[i] = new JTextField("");
            mouseCoordDisplay[i].setMaximumSize(new Dimension(80,18));
            mouseCoordDisplay[i].setMinimumSize(new Dimension(50,15));
            mouseCoordDisplay[i].setEditable(false);
        }
        moveButton = new ModeButton("Move");
        rotateButton = new ModeButton("Rotate");
        scaleButton = new ModeButton("Scale");
        extrudeButton = new ModeButton("Extrude");
        extendButton = new ModeButton("Extend");
        extendButton.setToolTipText("A modified version of extrude that favors creating less faces.");
        snapButton = new ModeButton("Snap");
        deleteButton = new ModeButton("Delete");
        cloneButton = new ModeButton("Clone");
        xButton = new ModeButton("X");
        yButton = new ModeButton("Y");
        zButton = new ModeButton("Z");
        
        contextMenu = new JPopupMenu();
        contextClose = new JMenuItem("Close");
        contextClose.addActionListener(this);
        contextMenu.add(contextClose);
        
        buttons.add(selectButton);
        buttons.add(addButton);
        buttons.add(deselectButton);
        buttons.add(moveButton);
        buttons.add(rotateButton);
        buttons.add(scaleButton);
        buttons.add(extrudeButton);
        buttons.add(extendButton);
        buttons.add(snapButton);
        buttons.add(deleteButton);
        buttons.add(cloneButton);
        
        xButton.addActionListener(this);
        xButton.setMaximumSize(new Dimension(26,26));
        xButton.setMinimumSize(new Dimension(20,20));
        xButton.setMargin(new Insets(0, 0, 0, 0));
        yButton.addActionListener(this);
        yButton.setMaximumSize(new Dimension(26,26));
        yButton.setMinimumSize(new Dimension(20,20));
        yButton.setMargin(new Insets(0, 0, 0, 0));
        zButton.addActionListener(this);
        zButton.setMaximumSize(new Dimension(26,26));
        zButton.setMinimumSize(new Dimension(20,20));
        zButton.setMargin(new Insets(0, 0, 0, 0));
        
        for( int i = 0; i < buttons.size(); i++ )
        {
            buttons.get(i).setMaximumSize(new Dimension(100,35));
            buttons.get(i).setMinimumSize(new Dimension(90,15));
            if( buttons.get(i) != deleteButton )
            {
                buttons.get(i).addActionListener(this);
            }
            else
            {
                buttons.get(i).addActionListener(deleteAction);
            }
        }
        
        
        
        tabbedPane = new DnDTabbedPane();
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(tabbedPane)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(mouseCoordDisplay[0])
                    .addComponent(mouseCoordDisplay[1])
                    .addComponent(mouseCoordDisplay[2])
                    )
                )
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(selectButton)
                .addComponent(addButton)
                .addComponent(deselectButton)
                .addComponent(divider[0])
                .addComponent(moveButton)
                .addComponent(rotateButton)
                .addComponent(scaleButton)
                .addComponent(extrudeButton)
                .addComponent(extendButton)
                .addComponent(divider[1])
                .addComponent(snapButton)
                .addComponent(deleteButton)
                .addComponent(cloneButton)
                .addComponent(divider[2])
                .addGroup(layout.createSequentialGroup()
                    	.addComponent(xButton)
                    	.addComponent(yButton)
                    	.addComponent(zButton)
                	)
                )
            );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabbedPane)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(mouseCoordDisplay[0])
                    .addComponent(mouseCoordDisplay[1])
                    .addComponent(mouseCoordDisplay[2])
                    )
                )
            .addGroup(layout.createSequentialGroup()
                .addComponent(selectButton)
                .addGap(8)
                .addComponent(addButton)
                .addGap(8)
                .addComponent(deselectButton)
                .addGap(8)
                .addComponent(divider[0])
                .addGap(8)
                .addComponent(moveButton)
                .addGap(8)
                .addComponent(rotateButton)
                .addGap(8)
                .addComponent(scaleButton)
                .addGap(8)
                .addComponent(extrudeButton)
                .addGap(8)
                .addComponent(extendButton)
                .addGap(8)
                .addComponent(divider[1])
                .addGap(8)
                .addComponent(snapButton)
                .addGap(8)
                .addComponent(deleteButton)
                .addGap(8)
                .addComponent(cloneButton)
                .addGap(8)
                .addComponent(divider[2])
                .addGap(8)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    	.addComponent(xButton)
                    	.addComponent(yButton)
                    	.addComponent(zButton)
                	)
                )
            );
        setLayout(layout);
        //Create a file chooser
        fc = new JFileChooser();
        filterFile = new File("",".mdl");
        filter = new MDLFilter();
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(filter);
        tabbedPane.addChangeListener(this);
        
//         getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );
        
        
        //         getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );
        
        tabbedPane.addMouseListener(this);
//         setFocusable(true);
//         selectButton.requestFocus();
    }
    public void init()
    {
        buttons.get(0).setColors(activeColor1,activeColor2);
        buttons.get(3).setColors(activeRColor1,activeRColor2);
        JRootPane root = getRootPane();
//         JPanel root = this;
        root.getActionMap().put("Undo",
            undoAction);
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"),"Undo");

        root.getActionMap().put("Redo",
            redoAction);
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"),"Redo");
        
        root.getActionMap().put("Delete",
            deleteAction);
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"),"Delete");
        
        root.getActionMap().put("shiftSelect",
            new AbstractAction("shiftSelect")
            {
                public void actionPerformed(ActionEvent e)
                {
                    if( selectionType == 0 )
                    {
                        for( int b = 0 ; b < 3; b++ )
                        {
                            buttons.get(b).resetColors();
                        }
                        addButton.setColors(activeColor1,activeColor2);
                        selectionType = 1;
                        cheatShift = true;
                    }
                }
            });
        root.getActionMap().put("altSelect",
                new AbstractAction("altSelect")
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if( selectionType == 0 )
                        {
                            for( int b = 0 ; b < 3; b++ )
                            {
                                buttons.get(b).resetColors();
                            }
                            deselectButton.setColors(activeColor1,activeColor2);
                            selectionType = 2;
                            cheatAlt = true;
                        }
                    }
                });
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift pressed SHIFT"),"shiftSelect");
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control pressed CONTROL"),"shiftSelect");
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"),"altSelect");

        root.getActionMap().put("unShiftSelect",
            new AbstractAction("unShiftSelect")
            {
                public void actionPerformed(ActionEvent e)
                {
                    if( selectionType == 1 && cheatShift )
                    {
                        for( int b = 0 ; b < 3; b++ )
                        {
                            buttons.get(b).resetColors();
                        }
                        selectButton.setColors(activeColor1,activeColor2);
                        selectionType = 0;
                        cheatShift = false;
                    }
                }
            });
        root.getActionMap().put("unAltSelect",
                new AbstractAction("unAltSelect")
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if( selectionType == 2 && cheatAlt )
                        {
                            for( int b = 0 ; b < 3; b++ )
                            {
                                buttons.get(b).resetColors();
                            }
                            selectButton.setColors(activeColor1,activeColor2);
                            selectionType = 0;
                            cheatAlt = false;
                        }
                    }
                });
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"),"unShiftSelect");
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released CONTROL"),"unShiftSelect");
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"),"unAltSelect");
        
        
        root.getActionMap().put("Select All",
            selectAllAction);
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"),"Select All");
        
        root.getActionMap().put("Invert Selection",
            invertSelectAction);
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"),"Invert Selection");
        
        root.getActionMap().put("Expand Selection",
            expandSelectionAction);
        root.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"),"Expand Selection");
    }
    
    public JMenuBar createMenuBar()
    {
        //Create my menu bar
        menuBar = new JMenuBar();
        
        //Build the file menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("Allows the user to open, save, close, and manipulate files.");
        menuBar.add(fileMenu);
        
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.addMouseListener(this);
        editMenu.getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
        menuBar.add(editMenu);

        modelMenu = new JMenu("Tools");
        modelMenu.setMnemonic(KeyEvent.VK_T);
        modelMenu.getAccessibleContext().setAccessibleDescription("Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");
        modelMenu.setEnabled(false);
        menuBar.add(modelMenu);

        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");
        menuBar.add(viewMenu);
        
        aboutMenu = new JMenu("Help");
        aboutMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(aboutMenu);

        creditsButton = new JMenuItem("About");
        creditsButton.setMnemonic(KeyEvent.VK_A);
        creditsButton.addActionListener(this);
        aboutMenu.add(creditsButton);
        
        showMatrices = new JMenuItem("View Selected \"Matrices\"");
        showMatrices.setMnemonic(KeyEvent.VK_V);
        showMatrices.addActionListener(viewMatricesAction);
        modelMenu.add(showMatrices);
        
        insideOut = new JMenuItem("Flip all selected faces");
        insideOut.setMnemonic(KeyEvent.VK_I);
        insideOut.addActionListener(insideOutAction);
        modelMenu.add(insideOut);
        
        modelMenu.add(new JSeparator());

        editUVs = new JMenuItem("Edit UV Mapping");
        editUVs.setMnemonic(KeyEvent.VK_U);
        editUVs.addActionListener(this);
        modelMenu.add(editUVs);
        
        mirrorSubmenu = new JMenu("Mirror");
        mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        modelMenu.add(mirrorSubmenu);
        
        mirrorX = new JMenuItem("Mirror X");
        mirrorX.setMnemonic(KeyEvent.VK_X);
        mirrorX.addActionListener(mirrorXAction);
        mirrorSubmenu.add(mirrorX);
        
        mirrorY = new JMenuItem("Mirror Y");
        mirrorY.setMnemonic(KeyEvent.VK_Y);
        mirrorY.addActionListener(mirrorYAction);
        mirrorSubmenu.add(mirrorY);
        
        mirrorZ = new JMenuItem("Mirror Z");
        mirrorZ.setMnemonic(KeyEvent.VK_Z);
        mirrorZ.addActionListener(mirrorZAction);
        mirrorSubmenu.add(mirrorZ);
        
        mirrorSubmenu.add(new JSeparator());
        
        mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)",true);
        mirrorFlip.setMnemonic(KeyEvent.VK_A);
        mirrorSubmenu.add(mirrorFlip);
        
        textureModels = new JCheckBoxMenuItem("Texture Models",true);
        textureModels.setMnemonic(KeyEvent.VK_T);
        textureModels.setSelected(true);
        viewMenu.add(textureModels);

        newDirectory = new JMenuItem("Change Game Directory");
        newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
        newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
        newDirectory.setMnemonic(KeyEvent.VK_D);
        newDirectory.addActionListener(this);
        viewMenu.add(newDirectory);

        viewMenu.add(new JSeparator());

        showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons",true);
        showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
        showVertexModifyControls.addActionListener(this);
        viewMenu.add(showVertexModifyControls);

        viewMenu.add(new JSeparator());

        showNormals = new JCheckBoxMenuItem("Show Normals",true);
        showNormals.setMnemonic(KeyEvent.VK_N);
        showNormals.setSelected(false);
        viewMenu.add(showNormals);

        viewMode = new JMenu("3D View Mode");
        viewMenu.add(viewMode);
        
        viewModes = new ButtonGroup();
 
        ActionListener repainter = new ActionListener() {
        	public void actionPerformed(ActionEvent e)
        	{
        		repaint();
        	}
        };
        
        wireframe = new JRadioButtonMenuItem("Wireframe");
        wireframe.addActionListener(repainter);
        viewMode.add(wireframe);
        viewModes.add(wireframe);
        
        solid = new JRadioButtonMenuItem("Solid");
        solid.addActionListener(repainter);
        viewMode.add(solid);
        viewModes.add(solid);
        
        viewModes.setSelected(solid.getModel(), true);
        
        open = new JMenuItem("Open");
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        open.setMnemonic(KeyEvent.VK_O);
        open.addActionListener(this);
        fileMenu.add(open);
        
        fileMenu.add(new JSeparator());

        importButton = new JMenuItem("Import");
        importButton.setAccelerator(KeyStroke.getKeyStroke("control shift I"));
        importButton.setMnemonic(KeyEvent.VK_I);
        importButton.addActionListener(this);
        fileMenu.add(importButton);


        importButtonS = new JMenuItem("Simple Import");
        importButtonS.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        importButtonS.setMnemonic(KeyEvent.VK_P);
        importButtonS.addActionListener(this);
//        importButtonS.setEnabled(false);
        fileMenu.add(importButtonS);
        
        mergeGeoset = new JMenuItem("Merge Geoset");
        mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
        mergeGeoset.setMnemonic(KeyEvent.VK_M);
        mergeGeoset.addActionListener(this);
        fileMenu.add(mergeGeoset);
        
        nullmodelButton = new JMenuItem("Edit/delete model components");
        nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
        nullmodelButton.setMnemonic(KeyEvent.VK_E);
        nullmodelButton.addActionListener(this);
        fileMenu.add(nullmodelButton);
        
        fileMenu.add(new JSeparator());
        
        save = new JMenuItem("Save");
        save.setMnemonic(KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke("control S"));
        save.addActionListener(this);
        fileMenu.add(save);
        
        saveAs = new JMenuItem("Save as");
        saveAs.setMnemonic(KeyEvent.VK_A);
        saveAs.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        saveAs.addActionListener(this);
        fileMenu.add(saveAs);
        
        fileMenu.add(new JSeparator());

        showController = new JMenuItem("Show Controller");
        showController.setMnemonic(KeyEvent.VK_H);
        showController.setAccelerator(KeyStroke.getKeyStroke("control H"));
        showController.addActionListener(this);
        fileMenu.add(showController);
        
        undo = new UndoMenuItem("Undo");
        undo.addActionListener(undoAction);
        undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        undo.addMouseListener(this);
        editMenu.add(undo);
        undo.setEnabled(undo.funcEnabled());
        
        redo = new RedoMenuItem("Redo");
        redo.addActionListener(redoAction);
        redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        redo.addMouseListener(this);
        editMenu.add(redo);
        redo.setEnabled(redo.funcEnabled());
        
        editMenu.add(new JSeparator());
        
        selectAll = new JMenuItem("Select All");
        selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
        selectAll.addActionListener(selectAllAction);
        editMenu.add(selectAll);
        
        invertSelect = new JMenuItem("Invert Selection");
        invertSelect.setAccelerator(KeyStroke.getKeyStroke("control I"));
        invertSelect.addActionListener(invertSelectAction);
        editMenu.add(invertSelect);
        
        expandSelection = new JMenuItem("Expand Selection");
        expandSelection.setAccelerator(KeyStroke.getKeyStroke("control E"));
        expandSelection.addActionListener(expandSelectionAction);
        editMenu.add(expandSelection);
        
        return menuBar;
    }
    public void actionPerformed( ActionEvent e )
    {
        //Open, off of the file menu:
        refreshUndo();
        if( e.getSource() == open )
        {
            fc.setDialogTitle("Open");
            MDL current = currentMDL();
            if( current != null && current.getFile() != null )
            {
            	fc.setCurrentDirectory(current.getFile().getParentFile());
            }
            else if( profile.getPath() != null )
            {
                fc.setCurrentDirectory(new File(profile.getPath()));
            }
            
            int returnValue = fc.showOpenDialog(this);
            
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
                currentFile = fc.getSelectedFile();
                profile.setPath(currentFile.getParent());
//                 frontArea.clearGeosets();
//                 sideArea.clearGeosets();
//                 botArea.clearGeosets();
                modelMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control which parts of the model are displayed for editing.");
                modelMenu.setEnabled(true);
                loadFile(currentFile);
            }
            
            fc.setSelectedFile(null);
            
//             //Special thanks to the JWSFileChooserDemo from oracle's Java tutorials, from which many ideas were borrowed for the following
//             FileOpenService fos = null;
//             FileContents fileContents = null;
//             
//             try
//             {
//                 fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
//             }
//             catch (UnavailableServiceException exc )
//             {
//                 
//             }
//             
//             if( fos != null )
//             {
//                 try
//                 {
//                     fileContents = fos.openFileDialog(null, null);
//                 }
//                 catch (Exception exc )
//                 {
//                     JOptionPane.showMessageDialog(this,"Opening command failed: "+exc.getLocalizedMessage());
//                 }
//             }
//             
//             if( fileContents != null)
//             {
//                 try
//                 {
//                     fileContents.getName();
//                 }
//                 catch (IOException exc)
//                 {
//                     JOptionPane.showMessageDialog(this,"Problem opening file: "+exc.getLocalizedMessage());
//                 }
//             }
        }
        else if( e.getSource() == importButton )
        {
            fc.setDialogTitle("Import");
            MDL current = currentMDL();
            if( current != null && current.getFile() != null )
            {
            	fc.setCurrentDirectory(current.getFile().getParentFile());
            }
            else if( profile.getPath() != null )
            {
                fc.setCurrentDirectory(new File(profile.getPath()));
            }
            int returnValue = fc.showOpenDialog(this);
            
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
                currentFile = fc.getSelectedFile();
                profile.setPath(currentFile.getParent());
                modelMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control which parts of the model are displayed for editing.");
                modelMenu.setEnabled(true);
                importFile(currentFile);
            }
            
            fc.setSelectedFile(null);
            
//             //Special thanks to the JWSFileChooserDemo from oracle's Java tutorials, from which many ideas were borrowed for the following
//             FileOpenService fos = null;
//             FileContents fileContents = null;
//             
//             try
//             {
//                 fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
//             }
//             catch (UnavailableServiceException exc )
//             {
//                 
//             }
//             
//             if( fos != null )
//             {
//                 try
//                 {
//                     fileContents = fos.openFileDialog(null, null);
//                 }
//                 catch (Exception exc )
//                 {
//                     JOptionPane.showMessageDialog(this,"Opening command failed: "+exc.getLocalizedMessage());
//                 }
//             }
//             
//             if( fileContents != null)
//             {
//                 try
//                 {
//                     fileContents.getName();
//                 }
//                 catch (IOException exc)
//                 {
//                     JOptionPane.showMessageDialog(this,"Problem opening file: "+exc.getLocalizedMessage());
//                 }
//             }
            refreshController();
        }
        else if( e.getSource() == importButtonS )
        {
        	new ImportPanelSimple();
            refreshController();
        }
        else if( e.getSource() == mergeGeoset )
        {
            fc.setDialogTitle("Merge Geoset");
            MDL current = currentMDL();
            if( current != null && current.getFile() != null )
            {
            	fc.setCurrentDirectory(current.getFile().getParentFile());
            }
            else if( profile.getPath() != null )
            {
                fc.setCurrentDirectory(new File(profile.getPath()));
            }
            int returnValue = fc.showOpenDialog(this);
            
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
                currentFile = fc.getSelectedFile();
                MDL geoSource = MDL.read(currentFile);
                profile.setPath(currentFile.getParent());
                boolean going = true;
                Geoset host = null;
                while( going )
                {
                	String s = JOptionPane.showInputDialog(this,"Geoset into which to Import: (1 to "+current.getGeosetsSize()+")");
                	try {
                		int x = Integer.parseInt(s);
                		if( x >= 1 && x <= current.getGeosetsSize() )
                		{
                			host = current.getGeoset(x-1);
                    		going = false;
                		}
                	}
                	catch (NumberFormatException exc)
                	{
                		
                	}
                }
                Geoset newGeoset = null;
                going = true;
                while( going )
                {
                	String s = JOptionPane.showInputDialog(this,"Geoset to Import: (1 to "+geoSource.getGeosetsSize()+")");
                	try {
                		int x = Integer.parseInt(s);
                		if( x <= geoSource.getGeosetsSize() )
                		{
                			newGeoset = geoSource.getGeoset(x-1);
                    		going = false;
                		}
                	}
                	catch (NumberFormatException exc)
                	{
                		
                	}
                }
                newGeoset.updateToObjects(current);
                System.out.println("putting "+newGeoset.numUVLayers()+" into a nice "+host.numUVLayers());
                for( int i = 0; i < newGeoset.numVerteces(); i++ )
                {
                	GeosetVertex ver = newGeoset.getVertex(i);
                	host.add(ver);
                	ver.geoset = host;
//                	for( int z = 0; z < host.n.numUVLayers(); z++ )
//                	{
//                		host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));
//                	}
                }
                for( int i = 0; i < newGeoset.numTriangles(); i++ )
                {
                	Triangle tri = newGeoset.getTriangle(i);
                	host.add(tri);
                	tri.setGeoRef(host);
                }
            }
            
            fc.setSelectedFile(null);
        }
        else if( e.getSource() == nullmodelButton )
        {
            nullmodelFile();
            refreshController();
        }
        else if( e.getSource() == showController )
        {
            if( geoControl == null )
            {
                geoControl = new ViewController(currentMDLDisp());
            }
            if( !geoControl.getFrame().isVisible() )
            {
                geoControl.getFrame().setVisible(true);
                //geoControl.getFrame().setExtendedState(geoControl.getFrame().getExtendedState() | JFrame.MAXIMIZED_BOTH);
                geoControl.getFrame().toFront();
            }
        }
        else if( e.getSource() == saveAs )
        {
            fc.setDialogTitle("Save as");
            MDL current = currentMDL();
            if( current != null && current.getFile() != null )
            {
            	fc.setCurrentDirectory(current.getFile().getParentFile());
            }
            else if( profile.getPath() != null )
            {
                fc.setCurrentDirectory(new File(profile.getPath()));
            }
            int returnValue = fc.showSaveDialog(this);
            File temp = fc.getSelectedFile();
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
                if( temp != null )
                {
                    String name = temp.getName();
                    if( name.lastIndexOf('.') != -1 )
                    {
                        if( !name.substring(name.lastIndexOf('.'),name.length()).equals(".mdl") )
                        {
                            temp = ( new File(temp.getAbsolutePath().substring(0,temp.getAbsolutePath() .lastIndexOf('.'))+".mdl"));
                        }
                    }
                    else
                    {
                        temp = ( new File(temp.getAbsolutePath()+".mdl"));
                    }
                    currentFile = temp;
                    if( temp.exists() )
                    {
                        Object [] options = {"Overwrite","Cancel"};
                        int n = JOptionPane.showOptionDialog(MainFrame.frame,"Selected file already exists.","Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[1]);
                        if( n == 1 )
                        {
                            fc.setSelectedFile(null);
                            return;
                        }
                    }
                    profile.setPath(currentFile.getParent());
                    currentMDL().printTo(currentFile);
                    currentMDL().setFile(currentFile);
                    currentMDLDisp().resetBeenSaved();
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),currentFile.getName().split("\\.")[0]);
                    tabbedPane.setToolTipTextAt(tabbedPane.getSelectedIndex(),currentFile.getPath());
                }
                else
                {
                    JOptionPane.showMessageDialog(this,"You tried to save, but you somehow didn't select a file.\nThat is bad.");
                }
            }
            fc.setSelectedFile(null);
            refreshController();
        }
        else if( e.getSource() == save )
        {
            if( currentMDL() != null )
            {
                currentMDL().saveFile();
                profile.setPath(currentMDL().getFile().getParent());
                currentMDLDisp().resetBeenSaved();
            }
            refreshController();
        }
        else if( e.getSource() == contextClose )
        {
            ((ModelPanel)tabbedPane.getComponentAt(contextClickedTab)).close(this);
        }
        else if( e.getSource() == snapButton )
        {
            MDLDisplay mdlDisp = currentMDLDisp();
            if( mdlDisp != null )
            {
                mdlDisp.snap();
            }
        }
        else if( e.getSource() == cloneButton )
        {
            setCloneOn(!cloneOn);
//             ModelPanel mpanel = ((ModelPanel)tabbedPane.getSelectedComponent());
//             if( mpanel != null )
//             mpanel.getMDLDisplay().clone(mpanel.getMDLDisplay().selection,true);
        }
        else if( e.getSource() == xButton )
        {
        	int x = 1;
            setDimLock(x,!dimLocks[x]);
        }
        else if( e.getSource() == yButton )
        {
        	int x = 2;
            setDimLock(x,!dimLocks[x]);
        }
        else if( e.getSource() == zButton )
        {
        	int x = 0;
            setDimLock(x,!dimLocks[x]);
        }
        else if( e.getSource() == newDirectory )
        {
			DirectorySelector selector = new DirectorySelector(SaveProfile.get().getGameDirectory(),"");
			JOptionPane.showMessageDialog(null, selector, "Locating Warcraft III Directory", JOptionPane.QUESTION_MESSAGE);
			String wcDirectory = selector.getDir();
			if( !(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\")) )
			{
				wcDirectory = wcDirectory + "\\";
			}
			SaveProfile.get().setGameDirectory(wcDirectory);

			PerspDisplayPanel pdp;
	    	for( int i = 0; i < tabbedPane.getComponentCount(); i++)
	    	{
	    		pdp = ((ModelPanel)tabbedPane.getComponentAt(i)).perspArea;
	    		pdp.reloadAllTextures();
	    	}
	    	MPQHandler.get().refresh();
        }
        else if( e.getSource() == showVertexModifyControls )
        {
	    	for( int i = 0; i < tabbedPane.getComponentCount(); i++)
	    	{
	    		ModelPanel panel = ((ModelPanel)tabbedPane.getComponentAt(i));
	    		panel.frontArea.setControlsVisible(showVertexModifyControls.isSelected());
	    		panel.botArea.setControlsVisible(showVertexModifyControls.isSelected());
	    		panel.sideArea.setControlsVisible(showVertexModifyControls.isSelected());
	    		MDLDisplay disp = ((ModelPanel)tabbedPane.getComponentAt(i)).getMDLDisplay();
	    		if( disp.uvpanel != null )
	    		{
	    			disp.uvpanel.setControlsVisible(showVertexModifyControls.isSelected());
	    		}
	    	}
        }
        else if( e.getSource() == editUVs )
        {
        	MDLDisplay disp = currentMDLDisp();
        	if( disp.uvpanel == null  )
        	{
        		UVPanel panel = new UVPanel(disp);
        		disp.setUVPanel(panel);
        		panel.showFrame();
        	}
        	else if(!disp.uvpanel.frameVisible() )
        	{
        		disp.uvpanel.showFrame();
        	}
        }
        else if( e.getSource() == creditsButton )
        {
			DefaultStyledDocument panel = new DefaultStyledDocument();
			JTextPane epane = new JTextPane();
			RTFEditorKit rtfk = new RTFEditorKit();
			try {
				rtfk.read(MainPanel.class.getResourceAsStream("credits.rtf"),panel,0);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			epane.setDocument(panel);
			JFrame frame = new JFrame("About");
			frame.setContentPane(new JScrollPane(epane));
			frame.setSize(650,500);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			//JOptionPane.showMessageDialog(this,new JScrollPane(epane));
        }
        else
        {
            boolean done = false;
            for( int i = 3; i < 8 && !done; i++ )
            {
                if( e.getSource() == buttons.get(i) )
                {
                    done = true;
                    for( int b = 3 ; b < 8; b++ )
                    {
                        buttons.get(b).resetColors();
                    }
                    buttons.get(i).setColors(activeRColor1,activeRColor2);
                    actionType = i;
                }
            }
            for( int i = 0; i < 3 && !done; i++ )
            {
                if( e.getSource() == buttons.get(i) )
                {
                    done = true;
                    for( int b = 0 ; b < 3; b++ )
                    {
                        buttons.get(b).resetColors();
                    }
                    buttons.get(i).setColors(activeColor1,activeColor2);
                    selectionType = i;
                }
            }
        }
//         for( int i = 0; i < geoItems.size(); i++ )
//         {
//             JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
//             if( e.getSource() == geoItem )
//             {
//                 frontArea.setGeosetVisible(i,geoItem.isSelected());
//                 frontArea.setGeosetHighlight(i,false);
//             }
//             repaint();
//         }
    }
    public MDL currentMDL()
    {
        if( tabbedPane.getSelectedComponent() != null )
        return ((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay().getMDL();
        else
        return null;
    }
    public MDLDisplay currentMDLDisp()
    {
        if( tabbedPane.getSelectedComponent() != null )
        return ((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay();
        else
        return null;
    }
    /**
     * Returns the MDLDisplay associated with a given MDL, or null if
     * one cannot be found.
     * 
     * @param model
     * @return
     */
    public MDLDisplay displayFor(MDL model)
    {
    	MDLDisplay output = null;
    	MDLDisplay tempDisplay;
    	for( int i = 0; i < tabbedPane.getComponentCount(); i++)
    	{
    		tempDisplay = ((ModelPanel)tabbedPane.getComponentAt(i)).getMDLDisplay();
    		if( tempDisplay.getMDL() == model)
    		{
    			output = tempDisplay;
    			break;
    		}
    	}
        return output;
    }
    public void loadFile(File f)
    {
        ModelPanel temp = new ModelPanel(f);
        temp.setFocusable(false);
        if( geoControl == null )
        {
            geoControl = new ViewController(temp.getMDLDisplay());
        }
        if( !geoControl.getFrame().isVisible() )
        {
            geoControl.getFrame().setVisible(true);
        }
        tabbedPane.addTab(f.getName().split("\\.")[0],MDLIcon,temp,f.getPath());
        tabbedPane.setSelectedComponent(temp);
    }
    public void importFile(File f)
    {
        if( currentMDL() != null )
        importPanel = new ImportPanel(currentMDL(),MDL.read(f));
    }
    public String incName(String name)
    {
    	String output = name;
    	
    	int depth = 1;
    	boolean continueLoop = true;
    	while( continueLoop )
    	{
    		char c = '0';
    		try {
    			 c = output.charAt(output.length()-depth);
    		}
    		catch (IndexOutOfBoundsException e)
    		{
    			//c remains '0'
    			continueLoop = false;
    		}
    		for( char n = '0'; n < '9' && continueLoop; n++ )
    		{
//    			JOptionPane.showMessageDialog(null,"checking "+c+" against "+n);
    			if( c == n )
    			{
    				char x = c;
    				x++;
    				output = output.substring(0,output.length()-depth)
    						+ x
    						+ output.substring(output.length()-depth+1);
    				continueLoop = false;
    			}
    		}
    		if( c == '9' )
    		{
				output = output.substring(0,output.length()-depth)
						+ (0)
						+ output.substring(output.length()-depth+1);
    		}
    		else if( continueLoop )
    		{
				output = output.substring(0,output.length()-depth+1)
						+ 1
						+ output.substring(output.length()-depth+1);
				continueLoop = false;
    		}
    		depth++;
    	}
    	if( output == null )
    	{
    		output = "name error";
    	}
    	else if( output.equals(name) )
    	{
    		output = output + "_edit";
    	}
    	
    	return output;
    }
    public void nullmodelFile()
    {
    	final MDL currentMDL = currentMDL();
        if( currentMDL != null )
        {
        	final MDL newModel = new MDL();
        	newModel.copyHeaders(currentMDL);
        	while( newModel.getFile().exists() ){
            	newModel.setFile(new File(currentMDL.getFile().getParent()+"/"+incName(newModel.getName())+".mdl"));
        	}
            importPanel = new ImportPanel(newModel,MDL.deepClone(currentMDL,"CurrentModel"));
            
            Thread watcher = new Thread(new Runnable() {
            	public void run()
            	{
                    while(importPanel.getParentFrame().isVisible() && (!importPanel.importStarted() || importPanel.importEnded()) )
                    {
                    	try { Thread.sleep(1); } catch (Exception e) {ExceptionPopup.display("MatrixEater detected error with Java's wait function",e); }
                    }
//                    if( !importPanel.getParentFrame().isVisible() && !importPanel.importEnded() )
//                    	JOptionPane.showMessageDialog(null,"bad voodoo "+importPanel.importSuccessful());
//                    else
//                        JOptionPane.showMessageDialog(null,"good voodoo "+importPanel.importSuccessful());
//                    if( importPanel.importSuccessful() )
//                    {
//                    	newModel.saveFile();
//                    	loadFile(newModel.getFile());
//                    }
                    
                    if( importPanel.importStarted() ){
                    	while( !importPanel.importEnded() )
                    	{
                        	try { Thread.sleep(1); } catch (Exception e) {ExceptionPopup.display("MatrixEater detected error with Java's wait function",e); }
                    	}

                        if( importPanel.importSuccessful() )
                        {
                        	newModel.saveFile();
                        	loadFile(newModel.getFile());
                        }
                    }
            	}
            });
            watcher.start();
        }
    }
    public void parseVertex(String input, Geoset geoset)
    {
        String [] entries = input.split(",");
        try
        {
            geoset.addVertex(new GeosetVertex(Double.parseDouble(entries[0].substring(4,entries[0].length())),Double.parseDouble(entries[1]),Double.parseDouble(entries[2].substring(0,entries[2].length()-1))));
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(this,"Error (on line "+c+"): Vertex coordinates could not be interpreted.");
        }
    }
    public void parseTriangles(String input, Geoset g)
    {
        //Loading triangles to a geoset requires verteces to be loaded first
        String [] s = input.split(",");
        s[0] = s[0].substring(4,s[0].length());
        int s_size = countContainsString(input,",");
        s[s_size-1]=s[s_size-1].substring(0,s[s_size-1].length()-2);
        for( int t = 0; t < s_size-1; t+=3  )//s[t+3].equals("")||
        {
            for( int i = 0; i < 3; i++ )
            {
                s[t+i]=s[t+i].substring(1);
            }
            try
            {
                g.addTriangle(new Triangle(Integer.parseInt(s[t]),Integer.parseInt(s[t+1]),Integer.parseInt(s[t+2]),g) );
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(this,"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
            }
        }
//         try
//         {
//             g.addTriangle(new Triangle(g.getVertex( Integer.parseInt(s[t]) ),g.getVertex( Integer.parseInt(s[t+1])),g.getVertex( Integer.parseInt(s[t+2])),g) );
//         }
//         catch (NumberFormatException e)
//         {
//             JOptionPane.showMessageDialog(this,"Error: Unable to interpret information in Triangles.");
//         }
    }
    public String nextLine(BufferedReader reader)
    {
        String output = "";
        try
        {
            output = reader.readLine();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this,"Error reading file.");
        }
        MDLReader.c++;
        c++;
        if( output == null )
        {
            loading = false;
            output = "COMPLETED PARSING";
        }
        return output;
    }
    public boolean doesContainString(String a, String b)//see if a contains b
    {
        int l = a.length();
        for( int i = 0; i < l; i++ )
        {
            if( a.startsWith(b,i) )
            {
                return true;
            }
        }
        return false;
    }
    public int countContainsString(String a, String b)//see if a contains b
    {
        int l = a.length();
        int x = 0;
        for( int i = 0; i < l; i++ )
        {
            if( a.startsWith(b,i) )
            {
                x++;
            }
        }
        return x;
    }
    public void refreshUndo()
    {
        undo.setEnabled(undo.funcEnabled());
        redo.setEnabled(redo.funcEnabled());
    }
    public void refreshController()
    {
    	if( geoControl != null )
    		geoControl.geoControl.repaint();
    }
    public void mouseEntered(MouseEvent e)
    {
        refreshUndo();
//         for( int i = 0; i < geoItems.size(); i++ )
//         {
//             JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
//             if( e.getSource() == geoItem )
//             {
//                 frontArea.setGeosetHighlight(i,true);
//             }
//         }
//         repaint();
    }
    public void mouseExited(MouseEvent e)
    {
        refreshUndo();
//         for( int i = 0; i < geoItems.size(); i++ )
//         {
//             JCheckBoxMenuItem geoItem = (JCheckBoxMenuItem)geoItems.get(i);
//             if( e.getSource() == geoItem )
//             {
//                 frontArea.setGeosetHighlight(i,false);
//             }
//         }
//         repaint();
    }
    public void mousePressed(MouseEvent e)
    {
        refreshUndo();
        Component compFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        System.out.println(compFocusOwner);
        
        
    }
    public void mouseReleased(MouseEvent e)
    {
        refreshUndo();
        
    }
    public void mouseClicked(MouseEvent e)
    {
        if( e.getSource() == tabbedPane && e.getButton() == MouseEvent.BUTTON3 )
        {
            for( int i = 0; i < tabbedPane.getTabCount(); i++ )
            {
                if( tabbedPane.getBoundsAt(i).contains(e.getX(),e.getY()) )
                {
                    contextClickedTab = i;
                    contextMenu.show(tabbedPane,e.getX(),e.getY());
                }
            }
        }
    }
    public void stateChanged(ChangeEvent e)
    {
        if( ((ModelPanel)tabbedPane.getSelectedComponent()) != null )
        {
            geoControl.setMDLDisplay(((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay());
        }
        else
        {
            geoControl.setMDLDisplay(null);
        }
    }
    
    public void setMouseCoordDisplay(byte dim1, byte dim2, double value1, double value2)
    {
        for( int i = 0; i < mouseCoordDisplay.length; i++ )
        {
            mouseCoordDisplay[i].setText("");
        }
        mouseCoordDisplay[dim1].setText((float)value1+"");
        mouseCoordDisplay[dim2].setText((float)value2+"");
    }
    
    public int currentSelectionType()
    {
        return selectionType;
    }
    public int currentActionType()
    {
        return actionType;
    }
    private class UndoMenuItem extends JMenuItem
    {
        public UndoMenuItem(String text)
        {
            super(text);
        }
        public String getText()
        {
            if( funcEnabled() )
            {
                return "Undo "+((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay().undoText();//+"     Ctrl+Z";
            }
            else
            {
                return "Can't undo";//+"     Ctrl+Z";
            }
        }
        public boolean funcEnabled()
        {
            try {
                return ((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay().canUndo();
            }
            catch( NullPointerException e)
            {
                return false;
            }
        }
    }
    private class RedoMenuItem extends JMenuItem
    {
        public RedoMenuItem(String text)
        {
            super(text);
        }
        public String getText()
        {
            if( funcEnabled() )
            {
                return "Redo "+((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay().redoText();//+"     Ctrl+Y";
            }
            else
            {
                return "Can't redo";//+"     Ctrl+Y";
            }
        }
        public boolean funcEnabled()
        {
            try {
                return ((ModelPanel)tabbedPane.getSelectedComponent()).getMDLDisplay().canRedo();
            }
            catch( NullPointerException e)
            {
                return false;
            }
        }
    }
    public boolean closeAll()
    {
        boolean success = true;
        for( int i = tabbedPane.getTabCount()-1; i >= 0  && success; i-- )
        {
            ModelPanel modelPanel = (ModelPanel)tabbedPane.getComponentAt(i);
            success = modelPanel.close(this);
        }
        return success;
    }
}
