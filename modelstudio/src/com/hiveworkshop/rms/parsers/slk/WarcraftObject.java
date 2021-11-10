package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
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

	// @Override
	// public String getName() {
	// return dataSource.profile.get(id).getName();
	// }
	@Override
	public String getName() {
		StringBuilder name = new StringBuilder(getField("Name"));
		boolean nameKnown = name.length() >= 1;
		if (!nameKnown && !getField("code").equals(id) && (getField("code").length() >= 4)) {
			final Element other = (Element) dataSource.get(getField("code").substring(0, 4));
			if (other != null) {
				name = new StringBuilder(other.getName());
				nameKnown = true;
			}
		}
		if (!nameKnown && (getField("EditorName").length() > 1)) {
			name = new StringBuilder(getField("EditorName"));
			nameKnown = true;
		}
		if (!nameKnown && (getField("Editorname").length() > 1)) {
			name = new StringBuilder(getField("Editorname"));
			nameKnown = true;
		}
		if (!nameKnown && (getField("BuffTip").length() > 1)) {
			name = new StringBuilder(getField("BuffTip"));
			nameKnown = true;
		}
		if (!nameKnown && (getField("Bufftip").length() > 1)) {
			name = new StringBuilder(getField("Bufftip"));
			nameKnown = true;
		}
		if (nameKnown && name.toString().startsWith("WESTRING")) {
			if (!name.toString().contains(" ")) {
				name = new StringBuilder(WEString.getString(name.toString()));
			} else {
				final String[] names = name.toString().split(" ");
				name = new StringBuilder();
				for (final String subName : names) {
					if (name.length() > 0) {
						name.append(" ");
					}
					if (subName.startsWith("WESTRING")) {
						name.append(WEString.getString(subName));
					} else {
						name.append(subName);
					}
				}
			}
			if (name.toString().startsWith("\"") && name.toString().endsWith("\"")) {
				name = new StringBuilder(name.substring(1, name.length() - 1));
			}
			setField("Name", name.toString());
		}
		if (!nameKnown) {
			name = new StringBuilder(WEString.getString("WESTRING_UNKNOWN") + " '" + getId() + "'");
		}
		if (getField("campaign").startsWith("1") && Character.isUpperCase(getId().charAt(0))) {
			name = new StringBuilder(getField("Propernames"));
			if (name.toString().contains(",")) {
				name = new StringBuilder(name.toString().split(",")[0]);
			}
		}
		String suf = getField("EditorSuffix");
		if ((suf.length() > 0) && !suf.equals("_")) {
			if (suf.startsWith("WESTRING")) {
				suf = WEString.getString(suf);
			}
			if (!suf.startsWith(" ")) {
				name.append(" ");
			}
			name.append(suf);
		}
		return name.toString();
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

	public ImageIcon getBigIcon() {
		final Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 1.25),
				(int) (img.getHeight(null) * 1.25), Image.SCALE_SMOOTH));
	}

	@Override
	public ImageIcon getScaledIcon(int size) {
		final Image img = getImage();
		return new ImageIcon(img.getScaledInstance(size,
				size, Image.SCALE_FAST));
	}

	@Override
	public ImageIcon getScaledTintedIcon(final Color tint, int amt) {
		final Image img = getTintedImage(tint);
		return new ImageIcon(img.getScaledInstance(amt,
				amt, Image.SCALE_SMOOTH));
	}

	public Image getTintedImage(final Color tint) {
		final Image img = getImage();
		final BufferedImage out = new BufferedImage(img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g2 = (Graphics2D) out.getGraphics();
		g2.drawImage(img, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
		g2.setColor(tint);
		g2.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
		return out;
	}

	public ImageIcon getSmallIcon() {
		final Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 0.25),
				(int) (img.getHeight(null) * 0.25), Image.SCALE_SMOOTH));
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
