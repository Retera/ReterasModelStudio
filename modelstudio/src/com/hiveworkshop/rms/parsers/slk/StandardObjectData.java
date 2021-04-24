package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public class StandardObjectData {
	public static WarcraftData getStandardUnits() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable profile = new DataTable();
		final DataTable unitAbilities = new DataTable();
		final DataTable unitBalance = new DataTable();
		final DataTable unitData = new DataTable();
		final DataTable unitUI = new DataTable();
		final DataTable unitWeapons = new DataTable();
		final DataTable skin = new DataTable();

		try {
			profile.readTXT(source.getResourceAsStream("Units\\CampaignUnitFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CampaignUnitStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanUnitFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanUnitStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralUnitFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralUnitStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfUnitFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfUnitStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcUnitFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcUnitStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadUnitFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadUnitStrings.txt"), true);

			unitAbilities.readSLK(source.getResourceAsStream("Units\\UnitAbilities.slk"));

			unitBalance.readSLK(source.getResourceAsStream("Units\\UnitBalance.slk"));

			unitData.readSLK(source.getResourceAsStream("Units\\UnitData.slk"));

			unitUI.readSLK(source.getResourceAsStream("Units\\UnitUI.slk"));

			unitWeapons.readSLK(source.getResourceAsStream("Units\\UnitWeapons.slk"));
			final InputStream unitSkin = source.getResourceAsStream("Units\\UnitSkin.txt");
			if (unitSkin != null) {
				skin.readTXT(unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData units = new WarcraftData();

		units.add(profile, "Profile", false);
		units.add(unitAbilities, "UnitAbilities", true);
		units.add(unitBalance, "UnitBalance", true);
		units.add(unitData, "UnitData", true);
		units.add(unitUI, "UnitUI", true);
		units.add(unitWeapons, "UnitWeapons", true);
		// TODO: The actual War3 game engine does not use this string, "ProfileSkin",
		// it appears that their architecture for handling this data is quite different.
		// They give the skin data a lower load priority than UnitUI, which has a lower
		// load priority than old profile data. However, they still use the
		// string "Profile" for the skin data. By putting the invented string
		// "ProfileSkin" here, my custom object editor will be unable to modify skin
		// data until further notice. But the model studio will work nicely with the
		// data being formatted visually the same as the game.
		units.add(skin, "ProfileSkin", false);

		return units;
	}

	public static WarcraftData getStandardItems() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable profile = new DataTable();
		final DataTable itemData = new DataTable();

		try {
			profile.readTXT(source.getResourceAsStream("Units\\ItemFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\ItemStrings.txt"), true);
			itemData.readSLK(source.getResourceAsStream("Units\\ItemData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData units = new WarcraftData();

		units.add(profile, "Profile", false);
		units.add(itemData, "ItemData", true);

		return units;
	}

	public static WarcraftData getStandardDestructables() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable destructableData = new DataTable();

		try {
			destructableData.readSLK(source.getResourceAsStream("Units\\DestructableData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData units = new WarcraftData();

		units.add(destructableData, "DestructableData", true);

		return units;
	}

	public static WarcraftData getStandardDoodads() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable destructableData = new DataTable();

		try {
			destructableData.readSLK(source.getResourceAsStream("Doodads\\Doodads.slk"));
			final InputStream unitSkin = source.getResourceAsStream("Doodads\\DoodadSkins.txt");
			if (unitSkin != null) {
				destructableData.readTXT(unitSkin, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData units = new WarcraftData();

		units.add(destructableData, "DoodadData", true);

		return units;
	}

	public static DataTable getStandardUnitMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Units\\UnitMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardDestructableMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Units\\DestructableMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardDoodadMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Doodads\\DoodadMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static WarcraftData getStandardAbilities() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable profile = new DataTable();
		final DataTable abilityData = new DataTable();

		try {
			profile.readTXT(source.getResourceAsStream("Units\\CampaignAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CampaignAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CommonAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CommonAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\ItemAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\ItemAbilityStrings.txt"), true);

			final InputStream unitSkin = source.getResourceAsStream("Units\\AbilitySkin.txt");
			if (unitSkin != null) {
				profile.readTXT(unitSkin, true);
			}

			abilityData.readSLK(source.getResourceAsStream("Units\\AbilityData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData abilities = new WarcraftData();

		abilities.add(profile, "Profile", false);
		abilities.add(abilityData, "AbilityData", true);

		return abilities;
	}

	public static WarcraftData getStandardAbilityBuffs() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable profile = new DataTable();
		final DataTable abilityData = new DataTable();

		try {
			profile.readTXT(source.getResourceAsStream("Units\\CampaignAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CampaignAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CommonAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CommonAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadAbilityStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\ItemAbilityFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\ItemAbilityStrings.txt"), true);

			abilityData.readSLK(source.getResourceAsStream("Units\\AbilityBuffData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData abilities = new WarcraftData();

		abilities.add(profile, "Profile", false);
		abilities.add(abilityData, "AbilityData", true);

		return abilities;
	}

	public static WarcraftData getStandardUpgrades() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();

		final DataTable profile = new DataTable();
		final DataTable upgradeData = new DataTable();

		try {
			profile.readTXT(source.getResourceAsStream("Units\\CampaignUpgradeFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\CampaignUpgradeStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanUpgradeFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\HumanUpgradeStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralUpgradeFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NeutralUpgradeStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfUpgradeFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\NightElfUpgradeStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcUpgradeFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\OrcUpgradeStrings.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadUpgradeFunc.txt"), true);
			profile.readTXT(source.getResourceAsStream("Units\\UndeadUpgradeStrings.txt"), true);

			upgradeData.readSLK(source.getResourceAsStream("Units\\UpgradeData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final WarcraftData units = new WarcraftData();

		units.add(profile, "Profile", false);
		units.add(upgradeData, "UpgradeData", true);

		return units;
	}

	public static DataTable getStandardUpgradeMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Units\\UpgradeMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardUpgradeEffectMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Units\\UpgradeEffectMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardAbilityMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Units\\AbilityMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardAbilityBuffMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readSLK(source.getResourceAsStream("Units\\AbilityBuffMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getUnitEditorData() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readTXT(source.getResourceAsStream("UI\\UnitEditorData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getWorldEditData() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			unitMetaData.readTXT(source.getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static class WarcraftData implements ObjectData {
		List<DataTable> tables = new ArrayList<>();
		Map<StringKey, DataTable> tableMap = new HashMap<>();
		Map<StringKey, WarcraftObject> units = new HashMap<>();

		public void add(final DataTable data, final String name, final boolean canMake) {
			tableMap.put(new StringKey(name), data);
			tables.add(data);
			if (canMake) {
				for (final String id : data.keySet()) {
					if (!units.containsKey(new StringKey(id))) {
						units.put(new StringKey(id), new WarcraftObject(data.get(id).getId(), this));
					}
				}
			}
		}

		public WarcraftData() {
		}

		public List<DataTable> getTables() {
			return tables;
		}

		public void setTables(final List<DataTable> tables) {
			this.tables = tables;
		}

		public DataTable getTable(final String tableName) {
			return tableMap.get(new StringKey(tableName));
		}

		@Override
		public GameObject get(final String id) {
			return units.get(new StringKey(id));
		}

		@Override
		public void setValue(final String id, final String field, final String value) {
			get(id).setField(field, value);
		}

		@Override
		public Set<String> keySet() {
			final Set<String> keySet = new HashSet<>();
			for (final StringKey key : units.keySet()) {
				keySet.add(key.getString());
			}
			return keySet;
		}

		public void cloneUnit(final String parentId, final String cloneId) {
			for (final DataTable table : tables) {
				final Element parentEntry = table.get(parentId);
				final LMUnit cloneUnit = new LMUnit(cloneId, table);
				for (final String key : parentEntry.keySet()) {
					cloneUnit.setField(key, parentEntry.getField(key));
				}
				table.put(cloneId, cloneUnit);
			}
			units.put(new StringKey(cloneId), new WarcraftObject(cloneId, this));
		}
	}

	public static class WarcraftObject implements GameObject {
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
			return new ImageIcon(BLPHandler.get().getGameTex(artField));
		}

		BufferedImage storedImage = null;
		String storedImagePath = null;

		@Override
		public BufferedImage getImage() {
			String artField = getField("Art");
			if (artField.indexOf(',') != -1) {
				artField = artField.substring(0, artField.indexOf(','));
			}
			if ((storedImage == null) || (storedImagePath == null) || !storedImagePath.equals(artField)) {
				try {
					storedImage = BLPHandler.get().getGameTex(artField);
					storedImagePath = artField;
					if (storedImage == null) {
						return IconUtils.scale(
								BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp"),
								64, 64);
					}
					return storedImage;
				} catch (final Exception exc) {
					// artField =
					// "ReplaceableTextures\\CommandButtons\\BTNTemp.blp";
					storedImage = BLPHandler.get().getGameTex(artField);
					storedImagePath = artField;
					if (storedImage == null) {
						return IconUtils.scale(
								BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp"),
								64, 64);
					}
					return storedImage;
					// return
					// BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNAcolyte.blp");
				}
			} else {
				if (storedImage == null) {
					return IconUtils.scale(
							BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp"), 64,
							64);
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

	private StandardObjectData() {
	}
}
