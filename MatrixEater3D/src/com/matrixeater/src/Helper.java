package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import javax.swing.JOptionPane;
/**
 * Write a description of class Helper here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Helper extends Bone//Haha blizz
{
    //Level 80 Tauren Helpers make code run smoothly
    private Helper()
    {
        super();
    }
    public Helper(int j)
    {
        super();
    }
    public Helper(Helper h)
    {
        super(h);
    }
    public static Helper read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Helper") )
        {
            Helper b = new Helper();
            b.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( (!line.contains("}")  || line.contains("},") || line.contains("\t}") )  && !line.equals("COMPLETED PARSING") )
            {
                if( line.contains("ObjectId") )
                {
                    b.objectId = MDLReader.readInt(line);
                }
                else if( line.contains("GeosetId") )
                {
                    String field = MDLReader.readField(line);
                    try
                    {
                        b.geosetId = Integer.parseInt(field);
//                         b.geoset = mdlr.getGeoset(b.geosetId);
                    }
                    catch (Exception e)
                    {
                        if( field.equals("Multiple") )
                        {
                            b.multiGeoId = true;
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing: Could not interpret integer from: "+line);
                        }
                    }
                }
                else if( line.contains("GeosetAnimId") )
                {
                    String field = MDLReader.readField(line);
                    b.hasGeoAnim = true;
                    try
                    {
                        b.geosetAnimId = Integer.parseInt(field);
//                         b.geosetAnim = mdlr.getGeosetAnim(b.geosetAnimId);
                    }
                    catch (Exception e)
                    {
                        if( field.equals("None") )
                        {
                            b.geosetAnim = null;
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing: Could not interpret integer from: "+line);
                        }
                    }
                }
                else if( line.contains("Parent") )
                {
                    b.parentId = MDLReader.splitToInts(line)[0];
//                     b.parent = mdlr.getIdObject(b.parentId);
                }
                else if( ( line.contains("Scaling") || line.contains("Rotation") || line.contains("Translation") ) && !line.contains("DontInherit") )
                {
                    MDLReader.reset(mdl);
                    b.animFlags.add(AnimFlag.read(mdl));
                }
                else//Flags like Billboarded
                {
                    b.flags.add(MDLReader.readFlag(line));
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return b;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Bone: Missing or unrecognized open statement.");
        }
        return null;
    }
    //printTo is already written as a part of bone; these two things are stupidly the same
    public IdObject copy()
    {
        return new Helper(this);
    }
}
