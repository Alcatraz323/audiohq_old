package io.alcatraz.audiohq.beans;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.alcatraz.audiohq.LogBuff;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.utils.IOUtils;

public class AdjustProfiles {
    public static String DEFAULT_PROFILE_POSITION_AFFIX = "/java_adjust_profiles/adjust_profiles.json";
    private Context mContext;

    private String targetLink;
    private List<ProcessProfile> processProfile;

    public void setProcessProfile(List<ProcessProfile> processProfile) {
        this.processProfile = processProfile;
    }

    public List<ProcessProfile> getProcessProfile() {
        return processProfile;
    }

    public String getTargetLink() {
        return targetLink;
    }

    public void setTargetLink(String targetLink) {
        this.targetLink = targetLink;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void saveToLocal() {
        IOUtils.Okiowrite(mContext.getFilesDir() + DEFAULT_PROFILE_POSITION_AFFIX,
                new Gson().toJson(this));
    }

    public static synchronized AdjustProfiles getProfiles(Context context) {
        boolean successful = true;
        String targetLink = context.getFilesDir() + DEFAULT_PROFILE_POSITION_AFFIX;
        LogBuff.I("Loading adjust profiles from \"" + targetLink + "\"");

        File target = new File(targetLink);
        target.getParentFile().mkdirs();
        if (!target.exists()) {
            LogBuff.I("Target profile does not exist, creating new file");
            try {
                target.createNewFile();
            } catch (IOException e) {
                successful = false;
                e.printStackTrace();
            }
        }

        AdjustProfiles result = new Gson().fromJson(
                IOUtils.Okioread(targetLink), AdjustProfiles.class);
        if(result == null){
            result = new AdjustProfiles();
            result.setProcessProfile(new ArrayList<>());
        }
        result.setTargetLink(targetLink);
        result.setContext(context);
        LogBuff.I("Profile read completed");
        if (!successful) {
            LogBuff.E("File operation failure");
            Toast.makeText(context, context.getResources().getText(R.string.read_profile_err), Toast.LENGTH_SHORT).show();
        }
        return result;
    }
}
