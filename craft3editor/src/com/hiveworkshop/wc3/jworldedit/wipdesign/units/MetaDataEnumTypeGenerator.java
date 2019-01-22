package com.hiveworkshop.wc3.jworldedit.wipdesign.units;

import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.StandardObjectData;

public final class MetaDataEnumTypeGenerator {
	public static void main(final String[] args) {
		final DataTable unitEditorData = StandardObjectData.getUnitEditorData();
		generate(unitEditorData);
		generateWEData(StandardObjectData.getWorldEditData());
	}

	private static void generate(final DataTable unitEditorData) {
		for (final String enumName : unitEditorData.keySet()) {
			final String enumJavaName = Character.toUpperCase(enumName.charAt(0)) + enumName.substring(1);
			System.out.println("public enum " + enumJavaName + " {");
			final Element editorType = unitEditorData.get(enumName);
			System.out.print("\t");
			for (int i = 0; i < editorType.getFieldValue("NumValues"); i++) {
				final String elementData = editorType.getField(String.format("%2d", i).replace(' ', '0'));
				final String[] codenameDispname = elementData.split(",");
				final String codename = codenameDispname[0];
				final String dispname = codenameDispname[1];
				if (i > 0) {
					System.out.print(", ");
				}
				final String[] westringTitleNameChunks = WEString.getString(dispname).split(" ");
				System.out.print(WEString.getString(dispname).replace(' ', '_').replace('-', '_').replace('(', '_').replace(")", "").replace(",", "_AND_").replace("/", "_OR_").replace("___", "_").replace("__", "_").replace("\'", "").replace("\"", "").replace("%", "PERCENT").replace("&", "").toUpperCase() + "(\"" + codename
						+ "\",\"" + dispname + "\")");
			}
			System.out.println(";");
			System.out.println("\tprivate final String codeName;");
			System.out.println("\tprivate final String dispName;");
			System.out.println("\t" + enumJavaName + "(final String codeName, final String dispName) {");
			System.out.println("\t\tthis.codeName = codeName;");
			System.out.println("\t\tthis.dispName = dispName;");
			System.out.println("\t}");
			System.out.println("\tpublic final String getDisplayName() {");
			System.out.println("		return WEString.getString(dispName);");
			System.out.println("	}");
			System.out.println("	final public String getCodeName() {");
			System.out.println("		return codeName;");
			System.out.println("	}");
			System.out.println("	public static "+enumJavaName+" fromCodeName(final String name) {");
			System.out.println("		for(final "+enumJavaName+" cat: values()) {");
			System.out.println("			if( cat.getCodeName().equals(name) ) {");
			System.out.println("				return cat;");
			System.out.println("			}");
			System.out.println("		}");
			System.out.println("		throw new IllegalArgumentException(\""+enumJavaName+" does not exist: \" + name);");
			System.out.println("	}");
			System.out.println("}");
		}
	}

	/**
	 * Jank hacked version, generates TileSet class
	 *
	 * @param unitEditorData
	 */
	private static void generateWEData(final DataTable unitEditorData) {
		for (final String enumName : unitEditorData.keySet()) {
			if( !enumName.equals("TileSets" ) ) {
				continue;
			}
			final String enumJavaName = Character.toUpperCase(enumName.charAt(0)) + enumName.substring(1);
			System.out.println("public enum " + enumJavaName + " {");
			final Element editorType = unitEditorData.get(enumName);
			System.out.print("\t");
			int i = 0;
			for(final String key: editorType.keySet()) {
				final String elementData = editorType.getField(key);
				final String[] codenameDispname = elementData.split(",");
				final String codename = key;//codenameDispname[0];
				final String dispname = codenameDispname[0];
				if (i > 0) {
					System.out.print(", ");
				}
				final String[] westringTitleNameChunks = WEString.getString(dispname).split(" ");
				System.out.print(WEString.getString(dispname).replace(' ', '_').replace('-', '_').replace('(', '_').replace(")", "").replace(",", "_AND_").replace("/", "_OR_").replace("___", "_").replace("__", "_").replace("\'", "").replace("\"", "").replace("%", "PERCENT").replace("&", "").toUpperCase() + "(\"" + codename
						+ "\",\"" + dispname + "\")");
				i++;
			}
			System.out.println(";");
			System.out.println("\tprivate final String codeName;");
			System.out.println("\tprivate final String dispName;");
			System.out.println("\t" + enumJavaName + "(final String codeName, final String dispName) {");
			System.out.println("\t\tthis.codeName = codeName;");
			System.out.println("\t\tthis.dispName = dispName;");
			System.out.println("\t}");
			System.out.println("\tpublic final String getDisplayName() {");
			System.out.println("		return WEString.getString(dispName);");
			System.out.println("	}");
			System.out.println("	final public String getCodeName() {");
			System.out.println("		return codeName;");
			System.out.println("	}");
			System.out.println("	public static "+enumJavaName+" fromCodeName(final String name) {");
			System.out.println("		for(final "+enumJavaName+" cat: values()) {");
			System.out.println("			if( cat.getCodeName().equals(name) ) {");
			System.out.println("				return cat;");
			System.out.println("			}");
			System.out.println("		}");
			System.out.println("		throw new IllegalArgumentException(\""+enumJavaName+" does not exist: \" + name);");
			System.out.println("	}");
			System.out.println("}");

//			final Unit peasant = new Unit();
//			for(final Unit structure: peasant.getStructuresBuilt()) {
//				structure.setArmorType(ArmorType.STONE);
//				structure.setDefenseType(DefenseType.FORTIFIED);
//				structure.setDefenseBase(2);
//			}
		}
	}
}
