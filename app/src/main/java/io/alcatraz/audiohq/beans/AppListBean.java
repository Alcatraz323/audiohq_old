package io.alcatraz.audiohq.beans;

import android.graphics.drawable.Drawable;

import java.util.LinkedList;

public class AppListBean {
    private String pkgName;
    private Drawable icon;
    private String profile = "1,1,1,0";
    private String default_profile;
    private String pid;
    private boolean muted;
    private LinkedList<TrackBean> tracks = new LinkedList<>();

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public LinkedList<TrackBean> getTracks() {
        return tracks;
    }

    public void clearTracks() {
        tracks.clear();
    }

    public void addTrack(TrackBean track) {
        tracks.add(track);
    }

    public TrackBean getTrack(int index) {
        return tracks.get(index);
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int activeCount() {
        int count = 0;
        for (TrackBean track : tracks) {
            if (track.getActive().equals(TrackBean.TRACK_STATE_ACTIVE))
                count++;
        }
        return count;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public String getDefault_profile() {
        return default_profile;
    }

    public void setDefault_profile(String default_profile) {
        this.default_profile = default_profile;
    }
}
