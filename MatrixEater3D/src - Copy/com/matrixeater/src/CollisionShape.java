package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
/**
 * A class for CollisionShapes, which handle unit selection and related matters
 * 
 * Eric Theller 
 * 3/10/2012 3:52 PM
 */
public class CollisionShape extends IdObject
{
    ArrayList<String> flags = new ArrayList<String>();
    ExtLog m_extents;
    ArrayList<Vertex> m_vertex = new ArrayList<Vertex>();
    ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();
    
    public IdObject copy()
    {
        CollisionShape x = new CollisionShape();
        
        x.name = name + " copy";
        x.pivotPoint = new Vertex(pivotPoint);
        x.objectId = objectId;
        x.parentId = parentId;
        x.parent = parent;
        
        x.flags = new ArrayList<String>(flags);
        x.m_vertex = new ArrayList<Vertex>(m_vertex);
        for( AnimFlag af: animFlags )
        {
            x.animFlags.add(new AnimFlag(af));
        }
        return x;
    }
    public void addVertex(Vertex v)
    {
        m_vertex.add(v);
    }
    public Vertex getVertex(int vertId)
    {
        return m_vertex.get(vertId);
    }
    public int numVerteces()
    {
        return m_vertex.size();
    }
    
    private CollisionShape()
    {
        
    }
    public static CollisionShape read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        if( line.contains("CollisionShape") )
        {
            CollisionShape e = new CollisionShape();
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
                else if( line.contains("Extent") || (line).contains("BoundsRadius") )
                {
                    MDLReader.reset(mdl);
                    e.m_extents = ExtLog.read(mdl);
                }
                else if( line.contains("Vertices") )
                {
                    while( !((line = MDLReader.nextLine(mdl)).contains("\t}") ) )
                    {
                        e.addVertex(Vertex.parseText(line));
                    }
                }
                else if( ( line.contains("Scaling") || line.contains("Rotation") || line.contains("Translation") ) && !line.contains("DontInherit") )
                {
                    MDLReader.reset(mdl);
                    e.animFlags.add(AnimFlag.read(mdl));
                }
                else
                {
                    e.flags.add(MDLReader.readFlag(line));
                }
                MDLReader.mark(mdl);
                line = MDLReader.nextLine(mdl);
            }
            return e;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse CollisionShape: Missing or unrecognized open statement.");
        }
        return null;
    }
    public void printTo(PrintWriter writer)
    {
        //Remember to update the ids of things before using this
        // -- uses objectId value of idObject superclass
        // -- uses parentId value of idObject superclass
        // -- uses the parent (java Object reference) of idObject superclass
        writer.println(MDLReader.getClassName(this.getClass())+" \""+getName()+"\" {");
        if( objectId != -1 )
        {
            writer.println("\tObjectId "+objectId+",");
        }
        if( parentId != -1 )
        {
            writer.println("\tParent "+parentId+",\t// \""+parent.getName()+"\"");
        }
        for( String s: flags )
        {
            writer.println("\t"+s+",");
        }
        writer.println("\tVertices "+m_vertex.size()+" {");
        for( Vertex v: m_vertex )
        {
            writer.println("\t\t"+v.toString()+",");
        }
        writer.println("\t}");
        if( m_extents != null )
        {
            m_extents.printTo(writer,1);
        }
        for( int i = 0; i < animFlags.size(); i++ )
        {
            animFlags.get(i).printTo(writer,1);
        }
        writer.println("}");
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
