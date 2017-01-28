/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lahiru
 */
public class MessageDecoder extends RequestHandler {

    private String receivedIp;
    private int receivedPort;
    private final RoutingTable table;
    private final CommunicationProtocol protocol;
    PerformaceParameters pp = PerformaceParameters.getInstance();
    long StartTime = 0l;
    long EndTime = 0l;
    FileTable fileTable;

    public MessageDecoder(ControlPanel mainWindow) {
        super(mainWindow);
        table = RoutingTable.getInstance();
        protocol = CommunicationProtocol.getInstance();
        fileTable = FileTable.getInstance();
    }

    /**
     * message decode gate way. appropriate method will be invoked according to
     * the response
     *
     * @param msg
     * @param receivedIp
     * @param receivedPort
     * @throws Exception
     */
    public void DecodeMessage(String msg, String receivedIp, int receivedPort) throws Exception {
        if (receivedIp != null && receivedPort >= 0) {
            this.receivedIp = receivedIp;
            this.receivedPort = receivedPort;
            pp.incfMsgCount();
        }

        if (msg.contains("REGOK")) {
            registerResponse(msg);
        } else if (msg.contains("UNROK")) {
            unregisterResponse(msg);
        } else if (msg.contains("JOINOK")) {
            joinResponse(msg);
        } else if (msg.contains("LEAVEOK")) {
            leaveResponse(msg);
        } else if (msg.contains("SEROK")) {
            searchResponse(msg);
        } else if (msg.contains("JOIN")) {
            handleJoinRequest(msg);
        } else if (msg.contains("LEAVE")) {
            handleLeaveRequest(msg);
        } else if (msg.contains("SER")) {
            handleSearchRequest(msg);
        }
    }

    /**
     * method will handle the register response
     *
     * @param message
     */
    private void registerResponse(String message) {
        String buffer[] = message.split(" ");
        int neighboursCount = Integer.parseInt(buffer[2]);

        if (neighboursCount > 0 && neighboursCount <= DistributedConstants.numberOfneighbours) {
            for (int i = 0; i < neighboursCount; i++) {
                table.addNeighBour(new NodeResource(buffer[3 + 2 * i], Integer.parseInt(buffer[4 + 2 * i])));
                updateRoutingTable(table, mainWindow);
            }
        }
    }

