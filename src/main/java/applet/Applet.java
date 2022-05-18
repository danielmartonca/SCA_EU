package applet;

import com.sun.javacard.jpcsclite.APDU;
import database.Queries;
import model.Examination;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.sun.javacard.apduio.*;
import utilities.LoggingUtilities;
import utilities.HexUtilities;

public class Applet {
    static CadClientInterface cad;
    static String crefFilePath = "F:\\Info\\FACULTATE\\ANUL_3\\SEM_2\\SCA\\SETUP\\Java Card Development Kit Simulator\\bin\\cref.bat";
    static Process process;

    private static byte[] sendApdu(String commandName, byte[] bytes) {
        try {
            Apdu apdu = createApduCommand(bytes);
            LoggingUtilities.printApduMessage(commandName, apdu);
            cad.exchangeApdu(apdu);

            byte[] sw1sw2 = apdu.getSw1Sw2();
            if (sw1sw2[0] != HexUtilities.getByteFromHexCode("0x90") || sw1sw2[1] != HexUtilities.getByteFromHexCode("0x00")) {
                LoggingUtilities.printApduError(sw1sw2);
                return null;
            }

            return apdu.getDataOut().clone();
        } catch (Exception e) {
            LoggingUtilities.printError(e.getMessage());
            System.exit(-1);
            return null;
        }
    }

    public static void openApplet() throws IOException {
        try {
            process = Runtime.getRuntime().exec(crefFilePath);
        } finally {
            process.destroy();
        }
        Socket sock = new Socket("localhost", 9025);
        InputStream is = sock.getInputStream();
        OutputStream os = sock.getOutputStream();
        cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T0, is, os);
    }

    public static void powerUp() {
        try {
            cad.powerUp();
        } catch (Exception e) {
            LoggingUtilities.printError(e.getMessage());
            System.exit(-11);
        }
    }

    public static void powerDown() {
        try {
            cad.powerDown();
        } catch (Exception e) {
            LoggingUtilities.printError(e.getMessage());
            System.exit(-11);
        }
    }

    public static void createWalletApplet() {
        String command = "0x80 0xB8 0x00 0x00 0x14 0x0a 0xa0 0x00 0x00 0x00 0x62 0x03 0x01 0x0c 0x06 0x01 0x08 0x00 0x00 0x05 0x01 0x02 0x03 0x04 0x05 0x7F;";
        List<Byte> bytesList = HexUtilities.convertStringToByteList(command);
        sendApdu("CREATE WALLET APPLET", HexUtilities.getByteArrayFromList(bytesList));
    }

    private static Apdu createApduCommand(byte[] bytes) {
        int length = bytes.length;
        byte[] dataIn = {};

        //0x80 0x40 0x00 0x00 0x05 0x01 0x09 0x01 0x01 0x01 0x7F;
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = bytes[0];
        apdu.command[APDU.INS] = bytes[1];
        apdu.command[APDU.P1] = bytes[2];
        apdu.command[APDU.P2] = bytes[3];

        int lc = bytes[4];

        if (length > 6) {
            dataIn = new byte[length - 6];
            System.arraycopy(bytes, 5, dataIn, 0, length - 1 - 5);
        }

        apdu.setDataIn(dataIn, lc);

        apdu.Le = bytes[length - 1];
        return apdu;
    }

    public static void runCapWallet() {
        String path = "src/main/resources/cap.script";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));

            String line;
            while ((line = reader.readLine()) != null) if (line.startsWith("0")) {
                List<Byte> byteList = HexUtilities.convertStringToByteList(line);
                sendApdu("CAP", HexUtilities.getByteArrayFromList(byteList));
            }

        } catch (IOException e) {
            LoggingUtilities.printError(e.getMessage());
            System.exit(-1);
        }
    }

    public static void selectWallet() {
        String command = "0x00 0xA4 0x04 0x00 0x0a 0xa0 0x00 0x00 0x00 0x62 0x03 0x01 0x0c 0x06 0x01 0x7F";
        List<Byte> byteList = HexUtilities.convertStringToByteList(command);
        sendApdu("SELECT WALLET", HexUtilities.getByteArrayFromList(byteList));
    }


    public static Integer sendPin(String pinString) {
        //0x80 0x20 0x00 0x00 0x05 0x01 0x02 0x03 0x04 0x05 0x7F
        StringBuilder sb = new StringBuilder("0x80 0x20 0x00 0x00 0x05");

        for (int i = 0; i < pinString.length(); i++) {
            char c = pinString.charAt(i);
            int p = Integer.parseInt(String.valueOf(c));
            sb.append(" ").append(HexUtilities.toHexString(p));
        }
        sb.append(" 0X7F");

        byte[] bytes = HexUtilities.getByteArrayFromList(HexUtilities.convertStringToByteList(sb.toString()));
        byte[] returnedBytes = sendApdu("PIN", bytes);
        //APDU|CLA: 80, INS: 30, P1: 00, P2: 00, Lc: 01, 01, Le: 04, 00, 0e, 07, 16, SW1: 90, SW2: 00
        //TODO transform student id from byte[]
        return null;
    }

    private static Examination getGrade(int courseId) {
        //0x80 0x30 0x00 0x00 0x01 0x01 0x7F;
        String sb = "0x80 0x30 0x00 0x00 0x01 " + Integer.toHexString(courseId) + " 0X7F";

        byte[] bytes = HexUtilities.getByteArrayFromList(HexUtilities.convertStringToByteList(sb));
        byte[] returnedBytes = sendApdu("GET GRADE", bytes);
        //APDU|CLA: 80, INS: 30, P1: 00, P2: 00, Lc: 01, 01, Le: 04, 00, 0e, 07, 16, SW1: 90, SW2: 00
        //TODO transform response
        return new Examination();
    }

    public static void sendGrade(Integer grade, int courseId, Date date) {
        //0x80 0x40 0x00 0x00 0x05 0x01 0x09 0x01 0x01 0x01 0x7F;
        StringBuilder sb = new StringBuilder("0x80 0x40 0x00 0x00 0x05");
        sb.append(" ").append(Integer.toHexString(courseId));
        sb.append(" ").append(Integer.toHexString(grade));
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
        String dateAsString = dateFormat.format(date);
        var words = dateAsString.split("-");
        sb.append(" ").append(words[3]); //day
        sb.append(" ").append(words[2]); //month
        sb.append(" ").append(words[1]); //year
        sb.append(" 0X7F");

        byte[] bytes = HexUtilities.getByteArrayFromList(HexUtilities.convertStringToByteList(sb.toString()));
        byte[] returnedBytes = sendApdu("SEND GRADE", bytes);
        //APDU|CLA: 80, INS: 30, P1: 00, P2: 00, Lc: 01, 01, Le: 04, 00, 0e, 07, 16, SW1: 90, SW2: 00
        //TODO transform response
    }

    public static List<Examination> receiveCardGrades() {
        List<Examination> cardExaminations = new LinkedList<>();
        var courses = Queries.getAllCourses();
        for (var course : courses) {
            int courseId = course.getId();
            cardExaminations.add(getGrade(courseId));
        }
        return cardExaminations;
    }


    public static void sendGradesToCard(List<Examination> newGrades) {
        for (var examination : newGrades) {
            sendGrade(examination.getGrade(), examination.getCourse().getId(), examination.getDate());
        }
    }
}
