package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.mesh.DeleteAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.InvertSelectionAction2;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction2;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.CloneAction2;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
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
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainPanelLinkActions {
	static void linkActions(final MainPanel mainPanel, final JComponent root) {
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"), "Undo");
		root.getActionMap().put("Undo", mainPanel.getUndoHandler().getUndoAction());

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"), "Redo");
		root.getActionMap().put("Redo", mainPanel.getUndoHandler().getRedoAction());

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");
		root.getActionMap().put("Delete", mainPanel.deleteAction);

		root.getActionMap().put("CloneSelection", mainPanel.cloneAction);

//		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
////		root.getActionMap().put("shiftSelect", shiftSelectAction(mainPanel));
//		root.getActionMap().put("shiftSelect", new AcAd(e -> shiftSelectActionRes(mainPanel)));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("SPACE"), "MaximizeSpacebar");
		root.getActionMap().put("MaximizeSpacebar", maximizeSpacebarAction(mainPanel));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"), "PressRight");
		root.getActionMap().put("PressRight", pressRightAction(mainPanel));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"), "PressLeft");
		root.getActionMap().put("PressLeft", pressLeftAction(mainPanel));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "PressUp");
		root.getActionMap().put("PressUp", jumpFramesAction(mainPanel, 1));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift UP"), "PressShiftUp");
		root.getActionMap().put("PressShiftUp", jumpFramesAction(mainPanel, 10));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"), "PressDown");
		root.getActionMap().put("PressDown", jumpFramesAction(mainPanel, -1));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift DOWN"), "PressShiftDown");
		root.getActionMap().put("PressShiftDown", jumpFramesAction(mainPanel, -10));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control SPACE"), "PlayKeyboardKey");
		root.getActionMap().put("PlayKeyboardKey", playKeyboardKeyAction(mainPanel));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("W"), "QKeyboardKey");
		root.getActionMap().put("QKeyboardKey", actionShortcutAction(mainPanel, 0));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("E"), "WKeyboardKey");
		root.getActionMap().put("WKeyboardKey", actionShortcutAction(mainPanel, 1));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("R"), "EKeyboardKey");
		root.getActionMap().put("EKeyboardKey", actionShortcutAction(mainPanel, 2));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("T"), "RKeyboardKey");
		root.getActionMap().put("RKeyboardKey", notAnimationActionShortcutAction(mainPanel, 3));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Y"), "TKeyboardKey");
		root.getActionMap().put("TKeyboardKey", notAnimationActionShortcutAction(mainPanel, 4));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("A"), "AKeyboardKey");
		root.getActionMap().put("AKeyboardKey", itemShortcutAction(mainPanel, 0));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"), "SKeyboardKey");
		root.getActionMap().put("SKeyboardKey", itemShortcutAction(mainPanel, 1));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("D"), "DKeyboardKey");
		root.getActionMap().put("DKeyboardKey", itemShortcutAction(mainPanel, 2));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("F"), "FKeyboardKey");
		root.getActionMap().put("FKeyboardKey", itemShortcutAction(mainPanel, 3));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("G"), "GKeyboardKey");
		root.getActionMap().put("GKeyboardKey", itemShortcutAction(mainPanel, 4));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("Z"), "ZKeyboardKey");
		root.getActionMap().put("ZKeyboardKey", zKeyboardKeyAction());

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control F"), "CreateFaceShortcut");
		root.getActionMap().put("CreateFaceShortcut", createFaceShortcutAction(mainPanel));

		for (int i = 1; i <= 9; i++) {
			root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed " + i), i + "KeyboardKey");
			final int index = i;
			root.getActionMap().put(i + "KeyboardKey", getKeyPressedAction(mainPanel, index));
		}

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "shiftSelect");
//		root.getActionMap().put("shiftSelect", shiftSelectAction(mainPanel));
		root.getActionMap().put("shiftSelect", new AcAd(e -> shiftSelectActionRes(mainPanel)));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("alt pressed ALT"), "altSelect");
//		root.getActionMap().put("altSelect", altSelectAction(mainPanel));
		root.getActionMap().put("altSelect", new AcAd(e -> altSelectActionRes(mainPanel)));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released SHIFT"), "unShiftSelect");
