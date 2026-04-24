package com.hiveworkshop.wc3.mdl;
import com.localizationmanager.localization.LocalizationManager;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

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

	public static SoundFile read(final BufferedReader mdl, final EditableModel mdlr) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("SoundFile")) {
			final SoundFile lay = new SoundFile();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
				if (line.contains("Path")) {
					lay.path = MDLReader.readName(line);
				} else if (line.contains("Volume")) {
					lay.volume = MDLReader.readDouble(line);
				} else if (line.contains("Pitch")) {
					lay.pitch = MDLReader.readDouble(line);
				} else if (line.contains("SoundChannel")) {
					lay.soundChannel = MDLReader.readInt(line);
				}
				MDLReader.mark(mdl);
			}
			return lay;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					LocalizationManager.getInstance().get("dialog.soundfile_read_parse"));
		}
		return null;
	}

	public void printTo(final PrintWriter writer, final int tabHeight, final boolean useCoords, final int version) {
		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		writer.println(tabs + LocalizationManager.getInstance().get("println.soundfile_printto_soundfile") + " {");
		writer.println(tabs + LocalizationManager.getInstance().get("println.soundfile_printto_path") + " \"" + path + "\",");
		writer.println(tabs + LocalizationManager.getInstance().get("println.soundfile_printto_volume") + MDLReader.doubleToString(volume) + ",");
		writer.println(tabs + LocalizationManager.getInstance().get("println.soundfile_printto_pitch") + MDLReader.doubleToString(pitch) + ",");
		writer.println(tabs + LocalizationManager.getInstance().get("println.soundfile_printto_soundchannel") + soundChannel + ",");
		writer.println(tabs + "}");
	}
}
