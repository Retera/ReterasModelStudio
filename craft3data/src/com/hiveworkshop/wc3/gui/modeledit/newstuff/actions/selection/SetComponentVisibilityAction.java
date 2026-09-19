package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.v2.ComponentVisibility;

/**
 * Changes the tri-state visibility of components when editability is not
 * involved (hidden to visible-but-locked and back). Transitions that make
 * something editable or take editability away go through the model editor's
 * showComponent/hideComponent so the selection is kept consistent.
 */
public final class SetComponentVisibilityAction implements UndoAction {
	/** A thing whose visibility can be read and written. */
	public interface VisibilityTarget {
		ComponentVisibility getVisibility();

		void setVisibility(ComponentVisibility state);
	}

	private final List<VisibilityTarget> targets;
	private final List<ComponentVisibility> before;
	private final ComponentVisibility after;
	private final Runnable refreshGUIRunnable;

	public SetComponentVisibilityAction(final List<? extends VisibilityTarget> targets,
			final ComponentVisibility after, final Runnable refreshGUIRunnable) {
		this.targets = new ArrayList<>(targets);
		this.before = new ArrayList<>();
		for (final VisibilityTarget target : targets) {
			before.add(target.getVisibility());
		}
		this.after = after;
		this.refreshGUIRunnable = refreshGUIRunnable;
	}

	@Override
	public void redo() {
		for (final VisibilityTarget target : targets) {
			target.setVisibility(after);
		}
		refreshGUIRunnable.run();
	}

	@Override
	public void undo() {
		for (int i = 0; i < targets.size(); i++) {
			targets.get(i).setVisibility(before.get(i));
		}
		refreshGUIRunnable.run();
	}

	@Override
	public String actionName() {
		switch (after) {
		case HIDDEN:
			return "hide";
		case VISIBLE:
			return "lock";
		default:
			return "make editable";
		}
	}
}
