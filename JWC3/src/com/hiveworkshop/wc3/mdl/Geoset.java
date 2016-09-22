package com.hiveworkshop.wc3.mdl;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdx.GeosetChunk;
public class Geoset implements Named, VisibilitySource
{
    ExtLog extents;
    ArrayList<GeosetVertex> vertex;
    ArrayList<Normal> normals;
    ArrayList<UVLayer> uvlayers;
    ArrayList<Triangle> triangle;
    ArrayList<Matrix> matrix;
    ArrayList<Animation> anims;
    ArrayList<String> flags;
    int materialID = 0;
    Material material;
    int selectionGroup = 0;
    
    MDL parentModel;
    
    GeosetAnim geosetAnim = null;
    public Geoset()
    {
        vertex = new ArrayList();
        matrix = new ArrayList();
        triangle = new ArrayList();
        normals = new ArrayList();
        uvlayers = new ArrayList();
        anims = new ArrayList();
        flags = new ArrayList();
    }
    public Geoset(GeosetChunk.Geoset mdxGeo) {
    	this();
		setExtLog(new ExtLog(mdxGeo.minimumExtent, mdxGeo.maximumExtent, mdxGeo.boundsRadius));
		for( GeosetChunk.Geoset.Extent ext: mdxGeo.extent ) {
			ExtLog extents = new ExtLog(ext);
			Animation anim = new Animation(extents);
			add(anim);
		}
		
		setMaterialID(mdxGeo.materialId);
		ArrayList<UVLayer> uv = new ArrayList<UVLayer>();
		for( int i = 0; i < mdxGeo.nrOfTextureVertexGroups; i++ ) {
			UVLayer layer = new UVLayer();
			uv.add(layer);
			addUVLayer(layer);
		}
		
		int nVertices = mdxGeo.vertexPositions.length / 3;
		for( int k = 0; k < nVertices; k++ ) {
			int i = k * 3;
			int j = k * 2;
			GeosetVertex gv;
			add(gv = new GeosetVertex(
					mdxGeo.vertexPositions[i],
					mdxGeo.vertexPositions[i+1],
					mdxGeo.vertexPositions[i+2]));
			gv.setVertexGroup((256+mdxGeo.vertexGroups[k])%256); // this is a byte, the other guys java code will read as signed
			addNormal(new Normal(
					mdxGeo.vertexNormals[i],
					mdxGeo.vertexNormals[i+1],
					mdxGeo.vertexNormals[i+2]));
			
			for( int uvId = 0; uvId < uv.size(); uvId++ ) {
				uv.get(uvId).addTVertex(new TVertex(
						mdxGeo.vertexTexturePositions[uvId][j],
						mdxGeo.vertexTexturePositions[uvId][j + 1]));
			}
		}
		// guys I didn't code this to allow experimental
		// non-triangle faces that were suggested to exist
		// on the web (i.e. quads).
		// if you wanted to fix that, you'd want to do it below
		for( int i = 0; i < mdxGeo.faces.length; i+=3 ) {
			Triangle triangle = new Triangle(mdxGeo.faces[i+0], mdxGeo.faces[i+1], mdxGeo.faces[i+2], this);
			add(triangle);
		}
		if( mdxGeo.selectionType == 4 ) {
			addFlag("Unselectable");
		}
		setSelectionGroup(mdxGeo.selectionGroup);
		int index = 0;
		for( int size: mdxGeo.matrixGroups ) {
			Matrix m = new Matrix();
			for( int i = 0; i < size; i++ ) {
				m.addId(mdxGeo.matrixIndexs[index++]);
			}
			addMatrix(m);
		}
    }
    @Override
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
        vertex.add(v);
    }
    public GeosetVertex getVertex(int vertId)
    {
        return vertex.get(vertId);
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
        return vertex.indexOf(v);
    }
    public void remove(GeosetVertex v)
    {
        vertex.remove(v);
    }
    public boolean containsReference(IdObject obj)
    {
    	//boolean does = false;
    	for( int i = 0; i < vertex.size(); i++ )
    	{
    		if( vertex.get(i).bones.contains(obj) )
    		{
    			return true;
    		}
    	}
    	return false;
    }
    public boolean contains(Triangle t)
    {
        return triangle.contains(t);
    }
    public boolean contains(Vertex v)
    {
        return vertex.contains(v);
    }
    public int numVerteces()
    {
        return vertex.size();
    }
    
    public void addNormal(Normal n)
    {
        normals.add(n);
    }
    public Normal getNormal(int vertId)
    {
        return normals.get(vertId);
    }
    public int numNormals()
    {
        return normals.size();
    }
    
    public void addUVLayer(UVLayer v)
    {
        uvlayers.add(v);
    }
    public UVLayer getUVLayer(int id)
    {
        return uvlayers.get(id);
    }
    public int numUVLayers()
    {
        return uvlayers.size();
    }
    
    public void setTriangles(ArrayList<Triangle> list)
    {
        triangle = list;
    }
    public void addTriangle(Triangle p)
    {
        //Left for compat
        add(p);
    }
    public void add(Triangle p)
    {
        triangle.add(p);
    }
    public Triangle getTriangle(int triId)
    {
        return triangle.get(triId);
    }
    public Triangle [] getTrianglesAll()
    {
        return (Triangle[])triangle.toArray();
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
    	for( GeosetVertex gv: vertex )
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
        return triangle.size();
    }
    public void removeTriangle(Triangle t)
    {
        triangle.remove(t);
    }
    
    public void addMatrix(Matrix v)
    {
        matrix.add(v);
    }
    public Matrix getMatrix(int vertId)
    {
    	if( vertId < 0 )
    	{
            return matrix.get(256+vertId);
    	}
        return matrix.get(vertId);
    }
    public int numMatrices()
    {
        return matrix.size();
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
        extents = e;
    }
    public ExtLog getExtLog()
    {
        return extents;
    }
    
    public void add(Animation a)
    {
        anims.add(a);
    }
    public Animation getAnim(int id)
    {
        return anims.get(id);
    }
    public int numAnims()
    {
        return anims.size();
    }
    
    public void addFlag(String a)
    {
        flags.add(a);
    }
    public String getFlag(int id)
    {
        return flags.get(id);
    }
    public int numFlags()
    {
        return flags.size();
    }
    
    public ArrayList<TVertex> getTVertecesInArea(Rectangle2D.Double area, int layerId)
    {
        ArrayList<TVertex> temp = new ArrayList<TVertex>();
        for( int i = 0; i < vertex.size(); i++ )
        {
        	TVertex ver = vertex.get(i).getTVertex(layerId);
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
        for( Vertex ver: vertex )
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
                JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Vertices not found at beginning of Geoset!");
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
                JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: VertexGroups missing or invalid!");
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
                JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Faces missing or invalid!");
            }
            line = MDLReader.nextLine(mdl);
            if( !line.contains("Triangles") )
            {
                System.out.println(line);
                JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Triangles missing or invalid!");
            }
            geo.setTriangles(Triangle.read(mdl,geo));
            line = MDLReader.nextLine(mdl);//Throw away the \t} closer for faces
            line = MDLReader.nextLine(mdl);
            if( !line.contains("Groups") )
            {
                JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Groups (Matrices) missing or invalid!");
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
                    geo.add(Animation.read(mdl));
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
//             JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Geoset reading completed!");
            System.out.println("Geoset reading completed!");
            
            return geo;
        }
        else
        {
            JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Unable to parse Geoset: Missing or unrecognized open statement '"+line+"'.");
        }
        return null;
    }
    public void updateToObjects(MDL mdlr)
    {
        //upload the temporary UVLayer and Matrix objects into the vertices themselves
        int sz = numVerteces();
        for( Matrix m: matrix )
        {
            m.updateBones(mdlr);
        }
        for( int i = 0; i < sz; i++ )
        {
            GeosetVertex gv = vertex.get(i);
            gv.clearTVerts();
            int szuv = uvlayers.size();
            for( int l = 0; l < szuv; l++ )
            {
                try {
                    gv.addTVertex(uvlayers.get(l).getTVertex(i));
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error: Length of TVertices and Vertices chunk differ (Or some other unknown error has occurred)!");
                }
            }
            Matrix mx = getMatrix(gv.getVertexGroup());
            int szmx = mx.size();
            gv.clearBoneAttachments();
            for( int m = 0; m < szmx; m++ )
            {
                gv.addBoneAttachment((Bone)mdlr.getIdObject(mx.getBoneId(m)));
            }
            gv.setNormal(normals.get(i));
            for( Triangle t: triangle )
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
            GeosetVertex gv = vertex.get(i);
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
        matrix.clear();
        for( int i = 0; i < vertex.size(); i++ )
        {
            Matrix newTemp = new Matrix(vertex.get(i).bones);
            boolean newMatrix = true;
            for( int m = 0; m < matrix.size() && newMatrix; m++ )
            {
                if( newTemp.equals(matrix.get(m)) )
                {
                    newTemp = matrix.get(m);
                    newMatrix = false;
                }
            }
            if( newMatrix )
            {
                matrix.add(newTemp);
                newTemp.updateIds(mdlr);
            }
            vertex.get(i).VertexGroup = matrix.indexOf(newTemp);
            vertex.get(i).setMatrix(newTemp);
        }
    }
    public void purifyFaces()
    {
        for( int i =  triangle.size()-1; i >= 0; i-- )
        {
            Triangle tri = triangle.get(i);
            for( int ix = 0; ix < triangle.size(); ix++ )
            {
                Triangle trix = triangle.get(ix);
                if( trix != tri )
                {
                    if( trix.equalRefsNoIds(tri) )//Changed this from "sameVerts" -- this means that
                    		 				      // triangles with the same vertices but in a different order will no
                    						      // longer be purged automatically.
                    {
                        triangle.remove(tri);
                        break;
                    }
                }
            }
        }
    }
    public boolean isEmpty()
    {
    	return vertex.size() <= 0;
    }
    public void printTo(PrintWriter writer, MDL mdlr, boolean trianglesTogether)
    {
        purifyFaces();
        writer.println("Geoset {");
        writer.println("\tVertices "+vertex.size()+" {");
        
        String tabs = "\t\t";
        //Normals cleared here, in case that becomes a problem later.
        normals.clear();
        //UV Layers cleared here
        uvlayers.clear();
        int bigNum = 0;
        int littleNum = -1;
        for( int i = 0; i < vertex.size(); i++ )
        {
            int temp = vertex.get(i).tverts.size();
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
            uvlayers.add(new UVLayer());
        }
        for( int i = 0; i < vertex.size(); i++ )
        {
            writer.println(tabs+vertex.get(i).toString()+",");
            normals.add(vertex.get(i).getNormal());
            for( int uv = 0; uv < bigNum; uv++ )
            {
            	try {
                    TVertex temp = vertex.get(i).getTVertex(uv);
                    if( temp != null )
                    {
                        uvlayers.get(uv).addTVertex(temp);
                    }
                    else
                    {
                        uvlayers.get(uv).addTVertex(new TVertex(0,0));
                    }
            	}
            	catch (IndexOutOfBoundsException e)
            	{
                    uvlayers.get(uv).addTVertex(new TVertex(0,0));
            	}
            }
        }
        writer.println("\t}");
        writer.println("\tNormals "+normals.size()+" {");
        for( int i = 0; i < normals.size(); i++ )
        {
            writer.println(tabs+normals.get(i).toString()+",");
        }
        writer.println("\t}");
        for( int i = 0; i < uvlayers.size(); i++ )
        {
            uvlayers.get(i).printTo(writer,1,true);
        }
        //Clearing matrix list
        matrix.clear();
        writer.println("\tVertexGroup {");
        for( int i = 0; i < vertex.size(); i++ )
        {
            Matrix newTemp = new Matrix(vertex.get(i).bones);
            boolean newMatrix = true;
            for( int m = 0; m < matrix.size() && newMatrix; m++ )
            {
                if( newTemp.equals(matrix.get(m)) )
                {
                    newTemp = matrix.get(m);
                    newMatrix = false;
                }
            }
            if( newMatrix )
            {
                matrix.add(newTemp);
                newTemp.updateIds(mdlr);
            }
            vertex.get(i).VertexGroup = matrix.indexOf(newTemp);
            vertex.get(i).setMatrix(newTemp);
            writer.println(tabs+vertex.get(i).VertexGroup+",");
        }
        writer.println("\t}");
        if( trianglesTogether )
        {
            writer.println("\tFaces 1 "+(triangle.size()*3)+" {");
            writer.println("\t\tTriangles {");
            String triangleOut = "\t\t\t{ ";
            for( int i = 0; i < triangle.size(); i++ )
            {
                triangle.get(i).updateVertexIds(this);
                if( i != triangle.size()-1 )
                {
                    triangleOut = triangleOut + triangle.get(i).toString() + ", ";
                }
                else
                {
                    triangleOut = triangleOut + triangle.get(i).toString() + " ";
                }
            }
            writer.println(triangleOut + "},");
            writer.println("\t\t}");
        }
        else
        {
            writer.println("\tFaces "+triangle.size()+" "+(triangle.size()*3)+" {");
            writer.println("\t\tTriangles {");
            String triangleOut = "\t\t\t{ ";
            for( int i = 0; i < triangle.size(); i++ )
            {
                triangle.get(i).updateVertexIds(this);
                writer.println(triangleOut + triangle.get(i).toString() + " },");
            }
            writer.println("\t\t}");
        }
        writer.println("\t}");
        int boneRefCount = 0;
        for( int i = 0; i < matrix.size(); i++ )
        {
            boneRefCount += matrix.get(i).bones.size();
        }
        writer.println("\tGroups "+matrix.size()+" "+boneRefCount+" {");
        for( int i = 0; i < matrix.size(); i++ )
        {
            matrix.get(i).updateIds(mdlr);
            matrix.get(i).printTo(writer,2);//2 is the tab height
        }
        writer.println("\t}");
        if( extents != null )
        	extents.printTo(writer,1);
        for( int i = 0; i < anims.size(); i++ )
        {
            anims.get(i).printTo(writer,1);
        }
        
        writer.println("\tMaterialID "+materialID+",");
        writer.println("\tSelectionGroup "+selectionGroup+",");
        for( int i = 0; i < flags.size(); i++ )
        {
            writer.println("\t"+flags.get(i)+",");
        }
        
        
        
        writer.println("}");
    }
    public void doSavePrep(MDL mdlr)
    {
        purifyFaces();
        
        //Normals cleared here, in case that becomes a problem later.
        normals.clear();
        //UV Layers cleared here
        uvlayers.clear();
        int bigNum = 0;
        int littleNum = -1;
        for( int i = 0; i < vertex.size(); i++ )
        {
            int temp = vertex.get(i).tverts.size();
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
            uvlayers.add(new UVLayer());
        }
        for( int i = 0; i < vertex.size(); i++ )
        {
            normals.add(vertex.get(i).getNormal());
            for( int uv = 0; uv < bigNum; uv++ )
            {
                TVertex temp = vertex.get(i).getTVertex(uv);
                if( temp != null )
                {
                    uvlayers.get(uv).addTVertex(temp);
                }
                else
                {
                    uvlayers.get(uv).addTVertex(new TVertex(0,0));
                }
            }
        }
        //Clearing matrix list
        matrix.clear();
        for( int i = 0; i < vertex.size(); i++ )
        {
            Matrix newTemp = new Matrix(vertex.get(i).bones);
            boolean newMatrix = true;
            for( int m = 0; m < matrix.size() && newMatrix; m++ )
            {
                if( newTemp.equals(matrix.get(m)) )
                {
                    newTemp = matrix.get(m);
                    newMatrix = false;
                }
            }
            if( newMatrix )
            {
                matrix.add(newTemp);
                newTemp.updateIds(mdlr);
            }
            vertex.get(i).VertexGroup = matrix.indexOf(newTemp);
            vertex.get(i).setMatrix(newTemp);
        }
        for( int i = 0; i < triangle.size(); i++ )
        {
            triangle.get(i).updateVertexIds(this);
        }
        int boneRefCount = 0;
        for( int i = 0; i < matrix.size(); i++ )
        {
            boneRefCount += matrix.get(i).bones.size();
        }
        for( int i = 0; i < matrix.size(); i++ )
        {
            matrix.get(i).updateIds(mdlr);
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
    @Override
	public void setVisibilityFlag(AnimFlag a)
    {
        if( a != null )
        forceGetGeosetAnim().setVisibilityFlag(a);
    }
    @Override
	public AnimFlag getVisibilityFlag()
    {
        if( geosetAnim != null )
        return geosetAnim.getVisibilityFlag();
        return null;
    }
    @Override
	public String visFlagName()
    {
        return "Alpha";
    }
	public ExtLog getExtents() {
		return extents;
	}
	public void setExtents(ExtLog extents) {
		this.extents = extents;
	}
	public ArrayList<GeosetVertex> getVertices() {
		return vertex;
	}
	public void setVertex(ArrayList<GeosetVertex> vertex) {
		this.vertex = vertex;
	}
	public ArrayList<Normal> getNormals() {
		return normals;
	}
	public void setNormals(ArrayList<Normal> normals) {
		this.normals = normals;
	}
	public ArrayList<UVLayer> getUVLayers() {
		return uvlayers;
	}
	public void setUvlayers(ArrayList<UVLayer> uvlayers) {
		this.uvlayers = uvlayers;
	}
	public ArrayList<Triangle> getTriangle() {
		return triangle;
	}
	public void setTriangle(ArrayList<Triangle> triangle) {
		this.triangle = triangle;
	}
	public ArrayList<Matrix> getMatrix() {
		return matrix;
	}
	public void setMatrix(ArrayList<Matrix> matrix) {
		this.matrix = matrix;
	}
	public ArrayList<Animation> getAnims() {
		return anims;
	}
	public void setAnims(ArrayList<Animation> anims) {
		this.anims = anims;
	}
	public ArrayList<String> getFlags() {
		return flags;
	}
	public void setFlags(ArrayList<String> flags) {
		this.flags = flags;
	}
	public int getMaterialID() {
		return materialID;
	}
	public void setMaterialID(int materialID) {
		this.materialID = materialID;
	}
	public int getSelectionGroup() {
		return selectionGroup;
	}
	public void setSelectionGroup(int selectionGroup) {
		this.selectionGroup = selectionGroup;
	}
	public MDL getParentModel() {
		return parentModel;
	}
	public void setParentModel(MDL parentModel) {
		this.parentModel = parentModel;
	}
	public GeosetAnim getGeosetAnim() {
		return geosetAnim;
	}
	public void setGeosetAnim(GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
	}
}
