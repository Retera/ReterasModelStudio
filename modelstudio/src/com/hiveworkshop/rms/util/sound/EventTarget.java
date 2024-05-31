package com.hiveworkshop.rms.util.sound;

public abstract class EventTarget {
	public abstract String getName();
	public abstract String getTag();
	public abstract String[] getFileNames();
	public abstract String[] getFilePaths();
	public abstract String[][] getFileNameAndPaths();


	protected int getInt(String s) {
		return Integer.parseInt(s.split("K")[1]);
	}

	protected float getFloat(String s) {
		return Float.parseFloat(s.split("K")[1]);
	}

	protected String getString(String s) {
		if (s.contains("\"")) {
			return s.split("\"")[1];
		}
		return "";
	}
	@Override
	public String toString() {
		return getTag() + " " + getName();
	}



	public static String getFullTag(EventTarget eventTarget) {
		if (eventTarget instanceof Sound sound) {
			return "SNDx" + sound.getTag();
		} else if (eventTarget instanceof SplatMappings.Splat splat) {
			if (splat.getName().contains("Footprint") || splat.getName().contains("FootPrint")) {
				return "FPTx" + splat.getTag();
			} else {
				return "SPLx" + splat.getTag();
			}
		} else if (eventTarget instanceof UberSplatMappings.UberSplat splat) {
			return "UBRx" + splat.getTag();
		} else if (eventTarget instanceof SpawnMappings.Spawn spawn) {
			return "SPNx" + spawn.getTag();
		}
		return "";
	}
}
