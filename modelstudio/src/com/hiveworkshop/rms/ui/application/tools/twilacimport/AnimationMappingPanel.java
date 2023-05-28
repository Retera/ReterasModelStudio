package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.TriCheckBox;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class AnimationMappingPanel extends JPanel {

	Animation prototypeAnim = new Animation("An Extra Empty Animation", 0, 1000);
	AnimShell prototypeAnimShell = new AnimShell(prototypeAnim);

	List<AnimShell> donAnimations;
	List<AnimShell> recAnimations;

	public AnimationMappingPanel(List<Animation> donAnims, List<Animation> recAnims){
		super(new MigLayout("ins 0, fill, wrap 2", "[sgx anim][sgx anim]", "[][grow][]"));

		donAnimations = getShellList(donAnims, true, false);
		recAnimations = getShellList(recAnims, false, true);

		TwiList<AnimShell> donAnimList = new TwiList<>(donAnimations);
		donAnimList.setPrototypeCellValue(prototypeAnimShell);
		AnimListCellRenderer donRenderer = new AnimListCellRenderer(true).setMarkDontImp(true);
		donAnimList.setCellRenderer(donRenderer);

		JButton autoMatchAnimations = new JButton("Auto match animations");
		autoMatchAnimations.addActionListener(e -> matchAnimsByName());
		add(autoMatchAnimations, "wrap");

		JPanel donAnimPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[]", "[][grow]"));
		donAnimPanel.add(new JLabel("Map motion from:"), "wrap");
		donAnimPanel.add(new JScrollPane(donAnimList), "growy, growx");


		AnimP recAnimPanel = new AnimP(recAnimations, donAnimPanel::repaint);

		add(donAnimPanel, "growy, growx");
		add(recAnimPanel, "growy, growx");

		donAnimList.addMultiSelectionListener(recAnimPanel::setSelectedDonAnims);

	}

	protected Map<Sequence, Sequence> getRecToDonSequenceMap() {
		Map<Sequence, Sequence> recToDonSequenceMap = new HashMap<>(); // receiving animations to donating animations

		for (AnimShell animShell : donAnimations) {
			if (animShell.isDoImport()) {
				recToDonSequenceMap.put(animShell.getAnim().deepCopy(), animShell.getAnim());
			}
		}

		for (AnimShell animShell : recAnimations) {
			if (animShell.getAnimDataSrc() != null) {
				recToDonSequenceMap.put(animShell.getAnim(), animShell.getAnimDataSrc().getAnim());
			}
		}
		return recToDonSequenceMap;
	}

	protected List<AnimShell> getShellList(List<Animation> anims, boolean fromDonating, boolean doImp) {
		ArrayList<AnimShell> animShells = new ArrayList<>();
		for (Animation animation : anims) {
			AnimShell animShell = new AnimShell(animation, fromDonating);
			animShell.setDoImport(doImp);
			animShells.add(animShell);
		}
		return animShells;
	}

	private void matchAnimsByName() {
		for (AnimShell recAnimShell : recAnimations) {
			for (AnimShell donAnimShell : donAnimations) {
				if (recAnimShell.getName().equals(donAnimShell.getName())) {
					recAnimShell.setAnimDataSrc(donAnimShell);
					break;
				} else if (recAnimShell.getName().startsWith(donAnimShell.getName().split(" ")[0])) {
					if (recAnimShell.getAnimDataSrcAnim() == null) {
						recAnimShell.setAnimDataSrc(donAnimShell);
					} else {
						int orgLength = recAnimShell.getAnim().getLength();
						int lengthDiffCurr = Math.abs(recAnimShell.getAnimDataSrcAnim().getLength() - orgLength);
						int lengthDiffNew = Math.abs(donAnimShell.getAnim().getLength() - orgLength);
						if (lengthDiffNew < lengthDiffCurr) {
							recAnimShell.setAnimDataSrc(donAnimShell);
						}
					}
				}
			}
		}
		repaint();
	}

	private static class AnimP extends JPanel {
		AnimListCellRenderer recRenderer;
		List<AnimShell> recAnimations;
		TwiList<AnimShell> recAnimList;
		TriCheckBox impBox;
		AnimShell donAnim;
		Collection<AnimShell> donAnims;
		Runnable updateUi;

		AnimP(List<AnimShell> recAnimations, Runnable updateUi){
			super(new MigLayout("ins 0, fill"));
			this.recAnimations = recAnimations;
			this.updateUi = updateUi;
			recAnimList = new TwiList<>(recAnimations).addMultiSelectionListener(this::onSelection);

			recAnimList.setPrototypeCellValue(new AnimShell(new Animation("An Extra Empty Animation", 0, 1000), false));

			recRenderer = new AnimListCellRenderer(true);
			recRenderer.setDestList(true);
			recAnimList.setRenderer(recRenderer);
			impBox = new TriCheckBox("Import as new Animation");
			impBox.addActionListener(e -> onImpToggle(impBox.isSelected()));

			add(impBox, "wrap");
			add(new JLabel("Into existing animation:"), "wrap");
			add(new JScrollPane(recAnimList), "growy, growx");
		}

		void onSelection(Collection<AnimShell> recAnims){
			if(!recAnims.isEmpty()){
				if(donAnim != null){
					for (AnimShell anim : recAnims){
						if (anim.getAnimDataSrc() == donAnim){
							anim.setAnimDataSrc(null);
						} else {
							anim.setAnimDataSrc(donAnim);
						}
					}
				}
				recAnimList.setSelectedValue(null, false);
			}
		}

		void onImpToggle(boolean imp){
			if(donAnim != null){
				donAnim.setDoImport(imp);
			} else if (donAnims != null){
				for(AnimShell donAnim : donAnims){
					donAnim.setDoImport(imp);
				}
			}
		}

		AnimP setSelectedDonAnim(AnimShell donAnim){
			this.donAnims = null;
			this.donAnim = null;
			impBox.setSelected(donAnim.isDoImport());
			recRenderer.setSelectedAnim(donAnim);
			scrollToRevealFirstChosen(donAnim);
			this.donAnim = donAnim;

			return this;
		}
		private void scrollToRevealFirstChosen(AnimShell animShell) {
			for (int indexOfFirst = 0; indexOfFirst < recAnimations.size(); indexOfFirst++) {
				if (recAnimations.get(indexOfFirst).getAnimDataSrc() == animShell) {
					Rectangle cellBounds = recAnimList.getCellBounds(indexOfFirst, indexOfFirst);
					if (cellBounds != null) {
						recAnimList.scrollRectToVisible(cellBounds);
					}
					break;
				}
			}
		}
		AnimP setSelectedDonAnims(Collection<AnimShell> donAnims){
			AnimShell firstAnimShell = donAnims.stream().findFirst().orElse(null);
			if(donAnims.size() == 1){
				setSelectedDonAnim(firstAnimShell);
			} else {
				this.donAnim = null;
				this.donAnims = null;

				boolean firstImp = firstAnimShell == null || firstAnimShell.isDoImport();
				if (donAnims.stream().anyMatch(as -> as.isDoImport() != firstImp)) {
					impBox.setIndeterminate(true);
				} else {
					impBox.setSelected(firstImp);
				}
				recRenderer.setSelectedAnims(donAnims);
				this.donAnims = donAnims;
			}
			repaint();
			return this;
		}
	}
}
