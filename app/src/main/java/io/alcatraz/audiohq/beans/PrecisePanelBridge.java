package io.alcatraz.audiohq.beans;

import android.app.AlertDialog;

public class PrecisePanelBridge {
    private AlertDialog alertDialog;
    private float left;
    private float right;
    private float general;
    private boolean control_lr;

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public void setAlertDialog(AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getRight() {
        return right;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float getGeneral() {
        return general;
    }

    public void setGeneral(float general) {
        this.general = general;
    }

    public boolean isControl_lr() {
        return control_lr;
    }

    public void setControl_lr(boolean control_lr) {
        this.control_lr = control_lr;
    }
}
