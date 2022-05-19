package actions;

import applet.Applet;
import database.Queries;
import model.Course;
import model.Examination;
import model.Student;
import utilities.UserType;
import utilities.LoggingUtilities;
import utilities.TextColor;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TeacherActions {
    static Scanner scanner = new Scanner(System.in);

    private static List<Course> availableCourses;
    private static Course course;
    private static Student student;
    private static List<Examination> studentGradesAtCourse;
    private static Integer newGrade;

    private static void printAvailableCourses() {
        availableCourses = Queries.getAllCourses();
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Available courses: ");
        for (var courseName : availableCourses)
            System.out.print(LoggingUtilities.colorString(courseName.getName(), TextColor.BLUE) + ' ');
        System.out.println('\n');
    }

    private static void insertGradeInDatabase(Integer gradeValue) {
        Examination grade = new Examination(student, course, gradeValue);
        Queries.insertGrade(grade);
        studentGradesAtCourse.add(grade);
        if (gradeValue == 11) {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Setting hasPaidTax to '" + false + "' for course '" + course.getName() + "' in database.\n");
            Queries.setHasPaidTax(false, course);
        }
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Inserted new grade '" + gradeValue + "' for course '" + course.getName() + "' in database.\n");
    }

    public static void chooseCourse() {
        printAvailableCourses();

        boolean notOk = true;
        String input;
        do {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Choose one course: ");
            input = scanner.nextLine().trim();
            for (var course : availableCourses)
                if (course.getName().equals(input)) {
                    notOk = false;
                    break;
                }
            if (notOk)
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Please choose a correct course from the list.\n");
        } while (notOk);

        course = Queries.getCourseByName(input);
        if (course == null) {
            LoggingUtilities.printError("COULDN'T FIND COURSE BY NAME IN DB.");
            System.exit(-1);
        }
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Course chosen is " + course + "\n");
    }

    public static void inputGrade() {
        String input;
        boolean isOk;
        do {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Insert student grade: ");
            input = scanner.nextLine().trim();
            try {
                newGrade = Integer.parseInt(input);
                if (newGrade <= 0 || newGrade > 10)
                    throw new NumberFormatException();
                isOk = true;
            } catch (NumberFormatException e) {
                isOk = false;
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Please insert a correct grade.\n");
            }
        } while (!isOk);

        LoggingUtilities.printUserMessage(UserType.TEACHER, "Sending to applet grade '" + newGrade + "' for course " + course + '\n');
        int studentId = StudentActions.studentId;
        Applet.sendGrade(newGrade, course.getId(), new Date());

        student = Queries.findStudentById(studentId);
        if (student == null) {
            LoggingUtilities.printError("Student wasn't found in the database.");
            System.exit(-1);
        }
//        LoggingUtilities.printUserMessage(UserType.TEACHER, "Applet returned studentId: '" + studentId + "'\n");
    }

    public static void insertStudentGradesAtCourse() {
        //get grades
        studentGradesAtCourse = Queries.getStudentGradesAtCourse(student, course);
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Got students grades:\n" + studentGradesAtCourse + '\n');
        //if student has less than two grades
        if (studentGradesAtCourse.size() < 2) {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has less than two grades.\n");
            insertGradeInDatabase(newGrade);
            return;
        }

        //if user has two grades < 5
        boolean hasPassed = true;
        int counter = 0;
        for (var grade : studentGradesAtCourse) {
            if (grade.getGrade() < 5)
                counter++;
            if (counter >= 2) {
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has more than two grades less than 5.\n");
                hasPassed = false;
                break;
            }
        }

        if (hasPassed) {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has less than two grades less than 5.\n");
            insertGradeInDatabase(newGrade);
            return;
        }

        //if the student has paid the tax, stop
        for (var grade : studentGradesAtCourse)
            if (grade.isHasPaidTax()) {
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has paid the tax.");
                insertGradeInDatabase(newGrade);
                Queries.setHasPaidTax(true, course);
                return;
            }


        //if the student has NOT paid the tax
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has not payed the tax. Inserting error code 11 as grade in database and sending it to applet.\n");
        Applet.sendGrade(11, course.getId(), new Date());
        insertGradeInDatabase(11);
    }
}
