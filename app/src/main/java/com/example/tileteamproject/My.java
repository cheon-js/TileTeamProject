package com.example.tileteamproject;

public class My {
    public String profile;
    public String title;
    public String content;
    public String username;




    public My(){}


    public My(String title, String content, String username , String profile){
        this.title = title;
        this.content = content;
        this.username = username;
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}