package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.CornChunk;

/**
 * Popcorn FX is what I am calling the CORN chunk, somebody said that's probably
 * what they represent
 */
public class PopcornFxEmitter extends IdObject implements VisibilitySource {
	ArrayList<AnimFlag> animFlags = new ArrayList<>();
	float[] maybeColor;
	String path = null;
	String flagString = null;
	ArrayList<String> flags = new ArrayList<>();

	private PopcornFxEmitter() {

	}

	public PopcornFxEmitter(final String name) {
		this.name = name;
	}

	public PopcornFxEmitter(final CornChunk.PopcornFxEmitter emitter) {
		this(emitter.node.name);
		// debug print:

		// ----- Convert Base NODE to "IDOBJECT" -----
		loadFrom(emitter.node);
		// ----- End Base NODE to "IDOBJECT" -----

		// System.out.println(attachment.node.name + ": " +
		// Integer.toBinaryString(attachment.unknownNull));

		if (emitter.cornVisibility != null) {
			add(new AnimFlag(emitter.cornVisibility));
		}
		if (emitter.cornEmissionRate != null) {
			add(new AnimFlag(emitter.cornEmissionRate));
		}
		if (emitter.cornAlpha != null) {
			add(new AnimFlag(emitter.cornAlpha));
		}

		setMaybeColor(emitter.maybeColor);
		setPath(emitter.path);
		setFlagString(emitter.flags);
		// if( emitter. != null ) {
		// mdlEmitter.add(new AnimFlag(emitter.attachmentVisibility));
		// }

	}

	@Override
	public IdObject copy() {
		final PopcornFxEmitter x = new PopcornFxEmitter();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.path = path;
		x.flagString = flagString;
		x.maybeColor = maybeColor.clone();

		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		return x;
	}

	public static PopcornFxEmitter read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("PopcornFxEmitter")) {
			final PopcornFxEmitter pe = new PopcornFxEmitter();
			pe.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				boolean foundType = false;
				if (line.contains("ObjectId")) {
					pe.objectId = MDLReader.readInt(line);
					foundType = true;
				} else if (line.contains("Parent")) {
					pe.parentId = MDLReader.splitToInts(line)[0];
					foundType = true;
					// pe.parent = mdlr.getIdObject(pe.parentId);
				} else if (line.contains("Path")) {
					pe.path = MDLReader.readName(line);
					foundType = true;
				} else if (line.contains("FlagString")) {
					pe.flagString = MDLReader.readName(line);
					foundType = true;
				} else if (line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation")
						|| line.contains("Scaling") || line.contains("Alpha") || line.contains("EmissionRate")) {
					MDLReader.reset(mdl);
					pe.animFlags.add(AnimFlag.read(mdl));
					foundType = true;
				} else if (line.contains("SegmentColor")) {
					pe.maybeColor = new float[8];
					boolean reading = true;
					foundType = true;
					for (int i = 0; reading && (i < 2); i++) {
						line = MDLReader.nextLine(mdl);
						if (line.contains("Color")) {
							// Reuse my quaternion parser, not actually smart
							final QuaternionRotation quatern = QuaternionRotation.parseText(line);
							pe.maybeColor[i * 4] = (float) quatern.a;
							pe.maybeColor[(i * 4) + 1] = (float) quatern.b;
							pe.maybeColor[(i * 4) + 2] = (float) quatern.c;
							pe.maybeColor[(i * 4) + 3] = (float) quatern.d;
						} else {
							reading = false;
							MDLReader.reset(mdl);
							line = MDLReader.nextLine(mdl);
						}
					}
					line = MDLReader.nextLine(mdl);
				}
				if (!foundType) {
					pe.flags.add(MDLReader.readFlag(line));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return pe;
		} else

		{
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse PopcornFxEmitter: Missing or unrecognized open statement.");
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
			writer.println("\tParent " + parentId + ",\t// \"" + getParent().getName() + "\"");
		}
		String currentFlag = "";
		currentFlag = "Alpha";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		currentFlag = "EmissionRate";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		currentFlag = "Visibility";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		writer.println("\tSegmentColor {");
		final int maybeColors = maybeColor.length / 4;
		for (int i = 0; i < maybeColors; i++) {
			writer.println("\t\tColor " + new QuaternionRotation(maybeColor[(i * 4) + 0], maybeColor[(i * 4) + 1],
					maybeColor[(i * 4) + 2], maybeColor[(i * 4) + 2]).toString() + ",");
		}
		writer.println("\t},");
		if (path != null) {
			writer.println("\tPath \"" + path + "\",");
		}
		if (flagString != null) {
			writer.println("\tFlagString \"" + flagString + "\",");
		}

		for (int i = pAnimFlags.size() - 1; i >= 0; i--) {
			if (pAnimFlags.get(i).getName().equals("Translation")) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		for (int i = pAnimFlags.size() - 1; i >= 0; i--) {
			if (pAnimFlags.get(i).getName().equals("Rotation")) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		for (int i = pAnimFlags.size() - 1; i >= 0; i--) {
			if (pAnimFlags.get(i).getName().equals("Scaling")) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		for (final String s : flags) {
			writer.println("\t" + s + ",");
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
			if (af.getName().equals("Visibility")) {
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
			if (af.getName().equals("Visibility")) {
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

	@Override
	public void add(final String flag) {
		flags.add(flag);
	}

	@Override
	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	public void setMaybeColor(final float[] maybeColor) {
		this.maybeColor = maybeColor;
	}

	public float[] getMaybeColor() {
		return maybeColor;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public void setFlagString(final String flagString) {
		this.flagString = flagString;
	}

	public String getFlagString() {
		return flagString;
	}

	@Override
	public List<String> getFlags() {
		return flags;
	}

	@Override
	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.popcornFxEmitter(this);
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

	public double getRenderEmissionRate(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "EmissionRate");
		if (translationFlag != null) {
			return (Double) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return 0;
	}
}
