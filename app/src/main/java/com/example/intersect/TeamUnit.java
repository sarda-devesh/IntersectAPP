package com.example.intersect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TeamUnit {

    private String id;
    private String admin;
    private long start_time;
    private long end_time;
    private HashMap<String, ArrayList<CalendarEvent>> user_activity = new HashMap<String, ArrayList<CalendarEvent>>();
    private long meeting_time;
    private long meeting_start;

    public TeamUnit(String id, long start_time, long end_time, long meeting_time, String admin) {
        this.id = id;
        this.start_time = start_time;
        this.end_time = end_time;
        this.meeting_time = meeting_time;
        this.admin = admin;
    }

    public TeamUnit() {

    }

    public ArrayList<CalendarEvent> get_events_list(String email) {
        return this.user_activity.get(email);
    }

    public void update_events_list(String email, ArrayList<CalendarEvent> arr) {
        this.user_activity.put(email, arr);

    }

    public void update_values(long start_time, long end_time, long meeting_time) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.meeting_time = meeting_time;
        String[] emails = user_activity.keySet().toArray(new String[0]);
        for (String email : emails) {
            user_activity.put(email, null);
        }
    }

    public void figure_user_times() {
        if (all_entries_filled()) {
            ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
            for (ArrayList<CalendarEvent> particular : user_activity.values()) {
                for (CalendarEvent current : particular) {
                    events.add(current);
                }
            }
            Collections.sort(events);
            int index = 0;
            while (index < events.size()) {
                CalendarEvent item = events.get(index);
                index += 1;
                while (index < events.size() && events.get(index).getBegin() < item.getEnd()) {
                    CalendarEvent current = events.remove(index);
                    item.setEnd(Math.max(item.getEnd(), current.getEnd()));
                }
            }
        }
    }

    private boolean all_entries_filled() {
        for (ArrayList<CalendarEvent> entry : user_activity.values()) {
            if (entry == null) {
                return false;
            }
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStart_time() {
        return start_time;
    }


    public long getEnd_time() {
        return end_time;
    }


    public long getMeeting_time() {
        return meeting_time;
    }


    public long getMeeting_start() {
        return meeting_start;
    }

    public String getAdmin() {
        return this.admin;
    }

    public void setAdmin(String user_email, String new_admin) {
        if (this.admin == user_email) {
            this.admin = user_email;
        }
    }

    public void removeUser(String admin_email, String user) {
        if (this.admin == admin_email) {
            user_activity.remove(user);
        }
    }


}
