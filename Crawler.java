import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.Statement;

public class Crawler {
    private Database db;
    private RobotHandler rh;

    public Crawler(Database db, RobotHandler rh)
    {
        this.db = db;
        this.rh = rh;
    }

    public String Normalize(String url)
    {
        url = url.replaceAll("HTTPS|HTTP|https", "http");
        url = url.replaceAll("www\\.","");
        url = url.replaceAll("%([A-Z0-9])*","~");
        url = url.replaceAll(":([0-9]+)","");
        url = url.replaceAll("/$","$");
        url = url.replaceAll("/\\.\\./","/");
        url = url.replaceAll("[a-z0-9]+\\.(asp|html|php|shtml|jsp)","");
        url = url.replaceAll("#([a-zA-Z0-9]*)","");
        url = url.replaceAll("[^:]//","/");


        return url;

    }

    public void crawl()
    {
        try {
            String query = "SELECT URL,FileName FROM aptproject.documentfile;";
            Statement stmt;
            ResultSet rs;

            stmt = db.con.createStatement();
            rs = stmt.executeQuery(query);
            String visited[] = new String[5000];
            for (int i=0;i<5000;i++) visited[i] = null;
            int visitedNext = 0;

            while (rs.next()) {
                String url = rs.getString("URL");
                int fileName = rs.getInt("FileName");

                Boolean check = true; //true: not visited, false: visited
                for (int i = 0; i < visitedNext; i++)
                    if (url.equals(visited[i])) check = false;



                if (check) {
                    Document doc = Jsoup.connect(url).get();
                    String docString = doc.outerHtml();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(Integer.toString(fileName)));
                    writer.write(docString);
                    writer.close();
                    org.jsoup.select.Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String newURL = Normalize(link.attr("abs:href"));

                        String exists = db.checkURL(newURL);
                        //boolean robotCheck = rh.robotCheck(newURL);
                        if (exists==null) {
                            System.out.println(newURL);
                            db.postDocuments(newURL,fileName);
                            fileName++;
                            visited[visitedNext] = newURL;
                            visitedNext++;
                        }
                    }
                }
            }

        } catch (Exception e){}
    }
}
