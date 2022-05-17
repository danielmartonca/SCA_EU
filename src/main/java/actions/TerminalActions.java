package actions;

import applet.Applet;
import database.Queries;
import model.Grade;
import model.Student;

import java.util.LinkedList;
import java.util.List;

public class TerminalActions {
    static Student student;
    static List<Grade> cardGrades;

    public static void receiveGradesFromCard() {
        cardGrades = Applet.receiveCardGrades();
    }

    public static void updateCardGradesFromDatabase() {
        student = new Student(StudentActions.studentId);

        List<Grade> newGrades = new LinkedList<>();
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
