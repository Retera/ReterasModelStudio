package com.matrixeater.src;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.PrintWriter;
/**
 * The overarching sequence parser for Animations.
 * 
 * @author Eric Theller
 * @version 11/10/2011
 */
public class Sequences
{
    public static ArrayList parseText(String [] line)
    {
        if( line[0].contains("Sequences") )
        {
            ArrayList seqs = new ArrayList();
            int i = 1;
            while( !line[i].startsWith("}") )
            {
                ArrayList animText = new ArrayList();
                if( line[i].contains("Anim") )
                {
                    while( !line[i].contains("}") )
                    {
                        animText.add(line[i]);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse sequences; Confused by opener line '"+line[i]+"'.");
                }
                seqs.add(Animation.parseText((String[])animText.toArray()));
                i++;
            }
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse sequences: Missing or unrecognized open statement.");
        }
        return null;
    }
    public static ArrayList<Animation> read(BufferedReader mdl)
    {
        ArrayList<Animation> seqs = new ArrayList<Animation>();
        String line = "";
        line = MDLReader.nextLine(mdl);
        if( !line.contains("Sequences") )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse sequences: Confused by opener line '"+line+"'.\nNo sequences?");
            MDLReader.reset(mdl);
        }
        else
        {
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( !(line).startsWith("}") )
            {
                if( line.contains("Anim") )
                {
                    MDLReader.reset(mdl);
                    Animation an = Animation.read(mdl);
                    seqs.add(an);
                }
                else
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse sequences: Confused by opener line '"+line+"'.");
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
        }
        return seqs;
    }
}