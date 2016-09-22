package com.matrixeater.src;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.PrintWriter;
/**
 * TextureAnims, inside them called TVertexAnims
 * 
 * Eric Theller
 * 3/9/2012
 */
public class TextureAnim
{
    ArrayList<AnimFlag> animFlags = new ArrayList();//The flags of animation
    /**
     * Constructor for objects of class TextureAnim
     */
    public TextureAnim(AnimFlag flag)
    {
        animFlags.add(flag);
    }
    public TextureAnim(ArrayList<AnimFlag> flags)
    {
        animFlags = flags;
    }
    public TextureAnim(TextureAnim other)
    {
    	for( AnimFlag af: other.animFlags )
    	{
    		animFlags.add(new AnimFlag(af));
    	}
    }
    private TextureAnim()
    {
        
    }
    public static TextureAnim parseText(String [] line)
    {
        if( line[0].contains("TVertexAnim") )
        {
            TextureAnim tan = new TextureAnim();
            for( int i = 1; i < line.length; i++ )
            {
                String [] flagStrings = MDLReader.breakElement(line,i);
                i+=flagStrings.length-1;
                tan.animFlags.add(AnimFlag.parseText(flagStrings));
            }
            return tan;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse TextureAnim: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static TextureAnim read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("TVertexAnim") )
        {
            TextureAnim tan = new TextureAnim();
            MDLReader.mark(mdl);
            while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
            {
                MDLReader.reset(mdl);
                tan.animFlags.add(AnimFlag.read(mdl));
                MDLReader.mark(mdl);
            }
            return tan;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse TextureAnim: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static ArrayList<TextureAnim> readAll(BufferedReader mdl)
    {
        String line = "";
        ArrayList<TextureAnim> outputs = new ArrayList<TextureAnim>();
        MDLReader.mark(mdl);
        if( (line = MDLReader.nextLine(mdl)).contains("TextureAnims") )
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
//             JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse TextureAnims: Missing or unrecognized open statement.");
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
        writer.println(tabs+"TVertexAnim {");
        for( int i = 0; i < animFlags.size(); i++ )
        {
            AnimFlag temp = animFlags.get(i);
            temp.printTo(writer,tabHeight+1);
        }
        writer.println(tabs+"}");
    }
}
