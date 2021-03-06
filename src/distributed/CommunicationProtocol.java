/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.text.NumberFormat;

/**
 *
 * @author yellowflash
 */
public class CommunicationProtocol {

    private static CommunicationProtocol protocol;

    private CommunicationProtocol() {
    }

    public static CommunicationProtocol getInstance() {
        if (CommunicationProtocol.protocol == null) {
            CommunicationProtocol.protocol = new CommunicationProtocol();
            return CommunicationProtocol.protocol;
        } else {
            return CommunicationProtocol.protocol;
        }
    }

    public String register(String IP, int port, String userName) throws invalidMessageLengthException {
        String message = " REG " + IP + " " + port + " " + userName;
        return validateMessage(message);
    }

    public String unRegister(String IP, int port, String userName) throws invalidMessageLengthException {
        String message = " UNREG " + IP + " " + port + " " + userName;
        return validateMessage(message);
    }

    public String join(String IP, int port) throws invalidMessageLengthException {
        String message = " JOIN " + IP + " " + port;
        return validateMessage(message);
    }

    public String leave(String IP, int port) throws invalidMessageLengthException {
        String message = " LEAVE " + IP + " " + port;
        return validateMessage(message);
    }

    public String searchFile(String IP, int port, int hopCount, String fileName) throws invalidMessageLengthException {
        String message = " SER " + IP + " " + port + " \"" + fileName + "\" ";
        if (hopCount < 1) {
            message += hopCount;
        }
        return validateMessage(message);
    }

    public String joinResponse(int status) throws invalidMessageLengthException {
        String message = " JOINOK " + status;
        return validateMessage(message);
    }

    public String leaveResponse(int status) throws invalidMessageLengthException {
        String message = " LEAVEOK " + status;
        return validateMessage(message);
    }

    public String searchResponse(int fileCount, String IP, int port, int requiredHops, String fileNameArray) throws invalidMessageLengthException {
        String message = " SEROK " + fileCount + " " + IP + " " + port + " " + requiredHops;

        if (fileNameArray != null) {
//            for (int i = 0; i < fileNameArray.length; i++) {
//                message += " " + fileNameArray[i];
//            }
            message += " " + fileNameArray;
        } else {
            fileCount = 0;
            message = " SEROK " + fileCount + " " + IP + " " + port + " " + requiredHops;
        }

        return validateMessage(message);
    }

    public String pingRequest(String IP, int port) throws invalidMessageLengthException {
        String message = " PING " + IP + " " + port;
        return validateMessage(message);
    }

    public String pingResponse(int status) throws invalidMessageLengthException {
        String message = " PINGOK " + status;
        return validateMessage(message);
    }

    /**
     * method being used for the length validation and embed message length
     *
     * @param message
     * @return
     * @throws Exception
     */
    private String validateMessage(String message) throws invalidMessageLengthException {
        int messageLength = message.length() + 4;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        if (messageLength >= 10000) {
            throw new invalidMessageLengthException("Invalid Message Length");
        }
        nf.setMinimumIntegerDigits(4);
        message = nf.format(messageLength) + message;
        return message;
    }

    class invalidMessageLengthException extends Exception {

        public invalidMessageLengthException(String message) {
            super(message);
        }

    }
}
