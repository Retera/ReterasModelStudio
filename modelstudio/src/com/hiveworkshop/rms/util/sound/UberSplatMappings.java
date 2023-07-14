package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UberSplatMappings extends EventMapping<UberSplatMappings.UberSplat>{
	private final Map<String, UberSplat> tagToEvent = new HashMap<>();

	public UberSplatMappings(){
		tagToEvent.clear();
		updateMappings();
	}

	public UberSplat getEvent(String eventCode){
		return tagToEvent.get(eventCode.substring(4));
	}

	public Collection<UberSplat> getEvents(){
		return tagToEvent.values();
	}

	public void updateMappings() {
		tagToEvent.clear();
		readLookups("war3.w3mod\\splats\\ubersplatdata.slk");
		currentUberSplat = null;
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
//		currentUberSplat = null;
//	}

	UberSplat currentUberSplat;

	protected void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1;")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currentUberSplat = tagToEvent.computeIfAbsent(strings[1], k -> new UberSplat(strings[1]));
			} else {
				currentUberSplat = null;
			}

		} else if (currentUberSplat != null) {
			currentUberSplat.setFromSklLine(s);
		}
	}

	public static class UberSplat extends EventTarget {
		private String tag;
		private String name; // comment
		private String dir;
		private String file;

		private int blendMode;
		private int scale;
		private float birthTime;
		private float pauseTime;
		private float decay;
		private Vec4 startRGBA = new Vec4();
		private Vec4 middleRGBA = new Vec4();
		private Vec4 endRGBA = new Vec4();
		private String soundTag;
		private int version;
		private int inBeta;
//		int startR;
//		int startG;
//		int startB;
//		int startA;
//		int middleR;
//		int middleG;
//		int middleB;
//		int middleA;
//		int endR;
//		int endG;
//		int endB;
//		int endA;

		UberSplat(String tag) {
			this.tag = tag;
		}

		UberSplat setFromSklLine(String lineString) {

			String[] split = lineString.split(";");
			if (1 < split.length) {
				switch (split[1]) {
					case "X1" -> tag = getString(split[2]);
					case "X2" -> name = getString(split[2]);
					case "X3" -> dir = getString(split[2]);
					case "X4" -> file = getString(split[2]);
					case "X5" -> blendMode = getInt(split[2]);
					case "X6" -> scale = getInt(split[2]);
					case "X7" -> birthTime = getFloat(split[2]);
					case "X8" -> pauseTime = getFloat(split[2]);
					case "X9" -> decay = getFloat(split[2]);
					case "X10" -> startRGBA.x = getInt(split[2]);
					case "X11" -> startRGBA.y = getInt(split[2]);
					case "X12" -> startRGBA.z = getInt(split[2]);
					case "X13" -> startRGBA.w = getInt(split[2]);
					case "X14" -> middleRGBA.x = getInt(split[2]);
					case "X15" -> middleRGBA.y = getInt(split[2]);
					case "X16" -> middleRGBA.z = getInt(split[2]);
					case "X17" -> middleRGBA.w = getInt(split[2]);
					case "X18" -> endRGBA.x = getInt(split[2]);
					case "X19" -> endRGBA.y = getInt(split[2]);
					case "X20" -> endRGBA.z = getInt(split[2]);
					case "X21" -> endRGBA.w = getInt(split[2]);
					case "X22" -> soundTag = getString(split[2]);
					case "X23" -> version = getInt(split[2]);
					case "X24" -> inBeta = getInt(split[2]);
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
			return name;
		}

		public String getTag() {
			return tag;
		}
		public String[] getFileNames() {
			return new String[] {file};
		}

		public String[][] getFileNameAndPaths() {
			if (file != null) {
				String[][] paths = new String[1][2];
				for (int i = 0; i < paths.length; i++) {
					paths[i][0] = file;
					paths[i][1] = dir + "\\" + file;
				}
				return paths;
			}
			return new String[0][0];
		}

		public int getBlendMode() {
			return blendMode;
		}

		public int getScale() {
			return scale;
		}

		public float getBirthTime() {
			return birthTime;
		}

		public float getPauseTime() {
			return pauseTime;
		}

		public float getDecay() {
			return decay;
		}

		public Vec4 getStartRGBA() {
			return startRGBA;
		}

		public Vec4 getMiddleRGBA() {
			return middleRGBA;
		}

		public Vec4 getEndRGBA() {
			return endRGBA;
		}

		public String getSoundTag() {
			return soundTag;
		}

		public int getVersion() {
			return version;
		}

		public int getInBeta() {
			return inBeta;
		}
	}
}
