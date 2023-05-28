package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.tools.ModelIconHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.GeosetShell;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeosetListCellRenderer2D extends DefaultListCellRenderer {
	protected static final Vec4 recModelColor = new Vec4(200, 255, 255, 255);
	protected static final Vec4 donModelColor = new Vec4(220, 180, 255, 255);
	protected static final Vec4 selectedOwnerBgCol = new Vec4(130, 230, 170, 255);
	protected static final Vec4 selectedOwnerFgCol = new Vec4(0, 0, 0, 255);
//	protected static final Vec4 otherOwnerBgCol = new Vec4(160, 160, 160, 255);
	protected static final Vec4 otherOwnerBgCol = new Vec4(0, 0, 0, 160);
//	protected static final Vec4 dontImpBgCol = new Vec4(200, 200, 200, 255);
	protected static final Vec4 dontImpBgCol = new Vec4(0, 0, 0, 200);
	protected static final Vec4 otherOwnerFgCol = new Vec4(60, 60, 60, 255);
	protected static final Vec4 noOwnerBgCol = new Vec4(255, 255, 255, 0);
	protected static final Vec4 noOwnerFgCol = new Vec4(0, 0, 0, 255);
	protected static final Vec4 hLAdjBgCol = new Vec4(0, 0, 50, 0);
	protected static final Vec4 bg = new Vec4();
	protected static final Vec4 fg = new Vec4();


	private static final int SIZE = 32;
	private static final int QUARTER_SIZE = SIZE / 4;
	private static final int EIGHTH_SIZE = SIZE / 8;
	private static Map<EditableModel, BufferedImage> modelOutlineImageMap;
	private static Map<EditableModel, Vec2[]> modelBoundsSizeMap;
	private final Map<GeosetShell, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	private final EditableModel model;
	private final EditableModel other;

	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private List<GeosetShell> geosetShellList;

	public GeosetListCellRenderer2D(EditableModel model, EditableModel other) {
		this.model = model;
		this.other = other;
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean chf) {
		super.getListCellRendererComponent(list, value.toString(), index, isSelected, chf);

		bg.set(noOwnerBgCol);
		fg.set(noOwnerFgCol);

		if (value instanceof GeosetShell) {
			bg.set(noOwnerBgCol);
			fg.set(noOwnerFgCol);
			setIcon(getIcon((GeosetShell) value));

			if (!((GeosetShell) value).isDoImport()) {
				bg.set(dontImpBgCol);
			}
			if(geosetShellList != null && geosetShellList.contains(((GeosetShell) value)) || isSelected){
				bg.add(hLAdjBgCol);
			}
			setBackground(bg.asIntColor());

		} else {
			setBackground(null);
			setIcon(getIcon(null));
		}
		return this;
	}

	private ImageIcon getIcon(GeosetShell geosetShell){
		if (geosetShell != null && geosetShell.getGeoset() != null) {
			Color backgroundColor = geosetShell.isFromDonating() ? donModelColor.asIntColor() : recModelColor.asIntColor();
			return iconHandler.getImageIcon(backgroundColor, geosetShell.getGeoset(), geosetShell.getGeoset().getParentModel());
		} else {
			return iconHandler.getImageIcon(noOwnerBgCol.asIntColor(), null);
		}
	}

	public void setSelectionList(List<GeosetShell> geosetShellList){
		this.geosetShellList = geosetShellList;
	}

	protected boolean contains(EditableModel model, Geoset object) {
		return model.getGeosets().contains(object);
	}
}
