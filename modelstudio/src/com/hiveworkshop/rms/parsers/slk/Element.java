package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;

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
		if (listAsArray.length > 0 && !listAsArray[0].equals("")) {
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
		return new ImageIcon(BLPHandler.getGameTex(artField));
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
			BufferedImage gameTex = BLPHandler.getGameTex(artField);
			if (gameTex != null) {
				return gameTex;
			}
		} catch (final NullPointerException ignored) {
		}
		return BLPHandler.getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
	}

	@Override
	public ImageIcon getScaledIcon(int size) {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	public String getUnitId() {
		return id;
	}

	public void addParent(String parentId) {
		addField(parentId, "Parents");
	}

	public void addChild(String parentId) {
		addField(parentId, "Children");
	}

	public void addRequiredBy(String parentId) {
		addField(parentId, "RequiredBy");
	}

	public void addResearches(String parentId) {
		addField(parentId, "Researches");
	}

	private void addField(String parentId, String fieldName) {
		String parentField = getField(fieldName);
		if (!parentField.contains(parentId)) {
			parentField = parentField + "," + parentId;
			setField(fieldName, parentField);
		}
	}

//	public void addToList(String parentId, String list) {
//		String parentField = getField(list);
//		if(!parentField.contains(parentId) ) {
//			parentField = parentField + "," + parentId;
//			setField(list, parentField);
//		}
//	}
//
//	public UnitDataTable getTable() {
//		return parentTable;
//	}
}
