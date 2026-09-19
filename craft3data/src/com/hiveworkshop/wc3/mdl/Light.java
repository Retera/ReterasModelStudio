package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.LightChunk;
import com.hiveworkshop.wc3.util.ModelUtils;

/**
 * Write a description of class Light here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Light extends IdObject implements VisibilitySource {
	float AttenuationStart = -1;
	float AttenuationEnd = -1;
	double Intensity = -1;
	Vertex staticColor;
	double AmbIntensity = -1;
	double ShadowIntensity = -1;
	// MDX 1800 (Forsaken Kingdom) light properties; defaults match stock 3.0 data
	double shadowCastingStart = 0;
	double shadowCastingEnd = 0;
	double quadraticFalloff = 0.0005;
	double linearFalloff = 0;
	double damping = 0.00001;
	Vertex staticAmbColor;
	ArrayList<AnimFlag> animFlags = new ArrayList<>();
	ArrayList<String> flags = new ArrayList<>();

	private Light() {

	}

	public Light(final String name) {
		this.name = name;
	}

	public Light(final LightChunk.Light light) {
		this(light.node.name);
		// debug print:
		// System.out.println(mdlBone.getName() + ": " +
		// Integer.toBinaryString(bone.node.flags));
		if ((light.node.flags & 512) != 512) {
			System.err.println("MDX -> MDL error: A light '" + light.node.name + "' not flagged as light in MDX!");
		}
		// ----- Convert Base NODE to "IDOBJECT" -----
		loadFrom(light.node);
		// ----- End Base NODE to "IDOBJECT" -----
		// System.out.println(mdlLight.getName() + ": " +
		// Integer.toBinaryString(light.type));
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
		if (light.lightAttenuationStart != null) {
			add(new AnimFlag(light.lightAttenuationStart));
		}
		else {
			setAttenuationStart(light.attenuationStart);
		}
		if (light.lightAttenuationEnd != null) {
			add(new AnimFlag(light.lightAttenuationEnd));
		}
		else {
			setAttenuationEnd(light.attenuationEnd);
		}
		if (light.lightVisibility != null) {
			add(new AnimFlag(light.lightVisibility));
		}
		if (light.lightColor != null) {
			add(new AnimFlag(light.lightColor));
		}
		else {
			setStaticColor(new Vertex(light.color, true));
		}
		if (light.lightIntensity != null) {
			add(new AnimFlag(light.lightIntensity));
		}
		else {
			setIntensity(light.intensity);
		}
		if (light.lightAmbientColor != null) {
			add(new AnimFlag(light.lightAmbientColor));
		}
		else {
			setStaticAmbColor(new Vertex(light.ambientColor, true));
		}
		if (light.lightAmbientIntensity != null) {
			add(new AnimFlag(light.lightAmbientIntensity));
		}
		else {
			setAmbIntensity(light.ambientIntensity);
		}
		// TODO: can shadow intensity be animated
		setShadowIntensity(light.shadowIntensity);
		if (light.shadowCasting != 0) {
			add("ShadowCasting");
		}
		shadowCastingStart = light.shadowCastingStart;
		shadowCastingEnd = light.shadowCastingEnd;
		quadraticFalloff = light.quadraticFalloff;
		linearFalloff = light.linearFalloff;
		damping = light.damping;
		if (light.lightShadowCastingStart != null) {
			add(new AnimFlag(light.lightShadowCastingStart));
		}
		if (light.lightShadowCastingEnd != null) {
			add(new AnimFlag(light.lightShadowCastingEnd));
		}
		if (light.lightQuadraticFalloff != null) {
			add(new AnimFlag(light.lightQuadraticFalloff));
		}
		if (light.lightLinearFalloff != null) {
			add(new AnimFlag(light.lightLinearFalloff));
		}
		if (light.lightDamping != null) {
			add(new AnimFlag(light.lightDamping));
		}
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
		x.ShadowIntensity = ShadowIntensity;
		x.shadowCastingStart = shadowCastingStart;
		x.shadowCastingEnd = shadowCastingEnd;
		x.quadraticFalloff = quadraticFalloff;
		x.linearFalloff = linearFalloff;
		x.damping = damping;
		x.staticAmbColor = staticAmbColor;
		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		flags = new ArrayList<>(x.flags);
		return x;
	}

	public static Light read(final BufferedReader mdl, final EditableModel mdlr) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Light")) {
			final Light lit = new Light();
			lit.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				if (line.contains("ObjectId")) {
					lit.objectId = MDLReader.readInt(line);
				}
				else if (line.contains("Parent")) {
					lit.parentId = MDLReader.splitToInts(line)[0];
					// lit.parent = mdlr.getIdObject(lit.parentId);
				}
				else if (!line.contains("static") && line.contains("{") && !line.contains("DontInherit")) {
					MDLReader.reset(mdl);
					lit.animFlags.add(AnimFlag.read(mdl));
				}
				else if (line.contains("AttenuationStart"))// These are
															// 'static'
															// ones, the
															// rest are
				{ // saved in animFlags
					lit.AttenuationStart = MDLReader.readInt(line);
				}
				else if (line.contains("AttenuationEnd")) {
					lit.AttenuationEnd = MDLReader.readInt(line);
				}
				else if (line.contains("AmbIntensity")) {
					lit.AmbIntensity = MDLReader.readDouble(line);
				}
				else if (line.contains("ShadowCastingStart")) {
					lit.shadowCastingStart = MDLReader.readDouble(line);
				}
				else if (line.contains("ShadowCastingEnd")) {
					lit.shadowCastingEnd = MDLReader.readDouble(line);
				}
				else if (line.contains("QuadraticFalloff")) {
					lit.quadraticFalloff = MDLReader.readDouble(line);
				}
				else if (line.contains("LinearFalloff")) {
					lit.linearFalloff = MDLReader.readDouble(line);
				}
				else if (line.contains("Damping")) {
					lit.damping = MDLReader.readDouble(line);
				}
				else if (line.contains("ShadowIntensity")) {
					// we allow parsing this even if it's a corrupted model with a ShadowIntensity
					// line despite not having version 1200 header, so that ShadowIntensity can't be
					// mistakenly read by the ".contains(Intensity)" check below.
					lit.ShadowIntensity = MDLReader.readDouble(line);
				}
				else if (line.contains("AmbColor")) {
					lit.staticAmbColor = Vertex.parseText(line);
				}
				else if (line.contains("Intensity")) {
					lit.Intensity = MDLReader.readDouble(line);
				}
				else if (line.contains("Color")) {
					lit.staticColor = Vertex.parseText(line);
				}
				else// Flags like Omnidirectional
				{
					lit.flags.add(MDLReader.readFlag(line));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return lit;
		}
		else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse Light: Missing or unrecognized open statement.");
		}
		return null;
	}

	@Override
	public void printTo(final PrintWriter writer, final int version) {
		// Remember to update the ids of things before using this
		// -- uses objectId value of idObject superclass
		// -- uses parentId value of idObject superclass
		// -- uses the parent (java Object reference) of idObject superclass
		final ArrayList<AnimFlag> pAnimFlags = new ArrayList<>(this.animFlags);
		writer.println(MDLReader.getClassName(this.getClass()) + " \"" + getName() + "\" {");
		if (objectId != -1) {
			writer.println("\tObjectId " + objectId + ",");
		}
		if (parentId != -1) {
			writer.println("\tParent " + parentId + ",\t// \"" + getParent().getName() + "\"");
		}
		// for( int i = 0; i < flags.size(); i++ )
		// {
		// writer.println("\t"+flags.get(i)+",");
		// //Stuff like omnidirectional
		// }
		for (final String s : flags) {
			if ("ShadowCasting".equals(s)) {
				continue; // written with the other MDX 1300 shadow keywords below
			}
			writer.println("\t" + s + ",");
			// Stuff like omnidirectional
		}

		// keyword order of the game's MDL writer; each group is gated on the MDX version that introduced it
		printStaticOrTrack(writer, pAnimFlags, "AttenuationStart", AttenuationStart != -1,
				Float.toString(AttenuationStart));
		printStaticOrTrack(writer, pAnimFlags, "AttenuationEnd", AttenuationEnd != -1, Float.toString(AttenuationEnd));
		printStaticOrTrack(writer, pAnimFlags, "Color", staticColor != null,
				staticColor == null ? null : staticColor.toString());
		printStaticOrTrack(writer, pAnimFlags, "Intensity", Intensity != -1, Double.toString(Intensity));
		printStaticOrTrack(writer, pAnimFlags, "AmbColor", staticAmbColor != null,
				staticAmbColor == null ? null : staticAmbColor.toString());
		printStaticOrTrack(writer, pAnimFlags, "AmbIntensity", AmbIntensity != -1, Double.toString(AmbIntensity));
		if (ModelUtils.isLightShadowIntensitySupported(version)) {
			printStaticOrTrack(writer, pAnimFlags, "ShadowIntensity", ShadowIntensity != -1,
					Double.toString(ShadowIntensity));
		}
		if (ModelUtils.isLightShadowCastingSupported(version)) {
			if (flags.contains("ShadowCasting")) {
				writer.println("\tShadowCasting,");
			}
			printTrackOrStatic(writer, pAnimFlags, "ShadowCastingStart", shadowCastingStart);
			printTrackOrStatic(writer, pAnimFlags, "ShadowCastingEnd", shadowCastingEnd);
		}
		if (ModelUtils.isLightFalloffSupported(version)) {
			printTrackOrStatic(writer, pAnimFlags, "QuadraticFalloff", quadraticFalloff);
			printTrackOrStatic(writer, pAnimFlags, "LinearFalloff", linearFalloff);
			printTrackOrStatic(writer, pAnimFlags, "Damping", damping);
		}
		for (final String gated : new String[] { "ShadowIntensity", "ShadowCastingStart", "ShadowCastingEnd",
				"QuadraticFalloff", "LinearFalloff", "Damping" }) {
			// a track the target version cannot carry is dropped rather than written as an unknown keyword
			pAnimFlags.removeIf(flag -> flag.getName().equals(gated));
		}
		for (int i = 0; i < pAnimFlags.size(); i++) {
			pAnimFlags.get(i).printTo(writer, 1);
			// This will probably just be visibility
		}
		writer.println("}");
	}

	/** Writes the static value when it is set, else the track of that name (and removes it from the list). */
	private static void printStaticOrTrack(final PrintWriter writer, final ArrayList<AnimFlag> pAnimFlags,
			final String name, final boolean hasStatic, final String staticText) {
		if (hasStatic) {
			writer.println("\tstatic " + name + " " + staticText + ",");
			return;
		}
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(name)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
				return;
			}
		}
	}

	/** Writes the track of that name when there is one, else the static value; these keywords are always written. */
	private static void printTrackOrStatic(final PrintWriter writer, final ArrayList<AnimFlag> pAnimFlags,
			final String name, final double staticValue) {
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(name) && (pAnimFlags.get(i).size() > 0)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
				return;
			}
		}
		writer.println("\tstatic " + name + " " + MDLReader.doubleToString(staticValue) + ",");
	}

	// VisibilitySource methods
	@Override
	public void setVisibilityFlag(final AnimFlag flag) {
		int count = 0;
		int index = 0;
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag af = animFlags.get(i);
			if (af.getName().equals("Visibility") || af.getName().equals("Alpha")) {
				count++;
				index = i;
				animFlags.remove(af);
			}
		}
		if (flag != null) {
			animFlags.add(index, flag);
		}
		if (count > 1) {
			JOptionPane.showMessageDialog(null,
					"Some visiblity animation data was lost unexpectedly during overwrite in " + getName() + ".");
		}
	}

	@Override
	public AnimFlag getVisibilityFlag() {
		int count = 0;
		AnimFlag output = null;
		for (final AnimFlag af : animFlags) {
			if (af.getName().equals("Visibility") || af.getName().equals("Alpha")) {
				count++;
				output = af;
			}
		}
		if (count > 1) {
			JOptionPane.showMessageDialog(null,
					"Some visiblity animation data was lost unexpectedly during retrieval in " + getName() + ".");
		}
		return output;
	}

	public String getVisTagname() {
		return "light";// geoset.getName();
	}

	@Override
	public String visFlagName() {
		return "Visibility";
	}

	@Override
	public void flipOver(final byte axis) {
		final String currentFlag = "Rotation";
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag flag = animFlags.get(i);
			flag.flipOver(axis);
		}
	}

	public float getAttenuationStart() {
		return AttenuationStart;
	}

	public boolean isShadowCasting() {
		return flags.contains("ShadowCasting");
	}

	public void setShadowCasting(final boolean shadowCasting) {
		flags.remove("ShadowCasting");
		if (shadowCasting) {
			flags.add("ShadowCasting");
		}
	}

	public double getShadowCastingStart() {
		return shadowCastingStart;
	}

	public void setShadowCastingStart(final double shadowCastingStart) {
		this.shadowCastingStart = shadowCastingStart;
	}

	public double getShadowCastingEnd() {
		return shadowCastingEnd;
	}

	public void setShadowCastingEnd(final double shadowCastingEnd) {
		this.shadowCastingEnd = shadowCastingEnd;
	}

	public double getQuadraticFalloff() {
		return quadraticFalloff;
	}

	public void setQuadraticFalloff(final double quadraticFalloff) {
		this.quadraticFalloff = quadraticFalloff;
	}

	public double getLinearFalloff() {
		return linearFalloff;
	}

	public void setLinearFalloff(final double linearFalloff) {
		this.linearFalloff = linearFalloff;
	}

	public double getDamping() {
		return damping;
	}

	public void setDamping(final double damping) {
		this.damping = damping;
	}

	public void setAttenuationStart(final float attenuationStart) {
		AttenuationStart = attenuationStart;
	}

	public float getAttenuationEnd() {
		return AttenuationEnd;
	}

	public void setAttenuationEnd(final float attenuationEnd) {
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

	public double getShadowIntensity() {
		return ShadowIntensity;
	}

	public void setShadowIntensity(final double ambIntensity) {
		ShadowIntensity = ambIntensity;
	}

	public Vertex getStaticAmbColor() {
		return staticAmbColor;
	}

	public void setStaticAmbColor(final Vertex staticAmbColor) {
		this.staticAmbColor = staticAmbColor;
	}

	@Override
	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	public void setAnimFlags(final ArrayList<AnimFlag> animFlags) {
		this.animFlags = animFlags;
	}

	@Override
	public ArrayList<String> getFlags() {
		return flags;
	}

	public void setFlags(final ArrayList<String> flags) {
		this.flags = flags;
	}

	@Override
	public void add(final String flag) {
		flags.add(flag);
	}

	@Override
	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.light(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag visibilityFlag = getVisibilityFlag();
		if (visibilityFlag != null) {
			final Number visibility = (Number) visibilityFlag.interpolateAt(animatedRenderEnvironment);
			return visibility.floatValue();
		}
		return 1;
	}

	@Override
	public Vertex getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "Translation");
		if (translationFlag != null) {
			return (Vertex) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return null;
	}

	@Override
	public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "Rotation");
		if (translationFlag != null) {
			return (QuaternionRotation) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return null;
	}

	@Override
	public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "Scaling");
		if (translationFlag != null) {
			return (Vertex) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return null;
	}
}
