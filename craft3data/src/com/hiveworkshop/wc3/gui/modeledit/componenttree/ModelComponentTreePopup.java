package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.mdl.IdObject;

/**
 * Builds the right-click menu for a Model tab row.
 */
public final class ModelComponentTreePopup {
	private ModelComponentTreePopup() {
	}

	public static JPopupMenu build(final ComponentRef ref, final ModelComponentTreeController controller,
			final ModelComponentNavigationListener navigation) {
		return build(ref, ref.isComponent() ? Collections.singletonList(ref) : Collections.<ComponentRef>emptyList(),
				controller, navigation);
	}

	/**
	 * @param ref       the row under the mouse
	 * @param selection every selected component row; when it holds more than the
	 *                  clicked row, Cut, Copy and Delete act on all of it
	 */
	public static JPopupMenu build(final ComponentRef ref, final List<ComponentRef> selection,
			final ModelComponentTreeController controller, final ModelComponentNavigationListener navigation) {
		final JPopupMenu menu = new JPopupMenu();
		final List<ComponentRef> targets = new ArrayList<>();
		if (ref.isComponent()) {
			boolean clickedInSelection = false;
			for (final ComponentRef selected : selection) {
				clickedInSelection |= selected.getItem() == ref.getItem();
			}
			if (clickedInSelection && (selection.size() > 1)) {
				targets.addAll(selection);
			} else {
				targets.add(ref);
			}
		}
		final String plural = targets.size() > 1 ? " " + targets.size() + " Components" : "";
		if (ref.isComponent()) {
			final boolean navigable = ref.isNode() || (ref.getKind() == ComponentKind.GEOSET)
					|| (ref.getKind() == ComponentKind.CAMERA);
			if (navigable) {
				menu.add(item("Open in Editor", null, e -> navigation.openInEditor(ref.getItem())));
			}
			menu.add(item("Open in Tracks", null, e -> navigation.openInTracks(ref.getItem())));
			menu.addSeparator();
		}
		if (ref.isComponent()) {
			final JMenuItem cut = item("Cut" + plural, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK),
					e -> controller.cut(targets));
			cut.setEnabled(controller.canCopy(targets));
			menu.add(cut);
			final JMenuItem copy = item("Copy" + plural, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
					e -> controller.copy(targets));
			copy.setEnabled(controller.canCopy(targets));
			menu.add(copy);
		}
		final JMenuItem paste = item("Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK),
				e -> controller.paste(ref));
		paste.setEnabled(controller.canPaste());
		menu.add(paste);
		if (ref.isComponent()) {
			menu.add(item("Delete" + plural, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
					e -> controller.delete(targets, false)));
			boolean anyChildren = false;
			for (final ComponentRef target : targets) {
				anyChildren |= target.isNode() && !target.asNode().getChildrenNodes().isEmpty();
			}
			if (anyChildren) {
				menu.add(item("Delete" + plural + " with Children", null, e -> controller.delete(targets, true)));
			}
		}
		if (ref.isNode()) {
			final IdObject node = ref.asNode();
			menu.addSeparator();
			final JMenuItem moveLeft = item("Move Left (out of parent)", null, e -> controller.moveLeft(node));
			moveLeft.setEnabled(controller.canMoveLeft(node));
			menu.add(moveLeft);
			final JMenuItem moveRight = item("Move Right (under sibling above)", null,
					e -> controller.moveRight(node));
			moveRight.setEnabled(controller.canMoveRight(node));
			menu.add(moveRight);
		}
		menu.addSeparator();
		menu.add(buildNewMenu(ref, controller));
		return menu;
	}

	private static JMenu buildNewMenu(final ComponentRef ref, final ModelComponentTreeController controller) {
		final JMenu newMenu = new JMenu("New");
		final ComponentKind preferred = ref.isNode() ? ComponentKind.BONE
				: ComponentKind.defaultForGroup(ref.getGroupName());
		if (preferred != null) {
			final String label = ref.isNode() ? "Child " + preferred.getDisplayName() : preferred.getDisplayName();
			newMenu.add(item(label, null, e -> controller.createNew(preferred, ref), preferred));
			newMenu.addSeparator();
		}
		JMenu nodes = null;
		for (final ComponentKind kind : ComponentKind.values()) {
			final JMenuItem entry = item(kind.getDisplayName(), null, e -> controller.createNew(kind, ref), kind);
			if (kind.isNode()) {
				if (nodes == null) {
					nodes = new JMenu(ref.isNode() ? "Child Node" : "Node");
					newMenu.add(nodes);
				}
				nodes.add(entry);
			} else {
				newMenu.add(entry);
			}
		}
		return newMenu;
	}

	private interface Handler {
		void run(ActionEvent e);
	}

	private static JMenuItem item(final String label, final KeyStroke accelerator, final Handler handler) {
		return item(label, accelerator, handler, null);
	}

	private static JMenuItem item(final String label, final KeyStroke accelerator, final Handler handler,
			final ComponentKind iconKind) {
		final JMenuItem item = new JMenuItem(new AbstractAction(label) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				handler.run(e);
			}
		});
		if (accelerator != null) {
			// shown for discoverability; the real bindings come from EditingHotkeys
			item.setAccelerator(accelerator);
		}
		if (iconKind != null) {
			final ImageIcon icon = iconFor(iconKind);
			if (icon != null) {
				item.setIcon(icon);
			}
		}
		return item;
	}

	private static ImageIcon iconFor(final ComponentKind kind) {
		try {
			return new ImageIcon(RMSIcons.loadNodeImage(kind.getIconName()));
		} catch (final Exception e) {
			return null;
		}
	}
}
