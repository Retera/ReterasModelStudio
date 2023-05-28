package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ModelThumbnailMaker;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThumbnailHelper {
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
	private final Map<Object, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	protected EditableModel model;
	protected EditableModel other;
	private static Map<EditableModel, BufferedImage> modelOutlineImageMap;
	private static Map<EditableModel, Vec2[]> modelBoundsSizeMap;

	private static Map<EditableModel, Map<Geoset, Map<Bone, List<GeosetVertex>>>> geosetBoneMap;

	public ThumbnailHelper() {
		this(null, null);
	}
	public ThumbnailHelper(EditableModel model) {
		this(model, null);
	}
	public ThumbnailHelper(EditableModel model, EditableModel other) {
		this.model = model;
		this.other = other;
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
		geosetBoneMap = new HashMap<>();
	}


	public ImageIcon getImageIcon(Object valueTyped) {
		ImageIcon myIcon = matrixShellToCachedRenderer.get(valueTyped);
		if (myIcon == null) {
			try {
				Color backgroundColor = getBackgroundColor(valueTyped);

				BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, SIZE, SIZE, true);
				graphics.setColor(backgroundColor.brighter());

				makeBoneIcon(backgroundColor, valueTyped, graphics, other);
				makeBoneIcon(backgroundColor, valueTyped, graphics, model);

				graphics.dispose();
				myIcon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			matrixShellToCachedRenderer.put(valueTyped, myIcon);
		}
		return myIcon;
	}

	private Color getBackgroundColor(Object valueType) {
		if (valueType instanceof IdObjectShell<?> && ((IdObjectShell<?>) valueType).isFromDonating()
				|| valueType instanceof IdObject && other != null && other.contains((IdObject)valueType)) {
			return donModelColor.asIntColor();
		} else if (valueType instanceof IdObjectShell<?> && !((IdObjectShell<?>) valueType).isFromDonating()
				|| valueType instanceof IdObject && model != null && model.contains((IdObject)valueType)) {
			return recModelColor.asIntColor();
		}
		return noOwnerBgCol.asIntColor();
	}

	public void makeBoneIcon(Color backgroundColor, Object object, Graphics graphics, EditableModel model) {
		IdObject node = getNode(object);
		if (contains(model, node)) {
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), getModelBoundsSize(model));

			if (node instanceof Bone) {
				ModelThumbnailMaker.drawFilteredTriangles2(model, graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, getBoneMap(model), (Bone) node);

				ModelThumbnailMaker.drawCrossHair(graphics, Color.YELLOW, Vec3.Y_AXIS, Vec3.Z_AXIS, node.getPivotPoint());
			} else if (node != null){
				ModelThumbnailMaker.drawCrossHair(graphics, Color.YELLOW, Vec3.Y_AXIS, Vec3.Z_AXIS, node.getPivotPoint());
			}
		}
	}

	private IdObject getNode(Object object){
		if(object instanceof IdObject) {
			return (IdObject) object;
		} else if (object instanceof IdObjectShell<?> && ((IdObjectShell<?>) object).getIdObject() != null) {
			return ((IdObjectShell<?>) object).getIdObject();
		}
		return null;
	}

	private Vec2[] getModelBoundsSize(EditableModel model) {
		return modelBoundsSizeMap.computeIfAbsent(model, m -> ModelThumbnailMaker.getBoundBoxSize(m, Vec3.Y_AXIS, Vec3.Z_AXIS));
	}

	private BufferedImage getModelOutlineImage(Color backgroundColor, EditableModel model) {
		return modelOutlineImageMap.computeIfAbsent(model, m -> ModelThumbnailMaker.getBufferedImage(backgroundColor, m, SIZE, getModelBoundsSize(m)));
	}

	private Map<Geoset, Map<Bone, List<GeosetVertex>>> getBoneMap(EditableModel model) {
		return geosetBoneMap.computeIfAbsent(model, m -> m.getGeosets().stream().collect(Collectors.toMap(g->g, Geoset::getBoneMap)));
	}

	protected boolean contains(EditableModel model, IdObject object) {
		if (model != null) {
			return model.contains(object);
		}
		return false;
	}
}
