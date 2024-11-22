import java.rmi.Naming;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Library musicLibrary = (Library) Naming.lookup("//localhost/Library");

            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Welcome to the Music Library");
                System.out.println("1. Search Songs");
                System.out.println("2. Stream Song");
                System.out.println("3. Upload Song");
                System.out.println("4. Get Song Metadata");
                System.out.println("5. Rate Song");
                System.out.println("6. Exit");

                while (true) {
                    System.out.print("Enter your choice: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            System.out.print("Enter search query: ");
                            String query = scanner.nextLine();
                            System.out.println("Search results: " + musicLibrary.searchSongs(query));
                            break;

                        case 2:
                            System.out.print("Enter song name: ");
                            String songName = scanner.nextLine();
                            byte[] data = musicLibrary.streamSong(songName);
                            System.out.println("Song streamed successfully: " + songName);
                            break;

                        case 3:
                            System.out.print("Enter song name: ");
                            String uploadName = scanner.nextLine();
                            System.out.println("Feature not implemented in CLI!");
                            break;

                        case 4:
                            System.out.print("Enter song name: ");
                            String metaName = scanner.nextLine();
                            System.out.println("Metadata: " + musicLibrary.getSongMetadata(metaName));
                            break;

                        case 5:
                            System.out.print("Enter song name to rate: ");
                            String rateName = scanner.nextLine();
                            System.out.print("Enter your rating (1-5): ");
                            int rating = scanner.nextInt();
                            scanner.nextLine();
                            musicLibrary.rateSong(rateName, rating);
                            System.out.println("Rated successfully.");
                            break;

                        case 6:
                            System.out.println("Exiting...");
                            return;

                        default:
                            System.out.println("Invalid choice!");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
