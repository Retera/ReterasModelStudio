package com.hiveworkshop.wc3.gui.modeledit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.mdl.MDL;
/**
 * The ModelPanel is a pane holding the display of a given MDL model.
 * I plan to tab between them.
 * 
 * Eric Theller
 * 6/7/2012
 */
public class ModelPanel extends JPanel implements ActionListener, MouseListener
{
    JMenuBar menuBar;
    JMenu fileMenu, modelMenu;
    ArrayList geoItems = new ArrayList();
    DisplayPanel frontArea, sideArea, botArea;
    PerspDisplayPanel perspArea;
    MDL model;
    MDLDisplay dispModel;
    File file;
    ProgramPreferences prefs;
    UndoHandler undoHandler;
    public ModelPanel(File input, ProgramPreferences prefs, UndoHandler undoHandler) {
    	this(MDL.read(input),prefs,undoHandler);
    	file = input;
    }
    public ModelPanel(MDL input, ProgramPreferences prefs, UndoHandler undoHandler)
    {
        super();
        this.prefs = prefs;
        this.undoHandler = undoHandler;
        //Produce the front display panel
//        file = input;
//        model = MDL.read(file);
//        dispModel = new MDLDisplay(model,this);
        loadModel(input);
        
        frontArea = new DisplayPanel("Front",(byte)1,(byte)2,dispModel);
        //frontArea.setViewport(1,2);
        add(frontArea);
        botArea = new DisplayPanel("Bottom",(byte)1,(byte)0,dispModel);
        //botArea.setViewport(0,1);
        add(botArea);
        sideArea = new DisplayPanel("Side",(byte)0,(byte)2,dispModel);
        //sideArea.setViewport(0,2);
        add(sideArea);
        
		frontArea.setControlsVisible(dispModel.getProgramPreferences().showVMControls());
		botArea.setControlsVisible(dispModel.getProgramPreferences().showVMControls());
		sideArea.setControlsVisible(dispModel.getProgramPreferences().showVMControls());
        
        perspArea = new PerspDisplayPanel("Perspective",dispModel);
//        perspAreaPanel.setMinimumSize(new Dimension(200,200));
//        perspAreaPanel.add(Box.createHorizontalStrut(200));
//        perspAreaPanel.add(Box.createVerticalStrut(200));
//        perspAreaPanel.setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
         //botArea.setViewport(0,1);
         add(perspArea);
         
        
         
         setLayout(new GridLayout(2,2));
         

         //Hacky viewer
//         frontArea.setVisible(false);
//         sideArea.setVisible(false);
//         botArea.setVisible(false);
//         setLayout(new GridLayout(1,1));
//        GroupLayout layout = new GroupLayout(this);
//        
//        layout.setHorizontalGroup(layout.createSequentialGroup()
//                .addGroup(layout.createParallelGroup()
//                        .addComponent(frontArea)
//                        .addComponent(sideArea))
//                .addGroup(layout.createParallelGroup()
//                        .addComponent(botArea)
//                        .addComponent(perspArea)));
//        layout.setVerticalGroup(layout.createSequentialGroup()
//                .addGroup(layout.createParallelGroup()
//                        .addComponent(frontArea)
//                        .addComponent(botArea))
//                .addGroup(layout.createParallelGroup()
//                        .addComponent(sideArea)
//                        .addComponent(perspArea)));
//        setLayout(layout);
            
        //Create a file chooser
    }
    public void loadModel(File input) {
        file = input;
        if( file != null ) {
            model = MDL.read(file);
            loadModel(model);
        }
    }
    public void loadModel(MDL model) {
    	this.model = model;
        dispModel = new MDLDisplay(model,this);
        dispModel.setProgramPreferences(prefs);
        dispModel.setUndoHandler(undoHandler);
    }
    @Override
    public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
    	perspArea.getViewport().setMinimumSize(new Dimension(200,200));
//    	botArea.setMaximumSize(frontArea.getSize());
//    	sideArea.setMaximumSize(frontArea.getSize());
    }
    @Override
	public void actionPerformed( ActionEvent e )
    {
//         //Open, off of the file menu:
//         if( e.getSource() == open )
//         {
//             int returnValue = fc.showOpenDialog(this);
//             
//             if( returnValue == JFileChooser.APPROVE_OPTION )
//             {
//                 currentFile = fc.getSelectedFile();
//                 frontArea.clearGeosets();
//                 sideArea.clearGeosets();
//                 botArea.clearGeosets();
//                 modelMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control which parts of the model are displayed for editing.");
//                 modelMenu.setEnabled(true);
//                 loadFile(currentFile);
//             }
//             
//             fc.setSelectedFile(null);
//             
// //             //Special thanks to the JWSFileChooserDemo from oracle's Java tutorials, from which many ideas were borrowed for the following
// //             FileOpenService fos = null;
// //             FileContents fileContents = null;
// //             
// //             try
// //             {
// //                 fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
// //             }
// //             catch (UnavailableServiceException exc )
// //             {
// //                 
// //             }
// //             
// //             if( fos != null )
// //             {
// //                 try
// //                 {
// //                     fileContents = fos.openFileDialog(null, null);
// //                 }
// //                 catch (Exception exc )
// //                 {
// //                     JOptionPane.showMessageDialog(this,"Opening command failed: "+exc.getLocalizedMessage());
// //                 }
// //             }
// //             
// //             if( fileContents != null)
// //             {
// //                 try
// //                 {
// //                     fileContents.getName();
// //                 }
// //                 catch (IOException exc)
// //                 {
// //                     JOptionPane.showMessageDialog(this,"Problem opening file: "+exc.getLocalizedMessage());
// //                 }
// //             }
//         }
//         if( e.getSource() == importButton )
//         {
//             int returnValue = fc.showOpenDialog(this);
//             
//             if( returnValue == JFileChooser.APPROVE_OPTION )
//             {
//                 currentFile = fc.getSelectedFile();
//                 modelMenu.getAccessibleContext().setAccessibleDescription("Allows the user to control which parts of the model are displayed for editing.");
//                 modelMenu.setEnabled(true);
//                 loadFile(currentFile);
//             }
//             
//             fc.setSelectedFile(null);
//             
// //             //Special thanks to the JWSFileChooserDemo from oracle's Java tutorials, from which many ideas were borrowed for the following
// //             FileOpenService fos = null;
// //             FileContents fileContents = null;
// //             
// //             try
// //             {
// //                 fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
// //             }
// //             catch (UnavailableServiceException exc )
// //             {
// //                 
// //             }
// //             
// //             if( fos != null )
// //             {
// //                 try
// //                 {
// //                     fileContents = fos.openFileDialog(null, null);
// //                 }
// //                 catch (Exception exc )
// //                 {
// //                     JOptionPane.showMessageDialog(this,"Opening command failed: "+exc.getLocalizedMessage());
// //                 }
// //             }
// //             
// //             if( fileContents != null)
// //             {
// //                 try
// //                 {
// //                     fileContents.getName();
// //                 }
// //                 catch (IOException exc)
// //                 {
// //                     JOptionPane.showMessageDialog(this,"Problem opening file: "+exc.getLocalizedMessage());
// //                 }
// //             }
//         }
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
    public MDLDisplay getMDLDisplay()
    {
        return dispModel;
    }
    public boolean close()//MainPanel parent) TODO fix
    {
        //returns true if closed successfully
        boolean canceled = false;
//        int myIndex = parent.tabbedPane.indexOfComponent(this);
        if( !getMDLDisplay().beenSaved() )
        {
            Object [] options = {"Yes","No","Cancel"};
            int n = JOptionPane.showOptionDialog(this,"Would you like to save "+model.getName()/*parent.tabbedPane.getTitleAt(myIndex)*/+" (\""+model.getHeaderName()+"\") before closing?","Warning",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[2]);
            switch( n )
            {
                case 0:
//                    ((ModelPanel)parent.tabbedPane.getComponentAt(myIndex)).getMDLDisplay().getMDL().saveFile();
//                    parent.tabbedPane.remove(myIndex);
                    if( dispModel.uvpanel != null)
                    	dispModel.uvpanel.frame.setVisible(false);
                    break;
                case 1:
//                    parent.tabbedPane.remove(myIndex);
                    if( dispModel.uvpanel != null)
                    	dispModel.uvpanel.frame.setVisible(false);
                    break;
                case 2:
                    canceled = true;
                    break;
            }
        }
        else
        {
//            parent.tabbedPane.remove(myIndex);
        }
        return !canceled;
    }
    @Override
	public void mouseEntered(MouseEvent e)
    {
        
    }
    @Override
	public void mouseExited(MouseEvent e)
    {
        
    }
    @Override
	public void mousePressed(MouseEvent e)
    {
        
    }
    @Override
	public void mouseReleased(MouseEvent e)
    {
        
    }
    @Override
	public void mouseClicked(MouseEvent e)
    {
        
    }
	public DisplayPanel getFrontArea() {
		return frontArea;
	}
	public void setFrontArea(DisplayPanel frontArea) {
		this.frontArea = frontArea;
	}
	public DisplayPanel getSideArea() {
		return sideArea;
	}
	public void setSideArea(DisplayPanel sideArea) {
		this.sideArea = sideArea;
	}
	public DisplayPanel getBotArea() {
		return botArea;
	}
	public void setBotArea(DisplayPanel botArea) {
		this.botArea = botArea;
	}
	public PerspDisplayPanel getPerspArea() {
		return perspArea;
	}
	public void setPerspArea(PerspDisplayPanel perspArea) {
		this.perspArea = perspArea;
	}
}
