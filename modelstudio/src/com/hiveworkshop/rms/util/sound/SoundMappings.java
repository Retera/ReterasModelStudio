package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class SoundMappings {
	private final Map<String, List<String>> nameToTags = new HashMap<>();
	private final Map<String, Sound> tagToSoundMap = new HashMap<>();

	public SoundMappings() {
		updateSoundMappings();
	}

	public void updateSoundMappings() {
		nameToTags.clear();
		tagToSoundMap.clear();
		readAnimLookups();
		readAnimSounds();
		System.out.println("SoundMap created! " + nameToTags.size() + " names, " + tagToSoundMap.size() + " tags");
	}

	private void readAnimLookups() {
		String animLookUpsPath = "war3.w3mod\\ui\\soundinfo\\animlookups.slk";
		CompoundDataSource source = GameDataFileSystem.getDefault();

		if (source.has(animLookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(animLookUpsPath)))) {
				r.lines().forEach(this::processMappingLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readAnimSounds() {
		String animSoundsPath = "war3.w3mod\\ui\\soundinfo\\animsounds.slk";
		CompoundDataSource source = GameDataFileSystem.getDefault();

		try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(animSoundsPath)))) {
			Set<String> tags = new HashSet<>();
			r.lines().forEach(l -> {
				if (l.startsWith("C;X1;Y")) {
					tags.clear();
					String name = l.split("\"")[1];
					if (nameToTags.containsKey(name)) {
						tags.addAll(nameToTags.get(name));
					}
				} else {
					for (String tag : tags) {
						if (tagToSoundMap.containsKey(tag)) {
							tagToSoundMap.get(tag).setFromSklLine(l);
						}
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Sound getSound(String eventCode) {
//		System.out.println("has \"" + eventCode + "\" (\"" + eventCode.substring(4) + "\") " + tagToSoundMap.containsKey(eventCode.substring(4)));
		return tagToSoundMap.get(eventCode.substring(4));
	}

	String[] tag_name_inBeta = new String[3];

	private void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y")) {
			String[] strings = s.split("\"");
			if (strings.length > 1) {
				tag_name_inBeta[0] = strings[1];
			}

		} else if (s.startsWith("C;X2;K")) {
			String[] strings = s.split("\"");
			if (strings.length > 1) {
				tag_name_inBeta[1] = strings[1];
			}
		} else if (s.startsWith("C;X3;K0") || s.startsWith("C;X3;K1")) {
			String[] strings = s.split(";K");
			if (!strings[1].contains("\"")) {
				tag_name_inBeta[2] = strings[1];
//				System.out.println(Arrays.toString(tag_name_inBeta));
				Sound sound = tagToSoundMap.computeIfAbsent(tag_name_inBeta[0], k -> new Sound(tag_name_inBeta));
				nameToTags.computeIfAbsent(sound.getSoundName(), k -> new ArrayList<>()).add(sound.getTag());
			}
		}
	}

	private File getFile(String filePath) {
		String local = "enus.w3mod";
		CompoundDataSource dataSource = GameDataFileSystem.getDefault();
		File file = null;

		String filepath = "war3.w3mod\\" + filePath.toLowerCase(Locale.ROOT);
		String filepathLoc = "war3.w3mod\\_locales\\" + local + "\\" + filePath.toLowerCase(Locale.ROOT);

		if (dataSource.has(filepath)) {
			System.out.println("default sound");
			file = dataSource.getFile(filepath);
		} else if (dataSource.has(filepathLoc)) {
			System.out.println("local sound");
			file = dataSource.getFile(filepathLoc);
		}
		if (file == null) {
			System.out.println("could not find \"" + filePath + "\"");
		}
		return file;
	}

}
