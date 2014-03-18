package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.datamodel.HeatingInstructions;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.UpdateHeatingInstructionsListener;

public class HeatingFragment extends BaseFragment {

    private TextView mTvInstructions;
	private final UpdateHeatingInstructionsListener mUpdateHeatingInstructionsListener = new UpdateHeatingInstructionsListener() {
        @Override
        public void onComplete(boolean updated) {
            if (updated) {
                Logr.d("updated!");
                setHeatingInstructionsText();
            } else {
                Logr.d("not updated");
            }
        }
    };

    public static HeatingFragment newInstance() {
		return new HeatingFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_heating, container, false);

        mTvInstructions = (TextView)rootView.findViewById(R.id.tv_heating_instructions);
        setHeatingInstructionsText();

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
        ApiInterface.updateHeatingInstructions(mUpdateHeatingInstructionsListener);
	}

    /*package*/ void setHeatingInstructionsText() {
        mTvInstructions.setText(HeatingInstructions.getPersistedText(getApplicationContext()));
    }
}
