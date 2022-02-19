package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.InvertSelectionAction2;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Select {
	private static SelectAll selectAll;
	private static InvertSelection invertSelection;
	private static ExpandSelection expandSelection;
	private static SelectNodeGeometry selectNodeGeometry;
	private static SelectLinkedGeometry selectLinkedGeometry;

	private static class SelectAll extends ActionFunction {

		SelectAll(){
			super(TextKey.SELECT_ALL, Select::selectAll);
			setKeyStroke(KeyStroke.getKeyStroke("control A"));
		}
	}
	private static class ExpandSelection  extends ActionFunction {

		ExpandSelection(){
			super(TextKey.EXPAND_SELECTION, Select::expandSelection);
			setKeyStroke(KeyStroke.getKeyStroke("control E"));
		}
	}
	private static class SelectLinkedGeometry  extends ActionFunction {

		SelectLinkedGeometry(){
			super(TextKey.SELECT_LINKED_GEOMETRY, Select::selectLinked);
			setKeyStroke(KeyStroke.getKeyStroke("control L"));
		}
	}
	private static class InvertSelection extends ActionFunction {

		InvertSelection(){
			super(TextKey.INVERT_SELECTION, Select::invertSelectActionRes);
			setKeyStroke(KeyStroke.getKeyStroke("control I"));
		}
	}

	private static class SelectNodeGeometry extends ActionFunction{
		SelectNodeGeometry() {
			super(TextKey.SELECT_NODE_GEOMETRY, Select::selectNodeGeometry);
		}
	}



//	public static JMenuItem getSelectAllMenuItem(){
//		return new SelectAll().getMenuItem();
//	}
//	public static JMenuItem getInvertSelectMenuItem(){
//		return new InvertSelection().getMenuItem();
//	}
//	public static JMenuItem getExpandSelectionMenuItem(){
//		return new ExpandSelection().getMenuItem();
//	}
//	public static JMenuItem getSelectNodeGeometryMenuItem(){
//		return new SelectNodeGeometry().getMenuItem();
//	}
//	public static JMenuItem getSelectLinkedGeometryMenuItem(){
//		return new SelectLinkedGeometry().getMenuItem();
//	}


	public static JMenuItem getSelectAllMenuItem(){
		return getSelectAll().getMenuItem();
	}
	public static JMenuItem getInvertSelectMenuItem(){
		return getInvertSelection().getMenuItem();
	}
	public static JMenuItem getExpandSelectionMenuItem(){
		return getExpandSelection().getMenuItem();
	}
	public static JMenuItem getSelectNodeGeometryMenuItem(){
		return getSelectNodeGeometry().getMenuItem();
	}
	public static JMenuItem getSelectLinkedGeometryMenuItem(){
		return getSelectLinkedGeometry().getMenuItem();
	}


	public static ActionFunction getSelectAll(){
		if(selectAll == null){
			selectAll = new SelectAll();
		}
		return selectAll;
	}
	public static ActionFunction getInvertSelection(){
		if(invertSelection == null){
			invertSelection = new InvertSelection();
		}
		return invertSelection;
	}
	public static ActionFunction getExpandSelection(){
		if(expandSelection == null){
			expandSelection = new ExpandSelection();
		}
		return expandSelection;
	}
	public static ActionFunction getSelectNodeGeometry(){
		if(selectNodeGeometry == null){
			selectNodeGeometry = new SelectNodeGeometry();
		}
		return selectNodeGeometry;
	}
	public static ActionFunction getSelectLinkedGeometry(){
		if(selectLinkedGeometry == null){
			selectLinkedGeometry = new SelectLinkedGeometry();
		}
		return selectLinkedGeometry;
	}

	private static void selectAll(ModelHandler modelHandler) {
		Set<GeosetVertex> allSelection = new HashSet<>();
		ModelView modelView = modelHandler.getModelView();
		for (Geoset geo : modelView.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}
		SelectionBundle bundle = new SelectionBundle(allSelection, modelView.getEditableIdObjects(), modelView.getEditableCameras());
		UndoAction action = new SetSelectionUggAction(bundle, modelView, "select all", ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private static void invertSelectActionRes(ModelHandler modelHandler) {
		modelHandler.getUndoManager().pushAction(new InvertSelectionAction2(modelHandler.getModelView()).redo());
	}

	private static void selectLinked(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());

		for (GeosetVertex v : modelView.getSelectedVertices()) {
			selectLinked(v, expandedSelection);
		}
		SelectionBundle bundle = new SelectionBundle(expandedSelection, modelView.getSelectedIdObjects(), modelView.getSelectedCameras());
		UndoAction action = new SetSelectionUggAction(bundle, modelView, "expand selection", ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private static void selectLinked(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					selectLinked(other, selection);
				}
			}
		}
	}

	private static void expandSelection(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());

		for (GeosetVertex v : modelView.getSelectedVertices()) {
			v.getTriangles().forEach(tri -> expandedSelection.addAll(Arrays.asList(tri.getVerts())));
		}
		SelectionBundle bundle = new SelectionBundle(expandedSelection, modelView.getSelectedIdObjects(), modelView.getSelectedCameras());
		UndoAction action = new SetSelectionUggAction(bundle, modelView, "expand selection", ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private static void selectNodeGeometry(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();

		Set<Bone> selectedBones = new HashSet<>();
		Set<GeosetVertex> vertexList = new HashSet<>();
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			if (idObject instanceof Bone) {
				selectedBones.add((Bone) idObject);
			}
		}
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Bone bone : selectedBones) {
				List<GeosetVertex> vertices = geoset.getBoneMap().get(bone);
				if (vertices != null) {
					vertexList.addAll(vertices);
				}
			}
		}
		if (!vertexList.isEmpty()) {
			UndoAction action = new SetSelectionUggAction(new SelectionBundle(vertexList), modelView, "Select", ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
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
