package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Debug;
import com.hiveworkshop.rms.util.TwiComboBox;

import java.util.ArrayList;
import java.util.List;

public class AnimationChooser extends TwiComboBox<Sequence> {
	private EditableModel model;
	private TimeEnvironmentImpl timeEnvironment;
	private boolean anims;
	private boolean globalSeqs;
	private boolean playOnSelect;
	private boolean allowUnanimated = false;
	private final List<Sequence> sequences = new ArrayList<>();

	public AnimationChooser(boolean anims, boolean globalSeqs, boolean playOnSelect) {
		super(new Animation("Stand and work for me", 0, 1));
		this.anims = anims;
		this.globalSeqs = globalSeqs;
		this.playOnSelect = playOnSelect;
		setNewLinkedModelOf(sequences);
		setStringFunctionRender(this::getDisplayString);
		addOnSelectItemListener(this::setSequence);

		setFocusable(true);
		addMouseWheelListener(e -> incIndex(e.getWheelRotation()));
	}

	public AnimationChooser setModel(EditableModel model, RenderModel renderModel) {
		if (this.model != model) {
			getComboBoxModel().setSelectedNoListener(null);
		}
		this.model = model;
		if (renderModel != null) {
			timeEnvironment = renderModel.getTimeEnvironment();
		} else {
			timeEnvironment = null;
		}
		updateAnimationList();
		return this;
	}
	public AnimationChooser setModel(RenderModel renderModel) {
//		Debug.print("[AnimationChooser] setModel");
		if (renderModel != null) {
			if (this.model != renderModel.getModel()) {
				getComboBoxModel().setSelectedNoListener(null);
			}
			this.model = renderModel.getModel();
			timeEnvironment = renderModel.getTimeEnvironment();
		} else {
			this.model = null;
			timeEnvironment = null;
		}
		updateAnimationList();
		return this;
	}

	private String getDisplayString(Object value) {
		if (model != null) {
			if (value instanceof Animation) {
				return "(" + model.getAnims().indexOf(value) + ") " + value;
			} else if (value instanceof GlobalSeq) {
				return "" + value;
			} else {
				return "(Unanimated)";
			}
		}
		return "";
	}

	public void chooseSequence(Sequence selectedItem) {
//		Debug.print("[AnimationChooser] chooseSequence: '" + selectedItem + "'");
		selectOrFirstWithListener(selectedItem);
	}

	private void setSequence(Sequence selectedItem) {
//		Debug.print("[AnimationChooser] setSequence: '" + selectedItem + "'");
		if (timeEnvironment != null) {
			Sequence currentSequence = timeEnvironment.getCurrentSequence();
			timeEnvironment.setSequence(selectedItem);

			if (playOnSelect && (currentSequence != selectedItem || !timeEnvironment.isLive() || timeEnvironment.getLength() == timeEnvironment.getAnimationTime())) {
				playAnimation();
			}
		}
	}


	public void playAnimation() {
		if (timeEnvironment != null) {
			timeEnvironment.setRelativeAnimationTime(0);
			timeEnvironment.setLive(true);
		}
	}

	public AnimationChooser setAnims(boolean anims) {
		this.anims = anims;
		return this;
	}

	public AnimationChooser setGlobalSeqs(boolean globalSeqs) {
		this.globalSeqs = globalSeqs;
		return this;
	}

	public AnimationChooser setPlayOnSelect(boolean playOnSelect) {
		this.playOnSelect = playOnSelect;
		return this;
	}

	public AnimationChooser setAllowUnanimated(boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;
		return this;
	}

	public AnimationChooser updateAnimationList() {
		Sequence selectedItem = getSelected();
		sequences.clear();
		Debug.print("[AnimationChooser] updateAnimationList - selected: '" + selectedItem + "'");
		if (model != null) {
			if (anims)          sequences.addAll(model.getAnims());
			if (globalSeqs)     sequences.addAll(model.getGlobalSeqs());

			if (allowUnanimated || (getItemCount() == 0)) {
				sequences.add(0, null);
			}
			selectOrFirstWithListener(selectedItem);
		} else {
			selectOrFirstWithListener(null);
		}
		return this;
	}
}
