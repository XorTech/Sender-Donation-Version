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

public class PanicDatabaseHandler extends SQLiteOpenHelper {

    // DATABASE VERSION
    private static final int DATABASE_VERSION = 2;

    // DATABASE NAME
    private static final String DATABASE_NAME = "myPanicManager";

    // PANIC TABLE NAMES
    private static final String TABLE_PANIC = "panic_numbers";
    private static final String TABLE_PANIC_TEMP = "panic_number_temp";

    // PANIC TABLE COLUMN NAMES
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_P_ACTIVE = "p_active";
    private static final String KEY_E_ACTIVE = "e_active";
    
    private final ArrayList<MyPanicNumbers> number_list = new ArrayList<MyPanicNumbers>();

    public PanicDatabaseHandler(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * METHOD TO CREATE DATABASE TABLE
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
    	String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_PANIC + "(" + 
    			KEY_ID + " INTEGER PRIMARY KEY," + 
    			KEY_TAG + " TEXT," + 
    			KEY_PH_NO + " TEXT," + 
    			KEY_EMAIL + " TEXT," +
    			KEY_ACTIVE + " BIT," +
    			KEY_P_ACTIVE + " BIT," +
    			KEY_E_ACTIVE + " BIT" +
    			")";
    	db.execSQL(CREATE_TAG_TABLE);
    }

    /**
     * METHOD TO UPGRADE THE DATABASE, USED AS REQUIRED
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	
    	// CREATE A TEMP DATABASE
    	String CREATE_TAG_TABLE_TEMP = "CREATE TABLE " + TABLE_PANIC_TEMP + " ( " + 
    			KEY_ID + " INTEGER PRIMARY KEY, " + 
    			KEY_TAG + " TEXT, " + 
    			KEY_PH_NO + " TEXT " +
    			")";
    	db.execSQL(CREATE_TAG_TABLE_TEMP);
    	    	
    	// COPY THE DATA TO THE TEMP DATABASAE
    	db.execSQL("INSERT INTO " + TABLE_PANIC_TEMP + " SELECT " + KEY_ID + ", " + KEY_TAG + ", " + 
    			KEY_PH_NO + " FROM " + TABLE_PANIC);
    	    	
    	// DELETE THE PRIMARY DATABASE
    	db.execSQL("DROP TABLE " + TABLE_PANIC);
    	    	
    	// CREATE A NEW COPY OF THE DATABASE WITH THE NEW COLUMNS
    	String CREATE_TAG_TABLE = "CREATE TABLE " + TABLE_PANIC + " ( " + 
    			KEY_ID + " INTEGER PRIMARY KEY, " + 
    			KEY_TAG + " TEXT, " + 
    			KEY_PH_NO + " TEXT, " + 
    			KEY_EMAIL + " TEXT, " +
    			KEY_ACTIVE + " BIT, " +
    			KEY_P_ACTIVE + " BIT, " +
    			KEY_E_ACTIVE + " BIT " +
    			")";
    	db.execSQL(CREATE_TAG_TABLE);
    	    	
    	// COPY THE DATA FROM THE TEMP DATABASE TO THE NEW DATABASE, WITH THE NEW VALUES
    	db.execSQL("INSERT INTO " + TABLE_PANIC + " SELECT " + KEY_ID + ", " + KEY_TAG + ", " + 
    			KEY_PH_NO + ", " + null + ", " + 1 + ", " + 1 + ", " + 0 + " FROM " + 
    			TABLE_PANIC_TEMP);
    	    	
    	// DROP THE TEMP DATABASE
    	db.execSQL("DROP TABLE " + TABLE_PANIC_TEMP);
    	       
    }

    /**
     * ALL CRUD(CREATE, READ, UPDATE, DELETE) OPERATIONS
     */

