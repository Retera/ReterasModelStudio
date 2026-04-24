package com.hiveworkshop.wc3.mdl.v2.timelines;
import hiveworkshop.localizationmanager.localizationmanager;

public enum InterpolationType {
	DONT_INTERP(LocalizationManager.getInstance().get("string.interpolationyype_interpolationyype_dontinterp")),
	LINEAR(LocalizationManager.getInstance().get("string.interpolationyype_interpolationyype_linear")),
	BEZIER(LocalizationManager.getInstance().get("string.interpolationyype_interpolationyype_bezier")),
	HERMITE(LocalizationManager.getInstance().get("string.interpolationyype_interpolationyype_hermite"));

	private String name;

	private InterpolationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
