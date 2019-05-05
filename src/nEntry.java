import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * The objects of this class represent an nhentai.net entry. The objects allow easy access to name, page count,
 * artists and tags. As well as a method to download the current entries pages.
 */
public class nEntry {
    // constants
    private static final String BASE_URL = "https://nhentai.net/g/";

    // save() return constants
    public static final int SAVE_SUCCESS = 0;
    public static final int SAVE_ERROR_DIRECTORY = 1;
    public static final int SAVE_ERROR_LOADING = 2;
    public static final int SAVE_ERROR_SAVING = 3;

    // media URL related
    private String media_base_url;
    private String defaultFileType;

    // entry data
    private String name;
    private ArrayList<String> tags = new ArrayList<>();
    private ArrayList<String> artists = new ArrayList<>();
    private int pages;

    // file related
    private String dirName;

    /**
     * Creates a new nEntry-object which represents a nhentai.net entry using a specified id and loads all relevant data.
     * @param id the entry id, part of the nhentai.net URL (<Code>https://nhentai.net/g/<id>/</Code>)
     * @throws IOException if there was an error to connect to the website
     */
    public nEntry(int id) throws IOException{

            // create document from built URL
            String URL = getUrlById(id);
            Document doc = Jsoup.connect(URL).get();
            Element informationBox = doc.getElementById("info");

            // extract name from site
            name = informationBox.getElementsByTag("h1").get(0).text();

            // create valid directory name from name
            dirName = IoUtil.dirNameFromString(name);

            // extract tags from site, sorts tags alphabetically
            Element tagsElement = informationBox.getElementById("tags");
            for (Element e : tagsElement.child(2).getElementsByTag("a")) {
                tags.add(e.text().substring(0, e.text().lastIndexOf(' ')));
            }
            tags.sort(Comparator.naturalOrder());

            // extract artists from site, sorts artists alphabetically
            for (Element e : tagsElement.child(3).getElementsByTag("a")) {
                artists.add(e.text().substring(0, e.text().lastIndexOf(' ')));
            }
            artists.sort(Comparator.naturalOrder());

            // extract page count from site
            pages = Integer.parseInt(informationBox.child(3).text().split(" ")[0]);


            // extract media URL;
            // nhentai.net uses a different domain to host its pictures
            // extracting this URL calls to the original site can be minimized
            if (pages == 0) {
                throw new IOException("Entry has no pages!");
            }
            Document doc2 = Jsoup.connect(URL + "1/").get();
            Element imageContainer = doc2.getElementById("image-container");
            String imageURL = imageContainer.getElementsByTag("img").get(0).attributes().get("src");
            media_base_url = imageURL.substring(0, imageURL.lastIndexOf('/') + 1);
            defaultFileType = imageURL.substring(imageURL.lastIndexOf('.') + 1);
    }

    /**
     * Returns a valid directory name constructed out of this entries name as a String.
     * @return a valid directory name constructed
     */
    public String getDirName() {
        return dirName;
    }


    /**
     * Returns a String representation of the entry URL on nhentai.net using an integer id.
     * The id represents the following part of the URL: <Code>https://nhentai.net/g/<id>/</Code>
     * @param id the entry id, part of the nhentai.net URL
     * @return a String representation of the URL of the entry with the specified id
     */
    private static String getUrlById(int id) {
        return BASE_URL + id + "/";
    }

    /**
     * Checks whether a String can be used as a valid id for nhentai.net. A String is valid, if it has a valid
     * integer representation and is positive.
     * @param number the String to be checked
     * @return <Code>true</Code> - if <Code>number</Code> has a valid integer representation. which is positive;
     * <Code>false</Code> - otherwise
     */
    public static boolean checkNumber(String number) {
        int id;
        try {
            id = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return (id > 0);
    }

    /**
     * Returns a formatted String representation of the entry, displaying name, page count, tags as well as artists.
     * @return a formatted String representation of the entry
     */
    @Override
    public String toString() {
        // tags separated by commas
        StringBuilder sbTags = new StringBuilder();
        int i;
        for (i = 0; i < tags.size() - 1; i++) {
            sbTags.append(tags.get(i));
            sbTags.append(", ");
        }
        if (tags.size() > 0) {
            sbTags.append(tags.get(i));
        }

        // artists separated by commas
        StringBuilder sbArtists = new StringBuilder();
        for (i = 0; i < artists.size() - 1; i++) {
            sbArtists.append(artists.get(i));
            sbArtists.append(", ");
        }
        if (artists.size() > 0) {
            sbArtists.append(artists.get(i));
        }

        // format
        return String.format("Name: %s\nPages: %d\nTags: %s\nArtists: %s\n",
                name, pages, sbTags.toString(), sbArtists.toString());
    }

    /**
     * Saves the images of the entry into a newly created folder inside the directory, which is represented by the path.
     * Can write the current progress to the command line.
     * @param path the String representation of the path in which the new folder for saving the images will be created
     * @param printProgress describes whether or not the progress is written to the command line
     * @return whether or not an error occurred while saving, the different errors are represented by different integer
     * constants
     */
    public int save(String path, boolean printProgress) {
        // creates the new directory
        File dir = new File(path + "/" + dirName);
        if (!dir.mkdir()) {
            return SAVE_ERROR_DIRECTORY;
        }

        // calculate digits of pages; used for image names to be in order when sorted alphabetically
        int tempPages = pages;
        int pageCountDigits = 0;
        while (tempPages != 0) {
            pageCountDigits++;
            tempPages /= 10;
        }

        // saves each page
        for (int i = 1; i <= pages; i++) {
            // build image URL; load image
            String currentFileType = defaultFileType;
            String currUrl = media_base_url + "/" + i + "." + currentFileType;
            BufferedImage image;
            try {
                image = ImageIO.read(new URL(currUrl));
            } catch (Exception e) {
                currentFileType = defaultFileType.equals("jpg") ? "png" : "jpg";
                currUrl = media_base_url + "/" + i + "." + currentFileType;
                try {
                    image = ImageIO.read(new URL(currUrl));
                } catch (Exception ex) {
                    return SAVE_ERROR_LOADING;
                }
            }

            // build file name, save image
            String formatString = "%s/%s/%0" + pageCountDigits + "d.%s";
            String imgPath = String.format(formatString, path, dirName, i, currentFileType);
            try {
                ImageIO.write(image, currentFileType, new File(imgPath));
            } catch (Exception e) {
                return SAVE_ERROR_SAVING;
            }

            // print current progress
            if (printProgress) {
                if (i != pages)
                    System.out.printf("(%d/%d)\r", i, pages);
                else
                    System.out.printf("(%d/%d)\n", i, pages);
            }
        }
        return SAVE_SUCCESS;
    }
}
