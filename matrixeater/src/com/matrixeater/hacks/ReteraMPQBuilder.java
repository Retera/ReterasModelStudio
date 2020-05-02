package com.matrixeater.hacks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class ReteraMPQBuilder {

	public static void main(final String[] args) {
		final String fileList = "ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp\r\n"
				+ "ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp\r\n" + "Units\\CampaignUnitFunc.txt\r\n"
				+ "Units\\CampaignUnitStrings.txt\r\n" + "Units\\HumanUnitFunc.txt\r\n"
				+ "Units\\HumanUnitStrings.txt\r\n" + "Units\\NeutralUnitFunc.txt\r\n"
				+ "Units\\NeutralUnitStrings.txt\r\n" + "Units\\NightElfUnitFunc.txt\r\n"
				+ "Units\\NightElfUnitStrings.txt\r\n" + "Units\\OrcUnitFunc.txt\r\n" + "Units\\OrcUnitStrings.txt\r\n"
				+ "Units\\UndeadUnitFunc.txt\r\n" + "Units\\UndeadUnitStrings.txt\r\n" + "Units\\UnitAbilities.slk\r\n"
				+ "Units\\UnitBalance.slk\r\n" + "Units\\UnitData.slk\r\n" + "Units\\UnitUI.slk\r\n"
				+ "Units\\UnitWeapons.slk\r\n" + "Units\\UnitMetaData.slk\r\n" + "UI\\WorldEditStrings.txt\r\n"
				+ "ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor00.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor01.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor02.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor03.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor04.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor05.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor06.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor07.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor08.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor09.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor10.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor11.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor12.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor13.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor14.blp\r\n"
				+ "ReplaceableTextures\\TeamColor\\TeamColor15.blp";
		final String[] files = fileList.split("\r\n");
		for (final String file : files) {
			try (InputStream stream = MpqCodebase.get().getResourceAsStream(file)) {
				final Path target = Paths.get("C:\\Temp\\ReteraMPQ", file);
				Files.createDirectories(target.getParent());
				Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
