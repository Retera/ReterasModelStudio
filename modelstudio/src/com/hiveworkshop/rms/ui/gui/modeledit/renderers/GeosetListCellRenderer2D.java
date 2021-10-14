package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.tools.ModelIconHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.GeosetShell;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GeosetListCellRenderer2D extends DefaultListCellRenderer {
	protected static final Vec3 recModelColor = new Vec3(200, 255, 255);
	protected static final Vec3 donModelColor = new Vec3(220, 180, 255);
	protected static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	protected static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	protected static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	protected static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	protected static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	protected static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	protected static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);

	private static final int SIZE = 32;
	private static final int QUARTER_SIZE = SIZE / 4;
	private static final int EIGHTH_SIZE = SIZE / 8;
	private static Map<EditableModel, BufferedImage> modelOutlineImageMap;
	private static Map<EditableModel, Vec2[]> modelBoundsSizeMap;
	private final Map<GeosetShell, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	private final EditableModel model;
	private final EditableModel other;

	ModelIconHandler iconHandler = new ModelIconHandler();


	public GeosetListCellRenderer2D(EditableModel model, EditableModel other) {
		this.model = model;
		this.other = other;
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean chf) {

		Color backgroundColor = noOwnerBgCol.asIntColor();
		GeosetShell geosetShell = null;

		if (value instanceof GeosetShell) {
			if (((GeosetShell) value).isFromDonating()) {
				backgroundColor = donModelColor.asIntColor();
			} else {
				backgroundColor = recModelColor.asIntColor();
			}
			geosetShell = (GeosetShell) value;
		}
		setBackground(null);


		super.getListCellRendererComponent(list, value.toString(), index, isSelected, chf);

		ImageIcon myIcon;
		if (geosetShell != null && geosetShell.getGeoset() != null) {
			myIcon = iconHandler.getImageIcon(backgroundColor, geosetShell.getGeoset(), geosetShell.getGeoset().getParentModel());
		} else {
			myIcon = iconHandler.getImageIcon(backgroundColor, null);

		}
		setIcon(myIcon);
		return this;
	}

	protected boolean contains(EditableModel model, Geoset object) {
		return model.getGeosets().contains(object);
	}
}
