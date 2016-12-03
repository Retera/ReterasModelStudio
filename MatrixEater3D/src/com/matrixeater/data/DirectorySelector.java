package com.matrixeater.data;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class DirectorySelector extends JPanel implements ActionListener {
	JLabel text = new JLabel("Please select a valid Warcraft III game directory:");
	JTextField pathField;
	JButton browseButton = new JButton("Browse");
	public DirectorySelector(String defaultDir, String specialText)
	{
		text.setText(text.getText());
		pathField = new JTextField(defaultDir);
		browseButton.addActionListener(this);
		
		
		GroupLayout layout = new GroupLayout(this);
		GroupLayout.ParallelGroup horizGroup = layout.createParallelGroup();
		GroupLayout.SequentialGroup vertGroup = layout.createSequentialGroup();

		for( int i = 0; i <= specialText.length()/70; i++)
		{
			JLabel text2 = new JLabel(specialText.substring(i*70,(int)Math.min(specialText.length(),(i+1)*70)));
			if( specialText == null || specialText.equals("") )
			{
				text2.setVisible(false);
			}
			horizGroup.addComponent(text2);
			vertGroup.addComponent(text2);
		}
		
		if( !(specialText == null || specialText.equals("")) )
		{
			vertGroup.addGap(12);
		}
		
		layout.setHorizontalGroup(horizGroup
				.addComponent(text)
				.addGroup(layout.createSequentialGroup()
						.addComponent(pathField)
						.addComponent(browseButton)
						)
					);
		layout.setVerticalGroup(vertGroup
				.addComponent(text)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(pathField)
						.addComponent(browseButton)
						)
					);
		setLayout(layout);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == browseButton )
		{
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setCurrentDirectory(new File(pathField.getText()));
			int x = jfc.showOpenDialog(this);
			if( x == JFileChooser.APPROVE_OPTION )
			{
				String wcDirectory = jfc.getSelectedFile().getAbsolutePath();
				if( !(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\")) )
				{
					wcDirectory = wcDirectory + "\\";
				}
				File temp = new File(wcDirectory+"mod_test_file.txt");
				boolean good = false;
				try {
					good = temp.createNewFile();
					temp.delete();
				} catch (IOException exc) {
					exc.printStackTrace();
				}
				if( !good )
				{
					JOptionPane.showMessageDialog(null, "You do not have permissions to access the chosen folder.\nYou should \"Run as Administrator\" on this program, or otherwise gain file permissions to the target folder, for the texture loader to work on that folder.","WARNING: Texture-Loader Won't Work", JOptionPane.WARNING_MESSAGE);
				}
				
				pathField.setText(jfc.getSelectedFile().getAbsolutePath());
			}
		}
	}
	
	public String getDir()
	{
		return pathField.getText();
	}
}
