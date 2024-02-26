package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import java.util.ArrayList;

enum SupportedCascPatchFormat {
	PATCH130("1.30", new String[] {"war3.mpq", "deprecated.mpq", "LOCALE" + "-war3local.mpq"}),
	PATCH131("1.31", new String[] {"war3.w3mod", "war3.w3mod\\_deprecated.w3mod", "war3.w3mod\\_locales\\" + "LOCALE" + ".w3mod"}),
	PATCH132("1.32", new String[] {"war3.w3mod", "war3.w3mod\\_deprecated.w3mod", "war3.w3mod\\_locales\\" + "LOCALE" + ".w3mod", "war3.w3mod\\_hd.w3mod", "war3.w3mod\\_hd.w3mod\\_locales\\" + "LOCALE" + ".w3mod"}),
	UNKNOWN_FUTURE_PATCH("?", new String[] {"war3.w3mod", "war3.w3mod", "war3.w3mod\\_deprecated.w3mod", "war3.w3mod\\_locales\\" + "LOCALE" + ".w3mod"});


	final String display;
	final String[] prefixes;

	SupportedCascPatchFormat(String display, String[] prefixes){
		this.display = display;
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
