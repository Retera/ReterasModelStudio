package com.matrixeater.src;

import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderBarHandler implements ChangeListener {
		JSlider bar;
		JSpinner spinner;
		boolean isAdjusting = false;

		public SliderBarHandler(final JSlider slider, final JSpinner spinner) {
			this.bar = slider;
			this.spinner = spinner;
		}

		@Override
		public void stateChanged(final ChangeEvent e) {
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