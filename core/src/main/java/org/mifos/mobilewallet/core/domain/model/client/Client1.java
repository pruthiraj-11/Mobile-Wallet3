package org.mifos.mobilewallet.core.domain.model.client;

public class Client1 {
    private String name;
    private String image;
    private String externalId;
    private long clientId;
    private String displayName;
    private String mobileNo;

    public Client1(String name, String image, String externalId, long clientId, String displayName, String mobileNo) {
        this.name = name;
        this.image = image;
        this.externalId = externalId;
        this.clientId = clientId;
        this.displayName = displayName;
        this.mobileNo = mobileNo;
    }

    public Client1() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }
}
