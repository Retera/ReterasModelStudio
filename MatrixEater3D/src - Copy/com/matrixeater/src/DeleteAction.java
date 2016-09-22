package com.matrixeater.src;
import java.util.*;
/**
 * Something to undo when you deleted something important.
 * 
 * Eric Theller
 * 6/11/2012
 */
public class DeleteAction extends UndoAction
{
    ArrayList<Vertex> selection;
    ArrayList<Vertex> deleted;
    ArrayList<Triangle> deletedTris;
    public DeleteAction(ArrayList<Vertex> selection, ArrayList<Triangle> deletedTris)
    {
        this.selection = selection;
        this.deleted = selection;
        this.deletedTris = deletedTris;
    }
    public void redo()
    {
        for( int i = 0; i < deleted.size(); i++ )
        {
            if( deleted.get(i).getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)deleted.get(i);
                gv.geoset.remove(gv);
            }
        }
        for( Triangle t: deletedTris )
        {
            t.m_geoRef.removeTriangle(t);
        }
    }
    public void undo()
    {
        for( int i = 0; i < deleted.size(); i++ )
        {
            if( deleted.get(i).getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)deleted.get(i);
                gv.geoset.addVertex(gv);
            }
        }
        for( Triangle t: deletedTris )
        {
            t.m_geoRef.addTriangle(t);
        }
    }
    public String actionName()
    {
        return "delete vertices";
    }
}
