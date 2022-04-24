package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.MpqDataSourceDescriptor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DataTreeRenderer extends DefaultTreeCellRenderer {
	private static final ImageIcon CASCIcon;
	private static final ImageIcon MPQIcon;
	private static final ImageIcon FolderIcon;

	static {
		CASCIcon    = getImageIcon("/UI/Widgets/ReteraStudio/DataSourceIcons/CASC.png");
		MPQIcon     = getImageIcon("/UI/Widgets/ReteraStudio/DataSourceIcons/MPQ.png");
		FolderIcon  = getImageIcon("/UI/Widgets/ReteraStudio/DataSourceIcons/Folder.png");
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
	                                              final boolean expanded, final boolean leaf, final int row,
	                                              final boolean hasFocus) {
		Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		JLabel label = (JLabel) comp;

		if (value instanceof DataSourceDescTreeNode) {
			final DataSourceDescriptor descriptor = ((DataSourceDescTreeNode) value).getDescriptor();
			if (descriptor instanceof CascDataSourceDescriptor) {
				label.setIcon(CASCIcon);
			} else if (descriptor instanceof MpqDataSourceDescriptor) {
				label.setIcon(MPQIcon);
			} else if (descriptor instanceof FolderDataSourceDescriptor) {
				label.setIcon(FolderIcon);
			} else {
				label.setIcon(null);
			}
			if (descriptor.getPath() != null && !new File(descriptor.getPath()).exists()){
				setForeground(Color.RED.darker());
			} else {
				setForeground(null);
			}
		}
		return comp;
	}

	private static ImageIcon getImageIcon(String iconPath) {
		ImageIcon imageIcon = null;
		try {
			URL resource = DataSourceChooserPanel.class.getResource(iconPath);
			if(resource != null){
				imageIcon = new ImageIcon(ImageIO.read(resource).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
			}
		} catch (final IOException ignored) {
		}
		return imageIcon;
	}
}
