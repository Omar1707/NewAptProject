import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class RobotHandler {
    public boolean robotCheck(String url)
    {
        try {
            //Robots.txt
            String robotUrl;
            boolean check = true;
            robotUrl = url.replaceAll("([^/])/([^/]|$).*", "$1/robots.txt");
            Document rob = Jsoup.connect(robotUrl).get();
            String robBody = rob.body().text();
            String[] disallows = new String[200];
            for (int i = 0; i < 200; i++) disallows[i] = null;
            disallows = robBody.split("Disallow: ");
            for (int i = 0; i < 200; i++) {
                if (disallows[i].charAt(0) == '/') {
                    disallows[i] = disallows[i].replaceAll(" .*", "");
                    check = url.contains(disallows[i]);
                    if (!check) break;
                }
            }
            return check;
        } catch(Exception e){}
        return false;
    }
}
