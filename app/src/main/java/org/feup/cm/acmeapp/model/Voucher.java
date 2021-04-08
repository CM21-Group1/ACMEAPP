package org.feup.cm.acmeapp.model;

import java.util.Date;

public class Voucher {
    private String userId;
    private Date createdAt;

    public Voucher() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
