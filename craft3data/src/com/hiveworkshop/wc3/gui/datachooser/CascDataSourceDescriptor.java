package com.hiveworkshop.wc3.gui.datachooser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.hiveworkshop.wc3.gui.datachooser.CascDataSource.Product;

public class CascDataSourceDescriptor implements DataSourceDescriptor {
	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 832549098549298820L;
	private final String gameInstallPath;
	private final List<String> prefixes;
	private final CascDataSource.Product product;

	public CascDataSourceDescriptor(final String gameInstallPath, final List<String> prefixes, CascDataSource.Product product) {
		this.gameInstallPath = gameInstallPath;
		this.prefixes = prefixes;
		this.product = product;
	}

	@Override
	public DataSource createDataSource() {
		CascDataSource.Product product = this.product;
		if(product == null) {
			product = Product.WARCRAFT_III;
		}
		return new CascDataSource(gameInstallPath, prefixes.toArray(new String[prefixes.size()]), product);
	}

	@Override
	public String getDisplayName() {
		return "CASC: " + gameInstallPath + " ("+ product+")";
	}

	public void addPrefix(final String prefix) {
		this.prefixes.add(prefix);
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
		return Objects.hash(gameInstallPath, prefixes, product);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CascDataSourceDescriptor other = (CascDataSourceDescriptor) obj;
		return Objects.equals(gameInstallPath, other.gameInstallPath) && Objects.equals(prefixes, other.prefixes)
				&& product == other.product;
	}

	@Override
	public DataSourceDescriptor duplicate() {
		return new CascDataSourceDescriptor(gameInstallPath, new ArrayList<>(prefixes), product);
	}
	
	public CascDataSource.Product getProduct() {
		return product;
	}
}
