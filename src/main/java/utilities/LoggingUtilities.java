package utilities;

public class LoggingUtilities {
    public static String colorString(String string, TextColor color) {
        return color.getColorCode() + string + TextColor.RESET.getColorCode();
    }

    public static void printError(String string) {
        System.out.println(colorString(string, TextColor.RED));
    }

    public static void printUserMessage(UserType type, String string) {
        System.out.print(colorString('[' + type.name() + "] " + string, type == UserType.STUDENT ? TextColor.CYAN : TextColor.BLUE));
    }


    public static void printSuccess(String string) {
        System.out.println(colorString(string, TextColor.GREEN));
    }
}
