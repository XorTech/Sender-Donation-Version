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

public class MessageData {

    public int _id;
    public String _tag;
    public String _phone_number;
    public String _latitude;
    public String _longitude;
    public String _time;

    public MessageData() {
    }

    /**
     * CONSTRUCTOR 
     * @param id
     * @param tag
     * @param phone_number
     * @param lat
     * @param lon
     * @param time
     */
    public MessageData(int id, String tag, String phone_number, String lat, String lon, String time) {
    	this._id = id;
    	this._tag = tag;
    	this._phone_number = phone_number;
    	this._latitude = lat;
    	this._longitude = lon;
    	this._time = time;
    }

    /**
     * CONSTRUCTOR
     * @param tag
     * @param phone_number
     * @param lat
     * @param lon
     * @param time
     */
    public MessageData(String tag, String phone_number, String lat, String lon, String time) {
    	this._tag = tag;
    	this._phone_number = phone_number;
    	this._latitude = lat;
    	this._longitude = lon;
    	this._time = time;
    }

    /**
     * METHOD TO GET THE MESSAGE ID
     * @return
     */
    public int getID() {
    	return this._id;
    }

    /**
     * METHOD TO SET THE MESSAGE ID
     * @param id
     */
    public void setID(int id) {
    	this._id = id;
    }

    /**
     * METHOD TO GET THE TAG NAME
     * @return
     */
    public String getTag() {
    	return this._tag;
    }

    /**
     * METHOD TO SET THE TAG NAME
     * @param tag
     */
    public void setTag(String tag) {
    	this._tag = tag;
    }

    /**
     * METHOD TO GET THE TAG'S PHONE NUMBER
     * @return
     */
    public String getPhoneNumber() {
    	return this._phone_number;
    }

    /**
     * METHOD TO SET THE TAG'S PHONE NUMBER
     * @param phone_number
     */
    public void setPhoneNumber(String phone_number) {
    	this._phone_number = phone_number;
    }

    /**
     * METHOD TO GET THE TAG'S LATITUDE
     * @return
     */
    public String getLatitude() {
    	return this._latitude;
    }

    /**
     * METHOD TO SET THE TAG'S LATITUDE
     * @param lat
     */
    public void setLatitude(String lat) {
    	this._latitude = lat;
    }
    
    /**
     * METHOD TO GET THE TAG'S LONGITUDE
     * @return
     */
    public String getLongitude() {
    	return this._longitude;
    }

    /**
     * METHOD TO SET THE TAG'S LONGITUDE
     * @param lon
     */
    public void setLongitude(String lon) {
    	this._longitude = lon;
    }
    
    /**
     * METHOD TO GET THE TAG'S REPORT TIME
     * @return
     */
    public String getTime() {
    	return this._time;
    }

    /**
     * METHOD TO SET THE TAG'S REPORT TIME
     * @param time
     */
    public void setTime(String time) {
    	this._time = time;
    }
}