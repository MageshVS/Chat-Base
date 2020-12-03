package com.example.chatbase.Utils;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotification {
    public SendNotification(String message, String heading, String notificationKey){
        try {
            notificationKey = "b89ab1cf-60fd-4780-b279-4e0cb1d61898";
            JSONObject notificationContent = new JSONObject(
                    "{'contents':{'en':'" + message + "'},"+
                    "'include_player_ids':['" + notificationKey + "']," +
                    "'headings':{'en': '" + heading + "'}}");
            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
