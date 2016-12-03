package com.matrixeater.src;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
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
            showPivots.setSelected(mdisp.isDispPivots());
            showPivots.addActionListener(this);
            showPivotNames = new JCheckBox("Show pivot point object names");
            showPivotNames.setSelected(mdisp.isDispPivotNames());
            showPivotNames.addActionListener(this);
            showParents = new JCheckBox("Only show parents of active geosets");
            showParents.setSelected(mdisp.isDispChildren());
            showParents.addActionListener(this);
            showCameras = new JCheckBox("Show cameras");
            showCameras.setSelected(mdisp.isDispCameras());
            showCameras.addActionListener(this);
            showCameraNames = new JCheckBox("Show camera names");
            showCameraNames.setSelected(mdisp.isDispCameraNames());
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
            showPivots.setSelected(dispModel.isDispPivots());
            showPivotNames.setSelected(dispModel.isDispPivotNames());
            showParents.setSelected(dispModel.isDispChildren());
            showCameras.setSelected(dispModel.isDispCameras());
            showCameraNames.setSelected(dispModel.isDispCameraNames());
            active = true;
        }
    }
    @Override
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
