package com.matrixeater.src;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.Timer;
public class Viewport extends JPanel implements MouseListener, ActionListener, MouseWheelListener
{
    MDLDisplay dispMDL;
    byte m_d1;
    byte m_d2;
    double m_a = 0;
    double m_b = 0;
    double m_zoom = 1;
    Point lastClick;
    Point selectStart;
    Point actStart;
    Timer clickTimer = new Timer(16,this);
    boolean mouseInBounds = false;
    JPopupMenu contextMenu;
    JMenuItem reAssignMatrix;
    JMenuItem cogBone;
    public Viewport(byte d1, byte d2, MDLDisplay dispMDL)
    {
        //Dimension 1 and Dimension 2, these specify which dimensions to display.
        //the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions
        //
        m_d1 = d1;
        m_d2 = d2;
        //Viewport border
        setBorder(BorderFactory.createBevelBorder(1));
        setBackground(new Color(255,255,255));
        setMinimumSize(new Dimension(200,200));
        add(Box.createHorizontalStrut(200));
        add(Box.createVerticalStrut(200));
        setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
        this.dispMDL = dispMDL;
        addMouseListener(this);
        addMouseWheelListener(this);
        
        contextMenu = new JPopupMenu();
        reAssignMatrix = new JMenuItem("Re-assign Matrix");
        reAssignMatrix.addActionListener(this);
        contextMenu.add(reAssignMatrix);
        cogBone = new JMenuItem("Auto-Center Bone(s)");
        cogBone.addActionListener(this);
        contextMenu.add(cogBone);
        
    }
    public void setPosition(double a, double b)
    {
        m_a = a;
        m_b = b;
    }
    public void translate(double a, double b)
    {
        m_a += a;
        m_b += b;
    }
    public void zoom(double amount)
    {
        m_zoom *= (1+amount);
    }
    public double getZoomAmount()
    {
        return m_zoom;
    }
    public Point2D.Double getDisplayOffset()
    {
        return new Point2D.Double(m_a,m_b);
    }
    public byte getPortFirstXYZ()
    {
        return m_d1;
    }
    public byte getPortSecondXYZ()
    {
        return m_d2;
    }
    public BufferedImage getBufferedImage()
    {
        BufferedImage image = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
        paintComponent(image.getGraphics(),5);
        return image;
    }
    public void paintComponent(Graphics g)
    {
        paintComponent(g,1);
    }
    public void paintComponent(Graphics g, int vertexSize)
    {
        super.paintComponent(g);
        dispMDL.drawGeosets(g,this,vertexSize);
        dispMDL.drawPivots(g,this,vertexSize);
        dispMDL.drawCameras(g,this,vertexSize);
        switch((int)m_d1)
        {
            case 0: g.setColor( new Color( 0, 255, 0 ) ); break;
            case 1: g.setColor( new Color( 255, 0, 0 ) ); break;
            case 2: g.setColor( new Color( 0, 0, 255 ) ); break;
        }
        //g.setColor( new Color( 255, 0, 0 ) );
        g.drawLine((int)Math.round(convertX(0)),(int)Math.round(convertY(0)),(int)Math.round(convertX(5)),(int)Math.round(convertY(0)));
        
        switch((int)m_d2)
        {
            case 0: g.setColor( new Color( 0, 255, 0 ) ); break;
            case 1: g.setColor( new Color( 255, 0, 0 ) ); break;
            case 2: g.setColor( new Color( 0, 0, 255 ) ); break;
        }
        //g.setColor( new Color( 255, 0, 0 ) );
        g.drawLine((int)Math.round(convertX(0)),(int)Math.round(convertY(0)),(int)Math.round(convertX(0)),(int)Math.round(convertY(5)));
        
        //Visual effects from user controls
        int xoff = 0;
        int yoff = 0;
        Component temp = this;
        while( temp != null )
        {
            xoff+=temp.getX();
            yoff+=temp.getY();
            if( temp.getClass() == ModelPanel.class )
            {
                temp = MainFrame.panel;
            }
            else
            {
                temp = temp.getParent();
            }
        }
        
        try {
            double mx = (MouseInfo.getPointerInfo().getLocation().x-xoff);//MainFrame.frame.getX()-8);
            double my = (MouseInfo.getPointerInfo().getLocation().y-yoff);//MainFrame.frame.getY()-30);
    
            //SelectionBox:
            if( selectStart != null )
            {
                Point sEnd = new Point((int)mx,(int)my);
                Rectangle2D.Double r = pointsToRect(selectStart,sEnd);
                g.setColor(MDLDisplay.selectColor);
                ((Graphics2D)g).draw(r);
            }
        }
        catch (Exception exc)
        {
        	exc.printStackTrace();
            //JOptionPane.showMessageDialog(null,"Error retrieving mouse coordinates. (Probably not a major issue. Due to sleep mode?)");
        }
    }
    public double convertX(double x)
    {
        return (x+m_a)*m_zoom+getWidth()/2;
    }
    public double convertY(double y)
    {
        return ((-y+m_b)*m_zoom)+getHeight()/2;
    }
    public double geomX(double x)
    {
        return (x-getWidth()/2)/m_zoom-m_a;
    }
    public double geomY(double y)
    {
        return -((y-getHeight()/2)/m_zoom-m_b);
    }
    public void actionPerformed(ActionEvent e)
    {
        if( e.getSource() == clickTimer )
        {
            int xoff = 0;
            int yoff = 0;
            Component temp = this;
            while( temp != null )
            {
                xoff+=temp.getX();
                yoff+=temp.getY();
                if( temp.getClass() == ModelPanel.class )
                {
                    temp = MainFrame.panel;
                }
                else
                {
                    temp = temp.getParent();
                }
            }
            double mx = (MouseInfo.getPointerInfo().getLocation().x-xoff);//MainFrame.frame.getX()-8);
            double my = (MouseInfo.getPointerInfo().getLocation().y-yoff);//MainFrame.frame.getY()-30);
//                 JOptionPane.showMessageDialog(null,mx+","+my+" as mouse, "+lastClick.x+","+lastClick.y+" as last.");
//                 System.out.println(xoff+" and "+mx);
            if( lastClick != null )
            {

                m_a += ((int)mx - lastClick.x)/m_zoom;
                m_b += ((int)my - lastClick.y)/m_zoom;
                lastClick.x = (int)mx;
                lastClick.y = (int)my;
            }
            MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));
            
