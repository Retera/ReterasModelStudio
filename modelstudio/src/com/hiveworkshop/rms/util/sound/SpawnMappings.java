package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SpawnMappings {
	private final Map<String, Spawn> tagToSpawn = new HashMap<>();

	public SpawnMappings(){
		tagToSpawn.clear();
		readAnimLookups();
	}

	public Spawn getSplat(String eventCode){
		return tagToSpawn.get(eventCode.substring(4));
	}

	private void readAnimLookups() {
		String splatLookUpsPath = "war3.w3mod\\splats\\spawndata.slk";
		CompoundDataSource source = GameDataFileSystem.getDefault();

		if (source.has(splatLookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(splatLookUpsPath)))) {
				r.lines().forEach(this::processMappingLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		currentSpawn = null;
	}

	Spawn currentSpawn;

	private void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currentSpawn = tagToSpawn.computeIfAbsent(strings[1], k -> new Spawn());
			} else {
				currentSpawn = null;
			}

		} else if (currentSpawn != null) {
			currentSpawn.setFromSklLine(s);
		}
	}

	public static class Spawn extends EventTarget {
		private String tag;
		private String name; // comment
		private String file;
		private int version;
		private int inBeta;

		Spawn setFromSklLine(String lineString) {

			String[] split = lineString.split(";");
			if (split.length > 1) {
				switch (split[1]) {
					case "X1" -> tag = getString(split[2]);
					case "X2" -> file = getString(split[2]);
					case "X3" -> version = getInt(split[2]);
					case "X4" -> inBeta = getInt(split[2]);
				}
			}
			return this;
		}

		private int getInt(String s) {
			return Integer.parseInt(s.split("K")[1]);
		}

		private float getFloat(String s) {
			return Float.parseFloat(s.split("K")[1]);
		}

		private String getString(String s) {
			return s.split("\"")[1];
		}

		public String getName() {
			if(name == null && file != null){
				name = file.replaceAll("^(.*\\\\)+", "").replaceAll("(\\.\\w\\w\\w)?$", "");
			} else {
				name = tag;
			}
			return name;
		}

		public String getTag() {
			return tag;
		}
	}
}
