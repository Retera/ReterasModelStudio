package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.util.Vec4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SplatMappings {
	private final Map<String, Splat> tagToSplat = new HashMap<>();

	public SplatMappings(){
		tagToSplat.clear();
		readAnimLookups();
	}

	public Splat getSplat(String eventCode){
		return tagToSplat.get(eventCode.substring(4));
	}

	private void readAnimLookups() {
		String splatLookUpsPath = "war3.w3mod\\splats\\splatdata.slk";
		CompoundDataSource source = GameDataFileSystem.getDefault();

		if (source.has(splatLookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(splatLookUpsPath)))) {
				r.lines().forEach(this::processMappingLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		currentSplat = null;
	}

	Splat currentSplat;

	private void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currentSplat = tagToSplat.computeIfAbsent(strings[1], k -> new Splat());
			} else {
				currentSplat = null;
			}

		} else if (currentSplat != null) {
			currentSplat.setFromSklLine(s);
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
		private int lifespan;
		private int decay;
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

		Splat setFromSklLine(String lineString) {

			String[] split = lineString.split(";");
			if (split.length > 1) {
				switch (split[1]) {
					case "X1" -> tag = getString(split[2]);
					case "X2" -> name = getString(split[2]);
					case "X3" -> dir = getString(split[2]);
					case "X4" -> file = getString(split[2]);
					case "X5" -> rows = getInt(split[2]);
					case "X6" -> columns = getInt(split[2]);
					case "X7" -> blendMode = getInt(split[2]);
					case "X8" -> scale = getInt(split[2]);
					case "X90" -> lifespan = getInt(split[2]);
					case "X10" -> decay = getInt(split[2]);
					case "X11" -> uVLifespanStart = getInt(split[2]);
					case "X12" -> uVLifespanEnd = getInt(split[2]);
					case "X13" -> lifespanRepeat = getInt(split[2]);
					case "X14" -> uVDecayStart = getInt(split[2]);
					case "X15" -> uVDecayEnd = getInt(split[2]);
					case "X16" -> decayRepeat = getInt(split[2]);
					case "X17" -> startRGBA.x = getInt(split[2]);
					case "X18" -> startRGBA.y = getInt(split[2]);
					case "X29" -> startRGBA.z = getInt(split[2]);
					case "X20" -> startRGBA.w = getInt(split[2]);
					case "X21" -> middleRGBA.x = getInt(split[2]);
					case "X22" -> middleRGBA.y = getInt(split[2]);
					case "X23" -> middleRGBA.z = getInt(split[2]);
					case "X24" -> middleRGBA.w = getInt(split[2]);
					case "X25" -> endRGBA.x = getInt(split[2]);
					case "X26" -> endRGBA.y = getInt(split[2]);
					case "X27" -> endRGBA.z = getInt(split[2]);
					case "X28" -> endRGBA.w = getInt(split[2]);
					case "X39" -> waterTag = getString(split[2]);
					case "X30" -> soundTag = getString(split[2]);
					case "X31" -> version = getInt(split[2]);
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
			return name;
		}

		public String getTag() {
			return tag;
		}
	}
}