//		root.getActionMap().put("unShiftSelect", unShiftSelectAction(mainPanel));
		root.getActionMap().put("unShiftSelect", new AcAd(e -> unShiftSelectActionRes(mainPanel)));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released ALT"), "unAltSelect");
		root.getActionMap().put("unAltSelect", unAltSelectAction(mainPanel));

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control A"), "Select All");
		root.getActionMap().put("Select All", mainPanel.selectAllAction);

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control I"), "Invert Selection");
		root.getActionMap().put("Invert Selection", mainPanel.invertSelectAction);

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control E"), "Expand Selection");
		root.getActionMap().put("Expand Selection", mainPanel.expandSelectionAction);

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control W"), "RigAction");
		root.getActionMap().put("RigAction", mainPanel.rigAction);
	}

	private static AbstractAction unAltSelectAction(MainPanel mainPanel) {
		return new AbstractAction("unAltSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.DESELECT) && mainPanel.cheatAlt) {
//					mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
					mainPanel.selectionModeGroup.setActiveButton(SelectionMode.SELECT);
					mainPanel.cheatAlt = false;
				}
			}
		};
	}

	private static AbstractAction unShiftSelectAction(MainPanel mainPanel) {
		return new AbstractAction("unShiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				unShiftSelectActionRes(mainPanel);
			}
		};
	}

	private static void unShiftSelectActionRes(MainPanel mainPanel) {
		if (isTextField()) return;
		if ((mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.ADD)
				&& mainPanel.cheatShift) {
//			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.SELECT);
			mainPanel.cheatShift = false;
		}
	}

	private static AbstractAction altSelectAction(MainPanel mainPanel) {
		return new AbstractAction("altSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				altSelectActionRes(mainPanel);
			}
		};
	}

	private static void altSelectActionRes(MainPanel mainPanel) {
		if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
//			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.DESELECT);
			mainPanel.cheatAlt = true;
		}
	}

	private static AbstractAction shiftSelectAction(MainPanel mainPanel) {
		return new AbstractAction("shiftSelect") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				shiftSelectActionRes(mainPanel);
			}
		};
	}

	private static void shiftSelectActionRes(MainPanel mainPanel) {
		if (isTextField()) return;
		if (mainPanel.selectionModeGroup.getActiveButtonType() == SelectionMode.SELECT) {
//			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
			mainPanel.selectionModeGroup.setActiveButton(SelectionMode.ADD);
			mainPanel.cheatShift = true;
		}
	}

	private static AbstractAction maximizeSpacebarAction(MainPanel mainPanel) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				maximizeFocusedWindow(mainPanel);
			}
		};
	}

	private static AbstractAction getKeyPressedAction(MainPanel mainPanel, int index) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final DockingWindow window = mainPanel.rootWindow.getWindow();
				if (window instanceof TabWindow) {
					final TabWindow tabWindow = (TabWindow) window;
					final int tabCount = tabWindow.getChildWindowCount();
					if ((index - 1) < tabCount) {
						tabWindow.setSelectedTab(index - 1);
					}
				}
			}
		};
	}

	private static AbstractAction createFaceShortcutAction(MainPanel mainPanel) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				if (!mainPanel.animationModeState) {
					try {
						ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
						if (modelPanel != null) {
							Viewport viewport = mainPanel.viewportListener.getViewport();
							Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//							UndoAction createFaceFromSelection = modelPanel.getModelEditorManager().getModelEditor().createFaceFromSelection(facingVector);
							UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelPanel.getModelView(), facingVector);


							modelPanel.getUndoManager().pushAction(createFaceFromSelection);
						}
					} catch (final FaceCreationException exc) {
						JOptionPane.showMessageDialog(mainPanel, exc.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					} catch (final Exception exc) {
						ExceptionPopup.display(exc);
					}
				}
			}
		};
	}

	private static AbstractAction zKeyboardKeyAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				ProgramGlobals.getPrefs().setViewMode(ProgramGlobals.getPrefs().getViewMode() == 1 ? 0 : 1);
			}
		};
	}

	private static AbstractAction itemShortcutAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
