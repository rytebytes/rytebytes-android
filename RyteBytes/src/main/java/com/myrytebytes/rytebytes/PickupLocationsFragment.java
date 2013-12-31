package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamodel.Location;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.widget.ButtonSpinner;
import com.myrytebytes.widget.ButtonSpinner.ButtonSpinnerListener;
import com.myrytebytes.widget.HoloDialog;

import java.util.ArrayList;
import java.util.List;

public class PickupLocationsFragment extends BaseFragment {

    private List<Location> mLocations;
    private Location mLocation;

    private Dialog mProgressDialog;

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
    private final ButtonSpinnerListener mButtonSpinnerListener = new ButtonSpinnerListener() {
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
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_set_pickup_location:
                    mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Updating Account", "Please wait...");
                    //TODO:
                    break;
                case R.id.spinner:
                    if (mLocations.size() == 0) {
                        mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Fetching Locations", "Please wait...");
                    }
                    break;
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
        View view = inflater.inflate(R.layout.fragment_pickup_locations, container, false);

        ((ButtonSpinner)view.findViewById(R.id.spinner)).setListener(mButtonSpinnerListener);
        view.findViewById(R.id.btn_set_pickup_location).setOnClickListener(mOnClickListener);

        Spanned htmlText = Html.fromHtml("Your pickup location is currently set to:<br><br><b>Fake Location<br>2000 W. Street</b><br><br>Use the button below to change locations if needed.");
        ((TextView)view.findViewById(R.id.tv_current_location)).setText(htmlText);

        return view;
    }

    @Override
    protected void onShown() { }

    @Override
    protected ContentType getContentType() {
        return ContentType.PICKUP_LOCATIONS;
    }

    @Override
    protected int getTitle() {
        return R.string.pickup_locations;
    }
}
