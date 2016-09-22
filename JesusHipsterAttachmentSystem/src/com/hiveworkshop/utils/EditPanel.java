package com.hiveworkshop.utils;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.AnimFlag.Entry;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.units.ModelOptionPane;
import com.matrixeater.src.SliderBarHandler;
import com.requestin8r.src.IconGet;

public class EditPanel extends JPanel implements ActionListener {
	private final MDL baseModel;
	private final MainFrame frame;

	JButton save, back;
	private final JFileChooser jfc = new JFileChooser();
	private final JPanel previousPanel;

	public EditPanel(final MainFrame frame, final JPanel previousPanel, final MDL baseModel) {
		this.frame = frame;
		this.previousPanel = previousPanel;
		this.baseModel = baseModel;

		final Font smallFont = new Font("Arial",Font.BOLD,16);
		final Font medFont = new Font("Arial",Font.BOLD,28);
		final Font bigFont = new Font("Arial",Font.BOLD,46);

		final JLabel title = new JLabel("Configure Attachments");
		title.setIcon(new ImageIcon(IconGet.get("ScatterRockets", 64)));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Determine which models you wish to attach.");
		desc.setFont(smallFont);

		add(title);
		add(desc);

		save = new JButton("Save", new ImageIcon(IconGet.get("GoldMine", 48)));
		save.setFont(medFont);
		save.addActionListener(this);
		final JLabel saveTip = new JLabel("Save this new attachment model to a file.");
		saveTip.setFont(smallFont);
		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);

		add(save);

		add(back);

