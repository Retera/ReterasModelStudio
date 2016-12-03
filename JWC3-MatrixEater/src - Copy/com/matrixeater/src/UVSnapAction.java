package com.matrixeater.src;
import java.util.*;
/**
 * Undoable snap action.
 * 
 * Eric Theller
 * 6/11/2012
 */
public class UVSnapAction extends UndoAction
{
    ArrayList<TVertex> oldSelLocs;
    ArrayList<TVertex> selection;
    TVertex snapPoint;
    public UVSnapAction(ArrayList<TVertex> selection, ArrayList<TVertex> oldSelLocs, TVertex snapPoint)
    {
        this.selection = new ArrayList<TVertex>(selection);
        this.oldSelLocs = oldSelLocs;
        this.snapPoint = new TVertex(snapPoint);
    }
    public void undo()
    {
        for( int i = 0; i < selection.size(); i++ )
        {
            selection.get(i).setTo(oldSelLocs.get(i));
        }
    }
    public void redo()
    {
        for( int i = 0; i < selection.size(); i++ )
        {
            selection.get(i).setTo(snapPoint);
        }
    }
    public String actionName()
    {
        return "snap TVerteces";
    }
}
