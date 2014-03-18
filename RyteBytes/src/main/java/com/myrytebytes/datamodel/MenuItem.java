package com.myrytebytes.datamodel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MenuItem extends DAObject implements JacksonParser, Parcelable {

	private Long id;
	public String name;
	public NutritionInformation nutritionInfo;
	public String imageName;
	public Integer imageResourceId;
	public String description;
	public Integer price;
	public String objectId;

	public MenuItem() { }

	public MenuItem(Cursor cursor) {
		fillFromCursor(cursor);
	}

	public MenuItem(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Logr.e("Error filling MenuItem", e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MenuItem)) {
			return false;
		} else {
			return objectId.equals(((MenuItem)o).objectId);
		}
	}

	@Override
	public int hashCode() {
		return objectId.hashCode();
	}

	public void fillFromCursor(Cursor c) {
		id = c.getLong(c.getColumnIndex(Columns._ID));
		name = getStringFromCursor(c, Columns.NAME);
		imageName = getStringFromCursor(c, Columns.IMAGE);
		imageResourceId = getIntFromCursor(c, Columns.IMAGE_RES_ID);
		description = getStringFromCursor(c, Columns.DESCRIPTION);
		price = getIntFromCursor(c, Columns.PRICE);
		objectId = getStringFromCursor(c, Columns.OBJECT_ID);

		nutritionInfo = new NutritionInformation();
		nutritionInfo.calories = getIntFromCursor(c, Columns.CALORIES);
		nutritionInfo.protein = getIntFromCursor(c, Columns.PROTEIN);
		nutritionInfo.saturatedFat = getIntFromCursor(c, Columns.SATURATED_FAT);
		nutritionInfo.sodium = getIntFromCursor(c, Columns.SODIUM);
		nutritionInfo.carbs = getIntFromCursor(c, Columns.CARBS);
	}

	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(Columns._ID, id);
		cv.put(Columns.NAME, name);
		cv.put(Columns.IMAGE, imageName);
		cv.put(Columns.IMAGE_RES_ID, imageResourceId);
		cv.put(Columns.DESCRIPTION, description);
		cv.put(Columns.PRICE, price);
		cv.put(Columns.OBJECT_ID, objectId);
		cv.put(Columns.CALORIES, nutritionInfo.calories);
		cv.put(Columns.PROTEIN, nutritionInfo.protein);
		cv.put(Columns.SATURATED_FAT, nutritionInfo.saturatedFat);
		cv.put(Columns.SODIUM, nutritionInfo.sodium);
		cv.put(Columns.CARBS, nutritionInfo.carbs);
		return cv;
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
				if (tag.equals("nutritionInfoId")) {
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
						imageName = jsonParser.getStringValue();
						break;
					case "objectId":
						objectId = jsonParser.getStringValue();
						break;
				}
			}
		}, closeWhenComplete);
	}

	public void insertOrUpdateByObjectId(Context context) {
		if (id != null) {
			update(context);
		} else {
			SQLiteDatabase db = RyteBytesSQLiteOpenHelper.getInstance(context).getWritableDatabase();

			MenuItem existingItem;
			if (objectId != null) {
				Cursor c = db.query(Columns.TABLE_NAME, null, Columns.OBJECT_ID + "=?", new String[] { ""+objectId}, null, null, null);
				if (c.moveToFirst()) {
					existingItem = new MenuItem(c);
				} else {
					existingItem = null;
				}
				c.close();

				if (existingItem != null) {
					id = existingItem.id;
					if (!objectsAreEqual(name, existingItem.name) ||
						!objectsAreEqual(imageName, existingItem.imageName) ||
						!objectsAreEqual(description, existingItem.description) ||
						!objectsAreEqual(price, existingItem.price) ||
						!objectsAreEqual(nutritionInfo.calories, existingItem.nutritionInfo.calories) ||
						!objectsAreEqual(nutritionInfo.protein, existingItem.nutritionInfo.protein) ||
						!objectsAreEqual(nutritionInfo.saturatedFat, existingItem.nutritionInfo.saturatedFat) ||
						!objectsAreEqual(nutritionInfo.sodium, existingItem.nutritionInfo.sodium) ||
						!objectsAreEqual(nutritionInfo.carbs, existingItem.nutritionInfo.carbs))
					{
						db.update(Columns.TABLE_NAME, toContentValues(), Columns._ID + "=?", new String[] { ""+id });
					}
				} else {
					id = db.insert(Columns.TABLE_NAME, null, toContentValues());
				}
			}
		}
	}

    public static List<String> getAllObjectIds(Context context) {
        SQLiteDatabase db = RyteBytesSQLiteOpenHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.query(Columns.TABLE_NAME, new String[] {Columns.OBJECT_ID}, null, null, null, null, null);
        List<String> objectIds = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                objectIds.add(c.getString(0));
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) { }
            }
        }
        return objectIds;
    }

    public static MenuItem getByObjectId(String objectId, Context context) {
        SQLiteDatabase db = RyteBytesSQLiteOpenHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.query(Columns.TABLE_NAME, null, Columns.OBJECT_ID + "=?", new String[] {objectId}, null, null, null, null);
        MenuItem item = null;
        try {
            if (c.moveToFirst()) {
                item = new MenuItem(c);
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) { }
            }
        }
        return item;
    }

    public static void deleteByObjectId(String objectId, Context context) {
        RyteBytesSQLiteOpenHelper.getInstance(context).getWritableDatabase().delete(Columns.TABLE_NAME, Columns.OBJECT_ID + "=?", new String[] {objectId});
    }

	public void insert(Context context) {
		id = RyteBytesSQLiteOpenHelper.getInstance(context).getWritableDatabase().insert(Columns.TABLE_NAME, null, toContentValues());
	}

	public void update(Context context) {
		RyteBytesSQLiteOpenHelper.getInstance(context).getWritableDatabase().update(Columns.TABLE_NAME, toContentValues(), Columns._ID + "=?", new String[]{"" + id});
	}

	public static final class Columns implements BaseColumns {
		public static final String TABLE_NAME = "MenuItems";

		public static final String NAME = "name";
		public static final String IMAGE = "image";
		public static final String IMAGE_RES_ID = "image_res_id";
		public static final String DESCRIPTION = "description";
		public static final String PRICE = "price";
		public static final String OBJECT_ID = "object_id";
		public static final String CALORIES = "calories";
		public static final String PROTEIN = "protein";
		public static final String SATURATED_FAT = "saturated_fat";
		public static final String SODIUM = "sodium";
		public static final String CARBS = "carbs";

		public static void createTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
					BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					NAME + " TEXT," +
					IMAGE + " TEXT," +
					IMAGE_RES_ID + " INTEGER," +
					DESCRIPTION + " TEXT," +
					PRICE + " INTEGER," +
					OBJECT_ID + " TEXT," +
					CALORIES + " INTEGER," +
					PROTEIN + " INTEGER," +
					SATURATED_FAT + " INTEGER," +
					SODIUM + " INTEGER," +
					CARBS + " INTEGER" +
					");");
		}
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
        out.writeString(objectId);
		out.writeString(imageName);
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
        objectId = in.readString();
		imageName = in.readString();
		description = in.readString();
		price = in.readInt();
		if (price == -1) {
			price = null;
		}
		nutritionInfo = in.readParcelable(NutritionInformation.class.getClassLoader());

	}
}
