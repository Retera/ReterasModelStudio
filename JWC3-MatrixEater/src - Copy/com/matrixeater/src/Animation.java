package com.matrixeater.src;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * A java object to represent MDL "Sequences" ("Animations").
 * 
 * Eric Theller
 * 11/5/2011
 */
public class Animation
{
    private String m_name = "";
    private int m_intervalStart = 0;
    private int m_intervalEnd = -1;
    private ArrayList tags = new ArrayList();//These are strings tags, i.e. "MoveSpeed X," "Rarity X," "NonLooping," etc.
    private ExtLog m_extents;
    private Animation()
    {
        
    }
    public Animation(String name, int intervalStart, int intervalEnd )
    {
        m_name = name;
        m_intervalStart = intervalStart;
        m_intervalEnd = intervalEnd;
        m_extents = new ExtLog(ExtLog.DEFAULT_MINEXT, ExtLog.DEFAULT_MAXEXT, ExtLog.DEFAULT_BOUNDSRADIUS );
    }
    public Animation(String name, int intervalStart, int intervalEnd, Vertex minimumExt, Vertex maximumExt, double boundsRad )
    {
        m_name = name;
        m_intervalStart = intervalStart;
        m_intervalEnd = intervalEnd;
        m_extents = new ExtLog(minimumExt, maximumExt, boundsRad);
    }
    public Animation(Animation other)
    {
    	m_name = other.m_name;
    	m_intervalStart = other.m_intervalStart;
    	m_intervalEnd = other.m_intervalEnd;
    	tags = new ArrayList(other.tags);
    	m_extents = new ExtLog(other.m_extents);
    }
    public void setName(String text)
    {
        m_name = text;
    }
    public String getName()
    {
        return m_name;
    }
    public int length()
    {
        return m_intervalEnd - m_intervalStart;
    }
    public void setInterval(int start, int end)
    {
        m_intervalStart = start;
        m_intervalEnd = end;
    }
    public void copyToInterval(int start, int end, List<AnimFlag> flags, List<EventObject> eventObjs, List<AnimFlag> newFlags, List<EventObject> newEventObjs)
    {
        for( AnimFlag af: newFlags )
        {
            if( !af.hasGlobalSeq ) // wouldn't want to mess THAT up... global sequences are using a different timeline!
            af.copyFrom(flags.get(newFlags.indexOf(af)), m_intervalStart, m_intervalEnd, start, end );
        }
        for( EventObject e: newEventObjs )
        {
            e.copyFrom(eventObjs.get(newEventObjs.indexOf(e)), m_intervalStart, m_intervalEnd, start, end );
        }
    }
    public void setInterval(int start, int end, List<AnimFlag> flags, List<EventObject> eventObjs)
    {
        for( AnimFlag af: flags )
        {
            if( !af.hasGlobalSeq ) // wouldn't want to mess THAT up...
            af.timeScale(m_intervalStart, m_intervalEnd, start, end );
        }
        for( EventObject e: eventObjs )
        {
            e.timeScale(m_intervalStart, m_intervalEnd, start, end );
        }
        m_intervalStart = start;
        m_intervalEnd = end;
    }
    public void reverse(List<AnimFlag> flags, List<EventObject> eventObjs)
    {
        for( AnimFlag af: flags )
        {
            if( !af.hasGlobalSeq && (af.getTypeId() == 1 || af.getTypeId() == 2 || af.getTypeId() == 3 ) ) // wouldn't want to mess THAT up...
            af.timeScale(m_intervalStart, m_intervalEnd, m_intervalEnd, m_intervalStart);
        }
        for( EventObject e: eventObjs )
        {
            e.timeScale(m_intervalStart, m_intervalEnd, m_intervalEnd, m_intervalStart);
        }
//         for( AnimFlag af: flags )
//         {
//             if( !af.hasGlobalSeq && (af.getTypeId() == 1 || af.getTypeId() == 2 || af.getTypeId() == 3 ) ) // wouldn't want to mess THAT up...
//             af.timeScale(m_intervalStart, m_intervalEnd, m_intervalStart+30, m_intervalStart+2);
//         }
//         for( EventObject e: eventObjs )
//         {
//             e.timeScale(m_intervalStart, m_intervalEnd, m_intervalStart+30, m_intervalStart+2);
//         }
    }
    public void clearData(List<AnimFlag> flags, List<EventObject> eventObjs)
    {
        for( AnimFlag af: flags )
        {
            if( (af.getTypeId() == 1 || af.getTypeId() == 2 || af.getTypeId() == 3 ) ) // wouldn't want to mess THAT up...
            	//!af.hasGlobalSeq &&  was above before
            	af.deleteAnim(this);//timeScale(m_intervalStart, m_intervalEnd, m_intervalEnd, m_intervalStart);
        }
        for( EventObject e: eventObjs )
        {
        	e.deleteAnim(this);
        }
    }
    public void setInterval(int start, int end, MDL mdlr)
    {
        List<AnimFlag> aniFlags = mdlr.getAllAnimFlags();
        ArrayList eventObjs = mdlr.sortedIdObjects(EventObject.class);
        setInterval(start,end,aniFlags,(ArrayList<EventObject>)eventObjs);
    }
    public int getStart()
    {
        return m_intervalStart;
    }
    public int getEnd()
    {
        return m_intervalEnd;
    }
    public static Animation parseText(String [] line)
    {
        if( line[0].contains("Anim") )
        {
            Animation anim = new Animation();
            anim.setName(line[0].split("\"")[1]);
            try
            {
                String [] entries = line[1].split("Interval ")[1].split(",");//Split the line into pieces, forming two entries that are before and after the comma in "{ <number>, <number> }"
                entries[0] = entries[0].substring(2,entries[0].length());//Shave off first two chars "{ "
                entries[1] = entries[1].substring(0,entries[1].length()-2);//Shave off last two " }"
                anim.setInterval(Integer.parseInt(entries[0]),Integer.parseInt(entries[1]));
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse animation: Interval could not be interpreted numerically.");
            }
            ArrayList extLog = new ArrayList();
            for( int i = 2; i < line.length; i++ )
            {
                if( line[i].contains("Extent") || line[i].contains("BoundsRadius") )
                {
                    extLog.add(line[i]);
                }
                else
                {
                    anim.tags.add(line[i]);
                }
            }
            anim.m_extents = ExtLog.parseText((String[])extLog.toArray());
            return anim;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse animation: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static Animation read(BufferedReader mdl)
    {
        Animation anim = new Animation();
        System.out.println("Beginning anim");
        boolean limited = false;
        String line = MDLReader.nextLine(mdl);
        try
        {
            anim.setName(line.split("\"")[1]);
            if( anim.m_name.equals("") )
            {
                anim.m_name = " ";
            }
        }
        catch (Exception e)
        {
//             System.out.println("Throwing nameless anim from: "+MDLReader.nextLine(mdl));//Read the word "Anim" from the top
            limited = true;
        }
        if( !limited )
        {
//             JOptionPane.showMessageDialog(MainFrame.panel,"This popup means Anims were interpreted as outside geoset!");
//             try
//             {
//                 String [] entries = MDLReader.nextLine(mdl).split("Interval ")[1].split(",");//Split the line into pieces, forming two entries that are before and after the comma in "{ <number>, <number> }"
//                 entries[0] = entries[0].substring(2,entries[0].length());//Shave off first two chars "{ "
//                 entries[1] = entries[1].substring(0,entries[1].length()-2);//Shave off last two " }"
                int [] bits = MDLReader.splitToInts(MDLReader.nextLine(mdl));
                anim.setInterval(bits[0],bits[1]);
//             }
//             catch (NumberFormatException e)
//             {
//                 JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse animation: Interval could not be interpreted numerically.\nThis may break lots of things.");
//             }
        }
        MDLReader.mark(mdl);
        line = MDLReader.nextLine(mdl);
        while(!(line).startsWith("\t}"))
        {
            if( line.contains("Extent") || (line).contains("BoundsRadius") )
            {
                MDLReader.reset(mdl);
                anim.m_extents = ExtLog.read(mdl);
            }
            else
            {
                System.out.println("Adding anim tag: "+line);
                anim.tags.add(MDLReader.readFlag(line));
            }
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
        }
        System.out.println("Anim completed");
        return anim;
    }
    public void printTo(PrintWriter writer, int tabHeight)
    {
        String tabs = "";
        for( int i = 0; i < tabHeight; i++ )
        {
            tabs = tabs + "\t";
        }
        if( !m_name.equals("") )
        {
            writer.println(tabs+"Anim \""+m_name+"\" {");
        }
        else
        {
            writer.println(tabs+"Anim {");
        }
        if( m_intervalEnd - m_intervalStart > 0 )
        {
            writer.println(tabs+"\tInterval { "+m_intervalStart+", "+m_intervalEnd+" },");
        }
        for( int i = 0; i < tags.size(); i++ )
        {
            writer.println(tabs+"\t"+tags.get(i)+",");
        }
        if( m_extents != null )
        {
            m_extents.printTo(writer,tabHeight+1);
        }
        writer.println(tabs+"}");
    }
    
    public String toString()
    {
    	return getName();
    }
}
