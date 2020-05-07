package com.example.intersect;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;


public class CalendarService {

    public static ArrayList<CalendarEvent> readCalendar(Context context, long lower, long higher, String email) {
        ContentResolver contentResolver = context.getContentResolver();
        email = email.replace(",", ".");
        System.out.println("Looking for " + lower + " " + higher + " " + email);
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
                new String[]{"calendar_displayName","title", "dtstart", "dtend", "eventLocation"},
                null, null, null);
        ArrayList<CalendarEvent> ce = new ArrayList<CalendarEvent>();
        try
        {
            if(cursor.getCount() > 0)
            {
                while (cursor.moveToNext()) {
                    String display_name =  "" + cursor.getString(0);
                    String event_name = cursor.getString(1);
                    long start = cursor.getLong(2);
                    long end =  cursor.getLong(3);
                    String location = "" + cursor.getString(4);
                    if(start > lower && start < higher && display_name.equals(email)) {
                        CalendarEvent current = new CalendarEvent(event_name, start, end, location);
                        ce.add(current);
                    }
                }
            }
            Collections.sort(ce);
            System.out.println("The Calendar looks like " + ce.toString());
            return ce;
        }
        catch(Exception e)
        {
            Log.w("Error: ", e.getMessage());
        }
        return null;
    }

}
