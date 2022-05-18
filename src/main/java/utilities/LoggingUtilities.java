package utilities;

import com.sun.javacard.apduio.Apdu;

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

    public static void printApduMessage(String message, Apdu apdu) {
        String prefix = "[APDU] ";
        System.out.println(colorString(prefix + message, TextColor.PURPLE));
        System.out.println(colorString(prefix + apdu + '\n', TextColor.PURPLE));
    }

    public static void printApduError(byte[] sw1sw2) {
        String prefix = "[APDU ERROR] ";
        System.out.println(colorString(prefix + "SW1:" + sw1sw2[0] + "  SW2:" + sw1sw2[1], TextColor.RED));
    }

    public static void printSuccess(String string) {
        System.out.println(colorString(string, TextColor.GREEN));
    }
}
