package com.indieweb.indigenous.model;

import android.accounts.Account;

public class User {

    private String me;
    private String accessToken;
    private String microsubEndpoint;
    private String micropubEndpoint;
    private String micropubMediaEndpoint;
    private String tokenEndpoint;
    private String authorizationEndpoint;
    private String syndicationTargets;
    private String avatar;
    private String name;
    private String postTypes;
    private boolean valid = false;
    private Account account;

    public String getMe() {
        return me;
    }

    public String getMeWithoutProtocol() {
        return me.replace("https://","").replace("http://", "");
    }

    public void setMe(String me) {
        this.me = me;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMicropubEndpoint() {
        return micropubEndpoint != null ? micropubEndpoint : "";
    }

    public void setMicropubEndpoint(String micropubEndpoint) {
        this.micropubEndpoint = micropubEndpoint;
    }

    public String getMicropubMediaEndpoint() {
        return micropubMediaEndpoint != null ? micropubMediaEndpoint : "";
    }

    public void setMicropubMediaEndpoint(String micropubMediaEndpoint) {
        this.micropubMediaEndpoint = micropubMediaEndpoint;
    }

    public String getMicrosubEndpoint() {
        return microsubEndpoint != null ? microsubEndpoint : "";
    }

    public void setMicrosubEndpoint(String microsubEndpoint) {
        this.microsubEndpoint = microsubEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint != null ? tokenEndpoint : "";
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint != null ? authorizationEndpoint : "";
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getSyndicationTargets() {
        return syndicationTargets != null ? syndicationTargets : "";
    }

    public void setSyndicationTargets(String syndicationTargets) {
        this.syndicationTargets = syndicationTargets;
    }

    public String getPostTypes() {
        return postTypes != null ? postTypes : "";
    }

    public void setPostTypes(String postTypes) {
        this.postTypes = postTypes;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getAvatar() {
        return avatar != null ? avatar : "";
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }
}
