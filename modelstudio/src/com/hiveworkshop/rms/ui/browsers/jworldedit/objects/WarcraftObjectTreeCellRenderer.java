package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.icons.IconUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WarcraftObjectTreeCellRenderer extends DefaultTreeCellRenderer {
	private String replWEUI = "ReplaceableTextures\\WorldEditUI\\";
	private static final int ICON_SIZE = 16;
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
				displayName = MutableObjectData.getDisplayAsRawDataName(unit);
			}
			revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
			// setText(displayName);
			// setPreferredSize(getUI().getPreferredSize(this));
			try {
				BufferedImage img = null;
				try {
					img = IconUtils.getIcon(unit, worldEditorDataType);
				} catch (final Exception e) {
					// img = BLPHandler.get().getGameTex("Textures\\BTNTemp.blp");
				}
				if (img == null) {
					BufferedImage gameTex = BLPHandler.getGameTex(replWEUI + "DoodadPlaceholder.blp");
					Image scaledInstance = gameTex.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_FAST);
					img = IconUtils.worldEditStyleIcon(scaledInstance);
				}
				BufferedImage image = toBufferedImage(img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
				BufferedImage subimage = image.getSubimage(1, 1, ICON_SIZE - 2, ICON_SIZE - 2);
				ImageIcon icon = getImageIcon(subimage);
				setIcon(icon);
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
			String value1 = value.toString() + " (" + leafCount + ")";
			super.getTreeCellRendererComponent(tree, value1, selected, expanded, false, row, hasFocus);
			if (expanded) {
				BufferedImage gameTex = BLPHandler.getGameTex(replWEUI + "Editor-TriggerGroup-Open.blp");
				ImageIcon icon = getImageIcon(gameTex);
				setIcon(icon);
			} else {
				BufferedImage gameTex = BLPHandler.getGameTex(replWEUI + "Editor-TriggerGroup.blp");
				ImageIcon icon = getImageIcon(gameTex);
				setIcon(icon);
			}
			if (((node instanceof SortingFolderTreeNode) && ((SortingFolderTreeNode) node).hasEditedChildren())
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

	private ImageIcon getImageIcon(BufferedImage image) {
		if (image != null) {
			return new ImageIcon(image);
		} else {
			System.out.println("failed to load image");
			BufferedImage bi = BLPHandler.getImage(new Bitmap("Textures\\white.blp"), GameDataFileSystem.getDefault());
			Image scaledInstance = bi.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_FAST);
			return new ImageIcon(scaledInstance);
		}
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
