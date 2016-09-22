package com.matrixeater.src;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.PrintWriter;
/**
 * The geoset anims, heaven forbit they be forgotten.
 * 
 * Eric Theller
 * 11/10/2011
 */
public class GeosetAnim implements VisibilitySource, Named
{
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    double staticAlpha = -1;
    Vertex staticColor = null;
    int geosetId = -1;
    Geoset geoset;
    boolean dropShadow = false;
    public GeosetAnim(AnimFlag flag)
    {
        animFlags.add(flag);
    }
    public GeosetAnim(ArrayList<AnimFlag> flags)
    {
        animFlags = flags;
    }
    public GeosetAnim(Geoset g)
    {
        geoset = g;
    }
    private GeosetAnim()
    {
        
    }
    public static GeosetAnim read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("GeosetAnim") )
        {
            GeosetAnim geo = new GeosetAnim();
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).contains("}") || line.contains("},") )
            {
                if( line.contains("static Alpha") )
                {
                    geo.staticAlpha = MDLReader.readDouble(line);
                }
                else if( line.contains("static Color") )
                {
                    geo.staticColor = Vertex.parseText(line);
                }
                else if( line.contains("GeosetId") )
                {
                    geo.geosetId = MDLReader.readInt(line);
                }
                else if( line.contains("DropShadow") )
                {
                    geo.dropShadow = true;
                }
                else
                {
                    MDLReader.reset(mdl);
                    geo.animFlags.add(AnimFlag.read(mdl));
                }
                MDLReader.mark(mdl);
            }
//             MDLReader.reset(mdl);
            System.out.println("GeosetAnim reading completed!");
            return geo;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse GeosetAnim: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void printTo(PrintWriter writer, int tabHeight)
    {
        
        String tabs = "";
        for( int i = 0; i < tabHeight; i++ )
        {
            tabs = tabs + "\t";
        }
        String inTabs = tabs;
        inTabs = inTabs + "\t";
        writer.println(tabs + "GeosetAnim {");
        if( dropShadow )
        {
            writer.println(inTabs + "DropShadow,");
        }
        for( int i = 0; i < animFlags.size(); i++ )
        {
            animFlags.get(i).printTo(writer,1);
        }
        if( staticAlpha != -1 )
        {
            writer.println(inTabs + "static Alpha "+staticAlpha+",");
        }
        if( staticColor != null )
        {
            writer.println(inTabs + "static Color "+staticColor+",");
        }
        if( geosetId != -1 )
        {
            writer.println("\tGeosetId "+geosetId+",");
        }
        writer.println(tabs+"}");
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
            JOptionPane.showMessageDialog(null,"Some visiblity animation data was lost unexpectedly during overwrite in "+getVisTagname()+".");
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
            JOptionPane.showMessageDialog(null,"Some visiblity animation data was lost unexpectedly during retrieval in "+getVisTagname()+".");
        }
        return output;
    }
    public String getVisTagname()
    {
        return geoset.getName();
    }
    public String getName()
    {
        return geoset.getName()+"'s Anim";
    }
    public GeosetAnim getMostVisible(GeosetAnim partner)
    {
        if( getVisibilityFlag() != null && partner != null )
        {
            AnimFlag thisFlag = getVisibilityFlag();
            AnimFlag thatFlag = partner.getVisibilityFlag();
            if( thatFlag != null )
            {
                AnimFlag result = thisFlag.getMostVisible(thatFlag);
                if( result == thisFlag )
                {
                    return this;
                }
                else if( result == thatFlag )
                {
                    return partner;
                }
            }
        }
        return null;
    }
    public String visFlagName()
    {
        return "Alpha";
    }
}
