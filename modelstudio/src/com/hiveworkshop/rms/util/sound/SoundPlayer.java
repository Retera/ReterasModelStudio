package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.util.sound.SimpleGuiFlacPlayer.tempStuff.FlacFileReader;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SoundPlayer {
	public static void play(String soundFile, JButton button) {
		if (soundFile != null && !soundFile.equals("")
				&& (soundFile.toLowerCase(Locale.ROOT).endsWith(".wav")
				|| soundFile.toLowerCase(Locale.ROOT).endsWith(".flac"))) {
			try {
				if (bTC.containsKey(button)) {
					bTC.get(button).stop();
				} else {

//					button.setText("stop");
					play2(getFile(soundFile), button);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("could not find file: \"" + soundFile + "\"");
		}
	}


	static Map<JButton, Clip> bTC = new HashMap<>();
	public static void play2(File wavFile, JButton button) {
		AudioInputStream stream = getStream(wavFile);
		if (stream != null ) {
			try {
				DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat());
				Clip clip = (Clip) AudioSystem.getLine(info);

				clip.open(stream);

				FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(-10.0f);

				bTC.put(button, clip);
				button.setText("stop");
				clip.start();
				clip.addLineListener(event -> {
					if (event.getType() == LineEvent.Type.STOP) {
						button.setText("play");
						Clip remove = bTC.remove(button);
						if (remove != null) {
							clip.close();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("file was null");
		}
	}

	public static AudioInputStream getStream(File wavFile) {
		if (wavFile != null) {
			try {
				if (wavFile.getPath().toLowerCase(Locale.ROOT).endsWith(".wav")) {
					return AudioSystem.getAudioInputStream(wavFile);
				} else if (wavFile.getPath().toLowerCase(Locale.ROOT).endsWith(".flac")) {
					return new FlacFileReader().getAudioInputStream(wavFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("file was null");
		}
		return null;
	}

	private static File getFile(String filePath) {
		CompoundDataSource dataSource = GameDataFileSystem.getDefault();

		if (dataSource.has(filePath)) {
			return dataSource.getFile(filePath);
		} else {
			System.out.println("could not find \"" + filePath + "\"");
		}
		return null;
	}

	private static void printClipInfo(AudioInputStream stream, Clip clip) {
		AudioFormat format = stream.getFormat();
		System.out.println(format.properties() + ", " + format.properties().size());
		for(String k : format.properties().keySet()) {
			System.out.println(k + ", " + format.properties().get(k));
		}
//				System.out.println(Arrays.toString(clip.getControls()));
		System.out.println("VOLUME " + clip.isControlSupported(FloatControl.Type.VOLUME));
		System.out.println("MASTER_GAIN " + clip.isControlSupported(FloatControl.Type.MASTER_GAIN));
		clip.isControlSupported(FloatControl.Type.MASTER_GAIN);
		System.out.println("MASTER_GAIN " + clip.isControlSupported(FloatControl.Type.MASTER_GAIN));
		System.out.println("MUTE " + clip.isControlSupported(BooleanControl.Type.MUTE));
		System.out.println("VOLUME " + clip.isControlSupported(FloatControl.Type.VOLUME));
		System.out.println("REVERB " + clip.isControlSupported(EnumControl.Type.REVERB));
	}
}
