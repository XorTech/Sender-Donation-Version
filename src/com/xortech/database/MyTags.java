package com.xortech.database;

public class MyTags {

    // private variables
    public int _id;
    public String _tag;
    public String _phone_number;
    public String _secret;

    public MyTags() {
    }

    // constructor
    public MyTags(int id, String tag, String phone_number, String secret) {
	this._id = id;
	this._tag = tag;
	this._phone_number = phone_number;
	this._secret = secret;
    }

    // constructor
    public MyTags(String tag, String phone_number, String secret) {
	this._tag = tag;
	this._phone_number = phone_number;
	this._secret = secret;
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
    public String getMyTag() {
	return this._tag;
    }

    // setting tag
    public void setMyTag(String tag) {
	this._tag = tag;
    }

    // getting phone number
    public String getMyTagPhoneNumber() {
	return this._phone_number;
    }

    // setting phone number
    public void setMyTagPhoneNumber(String phone_number) {
	this._phone_number = phone_number;
    }
    
    // getting phone number
    public String getTagSecret() {
	return this._secret;
    }

    // setting phone number
    public void setTagSecret(String secret) {
	this._secret = secret;
    }
    
}