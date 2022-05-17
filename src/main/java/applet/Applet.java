package applet;

import model.Course;
import model.Grade;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Applet {
   public static void openApplet(   )
   {

   }

    //TODO
    public static Integer sendPin(String pinString) {

        return Math.abs(new Random().nextInt());
    }

    //TODO
    public static void sendGrade(Course course, Double grade) {
    }

    //TODO
    public static void sendErrorCodeForTaxNotPaid(Course course, Double grade) {

    }

    //TODO
    public static List<Grade> receiveCardGrades() {
        List<Grade> cardGrades = new LinkedList<>();
        return cardGrades;
    }

    public static void sendGradesToCard(List<Grade> newGrades) {

    }
}
