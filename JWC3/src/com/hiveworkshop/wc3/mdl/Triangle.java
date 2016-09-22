package com.hiveworkshop.wc3.mdl;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
public class Triangle
{
    GeosetVertex [] verts = new GeosetVertex[3];
    int [] vertIds = new int[3];
    Geoset geoset;
    public Triangle( GeosetVertex a, GeosetVertex b, GeosetVertex c, Geoset geoRef )
    {
        verts[0] = a;
        verts[1] = b;
        verts[2] = c;
        geoset = geoRef;
    }
    public Triangle( int a, int b, int c, Geoset geoRef )
    {
        vertIds[0] = a;
        vertIds[1] = b;
        vertIds[2] = c;
        verts[0] = geoRef.getVertex(a);
        verts[1] = geoRef.getVertex(b);
        verts[2] = geoRef.getVertex(c);
        geoset = geoRef;
    }
    public Triangle( GeosetVertex a, GeosetVertex b, GeosetVertex c)
    {
        verts[0] = a;
        verts[1] = b;
        verts[2] = c;
        geoset = null;
    }
    public Triangle( int a, int b, int c)
    {
        vertIds[0] = a;
        vertIds[1] = b;
        vertIds[2] = c;
//         m_verts[0] = geoRef.getVertex(a);
//         m_verts[1] = geoRef.getVertex(b);
//         m_verts[2] = geoRef.getVertex(c);
        geoset = null;
    }
    public void setGeoRef(Geoset geoRef)
    {
        geoset = geoRef;
    }
    public void updateVertexRefs()
    {
        verts[0] = geoset.getVertex(vertIds[0]);
        verts[1] = geoset.getVertex(vertIds[1]);
        verts[2] = geoset.getVertex(vertIds[2]);
    }
    public void updateVertexIds()
    {
        //Potentially this procedure could lag a bunch in the way I wrote it, but it will
        // change vertex ids to match a changed geoset, assuming the geoset still contains the 
        // vertex
        vertIds[0] = geoset.getVertexId(verts[0]);
        vertIds[1] = geoset.getVertexId(verts[1]);
        vertIds[2] = geoset.getVertexId(verts[2]);
    }
    public void forceVertsUpdate()
    {
        if( !verts[0].triangles.contains(this) )
        verts[0].triangles.add(this);
        if( !verts[1].triangles.contains(this) )
        verts[1].triangles.add(this);
        if( !verts[2].triangles.contains(this) )
        verts[2].triangles.add(this);
    }
    public void updateVertexIds(Geoset geoRef)
    {
        geoset = geoRef;
        updateVertexIds();
    }
    public void updateVertexRefs(ArrayList<GeosetVertex> list)
    {
        verts[0] = list.get(vertIds[0]);
        verts[1] = list.get(vertIds[1]);
        verts[2] = list.get(vertIds[2]);
    }
    public boolean containsRef(GeosetVertex v)
    {
        return verts[0] == v || verts[1] == v || verts[2] == v;
    }
    public boolean contains(GeosetVertex v)
    {
        return verts[0].equalLocs(v) || verts[1].equalLocs(v) || verts[2].equalLocs(v);
    }
    public GeosetVertex get(int index)
    {
        return verts[index];
    }
    public int getId(int index) {
    	return vertIds[index];
    }
    public void set(int index, GeosetVertex v)
    {
        verts[index] = v;
        vertIds[index] = geoset.getVertexId(v);
    }
    public int indexOf(GeosetVertex v)
    {
        int out = -1;
        for( int i = 0; i < verts.length && out == -1; i++ )
        {
            if( verts[i].equalLocs(v) )
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
            if( !t.verts[i].equalLocs(verts[i]) || t.vertIds[i] != vertIds[i] )
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
            if( !contains(t.verts[i]) )
            {
                equal = false;
            }
        }
        return equal;
    }
    public int indexOfRef(GeosetVertex v)
    {
        int out = -1;
        for( int i = 0; i < verts.length && out == -1; i++ )
        {
            if( verts[i] == v )
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
            if( t.verts[i] != verts[i] )
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
            if( t.verts[i] != verts[i] || t.vertIds[i] != vertIds[i] )
            {
                equal = false;
            }
        }
        return equal;
    }
    public GeosetVertex [] getAll()
    {
        return verts;
    }
    public int [] getIntCoords( byte dim )
    {
        int [] output = new int[3];
        for( int i = 0; i < 3; i++ )
        {
            output[i] = (int)(verts[i].getCoord(dim));
        }
        return output;
    }
    public double [] getCoords( byte dim )
    {
        double [] output = new double[3];
        for( int i = 0; i < 3; i++ )
        {
            output[i] = (verts[i].getCoord(dim));
        }
        return output;
    }
    public double [] getTVertCoords( byte dim, int layerId )
    {
        double [] output = new double[3];
        for( int i = 0; i < 3; i++ )
        {
            output[i] = (verts[i].getTVertex(layerId).getCoord(dim));
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
                    JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
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
                    JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
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
                    JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Unable to interpret information in Triangles: "+s[t]+", "+s[t+1]+", or "+s[t+2]);
                }
            }
        }
        return output;
    }
    @Override
	public String toString()
    {
        return vertIds[0]+", "+vertIds[1]+", "+vertIds[2];
    }
    public void flip()
    {
        GeosetVertex tempVert;
        int tempVertId;
        tempVert = verts[2];
        tempVertId = vertIds[2];
        verts[2] = verts[1];
        vertIds[2] = vertIds[1];
        verts[1] = tempVert;
        vertIds[1] = tempVertId;
    }
	public Geoset getGeoset() {
		return geoset;
	}
	public void setGeoset(Geoset geoset) {
		this.geoset = geoset;
	}
	public GeosetVertex[] getVerts() {
		return verts;
	}
	public void setVerts(GeosetVertex[] verts) {
		this.verts = verts;
	}
	public int[] getVertIds() {
		return vertIds;
	}
	public void setVertIds(int[] vertIds) {
		this.vertIds = vertIds;
	}
}
