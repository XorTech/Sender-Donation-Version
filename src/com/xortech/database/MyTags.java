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

public class MyTags {

    public int _id;
    public String _tag;
    public String _phone_number;
    public String _secret;
    public int _active;

    public MyTags() {
    	// NULL
    }

    /**
     * CONSTRUCTOR
     * @param id
     * @param tag
     * @param phone_number
     * @param secret
     * @param active
     */
    public MyTags(int id, String tag, String phone_number, String secret, int active) {
    	this._id = id;
    	this._tag = tag;
    	this._phone_number = phone_number;
    	this._secret = secret;
    	this._active = active;
    }

    /**
     * CONSTRUCTOR
     * @param tag
     * @param phone_number
     * @param secret
     * @param active
     */
    public MyTags(String tag, String phone_number, String secret, int active) {
    	this._tag = tag;
    	this._phone_number = phone_number;
    	this._secret = secret;
    	this._active = active;
    }

    /**
     * METHOD TO GET THE ID
     * @return
     */
    public int getID() {
    	return this._id;
    }

    /**
     * METHOD TO SET THE ID
     * @param id
     */
    public void setID(int id) {
    	this._id = id;
    }

    /**
     * METHOD TO GET THE TAG NAME
     * @return
     */
    public String getMyTag() {
    	return this._tag;
    }

    /**
     * METHOD TO SET THE TAG NAME
     * @param tag
     */
    public void setMyTag(String tag) {
    	this._tag = tag;
    }

    /**
     * METHOD TO GET THE PHONE NUMBER
     * @return
     */
    public String getMyTagPhoneNumber() {
    	return this._phone_number;
    }

    /**
     * METHOD TO SET THE PHONE NUMBER
     * @param phone_number
     */
    public void setMyTagPhoneNumber(String phone_number) {
    	this._phone_number = phone_number;
    }
    
    /**
     * METHOD TO GET THE SECRET
     * @return
     */
    public String getTagSecret() {
    	return this._secret;
    }

    /**
     * METHOD TO SET THE SECRET
     * @param secret
     */
    public void setTagSecret(String secret) {
    	this._secret = secret;
    }
    
    /**
     * METHOD TO GET THE STATUS
     * @return
     */
    public int getTagStatus() {
    	return this._active;
    }

    /**
     * METHOD TO SET THE STATUS
     * @param active
     */
    public void setTagStatus(int active) {
    	this._active = active;
    }
}