package io.alcatraz.audiohq.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class ProcessProfile implements Parcelable{

    private String processName;
    private float left;
    private float right;
    private float general;
    private boolean control_lr;
    private String out_flags;
    private String out_device;

    public ProcessProfile(String processName){
        this.processName = processName;
    }

    protected ProcessProfile(Parcel in) {
        processName = in.readString();
        left = in.readFloat();
        right = in.readFloat();
        general = in.readFloat();
        control_lr = in.readByte() != 0;
        out_flags = in.readString();
        out_device = in.readString();
    }

    public static final Creator<ProcessProfile> CREATOR = new Creator<ProcessProfile>() {
        @Override
        public ProcessProfile createFromParcel(Parcel in) {
            return new ProcessProfile(in);
        }

        @Override
        public ProcessProfile[] newArray(int size) {
            return new ProcessProfile[size];
        }
    };

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

    public void setOut_flags(String out_flags) {
        this.out_flags = out_flags;
    }

    public String getOut_flags() {
        return out_flags;
    }

    public void setOut_device(String out_device) {
        this.out_device = out_device;
    }

    public String getOut_device() {
        return out_device;
    }

    public boolean getControl_lr() {
        return control_lr;
    }

    public void setControl_lr(boolean control_lr) {
        this.control_lr = control_lr;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(processName);
        parcel.writeFloat(left);
        parcel.writeFloat(right);
        parcel.writeFloat(general);
        parcel.writeByte((byte) (control_lr ? 1 : 0));
        parcel.writeString(out_flags);
        parcel.writeString(out_device);
    }
}
