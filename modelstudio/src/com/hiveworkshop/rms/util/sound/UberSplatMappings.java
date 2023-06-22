package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.util.Vec4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class UberSplatMappings {
	private final Map<String, UberSplat> tagToSplat = new HashMap<>();

	public UberSplatMappings(){
		tagToSplat.clear();
		readAnimLookups();
	}

	public UberSplat getSplat(String eventCode){
		return tagToSplat.get(eventCode.substring(4));
	}

	private void readAnimLookups() {
		String splatLookUpsPath = "war3.w3mod\\splats\\ubersplatdata.slk";
		CompoundDataSource source = GameDataFileSystem.getDefault();

		if (source.has(splatLookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(splatLookUpsPath)))) {
				r.lines().forEach(this::processMappingLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		currentUberSplat = null;
	}

	UberSplat currentUberSplat;

	private void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currentUberSplat = tagToSplat.computeIfAbsent(strings[1], k -> new UberSplat());
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
		private int birthTime;
		private int pauseTime;
		private int decay;
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

		UberSplat setFromSklLine(String lineString) {

			String[] split = lineString.split(";");
			if (split.length > 1) {
				switch (split[1]) {
					case "X1" -> tag = getString(split[2]);
					case "X2" -> name = getString(split[2]);
					case "X3" -> dir = getString(split[2]);
					case "X4" -> file = getString(split[2]);
					case "X5" -> blendMode = getInt(split[2]);
					case "X6" -> scale = getInt(split[2]);
					case "X7" -> birthTime = getInt(split[2]);
					case "X8" -> pauseTime = getInt(split[2]);
					case "X90" -> decay = getInt(split[2]);
					case "X10" -> startRGBA.x = getInt(split[2]);
					case "X11" -> startRGBA.y = getInt(split[2]);
					case "X12" -> startRGBA.z = getInt(split[2]);
					case "X13" -> startRGBA.w = getInt(split[2]);
					case "X14" -> middleRGBA.x = getInt(split[2]);
					case "X15" -> middleRGBA.y = getInt(split[2]);
					case "X16" -> middleRGBA.z = getInt(split[2]);
					case "X17" -> middleRGBA.w = getInt(split[2]);
					case "X18" -> endRGBA.x = getInt(split[2]);
					case "X29" -> endRGBA.y = getInt(split[2]);
					case "X20" -> endRGBA.z = getInt(split[2]);
					case "X21" -> endRGBA.w = getInt(split[2]);
					case "X22" -> soundTag = getString(split[2]);
					case "X23" -> version = getInt(split[2]);
					case "X24" -> inBeta = getInt(split[2]);
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
