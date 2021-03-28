import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet _wn;
    public Outcast(WordNet wordnet) {
        _wn = wordnet;
    }
    public String outcast(String[] nouns) {
        int[] distance = new int[nouns.length];
        int loc = -1;
        int maxDistance = -1;
        for (int i = 0; i < nouns.length; ++i) {
            for (int j = i+1; j < nouns.length; ++j) {
                int d = _wn.distance(nouns[i], nouns[j]);
                distance[i] += d;
                distance[j] += d;
            }
            if (distance[i] > maxDistance) {
                maxDistance = distance[i];
                loc = i;
            }
        }
        return nouns[loc];
    }
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
    
}
