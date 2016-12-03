package com.requestin8r.src.units;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import com.matrixeater.src.BLPHandler;
import com.requestin8r.src.WEString;

public class Unit {
	HashMap<String,String> fields = new HashMap<String,String>();
	String id;
	UnitDataTable parentTable;
	
	public Unit(String id, UnitDataTable table) {
		this.id = id;
		parentTable = table;
	}
	
	public void setField(String field, String value) {
		fields.put(field, value);
	}
	
	public String getField(String field) {
		String value = "";
		if( fields.get(field) != null ) {
			value = fields.get(field);
		}
		return value;
	}
	
	public int getFieldValue(String field) {
		int i = 0;
		try {
			i = Integer.parseInt(getField(field));
		}
		catch (NumberFormatException e) {
			
		}
		return i;
	}
	
	public List<Unit> builds() {
		return getFieldAsList("Builds");
	}
	
	public List<Unit> requires() {
		List<Unit> requirements = getFieldAsList("Requires");
		List<Integer> reqLvls = requiresLevels();
//		parentTable.updateListWithLevels(requirements, reqLvls);
		return requirements;
	}
	
	public List<Integer> requiresLevels() {
		String stringList = getField("Requiresamount");
		String [] listAsArray = stringList.split(",");
		LinkedList<Integer> output = new LinkedList<Integer>();
		if( listAsArray != null && listAsArray.length > 0 && !listAsArray[0].equals("") ) {
			for( String levelString: listAsArray ) {
				Integer level = Integer.parseInt(levelString);
				if( level != null)
					output.add(level);
			}
		}
		return output;
	}
	
	public List<Unit> parents() {
		return getFieldAsList("Parents");
	}
	
	public List<Unit> children() {
		return getFieldAsList("Children");
	}
	
	public List<Unit> requiredBy() {
		return getFieldAsList("RequiredBy");
	}
	
	public List<Unit> trains() {
		return getFieldAsList("Trains");
	}
	
	public List<Unit> upgrades() {
		return getFieldAsList("Upgrade");
	}
	
	public List<Unit> researches() {
		return getFieldAsList("Researches");
	}
	
	public List<Unit> dependencyOr() {
		return getFieldAsList("DependencyOr");
	}
	
	public List<Unit> abilities() {
		return getFieldAsList("abilList");
	}
	
	HashMap<String,List<Unit>> hashedLists = new HashMap<String,List<Unit>>();
	public List<Unit> getFieldAsList(String field) {
		List<Unit> fieldAsList = hashedLists.get(field);
		if( fieldAsList == null ) {
			fieldAsList = new ArrayList<Unit>();
			String stringList = getField(field);
			String [] listAsArray = stringList.split(",");
			if( listAsArray != null && listAsArray.length > 0 ) {
				for( String buildingId: listAsArray ) {
					Unit referencedUnit = parentTable.get(buildingId);
					if( referencedUnit != null)
						fieldAsList.add(referencedUnit);
				}
			}
			hashedLists.put(field, fieldAsList);
		}
		return fieldAsList;
	}
	
	@Override
	public String toString() {
		return getField("Name");
	}
	
	public int getTechTier() {
		String tier = fields.get("Custom Field: TechTier");
		if( tier == null ) {
			return -1;
		}
		return Integer.parseInt(tier);
	}
	
	public void setTechTier(int i) {
		setField("Custom Field: TechTier", i+ "");
	}
	
	public int getTechDepth() {
		String tier = fields.get("Custom Field: TechDepth");
		if( tier == null ) {
			return -1;
		}
		return Integer.parseInt(tier);
	}
	
	public void setTechDepth(int i) {
		setField("Custom Field: TechDepth", i+ "");
	}
	
	public ImageIcon getIcon() {
		String artField = getField("Art");
		if( artField.indexOf(',') != -1) 
			artField = artField.substring(0, artField.indexOf(','));
		return new ImageIcon(BLPHandler.get().getGameTex(artField));
	}
	
	public Image getImage() {
		String artField = getField("Art");
		if( artField.indexOf(',') != -1) 
			artField = artField.substring(0, artField.indexOf(','));
		try {
			return BLPHandler.get().getGameTex(artField);
		}
		catch (NullPointerException exc) {
			return BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNTemp.blp");
		}
	}
	
