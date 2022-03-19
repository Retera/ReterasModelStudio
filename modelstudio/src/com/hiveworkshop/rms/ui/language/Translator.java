package com.hiveworkshop.rms.ui.language;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Translator {
	Map<TextKey, String> translations;
	File file;

	public Translator(){
		translations = new HashMap<>();
	}

	public Translator setLangFile(File file){
		this.file = file;
		if(file.exists()){
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
				reader.lines().forEach(this::parseLine);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	public Translator setUgg(){
		return this;
	}

	private Translator makeMap(String string){
		String[] lines = string.split("\n");
		for(String line : lines){
			parseLine(line);
		}
		return this;
	}

	private void parseLine(String line) {
		String[] s = line.split("=");
		if(s.length>1){
			try {
				TextKey textKey = TextKey.valueOf(s[0].strip());
				String translation = (s[1]);
				translations.put(textKey, translation);
			} catch (Exception e){
				System.err.println("Failed to parse text key \"" + line + "\"");
				e.printStackTrace();
			}
		}
	}

	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		for (TextKey textKey : TextKey.values()){
			stringBuilder.append(textKey.name()).append("=").append(textKey.toString()).append("\n");
		}
		return stringBuilder.toString();
	}

	public String getText(TextKey key){
		if (translations.get(key) == null){
			return key.getDefaultTranslation();
		}
		return translations.get(key);
	}
}
