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

public class AdjustProfiles extends AdjustProfileForSave{
    public static String DEFAULT_PROFILE_POSITION_AFFIX = "/java_adjust_profiles/adjust_profiles.json";
    private Context mContext;

//    private String targetLink;
//    private List<ProcessProfile> processProfile;

//    public void setProcessProfile(List<ProcessProfile> processProfile) {
//        this.processProfile = processProfile;
//    }
//
//    public List<ProcessProfile> getProcessProfile() {
//        return processProfile;
//    }
//
//    public String getTargetLink() {
//        return targetLink;
//    }
//
//    public void setTargetLink(String targetLink) {
//        this.targetLink = targetLink;
//    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
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

        AdjustProfiles result = new AdjustProfiles();

        String raw = IOUtils.Okioread(targetLink);

        if(raw.length()>=3) {
            String[] process_1 = raw.trim().split("\n");
            for (String i : process_1) {
                String process_2[] = i.split(";");
                ProcessProfile profile = new ProcessProfile(process_2[0]);
                profile.setLeft(Float.parseFloat(process_2[1]));
                profile.setRight(Float.parseFloat(process_2[2]));
                profile.setGeneral(Float.parseFloat(process_2[3]));
                profile.setControl_lr(Boolean.parseBoolean(process_2[4]));
                result.getProcessProfile().add(profile);
            }
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
