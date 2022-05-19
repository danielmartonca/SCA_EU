package actions;

import applet.Applet;
import database.Queries;
import model.Examination;
import model.Student;

import java.util.LinkedList;
import java.util.List;

public class TerminalActions {
    static Student student;
    static List<Examination> cardGrades;

    public static void receiveGradesFromCard() {
        student = new Student(StudentActions.studentId);
        cardGrades = Applet.receiveCardGrades(student.getId());
    }

    public static void updateCardGradesFromDatabase() {

        List<Examination> newGrades = new LinkedList<>();
        boolean hasUpdated = false;
        var courses = Queries.getAllCourses();
        for (var course : courses) {
            var gradesList = Queries.getStudentGradesAtCourse(student, course);
            for (var grade : gradesList) {
                if (!cardGrades.contains(grade)) {
                    newGrades.add(grade);
                    hasUpdated = true;
                }
            }
        }

        if (hasUpdated)
            Applet.sendGradesToCard(newGrades);
    }
}
