package com.nsromapa.nsromeet.models;

import java.io.Serializable;

public class GroupListModel implements Serializable {
    private String id;
    private String tid;
    private String gid;
    private String name;
    private String description;
    private String conference_id;
    private String privacy;
    private String link;
    private String created_by;
    private String creator_name;
    private String created_idtype;
    private String date_added;
    private String active;
    private int Index;


    public GroupListModel(String id, String tid, String gid, String name, String description,
                          String conference_id, String privacy, String link,
                          String created_by, String creator_name, String created_idtype,
                          String date_added, String active) {
        this.id = id;
        this.tid = tid;
        this.gid = gid;
        this.name = name;
        this.description = description;
        this.conference_id = conference_id;
        this.privacy = privacy;
        this.link = link;
        this.created_by = created_by;
        this.creator_name = creator_name;
        this.created_idtype = created_idtype;
        this.date_added = date_added;
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

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConference_id() {
        return conference_id;
    }

    public void setConference_id(String conference_id) {
        this.conference_id = conference_id;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getCreator_name() {
        return creator_name;
    }

    public void setCreator_name(String creator_name) {
        this.creator_name = creator_name;
    }

    public String getCreated_idtype() {
        return created_idtype;
    }

    public void setCreated_idtype(String created_idtype) {
        this.created_idtype = created_idtype;
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
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }
}
