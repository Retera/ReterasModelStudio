package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

class AnimPanel extends JPanel {

	protected JLabel title;
	private JLabel animInfo;
	protected JCheckBox inReverse;

	private final JComboBox<String> importTypeBox = new JComboBox<>(AnimShell.ImportType.getDispList());

	private final JPanel cardPane = new JPanel();

	private JPanel blankCardImp = new JPanel();
	private JPanel dontImport = new JPanel();
	private JPanel doImport = new JPanel();
	private JPanel blankCardGS = new JPanel();

	private JLabel dontImportL = new JLabel();
	private JLabel doImportL = new JLabel();
	private JLabel timeScaleInfo = new JLabel("All bones set to receive motion will have the animation data of the following animation(s) replaced by this animation");
	private String reciveText = "All bones set to receive motion will have the animation data of this animation replaced by: ";
	private JLabel timeScaleRecInfo = new JLabel(reciveText);


	private final JTextField newNameEntry = new JTextField("", 40);

	private IterableListModel<AnimShell> possibleAnimDataDests;
	private IterableListModel<AnimShell> sortedAnimDataDests;
	private JList<AnimShell> animDataDestJList;

	private IterableListModel<AnimShell> sortedAnimDataSrcs;
	private JList<AnimShell> animDataSrcJList;

	private ModelHolderThing mht;

	protected AnimShell selectedAnim;
	protected JList<AnimShell> animJList;
	private AnimListCellRenderer animRenderer;

	private final CardLayout animCardLayout = new CardLayout();

	public AnimPanel() {
	}

	public AnimPanel(ModelHolderThing mht, JList<AnimShell> animJList, final AnimListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, fill", "[]", "[][][][][grow]"));
		this.animJList = animJList;
		animRenderer = renderer;
		this.possibleAnimDataDests = mht.recModAnims;
		sortedAnimDataDests = new IterableListModel<>(mht.recModAnims);
		sortedAnimDataSrcs = new IterableListModel<>(mht.recModAnims);

		title = new JLabel("Select an Animation");
		title.setFont(new Font("Arial", Font.BOLD, 20));

		animInfo = new JLabel("");

		add(title, "align center, spanx, wrap");
		add(animInfo, "align center, spanx, wrap");

		add(getReverseCheckBox(), "left, wrap");
		add(getImportTypeBox(), "wrap");

		JPanel nameCard = new JPanel(new MigLayout("ins 0, fill", "", "[][grow]"));
		nameCard.add(newNameEntry, "wrap");
//		nameCard.add(new JPanel(new MigLayout("fill")), "growx, growy");

		JPanel blankCard = new JPanel(new MigLayout("fill", "", "[][grow]"));
//		blankCard.add(new JPanel(new MigLayout("fill")), "growx, growy");

		cardPane.setLayout(animCardLayout);
//		cardPane.add(dontImport, AnimShell.ImportType.DONTIMPORT.getDispText());
//		cardPane.add(getPanelWithText("This animation will not be imported/will be removed"), AnimShell.ImportType.DONTIMPORT.getDispText());
		cardPane.add(blankCard, "blank");
		cardPane.add(getPanelWithLabel(dontImportL), AnimShell.ImportType.DONT_IMPORT.getDispText());
//		cardPane.add(doImport, AnimShell.ImportType.IMPORTBASIC.getDispText());
//		cardPane.add(doImport, AnimShell.ImportType.IMPORTBASIC.getDispText());
		cardPane.add(getPanelWithLabel(doImportL), AnimShell.ImportType.IMPORT_BASIC.getDispText());
		cardPane.add(nameCard, AnimShell.ImportType.CHANGE_NAME.getDispText());
//		cardPane.add(newNameEntry, AnimShell.ImportType.CHANGE_NAME.getDispText());
		cardPane.add(getTimeScaleIntoPane(), AnimShell.ImportType.TIMESCALE_INTO.getDispText());
		cardPane.add(getTimeScaleRecPane(), AnimShell.ImportType.TIMESCALE_RECEIVE.getDispText());
//		cardPane.add(blankCardGS, AnimShell.ImportType.GLOBALSEQ.getDispText());
		cardPane.add(getPanelWithText(""), AnimShell.ImportType.GLOBALSEQ.getDispText());
//		animCardLayout.invalidateLayout(cardPane);
		animCardLayout.show(cardPane, "blank");
		add(cardPane, "growx, growy");
	}

	private JCheckBox getReverseCheckBox() {
		inReverse = new JCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addActionListener(e -> setInReverse());
		inReverse.setEnabled(false);
		return inReverse;
	}

	private JComboBox<String> getImportTypeBox() {
		importTypeBox.setEditable(false);
		importTypeBox.addItemListener(this::showCorrectCard);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		importTypeBox.setEnabled(false);
		return importTypeBox;
	}

	private JPanel getTimeScaleIntoPane() {
		animDataDestJList = new JList<>(sortedAnimDataDests);
		animDataDestJList.setCellRenderer(animRenderer);
//		animDataDestJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		animDataDestJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		animDataDestJList.addListSelectionListener(this::selectAnimDataDest);
		animDataDestJList.setSelectedValue(null, false);
		JScrollPane animListPane = new JScrollPane(animDataDestJList);
		JPanel timescaleIntoPanel = new JPanel(new MigLayout("ins 0, fill", "", "[][grow]"));
		timescaleIntoPanel.add(timeScaleInfo, "wrap");
		timescaleIntoPanel.add(animListPane, "growx, growy");
		return timescaleIntoPanel;
	}

