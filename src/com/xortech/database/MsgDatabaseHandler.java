/*
 * Copyright 2014 XOR TECH LTD 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
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

    // DATABASE VERSION
    private static final int DATABASE_VERSION = 1;

    // DATABASE NAME
    private static final String DATABASE_NAME = "message_db";

    // TABLE NAME
    private static final String TABLE_MESSAGE = "message";

    // COLUMN NAMES
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

    /**
     * METHOD TO CREATE THE TABLE
     */
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

    /**
     * METHOD TO UPGRADE THE DATABASE TO A NEW VERSION
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	// DROP OLD TABLES IF THEY EXISTS
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);

    	// CREATE TABLE
    	onCreate(db);
    }

    /**
     * All CRUD(CREATE, READ, UPDATE, DELETE) OPERATIONS
     */

    /**
     * METHOD TO ADD A NEW MESSAGE TO THE DATABASE
     * @param message
     */
    public void Add_Message(MessageData message) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(KEY_TAG, message.getTag()); 
		values.put(KEY_PH_NO, message.getPhoneNumber());
		values.put(KEY_LAT, message.getLatitude()); 
		values.put(KEY_LONG, message.getLongitude());
		values.put(KEY_TIME, message.getTime());
		// INSERT ROW
		db.insert(TABLE_MESSAGE, null, values);
		db.close(); 
    }

    /**
     * METHOD TO RETURN A SIGNLE MESSAGE
     * @param id
     * @return
     */
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
    	// RETURN MESSAGE
    	cursor.close();
    	db.close();

    	return message;
    }

    /**
     * METHOD FOR GETTING ALL THE MESSAGES IN THE DATABASE
     * @return
     */
    public ArrayList<MessageData> Get_Messages() {
    	try {
    		message_list.clear();

    		// SELECT ALL QUERY
    		String selectQuery = "SELECT  * FROM " + TABLE_MESSAGE;

    		SQLiteDatabase db = this.getWritableDatabase();
    		Cursor cursor = db.rawQuery(selectQuery, null);

    		// LOOP THROUGH AND ADD TO THE LIST
    		if (cursor.moveToFirst()) {
    			do {
    				MessageData message = new MessageData();
    				message.setID(Integer.parseInt(cursor.getString(0)));
    				message.setTag(cursor.getString(1));
    				message.setPhoneNumber(cursor.getString(2));
    				message.setLatitude(cursor.getString(3));
    				message.setLongitude(cursor.getString(4));
    				message.setTime(cursor.getString(5));
    				// ADD MESSAGE TO THE LIST
    				message_list.add(message);
    			} while (cursor.moveToNext());
    		}

    		// RETURN THE MESSAGE LIST
    		cursor.close();
    		db.close();
    		return message_list;
    	} catch (Exception e) {
    		Log.e("all_message", "" + e);
    	}

    	return message_list;
    }

    /**
     * METHOD TO UPDATE A DATABASE ENTRY
     * @param message
     * @return
     */
    public int Update_Message(MessageData message) {
    	SQLiteDatabase db = this.getWritableDatabase();

    	ContentValues values = new ContentValues();
    	values.put(KEY_TAG, message.getTag());
    	values.put(KEY_PH_NO, message.getPhoneNumber());
    	values.put(KEY_LAT, message.getLatitude());
    	values.put(KEY_LONG, message.getLongitude());
    	values.put(KEY_TIME, message.getTime());

    	// UPDATE ROW
    	return db.update(TABLE_MESSAGE, values, KEY_ID + " = ?",
    			new String[] { String.valueOf(message.getID()) });
    }

    /**
     * METHOD TO DELETE A SINGLE MESSAGE FROM THE DATABASE
     * @param id
     */
    public void Delete_Message(int id) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_MESSAGE, KEY_ID + " = ?", new String[] { String.valueOf(id) });
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

    /**
     * METHOD TO GET THE TOTAL COUNT OF MESSAGES FROM THE DATABASE
     * @return
     */
    public int Get_Total_Messages() {
    	String countQuery = "SELECT  * FROM " + TABLE_MESSAGE;
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);
    	cursor.close();

    	// return count
    	return cursor.getCount();
    }
}
