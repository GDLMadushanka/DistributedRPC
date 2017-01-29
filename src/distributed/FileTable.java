/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author yellowflash
 */
public class FileTable {

    //    private ArrayList<String> neighbouringTable;
    private final Map<String, List<NodeResource>> fileMap = new HashMap<String, List<NodeResource>>();
    private static FileTable table;
    private List<String> MyFilelist = new ArrayList<>();

    private FileTable() {
    }

    public synchronized void addFile(String fileName, NodeResource node) {
        List<NodeResource> temp;
        temp = this.fileMap.get(fileName);
        if (temp != null) {
            temp.add(node);
        } else {
            temp = new ArrayList<NodeResource>();
            temp.add(node);
            this.fileMap.put(fileName, temp);
        }
    }

    public Map<String, List<NodeResource>> searchFile(String fileName) {
        Set<String> tempSet = this.fileMap.keySet();
        Map<String, List<NodeResource>> tempMap = new HashMap<>();
        String tempName;
        Iterator<String> itr = tempSet.iterator();
        List<NodeResource> tempList = new ArrayList<NodeResource>();
        while (itr.hasNext()) {
            tempName = itr.next();
            if (this.checkKeyword(fileName, tempName)) {
                tempMap.put(tempName, tempList);
            }
        }
        return tempMap;
    }

    public Map<String, List<NodeResource>> getFileMap() {
        return fileMap;
    }

    public static FileTable getInstance() {
        if (FileTable.table == null) {
            FileTable.table = new FileTable();
        }
        return FileTable.table;
    }

    private boolean checkKeyword(String keyword, String fileName) {
        boolean isKeyword = true;
        keyword = keyword.trim().toLowerCase();
        fileName = fileName.trim().toLowerCase();
        List splitFileName = Arrays.asList(fileName.split("\\s+"));
        String[] keywords = keyword.split("\\s+");
        for (int i = 0; i < keywords.length; i++) {
            if (!splitFileName.contains(keywords[i])) {
                isKeyword = false;
                break;
            }
        }
        return isKeyword;
    }

    public void initMyList(String file) {
        this.MyFilelist.add(file);
    }

    public List<String> getMyFilelist() {
        return MyFilelist;
    }

    public boolean checkContainFile(String file) {
        return getMyFilelist().contains(file);
    }

    public List<String> searchMyFileList(String fileName) {
        List<String> tempList = new ArrayList<>();
        for (int i = 0; i < this.MyFilelist.size(); i++) {
            if (this.checkKeyword(fileName, this.MyFilelist.get(i))) {
                tempList.add(this.MyFilelist.get(i));
            }
        }
        return tempList;
    }
}
