package com.hiveworkshop.modding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.JStormLib.MPQArchive;
import com.JStormLib.MPQArchiveException;
import com.JStormLib.StormLibWin;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class InputGatherer {
	public static void main(final String[] args) throws FileNotFoundException, IOException, MPQArchiveException {
		final InputStream blizzardJFile = MpqCodebase.get().getResourceAsStream("Scripts\\Blizzard.j");
		Files.copy(blizzardJFile, new File("Blizzard.j").toPath());
		final InputStream commonJFile = MpqCodebase.get().getResourceAsStream("Scripts\\common.j");
		Files.copy(commonJFile, new File("common.j").toPath());
		final MPQArchive map = MPQArchive.openArchive(new File("Derpa.w3m"), StormLibWin.MPQ_FLAG_READ_ONLY);
		map.extractFile("war3map.j", new File("war3map.j"));
	}
}
