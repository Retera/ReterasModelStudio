package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ModelThumbnailMaker;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelIconHandler {
	private final Map<EditableModel, Map<Geoset, Map<Bone, List<GeosetVertex>>>> geosetBoneMap = new HashMap<>();
	private final Map<IdObject, ImageIcon> idObjectToCachedRenderer = new HashMap<>();
	private final Map<Geoset, ImageIcon> geosetToCachedRenderer = new HashMap<>();
	private final Map<Set<Geoset>, ImageIcon> geosetsToCachedRenderer = new HashMap<>();
	private final Map<Geoset, BufferedImage> geosetToCachedHL = new HashMap<>();
	private final Map<EditableModel, BufferedImage> modelOutlineImageMap = new HashMap<>();
	private final Map<EditableModel, ImageIcon> modelImageMap = new HashMap<>();
	private final Map<EditableModel, Vec2[]> modelBoundsSizeMap = new HashMap<>();

	private final int size;

	public ModelIconHandler(int size) {
		this.size = size;
		modelBoundsSizeMap.put(null, new Vec2[]{new Vec2(-100,-100), new Vec2(100,100)});
	}
	public ModelIconHandler() {
		this(32);
	}

	public ImageIcon getImageIcon(EditableModel model) {
		return getImageIcon(Color.white, model);
	}

	public ImageIcon getImageIcon(Color backgroundColor, EditableModel model) {
		ImageIcon icon = modelImageMap.get(model);
		if (icon == null) {
			try {
				BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, size, size, true);
				graphics.setColor(backgroundColor.brighter());

				makeIcon(backgroundColor, graphics, model);

				graphics.dispose();
				icon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			modelImageMap.put(model, icon);
		}
		return icon;
	}

	private void makeIcon(Color backgroundColor, Graphics graphics, EditableModel model) {
		if (model != null) {
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(size, size), getModelBoundsSize(model));
		}
	}

	public ImageIcon getImageIcon(IdObject idObject, EditableModel model) {
		return getImageIcon(Color.white, idObject, model);
	}

	public ImageIcon getImageIcon(Color backgroundColor, IdObject idObject, EditableModel model) {
		ImageIcon icon = idObjectToCachedRenderer.get(idObject);
		if (icon == null) {
			try {
				BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, size, size, true);
				graphics.setColor(backgroundColor.brighter());

				makeBoneIcon(backgroundColor, idObject, graphics, model);

				graphics.dispose();
				icon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			idObjectToCachedRenderer.put(idObject, icon);
		}
		return icon;
	}

	private void makeBoneIcon(Color backgroundColor, IdObject idObject, Graphics graphics, EditableModel model) {
		if (model.contains(idObject)) {
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(size, size), getModelBoundsSize(model));
			if (idObject instanceof Bone) {
				ModelThumbnailMaker.drawFilteredTriangles2(model, graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, getBoneMap(model), (Bone) idObject);
			}
			ModelThumbnailMaker.drawBoneMarker(graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, idObject.getPivotPoint());
		}
	}

	public ImageIcon getImageIcon(Geoset geoset, EditableModel model) {
		return getImageIcon(Color.white, geoset, model);
	}

	public ImageIcon getImageIcon(Color backgroundColor, Geoset geoset, EditableModel model) {
		ImageIcon myIcon = geosetToCachedRenderer.get(geoset);
		if (myIcon == null) {
			try {
				final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				final Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, size, size, true);
				graphics.setColor(backgroundColor.brighter());

				if (geoset != null) {
					makeGeosetIcon(backgroundColor, geoset, graphics, model);
				}

				graphics.dispose();
				myIcon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			geosetToCachedRenderer.put(geoset, myIcon);
		}
		return myIcon;
	}

	public void makeGeosetIcon(Color backgroundColor, Geoset geoset, Graphics graphics, EditableModel model) {
		if (model == null || model.contains(geoset)) {
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(size, size), getModelBoundsSize(model));
			ModelThumbnailMaker.drawGeosetFlat(graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, geoset, Color.RED);
		}
	}

	public ImageIcon getImageIcon(Set<Geoset> geoset, EditableModel model) {
		return getImageIcon(Color.white, geoset, model);
	}

	public ImageIcon getImageIcon(Color backgroundColor, Set<Geoset> geosets, EditableModel model) {
		ImageIcon myIcon = geosetsToCachedRenderer.get(geosets);
		if (myIcon == null) {
			try {
				final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				final Graphics graphics = image.getGraphics();

				BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
				graphics.drawImage(modelOutline, 0, 0, null);

				for(Geoset geoset : geosets){
					if (geoset != null) {
						BufferedImage geosetTransparentIcon = getGeosetTransparentIcon(geoset, model);
						graphics.drawImage(geosetTransparentIcon, 0, 0, null);
					}
				}

				graphics.dispose();
				myIcon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			geosetsToCachedRenderer.put(geosets, myIcon);
		}
		return myIcon;
	}

	public BufferedImage getGeosetTransparentIcon(Geoset geoset, EditableModel model) {
		BufferedImage image = geosetToCachedHL.get(geoset);
		if (image == null) {
			image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			final Graphics graphics = image.getGraphics();
			graphics.setColor(new Color(255,255,255,0));
			graphics.fill3DRect(0, 0, size, size, false);
			if (model.contains(geoset)) {
				ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(size, size), getModelBoundsSize(model));
				ModelThumbnailMaker.drawGeosetFlat(graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, geoset, Color.RED);
			}
			graphics.dispose();
			geosetToCachedHL.put(geoset, image);
		}
		return image;
	}

	public void makeGeosetTransparentIcon(Geoset geoset, Graphics graphics1, EditableModel model) {
		if (model.contains(geoset)) {
			final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			final Graphics graphics = image.getGraphics();
			graphics.setColor(new Color(255,255,255,0));
			graphics.fill3DRect(0, 0, size, size, false);
			ModelThumbnailMaker.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(size, size), getModelBoundsSize(model));
			ModelThumbnailMaker.drawGeosetFlat(graphics, Vec3.Y_AXIS, Vec3.Z_AXIS, geoset, Color.RED);
		}
	}

	private BufferedImage getModelOutlineImage(Color backgroundColor, EditableModel model) {
		if (modelOutlineImageMap.containsKey(model)) {
			return modelOutlineImageMap.get(model);
		} else if(model == null) {
			return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		} else {
			BufferedImage image = ModelThumbnailMaker.getBufferedImage(backgroundColor, model, size, getModelBoundsSize(model));
			modelOutlineImageMap.put(model, image);
			return image;
		}
	}

	private Vec2[] getModelBoundsSize(EditableModel model) {
		if (modelBoundsSizeMap.containsKey(model)) {
			return modelBoundsSizeMap.get(model);
		} else {
			Vec2[] boundSize = ModelThumbnailMaker.getBoundBoxSize(model, Vec3.Y_AXIS, Vec3.Z_AXIS);
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
