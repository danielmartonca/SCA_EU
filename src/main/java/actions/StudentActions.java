package actions;

import applet.Applet;
import utilities.LoggingUtilities;
import utilities.UserType;

import java.util.Scanner;
import java.util.regex.Pattern;

public class StudentActions {
    static Scanner scanner = new Scanner(System.in);
    static String input;

    static Integer studentId;

    static final Pattern pinPattern = Pattern.compile("[0-9][0-9][0-9][0-9]");

    private static boolean isPinInputValid(String input) {
        input = input.trim().replaceAll("[\\s*]{2,}", " ");
        var words = input.split(" ");

        if (words.length != 1 || !pinPattern.matcher(words[0]).matches()) {
            LoggingUtilities.printError("[Authorization] Invalid input.");
            return false;
        }

        return true;
    }

    public static void checkIfAuthorised() {
        try {
            studentId = Applet.sendPin(input);
            if (studentId == null) System.exit(-1);
        } catch (Exception e) {
            LoggingUtilities.printError("[Authorization] Exception while sending pin to applet: '" + e.getMessage() + "'");
            System.exit(-1);
        }
    }

    public static void inputPin() {
        boolean isNotValid = true;
        while (isNotValid) {
            LoggingUtilities.printUserMessage(UserType.STUDENT, "Pin: ");
            input = scanner.nextLine().trim();
            if (isPinInputValid(input))
                isNotValid = false;
        }
    }
}
