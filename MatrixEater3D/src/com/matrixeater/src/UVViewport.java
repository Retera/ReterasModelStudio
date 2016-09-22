package com.matrixeater.src;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Point;
import java.awt.geom.*;
import java.awt.*;
import javax.swing.DefaultListCellRenderer;
public class UVViewport extends JPanel implements MouseListener, ActionListener, MouseWheelListener
{
	ArrayList<Image> backgrounds = new ArrayList<Image>();
    MDLDisplay dispMDL;
    double m_a = 0;
    double m_b = 0;
    double m_zoom = 1;
    double aspectRatio = 1;
    Point lastClick;
    Point selectStart;
    Point actStart;
    Timer clickTimer = new Timer(16,this);
    boolean mouseInBounds = false;
    JPopupMenu contextMenu;
    JMenuItem placeholderButton;
    UVPanel parent;
    Component boxX, boxY;
    public UVViewport(MDLDisplay dispMDL, UVPanel parent)
    {
        //Dimension 1 and Dimension 2, these specify which dimensions to display.
        //the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions
        //
        //Viewport border
        setBorder(BorderFactory.createBevelBorder(1));
        setBackground(new Color(255,255,255));
        setMinimumSize(new Dimension(400,400));
        add(boxX = Box.createHorizontalStrut(400));
        add(boxY = Box.createVerticalStrut(400));
        setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
        this.dispMDL = dispMDL;
        addMouseListener(this);
        addMouseWheelListener(this);
        
        contextMenu = new JPopupMenu();
        placeholderButton = new JMenuItem("Placeholder Button");
        placeholderButton.addActionListener(this);
        contextMenu.add(placeholderButton);
        
        this.parent = parent;
    }
    public void init()
    {
        m_zoom = (double)getWidth();
        m_a = geomX(0);
        m_b = geomY(0);
    }
    public void setPosition(double a, double b)
    {
        m_a = a;
        m_b = b;
    }
    public void translate(double a, double b)
    {
        m_a += a/aspectRatio;
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
        for( int i = 0; i < backgrounds.size(); i++ )
        {
        	if( parent.wrapImage.isSelected() )
        	{
        		for( int y = -15; y < 15; y++ )
        		{
            		for( int x = -15; x < 15; x++ )
            		{
                    	g.drawImage(backgrounds.get(i),(int)convertX(x),(int)convertY(y),(int)(convertX(x+1)-convertX(x)),(int)(convertY(y+1)-convertY(y)),null);
            		}
        		}
        	}
        	else
        		g.drawImage(backgrounds.get(i),(int)convertX(0),(int)convertY(0),(int)(convertX(1)-convertX(0)),(int)(convertY(1)-convertY(0)),null);
        }
        dispMDL.drawGeosets(g,this,vertexSize);
        
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
            JOptionPane.showMessageDialog(null,"Error retrieving mouse coordinates. (Probably not a major issue. Due to sleep mode?)");
        }
    }
    public double convertX(double x)
    {
        return ((x+m_a)*m_zoom)*aspectRatio+getWidth()/2;
    }
    public double convertY(double y)
    {
        return ((y+m_b)*m_zoom)+getHeight()/2;
    }
    public double geomX(double x)
    {
        return (x-getWidth()/2)/aspectRatio/m_zoom-m_a;
    }
    public double geomY(double y)
    {
        return ((y-getHeight()/2)/m_zoom-m_b);
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

                m_a += ((int)mx - lastClick.x)/aspectRatio/m_zoom;
                m_b += ((int)my - lastClick.y)/m_zoom;
                lastClick.x = (int)(mx);
                lastClick.y = (int)my;
            }
            parent.setMouseCoordDisplay(((mx-getWidth()/2)/aspectRatio/m_zoom)-m_a,(((my-getHeight()/2)/m_zoom)-m_b));
            
            if( actStart != null )
            {
                Point actEnd = new Point((int)mx,(int)my);
                Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x),geomY(actStart.y));
                Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x),geomY(actEnd.y));
                dispMDL.updateUVAction(convertedStart,convertedEnd);
                actStart = actEnd;
            }
            repaint();
        }
        else if( e.getSource() == placeholderButton )
        {
        	JOptionPane.showMessageDialog(null,"Placeholder code.");
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
            dispMDL.startUVAction(convertedStart,parent.currentActionType());
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
            dispMDL.selectTVerteces(area,parent.currentSelectionType());
            selectStart = null;
        }
        else if( e.getButton() == MouseEvent.BUTTON3 && actStart != null )
        {
            Point actEnd = new Point(e.getX(),e.getY());
            Point2D.Double convertedStart = new Point2D.Double(geomX(actStart.x),geomY(actStart.y));
            Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x),geomY(actEnd.y));
            dispMDL.finishUVAction(convertedStart,convertedEnd);
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
                m_a -= ((mx - getWidth()/2)/aspectRatio)*( 1 / m_zoom - 1 / ( m_zoom * 1.15 ) );
                m_b -= (my - getHeight()/2)*( 1 / m_zoom - 1 / ( m_zoom * 1.15 ) );
                m_zoom *= 1.15;
            }
            else
            {
                m_zoom /= 1.15;
                m_a -= ((mx - getWidth()/2)/aspectRatio)*( 1 / ( m_zoom * 1.15 ) - 1 / m_zoom );
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
    
    public void setAspectRatio(double ratio)
    {
    	aspectRatio = ratio;
    	setMinimumSize(new Dimension((int)(400*ratio),400));
    	remove(boxX);
        add(boxX = Box.createHorizontalStrut((int)(400*ratio)));
        parent.packFrame();
    }
    
    public void addBackgroundImage(Image i)
    {
    	backgrounds.add(i);
    	setAspectRatio(i.getWidth(null)/(double)i.getHeight(null));
    }
    public void clearBackgroundImage()
    {
    	backgrounds.clear();
    }
}