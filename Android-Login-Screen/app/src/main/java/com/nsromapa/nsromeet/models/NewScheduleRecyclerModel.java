package com.nsromapa.nsromeet.models;

import java.io.Serializable;

public class NewScheduleRecyclerModel implements Serializable {
    private String tid;
    private String type;
    private String name;
    private boolean selected = false;
    private int index;

    public NewScheduleRecyclerModel(String tid, String type, String name) {
        this.tid = tid;
        this.type = type;
        this.name = name;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
