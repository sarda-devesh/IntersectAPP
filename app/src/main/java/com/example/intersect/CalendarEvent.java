package com.example.intersect;

import java.util.Date;

public class CalendarEvent implements Comparable<CalendarEvent>{

    private String title;
    private long begin, end;
    private String location;

    public CalendarEvent() {

    }

    public CalendarEvent(String title, long begin, long end, String location) {
        setTitle(title);
        setBegin(begin);
        setEnd(end);
        setLocation(location);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString(){
        return getTitle() + " " + getBegin() + " " + getEnd() + " " + getLocation();
    }

    @Override
    public int compareTo(CalendarEvent other) {
        int first = Long.compare(this.begin, other.begin);
        if(first == 0) {
            Long.compare(this.end, other.end);
        }
        return first;
    }
}
