import java.io.IOException;

public class Main {
    public static void main(String[]args) throws IOException {
        Database db = new Database();
        try {
            java.sql.Connection con = db.connectDatabase();
        } catch (Exception e){}

        RobotHandler rh = new RobotHandler();

        Crawler crawler = new Crawler(db,rh);
        while (true) crawler.crawl();

    }
}
