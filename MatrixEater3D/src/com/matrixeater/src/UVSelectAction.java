package com.matrixeater.src;
import java.util.ArrayList;
public class UVSelectAction extends UndoAction
{
    ArrayList<TVertex> oldSel;
    ArrayList<TVertex> newSel;
    MDLDisplay mdl;
    int selectionType;
    public UVSelectAction(ArrayList<TVertex> oldSelection, ArrayList<TVertex> newSelection, MDLDisplay mdld, int selectType)
    {
        oldSel = oldSelection;
        newSel = new ArrayList<TVertex>(newSelection);
        mdl = mdld;
        selectionType = selectType;
    }
    public UVSelectAction()
    {
        
    }
    public void storeOldSelection(ArrayList<TVertex> oldSelection)
    {
        oldSel = new ArrayList<TVertex>(oldSelection);
    }
    public void storeNewSelection(ArrayList<TVertex> newSelection)
    {
        newSel = new ArrayList<TVertex>(newSelection);
    }
    public void redo()
    {
        mdl.uvselection = newSel;
    }
    public void undo()
    {
        mdl.uvselection = oldSel;
    }
    public String actionName()
    {
        String outName = "";
        switch (selectionType)
        {
            case 0:
                outName = "UV selection: select";
                break;
            case 1:
                outName = "UV selection: add";
                break;
            case 2:
                outName = "UV selection: deselect";
                break;
            case 3:
                outName = "UV Select All";
                break;
            case 4:
                outName = "UV Invert Selection";
                break;
            case 5:
                outName = "UV Expand Selection";
                break;
            case 6:
                outName = "UV Select from Viewer";
                break;
        }
        return outName;
    }
    
}
