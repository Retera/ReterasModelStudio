package com.matrixeater.src;
import javax.swing.JOptionPane;
import java.util.ArrayList;
public class Vertex
{
    double x = 0;
    double y = 0;
    double z = 0;
    public Vertex( double x, double y, double z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vertex( Vertex v )
    {
        x = v.x;
        y = v.y;
        z = v.z;
    }
    public double getCoord(byte dim)
    {
        switch(dim)
        {
            case 0: return x;
            case 1: return y;
            case 2: return z;
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
            case 2: z = value; break;
        }
    }
    public void translateCoord(byte dim, double value)
    {
        switch(dim)
        {
            case 0: x += value; break;
            case 1: y += value; break;
            case 2: z += value; break;
        }
    }
    public void setTo(Vertex v)
    {
        x = v.x;
        y = v.y;
        z = v.z;
    }
    public boolean equalLocs(Vertex v)
    {
        return x == v.x && y == v.y && z == v.z;
    }
    public double getX()
    {
        return x;
    }
    public double getY()
    {
        return y;
    }
    public double getZ()
    {
        return z;
    }
    public static Vertex parseText(String input)
    {
        String [] entries = input.split(",");
        Vertex temp = null;
        double x = 0;
        double y = 0;
        double z = 0;
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
            y = Double.parseDouble(entries[1]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        try
        {
            z = Double.parseDouble(entries[2].split("}")[0]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        temp = new Vertex(x,y,z);
        return temp;
    }
    public String toString()
    {
        return "{ "+MDLReader.doubleToString(x)+", "+MDLReader.doubleToString(y)+", "+MDLReader.doubleToString(z)+" }";
    }
    public String toStringLessSpace()
    {
        return "{"+MDLReader.doubleToString(x)+", "+MDLReader.doubleToString(y)+", "+MDLReader.doubleToString(z)+"}";
    }
    public static Vertex centerOfGroup(ArrayList<? extends Vertex> group)
    {
        double xTot = 0;
        double yTot = 0;
        double zTot = 0;
        for( Vertex v: group )
        {
            xTot += v.getX();
            yTot += v.getY();
            zTot += v.getZ();
        }
        xTot /= group.size();
        yTot /= group.size();
        zTot /= group.size();
        return new Vertex(xTot,yTot,zTot);
    }
}
