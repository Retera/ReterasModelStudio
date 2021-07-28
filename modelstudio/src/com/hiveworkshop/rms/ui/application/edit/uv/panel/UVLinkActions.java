package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorWidgetType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class UVLinkActions {
	ModelPanel modelPanel;
	UVPanel uvPanel;
	ToolbarButtonGroup2<TVertexSelectionItemTypes> selectionItemTypeGroup;
	ToolbarButtonGroup2<SelectionMode> selectionModeGroup;
	ToolbarButtonGroup2<ModelEditorWidgetType> actionTypeGroup;
	AbstractAction undoAction;
	AbstractAction redoAction;
	boolean cheatShift = false;
	boolean cheatAlt = false;

	public UVLinkActions(UVPanel uvPanel) {
		this.uvPanel = uvPanel;
		this.modelPanel = ProgramGlobals.getCurrentModelPanel();
//		undoAction = ProgramGlobals.getUndoHandler().getUndoAction();
//		redoAction = ProgramGlobals.getUndoHandler().getRedoAction();
		undoAction = ProgramGlobals.getUndoHandler().getUndoAction();
		redoAction = ProgramGlobals.getUndoHandler().getRedoAction();

	}

	AbstractAction selectAllAction = getAsAction("Select All", () -> selectAll(modelPanel.getModelView()));
	AbstractAction invertSelectAction = getAsAction("Invert Selection", () -> invertSelection(modelPanel.getModelView()));
	AbstractAction expandSelectionAction = getAsAction("Expand Selection", () -> expandSelection(modelPanel.getModelView()));
	AbstractAction selFromMainAction = getAsAction("Sel From Main", () -> selectFromViewer(modelPanel.getModelView()));


	public void expandSelection(ModelView modelView) {
		Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());
		Set<GeosetVertex> oldSelection = new HashSet<>(modelView.getSelectedVertices());
		for (GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}

		modelPanel.getUndoManager().pushAction(new SetSelectionAction(expandedSelection, modelView, "expand selection").redo());
		uvPanel.repaint();
	}

	private void expandSelection(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}

	public void invertSelection(ModelView modelView) {
		Set<GeosetVertex> invertedSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			invertedSelection.addAll(geoset.getVertices());
		}
		invertedSelection.removeAll(modelView.getSelectedVertices());

		modelPanel.getUndoManager().pushAction(new SetSelectionAction(invertedSelection, modelView, "invert selection").redo());
		uvPanel.repaint();
	}

	public void selectAll(ModelView modelView) {
		Set<GeosetVertex> allSelection = new HashSet<>();
		for (Geoset geo : modelView.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}

		modelPanel.getUndoManager().pushAction(new SetSelectionAction(allSelection, modelView, "select all").redo());
		uvPanel.repaint();
	}

	public void selectFromViewer(ModelView modelView) {
		modelPanel.getUndoManager().pushAction(new SetSelectionAction(modelView.getSelectedVertices(), modelView, "").redo());
		uvPanel.repaint();
	}


	public void linkActions(JRootPane root, UVPanel uvPanel) {
		int isAncestor = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
		ActionMap actionMap = root.getActionMap();
		InputMap inputMap = root.getInputMap(isAncestor);

		actionMap.put("Undo", getAsAction(() -> ProgramGlobals.getUndoHandler().undo()));
		inputMap.put(KeyStroke.getKeyStroke("control Z"), "Undo");

		actionMap.put("Redo", getAsAction(() -> ProgramGlobals.getUndoHandler().redo()));
		inputMap.put(KeyStroke.getKeyStroke("control Y"), "Redo");
//
//		actionMap.put("Undo", undoAction);
//		inputMap.put(KeyStroke.getKeyStroke("control Z"), "Undo");
//
//		actionMap.put("Redo", redoAction);
//		inputMap.put(KeyStroke.getKeyStroke("control Y"), "Redo");

		inputMap.put(KeyStroke.getKeyStroke("W"), "MoveKeyboardKey");
		actionMap.put("MoveKeyboardKey", getAsAction(() -> actionTypeGroup.setActiveButton(ModelEditorWidgetType.TRANSLATION)));

		inputMap.put(KeyStroke.getKeyStroke("E"), "RotateKeyboardKey");
		actionMap.put("RotateKeyboardKey", getAsAction(() -> actionTypeGroup.setActiveButton(ModelEditorWidgetType.ROTATION)));

		inputMap.put(KeyStroke.getKeyStroke("R"), "ScaleKeyboardKey");
		actionMap.put("ScaleKeyboardKey", getAsAction(() -> actionTypeGroup.setActiveButton(ModelEditorWidgetType.SCALING)));

		inputMap.put(KeyStroke.getKeyStroke("A"), "SelectKeyboardKey");
		actionMap.put("SelectKeyboardKey", getAsAction(() -> selectionItemTypeGroup.setActiveButton(TVertexSelectionItemTypes.VERTEX)));

		inputMap.put(KeyStroke.getKeyStroke("S"), "AddSelectKeyboardKey");
		actionMap.put("AddSelectKeyboardKey", getAsAction(() -> selectionItemTypeGroup.setActiveButton(TVertexSelectionItemTypes.FACE)));


		inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
		actionMap.put("shiftSelect", getAsAction("shiftSelect", this::shiftSelect));
		inputMap.put(KeyStroke.getKeyStroke("alt pressed ALT"), "altSelect");
		actionMap.put("altSelect", getAsAction("altSelect", this::altSelect));

		inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), "unShiftSelect");
		actionMap.put("unShiftSelect", getAsAction("unShiftSelect", this::unShiftSelect));
		inputMap.put(KeyStroke.getKeyStroke("released ALT"), "unAltSelect");
		actionMap.put("unAltSelect", getAsAction("unAltSelect", this::unAltSelect));

		inputMap.put(KeyStroke.getKeyStroke("control A"), "Select All");
		actionMap.put("Select All", selectAllAction);

		inputMap.put(KeyStroke.getKeyStroke("control I"), "Invert Selection");
		actionMap.put("Invert Selection", invertSelectAction);

		inputMap.put(KeyStroke.getKeyStroke("control E"), "Expand Selection");
		actionMap.put("Expand Selection", expandSelectionAction);
	}

	public void shiftSelect() {
		if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
			selectionModeGroup.setActiveButton(SelectionMode.ADD);
			cheatShift = true;
		}
	}

	public void altSelect() {
		if (selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
			selectionModeGroup.setActiveButton(SelectionMode.DESELECT);
			cheatAlt = true;
		}
	}

	public void unShiftSelect() {
		if ((selectionModeGroup.getActiveButtonType() == SelectionMode.ADD) && cheatShift) {
			selectionModeGroup.setActiveButton(SelectionMode.SELECT);
			cheatShift = false;
		}
	}

	public void unAltSelect() {
		if ((selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && cheatAlt) {
			selectionModeGroup.setActiveButton(SelectionMode.SELECT);
			cheatAlt = false;
		}
	}

	private AbstractAction getAsAction(String name, Runnable action) {
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		};
	}

	private AbstractAction getAsAction(Runnable action) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		};
	}
}
