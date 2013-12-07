package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccountFragment extends BaseFragment {

	public static AccountFragment newInstance() {
		return new AccountFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_heating, container, false);

		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.ACCOUNT;
	}

	@Override
	protected int getTitle() {
		return R.string.account;
	}

	@Override
	protected void onShown() {

	}
}
