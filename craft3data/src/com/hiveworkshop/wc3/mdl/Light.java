package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.etheller.warsmash.parsers.mdlx.MdlxLight;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * Write a description of class Light here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Light extends IdObject {
	int AttenuationStart = -1;
	int AttenuationEnd = -1;
	double Intensity = -1;
	Vertex staticColor;
	double AmbIntensity = -1;
	Vertex staticAmbColor;

	private Light() {

	}

	public Light(final String name) {
		this.name = name;
	}

	public Light(final MdlxLight light) {
		if ((light.flags & 512) != 512) {
			System.err.println("MDX -> MDL error: A light '" + light.name + "' not flagged as light in MDX!");
		}

		loadObject(light);

		switch (light.type) {
		case 0:
			add("Omnidirectional");
			break;
		case 1:
			add("Directional");
			break;
		case 2:
			add("Ambient"); // I'm not 100% that Ambient is supposed to be a
							// possible flag type
			break; // --- Is it for Ambient only? All lights have the Amb values
		default:
			add("Omnidirectional");
			break;
		}

		setAttenuationStart((int)light.attenuation[0]);
		setAttenuationEnd((int)light.attenuation[1]);
		setStaticColor(new Vertex(light.color, true));
		setIntensity(light.intensity);
		setStaticAmbColor(new Vertex(light.ambientColor, true));
		setAmbIntensity(light.ambientIntensity);
	}

	public MdlxLight toMdlx() {
		MdlxLight light = new MdlxLight();

		objectToMdlx(light);

		for (final String flag : getFlags()) {
			if (flag.equals("Omnidirectional")) {
				light.type = 0;
			} else if (flag.equals("Directional")) {
				light.type = 1;
			} else if (flag.equals("Ambient")) {
				light.type = 2;
			}
		}

		light.attenuation[0] = getAttenuationStart();
		light.attenuation[1] = getAttenuationEnd();
		light.color = MdlxUtils.flipRGBtoBGR(getStaticColor().toFloatArray());
		light.intensity = (float)getIntensity();
		light.ambientColor = MdlxUtils.flipRGBtoBGR(getStaticAmbColor().toFloatArray());
		light.ambientIntensity = (float)getAmbIntensity();
		
		return light;
	}

	@Override
	public IdObject copy() {
		final Light x = new Light();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.AttenuationStart = AttenuationStart;
		x.AttenuationEnd = AttenuationEnd;
		x.Intensity = Intensity;
		x.staticColor = staticColor;
		x.AmbIntensity = AmbIntensity;
		x.staticAmbColor = staticAmbColor;
		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		flags = new ArrayList<>(x.flags);
		return x;
	}

	public String getVisTagname() {
		return "light";// geoset.getName();
	}

	public int getAttenuationStart() {
		return AttenuationStart;
	}

	public void setAttenuationStart(final int attenuationStart) {
		AttenuationStart = attenuationStart;
	}

	public int getAttenuationEnd() {
		return AttenuationEnd;
	}

	public void setAttenuationEnd(final int attenuationEnd) {
		AttenuationEnd = attenuationEnd;
	}

	public double getIntensity() {
		return Intensity;
	}

	public void setIntensity(final double intensity) {
		Intensity = intensity;
	}

	public Vertex getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vertex staticColor) {
		this.staticColor = staticColor;
	}

	public double getAmbIntensity() {
		return AmbIntensity;
	}

	public void setAmbIntensity(final double ambIntensity) {
		AmbIntensity = ambIntensity;
	}

	public Vertex getStaticAmbColor() {
		return staticAmbColor;
	}

	public void setStaticAmbColor(final Vertex staticAmbColor) {
		this.staticAmbColor = staticAmbColor;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.light(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}
}
