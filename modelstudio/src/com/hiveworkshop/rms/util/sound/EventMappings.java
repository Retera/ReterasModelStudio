package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

public class EventMappings {
	private SplatMappings splatMappings;
	private UberSplatMappings uberSplatMappings;
	private SpawnMappings spawnMappings;
	private SoundMappings soundMappings;

	public EventMappings (SoundMappings soundMappings) {
		this.soundMappings = soundMappings;
	}

	public EventTarget getEvent(String eventCode) {
		String typeCode = eventCode.substring(0, 3);
		return switch (typeCode){
			case "SND" -> getSoundMappings().getEvent(eventCode);
			case "SPN" -> getSpawnMappings().getEvent(eventCode);
			case "UBR" -> getUberSplatMappings().getEvent(eventCode);
			case "SPL", "FPT" -> getSplatMappings().getEvent(eventCode);
			default -> {
				EventTarget obj = getSoundMappings().getEvent(eventCode);
				if(obj != null) yield obj;
				obj = getSpawnMappings().getEvent(eventCode);
				if(obj != null) yield obj;
				obj = getUberSplatMappings().getEvent(eventCode);
				if(obj != null) yield obj;
				yield getSplatMappings().getEvent(eventCode);
			}
		};
	}

	public SplatMappings getSplatMappings() {
		if (splatMappings == null) {
			splatMappings = new SplatMappings();
		}
		return splatMappings;
	}

	public UberSplatMappings getUberSplatMappings() {
		if (uberSplatMappings == null) {
			uberSplatMappings = new UberSplatMappings();
		}
		return uberSplatMappings;
	}

	public SpawnMappings getSpawnMappings() {
		if (spawnMappings == null) {
			spawnMappings = new SpawnMappings();
		}
		return spawnMappings;
	}

	public SoundMappings getSoundMappings() {
		if (soundMappings == null){
			soundMappings = ProgramGlobals.getSoundMappings();
		}
		return soundMappings;
	}


}
