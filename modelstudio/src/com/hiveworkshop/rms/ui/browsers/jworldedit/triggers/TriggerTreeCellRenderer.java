package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WorldEditArt;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WorldEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui.TriggerCategoryTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui.TriggerEnvironmentRootNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui.TriggerTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerEnvironment;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TriggerTreeCellRenderer extends DefaultTreeCellRenderer {
	private final WorldEditorSettings settings;
	private Color defaultBackgroundSelectionColor = null;
	private final WorldEditArt worldEditArt;

	public TriggerTreeCellRenderer(final WorldEditorSettings settings, final WorldEditArt worldEditArt) {
		super();
		this.settings = settings;
		this.worldEditArt = worldEditArt;
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object node, final boolean selected,
												  final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		System.out.println("getTreeCellRendererComponent");
		if (defaultBackgroundSelectionColor == null) {
			defaultBackgroundSelectionColor = getBackgroundSelectionColor();
		}
		if (node instanceof TriggerEnvironmentRootNode) {
			final TriggerEnvironmentRootNode rootNode = (TriggerEnvironmentRootNode) node;
			final TriggerEnvironment triggerEnv = rootNode.getTriggerEnvironment();
			final String displayName = triggerEnv.getName();
			revalidate();
			try {
				final BufferedImage img = BLPHandler.get()
						.getGameTex("ReplaceableTextures\\WorldEditUI\\CampaignEditor-Map.blp");
				final ImageIcon mapIcon = new ImageIcon(
						toBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_FAST)));
				setOpenIcon(mapIcon);
				setClosedIcon(mapIcon);
				setLeafIcon(mapIcon);
				System.out.println("leaf is MAP");
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
		} else if (node instanceof TriggerTreeNode) {
			final TriggerTreeNode triggerTreeNode = (TriggerTreeNode) node;
			final Trigger trigger = triggerTreeNode.getTrigger();
			final String displayName = trigger.getName();
			revalidate();
			try {
				if (trigger.isComment()) {
					setLeafIcon(worldEditArt.getIcon("SEIcon_TriggerComment"));
					System.out.println("leaf is COMMENT");
				} else {
					setLeafIcon(worldEditArt.getIcon("SEIcon_Trigger"));
					System.out.println("leaf is TRIGGER");
				}
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			if (trigger.isComment()) {
				setForeground(settings.getTriggerCommentColor());
			}
		} else if (node instanceof TriggerCategoryTreeNode) {
			final TriggerCategoryTreeNode triggerTreeNode = (TriggerCategoryTreeNode) node;
			final TriggerCategory trigger = triggerTreeNode.getCategory();
			final String displayName = trigger.getName();
			revalidate();
			try {
				setOpenIcon(worldEditArt.getIcon("SEIcon_TriggerCategoryOpen"));
				setLeafIcon(worldEditArt.getIcon("SEIcon_TriggerCategory"));
				System.out.println("leaf is CATEGORY");
				setClosedIcon(worldEditArt.getIcon("SEIcon_TriggerCategory"));
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
		} else {
			setLeafIcon(worldEditArt.getIcon("SEIcon_FunctionDisabled"));
			System.out.println("leaf is STATE CORRUPTION");
			// final TreePath selectionPath = tree.getSelectionPath();
			// if (tree.getSelectionCount() == 1) {
			// switch (selectionPath.getPathCount()) {
			// case 1:
			// try {
			// final BufferedImage img = BLPHandler.get()
			// .getGameTex("ReplaceableTextures\\WorldEditUI\\CampaignEditor-Map.blp");
			// final ImageIcon mapIcon = new ImageIcon(
			// toBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_FAST)));
			// setLeafIcon(mapIcon);
			// } catch (final Exception exc) {
			// exc.printStackTrace();
			// }
			// break;
			// case 2:
			// setLeafIcon(worldEditArt.getIcon("SEIcon_TriggerCategory"));
			// break;
			// case 3:
			// setLeafIcon(worldEditArt.getIcon("SEIcon_Trigger"));
			// break;
			// }
			// }
			super.getTreeCellRendererComponent(tree, node, selected, expanded, leaf, row, hasFocus);
		}
		if (selected) {
			if (tree.hasFocus()) {
				setForeground(settings.getSelectedValueColor());
				setBackgroundSelectionColor(defaultBackgroundSelectionColor);
			} else {
				setForeground(null);
				setBackgroundSelectionColor(settings.getSelectedUnfocusedValueColor());
			}
		}
		return this;
	}

	public static BufferedImage toBufferedImage(final Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		final Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
}
