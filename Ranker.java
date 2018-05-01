import opennlp.tools.stemmer.PorterStemmer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

public class Ranker {
    private Database db;
    public Ranker(Database db)
    {
        this.db = db;
    }

    public void rank(String searchWords)
    {
        String wordArr[] = searchWords.split(" ");
        int count = wordArr.length;
        Vector<Integer> documents = new Vector<Integer>();
        Vector<Integer> rank = new Vector<Integer>();
        int lastAddedIndex = 0;

        for (int i=0; i<count; i++)
        {
            String stem = new PorterStemmer().stem(wordArr[i]);
            int wid[] = new int[3];
            String type[] = new String[3];
            int arrLength = 0;
            arrLength = db.getWordID(stem, wid, type);

            for (int j=0;j<arrLength;j++)
            {
                db.getDocumentsContainingWord(wid[j],documents);
            }
            rank.setSize(documents.size());
            while (lastAddedIndex < rank.size())
            {
                rank.setElementAt(0, lastAddedIndex);
                lastAddedIndex++;
            }

            for (int j=0;j<arrLength;j++)
            {
                for (int k = 0;k<documents.size();k++)
                {
                    int wordCount = db.getCountPerDoc(documents.elementAt(k), wid[j]);
                    int factor;
                    if (type[j].equals("Title")) factor = 3;
                    else if (type[j].equals("Header")) factor = 2;
                    else factor = 1;

                    rank.setElementAt(rank.elementAt(k) + wordCount*factor, k);
                }
            }
        }
        db.updateTarget(documents,rank);
    }
}
