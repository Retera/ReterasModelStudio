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
public class ParticleEmitter2 extends IdObject implements VisibilitySource
{
    static final String [] timeDoubleNames = {"Speed","Variation","Latitude","Gravity","EmissionRate","Width","Length"};
    double [] timeDoubleData = new double[timeDoubleNames.length];
    static final String [] loneDoubleNames = {"LifeSpan","TailLength","Time"};
    double [] loneDoubleData = new double[loneDoubleNames.length];
    static final String [] loneIntNames = {"Rows","Columns","TextureID","ReplaceableId","PriorityPlane"};
    int [] loneIntData = new int[loneIntNames.length];
    static final String [] knownFlagNames = {"DontInherit { Rotation }","DontInherit { Translation }","DontInherit { Scaling }","SortPrimsFarZ","Unshaded","LineEmitter","Unfogged","ModelSpace","XYQuad","Squirt","Additive","Modulate2x","Modulate","AlphaKey","Blend","Tail","Head","Both"};
    boolean [] knownFlags = new boolean[knownFlagNames.length];
    static final String [] vertexDataNames = {"Alpha","ParticleScaling","LifeSpanUVAnim","DecayUVAnim","TailUVAnim","TailDecayUVAnim" };
    Vertex [] vertexData = new Vertex[vertexDataNames.length];
    
    Vertex [] segmentColor = new Vertex[3];
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    
    ArrayList<String> unknownFlags = new ArrayList<String>();
    
    Bitmap texture;
    
