package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.preferences.KeyBindingPrefs;

import javax.swing.*;

public class MainPanelLinkActions {


//	public void linkActions(final JComponent root) {
//		int isAncestor = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
//		InputMap inputMap = root.getInputMap(isAncestor);
//		ActionMap actionMap = root.getActionMap();
//
//		inputMap.put(KeyStroke.getKeyStroke("control Z"), "Undo");
//		actionMap.put("Undo", getAsAction(() -> ProgramGlobals.getUndoHandler().undo()));
//
//		inputMap.put(KeyStroke.getKeyStroke("control Y"), "Redo");
//		actionMap.put("Redo", getAsAction(() -> ProgramGlobals.getUndoHandler().redo()));
//
//		inputMap.put(KeyStroke.getKeyStroke("DELETE"), "Delete");
//		actionMap.put("Delete", getAsAction("Delete", () -> deleteActionRes()));
//
//		actionMap.put("CloneSelection", getAsAction("CloneSelection", () -> cloneActionRes()));
//
////		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
//////		root.getActionMap().put("shiftSelect", shiftSelectAction(mainPanel));
////		root.getActionMap().put("shiftSelect", new AcAd(e -> shiftSelectActionRes(mainPanel)));
//
//		inputMap.put(KeyStroke.getKeyStroke("SPACE"), "MaximizeSpacebar");
//		actionMap.put("MaximizeSpacebar", getAsAction(() -> maximizeFocusedWindow()));
//
//		inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "PressRight");
//		actionMap.put("PressRight", nextKeyframe());
//
//		inputMap.put(KeyStroke.getKeyStroke("LEFT"), "PressLeft");
//		actionMap.put("PressLeft", previousKeyframe());
//
//		inputMap.put(KeyStroke.getKeyStroke("UP"), "PressUp");
//		actionMap.put("PressUp", jumpFramesAction(1));
//
//		inputMap.put(KeyStroke.getKeyStroke("shift UP"), "PressShiftUp");
//		actionMap.put("PressShiftUp", jumpFramesAction(10));
//
//		inputMap.put(KeyStroke.getKeyStroke("DOWN"), "PressDown");
//		actionMap.put("PressDown", jumpFramesAction(-1));
//
//		inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), "PressShiftDown");
//		actionMap.put("PressShiftDown", jumpFramesAction(-10));
//
//		inputMap.put(KeyStroke.getKeyStroke("control SPACE"), "PlayKeyboardKey");
//		actionMap.put("PlayKeyboardKey", playKeyboardKeyAction());
//
//		inputMap.put(KeyStroke.getKeyStroke("W"), "WKeyboardKey");
//		actionMap.put("WKeyboardKey", setTransformModeAnim(ModelEditorActionType3.TRANSLATION));
//
//		inputMap.put(KeyStroke.getKeyStroke("E"), "EKeyboardKey");
//		actionMap.put("EKeyboardKey", setTransformModeAnim(ModelEditorActionType3.SCALING));
//
//		inputMap.put(KeyStroke.getKeyStroke("R"), "RKeyboardKey");
//		actionMap.put("RKeyboardKey", setTransformModeAnim(ModelEditorActionType3.ROTATION));
//
//		inputMap.put(KeyStroke.getKeyStroke("T"), "TKeyboardKey");
//		actionMap.put("TKeyboardKey", setTransformMode(ModelEditorActionType3.EXTRUDE));
//
//		inputMap.put(KeyStroke.getKeyStroke("Y"), "YKeyboardKey");
//		actionMap.put("YKeyboardKey", setTransformMode(ModelEditorActionType3.EXTEND));
//
//		inputMap.put(KeyStroke.getKeyStroke("A"), "Selection_Type_ANIMATE");
//		actionMap.put("Selection_Type_ANIMATE", setSelectionType(SelectionItemTypes.ANIMATE));
//
//		inputMap.put(KeyStroke.getKeyStroke("S"), "Selection_Type_VERTEX");
//		actionMap.put("Selection_Type_VERTEX", setSelectionType(SelectionItemTypes.VERTEX));
//
//		inputMap.put(KeyStroke.getKeyStroke("D"), "Selection_Type_CLUSTER");
//		actionMap.put("Selection_Type_CLUSTER", setSelectionType(SelectionItemTypes.CLUSTER));
//
//		inputMap.put(KeyStroke.getKeyStroke("F"), "Selection_Type_FACE");
//		actionMap.put("Selection_Type_FACE", setSelectionType(SelectionItemTypes.FACE));
//
//		inputMap.put(KeyStroke.getKeyStroke("G"), "Selection_Type_GROUP");
//		actionMap.put("Selection_Type_GROUP", setSelectionType(SelectionItemTypes.GROUP));
//
//		inputMap.put(KeyStroke.getKeyStroke("Z"), "ZKeyboardKey");
//		actionMap.put("ZKeyboardKey", toggleWireFrame());
//
//		inputMap.put(KeyStroke.getKeyStroke("control F"), "CreateFaceShortcut");
//		actionMap.put("CreateFaceShortcut", getAsAction(() -> createFace()));
//
//		for (int i = 1; i <= 9; i++) {
//			inputMap.put(KeyStroke.getKeyStroke("alt pressed " + i), i + "KeyboardKey");
//			int index = i;
//			actionMap.put(i + "KeyboardKey", getAsAction(() -> selectTab(index)));
//		}
//
//		inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
//		actionMap.put("shiftSelect", getAsAction("shiftSelect", () -> Select.shiftSelectActionRes()));
//
//		inputMap.put(KeyStroke.getKeyStroke("alt pressed ALT"), "altSelect");
//		actionMap.put("altSelect", getAsAction("altSelect", () -> Select.altSelectActionRes()));
//
//		inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), "unShiftSelect");
//		actionMap.put("unShiftSelect", getAsAction("unShiftSelect", () -> Select.unShiftSelectActionRes()));
//
//		inputMap.put(KeyStroke.getKeyStroke("released ALT"), "unAltSelect");
//		actionMap.put("unAltSelect", getAsAction("unAltSelect", () -> Select.unAltSelect()));
//
//		inputMap.put(KeyStroke.getKeyStroke("control A"), "Select All");
//		actionMap.put("Select All", getAsAction("Select All", () -> Select.selectAll()));
//
//		inputMap.put(KeyStroke.getKeyStroke("control I"), "Invert Selection");
//		actionMap.put("Invert Selection", getAsAction("Invert Selection", () -> Select.invertSelectActionRes()));
//
//		inputMap.put(KeyStroke.getKeyStroke("control E"), "Expand Selection");
//		actionMap.put("Expand Selection", getAsAction("Expand Selection", () -> Select.expandSelection()));
//
//		inputMap.put(KeyStroke.getKeyStroke("control W"), "RigAction");
//		actionMap.put("RigAction", getAsAction("Rig", () -> rigActionRes()));
//	}

