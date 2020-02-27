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
public class Pkgs {

    private String pkg;
    private List<Processes> processes = new LinkedList<>();
    public void setPkg(String pkg) {
         this.pkg = pkg;
     }
     public String getPkg() {
         return pkg;
     }

    public void setProcesses(List<Processes> processes) {
         this.processes = processes;
     }
     public List<Processes> getProcesses() {
         return processes;
     }

}