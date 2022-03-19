package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.icons.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarcraftObject extends GameObject {
	String id;
	WarcraftData dataSource;

	public WarcraftObject(final String id, final WarcraftData dataSource) {
		this.id = id;
		this.dataSource = dataSource;
	}

	@Override
	public void setField(final String field, final String value, final int index) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				element.setField(field, value, index);
				return;
			}
		}
	}

	@Override
	public String getField(final String field, final int index) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				return element.getField(field, index);
			}
		}
		return "";
	}

	@Override
	public int getFieldValue(final String field, final int index) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				return element.getFieldValue(field, index);
			}
		}
		return 0;
	}

	@Override
	public void setField(final String field, final String value) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				element.setField(field, value);
				return;
			}
		}
		throw new IllegalArgumentException("no field");
	}

	@Override
	public String getField(final String field) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				return element.getField(field);
			}
		}
		return "";
	}

	@Override
	public int getFieldValue(final String field) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				return element.getFieldValue(field);
			}
		}
		return 0;
	}

	/*
	 * (non-Javadoc) I'm not entirely sure this is still safe to use
	 *
	 * @see com.hiveworkshop.wc3.units.GameObject#getFieldAsList(java.lang. String)
	 */
	@Override
	public List<? extends GameObject> getFieldAsList(final String field, final ObjectData objectData) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null) && element.hasField(field)) {
				return element.getFieldAsList(field, objectData);
			}
		}
		return new ArrayList<>();// empty list if not found
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ObjectData getTable() {
		return dataSource;
	}

	public ImageIcon getIcon() {
		String artField = getField("Art");
		if (artField.indexOf(',') != -1) {
			artField = artField.substring(0, artField.indexOf(','));
		}
		return new ImageIcon(BLPHandler.getGameTex(artField));
	}

	BufferedImage storedImage = null;
	String storedImagePath = null;

	@Override
	public BufferedImage getImage() {
		String artField = getField("Art");
		if (artField.indexOf(',') != -1) {
			artField = artField.substring(0, artField.indexOf(','));
		}
		String doodadPlaceHolderPath = "ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp";
		if ((storedImage == null) || (storedImagePath == null) || !storedImagePath.equals(artField)) {
			try {
				storedImage = BLPHandler.getGameTex(artField);
				storedImagePath = artField;
				if (storedImage == null) {
					return IconUtils.scale(BLPHandler.getGameTex(doodadPlaceHolderPath), 64, 64);
				}
				return storedImage;
			} catch (final Exception exc) {
				// artField = "ReplaceableTextures\\CommandButtons\\BTNTemp.blp";
				storedImage = BLPHandler.getGameTex(artField);
				storedImagePath = artField;
				if (storedImage == null) {
					return IconUtils.scale(BLPHandler.getGameTex(doodadPlaceHolderPath), 64, 64);
				}
				return storedImage;
				// return BLPHandler.getGameTex("ReplaceableTextures\\CommandButtons\\BTNAcolyte.blp");
			}
		} else {
			if (storedImage == null) {
				return IconUtils.scale(BLPHandler.getGameTex(doodadPlaceHolderPath), 64, 64);
			}
			return storedImage;
		}
	}

	@Override
	public ImageIcon getScaledIcon(int size) {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_FAST));
	}

	@Override
	public Set<String> keySet() {
		final Set<String> keySet = new HashSet<>();
		for (final DataTable table : dataSource.tables) {
			final Element element = table.get(id);
			if (element != null) {
				keySet.addAll(element.keySet());
			}
		}
		return keySet;
	}
}