	public ImageIcon getBigIcon() {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int)(img.getWidth(null)*1.25), (int)(img.getHeight(null)*1.25), Image.SCALE_SMOOTH));
	}
	
	public ImageIcon getScaledIcon(double amt) {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int)(img.getWidth(null)*amt), (int)(img.getHeight(null)*amt), Image.SCALE_SMOOTH));
	}
	
	public ImageIcon getScaledTintedIcon(Color tint, double amt) {
		Image img = getTintedImage(tint);
		return new ImageIcon(img.getScaledInstance((int)(img.getWidth(null)*amt), (int)(img.getHeight(null)*amt), Image.SCALE_SMOOTH));
	}
	
	public Image getTintedImage(Color tint) {
		Image img = getImage();
		BufferedImage out = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = (Graphics2D)out.getGraphics();
		g2.drawImage(img, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
		g2.setColor(tint);
		g2.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
		return out;
	}
	
	public ImageIcon getSmallIcon() {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance((int)(img.getWidth(null)*0.25), (int)(img.getHeight(null)*0.25), Image.SCALE_SMOOTH));
	}
	
	public String getUnitId() {
		return id;
	}
	
	public String getName() {
		String name = getField("Name");
		boolean nameKnown = name.length() >= 1;
		if( !nameKnown && !getField("code").equals(id) && getField("code").length() >= 4 ) {
			Unit other = parentTable.get(getField("code").substring(0,4));
			if( other != null ) {
				name = other.getName();
				nameKnown = true;
			}
		}
		if( !nameKnown && getField("EditorName").length() > 1 ) {
			name = getField("EditorName");
			nameKnown = true;
		}
		if( !nameKnown && getField("Editorname").length() > 1 ) {
			name = getField("Editorname");
			nameKnown = true;
		}
		if( !nameKnown && getField("BuffTip").length() > 1 ) {
			name = getField("BuffTip");
			nameKnown = true;
		}
		if( !nameKnown && getField("Bufftip").length() > 1 ) {
			name = getField("Bufftip");
			nameKnown = true;
		}
		if( nameKnown && name.startsWith("WESTRING") ) {
			if( !name.contains(" ") ) {
				name = WEString.getString(name);
			}
			else {
				String[] names = name.split(" ");
				name = "";
				for( String subName: names ) {
					if( name.length() > 0 ) {
						name += " ";
					}
					if( subName.startsWith("WESTRING") )
						name += WEString.getString(subName);
					else
						name += subName;
				}
			}
			if( name.startsWith("\"") && name.endsWith("\"") ) {
				name = name.substring(1, name.length()-1);
			}
			setField("Name", name);
		}
		if( !nameKnown ) {
			name = WEString.getString("WESTRING_UNKNOWN") + " '"+getUnitId()+"'";
		}
		if( getField("campaign").startsWith("1") && Character.isUpperCase(getUnitId().charAt(0)) ) {
			name = getField("Propernames");
			if( name.contains(",") ) {
				name = name.split(",")[0];
			}
		}
		String suf = getField("EditorSuffix");
		if( suf.length() > 0 && !suf.equals("_") ) {
			if( suf.startsWith("WESTRING") ) {
				suf = WEString.getString(suf);
			}
			if( !suf.startsWith(" ")) {
				name += " ";
			}
			name += suf;
		}
		return name;
	}
	
	public void addParent(String parentId) {
		String parentField = getField("Parents");
		if( !parentField.contains(parentId) ) {
			parentField = parentField + "," + parentId;
			setField("Parents",parentField);
		}
	}
	
	public void addChild(String parentId) {
		String parentField = getField("Children");
		if( !parentField.contains(parentId) ) {
			parentField = parentField + "," + parentId;
			setField("Children",parentField);
		}
	}
	
	public void addRequiredBy(String parentId) {
		String parentField = getField("RequiredBy");
		if( !parentField.contains(parentId) ) {
			parentField = parentField + "," + parentId;
			setField("RequiredBy",parentField);
		}
	}
	
	public void addResearches(String parentId) {
		String parentField = getField("Researches");
		if( !parentField.contains(parentId) ) {
			parentField = parentField + "," + parentId;
			setField("Researches",parentField);
		}
	}
	
	public void addToList(String parentId, String list) {
		String parentField = getField(list);
		if( !parentField.contains(parentId) ) {
			parentField = parentField + "," + parentId;
			setField(list,parentField);
		}
	}
	
	public UnitDataTable getTable() {
		return parentTable;
	}
}
