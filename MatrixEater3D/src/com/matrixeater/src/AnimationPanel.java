package com.matrixeater.src;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AnimationPanel extends JPanel implements ActionListener {
	
	public static class SliderBarHandler implements ChangeListener {
		JSlider bar;
		JSpinner spinner;
		boolean isAdjusting = false;
		
		public SliderBarHandler(JSlider slider, JSpinner spinner) {
			this.bar = slider;
			this.spinner = spinner;
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			if( !isAdjusting ) {
				if( e.getSource() == bar ) {
					isAdjusting = true;
					
//					if( bar.getValue() > 7500 ) {
//						bar.setValue(7500);
//					}
					
					spinner.setValue(bar.getValue() / 1000.00);
					isAdjusting = false;
				}
				else if( e.getSource() == spinner ) {
					isAdjusting = true;
					if( !( spinner.getValue() instanceof Integer )) {
//						if( (int)(((Double)spinner.getValue()).doubleValue() * 100) > 7500 ) {
//							spinner.setValue(75.00);
//						}

						bar.setValue((int)(((Double)spinner.getValue()).doubleValue() * 1000));
					}
					
					isAdjusting = false;
				}
			}
		}
		
	}

	List<SliderBarHandler> bars = new ArrayList<SliderBarHandler>();
	JButton okay, cancel;
	MDLDisplay mdlDisp;
	AnimationFrame parentFrame;
	public AnimationPanel(MDLDisplay mdlDisp, AnimationFrame frame) {
		this.mdlDisp = mdlDisp;
		parentFrame = frame;
		GridLayout layout = new GridLayout(mdlDisp.getMDL().getAnimsSize() * 2 + 2, 2);
		setLayout(layout);
		
		for( Animation anim: mdlDisp.getMDL().m_anims ) {
			JLabel label = new JLabel(anim.getName() + " ("+anim.length() / 1000.00 + " s)");
			int maxLength = anim.length() * 4;
			JSlider bar = new JSlider(0, maxLength);
			bar.setValue(anim.length());
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(anim.length() / 1000.00, 0.0, maxLength / 1000.00, 0.001));
			
			SliderBarHandler handler = new SliderBarHandler(bar, spinner);
			bar.addChangeListener(handler);
			spinner.addChangeListener(handler);
//			defBar.addChangeListener(this);
//			defSpinner.addChangeListener(this);
			bars.add(handler);
			
			add(label);
			add(new JSeparator());
			add(bar);
			add(spinner);
		}
		
		okay = new JButton("OK");
		okay.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		add(cancel);
		add(okay);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == cancel ) {
			parentFrame.setVisible(false);
		}
		if( e.getSource() == okay ) {
			MDL mdl = mdlDisp.getMDL();
			int myAnimationsIndex = 0;
			for( Animation myAnimation: mdl.m_anims ) {
				
				int newLength = bars.get(myAnimationsIndex).bar.getValue();
				int lengthIncrease = (newLength) - myAnimation.length();
				
				if( lengthIncrease > 0 ) {
					// first move all the animations after it, so that when we make it 2x as long we don't get interlocking keyframes

					// (getAnimsSize is a badly named "number of animations" function)
					for(int index = mdl.getAnimsSize() - 1; index > myAnimationsIndex; index--) {
					    Animation anim = mdl.getAnim(index);
					    int startOfAnim = anim.getIntervalStart(); // I didn't know eclipse is smart enough to write functions like this one for me, so I haven't pushed "generate getters and setters" to auto-write this function, but it could exist
					    int endOfAnim = anim.getIntervalEnd(); // same
					    anim.setInterval(startOfAnim + lengthIncrease, endOfAnim + lengthIncrease, mdl);
					}
				}

				// now actually scale animation
				myAnimation.setInterval(myAnimation.getIntervalStart(), myAnimation.getIntervalStart() + newLength, mdl);
				myAnimationsIndex++;
			}
			
			mdlDisp.setBeenSaved(false);
			parentFrame.setVisible(false);
		}
	}
}
