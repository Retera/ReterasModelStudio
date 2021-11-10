package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

public abstract class GameObject implements Comparable<GameObject> {

	public abstract void setField(String field, String value);

	public abstract void setField(String field, String value, int index);

	public abstract String getField(String field);

	public abstract String getField(String field, int index);

	public abstract int getFieldValue(String field);

	public abstract int getFieldValue(String field, int index);

	public abstract List<? extends GameObject> getFieldAsList(String field, ObjectData objectData);

	public abstract String getId();

	public abstract ObjectData getTable();

	public abstract Set<String> keySet();

	public abstract ImageIcon getScaledIcon(int size);

	public abstract Image getImage();

	public ImageIcon getScaledTintedIcon(Color tint, int amt) {
		Image img = getTintedImage(tint);
		return new ImageIcon(img.getScaledInstance(amt, amt, Image.SCALE_SMOOTH));
	}

	public Image getTintedImage(Color tint) {
		Image img = getImage();
		if (img == null) {
			return BLPHandler.getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
		}
		BufferedImage out = new BufferedImage(img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_4BYTE_ABGR);
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

	public String getName() {
		String nameField = getField("Name");
		String name = null;
		if (1 <= nameField.length()) {
			name = nameField;
		}

		if (name == null) {
			String codeField = getField("code");
			if (!codeField.equals(getId())
					&& codeField.length() >= 4
					&& getTable().get(codeField.substring(0, 4)) != null) {
				name = getTable().get(codeField.substring(0, 4)).getName();
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
			name = WEString.getString("WESTRING_UNKNOWN") + " '" + getId() + "'";
		}

		if (getField("campaign").startsWith("1")
				&& Character.isUpperCase(getId().charAt(0))) {
			String propernames = getField("Propernames");
			name = propernames.split(",")[0];
		}

		return name + getSuffix();
	}

	private String getSuffix() {
		String suf = getField("EditorSuffix");
		if (suf.length() > 0 && !suf.equals("_")) {
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
			if (nameBuilder.length() > 0) {
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
}
