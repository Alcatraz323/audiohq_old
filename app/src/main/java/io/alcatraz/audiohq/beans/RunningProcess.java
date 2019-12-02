package io.alcatraz.audiohq.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class RunningProcess implements Parcelable {
    private String pid;
    private String processName;
    private String cmdline;

    public RunningProcess(){}

    protected RunningProcess(Parcel in) {
        pid = in.readString();
        processName = in.readString();
        cmdline = in.readString();
    }

    public static final Creator<RunningProcess> CREATOR = new Creator<RunningProcess>() {
        @Override
        public RunningProcess createFromParcel(Parcel in) {
            return new RunningProcess(in);
        }

        @Override
        public RunningProcess[] newArray(int size) {
            return new RunningProcess[size];
        }
    };

    public String getCmdline() {
        return cmdline;
    }

    public void setCmdline(String cmdline) {
        this.cmdline = cmdline;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(pid);
        parcel.writeString(processName);
        parcel.writeString(cmdline);
    }
}
