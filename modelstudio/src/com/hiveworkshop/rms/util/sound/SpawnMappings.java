package com.hiveworkshop.rms.util.sound;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpawnMappings extends EventMapping<SpawnMappings.Spawn> {
	private final Map<String, Spawn> tagToEvent = new HashMap<>();

	public SpawnMappings(){
		tagToEvent.clear();
		updateMappings();
	}

	public Spawn getEvent(String eventCode){
		return tagToEvent.get(eventCode.substring(4));
	}
	public Collection<Spawn> getEvents(){
		return tagToEvent.values();
	}

	public void updateMappings() {
		tagToEvent.clear();
		readLookups("war3.w3mod\\splats\\spawndata.slk");
		currentSpawn = null;
		System.out.println("SpawnMap created! " + tagToEvent.size() + " names, " + tagToEvent.size() + " tags");
	}

//	protected void readLookups(String lookUpsPath) {
//		CompoundDataSource source = GameDataFileSystem.getDefault();
//
//		if (source.has(lookUpsPath)) {
//			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(lookUpsPath)))) {
//				r.lines().forEach(this::processMappingLine);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		currentSpawn = null;
//	}

	Spawn currentSpawn;

	protected void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1;")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currentSpawn = tagToEvent.computeIfAbsent(strings[1], k -> new Spawn(strings[1]));
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

		Spawn(String tag) {
			this.tag = tag;
		}

		Spawn setFromSklLine(String lineString) {

			String[] split = lineString.split(";");
			if (1 < split.length) {
				switch (split[1]) {
					case "X1" -> tag = getString(split[2]);
					case "X2" -> file = getString(split[2]);
					case "X3" -> version = getInt(split[2]);
					case "X4" -> inBeta = getInt(split[2]);
				}
			}
			return this;
		}

//		private int getInt(String s) {
//			return Integer.parseInt(s.split("K")[1]);
//		}
//
//		private float getFloat(String s) {
//			return Float.parseFloat(s.split("K")[1]);
//		}
//
//		private String getString(String s) {
//			return s.split("\"")[1];
//		}

		public String getName() {
//			System.out.println("name: " + name + ", file: " + file);
			if (name == null && file != null) {
				name = file.replaceAll("^(.*\\\\)+", "").replaceAll("(\\.\\w\\w\\w)?$", "");
			} else if (name == null) {
				name = tag;
			}
			return name;
		}

		public String getTag() {
			return tag;
		}
		public String[] getFileNames() {
			return new String[] {file};
		}
	}
}
