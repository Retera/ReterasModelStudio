package com.hiveworkshop.wc3.jworldedit.triggers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.jworldedit.WorldEditArt;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.triggers.gui.TriggerCategoryTreeNode;
import com.hiveworkshop.wc3.jworldedit.triggers.gui.TriggerEnvironmentRootNode;
import com.hiveworkshop.wc3.jworldedit.triggers.gui.TriggerTreeNode;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerEnvironment;

public class TriggerTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
	UnitEditorSettings settings = new UnitEditorSettings();
	private Color defaultBackgroundSelectionColor = null;
	private final WorldEditArt worldEditArt;

	public TriggerTreeCellRenderer(final UnitEditorSettings settings, final WorldEditArt worldEditArt) {
		super();
		this.settings = settings;
		this.worldEditArt = worldEditArt;
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
			final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		if (defaultBackgroundSelectionColor == null) {
			defaultBackgroundSelectionColor = getBackgroundSelectionColor();
		}
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node instanceof TriggerEnvironmentRootNode) {
			final TriggerEnvironmentRootNode rootNode = (TriggerEnvironmentRootNode) node;
			final TriggerEnvironment triggerEnv = rootNode.getTriggerEnvironment();
			final String displayName = triggerEnv.getName();
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			try {
				final BufferedImage img = BLPHandler.get()
						.getGameTex("ReplaceableTextures\\WorldEditUI\\CampaignEditor-Map.blp");
				setIcon(new ImageIcon(toBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_FAST))));
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
		} else if (node instanceof TriggerTreeNode) {
			final TriggerTreeNode triggerTreeNode = (TriggerTreeNode) node;
			final Trigger trigger = triggerTreeNode.getTrigger();
			final String displayName = trigger.getName();
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			try {
				if (trigger.isComment()) {
					setIcon(worldEditArt.getIcon("SEIcon_TriggerComment"));
				} else {
					setIcon(worldEditArt.getIcon("SEIcon_Trigger"));
				}
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
		} else if (node instanceof TriggerCategoryTreeNode) {
			final TriggerCategoryTreeNode triggerTreeNode = (TriggerCategoryTreeNode) node;
			final TriggerCategory trigger = triggerTreeNode.getCategory();
			final String displayName = trigger.getName();
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			try {
				if (expanded) {
					setIcon(worldEditArt.getIcon("SEIcon_TriggerCategoryOpen"));
				} else {
					setIcon(worldEditArt.getIcon("SEIcon_TriggerCategory"));
				}
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
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
