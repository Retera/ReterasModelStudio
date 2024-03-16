package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataOutputStream;

public class GetMeDatas16 {
	static boolean once = true;

	public static void main(String[] args) {
		try {
			Warcraft3MapObjectData data = Warcraft3MapObjectData.load(true);
			MutableObjectData abilities = data.getAbilities();
			War3ObjectDataChangeset editorData = abilities.getEditorData();
			System.out.println(editorData);
			System.out.println(editorData.getOriginal().size());
			System.out.println(editorData.getCustom().size());
			War3ID channel = War3ID.fromString("ANcl");
			for (War3ID abilId : abilities.keySet()) {
				if (!channel.equals(abilId)) {
					List<AbilityCall> abilityCalls = new ArrayList<>();
					MutableGameObject old = abilities.get(abilId);
					War3ID code = old.getCode();
					ObjectData sourceSLKMetaData = abilities.getSourceSLKMetaData();
					for (String metaKey : sourceSLKMetaData.keySet()) {
						if (metaKey.length() == 4) {
							War3ID metaKeyID = War3ID.fromString(metaKey);
							GameObject metaElement = sourceSLKMetaData.get(metaKey);
							int repeat = metaElement.getFieldValue("repeat");
							if (repeat == 0) {
								handleLevel(abilityCalls, old, code, metaKey, metaKeyID, metaElement, 0);
							}
							else {
								for (int i = 0; i < repeat; i++) {
									handleLevel(abilityCalls, old, code, metaKey, metaKeyID, metaElement, i);
								}
							}
						}
					}
					MutableGameObject newAbility = abilities.createNew(abilId, channel);
					for (AbilityCall call : abilityCalls) {
						try {
							call.call(newAbility);
						}
						catch (Exception exc) {
							exc.printStackTrace();
							try {
								Thread.sleep(1000);
							}
							catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			try (BlizzardDataOutputStream str = new BlizzardDataOutputStream(new File("C:/Temp/bigchannel.w3a"))) {
				editorData.save(str, false);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleLevel(List<AbilityCall> abilityCalls, MutableGameObject old, War3ID code, String metaKey,
			War3ID metaKeyID, GameObject metaElement, int i) {
		String useSpecific = metaElement.getField("useSpecific");
		boolean use = true;
		if (useSpecific.length() > 3) {
			use = false;
			String[] useSpecifics = useSpecific.split(",");
			for (String specs : useSpecifics) {
				if (code.toString().equals(specs)) {
//											use = true;
				}
			}
		}
		if (use) {
			String type = metaElement.getField("type");
			final int idx = i;
			if (false && ("int".equals(type) || "uint".equals(type))) {
				int value = old.getFieldAsInteger(metaKeyID, i);
				abilityCalls.add(new AbilityCall() {
					@Override
					public void call(MutableGameObject newAbility) {
						newAbility.setField(metaKeyID, idx, value);
					}
				});
			}
			else if ("bool".equals(type)) {
				if ("aher".equals(metaKey) || "aite".equals(metaKey)) {
					if ((idx == 0)) {
						boolean value = old.getFieldAsBoolean(metaKeyID, i);
						if (!value) {
							if (once || true) {
								once = false;
								abilityCalls.add(new AbilityCall() {
									@Override
									public void call(MutableGameObject newAbility) {
										newAbility.setField(metaKeyID, idx, value);
									}
								});
							}
						}
					}
				}
			}
			else if ("string".equals(type) || "unitRace".equals(type) || "icon".equals(type)) {
				String value = old.getFieldAsString(metaKeyID, i);
				abilityCalls.add(new AbilityCall() {
					@Override
					public void call(MutableGameObject newAbility) {
						if ("anam".equals(metaKey)) {
							System.out.println("name");
						}
						newAbility.setField(metaKeyID, idx, value);
					}
				});
			}
			else if (false && ("unreal".equals(type) || "real".equals(type))) {
				float value = old.getFieldAsFloat(metaKeyID, i);
				abilityCalls.add(new AbilityCall() {
					@Override
					public void call(MutableGameObject newAbility) {
						newAbility.setField(metaKeyID, idx, value);
					}
				});
			}
			else {
				System.err.println(type);
			}
		}
	}

	private static interface AbilityCall {
		void call(MutableGameObject newAbility);
	}
}
