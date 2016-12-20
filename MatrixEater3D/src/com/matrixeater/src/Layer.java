package com.matrixeater.src;
import java.util.ArrayList;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
/**
 * Layers for MDLToolkit/MatrixEater.
 * 
 * Eric Theller
 * 3/8/2012
 */
public class Layer implements Named, VisibilitySource
{
    private String filterMode = "None";//
    int textureId = -1;
    int TVertexAnimId = -1;
    private int CoordId = 0;
    Bitmap texture;
    TextureAnim textureAnim;
    private double staticAlpha = -1;//Amount of static alpha (opacity)
    private ArrayList<String> flags;//My way of dealing with all the stuff that I forget/don't bother with: "Unshaded," "Unfogged," "TwoSided," "CoordId X," actually CoordId was moved into its own field 
    ArrayList<AnimFlag> anims;//Used instead of static alpha for animated alpha
    ArrayList<Bitmap> textures;
    
    public String getFilterMode()
    {
    	return filterMode;
    }
    public boolean equals( Object o )
    {
        if( !( o instanceof Layer ) )
        {
            return false;
        }
        Layer lay = (Layer)o;
        boolean does =staticAlpha == lay.staticAlpha
        && CoordId == lay.CoordId
        && (texture == null ? lay.texture == null : texture.equals(lay.texture) )
        && (textureAnim == null ? lay.textureAnim == null : textureAnim.equals(lay.textureAnim) )
        && (filterMode == null ? lay.filterMode == null : filterMode.equals(lay.filterMode) )
        && (textures == null ? lay.textures == null : textures.equals(lay.textures) )
        && (flags == null ? lay.flags == null : flags.equals(lay.flags) )
        && (anims == null ? lay.anims == null : anims.equals(lay.anims) );
        return does;
    }
    public Layer( String filterMode, int textureId )
    {
        this.filterMode = filterMode;
        this.textureId = textureId;
    }
    public Layer(Layer other)
    {
    	filterMode = other.filterMode;
    	textureId = other.textureId;
    	TVertexAnimId = other.TVertexAnimId;
    	CoordId = other.CoordId;
    	texture = new Bitmap(other.texture);
    	textureAnim = new TextureAnim(other.textureAnim);
    	staticAlpha = other.staticAlpha;
    	flags = new ArrayList<String>(other.flags);
    	anims = new ArrayList<AnimFlag>();
    	textures = new ArrayList<Bitmap>();
    	for( AnimFlag af: other.anims )
    	{
    		anims.add(new AnimFlag(af));
    	}
    	for( Bitmap bmp: other.textures )
    	{
    		textures.add(new Bitmap(bmp));
    	}
    }
    private Layer()
    {
        flags = new ArrayList<String>();
        anims = new ArrayList<AnimFlag>();
    }
    public Bitmap firstTexture()
    {
    	if( texture != null )
    		return texture;
    	else
    		return textures.get(0);
    }
    public void setTextureAnim(TextureAnim texa)
    {
        textureAnim = texa;
    }
    public void setTextureAnim(ArrayList<TextureAnim> list)
    {   //Sets the texture anim reference to the one from the list corresponding to the TVertexAnimId
        textureAnim = list.get(TVertexAnimId);
    }
    public void buildTextureList(MDL mdlr)
    {
        textures = new ArrayList<Bitmap>();
        AnimFlag txFlag = getFlag("TextureID");
        for( int i = 0; i < txFlag.values.size(); i++ )
        {
            textures.add(mdlr.getTexture(((Integer)txFlag.values.get(i)).intValue()));
        }
    }
    public void updateIds(MDL mdlr)
    {
        textureId = mdlr.getTextureId(texture);
        TVertexAnimId = mdlr.getTextureAnimId(textureAnim);
        if( textures != null )
        {
            AnimFlag txFlag = getFlag("TextureID");
            for( int i = 0; i < textures.size(); i++ )
            {
                txFlag.values.set(i,mdlr.getTextureId(textures.get(i)));
            }
        }
    }
    public static Layer parseText(String [] line)
    {
        if( line[0].contains("Layer") )
        {
            Layer lay = new Layer();
            for( int i = 1; i < line.length; i++ )
            {
                if( line[i].contains("FilterMode") )
                {
                    lay.filterMode = MDLReader.readField(line[i]);
                }
                else if( line[i].contains("static TextureId") )//non static? how did I handle them?
                {
                    lay.textureId = MDLReader.readInt(line[i]);
                }
                else if( line[i].contains("CoordId") )
                {
                    lay.CoordId = MDLReader.readInt(line[i]);
                }
                else if( line[i].contains("TVertexAnimId") )
                {
                    lay.TVertexAnimId = MDLReader.readInt(line[i]);
                }
                else if( line[i].contains("static Alpha") )
                {
                    lay.staticAlpha = MDLReader.readDouble(line[i]);
                }
                else if( line[i].contains("Alpha") )
                {
                    String [] alphaStrings = MDLReader.breakElement(line,i);
                    i+=alphaStrings.length-1;
                    lay.anims.add(AnimFlag.parseText(alphaStrings));
                }
                else
                {
                    lay.flags.add(MDLReader.readFlag(line[i]));
//                     JOptionPane.showMessageDialog(MainFrame.panel,"Error parsing Layer: Unrecognized statement '"+line[i]+"'.");
                }
            }
            return lay;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Layer: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static Layer read(BufferedReader mdl, MDL mdlr)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Layer") )
        {
            Layer lay = new Layer();
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
            {
                if( line.contains("FilterMode") )
                {
                    lay.filterMode = MDLReader.readField(line);
                }
                else if( line.contains("static TextureID") )
                {
                    lay.textureId = MDLReader.readInt(line);
                    lay.texture = mdlr.getTexture(lay.textureId);
                }
                else if( line.contains("CoordId") )
                {
                    lay.CoordId = MDLReader.readInt(line);
                }
                else if( line.contains("TVertexAnimId") )
                {
                    lay.TVertexAnimId = MDLReader.readInt(line);
                }
                else if( line.contains("static Alpha") )
                {
                    lay.staticAlpha = MDLReader.readDouble(line);
                }
                else if( line.contains("Alpha") )
                {
                    MDLReader.reset(mdl);
                    lay.anims.add(AnimFlag.read(mdl));
                }
                else if( line.contains("TextureID") )
                {
                    MDLReader.reset(mdl);
                    lay.anims.add(AnimFlag.read(mdl));
                    lay.buildTextureList(mdlr);
                }
                else
                {
                    lay.flags.add(MDLReader.readFlag(line));
//                     JOptionPane.showMessageDialog(MainFrame.panel,"Error parsing Layer: Unrecognized statement '"+line[i]+"'.");
                }
                MDLReader.mark(mdl);
            }
            return lay;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Layer: Missing or unrecognized open statement.");
        }
        return null;
    }
    public boolean hasCoordId()
    {
        return CoordId != 0;
    }
    public int getCoordId()
    {
    	return CoordId;
    }
    public boolean hasTexAnim()
    {
        return TVertexAnimId != -1;
    }
    public void printTo(PrintWriter writer, int tabHeight, boolean useCoords)
    {
        String tabs = "";
        for( int i = 0; i < tabHeight; i++ )
        {
            tabs = tabs + "\t";
        }
        writer.println(tabs+"Layer {");
        writer.println(tabs+"\tFilterMode "+filterMode+",");
        for( int i = 0; i < flags.size(); i++ )
        {
            writer.println(tabs+"\t"+flags.get(i)+",");
        }
        if( textureId != -1 )
        {
            writer.println(tabs+"\tstatic TextureID "+textureId+",");
        }
        for( int i = 0; i < anims.size(); i++ )
        {
            AnimFlag temp = anims.get(i);
            if( temp.getName().equals("TextureID") )
            {
                temp.printTo(writer,tabHeight+1);
            }
        }
        if( hasTexAnim() )
        {
            writer.println(tabs+"\tTVertexAnimId "+TVertexAnimId+",");
        }
        if( useCoords )
        {
            writer.println(tabs+"\tCoordId "+CoordId+",");
        }
        boolean foundAlpha = false;
        for( int i = 0; i < anims.size(); i++ )
        {
            AnimFlag temp = anims.get(i);
            if( temp.getName().equals("Alpha") )
            {
                temp.printTo(writer,tabHeight+1);
                foundAlpha = true;
            }
        }
        if( staticAlpha != -1 && !foundAlpha )
        {
            writer.println(tabs+"\tstatic Alpha "+staticAlpha+",");
        }
        writer.println(tabs+"}");
    }
    public String getName()
    {
        if( texture != null )
        return texture.getName()+" layer (mode "+filterMode+") ";
        return "multi-textured layer (mode "+filterMode+") ";
    }
    public AnimFlag getFlag(String what)
    {
        int count = 0;
        AnimFlag output = null;
        for( AnimFlag af: anims )
        {
            if( af.getName().equals(what) )
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
    public void setVisibilityFlag(AnimFlag flag)
    {
        int count = 0;
        int index = 0;
        for( int i = 0; i< anims.size(); i++ )
        {
            AnimFlag af = anims.get(i);
            if( af.getName().equals("Visibility") || af.getName().equals("Alpha") )
            {
                count++;
                index = i;
                anims.remove(af);
            }
        }
        if( flag != null )
        anims.add(index,flag);
        if( count > 1 )
        {
            JOptionPane.showMessageDialog(null,"Some visiblity animation data was lost unexpectedly during overwrite in "+getName()+".");
        }
    }
    public AnimFlag getVisibilityFlag()
    {
        int count = 0;
        AnimFlag output = null;
        for( AnimFlag af: anims )
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
        return "Alpha";
    }
}
