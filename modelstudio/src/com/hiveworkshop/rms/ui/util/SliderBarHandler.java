package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderBarHandler implements ChangeListener {
    public JSlider bar;
    public JSpinner spinner;
    boolean isAdjusting = false;

    public SliderBarHandler(int length) {
        int maxLengthSlider = Math.max(1000, length * 4);
        int maxLengthSpinner = Math.max(100000, length * 4);
        bar = new JSlider(0, maxLengthSlider, length);
        spinner = new JSpinner(new SpinnerNumberModel(length / 1000.00, 0.0, maxLengthSpinner / 1000.00, 0.001));

        bar.addChangeListener(this);
        spinner.addChangeListener(this);
    }

    public SliderBarHandler(final JSlider slider, final JSpinner spinner) {
        bar = slider;
        this.spinner = spinner;
    }

    public JSlider getBar() {
        return bar;
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public int getValue() {
        return bar.getValue();
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
                    if (((Double) spinner.getValue() * 1000) > bar.getMaximum()) {
                        spinner.setValue(bar.getMaximum() / 1000.00);
                    }
                    bar.setValue((int) ((Double) spinner.getValue() * 1000));
                }

                isAdjusting = false;
            }
        }
    }

}