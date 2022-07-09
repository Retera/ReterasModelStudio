package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class OtherUtils {

	public static String loadShader(final String path) {
		InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream("shaders\\" + path);
		BufferedReader r = new BufferedReader(new InputStreamReader(resourceAsStream));
		StringBuilder builder = new StringBuilder();
		Stream<String> lines = r.lines();
		lines.forEach(l -> builder.append(l).append("\n"));
		return builder.toString();
	}
}
