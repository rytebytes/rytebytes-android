package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DoRyteFragment extends BaseFragment {

	public static DoRyteFragment newInstance() {
		return new DoRyteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_do_ryte, container, false);

		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.DO_RYTE;
	}

	@Override
	protected int getTitle() {
		return R.string.do_ryte;
	}

	@Override
	protected void onShown() {

	}
}
