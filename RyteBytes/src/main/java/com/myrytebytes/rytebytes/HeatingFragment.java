package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HeatingFragment extends BaseFragment {

	public static HeatingFragment newInstance() {
		return new HeatingFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_heating, container, false);

        String instructions = "Remember - keep your items frozen until you cook them!  They should not be left out or in the fridge before cooking.  Always go from freezer to pot when heating.\n" +
                "\n" +
                "Heating your RyteBytes meal is as simple as heating water! \n" +
                "\n" +
                "1.) Place the bags in a pot (no more than 2 pasta meals or 6 individual components).\n" +
                "\n" +
                "2.) Fill with hot water until bags are covered.\n" +
                "\n" +
                "3.) Put the pot on your biggest burner, turn on high and cover with the lid.\n" +
                "\n" +
                "4.) Bring the water to a boil and allow to boil for the following time:\n" +
                "\n" +
                "1 - 3 bags : 5 minutes\n" +
                "\n" +
                "4 - 6 bags : 15 minutes";

        TextView tvInstructions = (TextView)rootView.findViewById(R.id.tv_heating_instructions);
        tvInstructions.setText(instructions);

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
