package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class AnimPanel extends JPanel {

	protected JLabel oldName;
	private JLabel animInfo;
	protected TriCheckBox inReverse;
	protected TriCheckBox doImportBox;

//	String timeScaleDonInfo = "All bones set to receive motion will have the animation data of the following animation(s) replaced by this animation";
	String timeScaleDonInfo = "<html><p>Use the animation data of this animation in the<br>following animations, where applicable:";

//	private String reciveText = "All bones set to receive motion will have the animation data of this animation replaced by: ";
	private String reciveText = "<html><p>Use the animation data of the following animation<br>in this animation, where applicable:";


	protected TwiTextField nameField;

	protected ModelHolderThing mht;

	protected AnimShell selectedAnim;
	protected TwiList<AnimShell> animJList;

	protected SearchListPanel<AnimShell> destList;
	protected SearchListPanel<AnimShell> sourceList;

	protected AnimListCellRenderer animDestRenderer;
	protected AnimListCellRenderer animSrcRenderer;


	public AnimPanel(ModelHolderThing mht, TwiList<AnimShell> animJList) {
		this.mht = mht;
		this.animJList = animJList;
	}

	public AnimPanel(ModelHolderThing mht, TwiList<AnimShell> animJList, final AnimListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, fill", "[][]", "[][][][][grow]"));
		this.animJList = animJList;

		animDestRenderer = new AnimListCellRenderer().setMarkDontImp(true).setDestList(true);
		animSrcRenderer = new AnimListCellRenderer();

		sourceList = new SearchListPanel<>(reciveText, this::search)
				.setRenderer(animSrcRenderer)
				.setSelectionConsumer(this::onSourceSelected)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//		destList = new SearchListPanel<>(timeScaleDonInfo, this::search)
//				.setRenderer(animDestRenderer)
//				.setSelectionConsumer(this::onDestSelected)
//				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

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

	protected JCheckBox getReverseCheckBox(Consumer<Boolean> boolConsumer) {
		inReverse = new TriCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addActionListener(e -> boolConsumer.accept(inReverse.isSelected()));
		return inReverse;
	}
	protected JCheckBox getImportCheckBox(Consumer<Boolean> boolConsumer) {
		doImportBox = new TriCheckBox("Import");
		doImportBox.setSelected(true);
		doImportBox.addActionListener(e -> boolConsumer.accept(doImportBox.isSelected()));
		return doImportBox;
	}

	protected boolean search(AnimShell animShell, String text){
		return animShell.getDisplayName().matches("(?i).*" + text + ".*");
	}

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnim = animShell;
//		animRenderer.setSelectedAnim(selectedAnim);
//		animDestRenderer.setSelectedAnim(selectedAnim);
		animSrcRenderer.setSelectedAnim(selectedAnim);

		updateAnimDataSrcList(animShell.isFromDonating());
		nameField.setText(animShell.getName());
		oldName.setText(animShell.getOldName());

		animInfo.setText(getInfoText());

		inReverse.setSelected(animShell.isReverse());
		doImportBox.setSelected(animShell.isDoImport());
	}

	private void renameAnim(String newName){
		if(selectedAnim != null){
			selectedAnim.setName(newName);
		}
	}

	private String getInfoText() {
		Animation anim = selectedAnim.getAnim();
		if (anim != null) {
			return "length: " + anim.getLength()
					+ "    speed: " + anim.getMoveSpeed()
					+ "    rarity: " + anim.getRarity()
					+ "    looping: " + !anim.isNonLooping();
		}
		return "";
	}

	private void setInReverse(boolean inReverse) {
		selectedAnim.setReverse(inReverse);
	}
	private void setImport(boolean doImport) {
		selectedAnim.setDoImport(doImport);
		animJList.repaint();
	}


	private void updateRecModAnimList(boolean fromDonating) {
		destList.clearAndReset();

		List<AnimShell> othersAnimShells = fromDonating ? mht.recModAnims : mht.donModAnims;
		List<AnimShell> selfAnimShells = fromDonating ? mht.donModAnims : mht.recModAnims;

		destList.addAll(othersAnimShells);
		destList.addAll(selfAnimShells);
		destList.remove(selectedAnim);

		if(!selectedAnim.getAnimDataDests().isEmpty()){
			destList.scrollToReveal(selectedAnim.getAnimDataDests().get(0));
		}
		destList.repaint();
	}

	private void updateAnimDataSrcList(boolean fromDonating) {
		sourceList.clearAndReset();

		List<AnimShell> othersAnimShells = fromDonating ? mht.recModAnims : mht.donModAnims;
		List<AnimShell> selfAnimShells = fromDonating ? mht.donModAnims : mht.recModAnims;

		sourceList.addAll(othersAnimShells);
		sourceList.addAll(selfAnimShells);
		sourceList.remove(selectedAnim);

		if(selectedAnim.getAnimDataSrc() != null){
			sourceList.scrollToReveal(selectedAnim.getAnimDataSrc());
		}

		sourceList.repaint();
	}

	private void onSourceSelected(AnimShell animShell){
		if(selectedAnim != null && animShell != null) {
			if (selectedAnim.getAnimDataSrc() == animShell) {
				selectedAnim.setAnimDataSrc(null);
			} else {
				selectedAnim.setAnimDataSrc(animShell);
			}
			SwingUtilities.invokeLater(animJList::repaint);
		}
	}
	private void onDestSelected(AnimShell animShell){
		if(selectedAnim != null && animShell != null) {
			if (animShell.getAnimDataSrc() == selectedAnim) {
				animShell.setAnimDataSrc(null);
			} else {
				animShell.setAnimDataSrc(selectedAnim);
			}
			SwingUtilities.invokeLater(animJList::repaint);
		}
	}
}
