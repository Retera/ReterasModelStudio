package com.requestin8r.src;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.ModelOptionPane;
import com.hiveworkshop.wc3.units.UnitOptionPane;

public class NewRequestPanel extends JPanel implements ActionListener {

	JButton unit, model, custom, back;
	MainFrame frame;
	JFileChooser jfc = new JFileChooser();

	public NewRequestPanel(final MainFrame frame) {
		this.frame = frame;
		final Font smallFont = new Font("Arial",Font.BOLD,16);
		final Font medFont = new Font("Arial",Font.BOLD,28);
		final Font bigFont = new Font("Arial",Font.BOLD,46);

		final JLabel title = new JLabel("New Project");
		title.setIcon(new ImageIcon(IconGet.get("BasicStruct", 64)));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Where do you want to start?");
		desc.setFont(smallFont);

		add(title);
		add(desc);

		unit = new JButton("Unit", new ImageIcon(IconGet.get("Footman", 48)));
		unit.setFont(medFont);
		unit.addActionListener(this);
		final JLabel unitTip = new JLabel("Choose a base model by selecting a unit type from Warcraft III.");
		unitTip.setFont(smallFont);
		model = new JButton("Model", new ImageIcon(new ImageIcon(getClass().getResource("BTNfootmanBro.png")).getImage().getScaledInstance(48,48,Image.SCALE_SMOOTH)));
		model.setFont(medFont);
		model.addActionListener(this);
		final JLabel modelTip = new JLabel("Choose a base model by selecting a model from the Warcraft III Object Editor.");
		modelTip.setFont(smallFont);
		custom = new JButton("Custom", new ImageIcon(IconGet.get("Temp", 48)));
		custom.setFont(medFont);
		custom.addActionListener(this);
		final JLabel customTip = new JLabel("Choose a base model by selecting a custom file on your PC.");
		customTip.setFont(smallFont);
		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);

		add(unit);
		add(model);
		add(custom);
		add(back);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(title)
								.addComponent(desc)
								.addComponent(unit)
								.addComponent(unitTip)
								.addComponent(model)
								.addComponent(modelTip)
								.addComponent(custom)
								.addComponent(customTip)

						)
						.addComponent(back)
				)
				.addGap(16));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addComponent(title)
				.addGap(4)
				.addComponent(desc)
				.addGap(64)
				.addComponent(unit)
				.addGap(4)
				.addComponent(unitTip)
				.addGap(32)
				.addComponent(model)
				.addGap(4)
				.addComponent(modelTip)
				.addGap(32)
				.addComponent(custom)
				.addGap(4)
				.addComponent(customTip)
				.addGap(84)
				.addComponent(back)
				.addGap(16));

		setLayout(layout);

		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model \"*.mdx\"", "mdx"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text-based Model \"*.mdl\"", "mdl"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(jfc.getChoosableFileFilters()[0]);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if( e.getSource() == unit ) {
			final Element choice = UnitOptionPane.show(this);
			if( choice != null ) {
				JOptionPane.showMessageDialog(null, choice.getName() + ". Good choice.");
			} else {
				return;
			}

			String filepath = choice.getField("file");
			final String name = choice.getName();
			final Image icon = choice.getImage();

			MDL toLoad;
			MDLDisplay modelDisp = null;
			try {
				if( filepath.endsWith(".mdl") ) {
					filepath = filepath.replace(".mdl", ".mdx");
				}
				else if( !filepath.endsWith(".mdx") ) {
					filepath = filepath.concat(".mdx");
				}
				toLoad = MDL.read(MpqCodebase.get().getFile(filepath));
				modelDisp = new MDLDisplay(toLoad, null);
			}
			catch (final Exception exc) {
				exc.printStackTrace();
				//bad model!
				JOptionPane.showMessageDialog(frame,"The chosen model could not be used.","Program Error",JOptionPane.ERROR_MESSAGE);
			}

			if( modelDisp != null ) {
				final WorkPanel workPanel = new WorkPanel(new Project(modelDisp, icon, name), frame);
				frame.setContentPane(workPanel);
				frame.revalidate();
				frame.pack();
			}
		}
		else if( e.getSource() == model ) {
			String filepath = ModelOptionPane.show(this);
			if( filepath != null ) {
				JOptionPane.showMessageDialog(null, filepath + ". Good choice.");
			} else {
				return;
			}

			String name = filepath;
			if( name.contains("\\") ) {
				name = name.substring(name.lastIndexOf("\\") + 1);
			}
			if( name.contains(".") ) {
				name = name.substring(0, name.indexOf("."));
			}
			final Image icon = IconGet.get("Temp", 64);

			MDL toLoad;
			MDLDisplay modelDisp = null;
			try {
				if( filepath.endsWith(".mdl") ) {
					filepath = filepath.replace(".mdl", ".mdx");
				}
				else if( !filepath.endsWith(".mdx") ) {
					filepath = filepath.concat(".mdx");
				}
				toLoad = MDL.read(MpqCodebase.get().getFile(filepath));
				modelDisp = new MDLDisplay(toLoad, null);
			}
			catch (final Exception exc) {
				exc.printStackTrace();
				//bad model!
				JOptionPane.showMessageDialog(frame,"The chosen model could not be used.","Program Error",JOptionPane.ERROR_MESSAGE);
			}

			if( modelDisp != null ) {
				final WorkPanel workPanel = new WorkPanel(new Project(modelDisp, icon, name), frame);
				frame.setContentPane(workPanel);
				frame.revalidate();
				frame.pack();
			}
		}
		else if( e.getSource() == custom ) {
			final int x = jfc.showOpenDialog(frame);
			if( x == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile() != null ) {
				JOptionPane.showMessageDialog(null, jfc.getSelectedFile() + ". Good choice.");
			} else {
				return;
			}

			final String filepath = jfc.getSelectedFile().getPath();
			String name = filepath;
			if( name.contains("\\") ) {
				name = name.substring(name.lastIndexOf("\\") + 1);
			}
			if( name.contains(".") ) {
				name = name.substring(0, name.indexOf("."));
			}
			final Image icon = IconGet.get("Temp", 64);

			MDL toLoad;
			MDLDisplay modelDisp = null;
			try {
				toLoad = MDL.read(new File(filepath));
				modelDisp = new MDLDisplay(toLoad, null);
			}
			catch (final Exception exc) {
				exc.printStackTrace();
				//bad model!
				JOptionPane.showMessageDialog(frame,"The chosen model could not be used.","Program Error",JOptionPane.ERROR_MESSAGE);
			}

			if( modelDisp != null ) {
				final WorkPanel workPanel = new WorkPanel(new Project(modelDisp, icon, name), frame);
				frame.setContentPane(workPanel);
				frame.revalidate();
				frame.pack();
			}
		}
		else if( e.getSource() == back ) {
			frame.setContentPane(frame.mainPanel);
			frame.revalidate();
			frame.pack();
		}
	}
}