	private JPanel getTimeScaleRecPane() {
		animDataSrcJList = new JList<>(sortedAnimDataSrcs);
		animDataSrcJList.setCellRenderer(animRenderer);
		animDataSrcJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		animDataSrcJList.addListSelectionListener(this::selectAnimDataSrc);
		animDataSrcJList.setSelectedValue(null, false);
		JScrollPane animListPane = new JScrollPane(animDataSrcJList);
		JPanel timescaleRecPanel = new JPanel(new MigLayout("ins 0, fill", "", "[][grow]"));
		timescaleRecPanel.add(timeScaleRecInfo, "wrap");
		timescaleRecPanel.add(animListPane, "growx, growy");
		return timescaleRecPanel;
	}

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnim = animShell;
		animRenderer.setSelectedAnim(selectedAnim);
		updateRecModAnimList();
		updateAnimDataSrcList();
		title.setText(animShell.getName());
		animInfo.setText(getInfoText());
		dontImportL.setText(animShell.isFromDonating() ? "This animation will not be imported" : "This animation will be removed");
		doImportL.setText(animShell.isFromDonating() ? "This animation will be imported" : "This animation will remain");
		newNameEntry.setText(animShell.getName());
		importTypeBox.setEnabled(true);
		importTypeBox.setSelectedIndex(animShell.getImportType().ordinal());
//		if(animShell.getImportAnimShell() != null){
//			timeScaleRecInfo.setText(reciveText + animShell.getImportAnimShell().getName());
//		} else {
//
//		}
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

	private JPanel getPanelWithText(String... strings) {
		JPanel panel = new JPanel(new MigLayout("fill", "", "[][grow]"));
		for (String string : strings) {
			panel.add(new JLabel(string), "wrap");
		}
		panel.add(new JPanel(new MigLayout("fill")), "growx, growy");

		return panel;
	}

	private JPanel getPanelWithLabel(JLabel label) {
		JPanel panel = new JPanel(new MigLayout("fill", "", "[][grow]"));
		panel.add(label, "wrap");
//		panel.add(new JPanel(new MigLayout("fill")), "growx, growy");

		return panel;
	}

	private void setInReverse() {
		selectedAnim.setReverse(inReverse.isSelected());
	}

	private void showCorrectCard(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			animCardLayout.show(cardPane, (String) e.getItem());
			System.out.println("StateChange: " + e.getStateChange() + ", selected Index: " + importTypeBox.getSelectedIndex());
			selectedAnim.setImportType(importTypeBox.getSelectedIndex());
			inReverse.setEnabled(selectedAnim.getImportType() != AnimShell.ImportType.DONT_IMPORT);
			updateRecModAnimList();
			updateAnimDataSrcList();
		}
	}

	private void updateRecModAnimList() {
		sortedAnimDataDests.clear();
		List<AnimShell> usedAnims = new ArrayList<>();

		for (AnimShell as : possibleAnimDataDests) {
			if (as.getAnimDataSrc() == null) {
				sortedAnimDataDests.addElement(as);
			} else if (as.getAnimDataSrc() == selectedAnim) {
				sortedAnimDataDests.add(0, as);
			} else if (as != selectedAnim) {
				usedAnims.add(as);
			}
		}
		sortedAnimDataDests.addAll(usedAnims);
	}

	private void updateAnimDataSrcList() {
		sortedAnimDataSrcs.clear();
		List<AnimShell> impAnims = new ArrayList<>();
		List<AnimShell> dontImpAnims = new ArrayList<>();
		List<AnimShell> timeScaleRecAnims = new ArrayList<>();
		List<AnimShell> glSeqAnims = new ArrayList<>();
		List<AnimShell> chNameAnims = new ArrayList<>();

		for (AnimShell as : possibleAnimDataDests) {
			if ((as != selectedAnim)) {
				if (as.getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
					if (selectedAnim.getAnimDataSrc() == as) {
						sortedAnimDataSrcs.add(0, as);
					} else {
						sortedAnimDataSrcs.addElement(as);
					}
				} else if (as.getImportType() == AnimShell.ImportType.TIMESCALE_RECEIVE) {
					timeScaleRecAnims.add(as);
				} else if (as.getImportType() == AnimShell.ImportType.IMPORT_BASIC) {
					impAnims.add(as);
				} else if (as.getImportType() == AnimShell.ImportType.DONT_IMPORT) {
					dontImpAnims.add(as);
				} else if (as.getImportType() == AnimShell.ImportType.GLOBALSEQ) {
					glSeqAnims.add(as);
				} else if (as.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
					chNameAnims.add(as);
				}
			}
		}
		sortedAnimDataSrcs.addAll(impAnims);
		sortedAnimDataSrcs.addAll(dontImpAnims);
		sortedAnimDataSrcs.addAll(chNameAnims);
		sortedAnimDataSrcs.addAll(timeScaleRecAnims);
		sortedAnimDataSrcs.addAll(glSeqAnims);
	}

	private void selectAnimDataDest(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			for (AnimShell animShell : animDataDestJList.getSelectedValuesList()) {
				if (animShell.getAnimDataSrc() == selectedAnim) {
					animShell.setAnimDataSrc(null);
				} else {
					animShell.setAnimDataSrc(selectedAnim);
				}
			}
			animDataDestJList.setSelectedValue(null, false);
			animJList.repaint();
		}
	}

	private void selectAnimDataSrc(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			for (AnimShell animShell : animDataSrcJList.getSelectedValuesList()) {
				if (selectedAnim.getAnimDataSrc() == animShell) {
					selectedAnim.setAnimDataSrc(null);
				} else {
					selectedAnim.setAnimDataSrc(animShell);
				}
			}
			animDataSrcJList.setSelectedValue(null, false);
			animJList.repaint();
		}
	}
}
