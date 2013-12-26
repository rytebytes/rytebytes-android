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

        String instructions = "1.) PUT THE BAGS IN A POT  (NO MORE THAN 2 PASTA MEALS  OR 6 INDIVIDUAL COMPONENTS)\n" +
                "\n" +
                "2.) FILL WITH HOT WATER UNTIL  BAGS ARE COVERED\n" +
                "\n" +
                "3.) PUT THE POT ON YOUR BIGGEST BURNER, TURN ON HIGH AND  COVER WITH THE LID.\n" +
                "\n" +
                "4.) HEAT FOR THE FOLLOWING TIME, BASED ON THE NUMBER OF BAGS:\n" +
                "\n" +
                "1 - 3 BAGS : 20 MINUTES\n" +
                "\n" +
                "4 - 6 BAGS : 25 MINUTES\n" +
                "\n";

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
