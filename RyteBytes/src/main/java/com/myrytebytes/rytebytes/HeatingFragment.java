package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HeatingFragment extends BaseFragment {

	public static HeatingFragment newInstance() {
		return new HeatingFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_heating, container, false);

		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.HEATING;
	}

	@Override
	protected int getTitle() {
		return R.string.heating;
	}

	@Override
	protected void onShown() {

	}
}
