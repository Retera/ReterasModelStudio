package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.LightChunk;

/**
 * Write a description of class Light here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Light extends IdObject implements VisibilitySource {
	int AttenuationStart = -1;
	int AttenuationEnd = -1;
	double Intensity = -1;
	Vertex staticColor;
	double AmbIntensity = -1;
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
		setAttenuationStart(light.attenuationStart);
		setAttenuationEnd(light.attenuationEnd);
		if (light.lightVisibility != null) {
			add(new AnimFlag(light.lightVisibility));
		}
		if (light.lightColor != null) {
			add(new AnimFlag(light.lightColor));
		} else {
			setStaticColor(new Vertex(light.color));
		}
		if (light.lightIntensity != null) {
			add(new AnimFlag(light.lightIntensity));
		} else {
			setIntensity(light.intensity);
		}
		if (light.lightAmbientColor != null) {
			add(new AnimFlag(light.lightAmbientColor));
		} else {
			setStaticAmbColor(new Vertex(light.ambientColor));
		}
		if (light.lightAmbientIntensity != null) {
			add(new AnimFlag(light.lightAmbientIntensity));
		} else {
			setAmbIntensity(light.ambientIntensity);
		}

	}

	@Override
	public IdObject copy() {
		final Light x = new Light();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.parent = parent;

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

	public static Light read(final BufferedReader mdl) {
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
				} else if (line.contains("Parent")) {
					lit.parentId = MDLReader.splitToInts(line)[0];
					// lit.parent = mdlr.getIdObject(lit.parentId);
				} else if (!line.contains("static") && line.contains("{")) {
					MDLReader.reset(mdl);
					lit.animFlags.add(AnimFlag.read(mdl));
				} else if (line.contains("AttenuationStart"))// These are
																// 'static'
																// ones, the
																// rest are
				{ // saved in animFlags
					lit.AttenuationStart = MDLReader.readInt(line);
				} else if (line.contains("AttenuationEnd")) {
					lit.AttenuationEnd = MDLReader.readInt(line);
				} else if (line.contains("AmbIntensity")) {
					lit.AmbIntensity = MDLReader.readDouble(line);
				} else if (line.contains("AmbColor")) {
					lit.staticAmbColor = Vertex.parseText(line);
				} else if (line.contains("Intensity")) {
					lit.Intensity = MDLReader.readDouble(line);
				} else if (line.contains("Color")) {
					lit.staticColor = Vertex.parseText(line);
				} else// Flags like Omnidirectional
				{
					lit.flags.add(MDLReader.readFlag(line));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return lit;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse Light: Missing or unrecognized open statement.");
		}
		return null;
	}

	@Override
	public void printTo(final PrintWriter writer) {
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
			writer.println("\tParent " + parentId + ",\t// \"" + parent.getName() + "\"");
		}
		// for( int i = 0; i < flags.size(); i++ )
		// {
		// writer.println("\t"+flags.get(i)+",");
		// //Stuff like omnidirectional
		// }
		for (final String s : flags) {
			writer.println("\t" + s + ",");
			// Stuff like omnidirectional
		}

		// AttenuationStart
		String currentFlag = "AttenuationStart";
		if (AttenuationStart != -1) {
			writer.println("\tstatic " + currentFlag + " " + AttenuationStart + ",");
		} else {
			boolean set = false;
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		}
		currentFlag = "AttenuationEnd";
		if (AttenuationEnd != -1) {
			writer.println("\tstatic " + currentFlag + " " + AttenuationEnd + ",");
		} else {
			boolean set = false;
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		}
		currentFlag = "Intensity";
		if (Intensity != -1) {
			writer.println("\tstatic " + currentFlag + " " + Intensity + ",");
		} else {
			boolean set = false;
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		}
		currentFlag = "Color";
		if (staticColor != null) {
			writer.println("\tstatic " + currentFlag + " " + staticColor.toString() + ",");
		} else {
			boolean set = false;
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		}
		currentFlag = "AmbIntensity";
		if (AmbIntensity != -1) {
			writer.println("\tstatic " + currentFlag + " " + AmbIntensity + ",");
		} else {
			boolean set = false;
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		}
		currentFlag = "AmbColor";
		if (staticAmbColor != null) {
			writer.println("\tstatic " + currentFlag + " " + staticAmbColor.toString() + ",");
		} else {
			boolean set = false;
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		}
		for (int i = 0; i < pAnimFlags.size(); i++) {
			pAnimFlags.get(i).printTo(writer, 1);
			// This will probably just be visibility
		}
		writer.println("}");
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
}
