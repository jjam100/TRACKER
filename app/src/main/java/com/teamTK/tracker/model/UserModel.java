package com.teamTK.tracker.model;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kakao.usermgmt.response.model.User;

public class UserModel {

    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("userid")
    @Expose
    private String userid;
    @SerializedName("usernm")
    @Expose
    private String usernm;
    @SerializedName("tracker")
    @Expose
    private List<Tracker> tracker = null;
    @SerializedName("userphoto")
    @Expose
    private String userphoto = null;



    public String getUserphoto() {
        return userphoto;
    }

    public void setUserphoto(String userphoto) {
        this.userphoto = userphoto;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsernm() {
        return usernm;
    }

    public void setUsernm(String usernm) {
        this.usernm = usernm;
    }

    public List<Tracker> getTracker() {
        return tracker;
    }

    public void setTracker(List<Tracker> tracker) {
        this.tracker = tracker;
    }

}