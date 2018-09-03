package com.example.kennck.mirroring.objects;

public class Helper {
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String PUBLIC_FOLDER = "http://mirroring.classworx.co/public/";
    public static final String PUBLIC_FOLDER_API = "http://mirroring.classworx.co/public/";
    public static final String CREATE_ACCOUNT = PUBLIC_FOLDER_API + "accounts/create";
    public static final String RETRIEVE_ACCOUNT = PUBLIC_FOLDER_API + "accounts/retrieve";
    public static final String LOGIN = PUBLIC_FOLDER_API + "accounts/login_mobile";
    public static final String CREATE_RECORD = PUBLIC_FOLDER_API + "records/create";
    public static final String RETRIEVE_RECORD = PUBLIC_FOLDER_API + "records/retrieve";
    public static final String RETRIEVE_RECORD_CUSTOM = PUBLIC_FOLDER_API + "records/retrieve_custom";
    public static final String DIRECTORY = "/recording/";
    public static final int PORT = 8888;
    public static final int TIME_OUT = 500;
    public static ThreadGroup threadGroup = new ThreadGroup("Mirroring");
}
