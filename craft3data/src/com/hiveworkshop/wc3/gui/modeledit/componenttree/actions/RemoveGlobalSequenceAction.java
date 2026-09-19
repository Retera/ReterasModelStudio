package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;

/**
 * Removes a global sequence and detaches every track and event object that
 * used it, so it does not come back through the save-time list rebuild.
 * <p>
 * Tracks reference the Integer object held in the model's list, so identity
 * is the primary test. A value match is accepted only when the model holds no
 * other sequence of that length, which covers tracks created with their own
 * Integer instance.
 */
public final class RemoveGlobalSequenceAction implements UndoAction {
	private final EditableModel model;
	private final Integer globalSeq;
	private final ModelStructureChangeListener listener;
	private final List<AnimFlag> detachedFlags = new ArrayList<>();
	private final List<EventObject> detachedEvents = new ArrayList<>();
	private int index = -1;

	public RemoveGlobalSequenceAction(final EditableModel model, final Integer globalSeq,
			final ModelStructureChangeListener listener) {
		this.model = model;
		this.globalSeq = globalSeq;
		this.listener = listener;
	}

	private boolean refersToUs(final Integer candidate, final boolean valueIsUnique) {
		if (candidate == null) {
			return false;
		}
		return (candidate == globalSeq) || (valueIsUnique && candidate.equals(globalSeq));
	}

	@Override
	public void redo() {
		detachedFlags.clear();
		detachedEvents.clear();
		int sameValueCount = 0;
		for (final Integer other : model.getGlobalSeqs()) {
			if (other.equals(globalSeq)) {
				sameValueCount++;
			}
		}
		final boolean valueIsUnique = sameValueCount <= 1;
		index = ComponentListUtil.removeIdentity(model.getGlobalSeqs(), globalSeq);
		for (final AnimFlag flag : model.getAllAnimFlags()) {
			if (refersToUs(flag.getGlobalSeq(), valueIsUnique)) {
				flag.setGlobalSeq(null);
				flag.setHasGlobalSeq(false);
				detachedFlags.add(flag);
			}
		}
		for (final EventObject eventObject : model.sortedIdObjects(EventObject.class)) {
			if (refersToUs(eventObject.getGlobalSeq(), valueIsUnique)) {
				eventObject.setGlobalSeq(null);
				eventObject.setHasGlobalSeq(false);
				detachedEvents.add(eventObject);
			}
		}
		listener.globalSequenceLengthChanged(index, null);
	}

	@Override
	public void undo() {
		ComponentListUtil.insertAt(model.getGlobalSeqs(), index, globalSeq);
		for (final AnimFlag flag : detachedFlags) {
			flag.setGlobalSeq(globalSeq);
			flag.setHasGlobalSeq(true);
		}
		for (final EventObject eventObject : detachedEvents) {
			eventObject.setGlobalSeq(globalSeq);
			eventObject.setHasGlobalSeq(true);
		}
		detachedFlags.clear();
		detachedEvents.clear();
		listener.globalSequenceLengthChanged(index, globalSeq);
	}

	@Override
	public String actionName() {
		return "delete global sequence";
	}
}