   /**
    * METHOD USED TO ADD A NEW PANIC NUMBER TO THE DATABASE
    * @param newPanic
    */
    public void Add_Number(MyPanicNumbers newPanic) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(KEY_TAG, newPanic.getMyPanicTag());
    	values.put(KEY_PH_NO, newPanic.getMyPanicPhoneNumber()); 
    	values.put(KEY_EMAIL, newPanic.getMyPanicEmail());
    	values.put(KEY_ACTIVE, newPanic.getPanicActive());
    	values.put(KEY_P_ACTIVE, newPanic.getPanicActiveP());
    	values.put(KEY_E_ACTIVE, newPanic.getPanicActiveE());
    	// INSERT ROW
    	db.insert(TABLE_PANIC, null, values);
    	db.close(); 
    }

    /**
     * METHOD TO GET ONE PANIC NUMBER FROM THE DATABASE
     * @param id
     * @return
     */
    public MyPanicNumbers Get_Numbers(int id) {
    	SQLiteDatabase db = this.getReadableDatabase();

    	Cursor cursor = db.query(TABLE_PANIC, new String[] { KEY_ID,
    			KEY_TAG, KEY_PH_NO, KEY_EMAIL, KEY_ACTIVE, KEY_P_ACTIVE, KEY_E_ACTIVE }, KEY_ID + "=?",
    			new String[] { String.valueOf(id) }, null, null, null, null);
    	if (cursor != null)
    		cursor.moveToFirst();

    	MyPanicNumbers tag = new MyPanicNumbers(Integer.parseInt(cursor.getString(0)),
    			cursor.getString(1), cursor.getString(2), cursor.getString(3), 
    			Integer.parseInt(cursor.getString(4)), Integer.parseInt(cursor.getString(5)),
    			Integer.parseInt(cursor.getString(6)));
    	// RETURN TAG
    	cursor.close();
    	db.close();

    	return tag;
    }

    /**
     * METHOD TO GET ALL PANIC NUMBERS FROM THE DATABASE
     * @return
     */
    public ArrayList<MyPanicNumbers> Get_Numbers() {
    	try {
    		number_list.clear();

    		// SELECT ALL QUERY
    		String selectQuery = "SELECT  * FROM " + TABLE_PANIC;

    		SQLiteDatabase db = this.getWritableDatabase();
    		Cursor cursor = db.rawQuery(selectQuery, null);
    		
    		// LOOPING THROUGH ALL ROWS AND ADDING TO LIST
    		if (cursor.moveToFirst()) {
    			do {
    				MyPanicNumbers number = new MyPanicNumbers();
    				number.setID(Integer.parseInt(cursor.getString(0)));
    				number.setMyPanicTag(cursor.getString(1));
    				number.setMyPanicPhoneNumber(cursor.getString(2));
    				number.setMyPanicEmail(cursor.getString(3));
    				number.setPanicActive(Integer.parseInt(cursor.getString(4)));
    				number.setPanicActiveP(Integer.parseInt(cursor.getString(5)));
    				number.setPanicActiveE(Integer.parseInt(cursor.getString(6)));
		    
    				// ADDING PANIC NUMBER TO LIST
    				number_list.add(number);
    			} while (cursor.moveToNext());
    		}

    		// RETURN PANIC LIST
    		cursor.close();
    		db.close();
    		return number_list;
    	} catch (Exception e) {
    		Log.e("all_tag", "" + e);
    	}

    	return number_list;
    }

    /**
     * METHOD TO UPDATE A SINGLE PANIC NUMBER IN THE DATABASE
     * @param number
     * @return
     */
    public int Update_Number(MyPanicNumbers number) {
    	SQLiteDatabase db = this.getWritableDatabase();

    	ContentValues values = new ContentValues();
    	values.put(KEY_TAG, number.getMyPanicTag());
    	values.put(KEY_PH_NO, number.getMyPanicPhoneNumber());
    	values.put(KEY_EMAIL, number.getMyPanicEmail());
    	values.put(KEY_ACTIVE, number.getPanicActive());
    	values.put(KEY_P_ACTIVE, number.getPanicActiveP());
    	values.put(KEY_E_ACTIVE, number.getPanicActiveE());

    	// UPDATE THE ROW
    	return db.update(TABLE_PANIC, values, KEY_ID + " = ?", new String[] { String.valueOf(number.getID()) });
    }

    /**
     * METHOD TO DELETE A SINGE PANIC NUMBER FROM THE DATABASE
     * @param id
     */
    public void Delete_Number(int id) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_PANIC, KEY_ID + " = ?", new String[] { String.valueOf(id) });
    	db.close();
    }

    /**
     * METHOD TO RETURN THE NUMBER OF TAGS IN THE DATABASE
     * @return
     */
    public int Get_Total_Numbers() {
    	String countQuery = "SELECT  * FROM " + TABLE_PANIC;
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);
    	cursor.close();

    	// RETURN COUNT
    	return cursor.getCount();
    }
}
