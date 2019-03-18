package com.hiveworkshop.wc3.units;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.WTSFile;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;

public final class Warcraft3MapObjectData {
	private final MutableObjectData units;
	private final MutableObjectData items;
	private final MutableObjectData destructibles;
	private final MutableObjectData doodads;
	private final MutableObjectData abilities;
	private final MutableObjectData buffs;
	private final MutableObjectData upgrades;
	private final List<MutableObjectData> datas;

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
		this.datas = new ArrayList<>();
		datas.add(units);
		datas.add(items);
		datas.add(destructibles);
		datas.add(doodads);
		datas.add(abilities);
		datas.add(buffs);
		datas.add(upgrades);
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

		unitChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3u")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);
		itemChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3t")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);
		doodadChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3d")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);
		destructableChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3b")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);
		abilityChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3a")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);
		buffChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3h")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);
		upgradeChangeset.load(new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream("war3map.w3q")),
				new WTSFile(MpqCodebase.get().getResourceAsStream("war3map.wts")), inlineWTS);

		final MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, standardUnits,
				standardUnitMeta, unitChangeset);
		final MutableObjectData itemData = new MutableObjectData(WorldEditorDataType.ITEM, standardItems,
				standardUnitMeta, itemChangeset);
		final MutableObjectData doodadData = new MutableObjectData(WorldEditorDataType.DOODADS, standardDoodads,
				standardDoodadMeta, doodadChangeset);
		final MutableObjectData destructableData = new MutableObjectData(WorldEditorDataType.DESTRUCTIBLES,
				standardDestructables, standardDestructableMeta, destructableChangeset);
		final MutableObjectData abilityData = new MutableObjectData(WorldEditorDataType.ABILITIES, abilities,
				abilityMeta, abilityChangeset);
		final MutableObjectData buffData = new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS,
				standardAbilityBuffs, standardAbilityBuffMeta, buffChangeset);
		final MutableObjectData upgradeData = new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS, standardUpgrades,
				standardUpgradeMeta, upgradeChangeset);

		return new Warcraft3MapObjectData(unitData, itemData, destructableData, doodadData, abilityData, buffData,
				upgradeData);
	}
}
