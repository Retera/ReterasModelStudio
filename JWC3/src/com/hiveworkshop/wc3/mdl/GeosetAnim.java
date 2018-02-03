package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdx.GeosetAnimationChunk;

/**
 * The geoset anims, heaven forbit they be forgotten.
 *
 * Eric Theller 11/10/2011
 */
public class GeosetAnim implements VisibilitySource, Named {
	ArrayList<AnimFlag> animFlags = new ArrayList<>();
	double staticAlpha = -1;
	Vertex staticColor = null;
	int geosetId = -1;
	Geoset geoset;
	boolean dropShadow = false;

	public GeosetAnim(final AnimFlag flag) {
		animFlags.add(flag);
	}

	public GeosetAnim(final ArrayList<AnimFlag> flags) {
		animFlags = flags;
	}

	public GeosetAnim(final Geoset g) {
		geoset = g;
	}

	public GeosetAnim(final Geoset geoset, final GeosetAnim other) {
		animFlags = new ArrayList<>();
		for (final AnimFlag flag : other.getAnimFlags()) {
			animFlags.add(new AnimFlag(flag));
		}
		staticAlpha = other.staticAlpha;
		staticColor = other.staticColor;
		geosetId = other.geosetId;
		this.geoset = geoset;
		dropShadow = other.dropShadow;
	}

	/**
	 * @param geosetId
	 * @deprecated GeosetIds are regenerated on save, this is here for MDX converter
	 */
	@Deprecated
	public GeosetAnim(final int geosetId) {
		this.geosetId = geosetId;
	}

	public GeosetAnim(final GeosetAnimationChunk.GeosetAnimation geosetAnim) {
		this.geosetId = geosetAnim.geosetId;
		if (geosetAnim.geosetAlpha == null) {
			setStaticAlpha(geosetAnim.alpha);
		} else {
			addAnimFlag(new AnimFlag(geosetAnim.geosetAlpha));
		}
		setDropShadow((geosetAnim.flags & 1) == 1);
		if ((geosetAnim.flags & 2) == 2) {
			if (geosetAnim.geosetColor == null) {
				final Vertex coloring = new Vertex(MdlxUtils.flipRGBtoBGR(geosetAnim.color));
				if (coloring.x != 1.0 || coloring.y != 1.0 || coloring.z != 1.0) {
					setStaticColor(coloring);
				}
			} else {
				addAnimFlag(new AnimFlag(geosetAnim.geosetColor));
			}
		}
	}

	private GeosetAnim() {

	}

	public static GeosetAnim read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("GeosetAnim")) {
			final GeosetAnim geo = new GeosetAnim();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("}") || line.contains("},")) {
				if (line.contains("static Alpha")) {
					geo.staticAlpha = MDLReader.readDouble(line);
				} else if (line.contains("static Color")) {
					geo.staticColor = Vertex.parseText(line);
				} else if (line.contains("GeosetId")) {
					geo.geosetId = MDLReader.readInt(line);
				} else if (line.contains("DropShadow")) {
					geo.dropShadow = true;
				} else {
					MDLReader.reset(mdl);
					geo.animFlags.add(AnimFlag.read(mdl));
				}
				MDLReader.mark(mdl);
			}
			// MDLReader.reset(mdl);
			return geo;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse GeosetAnim: Missing or unrecognized open statement.");
		}
		return null;
	}

	public void printTo(final PrintWriter writer, final int tabHeight) {

		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		String inTabs = tabs;
		inTabs = inTabs + "\t";
		writer.println(tabs + "GeosetAnim {");
		if (dropShadow) {
			writer.println(inTabs + "DropShadow,");
		}
		for (int i = 0; i < animFlags.size(); i++) {
			animFlags.get(i).printTo(writer, 1);
		}
		if (staticAlpha != -1) {
			writer.println(inTabs + "static Alpha " + staticAlpha + ",");
		}
		if (staticColor != null) {
			writer.println(inTabs + "static Color " + staticColor + ",");
		}
		if (geosetId != -1) {
			writer.println("\tGeosetId " + geosetId + ",");
		}
		writer.println(tabs + "}");
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
					"Some visiblity animation data was lost unexpectedly during overwrite in " + getVisTagname() + ".");
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
					"Some visiblity animation data was lost unexpectedly during retrieval in " + getVisTagname() + ".");
		}
		return output;
	}

	public String getVisTagname() {
		return geoset.getName();
	}

	@Override
	public String getName() {
		return geoset.getName() + "'s Anim";
	}

	public GeosetAnim getMostVisible(final GeosetAnim partner) {
		if (getVisibilityFlag() != null && partner != null) {
			final AnimFlag thisFlag = getVisibilityFlag();
			final AnimFlag thatFlag = partner.getVisibilityFlag();
			if (thatFlag != null) {
				final AnimFlag result = thisFlag.getMostVisible(thatFlag);
				if (result == thisFlag) {
					return this;
				} else if (result == thatFlag) {
					return partner;
				}
			}
		}
		return null;
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	public void setAnimFlags(final ArrayList<AnimFlag> animFlags) {
		this.animFlags = animFlags;
	}

	public void addAnimFlag(final AnimFlag af) {
		animFlags.add(af);
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(final double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public Vertex getStaticColor() {
		return staticColor;
	}

	public void setStaticColor(final Vertex staticColor) {
		this.staticColor = staticColor;
	}

	/**
	 * @return
	 * @deprecated Used for MDX -> MDL code
	 */
	@Deprecated
	public int getGeosetId() {
		return geosetId;
	}

	/**
	 * @param geosetId
	 * @deprecated Used for MDX -> MDL code
	 */
	@Deprecated
	public void setGeosetId(final int geosetId) {
		this.geosetId = geosetId;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public void setGeoset(final Geoset geoset) {
		this.geoset = geoset;
	}

	public boolean isDropShadow() {
		return dropShadow;
	}

	public void setDropShadow(final boolean dropShadow) {
		this.dropShadow = dropShadow;
	}
}