		final GroupLayout layout = new GroupLayout(this);
		GroupLayout.Group horizontalGroup;
		GroupLayout.Group verticalGroup;
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addGroup(layout.createParallelGroup()
						.addGroup(horizontalGroup=layout.createParallelGroup()
								.addComponent(title)
								.addComponent(desc)
						)
						.addComponent(back)
				)
				.addGap(16));

		layout.setVerticalGroup(verticalGroup=layout.createSequentialGroup()
				.addGap(16)
				.addComponent(title)
				.addGap(4)
				.addComponent(desc)
				.addGap(64)
				/*.addComponent(modelList)*/);



		for(final Attachment atch: baseModel.sortedIdObjects(Attachment.class)) {
			final JLabel label = new JLabel("Sets the model to attach to \""+atch.getName() + "\".");
			label.setFont(smallFont);
			final JLabel scaleLabel = new JLabel("Sets the attachment scaling.");
			scaleLabel.setFont(smallFont);
			String path = atch.getPath();
			if( path == null ) {
				path = "Abilities\\Weapons\\BloodElfMissile\\BloodElfMissile.mdl";
				atch.setPath(path);
			}
			path = path.substring(path.lastIndexOf('\\')+1);
			final JButton modelButton = new JButton(path, new ImageIcon(IconGet.get("OrbOfFire", 48)));
			modelButton.setFont(medFont);
			modelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					String newPath = ModelOptionPane.show(EditPanel.this, atch.getPath());
					if( newPath != null ) {
						if( !newPath.toLowerCase().endsWith(".mdl") ) {
							newPath += ".mdl";
						}
						atch.setPath(newPath);
						modelButton.setText(newPath.substring(newPath.lastIndexOf('\\')+1));
					}
				}
			});

			final JSlider bar = new JSlider(0, 10000);
			int currentValue = 1000;
			for(final AnimFlag flag: atch.getAnimFlags()) {
				if( flag.getTypeId() == AnimFlag.SCALING ) {
					currentValue=(int)(((Vertex)flag.getEntry(0).value).x*1000);
				}
			}
			bar.setValue(currentValue);
			final JSpinner spinner = new JSpinner(new SpinnerNumberModel(bar.getValue() / 1000.00, 0.0, 10.00, 0.001));

			final SliderBarHandler handler = new SliderBarHandler(bar, spinner);
			bar.addChangeListener(handler);
			spinner.addChangeListener(handler);
			spinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					boolean foundScalingFlag = false;
					final Vertex scalingValue = new Vertex(((Number)spinner.getValue()).doubleValue(),((Number)spinner.getValue()).doubleValue(),((Number)spinner.getValue()).doubleValue());
					for(final AnimFlag flag: atch.getAnimFlags()) {
						if( flag.getTypeId() == AnimFlag.SCALING ) {
							for(int i = 0; i < flag.size(); i++) {
								final Entry entry = flag.getEntry(i);
								flag.setEntry(entry.time, scalingValue);
							}
							foundScalingFlag = true;
						}
					}
					if( !foundScalingFlag ) {
						final ArrayList<Integer> times = new ArrayList<Integer>(1);
						times.add(0);
						final ArrayList<Vertex> values = new ArrayList<Vertex>();
						values.add(scalingValue);
						final AnimFlag newScalingFlag = new AnimFlag("Scaling", times, values);
						newScalingFlag.generateTypeId();
						newScalingFlag.addTag("Linear");
						atch.add(newScalingFlag);
					}
				}
			});

			horizontalGroup.addComponent(modelButton);
			horizontalGroup.addComponent(label);
			horizontalGroup.addGroup(layout.createSequentialGroup()
					.addComponent(bar)
					.addGap(8)
					.addComponent(spinner));
			horizontalGroup.addComponent(scaleLabel);
			verticalGroup.addComponent(modelButton);
			verticalGroup.addComponent(label);
			verticalGroup.addGroup(layout.createParallelGroup()
					.addComponent(bar)
					.addComponent(spinner));
			verticalGroup.addComponent(scaleLabel);
			verticalGroup.addGap(32);
		}

		horizontalGroup
		.addComponent(save)
		.addComponent(saveTip);

		verticalGroup
		.addComponent(save)
		.addGap(4)
		.addComponent(saveTip)
		.addGap(84)
		.addComponent(back)
		.addGap(16);

		setLayout(layout);

		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model \"*.mdx\"", "mdx"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text-based Model \"*.mdl\"", "mdl"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(jfc.getChoosableFileFilters()[0]);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if( e.getSource() == back ) {
			frame.jumpToPanel(previousPanel);
		} else if (e.getSource() == save) {
            final int returnValue = jfc.showSaveDialog(this);
            File temp = jfc.getSelectedFile();
            if( returnValue == JFileChooser.APPROVE_OPTION )
            {
                if( temp != null )
                {
                	final FileFilter ff = jfc.getFileFilter();
                	final String ext = ff.accept(new File("junk.mdl")) ? ".mdl" : ".mdx";
                    final String name = temp.getName();
                    if( name.lastIndexOf('.') != -1 )
                    {
                        if( !name.substring(name.lastIndexOf('.'),name.length()).equals(ext) )
                        {
                            temp = ( new File(temp.getAbsolutePath().substring(0,temp.getAbsolutePath() .lastIndexOf('.'))+ext));
                        }
                    }
                    else
                    {
                        temp = ( new File(temp.getAbsolutePath()+ext));
                    }
                    final File currentFile = temp;
                    if( temp.exists() )
                    {
                        final Object [] options = {"Overwrite","Cancel"};
                        final int n = JOptionPane.showOptionDialog(frame,"Selected file already exists.","Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[1]);
                        if( n == 1 )
                        {
                            jfc.setSelectedFile(null);
                            return;
                        }
                    }
//                    profile.setPath(currentFile.getParent());
                    baseModel.printTo(currentFile);
                    baseModel.setFile(currentFile);
                    //baseModel.resetBeenSaved();
//                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),currentFile.getName().split("\\.")[0]);
//                    tabbedPane.setToolTipTextAt(tabbedPane.getSelectedIndex(),currentFile.getPath());
                }
                else
                {
                    JOptionPane.showMessageDialog(this,"You tried to save, but you somehow didn't select a file.\nThat is unfortunate.");
                }
            }
            jfc.setSelectedFile(null);
		}
	}
}
