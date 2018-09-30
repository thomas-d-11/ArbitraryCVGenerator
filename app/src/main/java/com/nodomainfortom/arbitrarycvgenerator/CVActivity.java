package com.nodomainfortom.arbitrarycvgenerator;

import android.support.v4.app.Fragment;

public class CVActivity extends SingleFragmentActivity {
	@Override
	protected Fragment createFragment() {
		return ControlsFragment.newInstance();
	}

}
