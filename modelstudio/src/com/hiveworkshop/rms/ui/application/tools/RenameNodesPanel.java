package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.RenameBoneAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
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
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class RenameNodesPanel extends JPanel {

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

	public RenameNodesPanel() {
		super(new MigLayout("fill", "", ""));
//		SmartButtonGroup onlySelected = getSmartButtonGroup();
		onlySelected = getSmartButtonGroup();

		searchField = new JTextField("" , 24);
		replaceField = new JTextField("", 24);
		caseSensitive = new JCheckBox("CaSe sensitive", true);
		regex = new JCheckBox("Use RegEx");
		regex.setToolTipText("Use Regular Expression matching");

		add(onlySelected.getButtonPanel(), "spanx, split 2, growx");
		JButton helperButton = new JButton(">");
		helperButton.addActionListener(e -> showHelperFrame(helperButton));
		add(helperButton, "align right, wrap");

		add(new JLabel("Find:"));
		add(searchField, "wrap");
		add(new JLabel("Replace:"));
		add(replaceField, "wrap");
		add(caseSensitive, "spanx 2, wrap");
		add(regex, "spanx 2, wrap");

		doRenaming = new JButton("Rename");
//		doRenaming.addActionListener(e -> doUggUgg());
		doRenaming.addActionListener(e -> doRenaming(searchField.getText(), replaceField.getText(), caseSensitive.isSelected(), regex.isSelected(), onlySelected.getSelectedIndex() == 1));
		add(doRenaming, "spanx");

		testPanel = getTestPanel();
		regexPanel = getRegExCheatsheet();

	}

	private JPanel getRegExCheatsheet(){
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
//		TwiEditorPane editorPane = new TwiEditorPane(text, true);
//		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
//		textArea.setFont(font);
//		textArea.setTabSize(3);

//		AttributeSet smas = StyleContext.getDefaultStyleContext().getEmptySet();
//		StyleContext.getDefaultStyleContext().getStyle(StyleContext.);

//		JTextPane ugg = new JTextPane();
//		StyledDocument styledDocument = ugg.getStyledDocument();


//		Style regular = document.addStyle("regular", def);
//		StyleConstants.setFontFamily(def, "SansSerif");
//
//		Style s = document.addStyle("italic", regular);
//		StyleConstants.setItalic(s, true);
		regexPanel.add(textArea, "growx");
//		regexPanel.add(editorPane.getEditorPane(), "growx");
		return regexPanel;
	}

	private JPanel getTestPanel(){
		JPanel testPanel = new JPanel(new MigLayout("fill"));

		testPanel.setBorder(BorderFactory.createTitledBorder("Test find & replace"));
//		JTextField textField = new JTextField("baba_(2)_nana_baba_(2)_baba_(3)_baba_(23)", 24);
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

	private void showHelperFrame(JButton button){
		System.out.println();
		helperFrame.setVisible(!helperFrame.isVisible());
		if(helperFrame.isVisible()){
			button.setText("<");
		} else {
			button.setText(">");
		}
	}

	private void doUggUgg(){
		System.out.println("onlySelected.index: " + onlySelected.getSelectedIndex());
		repaint();
	}
	private void doTestReplacement(JTextField textField, String text, String find, String replace, boolean caseSensitive, boolean regex){
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
		JPanel panel = getHelperPanel();
		helperFrame = getFollowPanel(panel, mainFrame);
		helperFrame.pack();
	}

	private JPanel getHelperPanel() {
		JPanel panel = new JPanel(new MigLayout());
		panel.add(testPanel, "wrap");
		panel.add(regexPanel, "wrap");
		return panel;
	}

	public static void show(JComponent parent) {
		RenameNodesPanel animCopyPanel = new RenameNodesPanel();
		JFrame jFrame = FramePopup.show(animCopyPanel, parent, "Rename Nodes");
		animCopyPanel.initHelperFrame(jFrame);
	}

	private void doRenaming(String searchString, String replaceString, boolean caseSensitive, boolean regex, boolean onlySelected) {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
			ArrayList<IdObject> affectedIdObjects = new ArrayList<>();
			System.out.println("only selected: " + onlySelected + ", index: " + this.onlySelected.getSelectedIndex());
			if(onlySelected){
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
				Pattern pattern = getPattern(searchString, caseSensitive, regex);
				for (IdObject idObject : affectedIdObjects) {
					String oldName = idObject.getName();
//					String newName = getNewName(searchString, replaceString, caseSensitive, regex, oldName, pattern);
//					String newName = getNewName(searchString, replaceString, caseSensitive, regex, idObject);
					String newName = pattern.matcher(oldName).replaceAll(replaceString);
					if(!oldName.equals(newName) && newName != null){
						actions.add(new RenameBoneAction(newName, idObject));
						countRenamedNodes++;
					}
				}

				if (!actions.isEmpty()){
					modelHandler.getUndoManager().pushAction(new CompoundAction("Rename " + countRenamedNodes + " Nodes", actions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
				}
				JOptionPane.showMessageDialog(this, "Renamed " + countRenamedNodes + " nodes!", "Renamed Nodes", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String message = onlySelected ? "No nodes selected" : "No nodes found";
				JOptionPane.showMessageDialog(this, message,
						"Found no nodes", JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}

	private Pattern getPattern(String searchString, boolean caseSensitive, boolean regex) {
		int flags = 0;
		flags |= !caseSensitive ? Pattern.CASE_INSENSITIVE : 0;
		if(!regex) {
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

	private String getNewName(String searchString, String replaceString, boolean caseSensitive, boolean regex, IdObject idObject) {
		String newName = null;
		if(idObject.getName().equals(searchString) || caseSensitive || regex){

			newName = replaceString;
		}
		return newName;
	}
	private String getNewName(String searchString, String replaceString, boolean caseSensitive, boolean regex, String oldName,Pattern pattern) {
		String newName = pattern.matcher(oldName).replaceAll(replaceString);

		return newName;
	}

	private String getIndexString(int key) {
		List<Integer> indexes2 = new ArrayList<>();
		for (int tempKey = key; tempKey > 0; ) {
			int subKey = tempKey % 300;
			if (subKey != 0) {
				indexes2.add(subKey);
				tempKey -= subKey;
			} else {
				tempKey /= 300;
			}
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = indexes2.size() - 1; i >= 0; i--) {
			stringBuilder.append("_").append(indexes2.get(i));
		}
		return stringBuilder.toString();

	}


	private void doRenaming(Bone bone, ModelHandler modelHandler, String name, String subfix, boolean doTypePrefix, int depthLimit) {
		Map<Integer, List<Bone>> depthMap = new HashMap<>();
		fillDepthMap(bone, depthLimit, 0, depthMap);

		List<UndoAction> actions = new ArrayList<>();
		int depthStringSize = ("" + depthMap.size()).length();
		int countRenamedBones = 0;
		for (int depth : depthMap.keySet()) {
			List<Bone> bones = depthMap.get(depth);
			int siblingStringSize = ("" + bones.size()).length();
			for (int i = 0; i <= bones.size(); i++) {
				Bone nodeToRename = bones.get(i);
				String prefix = doTypePrefix ? nodeToRename.getClass().getName() + " " : "";
				String depthString = ("0000" + depth).substring(4 - depthStringSize, 4);
				String siblingString = bones.size() > 1 ? ("0000" + i).substring(4 - siblingStringSize, 4) : "";
				String newName = prefix + name + " " + depthString + siblingString + subfix;
				actions.add(new RenameBoneAction(newName, nodeToRename));
				countRenamedBones++;
			}
		}

		modelHandler.getUndoManager().pushAction(new CompoundAction("Rename Bone Chain", actions, ModelStructureChangeListener.changeListener::nodesUpdated));
		JOptionPane.showMessageDialog(this, "Renamed " + countRenamedBones + " bones/helpers!", "Renamed Bones", JOptionPane.INFORMATION_MESSAGE);
	}

	private void fillDepthMap(Bone bone, int depthLimit, int currDepth, Map<Integer, List<Bone>> depthMap) {
		depthMap.computeIfAbsent(currDepth, k -> new ArrayList<>()).add(bone);
		System.out.println("added bone: " + bone.getName() + ", dl: " + depthLimit + ", currD: " + currDepth + ", children: " + bone.getChildrenNodes().size());
		for (IdObject child : bone.getChildrenNodes()) {
			if (child instanceof Bone && depthLimit != 0) {
				fillDepthMap((Bone) child, depthLimit - 1, currDepth + 1, depthMap);
			}
		}

	}

	private void fillDepthMap2(ModelHandler modelHandler, Bone bone, int depthLimit, int siblingIndex, boolean hasSibling, boolean parSibling, int parentKey, Map<Integer, Bone> depthMap) {
		int key;
		if (hasSibling || parSibling) {
			key = (parentKey * 300 + (siblingIndex + 1));
		} else {
			key = parentKey + 1;
		}
		depthMap.put(key, bone);
		List<IdObject> childrenNodes = bone.getChildrenNodes();
//		childrenNodes.sort(Comparator.comparingInt(b -> b.getObjectId(modelHandler.getModel())));
		childrenNodes.sort(Comparator.comparingInt(b -> modelHandler.getModel().getObjectId(b)));
		System.out.println("added bone: " + bone.getName() + ", dl: " + depthLimit + ", parentKey: " + parentKey + ", key: " + key + ", siblingIndex: " + siblingIndex + ", children: " + childrenNodes.size() + ", siblings: " + hasSibling);
		for (int i = 0; i < childrenNodes.size(); i++) {
			IdObject child = childrenNodes.get(i);
			if (child instanceof Bone && depthLimit != 0) {
				fillDepthMap2(modelHandler, (Bone) child, depthLimit - 1, i, childrenNodes.size() > 1, hasSibling, key, depthMap);
			}
		}

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
