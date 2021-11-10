package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;
import java.io.InputStream;

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
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignUnitFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignUnitStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanUnitFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanUnitStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralUnitFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralUnitStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfUnitFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfUnitStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcUnitFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcUnitStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadUnitFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadUnitStrings.txt"), true);

			DataTableUtils.readSLK(unitAbilities, source.getResourceAsStream("Units\\UnitAbilities.slk"));

			DataTableUtils.readSLK(unitBalance, source.getResourceAsStream("Units\\UnitBalance.slk"));

			DataTableUtils.readSLK(unitData, source.getResourceAsStream("Units\\UnitData.slk"));

			DataTableUtils.readSLK(unitUI, source.getResourceAsStream("Units\\UnitUI.slk"));

			DataTableUtils.readSLK(unitWeapons, source.getResourceAsStream("Units\\UnitWeapons.slk"));
			final InputStream unitSkin = source.getResourceAsStream("Units\\UnitSkin.txt");
			if (unitSkin != null) {
				DataTableUtils.readTXT(skin, unitSkin, true);
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
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\ItemFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\ItemStrings.txt"), true);
			DataTableUtils.readSLK(itemData, source.getResourceAsStream("Units\\ItemData.slk"));
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
			DataTableUtils.readSLK(destructableData, source.getResourceAsStream("Units\\DestructableData.slk"));
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
			DataTableUtils.readSLK(destructableData, source.getResourceAsStream("Doodads\\Doodads.slk"));
			final InputStream unitSkin = source.getResourceAsStream("Doodads\\DoodadSkins.txt");
			if (unitSkin != null) {
				DataTableUtils.readTXT(destructableData, unitSkin, true);
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
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\UnitMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardDestructableMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\DestructableMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardDoodadMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Doodads\\DoodadMetaData.slk"));
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
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CommonAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CommonAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\ItemAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\ItemAbilityStrings.txt"), true);

			final InputStream unitSkin = source.getResourceAsStream("Units\\AbilitySkin.txt");
			if (unitSkin != null) {
				DataTableUtils.readTXT(profile, unitSkin, true);
			}

			DataTableUtils.readSLK(abilityData, source.getResourceAsStream("Units\\AbilityData.slk"));
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
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CommonAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CommonAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadAbilityStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\ItemAbilityFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\ItemAbilityStrings.txt"), true);

			DataTableUtils.readSLK(abilityData, source.getResourceAsStream("Units\\AbilityBuffData.slk"));
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
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignUpgradeFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\CampaignUpgradeStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanUpgradeFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\HumanUpgradeStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralUpgradeFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NeutralUpgradeStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfUpgradeFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\NightElfUpgradeStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcUpgradeFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\OrcUpgradeStrings.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadUpgradeFunc.txt"), true);
			DataTableUtils.readTXT(profile, source.getResourceAsStream("Units\\UndeadUpgradeStrings.txt"), true);

			DataTableUtils.readSLK(upgradeData, source.getResourceAsStream("Units\\UpgradeData.slk"));
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
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\UpgradeMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardUpgradeEffectMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\UpgradeEffectMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardAbilityMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\AbilityMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getStandardAbilityBuffMeta() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readSLK(unitMetaData, source.getResourceAsStream("Units\\AbilityBuffMetaData.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getUnitEditorData() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readTXT(unitMetaData, source.getResourceAsStream("UI\\UnitEditorData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	public static DataTable getWorldEditData() {
		final CompoundDataSource source = GameDataFileSystem.getDefault();
		final DataTable unitMetaData = new DataTable();
		try {
			DataTableUtils.readTXT(unitMetaData, source.getResourceAsStream("UI\\WorldEditData.txt"), true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
		return unitMetaData;
	}

	private StandardObjectData() {
	}
}
