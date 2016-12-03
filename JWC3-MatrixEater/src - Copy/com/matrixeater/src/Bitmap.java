package com.matrixeater.src;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.PrintWriter;
/**
 * A class to represent MDL texture references. (Not materials)
 * 
 * Eric Theller
 * 11/5/2011
 */
public class Bitmap
{
    private String imagePath = "";
    private int replaceableId = -1;
    private int wrapStyle;//0 = nothing, 1 = WrapWidth, 2 = WrapHeight, 3 = both
    public String getPath()
    {
        return imagePath;
    }
    public String getName()
    {
        if( !imagePath.equals("") )
        {
            try {
                String [] bits = imagePath.split("\\\\");
                return bits[bits.length-1].split("\\.")[0];
            }
            catch (Exception e)
            {
                return "bad blp path";
            }
        }
        else
        {
            if( replaceableId == 1 )
            {
                return "Team Color";
            }
            else if( replaceableId == 2 )
            {
                return "Team Glow";
            }
            else
            {
                return "Replaceable"+replaceableId;
            }
        }
    }
    public int getReplaceableId()
    {
    	return replaceableId;
    }
    public Bitmap( String imagePath, int replaceableId )
    {
        this.imagePath = imagePath;
        this.replaceableId = replaceableId;
    }
    public Bitmap(Bitmap other)
    {
    	imagePath = other.imagePath;
    	replaceableId = other.replaceableId;
    	wrapStyle = other.wrapStyle;
    }
    private Bitmap()
    {
        
    }
    public boolean equals(Object o)
    {
        if( !(o instanceof Bitmap ) )
        {
            return false;
        }
        Bitmap b = (Bitmap)o;
        boolean does = imagePath.equals(b.imagePath)
        && replaceableId == b.replaceableId
        && wrapStyle == b.wrapStyle;
        return does;
    }
    public void setWrapHeight(boolean flag)
    {
        if( flag )
        {
            if( wrapStyle == 1 )
            {
                wrapStyle = 3;
            }
            else if( wrapStyle == 0 )
            {
                wrapStyle = 2;
            }
        }
        else
        {
            if( wrapStyle == 3 )
            {
                wrapStyle = 1;
            }
            else if( wrapStyle == 2 )
            {
                wrapStyle = 0;
            }
        }
    }
    public void setWrapWidth(boolean flag)
    {
        if( flag )
        {
            if( wrapStyle == 2 )
            {
                wrapStyle = 3;
            }
            else if( wrapStyle == 0 )
            {
                wrapStyle = 1;
            }
        }
        else
        {
            if( wrapStyle == 3 )
            {
                wrapStyle = 2;
            }
            else if( wrapStyle == 1 )
            {
                wrapStyle = 0;
            }
        }
    }
    public static Bitmap parseText(String [] line)
    {
        if( line[0].contains("Bitmap") )
        {
            Bitmap tex = new Bitmap();
            for( int i = 1; i < line.length; i++ )
            {
                if( line[i].contains("Image") )
                {
                    tex.imagePath = line[i].split("\"")[1];
                }
                else if( line[i].contains("ReplaceableId ") )
                {
                    try
                    {
                        tex.replaceableId = Integer.parseInt(line[i].substring(line[i].indexOf("ReplaceableId ")+"ReplaceableId ".length(),line[i].length()-2));
                    }
                    catch (NumberFormatException e)
                    {
                        JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing bitmap: Could not interpret ReplaceableId.");
                    }
                }
                else if( line[i].contains("WrapWidth") )
                {
                    tex.setWrapWidth(true);
                }
                else if( line[i].contains("WrapHeight") )
                {
                    tex.setWrapHeight(true);
                }
                else
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error parsing Bitmap: Unrecognized statement '"+line[i]+"'.");
                }
            }
            return tex;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Bitmap: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static Bitmap read(BufferedReader mdl)
    {
        String line = "";
        if( (line = MDLReader.nextLine(mdl)).contains("Bitmap") )
        {
            Bitmap tex = new Bitmap();
            while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
            {
                if( line.contains("Image") )
                {
                    tex.imagePath = line.split("\"")[1];
                }
                else if( line.contains("ReplaceableId ") )
                {
                    tex.replaceableId = MDLReader.readInt(line);
//                     try
//                     {
//                         tex.replaceableId = Integer.parseInt(line[i].substring(line[i].indexOf("ReplaceableId ")+"ReplaceableId ".length(),line[i].length()-2));
//                     }
//                     catch (NumberFormatException e)
//                     {
//                         JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing bitmap: Could not interpret ReplaceableId.");
//                     }
                }
                else if( line.contains("WrapWidth") )
                {
                    tex.setWrapWidth(true);
                }
                else if( line.contains("WrapHeight") )
                {
                    tex.setWrapHeight(true);
                }
                else
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error parsing Bitmap: Unrecognized statement '"+line+"'.");
                }
            }
            return tex;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Bitmap: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static ArrayList<Bitmap> readAll(BufferedReader mdl)
    {
        String line = "";
        ArrayList<Bitmap> outputs = new ArrayList<Bitmap>();
        MDLReader.mark(mdl);
        if( (line = MDLReader.nextLine(mdl)).contains("Textures") )
        {
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).startsWith("}") )
            {
                MDLReader.reset(mdl);
                outputs.add(read(mdl));
                MDLReader.mark(mdl);
            }
            return outputs;
        }
        else
        {
            MDLReader.reset(mdl);
//            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Textures: Missing or unrecognized open statement.");
        }
        return outputs;
    }
    public void printTo(PrintWriter writer, int tabHeight)
    {
        String tabs = "";
        for( int i = 0; i < tabHeight; i++ )
        {
            tabs = tabs + "\t";
        }
        writer.println(tabs+"Bitmap {");
        writer.println(tabs+"\tImage \""+imagePath+"\",");
        if( replaceableId != -1 )
        {
            writer.println(tabs+"\tReplaceableId "+replaceableId+",");
        }
        switch (wrapStyle)
        {
            case 0:
                break;
            case 1:
                writer.println(tabs+"\tWrapWidth,");
                break;
            case 2:
                writer.println(tabs+"\tWrapHeight,");
                break;
            case 3:
                writer.println(tabs+"\tWrapWidth,");
                writer.println(tabs+"\tWrapHeight,");
                break;
        }
        writer.println(tabs+"}");
    }
}
