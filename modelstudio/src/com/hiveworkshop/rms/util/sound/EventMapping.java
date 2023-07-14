package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class EventMapping<E extends EventTarget> {
	protected final Map<String, E> tagToEvent = new HashMap<>();


	public E getEvent(String eventCode){
		return tagToEvent.get(eventCode.substring(4));
	}

	public Collection<E> getEvents(){
		return tagToEvent.values();
	}

	protected abstract void updateMappings();
	protected abstract void processMappingLine(String s);

	protected void readLookups(String lookUpsPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();

		if (source.has(lookUpsPath)) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(lookUpsPath)))) {
				r.lines().forEach(this::processMappingLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
