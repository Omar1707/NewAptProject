import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PhraseSearching {

    public PhraseSearching(){}

    public  void ProcessPhrase(String s) throws Exception {
        Queryprocessor q=new Queryprocessor();
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
        {
            System.out.println(matcher.group(1));
            s= s.replaceAll("\"(.*?)\"","");
            q.ProcessPhrase(s);
            //System.out.println(mydata);
        }
        else q.ProcessString(s);

    }

    public static void main(String[] args) throws Exception
    {
        String s=" Welcome \"The land of the dead\"";
        PhraseSearching p=new PhraseSearching();
        p.ProcessPhrase(s);
    }
}
