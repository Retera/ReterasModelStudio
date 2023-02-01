package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.application.tools.ModelIconHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeosetListRenderer extends DefaultListCellRenderer {
	protected static final Vec4 recModelColor = new Vec4(200, 255, 255, 255);
//	protected static final Vec4 donModelColor = new Vec4(220, 180, 255, 255);
//	protected static final Vec4 selectedOwnerBgCol = new Vec4(130, 230, 170, 255);
//	protected static final Vec4 selectedOwnerFgCol = new Vec4(0, 0, 0, 255);
////	protected static final Vec4 otherOwnerBgCol = new Vec4(160, 160, 160, 255);
//	protected static final Vec4 otherOwnerBgCol = new Vec4(0, 0, 0, 160);
////	protected static final Vec4 dontImpBgCol = new Vec4(200, 200, 200, 255);
//	protected static final Vec4 dontImpBgCol = new Vec4(0, 0, 0, 200);
//	protected static final Vec4 otherOwnerFgCol = new Vec4(60, 60, 60, 255);
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
	private final Map<Geoset, ImageIcon> matrixToCachedRenderer = new HashMap<>();
	private final EditableModel model;

	private final ModelIconHandler iconHandler;
	private List<Geoset> geosetList;

	public GeosetListRenderer(EditableModel model, int imageSize) {
		this.model = model;
		iconHandler = new ModelIconHandler(imageSize);
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean chf) {

		bg.set(noOwnerBgCol);
		fg.set(noOwnerFgCol);

		if (value instanceof Geoset) {
			super.getListCellRendererComponent(list, ((Geoset) value).getName(), index, isSelected, chf);
			bg.set(noOwnerBgCol);
			fg.set(noOwnerFgCol);
			setIcon(getIcon((Geoset) value));

			if(geosetList != null && geosetList.contains(((Geoset) value)) || isSelected){
				bg.add(hLAdjBgCol);
			}
			setBackground(bg.asIntColor());

		} else {
			super.getListCellRendererComponent(list, value.toString(), index, isSelected, chf);
			setBackground(null);
			setIcon(getIcon(null));
		}
		return this;
	}

	private ImageIcon getIcon(Geoset geoset){
		if (geoset != null) {
			return iconHandler.getImageIcon(recModelColor.asIntColor(), geoset, geoset.getParentModel());
		} else {
			return iconHandler.getImageIcon(noOwnerBgCol.asIntColor(), null);
		}
	}

	public void setSelectionList(List<Geoset> geosetList){
		this.geosetList = geosetList;
	}

	protected boolean contains(EditableModel model, Geoset object) {
		return model.getGeosets().contains(object);
	}
}
