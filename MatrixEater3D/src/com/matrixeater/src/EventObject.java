package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * A class for EventObjects, which include such things as craters,
 *    footprints, splashes, blood spurts, and sounds
 * 
 * Eric Theller 
 * 3/10/2012 3:52 PM
 */
public class EventObject extends IdObject
{
    ArrayList<Integer> eventTrack = new ArrayList<Integer>();
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    private EventObject()
    {
        
    }
    protected EventObject(EventObject source)
    {
        
    }
    public IdObject copy()
    {
        EventObject x = new EventObject();
        
        x.name = name + " copy";
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.eventTrack = new ArrayList<Integer>(eventTrack);
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        return x;
    }
    public static EventObject buildEmptyFrom(EventObject source)
    {
        return new EventObject(source);
        
    }
    public void setValuesTo(EventObject source)
    {
        eventTrack = source.eventTrack;
    }
    public static EventObject read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("EventObject") )
        {
            EventObject e = new EventObject();
            e.setName(MDLReader.readName(line));
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( ( !line.contains("}")  || line.contains("},") || line.contains("\t}") ) && !line.equals("COMPLETED PARSING") ) 
            {
                if( line.contains("ObjectId") )
                {
                    e.objectId = MDLReader.readInt(line);
                }
                else if( line.contains("Parent") )
                {
                    e.parentId = MDLReader.splitToInts(line)[0];
//                     e.parent = mdlr.getIdObject(e.parentId);
                }
                else if( (line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation") || line.contains("Scaling") ) )
                {
                    MDLReader.reset(mdl);
                    e.animFlags.add(AnimFlag.read(mdl));
                }
                else if( !line.contains("{") && !line.contains("}") )
                {
                    e.eventTrack.add(new Integer(MDLReader.readInt(line)));
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return e;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse EventObject: Missing or unrecognized open statement.");
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
        if( eventTrack.size() <= 0 )
        {
            writer.println("\tEventTrack "+1+" {");
            writer.println("\t\t"+0+",");
        }
        else
        {
            writer.println("\tEventTrack "+eventTrack.size()+" {");
            for( int i = 0; i < eventTrack.size(); i++ )
            {
                writer.println("\t\t"+eventTrack.get(i).toString()+",");
            }
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

    public void deleteAnim( Animation anim )
    {
        //Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
        for( int index = eventTrack.size()-1; index >= 0; index-- )
        {
            int i = eventTrack.get(index).intValue();
            if( i >= anim.getStart() && i <= anim.getEnd() )
            {
                //If this "i" is a part of the anim being removed
            	eventTrack.remove(index);
            }
        }
        
        //BOOM magic happens
    }
    
    public void timeScale( int start, int end, int newStart, int newEnd )
    {
        //Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
        for( Integer inte: eventTrack )
        {
            int i = inte.intValue();
            if( i >= start && i <= end )
            {
                //If this "i" is a part of the anim being rescaled
                double ratio = (double)(i-start)/(double)(end-start);
                eventTrack.set(eventTrack.indexOf(inte),new Integer((int)(newStart + ( ratio * ( newEnd - newStart ) ) ) ) );
            }
        }
        
        sort();
        
        //BOOM magic happens
    }
    public void copyFrom( EventObject source, int start, int end, int newStart, int newEnd )
    {
        //Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
        for( Integer inte: source.eventTrack )
        {
            int i = inte.intValue();
            if( i >= start && i <= end )
            {
                //If this "i" is a part of the anim being rescaled
                double ratio = (double)(i-start)/(double)(end-start);
                eventTrack.add(new Integer((int)(newStart + ( ratio * ( newEnd - newStart ) ) ) ) );
            }
        }
        
        sort();
        
        //BOOM magic happens
    }
    public void sort()
    {
        int low = 0;
        int high = eventTrack.size() - 1;
        
        if( eventTrack.size() > 0 )
        quicksort(low,high);
    }
    private void quicksort(int low, int high)
    {
        //Thanks to Lars Vogel for the quicksort concept code (something to look at), found on google
        // (re-written by Eric "Retera" for use in AnimFlags)
        int i = low, j = high;
        Integer pivot = eventTrack.get(low + (high-low)/2);
        
        while( i <= j )
        {
            while( eventTrack.get(i).intValue() < pivot.intValue() )
            {
                i++;
            }
            while( eventTrack.get(j).intValue() > pivot.intValue() )
            {
                j--;
            }
            if( i <= j )
            {
                exchange( i, j );
                i++;
                j--;
            }
        }
        
        if( low < j )
            quicksort(low, j);
        if( i < high )
            quicksort(i, high);
    }
    private void exchange( int i, int j )
    {
        Integer iTime = eventTrack.get(i);
        
        eventTrack.set(i,eventTrack.get(j));
        
        eventTrack.set(j,iTime);
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
