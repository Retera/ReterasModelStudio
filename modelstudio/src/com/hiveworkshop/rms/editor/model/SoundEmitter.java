package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
Sounds {
    SoundFile {
        Path <Sound file path - string>,
        ? { float,float },
        SoundChannel <long>, ?
    }
}

 SoundEmitter {
 ObjectId <long>,
 Parent <long>,
 SoundTrack <long_count> {
 <time - long>: <sound id - long>,
 ...
 }
 (Translation { <float_x>, <float_y>, <float_z> })
 (Rotation { <float_a>, <float_b>, <float_c>, <float_d> })
 (Scaling { <float_x>, <float_y>, <float_z> })
 }
 * </pre>
 *
 * The MDX Tags are: SNDS, SNME, KESK(Sound track animations)
 *
 * https://www.hiveworkshop.com/threads/mdx-secrets-discussion.238635/page-3
 */
public class SoundEmitter extends IdObject {
	private static final List<String> EMPTY = new ArrayList<>();
	List<AnimFlag<?>> animFlags = new ArrayList<>();
	List<SoundFile> soundFiles;

	private SoundEmitter() {

	}

	public SoundEmitter(final String name) {
		this.name = name;
	}

	@Override
	public IdObject copy() {
		final SoundEmitter x = new SoundEmitter();

		x.name = name;
		x.pivotPoint = new Vec3(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		for (final AnimFlag<?> af : animFlags) {
			x.animFlags.add(AnimFlag.createFromAnimFlag(af));
		}
		return x;
	}

	// public static SoundEmitter read(final BufferedReader mdl, final EditableModel mdlr) {
	// 	String line = MDLReader.nextLine(mdl);
	// 	if (line.contains("SoundEmitter")) {
	// 		final SoundEmitter lit = new SoundEmitter();
	// 		lit.setName(MDLReader.readName(line));
	// 		MDLReader.mark(mdl);
	// 		line = MDLReader.nextLine(mdl);
	// 		while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
	// 				&& !line.equals("COMPLETED PARSING")) {
	// 			if (line.contains("ObjectId")) {
	// 				lit.objectId = MDLReader.readInt(line);
	// 			} else if (line.contains("Parent")) {
	// 				lit.parentId = MDLReader.splitToInts(line)[0];
	// 				// lit.parent = mdlr.getIdObject(lit.parentId);
	// 			} else if (!line.contains("static") && line.contains("{")) {
	// 				MDLReader.reset(mdl);
	// 				final AnimFlag flag = AnimFlag.read(mdl);
	// 				lit.animFlags.add(flag);
	// 			}
	// 			MDLReader.mark(mdl);
	// 			line = MDLReader.nextLine(mdl);
	// 		}
	// 		return lit;
	// 	} else {
	// 		JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
	// 				"Unable to parse SoundEmitter: Missing or unrecognized open statement.");
	// 	}
	// 	return null;
	// }

	// @Override
	// public void printTo(final PrintWriter writer) {
	// 	// Remember to update the ids of things before using this
	// 	// -- uses objectId value of idObject superclass
	// 	// -- uses parentId value of idObject superclass
	// 	// -- uses the parent (java Object reference) of idObject superclass
	// 	final List<AnimFlag> pAnimFlags = new ArrayList<>(this.animFlags);
	// 	writer.println(MDLReader.getClassName(this.getClass()) + " \"" + getName() + "\" {");
	// 	if (objectId != -1) {
	// 		writer.println("\tObjectId " + objectId + ",");
	// 	}
	// 	if (parentId != -1) {
	// 		writer.println("\tParent " + parentId + ",\t// \"" + getParent().getName() + "\"");
	// 	}

	// 	for (int i = 0; i < pAnimFlags.size(); i++) {
	// 		pAnimFlags.get(i).printTo(writer, 1);
	// 	}
	// 	writer.println("}");
	// }

	public AnimFlag<?> getSoundTrackFlag() {
		int count = 0;
		AnimFlag<?> output = null;
		for (final AnimFlag<?> af : animFlags) {
			if (af.getName().equals("SoundTrack")) {
				count++;
				output = af;
			}
		}
		if (count > 1) {
			JOptionPane.showMessageDialog(null,
					"Some SoundTrack animation data was lost unexpectedly during retrieval in " + getName() + ".");
		}
		return output;
	}

	public void setSoundTrackFlag(final AnimFlag<?> flag) {
		int count = 0;
		int index = 0;
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag<?> af = animFlags.get(i);
			if (af.getName().equals("SoundTrack")) {
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
					"Some SoundTrack animation data was lost unexpectedly during overwrite in " + getName() + ".");
		}
	}

	public String getVisTagname() {
		return "soundEmitter";// geoset.getName();
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}
}