	public void linkActions2(final JComponent root) {
//		int isAncestor = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
//		InputMap inputMap = root.getInputMap(isAncestor);
//		ActionMap actionMap = root.getActionMap();

//		KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getPrefs().getKeyBindingPrefs();
//		root.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
//		root.setActionMap(keyBindingPrefs.getActionMap());
		KeyBindingPrefs keyBindingPrefs = ProgramGlobals.getKeyBindingPrefs();
		root.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyBindingPrefs.getInputMap());
		root.setActionMap(keyBindingPrefs.getActionMap());
	}

//	private void selectTab(int index) {
//		final DockingWindow window = ProgramGlobals.getMainPanel().rootWindow.getWindow();
//		if (window instanceof TabWindow) {
//			final TabWindow tabWindow = (TabWindow) window;
//			final int tabCount = tabWindow.getChildWindowCount();
//			if ((index - 1) < tabCount) {
//				tabWindow.setSelectedTab(index - 1);
//			}
//		}
//	}

//	private void createFace() {
//		if (!isTextField() && !isAnimationModeState()) {
//			try {
//				ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//				if (modelPanel != null) {
//					Viewport viewport = ProgramGlobals.getMainPanel().getViewportListener().getViewport();
//					Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//					UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelPanel.getModelView(), facingVector);
//
//					modelPanel.getUndoManager().pushAction(createFaceFromSelection);
//				}
//			} catch (final FaceCreationException exc) {
//				JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//			} catch (final Exception exc) {
//				ExceptionPopup.display(exc);
//			}
//		}
//	}

//	private boolean isAnimationModeState() {
//		return ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE;
//	}

//	private AbstractAction toggleWireFrame() {
//		return getAsAction(() -> {if (!isTextField())ProgramGlobals.getPrefs().setViewMode(ProgramGlobals.getPrefs().getViewMode() == 1 ? 0 : 1); });
//	}
//
//	private AbstractAction setSelectionType(SelectionItemTypes t) {
//		return getAsAction(() -> {if (!isTextField()) ProgramGlobals.setSelectionTypeButton(t);});
//	}
//
//	private AbstractAction setTransformMode(ModelEditorActionType3 t) {
//		return getAsAction(() -> {
//			if (!isTextField() && !isAnimationModeState()) {
//				ProgramGlobals.setEditorActionTypeButton(t);
//			}
//		});
//	}
//
//	private AbstractAction setTransformModeAnim(ModelEditorActionType3 t) {
//		return getAsAction(() -> {
//			if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(t);
//		});
//	}
//
//	private AbstractAction playKeyboardKeyAction() {
//		MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
//		return getAsAction(() -> {
//			if (!isTextField()) mainLayoutCreator.getTimeSliderPanel().play();
//		});
//	}
//
//	private AbstractAction jumpFramesAction(int i) {
//		MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
//		return getAsAction(() -> {
//			if (!isTextField() && isAnimationModeState()) {
//				mainLayoutCreator.getTimeSliderPanel().jumpFrames(i);
//			}
//		});
//	}
//
//	private AbstractAction previousKeyframe() {
//		MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
//		return getAsAction(() -> {
//			if (!isTextField() && isAnimationModeState()) {
//				mainLayoutCreator.getTimeSliderPanel().jumpToPreviousTime();
//			}
//		});
//	}
//
//	private AbstractAction nextKeyframe() {
//		MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
//		return getAsAction(() -> {
//			if (!isTextField() && isAnimationModeState()) mainLayoutCreator.getTimeSliderPanel().jumpToNextTime();
//		});
//	}

