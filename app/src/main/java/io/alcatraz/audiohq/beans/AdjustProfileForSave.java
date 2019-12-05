package io.alcatraz.audiohq.beans;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

import io.alcatraz.audiohq.utils.IOUtils;

public class AdjustProfileForSave {
    private String targetLink;
    private List<ProcessProfile> processProfile = new LinkedList<>();

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

    public void saveToLocal(String dir) {
        IOUtils.Okiowrite(dir, buildSelf());
    }

    private String buildSelf(){
        StringBuilder sb = new StringBuilder();
        List<ProcessProfile> peek = processProfile;
        for(int i = 0; i< peek.size(); i++){
            sb.append(peek.get(i).toSavingForm().buildSelf());
            if(i != peek.size()-1)
                sb.append("\n");
        }
        return sb.toString();
    }

//    private void appendSingleLine(StringBuilder builder,String title,String content,boolean last){
//        builder.append("\"");
//        builder.append(title);
//        builder.append("\":");
//        builder.append("\"");
//        builder.append(content);
//        builder.append("\"");
//        if(!last){
//            builder.append(",");
//        }
//    }
}
