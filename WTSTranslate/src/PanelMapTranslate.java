import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.gtranslate.Language;

import de.deaod.jstormlib.exceptions.MPQAlreadyExistsException;
import de.deaod.jstormlib.exceptions.MPQFileNotFoundException;
import de.deaod.jstormlib.exceptions.MPQFormatException;
import de.deaod.jstormlib.exceptions.MPQIsAVIException;


public class PanelMapTranslate extends JPanel {
	JButton open, saveAs;
	JComboBox<Lang> fromLang, toLang;
	//File currentFile = null;
	JTextField filePath;
	
	JFileChooser fileDialog = new JFileChooser();
	class Lang {
		String langName;
		String codeName;
		public Lang(String langName, String codeName) {
			this.langName = langName.charAt(0) + langName.substring(1).toLowerCase();
			this.codeName = codeName;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 * 
		 * This overrides toString so that the ComboBox I used will show the language name.
		 */
		@Override
		public String toString() {
			return langName;
		}
	}
	
	public PanelMapTranslate() {
		JPanel sub = new JPanel();
		
		open = new JButton("Open File (.w3x/.w3m)", new ImageIcon(WTSToolkitPanel.class.getResource("res/EditorTerrain.png")));
		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int result = fileDialog.showOpenDialog(PanelMapTranslate.this);
				File selectedFile = fileDialog.getSelectedFile();
				if( result == JFileChooser.APPROVE_OPTION && selectedFile != null ) {
					//currentFile = selectedFile;
					filePath.setText(selectedFile.getPath());
				}
			}
			
		});
		sub.add(open);
		
		//build a list of known languages from our library:
		
		// this is a place to store the found languages:
		List<Lang> languages = new ArrayList<Lang>();
		
		// this gets the language variables made inside the language class:
		Field[] fields = Language.class.getFields();
		
		for( Field field: fields ) { 
			try {
				// field.get(null) gets a static field value, like "Language.ENGLISH" having the value "en" or something like that
				languages.add(
						new Lang(
								field.getName(),
								field.get(null).toString()
								)
						);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		filePath = new JTextField();
		sub.add(filePath);
		
		
		sub.add(new JLabel("From language: "));
		
		fromLang = new JComboBox<Lang>(languages.toArray(new Lang[0]));
		fromLang.setMaximumRowCount(20);
		sub.add(fromLang);
		sub.add(new JLabel("To language: "));
		toLang = new JComboBox<Lang>(languages.toArray(new Lang[0]));
		toLang.setMaximumRowCount(20);
		sub.add(toLang);
		
		saveAs = new JButton("Translate and Save Target (.w3x/.w3m)", new ImageIcon(WTSToolkitPanel.class.getResource("res/ActionsNothing.png")));
		saveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File currentFile = new File(filePath.getText());
				if( currentFile == null || !currentFile.exists() ) {
					JOptionPane.showMessageDialog(PanelMapTranslate.this, "You must select a file.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					int result = fileDialog.showSaveDialog(PanelMapTranslate.this);
					File selectedFile = fileDialog.getSelectedFile();
					if( result == JFileChooser.APPROVE_OPTION && selectedFile != null ) {
						JFrame progress = new JFrame("Progress");
						progress.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						PanelMapTranslate.this.setEnabled(false);
						progress.getContentPane().add(new JLabel("Processing... please wait!"));
						progress.pack();
						progress.setLocationRelativeTo(PanelMapTranslate.this);
						progress.setVisible(true);
						boolean success = false;
						try {
							WTSTranslate.translateMap(currentFile,
									fromLang.getItemAt(fromLang.getSelectedIndex()).codeName,
									toLang.getItemAt(toLang.getSelectedIndex()).codeName,
									selectedFile);
							success = true;
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(PanelMapTranslate.this, "Translate operation failed:\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						} catch (MPQFormatException e1) {
							JOptionPane.showMessageDialog(PanelMapTranslate.this, "Translate operation failed:\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						} catch (MPQIsAVIException e1) {
							JOptionPane.showMessageDialog(PanelMapTranslate.this, "Translate operation failed:\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						} catch (MPQFileNotFoundException e1) {
							JOptionPane.showMessageDialog(PanelMapTranslate.this, "Translate operation failed:\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						} catch (MPQAlreadyExistsException e1) {
							JOptionPane.showMessageDialog(PanelMapTranslate.this, "Translate operation failed:\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						}
						PanelMapTranslate.this.setEnabled(true);
						progress.setVisible(false);
						if( success )
							JOptionPane.showMessageDialog(PanelMapTranslate.this, "Completed successfully!", "Success", JOptionPane.PLAIN_MESSAGE);

					}
				}
			}
			
		});
		sub.add(saveAs);
		filePath.setPreferredSize(new Dimension(500,15));
		sub.setLayout(new GridLayout(7,1));
		add(sub);
	}
}
