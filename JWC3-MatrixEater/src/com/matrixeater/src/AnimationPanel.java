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

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.MDL;

public class AnimationPanel extends JPanel implements ActionListener {

	List<SliderBarHandler> bars = new ArrayList<SliderBarHandler>();
	JButton okay, cancel;
	MDLDisplay mdlDisp;
	AnimationFrame parentFrame;
	public AnimationPanel(final MDLDisplay mdlDisp, final AnimationFrame frame) {
		this.mdlDisp = mdlDisp;
		parentFrame = frame;
		final GridLayout layout = new GridLayout((mdlDisp.getMDL().getAnimsSize() + mdlDisp.getMDL().getGlobalSeqs().size()) * 2 + 2, 2);
		setLayout(layout);

		for( final Animation anim: mdlDisp.getMDL().getAnims() ) {
			final JLabel label = new JLabel(anim.getName() + " ("+anim.length() / 1000.00 + " s)");
			final int maxLength = anim.length() * 4;
			final JSlider bar = new JSlider(0, maxLength);
			bar.setValue(anim.length());
			final JSpinner spinner = new JSpinner(new SpinnerNumberModel(anim.length() / 1000.00, 0.0, maxLength / 1000.00, 0.001));

			final SliderBarHandler handler = new SliderBarHandler(bar, spinner);
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
		int i = 0;
		for( final Integer globalSeq: mdlDisp.getMDL().getGlobalSeqs() ) {
			final JLabel label = new JLabel("Global Sequence " + ++i + " ("+globalSeq / 1000.00 + " s)");
			final int maxLength = globalSeq * 4;
			final JSlider bar = new JSlider(0, maxLength);
			bar.setValue(globalSeq);
			final JSpinner spinner = new JSpinner(new SpinnerNumberModel(globalSeq / 1000.00, 0.0, maxLength / 1000.00, 0.001));

			final SliderBarHandler handler = new SliderBarHandler(bar, spinner);
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
	public void actionPerformed(final ActionEvent e) {
		if( e.getSource() == cancel ) {
			parentFrame.setVisible(false);
		}
		if( e.getSource() == okay ) {
			final MDL mdl = mdlDisp.getMDL();
			int myAnimationsIndex = 0;
			for( final Animation myAnimation: mdl.getAnims() ) {

				final int newLength = bars.get(myAnimationsIndex).bar.getValue();
				final int lengthIncrease = (newLength) - myAnimation.length();

				if( lengthIncrease > 0 ) {
					// first move all the animations after it, so that when we make it 2x as long we don't get interlocking keyframes

					// (getAnimsSize is a badly named "number of animations" function)
					for(int index = mdl.getAnimsSize() - 1; index > myAnimationsIndex; index--) {
					    final Animation anim = mdl.getAnim(index);
					    final int startOfAnim = anim.getIntervalStart(); // I didn't know eclipse is smart enough to write functions like this one for me, so I haven't pushed "generate getters and setters" to auto-write this function, but it could exist
					    final int endOfAnim = anim.getIntervalEnd(); // same
					    anim.setInterval(startOfAnim + lengthIncrease, endOfAnim + lengthIncrease, mdl);
					}
				}

				// now actually scale animation
				myAnimation.setInterval(myAnimation.getIntervalStart(), myAnimation.getIntervalStart() + newLength, mdl);
				myAnimationsIndex++;
			}
			for( final Integer myAnimation: mdl.getGlobalSeqs() ) {

				final int newLength = bars.get(myAnimationsIndex).bar.getValue();
				final int lengthIncrease = (newLength) - myAnimation;

				for(final AnimFlag flag: mdl.getAllAnimFlags()) {
					if( flag.hasGlobalSeq() && flag.getGlobalSeq().equals(myAnimation) ) {
						flag.timeScale(0, myAnimation, 0, newLength);
						flag.setGlobSeq(new Integer(newLength));
					}
				}
				myAnimationsIndex++;
			}

			mdlDisp.setBeenSaved(false);
			parentFrame.setVisible(false);
		}
	}
}
