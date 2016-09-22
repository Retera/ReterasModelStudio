package com.matrixeater.src;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.io.*;
import java.util.Date;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;
public class MDLReader
{
    static int c = 0;
    static int markc = 0;
    static int lastMark = 0;
    static int followMarks = 0;
    public static String readName(String line)
    {
        //Obtains quoted information from a line
        return line.split("\"")[1];
    }
    public static String readField(String line)
    {
        //Obtains type information from the end of a line, "FilterMode None," could be read to obtain the text "None"
        String [] ex = line.split(" ");
        return ex[ex.length-1].split(",")[0];
        
    }
    public static String readFlag(String line)
    {
        //Obtains directly any kind of flag, i.e. "TwoSided," "Unshaded," etc
        String [] ex = line.split("\t");//The character was " " but I feel that that was a mistake
        return ex[ex.length-1].split(",")[0];
    }
    public static int readInt(String line)
    {
        //Obtains an integer, i.e. "static TextureId 7," would return the number 7
//         String [] ex = line.split(" ");
//         int out = 0;
//         try{
//             out = Integer.parseInt(ex[ex.length-1].split(",")[0]);
//         }
//         catch (NumberFormatException e)
//         {
//             try{
//                 out = Integer.parseInt(ex[ex.length-1].split(",")[0].split("\t")[2]);
//             }
//             catch (NumberFormatException exce)
//             {
//                 JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing: Could not interpret integer from: "+line);
//             }
//         }
    	if( line.contains("."))
    	{
    		return (int)readDouble(line);
    	}
        int [] ints = splitToInts(line);
        return ints[ints.length-1];//out;
    }
    public static int readBeforeColon(String line)
    {
//         //Obtains an integer, i.e. "static TextureId 7," would return the number 7
//         String [] ex = line.split(":");
//         String out = "";
//         ex = ex[0].split("\t");
//         for( int i = 0; i < ex.length; i++ )
//         {
//             if( ex[i].length() >= 1 )
//             {
//                 out = ex[i];
//                 break;
//             }
//         }
//         try{
//             return Integer.parseInt(out);
//         }
//         catch (NumberFormatException e)
//         {
//             JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing: Could not interpret integer from: "+line);
//         }
//         return -99999;//give them the feeling that there was a mistake
        return splitToInts(line)[0];
    }
    public static double readDouble(String line)
    {
        String [] ex = line.split(" ");
        double out = 0;
        try{
            out = Double.parseDouble(ex[ex.length-1].split(",")[0]);
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error while parsing: Could not interpret double from: "+line);
        }
        return out;
    }
    public static String readIntTitle(String line)
    {
        //does it work?
        String [] ex = line.split(" ");
        String out = "";
        ex = ex[0].split("\t");
        for( int i = 0; i < ex.length; i++ )
        {
            if( ex[i].length() > 1 )
            {
                out = ex[i];
                break;
            }
        }
        return out;
    }
    public static String [] breakElement( String [] input, int lineIndex )
    {
        //This would pull a layer out of a Material, for example
        int i = lineIndex;
        ArrayList<String> output = new ArrayList<String>();
        int dataHeight = 0;
        String [] temp = null;
        dataHeight+=occurrencesIn("{",input[i]);
        dataHeight-=occurrencesIn("}",input[i]);
        output.add(input[i]);
        i++;
        while( dataHeight > 0 && i < input.length )
        {
            dataHeight+=occurrencesIn("{",input[i]);
            dataHeight-=occurrencesIn("}",input[i]);
            output.add(input[i]);
            i++;
        }
        String [] out = new String[output.size()];
        for( int q = 0; q < output.size(); q++ )
        {
            out[q] = output.get(q);
        }
        return out;
    }
    public static int occurrencesIn(String what, String inputses)
    {
        int n = 0;
        int index = 0;
        int j = inputses.indexOf(what,index);
        boolean done = false;
        while( !done )//(j = inputses.indexOf(what,index)) != -1 )
        {
            if( j == -1 )
            {
                done = true;
            }
            else
            {
                n++;
                index = j;
            }
            j = inputses.indexOf(what,index+1);
        }
        return n;
    }
    public static void rmain(String [] args)
    {
//         for( int i = 0; i < readInt("            static TextureID 7,"); i++ )
//         {
//             System.out.println(readName("        Image \"Units\\Creeps\\HighElfArcher\\HighElfArcher.blp\",")+readFlag("         TwoSided,")+readField("         FilterMode Transparent,"));
//         }
        String [] test = {
"       my name is crash program",
"       Rotation 78 {",
"       Hermite,",
"       167: { 0.00398607, 0.10448, 0.0302773, 0.994058 },",
"           InTan { 0.00398607, 0.10448, 0.0302773, 0.994058 },",
"           OutTan { 0.00398607, 0.10448, 0.0302773, 0.994058 },",
"       467: { 0.00423933, 0.112796, 0.0302428, 0.993149 },",
"           InTan { 0.00423933, 0.112796, 0.0302428, 0.993149 },",
"           OutTan { 0.00423933, 0.112796, 0.0302428, 0.993149 },",
"       }",
"       john",
"       aup",
"       jones"
        };
        Vertex vez = Vertex.parseText("     MaximumExtent { -3.40282e+038, -3.40282e+038, -3.40282e+038 },");
        Matrix mrx = Matrix.parseText("     Matrices { 2 },");
        System.out.println(vez.getX()+","+vez.getY()+","+vez.getZ());//mrx.size()+":"+mrx.getBoneId(0));//+","+mrx.getBoneId(1)+","+mrx.getBoneId(2)+","+mrx.getBoneId(3)+","+mrx.getBoneId(4));//
        String [] eatTest = breakElement(test,1);
        System.out.println(readIntTitle(test[1]));
        for( int i = 0; i < eatTest.length; i++ )
        {
            System.out.println(eatTest[i]);
        }
        System.out.println((int)(occurrencesIn("yo","yo mana so low yo dun know it you dog you")));
        System.out.println((new Double(5.3)).toString());
        ArrayList<IdObject> testList = new ArrayList<IdObject>();
        testList.add(new Helper(5));
//         testList.add(new IdObject());
        System.out.println(Bone.class.isAssignableFrom((testList.get(0).getClass() )));
        System.out.println(Bone.class.isAssignableFrom((testList.get(1).getClass() )));
        String name [] = String.class.getName().split("\\.");
        
        System.out.println("The name of a String is: "+name[name.length-1]);
        System.out.println("It is: "+(new Date(System.currentTimeMillis())).toString());
    }
    public static String getClassName(Class what)
    {
        String name [] = what.getName().split("\\.");
        return name[name.length-1];
    }
    public static String nextLineSpecial(BufferedReader reader)
    {
        c++;
        String output = "";
        try
        {
            output = reader.readLine();
//             System.out.println(output);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error reading file.");
        }
        if( output == null )
        {
            output = "COMPLETED PARSING";
        }
        return output;
    }
    public static String nextLine(BufferedReader reader)
    {
        c++;
        String output = "";
        try
        {
            output = reader.readLine();
//             System.out.println(output);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error reading file.");
        }
        if( output == null )
        {
            output = "COMPLETED PARSING";
        }
        else if( output.contains("//") )
        {
            output = output.split("//")[0];
        }
        return output;
    }
    public static void reset(BufferedReader reader)
    {
        System.out.println("Line reset from line "+c+" back to line "+markc);
        if( c == markc + 1 && markc == lastMark )
        {
        	lastMark = markc;
        	followMarks ++;
        }
        else
        {
        	followMarks = 0;
        }
        if( followMarks > 30 )
        {
        	throw new RuntimeException("Program detected infinite loop in file reading sequence. Probably bad model, or error in interpreter!");
        }
        try
        {
            reader.reset();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Critical error in IO: Maybe length between line "+markc+" and line "+c+" got longer than expected?\nPortions of model data will be lost.");
            System.out.println("Line reset error: "+e);
        }
        c = markc;
    }
    public static void mark(BufferedReader reader, int m)
    {
        System.out.println("Line mark at line "+c+" of size "+m);
        markc = c;
        try
        {
            reader.mark(m);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Critical error: Read/write abilities lost. Loading will possibly be severely damaged and disfunctional.");
            System.out.println("Mark error: "+e);
//             System.out.println(e);
//             System.out.println("did not mark "+nextLine(reader));
//             JOptionPane.showMessageDialog(MainFrame.panel,"Critical error: Read/write abilities lost. Loading will be severely damaged and disfunctional.");
        }
        
//        lastMark = markc;
    }
    public static void mark(BufferedReader reader)
    {
        mark(reader,850);
    }
    public static void clearLineId()
    {
    	c = 0;
    	markc = 0;
    }
    
