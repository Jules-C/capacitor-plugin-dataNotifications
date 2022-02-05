package com.julesc.dataNotifications;

import com.getcapacitor.JSObject;

public class BackgroundFCMRemoteMessage {

    private String id;
    private JSObject data;

    public JSObject getData() {
        return data;
    }

    public void setData(JSObject data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
