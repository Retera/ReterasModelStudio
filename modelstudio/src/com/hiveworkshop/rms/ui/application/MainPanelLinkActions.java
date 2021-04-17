package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainPanelLinkActions {
	static void linkActions(final MainPanel mainPanel, final JComponent root) {
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Z"), "Undo");
		root.getActionMap().put("Undo", mainPanel.undoAction);

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control Y"), "Redo");
		root.getActionMap().put("Redo", mainPanel.redoAction);

		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");
		root.getActionMap().put("Delete", mainPanel.deleteAction);

		root.getActionMap().put("CloneSelection", mainPanel.cloneAction);

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
		root.getActionMap().put("ZKeyboardKey", zKeyboardKeyAction(mainPanel));

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
					mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
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
			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.SELECT);
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
			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.DESELECT);
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
			mainPanel.selectionModeGroup.setToolbarButtonType(SelectionMode.ADD);
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
						final ModelPanel modelPanel = mainPanel.currentModelPanel();
						if (modelPanel != null) {
							final Viewport viewport = mainPanel.viewportListener.getViewport();
							final Vec3 facingVector = viewport == null
									? new Vec3(0, 0, 1) : viewport.getFacingVector();
							final UndoAction createFaceFromSelection = modelPanel.getModelEditorManager()
									.getModelEditor().createFaceFromSelection(facingVector);
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

	private static AbstractAction zKeyboardKeyAction(MainPanel mainPanel) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				mainPanel.prefs.setViewMode(mainPanel.prefs.getViewMode() == 1 ? 0 : 1);
			}
		};
	}

	private static AbstractAction itemShortcutAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				mainPanel.selectionItemTypeGroup.setToolbarButtonType(mainPanel.selectionItemTypeGroup.getToolbarButtonTypes()[i]);
			}
		};
	}

	private static AbstractAction notAnimationActionShortcutAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				if (!mainPanel.animationModeState) {
					mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
				}
			}
		};
	}

	private static AbstractAction actionShortcutAction(MainPanel mainPanel, int i) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (isTextField()) return;
				mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[i]);
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
