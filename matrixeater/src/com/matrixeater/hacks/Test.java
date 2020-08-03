package com.matrixeater.hacks;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.units.ModelOptionPane;

public class Test {
	public static void main(final String[] args) {
		LwjglNativesLoader.load();
		final String show = ModelOptionPane.show(null);
	}
}