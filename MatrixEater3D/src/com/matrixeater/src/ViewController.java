package com.matrixeater.src;
import javax.swing.*;
/**
 * A view control, containing several control options
 * 
 * Eric Theller
 * 7/8/2012
 */
public class ViewController extends JTabbedPane
{
    JFrame frame;
    GeosetController geoControl;
    ObjectController objControl;
    MDLDisplay dispModel;
    JScrollPane geoScroll;
    public ViewController(MDLDisplay disp)
    {
        super();
        dispModel = disp;
        frame = new JFrame("View Controller");
        frame.setContentPane(this);
        frame.setIconImage(ImportPanel.redIcon.getImage());
        geoControl = new GeosetController(disp);
        objControl = new ObjectController(disp);
        geoScroll = new JScrollPane(geoControl);
        addTab("",ImportPanel.geoIcon,geoScroll,"Controls visibility of geosets.");
        addTab("",ImportPanel.boneIcon,objControl,"Controls visibility of bones.");
        frame.setVisible(true);
        frame.pack();
    }
    public JFrame getFrame()
    {
        return frame;
    }
    public void setMDLDisplay(MDLDisplay disp)
    {
        geoControl.setMDLDisplay(disp);
        objControl.setMDLDisplay(disp);
        repaint();
    }
}
