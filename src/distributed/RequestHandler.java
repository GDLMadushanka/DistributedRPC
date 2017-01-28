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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yellowflash
 */
public abstract class RequestHandler {

    public ControlPanel mainWindow;
    public static DatagramSocket socket;
    public static String clientIP;

    public RequestHandler(ControlPanel mainWindow) {
        this.mainWindow = mainWindow;
        try {
            RequestHandler.socket = new DatagramSocket();
            this.mainWindow.getTxtClientPort().setText("" + RequestHandler.socket.getLocalPort());
        } catch (SocketException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * this method will send messages to the nodes in the network
     *
     * @param message
     * @param Dest_IP
     * @param Dest_port
     * @throws Exception
     */
    public void SendMessage(String message, String Dest_IP, int Dest_port) throws Exception {
        mainWindow.displayMessage('\n' + "OUT - " + message);
        byte[] buf = message.getBytes();
        try {
            InetAddress address = InetAddress.getByName(Dest_IP);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Dest_port);
            RequestHandler.socket.send(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * this method will be used for receive the data from the socket
     * @return 
     */
    public DatagramPacket receiveMessage() {
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        try {
            RequestHandler.socket.receive(incoming);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return incoming;
    }

    /**
     * this method will update the routing table indicated in the GUI
     * @param table
     * @param mainWindow 
     */
    public void updateRoutingTable(RoutingTable table, ControlPanel mainWindow) {
        Map<String, String> neighbourTable = table.getNeighbouringTable();
        Iterator<String> keySet = neighbourTable.keySet().iterator();
        int initialRowCount = mainWindow.getNeighbourTable().getRowCount();
        for (int i = 0; i < initialRowCount; i++) {
            mainWindow.getNeighbourTable().removeRow(0);
        }

        while (keySet.hasNext()) {
            String key = keySet.next();
            if (!neighbourTable.get(key).equals(DistributedConstants.disconnected)) {
                Object[] temp = {key, neighbourTable.get(key)};
                mainWindow.getNeighbourTable().addRow(temp);
            }
        }
    }

}
