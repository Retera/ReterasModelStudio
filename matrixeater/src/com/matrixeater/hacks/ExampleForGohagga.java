package com.matrixeater.hacks;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.UnitOptionPane;

public class ExampleForGohagga {
    public static void main(String[] args) {
        LwjglNativesLoader.load();
        GameObject chosenUnit = UnitOptionPane.show(null);
        System.out.println(chosenUnit.getField("File"));
    }
}
