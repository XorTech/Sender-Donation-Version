package com.xortech.database;

public class MyPanicNumbers {

    // private variables
    public int _id;
    public String _tag;
    public String _phone_number;

    public MyPanicNumbers() {
    }

    // constructor
    public MyPanicNumbers(int id, String tag, String phone_number) {
	this._id = id;
	this._tag = tag;
	this._phone_number = phone_number;
    }

    // constructor
    public MyPanicNumbers(String tag, String phone_number) {
	this._tag = tag;
	this._phone_number = phone_number;
    }

    // getting id
    public int getID() {
	return this._id;
    }

    // setting id
    public void setID(int id) {
	this._id = id;
    }

    // getting tag
    public String getMyPanicTag() {
	return this._tag;
    }

    // setting tag
    public void setMyPanicTag(String tag) {
	this._tag = tag;
    }

    // getting phone number
    public String getMyPanicPhoneNumber() {
	return this._phone_number;
    }

    // setting phone number
    public void setMyPanicPhoneNumber(String phone_number) {
	this._phone_number = phone_number;
    }
}