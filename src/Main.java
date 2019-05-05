import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;


public class Main {
    // commands
    private static final String DISPLAY_HELP_COMMAND = "help";
    private static final String DIRECTORY_OPTION_COMMAND = "dir";
    private static final String QUIT_COMMAND = "exit";

    // file String constants
    private static final String DEFAULT_PATH = "nDownloads";
    private static final String PROPERTIES_FILENAME = "n-download.properties";
    private static final String PROPERTIES_PATH_PROPERTY_NAME = "save-directory-path";

    // displayed Strings constants
    private static final String NDOWNLOAD_ASCII =
            "        ___                  __             __\n" +
            "  ___  / _ \\___ _    _____  / /__  ___ ____/ /\n" +
            " / _ \\/ // / _ \\ |/|/ / _ \\/ / _ \\/ _ `/ _  / \n" +
            "/_//_/____/\\___/__,__/_//_/_/\\___/\\_,_/\\_,_/  \n";
    private static final String DIRECTORY_ASCII =
            "   ___  _             __               \n" +
            "  / _ \\(_)______ ____/ /____  ______ __\n" +
            " / // / / __/ -_) __/ __/ _ \\/ __/ // /\n" +
            "/____/_/_/  \\__/\\__/\\__/\\___/_/  \\_, / \n" +
            "                                /___/  \n";
    private static final String HELP_ASCII =
            "   __ __    __   \n" +
            "  / // /__ / /__ \n" +
            " / _  / -_) / _ \\\n" +
            "/_//_/\\__/_/ .__/\n" +
            "          /_/    \n";
    private static final String SEPERATOR_ASCII =
            "\n" +
                    " ____________________________________________\n" +
                    "/___/___/___/___/___/___/___/___/___/___/___/\n" +
                    "                                             \n";
    private static final String WELCOME_MESSAGE =
            "Welcome to n-download, a tool for downloading from nhentai.net!\n" +
                    "Use '" + DISPLAY_HELP_COMMAND + "' for addition information.\n" +
                    "Use '" + DIRECTORY_OPTION_COMMAND + "' to show/edit the save-directory.\n" +
                    "Use '" + QUIT_COMMAND + "' to quit.";

