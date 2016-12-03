package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * Camera class, these are the things most people would think of as a particle emitter,
 * I think. Blizzard favored use of these over ParticleEmitters and I do too simply because I
 * so often recycle data and there are more of these to use.
 * 
 * Eric Theller
 * 3/10/2012 3:32 PM
 */
public class Camera implements Named
{
    String name;
    
    Vertex Position;
    
    double FieldOfView;
    double FarClip;
    double NearClip;
    
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    
    Vertex targetPosition;
    ArrayList<AnimFlag> targetAnimFlags = new ArrayList<AnimFlag>();
    
    private Camera()
    {
        
    }
    public void setName(String text)
    {
        name = text;
    }
    public String getName()
    {
        return name;
    }
    public static Camera read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Camera") )
        {
            Camera c = new Camera();
            c.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") )
            {
                if( line.contains("Position") )
                {
                    c.Position = Vertex.parseText(line);
                }
                else if( line.contains("Rotation") || line.contains("Translation") )
                {
                    MDLReader.reset(mdl);
                    c.animFlags.add(AnimFlag.read(mdl));
                }
                else if( line.contains("FieldOfView") )
                {
                    c.FieldOfView = MDLReader.readDouble(line);
                }
                else if( line.contains("FarClip") )
                {
                    c.FarClip = MDLReader.readDouble(line);
                }
                else if( line.contains("NearClip") )
                {
                    c.NearClip = MDLReader.readDouble(line);
                }
                else if( line.contains("Target") )
                {
                    MDLReader.mark(mdl);
                    line = MDLReader.nextLine(mdl);
                    while( !line.startsWith("\t}") )
                    {
                        if( line.contains("Position") )
                        {
                            c.targetPosition = Vertex.parseText(line);
                        }
                        else if( line.contains("Translation") )
                        {
                            MDLReader.reset(mdl);
                            c.targetAnimFlags.add(AnimFlag.read(mdl));
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null,"Camera target did not recognize data at: "+line+"\nThis is probably not a major issue?");
                        }
                        MDLReader.mark(mdl);
                        line = MDLReader.nextLine(mdl);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null,"Camera did not recognize data at: "+line+"\nThis is probably not a major issue?");
                }
                
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return c;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Camera: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void printTo(PrintWriter writer)
    {
        //Remember to update the ids of things before using this
        // -- uses objectId value of idObject superclass
        // -- uses parentId value of idObject superclass
        // -- uses the parent (java Object reference) of idObject superclass
        writer.println(MDLReader.getClassName(this.getClass())+" \""+getName()+"\" {");
        writer.println("\tPosition "+Position.toString()+",");
        for( int i = 0; i < animFlags.size(); i++ )
        {
            if( animFlags.get(i).getName().equals("Translation") )
            {
                animFlags.get(i).printTo(writer,1);
            }
        }
        for( int i = 0; i < animFlags.size(); i++ )
        {
            if( animFlags.get(i).getName().equals("Rotation") )
            {
                animFlags.get(i).printTo(writer,1);
            }
        }
        writer.println("\tFieldOfView "+MDLReader.doubleToString(FieldOfView)+",");
        writer.println("\tFarClip "+MDLReader.doubleToString(FarClip)+",");
        writer.println("\tNearClip "+MDLReader.doubleToString(NearClip)+",");
        writer.println("\tTarget {");
        writer.println("\t\tPosition "+targetPosition.toString()+",");
        for( int i = 0; i < targetAnimFlags.size(); i++ )
        {
            targetAnimFlags.get(i).printTo(writer,2);
        }
        writer.println("\t}");
        writer.println("}");
    }
}
