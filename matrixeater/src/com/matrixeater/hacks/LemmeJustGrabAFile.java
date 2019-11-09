package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;

import mpq.MPQException;

public class LemmeJustGrabAFile {
	public static void main(final String[] args) {
		final File file = new File("C:\\Users\\micro\\Downloads\\Goblin_Survival_1.3b3.w3x");
		try {
			final LoadedMPQ loadMPQ = MpqCodebase.get().loadMPQ(file.toPath());
			System.out.println(loadMPQ.has("war3map.j"));
			System.out.println(loadMPQ.has("scripts\\war3map.j"));
			loadMPQ.unload();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final MPQException e) {
			e.printStackTrace();
		}
	}
}
