package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.ReteraShaderStuff.OtherUtils;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public class ShaderEditPanel extends JPanel {

	private RSyntaxTextArea vertEditorPane;
	private RSyntaxTextArea fragEditorPane;

	AttributeSet redText;
	AttributeSet whiteText;

	String orgVertexShader;
	String lastVertexShader;
	String currVertexShader;
	String orgFragmentShader;
	String lastFragmentShader;
	String currFragmentShader;

	ShaderManager shaderManager;
	JLabel loadedLabel = new JLabel();
	JTextPane logPane;

	public ShaderEditPanel(ShaderManager shaderManager) {
		super(new MigLayout("gap 0, ins 0, fill, wrap 1", "[grow]", "[50%][50%][]"));
		this.shaderManager = shaderManager;
		StyleContext sc = StyleContext.getDefaultStyleContext();
		redText = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED.brighter().brighter());
		whiteText = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.WHITE.darker());

		orgVertexShader = OtherUtils.loadShader("HDDiffuseVertColor.vert");
		vertEditorPane = getRSEditorPane(orgVertexShader);
		View vertex_view = getAsView("Vertex Shader", vertEditorPane, orgVertexShader, this::getLastVertexShader);
		vertex_view.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());
		vertex_view.getWindowProperties().setDragEnabled(false);
//		vertex_view.getViewProperties().getViewTitleBarProperties().setVisible(true);


		orgFragmentShader = OtherUtils.loadShader("HDDiffuseVertColor.frag");
		fragEditorPane = getRSEditorPane(orgFragmentShader);
//		JPanel fsPanel = getEditorPanel(fragEditorPane, getButtonPanel(fragEditorPane, orgFragmentShader, this::getLastFragmentShader));
		View fragment_view = getAsView("Fragment Shader", fragEditorPane, orgVertexShader, this::getLastFragmentShader);

//		fragment_view.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());
		fragment_view.getWindowProperties().setDragEnabled(false);
//		fragment_view.getViewProperties().getViewTitleBarProperties().setVisible(true);

		View errorView = getErrorView();
		TabWindow tabs = new TabWindow(new DockingWindow[] {vertex_view, fragment_view, errorView});
		tabs.setSelectedTab(1);
