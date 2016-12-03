package com.matrixeater.src;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.PrintWriter;
/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or Rotation
 * AnimFlags are not "real" things from an MDL and are given this name by me, as an invented
 * java class to simplify the programming
 * 
 * Eric Theller 
 * 11/5/2011
 */
public class AnimFlag
{
    ArrayList tags = new ArrayList();
    String title;
    Integer globalSeq;
    int globalSeqId = -1;
    boolean hasGlobalSeq = false;
    ArrayList<Integer> times = new ArrayList<Integer>();
    ArrayList values = new ArrayList();
    ArrayList inTans = new ArrayList();
    ArrayList outTans = new ArrayList();
    int typeid = 0;
    public boolean equals(AnimFlag o)
    {
        System.out.println("AF compared, important to note!");
        boolean does = o instanceof AnimFlag;
        if( !does )
        {
            return false;
        }
        AnimFlag af = (AnimFlag)o;
        does = title.equals(af.title);
        does = hasGlobalSeq == af.hasGlobalSeq;
        does = values.equals(af.values)
        && (globalSeq == null ? af.globalSeq == null : globalSeq.equals(af.globalSeq))
        && (tags == null ? af.tags == null : tags.equals(af.tags))
        && (inTans == null ? af.inTans == null : inTans.equals(af.inTans))
        && (outTans == null ? af.outTans == null : outTans.equals(af.outTans))
        && typeid == af.typeid;
        return does;
    }
    public void setGlobSeq(Integer inte)
    {
        globalSeq = inte;
        hasGlobalSeq = true;
    }
    public AnimFlag(String title, ArrayList<Integer> times, ArrayList values)
    {
        this.title = title;
        this.times = times;
        this.values = values;
    }
    public AnimFlag(String title)
    {
        this.title = title;
        tags.add("DontInterp");
    }
    public int size()
    {
        return times.size();
    }
    public int length()
    {
        return times.size();
    }
    public AnimFlag( AnimFlag af )
    {
        title = af.title;
        tags = af.tags;
        globalSeq = af.globalSeq;
        globalSeqId = af.globalSeqId;
        hasGlobalSeq = af.hasGlobalSeq;
        typeid = af.typeid;
        times = new ArrayList<Integer>(af.times);
        values = new ArrayList<Integer>(af.values);
        inTans = new ArrayList<Integer>(af.inTans);
        outTans = new ArrayList<Integer>(af.outTans);
    }
    public static AnimFlag buildEmptyFrom(AnimFlag af)
    {
        AnimFlag na = new AnimFlag(af.title);
        na.tags = af.tags;
        na.globalSeq = af.globalSeq;
        na.globalSeqId = af.globalSeqId;
        na.hasGlobalSeq = af.hasGlobalSeq;
        na.typeid = af.typeid;
        return na;
    }
    public void setValuesTo( AnimFlag af )
    {
        title = af.title;
        tags = af.tags;
        globalSeq = af.globalSeq;
        globalSeqId = af.globalSeqId;
        hasGlobalSeq = af.hasGlobalSeq;
        typeid = af.typeid;
        times = new ArrayList<Integer>(af.times);
        values = new ArrayList<Integer>(af.values);
        inTans = new ArrayList<Integer>(af.inTans);
        outTans = new ArrayList<Integer>(af.outTans);
    }
    public String getName()
    {
        return title;
    }
    public int getTypeId()
    {
        return typeid;
    }
    private AnimFlag()
    {
        
    }
    public static AnimFlag parseText(String [] line)
    {
        
        AnimFlag aflg = new AnimFlag();
        aflg.title = MDLReader.readIntTitle(line[0]);
        //Types of AnimFlags:
        // 0 Alpha
        // 1 Scaling
        // 2 Rotation
        // 3 Translation
        int typeid = 0;
        if( aflg.title.equals("Scaling") )
        {
            typeid = 1;
        }
        else if( aflg.title.equals("Rotation") )
        {
            typeid = 2;
        }
        else if( aflg.title.equals("Translation") )
        {
            typeid = 3;
        }
        else if( !aflg.title.equals("Alpha") )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse \""+aflg.title+"\": Missing or unrecognized open statement.");
        }
        aflg.typeid = typeid;
        for( int i = 1; i < line.length; i++ )
        {
            if( line[i].contains("Tan") )
            {
                ArrayList target = null;
                if( line[i].contains("In") )//InTan
                {
                    target = aflg.inTans;
                }
                else//OutTan
                {
                    target = aflg.outTans;
                }
                switch (typeid )
                {
                    case 0: //Alpha
                        //A single double is used to store alpha data
                        target.add(new Double(MDLReader.readDouble(line[i])));
                        break;
                    case 1: //Scaling
                        //A vertex is used to store scaling data
                        target.add(Vertex.parseText(line[i]));
                        break;
                    case 2: //Rotation
                        //A quaternion set of four values is used to store rotation data
                        target.add(QuaternionRotation.parseText(line[i]));
                        break;
                    case 3: //Translation
                        //A vertex is used to store translation data
                        target.add(Vertex.parseText(line[i]));
                        break;
                }
            }
            else if( line[i].contains(":") )
            {
                switch (typeid )
                {
                    case 0: //Alpha
                        //A single double is used to store alpha data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
                        aflg.values.add(new Double(MDLReader.readDouble(line[i])));
                        break;
                    case 1: //Scaling
                        //A vertex is used to store scaling data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
                        aflg.values.add(Vertex.parseText(line[i]));
                        break;
                    case 2: //Rotation
                        //A quaternion set of four values is used to store rotation data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
                        aflg.values.add(QuaternionRotation.parseText(line[i]));
                        break;
                    case 3: //Translation
                        //A vertex is used to store translation data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
                        aflg.values.add(Vertex.parseText(line[i]));
                        break;
                }
            }
            else if( line[i].contains("GlobalSeqId") )
            {
                if( !aflg.hasGlobalSeq )
                {
                    aflg.globalSeqId = MDLReader.readInt(line[i]);
                    aflg.hasGlobalSeq = true;
                }
                else
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing "+aflg.title+": More than one Global Sequence Id is present in the same "+aflg.title+"!");
                }
            }
            else
            {
                aflg.tags.add(MDLReader.readFlag(line[i]));
            }
        }
        return aflg;
    }
    public static AnimFlag read(BufferedReader mdl)
    {
        
        AnimFlag aflg = new AnimFlag();
        aflg.title = MDLReader.readIntTitle(MDLReader.nextLine(mdl));
        //Types of AnimFlags:
        // 0 Alpha
        // 1 Scaling
        // 2 Rotation
        // 3 Translation
        int typeid = 0;
        if( aflg.title.equals("Scaling") )
        {
            typeid = 1;
        }
        else if( aflg.title.equals("Rotation") )
        {
            typeid = 2;
        }
        else if( aflg.title.equals("Translation") )
        {
            typeid = 3;
        }
        else if( aflg.title.equals("TextureID") )//aflg.title.equals("Visibility") ||  -- 100.088% visible in UndeadCampaign3D OutTans! Go look!
        {
            typeid = 5;
        }
        else if( aflg.title.contains("Color") )//AmbColor
        {
            typeid = 4;
        }
        else if( !(aflg.title.equals("Alpha") ))
        {
//             JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse \""+aflg.title+"\": Missing or unrecognized open statement.");
            //Having BS random AnimFlags is okay now, they all use double entries
        }
        aflg.typeid = typeid;
        String line = "";
        while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
        {
            if( line.contains("Tan") )
            {
                ArrayList target = null;
                if( line.contains("In") )//InTan
                {
                    target = aflg.inTans;
                }
                else//OutTan
                {
                    target = aflg.outTans;
                }
                switch (typeid )
                {
                    case 0: //Alpha
                        //A single double is used to store alpha data
                        target.add(new Double(MDLReader.readDouble(line)));
                        break;
                    case 1: //Scaling
                        //A vertex is used to store scaling data
                        target.add(Vertex.parseText(line));
                        break;
                    case 2: //Rotation
                        //A quaternion set of four values is used to store rotation data
                        try {
                            target.add(QuaternionRotation.parseText(line));
                        }
                        catch( Exception e )
                        {
//                             typeid = 0;//Yay! random bad model.
                            target.add(new Double(MDLReader.readDouble(line)));
                        }
                        break;
                    case 3: //Translation
                        //A vertex is used to store translation data
                        target.add(Vertex.parseText(line));
                        break;
                    case 4: //Translation
                        //A vertex is used to store translation data
                        target.add(Vertex.parseText(line));
                        break;
                    case 5: //Alpha
                        //A single double is used to store alpha data
                        target.add(new Integer(MDLReader.readInt(line)));
                        break;
                }
            }
            else if( line.contains(":") )
            {
                switch (typeid )
                {
                    case 0: //Alpha
                        //A single double is used to store alpha data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                        aflg.values.add(new Double(MDLReader.readDouble(line)));
                        break;
                    case 1: //Scaling
                        //A vertex is used to store scaling data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                        aflg.values.add(Vertex.parseText(line));
                        break;
                    case 2: //Rotation
                        //A quaternion set of four values is used to store rotation data
                        try {
                            
                            aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                            aflg.values.add(QuaternionRotation.parseText(line));
                        }
                        catch (Exception e)
                        {
//                             JOptionPane.showMessageDialog(null,e.getStackTrace());
//                             typeid = 0;
//                             aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                            aflg.values.add(new Double(MDLReader.readDouble(line)));
                        }
                        break;
                    case 3: //Translation
                        //A vertex is used to store translation data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                        aflg.values.add(Vertex.parseText(line));
                        break;
                    case 4: //Color
                        //A vertex is used to store translation data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                        aflg.values.add(Vertex.parseText(line));
                        break;
                    case 5: //Visibility
                        //A vertex is used to store translation data
                        aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
                        aflg.values.add(new Integer(MDLReader.readInt(line)));
                        break;
                }
            }
            else if( line.contains("GlobalSeqId") )
            {
                if( !aflg.hasGlobalSeq )
                {
                    aflg.globalSeqId = MDLReader.readInt(line);
                    aflg.hasGlobalSeq = true;
                }
                else
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing "+aflg.title+": More than one Global Sequence Id is present in the same "+aflg.title+"!");
                }
            }
            else
            {
                aflg.tags.add(MDLReader.readFlag(line));
            }
        }
        return aflg;
    }
    public void updateGlobalSeqRef(MDL mdlr)
    {
        if( hasGlobalSeq )
        globalSeq = mdlr.getGlobalSeq(globalSeqId);
    }
    public void updateGlobalSeqId(MDL mdlr)
    {
        if( hasGlobalSeq )
        {
            globalSeqId = mdlr.getGlobalSeqId(globalSeq);
        }
    }
    public String flagToString(Object o)
    {
        if( o.getClass() == double.class )
        {
            return MDLReader.doubleToString((Double)o);
        }
        else if( o.getClass() == Double.class )
        {
            return MDLReader.doubleToString(((Double)o).doubleValue());
        }
        else
        {
            return o.toString();
        }
    }
    public void flipOver(byte axis)
    {
        if( typeid == 2 )
        {
            //Rotation
            for( int k = 0; k < values.size(); k++ )
            {
                QuaternionRotation rot = (QuaternionRotation)values.get(k);
                Vertex euler = rot.toEuler();
                switch (axis)
                {
                    case 0:
                        euler.setCoord((byte)0,-euler.getCoord((byte)0));
                        euler.setCoord((byte)1,-euler.getCoord((byte)1));
                        break;
                    case 1:
                        euler.setCoord((byte)0,-euler.getCoord((byte)0));
                        euler.setCoord((byte)2,-euler.getCoord((byte)2));
                        break;
                    case 2:
                        euler.setCoord((byte)1,-euler.getCoord((byte)1));
                        euler.setCoord((byte)2,-euler.getCoord((byte)2));
                        break;
                }
                values.set(k,new QuaternionRotation(euler));
            }
            for( int k = 0; k < inTans.size(); k++ )
            {
                QuaternionRotation rot = (QuaternionRotation)inTans.get(k);
                Vertex euler = rot.toEuler();
                switch (axis)
                {
                    case 0:
                        euler.setCoord((byte)0,-euler.getCoord((byte)0));
                        euler.setCoord((byte)1,-euler.getCoord((byte)1));
                        break;
                    case 1:
                        euler.setCoord((byte)0,-euler.getCoord((byte)0));
                        euler.setCoord((byte)2,-euler.getCoord((byte)2));
                        break;
                    case 2:
                        euler.setCoord((byte)1,-euler.getCoord((byte)1));
                        euler.setCoord((byte)2,-euler.getCoord((byte)2));
                        break;
                }
                inTans.set(k,new QuaternionRotation(euler));
            }
            for( int k = 0; k < outTans.size(); k++ )
            {
                QuaternionRotation rot = (QuaternionRotation)outTans.get(k);
                Vertex euler = rot.toEuler();
                switch (axis)
                {
                    case 0:
                        euler.setCoord((byte)0,-euler.getCoord((byte)0));
                        euler.setCoord((byte)1,-euler.getCoord((byte)1));
                        break;
                    case 1:
                        euler.setCoord((byte)0,-euler.getCoord((byte)0));
                        euler.setCoord((byte)2,-euler.getCoord((byte)2));
                        break;
                    case 2:
                        euler.setCoord((byte)1,-euler.getCoord((byte)1));
                        euler.setCoord((byte)2,-euler.getCoord((byte)2));
                        break;
                }
                outTans.set(k,new QuaternionRotation(euler));
            }
        }
        else if( typeid == 3 )
        {
            //Translation
            for( int k = 0; k < values.size(); k++ )
            {
                Vertex trans = (Vertex)values.get(k);
//                 trans.setCoord(axis,-trans.getCoord(axis));
                switch (axis)
                {
//                     case 0:
//                         trans.setCoord((byte)2,-trans.getCoord((byte)2));
//                         break;
//                     case 1:
//                         trans.setCoord((byte)0,-trans.getCoord((byte)0));
//                         break;
//                     case 2:
//                         trans.setCoord((byte)1,-trans.getCoord((byte)1));
//                         break;
                    case 0:
                        trans.setCoord((byte)0,-trans.getCoord((byte)0));
                        break;
                    case 1:
                        trans.setCoord((byte)1,-trans.getCoord((byte)1));
                        break;
                    case 2:
                        trans.setCoord((byte)2,-trans.getCoord((byte)2));
                        break;
                }
            }
            for( int k = 0; k < inTans.size(); k++ )
            {
                Vertex trans = (Vertex)inTans.get(k);
//                 trans.setCoord(axis,-trans.getCoord(axis));
                switch (axis)
                {
//                     case 0:
//                         trans.setCoord((byte)2,-trans.getCoord((byte)2));
//                         break;
//                     case 1:
//                         trans.setCoord((byte)0,-trans.getCoord((byte)0));
//                         break;
//                     case 2:
//                         trans.setCoord((byte)1,-trans.getCoord((byte)1));
//                         break;
                    case 0:
                        trans.setCoord((byte)0,-trans.getCoord((byte)0));
                        break;
                    case 1:
                        trans.setCoord((byte)1,-trans.getCoord((byte)1));
                        break;
                    case 2:
                        trans.setCoord((byte)2,-trans.getCoord((byte)2));
                        break;
                }
            }
            for( int k = 0; k < outTans.size(); k++ )
            {
                Vertex trans = (Vertex)outTans.get(k);
//                 trans.setCoord(axis,-trans.getCoord(axis));
                switch (axis)
                {
//                     case 0:
//                         trans.setCoord((byte)2,-trans.getCoord((byte)2));
//                         break;
//                     case 1:
//                         trans.setCoord((byte)0,-trans.getCoord((byte)0));
//                         break;
//                     case 2:
//                         trans.setCoord((byte)1,-trans.getCoord((byte)1));
//                         break;
                    case 0:
                        trans.setCoord((byte)0,-trans.getCoord((byte)0));
                        break;
                    case 1:
                        trans.setCoord((byte)1,-trans.getCoord((byte)1));
                        break;
                    case 2:
                        trans.setCoord((byte)2,-trans.getCoord((byte)2));
                        break;
                }
            }
        }
    }
    public void printTo(PrintWriter writer, int tabHeight)
    {
        if( size() > 0 )
        {
            sort();
            String tabs = "";
            for( int i = 0; i < tabHeight; i++ )
            {
                tabs = tabs + "\t";
            }
            writer.println(tabs+title+" "+times.size()+" {");
            for( int i = 0; i < tags.size(); i++ )
            {
                writer.println(tabs+"\t"+tags.get(i)+",");
            }
            if( hasGlobalSeq )
            {
                writer.println(tabs+"\tGlobalSeqId "+globalSeqId+",");
            }
            boolean tans = false;
            if( inTans.size() > 0 )
            {
                tans = true;
            }
            for( int i = 0; i < times.size(); i++ )
            {
                writer.println(tabs+"\t"+times.get(i)+": "+flagToString(values.get(i))+",");
                if( tans )
                {
                    writer.println(tabs+"\t\tInTan "+flagToString(inTans.get(i))+",");
                    writer.println(tabs+"\t\tOutTan "+flagToString(outTans.get(i))+",");
                }
            }
    //         switch (typeid )
    //         {
    //             case 0: //Alpha
    //                 //A single double is used to store alpha data
    //                 for( int i = 0; i < times.size(); i++ )
    //                 {
    //                     writer.println(tabs+"\t"+times.get(i)+": "+((Double)values.get(i)).doubleValue()+",");
    //                     if( tans )
    //                     {
    //                         writer.println(tabs+"\t\tInTan "+((Double)inTans.get(i)).doubleValue()+",");
    //                         writer.println(tabs+"\t\tOutTan "+((Double)outTans.get(i)).doubleValue()+",");
    //                     }
    //                 }
    //                 break;
    //             case 1: //Scaling
    //                 //A vertex is used to store scaling data
    //                 for( int i = 0; i < times.size(); i++ )
    //                 {
    //                     writer.println(tabs+"\t"+times.get(i)+": "+((Vertex)values.get(i)).toString()+",");
    //                     if( tans )
    //                     {
    //                         writer.println(tabs+"\t\tInTan "+((Vertex)inTans.get(i)).toString()+",");
    //                         writer.println(tabs+"\t\tOutTan "+((Double)outTans.get(i)).doubleValue()+",");
    //                     }
    //                 }
    //                 break;
    //             case 2: //Rotation
    //                 //A quaternion set of four values is used to store rotation data
    //                 aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
    //                 aflg.values.add(QuaternionRotation.parseText(line));
    //                 break;
    //             case 3: //Translation
    //                 //A vertex is used to store translation data
    //                 aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
    //                 aflg.values.add(Vertex.parseText(line));
    //                 break;
    //             case 4: //Color
    //                 //A vertex is used to store translation data
    //                 aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
    //                 aflg.values.add(Vertex.parseText(line));
    //                 break;
    //             case 5: //Visibility
    //                 //A vertex is used to store translation data
    //                 aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
    //                 aflg.values.add(new Integer(MDLReader.readInt(line)));
    //                 break;
    //         }
            writer.println(tabs+"}");
        }
    }
    public AnimFlag getMostVisible(AnimFlag partner)
    {
        if( partner != null )
        {
            if( (typeid == 0) && (partner.typeid == 0) )
            {
                ArrayList<Integer> atimes = new ArrayList<Integer>(times);
                ArrayList<Integer> btimes = new ArrayList<Integer>(partner.times);
                ArrayList<Double> avalues = (ArrayList<Double>)(new ArrayList(values));
                ArrayList<Double> bvalues = (ArrayList<Double>)(new ArrayList(partner.values));
                AnimFlag mostVisible = null;
                for( int i = atimes.size()-1; i >= 0; i-- )
                //count down from top, meaning that removing the current value causes no harm
                {
                    Integer currentTime = atimes.get(i);
                    Double currentVal = avalues.get(i);
                    
                    if( btimes.contains(currentTime) )
                    {
                        Double partVal = bvalues.get(btimes.indexOf(currentTime));
                        if( partVal.doubleValue() > currentVal.doubleValue() )
                        {
                            if( mostVisible == null )
                            {
                                mostVisible = partner;
                            }
                            else if( mostVisible == this )
                            {
                                return null;
                            }
                        }
                        else if( partVal.doubleValue() < currentVal.doubleValue() )
                        {
                            if( mostVisible == null )
                            {
                                mostVisible = this;
                            }
                            else if( mostVisible == partner )
                            {
                                return null;
                            }
                        }
                        else
                        {
                            System.out.println("Equal entries spell success");
                        }
//                         btimes.remove(currentTime);
//                         bvalues.remove(partVal);
                    }
                    else if( currentVal.doubleValue() < 1 )
                    {
                        if( mostVisible == null )
                        {
                            mostVisible = partner;
                        }
                        else if( mostVisible == this )
                        {
                            return null;
                        }
                    }
                }
                for( int i = btimes.size()-1; i >= 0; i-- )
                //count down from top, meaning that removing the current value causes no harm
                {
                    Integer currentTime = btimes.get(i);
                    Double currentVal = bvalues.get(i);
                    
                    if( atimes.contains(currentTime) )
                    {
                        Double partVal = avalues.get(atimes.indexOf(currentTime));
                        if( partVal.doubleValue() > currentVal.doubleValue() )
                        {
                            if( mostVisible == null )
                            {
                                mostVisible = this;
                            }
                            else if( mostVisible == partner )
                            {
                                return null;
                            }
                        }
                        else if( partVal.doubleValue() < currentVal.doubleValue() )
                        {
                            if( mostVisible == null )
                            {
                                mostVisible = partner;
                            }
                            else if( mostVisible == this )
                            {
                                return null;
                            }
                        }
                    }
                    else if( currentVal.doubleValue() < 1 )
                    {
                        if( mostVisible == null )
                        {
                            mostVisible = this;
                        }
                        else if( mostVisible == partner )
                        {
                            return null;
                        }
                    }
                }
                if( mostVisible == null )
                {
                    return partner;//partner has priority!
                }
                else
                {
                    return mostVisible;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Error: Program attempted to compare visibility with non-visibility animation component.\nThis... probably means something is horribly wrong. Save your work, if you can.");
            }
        }
        return null;
    }
    public boolean tans()
    {
        return tags.contains("Bezier") || tags.contains("Hermite") || inTans.size() > 0;
    }
    public void copyFrom( AnimFlag source )
    {
        times.addAll(source.times);
        values.addAll(source.values);
        boolean stans = source.tans();
        boolean mtans = tans();
        if( stans && mtans )
        {
            inTans.addAll(source.inTans);
            outTans.addAll(source.outTans);
        }
        else if( mtans )
        {
            JOptionPane.showMessageDialog(null,"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
            inTans.clear();
            outTans.clear();
            tags = source.tags;
            //Probably makes this flag linear, but certainly makes it more like the copy source
        }
    }
    public void deleteAnim( Animation anim )
    {
        if( !hasGlobalSeq )
        {
            boolean tans = tans();
            for( int index = times.size()-1; index >= 0 ; index-- )
            {
                Integer inte = times.get(index);
                int i = inte.intValue();
    //             int index = times.indexOf(inte);
                if( i >= anim.getStart() && i <= anim.getEnd())
                {
                    //If this "i" is a part of the anim being removed
                    
                    times.remove(index);
                    values.remove(index);
                    if( tans )
                    {
                        inTans.remove(index);
                        outTans.remove(index);
                    }
                }
            }
        }
        else
        {
            System.out.println("KeyFrame deleting was blocked by a GlobalSequence");
        }
        
        //BOOM magic happens
    }
    public void copyFrom( AnimFlag source, int sourceStart, int sourceEnd, int newStart, int newEnd )
    {
        //Timescales a part of the AnimFlag from the source into the new time "newStart" to "newEnd"
        boolean tans = source.tans();
        for( Integer inte: source.times )
        {
            int i = inte.intValue();
            int index = source.times.indexOf(inte);
            if( i >= sourceStart && i <= sourceEnd )
            {
                //If this "i" is a part of the anim being rescaled
                double ratio = (double)(i-sourceStart)/(double)(sourceEnd-sourceStart);
                times.add(new Integer((int)(newStart + ( ratio * ( newEnd - newStart ) ) ) ) );
                values.add(source.values.get(index));
                if( tans )
                {
                    inTans.add(source.inTans.get(index));
                    outTans.add(source.outTans.get(index));
                }
            }
        }
        
        sort();
        
        //BOOM magic happens
    }
    public void timeScale( int start, int end, int newStart, int newEnd )
    {
        //Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
//         if( newEnd > newStart )
//         {
            for( int z = 0; z < times.size(); z++ )//Integer inte: times )
            {
                Integer inte = times.get(z);
                int i = inte.intValue();
                if( i >= start && i <= end )
                {
                    //If this "i" is a part of the anim being rescaled
                    double ratio = (double)(i-start)/(double)(end-start);
                    times.set(z,new Integer((int)(newStart + ( ratio * ( newEnd - newStart ) ) ) ) );
                }
            }
//         }
//         else
//         {
//             for( Integer inte: times )
//             {
//                 int i = inte.intValue();
//                 if( i >= end && i <= start )
//                 {
//                     //If this "i" is a part of the anim being rescaled
//                     double ratio = (double)(i-start)/(double)(end-start);
//                     times.set(times.indexOf(inte),new Integer((int)(newStart + ( ratio * ( newStart - newEnd ) ) ) ) );
//                 }
//             }
//         }
        
        sort();
        
        //BOOM magic happens
    }
    public void sort()
    {
        int low = 0;
        int high = times.size() - 1;
        if( size() > 1 )
        quicksort(low,high);
    }
    private void quicksort(int low, int high)
    {
        //Thanks to Lars Vogel for the quicksort concept code (something to look at), found on google
        // (re-written by Eric "Retera" for use in AnimFlags)
        int i = low, j = high;
        Integer pivot = times.get(low + (high-low)/2);
        
        while( i <= j )
        {
            while( times.get(i).intValue() < pivot.intValue() )
            {
                i++;
            }
            while( times.get(j).intValue() > pivot.intValue() )
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
        Integer iTime = times.get(i);
        Object iValue = values.get(i);
        
        times.set(i,times.get(j));
        try {
            values.set(i,values.get(j));
        }
        catch( Exception e)
        {
            e.printStackTrace();
//             System.out.println(getName()+": "+times.size()+","+values.size());
//             System.out.println(times.get(0)+": "+values.get(0));
//             System.out.println(times.get(1));
        }
        
        times.set(j,iTime);
        values.set(j,iValue);
        
        if( inTans.size() > 0 )//if we have to mess with Tans
        {
            Object iInTan = inTans.get(i);
            Object iOutTan = outTans.get(i);
            
            inTans.set(i,inTans.get(j));
            outTans.set(i,outTans.get(j));
            
            inTans.set(j,iInTan);
            outTans.set(j,iOutTan);
        }
    }
}
