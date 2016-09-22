package com.hiveworkshop.wc3.gui.modeledit;
import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.Vertex;
/**
 * MotionAction -- something for you to undo when you screw up with motion
 * 
 * Eric Theller
 * 6/8/2012
 */
public class RotateAction extends MoveAction
{
    ArrayList<Normal> normals;
    ArrayList<Vertex> normalMoveVectors;
    public RotateAction(ArrayList<Vertex> selection, ArrayList<Vertex> moveVectors, int actionType)
    {
    	super(selection,moveVectors,actionType);
    	normals = new ArrayList<Normal>();
    	for( Vertex ver: selection )
    	{
    		if( ver instanceof GeosetVertex )
    		{
    			GeosetVertex gv = (GeosetVertex)ver;
    			normals.add(gv.getNormal());
    		}
    	}
    }
    public RotateAction()
    {
    	super();
    	normals = new ArrayList<Normal>();
    }
    
    @Override
	public void storeSelection(ArrayList<Vertex> selection)
    {
    	super.storeSelection(selection);
    	for( Vertex ver: selection )
    	{
    		if( ver instanceof GeosetVertex )
    		{
    			GeosetVertex gv = (GeosetVertex)ver;
    			normals.add(gv.getNormal());
    		}
    	}
    }
    
    @Override
    public void createEmptyMoveVectors()
    {
    	super.createEmptyMoveVectors();
    	
        normalMoveVectors = new ArrayList<Vertex>();
        for( int i = 0; i < normals.size(); i++ )
        {
            normalMoveVectors.add(new Vertex( 0,0,0 ) );
        }
    }
    @Override
    public void redo()
    {
        super.redo();
        for( int i = 0; i < normals.size(); i++ )
        {
            Normal ver = normals.get(i);
            Vertex vect = normalMoveVectors.get(i);
            ver.x += vect.x;
            ver.y += vect.y;
            ver.z += vect.z;
        }
    }
    @Override
    public void undo()
    {
        super.undo();
        for( int i = 0; i < normals.size(); i++ )
        {
            Normal ver = normals.get(i);
            Vertex vect = normalMoveVectors.get(i);
            ver.x -= vect.x;
            ver.y -= vect.y;
            ver.z -= vect.z;
        }
    }
}
