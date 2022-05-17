import actions.StudentActions;
import actions.TeacherActions;
import actions.TerminalActions;

import java.io.IOException;

public class CLI {

    static String crefFilePath = "F:\\Info\\FACULTATE\\ANUL_3\\SEM_2\\SCA\\SETUP\\Java Card Development Kit Simulator\\bin\\cref.bat";
    static Process process;

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
        try {
            process = Runtime.getRuntime().exec(crefFilePath);
            afterExam();
            afterSession();
        } finally {
            process.destroy();
        }
    }
}