    /**
     * method will handle the response of the unregister request
     *
     * @param message
     * @throws Exception
     */
    private void unregisterResponse(String message) throws Exception {
        String buffer[] = message.split(" ");
        System.out.println(message);
        if (buffer[2].equals("9999")) {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2].equals("0")) {
            System.out.println("Unregistered");
            List<NodeResource> list = table.getNodeList();
//            Iterator<String> iterator = table.getNeighbouringTable().keySet().iterator();
            for (int i = 0; i < 10; i++) {
                String leaveRequestMessage = protocol.leave(RequestHandler.clientIP, RequestHandler.socket.getLocalPort());
                SendMessage(leaveRequestMessage, list.get(i).getIp(), list.get(i).getPort());
            }
        }
    }

    /**
     * method will handle the response for the join request
     *
     * @param message
     */
    private void joinResponse(String message) {
//        String receivingEndState = table.getNeighbouringTable().get(this.receivedIp + ":" + this.receivedPort);
        String buffer[] = message.split(" ");
        if (buffer[2].equals("9999")) {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2].equals("0")) {
            table.addNeighBour(new NodeResource(this.receivedIp, this.receivedPort));
            this.updateRoutingTable(table, mainWindow);
        }
    }

    /**
     * leave response will be handled
     *
     * @param message
     */
    private void leaveResponse(String message) {
        String buffer[] = message.split(" ");
        if (buffer[2].equals("9999")) {
            /*
                solution has to be a group decision
             */
        } else if (buffer[2].equals("0")) {
            System.out.println("Succesfully Left");
        }
    }

    /**
     * search response will be handled
     *
     * @param message
     */
    private void searchResponse(String message) {
        long timout = mainWindow.stopTimer();
        String buffer[] = message.split(" ");
        int fileCount = Integer.parseInt(buffer[2]);
        if (fileCount > 0) {
            String fileHostedNodeIP = buffer[3];
            int fileHostedNodePort = Integer.parseInt(buffer[4]);
            int requiredHops = Integer.parseInt(buffer[5]);
            pp.addSearchValue(requiredHops, timout);
            //e
            for (int i = 0; i < fileCount; i++) {
                mainWindow.getDisplaySearchResult().append(buffer[6 + i] + " ==> " + fileHostedNodeIP + ":" + fileHostedNodePort + "\n");
            }
        }

    }

    /**
     * join request will be handled. according to the new join requests the
     * routing table is updated
     *
     * @param message
     * @throws Exception
     */
    private void handleJoinRequest(String message) throws Exception {

        String buffer[] = message.split(" ");
        String ipOfRequestedNode = buffer[2];
        String responseMessage;
        int portOfRequestedNode = Integer.parseInt(buffer[3]);

        if (ipOfRequestedNode.equals(this.receivedIp) && this.receivedPort == portOfRequestedNode) {
            this.table.addNeighBour(new NodeResource(ipOfRequestedNode, portOfRequestedNode));
            updateRoutingTable(table, mainWindow);
            responseMessage = protocol.joinResponse(0);
        } else {
            responseMessage = protocol.joinResponse(9999);
        }
        SendMessage(responseMessage, ipOfRequestedNode, portOfRequestedNode);
    }

    /**
     * leave request will be handled. this method will not be called manually.
     * this method is invoked by the unregister response
     *
     * @param message
     * @throws Exception
     */
    private void handleLeaveRequest(String message) throws Exception {
        String buffer[] = message.split(" ");
        String ipOfRequestedNode = buffer[2];
        String responseMessage;
        int portOfRequestedNode = Integer.parseInt(buffer[3]);

        if (ipOfRequestedNode.equals(this.receivedIp) && this.receivedPort == portOfRequestedNode) {
            this.table.removeNeighbour(new NodeResource(ipOfRequestedNode, portOfRequestedNode));
            updateRoutingTable(table, mainWindow);
            responseMessage = protocol.leaveResponse(0);
        } else {
            responseMessage = protocol.leaveResponse(9999);
        }

        SendMessage(responseMessage, ipOfRequestedNode, portOfRequestedNode);
    }

    /**
     * this method will consist the file searching algorithm. currently the
     * maximum number of hops is two and when they reach the hop limit request
     * chain is discarded automatically.
     *
     * @param message
     * @throws Exception
     */
    private void handleSearchRequest(String message) throws Exception {
        String buffer[] = message.split("\"");
        String buffer_1[] = message.split(" ");
        String ipOfRequestedNode = buffer_1[2];
        String fileName = buffer[1];
        int fileCount = 0;
        int portOfRequestedNode = Integer.parseInt(buffer_1[3]);
        int hopCount = Integer.parseInt(buffer_1[buffer_1.length - 1]);
        String fileList = "";
        List<String> list;
        Map<String, List<NodeResource>> tempMap;
        if (hopCount > 0) {
            hopCount--;
//            String[] keywords = fileName.split("_");
            tempMap = fileTable.searchFile(fileName);
            for (int i = 0; i < keywords.length; i++) {
                list = table.getFileMap().get(keywords[i]);
                for (int j = 0; j < list.size(); j++) {
                    String tempFileName = list.get(i);
                    if (!fileList.contains(tempFileName)) {
                        fileList += tempFileName + " ";
                        fileCount++;
                    }
                }
            }

            String fileRequestMsg = protocol.searchFile(ipOfRequestedNode, portOfRequestedNode, hopCount, fileName);
            Iterator<String> iterator = table.getNeighbouringTable().keySet().iterator();
            String tempKey;
            while (iterator.hasNext()) {
                tempKey = iterator.next();
                if (table.getNeighbouringTable().get(tempKey).equals(DistributedConstants.connected)
                        && !tempKey.equals(this.receivedIp + ":" + this.receivedPort)) {
                    String[] temp = tempKey.split(":");
                    SendMessage(fileRequestMsg, temp[0], Integer.parseInt(temp[1]));
                }
            }
        }

        if (fileList.length() > 0) {
            String searchResponse = protocol.searchResponse(fileCount, RequestHandler.clientIP, RequestHandler.socket.getLocalPort(), hopCount, fileList);
            SendMessage(searchResponse, ipOfRequestedNode, portOfRequestedNode);
        }
    }
}
