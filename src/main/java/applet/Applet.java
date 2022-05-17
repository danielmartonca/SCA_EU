package applet;

import model.Examination;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.sun.javacard.apduio.*;

public class Applet {
    static CadClientInterface cad;

    public static void openApplet() throws IOException {
        Socket sock;
        sock = new Socket("localhost", 9025);
        InputStream is = sock.getInputStream();
        OutputStream os = sock.getOutputStream();
        cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T0, is, os);
    }

    //TODO
    public static Integer sendPin(String pinString) {
        Apdu apdu = new Apdu();
        apdu.setDataIn(pinString.getBytes(), 4);
        return Math.abs(new Random().nextInt());
    }

    //TODO
    public static void sendGrade(Double grade, int courseId, Date date) {
    }

    //TODO
    public static void sendErrorCodeForTaxNotPaid(Double grade, int courseId, Date date) {

    }

    //TODO
    public static List<Examination> receiveCardGrades() {
        List<Examination> cardExaminations = new LinkedList<>();
        return cardExaminations;
    }

    public static void sendGradesToCard(List<Examination> newGrades) {

    }
}
