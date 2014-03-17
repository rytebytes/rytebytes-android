package com.myrytebytes.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class NutritionInformation implements JacksonParser, Parcelable {

	public Integer calories;
	public Integer protein;
	public Integer saturatedFat;
	public Integer sodium;
	public Integer carbs;

	public NutritionInformation() { }

	public NutritionInformation(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Logr.e("Error filling NutritionInformation", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "calories":
						calories = jsonParser.getIntValue();
						break;
					case "protein":
						protein = jsonParser.getIntValue();
						break;
					case "carbs":
						carbs = jsonParser.getIntValue();
						break;
					case "saturated_fat":
						saturatedFat = jsonParser.getIntValue();
						break;
					case "sodium":
						sodium = jsonParser.getIntValue();
						break;
				}
			}
		}, closeWhenComplete);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(calories != null ? calories : -1);
		out.writeInt(protein != null ? protein : -1);
		out.writeInt(saturatedFat != null ? saturatedFat : -1);
		out.writeInt(sodium != null ? sodium : -1);
		out.writeInt(carbs != null ? carbs : -1);
	}

	public static final Parcelable.Creator<NutritionInformation> CREATOR = new Parcelable.Creator<NutritionInformation>() {
		public NutritionInformation createFromParcel(Parcel in) {
			return new NutritionInformation(in);
		}

		public NutritionInformation[] newArray(int size) {
			return new NutritionInformation[size];
		}
	};

	private NutritionInformation(Parcel in) {
		calories = in.readInt();
		protein = in.readInt();
		saturatedFat = in.readInt();
		sodium = in.readInt();
		carbs = in.readInt();

		if (calories == -1) {
			calories = null;
		}
		if (protein == -1) {
			protein = null;
		}
		if (saturatedFat == -1) {
			saturatedFat = null;
		}
		if (sodium == -1) {
			sodium = null;
		}
		if (carbs == -1) {
			carbs = null;
		}
	}
}
