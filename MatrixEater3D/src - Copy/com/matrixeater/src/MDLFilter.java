package com.matrixeater.src;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
/**
 * Write a description of class MDLFilter here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MDLFilter extends FileFilter
{
    public boolean accept(File f)
    {
        //Special thanks to the Oracle Java tutorials for a lot of the major code concepts here
        if( f.isDirectory() )
        {
            return true;
        }
        String name = f.getName();
        int perIndex = name.lastIndexOf('.');
        if( name.substring(perIndex+1).toLowerCase().equals("mdl") && perIndex > 0 && perIndex < name.length() - 1 )
        {
            return true;
        }
        return false;
    }
    
    public String getDescription()
    {
        return "Warcraft III Model Files \"-.mdl\"";
    }
}
