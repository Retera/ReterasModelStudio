package com.hiveworkshop.wc3.gui.modeledit;
import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.Vertex;
public class SelectAction extends UndoAction
{
    ArrayList<Vertex> oldSel;
    ArrayList<Vertex> newSel;
    MDLDisplay mdl;
    int selectionType;
    public SelectAction(ArrayList<Vertex> oldSelection, ArrayList<Vertex> newSelection, MDLDisplay mdld, int selectType)
    {
        oldSel = oldSelection;
        newSel = new ArrayList<Vertex>(newSelection);
        mdl = mdld;
        selectionType = selectType;
    }
    public SelectAction()
    {
        
    }
    public void storeOldSelection(ArrayList<Vertex> oldSelection)
    {
        oldSel = new ArrayList<Vertex>(oldSelection);
    }
    public void storeNewSelection(ArrayList<Vertex> newSelection)
    {
        newSel = new ArrayList<Vertex>(newSelection);
    }
    @Override
	public void redo()
    {
        mdl.selection = newSel;
    }
    @Override
	public void undo()
    {
        mdl.selection = oldSel;
    }
    @Override
	public String actionName()
    {
        String outName = "";
        switch (selectionType)
        {
            case 0:
                outName = "selection: select";
                break;
            case 1:
                outName = "selection: add";
                break;
            case 2:
                outName = "selection: deselect";
                break;
            case 3:
                outName = "Select All";
                break;
            case 4:
                outName = "Invert Selection";
                break;
            case 5:
                outName = "Expand Selection";
                break;
        }
        return outName;
    }
    
}