//		add(vertex_view, "growx, growy");
//		add(fragment_view, "growx, growy");
		add(tabs, "growx, growy, spany 2");

		JButton applyButton = new JButton("Use Shader");
		applyButton.addActionListener(e -> apply());
		add(applyButton, "split 3");
		JButton useInternal = new JButton("Use Internal Shader");
		useInternal.addActionListener(e -> useInternalShader());
		add(useInternal, "");
		add(loadedLabel, "");
	}

	private void apply(){
		String newVertShader = vertEditorPane.getText();
		if(currVertexShader != null && !currVertexShader.equals(newVertShader)){
			lastVertexShader = currVertexShader;
		}
		currVertexShader = newVertShader;

		String newFragShader = fragEditorPane.getText();
		if(currFragmentShader != null && !currFragmentShader.equals(newFragShader)){
			lastFragmentShader = currFragmentShader;
		}
		currFragmentShader = newFragShader;

		shaderManager.createCustomShader(currVertexShader, currFragmentShader);

		Timer timer = new Timer(50, e -> checkShader());
		timer.setRepeats(false);
		timer.start();
	}
	private void useInternalShader(){
		shaderManager.removeCustomShader();

		try {
			LocalTime time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
			String timeString = time.toString();
			if(!timeString.matches("\\d\\d:\\d\\d:\\d\\d")){
				timeString = timeString + ":00";
			}
			String text = "Loaded Internal Shader";
			loadedLabel.setText(text);
//			logPane.getDocument().insertString(logPane.getDocument().getLength(), "\n" + time + " " + text, aset);
			logPane.getDocument().insertString(logPane.getDocument().getLength(), "\n" + timeString + " " + text, whiteText);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	private void checkShader() {
		Exception shaderException = shaderManager.getCustomHDShaderException();
//		System.out.println("time: " + LocalTime.now().format(DateTimeFormatter.ISO_TIME));
		LocalTime time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
		String text;
		AttributeSet aset;
		if(shaderException != null){
			aset = redText;
			loadedLabel.setForeground(Color.RED.darker());
			text = "Failed to load " + makePrettyMessage(shaderException);

			shaderManager.clearLastException();
			currVertexShader = null;
			currFragmentShader = null;
		} else {
			aset = whiteText;
			loadedLabel.setForeground(null);
			text = "Shader Loaded";
		}
		try {
			String timeString = time.toString();
			if(!timeString.matches("\\d\\d:\\d\\d:\\d\\d")){
				timeString = timeString + ":00";
			}
			loadedLabel.setText(text);
//			logPane.getDocument().insertString(logPane.getDocument().getLength(), "\n" + time + " " + text, aset);
			logPane.getDocument().insertString(logPane.getDocument().getLength(), "\n" + timeString + " " + text, aset);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	private String makePrettyMessage(Exception exception){
		String localizedMessage = exception.getLocalizedMessage().trim();
		for(StackTraceElement element : exception.getStackTrace()){
			if(element.getMethodName().contains("VertexShader")){
				return localizedMessage.replaceFirst("0: 0\\(", "(VertexShader:");
			} else if(element.getMethodName().contains("GeometryShader")){
				return localizedMessage.replaceFirst("0: 0\\(", "(GeometryShader:");
			} else if(element.getMethodName().contains("FragmentShader")){
				return localizedMessage.replaceFirst("0: 0\\(", "(FragmentShader:");

			}
		}
		return localizedMessage;
	}

	private String getLastFragmentShader(){
		if(lastFragmentShader != null){
			return lastFragmentShader;
		}
		return orgFragmentShader;
	}
	private String getLastVertexShader(){
		if(lastFragmentShader != null){
			return lastFragmentShader;
		}
		return orgFragmentShader;
	}

	public View getAsView(String title, RSyntaxTextArea textArea, String text, Supplier<String> lastText) {
		JPanel rsButtonPanel = getRSButtonPanel(textArea, text, lastText);
		JPanel rsEditorPanel = getRSEditorPanel(textArea, rsButtonPanel);

		return new View(title, null, rsEditorPanel);
	}

	private RSyntaxTextArea getRSEditorPane(String text) {
		RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
		textArea.setCodeFoldingEnabled(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
		textArea.setCodeFoldingEnabled(true);
//		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		textArea.setText(text);

		return textArea;
	}

	public JPanel getRSEditorPanel(RSyntaxTextArea editorPane, JPanel buttonPanel){
		RTextScrollPane scrollPane = new RTextScrollPane(editorPane);
		JPanel mainPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "", "[grow][]"));
		mainPanel.add(scrollPane, "spanx, growx, growy, wrap");


		mainPanel.add(buttonPanel, "");
		return mainPanel;
	}

	private JPanel getRSButtonPanel(RSyntaxTextArea editorPane, String orgText, Supplier<String> lastText) {
		JPanel buttonPanel = new JPanel(new MigLayout("", "", ""));

		JButton resetButton1 = new JButton("Reset to last");
		resetButton1.addActionListener(e -> editorPane.setText(lastText.get()));

		JButton resetButton2 = new JButton("Reset");
		resetButton2.addActionListener(e -> editorPane.setText(orgText));

		buttonPanel.add(resetButton1, "");
		buttonPanel.add(resetButton2, "");
		return buttonPanel;
	}

	private View getErrorView(){

		logPane = new JTextPane();
		logPane.setBackground(new Color(30,30,30));
		logPane.setForeground(new Color(240, 80, 60));

		JScrollPane jScrollPane = new JScrollPane(logPane);

		jScrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		return new View("log", null, jScrollPane);
	}


	public static void show(JComponent parent, ShaderManager shaderManager) {
		ShaderEditPanel animCopyPanel = new ShaderEditPanel(shaderManager);
//		animCopyPanel.setSize(1600, 900);
		animCopyPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(animCopyPanel, parent, "Shader Editor");

		ActionMap actionMap = animCopyPanel.vertEditorPane.getActionMap();
		InputMap inputMap = animCopyPanel.vertEditorPane.getInputMap();

		for(KeyStroke keyStroke : inputMap.allKeys()){
			Object o = inputMap.get(keyStroke);
			if(o != null){
				Action action = actionMap.get(o);
				if(action != null){

					System.out.println("ActionKey[" + keyStroke + "] : " + o + " (" + action + ")");
				}
			}
		}
//		for(Object o : actionMap.allKeys()){
//			Action action = actionMap.get(o);
//			if(action != null){
//
//				System.out.println("ActionKey[" + o + "] : " + action);
//			}
//		}
	}

}