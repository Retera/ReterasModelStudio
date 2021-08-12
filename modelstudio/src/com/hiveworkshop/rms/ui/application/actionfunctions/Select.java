package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.InvertSelectionAction2;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Select {

	private static class SelectAll  extends ActionFunction {

		SelectAll(){
			super(TextKey.SELECT_ALL, () -> selectAll());
			setKeyStroke(KeyStroke.getKeyStroke("control A"));
		}
	}
	private static class ExpandSelection  extends ActionFunction {

		ExpandSelection(){
			super(TextKey.EXPAND_SELECTION, () -> expandSelection());
			setKeyStroke(KeyStroke.getKeyStroke("control E"));
		}
	}
	private static class InvertSelection extends ActionFunction {

		InvertSelection(){
			super(TextKey.INVERT_SELECTION, () -> invertSelectActionRes());
			setKeyStroke(KeyStroke.getKeyStroke("control I"));
		}
	}

	public static JMenuItem getSelectAllMenuItem(){
		return new SelectAll().getMenuItem();
	}
	public static JMenuItem getInvertSelectMenuItem(){
		return new InvertSelection().getMenuItem();
	}
	public static JMenuItem getExpandSelectionMenuItem(){
		return new ExpandSelection().getMenuItem();
	}

	public static void selectAll() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			Set<GeosetVertex> allSelection = new HashSet<>();
			ModelView modelView = modelPanel.getModelView();
			for (Geoset geo : modelView.getEditableGeosets()) {
				allSelection.addAll(geo.getVertices());
			}
			UndoAction action = new SetSelectionAction(allSelection, modelView.getEditableIdObjects(), modelView.getEditableCameras(), modelView, "select all");
			modelPanel.getUndoManager().pushAction(action.redo());
		}
	}

	public static void invertSelectActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			modelPanel.getUndoManager().pushAction(new InvertSelectionAction2(modelPanel.getModelView()).redo());
		}
	}

	public static void expandSelection() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		// ToDo this should be renamed select linked, and a real "expand selection" should be implemented (ie this without the recursive call)
		//  also, maybe care about collision shape vertices...
		if (modelPanel != null) {
			ModelView modelView = modelPanel.getModelView();
			Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());

			for (GeosetVertex v : modelView.getSelectedVertices()) {
				expandSelection(v, expandedSelection);
			}
			SetSelectionAction setSelectionAction = new SetSelectionAction(expandedSelection, modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), modelView, "expand selection");

			modelPanel.getUndoManager().pushAction(setSelectionAction.redo());
		}
	}

	private static void expandSelection(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (final GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}



	private static class ModAddSelect extends ActionFunction {

		ModAddSelect(){
			super(TextKey.INVERT_SELECTION, () -> shiftSelectActionRes());
			setKeyStroke(KeyStroke.getKeyStroke("shift pressed SHIFT"));
		}
	}
	public static void unAltSelect() {
		if ((ProgramGlobals.getSelectionMode() == SelectionMode.DESELECT) && ProgramGlobals.isCheatAlt()) {
			ProgramGlobals.setSelectionModeButton(SelectionMode.SELECT);
			ProgramGlobals.setCheatAlt(false);
		}
	}

	public static void unShiftSelectActionRes() {
		if ((ProgramGlobals.getSelectionMode() == SelectionMode.ADD) && ProgramGlobals.isCheatShift()) {
			ProgramGlobals.setSelectionModeButton(SelectionMode.SELECT);
			ProgramGlobals.setCheatShift(false);
		}
	}

	public static void altSelectActionRes() {
		if (ProgramGlobals.getSelectionMode() == SelectionMode.SELECT) {
			ProgramGlobals.setSelectionModeButton(SelectionMode.DESELECT);
			ProgramGlobals.setCheatAlt(true);
		}
	}

	public static void shiftSelectActionRes() {
		if (!isTextField() && ProgramGlobals.getSelectionMode() == SelectionMode.SELECT) {
			ProgramGlobals.setSelectionModeButton(SelectionMode.ADD);
			ProgramGlobals.setCheatShift(true);
		}
	}

	private static class Animate extends ActionFunction{
		Animate(){
			super(TextKey.SELECTION_TYPE_ANIMATE, () -> setType(SelectionItemTypes.ANIMATE), "A");
		}
	}
	private static class Vertex extends ActionFunction{
		Vertex(){
			super(TextKey.SELECTION_TYPE_VERTEX, () -> setType(SelectionItemTypes.VERTEX), "S");
		}
	}
	private static class Cluster extends ActionFunction{
		Cluster(){
			super(TextKey.SELECTION_TYPE_CLUSTER, () -> setType(SelectionItemTypes.CLUSTER), "D");
		}
	}
	private static class Face extends ActionFunction{
		Face(){
			super(TextKey.SELECTION_TYPE_FACE, () -> setType(SelectionItemTypes.FACE), "F");
		}
	}
	private static class Group extends ActionFunction{
		Group(){
			super(TextKey.SELECTION_TYPE_GROUP, () -> setType(SelectionItemTypes.GROUP), "G");
		}
	}

	public static JMenuItem getAnimateItem(){
		return new Animate().getMenuItem();
	}
	public static JMenuItem getVertexItem(){
		return new Vertex().getMenuItem();
	}
	public static JMenuItem getClusterItem(){
		return new Cluster().getMenuItem();
	}
	public static JMenuItem getFaceItem(){
		return new Face().getMenuItem();
	}
	public static JMenuItem getGroupItem(){
		return new Group().getMenuItem();
	}

	public static void setType(SelectionItemTypes type){
		if (!isTextField()){
			ProgramGlobals.setSelectionTypeButton(type);
		}
	}


	private static boolean isTextField() {
		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return (focusedComponent instanceof JTextComponent);
	}

}
