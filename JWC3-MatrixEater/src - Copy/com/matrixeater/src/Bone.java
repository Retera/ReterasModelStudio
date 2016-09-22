package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * Bones that make geometry animate.
 * 
 * Eric Theller
 * 11/10/2011
 */
public class Bone extends IdObject
{
    int geosetId = -1;
    boolean multiGeoId;
    Geoset geoset;
    
    int geosetAnimId = -1;
    GeosetAnim geosetAnim;
    boolean hasGeoAnim;//Sometimes its "None," sometimes it's not used
    
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    ArrayList<String> flags = new ArrayList<String>();
    protected Bone()
    {
        
    }
    public Bone(Bone b)
    {
        name = b.name + " copy";
        pivotPoint = new Vertex(b.pivotPoint);
        objectId = b.objectId;
        parentId = b.parentId;
        parent = b.parent;
        
        geosetId = b.geosetId;
        multiGeoId = b.multiGeoId;
        geoset = b.geoset;
        geosetAnimId = b.geosetAnimId;
        geosetAnim = b.geosetAnim;
        hasGeoAnim = b.hasGeoAnim;
        for( AnimFlag af: b.animFlags )
        {
            animFlags.add(new AnimFlag(af));
        }
        flags = new ArrayList<String>(b.flags);
    }
    public static Bone read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Bone") )
        {
            Bone b = new Bone();
            b.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") )
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
        for( int i = 0; i < flags.size(); i++ )
        {
            writer.println("\t"+flags.get(i)+",");
        }
        if( multiGeoId )
        {
            writer.println("\tGeosetId Multiple,");
        }
        else if( geosetId != -1 )
        {
            writer.println("\tGeosetId "+geosetId+",");
        }
        if( this.getClass() == Bone.class )//hasGeoAnim ) HELPERS DONT SEEM TO HAVE GEOSET ANIM ID
        {
            if( geosetAnim == null || geosetAnimId == -1 )
            {
                writer.println("\tGeosetAnimId None,");
            }
            else
            {
                writer.println("\tGeosetAnimId "+geosetAnimId+",");
            }
        }

//         if( this.getClass() == Bone.class )
//         {
// //             writer.println("\tGeosetId Multiple,");
//             writer.println("\tGeosetAnimId None,");
//         }
        for( int i = 0; i < animFlags.size(); i++ )
        {
            animFlags.get(i).printTo(writer,1);
        }
        writer.println("}");
    }
    public void copyMotionFrom(Bone b)
    {
        for( AnimFlag baf: b.animFlags )
        {
            boolean foundMatch = false;
            for( AnimFlag af: animFlags )
            {
                boolean sameSeq = false;
                if( baf.globalSeq == null && af.globalSeq == null )
                {
                    sameSeq = true;
                }
                else if( baf.globalSeq != null && af.globalSeq != null )
                {
                     sameSeq = baf.globalSeq.equals(af.globalSeq);
                }
                if( baf.getName().equals(af.getName()) && sameSeq && baf.hasGlobalSeq == af.hasGlobalSeq )
                {
//                     if(  && baf.tags.equals(af.tags)
                    foundMatch = true;
                    af.copyFrom(baf);
                }
            }
            if( !foundMatch )
            {
                animFlags.add(baf);
            }
        }
    }
    public void clearAnimation(Animation a)
    {
        for( AnimFlag af: animFlags )
        {
            af.deleteAnim(a);
        }
    }
    public IdObject copy()
    {
        return new Bone(this);
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
