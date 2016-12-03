package com.matrixeater.imp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.matrixeater.data.SaveProfile;
import com.matrixeater.src.Animation;
import com.matrixeater.src.ImportPanel;
import com.matrixeater.src.MDL;
import com.matrixeater.src.MainFrame;

public class AnimationTransfer extends JPanel implements ActionListener {
	JLabel baseFileLabel, animFileLabel, outFileLabel, transSingleLabel, pickAnimLabel, visFromLabel;
	JTextField baseFileInput, animFileInput, outFileInput;
	JCheckBox transferSingleAnimation, useCurrentModel;
	JButton baseBrowse, animBrowse, outBrowse, transfer, done, goAdvanced;
	JComboBox<Animation> pickAnimBox, visFromBox;
	DefaultComboBoxModel<Animation> baseAnims;
	DefaultComboBoxModel<Animation> animAnims;
	
	JFileChooser fc = new JFileChooser();
	public AnimationTransfer()
	{
        MDL current = MainFrame.getPanel().currentMDL();
        if( current != null && current.getFile() != null )
        {
        	fc.setCurrentDirectory(current.getFile().getParentFile());
        }
        else if( SaveProfile.get().getPath() != null )
        {
            fc.setCurrentDirectory(new File(SaveProfile.get().getPath()));
        }
		
		baseFileLabel = new JLabel("Base file:");
		baseFileInput = new JTextField("");
		baseFileInput.setMinimumSize(new Dimension(200,18));
		baseBrowse = new JButton("...");
        Dimension dim = new Dimension(28,18);
        baseBrowse.setMaximumSize(dim);
        baseBrowse.setMinimumSize(dim);
        baseBrowse.setPreferredSize(dim);
		baseBrowse.addActionListener(this);
		
		animFileLabel = new JLabel("Animation file:");
		animFileInput = new JTextField("");
		animFileInput.setMinimumSize(new Dimension(200,18));
		animBrowse = new JButton("...");
        animBrowse.setMaximumSize(dim);
        animBrowse.setMinimumSize(dim);
        animBrowse.setPreferredSize(dim);
		animBrowse.addActionListener(this);
		
		outFileLabel = new JLabel("Output file:");
		outFileInput = new JTextField("");
		outFileInput.setMinimumSize(new Dimension(200,18));
		outBrowse = new JButton("...");
        outBrowse.setMaximumSize(dim);
        outBrowse.setMinimumSize(dim);
        outBrowse.setPreferredSize(dim);
		outBrowse.addActionListener(this);
		
		transferSingleAnimation = new JCheckBox("", false);
		transferSingleAnimation.addActionListener(this);
		transSingleLabel = new JLabel("Transfer single animation:");
		
		pickAnimLabel = new JLabel("Animation to transfer:");
		pickAnimBox = new JComboBox<Animation>();
		pickAnimBox.setEnabled(false);

		visFromLabel = new JLabel("Get visibility from:");
		visFromBox = new JComboBox<Animation>();
		visFromBox.setEnabled(false);
		
		transfer = new JButton("Transfer");
		transfer.setMnemonic(KeyEvent.VK_T);
		transfer.setMinimumSize(new Dimension(200,35));
		transfer.addActionListener(this);
		
		done = new JButton("Done");
		done.setMnemonic(KeyEvent.VK_D);
		done.setMinimumSize(new Dimension(80,35));
		done.addActionListener(this);
		
		goAdvanced = new JButton("Go Advanced");
		goAdvanced.setMnemonic(KeyEvent.VK_G);
		goAdvanced.setToolTipText("Opens the traditional MatrixEater Import window responsible for this Simple Import, so that you can micro-manage particular settings before finishing the operation.");
		
		GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGap(12)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup()
								.addComponent(baseFileLabel)
								.addComponent(animFileLabel)
								.addComponent(outFileLabel)
								)
							.addGap(16)
							.addGroup(layout.createParallelGroup()
								.addComponent(baseFileInput)
								.addComponent(animFileInput)
								.addComponent(outFileInput)
								)
							.addGap(16)
							.addGroup(layout.createParallelGroup()
								.addComponent(baseBrowse)
								.addComponent(animBrowse)
								.addComponent(outBrowse)
								)
							)
						.addGroup(layout.createSequentialGroup()
							.addComponent(transSingleLabel)
							.addComponent(transferSingleAnimation)
							)
						)
					.addGroup(layout.createSequentialGroup()
						.addGap(48)
						.addGroup(layout.createParallelGroup()
							.addComponent(pickAnimLabel)
							.addComponent(visFromLabel)
							)
						.addGap(16)
						.addGroup(layout.createParallelGroup()
							.addComponent(pickAnimBox)
							.addComponent(visFromBox)
							)
						)
					.addGroup(layout.createSequentialGroup()
							.addComponent(transfer)
							.addComponent(done)
						)
					)
				.addGap(12)
				);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGap(12)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(baseFileLabel)
						.addComponent(baseFileInput)
						.addComponent(baseBrowse)
						)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(animFileLabel)
						.addComponent(animFileInput)
						.addComponent(animBrowse)
						)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(outFileLabel)
						.addComponent(outFileInput)
						.addComponent(outBrowse)
						)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(transSingleLabel)
						.addComponent(transferSingleAnimation)
						)
					.addGap(8)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(pickAnimLabel)
						.addComponent(pickAnimBox)
						)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(visFromLabel)
						.addComponent(visFromBox)
						)
					.addGap(24)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(transfer)
						.addComponent(done)
						)
						
				)
				.addGap(12)
			);
		setLayout(layout);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == baseBrowse )
		{
            fc.setDialogTitle("Open");
            int returnValue = fc.showOpenDialog(this);
            
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
            	baseFileInput.setText(fc.getSelectedFile().getPath());
            }
		}
		else if( e.getSource() == animBrowse )
		{
            fc.setDialogTitle("Open");
            int returnValue = fc.showOpenDialog(this);
            
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
            	animFileInput.setText(fc.getSelectedFile().getPath());
            }
		}
		else if( e.getSource() == outBrowse )
		{
            fc.setDialogTitle("Save");
            int returnValue = fc.showSaveDialog(this);
            
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
            	outFileInput.setText(fc.getSelectedFile().getPath());
            }
		}
		else if( e.getSource() == transfer )
		{
			ImportPanel host = new ImportPanel(MDL.read(new File(baseFileInput.getText())),MDL.read(new File(animFileInput.getText())),false);
			host.animTransfer();
		}
	}
}
