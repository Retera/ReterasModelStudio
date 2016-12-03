package com.matrixeater.src;
import java.util.ArrayList;
import java.io.BufferedReader;
import javax.swing.JOptionPane;
import java.io.PrintWriter;
import java.awt.Graphics;
import java.awt.geom.*;
import java.awt.*;
public class Geoset implements Named, VisibilitySource
{
    ExtLog m_extents;
    ArrayList<GeosetVertex> m_vertex;
    ArrayList<Normal> m_normals;
    ArrayList<UVLayer> m_uvlayers;
    ArrayList<Triangle> m_triangle;
    ArrayList<Matrix> m_matrix;
    ArrayList<Animation> m_anims;
    ArrayList<String> m_flags;
    int materialID = 0;
    Material material;
    int selectionGroup = 0;
    
    MDL parentModel;
    
    GeosetAnim geosetAnim = null;
    public Geoset()
    {
        m_vertex = new ArrayList();
        m_matrix = new ArrayList();
        m_triangle = new ArrayList();
        m_normals = new ArrayList();
        m_uvlayers = new ArrayList();
        m_anims = new ArrayList();
        m_flags = new ArrayList();
    }
    public String getName()
    {
        return "Geoset "+(parentModel.getGeosetId(this)+1);//parentModel.getName() + " 
    }
    public void addVertex(GeosetVertex v)
    {
        add(v);
    }
    public void add(GeosetVertex v)
    {
        m_vertex.add(v);
    }
    public GeosetVertex getVertex(int vertId)
    {
        return m_vertex.get(vertId);
    }
    public int getVertexId(GeosetVertex v)
    {
//         int x = 0;
//         for(int i = 0; i < m_vertex.size(); i++ )
//         {
//             if( m_vertex.get(i) == v )
//             {
//                 x = i;
//                 break;
//             }
//         }
//         return x;
        return m_vertex.indexOf(v);
    }
    public void remove(GeosetVertex v)
    {
        m_vertex.remove(v);
    }
    public boolean containsReference(IdObject obj)
    {
    	//boolean does = false;
    	for( int i = 0; i < m_vertex.size(); i++ )
    	{
    		if( m_vertex.get(i).bones.contains(obj) )
    		{
    			return true;
    		}
    	}
    	return false;
    }
    public boolean contains(Triangle t)
    {
        return m_triangle.contains(t);
    }
    public boolean contains(Vertex v)
    {
        return m_vertex.contains(v);
    }
    public int numVerteces()
    {
        return m_vertex.size();
    }
    
    public void addNormal(Normal n)
    {
        m_normals.add(n);
    }
    public Normal getNormal(int vertId)
    {
        return m_normals.get(vertId);
    }
    public int numNormals()
    {
        return m_normals.size();
    }
    
    public void addUVLayer(UVLayer v)
    {
        m_uvlayers.add(v);
    }
    public UVLayer getUVLayer(int id)
    {
        return m_uvlayers.get(id);
    }
    public int numUVLayers()
    {
        return m_uvlayers.size();
    }
    
    public void setTriangles(ArrayList<Triangle> list)
    {
        m_triangle = list;
    }
    public void addTriangle(Triangle p)
    {
        //Left for compat
        add(p);
    }
    public void add(Triangle p)
    {
        m_triangle.add(p);
    }
    public Triangle getTriangle(int triId)
    {
        return m_triangle.get(triId);
    }
    public Triangle [] getTrianglesAll()
    {
        return (Triangle[])m_triangle.toArray();
    }
    /**
     * Returns all vertices that directly inherit motion from the
     * specified Bone, or an empty list if no vertices reference
     * the object.
     * @param parent
     * @return
     */
    public ArrayList<GeosetVertex> getChildrenOf(Bone parent)
    {
    	ArrayList<GeosetVertex> children = new ArrayList<GeosetVertex>();
    	for( GeosetVertex gv: m_vertex )
    	{
    		if( gv.bones.contains(parent) )
    		{
    			children.add(gv);
    		}
    	}
    	return children;
    }
    public int numTriangles()
    {
        return m_triangle.size();
    }
    public void removeTriangle(Triangle t)
    {
        m_triangle.remove(t);
    }
    
