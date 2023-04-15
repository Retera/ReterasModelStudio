package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.OtherUtils;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
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

	ShaderManager.PipelineType pipelineType;
	AttributeSet redText;
	AttributeSet whiteText;

	ShaderTracker[] shaderTrackers;


	ShaderManager shaderManager;
	JLabel loadedLabel = new JLabel();
	JTextPane logPane;

	public ShaderEditPanel(ShaderManager shaderManager, ShaderEditorType shaderEditorType){
		this(shaderManager, shaderEditorType.pipelineType, shaderEditorType.shaders);
	}

	public ShaderEditPanel(ShaderManager shaderManager, ShaderManager.PipelineType pipelineType, String... shaderFiles) {
		super(new MigLayout("gap 0, ins 0, fill, wrap 1", "[grow]", "[50%][50%][]"));
		this.shaderManager = shaderManager;
		this.pipelineType = pipelineType;
		shaderTrackers = loadShaderStrings(shaderFiles);

		StyleContext sc = StyleContext.getDefaultStyleContext();
		redText = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED.brighter().brighter());
		whiteText = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.WHITE.darker());


		TabWindow tabs = new TabWindow(getAsViews(shaderTrackers));
		tabs.setSelectedTab(1);
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
		for(ShaderTracker shaderTracker : shaderTrackers){
			shaderTracker.applyNew();
		}

