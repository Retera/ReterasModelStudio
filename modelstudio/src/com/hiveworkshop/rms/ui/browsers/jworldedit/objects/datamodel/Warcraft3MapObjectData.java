//package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;
//
//import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
//import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
//import com.hiveworkshop.rms.parsers.slk.DataTable;
//import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
//import com.hiveworkshop.rms.parsers.slk.WarcraftData;
//import com.hiveworkshop.rms.parsers.w3o.WTSFile;
//import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
//import de.wc3data.stream.BlizzardDataInputStream;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public final class Warcraft3MapObjectData {
//	private final MutableObjectData units;
//	private final MutableObjectData items;
//	private final MutableObjectData destructibles;
//	private final MutableObjectData doodads;
//	private final MutableObjectData abilities;
//	private final MutableObjectData buffs;
//	private final MutableObjectData upgrades;
//	private final List<MutableObjectData> datas;
//	private final transient Map<WorldEditorDataType, MutableObjectData> typeToData = new HashMap<>();
//
//	public Warcraft3MapObjectData(MutableObjectData units, MutableObjectData items,
//								  MutableObjectData destructibles, MutableObjectData doodads, MutableObjectData abilities,
//								  MutableObjectData buffs, MutableObjectData upgrades) {
//		this.units = units;
//		this.items = items;
//		this.destructibles = destructibles;
//		this.doodads = doodads;
//		this.abilities = abilities;
//		this.buffs = buffs;
//		this.upgrades = upgrades;
//		datas = new ArrayList<>();
//		datas.add(units);
//		datas.add(items);
//		datas.add(destructibles);
//		datas.add(doodads);
//		datas.add(abilities);
//		datas.add(buffs);
//		datas.add(upgrades);
//		for(final MutableObjectData data: datas) {
//			typeToData.put(data.getWorldEditorDataType(), data);
//		}
//	}
//
//	public MutableObjectData getDataByType(final WorldEditorDataType type) {
//		return typeToData.get(type);
//	}
//
//	public MutableObjectData getUnits() {
//		return units;
//	}
//
//	public MutableObjectData getItems() {
//		return items;
//	}
//
//	public MutableObjectData getDestructibles() {
//		return destructibles;
//	}
//
//	public MutableObjectData getDoodads() {
//		return doodads;
//	}
//
//	public MutableObjectData getAbilities() {
//		return abilities;
//	}
//
//	public MutableObjectData getBuffs() {
//		return buffs;
//	}
//
//	public MutableObjectData getUpgrades() {
//		return upgrades;
//	}
//
//	public List<MutableObjectData> getDatas() {
//		return datas;
//	}
//
//	public static Warcraft3MapObjectData load(final boolean inlineWTS) throws IOException {
//
//		WarcraftData standardUnits = StandardObjectData.getStandardUnits();
//		WarcraftData standardItems = StandardObjectData.getStandardItems();
//		WarcraftData standardDoodads = StandardObjectData.getStandardDoodads();
//		WarcraftData standardDestructables = StandardObjectData.getStandardDestructables();
//		WarcraftData abilities = StandardObjectData.getStandardAbilities();
//		WarcraftData standardAbilityBuffs = StandardObjectData.getStandardAbilityBuffs();
//		WarcraftData standardUpgrades = StandardObjectData.getStandardUpgrades();
//
//		DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
//		DataTable standardDoodadMeta = StandardObjectData.getStandardDoodadMeta();
//		DataTable standardDestructableMeta = StandardObjectData.getStandardDestructableMeta();
//		DataTable abilityMeta = StandardObjectData.getStandardAbilityMeta();
//		DataTable standardAbilityBuffMeta = StandardObjectData.getStandardAbilityBuffMeta();
//		DataTable standardUpgradeMeta = StandardObjectData.getStandardUpgradeMeta();
//
//		War3ObjectDataChangeset unitChangeset = new War3ObjectDataChangeset('u');
//		War3ObjectDataChangeset itemChangeset = new War3ObjectDataChangeset('t');
//		War3ObjectDataChangeset doodadChangeset = new War3ObjectDataChangeset('d');
//		War3ObjectDataChangeset destructableChangeset = new War3ObjectDataChangeset('b');
//		War3ObjectDataChangeset abilityChangeset = new War3ObjectDataChangeset('a');
//		War3ObjectDataChangeset buffChangeset = new War3ObjectDataChangeset('h');
//		War3ObjectDataChangeset upgradeChangeset = new War3ObjectDataChangeset('q');
//
//		loadResources(inlineWTS, "war3map.w3u", unitChangeset);
//		loadResources(inlineWTS, "war3map.w3t", itemChangeset);
//		loadResources(inlineWTS, "war3map.w3d", doodadChangeset);
//		loadResources(inlineWTS, "war3map.w3b", destructableChangeset);
//		loadResources(inlineWTS, "war3map.w3a", abilityChangeset);
//		loadResources(inlineWTS, "war3map.w3h", buffChangeset);
//		loadResources(inlineWTS, "war3map.w3q", upgradeChangeset);
//
//		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, StandardObjectData.getStandardUnits(), StandardObjectData.getStandardUnitMeta(), unitChangeset);
//		MutableObjectData itemData = new MutableObjectData(WorldEditorDataType.ITEM, StandardObjectData.getStandardItems(), StandardObjectData.getStandardUnitMeta(), itemChangeset);
//		MutableObjectData doodadData = new MutableObjectData(WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(), StandardObjectData.getStandardDoodadMeta(), doodadChangeset);
//		MutableObjectData destructableData = new MutableObjectData(WorldEditorDataType.DESTRUCTIBLES, StandardObjectData.getStandardDestructables(), StandardObjectData.getStandardDestructableMeta(), destructableChangeset);
//		MutableObjectData abilityData = new MutableObjectData(WorldEditorDataType.ABILITIES, StandardObjectData.getStandardAbilities(), StandardObjectData.getStandardAbilityMeta(), abilityChangeset);
//		MutableObjectData buffData = new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS, StandardObjectData.getStandardAbilityBuffs(), StandardObjectData.getStandardAbilityBuffMeta(), buffChangeset);
//		MutableObjectData upgradeData = new MutableObjectData(WorldEditorDataType.UPGRADES, StandardObjectData.getStandardUpgrades(), StandardObjectData.getStandardUpgradeMeta(), upgradeChangeset);
////
////		MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, standardUnits, standardUnitMeta, unitChangeset);
////		MutableObjectData itemData = new MutableObjectData(WorldEditorDataType.ITEM, standardItems, standardUnitMeta, itemChangeset);
////		MutableObjectData doodadData = new MutableObjectData(WorldEditorDataType.DOODADS, standardDoodads, standardDoodadMeta, doodadChangeset);
////		MutableObjectData destructableData = new MutableObjectData(WorldEditorDataType.DESTRUCTIBLES, standardDestructables, standardDestructableMeta, destructableChangeset);
////		MutableObjectData abilityData = new MutableObjectData(WorldEditorDataType.ABILITIES, abilities, abilityMeta, abilityChangeset);
////		MutableObjectData buffData = new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS, standardAbilityBuffs, standardAbilityBuffMeta, buffChangeset);
////		MutableObjectData upgradeData = new MutableObjectData(WorldEditorDataType.UPGRADES, standardUpgrades, standardUpgradeMeta, upgradeChangeset);
//
//		return new Warcraft3MapObjectData(unitData, itemData, destructableData, doodadData, abilityData, buffData,
//				upgradeData);
//	}
//
//	private static void loadResources(boolean inlineWTS, String filePath, War3ObjectDataChangeset changeset) throws IOException {
//		CompoundDataSource dataSource = GameDataFileSystem.getDefault();
//		if (dataSource.has(filePath)) {
//			InputStream resourceAsStream1 = dataSource.getResourceAsStream("war3map.wts");
//			InputStream resourceAsStream = dataSource.getResourceAsStream(filePath);
//			changeset.load(new BlizzardDataInputStream(resourceAsStream), new WTSFile(resourceAsStream1), inlineWTS);
//		}
//	}
//}
