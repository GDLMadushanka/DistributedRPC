/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package distributed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author yellowflash
 */
public class RoutingTable {

//    private ArrayList<String> neighbouringTable;
    private final Map<String, String> neighbouringTable = new HashMap<>();
    private final Map<String, List<String>> fileMap = new HashMap<>();
    private static RoutingTable table;

    private RoutingTable() {
    }

    public synchronized void addNeighBour(String ip, int port) {
        this.neighbouringTable.put(ip +":"+ port, DistributedConstants.notConnected);
    }

    public synchronized void removeNeighbour(String ip, int port) {
        this.neighbouringTable.remove(ip +":"+ port);
    }

    public synchronized void updateNeighbourState(String ip, int port, String status) {
        this.neighbouringTable.put(ip +":"+ port, status);
    }

    public synchronized void addFile(String fileName) {
        String[] temp = fileName.split("_");
        List<String> fileList;
        ArrayList<String> content;
        for (int i = 0; i < temp.length; i++) {
//            System.out.println(temp[i]);
            fileList = this.fileMap.get(temp[i]);
            if (fileList == null) {
                content = new ArrayList<>();
                content.add(fileName);
                this.fileMap.put(temp[i], content);
            } else {
                fileList.add(fileName);
            }

        }
    }

    public synchronized Set<String> searchFile(String fileName) {
        Set<String> tempSet = new HashSet<>();
        String[] tempKeywords = fileName.split(" ");
        List<String> fileList;
        Iterator<String> fileIterator;
        for (int i = 0; i < tempKeywords.length; i++) {
            fileList = this.fileMap.get(tempKeywords[i]);
            for (String string : fileList) {
                tempSet.add(string);
            }
/*            fileIterator = fileList.iterator();
            while (fileIterator.hasNext()) {
                tempSet.add(fileIterator.next());
            }*/
        }
        return tempSet;
    }

    public Map<String, String> getNeighbouringTable() {
        return neighbouringTable;
    }

    public Map<String, List<String>> getFileMap() {
        return fileMap;
    }

    public static RoutingTable getInstance() {
        if (RoutingTable.table == null) {
            RoutingTable.table = new RoutingTable();
        }
        return RoutingTable.table;
    }
}
