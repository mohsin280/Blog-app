package com.mi.loginfirebase;

import android.app.Application;

public class GrpCode extends Application {
    private String grpCode;

    public String getGrpCode() {
        return grpCode;
    }

    public void setGrpCode(String grpCode) {
        this.grpCode = grpCode;
    }
}