    public void addMatrix(Matrix v)
    {
        m_matrix.add(v);
    }
    public Matrix getMatrix(int vertId)
    {
    	if( vertId < 0 )
    	{
            return m_matrix.get(256+vertId);
    	}
        return m_matrix.get(vertId);
    }
    public int numMatrices()
    {
        return m_matrix.size();
    }
    
    public void setMaterialId(int i)
    {
        materialID = i;
    }
    public void setMaterial(Material m)
    {
        material = m;
    }
    public Material getMaterial()
    {
    	return material;
    }
    
    public void setExtLog(ExtLog e)
    {
        m_extents = e;
    }
    public ExtLog getExtLog()
    {
        return m_extents;
    }
    
    public void addAnim(Animation a)
    {
        m_anims.add(a);
    }
    public Animation getAnim(int id)
    {
        return m_anims.get(id);
    }
    public int numAnims()
    {
        return m_anims.size();
    }
    
    public void addFlag(String a)
    {
        m_flags.add(a);
    }
    public String getFlag(int id)
    {
        return m_flags.get(id);
    }
    public int numFlags()
    {
        return m_flags.size();
    }

    public void drawTriangles(Graphics g, Viewport vp)
    {
    	if( MainFrame.panel.viewMode() == 0 || true )
            for( Triangle t: m_triangle )
            {
                double [] x = t.getCoords(vp.getPortFirstXYZ());
                double [] y = t.getCoords(vp.getPortSecondXYZ());
                int [] xint = new int[4];
                int [] yint = new int[4];
                for(int ix = 0; ix < 3; ix++ )
                {
                    xint[ix] = (int)Math.round(vp.convertX(x[ix]));
                    yint[ix] = (int)Math.round(vp.convertY(y[ix]));
                }
                xint[3]=xint[0];
                yint[3]=yint[0];
                g.drawPolyline(xint,yint,4);
            }
    	else if( MainFrame.panel.viewMode() == 1 )
            for( Triangle t: m_triangle )
            {
                double [] x = t.getCoords(vp.getPortFirstXYZ());
                double [] y = t.getCoords(vp.getPortSecondXYZ());
                int [] xint = new int[4];
                int [] yint = new int[4];
                for(int ix = 0; ix < 3; ix++ )
                {
                    xint[ix] = (int)Math.round(vp.convertX(x[ix]));
                    yint[ix] = (int)Math.round(vp.convertY(y[ix]));
                }
                xint[3]=xint[0];
                yint[3]=yint[0];
                g.drawPolyline(xint,yint,4);
                g.fillPolygon(xint,yint,4);
            }
    }
    

    public void drawTriangles(Graphics g, UVViewport vp, int layerId)
    {
    	if( MainFrame.panel.viewMode() == 0 || true )
            for( Triangle t: m_triangle )
            {
                double [] x = t.getTVertCoords((byte)0,layerId);
                double [] y = t.getTVertCoords((byte)1,layerId);
                int [] xint = new int[4];
                int [] yint = new int[4];
                for(int ix = 0; ix < 3; ix++ )
                {
                    xint[ix] = (int)Math.round(vp.convertX(x[ix]));
                    yint[ix] = (int)Math.round(vp.convertY(y[ix]));
                }
                xint[3]=xint[0];
                yint[3]=yint[0];
                g.drawPolyline(xint,yint,4);
            }
    	else if( MainFrame.panel.viewMode() == 1 )
            for( Triangle t: m_triangle )
            {
                double [] x = t.getTVertCoords((byte)0,layerId);
                double [] y = t.getTVertCoords((byte)1,layerId);
                int [] xint = new int[4];
                int [] yint = new int[4];
                for(int ix = 0; ix < 3; ix++ )
                {
                    xint[ix] = (int)Math.round(vp.convertX(x[ix]));
                    yint[ix] = (int)Math.round(vp.convertY(y[ix]));
                }
                xint[3]=xint[0];
                yint[3]=yint[0];
                g.drawPolyline(xint,yint,4);
                g.fillPolygon(xint,yint,4);
            }
    }
    
