package com.matrixeater.src;
import java.util.*;
/**
 * MotionAction -- something for you to undo when you screw up with motion
 * 
 * Eric Theller
 * 6/8/2012
 */
public class MoveAction extends UndoAction
{
    ArrayList<Vertex> selection;
    ArrayList<Vertex> moveVectors;
    Vertex moveVector;
    int actType = 0;
    public MoveAction(ArrayList<Vertex> selection, ArrayList<Vertex> moveVectors, int actionType)
    {
        this.selection = new ArrayList<Vertex>(selection);
        this.moveVectors = moveVectors;
        actType = actionType;
    }
    public MoveAction(ArrayList<Vertex> selection, Vertex moveVector, int actionType)
    {
        this.selection = new ArrayList<Vertex>(selection);
        this.moveVector = moveVector;
        actType = actionType;
    }
    public MoveAction()
    {
        
    }
    public void storeSelection(ArrayList<Vertex> selection)
    {
        this.selection = new ArrayList<Vertex>(selection);
    }
    public void createEmptyMoveVectors()
    {
        moveVectors = new ArrayList<Vertex>();
        for( int i = 0; i < selection.size(); i++ )
        {
            moveVectors.add(new Vertex( 0,0,0 ) );
        }
    }
    public void createEmptyMoveVector()
    {
        moveVector = new Vertex( 0,0,0 );
    }
    public void redo()
    {
        if( moveVector == null )
        {
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex ver = selection.get(i);
                Vertex vect = moveVectors.get(i);
                ver.x += vect.x;
                ver.y += vect.y;
                ver.z += vect.z;
            }
        }
        else
        {
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex ver = selection.get(i);
                Vertex vect = moveVector;
                ver.x += vect.x;
                ver.y += vect.y;
                ver.z += vect.z;
            }
        }
    }
    public void undo()
    {
        if( moveVector == null )
        {
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex ver = selection.get(i);
                Vertex vect = moveVectors.get(i);
                ver.x -= vect.x;
                ver.y -= vect.y;
                ver.z -= vect.z;
            }
        }
        else
        {
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex ver = selection.get(i);
                Vertex vect = moveVector;
                ver.x -= vect.x;
                ver.y -= vect.y;
                ver.z -= vect.z;
            }
        }
    }
    public String actionName()
    {
        String outName = "";
        switch (actType)
        {
            case 3:
                outName = "move";
                break;
            case 4:
                outName = "rotate";
                break;
            case 5:
                outName = "scale";
                break;
        }
        if( outName.equals("") )
        {
            outName = "actionType_"+actType;
        }
        return outName+" vertices";
    }
}
