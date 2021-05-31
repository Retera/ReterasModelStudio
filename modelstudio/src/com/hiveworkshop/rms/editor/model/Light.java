package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxLight.Type;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

/**
 * Write a description of class Light here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Light extends IdObject {
	Type type = Type.OMNIDIRECTIONAL;
	float attenuationStart = 0;
	float attenuationEnd = 0;
	double intensity = 0;
	Vec3 staticColor = new Vec3();
	double ambIntensity = 0;
	Vec3 staticAmbColor = new Vec3();

	public Light() {

	}

	public Light(final String name) {
		this.name = name;
	}

	public Light(final Light light) {
		copyObject(light);

		type = light.type;
		attenuationStart = light.attenuationStart;
		attenuationEnd = light.attenuationEnd;
		intensity = light.intensity;
		staticColor = light.staticColor;
		ambIntensity = light.ambIntensity;
		staticAmbColor = light.staticAmbColor;
	}

	@Override
	public Light copy() {
		return new Light(this);
	}

	public String getVisTagname() {
		return "light";// geoset.getName();
	}

	public Type getType() {
		return type;
	}

	public Light setType(Type type) {
		this.type = type;
		return this;
	}

	public float getAttenuationStart() {
		return attenuationStart;
	}

	public void setAttenuationStart(final float attenuationStart) {
		this.attenuationStart = attenuationStart;
	}

	public float getAttenuationEnd() {
		return attenuationEnd;
	}

	public void setAttenuationEnd(final float attenuationEnd) {
		this.attenuationEnd = attenuationEnd;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(final double intensity) {
		this.intensity = intensity;
	}

	public Vec3 getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vec3 staticColor) {
		this.staticColor = staticColor;
	}

	public double getAmbIntensity() {
		return ambIntensity;
	}

	public void setAmbIntensity(final double ambIntensity) {
		this.ambIntensity = ambIntensity;
	}

	public Vec3 getStaticAmbColor() {
		return staticAmbColor;
	}

	public void setStaticAmbColor(final Vec3 staticAmbColor) {
		this.staticAmbColor = staticAmbColor;
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
	}
}
