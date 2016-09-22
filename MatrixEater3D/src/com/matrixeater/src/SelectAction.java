package com.matrixeater.src;
import java.util.ArrayList;
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
    public void redo()
    {
        mdl.selection = newSel;
    }
    public void undo()
    {
        mdl.selection = oldSel;
    }
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