//				mainPanel.selectionItemTypeGroup.setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[i]);
				mainPanel.selectionItemTypeGroup.setActiveButton(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[i]);
			}
		};
	}

	private static AbstractAction notAnimationActionShortcutAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				if (!mainPanel.animationModeState) {
//					mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
					mainPanel.actionTypeGroup.setActiveButton(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
				}
			}
		};
	}

	private static AbstractAction actionShortcutAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
//				mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
				mainPanel.actionTypeGroup.setActiveButton(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
			}
		};
	}

	private static AbstractAction playKeyboardKeyAction(MainPanel mainPanel) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				mainPanel.timeSliderPanel.play();
			}
		};
	}

	private static AbstractAction jumpFramesAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				if (mainPanel.animationModeState) {
					mainPanel.timeSliderPanel.jumpFrames(i);
				}
			}
		};
	}

	private static AbstractAction pressLeftAction(MainPanel mainPanel) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				if (mainPanel.animationModeState) {
					mainPanel.timeSliderPanel.jumpToPreviousTime();
				}
			}
		};
	}

	private static AbstractAction pressRightAction(MainPanel mainPanel) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				if (mainPanel.animationModeState) {
					mainPanel.timeSliderPanel.jumpToNextTime();
				}
			}
		};
	}

	private static boolean isTextField() {
		return focusedComponentNeedsTyping(getFocusedComponent());
	}

	private static void maximizeFocusedWindow(MainPanel mainPanel) {
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

	private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
	}

	private static Component getFocusedComponent() {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		return kfm.getFocusOwner();
	}

	public static void selectAllActionRes(ModelPanel modelPanel) {
		if (modelPanel != null) {
//			UndoAction action = modelPanel.getModelEditorManager().getModelEditor().selectAll();
			UndoAction action = ModelEditActions.selectAll(modelPanel.getModelView());
			action.redo();
			modelPanel.getUndoManager().pushAction(action);
		}
	}

	public static void invertSelectActionRes(ModelPanel modelPanel) {
		if (modelPanel != null) {
//			UndoAction action = modelPanel.getModelEditorManager().getModelEditor().invertSelection();
			InvertSelectionAction2 invertSelectionAction = new InvertSelectionAction2(modelPanel.getModelView());
			invertSelectionAction.redo();

			modelPanel.getUndoManager().pushAction(invertSelectionAction);
		}
	}

	public static void getExpandSelectionActionRes(ModelPanel modelPanel) {
		// ToDo this should be renamed select linked, and a real "expand selection" should be implemented (ie this without the recursive call)
		//  also, maybe care about collision shape vertices...
		if (modelPanel != null) {
			ModelView modelView = modelPanel.getModelView();
			Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());

			for (GeosetVertex v : modelView.getSelectedVertices()) {
				expandSelection(v, expandedSelection);
			}
			SetSelectionAction2 setSelectionAction2 = new SetSelectionAction2(expandedSelection, modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), modelView, "expand selection");
			setSelectionAction2.redo();

			modelPanel.getUndoManager().pushAction(setSelectionAction2);
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

	public static void actionTypeGroupActionRes(MainPanel mainPanel, ModelEditorActionType3 newType) {
		if (newType != null) {
			mainPanel.changeActivity(newType);
		}
	}

