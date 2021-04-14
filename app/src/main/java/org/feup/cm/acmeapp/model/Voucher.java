package org.feup.cm.acmeapp.model;

import java.util.Date;

public class Voucher {
    private String _id;
    private String userId;
    private Date createdAt;

    public Voucher() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    @Override
    public String toString() {
        return get_id();
    }
}
