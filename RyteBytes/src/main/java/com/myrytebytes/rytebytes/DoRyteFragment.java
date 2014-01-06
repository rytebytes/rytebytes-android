package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.Charity;
import com.myrytebytes.datamodel.User;
import com.myrytebytes.widget.NetworkImageView;

public class DoRyteFragment extends BaseFragment {

    private TextView mTvTotalDonations;
    private TextView mTvTitle;
    private TextView mTvInfo;
    private TextView mTvLoggedOut;
    private NetworkImageView mImgLogo;

	public static DoRyteFragment newInstance() {
		return new DoRyteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_do_ryte, container, false);

        mTvTitle = (TextView)rootView.findViewById(R.id.tv_title);
        mTvInfo = (TextView)rootView.findViewById(R.id.tv_info);
        mImgLogo = (NetworkImageView)rootView.findViewById(R.id.img_charity);
        mTvTotalDonations = (TextView)rootView.findViewById(R.id.tv_estimated_donation);
        mTvLoggedOut = (TextView)rootView.findViewById(R.id.tv_logged_out);

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
        User user = UserController.getActiveUser();
        if (user != null) {
            Charity charity = UserController.getActiveUser().location.charity;
            mTvLoggedOut.setVisibility(View.GONE);
            mTvTitle.setVisibility(View.VISIBLE);
            mTvInfo.setVisibility(View.VISIBLE);
            mImgLogo.setVisibility(View.VISIBLE);
            mTvTotalDonations.setVisibility(View.VISIBLE);

            mTvTitle.setText(charity.name);
            mTvInfo.setText(charity.description);
            mImgLogo.setImageReference("charityimages", charity.image);
            mTvTotalDonations.setText("Total monthly donations:  " + "$120");
        } else {
            mTvLoggedOut.setVisibility(View.VISIBLE);
            mTvTitle.setVisibility(View.GONE);
            mTvInfo.setVisibility(View.GONE);
            mImgLogo.setVisibility(View.GONE);
            mTvTotalDonations.setVisibility(View.GONE);
        }
    }
}
