import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class WordNet {
    private final HashMap<String, Bag<Integer>> _dict;
    private final HashMap<Integer, String> _nameLookup;
    private final Digraph _G;
    private SAP _sap;
    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        In synsetsFI = new In(synsets);
        _dict = new HashMap<String, Bag<Integer>>();
        _nameLookup = new HashMap<Integer, String>();
        try {
            while(synsetsFI.hasNextLine()) {
                String line = synsetsFI.readLine();

                if (line.isEmpty()) break;
                String[] breakUp = line.split(",");
                String synset = breakUp[1];
                int num = Integer.parseInt(breakUp[0]);
                String[] synsetBreak = synset.split(" ");
                _nameLookup.put(num, breakUp[1]);
                for (String each : synsetBreak) {
                    Bag<Integer> foundBag = _dict.get(each);
                    if (foundBag == null) {
                        foundBag = new Bag<Integer>();
                        foundBag.add(num);
                        _dict.put(each, foundBag);
                    }
                    else {
                        foundBag.add(num);
                    }
                }
            }
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("invalid input format in Digraph constructor", e);
        }
        int size = _nameLookup.size();
        _G = new Digraph(size);
        In hypernymsFI = new In(hypernyms);
        try {
            while(hypernymsFI.hasNextLine()) {
                String line = hypernymsFI.readLine();
                if (line.isEmpty()) break;
                String[] breakUp = line.split(",");
                int baseNum = Integer.parseInt(breakUp[0]);
                for (int i = 1; i < breakUp.length; ++i) {
                    _G.addEdge(baseNum, Integer.parseInt(breakUp[i]));
                }
            }
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("invalid input format in Digraph constructor", e);
        }
        DirectedCycle cycleCheck = new DirectedCycle(_G);
        if (cycleCheck.hasCycle()) {
            throw new IllegalArgumentException("Cycle detected");
        }
        int root = -1;
        for (int i = 0; i < _G.V(); ++i) {
            if (_G.outdegree(i) == 0) {
                if (root == -1) root = i;
                else throw new IllegalArgumentException("Not single rooted");
            }
        }
        _sap = new SAP(_G);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return _dict.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("Null noun");
        return _dict.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException("Unknown noun");
        Bag<Integer> Alist = _dict.get(nounA);
        Bag<Integer> Blist = _dict.get(nounB);
        return _sap.length(Alist, Blist);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException("Unknown noun");
        Bag<Integer> Alist = _dict.get(nounA);
        Bag<Integer> Blist = _dict.get(nounB);
        return _nameLookup.get(_sap.ancestor(Alist, Blist));
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet test = new WordNet(args[0], args[1]);
        while (!StdIn.isEmpty()) {
            String v = StdIn.readString();
            String w = StdIn.readString();
            StdOut.printf("v = %s, w = %s\n", v, w);
            int length = test.distance(v, w);
            String ancestor = test.sap(v, w);
            StdOut.printf("length = %d, ancestor = %s\n", length, ancestor);
        }
    }
}
