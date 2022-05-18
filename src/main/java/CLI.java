import actions.StudentActions;
import actions.TeacherActions;
import actions.TerminalActions;
import applet.Applet;

import java.io.IOException;

public class CLI {


    public static void afterExam() {
        StudentActions.inputPin();
        StudentActions.checkIfAuthorised();
        TeacherActions.chooseCourse();
        TeacherActions.inputGrade();
        TeacherActions.insertStudentGradesAtCourse();
    }

    public static void afterSession() {
        StudentActions.inputPin();
        StudentActions.checkIfAuthorised();
        TerminalActions.receiveGradesFromCard();
        TerminalActions.updateCardGradesFromDatabase();
    }

    public static void main(String[] args) throws IOException {
        Applet.openApplet();
        Applet.powerUp();
        Applet.createWalletApplet();
        Applet.runCapWallet();
        Applet.selectWallet();

        afterExam();
        afterSession();

        Applet.powerDown();
    }
}
