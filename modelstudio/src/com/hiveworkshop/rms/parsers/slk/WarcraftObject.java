package com.hiveworkshop.rms.parsers.slk;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarcraftObject extends GameObject {
	WarcraftData dataSource;

	public WarcraftObject(final String id, final WarcraftData dataSource) {
		this.id = id;
		this.dataSource = dataSource;
		placeholderTexPath = "ReplaceableTextures\\WorldEditUI\\DoodadPlaceholder.blp";
	}

	@Override
	public void setField(final String field, final String value, final int index) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("1 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				element.setField(field, value, index);
				return;
			}
		}
	}

	@Override
	public void setField(final String field, final String value) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("2 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				element.setField(field, value);
				return;
			}
		}
		throw new IllegalArgumentException("no field");
	}

	@Override
	public String getField(final String field, final int index) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("3 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				return element.getField(field, index);
			}
		}
		return "";
	}

	@Override
	public int getFieldValue(final String field, final int index) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("4 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				return element.getFieldValue(field, index);
			}
		}
		return 0;
	}

	@Override
	public String getField(final String field) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("5 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				return element.getField(field);
			}
		}
		return "";
	}

	@Override
	public int getFieldValue(final String field) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("6 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				return element.getFieldValue(field);
			}
		}
		return 0;
	}

	/*
	 * (non-Javadoc) I'm not entirely sure this is still safe to use
	 *
	 * @see com.hiveworkshop.wc3.units.GameObject#getFieldAsList(java.lang. String)
	 */
	@Override
	public List<? extends GameObject> getFieldAsList(final String field, final ObjectData objectData) {
		for (final DataTable table : dataSource.getTables()) {
			final Element element = table.get(id);
			if ((element != null)){
				System.out.println("7 Found element for ID '" + id + "' in table '" + table + "', had field '" + field + "':" + (element.hasField(field)));
			}
			if ((element != null) && element.hasField(field)) {
				return element.getFieldAsList(field, objectData);
			}
		}
		return new ArrayList<>();// empty list if not found
	}

	@Override
	public ObjectData getTable() {
		return dataSource;
	}

	@Override
	public ImageIcon getScaledIcon(int size) {
		Image img = getImage();
		return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_FAST));
	}
	@Override
	public Set<String> keySet() {
		final Set<String> keySet = new HashSet<>();
		for (final DataTable table : dataSource.tables) {
			final Element element = table.get(id);
			if (element != null) {
				keySet.addAll(element.keySet());
			}
		}
		return keySet;
	}
}
