package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.application.tools.TwiRenamingPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AnimEditPanel extends JPanel {

	private ModelHolderThing mht;
	private AnimPanel singleAnimPanel;
	private AnimListCellRenderer animRenderer;
	private CardLayout animCardLayout = new CardLayout();
	private JPanel animPanelCards = new JPanel(animCardLayout);
	private MultiAnimPanel multiAnimPanel;
	private JList<AnimShell> animJList;

	public AnimEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;
		animJList = new JList<>(mht.allAnimShells);

		JPanel topPanel = getTopPanel();
		add(topPanel, "align center, wrap");
		System.out.println("parent11: " + getParent());
		System.out.println("rotepane11: " + getRootPane());
		System.out.println("anc11: " + getTopLevelAncestor());
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				if(AnimEditPanel.this.getTopLevelAncestor() != null && AnimEditPanel.this.getTopLevelAncestor().getWidth()<topPanel.getComponent(0).getWidth()*2){
					((MigLayout)topPanel.getLayout()).setLayoutConstraints("gap 0, ins 0 n n n, wrap 1");
					((MigLayout)AnimEditPanel.this.getLayout()).setComponentConstraints(topPanel, "align left, wrap");
				} else {
					((MigLayout)topPanel.getLayout()).setLayoutConstraints("gap 0, ins 0 n n n, wrap 2");
					((MigLayout)AnimEditPanel.this.getLayout()).setComponentConstraints(topPanel, "align center, wrap");
				}
				topPanel.revalidate();
			}
		});


		animRenderer = new AnimListCellRenderer();
		animRenderer.setSelectedAnim(null);

		// Build the animTabs list of AnimPanels
		singleAnimPanel = new AnimPanel(mht, animJList, animRenderer);
		multiAnimPanel = new MultiAnimPanel(mht);

		animPanelCards.add(new JPanel(), "blank");
		animPanelCards.add(singleAnimPanel, "single");
		animPanelCards.add(multiAnimPanel, "multiple");
		JScrollPane cardScrollPane = new JScrollPane(animPanelCards);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getAnimListPane(mht), cardScrollPane);
		add(splitPane, "growx, growy");
		splitPane.setDividerLocation(.3);
	}

	private JScrollPane getAnimListPane(ModelHolderThing mht) {
		animJList.setCellRenderer(animRenderer);
		animJList.addListSelectionListener(e -> changeAnim(mht, e));
		animJList.setSelectedValue(null, false);
		JScrollPane animStrollPane = new JScrollPane(animJList);
//		animStrollPane.setMinimumSize(new Dimension(150, 200));
		return animStrollPane;
	}

	private void changeAnim(ModelHolderThing mht, ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			List<AnimShell> selectedValuesList = animJList.getSelectedValuesList();

			singleAnimPanel.setSelectedAnim(animJList.getSelectedValue());
			if (selectedValuesList.size() < 1) {
				animRenderer.setSelectedAnim(null);
				animCardLayout.show(animPanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				animRenderer.setSelectedAnim(animJList.getSelectedValue());
				singleAnimPanel.setSelectedAnim(animJList.getSelectedValue());
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
		topPanel.add(getSetImpTypePanel(mht.receivingModel.getName(), (i) -> mht.setImportTypeForAllRecAnims(i)), "");
		topPanel.add(getSetImpTypePanel(mht.donatingModel.getName(), (i) -> mht.setImportTypeForAllDonAnims(i)), "wrap");
		topPanel.add(Button.create("bulk rename animations", e -> doSomeRenaming(mht)), "");
		topPanel.add(Button.create("auto match animations", e -> matchAnimsByName()), "wrap");
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

		return panel;
	}

	private void renameAllAnims(ModelHolderThing mht) {
		String newTagString = JOptionPane.showInputDialog(this, "Choose additional naming (i.e. swim or alternate)");

		if (newTagString != null) {
			for (AnimShell animShell : mht.allAnimShells) {
				animShell.setImportType(AnimShell.ImportType.CHANGE_NAME);
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
				if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
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
			if (animShell.getImportType() == AnimShell.ImportType.CHANGE_NAME) {
				animShell.setName(pattern.matcher(animShell.getOldName()).replaceAll(replaceString));
			}
		}
	}

	private void matchAnimsByName() {
		for (AnimShell recAnimShell : mht.recModAnims) {
			for (AnimShell donAnimShell : mht.donModAnims) {
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
		animJList.repaint();
	}

	private void matchAnimsByName(IterableListModel<AnimShell> animsToKeep, IterableListModel<AnimShell> recModAnims) {
		for (AnimShell recAnimShell : mht.recModAnims) {
			for (AnimShell donAnimShell : mht.donModAnims) {
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
		animJList.repaint();
	}
}
