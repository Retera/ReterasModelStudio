package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import java.util.ArrayList;

enum SupportedCascPatchFormat {
	PATCH130("", new String[] {"war3.mpq", "deprecated.mpq", "LOCALE" + "-war3local.mpq"}),
	PATCH131("", new String[] {"war3.w3mod", "war3.w3mod\\_deprecated.w3mod", "war3.w3mod\\_locales\\" + "LOCALE" + ".w3mod"}),
	PATCH132("", new String[] {"war3.w3mod", "war3.w3mod\\_deprecated.w3mod", "war3.w3mod\\_locales\\" + "LOCALE" + ".w3mod", "war3.w3mod\\_hd.w3mod", "war3.w3mod\\_hd.w3mod\\_locales\\" + "LOCALE" + ".w3mod"}),
	UNKNOWN_FUTURE_PATCH("", new String[] {"war3.w3mod", "war3.w3mod", "war3.w3mod\\_deprecated.w3mod", "war3.w3mod\\_locales\\" + "LOCALE" + ".w3mod"});


	String filePathToTest;
	String[] prefixes;

	SupportedCascPatchFormat(String filePathToTest, String[] prefixes){
		this.filePathToTest = filePathToTest;
		this.prefixes = prefixes;
	}

	public ArrayList<String> getPrefixes(String locale) {
		ArrayList<String> list = new ArrayList<>();
		locale = locale.toLowerCase();
		for(String prefix : prefixes) {
			list.add(prefix.replace("LOCALE", locale));
		}
		return list;
	}
}
