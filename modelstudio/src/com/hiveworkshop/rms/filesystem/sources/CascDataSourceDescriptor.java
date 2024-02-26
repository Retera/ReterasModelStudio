package com.hiveworkshop.rms.filesystem.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CascDataSourceDescriptor implements DataSourceDescriptor {
	private static final long serialVersionUID = 832549098549298820L;
	private final String gameInstallPath;
	private final List<String> prefixes;

	public CascDataSourceDescriptor(final String gameInstallPath) {
		this(gameInstallPath, new ArrayList<>());
	}

	public CascDataSourceDescriptor(final String gameInstallPath, final List<String> prefixes) {
		this.gameInstallPath = gameInstallPath;
		this.prefixes = prefixes;
	}

	@Override
	public CascDataSource createDataSource() {
		return new CascDataSource(gameInstallPath, prefixes.toArray(new String[0]));
	}

	@Override
	public String getDisplayName() {
		return "CASC: " + gameInstallPath;
	}

	public CascDataSourceDescriptor addPrefix(final String prefix) {
		prefixes.add(prefix);
		return this;
	}

	public CascDataSourceDescriptor addPrefixes(List<String> prefixes) {
		this.prefixes.addAll(prefixes);
		return this;
	}

	public CascDataSourceDescriptor removePrefix(final int index) {
		prefixes.remove(index);
		return this;
	}

	public void movePrefixUp(final int index) {
		if (index > 0) {
			Collections.swap(prefixes, index, index - 1);
		}
	}

	public void movePrefixDown(final int index) {
		if (index < (prefixes.size() - 1)) {
			Collections.swap(prefixes, index, index + 1);
		}
	}

	public void movePrefix(final int index, int dir) {
		if (index + dir >= 0 && index+dir < prefixes.size()) {
			Collections.swap(prefixes, index, index + dir);
		}
	}

	public String getPath() {
		return gameInstallPath;
	}

	public List<String> getPrefixes() {
		return prefixes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Objects.hashCode(gameInstallPath);
		result = (prime * result) + Objects.hashCode(prefixes);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof final CascDataSourceDescriptor other) {
			return Objects.equals(gameInstallPath, other.gameInstallPath) && Objects.equals(prefixes, other.prefixes);
		}
		return false;
	}

	@Override
	public CascDataSourceDescriptor duplicate() {
		return new CascDataSourceDescriptor(gameInstallPath, new ArrayList<>(prefixes));
	}

	@Override
	public String toString() {
		return DataSourceDescriptor.toSaveString(this);
	}

	public CascDataSourceDescriptor parsePrefixes(String prefixString) {
		String[] prefixes = prefixString.replaceAll("(^\\[)|(]$)", "").split("(?<=\"), ((?=\")|$)");
		for (String prefix : prefixes) {
			if (!prefix.isBlank()) {
				addPrefix(prefix.replaceAll("(^\")|(\"$)", ""));
			}
		}
		return this;
	}
}
