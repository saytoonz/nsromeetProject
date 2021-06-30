package com.sayt.godslove.models;

public class Added_Classes_Model {
    private String id;
    private String tid;
    private String student;
    private String _class;
    private String secret_key;
    private String month;
    private String permit;
    private String active;
    private String conferenceId;
    private int itemIndex;

    public Added_Classes_Model(String id, String tid, String student, String _class,
                               String secret_key, String month, String conferenceId, String permit, String active) {
        this.id = id;
        this.tid = tid;
        this.student = student;
        this._class = _class;
        this.secret_key = secret_key;
        this.month = month;
        this.conferenceId = conferenceId;
        this.permit = permit;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
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

    public String getPermit() {
        return permit;
    }

    public void setPermit(String permit) {
        this.permit = permit;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }
}
