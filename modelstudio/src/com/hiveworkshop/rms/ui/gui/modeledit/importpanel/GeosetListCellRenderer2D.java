package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportModelRenderer;
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
	private final ModelView modelDisplay;
	private final ModelView otherDisplay;


	public GeosetListCellRenderer2D(final ModelView modelDisplay, final ModelView otherDisplay) {
		this.modelDisplay = modelDisplay;
		this.otherDisplay = otherDisplay;
		modelOutlineImageMap = new HashMap<>();
		modelBoundsSizeMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean chf) {

		Color backgroundColor = noOwnerBgCol.asIntColor();
		GeosetShell geoset = null;

		if (value instanceof GeosetShell) {
			if (((GeosetShell) value).isFromDonating()) {
				backgroundColor = donModelColor.asIntColor();
			} else {
				backgroundColor = recModelColor.asIntColor();
			}
			geoset = (GeosetShell) value;
		}
		setBackground(null);


		super.getListCellRendererComponent(list, value.toString(), index, isSelected, chf);

		ImageIcon myIcon = getImageIcon(backgroundColor, geoset);
		setIcon(myIcon);
		return this;
	}

	private ImageIcon getImageIcon(Color backgroundColor, GeosetShell geosetShell) {
		ImageIcon myIcon = matrixShellToCachedRenderer.get(geosetShell);
		if (myIcon == null) {
			try {
				final BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
				final Graphics graphics = image.getGraphics();
				graphics.setColor(backgroundColor);
				graphics.fill3DRect(0, 0, SIZE, SIZE, true);
				graphics.setColor(backgroundColor.brighter());

				if (geosetShell != null) {
					makeGeosetIcon(backgroundColor, geosetShell, graphics, otherDisplay);
					makeGeosetIcon(backgroundColor, geosetShell, graphics, modelDisplay);
				}

				graphics.dispose();
				myIcon = new ImageIcon(image);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			matrixShellToCachedRenderer.put(geosetShell, myIcon);
		}
		return myIcon;
	}

	public void makeGeosetIcon(Color backgroundColor, GeosetShell geoset, Graphics graphics, ModelView modelDisplay) {
		if (modelDisplay != null && contains(modelDisplay, geoset.getGeoset())) {
			EditableModel model = modelDisplay.getModel();
			BufferedImage modelOutline = getModelOutlineImage(backgroundColor, model);
			graphics.drawImage(modelOutline, 0, 0, null);
			ViewportModelRenderer.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), getModelBoundsSize(model));
			ViewportModelRenderer.drawGeosetFlat(graphics, (byte) 1, (byte) 2, geoset.getGeoset(), Color.RED);
		}
	}

	private Vec2[] getModelBoundsSize(EditableModel model) {
		if (modelBoundsSizeMap.containsKey(model)) {
			return modelBoundsSizeMap.get(model);
		} else {
			Vec2[] boundSize = ViewportModelRenderer.getBoundBoxSize(model, (byte) 1, (byte) 2);
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
			final BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
			final Graphics graphics = image.getGraphics();
			graphics.setColor(backgroundColor);
			graphics.fill3DRect(0, 0, SIZE, SIZE, true);
			graphics.setColor(backgroundColor.brighter());
			graphics.fill3DRect(EIGHTH_SIZE, EIGHTH_SIZE, SIZE - QUARTER_SIZE, SIZE - QUARTER_SIZE, true);

			ViewportModelRenderer.scaleAndTranslateGraphic((Graphics2D) graphics, new Rectangle(SIZE, SIZE), getModelBoundsSize(model));

			ViewportModelRenderer.drawGeosetsFlat(model, graphics, (byte) 1, (byte) 2, Color.GRAY);
			modelOutlineImageMap.put(model, image);

			graphics.dispose();
			return image;
		}
	}

	protected boolean contains(ModelView modelDisp, Geoset object) {
		return modelDisp.getModel().getGeosets().contains(object);
	}
}
