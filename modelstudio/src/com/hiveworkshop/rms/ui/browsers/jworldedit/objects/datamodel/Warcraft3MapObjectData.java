package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData.WarcraftData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import de.wc3data.stream.BlizzardDataInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Warcraft3MapObjectData {
	private final MutableObjectData units;
	private final MutableObjectData items;
	private final MutableObjectData destructibles;
	private final MutableObjectData doodads;
	private final MutableObjectData abilities;
	private final MutableObjectData buffs;
	private final MutableObjectData upgrades;
	private final List<MutableObjectData> datas;
	private final transient Map<MutableObjectData.WorldEditorDataType, MutableObjectData> typeToData = new HashMap<>();

	public Warcraft3MapObjectData(final MutableObjectData units, final MutableObjectData items,
								  final MutableObjectData destructibles, final MutableObjectData doodads, final MutableObjectData abilities,
								  final MutableObjectData buffs, final MutableObjectData upgrades) {
		this.units = units;
		this.items = items;
		this.destructibles = destructibles;
		this.doodads = doodads;
		this.abilities = abilities;
		this.buffs = buffs;
		this.upgrades = upgrades;
		datas = new ArrayList<>();
		datas.add(units);
		datas.add(items);
		datas.add(destructibles);
		datas.add(doodads);
		datas.add(abilities);
		datas.add(buffs);
		datas.add(upgrades);
		for(final MutableObjectData data: datas) {
			typeToData.put(data.getWorldEditorDataType(), data);
		}
	}

	public MutableObjectData getDataByType(final MutableObjectData.WorldEditorDataType type) {
		return typeToData.get(type);
	}

	public MutableObjectData getUnits() {
		return units;
	}

	public MutableObjectData getItems() {
		return items;
	}

	public MutableObjectData getDestructibles() {
		return destructibles;
	}

	public MutableObjectData getDoodads() {
		return doodads;
	}

	public MutableObjectData getAbilities() {
		return abilities;
	}

	public MutableObjectData getBuffs() {
		return buffs;
	}

	public MutableObjectData getUpgrades() {
		return upgrades;
	}

	public List<MutableObjectData> getDatas() {
		return datas;
	}

	public static Warcraft3MapObjectData load(final boolean inlineWTS) throws IOException {

		final WarcraftData standardUnits = StandardObjectData.getStandardUnits();
		final WarcraftData standardItems = StandardObjectData.getStandardItems();
		final WarcraftData standardDoodads = StandardObjectData.getStandardDoodads();
		final WarcraftData standardDestructables = StandardObjectData.getStandardDestructables();
		final WarcraftData abilities = StandardObjectData.getStandardAbilities();
		final WarcraftData standardAbilityBuffs = StandardObjectData.getStandardAbilityBuffs();
		final WarcraftData standardUpgrades = StandardObjectData.getStandardUpgrades();

		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		final DataTable standardDoodadMeta = StandardObjectData.getStandardDoodadMeta();
		final DataTable standardDestructableMeta = StandardObjectData.getStandardDestructableMeta();
		final DataTable abilityMeta = StandardObjectData.getStandardAbilityMeta();
		final DataTable standardAbilityBuffMeta = StandardObjectData.getStandardAbilityBuffMeta();
		final DataTable standardUpgradeMeta = StandardObjectData.getStandardUpgradeMeta();

		final War3ObjectDataChangeset unitChangeset = new War3ObjectDataChangeset('u');
		final War3ObjectDataChangeset itemChangeset = new War3ObjectDataChangeset('t');
		final War3ObjectDataChangeset doodadChangeset = new War3ObjectDataChangeset('d');
		final War3ObjectDataChangeset destructableChangeset = new War3ObjectDataChangeset('b');
		final War3ObjectDataChangeset abilityChangeset = new War3ObjectDataChangeset('a');
		final War3ObjectDataChangeset buffChangeset = new War3ObjectDataChangeset('h');
		final War3ObjectDataChangeset upgradeChangeset = new War3ObjectDataChangeset('q');

		if (GameDataFileSystem.getDefault().has("war3map.w3u")) {
			unitChangeset.load(new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3u")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}
		if (GameDataFileSystem.getDefault().has("war3map.w3t")) {
			itemChangeset.load(new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3t")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}
		if (GameDataFileSystem.getDefault().has("war3map.w3d")) {
			doodadChangeset.load(new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3d")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}
		if (GameDataFileSystem.getDefault().has("war3map.w3b")) {
			destructableChangeset.load(
					new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3b")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}
		if (GameDataFileSystem.getDefault().has("war3map.w3a")) {
			abilityChangeset.load(new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3a")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}
		if (GameDataFileSystem.getDefault().has("war3map.w3h")) {
			buffChangeset.load(new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3h")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}
		if (GameDataFileSystem.getDefault().has("war3map.w3q")) {
			upgradeChangeset.load(new BlizzardDataInputStream(GameDataFileSystem.getDefault().getResourceAsStream("war3map.w3q")),
					new WTSFile(GameDataFileSystem.getDefault().getResourceAsStream("war3map.wts")), inlineWTS);
		}

		final MutableObjectData unitData = new MutableObjectData(MutableObjectData.WorldEditorDataType.UNITS, standardUnits,
				standardUnitMeta, unitChangeset);
		final MutableObjectData itemData = new MutableObjectData(MutableObjectData.WorldEditorDataType.ITEM, standardItems,
				standardUnitMeta, itemChangeset);
		final MutableObjectData doodadData = new MutableObjectData(MutableObjectData.WorldEditorDataType.DOODADS, standardDoodads,
				standardDoodadMeta, doodadChangeset);
		final MutableObjectData destructableData = new MutableObjectData(MutableObjectData.WorldEditorDataType.DESTRUCTIBLES,
				standardDestructables, standardDestructableMeta, destructableChangeset);
		final MutableObjectData abilityData = new MutableObjectData(MutableObjectData.WorldEditorDataType.ABILITIES, abilities,
				abilityMeta, abilityChangeset);
		final MutableObjectData buffData = new MutableObjectData(MutableObjectData.WorldEditorDataType.BUFFS_EFFECTS,
				standardAbilityBuffs, standardAbilityBuffMeta, buffChangeset);
		final MutableObjectData upgradeData = new MutableObjectData(MutableObjectData.WorldEditorDataType.UPGRADES, standardUpgrades,
				standardUpgradeMeta, upgradeChangeset);

		return new Warcraft3MapObjectData(unitData, itemData, destructableData, doodadData, abilityData, buffData,
				upgradeData);
	}
}
