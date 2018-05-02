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
    private double d;

    public Crawler(Database db, RobotHandler rh)
    {
        this.db = db;
        this.rh = rh;
        d = 0.85;
    }

    public String Normalize(String url)
    {
        url = url.replaceAll("HTTPS|HTTP|https", "http");
        url = url.replaceAll("www\\.","");
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
            String query = "SELECT Uid,URL,FileName,Popularity FROM aptproject.documentfile;";
            Statement stmt;
            ResultSet rs;

            stmt = db.con.createStatement();
            rs = stmt.executeQuery(query);
            String visited[] = new String[5000];
            for (int i=0;i<5000;i++) visited[i] = null;
            int visitedNext = 0;
            int fileName = db.getMaxFileName();
            fileName++;

            while (rs.next()) {
                String url = rs.getString("URL");
                int uid = rs.getInt("Uid");
                double MyPop = rs.getDouble("Popularity");

                Boolean check = true; //true: not visited, false: visited
                for (int i = 0; i < visitedNext; i++)
                    if (url.equals(visited[i])) check = false;



                if (check) {
                    Document doc = Jsoup.connect(url).get();
                    String docString = doc.outerHtml();
                    if (uid == 1) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("Files\\1.txt"));
                        writer.write(docString);
                        writer.close();
                    }
                    org.jsoup.select.Elements links = doc.select("a[href]");
                    int noOfLinks = links.size();
                    for (Element link : links) {
                        String newURL = Normalize(link.attr("abs:href"));

                        String exists = db.checkURL(newURL);
                        boolean canConnect = !(newURL.contains("-"));
                        //boolean robotCheck = rh.robotCheck(newURL);
                        if (exists==null && canConnect) {
                            System.out.println(newURL);
                            String urlDoc = Jsoup.connect(newURL).get().outerHtml();
                            String file = "Files\\" + Integer.toString(fileName) + ".txt";
                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            writer.write(urlDoc);
                            writer.close();
                            double pop = (1-d) + (d*(MyPop/noOfLinks));
                            db.postDocuments(newURL,fileName,0.15);
                            fileName++;
                            visited[visitedNext] = newURL;
                            visitedNext++;
                        }
                        else if (exists!=null){
                            double currentPop = db.getPopularity(newURL);
                            double newPop = currentPop + d*(MyPop/noOfLinks);
                            db.updatePopularity(newPop,newURL);
                        }
                    }
                }
            }

        } catch (Exception e){}
    }
}
