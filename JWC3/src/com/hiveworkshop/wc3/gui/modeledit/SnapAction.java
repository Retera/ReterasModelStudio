package com.hiveworkshop.wc3.gui.modeledit;
import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.Vertex;
/**
 * Undoable snap action.
 * 
 * Eric Theller
 * 6/11/2012
 */
public class SnapAction extends UndoAction
{
    ArrayList<Vertex> oldSelLocs;
    ArrayList<Vertex> selection;
    Vertex snapPoint;
    public SnapAction(ArrayList<Vertex> selection, ArrayList<Vertex> oldSelLocs, Vertex snapPoint)
    {
        this.selection = new ArrayList<Vertex>(selection);
        this.oldSelLocs = oldSelLocs;
        this.snapPoint = new Vertex(snapPoint);
    }
    @Override
	public void undo()
    {
        for( int i = 0; i < selection.size(); i++ )
        {
            selection.get(i).setTo(oldSelLocs.get(i));
        }
    }
    @Override
	public void redo()
    {
        for( int i = 0; i < selection.size(); i++ )
        {
            selection.get(i).setTo(snapPoint);
        }
    }
    @Override
	public String actionName()
    {
        return "snap verteces";
    }
}
