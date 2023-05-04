package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.application.tools.TwiRenamingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AnimEditPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel panelCards = new JPanel(cardLayout);
	private final ModelHolderThing mht;
	private final AnimPanel singleAnimPanel;
	private final AnimMultiPanel animMultiPanel;
	private final AnimListCellRenderer animRenderer;
	private final TwiList<AnimShell> animJList;

	public AnimEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;
		animJList = new TwiList<>(mht.allAnimShells);

		JPanel topPanel = getTopPanel();
		add(topPanel, "align center, wrap");
		System.out.println("parent11: " + getParent());
		System.out.println("rotepane11: " + getRootPane());
		System.out.println("anc11: " + getTopLevelAncestor());
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				if(AnimEditPanel.this.getTopLevelAncestor() != null && AnimEditPanel.this.getTopLevelAncestor().getWidth() < topPanel.getComponent(0).getWidth()*2){
					((MigLayout)topPanel.getLayout()).setLayoutConstraints("gap 0, ins 0, wrap 1");
					((MigLayout)AnimEditPanel.this.getLayout()).setComponentConstraints(topPanel, "align left, wrap");
				} else {
					((MigLayout)topPanel.getLayout()).setLayoutConstraints("gap 0, ins 0, wrap 2");
					((MigLayout)AnimEditPanel.this.getLayout()).setComponentConstraints(topPanel, "align center, wrap");
				}
				topPanel.revalidate();
			}
		});


		animRenderer = new AnimListCellRenderer().setMarkDontImp(true);
		animRenderer.setSelectedAnim(null);

		// Build the animTabs list of AnimPanels
		singleAnimPanel = new AnimPanel(mht, animJList, animRenderer);
		animMultiPanel = new AnimMultiPanel(mht, animJList);

		panelCards.add(new JPanel(), "blank");
		panelCards.add(singleAnimPanel, "single");
		panelCards.add(animMultiPanel, "multiple");
		JScrollPane cardScrollPane = new JScrollPane(panelCards);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getAnimListPane(mht), cardScrollPane);
		add(splitPane, "wrap, growx, growy, spany");
		splitPane.setDividerLocation(.3);
	}

	private JScrollPane getAnimListPane(ModelHolderThing mht) {
		animJList.setCellRenderer(animRenderer);
		animJList.addMultiSelectionListener(this::changeAnim);
		animJList.setSelectedValue(null, false);
		return new JScrollPane(animJList);
	}

	private void changeAnim(Collection<AnimShell> selectedValuesList) {
		if (selectedValuesList.size() < 1) {
			showCard("blank");
		} else if (selectedValuesList.size() == 1) {
			singleAnimPanel.setSelectedAnim(((List<AnimShell>)selectedValuesList).get(0));
			showCard("single");
		} else {
			animMultiPanel.updateMultiAnimPanel((List<AnimShell>)selectedValuesList);
			showCard("multiple");
		}
	}

	private void showCard(String name){
		cardLayout.show(panelCards, name);
	}


	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0"));
		topPanel.add(getSetImpTypePanel(false), "");
		topPanel.add(getSetImpTypePanel(true), "wrap");
		topPanel.add(Button.create("bulk rename animations", e -> doSomeRenaming(mht)), "");
		return topPanel;
	}

	private JPanel getSetImpTypePanel(String modelName, Consumer<AnimShell.ImportType> importTypeConsumer) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(Button.create("Import All", e -> importTypeConsumer.accept(AnimShell.ImportType.IMPORT_BASIC)), "");
		panel.add(Button.create("Time-scale All", e -> importTypeConsumer.accept(AnimShell.ImportType.TIMESCALE_INTO)), "");
		panel.add(Button.create("Import and Rename All", e -> importTypeConsumer.accept(AnimShell.ImportType.CHANGE_NAME)), "");
		panel.add(Button.create("Leave All", e -> importTypeConsumer.accept(AnimShell.ImportType.DONT_IMPORT)), "");
		panel.add(Button.create("auto match animations", e -> matchAnimsByName(true)), "wrap");

		return panel;
	}
	private JPanel getSetImpTypePanel(boolean fromDonating) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		String modelName = fromDonating ? mht.donatingModel.getName() : mht.receivingModel.getName();
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		String matchTip = "Set animaions to use in these animations; Matches by name.";
		String clearTip = "Clear all animations to use in these animations";

		panel.add(Button.create("Import All", e -> setImportAnims(true, fromDonating)), "");
		panel.add(Button.create("Leave All", e -> setImportAnims(false, fromDonating)), "");
		panel.add(setTooltip(Button.create("Auto Match Sources", e -> matchAnimsByName(fromDonating)), matchTip), "");
		panel.add(setTooltip(Button.create("Clear Sources", e -> clearAnimsSources(fromDonating)), clearTip), "");

		return panel;
	}

	private JComponent setTooltip(JComponent comp, String tip){
		comp.setToolTipText(tip);
		return comp;
	}

	private void setImportAnims(boolean imp, boolean donMod){
		List<AnimShell> animShells = donMod ? mht.donModAnims : mht.recModAnims;
		animShells.forEach(a -> a.setDoImport(imp));
		repaint();
	}


	private void doSomeRenaming(ModelHolderThing mht) {
		TwiRenamingPanel renamingPanel = new TwiRenamingPanel((p, s) -> doSomeRenaming(mht, p, s));
		JPanel panel = new JPanel(new MigLayout("fill, gap 0"));
//		panel.setPreferredSize(new Dimension(700, 300));
//		panel.add(renamingPanel, "w 350, h 300");
//		panel.add(renamingPanel.getHelperPanel(), "w 350, h 300");
		panel.add(renamingPanel, "");
		panel.add(renamingPanel.getHelperPanel(), "");
		JOptionPane.showMessageDialog(this, panel, "rename animations", JOptionPane.PLAIN_MESSAGE);
	}

	private void doSomeRenaming(ModelHolderThing mht, Pattern pattern, String replaceString) {
		for (AnimShell animShell : mht.allAnimShells) {
			if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
				animShell.setName(pattern.matcher(animShell.getOldName()).replaceAll(replaceString));
			}
		}
	}

	private void matchAnimsByName(boolean fromDonating) {
		List<AnimShell> destAnims = fromDonating ? mht.donModAnims : mht.recModAnims;
		List<AnimShell> srcAnims = fromDonating ? mht.recModAnims : mht.donModAnims;
		for (AnimShell destAnimShell : destAnims) {
			for (AnimShell srcAnimShell : srcAnims) {
				if (destAnimShell.getName().equals(srcAnimShell.getName())) {
					destAnimShell.setAnimDataSrc(srcAnimShell);
					break;
				} else if (destAnimShell.getName().startsWith(srcAnimShell.getName().split(" ")[0])) {
					if (destAnimShell.getAnimDataSrcAnim() == null) {
						destAnimShell.setAnimDataSrc(srcAnimShell);
					} else {
						int orgLength = destAnimShell.getAnim().getLength();
						int lengthDiffCurr = Math.abs(destAnimShell.getAnimDataSrcAnim().getLength() - orgLength);
						int lengthDiffNew = Math.abs(srcAnimShell.getAnim().getLength() - orgLength);
						if (lengthDiffNew < lengthDiffCurr) {
							destAnimShell.setAnimDataSrc(srcAnimShell);
						}
					}
				}
			}
		}
		animJList.repaint();
	}
	private void clearAnimsSources(boolean fromDonating) {
		List<AnimShell> destAnims = fromDonating ? mht.donModAnims : mht.recModAnims;
		for (AnimShell destAnimShell : destAnims) {
			destAnimShell.setAnimDataSrc(null);
		}
		animJList.repaint();
	}
}
