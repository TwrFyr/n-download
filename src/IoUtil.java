import java.io.File;

/**
 * This class contains methods for more IO usability for files.
 */
public class IoUtil {

    // array of illegal characters in directory names
    private static final char[] FORBIDDEN_CHARACTERS = {'/', '<', '>', ':', '"', '\\', '|', '?', '*'};

    // max allowed directory name length
    private static final int MAX_DIR_NAME_LENGHT = 200;

    /**
     * Deletes the folder as well as all its contents.
     * @param folder the File with the abstract path of the folder which is to be deleted
     */
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Removes all characters which are not allowed in directory names from the input string, cuts the string length
     * down to a certain size and makes sure the string does not end on '.' or ' '.
     * @param input the string from which the characters are to be removed
     * @return the input string without the invalid characters
     */
    public static String dirNameFromString(String input) {
        int length = input.length() >= MAX_DIR_NAME_LENGHT ? MAX_DIR_NAME_LENGHT : input.length();

        // removes invalid characters
        boolean valid;
        StringBuilder validStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char s = input.charAt(i);
            valid = true;
            for (char c : FORBIDDEN_CHARACTERS) {
                if (s == c) {
                    valid = false;
                    break;
                }
            }
            if (valid)
                validStringBuilder.append(s);
        }

        // makes sure the name does not end on ' ' or '.'
        char lastChar;
        while (true) {
            lastChar = validStringBuilder.charAt(validStringBuilder.length() - 1);
            if (lastChar == ' ' || lastChar == '.') {
                validStringBuilder.deleteCharAt(validStringBuilder.length() - 1);
                continue;
            }
            break;
        }

        return validStringBuilder.toString();
    }
}


