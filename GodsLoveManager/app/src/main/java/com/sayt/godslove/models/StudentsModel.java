package com.sayt.godslove.models;

import java.io.Serializable;

public class StudentsModel implements Serializable {
    private String id = "";
    private String student = "";
    private String _class = "";
    private String secret_key = "";
    private String month = "";
    private String conferenceId = "";
    private String permit = "";
    private String last_update = "";
    private String date_added = "";
    private String active = "";
    private int index;

    public StudentsModel(String id, String student, String _class, String secret_key,
                         String month, String conferenceId, String permit,
                         String last_update, String date_added, String active) {
        this.id = id;
        this.student = student;
        this._class = _class;
        this.secret_key = secret_key;
        this.month = month;
        this.conferenceId = conferenceId;
        this.permit = permit;
        this.last_update = last_update;
        this.date_added = date_added;
        this.active = active;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public String getSecret_key() {
        return secret_key;
    }

    public void setSecret_key(String secret_key) {
        this.secret_key = secret_key;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getPermit() {
        return permit;
    }

    public void setPermit(String permit) {
        this.permit = permit;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
