/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahiru
 */
public class Client extends RequestHandler {

    private String serverIp = "";
    private int serverPort;
    private String userName = "";
    private FileTable fileTable;
    public MessageDecoder msgDecoder;
    private final CommunicationProtocol protocol;
    private final RoutingTable routingTable;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public Client(ControlPanel mainWindow) {
        super(mainWindow);
        msgDecoder = new MessageDecoder(mainWindow);
        protocol = CommunicationProtocol.getInstance();
        routingTable = RoutingTable.getInstance();
        this.fileTable = FileTable.getInstance();

    }

    /**
     * this method launch a register request to the BS server
     *
     * @throws Exception
     */
    public void SendRegisterPacket() throws Exception {
        String tempMessage = protocol.register(RequestHandler.clientIP, RequestHandler.socket.getLocalPort(), this.userName);
        SendMessage(tempMessage, serverIp, serverPort);
    }

    /**
     * this method launch a join request to another node in the distributed
     * network
     *
     * @param NodeIp
     * @param nodePort
     * @throws Exception
     */
    public void SendJoinPacket(String NodeIp, int nodePort) throws Exception {
        String tempMessage = protocol.join(RequestHandler.clientIP, RequestHandler.socket.getLocalPort());
        SendMessage(tempMessage, NodeIp, nodePort);

    }

    /**
     * this method first search file it self and make a request to the other
     * neighbors
     *
     * @param fileName
     * @throws Exception
     */
    public void searchFile(String fileName) throws Exception {

        List<NodeResource> list;

        Map<String, List<NodeResource>> tempResults = SearchFileInsideClient(fileName);

        if (tempResults != null) {
            Set<String> tempFileSet = tempResults.keySet();
            Iterator itr = tempFileSet.iterator();
            while (itr.hasNext()) {
                String tempfileName = (String) itr.next();
                mainWindow.getDisplaySearchResult().append(tempfileName + " ==> " + RequestHandler.clientIP
                        + ":" + RequestHandler.socket.getLocalPort() + "\n");
            }
        } else {
            String tempMessage = protocol.searchFile(RequestHandler.clientIP, RequestHandler.socket.getLocalPort(), 0, fileName);
            list = routingTable.getNodeList();
//            Iterator<String> iterator = routingTable.getNeighbouringTable().keySet().iterator();
            String tempKey;
            for (int i = 0; i < list.size(); i++) {
                SendMessage(tempMessage, list.get(i).getIp(), list.get(i).getPort());
            }
        }

    }

    /**
     * this service search files inside my file list and return map
     *
     * @param fileName
     * @return
     */
    public Map<String, List<NodeResource>> SearchFileInsideClient(String fileName) {
        NodeResource node = new NodeResource();
        node.setIp(RequestHandler.clientIP);
        node.setPort(RequestHandler.socket.getLocalPort());
        List<NodeResource> tempNodeList = new ArrayList<>();
        tempNodeList.add(node);

        Map<String, List<NodeResource>> tempMap;
        tempMap = fileTable.searchFile(fileName);

        List<String> tempList = fileTable.searchMyFileList(fileName);
        if (tempList != null) {
            tempMap = new HashMap<>();
            for (int i = 0; i < tempList.size(); i++) {
                tempMap.put(tempList.get(i), tempNodeList);
            }
            return tempMap;
        } else if (tempList == null && tempMap != null) {
            return tempMap;
        } else {
            return null;
        }
    }

    /**
     * this method invokes a unregister request
     *
     * @throws Exception
     */
    public void sendUnregisterRequest() throws Exception {
        String tempMessage = protocol.unRegister(RequestHandler.clientIP, RequestHandler.socket.getLocalPort(), this.userName);
        SendMessage(tempMessage, serverIp, serverPort);
    }

    /**
     * this method will create a thread to listen to the port
     */
    public void RunMessageGateway() {
        Thread T = new Thread() {
            public void run() {
                try {
                    whileRunning();
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        T.start();
    }

    /**
     * listening to the port
     *
     * @throws Exception
     */
    public void whileRunning() throws Exception {
        DatagramPacket incomingPacket;
        while (true) {
            incomingPacket = receiveMessage();
            byte[] data = incomingPacket.getData();
            String s = new String(data, 0, incomingPacket.getLength());
            //echo the details of incoming data - client ip : client port - client message
            mainWindow.displayMessage('\n' + "IN - " + s);
            msgDecoder.DecodeMessage(s, incomingPacket.getAddress().getHostAddress(), incomingPacket.getPort());
        }
    }

}
