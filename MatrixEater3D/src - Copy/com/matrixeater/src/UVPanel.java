package com.matrixeater.src;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import mpqlib.TargaReader;

import com.matrixeater.data.SaveProfile;
/**
 * Write a description of class DisplayPanel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class UVPanel extends JPanel implements ActionListener
{
    static final ImageIcon UVIcon = new ImageIcon(UVPanel.class.getResource("ImageBin/UVMap.png"));
    
    ModeButton loadImage, selectButton, addButton,deselectButton,moveButton,rotateButton,scaleButton,snapButton, xButton, yButton;
    JMenuItem selectAll, invertSelect, expandSelection, selFromMain, mirrorX, mirrorY, setAspectRatio;
    JMenu editMenu, mirrorSubmenu, dispMenu;
    JMenuBar menuBar;
    JCheckBoxMenuItem wrapImage;

    
    ArrayList<ModeButton> buttons = new ArrayList<ModeButton>();
    private MDLDisplay dispMDL;
    private UVViewport vp;
    private JButton up, down, left, right, plusZoom, minusZoom;
    private JTextField[] mouseCoordDisplay = new JTextField[2];
    
    int selectionType = 0;
    boolean cheatShift = false;
    boolean cheatAlt = false;
    int actionType = 3;
    
    JFrame frame;
    AbstractAction selectAllAction = new AbstractAction("Select All")
    {
        public void actionPerformed(ActionEvent e)
        {
            dispMDL.selectAllUV();
            repaint();
        }
    };
AbstractAction invertSelectAction = new AbstractAction("Invert Selection")
    {
        public void actionPerformed(ActionEvent e)
        {
        	dispMDL.invertSelectionUV();
            repaint();
        }
    };
AbstractAction expandSelectionAction = new AbstractAction("Expand Selection")
    {
        public void actionPerformed(ActionEvent e)
        {
        	dispMDL.expandSelectionUV();
            repaint();
        }
    };
AbstractAction selFromMainAction = new AbstractAction("Sel From Main")
    {
        public void actionPerformed(ActionEvent e)
        {
        	dispMDL.selFromMain();
            repaint();
        }
    };
AbstractAction mirrorXAction = new AbstractAction("Mirror X")
    {
        public void actionPerformed(ActionEvent e)
        {
        	dispMDL.mirrorUV((byte)0);
            repaint();
        }
    };
AbstractAction mirrorYAction = new AbstractAction("Mirror Y")
    {
        public void actionPerformed(ActionEvent e)
        {
        	dispMDL.mirrorUV((byte)1);
            repaint();
        }
    };
    public UVPanel(MDLDisplay dispMDL)
    {
        super();
        setBorder(BorderFactory.createLineBorder(Color.black));//BorderFactory.createCompoundBorder(
//             BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)
//             ));
        setOpaque(true);
        setViewport(dispMDL);
        this.dispMDL = dispMDL;
        
        //Copied from MainPanel
        selectButton = new ModeButton("Select");
        addButton = new ModeButton("Add");
        deselectButton = new ModeButton("Deselect");
        JLabel [] divider = new JLabel[4];
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
        loadImage = new ModeButton("Load Image");
        moveButton = new ModeButton("Move");
        rotateButton = new ModeButton("Rotate");
        scaleButton = new ModeButton("Scale");
        snapButton = new ModeButton("Snap");
        xButton = new ModeButton("X");
        yButton = new ModeButton("Y");

        buttons.add(selectButton);
        buttons.add(addButton);
        buttons.add(deselectButton);
        buttons.add(moveButton);
        buttons.add(rotateButton);
        buttons.add(scaleButton);
        buttons.add(snapButton);
        buttons.add(loadImage);
        
        xButton.addActionListener(this);
        xButton.setMaximumSize(new Dimension(26,26));
        xButton.setMinimumSize(new Dimension(20,20));
        xButton.setMargin(new Insets(0, 0, 0, 0));
        yButton.addActionListener(this);
        yButton.setMaximumSize(new Dimension(26,26));
        yButton.setMinimumSize(new Dimension(20,20));
        yButton.setMargin(new Insets(0, 0, 0, 0));
        
        for( int i = 0; i < buttons.size(); i++ )
        {
            buttons.get(i).setMaximumSize(new Dimension(100,35));
            buttons.get(i).setMinimumSize(new Dimension(90,15));
            buttons.get(i).addActionListener(this);
        }
        
        plusZoom = new JButton("");
        Dimension dim = new Dimension(20,20);
        plusZoom.setMaximumSize(dim);
        plusZoom.setMinimumSize(dim);
        plusZoom.setPreferredSize(dim);
        plusZoom.setIcon(new ImageIcon(UVPanel.class.getResource("ImageBin/Plus.png")));
        plusZoom.addActionListener(this);
        add(plusZoom);

        minusZoom = new JButton("");
        minusZoom.setMaximumSize(dim);
        minusZoom.setMinimumSize(dim);
        minusZoom.setPreferredSize(dim);
        minusZoom.setIcon(new ImageIcon(UVPanel.class.getResource("ImageBin/Minus.png")));
        minusZoom.addActionListener(this);
        add(minusZoom);

        up = new JButton("");
        dim = new Dimension(32,16);
        up.setMaximumSize(dim);
        up.setMinimumSize(dim);
        up.setPreferredSize(dim);
        up.setIcon(new ImageIcon(UVPanel.class.getResource("ImageBin/ArrowUp.png")));
        up.addActionListener(this);
        add(up);
        
        down = new JButton("");
        down.setMaximumSize(dim);
        down.setMinimumSize(dim);
        down.setPreferredSize(dim);
        down.setIcon(new ImageIcon(UVPanel.class.getResource("ImageBin/ArrowDown.png")));
        down.addActionListener(this);
        add(down);
        
        dim = new Dimension(16,32);
        left = new JButton("");
        left.setMaximumSize(dim);
        left.setMinimumSize(dim);
        left.setPreferredSize(dim);
        left.setIcon(new ImageIcon(UVPanel.class.getResource("ImageBin/ArrowLeft.png")));
        left.addActionListener(this);
        add(left);
        
        right = new JButton("");
        right.setMaximumSize(dim);
        right.setMinimumSize(dim);
        right.setPreferredSize(dim);
        right.setIcon(new ImageIcon(UVPanel.class.getResource("ImageBin/ArrowRight.png")));
        right.addActionListener(this);
        add(right);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(vp)
                  .addGroup(layout.createSequentialGroup()
                       .addComponent(mouseCoordDisplay[0])
                       .addComponent(mouseCoordDisplay[1])
                       .addGap(120)
                       .addGroup(layout.createSequentialGroup()
                           .addComponent(left)
                           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                               .addComponent(up)
                               .addComponent(down))
                           .addComponent(right))
                                             
                           .addGap(16)
                       .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                           .addComponent(plusZoom)
                           .addComponent(minusZoom)
                         )
                  )
                 )
                    
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(loadImage)
                            .addComponent(divider[0])
                            .addComponent(selectButton)
                            .addComponent(addButton)
                            .addComponent(deselectButton)
                            .addComponent(divider[1])
                            .addComponent(moveButton)
                            .addComponent(rotateButton)
                            .addComponent(scaleButton)
                            .addComponent(divider[2])
                            .addComponent(snapButton)
                            .addComponent(divider[3])
                            .addGroup(layout.createSequentialGroup()
                                	.addComponent(xButton)
                                	.addComponent(yButton)
                            	)
                            )
            );
        layout.setVerticalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(vp)
                        .addGroup(layout.createParallelGroup()
                            .addComponent(mouseCoordDisplay[0])
                            .addComponent(mouseCoordDisplay[1])
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            	.addGroup(layout.createSequentialGroup()
                            	.addComponent(up)
                            	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            			.addComponent(left)
                            			.addComponent(right))
                            	.addComponent(down)
                            	)
                        
                            	.addGroup(layout.createSequentialGroup()
                                .addComponent(plusZoom)
                                .addGap(16)
                                .addComponent(minusZoom)
                                )
    		 		)

                  )
                )

            .addGroup(layout.createSequentialGroup()
                .addComponent(loadImage)
                .addGap(8)
                .addComponent(divider[0])
                .addGap(8)
                .addComponent(selectButton)
                .addGap(8)
                .addComponent(addButton)
                .addGap(8)
                .addComponent(deselectButton)
                .addGap(8)
                .addComponent(divider[1])
                .addGap(8)
                .addComponent(moveButton)
                .addGap(8)
                .addComponent(rotateButton)
                .addGap(8)
                .addComponent(scaleButton)
                .addGap(8)
                .addComponent(divider[2])
                .addGap(8)
                .addComponent(snapButton)
                .addGap(8)
                .addComponent(divider[3])
                .addGap(8)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    	.addComponent(xButton)
                    	.addComponent(yButton)
                	)
                ));
        
        setLayout(layout);
    }

    public JMenuBar createMenuBar()
    {
        //Create my menu bar
        menuBar = new JMenuBar();
        
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model's TVertices.");
        menuBar.add(editMenu);

        dispMenu = new JMenu("View");
        dispMenu.setMnemonic(KeyEvent.VK_V);
        dispMenu.getAccessibleContext().setAccessibleDescription("Control display settings for this Texture Coordinate Editor window.");
        menuBar.add(dispMenu);
        
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

        selFromMain = new JMenuItem("Select from Viewer");
        selFromMain.setAccelerator(KeyStroke.getKeyStroke("control V"));
        selFromMain.addActionListener(selFromMainAction);
        editMenu.add(selFromMain);

        wrapImage = new JCheckBoxMenuItem("Wrap Image",false);
        wrapImage.setToolTipText("Repeat the texture many times in a grid-like display. This feature does not edit the model in any way; only this viewing window.");
        //wrapImage.addActionListener(this);
        dispMenu.add(wrapImage);
        
        setAspectRatio = new JMenuItem("Set Aspect Ratio");
        setAspectRatio.setMnemonic(KeyEvent.VK_S);
        setAspectRatio.setAccelerator(KeyStroke.getKeyStroke("control R"));
        setAspectRatio.setToolTipText("Sets the amount by which the texture display is stretched, for editing textures with non-uniform width and height.");
        setAspectRatio.addActionListener(this);
        dispMenu.add(setAspectRatio);
        
        editMenu.add(new JSeparator());
        

        mirrorSubmenu = new JMenu("Mirror");
        mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
        mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
        editMenu.add(mirrorSubmenu);
        
        mirrorX = new JMenuItem("Mirror X");
        mirrorX.setMnemonic(KeyEvent.VK_X);
        mirrorX.addActionListener(mirrorXAction);
        mirrorSubmenu.add(mirrorX);
        
        mirrorY = new JMenuItem("Mirror Y");
        mirrorY.setMnemonic(KeyEvent.VK_Y);
        mirrorY.addActionListener(mirrorYAction);
        mirrorSubmenu.add(mirrorY);
        

        
        return menuBar;
    }
    public void setControlsVisible(boolean flag)
    {
    	up.setVisible(flag);
    	down.setVisible(flag);
    	left.setVisible(flag);
    	right.setVisible(flag);
    	plusZoom.setVisible(flag);
    	minusZoom.setVisible(flag);
    }
    public void init()
    {
    	vp.init();
        buttons.get(0).setColors(MainFrame.panel.activeColor1,MainFrame.panel.activeColor2);
        buttons.get(3).setColors(MainFrame.panel.activeRColor1,MainFrame.panel.activeRColor2);
        

        JRootPane root = getRootPane();
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
                        addButton.setColors(MainFrame.panel.activeColor1,MainFrame.panel.activeColor2);
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
                            deselectButton.setColors(MainFrame.panel.activeColor1,MainFrame.panel.activeColor2);
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
                        selectButton.setColors(MainFrame.panel.activeColor1,MainFrame.panel.activeColor2);
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
                            selectButton.setColors(MainFrame.panel.activeColor1,MainFrame.panel.activeColor2);
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
        
        setControlsVisible(MainFrame.panel.showVMControls());
    }
    public boolean frameVisible()
    {
    	return frame.isVisible();
    }
    public void showFrame()
    {
    	if( frame == null )
    	{
        	frame = new JFrame("Texture Coordinate Editor: "+dispMDL.getMDL().getName());
        	frame.setContentPane(this);
        	frame.setIconImage(UVIcon.getImage());
        	//frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        	frame.setJMenuBar(createMenuBar());
        	frame.pack();
        	frame.setLocationRelativeTo(null);
        	init();
    	}
    	frame.setVisible(true);
    	//frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    	frame.toFront();
    }
    public void packFrame()
    {
    	frame.pack();
    	frame.setLocationRelativeTo(null);
    }
    public void setMouseCoordDisplay( double x, double y )
    {
    	mouseCoordDisplay[0].setText((float)x+"");
    	mouseCoordDisplay[1].setText((float)y+"");
    }
    public void setViewport(MDLDisplay dispModel)
    {
        vp = new UVViewport(dispModel,this);
        add(vp);
    }
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        //g.drawString(title,3,3);
        //vp.repaint();
    }
//     public void addGeoset(Geoset g)
//     {
//         m_geosets.add(g);
//     }
//     public void setGeosetVisible(int index, boolean flag)
//     {
//         Geoset geo = (Geoset)m_geosets.get(index);
//         geo.setVisible(flag);
//     }
//     public void setGeosetHighlight(int index, boolean flag)
//     {
//         Geoset geo = (Geoset)m_geosets.get(index);
//         geo.setHighlight(flag);
//     }
//     public void clearGeosets()
//     {
//         m_geosets.clear();
//     }
//     public int getGeosetsSize()
//     {
//         return m_geosets.size();
//     }
    public void actionPerformed(ActionEvent e)
    {
        if( e.getSource() == up )
        {
            vp.translate(0,(20*(1/vp.getZoomAmount())));
            vp.repaint();
        }
        else if( e.getSource() == down )
        {
            vp.translate(0,(-20*(1/vp.getZoomAmount())));
            vp.repaint();
        }
        else if( e.getSource() == left )
        {
            vp.translate((20*(1/vp.getZoomAmount())),0);
            vp.repaint();
        }
        else if( e.getSource() == right )
        {
            vp.translate((-20*(1/vp.getZoomAmount())),0);//*vp.getZoomAmount()
            vp.repaint();
        }
        else if( e.getSource() == plusZoom )
        {
            vp.zoom(.15);
            vp.repaint();
        }
        else if( e.getSource() == minusZoom )
        {
            vp.zoom(-.15);
            vp.repaint();
        }
        else if( e.getSource() == xButton )
        {
        	int x = 0;
            setDimLock(x,!dimLocks[x]);
        }
        else if( e.getSource() == yButton )
        {
        	int x = 1;
            setDimLock(x,!dimLocks[x]);
        }
        else if( e.getSource() == snapButton )
        {
            dispMDL.snapUVs();
            repaint();
        }
        else if( e.getSource() == loadImage )
        {

    		int x = JOptionPane.showConfirmDialog(this,"Do you want to use the texture auto-loader to find avaiable textures?\nIf you choose \"No\", then you will have to find a file on your hard drive instead.","Load Image",JOptionPane.YES_NO_CANCEL_OPTION);
    		if( x == JOptionPane.YES_OPTION )
    		{
        		DefaultListModel<Material> materials = new DefaultListModel<Material>();
        		for( int i = 0; i < dispMDL.model.m_materials.size(); i++ )
        		{
        			Material mat = dispMDL.model.m_materials.get(i);
        			materials.addElement(mat);
        		}
        		
        		JList<Material> materialsList = new JList<Material>(materials);
        		materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        		materialsList.setCellRenderer(new MaterialListRenderer());
        		JOptionPane.showMessageDialog(this, new JScrollPane(materialsList));

        		vp.clearBackgroundImage();
        		if( materialsList.getSelectedValue() != null )
        		{
            		vp.addBackgroundImage(materialsList.getSelectedValue().getBufferedImage());
        		}
    		}
    		else if( x == JOptionPane.NO_OPTION )
    		{
    			JFileChooser jfc = new JFileChooser();
                MDL current = MainFrame.panel.currentMDL();
                SaveProfile profile = SaveProfile.get();
                if( current != null && current.getFile() != null )
                {
                	jfc.setCurrentDirectory(current.getFile().getParentFile());
                }
                else if( profile.getPath() != null )
                {
                    jfc.setCurrentDirectory(new File(profile.getPath()));
                }
    			jfc.setSelectedFile(null);
    			int returnValue = jfc.showOpenDialog(this);

                if( returnValue == JFileChooser.APPROVE_OPTION )
                {
                	File temp = jfc.getSelectedFile();
                	String fileName = temp.getName();
                	String extension = "";
                	int i = fileName.lastIndexOf('.');
                	if (i > 0) {
                	    extension = fileName.substring(i+1);
                	}
                	if( extension.toLowerCase().equals("blp"))
                	{
                		vp.clearBackgroundImage();
                		vp.addBackgroundImage(BLPHandler.get().getCustomTex(temp.getPath()));
                	}
                	else if( extension.toLowerCase().equals("tga"))
                	{
                		try {
                    		vp.clearBackgroundImage();
							vp.addBackgroundImage(TargaReader.getImage(temp.getPath()));
						} catch (Exception e1) {
							e1.printStackTrace();
							ExceptionPopup.display("Unable to load (special case TGA) image file:",e1);
						}
                	}
                	else
                	{
                		try {
                    		vp.clearBackgroundImage();
							vp.addBackgroundImage(ImageIO.read(temp));
						} catch (IOException e1) {
							e1.printStackTrace();
							ExceptionPopup.display("Unable to load image file:",e1);
						}
                	}
                }
//        	if( dispMDL.visibleGeosets.size() > 0 )
//        	{
////        		for(Layer lay: dispMDL.visibleGeosets.get(0).getMaterial().layers)
////        		{
////        			Bitmap tex = lay.firstTexture();
////					String path = tex.getPath();
////					if( path.length() == 0 )
////					{
////						if( tex.getReplaceableId() == 1 )
////						{
////							path = "ReplaceableTextures\\TeamColor\\TeamColor00.blp";
////						}
////						else if( tex.getReplaceableId() == 2 )
////						{
////							path = "ReplaceableTextures\\TeamGlow\\TeamGlow00.blp";
////						}
////					}
////					try {
////	                	vp.addBackgroundImage(BLPHandler.get().getGameTex(path));
////					}
////					catch (Exception exc)
////					{
////						exc.printStackTrace();
////						try {
////							vp.addBackgroundImage(BLPHandler.get().getCustomTex(dispMDL.getMDL().getFile().getParent()+"\\"+path));
////						}
////						catch (Exception exc2)
////						{
////							exc2.printStackTrace();
////							JOptionPane.showMessageDialog(this, "BLP texture-loader failed.");
////						}
////	                }
////        		}
//        		}
        	}
        }
        else if( e.getSource() == setAspectRatio )
        {
        	JPanel panel = new JPanel();
        	JSpinner widthVal = new JSpinner(new SpinnerNumberModel(1,1,10000,1));
        	JSpinner heightVal = new JSpinner(new SpinnerNumberModel(1,1,10000,1));
        	JLabel toLabel = new JLabel(" to ");
        	panel.add(widthVal);
        	panel.add(toLabel);
        	panel.add(heightVal);
        	JOptionPane.showMessageDialog(this,panel);
        	vp.setAspectRatio(((Integer)widthVal.getValue()).intValue()/(double)((Integer)heightVal.getValue()).intValue());
        }
        else
        {
            boolean done = false;
            for( int i = 3; i < 6 && !done; i++ )
            {
                if( e.getSource() == buttons.get(i) )
                {
                    done = true;
                    for( int b = 3 ; b < 6; b++ )
                    {
                        buttons.get(b).resetColors();
                    }
                    buttons.get(i).setColors(MainFrame.panel.activeRColor1,MainFrame.panel.activeRColor2);
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
                    buttons.get(i).setColors(MainFrame.panel.activeColor1,MainFrame.panel.activeColor2);
                    selectionType = i;
                }
            }
        }
    }
    public ImageIcon getImageIcon()
    {
        return new ImageIcon(vp.getBufferedImage());
    }
    public BufferedImage getBufferedImage()
    {
        return vp.getBufferedImage();
    }
    /**
     * A method defining the currently selected UV layer.
     * 
     * @return
     */
    public int currentLayer()
    {
    	return 0;
    }
    
    boolean [] dimLocks = new boolean[2];
    public ModeButton getDLockButton(int x)
    {
    	switch (x)
    	{
    	case 0:
    		return xButton;
    	case 1:
    		return yButton;
    	}
    	return null;
    }
    public void setDimLock(int x, boolean flag)
    {
        dimLocks[x] = flag;
        if( dimLocks[x] )
        {
            getDLockButton(x).setColors(MainFrame.panel.activeBColor1,MainFrame.panel.activeBColor2);
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
    
    public int currentSelectionType()
    {
        return selectionType;
    }
    public int currentActionType()
    {
        return actionType;
    }
}
