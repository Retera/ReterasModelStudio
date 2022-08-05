package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Locale;

public class ValueParserUtil {
	//		if (!valueString.matches("\\{? ?(-?\\d*\\.?\\d+)( ?, ?(-?\\d*\\.?\\d+))* ?}?"))
	public Quat parseQuat(String valueString) {
		int vectorSize = 4;

		return Quat.parseQuat(getString(vectorSize, valueString));
	}

	public Vec3 parseVec3(String valueString) {
		int vectorSize = 3;

		return Vec3.parseVec3(getString(vectorSize, valueString));
	}

	Float parseValueFloat(String valueString) {
		valueString = valueString.toLowerCase(Locale.ROOT).replaceAll("[^-\\.e\\d]", "");
		valueString = valueString.replaceAll("(\\.+)", ".");
		if (valueString.matches("(-?\\d+\\.+)")) {
			valueString = valueString.replace(".", "");
		}
		if (valueString.matches(".*\\d.*") && valueString.matches("(-?\\d*\\.?\\d+(e-?\\d+)?)")) {
			return Float.parseFloat(valueString);
		}
		return 0.0f;
	}

//	private String getGetString1(int vectorSize, String[] split) {
//		String ugg = "";
//		if (split.length < vectorSize) {
//			ugg += getDupedString(vectorSize, split);
//		} else {
//			if (split[2].equals("")) {
//				ugg += split[1];
//			}
//		}
//		return ugg;
//	}

//	private String getDupedString(int vectorSize, String[] split) {
//		int vecSize = split.length;
//		String addString = getAddString(split, vecSize);
//		StringBuilder combinedString = new StringBuilder();
//		for(int i = vecSize; i<vectorSize && i<3; i++){
//			combinedString.append(addString);
//		}
//		return combinedString.toString();
//	}

//	private String getAddString(String[] split, int vecSize) {
//		String addString = ",0";
//		if (!split[vecSize - 1].equals("")) {
//			addString = "," + split[vecSize - 1];
//		}
//		return addString;
//	}

	public static String getString(int vectorSize, String valueString) {
//		valueString = valueString.toLowerCase(Locale.ROOT).replaceAll("[^-\\.e\\d,]", "");
		String[] split = getSplitStrings(valueString, vectorSize);
		StringBuilder newS = new StringBuilder();
		int vecSize = split.length;
		for (int i = 0; i < vectorSize; i++) {
			if (vecSize != 0) {
				if(i < 3 || i == 3 && vecSize>3){
					newS.append(getSubString(split[Math.min(i, vecSize - 1)]));
				} else {
					newS.append("1.0");
				}
			} else {
				newS.append("0");
			}
			if (i < vectorSize - 1) {
				newS.append(",");
			}
		}
//		System.out.println("polished string: " + newS.toString());
		return newS.toString();
	}

	private static String[] getSplitStrings(String valueString, int vectorSize) {
//		valueString = valueString.toLowerCase(Locale.ROOT).replaceAll("[^-\\.e\\d,]", "");
		String polishedString = valueString.replaceAll("[\\{} ]", "");
		String[] split = polishedString.split(",");
		if (split.length < vectorSize) {
			polishedString = polishedString.replaceAll("\\.\\.", ".0,.");
			split = polishedString.split(",");
		}
		return split;
	}

	private static String getSubString(String s) {
//		System.out.println("string: " + s);
		if (s.equals("") || s.equals(".")) {
			return "0";
		} else if (s.matches("-?\\d+\\.")) {
			return s + "0";
		} else if (s.matches("-?\\d*\\.\\d+\\..*")) {
			return s.substring(0, s.indexOf(".", s.indexOf(".") + 1));
		}
		return s;
	}
}
