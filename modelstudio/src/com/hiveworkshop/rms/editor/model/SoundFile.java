package com.hiveworkshop.rms.editor.model;

public class SoundFile {
	String path;
	double volume;
	double pitch;
	int soundChannel;

	public String getPath() {
		return path;
	}

	public double getVolume() {
		return volume;
	}

	public double getPitch() {
		return pitch;
	}

	public int getSoundChannel() {
		return soundChannel;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public void setVolume(final double volume) {
		this.volume = volume;
	}

	public void setPitch(final double pitch) {
		this.pitch = pitch;
	}

	public void setSoundChannel(final int soundChannel) {
		this.soundChannel = soundChannel;
	}

	// public static SoundFile read(final BufferedReader mdl, final EditableModel mdlr) {
	// 	String line = MDLReader.nextLine(mdl);
	// 	if (line.contains("SoundFile")) {
	// 		final SoundFile lay = new SoundFile();
	// 		MDLReader.mark(mdl);
	// 		while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
	// 			if (line.contains("Path")) {
	// 				lay.path = MDLReader.readName(line);
	// 			} else if (line.contains("Volume")) {
	// 				lay.volume = MDLReader.readDouble(line);
	// 			} else if (line.contains("Pitch")) {
	// 				lay.pitch = MDLReader.readDouble(line);
	// 			} else if (line.contains("SoundChannel")) {
	// 				lay.soundChannel = MDLReader.readInt(line);
	// 			}
	// 			MDLReader.mark(mdl);
	// 		}
	// 		return lay;
	// 	} else {
	// 		JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
	// 				"Unable to parse SoundFile: Missing or unrecognized open statement.");
	// 	}
	// 	return null;
	// }

	// public void printTo(final PrintWriter writer, final int tabHeight, final boolean useCoords, final int version) {
	// 	String tabs = "";
	// 	for (int i = 0; i < tabHeight; i++) {
	// 		tabs = tabs + "\t";
	// 	}
	// 	writer.println(tabs + "SoundFile {");
	// 	writer.println(tabs + "\tPath \"" + path + "\",");
	// 	writer.println(tabs + "\tVolume " + MDLReader.doubleToString(volume) + ",");
	// 	writer.println(tabs + "\tPitch " + MDLReader.doubleToString(pitch) + ",");
	// 	writer.println(tabs + "\tSoundChannel " + soundChannel + ",");
	// 	writer.println(tabs + "}");
	// }
}
