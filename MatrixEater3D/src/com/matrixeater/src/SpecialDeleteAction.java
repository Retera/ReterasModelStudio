package com.matrixeater.src;

import java.util.ArrayList;

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
    		if( g.geosetAnim != null )
    		{
    			parent.remove(g.geosetAnim);
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
    		if( g.geosetAnim != null )
    		{
    			parent.add(g.geosetAnim);
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
