package com.matrixeater.hacks;

import java.io.IOException;
import java.nio.file.Paths;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import mpq.MPQException;

public class GetMeTheHeroSkills {
	private static final War3ID HERO_ABIL_LIST = War3ID.fromString("uhab");
	private static final War3ID UNIT_NAME = War3ID.fromString("unam");
	private static final War3ID ABILITY_NAME = War3ID.fromString("anam");
	private static final War3ID CAMPAIGN_UNIT_SETTING = War3ID.fromString("ucam");
	private static final War3ID UNIT_HERO_PROPER_NAME = War3ID.fromString("upro");

	public static void main(final String[] args) {
		final LoadedMPQ mpq;
		try {
			mpq = MpqCodebase.get()
					.loadMPQ(Paths.get("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/HeroAbilityJassTest.w3x"));
		} catch (final MPQException e1) {
			e1.printStackTrace();
			return;
		} catch (final IOException e1) {
			e1.printStackTrace();
			return;
		}
		Warcraft3MapObjectData objectData;
		try {
			objectData = Warcraft3MapObjectData.load(true);
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("// =========================================================================");
		System.out.println("// This library code was automatically generated and should not be modified:");
		System.out.println("library GetHeroAbilities initializer init");
		System.out.println("\tglobals");
		System.out.println("\t\thashtable HERO_ABILITY_LISTING = InitHashtable()");
		System.out
				.println("\t\t// The game only handles heros with up to 5 abilities, so we use the 6th item to store");
		System.out.println("\t\t// number of abilities");
		System.out.println("\t\tconstant integer HERO_ABILITY_COUNT_KEY = 5");
		System.out.println("\tendglobals");
		System.out.println();
		System.out.println("\tfunction LoadGeneratedHeroDataList takes nothing returns nothing");
		System.out.println("\t\t local integer heroid");
		for (final War3ID unitId : objectData.getUnits().keySet()) {
			final MutableGameObject unitType = objectData.getUnits().get(unitId);
			final String heroAbilityList = unitType.getFieldAsString(HERO_ABIL_LIST, 0);
			if ((heroAbilityList != null) && (heroAbilityList.length() > 0)) {
				String heroUnitName = unitType.getFieldAsString(UNIT_NAME, 0);
				final boolean campaignUnit = unitType.getFieldAsBoolean(CAMPAIGN_UNIT_SETTING, 0);
				if (campaignUnit) {
					heroUnitName = unitType.getFieldAsString(UNIT_HERO_PROPER_NAME, 0).split("\\,")[0];
				}
				System.out.println();
				System.out.println("\t\t// " + heroUnitName + " ('" + unitId + "')");
				System.out.println("\t\tset heroid = '" + unitId + "'");
				int abilityIndex = 0;
				final String[] heroAbilityIdStrings = heroAbilityList.split("\\,");
				for (final String abilityIdString : heroAbilityIdStrings) {
					final War3ID abilityId = War3ID.fromString(abilityIdString);
					final MutableGameObject abilityType = objectData.getAbilities().get(abilityId);
					final String abilityName = abilityType.getFieldAsString(ABILITY_NAME, 0);
					System.out.println("\t\tcall SaveInteger(HERO_ABILITY_LISTING, '" + unitId + "', "
							+ (abilityIndex++) + ", '" + abilityIdString + "') // " + abilityName);
				}
				System.out
						.println("\t\tcall SaveInteger(HERO_ABILITY_LISTING, '" + unitId + "', HERO_ABILITY_COUNT_KEY, "
								+ heroAbilityIdStrings.length + ") // " + heroAbilityIdStrings.length + " abilities");
			}
		}
		System.out.println("\tendfunction");
		System.out.println();
		System.out.println("\tfunction GetHeroAbilityCount takes integer heroId returns integer");
		System.out.println("\t\treturn LoadInteger(HERO_ABILITY_LISTING, heroId, HERO_ABILITY_COUNT_KEY)");
		System.out.println("\tendfunction");
		System.out.println();
		System.out.println("\tfunction GetHeroAbility takes integer heroId, integer abilityIndex returns integer");
		System.out.println("\t\treturn LoadInteger(HERO_ABILITY_LISTING, heroId, abilityIndex)");
		System.out.println("\tendfunction");
		System.out.println();
		System.out.println("\tprivate function init takes nothing returns nothing");
		System.out.println("\t\t// Use separate 'thread' for startup, so we cannot hit op limit");
		System.out.println("\t\tcall ExecuteFunc(\"LoadGeneratedHeroDataList\")");
		System.out.println("\tendfunction");
		System.out.println("endlibrary");

		mpq.unload();
	}

}
