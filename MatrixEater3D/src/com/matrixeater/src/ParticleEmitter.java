package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * ParticleEmitter2 class, these are the things most people would think of as a particle emitter,
 * I think. Blizzard favored use of these over ParticleEmitters and I do too simply because I
 * so often recycle data and there are more of these to use.
 * 
 * Eric Theller
 * 3/10/2012 3:32 PM
 */
public class ParticleEmitter extends IdObject implements VisibilitySource
{
    static final String [] timeDoubleNames = {"EmissionRate","Gravity","Longitude","Latitude","LifeSpan","InitVelocity"};
    double [] timeDoubleData = new double[timeDoubleNames.length];
    
    boolean MDLEmitter = true;
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    
    String path = null;
    private ParticleEmitter()
    {
        
    }
    public IdObject copy()
    {
        ParticleEmitter x = new ParticleEmitter();
        
        x.name = name + " copy";
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.timeDoubleData = timeDoubleData.clone();
        x.MDLEmitter = MDLEmitter;
        x.path = path;
        
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        return x;
    }
    public static ParticleEmitter read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("ParticleEmitter") )
        {
            ParticleEmitter pe = new ParticleEmitter();
            pe.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") )  && !line.equals("COMPLETED PARSING") ) 
            {
                boolean foundType = false;
                if( line.contains("ObjectId") )
                {
                    pe.objectId = MDLReader.readInt(line);
                    foundType = true;
                }
                else if( line.contains("Parent") )
                {
                    pe.parentId = MDLReader.splitToInts(line)[0];
                    foundType = true;
//                     pe.parent = mdlr.getIdObject(pe.parentId);
                }
                else if( line.contains("EmitterUses") )
                {
                    pe.MDLEmitter = line.contains("MDL");
                    foundType = true;
                }
                else if( line.contains("Path") )
                {
                    pe.path = MDLReader.readName(line);
                    foundType = true;
                }
                else if( line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation") || line.contains("Scaling") )
                {
                    MDLReader.reset(mdl);
                    pe.animFlags.add(AnimFlag.read(mdl));
                    foundType = true;
                }
                for( int i = 0; i < timeDoubleNames.length && !foundType; i++ )
                {
                    if( line.contains(timeDoubleNames[i]) )
                    {
                        foundType = true;
                        if( line.contains("static") )
                        {
                            pe.timeDoubleData[i] = MDLReader.readDouble(line);
                        }
                        else
                        {
                            MDLReader.reset(mdl);
                            pe.animFlags.add(AnimFlag.read(mdl));
                        }
                    }
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return pe;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse ParticleEmitter: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void printTo(PrintWriter writer)
    {
        //Remember to update the ids of things before using this
        // -- uses objectId value of idObject superclass
        // -- uses parentId value of idObject superclass
        // -- uses the parent (java Object reference) of idObject superclass
        ArrayList<AnimFlag> pAnimFlags = new ArrayList<AnimFlag>(this.animFlags);
        writer.println(MDLReader.getClassName(this.getClass())+" \""+getName()+"\" {");
        if( objectId != -1 )
        {
            writer.println("\tObjectId "+objectId+",");
        }
        if( parentId != -1 )
        {
            writer.println("\tParent "+parentId+",\t// \""+parent.getName()+"\"");
        }
        if( MDLEmitter )
        {
            writer.println("\tEmitterUsesMDL,");
        }
        else
        {
            writer.println("\tEmitterUsesTGA,");
        }
        String currentFlag = "";
        for( int i = 0; i < 4; i++ )
        {
            currentFlag = timeDoubleNames[i];
            if( timeDoubleData[i] != 0 )
            {
                writer.println("\tstatic "+currentFlag+" "+MDLReader.doubleToString(timeDoubleData[i])+",");
            }
            else
            {
                boolean set = false;
                for( int a = 0; a < pAnimFlags.size() && !set; a++ )
                {
                    if( pAnimFlags.get(a).getName().equals(currentFlag) )
                    {
                        pAnimFlags.get(a).printTo(writer,1);
                        pAnimFlags.remove(a);
                        set = true;
                    }
                }
                if( !set )
                {
                    writer.println("\tstatic "+currentFlag+" "+MDLReader.doubleToString(timeDoubleData[i])+",");
                }
            }
        }
        currentFlag = "Visibility";
        for( int i = 0; i < pAnimFlags.size(); i++ )
        {
            if( pAnimFlags.get(i).getName().equals(currentFlag) )
            {
                pAnimFlags.get(i).printTo(writer,1);
                pAnimFlags.remove(i);
            }
        }
        writer.println("\tParticle {");
        for( int i = 4; i < 6; i++ )
        {
            currentFlag = timeDoubleNames[i];
            if( timeDoubleData[i] != 0 )
            {
                writer.println("\t\tstatic "+currentFlag+" "+MDLReader.doubleToString(timeDoubleData[i])+",");
            }
            else
            {
                boolean set = false;
                for( int a = 0; a < pAnimFlags.size() && !set; a++ )
                {
                    if( pAnimFlags.get(a).getName().equals(currentFlag) )
                    {
                        pAnimFlags.get(a).printTo(writer,2);
                        pAnimFlags.remove(a);
                        set = true;
                    }
                }
                if( !set )
                {
                    writer.println("\t\tstatic "+currentFlag+" "+MDLReader.doubleToString(timeDoubleData[i])+",");
                }
            }
        }
        if( path != null )
        {
            writer.println("\t\tPath \""+path+"\",");
        }
        writer.println("\t}");
        
        for( int i = pAnimFlags.size()-1; i >= 0 ; i-- )
        {
            if( pAnimFlags.get(i).getName().equals("Translation") )
            {
                pAnimFlags.get(i).printTo(writer,1);
                pAnimFlags.remove(i);
            }
        }
        for( int i = pAnimFlags.size()-1; i >= 0 ; i-- )
        {
            if( pAnimFlags.get(i).getName().equals("Rotation") )
            {
                pAnimFlags.get(i).printTo(writer,1);
                pAnimFlags.remove(i);
            }
        }
        for( int i = pAnimFlags.size()-1; i >= 0 ; i-- )
        {
            if( pAnimFlags.get(i).getName().equals("Scaling") )
            {
                pAnimFlags.get(i).printTo(writer,1);
                pAnimFlags.remove(i);
            }
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
