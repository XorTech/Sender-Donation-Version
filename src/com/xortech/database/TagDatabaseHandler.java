package com.xortech.database;

import java.util.ArrayList;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TagDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "myTagManager";

    // Contacts table name
    private static final String TABLE_TAGS = "tags";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_SECRET = "secret";
    private final ArrayList<MyTags> tag_list = new ArrayList<MyTags>();

    public TagDatabaseHandler(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
	String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_TAGS + "(" + 
    KEY_ID + " INTEGER PRIMARY KEY," + 
	KEY_TAG + " TEXT," + 
    KEY_PH_NO + " TEXT," + 
	KEY_SECRET + " TEXT" +
	")";
	db.execSQL(CREATE_TAG_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// Drop older table if existed
	db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);

	// Create tables again
	onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new tag
    public void Add_Tag(MyTags tag) {
	SQLiteDatabase db = this.getWritableDatabase();
	ContentValues values = new ContentValues();
	values.put(KEY_TAG, tag.getMyTag());
	values.put(KEY_PH_NO, tag.getMyTagPhoneNumber()); 
	values.put(KEY_SECRET, tag.getTagSecret());
	// Inserting Row
	db.insert(TABLE_TAGS, null, values);
	db.close(); 
    }

    // Getting single tag
    public MyTags Get_Tag(int id) {
	SQLiteDatabase db = this.getReadableDatabase();

	Cursor cursor = db.query(TABLE_TAGS, new String[] { KEY_ID,
		KEY_TAG, KEY_PH_NO, KEY_SECRET }, KEY_ID + "=?",
		new String[] { String.valueOf(id) }, null, null, null, null);
	if (cursor != null)
	    cursor.moveToFirst();

	MyTags tag = new MyTags(Integer.parseInt(cursor.getString(0)),
		cursor.getString(1), cursor.getString(2), cursor.getString(3));
	// return tag
	cursor.close();
	db.close();

	return tag;
    }

    // Getting All Tags
    public ArrayList<MyTags> Get_Tags() {
	try {
	    tag_list.clear();

	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_TAGS;

	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
		do {
		    MyTags tag = new MyTags();
		    tag.setID(Integer.parseInt(cursor.getString(0)));
		    tag.setMyTag(cursor.getString(1));
		    tag.setMyTagPhoneNumber(cursor.getString(2));
		    tag.setTagSecret(cursor.getString(3));
		    // Adding contact to list
		    tag_list.add(tag);
		} while (cursor.moveToNext());
	    }

	    // return tag list
	    cursor.close();
	    db.close();
	    return tag_list;
	} catch (Exception e) {
	    Log.e("all_tag", "" + e);
	}

	return tag_list;
    }

    // Updating single tag
    public int Update_Tag(MyTags tag) {
	SQLiteDatabase db = this.getWritableDatabase();

	ContentValues values = new ContentValues();
	values.put(KEY_TAG, tag.getMyTag());
	values.put(KEY_PH_NO, tag.getMyTagPhoneNumber());
	values.put(KEY_SECRET, tag.getTagSecret());

	// updating row
	return db.update(TABLE_TAGS, values, KEY_ID + " = ?",
		new String[] { String.valueOf(tag.getID()) });
    }

    // Deleting single tag
    public void Delete_Tag(int id) {
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(TABLE_TAGS, KEY_ID + " = ?",
		new String[] { String.valueOf(id) });
	db.close();
    }

    // Getting tag Count
    public int Get_Total_Tags() {
	String countQuery = "SELECT  * FROM " + TABLE_TAGS;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(countQuery, null);
	cursor.close();

	// return count
	return cursor.getCount();
    }
}
