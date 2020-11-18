package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderBarHandler implements ChangeListener {
    public JSlider bar;
    public JSpinner spinner;
    boolean isAdjusting = false;

    public SliderBarHandler(final JSlider slider, final JSpinner spinner) {
        bar = slider;
        this.spinner = spinner;
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        if (!isAdjusting) {
            if (e.getSource() == bar) {
                isAdjusting = true;


                spinner.setValue(bar.getValue() / 1000.00);
                isAdjusting = false;
            } else if (e.getSource() == spinner) {
                isAdjusting = true;
                if (!(spinner.getValue() instanceof Integer)) {

                    bar.setValue((int) ((Double) spinner.getValue() * 1000));
                }

                isAdjusting = false;
            }
        }
    }

}