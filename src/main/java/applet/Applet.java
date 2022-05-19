package applet;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.jpcsclite.APDU;
import database.Queries;
import model.Course;
import model.Examination;
import model.Student;
import utilities.HexUtilities;
import utilities.LoggingUtilities;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Applet {
    static CadClientInterface cad;
    static String crefFilePath = "F:\\Info\\FACULTATE\\ANUL_3\\SEM_2\\SCA\\SETUP\\Java Card Development Kit Simulator\\bin\\cref.bat";
    static Process process;

    private static ApduResponse sendApdu(byte[] bytes) {
        try {
            Apdu apdu = createApduCommand(bytes);
            cad.exchangeApdu(apdu);

            byte[] sw1sw2 = apdu.getSw1Sw2();
            if (sw1sw2[0] != HexUtilities.getByteFromHexCode("0x90") || sw1sw2[1] != HexUtilities.getByteFromHexCode("0x00")) {
                LoggingUtilities.printApduError(sw1sw2);
            }

            return new ApduResponse(apdu.getDataOut().clone(), sw1sw2[0], sw1sw2[2]);
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
        LoggingUtilities.printApduMessage("CREATE WALLET APPLET", command);
        sendApdu(HexUtilities.getByteArrayFromList(bytesList));
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
                sendApdu(HexUtilities.getByteArrayFromList(byteList));
            }

        } catch (IOException e) {
            LoggingUtilities.printError(e.getMessage());
            System.exit(-1);
        }
    }

    public static void selectWallet() {
        String command = "0x00 0xA4 0x04 0x00 0x0a 0xa0 0x00 0x00 0x00 0x62 0x03 0x01 0x0c 0x06 0x01 0x7F";
        List<Byte> byteList = HexUtilities.convertStringToByteList(command);
        LoggingUtilities.printApduMessage("SELECT WALLET", command);
        sendApdu(HexUtilities.getByteArrayFromList(byteList));
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
        LoggingUtilities.printApduMessage("PIN", sb.toString());
        var apduResponse = sendApdu(bytes);

        // APDU|CLA: 80, INS: 20, P1: 00, P2: 00, Lc: 05, 01, 02, 03, 04, 05, Le: 02, 00, 0a, SW1: 90, SW2: 00
        if (apduResponse.getSw1() != HexUtilities.getByteFromHexCode("0x90") || apduResponse.getSw2() != HexUtilities.getByteFromHexCode("0x00")) {
            LoggingUtilities.printApduError(apduResponse.getSw1(), apduResponse.getSw2());
            LoggingUtilities.printError("[AUTHORIZATION] PIN INCORRECT");
            return null;
        }
        var data = apduResponse.getBytes();
        if ((int) data[0] != 1) {
            LoggingUtilities.printError("[AUTHORIZATION] INVALID RESPONSE LENGTH BYTE");
        }

        return (int) data[1];
    }

    private static Examination getGrade(int studentId, int courseId) {
        //0x80 0x30 0x00 0x00 0x01 0x01 0x7F;
        String sb = "0x80 0x30 0x00 0x00 0x01 " + "0x0" + Integer.toHexString(courseId) + " 0x7F";

        byte[] bytes = HexUtilities.getByteArrayFromList(HexUtilities.convertStringToByteList(sb));
        LoggingUtilities.printApduMessage("GET GRADE", sb);

        var apduResponse = sendApdu(bytes);

        // APDU|CLA: 80, INS: 30, P1: 00, P2: 00, Lc: 01, 01, Le: 04, 00, 0e, 07, 16, SW1: 90, SW2: 00
        if (apduResponse.getSw1() != HexUtilities.getByteFromHexCode("0x90") || apduResponse.getSw2() != HexUtilities.getByteFromHexCode("0x00")) {
            LoggingUtilities.printApduError(apduResponse.getSw1(), apduResponse.getSw2());
            LoggingUtilities.printError("[GET_GRADE] INVALID APDU");
            return null;
        }
        var data = apduResponse.getBytes();
        if ((int) data[0] != 5) {
            LoggingUtilities.printError("[GET_GRADE] INVALID RESPONSE LENGTH BYTE");
            return null;
        }
        try {
            int grade = data[1];
            int day = data[2];
            int month = data[3];
            int year = data[4];
            String dayAsString = day < 10 ? "0" + day : String.valueOf(day);
            String monthAsString = month < 10 ? "0" + month : String.valueOf(month);
            String yearAsString = year < 10 ? "0" + year : String.valueOf(year);

            SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
            String dateString = yearAsString + '-' + monthAsString + '-' + dayAsString;
            Date date = formatter.parse(dateString);
            Course course = Queries.getCourseById(courseId);
            return new Examination(new Student(studentId), course, date, grade);
        } catch (ParseException e) {
            LoggingUtilities.printError(e.getMessage());
            return null;
        }
    }

    public static void sendGrade(Integer grade, int courseId, Date date) {
        //0x80 0x40 0x00 0x00 0x05 0x01 0x09 0x01 0x01 0x01 0x7F;
        StringBuilder sb = new StringBuilder("0x80 0x40 0x00 0x00 0x05");
        sb.append(" ").append("0x0").append(Integer.toHexString(courseId));
        sb.append(" ").append("0x0").append(Integer.toHexString(grade));
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
        String dateAsString = dateFormat.format(date);
        var words = dateAsString.split("-");
        var listOfWords = Arrays.stream(words).map(Integer::parseInt).map(Integer::toHexString).toList();
        List<String> updatedWords = new LinkedList<>();
        listOfWords.forEach(word -> {
            var hex = Integer.toHexString(Integer.parseInt(word));
            if (hex.length() == 1) {
                hex = "0" + hex;
            }

            updatedWords.add(hex);
        });
        sb.append(" ").append("0x").append(updatedWords.get(2)); //day
        sb.append(" ").append("0x").append(updatedWords.get(1)); //month
        sb.append(" ").append("0x").append(updatedWords.get(0)); //year
        sb.append(" 0x7F");

        LoggingUtilities.printApduMessage("SEND GRADE", sb.toString());

        byte[] bytes = HexUtilities.getByteArrayFromList(HexUtilities.convertStringToByteList(sb.toString()));
        var apduResponse = sendApdu(bytes);

        // APDU|CLA: 80, INS: 30, P1: 00, P2: 00, Lc: 01, 01, Le: 04, 00, 0e, 07, 16, SW1: 90, SW2: 00
        if (apduResponse.getSw1() != HexUtilities.getByteFromHexCode("0x90") || apduResponse.getSw2() != HexUtilities.getByteFromHexCode("0x00")) {
            LoggingUtilities.printApduError(apduResponse.getSw1(), apduResponse.getSw2());
            LoggingUtilities.printError("[GET_GRADE] INVALID APDU");
        }
        var data = apduResponse.getBytes();
        if ((int) data[0] != 1) {
            LoggingUtilities.printError("[GET_GRADE] INVALID RESPONSE LENGTH BYTE");
        }
        int gradeReturned = data[1];
        if (gradeReturned != grade)
            LoggingUtilities.printError("[GET_GRADE] INVALID RESPONSE GRADE - grade as parameter should match with returned grade '" + gradeReturned + "'");
    }

    public static List<Examination> receiveCardGrades(int student_id) {
        List<Examination> cardExaminations = new LinkedList<>();
        var courses = Queries.getAllCourses();
        for (var course : courses) {
            int courseId = course.getId();
            var examination = getGrade(student_id, courseId);
            if (examination != null) cardExaminations.add(examination);
        }
        return cardExaminations;
    }

    public static void sendGradesToCard(List<Examination> newGrades) {
        for (var examination : newGrades)
            sendGrade(examination.getGrade(), examination.getCourse().getId(), examination.getDate());

    }
}
