package com.itdar.phonegap.smsreader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Amer on 06/12/14.
 */
public class SmsReaderPlugin extends CordovaPlugin {

    public enum ActionType{
        GET_UNREAD_SMS_MESSAGES, SET_SMS_MESSAGE_UNREAD, SET_SMS_MESSAGE_READ, DELETE_SMS_MESSAGE;
    }
    
    public static final int READ_THREAD = 1;
    public static final int UNREAD_THREAD = 0;
    
    final static String TAG = "mytag";

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        action=action.toUpperCase();
        Boolean result;
        switch(ActionType.valueOf(action)){
            case GET_UNREAD_SMS_MESSAGES:
            	callbackContext.sendPluginResult(
            			new PluginResult(
            					PluginResult.Status.OK,
            					getUnreadMessages(this.cordova.getActivity().getApplicationContext())
            					)
            			);
                result=true;
                break;
            case SET_SMS_MESSAGE_READ:
            	callbackContext.sendPluginResult(
            			new PluginResult(
            					PluginResult.Status.OK,
            					setMessageRead(this.cordova.getActivity().getApplicationContext(), args.getLong(0), true)));
                result=true;
                break;
            case SET_SMS_MESSAGE_UNREAD:
            	callbackContext.sendPluginResult(
            			new PluginResult(
            					PluginResult.Status.OK,
            					setMessageRead(this.cordova.getActivity().getApplicationContext(), args.getLong(0), false)));
                result=true;
            case DELETE_SMS_MESSAGE:
            	callbackContext.sendPluginResult(
            			new PluginResult(
            					PluginResult.Status.OK,
            					setMessageRead(this.cordova.getActivity().getApplicationContext(), args.getLong(0), false)));
                result=true;
            default:
                result=false;
        }
        return result;
	}

    
    
    /**
     * Marks a specific message as read
     */
    public static int setMessageRead(
            Context context, long messageId, Boolean setRead) {
        int result = 0;
    	
        if (messageId > 0) {
            ContentValues values = new ContentValues(1);
            values.put("read", setRead ? READ_THREAD : UNREAD_THREAD);

            Uri messageUri;

            messageUri = Uri.withAppendedPath(SMS_CONTENT_URI, String.valueOf(messageId));

            ContentResolver cr = context.getContentResolver();
            try {
                result = cr.update(messageUri, values, null, null);
            } catch (Exception e) {
                    Log.v(TAG, "setMessageRead(): Problem deleting message - " + e.toString() + " " + setRead.toString());
                result = -100;
            }
            Log.v(TAG, String.format("message id = %s marked as read, result = %s", messageId, result));
        }
        return result;
    }

    /**
     * Tries to delete a message from the system database, given the message id
     */
    public static int deleteMessage(Context context, long messageId) {

        int result = 0;

        if (messageId > 0) {
            // We need to mark this message read first to ensure the entire thread is marked as read
            setMessageRead(context, messageId, true);

            // Construct delete message uri
            Uri deleteUri;

            deleteUri = Uri.withAppendedPath(SMS_CONTENT_URI, String.valueOf(messageId));

            int count = 0;
            try {
                count = context.getContentResolver().delete(deleteUri, null, null);
                Log.v(TAG, "Messages deleted: " + count);
                result = 1;
            } catch (Exception e) {
                Log.v(TAG, "deleteMessage(): Problem deleting message - " + e.toString());
                result = -1;
            }
        }
        return result;
    }
    
    
    private static final String UNREAD_CONDITION = "read=0";
    public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

    /**
     * Fetches a list of unread messages from the system database
     *
     * @param context
     *            app context
     * @return JSONArray
     */

    public static JSONArray getUnreadMessages(Context context) {
        final String[] projection =
                new String[] { "_id", "thread_id", "address", "date", "body" };
        // Create cursor
        Cursor cursor = context.getContentResolver().query(
                SMS_INBOX_CONTENT_URI,
                projection,
                UNREAD_CONDITION,
                null,
                "date ASC");

        JSONArray messages = null;
        JSONObject message = null;

        if (cursor != null) {
            try {
                int count = cursor.getCount();
                if (count > 0) {
                    messages = new JSONArray();
                    while (cursor.moveToNext()) {
                        message = new JSONObject();
                        message.put("id", cursor.getLong(0));
                        message.put("threadId", cursor.getLong(1));
                        message.put("address", cursor.getString(2));
                        message.put("timestamp", cursor.getLong(3));
                        message.put("body", cursor.getString(4));
                        messages.put(message);
                    }
                }
            } catch (JSONException e) {
				//e.printStackTrace();
                Log.v(TAG, "getUnreadMessages: JSONException - " + e.toString());
				messages = null;
			} finally {
                cursor.close();
            }
        }
        return messages;
    }
}