    public void drawVerteces(Graphics g, Viewport vp, int vertexSize)
    {
        for( Vertex ver: m_vertex )
        {
            g.fillRect((int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ())))-vertexSize,(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ())))-vertexSize,1+vertexSize*2,1+vertexSize*2);
        }
    }
    
    public void drawVerteces(Graphics g, Viewport vp, int vertexSize, ArrayList<Vertex> selection)
    {
    	if( MainFrame.panel.showNormals() ){
            for( GeosetVertex ver: m_vertex )
            {
                Color temp = g.getColor();
                g.setColor(new Color(128,128,255));
                g.drawLine((int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))),(int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ())+ver.normal.getCoord(vp.getPortFirstXYZ())*12/vp.getZoomAmount())),(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ())+ver.normal.getCoord(vp.getPortSecondXYZ())*12/vp.getZoomAmount())));
            }
    	}
        g.setColor(Color.black);
        for( Vertex ver: m_vertex )
        {
            Color temp = g.getColor();
            if( selection.contains(ver) )
            {
                g.setColor(MDLDisplay.selectColor);
            }
            g.fillRect((int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ())))-vertexSize,(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ())))-vertexSize,1+vertexSize*2,1+vertexSize*2);
            if( selection.contains(ver) )
            {
                g.setColor(temp);
            }
        }
    }
    
    public void drawVerteces(Graphics g, UVViewport vp, int vertexSize, int layerId)
    {
        for( int i = 0; i < m_vertex.size(); i++ )
        {
        	TVertex ver = m_vertex.get(i).getTVertex(layerId);
            g.fillRect((int)Math.round(vp.convertX(ver.getCoord(0)))-vertexSize,(int)Math.round(vp.convertY(ver.getCoord(1)))-vertexSize,1+vertexSize*2,1+vertexSize*2);
        }
    }

    public void drawTVerteces(Graphics g, UVViewport vp, int vertexSize, ArrayList<TVertex> selection, int layerId)
    {
        g.setColor(Color.black);
        for( int i = 0; i < m_vertex.size(); i++ )
        {
        	TVertex ver = m_vertex.get(i).getTVertex(layerId);
            Color temp = g.getColor();
            if( selection.contains(ver) )
            {
                g.setColor(MDLDisplay.selectColor);
            }
            g.fillRect((int)Math.round(vp.convertX(ver.getCoord(0)))-vertexSize,(int)Math.round(vp.convertY(ver.getCoord(1)))-vertexSize,1+vertexSize*2,1+vertexSize*2);
            if( selection.contains(ver) )
            {
                g.setColor(temp);
            }
        }
    }
    
    public ArrayList<TVertex> getTVertecesInArea(Rectangle2D.Double area, int layerId)
    {
        ArrayList<TVertex> temp = new ArrayList<TVertex>();
        for( int i = 0; i < m_vertex.size(); i++ )
        {
        	TVertex ver = m_vertex.get(i).getTVertex(layerId);
//             Point2D.Double p = new Point(ver.getCoords(dim1),ver.getCoords(dim2))
            if( area.contains(ver.getX(),ver.getY()) )
            {
                temp.add(ver);
            }
        }
        return temp;
    }
    
    public ArrayList<Vertex> getVertecesInArea(Rectangle2D.Double area, byte dim1, byte dim2)
    {
        ArrayList<Vertex> temp = new ArrayList<Vertex>();
        for( Vertex ver: m_vertex )
        {
//             Point2D.Double p = new Point(ver.getCoords(dim1),ver.getCoords(dim2))
            if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) )
            {
                temp.add(ver);
            }
        }
        return temp;
    }
    
    public static Geoset read(BufferedReader mdl)
    {
        String line = MDLReader.nextLine(mdl);
        System.out.println("geo begins with "+line);
        if( line.contains("Geoset") )
        {
            line = MDLReader.nextLine(mdl);
            Geoset geo = new Geoset();
            if( !line.contains("Vertices") )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: Vertices not found at beginning of Geoset!");
            }
            while( !((line = MDLReader.nextLine(mdl)).contains("\t}") ) )
            {
                geo.addVertex(GeosetVertex.parseText(line));
            }
            line = MDLReader.nextLine(mdl);
            if( line.contains("Normals") )
            {
                //If we have normals:
                while( !((line = MDLReader.nextLine(mdl)).contains("\t}") ) )
                {
                    geo.addNormal(Normal.parseText(line));
                }
            }
            while( ((line = MDLReader.nextLine(mdl)).contains("TVertices") ) )
            {
                geo.addUVLayer(UVLayer.read(mdl));
            }
            if( !line.contains("VertexGroup") )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: VertexGroups missing or invalid!");
            }
            int i = 0;
            while( !((line = MDLReader.nextLine(mdl)).contains("\t}") ) )
            {
                geo.getVertex(i).setVertexGroup(MDLReader.readInt(line));
                i++;
            }
            line = MDLReader.nextLine(mdl);
            if( !line.contains("Faces") )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: Faces missing or invalid!");
            }
            line = MDLReader.nextLine(mdl);
            if( !line.contains("Triangles") )
            {
                System.out.println(line);
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: Triangles missing or invalid!");
            }
            geo.setTriangles(Triangle.read(mdl,geo));
            line = MDLReader.nextLine(mdl);//Throw away the \t} closer for faces
            line = MDLReader.nextLine(mdl);
            if( !line.contains("Groups") )
            {
                JOptionPane.showMessageDialog(MainFrame.panel,"Error: Groups (Matrices) missing or invalid!");
            }
            while( !((line = MDLReader.nextLine(mdl)).contains("\t}") ) )
            {
                geo.addMatrix(Matrix.parseText(line));
            }
            MDLReader.mark(mdl);
            line = MDLReader.nextLine(mdl);
            while( !line.contains("}")  || line.contains("},") )
            {
                if( line.contains("Extent") || line.contains("BoundsRadius") )
                {
                    System.out.println("Parsing geoset extLog:"+line);
                    MDLReader.reset(mdl);
                    geo.setExtLog(ExtLog.read(mdl));
                    System.out.println("Completed geoset extLog.");
                }
                else if( line.contains("Anim") )
                {
                    MDLReader.reset(mdl);
                    geo.addAnim(Animation.read(mdl));
                    MDLReader.mark(mdl);
                }
                else if( line.contains("MaterialID") )
                {
                    geo.materialID = MDLReader.readInt(line);
                    MDLReader.mark(mdl);
                }
                else if( line.contains("SelectionGroup") )
                {
                    geo.selectionGroup = MDLReader.readInt(line);
                    MDLReader.mark(mdl);
                }
                else
                {
                    geo.addFlag(MDLReader.readFlag(line));
                    System.out.println("Reading to geoFlag: "+line);
                    MDLReader.mark(mdl);
                }
                line = MDLReader.nextLine(mdl);
            }
