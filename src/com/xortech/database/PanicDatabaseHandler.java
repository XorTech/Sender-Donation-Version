package com.xortech.database;

import java.util.ArrayList;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PanicDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "myPanicManager";

    // Contacts table name
    private static final String TABLE_PANIC = "panic_numbers";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_PH_NO = "phone_number";
    private final ArrayList<MyPanicNumbers> number_list = new ArrayList<MyPanicNumbers>();

    public PanicDatabaseHandler(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
	String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_PANIC + "(" + 
    KEY_ID + " INTEGER PRIMARY KEY," + 
	KEY_TAG + " TEXT," + 
    KEY_PH_NO + " TEXT" + 
	")";
	db.execSQL(CREATE_TAG_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// Drop older table if existed
	db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANIC);

	// Create tables again
	onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new tag
    public void Add_Number(MyPanicNumbers newPanic) {
	SQLiteDatabase db = this.getWritableDatabase();
	ContentValues values = new ContentValues();
	values.put(KEY_TAG, newPanic.getMyPanicTag());
	values.put(KEY_PH_NO, newPanic.getMyPanicPhoneNumber()); 
	// Inserting Row
	db.insert(TABLE_PANIC, null, values);
	db.close(); 
    }

    // Getting single tag
    public MyPanicNumbers Get_Numbers(int id) {
	SQLiteDatabase db = this.getReadableDatabase();

	Cursor cursor = db.query(TABLE_PANIC, new String[] { KEY_ID,
		KEY_TAG, KEY_PH_NO }, KEY_ID + "=?",
		new String[] { String.valueOf(id) }, null, null, null, null);
	if (cursor != null)
	    cursor.moveToFirst();

	MyPanicNumbers tag = new MyPanicNumbers(Integer.parseInt(cursor.getString(0)),
		cursor.getString(1), cursor.getString(2));
	// return tag
	cursor.close();
	db.close();

	return tag;
    }

    // Getting All Tags
    public ArrayList<MyPanicNumbers> Get_Numbers() {
	try {
	    number_list.clear();

	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_PANIC;

	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
		do {
		    MyPanicNumbers number = new MyPanicNumbers();
		    number.setID(Integer.parseInt(cursor.getString(0)));
		    number.setMyPanicTag(cursor.getString(1));
		    number.setMyPanicPhoneNumber(cursor.getString(2));
		    // Adding contact to list
		    number_list.add(number);
		} while (cursor.moveToNext());
	    }

	    // return tag list
	    cursor.close();
	    db.close();
	    return number_list;
	} catch (Exception e) {
	    Log.e("all_tag", "" + e);
	}

	return number_list;
    }

    // Updating single tag
    public int Update_Number(MyPanicNumbers number) {
	SQLiteDatabase db = this.getWritableDatabase();

	ContentValues values = new ContentValues();
	values.put(KEY_TAG, number.getMyPanicTag());
	values.put(KEY_PH_NO, number.getMyPanicPhoneNumber());

	// updating row
	return db.update(TABLE_PANIC, values, KEY_ID + " = ?",
		new String[] { String.valueOf(number.getID()) });
    }

    // Deleting single tag
    public void Delete_Number(int id) {
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(TABLE_PANIC, KEY_ID + " = ?",
		new String[] { String.valueOf(id) });
	db.close();
    }

    // Getting tag Count
    public int Get_Total_Numbers() {
	String countQuery = "SELECT  * FROM " + TABLE_PANIC;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(countQuery, null);
	cursor.close();

	// return count
	return cursor.getCount();
    }
}
