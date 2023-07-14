package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SoundPlayer {
	public static void play(File wavFile) {
		if (wavFile != null && wavFile.getPath().toLowerCase(Locale.ROOT).endsWith(".wav")) {
			try {
				AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
				AudioFormat format = stream.getFormat();
//				System.out.println(format.properties() + ", " + format.properties().size());
//				for(String k : format.properties().keySet()){
//					System.out.println(k + ", " + format.properties().get(k));
//				}
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				Clip clip = (Clip) AudioSystem.getLine(info);
//				System.out.println(Arrays.toString(clip.getControls()));

//				System.out.println("VOLUME " + clip.isControlSupported(FloatControl.Type.VOLUME));
//				System.out.println("MASTER_GAIN " + clip.isControlSupported(FloatControl.Type.MASTER_GAIN));
//				clip.isControlSupported(FloatControl.Type.MASTER_GAIN);
				clip.open(stream);
//				System.out.println("MASTER_GAIN " + clip.isControlSupported(FloatControl.Type.MASTER_GAIN));
//				System.out.println("MUTE " + clip.isControlSupported(BooleanControl.Type.MUTE));
//				System.out.println("VOLUME " + clip.isControlSupported(FloatControl.Type.VOLUME));
//				System.out.println("REVERB " + clip.isControlSupported(EnumControl.Type.REVERB));

				FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(-10.0f);

				clip.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("file was null");
		}
	}

	public static void play(String wavFilePath) {
		if (wavFilePath != null && !wavFilePath.equals("") && wavFilePath.toLowerCase(Locale.ROOT).endsWith(".wav")) {
			try {
				play(getFile(wavFilePath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("could not find file");
		}
	}

	public static void play(String wavFilePath, JButton button) {
		if (wavFilePath != null && !wavFilePath.equals("") && wavFilePath.toLowerCase(Locale.ROOT).endsWith(".wav")) {
			try {
				if(bTC.containsKey(button)){
					bTC.get(button).stop();
				} else {

//					button.setText("stop");
					play2(getFile(wavFilePath), button);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("could not find file");
		}
	}


	static Map<JButton, Clip> bTC = new HashMap<>();
	public static void play2(File wavFile, JButton button) {
		if (wavFile != null && wavFile.getPath().toLowerCase(Locale.ROOT).endsWith(".wav")) {
			try {
				AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
				AudioFormat format = stream.getFormat();
//				System.out.println(format.properties() + ", " + format.properties().size());
//				for(String k : format.properties().keySet()){
//					System.out.println(k + ", " + format.properties().get(k));
//				}
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				Clip clip = (Clip) AudioSystem.getLine(info);
//				System.out.println(Arrays.toString(clip.getControls()));

//				System.out.println("VOLUME " + clip.isControlSupported(FloatControl.Type.VOLUME));
//				System.out.println("MASTER_GAIN " + clip.isControlSupported(FloatControl.Type.MASTER_GAIN));
//				clip.isControlSupported(FloatControl.Type.MASTER_GAIN);
				clip.open(stream);
//				System.out.println("MASTER_GAIN " + clip.isControlSupported(FloatControl.Type.MASTER_GAIN));
//				System.out.println("MUTE " + clip.isControlSupported(BooleanControl.Type.MUTE));
//				System.out.println("VOLUME " + clip.isControlSupported(FloatControl.Type.VOLUME));
//				System.out.println("REVERB " + clip.isControlSupported(EnumControl.Type.REVERB));

				FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(-10.0f);

				bTC.put(button, clip);
				button.setText("stop");
				clip.start();
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						if(event.getType() == LineEvent.Type.STOP){
							button.setText("play");
							bTC.remove(button);
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

	private static File getFile(String filePath) {
		CompoundDataSource dataSource = GameDataFileSystem.getDefault();

		if (dataSource.has(filePath)) {
			return dataSource.getFile(filePath);
		} else {
			System.out.println("could not find \"" + filePath + "\"");
		}
		return null;
	}
}
