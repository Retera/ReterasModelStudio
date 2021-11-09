package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.RenameBoneAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.TwiTextArea;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class TwiRenamingPanel extends JPanel {

	private JButton doRenaming;
	private Frame mainFrame;
	private Frame helperFrame;
	private final JTextField searchField;
	private final JTextField replaceField;
	private final JCheckBox caseSensitive;
	private final JCheckBox regex;
	private final JPanel testPanel;
	private final JPanel regexPanel;
	SmartButtonGroup onlySelected;

	public TwiRenamingPanel(BiConsumer<Pattern, String> patternConsumer) {
		super(new MigLayout("fill", "", ""));
//		SmartButtonGroup onlySelected = getSmartButtonGroup();
		onlySelected = getSmartButtonGroup();

		searchField = new JTextField("", 24);
		replaceField = new JTextField("", 24);
		caseSensitive = new JCheckBox("CaSe sensitive", true);
		regex = new JCheckBox("Use RegEx");
		regex.setToolTipText("Use Regular Expression matching");

		JButton helperButton = new JButton(">");
		helperButton.addActionListener(e -> showHelperFrame(helperButton));
		add(helperButton, "spanx, align right, wrap");

		add(new JLabel("Find:"));
		add(searchField, "wrap");
		add(new JLabel("Replace:"));
		add(replaceField, "wrap");
		add(caseSensitive, "spanx 2, wrap");
		add(regex, "spanx 2, wrap");

		doRenaming = new JButton("Rename");
//		doRenaming.addActionListener(e -> doUggUgg());
//		doRenaming.addActionListener(e -> doRenaming(searchField.getText(), replaceField.getText(), caseSensitive.isSelected(), regex.isSelected(), onlySelected.getSelectedIndex() == 1));
		doRenaming.addActionListener(e -> patternConsumer.accept(getPattern(searchField.getText(), caseSensitive.isSelected(), regex.isSelected()), replaceField.getText()));
		add(doRenaming, "spanx");

		testPanel = getTestPanel();
		regexPanel = getRegExCheatsheet();

	}

	private JPanel getRegExCheatsheet() {
		JPanel regexPanel = new JPanel(new MigLayout("fill"));
		regexPanel.setBorder(BorderFactory.createTitledBorder("RegEx Cheatsheet"));
//		regexPanel.add(new JLabel("RegEx Cheatsheet"), "wrap");
		String text = "" +
				".\tany character" + "\n" +
				"?\t0 or 1 preceding character" + "\n" +
				"*\t0 or more of preceding character" + "\n" +
				"+\t1 or more of preceding character" + "\n" +
				"\\d\tany digit" + "\n" +
				"^\tstart of name" + "\n" +
				"$\tend of name" +
				"";
		TwiTextArea textArea = new TwiTextArea(text);
		regexPanel.add(new JScrollPane(textArea), "growx");
		return regexPanel;
	}

	private JPanel getTestPanel() {
		JPanel testPanel = new JPanel(new MigLayout("fill"));

		testPanel.setBorder(BorderFactory.createTitledBorder("Test find & replace"));
		JTextField textField = new JTextField("", 24);
		JTextField resultField = new JTextField("", 24);
		JButton testButton = new JButton("Test");
		testButton.addActionListener(e -> doTestReplacement(resultField, textField.getText(), searchField.getText(), replaceField.getText(), caseSensitive.isSelected(), regex.isSelected()));
		testPanel.add(new JLabel("Text:"));
		testPanel.add(textField, "wrap");
		testPanel.add(testButton, "spanx, align center, wrap");
		testPanel.add(new JLabel("Result:"));
		testPanel.add(resultField, "wrap");
		return testPanel;
	}

	private void showHelperFrame(JButton button) {
		System.out.println();
		if (helperFrame == null) {
//			initHelperFrame(null);
		}
		if (helperFrame != null) {
//			initHelperFrame(null);
			helperFrame.setVisible(!helperFrame.isVisible());
			if (helperFrame.isVisible()) {
				button.setText("<");
			} else {
				button.setText(">");
			}
		}
	}

	private void doTestReplacement(JTextField textField, String text, String find, String replace, boolean caseSensitive, boolean regex) {
		Pattern pattern = getPattern(find, caseSensitive, regex);
		textField.setText(pattern.matcher(text).replaceAll(replace));
		repaint();
	}


	private SmartButtonGroup getSmartButtonGroup() {
		SmartButtonGroup onlySelected = new SmartButtonGroup();
		onlySelected.addJRadioButton("All Nodes", null);
		onlySelected.addJRadioButton("Selected Nodes", null);
		onlySelected.setSelectedIndex(0);
		onlySelected.setButtonConst("");
		return onlySelected;
	}

	private void initHelperFrame(JFrame mainFrame) {
		helperFrame = getFollowPanel(getHelperPanel(), mainFrame);
		helperFrame.pack();
	}

	public JPanel getHelperPanel() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(testPanel, "wrap");
		panel.add(regexPanel, "wrap");
		return panel;
	}

	public static void show(JComponent parent) {
		TwiRenamingPanel animCopyPanel = new TwiRenamingPanel((p, s) -> doRenaming(null, s, true, p));
		JFrame jFrame = FramePopup.show(animCopyPanel, parent, "Rename Nodes");
		animCopyPanel.initHelperFrame(jFrame);
	}

	private void doRenaming(String searchString, String replaceString, boolean caseSensitive, boolean regex, boolean onlySelected) {
		Pattern pattern = getPattern(searchString, caseSensitive, regex);
		doRenaming(this, replaceString, onlySelected, pattern);

	}

	private static void doRenaming(Component parent, String replaceString, boolean onlySelected, Pattern pattern) {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
			ArrayList<IdObject> affectedIdObjects = new ArrayList<>();
			if (onlySelected) {
				affectedIdObjects.addAll(modelHandler.getModelView().getSelectedIdObjects());

			} else {
				affectedIdObjects.addAll(modelHandler.getModel().getIdObjects());
			}


			if (0 < affectedIdObjects.size()) {
				//String prefix = doTypePrefix ? nodeToRename.getClass().getSimpleName() + "_" : "";

//				Pattern pattern = getPattern(find, caseSensitive, regex);
//				resultLabel.setText(pattern.matcher(text).replaceAll(replace));
//				repaint();

				List<UndoAction> actions = new ArrayList<>();
				int countRenamedNodes = 0;
				for (IdObject idObject : affectedIdObjects) {
					String oldName = idObject.getName();
					String newName = pattern.matcher(oldName).replaceAll(replaceString);
					if (!oldName.equals(newName) && newName != null) {
						actions.add(new RenameBoneAction(newName, idObject));
						countRenamedNodes++;
					}
				}

				if (!actions.isEmpty()) {
					modelHandler.getUndoManager().pushAction(new CompoundAction("Rename " + countRenamedNodes + " Nodes", actions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
				}
				JOptionPane.showMessageDialog(parent, "Renamed " + countRenamedNodes + " nodes!", "Renamed Nodes", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String message = onlySelected ? "No nodes selected" : "No nodes found";
				JOptionPane.showMessageDialog(parent, message,
						"Found no nodes", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private Pattern getPattern(String searchString, boolean caseSensitive, boolean regex) {
		int flags = 0;
		flags |= !caseSensitive ? Pattern.CASE_INSENSITIVE : 0;
		if (!regex) {
//			String regexEscapes1 = "\\(\\)\\[\\]\\{\\}\\^\\|\\\\+?.*&/<";
//			String regexEscapes1 = "()[]{}^|\\+?.*&/<";
//			String regexEscapes1 = '(' + ')' + '[' + ']' + '{' + '}' + '^' + '|' + '\\' + '+' + '?' + '.' + '*' + '&' + '/' + '<';
//			char backSlash1 = 92;
			char backSlash = '\\';
			char[] regexEscapes = new char[] {'(', ')', '[', ']', '{', '}', '^', '|', '\\', '+', '?', '.', '*', '&', '/', '<'};
			StringBuilder sb = new StringBuilder();
			for (char c : regexEscapes) {
				sb.append(backSlash).append(c);
			}
//			System.out.println("regexEscapes: " + Arrays.toString(regexEscapes));
//			System.out.println("sb: " + sb);
//			System.out.println("not regex search: " + searchString);
			searchString = searchString.replaceAll("(?=[" + sb.toString() + "])", "\\\\");
//			searchString = searchString.replaceAll("(?=[" + regexEscapes1 + "])", "\\\\");
//			System.out.println("not regex search: " + searchString);
		}
		return Pattern.compile(searchString, flags);
	}


	private JFrame getFollowPanel(JPanel panel, JFrame frameToFollow) {
		JFrame frame = FramePopup.get(panel, null, "", true);
		if (frameToFollow != null) {
			frame.setLocation(frameToFollow.getX() + frameToFollow.getWidth(), frameToFollow.getY());

			frameToFollow.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					frame.setVisible(false);
					frame.dispose();
				}

				@Override
				public void windowIconified(WindowEvent e) {
					if (frame.isVisible()) {
						frame.setExtendedState(Frame.ICONIFIED);
//					System.out.println("frame x: " + frameToFollow.getX() + ", frame width: " + frameToFollow.getWidth() + ", follow x: " + frameToFollow.getX() + frameToFollow.getWidth());
					}
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
					if (frame.isVisible()) {
						frame.setExtendedState(Frame.NORMAL);
						frameToFollow.toFront();
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {
//				System.out.println("windowActivated: "
//						+ " OppositeWindow == toFollow: " + (e.getOppositeWindow() == frameToFollow)
//						+ " OppositeWindow == frame: " + (e.getOppositeWindow() == frame));
					if (frame.isVisible() && e.getOppositeWindow() != frameToFollow && e.getOppositeWindow() != frame) {
//					System.out.println("windowActivated! " + " ");
						frame.toFront();
						frameToFollow.toFront();
					}

				}
			});

			frameToFollow.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					frame.setLocation(frameToFollow.getX() + frameToFollow.getWidth(), frameToFollow.getY());
				}

				@Override
				public void componentMoved(ComponentEvent e) {
					frame.setLocation(frameToFollow.getX() + frameToFollow.getWidth(), frameToFollow.getY());
				}
			});
		}

		return frame;
	}
}
