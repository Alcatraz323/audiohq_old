/**
 * Copyright 2020 bejson.com
 */
package io.alcatraz.audiohq.beans.nativebuffers;

import java.util.LinkedList;
import java.util.List;

/**
 * Auto-generated: 2020-02-23 6:48:15
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Processes {

    private String pid;
    private String process;
    private List<Buffers> buffers = new LinkedList<>();

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getProcess() {
        return process;
    }

    public void setBuffers(List<Buffers> buffers) {
        this.buffers = buffers;
    }

    public List<Buffers> getBuffers() {
        return buffers;
    }

}