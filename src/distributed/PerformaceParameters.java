/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import sun.misc.PerformanceLogger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author lahiru
 */
public class PerformaceParameters {

    private int searchCount = 0;
    private int messageCount = 0;
    private ArrayList<SearchValues> valueList = new ArrayList<SearchValues>();

    private static PerformaceParameters instance = null;

    private PerformaceParameters() {
    }

    public static PerformaceParameters getInstance() {
        if (instance == null) {
            instance = new PerformaceParameters();
        }
        return instance;
    }

    public void addSearchValue(int hopCount, double delay) {
        searchCount++;
        SearchValues searchval = new SearchValues();
        searchval.hopCount = hopCount;
        searchval.searchTime = delay;
        valueList.add(searchval);
    }

    public void incfMsgCount() {
        messageCount++;
    }

    public void printAllValues() {
        File file = new File("PerformanceResults.txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write("No_of_search : "+searchCount+"\n");
            writer.write("No_of_messages : "+messageCount+"\n");
            writer.write("hop count  delay \n");
            for(SearchValues val : valueList){
                writer.write(val.hopCount+" "+ val.searchTime+"\n");
            }
            
        } catch (IOException e) {
            e.printStackTrace(); // I'd rather declare method with throws IOException and omit this catch.
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignore) {
                }
            }
        }

    }
}
