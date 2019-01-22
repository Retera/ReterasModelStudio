package com.hiveworkshop.wc3.units;

import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;

public final class Warcraft3MapObjectData {
	private final MutableObjectData units;
	private final MutableObjectData items;
	private final MutableObjectData destructibles;
	private final MutableObjectData doodads;
	private final MutableObjectData abilities;
	private final MutableObjectData buffs;
	private final MutableObjectData upgrades;

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
}
