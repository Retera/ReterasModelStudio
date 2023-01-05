package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

public abstract class GameObject implements Comparable<GameObject> {
	String id;
	BufferedImage cachedImage = null;
	String cachedImagePath = null;
	String placeholderTexPath;

	public abstract void setField(String field, String value);

	public abstract void setField(String field, String value, int index);

	public abstract String getField(String field);

	public abstract String getField(String field, int index);

	public abstract int getFieldValue(String field);

	public abstract int getFieldValue(String field, int index);

	public abstract List<? extends GameObject> getFieldAsList(String field, ObjectData objectData);

	public String getId() {
		return id;
	}

	public abstract GameObject getSiblingObjectFromCode(String fieldCode);

	public abstract Set<String> keySet();

	public String getName() {
		String nameField = getField("Name");
		String name = null;
		if (1 <= nameField.length()) {
			name = nameField;
		}

		if (name == null) {
			GameObject codeObject = getSiblingObjectFromCode(getField("code"));
			if (codeObject != null) {
				name = codeObject.getName();
			} else if (getField("EditorName").length() > 1) {
				name = getField("EditorName");
			} else if (getField("Editorname").length() > 1) {
				name = getField("Editorname");
			} else if (getField("BuffTip").length() > 1) {
				name = getField("BuffTip");
			} else if (getField("Bufftip").length() > 1) {
				name = getField("Bufftip");
			}
		}

		if (name != null && name.startsWith("WESTRING")) {
			if (!name.contains(" ")) {
				name = WEString.getString(name);
			} else {
				name = getSanitizedName(name);
			}

			if (name.startsWith("\"") && name.endsWith("\"")) {
				name = name.substring(1, name.length() - 1);
			}

			setField("Name", name);
		} else if (name == null) {
			name = WEString.getString("WESTRING_UNKNOWN") + " '" + id + "'";
		}

		if (getField("campaign").startsWith("1")
				&& Character.isUpperCase(id.charAt(0))) {
			String propernames = getField("Propernames");
			name = propernames.split(",")[0];
		}

		return name + getSuffix();
	}

	private String getSuffix() {
		String suf = getField("EditorSuffix");
		if (0 < suf.length() && !suf.equals("_")) {
			if (suf.startsWith("WESTRING")) {
				suf = WEString.getString(suf);
			}
			if (!suf.startsWith(" ")) {
				suf = " " + suf;
			}
			return suf;
		}
		return "";
	}

	private String getSanitizedName(String nameString) {
		String[] names = nameString.split(" ");
		StringBuilder nameBuilder = new StringBuilder();
		for (String subName : names) {
			if (0 < nameBuilder.length()) {
				nameBuilder.append(" ");
			}
			if (subName.startsWith("WESTRING")) {
				nameBuilder.append(WEString.getString(subName));
			} else {
				nameBuilder.append(subName);
			}
		}
		return nameBuilder.toString();
	}

	public abstract ImageIcon getScaledIcon(int size);

	public BufferedImage getImage() {
		String artField = getIconPath();
		if (!isCached(artField)) {
			try {
				BufferedImage gameTex = BLPHandler.getImage(artField);
				cash(artField, gameTex);
				if (gameTex == null) {
					gameTex = BLPHandler.getImage(placeholderTexPath);
				}
				if (gameTex == null) {
					gameTex = BLPHandler.getBlankImage();
				}
				return gameTex;
			} catch (final Exception ignored) {
			}
		} else {
			return cachedImage;
		}
		return BLPHandler.getBlankImage();
	}

	public ImageIcon getScaledTintedIcon(Color tint, int size) {
		Image img = getTintedImage(tint);
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	public Image getTintedImage(Color tint) {
		Image img = getImage();
		if (img == null) {
			return BLPHandler.getImage("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
		}
		BufferedImage out = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = (Graphics2D) out.getGraphics();
		g2.drawImage(img, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
		g2.setColor(tint);
		g2.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
		return out;
	}

	public ImageIcon getSmallIcon() {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 0.25),
				(int) (img.getHeight(null) * 0.25), Image.SCALE_SMOOTH));
	}

	public ImageIcon getBigIcon() {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 1.25),
				(int) (img.getHeight(null) * 1.25), Image.SCALE_SMOOTH));
	}

	public ImageIcon getIcon() {
		String artField = getIconPath();
		return new ImageIcon(BLPHandler.getImage(artField));
	}

	public String getIconPath() {
		String artField = getField("Art");
		if (artField.indexOf(',') != -1) {
			artField = artField.substring(0, artField.indexOf(','));
		}
		return artField;
	}

	public boolean isCached(String artField) {
		return cachedImage != null && cachedImagePath != null && cachedImagePath.equals(artField);
	}
	public void cash(String artField, BufferedImage gameTex) {
		cachedImage = gameTex;
		cachedImagePath = artField;
	}
	@Override
	public int compareTo(GameObject b) {
		if (getField("unitClass").equals("") && !b.getField("unitClass").equals("")) {
			return 1;
		} else if (b.getField("unitClass").equals("") && !getField("unitClass").equals("")) {
			return -1;
		}
		final int comp1 = getField("unitClass").compareTo(b.getField("unitClass"));
		if (comp1 == 0) {
			final int comp2 = Integer.compare(getFieldValue("level"), b.getFieldValue("level"));
			if (comp2 == 0) {
				return getName().compareTo(b.getName());
			}
			return comp2;
		}
		return comp1;
	}

	public String getEditorMetaDataDisplayKey(int level) {
		int index = getFieldValue("index");
		String metaDataName = getField("field");
		int repeatCount = getFieldValue("repeat");
		String upgradeHack = getField("appendIndex");
		boolean repeats = (repeatCount > 0) && !"0".equals(upgradeHack);
		int data = getFieldValue("data");
		if (0 < data) {
			metaDataName += (char) ('A' + (data - 1));
		}
		if ("1".equals(upgradeHack)) {
			int upgradeExtensionLevel = level - 1;
			if (0 < upgradeExtensionLevel) {
				metaDataName += Integer.toString(upgradeExtensionLevel);
			}
		} else if (repeats && (index == -1)) {
			if (level == 0) {
				level = 1;
			}
			if (10 <= repeatCount) {
				metaDataName += String.format("%2d", level).replace(' ', '0');
			} else {
				metaDataName += Integer.toString(level);
			}
		}
		return metaDataName;
	}
}