//	private boolean isTextField() {
//		return focusedComponentNeedsTyping(getFocusedComponent());
//	}
//
////	private boolean isTextField2() {
////		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
////		return (focusedComponent instanceof JTextArea)
////				|| (focusedComponent instanceof JTextField)
////				|| (focusedComponent instanceof JTextPane);
////	}

//	private boolean focusedComponentNeedsTyping(final Component focusedComponent) {
//		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
//	}
//
//	private Component getFocusedComponent() {
//		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//		return kfm.getFocusOwner();
//	}

//	private void maximizeFocusedWindow() {
//		if (isTextField()) return;
//		View focusedView = ProgramGlobals.getMainPanel().rootWindow.getFocusedView();
//		if (focusedView != null) {
//			if (focusedView.isMaximized()) {
//				ProgramGlobals.getMainPanel().rootWindow.setMaximizedWindow(null);
//			} else {
//				focusedView.maximize();
//			}
//		}
//	}
//
//	public void changeTransformMode(ModelEditorActionType3 newType, MainPanel mainPanel) {
//		if (newType != null) {
////			ProgramGlobals.getMainPanel().changeActivity(newType);
//			mainPanel.changeActivity(newType);
//		}
//	}
//
//	public static void cloneActionRes() {
//		MainPanel mainPanel = ProgramGlobals.getMainPanel();
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
//			try {
//				ModelView modelView = modelPanel.getModelView();
//				CloneAction2 cloneAction = new CloneAction2(modelView, ModelStructureChangeListener.changeListener, modelView.getSelectedVertices(), modelView.getSelectedIdObjects(), modelView.getSelectedCameras());
//				cloneAction.redo();
//
//				modelPanel.getUndoManager().pushAction(cloneAction);
//			} catch (final Exception exc) {
//				ExceptionPopup.display(exc);
//			}
//			ProgramGlobals.getUndoHandler().refreshUndo();
//			mainPanel.repaintSelfAndChildren();
//			modelPanel.repaintSelfAndRelatedChildren();
//		}
//	}
//
//	public void deleteActionRes() {
//		MainPanel mainPanel = ProgramGlobals.getMainPanel();
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
//			if (isAnimationModeState()) {
//				mainPanel.getMainLayoutCreator().getTimeSliderPanel().getKeyframeHandler().deleteSelectedKeyframes();
//			} else {
//				ModelView modelView = modelPanel.getModelView();
//				DeleteAction deleteAction = new DeleteAction(modelView.getSelectedVertices(), ModelStructureChangeListener.changeListener, modelView);
//				DeleteNodesAction deleteNodesAction = new DeleteNodesAction(modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), ModelStructureChangeListener.changeListener, modelView);
//				CompoundAction compoundAction = new CompoundAction("deleted components", Arrays.asList(deleteAction, deleteNodesAction));
//				compoundAction.redo();
//				modelPanel.getUndoManager().pushAction(compoundAction);
//			}
//			mainPanel.repaintSelfAndChildren();
//			modelPanel.repaintSelfAndRelatedChildren();
//		}
//	}
//
//	public void rigActionRes() {
//		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//		if (modelPanel != null) {
//			ModelView modelView = modelPanel.getModelView();
//			if (!modelView.getSelectedIdObjects().isEmpty() && !modelView.getSelectedVertices().isEmpty()) {
//				modelPanel.getUndoManager().pushAction(ModelEditActions.rig(modelView));
//			} else {
//				System.err.println("NOT RIGGING, NOT VALID: " + modelView.getSelectedIdObjects().size() + " idObjects and " + modelView.getSelectedVertices() + " vertices selected");
//			}
//		}
//	}

//	private AbstractAction getAsAction(String name, Runnable action) {
//		return new AbstractAction(name) {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				action.run();
//			}
//		};
//	}
//
//	private AbstractAction getAsAction(Runnable action) {
//		return new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				action.run();
//			}
//		};
//	}
}
