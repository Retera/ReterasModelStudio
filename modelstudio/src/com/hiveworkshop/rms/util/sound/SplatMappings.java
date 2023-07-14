package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;

public class SplatMappings extends EventMapping<SplatMappings.Splat> {
//	private final Map<String, Splat> tagToEvent = new HashMap<>();

	public SplatMappings(){
		tagToEvent.clear();
		updateMappings();
	}

	public Splat getEvent(String eventCode){
		return tagToEvent.get(eventCode.substring(4));
	}
	public Collection<Splat> getEvents(){
		return tagToEvent.values();
	}

	public void updateMappings() {
		tagToEvent.clear();
		readLookups("war3.w3mod\\splats\\splatdata.slk");
		currentSplat = null;
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
//		currentSplat = null;
//	}

	Splat currentSplat;

	protected void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1;")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currentSplat = tagToEvent.computeIfAbsent(strings[1], k -> new Splat(strings[1]));
			} else {
				currentSplat = null;
			}

		} else if (currentSplat != null) {
			try {
				currentSplat.setFromSklLine(s);

			} catch (Exception e) {
				System.err.println("splat:  \"" + s + "\"");
				throw new RuntimeException("failed to parse '" + s + "'", e);

			}
		}
	}

	public static class Splat extends EventTarget {
		private String tag;
		private String name; // comment
		private String dir;
		private String file;

		private int rows;
		private int columns;
		private int blendMode;
		private int scale;
		private float lifespan;
		private float decay;
		private int uVLifespanStart;
		private int uVLifespanEnd;
		private int lifespanRepeat;
		private int uVDecayStart;
		private int uVDecayEnd;
		private int decayRepeat;
		private Vec4 startRGBA = new Vec4();
		private Vec4 middleRGBA = new Vec4();
		private Vec4 endRGBA = new Vec4();
		private String waterTag;
		private String soundTag;
		private int version;
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

		Splat(String tag){
			this.tag = tag;
		}

		Splat setFromSklLine(String lineString) {

			String[] split = lineString.split(";");
			if (1 < split.length) {
				switch (split[1]) {
					case "X1" -> tag = getString(split[2]);
					case "X2" -> name = getString(split[2]);
					case "X3" -> dir = getString(split[2]);
					case "X4" -> file = getString(split[2]);
					case "X5" -> rows = getInt(split[2]);
					case "X6" -> columns = getInt(split[2]);
					case "X7" -> blendMode = getInt(split[2]);
					case "X8" -> scale = getInt(split[2]);
					case "X9" -> lifespan = getFloat(split[2]);
					case "X10" -> decay = getFloat(split[2]);
					case "X11" -> uVLifespanStart = getInt(split[2]);
					case "X12" -> uVLifespanEnd = getInt(split[2]);
					case "X13" -> lifespanRepeat = getInt(split[2]);
					case "X14" -> uVDecayStart = getInt(split[2]);
					case "X15" -> uVDecayEnd = getInt(split[2]);
					case "X16" -> decayRepeat = getInt(split[2]);
					case "X17" -> startRGBA.x = getInt(split[2]);
					case "X18" -> startRGBA.y = getInt(split[2]);
					case "X19" -> startRGBA.z = getInt(split[2]);
					case "X20" -> startRGBA.w = getInt(split[2]);
					case "X21" -> middleRGBA.x = getInt(split[2]);
					case "X22" -> middleRGBA.y = getInt(split[2]);
					case "X23" -> middleRGBA.z = getInt(split[2]);
					case "X24" -> middleRGBA.w = getInt(split[2]);
					case "X25" -> endRGBA.x = getInt(split[2]);
					case "X26" -> endRGBA.y = getInt(split[2]);
					case "X27" -> endRGBA.z = getInt(split[2]);
					case "X28" -> endRGBA.w = getInt(split[2]);
					case "X29" -> waterTag = getString(split[2]);
					case "X30" -> soundTag = getString(split[2]);
					case "X31" -> version = getInt(split[2]);
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

		public int getRows() {
			return rows;
		}

		public int getColumns() {
			return columns;
		}

		public int getBlendMode() {
			return blendMode;
		}

		public int getScale() {
			return scale;
		}

		public float getLifespan() {
			return lifespan;
		}

		public float getDecay() {
			return decay;
		}

		public int getuVLifespanStart() {
			return uVLifespanStart;
		}

		public int getuVLifespanEnd() {
			return uVLifespanEnd;
		}

		public int getLifespanRepeat() {
			return lifespanRepeat;
		}

		public int getuVDecayStart() {
			return uVDecayStart;
		}

		public int getuVDecayEnd() {
			return uVDecayEnd;
		}

		public int getDecayRepeat() {
			return decayRepeat;
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

		public String getWaterTag() {
			return waterTag;
		}

		public String getSoundTag() {
			return soundTag;
		}
	}
}
