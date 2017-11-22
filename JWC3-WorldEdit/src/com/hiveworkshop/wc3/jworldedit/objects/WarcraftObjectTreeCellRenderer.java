package com.hiveworkshop.wc3.jworldedit.objects;

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
import com.hiveworkshop.wc3.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.util.IconUtils;

public class WarcraftObjectTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
	UnitEditorSettings settings = new UnitEditorSettings();
	private final WorldEditorDataType worldEditorDataType;
	private Color defaultBackgroundSelectionColor = null;

	public WarcraftObjectTreeCellRenderer(final UnitEditorSettings settings,
			final WorldEditorDataType worldEditorDataType) {
		super();
		this.settings = settings;
		this.worldEditorDataType = worldEditorDataType;
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
			final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		if (defaultBackgroundSelectionColor == null) {
			defaultBackgroundSelectionColor = getBackgroundSelectionColor();
		}
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getUserObject() instanceof MutableGameObject) {
			final MutableGameObject unit = (MutableGameObject) node.getUserObject();
			String displayName = unit.getName();
			if (settings.isDisplayAsRawData()) {
				String aliasString = unit.getAlias().toString();
				if (!unit.getAlias().equals(unit.getCode())) {
					aliasString += ":" + unit.getCode().toString();
				}
				displayName = aliasString + " (" + displayName + ")";
			}
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			// setText(displayName);
			// setPreferredSize(getUI().getPreferredSize(this));
			try {
				BufferedImage img;
				try {
					img = IconUtils.getIcon(unit, worldEditorDataType);
				} catch (final Exception e) {
					img = BLPHandler.get().getGameTex("Textures\\BTNTemp.blp");
				}
				if (img == null) {
					img = IconUtils.worldEditStyleIcon(
							BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp")
									.getScaledInstance(16, 16, Image.SCALE_FAST));
				}
				setIcon(new ImageIcon(
						toBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_DEFAULT)).getSubimage(1, 1, 14, 14)));
			} catch (final Exception exc) {
				exc.printStackTrace();
			}
			if (unit.hasEditorData() && !selected) {
				setForeground(settings.getEditedValueColor());
			} else {
				setForeground(null);
			}
		} else {
			int leafCount = node.getLeafCount();
			if (node.isLeaf()) {
				leafCount = 0;
			}
			super.getTreeCellRendererComponent(tree, value.toString() + " (" + leafCount + ")", selected, expanded,
					false, row, hasFocus);
			if (expanded) {
				setIcon(new ImageIcon(
						BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
			} else {
				setIcon(new ImageIcon(
						BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
			}
			if ((node instanceof SortingFolderTreeNode && ((SortingFolderTreeNode) node).hasEditedChildren())
					&& !selected) {
				setForeground(settings.getEditedValueColor());
			} else {
				setForeground(null);
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
