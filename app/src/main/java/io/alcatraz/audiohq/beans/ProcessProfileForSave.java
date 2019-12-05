package io.alcatraz.audiohq.beans;

public class ProcessProfileForSave {
    private String processName;
    private float left;
    private float right;
    private float general;
    private boolean control_lr;

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getLeft() {
        return left;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public float getRight() {
        return right;
    }

    public void setGeneral(float general) {
        this.general = general;
    }

    public float getGeneral() {
        return general;
    }

    public boolean getControl_lr() {
        return control_lr;
    }

    public void setControl_lr(boolean control_lr) {
        this.control_lr = control_lr;
    }

    public String buildSelf() {
        return processName + ";" + left + ";" + right + ";" + general + ";" + control_lr;
    }
}
