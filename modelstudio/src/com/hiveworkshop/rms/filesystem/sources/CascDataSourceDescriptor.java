package com.hiveworkshop.rms.filesystem.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CascDataSourceDescriptor implements DataSourceDescriptor {
	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 832549098549298820L;
	private final String gameInstallPath;
	private final List<String> prefixes;

	public CascDataSourceDescriptor(final String gameInstallPath, final List<String> prefixes) {
		this.gameInstallPath = gameInstallPath;
		this.prefixes = prefixes;
	}

	@Override
	public DataSource createDataSource() {
		return new CascDataSource(gameInstallPath, prefixes.toArray(new String[0]));
	}

	@Override
	public String getDisplayName() {
		return "CASC: " + gameInstallPath;
	}

	public void addPrefix(final String prefix) {
		prefixes.add(prefix);
	}

	public void deletePrefix(final int index) {
		prefixes.remove(index);
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

	public String getGameInstallPath() {
		return gameInstallPath;
	}

	public List<String> getPrefixes() {
		return prefixes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((gameInstallPath == null) ? 0 : gameInstallPath.hashCode());
		result = (prime * result) + ((prefixes == null) ? 0 : prefixes.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CascDataSourceDescriptor other = (CascDataSourceDescriptor) obj;
		if (gameInstallPath == null) {
			if (other.gameInstallPath != null) {
				return false;
			}
		} else if (!gameInstallPath.equals(other.gameInstallPath)) {
			return false;
		}
		if (prefixes == null) {
			return other.prefixes == null;
		} else {
			return prefixes.equals(other.prefixes);
		}
	}

	@Override
	public DataSourceDescriptor duplicate() {
		return new CascDataSourceDescriptor(gameInstallPath, new ArrayList<>(prefixes));
	}
}
