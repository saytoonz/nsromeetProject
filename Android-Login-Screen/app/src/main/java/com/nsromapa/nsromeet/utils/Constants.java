package com.nsromapa.nsromeet.utils;

public class Constants {
    private static String PRODUCTION_TYPE = "debug";

    public static void setServerUrl(String serverUrl) {
        SERVER_URL = serverUrl;
    }

    public static String SERVER_URL = "https://meet.jit.si";
//  public static String SERVER_URL = "https://meeting.nsromeet.com";

    public static String BASEURL = "http://192.168.8.100/nsromeet/";
//    public static String BASEURL = "https://nsromeet.app.nsromapa.com/";

    public static String TERMS_AND_POLICIES = "https://nsromeet.com/Privacy_Policy.php";

    public static String APP = BASEURL + "app";
    public static String USER = BASEURL + "user";
    public static String LOGIN_URL = BASEURL + "enter";
    public static String SIGNUP_URL = BASEURL + "join";
    public static String CHANGE_PASS_URL = BASEURL + "changepass";
    public static String CHANGE_NAME_URL = BASEURL + "changename";
    public static String FUID_PHONE_ACTIVE = BASEURL + "fuidphoneactive";
    public static String FB_USER = BASEURL + "firebaseuser";
    public static String ADD_SCHOOL_URL = BASEURL + "addschool";
    public static String ADD_FACULTY_URL = BASEURL + "addfaculty";
    public static String ADD_DEPARTMENT_URL = BASEURL + "adddepartment";
    public static String ADD_CLASS_URL = BASEURL + "addclass";
    public static String GET_ALL = BASEURL + "getall";
    public static String NEW_SCHEDULE = BASEURL + "new_sched";
    public static String UPDATE_SINGLE_SCHEDULE_VALUE = BASEURL + "update_sing_sched";
    public static String GET_USER_SCHEDULES = BASEURL + "get_user_sched";
    public static String GET_SINGLE_SCHEDULE = BASEURL + "one_schedule";
    public static String CREATE_GROUP = BASEURL + "write_group";
    public static String GET_USER_GROUPS = BASEURL + "groups";
    public static String GET_SINGLE_GROUP = BASEURL + "one_group";
    public static String ADD_GROUP_MEMBER = BASEURL + "add_member";
    public static String PROFILE_IMAGE = BASEURL + "profile";
    public static String ID_TYPE = BASEURL + "idtype";
    public static final String GROUP_REVOKE = BASEURL + "group_revoke";
}
