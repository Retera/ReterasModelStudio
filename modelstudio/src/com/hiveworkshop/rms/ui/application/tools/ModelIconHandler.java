package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ModelThumbnailMaker;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelIconHandler {
	private final Map<EditableModel, Map<Geoset, Map<Bone, List<GeosetVertex>>>> geosetBoneMap = new HashMap<>();
	private final Map<IdObject, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	private final Map<EditableModel, BufferedImage> modelOutlineImageMap = new HashMap<>();
	private final Map<EditableModel, Vec2[]> modelBoundsSizeMap = new HashMap<>();

	ModelIconHandler(){

	}

	public ImageIcon getImageIcon(IdObject idObject, EditableModel model) {
		ImageIcon icon = matrixShellToCachedRenderer.get(idObject);
		if (icon == null) {
			try {
				Color backgroundColor = Color.white;

				BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, 32, 32, true);
				graphics.setColor(backgroundColor.brighter());

				makeBoneIcon(backgroundColor, idObject, graphics, model);

				graphics.dispose();
				icon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			matrixShellToCachedRenderer.put(idObject, icon);
		}
		return icon;
	}

	private void makeBoneIcon(Color backgroundColor, IdObject idObject, Graphics graphics, EditableModel model) {
		if (model.contains(idObject)) {
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(32, 32), getModelBoundsSize(model));
			if (idObject instanceof Bone) {
				ModelThumbnailMaker.drawFilteredTriangles2(model, graphics, (byte) 1, (byte) 2, getBoneMap(model), (Bone) idObject);
			}
			ModelThumbnailMaker.drawBoneMarker(graphics, (byte) 1, (byte) 2, idObject.getPivotPoint());
		}
	}

	private BufferedImage getModelOutlineImage(Color backgroundColor, EditableModel model) {
		if (modelOutlineImageMap.containsKey(model)) {
			return modelOutlineImageMap.get(model);
		} else {
			BufferedImage image = ModelThumbnailMaker.getBufferedImage(backgroundColor, model, 32, getModelBoundsSize(model));
			modelOutlineImageMap.put(model, image);
			return image;
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

}