    public static void main(String[] args) {
        // print welcome / information
        System.out.println(NDOWNLOAD_ASCII + "\n" + WELCOME_MESSAGE);

        File saveDirectory;

        // check for properties
        File properties = new File(PROPERTIES_FILENAME);
        if (!properties.exists()) {
            // create standard save directory
            Properties p = new Properties();
            p.setProperty(PROPERTIES_PATH_PROPERTY_NAME, DEFAULT_PATH);
            try {
                p.store(new FileWriter(PROPERTIES_FILENAME), "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveDirectory = new File(DEFAULT_PATH);
        } else {
            // load existing save directory
            Properties p = new Properties();
            try {
                p.load(new FileReader(PROPERTIES_FILENAME));
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveDirectory = new File(p.getProperty(PROPERTIES_PATH_PROPERTY_NAME));
        }
        if (!saveDirectory.exists()) {
            if (!saveDirectory.mkdirs()) {
                throw new RuntimeException("Directory could not be created!");
            }
        }

        // user main input loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(SEPERATOR_ASCII);
            System.out.println("Please enter a number...");
            String numberString = scanner.nextLine();

            // command-section

            // exits program
            if (numberString.equals(QUIT_COMMAND)) {
                break;
            }
            // opens help section
            else if (numberString.equals(DISPLAY_HELP_COMMAND)) {
                printHelpInformation();
                continue;
            }
            // opens directory options
            else if (numberString.equals(DIRECTORY_OPTION_COMMAND)) {
                System.out.println("\n" + DIRECTORY_ASCII +
                        "Current directory: \n" + saveDirectory.getAbsolutePath() + "\n");

                // directory change input loop
                boolean running = true;
                String userInput;
                while (running) {
                    System.out.println("Would you like to change the save-directory? (y/n/reset)");
                    userInput = scanner.nextLine();
                    if (userInput.equals("n")) {
                        running = false;
                    }
                    // prompts the user to enter a new saving directory path; saves that path for future use
                    else if (userInput.equals("y")) {
                        System.out.println("\nPlease enter the absolute path of the new directory...");
                        File tempDirectory =  new File(scanner.nextLine());
                        if (!tempDirectory.exists() || !tempDirectory.isDirectory()) {
                            System.out.println("No such directory!");
                            continue;
                        }
                        Properties p = new Properties();
                        p.setProperty(PROPERTIES_PATH_PROPERTY_NAME, tempDirectory.getAbsolutePath());
                        try {
                            p.store(new FileWriter(PROPERTIES_FILENAME), "");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        saveDirectory = tempDirectory;
                        System.out.println("Change complete!\n" +
                                "new path: " + saveDirectory.getAbsolutePath());
                        running = false;
                    }
                    // resets the current save directory; saves the path for future use
                    else if (userInput.equals("reset")) {
                        System.out.print("\nResetting...\r");
                        Properties p = new Properties();
                        p.setProperty(PROPERTIES_PATH_PROPERTY_NAME, DEFAULT_PATH);
                        try {
                            p.store(new FileWriter(PROPERTIES_FILENAME), "");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        saveDirectory = new File(DEFAULT_PATH);
                        if (!saveDirectory.exists()) {
                            if (!saveDirectory.mkdir()) {
                                throw new RuntimeException("Directory could not be created!");
                            }
                        }
                        System.out.print("Reset complete!");
                        running = false;
                    }
                }
                continue;
            }

            // checks for valid id and valid entry creation
            if (!nEntry.checkNumber(numberString)) {
                System.out.println("Invalid ID / command!\n");
                continue;
            }
            nEntry entry;
            try {
                entry = new nEntry(Integer.valueOf(numberString));
            } catch (IOException e) {
                System.out.println("Error!\nPlease check your id, as well as your internet connection.");
                continue;
            }

            // print entry to user and ask confirmation
            System.out.print("\n" + entry.toString() + "\n");

            String confirmation;
            while (true) {
                System.out.println("Download? (y/n)");
                confirmation = scanner.nextLine();
                if (!confirmation.equals("n") && !confirmation.equals("y")) {
                    continue;
                }
                break;
            }
            // user interrupts download
            if (confirmation.equals("n"))
                continue;
            System.out.println();

            // saving the entry
            String saveFolder = saveDirectory.getAbsolutePath();

            // input loop for save errors
            boolean stop = false;
            while (!stop) {
                int status = entry.save(saveFolder, true);
                switch (status) {
                    case nEntry.SAVE_SUCCESS:
                        System.out.println("Saving was successful.");
                        stop = true;
                        break;
                    case nEntry.SAVE_ERROR_DIRECTORY:
                        System.out.println("Saving failed!\nThe specified directory could not be created or does already exist.");
                        while (true) {
                            System.out.println("Would you like to delete the directory and all its contents and try again? (y/n)");
                            confirmation = scanner.nextLine();
                            if (!confirmation.equals("n") && !confirmation.equals("y")) {
                                continue;
                            }
                            break;
                        }
                        if (confirmation.equals("n")) {
                            stop = true;
                            System.out.print("\nDownload cancelled!\n\n");
                            break;
                        }
                        System.out.print("Deleting folder...\r");
                        IoUtil.deleteFolder(new File(saveFolder + "\\" + entry.getDirName()));
                        System.out.print("Folder deleted!");
                        System.out.println("\nTrying again...");
                        continue;
                    case nEntry.SAVE_ERROR_SAVING:
                        System.out.println("Saving failed!\nOne or more images could not be saved correctly.");
                        stop = true;
                        break;
                    case nEntry.SAVE_ERROR_LOADING:
                        System.out.println("Saving failed!\nOne or more images could not be loaded correctly.");
                        stop = true;
                        break;
                    default:
                        System.out.println("You shouldn't be here Ò_Ó");
                        stop = true;
                        break;
                }
            }
        }
    }

    /**
     * Method for printing the information about the program usage.
     */
    private static void printHelpInformation() {
        System.out.println("\n" + HELP_ASCII + "\n" +
                "'"+ DISPLAY_HELP_COMMAND + "':\n" +
                "\tOpens this screen.\n\n" +
                "'" + DIRECTORY_OPTION_COMMAND + "':\n" +
                "\tShows the user the current directory in which the entries are being saved.\n" +
                "\tAllows the user to change said directory as well as reset it to its default, which is the" +
                " 'nDownloads'\n\tfolder in the same directory as the .jar.\n" +
                "\t(deleting the '" + PROPERTIES_FILENAME + "' file resets the changed directory to its default as well)\n\n" +
                "'" + QUIT_COMMAND + "':\n" +
                "\tCloses this program.\n\n" +
                "Additional information:\n" +
                "\tWhen asked to enter a number use the number in the link of the entry you want to download. " +
                "\n\tE.g. you want to download the entry with the URL 'https://nhentai.net/g/144725/', " +
                "you would enter '144725'.\n" +
                "\tIn certain menus you are only allowed to write certain keywords, these are noted in parenthesis. \n" +
                "\t(y = yes; n = no)\n");
    }
}
