package com.hiveworkshop.wc3.jworldedit.wipdesign;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hiveworkshop.wc3.jworldedit.wipdesign.units.meta.Category;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData;

public class MetaDataToJavaClassConverter {
	private final ObjectData metaData;

	public MetaDataToJavaClassConverter(final ObjectData metaData) {
		this.metaData = metaData;
	}

	public void printJavaClass(final PrintStream out) {

	}

	public static void main(final String[] args) {
		generateUnitClassWithFields();
	}

	private static void generateUnitClassWithTable() {
		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
	}

	private static void generateUnitClassWithFields() {
		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		final MetaDataToJavaClassConverter converter = new MetaDataToJavaClassConverter(standardUnitMeta);
		final Map<String, String> typeToJava = new HashMap<>();
		typeToJava.put("string", "String");
		typeToJava.put("real", "float");
		typeToJava.put("int", "int");
		typeToJava.put("unreal", "float");
		typeToJava.put("bool", "boolean");
		typeToJava.put("char", "char");
		typeToJava.put("abilCode", "Ability");
		for (final String key : standardUnitMeta.get("unam").keySet()) {
			System.out.println(key);
		}
		System.out.println("------");
		final Set<String> types = new HashSet<>();
		final List<Element> unitMetaFields = new ArrayList<>();
		for (final String key : standardUnitMeta.keySet()) {
			final Element metaField = standardUnitMeta.get(key);
			if ((metaField.getFieldValue("useUnit") == 1) && (metaField.getFieldValue("useBuilding") == 1)) {
				System.out.println(WEString.getString(metaField.getField("displayName")));
				final String type = metaField.getField("type");
				System.out.println(type);
				types.add(type);
				unitMetaFields.add(metaField);
			}
		}
		Collections.sort(unitMetaFields, new Comparator<Element>() {
			@Override
			public int compare(final Element a, final Element b) {
				return WEString.getString(a.getField("displayName"))
						.compareTo(WEString.getString(b.getField("displayName")));
			}

		});
		System.out.println("------TYPES ENUMERATED------");
		for (final String type : types) {
			if (!typeToJava.containsKey(type)) {
				String typeString = type;
				boolean isList = false;
				if (typeString.endsWith("List")) {
					isList = true;
					typeString = typeString.substring(0, typeString.length() - 4);
				}
				if (typeToJava.containsKey(typeString)) {
					typeString = typeToJava.get(typeString);
				} else {
					typeString = Character.toUpperCase(typeString.charAt(0)) + typeString.substring(1);
				}
				if (isList) {
					typeString = "List<" + typeString + ">";
				}
				typeToJava.put(type, typeString);
			}
			System.out.println(type);
		}
		System.out.println("------------");
		System.out.println("public class Unit {");
		for (final Element metaField : unitMetaFields) {
			if ((metaField.getFieldValue("useUnit") == 1) && (metaField.getFieldValue("useBuilding") == 1)) {
				final String baseDisplayName = WEString.getString(metaField.getField("displayName"));
				String displayName = baseDisplayName;
				displayName = displayName.replace('-', '_');
				displayName = displayName.replace('(', '_');
				displayName = displayName.replace(")", "");
				final StringBuffer finalDisplayName = new StringBuffer();
				for (final String s : displayName.split("\\s+")) {
					finalDisplayName.append(Character.toUpperCase(s.charAt(0)) + s.substring(1));
				}
				final String type = metaField.getField("type");
				System.out.println("\t@Rawcode(\"" + metaField.getId() + "\")");
				System.out.println("\t@SourceLocation(\"" + metaField.getField("slk") + "\")");
				System.out.println("\t@EditorCategory(Category."
						+ Category.fromCodeName(metaField.getField("category")).name() + ")");
				System.out.println("\t@DisplayName(\"" + baseDisplayName + "\")");
				if (metaField.hasField("sort")) {
					System.out.println("\t@SortLabel(\"" + metaField.getField("sort") + "\")");
				}
				if (metaField.getFieldValue("caseSens") > 0) {
					System.out.println("\t@CaseSensitive");
				}
				if (metaField.getFieldValue("canBeEmpty") > 0) {
					System.out.println("\t@CanBeEmpty");
				}
				if (metaField.hasField("minVal")) {
					System.out.println("\t@MinValue(" + metaField.getField("minVal") + "f)");
				}
				if (metaField.hasField("maxVal")) {
					final String maxVal = metaField.getField("maxVal");
					if (maxVal.contains("TT")) {
						if (maxVal.equals("TTDesc")) {
							System.out.println("\t@TTDesc");
						} else if (maxVal.equals("TTName")) {
							System.out.println("\t@TTName");
						} else if (maxVal.equals("TTUber")) {
							System.out.println("\t@TTUber");
						}
					} else {
						System.out.println("\t@MaxValue(" + maxVal + "f)");
					}
				}

				System.out
						.println("\t" + typeToJava.get(type) + " " + lowerCaseTitle(finalDisplayName.toString()) + ";");
			}
		}
		for (final Element metaField : unitMetaFields) {
			if ((metaField.getFieldValue("useUnit") == 1) && (metaField.getFieldValue("useBuilding") == 1)) {
				final String baseDisplayName = WEString.getString(metaField.getField("displayName"));
				String displayName = baseDisplayName;
				displayName = displayName.replace('-', '_');
				displayName = displayName.replace('(', '_');
				displayName = displayName.replace(")", "");
				final StringBuffer finalDisplayName = new StringBuffer();
				for (final String s : displayName.split("\\s+")) {
					finalDisplayName.append(Character.toUpperCase(s.charAt(0)) + s.substring(1));
				}
				final String type = metaField.getField("type");

				System.out.println("\tpublic " + typeToJava.get(type) + " get" + finalDisplayName.toString() + "() {");
				System.out.println("\t\treturn " + lowerCaseTitle(finalDisplayName.toString()) + ";");
				System.out.println("\t}");
				System.out.println("\tpublic void set" + finalDisplayName.toString() + "(" + typeToJava.get(type) + " "
						+ lowerCaseTitle(finalDisplayName.toString()) + ") {");
				System.out.println("\t\tthis." + lowerCaseTitle(finalDisplayName.toString()) + " = "
						+ lowerCaseTitle(finalDisplayName.toString()) + ";");
				System.out.println("\t}");
			}
		}
		System.out.println("}");
	}

	public static String lowerCaseTitle(final String title) {
		return Character.toLowerCase(title.charAt(0)) + title.substring(1);
	}

	public static String upperCaseTitle(final String title) {
		return Character.toUpperCase(title.charAt(0)) + title.substring(1);
	}
}
