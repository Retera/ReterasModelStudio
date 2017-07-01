package com.hiveworkshop.assetextractor;

import java.util.Arrays;
import java.util.List;

import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;

import net.wc3c.util.CharInt;

public class TempMain {
	private static final String BASE_UNIT_TYPE = "zshv";
	private static final List<String> ORIGINAL_HEROES = Arrays.asList("Hamg", "Hblm", "Hmkg", "Hpal", "Obla", "Ofar",
			"Oshd", "Otch", "Edem", "Ekee", "Emoo", "Ewar", "Ucrl", "Udea", "Udre", "Ulic", "Npbm", "Nbrn", "Nngs",
			"Nplh", "Nbst", "Nfir", "Nalc", "Ntin");

	public static String convertUnitId(final String unitId) {
		if (unitId.charAt(0) == 'h') {
			return 'M' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'o') {
			return 'C' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'e') {
			return 'F' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'u') {
			return 'K' + unitId.substring(1);
		}
		// if (unitId.charAt(0) == 'H') {
		// return 'J' + unitId.substring(1);
		// }
		// if (unitId.charAt(0) == 'O') {
		// return 'L' + unitId.substring(1);
		// }
		// if (unitId.charAt(0) == 'E') {
		// return 'G' + unitId.substring(1);
		// }
		// if (unitId.charAt(0) == 'U') {
		// return 'P' + unitId.substring(1);
		// }
		// if (unitId.charAt(0) == 'N') {
		// return 'Q' + unitId.substring(1);
		// }
		return unitId;
	}

	public static String convertHeroId(final String unitId) {
		if (unitId.charAt(0) == 'H') {
			return 'J' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'O') {
			return 'L' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'E') {
			return 'G' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'U') {
			return 'P' + unitId.substring(1);
		}
		if (unitId.charAt(0) == 'N') {
			return 'Q' + unitId.substring(1);
		}
		return unitId;
	}

	public static void main(final String[] args) {
		final WarcraftData standardUnits = StandardObjectData.getStandardUnits();
		for (final String structKey : standardUnits.keySet()) {
			final GameObject struct = standardUnits.get(structKey);
			final List<? extends GameObject> trainsList = struct.getFieldAsList("Trains", standardUnits);
			for (final GameObject obj : trainsList) {
				// if (Character.isLowerCase(obj.getId().charAt(0))) {
				System.out.println("\tcall TrainBySelling('" + struct.getId() + "','" + obj.getId() + "',"
						+ obj.getFieldValue("stockMax") + ") // " + struct.getName() + " trains " + obj.getName());
				// }
			}
		}

		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		// now the dark magic
		System.out.println("//! i setobjecttype(\"units\")");
		int abilityIdIndex = 0;
		for (final String structKey : standardUnits.keySet()) {
			final GameObject struct = standardUnits.get(structKey);
			final List<? extends GameObject> trainsList = struct.getFieldAsList("Trains", standardUnits);
			final StringBuilder sellList = new StringBuilder();
			for (final GameObject obj : trainsList) {
				final String convertedId = convertUnitId(obj.getId());
				if (sellList.length() > 0) {
					sellList.append(',');
				}
				sellList.append(convertedId);
				if (Character.isUpperCase(obj.getId().charAt(0))) {
					sellList.append(',');
					sellList.append(convertHeroId(obj.getId()));
				}
				if (Character.isLowerCase(obj.getId().charAt(0))) {
					if (!convertedId.equals(obj.getId())) {
						// final String abilityKey = "Ax" + String.format("%2d",
						// abilityIdIndex).replace(' ', '0');
						abilityIdIndex++;
						produceDummyUnit(standardUnitMeta, obj, obj.getId(), convertedId, true);
					} else {
						// System.out.println("//! i deleteobject(\"" +
						// convertedId + "\")");
						// System.out.println();
					}
				}
			}

			if (trainsList.size() > 0) {

				System.out.println("//! i modifyobject(\"" + structKey + "\")");
				System.out.println("//! i makechange(current, \"utra\", \"\")");
				System.out.println("//! i makechange(current, \"useu\", \"" + sellList.toString() + "\")");
				System.out.println();
			}
		}

		for (final String heroKey : ORIGINAL_HEROES) {
			// System.out.println("\tcall RegisterHero(\'" + heroKey + "\')");
		}
		for (final String heroKey : ORIGINAL_HEROES) {
			// we need a free dummy hero
			final String dummyId = convertHeroId(heroKey);
			// System.out.println("//! i createobject(\"" + heroKey + "\", \"" +
			// dummyId + "\")");
			produceDummyUnit(standardUnitMeta, standardUnits.get(heroKey), heroKey, dummyId, false);
			System.out.println("//! i makechange(current, \"ulum\", 0)");
			System.out.println("//! i makechange(current, \"ugol\", 0)");
			// System.out.println("//! i makechange(current, \"unsf\", \"
			// (free)\")");
		}

		System.out.println(CharInt.toInt("Mrtt"));
		System.out.println(CharInt.toInt("Ctbk"));
	}

	private static void produceDummyUnit(final DataTable standardUnitMeta, final GameObject obj, final String baseId,
			final String convertedId, final boolean hideHeroIcons) {
		System.out.println("//! i createobject(\"" + baseId + "\", \"" + convertedId + "\")");
		// System.out.println("//! i modifyobject(\"" +
		// convertUnitId(obj.getId()) + "\")");
		// for (final String metaKey : standardUnitMeta.keySet()) {
		// final Element metaNode = standardUnitMeta.get(metaKey);
		// if (metaNode.getFieldValue("useUnit") > 0) {
		// final String field = metaNode.getField("field");
		// int index = metaNode.getFieldValue("index");
		// final String fieldType = metaNode.getField("type");
		// String fieldContents = obj.getField(field);
		// if (index != -1 && !fieldType.toLowerCase().equals("string")) {
		// final String[] fieldContentsArray = fieldContents.split(",");
		// if (index >= fieldContentsArray.length) {
		// index = 0;
		// }
		// fieldContents = fieldContentsArray[index];
		// }
		// fieldContents = fieldContents.replace("\\", "\\\\").replace("\"",
		// "\\\"");
		// if (fieldContents.equals("-") || fieldContents.equals(" - ") ||
		// fieldContents.equals("_")) {
		// fieldContents = "";
		// }
		// if (fieldContents.equals("")) {
		// if (fieldType.equals("int") || fieldType.contains("real") ||
		// fieldType.contains("bool")) {
		// fieldContents = "0";
		// } else if (fieldType.equals("string")) {
		// fieldContents = "\"\"";
		// } else {
		// fieldContents = "\"_\"";
		// }
		// } else {
		// try {
		// Float.parseFloat(fieldContents);
		// } catch (final NumberFormatException exc) {
		// fieldContents = "\"" + fieldContents + "\"";
		// }
		// }
		// System.out.println("//! i makechange(current, \"" + metaKey + "\", "
		// + fieldContents + ")");
		// }
		// }
		if (hideHeroIcons) {
			System.out.println("//! i makechange(current, \"uhhd\", 1)");
			System.out.println("//! i makechange(current, \"uhhb\", 1)");
			System.out.println("//! i makechange(current, \"uhhm\", 1)");
		}
		System.out.println();
	}
}
