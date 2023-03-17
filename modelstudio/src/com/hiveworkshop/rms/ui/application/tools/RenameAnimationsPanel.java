package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetAnimationNameAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.TwiPopup;
import com.hiveworkshop.rms.util.FramePopup;
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
import java.util.regex.Pattern;

public class RenameAnimationsPanel extends JPanel {

	private JButton doRenaming;
	private Frame mainFrame;
	private Frame helperFrame;
	private final JTextField searchField;
	private final JTextField replaceField;
	private final JCheckBox caseSensitive;
	private final JCheckBox regex;
	private final JPanel testPanel;
	private final JPanel regexPanel;

	public RenameAnimationsPanel() {
		super(new MigLayout("fill", "", ""));

		searchField = new JTextField("" , 24);
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
		doRenaming.addActionListener(e -> doRenaming(searchField.getText(), replaceField.getText(), caseSensitive.isSelected(), regex.isSelected()));
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

//		"XX(?=TEXT)       XX followed by TEXT"
//		"(?<=TEXT)XX      XX preceded by TEXT"
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


	private void doTestReplacement(JTextField textField, String text, String find, String replace, boolean caseSensitive, boolean regex){
		Pattern pattern = getPattern(find, caseSensitive, regex);
		textField.setText(pattern.matcher(text).replaceAll(replace));
		repaint();
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
		RenameAnimationsPanel animCopyPanel = new RenameAnimationsPanel();
		JFrame jFrame = FramePopup.show(animCopyPanel, parent, "Rename Animations");
		animCopyPanel.initHelperFrame(jFrame);
	}

	private void doRenaming(String searchString, String replaceString, boolean caseSensitive, boolean regex) {
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();


			List<UndoAction> actions = new ArrayList<>();
			int countRenamedAnims = 0;
			Pattern pattern = getPattern(searchString, caseSensitive, regex);

			for (Animation animation : modelHandler.getModel().getAnims()) {
				String oldName = animation.getName();
				String newName = pattern.matcher(oldName).replaceAll(replaceString);

				if(!oldName.equals(newName) && newName != null){
					actions.add(new SetAnimationNameAction(newName, animation, null));
					countRenamedAnims++;
				}
			}

			if (!actions.isEmpty()){
				modelHandler.getUndoManager().pushAction(new CompoundAction("Rename " + countRenamedAnims + " Animation(s)", actions, ModelStructureChangeListener.changeListener::animationParamsChanged).redo());
			}
			TwiPopup.quickDismissPopup(this, "Renamed " + countRenamedAnims + " animations!", "Renamed Animations");

		}

	}

