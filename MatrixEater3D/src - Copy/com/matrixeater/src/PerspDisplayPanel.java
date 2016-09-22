package com.matrixeater.src;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
/**
 * Write a description of class DisplayPanel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PerspDisplayPanel extends JPanel implements ActionListener
{
    private MDLDisplay dispMDL;
    private PerspectiveViewport vp;
    private JPanel vpp;
    private String title;
    private JButton up, down, left, right, plusZoom, minusZoom;
    //private JCheckBox wireframe;
    public PerspDisplayPanel(String title, MDLDisplay dispMDL)
    {
        super();
        setBorder(BorderFactory.createTitledBorder(title));//BorderFactory.createCompoundBorder(
//             BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)
//             ));
        setOpaque(true);
        
        //wireframe = new JCheckBox("Wireframe");
        //add(wireframe);
        setViewport(dispMDL);
        this.title = title;
        this.dispMDL = dispMDL;
        
        plusZoom = new JButton("");
        Dimension dim = new Dimension(20,20);
        plusZoom.setMaximumSize(dim);
        plusZoom.setMinimumSize(dim);
        plusZoom.setPreferredSize(dim);
        plusZoom.setIcon(new ImageIcon(PerspDisplayPanel.class.getResource("ImageBin/Plus.png")));
        plusZoom.addActionListener(this);
//        add(plusZoom);

        minusZoom = new JButton("");
        minusZoom.setMaximumSize(dim);
        minusZoom.setMinimumSize(dim);
        minusZoom.setPreferredSize(dim);
        minusZoom.setIcon(new ImageIcon(PerspDisplayPanel.class.getResource("ImageBin/Minus.png")));
        minusZoom.addActionListener(this);
//        add(minusZoom);

        up = new JButton("");
        dim = new Dimension(32,16);
        up.setMaximumSize(dim);
        up.setMinimumSize(dim);
        up.setPreferredSize(dim);
        up.setIcon(new ImageIcon(PerspDisplayPanel.class.getResource("ImageBin/ArrowUp.png")));
        up.addActionListener(this);
//        add(up);
        
        down = new JButton("");
        down.setMaximumSize(dim);
        down.setMinimumSize(dim);
        down.setPreferredSize(dim);
        down.setIcon(new ImageIcon(PerspDisplayPanel.class.getResource("ImageBin/ArrowDown.png")));
        down.addActionListener(this);
//        add(down);
        
        dim = new Dimension(16,32);
        left = new JButton("");
        left.setMaximumSize(dim);
        left.setMinimumSize(dim);
        left.setPreferredSize(dim);
        left.setIcon(new ImageIcon(PerspDisplayPanel.class.getResource("ImageBin/ArrowLeft.png")));
        left.addActionListener(this);
//        add(left);
        
        right = new JButton("");
        right.setMaximumSize(dim);
        right.setMinimumSize(dim);
        right.setPreferredSize(dim);
        right.setIcon(new ImageIcon(PerspDisplayPanel.class.getResource("ImageBin/ArrowRight.png")));
        right.addActionListener(this);
//        add(right);
        
        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
        		.addComponent(vp) );
        //		.addComponent(wireframe));
//            .addComponent(vp)
//            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                .addComponent(plusZoom)
//                .addComponent(minusZoom)
//                .addGroup(layout.createSequentialGroup()
//                    .addComponent(left)
//                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                        .addComponent(up)
//                        .addComponent(down))
//                    .addComponent(right)))
//            );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
        		.addComponent(vp) );
        //		.addComponent(wireframe));
//        .addComponent(vp)
//        .addGroup(layout.createSequentialGroup()
//            .addComponent(plusZoom)
//            .addGap(16)
//            .addComponent(minusZoom)
//            .addGap(16)
//            .addComponent(up)
//            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//                .addComponent(left)
//                .addComponent(right))
//            .addComponent(down)
//            ));
//        
        setLayout(layout);
//		 add(Box.createHorizontalStrut(200));
//		 add(Box.createVerticalStrut(200));
//		 setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
//        setLayout(new GridLayout(1,1));
    }
    public void addGeosets(List<Geoset> list)
    {
    	vp.addGeosets(list);
    }
    public void reloadTextures()
    {
    	vp.reloadTextures();
    }
    public void reloadAllTextures()
    {
    	vp.reloadAllTextures();
    }
    public void setViewport(MDLDisplay dispModel)
    {
        try {
			vp = new PerspectiveViewport(dispModel);
			//vp.setWireframeHandler(wireframe);
//			vpp = new JPanel();
//			vpp.add(Box.createHorizontalStrut(200));
//			vpp.add(Box.createVerticalStrut(200));
//			vpp.setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
//			vpp.add(vp);
//			vp.initGL();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        add(vp);
    }
    public PerspectiveViewport getViewport()
    {
    	return vp;
    }
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        //g.drawString(title,3,3);
        vp.repaint();
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
        if( e.getSource() == down )
        {
            vp.translate(0,(-20*(1/vp.getZoomAmount())));
            vp.repaint();
        }
        if( e.getSource() == left )
        {
            vp.translate((20*(1/vp.getZoomAmount())),0);
            vp.repaint();
        }
        if( e.getSource() == right )
        {
            vp.translate((-20*(1/vp.getZoomAmount())),0);//*vp.getZoomAmount()
            vp.repaint();
        }
        if( e.getSource() == plusZoom )
        {
            vp.zoom(.15);
            vp.repaint();
        }
        if( e.getSource() == minusZoom )
        {
            vp.zoom(-.15);
            vp.repaint();
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
}
