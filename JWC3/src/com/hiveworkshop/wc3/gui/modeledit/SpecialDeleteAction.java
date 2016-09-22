package com.hiveworkshop.wc3.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public class SpecialDeleteAction extends DeleteAction {

	ArrayList<Geoset> deletedGeosets;
	MDL parent;
	public SpecialDeleteAction(ArrayList<Vertex> selection,
			ArrayList<Triangle> deletedTris,
			ArrayList<Geoset> deletedGs,
			MDL parentModel) {
		super(selection, deletedTris);
		deletedGeosets = deletedGs;
		parent = parentModel;
	}
	@Override
    public void redo()
    {
		super.redo();
        for( int i = 0; i < deletedGeosets.size(); i++ )
        {
        	Geoset g = deletedGeosets.get(i);
    		if( g.getGeosetAnim() != null )
    		{
    			parent.remove(g.getGeosetAnim());
    		}
        	parent.remove(g);
        }
    }
	@Override
    public void undo()
    {
		super.undo();
        for( int i = 0; i < deletedGeosets.size(); i++ )
        {
        	Geoset g = deletedGeosets.get(i);
    		if( g.getGeosetAnim() != null )
    		{
    			parent.add(g.getGeosetAnim());
    		}
        	parent.add(g);
        }
    }
	@Override
    public String actionName()
    {
        return "delete vertices and geoset";
    }
}
