package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.Charity;
import com.myrytebytes.widget.NetworkImageView;

public class DoRyteFragment extends BaseFragment {

    private TextView mTvTotalDonations;

	public static DoRyteFragment newInstance() {
		return new DoRyteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_do_ryte, container, false);

        Charity charity = UserController.getActiveUser().location.charity;

        TextView tvTitle = (TextView)rootView.findViewById(R.id.tv_title);
        TextView tvInfo = (TextView)rootView.findViewById(R.id.tv_info);
        NetworkImageView imgLogo = (NetworkImageView)rootView.findViewById(R.id.img_charity);
        mTvTotalDonations = (TextView)rootView.findViewById(R.id.tv_estimated_donation);

        tvTitle.setText(charity.name);
        tvInfo.setText(charity.description);
        imgLogo.setImageReference("charityimages", charity.image);
        mTvTotalDonations.setText("$120");

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
