package com.matrixeater.src;
import java.awt.Polygon;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
public class Triangle
{
    GeosetVertex [] m_verts = new GeosetVertex[3];
    int [] m_vertIds = new int[3];
    Geoset m_geoRef;
    public Triangle( GeosetVertex a, GeosetVertex b, GeosetVertex c, Geoset geoRef )
    {
        m_verts[0] = a;
        m_verts[1] = b;
        m_verts[2] = c;
        m_geoRef = geoRef;
    }
    public Triangle( int a, int b, int c, Geoset geoRef )
    {
        m_vertIds[0] = a;
        m_vertIds[1] = b;
        m_vertIds[2] = c;
        m_verts[0] = geoRef.getVertex(a);
        m_verts[1] = geoRef.getVertex(b);
        m_verts[2] = geoRef.getVertex(c);
        m_geoRef = geoRef;
    }
    public Triangle( GeosetVertex a, GeosetVertex b, GeosetVertex c)
    {
        m_verts[0] = a;
        m_verts[1] = b;
        m_verts[2] = c;
        m_geoRef = null;
    }
    public Triangle( int a, int b, int c)
    {
        m_vertIds[0] = a;
        m_vertIds[1] = b;
        m_vertIds[2] = c;
//         m_verts[0] = geoRef.getVertex(a);
//         m_verts[1] = geoRef.getVertex(b);
//         m_verts[2] = geoRef.getVertex(c);
        m_geoRef = null;
    }
    public void setGeoRef(Geoset geoRef)
    {
        m_geoRef = geoRef;
    }
    public void updateVertexRefs()
    {
        m_verts[0] = m_geoRef.getVertex(m_vertIds[0]);
        m_verts[1] = m_geoRef.getVertex(m_vertIds[1]);
        m_verts[2] = m_geoRef.getVertex(m_vertIds[2]);
    }
    public void updateVertexIds()
    {
        //Potentially this procedure could lag a bunch in the way I wrote it, but it will
        // change vertex ids to match a changed geoset, assuming the geoset still contains the 
        // vertex
        m_vertIds[0] = m_geoRef.getVertexId(m_verts[0]);
        m_vertIds[1] = m_geoRef.getVertexId(m_verts[1]);
        m_vertIds[2] = m_geoRef.getVertexId(m_verts[2]);
    }
    public void forceVertsUpdate()
    {
        if( !m_verts[0].triangles.contains(this) )
        m_verts[0].triangles.add(this);
        if( !m_verts[1].triangles.contains(this) )
        m_verts[1].triangles.add(this);
        if( !m_verts[2].triangles.contains(this) )
        m_verts[2].triangles.add(this);
    }
    public void updateVertexIds(Geoset geoRef)
    {
        m_geoRef = geoRef;
        updateVertexIds();
    }
    public void updateVertexRefs(ArrayList<GeosetVertex> list)
    {
        m_verts[0] = list.get(m_vertIds[0]);
        m_verts[1] = list.get(m_vertIds[1]);
        m_verts[2] = list.get(m_vertIds[2]);
    }
    public boolean containsRef(GeosetVertex v)
    {
        return m_verts[0] == v || m_verts[1] == v || m_verts[2] == v;
    }
    public boolean contains(GeosetVertex v)
    {
        return m_verts[0].equalLocs(v) || m_verts[1].equalLocs(v) || m_verts[2].equalLocs(v);
    }
    public GeosetVertex get(int index)
    {
        return m_verts[index];
    }
    public void set(int index, GeosetVertex v)
    {
        m_verts[index] = v;
        m_vertIds[index] = m_geoRef.getVertexId(v);
    }
    public int indexOf(GeosetVertex v)
    {
        int out = -1;
        for( int i = 0; i < m_verts.length && out == -1; i++ )
        {
            if( m_verts[i].equalLocs(v) )
            {
                out = i;
            }
        }
        return out;
    }
    public boolean equalLocs(Triangle t)
    {
        boolean equal = true;
        for( int i = 0; i < 3 && equal; i++ )
        {
            if( !t.m_verts[i].equalLocs(m_verts[i]) || t.m_vertIds[i] != m_vertIds[i] )
            {
                equal = false;
            }
        }
        return equal;
    }
    public boolean sameVerts(Triangle t)
    {
        boolean equal = true;
        for( int i = 0; i < 3 && equal; i++ )
        {
            if( !contains(t.m_verts[i]) )
            {
                equal = false;
            }
        }
        return equal;
    }
    public int indexOfRef(GeosetVertex v)
    {
        int out = -1;
        for( int i = 0; i < m_verts.length && out == -1; i++ )
        {
            if( m_verts[i] == v )
            {
                out = i;
            }
        }
        return out;
    }
    public boolean equalRefsNoIds(Triangle t)
    {
        boolean equal = true;
        for( int i = 0; i < 3 && equal; i++ )
        {
            if( t.m_verts[i] != m_verts[i] )
            {
                equal = false;
            }
        }
        return equal;
    }
    public boolean equalRefs(Triangle t)
    {
        boolean equal = true;
        for( int i = 0; i < 3 && equal; i++ )
        {
            if( t.m_verts[i] != m_verts[i] || t.m_vertIds[i] != m_vertIds[i] )
            {
                equal = false;
            }
        }
        return equal;
    }
    public GeosetVertex [] getAll()
    {
        return m_verts;
    }
    public int [] getIntCoords( byte dim )
    {
        int [] output = new int[3];
        for( int i = 0; i < 3; i++ )
        {
            output[i] = (int)(m_verts[i].getCoord(dim));
        }
        return output;
    }
    public double [] getCoords( byte dim )
    {
        double [] output = new double[3];
        for( int i = 0; i < 3; i++ )
        {
            output[i] = (m_verts[i].getCoord(dim));
        }
        return output;
    }
    public double [] getTVertCoords( byte dim, int layerId )
    {
        double [] output = new double[3];
        for( int i = 0; i < 3; i++ )
        {
            output[i] = (m_verts[i].getTVertex(layerId).getCoord(dim));
        }
        return output;
    }
    public static ArrayList<Triangle> parseText(String [] input)
    {
        //Usually triangles come in a single entry with all of them, so we parse the input into an ArrayList
        ArrayList<Triangle> output = new ArrayList<Triangle>();
        for( int l = 1; l < input.length; l++ )
        {
            String [] s = input[l].split(",");
            s[0] = s[0].substring(4,s[0].length());
            int s_size = MDLReader.occurrencesIn(input[l],",");
            s[s_size-1]=s[s_size-1].substring(0,s[s_size-1].length()-2);
            for( int t = 0; t < s_size-1; t+=3  )//s[t+3].equals("")||
            {
                for( int i = 0; i < 3; i++ )
                {
                    s[t+i]=s[t+i].substring(1);
                }
                try
                {
                    output.add(new Triangle(Integer.parseInt(s[t]),Integer.parseInt(s[t+1]),Integer.parseInt(s[t+2])) );
                }
                catch (NumberFormatException e)
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
                }
            }
        }
        return output;
    }
    public static ArrayList<Triangle> read(BufferedReader mdl)
    {
        //Usually triangles come in a single entry with all of them, so we parse the input into an ArrayList
        ArrayList<Triangle> output = new ArrayList<Triangle>();
        String line = "";
        while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
        {
//             System.out.println("Interpreting "+line+" for Triangles");
            String [] s = line.split(",");
            s[0] = s[0].substring(4,s[0].length());
            int s_size = MDLReader.occurrencesIn(",",line);
//             System.out.println("We broke it into "+s_size+" parts.");
            s[s_size-1]=s[s_size-1].substring(0,s[s_size-1].length()-2);
            for( int t = 0; t < s_size-1; t+=3  )//s[t+3].equals("")||
            {
                for( int i = 0; i < 3; i++ )
                {
                    s[t+i]=s[t+i].substring(1);
                }
                try
                {
                    output.add(new Triangle(Integer.parseInt(s[t]),Integer.parseInt(s[t+1]),Integer.parseInt(s[t+2])) );
                }
                catch (NumberFormatException e)
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
                }
            }
        }
        return output;
    }
    public static ArrayList<Triangle> read(BufferedReader mdl, Geoset geoRef)
    {
        //Usually triangles come in a single entry with all of them, so we parse the input into an ArrayList
        ArrayList<Triangle> output = new ArrayList<Triangle>();
        String line = "";
        while( !(line = MDLReader.nextLine(mdl)).contains("\t}") )
        {
//             System.out.println("Interpreting "+line+" for Triangles");
            String [] s = line.split(",");
            s[0] = s[0].substring(4,s[0].length());
            int s_size = MDLReader.occurrencesIn(",",line);
//             System.out.println("We broke it into "+s_size+" parts.");
            s[s_size-1]=s[s_size-1].substring(0,s[s_size-1].length()-2);
            for( int t = 0; t < s_size-1; t+=3  )//s[t+3].equals("")||
            {
                for( int i = 0; i < 3; i++ )
                {
                    s[t+i]=s[t+i].substring(1);
                }
                try
                {
                    output.add(new Triangle(Integer.parseInt(s[t]),Integer.parseInt(s[t+1]),Integer.parseInt(s[t+2]),geoRef) );
                }
                catch (NumberFormatException e)
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
                }
            }
        }
        return output;
    }
    public String toString()
    {
        return m_vertIds[0]+", "+m_vertIds[1]+", "+m_vertIds[2];
    }
    public void flip()
    {
        GeosetVertex tempVert;
        int tempVertId;
        tempVert = m_verts[2];
        tempVertId = m_vertIds[2];
        m_verts[2] = m_verts[1];
        m_vertIds[2] = m_vertIds[1];
        m_verts[1] = tempVert;
        m_vertIds[1] = tempVertId;
    }
}
