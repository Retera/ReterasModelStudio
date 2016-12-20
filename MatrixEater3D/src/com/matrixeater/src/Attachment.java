package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * Write a description of class Attachment here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Attachment extends IdObject implements VisibilitySource
{
    String path = null;
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    ArrayList<String> flags = new ArrayList<String>();
    
    int AttachmentID = 0;
    private Attachment()
    {
        
    }
    public IdObject copy()
    {
        Attachment x = new Attachment();
        
        x.name = name + " copy";
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.path = path;
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        flags = new ArrayList<String>(x.flags);
        return x;
    }
    public static Attachment read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Attachment") )
        {
            Attachment at = new Attachment();
            at.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") )
            {
                if( line.contains("ObjectId") )
                {
                    at.objectId = MDLReader.readInt(line);
                }
                else if( line.contains("Parent") )
                {
                    at.parentId = MDLReader.splitToInts(line)[0];
//                     at.parent = mdlr.getIdObject(at.parentId);
                }
                else if( line.contains("Path") )
                {
                    at.path = MDLReader.readName(line);
                }
                else if( line.contains("AttachmentID ") )
                {
                    at.AttachmentID = MDLReader.readInt(line);
                }
                else if( ( line.contains("Visibility") || line.contains("Scaling") || line.contains("Translation") || line.contains("Rotation") ) && !line.contains("DontInherit") )//Visibility, Rotation, etc
                {
                    MDLReader.reset(mdl);
                    at.animFlags.add(AnimFlag.read(mdl));
                }
                else
                {
                    at.flags.add(MDLReader.readFlag(line));
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return at;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Attachment: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void printTo(PrintWriter writer)
    {
        //Remember to update the ids of things before using this
        // -- uses objectId value of idObject superclass
        // -- uses parentId value of idObject superclass
        // -- uses the parent (java Object reference) of idObject superclass
        // -- uses geosetAnimId
        // -- uses geosetId
        writer.println(MDLReader.getClassName(this.getClass())+" \""+getName()+"\" {");
        if( objectId != -1 )
        {
            writer.println("\tObjectId "+objectId+",");
        }
        if( parentId != -1 )
        {
            writer.println("\tParent "+parentId+",\t// \""+parent.getName()+"\"");
        }
        if( path != null )
        {
            writer.println("\tPath \""+path+"\",");
        }
        if( AttachmentID != 0 )
        {
            writer.println("\tAttachmentID "+AttachmentID+",");
        }
        for( int i = 0; i < flags.size(); i++ )
        {
            writer.println("\t"+flags.get(i)+",");
        }
        for( int i = 0; i < animFlags.size(); i++ )
        {
            animFlags.get(i).printTo(writer,1);
        }
        writer.println("}");
    }
    //VisibilitySource methods
    public void setVisibilityFlag(AnimFlag flag)
    {
        int count = 0;
        int index = 0;
        for( int i = 0; i< animFlags.size(); i++ )
        {
            AnimFlag af = animFlags.get(i);
            if( af.getName().equals("Visibility") || af.getName().equals("Alpha") )
            {
                count++;
                index = i;
                animFlags.remove(af);
            }
        }
        if( flag != null )
        animFlags.add(index,flag);
        if( count > 1 )
        {
            JOptionPane.showMessageDialog(null,"Some visiblity animation data was lost unexpectedly during overwrite in "+getName()+".");
        }
    }
    public AnimFlag getVisibilityFlag()
    {
        int count = 0;
        AnimFlag output = null;
        for( AnimFlag af: animFlags )
        {
            if( af.getName().equals("Visibility") || af.getName().equals("Alpha") )
            {
                count++;
                output = af;
            }
        }
        if( count > 1 )
        {
            JOptionPane.showMessageDialog(null,"Some visiblity animation data was lost unexpectedly during retrieval in "+getName()+".");
        }
        return output;
    }
    public String visFlagName()
    {
        return "Visibility";
    }
    public void flipOver(byte axis)
    {
        String currentFlag = "Rotation";
        for( int i = 0; i < animFlags.size(); i++ )
        {
            AnimFlag flag = animFlags.get(i);
            flag.flipOver(axis);
        }
    }
}
