package com.xortech.database;

import java.util.ArrayList;

import com.xortech.database.MessageData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MsgDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "message_db";

    // Contacts table name
    private static final String TABLE_MESSAGE = "message";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag_id";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG = "longitude";
    private static final String KEY_TIME = "time";
    
    private final ArrayList<MessageData> message_list = new ArrayList<MessageData>();

    public MsgDatabaseHandler(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
	String CREATE_MESSAGE_TABLE = "CREATE TABLE " + TABLE_MESSAGE + "(" + 
    KEY_ID + " INTEGER PRIMARY KEY," + 
	KEY_TAG + " TEXT," + 
    KEY_PH_NO + " TEXT," + 
	KEY_LAT + " TEXT," +
    KEY_LONG + " TEXT," +
	KEY_TIME + " TEXT" +
    ")";
	db.execSQL(CREATE_MESSAGE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// Drop older table if existed
	db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);

	// Create tables again
	onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public void Add_Message(MessageData message) {
	SQLiteDatabase db = this.getWritableDatabase();
	ContentValues values = new ContentValues();
	values.put(KEY_TAG, message.getTag()); 
	values.put(KEY_PH_NO, message.getPhoneNumber());
	values.put(KEY_LAT, message.getLatitude()); 
	values.put(KEY_LONG, message.getLongitude());
	values.put(KEY_TIME, message.getTime());
	// Inserting Row
	db.insert(TABLE_MESSAGE, null, values);
	db.close(); // Closing database connection
    }

    // Getting single message
    MessageData Get_Message(int id) {
	SQLiteDatabase db = this.getReadableDatabase();

	Cursor cursor = db.query(TABLE_MESSAGE, new String[] { KEY_ID,
		KEY_TAG, KEY_PH_NO, KEY_LAT, KEY_LONG, KEY_TIME }, KEY_ID + "=?",
		new String[] { String.valueOf(id) }, null, null, null, null);
	if (cursor != null)
	    cursor.moveToFirst();

	MessageData message = new MessageData(Integer.parseInt(cursor.getString(0)),
		cursor.getString(1), cursor.getString(2), cursor.getString(3), 
		cursor.getString(4), cursor.getString(5));
	// return message
	cursor.close();
	db.close();

	return message;
    }

    // Getting All Messages
    public ArrayList<MessageData> Get_Messages() {
	try {
	    message_list.clear();

	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_MESSAGE;

	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
		do {
		    MessageData message = new MessageData();
		    message.setID(Integer.parseInt(cursor.getString(0)));
		    message.setTag(cursor.getString(1));
		    message.setPhoneNumber(cursor.getString(2));
		    message.setLatitude(cursor.getString(3));
		    message.setLongitude(cursor.getString(4));
		    message.setTime(cursor.getString(5));
		    // Adding message to list
		    message_list.add(message);
		} while (cursor.moveToNext());
	    }

	    // return message list
	    cursor.close();
	    db.close();
	    return message_list;
	} catch (Exception e) {
	    // TODO: handle exception
	    Log.e("all_message", "" + e);
	}

	return message_list;
    }

    // Updating single message
    public int Update_Message(MessageData message) {
	SQLiteDatabase db = this.getWritableDatabase();

	ContentValues values = new ContentValues();
	values.put(KEY_TAG, message.getTag());
	values.put(KEY_PH_NO, message.getPhoneNumber());
	values.put(KEY_LAT, message.getLatitude());
	values.put(KEY_LONG, message.getLongitude());
	values.put(KEY_TIME, message.getTime());

	// updating row
	return db.update(TABLE_MESSAGE, values, KEY_ID + " = ?",
		new String[] { String.valueOf(message.getID()) });
    }

    // Deleting single message
    public void Delete_Message(int id) {
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(TABLE_MESSAGE, KEY_ID + " = ?",
		new String[] { String.valueOf(id) });
	db.close();
    }
    
    public void Delete_All_Messages() {
    	try {
    	    message_list.clear();
    	    SQLiteDatabase db = this.getWritableDatabase();
    	    db.delete(TABLE_MESSAGE, null, null);
    	}
    	catch (Exception e) {
    		 Log.e("delete_all_message", "" + e);
    	} 	    
    }

    // Getting message Count
    public int Get_Total_Messages() {
	String countQuery = "SELECT  * FROM " + TABLE_MESSAGE;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(countQuery, null);
	cursor.close();

	// return count
	return cursor.getCount();
    }
}
