package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.application.tools.TwiRenamingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AnimEditPanel extends JPanel {

	ModelHolderThing mht;
	AnimPanel singleAnimPanel;
	AnimListCellRenderer animRenderer;
	CardLayout animCardLayout = new CardLayout();
	JPanel animPanelCards = new JPanel(animCardLayout);
	MultiAnimPanel multiAnimPanel;

	public AnimEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");

		animRenderer = new AnimListCellRenderer();
		animRenderer.setSelectedAnim(null);

		// Build the animTabs list of AnimPanels
		singleAnimPanel = new AnimPanel(mht, mht.recModAnims, animRenderer);
		multiAnimPanel = new MultiAnimPanel(mht);

		animPanelCards.add(new JPanel(), "blank");
		animPanelCards.add(singleAnimPanel, "single");
		animPanelCards.add(multiAnimPanel, "multiple");

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getAnimListPane(mht), animPanelCards);
		add(splitPane, "growx, growy");
	}

	private JScrollPane getAnimListPane(ModelHolderThing mht) {
		mht.animJList.setCellRenderer(animRenderer);
		mht.animJList.addListSelectionListener(e -> changeAnim(mht, e));
		mht.animJList.setSelectedValue(null, false);
		JScrollPane animStrollPane = new JScrollPane(mht.animJList);
		animStrollPane.setMinimumSize(new Dimension(150, 200));
		return animStrollPane;
	}

	private void changeAnim(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<AnimShell> selectedValuesList = mht.animJList.getSelectedValuesList();

			singleAnimPanel.setSelectedAnim(mht.animJList.getSelectedValue());
			if (selectedValuesList.size() < 1) {
				animRenderer.setSelectedAnim(null);
				animCardLayout.show(animPanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				animRenderer.setSelectedAnim(mht.animJList.getSelectedValue());
				singleAnimPanel.setSelectedAnim(mht.animJList.getSelectedValue());
				animCardLayout.show(animPanelCards, "single");
			} else {
				animRenderer.setSelectedAnim(null);
				multiAnimPanel.updateMultiAnimPanel(selectedValuesList);
				animCardLayout.show(animPanelCards, "multiple");
			}
		}
	}


	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0"));
//		topPanel.add(getButton("Import All", e -> mht.setImportTypeForAllAnims(AnimShell.ImportType.IMPORTBASIC)));
//		topPanel.add(getButton("Time-scale All", e -> mht.setImportTypeForAllAnims(AnimShell.ImportType.TIMESCALE)));
//		topPanel.add(getButton("Import and Rename All", e -> renameAllAnims(mht)));
//		topPanel.add(getButton("Leave All", e -> mht.setImportTypeForAllAnims(AnimShell.ImportType.DONTIMPORT)), "wrap");
//		topPanel.add(mht.clearRecModAnims, "spanx, align center");

		topPanel.add(getSetImpTypePanel(mht.receivingModel.getName(), (i) -> mht.setImportTypeForAllAnims(i)), "");
		topPanel.add(getSetImpTypePanel(mht.donatingModel.getName(), (i) -> mht.setImportTypeForAllAnims(i)), "wrap");
		topPanel.add(getButton("bulk rename animations", e -> doSomeRenaming(mht)), "wrap");
		return topPanel;
	}

	private JPanel getSetImpTypePanel(String modelName, Consumer<AnimShell.ImportType> importTypeConsumer) {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0", "[][][]", "[align center]"));
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		panel.add(getButton("Import All", e -> importTypeConsumer.accept(AnimShell.ImportType.IMPORTBASIC)), "");
		panel.add(getButton("Time-scale All", e -> importTypeConsumer.accept(AnimShell.ImportType.TIMESCALE)), "");
		panel.add(getButton("Import and Rename All", e -> importTypeConsumer.accept(AnimShell.ImportType.CHANGENAME)), "");
		panel.add(getButton("Leave All", e -> importTypeConsumer.accept(AnimShell.ImportType.DONTIMPORT)), "");

		return panel;
	}

	public JButton getButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}

	private void renameAllAnims(ModelHolderThing mht) {
		String newTagString = JOptionPane.showInputDialog(this, "Choose additional naming (i.e. swim or alternate)");

		if (newTagString != null) {
			for (AnimShell animShell : mht.allAnimShells) {
				animShell.setImportType(AnimShell.ImportType.CHANGENAME);
				String oldName = animShell.getOldName();
				String baseName = oldName;
				while ((baseName.length() > 0) && baseName.contains(" ")) {
					int lastSpaceIndex = baseName.lastIndexOf(' ');
					String lastWord = baseName.substring(lastSpaceIndex + 1);
					boolean chunkHasInt = false;
					for (int animationId = 0; animationId < 10; animationId++) {
						if (lastWord.contains(Integer.toString(animationId))) {
							chunkHasInt = true;
						}
					}
					if (lastWord.contains("-") || chunkHasInt || lastWord.toLowerCase().contains("alternate") || (lastWord.length() <= 0)) {
						baseName = baseName.substring(0, baseName.lastIndexOf(' '));
					} else {
						break;
					}
				}
				String afterBase = oldName.substring(Math.min(oldName.length(), baseName.length() + 1));
				String newName = baseName + " " + newTagString + " " + afterBase;
				animShell.setName(newName);
			}
		}
	}

	private void bulkRenameAllAnims(ModelHolderThing mht) {
		String newTagString = JOptionPane.showInputDialog(this, "Choose additional naming (i.e. swim or alternate)");

		if (newTagString != null) {
			for (AnimShell animShell : mht.allAnimShells) {
				if (animShell.getImportType() == AnimShell.ImportType.CHANGENAME) {
//					animShell.setImportType(AnimShell.ImportType.CHANGENAME);
					String oldName = animShell.getOldName();
					String baseName = oldName;
					while ((baseName.length() > 0) && baseName.contains(" ")) {
						int lastSpaceIndex = baseName.lastIndexOf(' ');
						String lastWord = baseName.substring(lastSpaceIndex + 1);
						boolean chunkHasInt = false;
						for (int animationId = 0; animationId < 10; animationId++) {
							if (lastWord.contains(Integer.toString(animationId))) {
								chunkHasInt = true;
							}
						}
						if (lastWord.contains("-") || chunkHasInt || lastWord.toLowerCase().contains("alternate") || (lastWord.length() <= 0)) {
							baseName = baseName.substring(0, baseName.lastIndexOf(' '));
						} else {
							break;
						}
					}
					String afterBase = oldName.substring(Math.min(oldName.length(), baseName.length() + 1));
					String newName = baseName + " " + newTagString + " " + afterBase;
					animShell.setName(newName);
				}
			}
		}
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
			if (animShell.getImportType() == AnimShell.ImportType.CHANGENAME) {
				animShell.setName(pattern.matcher(animShell.getOldName()).replaceAll(replaceString));
			}
		}
	}
}
