package com.hiveworkshop.wc3.gui.modeledit.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

/**
 * The one way a docked editing view claims the Delete, Cut, Copy and Paste
 * hotkeys.
 * <p>
 * The main window binds these keys on the root pane with
 * {@code WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}. Swing resolves such a binding by
 * walking up from the focus owner and using the first component whose
 * {@code InputMap} knows the key, together with that same component's
 * {@code ActionMap}. So a view that wants its own Delete must (1) be focusable,
 * (2) actually take focus when clicked, and (3) register the key in its own
 * maps. Cut, Copy and Paste are dispatched by the menu bar through
 * {@link TransferActionListener}, which looks up the action by name in the
 * focus owner's {@code ActionMap}; for those only (1), (2) and an
 * {@code ActionMap} entry are needed.
 * <p>
 * Views that forget any of the three steps silently fall through to the root
 * pane binding, which does nothing outside animation mode. Use this helper
 * instead of repeating the recipe.
 */
public final class EditingHotkeys {
	public static final String DELETE_ACTION_KEY = "Delete";

	private EditingHotkeys() {
	}

	/**
	 * Makes the component focusable, focuses it on mouse press, and routes the
	 * Delete key to the given handler while it (or a descendant) has focus.
	 */
	public static void installDelete(final JComponent component, final Runnable onDelete) {
		claimFocus(component);
		component.getActionMap().put(DELETE_ACTION_KEY, new AbstractAction(DELETE_ACTION_KEY) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				onDelete.run();
			}
		});
		component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"),
				DELETE_ACTION_KEY);
	}

	/**
	 * Makes the component focusable, focuses it on mouse press, and lets the Edit
	 * menu's Cut, Copy and Paste items (and their accelerators) act on the given
	 * transfer handler while the component has focus.
	 */
	public static void installClipboard(final JComponent component, final TransferHandler transferHandler) {
		claimFocus(component);
		component.setTransferHandler(transferHandler);
		final ActionMap map = component.getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
	}

	/**
	 * Same as {@link #installClipboard(JComponent, TransferHandler)} but with plain
	 * callbacks, for views whose clipboard is not a Swing transferable.
	 */
	public static void installClipboard(final JComponent component, final Runnable onCut, final Runnable onCopy,
			final Runnable onPaste) {
		claimFocus(component);
		final ActionMap map = component.getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME), runnableAction("cut", onCut));
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), runnableAction("copy", onCopy));
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), runnableAction("paste", onPaste));
	}

	/**
	 * Focusable plus request-focus-on-press. Idempotent.
	 */
	public static void claimFocus(final JComponent component) {
		if (Boolean.TRUE.equals(component.getClientProperty(EditingHotkeys.class))) {
			return;
		}
		component.putClientProperty(EditingHotkeys.class, Boolean.TRUE);
		component.setFocusable(true);
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (!component.isFocusOwner()) {
					component.requestFocusInWindow();
				}
			}
		});
	}

	/**
	 * True when a single-key hotkey (Delete, Space, digits...) must be left alone
	 * because the focused component is consuming typed text: text fields and
	 * areas, editable combo boxes, spinners, and trees or tables with an open cell
	 * editor.
	 */
	public static boolean needsTyping(final Component focusedComponent) {
		if (focusedComponent == null) {
			return false;
		}
		if (focusedComponent instanceof JTextComponent) {
			return true;
		}
		if (focusedComponent instanceof JSpinner) {
			return true;
		}
		if (focusedComponent instanceof JComboBox) {
			return ((JComboBox<?>) focusedComponent).isEditable();
		}
		if (focusedComponent instanceof JTree) {
			return ((JTree) focusedComponent).isEditing();
		}
		if (focusedComponent instanceof JTable) {
			return ((JTable) focusedComponent).isEditing();
		}
		// a cell editor's text field is focused while editing; it is a JTextComponent
		// and already handled, but its parent chain may be what is asked about
		Component parent = focusedComponent.getParent();
		while (parent != null) {
			if ((parent instanceof JTree) && ((JTree) parent).isEditing()) {
				return true;
			}
			if ((parent instanceof JTable) && ((JTable) parent).isEditing()) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	private static Action runnableAction(final String name, final Runnable runnable) {
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				runnable.run();
			}
		};
	}
}
