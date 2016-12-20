package com.matrixeater.src;
import javax.swing.JOptionPane;
public class Normal extends Vertex
{
    public Normal( double x, double y, double z )
    {
        super(x,y,z);
    }
    public Normal( Normal oldNorm )
    {
        super( oldNorm.x, oldNorm.y, oldNorm.z );
    }
    public static Normal parseText(String input)
    {
        String [] entries = input.split(",");
        Normal temp = null;
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
        temp = new Normal(x,y,z);
        return temp;
    }
}