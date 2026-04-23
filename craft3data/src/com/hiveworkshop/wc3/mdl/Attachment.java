package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.AttachmentChunk;
import com.matrixeater.localization.LocalizationManager;

/**
 * Write a description of class Attachment here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Attachment extends IdObject implements VisibilitySource {
	String path = null;
	ArrayList<AnimFlag> animFlags = new ArrayList<>();
	ArrayList<String> flags = new ArrayList<>();

	int AttachmentID = 0;

	private Attachment() {

	}

	public Attachment(final String name) {
		this.name = name;
	}

	public Attachment(final AttachmentChunk.Attachment attachment) {
		this(attachment.node.name);
		// debug print:
		// System.out.println(mdlBone.getName() + ": " +
		// Integer.toBinaryString(bone.node.flags));
		if ((attachment.node.flags & 2048) != 2048) {
			System.err.println(
					"MDX -> MDL " + LocalizationManager.getInstance().get("println.attachment_attachment_1") + " '" + attachment.node.name + "' " + LocalizationManager.getInstance().get("println.attachment_attachment_2"));
		}
		// ----- Convert Base NODE to "IDOBJECT" -----
		loadFrom(attachment.node);
		// ----- End Base NODE to "IDOBJECT" -----

		if (attachment.unknownNull != 0) {
			System.err
					.println(LocalizationManager.getInstance().get("println.attachment_attachment_3") + name);
		}
		// System.out.println(attachment.node.name + ": " +
		// Integer.toBinaryString(attachment.unknownNull));

		if (attachment.attachmentVisibility != null) {
			add(new AnimFlag(attachment.attachmentVisibility));
		}

		setAttachmentID(attachment.attachmentId);
		setPath(attachment.unknownName_modelPath);

	}

	@Override
	public IdObject copy() {
		final Attachment x = new Attachment();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.path = path;
		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		flags = new ArrayList<>(x.flags);
		return x;
	}

	public static Attachment read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Attachment")) {
			final Attachment at = new Attachment();
			at.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				if (line.contains("ObjectId")) {
					at.objectId = MDLReader.readInt(line);
				} else if (line.contains("Parent")) {
					at.parentId = MDLReader.splitToInts(line)[0];
					// at.parent = mdlr.getIdObject(at.parentId);
				} else if (line.contains("Path")) {
					at.path = MDLReader.readName(line);
				} else if (line.contains("AttachmentID ")) {
					at.AttachmentID = MDLReader.readInt(line);
				} else if ((line.contains("Visibility") || line.contains("Scaling") || line.contains("Translation")
						|| line.contains("Rotation")) && !line.contains("DontInherit"))// Visibility,
																						// Rotation,
																						// etc
				{
					MDLReader.reset(mdl);
					at.animFlags.add(AnimFlag.read(mdl));
				} else {
					at.flags.add(MDLReader.readFlag(line));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return at;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
				LocalizationManager.getInstance().get("dialog.attachment_read"));
		}
		return null;
	}

	@Override
	public void printTo(final PrintWriter writer, final int version) {
		// Remember to update the ids of things before using this
		// -- uses objectId value of idObject superclass
		// -- uses parentId value of idObject superclass
		// -- uses the parent (java Object reference) of idObject superclass
		// -- uses geosetAnimId
		// -- uses geosetId
		writer.println(MDLReader.getClassName(this.getClass()) + " \"" + getName() + "\" {");
		if (objectId != -1) {
			writer.println(LocalizationManager.getInstance().get("println.attachment_printto_object") + objectId + ",");
		}
		if (parentId != -1) {
			writer.println(LocalizationManager.getInstance().get("println.attachment_printto_parent") + parentId + ",\t// \"" + getParent().getName() + "\"");
		}
		if (path != null) {
			writer.println(LocalizationManager.getInstance().get("println.attachment_printto_path") + path + "\",");
		}
		if (AttachmentID != 0) {
			writer.println(LocalizationManager.getInstance().get("println.attachment_printto_attachment") + AttachmentID + ",");
		}
		for (int i = 0; i < flags.size(); i++) {
			writer.println("\t" + flags.get(i) + ",");
		}
		for (int i = 0; i < animFlags.size(); i++) {
			animFlags.get(i).printTo(writer, 1);
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
					LocalizationManager.getInstance().get("dialog.attachment_setvisibilityflag") + getName() + ".");
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
			System.err.println(
					LocalizationManager.getInstance().get("dialog.attachment_getvisibilityflag") + getName() + ".");
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

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		if (!"".equals(path)) {
			this.path = path;
		}
	}

	public int getAttachmentID() {
		return AttachmentID;
	}

	public void setAttachmentID(final int attachmentID) {
		AttachmentID = attachmentID;
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
	public void apply(final IdObjectVisitor visitor) {
		visitor.attachment(this);
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
