package com.hiveworkshop.rms.util.sound;

public class Sound extends EventTarget {
	private String soundName;
	private String tag;
	private String[] fileNames;
	private String directoryBase;
	private int volume;
	private float volumeVariance;
	private float pitch;
	private float pitchVariance;
	private int maximumConcurrentInstances;
	private int priority;
	private int channel;
	private String flags;
	private int minDistance;
	private int maxDistance;
	private int distanceCutoff;
	private String eAXFlags;
	private int inBeta;
	private int version;
	private String rolloffPoints;

	Sound(String[] tag_name_inBeta) {
		tag = tag_name_inBeta[0];
		soundName = tag_name_inBeta[1];
		inBeta = Integer.parseInt(tag_name_inBeta[2]);
	}
	Sound(String tag) {
		this.tag = tag;
	}
	Sound setFromSklLine(String lineString) {
		String[] split = lineString.split(";");
//		System.out.println(Arrays.toString(split));
		if (1 < split.length) {
			switch (split[1]) {
				case "X1" -> tag = getString(split[2]);
				case "X2" -> soundName = getString(split[2]);
				case "X3" -> inBeta = getInt(split[2]);
			}
		}
		return this;
	}


	Sound setFromSklLine2(String lineString) {

		String[] split = lineString.split(";");
//		System.out.println(Arrays.toString(split));
		if (1 < split.length) {
			switch (split[1]) {
				case "X1" -> soundName = getString(split[2]).split(",")[2];
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


	Sound setFromSklLine3(String lineString) {

		String[] split = lineString.split(";(Y\\d+;)?");

		if (1 < split.length) {
//			if ("X3".matches(split[1])) {
//				String s2 = split[2];
//				String s3 = getString(s2);
//				String[] split1 = s3.split(",");
//				System.out.println(tag + "  " + lineString + "  " + s2 + "  " + s3 + "  " + Arrays.toString(split1));
//			}
			switch (split[1]) {
				case "X1" -> soundName = getString(split[2]);
				case "X2" -> tag = getString(split[2]);
				case "X3" -> fileNames = getString(split[2]).split(",");
				case "X4" -> volume = getInt(split[2]);
				case "X5" -> volumeVariance = getInt(split[2]);
				case "X6" -> pitch = getFloat(split[2]);
				case "X7" -> pitchVariance = getFloat(split[2]);
				case "X8" -> maximumConcurrentInstances = getInt(split[2]);
				case "X9" -> priority = getInt(split[2]);
				case "X10" -> channel = getInt(split[2]);
				case "X11" -> flags = getString(split[2]);
				case "X12" -> minDistance = getInt(split[2]);
				case "X13" -> maxDistance = getInt(split[2]);
				case "X14" -> distanceCutoff = getInt(split[2]);
				case "X15" -> eAXFlags = getString(split[2]);
				case "X16" -> version = getInt(split[2]);
				case "X17" -> rolloffPoints = getString(split[2]);
			}
		}
		return this;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public String[] getFilePaths() {
		if (fileNames != null) {
			String[] paths = new String[fileNames.length];
			for (int i = 0; i < paths.length; i++) {
				paths[i] = (directoryBase == null ? "" : directoryBase) + fileNames[i];
			}
			return paths;
		}
		return new String[0];
	}
	public String[][] getFileNameAndPaths() {
		if (fileNames != null) {
			String[][] paths = new String[fileNames.length][2];
			for (int i = 0; i < paths.length; i++) {
				paths[i][0] = fileNames[i];
				paths[i][1] = (directoryBase == null ? "" : directoryBase) + fileNames[i];
			}
			return paths;
		}
		return new String[0][0];
	}

	public String getSoundName() {
		return soundName;
	}
	public String getName() {
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

	public float getPitch() {
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
