package com.hiveworkshop.wc3.gui.modeledit;
import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
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
    public DeleteAction(final ArrayList<Vertex> selection, final ArrayList<Triangle> deletedTris)
    {
        this.selection = selection;
        this.deleted = selection;
        this.deletedTris = deletedTris;
    }
    @Override
	public void redo()
    {
        for( int i = 0; i < deleted.size(); i++ )
        {
            if( deleted.get(i).getClass() == GeosetVertex.class )
            {
                final GeosetVertex gv = (GeosetVertex)deleted.get(i);
                gv.getGeoset().remove(gv);
            }
        }
        for( final Triangle t: deletedTris )
        {
            t.getGeoset().removeTriangle(t);
        }
    }
    @Override
	public void undo()
    {
        for( int i = 0; i < deleted.size(); i++ )
        {
            if( deleted.get(i).getClass() == GeosetVertex.class )
            {
                final GeosetVertex gv = (GeosetVertex)deleted.get(i);
                gv.getGeoset().addVertex(gv);
            }
        }
        for( final Triangle t: deletedTris )
        {
            t.getGeoset().addTriangle(t);
        }
    }
    @Override
	public String actionName()
    {
        return "delete vertices";
    }
}
