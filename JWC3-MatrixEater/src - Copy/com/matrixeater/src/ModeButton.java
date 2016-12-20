package com.matrixeater.src;
import javax.swing.*;
import java.awt.*;
/**
 * Cool gradient colored JButton
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ModeButton extends JButton
{
    GradientPaint gPaint;
    public ModeButton(String s)
    {
        super(s);
    }
    public void paintComponent(Graphics g)
    {
        if( gPaint != null )
        {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setPaint(gPaint);
            int amt = 4;
            int indent = 1;
            g2.fillRect(indent, indent, getWidth()-indent*3,getHeight()-indent*3);
            g2.setColor(Color.black);
            g2.drawRoundRect(indent, indent, getWidth()-indent*3,getHeight()-indent*3, amt, amt);
            g2.dispose();
        }
        super.paintComponent(g);
    }
    public void setColors(Color a, Color b)
    {
//    	setBackground(a);
//    	setOpaque(false);
        setContentAreaFilled(false);
        gPaint = new GradientPaint(new Point(0,10),a,new Point(0,getHeight()),b,true);
    }
    public void resetColors()
    {
//    	this.setBackground(null);
//    	setOpaque(true);
        gPaint = null;
        setContentAreaFilled(true);
    }
}
