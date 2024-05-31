package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class SoundMappings extends EventMapping<Sound> {
	private final Map<String, List<String>> nameToTags = new HashMap<>();
	private final Map<String, String> fieldToKey = new HashMap<>();
	public SoundMappings() {
		updateMappings();
	}

	public void updateMappings() {
		nameToTags.clear();
		tagToEvent.clear();
		readAnimLookups("war3.w3mod\\ui\\soundinfo\\animlookups.slk");
		if (!tagToEvent.isEmpty()) {
//			System.out.println("filling sound data!");
			fillSoundData("war3.w3mod\\ui\\soundinfo\\animsounds.slk");
		} else {
//			System.out.println("generating sound data!");
			readAnimLookups2("war3.w3mod\\ui\\soundinfo\\animsounds.slk");
		}
		System.out.println("SoundMap created! " + nameToTags.size() + " names, " + tagToEvent.size() + " tags");
	}

	protected void readAnimLookups(String lookUpsPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();
		fieldToKey.clear();

		if (source.has(lookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(lookUpsPath)))) {
				r.lines().forEach(this::processMappingLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	Sound currSound;
	protected void processMappingLine(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1;")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				currSound = tagToEvent.computeIfAbsent(strings[1], k -> new Sound(strings[1]));
			} else {
				currSound = null;
			}
		} else if (currSound != null) {
			currSound.setFromSklLine(s, fieldToKey);
		} else if (s.startsWith("C;X")) {
			String[] field = s.split(";");
			String[] strings = s.split("\"");
			if (1 < field.length && 1 < strings.length && !fieldToKey.containsKey(field[1])) {
				fieldToKey.put(field[1], strings[1]);
			}
		}
	}
	protected void readAnimLookups2(String lookUpsPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();
		fieldToKey.clear();

		if (source.has(lookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(lookUpsPath)))) {
				r.lines().forEach(this::processMappingLine2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	protected void processMappingLine2(String s) {
		if (s.startsWith("C;X1;Y") && !s.startsWith("C;X1;Y1;")) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
				if (currSound != null) {
					System.out.println(currSound.getTag() + " \"" + currSound.getName() + "\"  \"" + Arrays.toString(currSound.getFilePaths()) + "\"");
				}
				currSound = new Sound(strings[1]);
//				currSound.setFromNewAnimSounds(s);
				currSound.setFromSklLine(s, fieldToKey);
			} else {
				currSound = null;
			}
		} else if (s.startsWith("C;X2") && currSound != null) {
			String[] strings = s.split("\"");
			if (1 < strings.length) {
//				currSound.setFromNewAnimSounds(s);
				currSound.setFromSklLine(s, fieldToKey);
				tagToEvent.put(currSound.getTag(), currSound);
			} else {
				currSound = null;
			}
		} else if (currSound != null) {
//			currSound.setFromNewAnimSounds(s);
			currSound.setFromSklLine(s, fieldToKey);
		} else if (s.startsWith("C;X")) {
			String[] field = s.split(";");
			String[] strings = s.split("\"");
			if (1 < field.length && 1 < strings.length && !fieldToKey.containsKey(field[1])) {
				fieldToKey.put(field[1], strings[1]);
			}
		}
	}

	private void fillSoundData(String animSoundsPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();
		fieldToKey.clear();
//		System.out.println("filling from \"" + animSoundsPath + "\"");
		try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(animSoundsPath)))) {
			Map<String, List<String>> nameToTags = new HashMap<>();
			tagToEvent.forEach((t, e) -> nameToTags.computeIfAbsent(e.getName(), k -> new ArrayList<>()).add(t));
			Set<String> tags = new HashSet<>();
			r.lines().forEach(l -> {
				if (l.startsWith("C;X1;Y")) {
					tags.clear();
					String name = l.split("\"")[1];
					if (nameToTags.containsKey(name)) {
						tags.addAll(nameToTags.get(name));
					} else {
						String[] field = l.split(";");
						String[] strings = l.split("\"");
						if (1 < field.length && 1 < strings.length && !fieldToKey.containsKey(field[1])) {
							fieldToKey.put(field[1], strings[1]);
						}
					}
				} else {
					if (tags.isEmpty()) {
						String[] field = l.split(";");
						String[] strings = l.split("\"");
						if (1 < field.length && 1 < strings.length && !fieldToKey.containsKey(field[1])) {
							fieldToKey.put(field[1], strings[1]);
						}
					} else {
						for (String tag : tags) {
							if (tagToEvent.containsKey(tag)) {
								tagToEvent.get(tag).setFromSklLine(l, fieldToKey);
//							    tagToEvent.get(tag).setFromOldAnimSounds(l);
							}
						}
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
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
