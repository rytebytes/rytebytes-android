package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.widget.NetworkImageView;

public class DoRyteFragment extends BaseFragment {

    private TextView mTvTotalDonations;
    private TextView mTvMyDonations;

	public static DoRyteFragment newInstance() {
		return new DoRyteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_do_ryte, container, false);

        TextView tvTitle = (TextView)rootView.findViewById(R.id.tv_title);
        NetworkImageView imgLogo = (NetworkImageView)rootView.findViewById(R.id.img_charity);
        mTvTotalDonations = (TextView)rootView.findViewById(R.id.tv_estimated_donation);
        mTvMyDonations = (TextView)rootView.findViewById(R.id.tv_my_donation);

        tvTitle.setText("Starbucks");
        imgLogo.setImageUrl("http://bizcolostate.files.wordpress.com/2013/08/starbucks-coffee-logo.gif");
        mTvTotalDonations.setText("$120");
        mTvMyDonations.setText("$12");

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
	protected void onShown() { }
}