    private ParticleEmitter2()
    {
        
    }
    public IdObject copy()
    {
        ParticleEmitter2 x = new ParticleEmitter2();
        
        x.name = name + " copy";
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.timeDoubleData = timeDoubleData.clone();
        x.loneDoubleData = loneDoubleData.clone();
        x.loneIntData = loneIntData.clone();
        x.knownFlags = knownFlags.clone();
        x.vertexData = vertexData.clone();
        x.segmentColor = segmentColor.clone();
        
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        unknownFlags = new ArrayList<String>(x.unknownFlags);
        
        x.texture = texture;
        return x;
    }
    public void updateTextureRef(ArrayList<Bitmap> textures)
    {
        texture = textures.get(getTextureId());
    }
    public int getTextureId()
    {
        return loneIntData[2];
    }
    public void setTextureId(int id)
    {
        loneIntData[2] = id;
    }
    public static ParticleEmitter2 read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("ParticleEmitter2") )
        {
            ParticleEmitter2 pe = new ParticleEmitter2();
            pe.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") )
            {
                boolean foundType = false;
                if( line.contains("ObjectId") )
                {
//                     JOptionPane.showMessageDialog(null,"Read an object id!");
                    pe.objectId = MDLReader.readInt(line);
                    foundType = true;
//                     JOptionPane.showMessageDialog(null,"ObjectId from line: "+line);
                }
                else if( line.contains("Parent") )
                {
                    pe.parentId = MDLReader.splitToInts(line)[0];
                    foundType = true;
//                     JOptionPane.showMessageDialog(null,"Parent from line: "+line);
//                     lit.parent = mdlr.getIdObject(lit.parentId);
                }
                else if( line.contains("SegmentColor") )
                {
                    boolean reading = true;
                    foundType = true;
//                     JOptionPane.showMessageDialog(null,"SegmentColor from line: "+line);
                    for( int i = 0; reading && i < 3; i++ )
                    {
                        line = MDLReader.nextLine(mdl);
                        if( line.contains("Color") )
                        {
                            pe.segmentColor[i] = Vertex.parseText(line);
                        }
                        else
                        {
                            reading = false;
                            MDLReader.reset(mdl);
                            line = MDLReader.nextLine(mdl);
                        }
                    }
                    line = MDLReader.nextLine(mdl);
                }
                for( int i = 0; i < vertexDataNames.length && !foundType; i++ )
                {
                    if( line.contains("\t"+vertexDataNames[i]+" ") )
                    {
                        foundType = true;
                        pe.vertexData[i] = Vertex.parseText(line);
//                     JOptionPane.showMessageDialog(null,vertexDataNames[i]+" from line: "+line);
                    }
                }
                for( int i = 0; i < loneDoubleNames.length && !foundType; i++ )
                {
                    if( line.contains(loneDoubleNames[i]) )
                    {
                        foundType = true;
                        pe.loneDoubleData[i] = MDLReader.readDouble(line);
//                         JOptionPane.showMessageDialog(null,loneDoubleNames[i]+" from line: "+line);
                    }
                }
                for( int i = 0; i < loneIntNames.length && !foundType; i++ )
                {
                    if( line.contains(loneIntNames[i]) )
                    {
                        foundType = true;
                        pe.loneIntData[i] = MDLReader.readInt(line);
//                         JOptionPane.showMessageDialog(null,loneIntNames[i]+" from line: "+line);
                    }
                }
                for( int i = 0; i < knownFlagNames.length && !foundType; i++ )
                {
                    if( line.contains(knownFlagNames[i]) )
                    {
                        foundType = true;
                        pe.knownFlags[i] = true;
//                         JOptionPane.showMessageDialog(null,knownFlagNames[i]+" from line: "+line);
                    }
                }
                for( int i = 0; i < timeDoubleNames.length && !foundType; i++ )
                {
                    if( line.contains(timeDoubleNames[i]) )
                    {
                        foundType = true;
//                         JOptionPane.showMessageDialog(null,timeDoubleNames[i]+" from line: "+line);
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
                if( !foundType && (line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation") || line.contains("Scaling") ) )
                {
                    MDLReader.reset(mdl);
                    pe.animFlags.add(AnimFlag.read(mdl));
                    foundType = true;
//                     JOptionPane.showMessageDialog(null,"AnimFlag from line: "+line);
                }
                if( !foundType )
                {
//                     JOptionPane.showMessageDialog(null,"Particle emitter 2 did not recognize data at: "+line+"\nThis is probably not a major issue?");
                    pe.unknownFlags.add(MDLReader.readFlag(line));
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return pe;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse ParticleEmitter2: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void printTo(PrintWriter writer)
    {
        //Remember to update the ids of things before using this
        // -- uses objectId value of idObject superclass
        // -- uses parentId value of idObject superclass
        // -- uses the parent (java Object reference) of idObject superclass
        // -- uses the TextureID value
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
        for( int i = 0; i < 9; i++ )
        {
            if( knownFlags[i] )
            {
                writer.println("\t"+knownFlagNames[i]+",");
            }
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
        for( int i = 9; i < 10; i++ )
        {
            if( knownFlags[i] )
            {
                writer.println("\t"+knownFlagNames[i]+",");
            }
        }
        for( int i = 0; i < 1; i++ )
        {
            writer.println("\t"+loneDoubleNames[i]+" "+MDLReader.doubleToString(loneDoubleData[i])+",");
        }
        for( int i = 4; i < 7; i++ )
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
        for( int i = 10; i < 15; i++ )
        {
            if( knownFlags[i] )
            {
                writer.println("\t"+knownFlagNames[i]+",");
            }
        }
        for( int i = 0; i < 2; i++ )
        {
            writer.println("\t"+loneIntNames[i]+" "+loneIntData[i]+",");
        }
        for( int i = 15; i < 18; i++ )
        {
            if( knownFlags[i] )
            {
                writer.println("\t"+knownFlagNames[i]+",");
            }
        }
        for( int i = 1; i < 3; i++ )
        {
            writer.println("\t"+loneDoubleNames[i]+" "+MDLReader.doubleToString(loneDoubleData[i])+",");
        }
        writer.println("\tSegmentColor {");
        for( int i = 0; i < segmentColor.length; i++ )
        {
            writer.println("\t\tColor "+segmentColor[i].toString()+",");
        }
        writer.println("\t},");
        for( int i = 0; i < vertexData.length; i++ )
        {
            writer.println("\t"+vertexDataNames[i]+" "+vertexData[i].toStringLessSpace()+",");
        }
        for( int i = 2; i < 3; i++ )
        {
            writer.println("\t"+loneIntNames[i]+" "+loneIntData[i]+",");
        }
        for( int i = 3; i < 5; i++ )
        {
            if( loneIntData[i] != 0 )
            {
                writer.println("\t"+loneIntNames[i]+" "+loneIntData[i]+",");
            }
        }
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
        for( int i = 0; i < unknownFlags.size(); i++ )
        {
            writer.println("\t"+unknownFlags.get(i)+",");
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
