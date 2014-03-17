package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.widget.ButtonSpinner;
import com.myrytebytes.widget.ButtonSpinner.ButtonSpinnerListener;
import com.myrytebytes.widget.HoloDialog;

import java.util.ArrayList;
import java.util.List;

public class IntroFragment extends BaseFragment {

    private List<Location> mLocations;
    private Location mLocation;

    private Dialog mProgressDialog;

	private ButtonSpinner mSpinnerLocations;
    private final ButtonSpinnerListener mSpinnerListener = new ButtonSpinnerListener() {
        @Override
        public String[] getDropdownContents(ButtonSpinner spinner) {
            String[] contents = new String[mLocations.size()];
            for (int i = 0; i < mLocations.size(); i++) {
                contents[i] = mLocations.get(i).name;
            }
            return contents;
        }

        @Override
        public void onItemSelected(int index, ButtonSpinner spinner) {
            mLocation = mLocations.get(index);
        }
    };
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.spinner:
                    if (mLocations.size() == 0) {
                        mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Fetching Locations", "Please wait...");
                    }
                    break;
                case R.id.btn_get_started:
                    if (mLocation != null) {
                        UserController.setDefaultPickupLocation(mLocation);
                        mActivityCallbacks.replaceContent(ContentType.MENU);
                    } else {
                        showOkDialog("No Pickup Location", "Please select the location where you'd like to pick up your food before proceeding.");
                    }
                    break;
            }
        }
    };
    private final GetLocationsListener mGetLocationsListener = new GetLocationsListener() {
        @Override
        public void onComplete(List<Location> locations, int statusCode) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            if (locations == null) {
                new HoloDialog.Builder(getActivity())
                        .setTitle("Error")
                        .setMessage("An error occurred while retrieving the available pickup locaitons. Please try again later.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
            } else {
                mLocations.clear();
                mLocations.addAll(locations);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiInterface.getLocations(mGetLocationsListener);
        mLocations = new ArrayList<>();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        mSpinnerLocations = (ButtonSpinner)rootView.findViewById(R.id.spinner);
        mSpinnerLocations.setListener(mSpinnerListener);
        mSpinnerLocations.setOnClickListener(mOnClickListener);
        rootView.findViewById(R.id.btn_get_started).setOnClickListener(mOnClickListener);
		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.INTRO;
	}

	@Override
	protected int getTitle() {
		return 0;
	}

	@Override
	protected void onShown() { }
}
