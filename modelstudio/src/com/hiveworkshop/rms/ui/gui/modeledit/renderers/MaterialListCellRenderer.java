package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.GeosetShell;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MaterialListCellRenderer extends DefaultListCellRenderer {
	private static final Vec3 recModelColor = new Vec3(200, 255, 255);
	private static final Vec3 donModelColor = new Vec3(220, 180, 255);
	private static final Color bgColor = new Color(130, 230, 170);
	private static final int SIZE = 64;
	private static final int EIGHTH_SIZE = SIZE / 8;
	private static final int SIXTEENTH_SIZE = SIZE / 16;

	private final Font theFont = new Font("Arial", Font.BOLD, 16);

	private final EditableModel model;
	private final HashMap<Material, ImageIcon> map = new HashMap<>();

	private GeosetShell geosetShell;
	private final Set<GeosetShell> geosetShells = new HashSet<>();

	public MaterialListCellRenderer(final EditableModel model) {
		this.model = model;
	}

	public void setSelectedGeosets(Collection<GeosetShell> geosets) {
		geosetShells.clear();
		geosetShells.addAll(geosets);
		geosetShell = null;
	}
	public void setSelectedGeoset(GeosetShell geoset) {
		geosetShell = geoset;
		geosetShells.clear();
//		geosetShells.add(geoset);
	}

	public void setMaterial(final Object o) {
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		Material material = (Material) value;
		String name = (material).getName();
		if (geosetShell != null && geosetShell.getOldMaterial() == material) {
			name = name + " (Original)";
		}
		Color color;
		if (model.contains(material)) {
			color = recModelColor.asIntColor();
		} else {
//			name = "Import: " + name;
//			name = name;
			color = donModelColor.asIntColor();
		}
		super.getListCellRendererComponent(list, name, index, iss, chf);
		BufferedImage materialImage = ImageCreator.getBufferedImage((material), model.getWrappedDataSource());
		setIcon(makeMaterialIcon(material, materialImage, color));
		setFont(theFont);

		if(!geosetShells.isEmpty()){
			long count = geosetShells.stream().filter(g -> g.getMaterial() == material).count();
			if (count == geosetShells.size()) {
				setBackground(bgColor);
			} else if (count != 0) {
				setBackground(bgColor.brighter());
			} else {
				setBackground(null);
			}
		} else {
			if (geosetShell != null && geosetShell.getMaterial() == material) {
				setBackground(bgColor);
			} else {
				setBackground(null);
			}
		}

//		if (geosetShell != null && geosetShell.getMaterial() == material) {
//			setBackground(bgColor);
//		} else if (geosetShells.stream().anyMatch(gs -> gs.getMaterial() == material)) {
//			setBackground(bgColor);
//		} else {
//			setBackground(null);
//		}


		return this;
	}

	public ImageIcon makeMaterialIcon(Material material, BufferedImage materialImage, Color color) {
		ImageIcon materialIcon = map.get(material);
		if (materialIcon == null) {
			BufferedImage outlineImage = getOutlineImage(color);

			BufferedImage materialIconImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics graphics = materialIconImage.getGraphics();

			graphics.drawImage(outlineImage, 0, 0, null);
			graphics.drawImage(materialImage, SIXTEENTH_SIZE, SIXTEENTH_SIZE, SIZE - EIGHTH_SIZE, SIZE - EIGHTH_SIZE, null);

			graphics.dispose();
			materialIcon = new ImageIcon(materialIconImage);
			map.put(material, materialIcon);
		}

		return materialIcon;
	}


	private BufferedImage getOutlineImage(Color backgroundColor) {
		BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(backgroundColor);
		graphics.fill3DRect(0, 0, SIZE, SIZE, true);

		graphics.dispose();
		return image;
	}
}
