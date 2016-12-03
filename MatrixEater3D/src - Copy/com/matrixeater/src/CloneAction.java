package com.matrixeater.src;
import java.util.*;
/**
 * CloneAction -- allowing you to undo clone!
 * 
 * Eric Theller 'Retera'
 * 6/11/2012
 */
public class CloneAction extends UndoAction
{
    MoveAction baseMovement;
    ArrayList<Vertex> selection;
    ArrayList<GeosetVertex> addedVerts;
    ArrayList<Triangle> addedTriangles;
    ArrayList<GeosetVertex> copiedGroup;
    boolean type;
    public CloneAction(ArrayList<Vertex> selection, Vertex moveVector, ArrayList<GeosetVertex> clones, ArrayList<Triangle> addedTriangles, boolean isExtrude)
    {
        addedVerts = clones;
        this.addedTriangles = addedTriangles;
        this.selection = new ArrayList<Vertex>(selection);
        baseMovement = new MoveAction(this.selection, moveVector, 0 );
        type = isExtrude;
    }
    public CloneAction()
    {
        
    }
    public void storeSelection(ArrayList<Vertex> selection)
    {
        this.selection = new ArrayList<Vertex>(selection);
    }
    public void storeBaseMovement(Vertex moveVector)
    {
        baseMovement = new MoveAction(this.selection, moveVector, 0 );
    }
    public void redo()
    {
        baseMovement.redo();
        for( int i = 0; i < selection.size(); i++ )
        {
            if( selection.get(i).getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)selection.get(i);
                GeosetVertex cgv = null;
                boolean good = true;
                if( type )
                {
                    cgv = addedVerts.get(selection.indexOf(gv));
                }
                else
                {
                    if( !copiedGroup.contains(gv) )
                    {
                        good = false;
                    }
                    if( good )
                    cgv = addedVerts.get(copiedGroup.indexOf(gv));
                }
                if( good )
                {
                    ArrayList<Triangle> tris = new ArrayList<Triangle>(gv.triangles);
                    for( Triangle t: tris )
                    {
                        if( !selection.contains(t.get(0))
                            || !selection.contains(t.get(1))
                            || !selection.contains(t.get(2))
                            )
                        {
    //                         System.out.println("SHOULD be one: "+Collections.frequency(tris,t));
    //                         System.out.println("should be a number: "+t.indexOfRef(gv));
    //                         System.out.println("should be a negative one number: "+t.indexOfRef(cgv));
                            t.set(t.indexOfRef(gv),cgv);
                            gv.triangles.remove(t);
                            cgv.triangles.add(t);
                        }
                    }
                }
//                 cgv.geoset.addVertex(cgv);
            }
        }
                    for( Triangle t: addedTriangles )
                    {
                        for( GeosetVertex gv: t.getAll() )
                        {
                            if( !gv.triangles.contains(t) )
                            {
                                gv.triangles.add(t);
                            }
                        }
                        if( !t.m_geoRef.contains(t) )
                        {
                            t.m_geoRef.addTriangle(t);
                        }
                    }
                    for( GeosetVertex cgv: addedVerts )
                    {
                        if( cgv != null )
                        {
                            boolean inGeoset = false;
                            for( Triangle t: cgv.geoset.m_triangle )
                            {
                                if( t.containsRef(cgv) )
                                {
                                    inGeoset = true;
                                    break;
                                }
                            }
                            if( inGeoset )
                            cgv.geoset.addVertex(cgv);
                        }
                    }
//         for( Triangle t: addedTriangles )
//         {
//             t.m_geoRef.addTriangle(t);
//         }
        int probs = 0;
        for( int k = 0; k < selection.size(); k++ )
        {
            Vertex vert = selection.get(k);
            if( vert.getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)vert;
                for( Triangle t: gv.triangles )
                {
                    System.out.println("SHOULD be one: "+Collections.frequency(gv.triangles,t));
                    if( !t.containsRef(gv) )
                    {
                        probs++;
                    }
                }
            }
        }
        System.out.println("Redo "+actionName()+" finished with "+probs+ " inexplicable errors.");
    }
    public void undo()
    {
        baseMovement.undo();
        if( type )
        {
            for( Triangle t: addedTriangles )
            {
                for( GeosetVertex gv: t.getAll() )
                {
                    if( gv.triangles.contains(t) )
                    {
                        gv.triangles.remove(t);
                    }
                }
                t.m_geoRef.removeTriangle(t);
            }
            for( int i = 0; i < addedVerts.size(); i++ )
            {
                GeosetVertex cgv = addedVerts.get(i);
                if( cgv != null )
                {
                    GeosetVertex gv = (GeosetVertex)selection.get(addedVerts.indexOf(cgv));
                    ArrayList<Triangle> ctris = new ArrayList<Triangle>(cgv.triangles);
                    for( Triangle t: ctris )
                    {
                        t.set(t.indexOf(cgv),gv);
                        cgv.triangles.remove(t);
                        gv.triangles.add(t);
                    }
                    cgv.geoset.remove(cgv);
                    if( !gv.geoset.contains(gv) )
                    {
                        gv.geoset.addVertex(gv);
                    }
                }
            }
        }
        else
        {
            for( Triangle t: addedTriangles )
            {
                for( GeosetVertex gv: t.getAll() )
                {
                    if( gv.triangles.contains(t) )
                    {
                        gv.triangles.remove(t);
                    }
                }
                t.m_geoRef.removeTriangle(t);
            }
            for( int i = 0; i < addedVerts.size(); i++ )
            {
                GeosetVertex cgv = addedVerts.get(i);
                if( cgv != null )
                {
                    GeosetVertex gv = (GeosetVertex)copiedGroup.get(addedVerts.indexOf(cgv));
                    ArrayList<Triangle> ctris = new ArrayList<Triangle>(cgv.triangles);
                    for( Triangle t: ctris )
                    {
                        t.set(t.indexOf(cgv),gv);
                        cgv.triangles.remove(t);
                        gv.triangles.add(t);
                    }
                    cgv.geoset.remove(cgv);
                    if( !gv.geoset.contains(gv) )
                    {
                        gv.geoset.addVertex(gv);
                    }
                }
            }
        }
//                     for( GeosetVertex cgv: addedVerts )
//                     {
//                         if( cgv != null )
//                         {
//                             boolean inGeoset = false;
//                             for( Triangle t: cgv.geoset.m_triangle )
//                             {
//                                 if( t.containsRef(cgv) )
//                                 {
//                                     inGeoset = true;
//                                     break;
//                                 }
//                             }
//                             if( inGeoset )
//                             cgv.geoset.remove(cgv);
//                         }
//                     }
        int probs = 0;
        for( int k = 0; k < selection.size(); k++ )
        {
            Vertex vert = selection.get(k);
            if( vert.getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)vert;
                for( Triangle t: gv.triangles )
                {
                    System.out.println("SHOULD be one: "+Collections.frequency(gv.triangles,t));
                    if( !t.containsRef(gv) )
                    {
                        probs++;
                    }
                }
            }
        }
        System.out.println("Undo "+actionName()+" finished with "+probs+ " inexplicable errors.");
    }
    public String actionName()
    {
        if( type )
        {
            return "extrude";
        }
        else
        {
            return "extrude";
        }
    }
}