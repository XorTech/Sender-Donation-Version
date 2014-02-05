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


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TagDatabaseHandler extends SQLiteOpenHelper {

    // DATABASE VERSION
    public static final int DATABASE_VERSION = 2;

    // DATABASE NAME
    private static final String DATABASE_NAME = "myTagManager";

    // TAG TABLE NAMES
    private static final String TABLE_TAGS = "tags";
    private static final String TABLE_TAGS_TEMP = "tags_temp";

    // TAG TABLE COLUMN NAMES
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_SECRET = "secret";
    private static final String KEY_ACTIVE = "active";
    
    private final ArrayList<MyTags> tag_list = new ArrayList<MyTags>();

    public TagDatabaseHandler(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * METHOD TO CREATE THE INITIAL TAG DATABASE
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
    	String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_TAGS + "(" + 
    			KEY_ID + " INTEGER PRIMARY KEY," + 
    			KEY_TAG + " TEXT," + 
    			KEY_PH_NO + " TEXT," + 
    			KEY_SECRET + " TEXT," +
    			KEY_ACTIVE + " BIT" +
    			")";
    	db.execSQL(CREATE_TAG_TABLE);
    	
    }

    /**
     * METHOD FOR HANDLING UPDATING THE DATABASE
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	
    	// CREATE A TEMP DATABASE
    	String CREATE_TAG_TABLE_TEMP = "CREATE TABLE " + TABLE_TAGS_TEMP + " ( " + 
    			KEY_ID + " INTEGER PRIMARY KEY, " + 
    			KEY_TAG + " TEXT, " + 
    			KEY_PH_NO + " TEXT, " +
    			KEY_SECRET + " TEXT " +
    			")";
    	db.execSQL(CREATE_TAG_TABLE_TEMP);
    	    	
    	// COPY THE DATA TO THE TEMP DATABASAE
    	db.execSQL("INSERT INTO " + TABLE_TAGS_TEMP + " SELECT " + KEY_ID + ", " + KEY_TAG + ", " + 
    			KEY_PH_NO + ", " + KEY_SECRET + " FROM " + TABLE_TAGS);
    	    	
    	// DELETE THE PRIMARY DATABASE
    	db.execSQL("DROP TABLE " + TABLE_TAGS);
    	    	
    	// CREATE A NEW COPY OF THE DATABASE WITH THE NEW COLUMNS
    	String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_TAGS + " ( " + 
    			KEY_ID + " INTEGER PRIMARY KEY," + 
    			KEY_TAG + " TEXT, " + 
    			KEY_PH_NO + " TEXT, " + 
    			KEY_SECRET + " TEXT, " +
    			KEY_ACTIVE + " BIT " +
    			")";
    	db.execSQL(CREATE_TAG_TABLE);
    	
    	int one = 1;
    	    	
    	// COPY THE DATA FROM THE TEMP DATABASE TO THE NEW DATABASE, WITH THE NEW VALUES
    	db.execSQL("INSERT INTO " + TABLE_TAGS + " SELECT " + KEY_ID + ", " + KEY_TAG + ", " + 
    			KEY_PH_NO + ", " + KEY_SECRET + ", " + one + " FROM " + TABLE_TAGS_TEMP);
    	    	
    	// DROP THE TEMP DATABASE
    	db.execSQL("DROP TABLE " + TABLE_TAGS_TEMP);
    	
    }

    /**
     * ALL CRUD(CREATE, READ, UPDATE, DELETE) OPERATIONS
     */
    /**
     * METHOD TO ADD A NEW TAG TO THE DATABASE
     * @param tag
     */
    public void Add_Tag(MyTags tag) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(KEY_TAG, tag.getMyTag());
    	values.put(KEY_PH_NO, tag.getMyTagPhoneNumber()); 
    	values.put(KEY_SECRET, tag.getTagSecret());
    	values.put(KEY_ACTIVE, tag.getTagStatus());
    	// INSERT ROW
    	db.insert(TABLE_TAGS, null, values);
    	db.close(); 
    }

    /**
     * METHOD TO GET A SINGLE TAG FROM THE DATABASE
     * @param id
     * @return
     */
    public MyTags Get_Tag(int id) {
    	SQLiteDatabase db = this.getReadableDatabase();

    	Cursor cursor = db.query(TABLE_TAGS, new String[] { KEY_ID,
    			KEY_TAG, KEY_PH_NO, KEY_SECRET, KEY_ACTIVE }, KEY_ID + "=?",
    			new String[] { String.valueOf(id) }, null, null, null, null);
    	if (cursor != null)
    		cursor.moveToFirst();

    	MyTags tag = new MyTags(Integer.parseInt(cursor.getString(0)),
    			cursor.getString(1), cursor.getString(2), cursor.getString(3), 
    			Integer.parseInt(cursor.getString(4)));
    	// RETURN TAG
    	cursor.close();
    	db.close();

    	return tag;
    }

    /**
     * METHOD TO GET ALL THE TAGS FROM THE DATABASE
     * @return
     */
    public ArrayList<MyTags> Get_Tags() {
    	try {
    		tag_list.clear();

    		// SELECT ALL QUERY
    		String selectQuery = "SELECT * FROM " + TABLE_TAGS;

    		SQLiteDatabase db = this.getWritableDatabase();
    		Cursor cursor = db.rawQuery(selectQuery, null);

    		// LOOP THROUGH THE ROWS AND ADD TO THE LIST
    		if (cursor.moveToFirst()) {
    			do {
    				MyTags tag = new MyTags();
    				tag.setID(Integer.parseInt(cursor.getString(0)));
		    	tag.setMyTag(cursor.getString(1));
		    	tag.setMyTagPhoneNumber(cursor.getString(2));
		    	tag.setTagSecret(cursor.getString(3));
		    	tag.setTagStatus(Integer.parseInt(cursor.getString(4)));
		    	// ADD TAG TO THE LIST
		    	tag_list.add(tag);
    			} while (cursor.moveToNext());
    		}

    		// RETURN THE TAG LIST
    		cursor.close();
    		db.close();
    		return tag_list;
    	} catch (Exception e) {
    		Log.e("all_tag", "" + e);
    	}

    	return tag_list;
    }

    /**
     * METHOD TO UPDATE TAG DATA
     * @param tag
     * @return
     */
    public int Update_Tag(MyTags tag) {
    	SQLiteDatabase db = this.getWritableDatabase();

    	ContentValues values = new ContentValues();
    	values.put(KEY_TAG, tag.getMyTag());
    	values.put(KEY_PH_NO, tag.getMyTagPhoneNumber());
    	values.put(KEY_SECRET, tag.getTagSecret());
    	values.put(KEY_ACTIVE, tag.getTagStatus());

    	// UPDATE THE ROW
    	return db.update(TABLE_TAGS, values, KEY_ID + " = ?",
    			new String[] { String.valueOf(tag.getID()) });
    }

    /**
     * METHOD TO REMOVE A SINGLE TAG FROM THE DATABASE
     * @param id
     */
    public void Delete_Tag(int id) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_TAGS, KEY_ID + " = ?", new String[] { String.valueOf(id) });
    	db.close();
    }

    /**
     * METHOD TO GET A COUNT OF THE TOTAL NUMBER OF TAGS IN THE DATABASE
     * @return
     */
    public int Get_Total_Tags() {
    	String countQuery = "SELECT  * FROM " + TABLE_TAGS;
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);
    	cursor.close();

    	// RETURN THE TAG COUNT
    	return cursor.getCount();
    }
}