	private Pattern getPattern(String searchString, boolean caseSensitive, boolean regex) {
		int flags = 0;
		flags |= !caseSensitive ? Pattern.CASE_INSENSITIVE : 0;
		if(!regex) {
			char backSlash = '\\';
			char[] regexEscapes = new char[] {'(', ')', '[', ']', '{', '}', '^', '|', '\\', '+', '?', '.', '*', '&', '/', '<'};
			StringBuilder sb = new StringBuilder();
			for (char c : regexEscapes) {
				sb.append(backSlash).append(c);
			}
			searchString = searchString.replaceAll("(?=[" + sb + "])", "\\\\");
		}
		return Pattern.compile(searchString, flags);
	}

//	private String getNewName(String searchString, String replaceString, boolean caseSensitive, boolean regex, IdObject idObject) {
//		String newName = null;
//		if(idObject.getName().equals(searchString) || caseSensitive || regex){
//
//			newName = replaceString;
//		}
//		return newName;
//	}
//	private String getNewName(String searchString, String replaceString, boolean caseSensitive, boolean regex, String oldName,Pattern pattern) {
//		String newName = pattern.matcher(oldName).replaceAll(replaceString);
//
//		return newName;
//	}
//
//	private String getIndexString(int key) {
//		List<Integer> indexes2 = new ArrayList<>();
//		for (int tempKey = key; tempKey > 0; ) {
//			int subKey = tempKey % 300;
//			if (subKey != 0) {
//				indexes2.add(subKey);
//				tempKey -= subKey;
//			} else {
//				tempKey /= 300;
//			}
//		}
//		StringBuilder stringBuilder = new StringBuilder();
//		for (int i = indexes2.size() - 1; i >= 0; i--) {
//			stringBuilder.append("_").append(indexes2.get(i));
//		}
//		return stringBuilder.toString();
//
//	}
//
//
//	private void doRenaming(Bone bone, ModelHandler modelHandler, String name, String subfix, boolean doTypePrefix, int depthLimit) {
//		Map<Integer, List<Bone>> depthMap = new HashMap<>();
//		fillDepthMap(bone, depthLimit, 0, depthMap);
//
//		List<UndoAction> actions = new ArrayList<>();
//		int depthStringSize = ("" + depthMap.size()).length();
//		int countRenamedBones = 0;
//		for (int depth : depthMap.keySet()) {
//			List<Bone> bones = depthMap.get(depth);
//			int siblingStringSize = ("" + bones.size()).length();
//			for (int i = 0; i <= bones.size(); i++) {
//				Bone nodeToRename = bones.get(i);
//				String prefix = doTypePrefix ? nodeToRename.getClass().getName() + " " : "";
//				String depthString = ("0000" + depth).substring(4 - depthStringSize, 4);
//				String siblingString = bones.size() > 1 ? ("0000" + i).substring(4 - siblingStringSize, 4) : "";
//				String newName = prefix + name + " " + depthString + siblingString + subfix;
//				actions.add(new RenameBoneAction(newName, nodeToRename));
//				countRenamedBones++;
//			}
//		}
//
//		modelHandler.getUndoManager().pushAction(new CompoundAction("Rename Bone Chain", actions, ModelStructureChangeListener.changeListener::nodesUpdated));
//		JOptionPane.showMessageDialog(this, "Renamed " + countRenamedBones + " bones/helpers!", "Renamed Bones", JOptionPane.INFORMATION_MESSAGE);
//	}
//
//	private void fillDepthMap(Bone bone, int depthLimit, int currDepth, Map<Integer, List<Bone>> depthMap) {
//		depthMap.computeIfAbsent(currDepth, k -> new ArrayList<>()).add(bone);
//		System.out.println("added bone: " + bone.getName() + ", dl: " + depthLimit + ", currD: " + currDepth + ", children: " + bone.getChildrenNodes().size());
//		for (IdObject child : bone.getChildrenNodes()) {
//			if (child instanceof Bone && depthLimit != 0) {
//				fillDepthMap((Bone) child, depthLimit - 1, currDepth + 1, depthMap);
//			}
//		}
//
//	}
//
//	private void fillDepthMap2(ModelHandler modelHandler, Bone bone, int depthLimit, int siblingIndex, boolean hasSibling, boolean parSibling, int parentKey, Map<Integer, Bone> depthMap) {
//		int key;
//		if (hasSibling || parSibling) {
//			key = (parentKey * 300 + (siblingIndex + 1));
//		} else {
//			key = parentKey + 1;
//		}
//		depthMap.put(key, bone);
//		List<IdObject> childrenNodes = bone.getChildrenNodes();
////		childrenNodes.sort(Comparator.comparingInt(b -> b.getObjectId(modelHandler.getModel())));
//		childrenNodes.sort(Comparator.comparingInt(b -> modelHandler.getModel().getObjectId(b)));
//		System.out.println("added bone: " + bone.getName() + ", dl: " + depthLimit + ", parentKey: " + parentKey + ", key: " + key + ", siblingIndex: " + siblingIndex + ", children: " + childrenNodes.size() + ", siblings: " + hasSibling);
//		for (int i = 0; i < childrenNodes.size(); i++) {
//			IdObject child = childrenNodes.get(i);
//			if (child instanceof Bone && depthLimit != 0) {
//				fillDepthMap2(modelHandler, (Bone) child, depthLimit - 1, i, childrenNodes.size() > 1, hasSibling, key, depthMap);
//			}
//		}
//	}

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
