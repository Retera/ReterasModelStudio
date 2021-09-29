package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ModelThumbnailMaker;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ObjectShell;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSnapshottingListCellRenderer2D<TYPE> extends DefaultListCellRenderer {
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
	private final Map<TYPE, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	private final EditableModel model;
	private final EditableModel other;
	private static Map<EditableModel, BufferedImage> modelOutlineImageMap;
	private static Map<EditableModel, Vec2[]> modelBoundsSizeMap;

	private static Map<EditableModel, Map<Geoset, Map<Bone, List<GeosetVertex>>>> geosetBoneMap;

	public AbstractSnapshottingListCellRenderer2D(EditableModel model, EditableModel other) {
		this.model = model;
		this.other = other;
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
		geosetBoneMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSel, boolean hasFoc) {
		setBackground(null);
		TYPE valueType = valueToType(value);

		super.getListCellRendererComponent(list, valueType.toString(), index, isSel, hasFoc);

		setIcon(getImageIcon(valueType));
		return this;
	}

	private ImageIcon getImageIcon(TYPE valueType) {
		ImageIcon myIcon = matrixShellToCachedRenderer.get(valueType);
		if (myIcon == null) {
			try {
				Color backgroundColor = getBackgroundColor(valueType);

				BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, SIZE, SIZE, true);
				graphics.setColor(backgroundColor.brighter());

				makeBoneIcon(backgroundColor, valueType, graphics, other);
				makeBoneIcon(backgroundColor, valueType, graphics, model);

				graphics.dispose();
				myIcon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			matrixShellToCachedRenderer.put(valueType, myIcon);
		}
		return myIcon;
	}

	public void makeBoneIcon(Color backgroundColor, TYPE valueType, Graphics graphics, EditableModel model) {
		if (contains(model, valueType)) {
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), getModelBoundsSize(model));
			if (valueType instanceof BoneShell) {
				ModelThumbnailMaker.drawFilteredTriangles2(model, graphics, (byte) 1, (byte) 2, getBoneMap(model), ((BoneShell) valueType).getBone());
			} else if (valueType instanceof ObjectShell && ((ObjectShell) valueType).getIdObject() instanceof Bone) {
				ModelThumbnailMaker.drawFilteredTriangles2(model, graphics, (byte) 1, (byte) 2, getBoneMap(model), (Bone) ((ObjectShell) valueType).getIdObject());
			}
			ModelThumbnailMaker.drawBoneMarker(graphics, (byte) 1, (byte) 2, getRenderVertex(valueType));
		}
	}

	private Vec2[] getModelBoundsSize(EditableModel model) {
		if (modelBoundsSizeMap.containsKey(model)) {
			return modelBoundsSizeMap.get(model);
		} else {
			Vec2[] boundSize = ModelThumbnailMaker.getBoundBoxSize(model, (byte) 1, (byte) 2);
			modelBoundsSizeMap.put(model, boundSize);
			return boundSize;
		}
	}

	private BufferedImage getModelOutlineImage(Color backgroundColor, EditableModel model) {
		if (modelOutlineImageMap.containsKey(model)) {
//			System.out.println("fetching icon for model: " + model.getName());
//			System.out.println("nr geosets: " + model.getGeosets().size());
			return modelOutlineImageMap.get(model);
		} else {
			BufferedImage image = ModelThumbnailMaker.getBufferedImage(backgroundColor, model, SIZE, getModelBoundsSize(model));
			modelOutlineImageMap.put(model, image);
			return image;
		}
	}

	private BufferedImage getBufferedImage(Color backgroundColor, EditableModel model) {
		BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(backgroundColor);
		graphics.fill3DRect(0, 0, SIZE, SIZE, true);
		graphics.setColor(backgroundColor.brighter());
		graphics.fill3DRect(EIGHTH_SIZE, EIGHTH_SIZE, SIZE - QUARTER_SIZE, SIZE - QUARTER_SIZE, true);


//			System.out.println("creating icon for model: " + model.getName());
//			System.out.println("nr geosets: " + model.getGeosets().size());
//			System.out.println("bounds: " + Arrays.toString(getModelBoundsSize(model)));
		ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), getModelBoundsSize(model));

		ModelThumbnailMaker.drawGeosetsFlat(model, graphics, (byte) 1, (byte) 2, Color.GRAY);
		graphics.dispose();
		return image;
	}

	private Color getBackgroundColor(TYPE valueType) {
		if (isFromDonating(valueType)) {
			return donModelColor.asIntColor();
		} else if (isFromReceiving(valueType)) {
			return recModelColor.asIntColor();
		}
		return noOwnerBgCol.asIntColor();
	}

	private Map<Geoset, Map<Bone, List<GeosetVertex>>> getBoneMap(EditableModel model) {
		if (geosetBoneMap.containsKey(model)) {
			return geosetBoneMap.get(model);
		} else {
			Map<Geoset, Map<Bone, List<GeosetVertex>>> boneMap = new HashMap<>();
			for (Geoset geoset : model.getGeosets()) {
				boneMap.put(geoset, geoset.getBoneMap());
			}
			geosetBoneMap.put(model, boneMap);
			return boneMap;
		}
	}

	protected abstract boolean isFromDonating(TYPE value);

	protected abstract boolean isFromReceiving(TYPE value);

	protected abstract TYPE valueToType(Object value);

	protected abstract Vec3 getRenderVertex(TYPE value);

	protected abstract boolean contains(EditableModel model, TYPE object);
}
