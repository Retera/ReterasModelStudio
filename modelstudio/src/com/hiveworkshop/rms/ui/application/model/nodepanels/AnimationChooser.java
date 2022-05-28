package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.TwiComboBox;

import java.awt.*;

public class AnimationChooser extends TwiComboBox<Sequence>{
	private EditableModel model;
	private RenderModel renderModel;
	private boolean anims;
	private boolean globalSeqs;
	private boolean playOnSelect;
	private boolean allowUnanimated = false;

	public AnimationChooser(boolean anims, boolean globalSeqs, boolean playOnSelect) {
		super(new Animation("Stand and work for me", 0, 1));
		this.anims = anims;
		this.globalSeqs = globalSeqs;
		this.playOnSelect = playOnSelect;
		setStringFunctionRender(this::getDisplayString);
		addOnSelectItemListener(this::setSequence);

		setMaximumSize(new Dimension(99999999, 35));
		setFocusable(true);
		addMouseWheelListener(e -> incIndex(e.getWheelRotation()));
	}

	public AnimationChooser setModel(EditableModel model, RenderModel renderModel) {
		this.model = model;
		this.renderModel = renderModel;
		updateAnimationList();
		return this;
	}

	private String getDisplayString(Object value) {
		if(model != null){
			if (value instanceof Animation) {
				return "(" + model.getAnims().indexOf(value) + ") " + value;
			} else if (value instanceof GlobalSeq){
				return "" + value;
			} else {
				return "(Unanimated)";
			}
		}
		return "";
	}

	public void chooseSequence(Sequence selectedItem) {
		selectOrFirst(selectedItem);
	}
	private void setSequence(Sequence selectedItem) {
		System.out.println("setting sequence: " + selectedItem);
		if (renderModel != null) {
			renderModel.getTimeEnvironment().setSequence(selectedItem);
			if(playOnSelect){
				playAnimation();
			}
		}
	}


	public void playAnimation() {
		if (renderModel != null) {
			renderModel.getTimeEnvironment().setRelativeAnimationTime(0);
			renderModel.getTimeEnvironment().setLive(true);
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
		Sequence selectedItem = (Sequence) getSelectedItem();
		removeAllItems();
		if (model != null) {
			if (anims)          addAll(model.getAnims());
			if (globalSeqs)     addAll(model.getGlobalSeqs());

			if (allowUnanimated || (getItemCount() == 0)) {
				addItem(null, 0);
			}

			selectOrFirst(selectedItem);
		}
		return this;
	}
}
