package com.sayt.godslove.utils;

public class Constants {


    private static String SERVER_URL = "https://meet.jit.si";


    public static String getServerUrl() {
        return SERVER_URL;
    }

    public static void setServerUrl(String serverUrl) {
        SERVER_URL = serverUrl;
    }

//    private static String BASE_URL = "http://192.168.8.101/godslove_nsromeet/";
//private static String BASE_URL = "http://godslove.mywebcommunity.org/";
    private static String BASE_URL = "http://godslove.nsromeet.com/";

    public static String GET_URL = BASE_URL+ "get";
    public static String STUDENT = BASE_URL+ "student";
    public static String STUDENT_ADD_UPDATE = BASE_URL+ "insert_update";
}
