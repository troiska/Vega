package com.subgraph.vega.ui.scanner.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.subgraph.vega.api.scanner.IScannerConfig;
import com.subgraph.vega.ui.scanner.Activator;

public class ScannerOptionsPreferenceInitializer extends
		AbstractPreferenceInitializer {

	public ScannerOptionsPreferenceInitializer() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("MaxScanDescendants", IScannerConfig.DEFAULT_MAX_DESCENDANTS);
		store.setDefault("MaxScanChildren", IScannerConfig.DEFAULT_MAX_CHILDREN);
		store.setDefault("MaxScanDepth", IScannerConfig.DEFAULT_MAX_DEPTH);
		store.setDefault("MaxScanDuplicatePaths", IScannerConfig.DEFAULT_MAX_DUPLICATE_PATHS);
		store.setDefault("MaxRequestsPerSecond", IScannerConfig.DEFAULT_MAX_REQUEST_PER_SECOND);
		store.setDefault("MaxAlertString", 400);
	}

	@Override
	public void initializeDefaultPreferences() {
	}
}
