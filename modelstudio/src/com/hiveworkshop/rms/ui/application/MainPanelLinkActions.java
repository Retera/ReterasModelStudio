package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.DeleteAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.selection.InvertSelectionAction2;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.editor.actions.tools.CloneAction2;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainPanelLinkActions {


	public void linkActions(final JComponent root) {
		int isAncestor = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
		InputMap inputMap = root.getInputMap(isAncestor);
		ActionMap actionMap = root.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("control Z"), "Undo");
		actionMap.put("Undo", ProgramGlobals.getUndoHandler().getUndoAction());

		inputMap.put(KeyStroke.getKeyStroke("control Y"), "Redo");
		actionMap.put("Redo", ProgramGlobals.getUndoHandler().getRedoAction());

		inputMap.put(KeyStroke.getKeyStroke("DELETE"), "Delete");
		actionMap.put("Delete", getAsAction("Delete", () -> deleteActionRes()));

		actionMap.put("CloneSelection", getAsAction("CloneSelection", () -> cloneActionRes()));

//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
////		root.getActionMap().put("shiftSelect", shiftSelectAction(mainPanel));
//		root.getActionMap().put("shiftSelect", new AcAd(e -> shiftSelectActionRes(mainPanel)));

		inputMap.put(KeyStroke.getKeyStroke("SPACE"), "MaximizeSpacebar");
		actionMap.put("MaximizeSpacebar", getAsAction(() -> maximizeFocusedWindow()));

		inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "PressRight");
		actionMap.put("PressRight", nextKeyframe());

		inputMap.put(KeyStroke.getKeyStroke("LEFT"), "PressLeft");
		actionMap.put("PressLeft", previousKeyframe());

		inputMap.put(KeyStroke.getKeyStroke("UP"), "PressUp");
		actionMap.put("PressUp", jumpFramesAction(1));

		inputMap.put(KeyStroke.getKeyStroke("shift UP"), "PressShiftUp");
		actionMap.put("PressShiftUp", jumpFramesAction(10));

		inputMap.put(KeyStroke.getKeyStroke("DOWN"), "PressDown");
		actionMap.put("PressDown", jumpFramesAction(-1));

		inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), "PressShiftDown");
		actionMap.put("PressShiftDown", jumpFramesAction(-10));

		inputMap.put(KeyStroke.getKeyStroke("control SPACE"), "PlayKeyboardKey");
		actionMap.put("PlayKeyboardKey", playKeyboardKeyAction());

		inputMap.put(KeyStroke.getKeyStroke("W"), "WKeyboardKey");
		actionMap.put("WKeyboardKey", setTransformModeAnim(ModelEditorActionType3.TRANSLATION));

		inputMap.put(KeyStroke.getKeyStroke("E"), "EKeyboardKey");
		actionMap.put("EKeyboardKey", setTransformModeAnim(ModelEditorActionType3.SCALING));

		inputMap.put(KeyStroke.getKeyStroke("R"), "RKeyboardKey");
		actionMap.put("RKeyboardKey", setTransformModeAnim(ModelEditorActionType3.ROTATION));

		inputMap.put(KeyStroke.getKeyStroke("T"), "TKeyboardKey");
		actionMap.put("TKeyboardKey", setTransformMode(ModelEditorActionType3.EXTRUDE));

		inputMap.put(KeyStroke.getKeyStroke("Y"), "YKeyboardKey");
		actionMap.put("YKeyboardKey", setTransformMode(ModelEditorActionType3.EXTEND));

		inputMap.put(KeyStroke.getKeyStroke("A"), "AKeyboardKey");
		actionMap.put("AKeyboardKey", setSelectionType(SelectionItemTypes.ANIMATE));

		inputMap.put(KeyStroke.getKeyStroke("S"), "SKeyboardKey");
		actionMap.put("SKeyboardKey", setSelectionType(SelectionItemTypes.VERTEX));

		inputMap.put(KeyStroke.getKeyStroke("D"), "DKeyboardKey");
		actionMap.put("DKeyboardKey", setSelectionType(SelectionItemTypes.CLUSTER));

		inputMap.put(KeyStroke.getKeyStroke("F"), "FKeyboardKey");
		actionMap.put("FKeyboardKey", setSelectionType(SelectionItemTypes.FACE));

		inputMap.put(KeyStroke.getKeyStroke("G"), "GKeyboardKey");
		actionMap.put("GKeyboardKey", setSelectionType(SelectionItemTypes.GROUP));

		inputMap.put(KeyStroke.getKeyStroke("Z"), "ZKeyboardKey");
		actionMap.put("ZKeyboardKey", toggleWireFrame());

		inputMap.put(KeyStroke.getKeyStroke("control F"), "CreateFaceShortcut");
		actionMap.put("CreateFaceShortcut", getAsAction(() -> createFace()));

		for (int i = 1; i <= 9; i++) {
			inputMap.put(KeyStroke.getKeyStroke("alt pressed " + i), i + "KeyboardKey");
			final int index = i;
			actionMap.put(i + "KeyboardKey", getAsAction(() -> keyPressedAction(index)));
		}

		inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
		actionMap.put("shiftSelect", getAsAction("shiftSelect", () -> shiftSelectActionRes()));

		inputMap.put(KeyStroke.getKeyStroke("alt pressed ALT"), "altSelect");
		actionMap.put("altSelect", getAsAction("altSelect", () -> altSelectActionRes()));

		inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), "unShiftSelect");
		actionMap.put("unShiftSelect", getAsAction("unShiftSelect", () -> unShiftSelectActionRes()));

		inputMap.put(KeyStroke.getKeyStroke("released ALT"), "unAltSelect");
		actionMap.put("unAltSelect", getAsAction("unAltSelect", () -> unAltSelect()));

		inputMap.put(KeyStroke.getKeyStroke("control A"), "Select All");
		actionMap.put("Select All", getAsAction("Select All", () -> selectAllActionRes()));

		inputMap.put(KeyStroke.getKeyStroke("control I"), "Invert Selection");
		actionMap.put("Invert Selection", getAsAction("Invert Selection", () -> invertSelectActionRes()));

		inputMap.put(KeyStroke.getKeyStroke("control E"), "Expand Selection");
		actionMap.put("Expand Selection", getAsAction("Expand Selection", () -> getExpandSelectionActionRes()));

		inputMap.put(KeyStroke.getKeyStroke("control W"), "RigAction");
		actionMap.put("RigAction", getAsAction("Rig", () -> rigActionRes()));
	}

	private void unAltSelect() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && mainPanel.cheatAlt) {
//					mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.SELECT);
			mainPanel.cheatAlt = false;
		}
	}

	private void unShiftSelectActionRes() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (isTextField()) return;
		if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.ADD) && mainPanel.cheatShift) {
//			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.SELECT);
			mainPanel.cheatShift = false;
		}
	}

	private void altSelectActionRes() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
