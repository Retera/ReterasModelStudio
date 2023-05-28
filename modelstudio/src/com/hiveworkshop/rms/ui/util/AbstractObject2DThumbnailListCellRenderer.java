package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractObject2DThumbnailListCellRenderer<TYPE> extends DefaultListCellRenderer {
	protected static final Vec3 recModelColor = new Vec3(200, 255, 255);
	protected static final Vec3 donModelColor = new Vec3(220, 180, 255);
	protected static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	protected static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	protected static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	protected static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	protected static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	protected static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	protected static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);
	protected static final Vec3 bg = new Vec3();
	protected static final Vec3 fg = new Vec3();

	private static final int SIZE = 32;
	private static final int QUARTER_SIZE = SIZE / 4;
	private static final int EIGHTH_SIZE = SIZE / 8;
	private final Map<TYPE, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	protected final EditableModel model;
	protected final EditableModel other;
	private static Map<EditableModel, BufferedImage> modelOutlineImageMap;
	private static Map<EditableModel, Vec2[]> modelBoundsSizeMap;
	private ThumbnailHelper thumbnailHelper;

	private static Map<EditableModel, Map<Geoset, Map<Bone, List<GeosetVertex>>>> geosetBoneMap;

	public AbstractObject2DThumbnailListCellRenderer(EditableModel model, EditableModel other) {
		this.model = model;
		this.other = other;
		thumbnailHelper = new ThumbnailHelper(model, other);
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
		geosetBoneMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSel, boolean hasFoc) {
		setBackground(null);
//		TYPE valueTyped = valueToTyped(value);

//		String typeString = valueTyped != null ? valueTyped.toString() : "";
		String typeString = value !=  null ? value.toString() : "";
		super.getListCellRendererComponent(list, typeString, index, isSel, hasFoc);

		setIcon(getImageIcon(value));
		return this;
	}

	public ImageIcon getImageIcon(Object valueTyped) {
		return thumbnailHelper.getImageIcon(valueTyped);
	}
}
