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
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerEnvironment;
import com.hiveworkshop.wc3.util.IconUtils;

public class TriggerTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
	UnitEditorSettings settings = new UnitEditorSettings();
	private Color defaultBackgroundSelectionColor = null;

	public TriggerTreeCellRenderer(final UnitEditorSettings settings) {
		super();
		this.settings = settings;
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
			final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		if (defaultBackgroundSelectionColor == null) {
			defaultBackgroundSelectionColor = getBackgroundSelectionColor();
		}
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node.getUserObject() instanceof TriggerEnvironment) {
			final TriggerEnvironment unit = (TriggerEnvironment) node.getUserObject();
			final String displayName = unit.getName();
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			try {
				final BufferedImage img = BLPHandler.get()
						.getGameTex("ReplaceableTextures\\WorldEditUI\\CampaignEditor-Map.blp");
				setIcon(new ImageIcon(toBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_FAST))));
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
		} else if (node.getUserObject() instanceof Trigger) {
			final Trigger unit = (Trigger) node.getUserObject();
			final String displayName = unit.getName();
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			try {
				final BufferedImage img = IconUtils.worldEditStyleIcon(
						BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-Trigger.blp")
								.getScaledInstance(16, 16, Image.SCALE_FAST));
				setIcon(new ImageIcon(
						toBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_DEFAULT)).getSubimage(1, 1, 14, 14)));
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
		} else {
			super.getTreeCellRendererComponent(tree, value.toString(), selected, expanded, false, row, hasFocus);
			if (expanded) {
				setIcon(new ImageIcon(
						BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
			} else {
				setIcon(new ImageIcon(
						BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
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
