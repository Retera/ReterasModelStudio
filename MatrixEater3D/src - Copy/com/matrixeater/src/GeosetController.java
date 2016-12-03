package com.matrixeater.src;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;
/**
 * ViewControl
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class GeosetController extends JPanel implements ActionListener, MouseListener
{
    ArrayList<JCheckBox> visibleButtons = new ArrayList<JCheckBox>();
    ArrayList<JCheckBox> editableButtons = new ArrayList<JCheckBox>();
    int size = 0;
    MDLDisplay dispModel;
    JButton hideAll = new JButton("All Hidden");
    JButton showAll = new JButton("All Visible");
    JButton editableAll = new JButton("All Editable");
    JLabel title;
    public GeosetController(MDLDisplay disp)
    {
        super();
        dispModel = disp;
        setMDLDisplay(disp);
        hideAll.addActionListener(this);
        showAll.addActionListener(this);
        editableAll.addActionListener(this);
    }
    public void setMDLDisplay(MDLDisplay disp)
    {
        removeAll();
        visibleButtons.clear();
        editableButtons.clear();
        if( disp != null )
        {
            int nGeos = disp.getMDL().getGeosetsSize();
            setLayout(new GridLayout(nGeos*3+4,1));
            add( title = new JLabel(disp.getMDL().getFile().getName().split("\\.")[0]));
            add(hideAll);
            add(showAll);
            add(editableAll);
            for( int i = 0; i < nGeos; i++ )
            {
                add( new JLabel("Geoset " + (i+1)+":"));
                JCheckBox cb = new JCheckBox("Visible",disp.isGeosetVisible(i));
                cb.addActionListener(this);
                cb.addMouseListener(this);
                add(cb);
                visibleButtons.add(cb);
                cb = new JCheckBox("Editable",disp.isGeosetEditable(i));
                cb.addActionListener(this);
                cb.addMouseListener(this);
                editableButtons.add(cb);
                add(cb);
            }
            size = nGeos;
            dispModel = disp;
    //         updateMDLDisplay();
//             frame.pack();
        }
        repaint();
    }
    public void updateBoxCount()
    {
        MDLDisplay disp = dispModel;
        if( disp != null )
        {
            int nGeos = disp.getMDL().getGeosetsSize();
            if( nGeos > size )
            {
                System.out.println("Added missing Geoset display options");
                setLayout(new GridLayout(nGeos*3+4,1));
                title.setText(disp.getMDL().getFile().getName().split("\\.")[0]);
                for( int i = size; i < nGeos; i++ )
                {
                    add( new JLabel("Geoset " + (i+1)+":"));
                    JCheckBox cb = new JCheckBox("Visible",disp.isGeosetVisible(i));
                    cb.addActionListener(this);
                    cb.setSelected(false);
                    cb.setSelected(true);
                    cb.addMouseListener(this);
                    add(cb);
                    visibleButtons.add(cb);
                    cb = new JCheckBox("Editable",disp.isGeosetEditable(i));
                    cb.addActionListener(this);
                    cb.setSelected(false);
                    cb.setSelected(true);
                    cb.addMouseListener(this);
                    editableButtons.add(cb);
                    add(cb);
                }
            }
            else if( size > nGeos )
            {
                for( int i = size-1; i >= nGeos; i-- )
                {
                	remove(visibleButtons.get(i));
                	visibleButtons.remove(visibleButtons.get(i));
                	remove(this.getComponent(i*3+4));
                	remove(editableButtons.get(i));
                	editableButtons.remove(editableButtons.get(i));
                }
                setLayout(new GridLayout(nGeos*3+4,1));
            }
            this.validate();
            size = nGeos;
        }
    }
    public void updateMDLDisplay()
    {
        for( int i = 0; i < visibleButtons.size(); i++ )
        {
            JCheckBox cb = visibleButtons.get(i);
            dispModel.makeGeosetVisible(i,cb.isSelected());
        }
        for( int i = 0; i < editableButtons.size(); i++ )
        {
            JCheckBox cb = editableButtons.get(i);
            dispModel.makeGeosetEditable(i,cb.isSelected());
        }
    }
    public void actionPerformed(ActionEvent e)
    {
        for( int i = 0; i < visibleButtons.size(); i++ )
        {
            JCheckBox cb = visibleButtons.get(i);
            if( e.getSource() == cb )
            {
                dispModel.makeGeosetVisible(i,cb.isSelected());
                if( !cb.isSelected() )
                {
                    editableButtons.get(i).setSelected(false);
                    dispModel.makeGeosetEditable(i,false);
                }
            }
        }
        for( int i = 0; i < editableButtons.size(); i++ )
        {
            JCheckBox cb = editableButtons.get(i);
            if( e.getSource() == cb )
            {
                dispModel.makeGeosetEditable(i,cb.isSelected());
                if( cb.isSelected() )
                {
                    visibleButtons.get(i).setSelected(true);
                    dispModel.makeGeosetVisible(i,true);
                }
            }
        
        }
        if( e.getSource() == hideAll )
        {
            for( int i = 0; i < visibleButtons.size(); i++ )
            {
                JCheckBox cb = visibleButtons.get(i);
                if( cb.isSelected() ){
                	cb.doClick();
                }
            }
        }
        else if( e.getSource() == showAll )
        {
            for( int i = 0; i < visibleButtons.size(); i++ )
            {
                JCheckBox cb = visibleButtons.get(i);
                if( !cb.isSelected() ){
                	cb.doClick();
                }
            }
            for( int i = 0; i < editableButtons.size(); i++ )
            {
                JCheckBox cb = editableButtons.get(i);
                if( cb.isSelected() ){
                	cb.doClick();
                }
            }
        }
        else if( e.getSource() == editableAll )
        {
            for( int i = 0; i < editableButtons.size(); i++ )
            {
                JCheckBox cb = editableButtons.get(i);
                if( !cb.isSelected() ){
                	cb.doClick();
                }
            }
        }
        MainFrame.panel.repaint();
        if( dispModel.uvpanel != null )
        	dispModel.uvpanel.repaint();
    }
    public void mouseEntered(MouseEvent e)
    {
        for( int i = 0; i < visibleButtons.size(); i++ )
        {
            JCheckBox cb = visibleButtons.get(i);
            if( e.getSource() == cb )
            {
                dispModel.highlightGeoset(i,true);
            }
        }
        for( int i = 0; i < editableButtons.size(); i++ )
        {
            JCheckBox cb = editableButtons.get(i);
            if( e.getSource() == cb )
            {
                dispModel.highlightGeoset(i,true);
            }
        }
        MainFrame.panel.repaint();
        if( dispModel.uvpanel != null )
        	dispModel.uvpanel.repaint();
    }
    public void mouseExited(MouseEvent e)
    {
        for( int i = 0; i < visibleButtons.size(); i++ )
        {
            JCheckBox cb = visibleButtons.get(i);
            if( e.getSource() == cb )
            {
                dispModel.highlightGeoset(i,false);
            }
        }
        for( int i = 0; i < editableButtons.size(); i++ )
        {
            JCheckBox cb = editableButtons.get(i);
            if( e.getSource() == cb )
            {
                dispModel.highlightGeoset(i,false);
            }
        }
        MainFrame.panel.repaint();
        if( dispModel.uvpanel != null )
        	dispModel.uvpanel.repaint();
    }
    public void mousePressed(MouseEvent e)
    {
        
    }
    public void mouseReleased(MouseEvent e)
    {
        
    }
    public void mouseClicked(MouseEvent e)
    {
        
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        updateBoxCount();
        super.paintComponent(g);
    }
}
