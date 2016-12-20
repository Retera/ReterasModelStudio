package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * RibbonEmitter class, these are the things most people would think of as a particle emitter,
 * I think. Blizzard favored use of these over ParticleEmitters and I do too simply because I
 * so often recycle data and there are more of these to use.
 * 
 * Eric Theller
 * 3/10/2012 3:32 PM
 */
public class RibbonEmitter extends IdObject implements VisibilitySource
{
    static final String [] timeDoubleNames = {"HeightAbove","HeightBelow","Alpha","TextureSlot"};
    double [] timeDoubleData = new double[timeDoubleNames.length];
    static final String [] loneDoubleNames = {"LifeSpan","Gravity"};
    double [] loneDoubleData = new double[loneDoubleNames.length];
    static final String [] loneIntNames = {"EmissionRate","Rows","Columns","MaterialID"};
    Material material;
    int [] loneIntData = new int[loneIntNames.length];
    Vertex staticColor;
    
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    
    private RibbonEmitter()
    {
        
    }
    public IdObject copy()
    {
        RibbonEmitter x = new RibbonEmitter();
        
        x.name = name + " copy";
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.timeDoubleData = timeDoubleData.clone();
        x.loneDoubleData = loneDoubleData.clone();
        x.loneIntData = loneIntData.clone();
        x.material = material;
        x.staticColor = new Vertex(staticColor);
        
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        return x;
    }
    public void updateMaterialRef(ArrayList<Material> mats)
    {
        material = mats.get(getMaterialId());
    }
    public int getMaterialId()
    {
        return loneIntData[3];
    }
    public void setMaterialId(int i)
    {
        loneIntData[3] = i;
    }
    public static RibbonEmitter read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("RibbonEmitter") )
        {
            RibbonEmitter pe = new RibbonEmitter();
            pe.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") )
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
                }
                else if( line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation") || line.contains("Scaling") )
                {
                    MDLReader.reset(mdl);
                    pe.animFlags.add(AnimFlag.read(mdl));
                    foundType = true;
                }
                else if( line.contains("Color") )
                {
                    foundType = true;
                    if( line.contains("static") )
                    {
                        pe.staticColor = Vertex.parseText(line);
                    }
                    else
                    {
                        MDLReader.reset(mdl);
                        pe.animFlags.add(AnimFlag.read(mdl));
                    }
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
                for( int i = 0; i < loneDoubleNames.length && !foundType; i++ )
                {
                    if( line.contains(loneDoubleNames[i]) )
                    {
                        foundType = true;
                        pe.loneDoubleData[i] = MDLReader.readDouble(line);
                    }
                }
                for( int i = 0; i < loneIntNames.length && !foundType; i++ )
                {
                    if( line.contains(loneIntNames[i]) )
                    {
                        foundType = true;
                        pe.loneIntData[i] = MDLReader.readInt(line);
                    }
                }
                if( !foundType )
                {
                    JOptionPane.showMessageDialog(null,"Ribbon emitter did not recognize data at: "+line+"\nThis is probably not a major issue?");
                }
                
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return pe;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse RibbonEmitter: Missing or unrecognized open statement.");
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
        String currentFlag = "";
        for( int i = 0; i < 3; i++ )
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
        currentFlag = "Color";
        boolean set = false;
        if( staticColor == null )
        {
            for( int i = 0; i < pAnimFlags.size() && !set; i++ )
            {
                if( pAnimFlags.get(i).getName().equals(currentFlag) )
                {
                    pAnimFlags.get(i).printTo(writer,1);
                    pAnimFlags.remove(i);
                    set = true;
                }
            }
        }
        else
        {
            writer.println("\tstatic "+currentFlag+" "+staticColor.toString()+",");
        }
        for( int i = 3; i < 4; i++ )
        {
            currentFlag = timeDoubleNames[i];
            if( timeDoubleData[i] != 0 )
            {
                writer.println("\tstatic "+currentFlag+" "+MDLReader.doubleToString(timeDoubleData[i])+",");
            }
            else
            {
                set = false;
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
        for( int i = 0; i < 1; i++ )
        {
            writer.println("\t"+loneIntNames[i]+" "+loneIntData[i]+",");
        }
        for( int i = 0; i < loneDoubleData.length; i++ )
        {
            if( i == 0 || loneDoubleData[i] != 0 )
            writer.println("\t"+loneDoubleNames[i]+" "+loneDoubleData[i]+",");
        }
        for( int i = 1; i < loneIntData.length; i++ )
        {
            writer.println("\t"+loneIntNames[i]+" "+loneIntData[i]+",");
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
