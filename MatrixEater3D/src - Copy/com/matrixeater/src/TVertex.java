package com.matrixeater.src;
import java.util.ArrayList;

import javax.swing.JOptionPane;
public class TVertex
{
	GeosetVertex parent;
    double x = 0;
    double y = 0;
    public TVertex( double x, double y )
    {
        this.x = x;
        this.y = y;
    }
    public TVertex(TVertex old)
    {
        this.x = old.x;
        this.y = old.y;
    }
	/**
	 * This method was designed late and is not reliable unless updated by an outside source.
	 * @param gv
	 */
	public void setParent(GeosetVertex gv)
	{
		parent = gv;
	}
	/**
	 * This method was designed late and is not reliable unless updated by an outside source.
	 * @return
	 */
	public GeosetVertex getParent()
	{
		return parent;
	}
    public double getCoord(float dim)
    {
        int i = (int)dim;
        switch(i)
        {
            case 0: return x;
            case 1: return y;
        }
        return 0;
    }
    public void setCoord(byte dim, double value)
    {
    	if( !Double.isNaN(value))
        switch(dim)
        {
            case 0: x = value; break;
            case 1: y = value; break;
        }
    }
    public void translateCoord(byte dim, double value)
    {
        switch(dim)
        {
            case 0: x += value; break;
            case 1: y += value; break;
        }
    }
    public void setTo(TVertex v)
    {
        x = v.x;
        y = v.y;
    }
    public double getX()
    {
        return x;
    }
    public double getY()
    {
        return y;
    }
    public static TVertex parseText(String input)
    {
        String [] entries = input.split(",");
        TVertex temp = null;
        double x = 0;
        double y = 0;
        try
        {
            x = Double.parseDouble(entries[0].split("\\{")[1]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        try
        {
            y = Double.parseDouble(entries[1].split("}")[0]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        temp = new TVertex(x,y);
        return temp;
    }
    public String toString()
    {
        return "{ "+MDLReader.doubleToString(x)+", "+MDLReader.doubleToString(y)+" }";
    }
    public static TVertex centerOfGroup(ArrayList<? extends TVertex> group)
    {
        double xTot = 0;
        double yTot = 0;
        for( TVertex v: group )
        {
            xTot += v.getX();
            yTot += v.getY();
        }
        xTot /= group.size();
        yTot /= group.size();
        return new TVertex(xTot,yTot);
    }
}