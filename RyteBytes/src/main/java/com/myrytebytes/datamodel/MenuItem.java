package com.myrytebytes.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class MenuItem implements JacksonParser, Parcelable {

	public String name;
	public NutritionInformation nutritionInfo;
	public String image;
	public String description;
	public Integer price;

	public MenuItem() { }

	public MenuItem(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Log.e("Error filling MenuItem", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
				if (tag.equals("nutritionInfo")) {
					nutritionInfo = new NutritionInformation(jsonParser, false);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "name":
						name = jsonParser.getStringValue();
						break;
					case "longDescription":
						description = jsonParser.getStringValue();
						break;
					case "picture":
						image = jsonParser.getStringValue();
						break;
					case "price":
						price = jsonParser.getIntValue();
						break;
				}
			}
		}, closeWhenComplete);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(image);
		out.writeString(description);
		out.writeInt(price != null ? price : -1);
		out.writeParcelable(nutritionInfo, 0);
	}

	public static final Parcelable.Creator<MenuItem> CREATOR = new Parcelable.Creator<MenuItem>() {
		public MenuItem createFromParcel(Parcel in) {
			return new MenuItem(in);
		}

		public MenuItem[] newArray(int size) {
			return new MenuItem[size];
		}
	};

	private MenuItem(Parcel in) {
		name = in.readString();
		image = in.readString();
		description = in.readString();
		price = in.readInt();
		if (price == -1) {
			price = null;
		}
		nutritionInfo = in.readParcelable(NutritionInformation.class.getClassLoader());
	}
}
