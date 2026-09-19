package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.Arrays;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Light;

public class ComponentLightCard extends ComponentNodeCard<Light> {
	private static final List<String> TYPES = Arrays.asList("Omnidirectional", "Directional", "Ambient");

	@Override
	protected void addTypeRows() {
		beginSection("Light");
		addComboBox("Type", () -> TYPES, type -> type, light -> {
			for (final String type : TYPES) {
				if (light.getFlags().contains(type)) {
					return type;
				}
			}
			return TYPES.get(0);
		}, (light, type) -> {
			light.getFlags().removeAll(TYPES);
			if (type != null) {
				light.getFlags().add(type);
			}
		});
		endSection();
		addFloatValue("Attenuation Start", light -> (double) light.getAttenuationStart(),
				(light, value) -> light.setAttenuationStart(value.floatValue()), light -> light, Light::getAnimFlags,
				"AttenuationStart");
		addFloatValue("Attenuation End", light -> (double) light.getAttenuationEnd(),
				(light, value) -> light.setAttenuationEnd(value.floatValue()), light -> light, Light::getAnimFlags,
				"AttenuationEnd");
		addFloatValue("Intensity", Light::getIntensity, Light::setIntensity, light -> light, Light::getAnimFlags,
				"Intensity");
		addColorValue("Color", Light::getStaticColor, Light::setStaticColor, light -> light, Light::getAnimFlags,
				"Color");
		addFloatValue("Ambient Intensity", Light::getAmbIntensity, Light::setAmbIntensity, light -> light,
				Light::getAnimFlags, "AmbIntensity");
		addColorValue("Ambient Color", Light::getStaticAmbColor, Light::setStaticAmbColor, light -> light,
				Light::getAnimFlags, "AmbColor");
	}
}
