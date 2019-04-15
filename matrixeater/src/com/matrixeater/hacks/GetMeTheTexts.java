package com.matrixeater.hacks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.etheller.collections.List;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.objectdata.Change;
import com.hiveworkshop.wc3.units.objectdata.ObjectDataChangeEntry;
import com.hiveworkshop.wc3.units.objectdata.ObjectMap;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeTheTexts {
	private static final String[] fieldsWeCareAbout = { "atp1", "aut1", "auu1", "aret", "arut", "arhk" };

	public static void main(final String[] args) {
		final java.util.List<War3ID> idsWeCareAbout = new ArrayList<>();
		for (final String field : fieldsWeCareAbout) {
			idsWeCareAbout.add(War3ID.fromString(field));
		}
		final War3ObjectDataChangeset war3ObjectDataChangeset = new War3ObjectDataChangeset('a');
		final DataTable standardAbilityMeta = StandardObjectData.getStandardAbilityMeta();
		try {
			war3ObjectDataChangeset.load(new BlizzardDataInputStream(
					new FileInputStream("C:/users/micro/downloads/VERY_SECRET/war3map.w3a")), null, false);

			final ObjectMap custom = war3ObjectDataChangeset.getCustom();
			for (final War3ID key : custom.keySet()) {
				final ObjectDataChangeEntry objectDataChangeEntry = custom.get(key);
				for (final War3ID metaDataKey : idsWeCareAbout) {
					final List<Change> dataField = objectDataChangeEntry.getChanges().get(metaDataKey);
					if (dataField != null) {
						final Element metaDataEntry = standardAbilityMeta.get(metaDataKey.toString());
						final String fieldName = metaDataEntry.getField("field");
						for (final Change levelChange : dataField) {
							System.out.println(fieldName + " = " + levelChange.getStrval());
							JOptionPane.showMessageDialog(null, fieldName + " = " + levelChange.getStrval());
						}
					}
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
