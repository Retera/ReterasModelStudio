package com.matrixeater.src;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
/**
 * Something to handle the display of objects' PivotPoints
 * 
 * Eric Theller
 * 7/8/2012
 */
public class ObjectController extends JPanel implements ActionListener
{
    JCheckBox showPivots;
    JCheckBox showPivotNames;
    JCheckBox showParents;
    JCheckBox showCameras;
    JCheckBox showCameraNames;
    MDLDisplay dispModel;
    public ObjectController(MDLDisplay mdisp)
    {
        dispModel = mdisp;
        if( dispModel != null )
        {
            showPivots = new JCheckBox("Show pivot points");
            showPivots.setSelected(mdisp.dispPivots);
            showPivots.addActionListener(this);
            showPivotNames = new JCheckBox("Show pivot point object names");
            showPivotNames.setSelected(mdisp.dispPivotNames);
            showPivotNames.addActionListener(this);
            showParents = new JCheckBox("Only show parents of active geosets");
            showParents.setSelected(mdisp.dispChildren);
            showParents.addActionListener(this);
            showCameras = new JCheckBox("Show cameras");
            showCameras.setSelected(mdisp.dispCameras);
            showCameras.addActionListener(this);
            showCameraNames = new JCheckBox("Show camera names");
            showCameraNames.setSelected(mdisp.dispCameraNames);
            showCameraNames.addActionListener(this);
            setLayout(new GridLayout(5,1));
            add(showPivots);
            add(showPivotNames);
            add(showParents);
            add(showCameras);
            add(showCameraNames);
        }
        else
        {
            showPivots = new JCheckBox("Show pivot points");
            showPivots.addActionListener(this);
            showPivotNames = new JCheckBox("Show pivot point object names");
            showPivotNames.addActionListener(this);
            showParents = new JCheckBox("Only show parents of active geosets");
            showParents.addActionListener(this);
            showCameras = new JCheckBox("Show cameras");
            showCameras.addActionListener(this);
            showCameraNames = new JCheckBox("Show camera names");
            showCameraNames.addActionListener(this);
            setLayout(new GridLayout(5,1));
            add(showPivots);
            add(showPivotNames);
            add(showParents);
            add(showCameras);
            add(showCameraNames);
            active = false;
        }
        //showChildren.setVisible(false);
    }
    boolean active = true;
    public void setMDLDisplay(MDLDisplay disp)
    {
        active = false;
        dispModel = disp;
        if( disp != null )
        {
            showPivots.setSelected(dispModel.dispPivots);
            showPivotNames.setSelected(dispModel.dispPivotNames);
            showParents.setSelected(dispModel.dispChildren);
            showCameras.setSelected(dispModel.dispCameras);
            showCameraNames.setSelected(dispModel.dispCameraNames);
            active = true;
        }
    }
    public void actionPerformed(ActionEvent e)
    {
        if( active )
        {
            dispModel.setDispPivots(showPivots.isSelected());
            dispModel.setDispPivotNames(showPivotNames.isSelected());
            dispModel.setDispChildren(showParents.isSelected());
            dispModel.setDispCameras(showCameras.isSelected());
            dispModel.setDispCameraNames(showCameraNames.isSelected());
            MainFrame.panel.repaint();
        }
    }
}
