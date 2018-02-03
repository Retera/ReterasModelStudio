package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.RibbonEmitterChunk;

/**
 * RibbonEmitter class, these are the things most people would think of as a particle emitter, I think. Blizzard favored
 * use of these over ParticleEmitters and I do too simply because I so often recycle data and there are more of these to
 * use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class RibbonEmitter extends IdObject implements VisibilitySource {
	public static enum TimeDoubles {
		HeightAbove, HeightBelow, Alpha, TextureSlot;
	}

	public static enum LoneDoubles {
		LifeSpan, Gravity;
	}

	public static enum LoneInts {
		EmissionRate, Rows, Columns, MaterialID;
	}

	static final String[] timeDoubleNames = { "HeightAbove", "HeightBelow", "Alpha", "TextureSlot" };
	double[] timeDoubleData = new double[timeDoubleNames.length];
	static final String[] loneDoubleNames = { "LifeSpan", "Gravity" };
	double[] loneDoubleData = new double[loneDoubleNames.length];
	static final String[] loneIntNames = { "EmissionRate", "Rows", "Columns", "MaterialID" };
	Material material;
	int[] loneIntData = new int[loneIntNames.length];
	Vertex staticColor;

	ArrayList<AnimFlag> animFlags = new ArrayList<>();

	private RibbonEmitter() {

	}

	public RibbonEmitter(final String name) {
		this.name = name;
	}

	public RibbonEmitter(final RibbonEmitterChunk.RibbonEmitter emitter) {
		// debug print:
		if ((emitter.node.flags & 16384) != 16384) {
			System.err.println("MDX -> MDL error: A ribbon emitter '" + emitter.node.name
					+ "' not flagged as ribbon emitter in MDX!");
		}
		// System.out.println(emitter.node.name + ": " +
		// Integer.toBinaryString(emitter.node.flags));
		// ----- Convert Base NODE to "IDOBJECT" -----
		loadFrom(emitter.node);
		// ----- End Base NODE to "IDOBJECT" -----

		setTextureSlot(emitter.textureSlot);
		// System.out.println(attachment.node.name + ": " +
		// Integer.toBinaryString(attachment.unknownNull));

		if (emitter.ribbonEmitterVisibility != null) {
			add(new AnimFlag(emitter.ribbonEmitterVisibility));
		}
		if (emitter.ribbonEmitterHeightAbove != null) {
			add(new AnimFlag(emitter.ribbonEmitterHeightAbove));
		} else {
			setHeightAbove(emitter.heightAbove);
		}
		if (emitter.ribbonEmitterHeightBelow != null) {
			add(new AnimFlag(emitter.ribbonEmitterHeightBelow));
		} else {
			setHeightBelow(emitter.heightBelow);
		}
		setAlpha(emitter.alpha);
		setStaticColor(new Vertex(MdlxUtils.flipRGBtoBGR(emitter.color)));
		setLifeSpan(emitter.lifeSpan);
		setEmissionRate(emitter.emissionRate);
		setRows(emitter.rows);
		setColumns(emitter.columns);
		setMaterialId(emitter.materialId);
		setGravity(emitter.gravity);
	}

	@Override
	public IdObject copy() {
		final RibbonEmitter x = new RibbonEmitter();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.parent = parent;

		x.timeDoubleData = timeDoubleData.clone();
		x.loneDoubleData = loneDoubleData.clone();
		x.loneIntData = loneIntData.clone();
		x.material = material;
		x.staticColor = new Vertex(staticColor);

		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		return x;
	}

	public void updateMaterialRef(final ArrayList<Material> mats) {
		if (getMaterialId() == -1) {
			material = null;
			return;
		}
		material = mats.get(getMaterialId());
	}

	public int getMaterialId() {
		return loneIntData[3];
	}

	public void setMaterialId(final int i) {
		loneIntData[3] = i;
	}

	public static RibbonEmitter read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("RibbonEmitter")) {
			final RibbonEmitter pe = new RibbonEmitter();
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
				} else if (line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation")
						|| line.contains("Scaling")) {
					MDLReader.reset(mdl);
					pe.animFlags.add(AnimFlag.read(mdl));
					foundType = true;
				} else if (line.contains("Color")) {
					foundType = true;
					if (line.contains("static")) {
						pe.staticColor = Vertex.parseText(line);
					} else {
						MDLReader.reset(mdl);
						pe.animFlags.add(AnimFlag.read(mdl));
					}
				}
				for (int i = 0; i < timeDoubleNames.length && !foundType; i++) {
					if (line.contains(timeDoubleNames[i])) {
						foundType = true;
						if (line.contains("static")) {
							pe.timeDoubleData[i] = MDLReader.readDouble(line);
						} else {
							MDLReader.reset(mdl);
							pe.animFlags.add(AnimFlag.read(mdl));
						}
					}
				}
				for (int i = 0; i < loneDoubleNames.length && !foundType; i++) {
					if (line.contains(loneDoubleNames[i])) {
						foundType = true;
						pe.loneDoubleData[i] = MDLReader.readDouble(line);
					}
				}
				for (int i = 0; i < loneIntNames.length && !foundType; i++) {
					if (line.contains(loneIntNames[i])) {
						foundType = true;
						pe.loneIntData[i] = MDLReader.readInt(line);
					}
				}
				if (!foundType) {
					JOptionPane.showMessageDialog(null, "Ribbon emitter did not recognize data at: " + line
							+ "\nThis is probably not a major issue?");
				}

				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return pe;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse RibbonEmitter: Missing or unrecognized open statement.");
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
		String currentFlag = "";
		for (int i = 0; i < 3; i++) {
			currentFlag = timeDoubleNames[i];
			if (timeDoubleData[i] != 0) {
				writer.println("\tstatic " + currentFlag + " " + MDLReader.doubleToString(timeDoubleData[i]) + ",");
			} else {
				boolean set = false;
				for (int a = 0; a < pAnimFlags.size() && !set; a++) {
					if (pAnimFlags.get(a).getName().equals(currentFlag)) {
						pAnimFlags.get(a).printTo(writer, 1);
						pAnimFlags.remove(a);
						set = true;
					}
				}
				if (!set) {
					writer.println("\tstatic " + currentFlag + " " + MDLReader.doubleToString(timeDoubleData[i]) + ",");
				}
			}
		}
		currentFlag = "Color";
		boolean set = false;
		if (staticColor == null) {
			for (int i = 0; i < pAnimFlags.size() && !set; i++) {
				if (pAnimFlags.get(i).getName().equals(currentFlag)) {
					pAnimFlags.get(i).printTo(writer, 1);
					pAnimFlags.remove(i);
					set = true;
				}
			}
		} else {
			writer.println("\tstatic " + currentFlag + " " + staticColor.toString() + ",");
		}
		for (int i = 3; i < 4; i++) {
			currentFlag = timeDoubleNames[i];
			if (timeDoubleData[i] != 0) {
				writer.println("\tstatic " + currentFlag + " " + MDLReader.doubleToString(timeDoubleData[i]) + ",");
			} else {
				set = false;
				for (int a = 0; a < pAnimFlags.size() && !set; a++) {
					if (pAnimFlags.get(a).getName().equals(currentFlag)) {
						pAnimFlags.get(a).printTo(writer, 1);
						pAnimFlags.remove(a);
						set = true;
					}
				}
				if (!set) {
					writer.println("\tstatic " + currentFlag + " " + MDLReader.doubleToString(timeDoubleData[i]) + ",");
				}
			}
		}
		currentFlag = "Visibility";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		for (int i = 0; i < 1; i++) {
			writer.println("\t" + loneIntNames[i] + " " + loneIntData[i] + ",");
		}
		for (int i = 0; i < loneDoubleData.length; i++) {
			if (i == 0 || loneDoubleData[i] != 0) {
				writer.println("\t" + loneDoubleNames[i] + " " + loneDoubleData[i] + ",");
			}
		}
		for (int i = 1; i < loneIntData.length; i++) {
			writer.println("\t" + loneIntNames[i] + " " + loneIntData[i] + ",");
		}
		currentFlag = "Rotation";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		currentFlag = "Translation";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
		}
		currentFlag = "Scaling";
		for (int i = 0; i < pAnimFlags.size(); i++) {
			if (pAnimFlags.get(i).getName().equals(currentFlag)) {
				pAnimFlags.get(i).printTo(writer, 1);
				pAnimFlags.remove(i);
			}
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

	public double getHeightAbove() {
		return timeDoubleData[TimeDoubles.HeightAbove.ordinal()];
	}

	public void setHeightAbove(final double heightAbove) {
		timeDoubleData[TimeDoubles.HeightAbove.ordinal()] = heightAbove;
	}

	public double getHeightBelow() {
		return timeDoubleData[TimeDoubles.HeightBelow.ordinal()];
	}

	public void setHeightBelow(final double heightBelow) {
		timeDoubleData[TimeDoubles.HeightBelow.ordinal()] = heightBelow;
	}

	public double getAlpha() {
		return timeDoubleData[TimeDoubles.Alpha.ordinal()];
	}

	public void setAlpha(final double alpha) {
		timeDoubleData[TimeDoubles.Alpha.ordinal()] = alpha;
	}

	public double getTextureSlot() {
		return timeDoubleData[TimeDoubles.TextureSlot.ordinal()];
	}

	public void setTextureSlot(final double textureSlot) {
		timeDoubleData[TimeDoubles.TextureSlot.ordinal()] = textureSlot;
	}

	public double getLifeSpan() {
		return loneDoubleData[LoneDoubles.LifeSpan.ordinal()];
	}

	public void setLifeSpan(final double lifeSpan) {
		loneDoubleData[LoneDoubles.LifeSpan.ordinal()] = lifeSpan;
	}

	public double getGravity() {
		return loneDoubleData[LoneDoubles.Gravity.ordinal()];
	}

	public void setGravity(final double gravity) {
		loneDoubleData[LoneDoubles.Gravity.ordinal()] = gravity;
	}

	public int getEmissionRate() {
		return loneIntData[LoneInts.EmissionRate.ordinal()];
	}

	public void setEmissionRate(final int emissionRate) {
		loneIntData[LoneInts.EmissionRate.ordinal()] = emissionRate;
	}

	public int getRows() {
		return loneIntData[LoneInts.Rows.ordinal()];
	}

	public void setRows(final int rows) {
		loneIntData[LoneInts.Rows.ordinal()] = rows;
	}

	public int getColumns() {
		return loneIntData[LoneInts.Columns.ordinal()];
	}

	public void setColumns(final int columns) {
		loneIntData[LoneInts.Columns.ordinal()] = columns;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(final Material material) {
		this.material = material;
	}

	public Vertex getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vertex staticColor) {
		this.staticColor = staticColor;
	}

	@Override
	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	@Override
	public void add(final String flag) {
		System.err.println("ERROR: RibbonEmitter given unknown flag: " + flag);
	}

	@Override
	public List<String> getFlags() {
		return new ArrayList<>();// Current ribbon implementation uses no
									// flags!
	}

	@Override
	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.ribbonEmitter(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}
}
