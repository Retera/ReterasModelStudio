package com.matrixeater.hacks;

import java.io.IOException;

import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class GetMeTheItemAbilities {
	private static final War3ID ITEM_ABIL_LIST = War3ID.fromString("iabi");
	private static final War3ID ITEM_NAME = War3ID.fromString("unam");
	private static final War3ID ABILITY_NAME = War3ID.fromString("anam");

	public static void main(final String[] args) {
		final LoadedMPQ mpq;
//		try {
//			mpq = MpqCodebase.get().loadMPQ(Paths
//					.get("C:/Users/micro/OneDrive/Documents/Warcraft III Public Test/Maps/HeroAbilityJassTest.w3x"));
//		} catch (final MPQException e1) {
//			e1.printStackTrace();
//			return;
//		} catch (final IOException e1) {
//			e1.printStackTrace();
//			return;
//		}
		Warcraft3MapObjectData objectData;
		try {
			objectData = Warcraft3MapObjectData.load(true);
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("// =========================================================================");
		System.out.println("// This library code was automatically generated and should not be modified:");
		System.out.println("library GetItemAbilities initializer init");
		System.out.println("\tglobals");
		System.out.println("\t\thashtable ITEM_ABILITY_LISTING = InitHashtable()");
		System.out.println("\t\t// The game used to only handle 4 item abilities, for safety lets assume up to 31");
		System.out.println("\t\tconstant integer ITEM_ABILITY_COUNT_KEY = 32");
		System.out.println("\tendglobals");
		System.out.println();
		System.out.println("\tfunction LoadGeneratedItemDataList takes nothing returns nothing");
		System.out.println("\t\t local integer itemid");
		for (final War3ID unitId : objectData.getItems().keySet()) {
			final MutableGameObject unitType = objectData.getItems().get(unitId);
			final String heroAbilityList = unitType.getFieldAsString(ITEM_ABIL_LIST, 0);
			if ((heroAbilityList != null) && (heroAbilityList.length() > 1)) {
				final String heroUnitName = unitType.getFieldAsString(ITEM_NAME, 0);
				System.out.println();
				System.out.println("\t\t// " + heroUnitName + " ('" + unitId + "')");
				System.out.println("\t\tset itemid = '" + unitId + "'");
				int abilityIndex = 0;
				final String[] heroAbilityIdStrings = heroAbilityList.split("\\,");
				for (final String abilityIdString : heroAbilityIdStrings) {
					final War3ID abilityId = War3ID.fromString(abilityIdString);
					final MutableGameObject abilityType = objectData.getAbilities().get(abilityId);
					final String abilityName = abilityType.getFieldAsString(ABILITY_NAME, 0);
					System.out.println("\t\tcall SaveInteger(ITEM_ABILITY_LISTING, '" + unitId + "', "
							+ (abilityIndex++) + ", '" + abilityIdString + "') // " + abilityName);
				}
				System.out
						.println("\t\tcall SaveInteger(ITEM_ABILITY_LISTING, '" + unitId + "', ITEM_ABILITY_COUNT_KEY, "
								+ heroAbilityIdStrings.length + ") // " + heroAbilityIdStrings.length + " abilities");
			}
		}
		System.out.println("\tendfunction");
		System.out.println();
		System.out.println("\tfunction GetItemAbilityCount takes integer itemid returns integer");
		System.out.println("\t\treturn LoadInteger(ITEM_ABILITY_LISTING, itemid, ITEM_ABILITY_COUNT_KEY)");
		System.out.println("\tendfunction");
		System.out.println();
		System.out.println("\tfunction GetItemAbility takes integer itemid, integer abilityIndex returns integer");
		System.out.println("\t\treturn LoadInteger(ITEM_ABILITY_LISTING, itemid, abilityIndex)");
		System.out.println("\tendfunction");
		System.out.println();
		System.out.println("\tprivate function init takes nothing returns nothing");
		System.out.println("\t\t// Use separate 'thread' for startup, so we cannot hit op limit");
		System.out.println("\t\tcall ExecuteFunc(\"LoadGeneratedItemDataList\")");
		System.out.println("\tendfunction");
		System.out.println("endlibrary");

//		mpq.unload();
	}

}
