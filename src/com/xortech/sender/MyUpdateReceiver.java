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
package com.xortech.sender;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyUpdateReceiver extends BroadcastReceiver {  
   @Override 
   public void onReceive(Context context, Intent intent) {  
      if ("Intent.ACTION_PACKAGE_REPLACED".equals(intent.getAction())) {
    	  trimCache(context);
      }
   }
   
	/**
	 * METHOD TO CLEAR THE APPLICATION CACHE
	 * */
   public static void trimCache(Context context) {
       try {
          File dir = context.getCacheDir();
          if (dir != null && dir.isDirectory()) {
             deleteDir(dir);
          }
       } catch (Exception e) {
    	   Log.e("Error clearing application cache! ", "" + e);
       }
    }
   
   /**
    * METHOD TO DELETE THE CACHE DIRECTORY
    * @param dir
    * @return
    */
    public static boolean deleteDir(File dir) {
       if (dir != null && dir.isDirectory()) {
          String[] children = dir.list();
          for (int i = 0; i < children.length; i++) {
             boolean success = deleteDir(new File(dir, children[i]));
             if (!success) {
                return false;
             }
          }
       }

       // THE DIRECTORY IS NOW EMPTY, SO DELETE IT
       return dir.delete();
    }
}
