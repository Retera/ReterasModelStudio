package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Element extends HashedGameObject {
	// HashMap<String,String> fields = new HashMap<String,String>();
	// String id;
	// UnitDataTable parentTable;

	public Element(String id, DataTable table) {
		super(id, table);
	}

	public List<GameObject> builds() {
		return getFieldAsList("Builds", parentTable);
	}

	public List<GameObject> requires() {
		List<GameObject> requirements = getFieldAsList("Requires", parentTable);
		List<Integer> reqLvls = requiresLevels();
		// parentTable.updateListWithLevels(requirements, reqLvls);
		return requirements;
	}

	public List<Integer> requiresLevels() {
		String stringList = getField("Requiresamount");
		String[] listAsArray = stringList.split(",");
		LinkedList<Integer> output = new LinkedList<>();
		if (listAsArray != null && listAsArray.length > 0 && !listAsArray[0].equals("")) {
			for (String levelString : listAsArray) {
				Integer level = Integer.parseInt(levelString);
				if (level != null) {
					output.add(level);
				}
			}
		}
		return output;
	}

	public List<GameObject> parents() {
		return getFieldAsList("Parents", parentTable);
	}

	public List<GameObject> children() {
		return getFieldAsList("Children", parentTable);
	}

	public List<GameObject> requiredBy() {
		return getFieldAsList("RequiredBy", parentTable);
	}

	public List<GameObject> trains() {
		return getFieldAsList("Trains", parentTable);
	}

	public List<GameObject> upgrades() {
		return getFieldAsList("Upgrade", parentTable);
	}

	public List<GameObject> researches() {
		return getFieldAsList("Researches", parentTable);
	}

	public List<GameObject> dependencyOr() {
		return getFieldAsList("DependencyOr", parentTable);
	}

	public List<GameObject> abilities() {
		return getFieldAsList("abilList", parentTable);
	}

	HashMap<String, List<Element>> hashedLists = new HashMap<>();

	@Override
	public String toString() {
		return getField("Name");
	}

	public int getTechTier() {
		String tier = getField("Custom Field: TechTier");
		if (tier == null) {
			return -1;
		}
		return Integer.parseInt(tier);
	}

	public void setTechTier(int i) {
		setField("Custom Field: TechTier", i + "");
	}

	public int getTechDepth() {
		String tier = getField("Custom Field: TechDepth");
		if (tier == null) {
			return -1;
		}
		return Integer.parseInt(tier);
	}

	public void setTechDepth(int i) {
		setField("Custom Field: TechDepth", i + "");
	}

	public ImageIcon getIcon() {
		String artField = getIconPath();
		return new ImageIcon(BLPHandler.get().getGameTex(artField));
	}

	public String getIconPath() {
		String artField = getField("Art");
		if (artField.indexOf(',') != -1) {
			artField = artField.substring(0, artField.indexOf(','));
		}
		return artField;
	}

	@Override
	public Image getImage() {
		String artField = getIconPath();
		try {
			return BLPHandler.get().getGameTex(artField);
		} catch (final NullPointerException exc) {
			return BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
		}
	}

	public ImageIcon getBigIcon() {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 1.25),
				(int) (img.getHeight(null) * 1.25), Image.SCALE_SMOOTH));
	}

	@Override
	public ImageIcon getScaledIcon(int size) {
		Image img = getImage();
		if (img == null) {
			img = BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
		}
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	@Override
	public ImageIcon getScaledTintedIcon(Color tint, int size) {
		Image img = getTintedImage(tint);
		if (img == null) {
			img = BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
		}
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	public Image getTintedImage(Color tint) {
		Image img = getImage();
		if (img == null) {
			return BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
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

	public String getUnitId() {
		return id;
	}

	@Override
	public String getName() {
		StringBuilder name = new StringBuilder(getField("Name"));
		boolean nameKnown = name.length() >= 1;
		if (!nameKnown && !getField("code").equals(id) && getField("code").length() >= 4) {
			Element other = (Element) parentTable.get(getField("code").substring(0, 4));
			if (other != null) {
				name = new StringBuilder(other.getName());
				nameKnown = true;
			}
		}
		if (!nameKnown && getField("EditorName").length() > 1) {
			name = new StringBuilder(getField("EditorName"));
			nameKnown = true;
		}
		if (!nameKnown && getField("Editorname").length() > 1) {
			name = new StringBuilder(getField("Editorname"));
			nameKnown = true;
		}
		if (!nameKnown && getField("BuffTip").length() > 1) {
			name = new StringBuilder(getField("BuffTip"));
			nameKnown = true;
		}
		if (!nameKnown && getField("Bufftip").length() > 1) {
			name = new StringBuilder(getField("Bufftip"));
			nameKnown = true;
		}
		if (nameKnown && name.toString().startsWith("WESTRING")) {
			if (!name.toString().contains(" ")) {
				name = new StringBuilder(WEString.getString(name.toString()));
			} else {
				String[] names = name.toString().split(" ");
				name = new StringBuilder();
				for (String subName : names) {
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
			name = new StringBuilder(WEString.getString("WESTRING_UNKNOWN") + " '" + getUnitId() + "'");
		}
		if (getField("campaign").startsWith("1") && Character.isUpperCase(getUnitId().charAt(0))) {
			name = new StringBuilder(getField("Propernames"));
			if (name.toString().contains(",")) {
				name = new StringBuilder(name.toString().split(",")[0]);
			}
		}
		String suf = getField("EditorSuffix");
		if (suf.length() > 0 && !suf.equals("_")) {
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

	public void addParent(String parentId) {
		String parentField = getField("Parents");
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField("Parents", parentField);
		}
	}

	public void addChild(String parentId) {
		String parentField = getField("Children");
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField("Children", parentField);
		}
	}

	public void addRequiredBy(String parentId) {
		String parentField = getField("RequiredBy");
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField("RequiredBy", parentField);
		}
	}

	public void addResearches(String parentId) {
		String parentField = getField("Researches");
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField("Researches", parentField);
		}
	}

	// public void addToList(String parentId, String list) {
	// String parentField = getField(list);
	// if( !parentField.contains(parentId) ) {
	// parentField = parentField + "," + parentId;
	// setField(list,parentField);
	// }
	// }

	// public UnitDataTable getTable() {
	// return parentTable;
	// }
}
