package com.matrixeater.src;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.PrintWriter;
/**
 * Vertex motion matrices.
 * 
 * Eric Theller
 * 11/10/2011
 */
public class Matrix
{
    ArrayList m_boneIds;
    ArrayList<Bone> bones;
    public Matrix()
    {
        m_boneIds = new ArrayList();
    }
    public Matrix(int id)
    {
        m_boneIds = new ArrayList();
        m_boneIds.add(id);
    }
    public Matrix(ArrayList boneIds, boolean useIds)
    {
        if( useIds )
        {
            m_boneIds = boneIds;
        }
        else
        {
            bones = boneIds;
        }
    }
    public String getName()
    {
        String out = "";
        if( bones != null )
        {
            if( bones.size() > 0 )
            {
                out = bones.get(0).getName();
                for( int i = 1; i < bones.size(); i++ )
                {
                    out = out + ", " + bones.get(i).getName();
                }
            }
            else
            {
                out = "Error bad bone list";
            }
        }
        else if( m_boneIds != null )
        {
            if( m_boneIds.size() > 0 )
            {
                out = m_boneIds.get(0).toString();
                for( int i = 1; i < m_boneIds.size(); i++ )
                {
                    out = out + ", " + m_boneIds.get(i).toString();
                }
            }
            else
            {
                out = "Error bad bone ids";
            }
        }
        return out;
    }
    public void updateIds(MDL mdlr)
    {
        int sz1 = bones.size();
        if( m_boneIds == null )
        {
            m_boneIds = new ArrayList();
        }
        else
        {
            m_boneIds.clear();
        }
        for( int i = 0; i < bones.size(); i++ )
        {
            int newId = mdlr.getObjectId(bones.get(i));
            if( newId >= 0 )
            {
                m_boneIds.add(newId);
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Error: A matrix's bone reference was missing in the model!\nDid you move geometry between models and forget to update bones?");
            }
        }
        if( m_boneIds.size() < sz1 || (sz1 != 0 && m_boneIds.size() == 0) )
        {
            JOptionPane.showMessageDialog(null,"Error: bad sizes in matrix ("+(sz1-m_boneIds.size())+" as difference, should be same size)");
        }
    }
    public void updateBones(MDL mdlr)
    {
        System.out.println("bones updated");
        if( bones == null )
        {
            bones = new ArrayList<Bone>();
        }
        else
        {
            bones.clear();
        }
        for( int i = 0; i < m_boneIds.size(); i++ )
        {
            Bone b = mdlr.getBone((Integer)m_boneIds.get(i));
//             if( b.getClass() == Helper.class )
//             {
//                 JOptionPane.showMessageDialog(null,"Error: Holy fo shizzle my grizzle! There's geometry attached to Helper "+b.getName()+" and that is very bad!");
//             }
            if( b != null )
            {
                bones.add(b);
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Error: A matrix's bone id was not referencing a real bone!");
            }
        }
    }
    public Matrix(ArrayList<Bone> newBones)
    {
        bones = new ArrayList<Bone>(newBones);
    }
    public Matrix(int[] boneIds)
    {
        m_boneIds = new ArrayList();
        for( int i = 0; i < boneIds.length; i++ )
        {
            m_boneIds.add(boneIds[i]);
        }
    }
    public void add(Bone b)
    {
        bones.add(b);
    }
    public void addId(int id)
    {
        m_boneIds.add(id);
    }
    public int getBoneId(int index)
    {
        return (Integer)m_boneIds.get(index);
    }
    public int size()
    {
        if( m_boneIds != null && m_boneIds.size() > 0 )
        {
            return m_boneIds.size();
        }
        else if( bones != null && bones.size() > 0 )
        {
            return bones.size();
        }
//         JOptionPane.showMessageDialog(null,"Warning: A matrix with no contents was used!");
//         System.out.println("Warning: A matrix with no contents was used!");
        return -1;//bad stuff
    }
    public static Matrix parseText(String input)
    {
        String [] entries = input.split(",");
        Matrix temp = null;
        int size = entries.length;
        int[] boneId = new int[size];
        //When { 4, 2 }, splits around commas, it has 3 segments and we
        // only want the first two (the third being "" after ending comma)
        if( size == 1 )
        {
            try
            {
                boneId[0] = Integer.parseInt(entries[0].split("\\{")[1].split("}")[0].split(" ")[1]);
            }
            catch( NumberFormatException e )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error \""+input+"\": Matrix data could not be interpreted.");
            }
        }
        else
        {
            try
            {
                boneId[0] = Integer.parseInt(entries[0].split("\\{")[1].split(" ")[1]);
            }
            catch( NumberFormatException e )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error \""+input+"\": Matrix data could not be interpreted.");
            }
            for(int i = 1; i < size; i++ )
            {
                try
                {
                    boneId[i] = Integer.parseInt(entries[i].split(" ")[1]);
                }
                catch( NumberFormatException e )
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error \""+input+"\": Matrix data could not be interpreted.");
                }
            }
        }
        if( boneId != null && boneId[0] != -1 )
        {
            temp = new Matrix(boneId);
        }
        else
        {
            temp = new Matrix();
        }
        return temp;
    }
    public void printTo(PrintWriter writer, int tabHeight)
    {
        String tabs = "";
        for( int i = 0; i < tabHeight; i++ )
        {
            tabs = tabs + "\t";
        }
        if( m_boneIds.size() > 0 )
        {
            System.out.print(tabs+"Matrices { "+m_boneIds.get(0).toString());
            writer.print(tabs+"Matrices { "+m_boneIds.get(0).toString());
        }
        else
        {
            System.out.print(tabs+"Matrices { -1");
            writer.print(tabs+"Matrices { -1");
        }
//         writer.print(tabs+"Matrices { "+bones.get(0).getClass().getName());
        for( int i = 1; i<bones.size(); i++ )
        {
            System.out.print(", "+m_boneIds.get(i));
            writer.print(", "+m_boneIds.get(i));
//             writer.print(", "+bones.get(i).getClass().getName());
        }
        System.out.println(" },");
        writer.println(" },");
    }
    
    public boolean equals(Matrix other)
    {
        if( other.size() != size() )
        {
            return false;
        }
        boolean same = true;
        for( int i = 0; i < size() && same; i++ )
        {
            if( bones.get(i) != other.bones.get(i) )
            {
                same = false;
            }
        }
        return same;
    }
}
