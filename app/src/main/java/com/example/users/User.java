package com.example.users;

public class User {
    //声明全局变量
    public static String USER_NAME="";
    public static String PASSWORD="";
    public static String ROLE="";
    public static String USER_PHONE="";
    public static boolean existing=false;
    public static int USER_ID= 15;//用户id

        //  这几个变量只有亲属端才有用
    public static boolean bindging=false;
    public static String BLIND_PHONE=""; // 此字符串在盲人端表达亲属电话
    public static int BLIND_ID=-1;
    public static int RELATION_ID=-1;
    public static int MODE=-1;
    public static double BLIND_LAT=0;
    public static double BLIND_LON=0;
    public static int BLIND_STATE=0;
    public static String BLIND_LOC="";


    public static int getUserId() {
        return USER_ID;
    }

    public static void setUserId(int userId) {
        USER_ID = userId;
    }

    public static String getUserName() {
        return USER_NAME;
    }

    public static boolean isExisting() {
        return existing;
    }

    public static void setExisting(boolean existing) {
        User.existing = existing;
    }

    public static void setUserName(String userName) {
        USER_NAME = userName;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

    public static void setPASSWORD(String PASSWORD) {
        User.PASSWORD = PASSWORD;
    }

    public static String getROLE() {
        return ROLE;
    }

    public static void setROLE(String ROLE) {
        User.ROLE = ROLE;
    }

    public static String getUserPhone() {
        return USER_PHONE;
    }

    public static String getBlindLoc(){return  BLIND_LOC;}

    public static void setUserPhone(String userPhone) {
        USER_PHONE = userPhone;
    }

    public static boolean isBindging() {
        return bindging;
    }

    public static void setBindging(boolean bindging) {
        User.bindging = bindging;
    }

    public static String getBlindPhone() {
        return BLIND_PHONE;
    }

    public static void setBlindPhone(String blindPhone) {
        BLIND_PHONE = blindPhone;
    }

    public static int getBlindId() {
        return BLIND_ID;
    }

    public static void setBlindId(int blindId) {
        BLIND_ID = blindId;
    }

    public static int getRelationId() {
        return RELATION_ID;
    }

    public static void setRelationId(int relationId) {
        RELATION_ID = relationId;
    }

    public static int getMODE() {
        return MODE;
    }

    public static void setMODE(int MODE) {
        User.MODE = MODE;
    }

    public static double getBlindLat() {
        return BLIND_LAT;
    }

    public static void setBlindLat(double blindLat) {
        BLIND_LAT = blindLat;
    }

    public static double getBlindLon() {
        return BLIND_LON;
    }

    public static void setBlindLon(double blindLon) {
        BLIND_LON = blindLon;
    }

    public static int getBlindState() {
        return BLIND_STATE;
    }

    public static void setBlindState(int blindState) {
        BLIND_STATE = blindState;
    }
    public  static void setBlindLoc(String loc){
        BLIND_LOC=loc;
    }
}
