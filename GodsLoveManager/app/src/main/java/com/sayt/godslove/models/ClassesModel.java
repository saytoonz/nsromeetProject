package com.sayt.godslove.models;

import java.io.Serializable;

public class ClassesModel implements Serializable {
    private String name ="";
    private String conference_id ="";
    private String date_added ="";

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private int index;

    public ClassesModel(String name, String conference_id, String date_added) {
        this.name = name;
        this.conference_id = conference_id;
        this.date_added = date_added;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConference_id() {
        return conference_id;
    }

    public void setConference_id(String conference_id) {
        this.conference_id = conference_id;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }
}