//		shaderManager.createCustomShader(currVertexShader, currFragmentShader, isHD);
		createCustomShader();

		Timer timer = new Timer(50, e -> checkShader());
		timer.setRepeats(false);
		timer.start();
	}

	protected void createCustomShader(){
		shaderManager.createCustomShader(pipelineType, shaderTrackers[0].getCurrShader(),
				shaderTrackers.length == 3 ? shaderTrackers[2].getCurrShader() : shaderTrackers[1].getCurrShader(),
				shaderTrackers.length == 3 ? shaderTrackers[1].getCurrShader() : "");
		if(shaderTrackers.length == 2){
			shaderManager.createCustomShader(pipelineType, shaderTrackers[0].getCurrShader(), shaderTrackers[1].getCurrShader(), "");
		} else {
			shaderManager.createCustomShader(pipelineType, shaderTrackers[0].getCurrShader(), shaderTrackers[2].getCurrShader(), shaderTrackers[1].getCurrShader());
		}
	}
	protected void removeCustomShader(){
		shaderManager.removeCustomShader(pipelineType);
	}

	private void useInternalShader(){
		removeCustomShader();

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
		Exception shaderException = shaderManager.getCustomShaderException(pipelineType);
//		System.out.println("time: " + LocalTime.now().format(DateTimeFormatter.ISO_TIME));
		LocalTime time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
		String text;
		AttributeSet aset;
		if(shaderException != null){
			aset = redText;
			loadedLabel.setForeground(Color.RED.darker());
			text = "Failed to load " + makePrettyMessage(shaderException);

			shaderManager.clearLastException(pipelineType);
			for(ShaderTracker shaderTracker : shaderTrackers){
				shaderTracker.setCurrShader(null);
			}
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

	public View[] getAsViews(ShaderTracker[] shaderTrackers) {
		View[] view = new View[shaderTrackers.length + 1];
		for(int i = 0; i<shaderTrackers.length; i++){
			view[i] = getAsView(shaderTrackers[i]);
		}
		view[shaderTrackers.length] = getErrorView();
		return view;
	}

	public View getAsView(ShaderTracker shaderTracker) {
		JPanel rsButtonPanel = getRSButtonPanel(shaderTracker.getEditorPane(), shaderTracker.getOrgShader(), shaderTracker::getLastShader);
		JPanel rsEditorPanel = getRSEditorPanel(shaderTracker.getEditorPane(), rsButtonPanel);

		View view = new View(shaderTracker.getName(), null, rsEditorPanel);
		view.getWindowProperties().setDragEnabled(!ProgramGlobals.isLockLayout());
		view.getWindowProperties().setDragEnabled(false);
		return view;
	}

	private RSyntaxTextArea getRSEditorPane(String text) {
		RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
		textArea.setCodeFoldingEnabled(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
		textArea.setCodeFoldingEnabled(true);
//		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		textArea.setText(text);

		InputMap inputMap = textArea.getInputMap();
		Object toggleCommentKey = inputMap.get(KeyStroke.getKeyStroke("ctrl pressed SLASH"));
		inputMap.put(KeyStroke.getKeyStroke("ctrl pressed DIVIDE"), toggleCommentKey);

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

	private ShaderTracker[] loadShaderStrings(String... shaderFiles){
		ShaderTracker[] shaderTrackers = new ShaderTracker[shaderFiles.length];
		for(int i = 0; i< shaderFiles.length; i++){
			String name;
			if(shaderFiles[i].matches(".+\\.g.*")){
				name = "Geometry Shader";
			} else if(shaderFiles[i].matches(".+\\.f.*")){
				name = "Fragment Shader";
			} else if(shaderFiles[i].matches(".+\\.v.*")){
				name = "Vertex Shader";
			} else {
				name = shaderFiles[i].replaceAll(".+\\.", "");
			}
			shaderTrackers[i] = new ShaderTracker(name, OtherUtils.loadShader(shaderFiles[i]));
		}
		return shaderTrackers;
	}

//	public static void show(JComponent parent, BufferFiller bufferFiller) {
//		ShaderEditPanel shaderEditPanel = new ShaderEditPanel(bufferFiller.getShaderManager(), bufferFiller.isHD());
////		shaderEditPanel.setSize(1600, 900);
//		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
//		FramePopup.show(shaderEditPanel, parent, "Shader Editor");
//	}


	protected static class ShaderTracker{
		String orgShader;
		String currShader;
		String lastShader;
		RSyntaxTextArea editorPane;
		String name;

		ShaderTracker(String name, String orgShader){
			this.name = name;
			this.orgShader = orgShader;
			this.editorPane = getRSEditorPane(orgShader);
		}

		public String getName() {
			return name;
		}

		public ShaderTracker applyNew(){
			String newShader = editorPane.getText();
			if(currShader != null && !currShader.equals(newShader)){
				lastShader = currShader;
			}
			currShader = newShader;
			return this;
		}

		public RSyntaxTextArea getEditorPane() {
			return editorPane;
		}

		public String getCurrShader() {
			return currShader;
		}

		public String getLastShader() {
			if(lastShader != null){
				return lastShader;
			}
			return orgShader;
		}

		public String getOrgShader() {
			return orgShader;
		}

		public ShaderTracker setCurrShader(String currShader) {
			this.currShader = currShader;
			return this;
		}

		public ShaderTracker setLastShader(String lastShader) {
			this.lastShader = lastShader;
			return this;
		}

		private RSyntaxTextArea getRSEditorPane(String text) {
			RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
			textArea.setCodeFoldingEnabled(true);
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
			textArea.setCodeFoldingEnabled(true);
//		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			textArea.setText(text);

			InputMap inputMap = textArea.getInputMap();
			Object toggleCommentKey = inputMap.get(KeyStroke.getKeyStroke("ctrl pressed SLASH"));
			inputMap.put(KeyStroke.getKeyStroke("ctrl pressed DIVIDE"), toggleCommentKey);

			return textArea;
		}
	}



	public static void show2(JComponent parent, BufferFiller bufferFiller, String title, ShaderManager.PipelineType pipelineType, String... shaderFiles) {
		ShaderEditPanel shaderEditPanel = new ShaderEditPanel(bufferFiller.getShaderManager(), pipelineType, shaderFiles);
//		shaderEditPanel.setSize(1600, 900);
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, title);

	}


	public static void show2(JComponent parent, BufferFiller bufferFiller, ShaderEditorType shaderEditorType) {
		ShaderEditPanel shaderEditPanel = new ShaderEditPanel(bufferFiller.getShaderManager(), shaderEditorType.getPipelineType(), shaderEditorType.getShaders());
//		shaderEditPanel.setSize(1600, 900);
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, shaderEditorType.getTitle());

	}

}
