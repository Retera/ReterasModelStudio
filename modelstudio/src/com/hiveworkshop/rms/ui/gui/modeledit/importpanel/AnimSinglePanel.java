package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AnimSinglePanel extends AnimPanel {

	private final TwiTextField nameField;
	protected JLabel oldName;

//	String timeScaleDonInfo = "All bones set to receive motion will have the animation data of the following animation(s) replaced by this animation";
	String timeScaleDonInfo = "<html><p>Use the animation data of this animation in the<br>following animations, where applicable:";

//	private String reciveText = "All bones set to receive motion will have the animation data of this animation replaced by: ";
	private String reciveText = "<html><p>Use the animation data of the following animation<br>in this animation, where applicable:";


	public AnimSinglePanel(ModelHolderThing mht, TwiList<AnimShell> animJList) {
		super(mht, animJList);
		setLayout(new MigLayout("gap 0, fill", "[][]", "[][][][][grow]"));

		animSrcRenderer = new AnimListCellRenderer(false, this::getSrcStatus, this::isSrcSelected);

		sourceList = new SearchListPanel<>(reciveText, this::search)
				.setRenderer(animSrcRenderer)
				.setSelectionConsumer(this::onSourceSelected)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		nameField = new TwiTextField(24, this::renameAnim);
		nameField.setFont(new Font("Arial", Font.BOLD, 18));
		add(nameField, "align center, spanx, wrap");
		oldName = new JLabel("Select an Animation");

		animInfo = new JLabel("");

		add(oldName, "align center, spanx, wrap");
		add(animInfo, "align center, spanx, wrap");

		add(getImportCheckBox(this::setImport), "align center, split");
		add(getReverseCheckBox(this::setInReverse), "wrap");

		add(sourceList, "spanx, growx, growy");
	}

	public void updateAnimPanel(List<AnimShell> selectedValuesList) {
		super.updateAnimPanel(selectedValuesList);
		AnimShell firstAnimShell = selectedValuesList.get(0);
		nameField.setText(firstAnimShell.getName());
		oldName.setText(firstAnimShell.getOldName());
	}

	private void renameAnim(String newName){
		if(selectedValuesList.size() == 1 && selectedValuesList.get(0) != null){
			selectedValuesList.get(0).setName(newName);
		}
	}

	protected String getInfoText() {
		if (selectedValuesList.size() == 1 && selectedValuesList.get(0) != null) {
			Animation anim = selectedValuesList.get(0).getAnim();
			return "length: " + anim.getLength()
					+ "    speed: " + anim.getMoveSpeed()
					+ "    rarity: " + anim.getRarity()
					+ "    looping: " + !anim.isNonLooping();
		}
		return "";
	}
}