//             JOptionPane.showMessageDialog(MainFrame.panel,"Geoset reading completed!");
            System.out.println("Geoset reading completed!");
            
            return geo;
        }
        else
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Unable to parse Geoset: Missing or unrecognized open statement '"+line+"'.");
        }
        return null;
    }
    public void updateToObjects(MDL mdlr)
    {
        //upload the temporary UVLayer and Matrix objects into the vertices themselves
        int sz = numVerteces();
        for( Matrix m: m_matrix )
        {
            m.updateBones(mdlr);
        }
        for( int i = 0; i < sz; i++ )
        {
            GeosetVertex gv = m_vertex.get(i);
            gv.clearTVerts();
            int szuv = m_uvlayers.size();
            for( int l = 0; l < szuv; l++ )
            {
                try {
                    gv.addTVertex(m_uvlayers.get(l).getTVertex(i));
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(MainFrame.panel,"Error: Length of TVertices and Vertices chunk differ (Or some other unknown error has occurred)!");
                }
            }
            Matrix mx = getMatrix(gv.getVertexGroup());
            int szmx = mx.size();
            gv.clearBoneAttachments();
            for( int m = 0; m < szmx; m++ )
            {
                gv.addBoneAttachment((Bone)mdlr.getIdObject(mx.getBoneId(m)));
            }
            gv.setNormal(m_normals.get(i));
            for( Triangle t: m_triangle )
            {
                if( t.containsRef(gv) )
                {
                    gv.triangles.add(t);
                }
            }
            gv.geoset = this;
//             gv.addBoneAttachment(null);//Why was this here?
        }
        try {
            material = mdlr.getMaterial(materialID);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            JOptionPane.showMessageDialog(null,"Error: Material index out of bounds for geoset!");
        }
        parentModel = mdlr;
    }
    public void applyMatricesToVertices(MDL mdlr)
    {
        int sz = numVerteces();
        for( int i = 0; i < sz; i++ )
        {
            GeosetVertex gv = m_vertex.get(i);
            gv.clearBoneAttachments();
            Matrix mx = getMatrix(gv.getVertexGroup());
            mx.updateIds(mdlr);
            int szmx = mx.size();
            for( int m = 0; m < szmx; m++ )
            {
                gv.addBoneAttachment((Bone)mdlr.getIdObject(mx.getBoneId(m)));
            }
        }
    }
    public void applyVerticesToMatrices(MDL mdlr)
    {
        m_matrix.clear();
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            Matrix newTemp = new Matrix(m_vertex.get(i).bones);
            boolean newMatrix = true;
            for( int m = 0; m < m_matrix.size() && newMatrix; m++ )
            {
                if( newTemp.equals(m_matrix.get(m)) )
                {
                    newTemp = m_matrix.get(m);
                    newMatrix = false;
                }
            }
            if( newMatrix )
            {
                m_matrix.add(newTemp);
                newTemp.updateIds(mdlr);
            }
            m_vertex.get(i).VertexGroup = m_matrix.indexOf(newTemp);
            m_vertex.get(i).setMatrix(newTemp);
        }
    }
    public void purifyFaces()
    {
        for( int i =  m_triangle.size()-1; i >= 0; i-- )
        {
            Triangle tri = m_triangle.get(i);
            for( int ix = 0; ix < m_triangle.size(); ix++ )
            {
                Triangle trix = m_triangle.get(ix);
                if( trix != tri )
                {
                    if( trix.equalRefsNoIds(tri) )//Changed this from "sameVerts" -- this means that
                    		 				      // triangles with the same vertices but in a different order will no
                    						      // longer be purged automatically.
                    {
                        m_triangle.remove(tri);
                        break;
                    }
                }
            }
        }
    }
    public boolean isEmpty()
    {
    	return m_vertex.size() <= 0;
    }
    public void printTo(PrintWriter writer, MDL mdlr, boolean trianglesTogether)
    {
        purifyFaces();
        writer.println("Geoset {");
        writer.println("\tVertices "+m_vertex.size()+" {");
        
        String tabs = "\t\t";
        //Normals cleared here, in case that becomes a problem later.
        m_normals.clear();
        //UV Layers cleared here
        m_uvlayers.clear();
        int bigNum = 0;
        int littleNum = -1;
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            int temp = m_vertex.get(i).tverts.size();
            if( temp > bigNum )
            {
                bigNum = temp;
            }
            if( littleNum == -1 || temp < littleNum )
            {
                littleNum = temp;
            }
        }
        if( littleNum != bigNum )
        {
            JOptionPane.showMessageDialog(null,"Error: Attempting to save a Geoset with Verteces that have differing numbers of TVertices! Empty TVertices will be autogenerated.");
        }
        for( int i = 0; i < bigNum; i++ )
        {
            m_uvlayers.add(new UVLayer());
        }
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            writer.println(tabs+m_vertex.get(i).toString()+",");
            m_normals.add(m_vertex.get(i).getNormal());
            for( int uv = 0; uv < bigNum; uv++ )
            {
            	try {
                    TVertex temp = m_vertex.get(i).getTVertex(uv);
                    if( temp != null )
                    {
                        m_uvlayers.get(uv).addTVertex(temp);
                    }
                    else
                    {
                        m_uvlayers.get(uv).addTVertex(new TVertex(0,0));
                    }
            	}
            	catch (IndexOutOfBoundsException e)
            	{
                    m_uvlayers.get(uv).addTVertex(new TVertex(0,0));
            	}
            }
        }
        writer.println("\t}");
        writer.println("\tNormals "+m_normals.size()+" {");
        for( int i = 0; i < m_normals.size(); i++ )
        {
            writer.println(tabs+m_normals.get(i).toString()+",");
        }
        writer.println("\t}");
        for( int i = 0; i < m_uvlayers.size(); i++ )
        {
            m_uvlayers.get(i).printTo(writer,1,true);
        }
        //Clearing matrix list
        m_matrix.clear();
        writer.println("\tVertexGroup {");
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            Matrix newTemp = new Matrix(m_vertex.get(i).bones);
            boolean newMatrix = true;
            for( int m = 0; m < m_matrix.size() && newMatrix; m++ )
            {
                if( newTemp.equals(m_matrix.get(m)) )
                {
                    newTemp = m_matrix.get(m);
                    newMatrix = false;
                }
            }
            if( newMatrix )
            {
                m_matrix.add(newTemp);
                newTemp.updateIds(mdlr);
            }
            m_vertex.get(i).VertexGroup = m_matrix.indexOf(newTemp);
            m_vertex.get(i).setMatrix(newTemp);
            writer.println(tabs+m_vertex.get(i).VertexGroup+",");
        }
        writer.println("\t}");
        if( trianglesTogether )
        {
            writer.println("\tFaces 1 "+(m_triangle.size()*3)+" {");
            writer.println("\t\tTriangles {");
            String triangleOut = "\t\t\t{ ";
            for( int i = 0; i < m_triangle.size(); i++ )
            {
                m_triangle.get(i).updateVertexIds(this);
                if( i != m_triangle.size()-1 )
                {
                    triangleOut = triangleOut + m_triangle.get(i).toString() + ", ";
                }
                else
                {
                    triangleOut = triangleOut + m_triangle.get(i).toString() + " ";
                }
            }
            writer.println(triangleOut + "},");
            writer.println("\t\t}");
        }
        else
        {
            writer.println("\tFaces "+m_triangle.size()+" "+(m_triangle.size()*3)+" {");
            writer.println("\t\tTriangles {");
            String triangleOut = "\t\t\t{ ";
            for( int i = 0; i < m_triangle.size(); i++ )
            {
                m_triangle.get(i).updateVertexIds(this);
                writer.println(triangleOut + m_triangle.get(i).toString() + " },");
            }
            writer.println("\t\t}");
        }
        writer.println("\t}");
        int boneRefCount = 0;
        for( int i = 0; i < m_matrix.size(); i++ )
        {
            boneRefCount += m_matrix.get(i).bones.size();
        }
        writer.println("\tGroups "+m_matrix.size()+" "+boneRefCount+" {");
        for( int i = 0; i < m_matrix.size(); i++ )
        {
            m_matrix.get(i).updateIds(mdlr);
            m_matrix.get(i).printTo(writer,2);//2 is the tab height
        }
        writer.println("\t}");
        if( m_extents != null )
        	m_extents.printTo(writer,1);
        for( int i = 0; i < m_anims.size(); i++ )
        {
            m_anims.get(i).printTo(writer,1);
        }
        
        writer.println("\tMaterialID "+materialID+",");
        writer.println("\tSelectionGroup "+selectionGroup+",");
        for( int i = 0; i < m_flags.size(); i++ )
        {
            writer.println("\t"+m_flags.get(i)+",");
        }
        
        
        
        writer.println("}");
    }
    public void doSavePrep(MDL mdlr)
    {
        purifyFaces();
        
        //Normals cleared here, in case that becomes a problem later.
        m_normals.clear();
        //UV Layers cleared here
        m_uvlayers.clear();
        int bigNum = 0;
        int littleNum = -1;
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            int temp = m_vertex.get(i).tverts.size();
            if( temp > bigNum )
            {
                bigNum = temp;
            }
            if( littleNum == -1 || temp < littleNum )
            {
                littleNum = temp;
            }
        }
        if( littleNum != bigNum )
        {
            JOptionPane.showMessageDialog(null,"Error: Attempting to save a Geoset with Verteces that have differing numbers of TVertices! Empty TVertices will be autogenerated.");
        }
        for( int i = 0; i < bigNum; i++ )
        {
            m_uvlayers.add(new UVLayer());
        }
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            m_normals.add(m_vertex.get(i).getNormal());
            for( int uv = 0; uv < bigNum; uv++ )
            {
                TVertex temp = m_vertex.get(i).getTVertex(uv);
                if( temp != null )
                {
                    m_uvlayers.get(uv).addTVertex(temp);
                }
                else
                {
                    m_uvlayers.get(uv).addTVertex(new TVertex(0,0));
                }
            }
        }
        //Clearing matrix list
        m_matrix.clear();
        for( int i = 0; i < m_vertex.size(); i++ )
        {
            Matrix newTemp = new Matrix(m_vertex.get(i).bones);
            boolean newMatrix = true;
            for( int m = 0; m < m_matrix.size() && newMatrix; m++ )
            {
                if( newTemp.equals(m_matrix.get(m)) )
                {
                    newTemp = m_matrix.get(m);
                    newMatrix = false;
                }
            }
            if( newMatrix )
            {
                m_matrix.add(newTemp);
                newTemp.updateIds(mdlr);
            }
            m_vertex.get(i).VertexGroup = m_matrix.indexOf(newTemp);
            m_vertex.get(i).setMatrix(newTemp);
        }
        for( int i = 0; i < m_triangle.size(); i++ )
        {
            m_triangle.get(i).updateVertexIds(this);
        }
        int boneRefCount = 0;
        for( int i = 0; i < m_matrix.size(); i++ )
        {
            boneRefCount += m_matrix.get(i).bones.size();
        }
        for( int i = 0; i < m_matrix.size(); i++ )
        {
            m_matrix.get(i).updateIds(mdlr);
        }
    }
    public GeosetAnim forceGetGeosetAnim()
    {
        if( geosetAnim == null )
        {
            geosetAnim = new GeosetAnim(this);
            parentModel.add(geosetAnim);
        }
        return geosetAnim;
    }
    public void setVisibilityFlag(AnimFlag a)
    {
        if( a != null )
        forceGetGeosetAnim().setVisibilityFlag(a);
    }
    public AnimFlag getVisibilityFlag()
    {
        if( geosetAnim != null )
        return geosetAnim.getVisibilityFlag();
        return null;
    }
    public String visFlagName()
    {
        return "Alpha";
    }
}
