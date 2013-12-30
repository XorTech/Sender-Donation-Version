package com.xortech.database;

public class MessageData {

    // private variables
    public int _id;
    public String _tag;
    public String _phone_number;
    public String _latitude;
    public String _longitude;
    public String _time;

    public MessageData() {
    }

    // constructor
    public MessageData(int id, String tag, String phone_number, String lat, String lon, String time) {
	this._id = id;
	this._tag = tag;
	this._phone_number = phone_number;
	this._latitude = lat;
	this._longitude = lon;
	this._time = time;
    }

    // constructor
    public MessageData(String tag, String phone_number, String lat, String lon, String time) {
	this._tag = tag;
	this._phone_number = phone_number;
	this._latitude = lat;
	this._longitude = lon;
	this._time = time;
    }

    // getting ID
    public int getID() {
	return this._id;
    }

    // setting id
    public void setID(int id) {
	this._id = id;
    }

    // getting name
    public String getTag() {
	return this._tag;
    }

    // setting name
    public void setTag(String tag) {
	this._tag = tag;
    }

    // getting phone number
    public String getPhoneNumber() {
	return this._phone_number;
    }

    // setting phone number
    public void setPhoneNumber(String phone_number) {
	this._phone_number = phone_number;
    }

    // getting latitude
    public String getLatitude() {
	return this._latitude;
    }

    // setting latitude
    public void setLatitude(String lat) {
	this._latitude = lat;
    }
    
    // getting longitude
    public String getLongitude() {
	return this._longitude;
    }

    // setting longitude
    public void setLongitude(String lon) {
	this._longitude = lon;
    }
    
    // getting time
    public String getTime() {
	return this._time;
    }

    // setting time
    public void setTime(String time) {
	this._time = time;
    }
}