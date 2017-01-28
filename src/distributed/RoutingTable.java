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
    private List<NodeResource> nodeList = new ArrayList<NodeResource>();

    private static RoutingTable table;

    private RoutingTable() {
    }

    public void addNeighBour(NodeResource node) {
        this.nodeList.add(node);
    }

    public boolean removeNeighbour(NodeResource node) {
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).equals(node)) {
                nodeList.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<NodeResource> getNodeList() {
        return nodeList;
    }

    public static RoutingTable getInstance() {
        if (RoutingTable.table == null) {
            RoutingTable.table = new RoutingTable();
        }
        return RoutingTable.table;
    }
}
