package com.hiveworkshop.rms.util.sound;

public class Sound {
	private String soundName;
	private String tag;
	private String[] fileNames;
	private String directoryBase;
	private int volume;
	private int pitch;
	private float pitchVariance;
	private int priority;
	private int channel;
	private String flags;
	private int minDistance;
	private int maxDistance;
	private int distanceCutoff;
	private String eAXFlags;
	private int inBeta;
	private int version;

	Sound(String[] tag_name_inBeta) {
		tag = tag_name_inBeta[0];
		soundName = tag_name_inBeta[1];
		inBeta = Integer.parseInt(tag_name_inBeta[2]);
	}

	Sound setFromSklLine(String lineString) {

		String[] split = lineString.split(";");
//		System.out.println(Arrays.toString(split));
		if (split.length > 1) {
			switch (split[1]) {
				case "X2" -> fileNames = getString(split[2]).split(",");
				case "X3" -> directoryBase = getString(split[2]);
				case "X4" -> volume = getInt(split[2]);
				case "X5" -> pitch = getInt(split[2]);
				case "X6" -> pitchVariance = getFloat(split[2]);
				case "X7" -> priority = getInt(split[2]);
				case "X8" -> channel = getInt(split[2]);
				case "X9" -> flags = getString(split[2]);
				case "X10" -> minDistance = getInt(split[2]);
				case "X11" -> maxDistance = getInt(split[2]);
				case "X12" -> distanceCutoff = getInt(split[2]);
				case "X13" -> eAXFlags = getString(split[2]);
				case "X14" -> inBeta = getInt(split[2]);
				case "X15" -> version = getInt(split[2]);
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

	public String[] getFileNames() {
		return fileNames;
	}

	public String[] getFilePaths() {
		if (fileNames != null) {
			String[] paths = new String[fileNames.length];
			for (int i = 0; i < paths.length; i++) {
				paths[i] = directoryBase + fileNames[i];
			}
			return paths;
		}
		return new String[0];
	}

	public String getSoundName() {
		return soundName;
	}

	public String getTag() {
		return tag;
	}

	public String getDirectoryBase() {
		return directoryBase;
	}

	public int getVolume() {
		return volume;
	}

	public int getPitch() {
		return pitch;
	}

	public float getPitchVariance() {
		return pitchVariance;
	}

	public int getPriority() {
		return priority;
	}

	public int getChannel() {
		return channel;
	}

	public String getFlags() {
		return flags;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public int getDistanceCutoff() {
		return distanceCutoff;
	}

	public String geteAXFlags() {
		return eAXFlags;
	}

	public int getInBeta() {
		return inBeta;
	}

	public int getVersion() {
		return version;
	}
}
