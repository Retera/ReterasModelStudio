package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.EventObjectChunk;
import com.hiveworkshop.wc3.mdx.Node;

/**
 * A class for EventObjects, which include such things as craters, footprints, splashes, blood spurts, and sounds
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class EventObject extends IdObject {
	ArrayList<Integer> eventTrack = new ArrayList<>();
	ArrayList<AnimFlag> animFlags = new ArrayList<>();
	Integer globalSeq;
	int globalSeqId = -1;
	boolean hasGlobalSeq;

	private EventObject() {

	}

	protected EventObject(final EventObject source) {

	}

	public EventObject(final String name) {
		this.name = name;
	}

	public EventObject(final EventObjectChunk.EventObject mdxSource) {
		this.name = mdxSource.node.name;
		// debug print:
		// System.out.println(getName() + ": " + Integer.toBinaryString(mdxSource.node.flags));
		if ((mdxSource.node.flags & 1024) != 1024) {
			System.err.println("MDX -> MDL error: An eventobject '" + mdxSource.node.name
					+ "' not flagged as eventobject in MDX!");
		}
		// System.out.println(emitter.node.name + ": " +
		// Integer.toBinaryString(emitter.node.flags));
		// ----- Convert Base NODE to "IDOBJECT" -----

		// (Doesn't use LOADFROM, has no flags)
		setParentId(mdxSource.node.parentId);
		setObjectId(mdxSource.node.objectId);
		final Node node = mdxSource.node;
		if (node.geosetTranslation != null) {
			add(new AnimFlag(node.geosetTranslation));
		}
		if (node.geosetScaling != null) {
			add(new AnimFlag(node.geosetScaling));
		}
		if (node.geosetRotation != null) {
			add(new AnimFlag(node.geosetRotation));
		}
		// ----- End Base NODE to "IDOBJECT" -----

		if (mdxSource.tracks.globalSequenceId >= 0) {
			globalSeqId = mdxSource.tracks.globalSequenceId;
			hasGlobalSeq = true;
		}
		for (final int val : mdxSource.tracks.tracks) {
			eventTrack.add(new Integer(val));
		}
	}

	@Override
	public EventObject copy() {
		final EventObject x = new EventObject();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.parent = parent;

		x.eventTrack = new ArrayList<>(eventTrack);
		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		return x;
	}

	public int size() {
		return eventTrack.size();
	}

	public static EventObject buildEmptyFrom(final EventObject source) {
		return new EventObject(source);

	}

	public void setValuesTo(final EventObject source) {
		eventTrack = source.eventTrack;
	}

	public static EventObject read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("EventObject")) {
			final EventObject e = new EventObject();
			e.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				if (line.contains("ObjectId")) {
					e.objectId = MDLReader.readInt(line);
				} else if (line.contains("Parent")) {
					e.parentId = MDLReader.splitToInts(line)[0];
					// e.parent = mdlr.getIdObject(e.parentId);
				} else if ((line.contains("Visibility") || line.contains("Rotation") || line.contains("Translation")
						|| line.contains("Scaling"))) {
					MDLReader.reset(mdl);
					e.animFlags.add(AnimFlag.read(mdl));
				} else if (line.contains("GlobalSeqId")) {
					if (!e.hasGlobalSeq) {
						e.globalSeqId = MDLReader.readInt(line);
						e.hasGlobalSeq = true;
					} else {
						JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
								"Error while parsing event object: More than one Global Sequence Id is present in the same object!");
					}
				} else if (!line.contains("{") && !line.contains("}")) {
					e.eventTrack.add(new Integer(MDLReader.readInt(line)));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return e;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse EventObject: Missing or unrecognized open statement.");
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
		if (eventTrack.size() <= 0) {
			writer.println("\tEventTrack " + 1 + " {");
			if (hasGlobalSeq) {
				writer.println("\t\tGlobalSeqId " + globalSeqId + ",");
			}
			writer.println("\t\t" + 0 + ",");
		} else {
			writer.println("\tEventTrack " + eventTrack.size() + " {");
			if (hasGlobalSeq) {
				writer.println("\t\tGlobalSeqId " + globalSeqId + ",");
			}
			for (int i = 0; i < eventTrack.size(); i++) {
				writer.println("\t\t" + eventTrack.get(i).toString() + ",");
			}
		}
		writer.println("\t}");
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
		writer.println("}");
	}

	public void deleteAnim(final Animation anim) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (int index = eventTrack.size() - 1; index >= 0; index--) {
			final int i = eventTrack.get(index).intValue();
			if (i >= anim.getStart() && i <= anim.getEnd()) {
				// If this "i" is a part of the anim being removed
				eventTrack.remove(index);
			}
		}

		// BOOM magic happens
	}

	public void timeScale(final int start, final int end, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (final Integer inte : eventTrack) {
			final int i = inte.intValue();
			if (i >= start && i <= end) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.set(eventTrack.indexOf(inte), new Integer((int) (newStart + (ratio * (newEnd - newStart)))));
			}
		}

		sort();

		// BOOM magic happens
	}

	public void copyFrom(final EventObject source, final int start, final int end, final int newStart,
			final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (final Integer inte : source.eventTrack) {
			final int i = inte.intValue();
			if (i >= start && i <= end) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.add(new Integer((int) (newStart + (ratio * (newEnd - newStart)))));
			}
		}

		sort();

		// BOOM magic happens
	}

	public void sort() {
		final int low = 0;
		final int high = eventTrack.size() - 1;

		if (eventTrack.size() > 0) {
			quicksort(low, high);
		}
	}

	private void quicksort(final int low, final int high) {
		// Thanks to Lars Vogel for the quicksort concept code (something to
		// look at), found on google
		// (re-written by Eric "Retera" for use in AnimFlags)
		int i = low, j = high;
		final Integer pivot = eventTrack.get(low + (high - low) / 2);

		while (i <= j) {
			while (eventTrack.get(i).intValue() < pivot.intValue()) {
				i++;
			}
			while (eventTrack.get(j).intValue() > pivot.intValue()) {
				j--;
			}
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}

		if (low < j) {
			quicksort(low, j);
		}
		if (i < high) {
			quicksort(i, high);
		}
	}

	private void exchange(final int i, final int j) {
		final Integer iTime = eventTrack.get(i);

		eventTrack.set(i, eventTrack.get(j));

		eventTrack.set(j, iTime);
	}

	@Override
	public void flipOver(final byte axis) {
		final String currentFlag = "Rotation";
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag flag = animFlags.get(i);
			flag.flipOver(axis);
		}
	}

	public void updateGlobalSeqRef(final MDL mdlr) {
		if (hasGlobalSeq) {
			globalSeq = mdlr.getGlobalSeq(globalSeqId);
		}
	}

	public void updateGlobalSeqId(final MDL mdlr) {
		if (hasGlobalSeq) {
			globalSeqId = mdlr.getGlobalSeqId(globalSeq);
		}
	}

	@Override
	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	@Override
	public void add(final String flag) {
		System.err.println("ERROR: EventObject given unknown flag: " + flag);
	}

	@Override
	public List<String> getFlags() {
		return new ArrayList<>();// Current eventobject implementation
									// uses no flags!
	}

	@Override
	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	/**
	 * @return
	 * @deprecated Use getGlobalSeq
	 */
	@Deprecated
	public int getGlobalSeqId() {
		return globalSeqId;
	}

	/**
	 * @param globalSeqId
	 * @deprecated Use setGlobalSeq
	 */
	@Deprecated
	public void setGlobalSeqId(final int globalSeqId) {
		this.globalSeqId = globalSeqId;
	}

	public boolean isHasGlobalSeq() {
		return hasGlobalSeq;
	}

	public void setHasGlobalSeq(final boolean hasGlobalSeq) {
		this.hasGlobalSeq = hasGlobalSeq;
	}

	public Integer getGlobalSeq() {
		return globalSeq;
	}

	public void setGlobalSeq(final Integer globalSeq) {
		this.globalSeq = globalSeq;
	}

	public ArrayList<Integer> getEventTrack() {
		return eventTrack;
	}

	public void setEventTrack(final ArrayList<Integer> eventTrack) {
		this.eventTrack = eventTrack;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.eventObject(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}
}
