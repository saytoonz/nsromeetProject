package com.nsromapa.nsromeet.models;

import java.io.Serializable;

public class SchoolsetupModel implements Serializable {
    private String its_what;
    private String id = "";
    private String schoolName = "";
    private String facultyName = "";
    private String departmentName = "";
    private String className = "";
    private String levelNmae="";
    private String conference = "";
    private String location = "";
    private String reports = "";
    private String created_by = "";
    private String date_added = "";
    private int itemIndex;
    private boolean i_added = false;


    public SchoolsetupModel(String its_what, String id, String schoolName, String facultyName,
                            String departmentName, String className, String levelNmae,
                            String conference, String reports, String created_by, String date_added, String location) {
        this.its_what = its_what;
        this.id = id;
        this.schoolName = schoolName;
        this.facultyName = facultyName;
        this.departmentName = departmentName;
        this.className = className;
        this.levelNmae = levelNmae;
        this.conference = conference;
        this.created_by = created_by;
        this.date_added = date_added;
        this.reports = reports;
        this.location = location;
    }


    public boolean isI_added() {
        return i_added;
    }

    public void setI_added(boolean i_added) {
        this.i_added = i_added;
    }

    public String getIts_what() {
        return its_what;
    }

    public void setIts_what(String its_what) {
        this.its_what = its_what;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLevelNmae() {
        return levelNmae;
    }

    public void setLevelNmae(String levelNmae) {
        this.levelNmae = levelNmae;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String ate_added) {
        this.date_added = ate_added;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public String getReports() {
        return reports;
    }

    public void setReports(String reports) {
        this.reports = reports;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
