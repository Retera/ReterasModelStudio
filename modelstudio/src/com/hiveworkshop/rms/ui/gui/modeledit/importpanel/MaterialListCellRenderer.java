package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

class MaterialListCellRenderer extends DefaultListCellRenderer {
	protected static final Vec3 recModelColor = new Vec3(200, 255, 255);
	protected static final Vec3 donModelColor = new Vec3(220, 180, 255);
	EditableModel myModel;
	Object myMaterial;
	private static final int SIZE = 64;
	Font theFont = new Font("Arial", Font.BOLD, 32);
	HashMap<Material, ImageIcon> map = new HashMap<>();
	private static final int EIGHTH_SIZE = SIZE / 8;
	private static final int SIXTEENTH_SIZE = SIZE / 16;
	GeosetShell geosetShell;

	public MaterialListCellRenderer(final EditableModel model) {
		myModel = model;
	}

	public void setSelectedGeoset(GeosetShell geoset) {
		geosetShell = geoset;
	}

	public void setMaterial(final Object o) {
		myMaterial = o;
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		Material material = (Material) value;
		String name = (material).getName();
		if (geosetShell != null && geosetShell.getOldMaterial() == material) {
			name = name + " (Original)";
		}
		BufferedImage materialImage = (material).getBufferedImage(myModel.getWrappedDataSource());
		if (myModel.contains(material)) {
			super.getListCellRendererComponent(list, name, index, iss, chf);
			setIcon(makeMaterialIcon(material, materialImage, recModelColor.asIntColor()));
		} else {
			super.getListCellRendererComponent(list, "Import: " + name, index, iss, chf);
			setIcon(makeMaterialIcon(material, materialImage, donModelColor.asIntColor()));
		}
		setFont(theFont);
		return this;
	}

	public ImageIcon makeMaterialIcon(Material material, BufferedImage materialImage, Color color) {
		ImageIcon materialIcon = map.get(material);
		if (materialIcon == null) {
			BufferedImage outlineImage = getModelOutlineImage(color);

			final BufferedImage materialIconImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
			final Graphics graphics = materialIconImage.getGraphics();

			graphics.drawImage(outlineImage, 0, 0, null);
			graphics.drawImage(materialImage, SIXTEENTH_SIZE, SIXTEENTH_SIZE, SIZE - EIGHTH_SIZE, SIZE - EIGHTH_SIZE, null);

			graphics.dispose();
			materialIcon = new ImageIcon(materialIconImage);
			map.put(material, materialIcon);
		}

		return materialIcon;
	}


	private BufferedImage getModelOutlineImage(Color backgroundColor) {
		final BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		final Graphics graphics = image.getGraphics();
		graphics.setColor(backgroundColor);
		graphics.fill3DRect(0, 0, SIZE, SIZE, true);

		graphics.dispose();
		return image;
	}
}
