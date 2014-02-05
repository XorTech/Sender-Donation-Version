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

public class MyPanicNumbers {

    public int _id;
    public String _tag;
    public String _phone_number;
    public String _email;
    public int _active;
    public int _p_active;
    public int _e_active;

    public MyPanicNumbers() {
    	// NULL
    }

    /**
     * CONSTRUCTOR
     * @param id
     * @param tag
     * @param phone_number
     * @param email
     * @param active
     * @param p_active
     * @param e_active
     */
    public MyPanicNumbers(int id, String tag, String phone_number, String email, int active,
    		int p_active, int e_active) {
    	this._id = id;
    	this._tag = tag;
    	this._phone_number = phone_number;
    	this._email = email;
    	this._active = active;
    	this._p_active = p_active;
    	this._e_active = e_active;
    }

    /**
     * CONSTRUCTOR
     * @param tag
     * @param phone_number
     * @param email
     * @param active
     * @param p_active
     * @param e_active
     */
    public MyPanicNumbers(String tag, String phone_number, String email, int active,
    		int p_active, int e_active) {
    	this._tag = tag;
    	this._phone_number = phone_number;
    	this._email = email;
    	this._active = active;
    	this._p_active = p_active;
    	this._e_active = e_active;
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
    public String getMyPanicTag() {
    	return this._tag;
    }

    /**
     * METHOD TO SET THE TAG NAME
     * @param tag
     */
    public void setMyPanicTag(String tag) {
    	this._tag = tag;
    }

    /**
     * METHOD TO GET THE PHONE NUMBER
     * @return
     */
    public String getMyPanicPhoneNumber() {
    	return this._phone_number;
    }

    /**
     * METHOD TO SET THE PHONE NUMBER
     * @param phone_number
     */
    public void setMyPanicPhoneNumber(String phone_number) {
    	this._phone_number = phone_number;
    }
    
    /**
     * METHOD TO GET THE EMAIL
     * @return
     */
    public String getMyPanicEmail() {
    	return this._email;
    }

    /**
     * METHOD TO SET THE EMAIL
     * @param email
     */
    public void setMyPanicEmail(String email) {
    	this._email = email;
    }
    
    /**
     * METHOD TO DETERMINE IF THE NUMBER IS ACTIVE
     * @return
     */
    public int getPanicActive() {
    	return this._active;
    }

    /**
     * METHOD TO SET THE ACTIVE STATE OF THE NUMBER
     * @param active
     */
    public void setPanicActive(int active) {
    	this._active = active;
    }
    
    /**
     * METHOD TO GET THE STATUS OF THE PHONE
     * @return
     */
    public int getPanicActiveP() {
    	return this._p_active;
    }

    /**
     * METHOD TO SET THE STATUS OF THE PHONE
     * @param active
     */
    public void setPanicActiveP(int active) {
    	this._p_active = active;
    }
    
    /**
     * METHOD TO GET THE STATUS OF THE EMAIL
     * @return
     */
    public int getPanicActiveE() {
    	return this._e_active;
    }

    /**
     * METHOD TO SET THE STATUS OF THE EMAIL
     * @param active
     */
    public void setPanicActiveE(int active) {
    	this._e_active = active;
    }
}