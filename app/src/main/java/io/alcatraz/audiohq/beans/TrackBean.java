package io.alcatraz.audiohq.beans;

public class TrackBean {
    public static final String TRACK_STATE_ACTIVE = "Active";
    public static final String TRACK_STATE_INACTIVE = "Inactive";

    private String thread;
    private String pid;
    private String sampleRate;
    private String frameSize;
    private String sessionId;
    private String active;

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(String frameSize) {
        this.frameSize = frameSize;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active.equals("yes") ? TRACK_STATE_ACTIVE : TRACK_STATE_INACTIVE;
    }
}
