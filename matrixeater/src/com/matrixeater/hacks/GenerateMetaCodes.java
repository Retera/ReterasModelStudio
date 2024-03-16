package com.matrixeater.hacks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;

public class GenerateMetaCodes {
	private static final class DefinedClass {
		List<String> fields = new ArrayList<>();

	}

	public static void main(String[] args) {
		DataTable standardAbilityMeta = StandardObjectData.getStandardAbilityMeta();
		WarcraftData standardAbilities = StandardObjectData.getStandardAbilities();

		Map<String, DefinedClass> classNameToDefinition = new LinkedHashMap<>();
		for (String key : standardAbilityMeta.keySet()) {
			Element element = standardAbilityMeta.get(key);
			String useSpecific = element.getField("useSpecific");
			if (!useSpecific.isEmpty() && (useSpecific.length() >= 4)) {
				String[] items = useSpecific.split(",");
				GameObject someAbility = null;
				int idx = 0;
				do {
					String someAbilityId = items[idx++];
					someAbility = standardAbilities.get(someAbilityId);
				}
				while ((someAbility == null) && (idx < items.length));
				String abilityName = someAbility == null ? "Unknown" : someAbility.getName();
				String name = toTitle(abilityName);

				DefinedClass definedClass = classNameToDefinition.get(name);
				if (definedClass == null) {
					definedClass = new DefinedClass();
					classNameToDefinition.put(name, definedClass);
				}
				definedClass.fields.add(
						"public static final War3ID " + toConstant(WEString.getString(element.getField("displayName")))
								+ " = War3ID.fromString(\"" + key + "\");");
			}
		}
		for (Map.Entry<String, DefinedClass> entry : classNameToDefinition.entrySet()) {
			System.out.println("final class " + entry.getKey() + " {");
			System.out.println("\tprivate " + entry.getKey() + "() {}");
			System.out.println();
			for (String field : entry.getValue().fields) {
				System.out.println("\t" + field);
			}
			System.out.println("}");
		}
	}

	private static String toConstant(String secondPart) {
		StringBuilder updatedSecondPart = new StringBuilder();
		for (String word : secondPart.replace("(", " ").replace(")", " ").replace(",", " ").replace("%", "PERCENT")
				.replace("-", " ").replace("/", " OR ").split("\\s+")) {
			if (!word.isEmpty()) {
				if (!updatedSecondPart.isEmpty()) {
					updatedSecondPart.append("_");
				}
				updatedSecondPart.append(word.toUpperCase());
			}
		}
		return updatedSecondPart.toString().replace(".", "").replace("'", "").replace("!", "");
	}

	private static String toCamel(String secondPart) {
		StringBuilder updatedSecondPart = new StringBuilder();
		for (String word : secondPart.replace("(", " ").replace(")", " ").split("\\s+")) {
			if (!word.isEmpty()) {
				updatedSecondPart.append(Character.toUpperCase(word.charAt(0)));
				updatedSecondPart.append(word.substring(1).toLowerCase());
			}
		}
		updatedSecondPart.setCharAt(0, Character.toLowerCase(updatedSecondPart.charAt(0)));
		return updatedSecondPart.toString().replace(".", "").replace("'", "").replace("!", "");
	}

	private static String toTitle(String secondPart) {
		StringBuilder updatedSecondPart = new StringBuilder();
		for (String word : secondPart.replace("(", " ").replace(")", " ").replace("%", "PERCENT").replace("-", " ")
				.replace(",", " ").split("\\s+")) {
			if (!word.isEmpty()) {
				updatedSecondPart.append(Character.toUpperCase(word.charAt(0)));
				updatedSecondPart.append(word.substring(1).toLowerCase());
			}
		}
		return updatedSecondPart.toString().replace(".", "").replace("'", "").replace("!", "");
	}
}
