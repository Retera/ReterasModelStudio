package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;
import java.util.*;

public class AnimationMappingPanel extends JPanel {

	Animation prototypeAnim = new Animation("An Extra Empty Animation", 0, 1000);
	AnimShell prototypeAnimShell = new AnimShell(prototypeAnim);

	AnimShell asNewAnim = new AnimShell(new Animation("as New Anim", 0, 1));

	AnimListCellRenderer donRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	List<AnimShell> donAnimations = new ArrayList<>();
	TwiList<AnimShell> donAnimList = new TwiList<>(donAnimations);

	AnimListCellRenderer recRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	List<AnimShell> recAnimations = new ArrayList<>();
	TwiList<AnimShell> recAnimList = new TwiList<>(recAnimations);



	public AnimationMappingPanel(List<Animation> donAnims, List<Animation> recAnims){
		super(new MigLayout("ins 0, fill, wrap 2", "[sgx anim][sgx anim]", "[][grow][]"));

		donAnimList.setPrototypeCellValue(prototypeAnimShell);
		recAnimList.setPrototypeCellValue(prototypeAnimShell);

		JButton autoMatchAnimations = new JButton("Auto match animations");
		autoMatchAnimations.addActionListener(e -> matchAnimsByName());
		add(autoMatchAnimations, "wrap");
		add(new JLabel("Map motion from:"), "");
		add(new JLabel("Into existing animation:"), "wrap");

		add(new JScrollPane(donAnimList), "growy, growx");
		add(new JScrollPane(recAnimList), "growy, growx");

//		animMapPanel.add(new JScrollPane(donAnimList), "growy, growx, gpy 200");
//		animMapPanel.add(new JScrollPane(recAnimList), "growy, growx, gpy 200, wrap");
		fillLists(donAnims, recAnims);
		donAnimList.addListSelectionListener(this::donAnimationSelectionChanged);
		recAnimList.addListSelectionListener(this::recAnimationSelectionChanged);

	}

	public List<AnimShell> getDonAnimations() {
		return donAnimations;
	}

	protected Map<Sequence, Sequence> getRecToDonSequenceMap() {
		Map<Sequence, Sequence> recToDonSequenceMap = new HashMap<>(); // receiving animations to donating animations
//		for (AnimShell animShell : recAnimations) {
//			if (animShell.getAnimDataSrc() != null && animShell.getAnimDataSrc().getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
//				if (animShell == asNewAnim) {
//					recToDonSequenceMap.put(animShell.getAnimDataSrc().getAnim().deepCopy(), animShell.getAnimDataSrc().getAnim());
//				} else {
//					recToDonSequenceMap.put(animShell.getAnim(), animShell.getAnimDataSrc().getAnim());
//				}
//			}
//		}
		for (AnimShell animShell : donAnimations) {
			if (animShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
				recToDonSequenceMap.put(animShell.getAnim().deepCopy(), animShell.getAnim());
			} else if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO){
				List<AnimShell> animDataDests = animShell.getAnimDataDests();
				for (AnimShell animDataDest : animDataDests){
					recToDonSequenceMap.put(animDataDest.getAnim(), animShell.getAnim());
				}
			}
		}
		return recToDonSequenceMap;
	}

	protected void fillLists(List<Animation> donAnims, List<Animation> recAnims) {
		for (Animation animation : donAnims) {
			AnimShell animShell = new AnimShell(animation);
			animShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
			donAnimations.add(animShell);
		}
		donAnimList.setCellRenderer(donRenderer);

		recAnimations.add(asNewAnim);
		for (Animation animation : recAnims) {
			recAnimations.add(new AnimShell(animation));
		}
		recAnimList.setCellRenderer(recRenderer);
	}


	private void donAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() && donAnimList.getSelectedValue() != null) {
			recAnimList.setSelectedValue(null, false);
			scrollToRevealFirstChosen(donAnimList.getSelectedValue());
			recRenderer.setSelectedAnim(donAnimList.getSelectedValue());
		}
	}

	private void scrollToRevealFirstChosen(AnimShell animShell) {
		if (animShell.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
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
	}

	// todo make use of "animShellsToTimeScaleInto" in AnimShell to facilitate
	//  importing into animation and importing animation at the same time
	private void recAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			AnimShell donAnimShell = donAnimList.getSelectedValue();

			Set<AnimShell> donAnimsToCheck = new HashSet<>();
			for (AnimShell as : recAnimList.getSelectedValuesList()) {
				if (as == asNewAnim && recAnimList.getSelectedValuesList().size() == 1) {
					if (donAnimShell.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
						donAnimShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
					} else {
						donAnimShell.setImportType(AnimShell.ImportType.IMPORT_BASIC);
					}
				} else if (as != asNewAnim){
					if (donAnimShell.getAnimDataDests().contains(as)) {
						donAnimShell.removeAnimDataDest(as);

					} else {
						donAnimShell.addAnimDataDest(as);
					}
				}
			}
			recAnimList.setSelectedValue(null, false);

			fixImportType(donAnimsToCheck);
		}
	}




	private void matchAnimsByName() {
		Set<AnimShell> donAnimsToCheck = new HashSet<>();
		for (AnimShell recAnimShell : recAnimations) {
			for (AnimShell donAnimShell : donAnimations) {
				if (recAnimShell.getName().equals(donAnimShell.getName())) {
					recAnimShell.setAnimDataSrc(donAnimShell);
					donAnimsToCheck.add(donAnimShell);
					donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
					break;
				} else if (recAnimShell.getName().startsWith(donAnimShell.getName().split(" ")[0])) {
					if (recAnimShell.getAnimDataSrcAnim() == null) {
						recAnimShell.setAnimDataSrc(donAnimShell);
						donAnimsToCheck.add(donAnimShell);
						donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
					} else {
						int orgLength = recAnimShell.getAnim().getLength();
						int lengthDiffCurr = Math.abs(recAnimShell.getAnimDataSrcAnim().getLength() - orgLength);
						int lengthDiffNew = Math.abs(donAnimShell.getAnim().getLength() - orgLength);
						if (lengthDiffNew < lengthDiffCurr) {
							donAnimsToCheck.add(recAnimShell.getAnimDataSrc());
							recAnimShell.setAnimDataSrc(donAnimShell);
							donAnimsToCheck.add(donAnimShell);
							donAnimShell.setImportType(AnimShell.ImportType.TIMESCALE_INTO);
						}
					}
				}
			}
		}
		fixImportType(donAnimsToCheck);
		recAnimList.repaint();

	}



	private void fixImportType(Set<AnimShell> donAnimsToCheck) {
		for (AnimShell animShell : donAnimsToCheck) {
			boolean isImp = false;
			for (AnimShell as : recAnimations) {
				isImp = as.getAnimDataSrc() == animShell;
				if (isImp) {
					break;
				}
			}
			if (!isImp) {
				animShell.setImportType(AnimShell.ImportType.DONT_IMPORT);
			}
		}
		donAnimList.repaint();
	}
}