            if( actStart != null )
            {
                Point actEnd = new Point((int)mx,(int)my);
                Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x),geomY(actStart.y));
                Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x),geomY(actEnd.y));
                dispMDL.updateAction(convertedStart,convertedEnd,m_d1,m_d2);
                actStart = actEnd;
            }
            repaint();
        }
        else if( e.getSource() == reAssignMatrix )
        {
            MatrixPopup matrixPopup = new MatrixPopup(dispMDL.getMDL());
            String [] words = { "Accept", "Cancel"};
            int i = JOptionPane.showOptionDialog(MainFrame.panel,matrixPopup,"Rebuild Matrix",JOptionPane.PLAIN_MESSAGE,JOptionPane.YES_NO_OPTION,null,words,words[1]);
            if( i == 0 )
            {
//                 JOptionPane.showMessageDialog(null,"action approved");
                dispMDL.setMatrix(matrixPopup.newRefs);
            }
        }
        else if( e.getSource() == cogBone )
        {
            dispMDL.cogBones();
        }
    }
    public void mouseEntered(MouseEvent e)
    {
        clickTimer.setRepeats(true);
        clickTimer.start();
        mouseInBounds = true;
    }
    public void mouseExited(MouseEvent e)
    {
        if( selectStart == null && actStart == null && lastClick == null )
        clickTimer.stop();
        mouseInBounds = false;
    }
    public void mousePressed(MouseEvent e)
    {
        if( e.getButton() == MouseEvent.BUTTON2 )
        {
            lastClick = new Point(e.getX(),e.getY());
        }
        else if( e.getButton() == MouseEvent.BUTTON1 )
        {
            selectStart = new Point(e.getX(),e.getY());
        }
        else if( e.getButton() == MouseEvent.BUTTON3 )
        {
            actStart = new Point(e.getX(),e.getY());
            Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x),geomY(actStart.y));
            dispMDL.startAction(convertedStart,m_d1,m_d2,MainFrame.panel.currentActionType());
        }
    }
    public void mouseReleased(MouseEvent e)
    {
        if( e.getButton() == MouseEvent.BUTTON2 )
        {
            m_a += (e.getX() - lastClick.x)/m_zoom;
            m_b += (e.getY() - lastClick.y)/m_zoom;
            lastClick = null;
        }
        else if( e.getButton() == MouseEvent.BUTTON1 && selectStart != null )
        {
            Point selectEnd = new Point(e.getX(),e.getY());
            Rectangle2D.Double area = pointsToGeomRect(selectStart,selectEnd);
//             System.out.println(area);
            dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
            selectStart = null;
        }
        else if( e.getButton() == MouseEvent.BUTTON3 && actStart != null )
        {
            Point actEnd = new Point(e.getX(),e.getY());
            Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x),geomY(actStart.y));
            Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x),geomY(actEnd.y));
            dispMDL.finishAction(convertedStart,convertedEnd,m_d1,m_d2);
            actStart = null;
        }
        if( !mouseInBounds && selectStart == null && actStart == null && lastClick == null )
        {
            clickTimer.stop();
            repaint();
        }
        MainFrame.panel.refreshUndo();
    }
    public void mouseClicked(MouseEvent e)
    {
        if( e.getButton() == MouseEvent.BUTTON3 )
        {
            
//             if( actEnd.equals(actStart) )
//             {
//                 actStart = null;
                contextMenu.show(this,e.getX(),e.getY());
//             }
        }
    }
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int wr = e.getWheelRotation();
        boolean neg = wr < 0;
        
        //get mouse coords
        int xoff = 0;
        int yoff = 0;
        Component temp = this;
        while( temp != null )
        {
            xoff+=temp.getX();
            yoff+=temp.getY();
            if( temp.getClass() == ModelPanel.class )
            {
                temp = MainFrame.panel;
            }
            else
            {
                temp = temp.getParent();
            }
        }
        double mx = (MouseInfo.getPointerInfo().getLocation().x-xoff);
        double my = (MouseInfo.getPointerInfo().getLocation().y-yoff);

        if( neg )
        {
            wr = -wr;
        }
        for( int i = 0; i < wr; i++ )
        {
            if( neg )
            {
                m_a -= (mx - getWidth()/2)*( 1 / m_zoom - 1 / ( m_zoom * 1.15 ) );
                m_b -= (my - getHeight()/2)*( 1 / m_zoom - 1 / ( m_zoom * 1.15 ) );
                m_zoom *= 1.15;
            }
            else
            {
                m_zoom /= 1.15;
                m_a -= (mx - getWidth()/2)*( 1 / ( m_zoom * 1.15 ) - 1 / m_zoom );
                m_b -= (my - getHeight()/2)*( 1 / ( m_zoom * 1.15 ) - 1 / m_zoom );
            }
        }
    }
    public Rectangle2D.Double pointsToGeomRect(Point a, Point b)
    {
        Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x),geomX(b.x)),Math.min(geomY(a.y),geomY(b.y)));
        Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x),geomX(b.x)),Math.max(geomY(a.y),geomY(b.y)));
        Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x,topLeft.y,(lowRight.x-(topLeft.x)),((lowRight.y)-(topLeft.y)));
        return temp;
    }
    public Rectangle2D.Double pointsToRect(Point a, Point b)
    {
        Point2D.Double topLeft = new Point2D.Double(Math.min((a.x),(b.x)),Math.min((a.y),(b.y)));
        Point2D.Double lowRight = new Point2D.Double(Math.max((a.x),(b.x)),Math.max((a.y),(b.y)));
        Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x,topLeft.y,(lowRight.x-(topLeft.x)),((lowRight.y)-(topLeft.y)));
        return temp;
    }
}