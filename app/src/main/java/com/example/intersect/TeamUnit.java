package com.example.intersect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TeamUnit {

    private String id;
    public HashMap<String, ArrayList<CalendarEvent>> user_activity = new HashMap<>();
    private String admin;
    private long start_time;
    private long end_time;
    public HashMap<String, Integer> team_members = new HashMap<>();
    private String name;
    private long meeting_time;
    private long meeting_start;

    public TeamUnit(String id, String name, String admin) {
        this.id = id;
        this.name = name;
        this.start_time = 1;
        this.end_time = 1;
        this.meeting_time = 1;
        this.meeting_start = 1;
        this.admin = admin;
        this.add_user(admin);
    }

    public TeamUnit() {

    }

    public void add_user(String user_email) {
        this.user_activity.put(user_email, new ArrayList<CalendarEvent>());
        user_activity.get(user_email).add(new CalendarEvent("Testing", 0, 1, "Nowhere"));
    }

    @Override
    public String toString() {
        return this.id + " " + this.name + " " + this.admin + " " + this.start_time + " " + this.end_time + " " + this.meeting_time + " " + this.meeting_start;
    }


    public void update_values(long start_time, long end_time, long meeting_time) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.meeting_time = meeting_time;
        System.out.println("Updated settings: " + this.start_time + " " + this.end_time + " " + this.meeting_time);
        this.meeting_start = 0;
        Set<String> user_names = user_activity.keySet();
        for (String email : user_names) {
            this.add_user(email);
        }
        System.out.println("Finished updating values: " + this.user_activity.keySet());
    }

    private long[] get_best_range(long range_start, long range_end, ArrayList<CalendarEvent> complete_list) {
        long[] suggest_started = {-1590L, -10};
        ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
        CalendarEvent ce_current = complete_list.get(0);
        while (ce_current.getBegin() > range_start && ce_current.getEnd() < range_end) {
            events.add(complete_list.remove(0));
            ce_current = complete_list.get(0);
        }
        int index = 0;
        while (index < events.size()) {
            CalendarEvent item = events.get(index);
            index += 1;
            while (index < events.size() && events.get(index).getBegin() < item.getEnd()) {
                CalendarEvent current = events.remove(index);
                item.setEnd(Math.max(item.getEnd(), current.getEnd()));
            }
        }
        ArrayList<String> gaps = new ArrayList<String>();
        CalendarEvent first_start = events.get(0);
        if (first_start.getBegin() > this.start_time) {
            gaps.add(this.start_time + "," + first_start.getBegin());
        }
        for (int i = 1; i < events.size(); i++) {
            gaps.add(events.get(i - 1).getEnd() + "," + events.get(i).getBegin());
        }
        first_start = events.get(events.size() - 1);
        if (first_start.getEnd() < this.end_time) {
            gaps.add(first_start.getEnd() + "," + this.end_time);
        }
        Collections.sort(gaps, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] s1 = o1.split(",");
                String[] s2 = o2.split(",");
                long difference1 = Math.abs(Long.parseLong(s1[1]) - Long.parseLong(s1[0]));
                long differenc2 = Math.abs(Long.parseLong(s2[1]) - Long.parseLong(s2[0]));
                return Long.compare(difference1, differenc2);
            }
        });
        String[] best_gap = gaps.get(gaps.size() - 1).split(",");
        long best_start = Long.parseLong(best_gap[0]);
        long best_end = Long.parseLong(best_gap[1]);
        if (best_end - best_start >= this.meeting_time) {
            long middle = (best_end + best_start) / 2;
            suggest_started[0] = middle - this.meeting_time / 2;
            suggest_started[1] = best_end - best_start;
        }
        return suggest_started;
    }


    public void figure_user_times() {
        if (all_entries_filled()) {
            ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
            for (ArrayList<CalendarEvent> particular : user_activity.values()) {
                for (CalendarEvent current : particular) {
                    if (current.getBegin() != 0) {
                        events.add(current);
                    }
                }
            }
            Collections.sort(events);
            long starting_range = this.start_time + TimeUnit.HOURS.toMillis(15);
            long greatest_difference = 0;
            this.start_time = -1590L;
            while (starting_range < this.end_time) {
                long[] suggest_data = this.get_best_range(starting_range, starting_range + TimeUnit.HOURS.toMillis(6), events);
                if (suggest_data[0] > 0 && suggest_data[1] > greatest_difference) {
                    this.start_time = suggest_data[0];
                    greatest_difference = suggest_data[1];
                }
            }
        }
    }

    private boolean all_entries_filled() {
        for (ArrayList<CalendarEvent> entry : user_activity.values()) {
            if (entry.size() == 1 & entry.get(0).getBegin() == 0) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean setAdmin(String user_email, String new_admin) {
        if (this.admin.equals(user_email)) {
            this.admin = new_admin;
            return true;
        }
        return false;
    }

    public boolean removeUser(String admin_email, String user) {
        if (this.admin.equals(admin_email)) {
            user_activity.remove(user);
            return true;
        }
        return false;
    }

}