//			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.DESELECT);
			mainPanel.cheatAlt = true;
		}
	}

	private void shiftSelectActionRes() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (isTextField()) return;
		if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
//			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.ADD);
			mainPanel.cheatShift = true;
		}
	}

	private void keyPressedAction(int index) {
		final DockingWindow window = ProgramGlobals.getMainPanel().rootWindow.getWindow();
		if (window instanceof TabWindow) {
			final TabWindow tabWindow = (TabWindow) window;
			final int tabCount = tabWindow.getChildWindowCount();
			if ((index - 1) < tabCount) {
				tabWindow.setSelectedTab(index - 1);
			}
		}
	}

	private void createFace() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (!isTextField() && !mainPanel.animationModeState) {
			try {
				ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
				if (modelPanel != null) {
					Viewport viewport = mainPanel.viewportListener.getViewport();
					Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
					UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelPanel.getModelView(), facingVector);

					modelPanel.getUndoManager().pushAction(createFaceFromSelection);
				}
			} catch (final FaceCreationException exc) {
				JOptionPane.showMessageDialog(mainPanel, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
		}
	}

	private AbstractAction toggleWireFrame() {
		return getAsAction(() -> {
			if (!isTextField())
				ProgramGlobals.getPrefs().setViewMode(ProgramGlobals.getPrefs().getViewMode() == 1 ? 0 : 1);
		});
	}

	private AbstractAction setSelectionType(SelectionItemTypes t) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		return getAsAction(() ->
		{
			if (!isTextField())
				mainPanel.selectionItemTypeGroup.setActiveButton(t);
		});
	}

	private AbstractAction setTransformMode(ModelEditorActionType3 t) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		return getAsAction(() -> {
			if (!isTextField() && !mainPanel.animationModeState) {
				mainPanel.actionTypeGroup.setActiveButton(t);
			}
		});
	}

	private AbstractAction setTransformModeAnim(ModelEditorActionType3 t) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		return getAsAction(() -> {
			if (!isTextField()) mainPanel.actionTypeGroup.setActiveButton(t);
		});
	}

	private AbstractAction playKeyboardKeyAction() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		return getAsAction(() -> {
			if (!isTextField()) mainLayoutCreator.getTimeSliderPanel().play();
		});
	}

	private AbstractAction jumpFramesAction(int i) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		return getAsAction(() -> {
			if (!isTextField() && mainPanel.animationModeState) {
				mainLayoutCreator.getTimeSliderPanel().jumpFrames(i);
			}
		});
	}

	private AbstractAction previousKeyframe() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		return getAsAction(() -> {
			if (!isTextField() && mainPanel.animationModeState) {
				mainLayoutCreator.getTimeSliderPanel().jumpToPreviousTime();
			}
		});
	}

	private AbstractAction nextKeyframe() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		return getAsAction(() -> {
			if (!isTextField() && mainPanel.animationModeState) mainLayoutCreator.getTimeSliderPanel().jumpToNextTime();
		});
	}

	private boolean isTextField() {
		return focusedComponentNeedsTyping(getFocusedComponent());
	}

	private boolean isTextField2() {
		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return (focusedComponent instanceof JTextArea)
				|| (focusedComponent instanceof JTextField)
				|| (focusedComponent instanceof JTextPane);
	}

	private boolean focusedComponentNeedsTyping(final Component focusedComponent) {
		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
	}

	private Component getFocusedComponent() {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		return kfm.getFocusOwner();
	}

	private void maximizeFocusedWindow() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (isTextField()) return;
		final View focusedView = mainPanel.rootWindow.getFocusedView();
		if (focusedView != null) {
			if (focusedView.isMaximized()) {
				mainPanel.rootWindow.setMaximizedWindow(null);
			} else {
				focusedView.maximize();
			}
		}
	}

	public void selectAllActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			UndoAction action = ModelEditActions.selectAll(modelPanel.getModelView());
			action.redo();
			modelPanel.getUndoManager().pushAction(action);
		}
	}

	public void invertSelectActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			InvertSelectionAction2 invertSelectionAction = new InvertSelectionAction2(modelPanel.getModelView());
			invertSelectionAction.redo();

			modelPanel.getUndoManager().pushAction(invertSelectionAction);
		}
	}

	public void getExpandSelectionActionRes() {
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
			setSelectionAction.redo();

			modelPanel.getUndoManager().pushAction(setSelectionAction);
		}
	}

	private void expandSelection(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (final GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}

	public void changeTransformMode(ModelEditorActionType3 newType, MainPanel mainPanel) {
		if (newType != null) {
//			ProgramGlobals.getMainPanel().changeActivity(newType);
			mainPanel.changeActivity(newType);
		}
	}

	public void cloneActionRes() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			try {
				ModelView modelView = modelPanel.getModelView();
				CloneAction2 cloneAction = new CloneAction2(modelView, ModelStructureChangeListener.changeListener, modelView.getSelectedVertices(), modelView.getSelectedIdObjects(), modelView.getSelectedCameras());
				cloneAction.redo();

				modelPanel.getUndoManager().pushAction(cloneAction);
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
			ProgramGlobals.getUndoHandler().refreshUndo();
			mainPanel.repaintSelfAndChildren();
			modelPanel.repaintSelfAndRelatedChildren();
		}
	}

	public void deleteActionRes() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			if (mainPanel.animationModeState) {
				mainPanel.getMainLayoutCreator().getTimeSliderPanel().deleteSelectedKeyframes();
			} else {
				ModelView modelView = modelPanel.getModelView();
				DeleteAction deleteAction = new DeleteAction(modelView.getSelectedVertices(), ModelStructureChangeListener.changeListener, modelView);
				DeleteNodesAction deleteNodesAction = new DeleteNodesAction(modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), ModelStructureChangeListener.changeListener, modelView);
				CompoundAction compoundAction = new CompoundAction("deleted components", Arrays.asList(deleteAction, deleteNodesAction));
				compoundAction.redo();
				modelPanel.getUndoManager().pushAction(compoundAction);
			}
			mainPanel.repaintSelfAndChildren();
			modelPanel.repaintSelfAndRelatedChildren();
		}
	}

	public void rigActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			final ModelView modelView = modelPanel.getModelView();
			if (!modelView.getSelectedIdObjects().isEmpty() && !modelView.getSelectedVertices().isEmpty()) {
				modelPanel.getUndoManager().pushAction(ModelEditActions.rig(modelView));
			} else {
				System.err.println("NOT RIGGING, NOT VALID: " + modelView.getSelectedIdObjects().size() + " idObjects and " + modelView.getSelectedVertices() + " vertices selected");
			}
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
