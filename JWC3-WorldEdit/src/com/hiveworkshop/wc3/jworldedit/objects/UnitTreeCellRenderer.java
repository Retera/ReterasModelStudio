package com.hiveworkshop.wc3.jworldedit.objects;

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
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftObject;

public class UnitTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
	UnitEditorSettings settings = new UnitEditorSettings();
	public UnitTreeCellRenderer(UnitEditorSettings settings) {
		super();
		this.settings = settings;
	}
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		if (node.getUserObject() instanceof WarcraftObject) {
			WarcraftObject unit = (WarcraftObject)node.getUserObject();
			String displayName = unit.getName();
			if( settings.isDisplayAsRawData() ) {
				displayName = unit.getId() + " (" + displayName + ")";
			}
			this.revalidate();
			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
//			setText(displayName);
//			setPreferredSize(getUI().getPreferredSize(this));
			try {
				BufferedImage img = unit.getImage();
				setIcon(new ImageIcon(toBufferedImage(img.getScaledInstance(16,16, Image.SCALE_DEFAULT)).getSubimage(1, 1, 14, 14)));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		} else {
			int leafCount = node.getLeafCount();
			if( node.isLeaf() )
				leafCount = 0;
			super.getTreeCellRendererComponent(tree, value.toString() + " (" + leafCount + ")", selected, expanded, false, row, hasFocus);
			if( expanded ) {
				setIcon(new ImageIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
			}
			else
				setIcon(new ImageIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
		}
		return this;
	}
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
}
