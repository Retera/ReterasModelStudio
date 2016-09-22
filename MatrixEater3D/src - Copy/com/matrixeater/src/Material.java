package com.matrixeater.src;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;
/**
 * A class for MDL materials.
 * 
 * Eric Theller
 * 11/5/2011
 */
public class Material
{
	public static int teamColor = 0;
    ArrayList<Layer> layers;
    private int priorityPlane = 0;
    private ArrayList<String> flags;//My way of dealing with all the stuff that I forget/don't bother with: "Unshaded," "Unfogged," "TwoSided," "CoordId X," actually CoordId was moved into its own field 
    public String getName()
    {
        String name = "";
        if( layers.size() > 0 )
        {
            if( layers.get(layers.size()-1).texture != null )
            {
                name = layers.get(layers.size()-1).texture.getName();
                if( layers.get(layers.size()-1).getFlag("Alpha") != null)
                	name = name + " (animated Alpha)";
            }
            else
            {
                name = "animated texture layers";
            }
            for( int i = layers.size() - 2; i >= 0; i-- )
            {
                try {
                    name = name + " over " + layers.get(i).texture.getName();
                    if( layers.get(i).getFlag("Alpha") != null)
                    	name = name + " (animated Alpha)";
                }
                catch (NullPointerException e)
                {
                    name = name + " over " + "animated texture layers (" + layers.get(i).textures.get(0).getName()+")";
                }
            }
        }
        return name;
    }
    public Layer firstLayer()
    {
        if( layers.size() > 0 )
        {
            return layers.get(layers.size()-1);
        }
        return null;
    }
    public Material( Layer lay )
    {
       layers = new ArrayList<Layer>();
       flags = new ArrayList<String>();
       layers.add(lay);
    }
    private Material()
    {
        layers = new ArrayList<Layer>();
        flags = new ArrayList<String>();
    }
    public Material(Material other)
    {
        layers = new ArrayList<Layer>();
        flags = new ArrayList<String>(other.flags);
        for( Layer lay: other.layers )
        {
        	layers.add(new Layer(lay));
        }
        priorityPlane = other.priorityPlane;
    }
    public static Material parseText(String [] line)
    {
        if( line[0].contains("Material") )
        {
            Material mat = new Material();
            for( int i = 1; i < line.length; i++ )
            {
                if( line[i].contains("Layer") )
                {
                    String [] layerStrings = MDLReader.breakElement(line,i);
                    i+=layerStrings.length-1;
                    mat.layers.add(Layer.parseText(layerStrings));
                }
                else if( line[i].contains("PriorityPlane") )
                {
                    mat.priorityPlane = MDLReader.readInt(line[i]);
                }
                else
                {
                    mat.flags.add(MDLReader.readFlag(line[i]));
//                     JOptionPane.showMessageDialog(MainFrame.panel,"Error parsing Material: Unrecognized statement '"+line[i]+"'.");
                }
            }
            return mat;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Material: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void updateTextureAnims(ArrayList<TextureAnim> list)
    {
        int sz = layers.size();
        for( int i = 0; i < sz; i++ )
        {
            Layer lay = layers.get(i);
            if( lay.hasTexAnim() )
            {
                lay.setTextureAnim(list);
            }
        }
    }
    public void updateReferenceIds(MDL mdlr)
    {
        for( Layer lay: layers )
        {
            lay.updateIds(mdlr);
        }
    }
    public boolean equals(Object o)
    {
        if( !( o instanceof Material ) )
        {
            return false;
        }
        Material m = (Material)o;
        boolean does = priorityPlane == m.priorityPlane
         && flags.size() == m.flags.size()
         && layers.size() == m.layers.size();
        for( int i = 0; i < flags.size() && does; i++ )
        {
            if( !flags.get(i).equals(m.flags.get(i)) )
            {
                does = false;
            }
        }
        for( int i = 0; i < layers.size() && does; i++ )
        {
            if( !layers.get(i).equals(m.layers.get(i)) )
            {
                does = false;
            }
        }
        return does;
    }
    public static Material read(BufferedReader mdl, MDL mdlr)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("Material") )
        {
            Material mat = new Material();
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
            {
                if( line.contains("Layer") )
                {
                    MDLReader.reset(mdl);
                    mat.layers.add(Layer.read(mdl,mdlr));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("PriorityPlane") )
                {
                    mat.priorityPlane = MDLReader.readInt(line);
                }
                else
                {
                    mat.flags.add(MDLReader.readFlag(line));
//                     JOptionPane.showMessageDialog(MainFrame.panel,"Error parsing Material: Unrecognized statement '"+line[i]+"'.");
                }
                MDLReader.mark(mdl);
            }
            return mat;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Material: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static ArrayList<Material> readAll(BufferedReader mdl, MDL mdlr)
    {
        String line = "";
        ArrayList<Material> outputs = new ArrayList<Material>();
        MDLReader.mark(mdl);
        if( (line = MDLReader.nextLine(mdl)).contains("Materials") )
        {
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).startsWith("}") )
            {
                MDLReader.reset(mdl);
                outputs.add(read(mdl,mdlr));
                MDLReader.mark(mdl);
            }
            return outputs;
        }
        else
        {
            MDLReader.reset(mdl);
//             JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Materials: Missing or unrecognized open statement.");
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
        writer.println(tabs+"Material {");
        if( priorityPlane != 0 )
        {
            writer.println(tabs+"\tPriorityPlane "+priorityPlane+",");
        }
        for( int i = 0; i < flags.size(); i++ )
        {
            writer.println(tabs+"\t"+flags.get(i)+",");
        }
        boolean useCoords = false;
        for( int i = 0; i < layers.size(); i++ )
        {
            useCoords = layers.get(i).hasCoordId();
            if( useCoords )
            {
                break;
            }
        }
        for( int i = 0; i < layers.size(); i++ )
        {
            layers.get(i).printTo(writer,tabHeight+1,useCoords);
        }
        writer.println(tabs+"}");
    }
    public static BufferedImage mergeImage(BufferedImage source, BufferedImage overlay)
    {
    	int w = Math.max(source.getWidth(), overlay.getWidth());
    	int h = Math.max(source.getHeight(), overlay.getHeight());
    	BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    	
    	Graphics g = combined.getGraphics();
    	g.drawImage(source,0,0,w,h,null);
    	g.drawImage(overlay,0,0,w,h,null);
    	
    	return combined;
    }
    public static BufferedImage mergeImageScaled(Image source, Image overlay,int w1, int h1, int w2, int h2)
    {
    	int w = Math.max(w1, w2);
    	int h = Math.max(h1, h2);
    	BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    	
    	Graphics g = combined.getGraphics();
    	g.drawImage(source,0,0,w1,h1,null);
    	g.drawImage(overlay,(w1-w2)/2,(h1-h2)/2,w2,h2,null);
    	
    	return combined;
    }
    public BufferedImage getBufferedImage()
    {
    	BufferedImage theImage = null;
		for(int i = 0; i < layers.size(); i++ )
		{
			Layer lay = layers.get(i);
			Bitmap tex = lay.firstTexture();
			String path = tex.getPath();
			if( path.length() == 0 )
			{
				System.err.println("sup homes");
				if( tex.getReplaceableId() == 1 )
				{
					path = "ReplaceableTextures\\TeamColor\\TeamColor0"+teamColor+".blp";
				}
				else if( tex.getReplaceableId() == 2 )
				{
					path = "ReplaceableTextures\\TeamGlow\\TeamGlow0"+teamColor+".blp";
				}
			}
			try {
				BufferedImage newImage = BLPHandler.get().getGameTex(path);
				if( theImage == null )
					theImage = newImage;
				else
					theImage = mergeImage(theImage, newImage);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				try {
					BufferedImage newImage = BLPHandler.get().getCustomTex(MainFrame.panel.currentMDL().getFile().getParent()+"\\"+path);
					if( theImage == null )
						theImage = newImage;
					else
						theImage = mergeImage(theImage, newImage);
				}
				catch (Exception exc2)
				{
					exc2.printStackTrace();
					JOptionPane.showMessageDialog(null, "BLP texture-loader failed.");
				}
            }
		}
		return theImage;
    }
}