//	static void animatedRenderEnvChangeResult(MainPanel mainPanel, int start, int end) {
//		Integer globalSeq = mainPanel.animatedRenderEnvironment.getGlobalSeq();
//		if (globalSeq != null) {
//			mainPanel.creatorPanel.setChosenGlobalSeq(globalSeq);
//		} else {
//			final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
//			if (modelPanel != null) {
//				boolean foundAnim = false;
//				for (final Animation animation : modelPanel.getModel().getAnims()) {
//					if ((animation.getStart() == start) && (animation.getEnd() == end)) {
//						mainPanel.creatorPanel.setChosenAnimation(animation);
//						foundAnim = true;
//						break;
//					}
//				}
//				if (!foundAnim) {
//					mainPanel.creatorPanel.setChosenAnimation(null);
//				}
//			}
//
//		}
//	}

	public static void cloneActionRes(MainPanel mainPanel) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			try {
//				UndoAction cloneAction = modelPanel.getModelEditorManager().getModelEditor().cloneSelectedComponents(mainPanel.namePicker);
				ModelView modelView = modelPanel.getModelView();
				CloneAction2 cloneAction = new CloneAction2(modelView, modelPanel.getModelStructureChangeListener(), modelView.getSelectedVertices(), modelView.getSelectedIdObjects(), modelView.getSelectedCameras());
				cloneAction.redo();

				modelPanel.getUndoManager().pushAction(cloneAction);
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
			mainPanel.getUndoHandler().refreshUndo();
			MainPanel.repaintSelfAndChildren(mainPanel);
			modelPanel.repaintSelfAndRelatedChildren();
		}
	}

	public static void deleteActionRes(MainPanel mainPanel) {
		ModelPanel mpanel = ProgramGlobals.getCurrentModelPanel();
		if (mpanel != null) {
			if (mainPanel.animationModeState) {
				mainPanel.timeSliderPanel.deleteSelectedKeyframes();
			} else {
				ModelView modelView = mpanel.getModelView();
//				UndoAction action = mpanel.getModelEditorManager().getModelEditor().deleteSelectedComponents();
				DeleteAction deleteAction = new DeleteAction(modelView.getSelectedVertices(), mpanel.getModelStructureChangeListener(), modelView);
				DeleteNodesAction deleteNodesAction = new DeleteNodesAction(modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), mpanel.getModelStructureChangeListener(), modelView);
				CompoundAction compoundAction = new CompoundAction("deleted components", Arrays.asList(deleteAction, deleteNodesAction));
				compoundAction.redo();
				mpanel.getUndoManager().pushAction(compoundAction);
			}
			MainPanel.repaintSelfAndChildren(mainPanel);
			mpanel.repaintSelfAndRelatedChildren();
		}
	}

	public static void rigActionRes(ModelPanel modelPanel) {
		if (modelPanel != null) {
//			EditableModel model = modelPanel.getModel();
//			ModelEditorManager editorManager = modelPanel.getModelEditorManager();
//			boolean valid = false;
//			for (Vec3 v : editorManager.getSelectionView().getSelectedVertices()) {
//				int index = model.getPivots().indexOf(v);
//
//				if (index != -1 && index < model.getIdObjects().size()) {
//					IdObject node = model.getIdObject(index);
//					if ((node instanceof Bone) && !(node instanceof Helper)) {
//						valid = true;
//					}
//				}
//			}
			final ModelView modelView = modelPanel.getModelView();
			if (!modelView.getSelectedIdObjects().isEmpty() && !modelView.getSelectedVertices().isEmpty()) {
				modelPanel.getUndoManager().pushAction(ModelEditActions.rig(modelView));
			} else {
				System.err.println("NOT RIGGING, NOT VALID: " + modelView.getSelectedIdObjects().size() + " idObjects and " + modelView.getSelectedVertices() + " vertices selected");
			}
		}
	}

	static AbstractAction getSelectAllAction() {
		return new AbstractAction("Select All") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				selectAllActionRes(ProgramGlobals.getCurrentModelPanel());
			}
		};
	}

	static AbstractAction getInvertSelectAction() {
		return new AbstractAction("Invert Selection") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				invertSelectActionRes(ProgramGlobals.getCurrentModelPanel());
			}
		};
	}

	static AbstractAction getExpandSelectionAction() {
		return new AbstractAction("Expand Selection") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				getExpandSelectionActionRes(ProgramGlobals.getCurrentModelPanel());
			}
		};
	}

	static AbstractAction getRigAction() {
		return new AbstractAction("Rig") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				rigActionRes(ProgramGlobals.getCurrentModelPanel());
			}
		};
	}

	static AbstractAction getCloneAction(final MainPanel mainPanel) {
		return new AbstractAction("CloneSelection") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				cloneActionRes(mainPanel);
			}
		};
	}

	static AbstractAction getDeleteAction(final MainPanel mainPanel) {
		return new AbstractAction("Delete") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				deleteActionRes(mainPanel);
			}
		};
	}

	private static class AcAd extends AbstractAction {
		ActionListener actionListener;

		AcAd(ActionListener actionListener) {
			this.actionListener = actionListener;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			actionListener.actionPerformed(e);
		}
	}
}
