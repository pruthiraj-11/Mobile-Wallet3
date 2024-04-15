package org.mifos.mobilewallet.core.domain.model.user;

public class User1 {
    private long userId;
    private String username;
    private String authenticationKey;

    public User1() {
    }

    public User1(long userId, String username, String authenticationKey) {
        this.userId = userId;
        this.username = username;
        this.authenticationKey = authenticationKey;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

}