    public static int [] splitToIntsMath(String s)
    {
        int lastIndex = 0;
        int index = 0;
        int [] outputs = null;
        boolean wasInt = false;
        while( index < s.length() )
        {
            boolean isInt = false;
            for( int e = 0; e < 10; e++ )
            {
                if( s.substring(index,index+1).equals((new Integer(e)).toString()) )
                {
                    isInt = true;
                    break;
                }
            }
            if( ( s.substring(index,index+1).equals("*") || s.substring(index,index+1).equals("/") || s.substring(index,index+1).equals("+") || s.substring(index,index+1).equals("-") ) && wasInt )
            {
                isInt = true;
            }
//          System.out.println(s.substring(index,index+1)+","+isInt);
            if( !isInt )
            {
                if( wasInt )
                {
                    if( outputs != null )
                    {
                        int [] clone = new int[outputs.length+1];
                        for( int i = 0; i < outputs.length; i++ )
                        {
                            clone[i] = outputs[i];
                        }
                        outputs = clone;
                    }
                    else
                    {
                        outputs = new int[1];
                    }
                    String tempStr = s.substring(lastIndex,index);
                    int outint = 0;
                    if( tempStr.contains("*") )
                    {
                        String [] bits = tempStr.split("\\*");
                        outint = Integer.parseInt(bits[0]);
                        for( int x = 1; x < bits.length; x++ )
                        {
                            outint *= Integer.parseInt(bits[x]);
                        }
                        outputs[outputs.length-1] = outint;
                    }
                    else if( tempStr.contains("/") )
                    {
                        String [] bits = tempStr.split("/");
                        outint = Integer.parseInt(bits[0]);
                        for( int x = 1; x < bits.length; x++ )
                        {
                            outint /= Integer.parseInt(bits[x]);
                        }
                        outputs[outputs.length-1] = outint;
                    }
                    else if( tempStr.contains("+") )
                    {
                        String [] bits = tempStr.split("\\+");
                        outint = Integer.parseInt(bits[0]);
                        for( int x = 1; x < bits.length; x++ )
                        {
                            outint += Integer.parseInt(bits[x]);
                        }
                        outputs[outputs.length-1] = outint;
                    }
                    else if( tempStr.contains("-") )
                    {
                        String [] bits = tempStr.split("-");
                        outint = Integer.parseInt(bits[0]);
                        for( int x = 1; x < bits.length; x++ )
                        {
                            outint -= Integer.parseInt(bits[x]);
                        }
                        outputs[outputs.length-1] = outint;
                    }
                    else 
                    {
                        outputs[outputs.length-1] = Integer.parseInt(tempStr);
                    }
                }
            }
            else if( !wasInt )
            {
                lastIndex = index;
            }
            wasInt = isInt;
            index++;
        }
        if( wasInt )
        {
            if( outputs != null )
            {
                int [] clone = new int[outputs.length+1];
                for( int i = 0; i < outputs.length; i++ )
                {
                    clone[i] = outputs[i];
                }
                outputs = clone;
            }
            else
            {
                outputs = new int[1];
            }
            String tempStr = s.substring(lastIndex,index);
            int outint = 0;
            if( tempStr.contains("*") )
            {
                String [] bits = tempStr.split("\\*");
                outint = Integer.parseInt(bits[0]);
                for( int x = 1; x < bits.length; x++ )
                {
                    outint *= Integer.parseInt(bits[x]);
                }
                outputs[outputs.length-1] = outint;
            }
            else if( tempStr.contains("/") )
            {
                String [] bits = tempStr.split("/");
                outint = Integer.parseInt(bits[0]);
                for( int x = 1; x < bits.length; x++ )
                {
                    outint /= Integer.parseInt(bits[x]);
                }
                outputs[outputs.length-1] = outint;
            }
            else if( tempStr.contains("+") )
            {
                String [] bits = tempStr.split("\\+");
                outint = Integer.parseInt(bits[0]);
                for( int x = 1; x < bits.length; x++ )
                {
                    outint += Integer.parseInt(bits[x]);
                }
                outputs[outputs.length-1] = outint;
            }
            else if( tempStr.contains("-") )
            {
                String [] bits = tempStr.split("-");
                outint = Integer.parseInt(bits[0]);
                for( int x = 1; x < bits.length; x++ )
                {
                    outint -= Integer.parseInt(bits[x]);
                }
                outputs[outputs.length-1] = outint;
            }
            else 
            {
                outputs[outputs.length-1] = Integer.parseInt(tempStr);
            }
        }
        return outputs;
    }
    public static int [] splitToInts(String s)
    {
        int lastIndex = 0;
        int index = 0;
        int [] outputs = null;
        boolean wasInt = false;
        while( index < s.length() )
        {
            boolean isInt = false;
            for( int e = 0; e < 10 && !isInt; e++ )
            {
                if( s.substring(index,index+1).equals((new Integer(e)).toString()) )
                {
                    isInt = true;
                }
            }
            if( !isInt && s.substring(index,index+1).equals("-")  && !wasInt )
            {
                for( int e = 0; e < 10 && !isInt; e++ )
                {
                    if( s.substring(index+1,index+2).equals((new Integer(e)).toString()) )
                    {
                        isInt = true;
                    }
                }
            }
            if( !isInt )
            {
                if( wasInt )
                {
                    if( outputs != null )
                    {
                        int [] clone = new int[outputs.length+1];
                        for( int i = 0; i < outputs.length; i++ )
                        {
                            clone[i] = outputs[i];
                        }
                        outputs = clone;
                    }
                    else
                    {
                        outputs = new int[1];
                    }
                    String tempStr = s.substring(lastIndex,index);
                    int outint = 0;
                    try {
                        outputs[outputs.length-1] = Integer.parseInt(tempStr);
                    }
                    catch (NumberFormatException exc)
                    {
                        //This happens if the stuff contains a really long set of numbers or something like that
                        outputs[outputs.length-1] = (int)Long.parseLong(tempStr);
                    }
                }
            }
            else if( !wasInt )
            {
                lastIndex = index;
            }
            wasInt = isInt;
            index++;
        }
        if( wasInt )
        {
            if( outputs != null )
            {
                int [] clone = new int[outputs.length+1];
                for( int i = 0; i < outputs.length; i++ )
                {
                    clone[i] = outputs[i];
                }
                outputs = clone;
            }
            else
            {
                outputs = new int[1];
            }
            String tempStr = s.substring(lastIndex,index);
            int outint = 0;
            try {
                outputs[outputs.length-1] = Integer.parseInt(tempStr);
            }
            catch (NumberFormatException exc)
            {
                outputs[outputs.length-1] = (int)Long.parseLong(tempStr);
            }
        }
        return outputs;
    }
    public static String doubleToString( double n )
    {
        String base = (n+"").toLowerCase();
        if( base.equals("nan") )
        {
            JOptionPane.showMessageDialog(null,"Major problems with numeric data.");
            new Exception().printStackTrace();
            base = "0";
        }
        String out = "";
        if( base.contains("e-") )
        {
            String [] bits = base.split("e-");
            boolean done = false;
            for( int i = bits[0].length(); i > 0 && !done; i-- )
            {
                String c = bits[0].substring(i-1,i);
                if( c.equals("0") )
                {
                    bits[0] = bits[0].substring(0,i-1);
                }
                else if( c.equals(".") )
                {
                    bits[0] = bits[0].substring(0,i-1);
                    done = true;
                }
                else
                {
                    done = true;
                }
            }
            int btln = bits[1].length();
            if( btln > 3 )
            {
                bits[1] = "999";
            }
            else if( btln < 3 )
            {
                btln = 3 - btln;
                for( int i = 0; i < btln; i++ )
                {
                    bits[1] = "0" + bits[1];
                }
            }
            out = bits[0] + "e-" + bits[1];
        }
        else if( base.contains("e") )
        {
            String [] bits = base.split("e");
            boolean done = false;
            for( int i = bits[0].length(); i > 0 && !done; i-- )
            {
                String c = bits[0].substring(i-1,i);
                if( c.equals("0") )
                {
                    bits[0] = bits[0].substring(0,i-1);
                }
                else if( c.equals(".") )
                {
                    bits[0] = bits[0].substring(0,i-1);
                    done = true;
                }
                else
                {
                    done = true;
                }
            }
            int btln = bits[1].length();
            if( btln > 3 )
            {
                bits[1] = "999";
            }
            else if( btln < 3 )
            {
                btln = 3 - btln;
                for( int i = 0; i < btln; i++ )
                {
                    bits[1] = "0" + bits[1];
                }
            }
            out = bits[0] + "e+" + bits[1];
        }
        else if( base.contains(".") )
        {
            boolean done = false;
            for( int i = base.length(); i > 0 && !done; i-- )
            {
                String c = base.substring(i-1,i);
                if( c.equals("0") )
                {
                    base = base.substring(0,i-1);
                }
                else if( c.equals(".") )
                {
                    base = base.substring(0,i-1);
                    done = true;
                }
                else
                {
                    done = true;
                }
            }
            out = base;
        }
        if( out.equals("-0") )
        {
            out = "0";
        }
        return out;
    }
}
