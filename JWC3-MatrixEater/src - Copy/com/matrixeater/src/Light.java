package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * Write a description of class Light here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Light extends IdObject implements VisibilitySource
{
    int AttenuationStart = -1;
    int AttenuationEnd = -1;
    int Intensity = -1;
    Vertex staticColor;
    int AmbIntensity = -1;
    Vertex staticAmbColor;
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    ArrayList<String> flags = new ArrayList<String>();
    private Light()
    {
        
    }
    public IdObject copy()
    {
        Light x = new Light();
        
        x.name = name;
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.AttenuationStart = AttenuationStart;
        x.AttenuationEnd = AttenuationEnd;
        x.Intensity = Intensity;
        x.staticColor = staticColor;
        x.AmbIntensity = AmbIntensity;
        x.staticAmbColor = staticAmbColor;
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        flags = new ArrayList<String>(x.flags);
        return x;
    }
    public static Light read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Light") )
        {
            Light lit = new Light();
            lit.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") )
            {
                if( line.contains("ObjectId") )
                {
                    lit.objectId = MDLReader.readInt(line);
                }
                else if( line.contains("Parent") )
                {
                    lit.parentId = MDLReader.splitToInts(line)[0];
//                     lit.parent = mdlr.getIdObject(lit.parentId);
                }
                else if( !line.contains("static") && line.contains("{") )
                {
                    MDLReader.reset(mdl);
                    lit.animFlags.add(AnimFlag.read(mdl));
                }
                else if( line.contains("AttenuationStart") )//These are 'static' ones, the rest are
                {                                           // saved in animFlags
                    lit.AttenuationStart = MDLReader.readInt(line);
                }
                else if( line.contains("AttenuationEnd") )
                {
                    lit.AttenuationEnd = MDLReader.readInt(line);
                }
                else if( line.contains("AmbIntensity") )
                {
                    lit.AmbIntensity = MDLReader.readInt(line);
                }
                else if( line.contains("AmbColor") )
                {
                    lit.staticAmbColor = Vertex.parseText(line);
                }
                else if( line.contains("Intensity") )
                {
                    lit.Intensity = MDLReader.readInt(line);
                }
                else if( line.contains("Color") )
                {
                    lit.staticColor = Vertex.parseText(line);
                }
                else//Flags like Omnidirectional
                {
                    lit.flags.add(MDLReader.readFlag(line));
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return lit;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Light: Missing or unrecognized open statement.");
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
//         for( int i = 0; i < flags.size(); i++ )
//         {
//             writer.println("\t"+flags.get(i)+",");
//             //Stuff like omnidirectional
//         }
        for( String s: flags )
        {
            writer.println("\t"+s+",");
            //Stuff like omnidirectional
        }

        //AttenuationStart
        String currentFlag = "AttenuationStart";
        if( AttenuationStart != -1 )
        {
            writer.println("\tstatic "+currentFlag+" "+AttenuationStart+",");
        }
        else
        {
            boolean set = false;
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
        currentFlag = "AttenuationEnd";
        if( AttenuationEnd != -1 )
        {
            writer.println("\tstatic "+currentFlag+" "+AttenuationEnd+",");
        }
        else
        {
            boolean set = false;
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
        currentFlag = "Intensity";
        if( Intensity != -1 )
        {
            writer.println("\tstatic "+currentFlag+" "+Intensity+",");
        }
        else
        {
            boolean set = false;
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
        currentFlag = "Color";
        if( staticColor != null )
        {
            writer.println("\tstatic "+currentFlag+" "+staticColor.toString()+",");
        }
        else
        {
            boolean set = false;
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
        currentFlag = "AmbIntensity";
        if( AmbIntensity != -1 )
        {
            writer.println("\tstatic "+currentFlag+" "+AmbIntensity+",");
        }
        else
        {
            boolean set = false;
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
        currentFlag = "AmbColor";
        if( staticAmbColor != null )
        {
            writer.println("\tstatic "+currentFlag+" "+staticAmbColor.toString()+",");
        }
        else
        {
            boolean set = false;
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
        for( int i = 0; i < pAnimFlags.size(); i++ )
        {
            pAnimFlags.get(i).printTo(writer,1);
            //This will probably just be visibility
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
    public String getVisTagname()
    {
        return "light";//geoset.getName();
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
