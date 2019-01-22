package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.HelperChunk;

/**
 * Write a description of class Helper here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Helper extends Bone// Haha blizz
{
	// Level 80 Tauren Helpers make code run smoothly
	private Helper() {
		super();
	}

	public Helper(final String name) {
		super(name);
	}

	public Helper(final int j) {
		super();
	}

	public Helper(final Helper h) {
		super(h);
	}

	public Helper(final HelperChunk.Helper helper) {
		// debug print:
		// System.out.println(mdlBone.getName() + ": " +
		// Integer.toBinaryString(bone.node.flags));
		if ((helper.node.flags & 1) != 0) {
			System.err.println("MDX -> MDL error: A helper '" + helper.node.name + "' not flagged as helper in MDX!");
		}
		// ----- Convert Base NODE to "IDOBJECT" -----
		loadFrom(helper.node);
		// ----- End Base NODE to "IDOBJECT" -----

	}

	public static Helper read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Helper")) {
			final Helper b = new Helper();
			b.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				if (line.contains("ObjectId")) {
					b.objectId = MDLReader.readInt(line);
				} else if (line.contains("GeosetId")) {
					final String field = MDLReader.readField(line);
					try {
						b.geosetId = Integer.parseInt(field);
						// b.geoset = mdlr.getGeoset(b.geosetId);
					} catch (final Exception e) {
						if (field.equals("Multiple")) {
							b.multiGeoId = true;
						} else {
							JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
									"Error while parsing: Could not interpret integer from: " + line);
						}
					}
				} else if (line.contains("GeosetAnimId")) {
					final String field = MDLReader.readField(line);
					b.hasGeoAnim = true;
					try {
						b.geosetAnimId = Integer.parseInt(field);
						// b.geosetAnim = mdlr.getGeosetAnim(b.geosetAnimId);
					} catch (final Exception e) {
						if (field.equals("None")) {
							b.geosetAnim = null;
						} else {
							JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
									"Error while parsing: Could not interpret integer from: " + line);
						}
					}
				} else if (line.contains("Parent")) {
					b.parentId = MDLReader.splitToInts(line)[0];
					// b.parent = mdlr.getIdObject(b.parentId);
				} else if ((line.contains("Scaling") || line.contains("Rotation") || line.contains("Translation"))
						&& !line.contains("DontInherit")) {
					MDLReader.reset(mdl);
					b.animFlags.add(AnimFlag.read(mdl));
				} else// Flags like Billboarded
				{
					b.flags.add(MDLReader.readFlag(line));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return b;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse Bone: Missing or unrecognized open statement.");
		}
		return null;
	}

	// printTo is already written as a part of bone; these two things are
	// stupidly the same
	@Override
	public IdObject copy() {
		return new Helper(this);
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.helper(this);
	}
}
