package com.nsromapa.nsromeet.models;

import java.io.Serializable;

public class ScheduleListModel implements Serializable {
    private String id;
    private String forid;
    private String tid;
    private String title;
    private String conference_id;
    private String date_string;
    private String duration_hours;
    private String duration_minutes;
    private String time_stamp;
    private String host_type;
    private String status;
    private String created_by;
    private String creator_name;
    private String creator_idtype;
    private String sched_type;
    private String sched_type_id;
    private String sched_type_name;
    private String deleted;
    private String active;
    private int Index;

    public ScheduleListModel(String id,String forid, String tid, String title, String conference_id,
                             String date_string, String duration_hours, String duration_minutes,
                             String time_stamp, String host_type, String status, String created_by, String creator_name,
                             String creator_idtype, String sched_type, String sched_type_id, String  sched_type_name,String deleted, String active) {
        this.id = id;
        this.forid = forid;
        this.tid = tid;
        this.title = title;
        this.conference_id = conference_id;
        this.date_string = date_string;
        this.duration_hours = duration_hours;
        this.duration_minutes = duration_minutes;
        this.time_stamp = time_stamp;
        this.host_type = host_type;
        this.status = status;
        this.created_by = created_by;
        this.sched_type = sched_type;
        this.sched_type_id = sched_type_id;
        this.sched_type_name = sched_type_name;
        this.deleted = deleted;
        this.creator_name = creator_name;
        this.creator_idtype = creator_idtype;
        this.active = active;
    }

    public String getSched_type() {
        return sched_type;
    }

    public void setSched_type(String sched_type) {
        this.sched_type = sched_type;
    }

    public String getSched_type_id() {
        return sched_type_id;
    }

    public void setSched_type_id(String sched_type_id) {
        this.sched_type_id = sched_type_id;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String tid) {
        this.id = id;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConference_id() {
        return conference_id;
    }

    public void setConference_id(String conference_id) {
        this.conference_id = conference_id;
    }

    public String getDate_string() {
        return date_string;
    }

    public void setDate_string(String date_string) {
        this.date_string = date_string;
    }

    public String getDuration_hours() {
        return duration_hours;
    }

    public void setDuration_hours(String duration_hours) {
        this.duration_hours = duration_hours;
    }

    public String getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(String duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getHost_type() {
        return host_type;
    }

    public void setHost_type(String host_type) {
        this.host_type = host_type;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getCreator_name() {
        return creator_name;
    }

    public void setCreator_name(String creator_name) {
        this.creator_name = creator_name;
    }

    public String getCreator_idtype() {
        return creator_idtype;
    }

    public void setCreator_idtype(String creator_idtype) {
        this.creator_idtype = creator_idtype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getForid() {
        return forid;
    }

    public void setForid(String forid) {
        this.forid = forid;
    }

    public String getSched_type_name() {
        return sched_type_name;
    }
}
