package com.juankyapps.igenda.model;

import java.util.Date;

public class Event {
    private String id;
    private String title;
    private String location;
    private String note;
    private Date start;
    private Date end;

    public Event(String id, String title, String location, String note, Date start, Date end) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.note = note;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getNote() {
        return note;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

}
