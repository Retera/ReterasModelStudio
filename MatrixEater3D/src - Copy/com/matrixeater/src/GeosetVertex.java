package com.matrixeater.src;
import javax.swing.JOptionPane;
import java.util.ArrayList;
/**
 * GeosetVertex is a extended version of the Vertex class, for use strictly inside of Geosets.
 * The idea is that a Vertex object is used all over this program for any sort of point in
 * 3d space (PivotPoint, Min/max extents, data in translations and scaling) and is strictly
 * three connected double values, while a GeosetVertex is an object that has many additional
 * useful parts for a Geoset
 * 
 * Eric Theller
 * 3/9/2012
 */
public class GeosetVertex extends Vertex
{
    Matrix matrixRef;
    Normal normal;
    public int VertexGroup;
    ArrayList<TVertex> tverts = new ArrayList<TVertex>();
    ArrayList<Bone> bones = new ArrayList<Bone>();
    ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    Geoset geoset;
    public GeosetVertex( double x, double y, double z )
    {
        super(x,y,z);
    }
    public GeosetVertex( double x, double y, double z, Normal n )
    {
        super(x,y,z);
        normal = n;
    }
    public GeosetVertex( GeosetVertex old)
    {
        super( old.x, old.y, old.z );
        this.normal = new Normal(old.normal);
        this.bones = new ArrayList<Bone>(old.bones);
        this.tverts = new ArrayList<TVertex>();
        for( TVertex tv: old.tverts )
        {
            tverts.add( new TVertex(tv) );
        }
        //odd, but when writing
        this.geoset = old.geoset;
    }
    public void addTVertex(TVertex v)
    {
        tverts.add(v);
    }
    public TVertex getTVertex(int i)
    {
        try {
            return tverts.get(i);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }
    public void setVertexGroup( int k )
    {
        VertexGroup = k;
    }
    public int getVertexGroup()
    {
        return VertexGroup;
    }
    public void clearBoneAttachments()
    {
        bones.clear();
    }
    public void clearTVerts()
    {
    	tverts.clear();
    }
    public void addBoneAttachment(Bone b)
    {
        bones.add(b);
    }
    public void addBoneAttachments(ArrayList<Bone> b)
    {
        bones.addAll(b);
    }
    public void updateMatrixRef(ArrayList<Matrix> list)
    {
        try
        {
            matrixRef = list.get(VertexGroup);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error in Matrices: VertexGroup does not reference a real matrix id!");
        }
    }
    public void setMatrix(Matrix ref)
    {
        matrixRef = ref;
    }
    public void setNormal(Normal n)
    {
        normal = n;
    }
    public Normal getNormal()
    {
        return normal;
    }
    public static GeosetVertex parseText(String input)
    {
        String [] entries = input.split(",");
        GeosetVertex temp = null;
        double x = 0;
        double y = 0;
        double z = 0;
        try
        {
            x = Double.parseDouble(entries[0].split("\\{")[1]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        try
        {
            y = Double.parseDouble(entries[1]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        try
        {
            z = Double.parseDouble(entries[2].split("}")[0]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: Vertex coordinates could not be interpreted.");
        }
        temp = new GeosetVertex(x,y,z);
        return temp;
    }
}
