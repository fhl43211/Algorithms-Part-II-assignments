import java.util.Iterator;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BoggleSolver {
    private final SimpleTrie _dictTrie;
    private final int[] _xDirections;
    private final int[] _yDirections;

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("Null dict");
        }
        _dictTrie = new SimpleTrie();
        for (String word : dictionary) {
            _dictTrie.add(word);
        }
        _xDirections = new int[]{1, -1, 0, 0, 1, -1, 1, -1};
        _yDirections = new int[]{0, 0, 1, -1, 1, -1, -1, 1};
    }

    
    // R-way trie node
    private class TrieNode {
        private static final int R = 26;  // A-Z
        private static final int Anum = 65; // letter A in ASCII
        private TrieNode[] next = new TrieNode[R];
        private boolean isString;
    }

    private class SimpleTrie implements Iterable<String>{
        private TrieNode root;
        public SimpleTrie() {
        }
        /**
         * Adds the key to the set if it is not already present.
         * @param key the key to add
         * @throws IllegalArgumentException if {@code key} is {@code null}
         */
        public void add(String key) {
            if (key == null) throw new IllegalArgumentException("argument to add() is null");
            root = add(root, key, 0);
        }
    
        private TrieNode add(TrieNode x, String key, int d) {
            if (x == null) x = new TrieNode();
            if (d == key.length()) {
                x.isString = true;
            }
            else {
                char c = key.charAt(d);
                x.next[c-'A'] = add(x.next[c-'A'], key, d+1);
            }
            return x;
        }

        private TrieNode get(TrieNode currentNode, String s, int index) {
            if (currentNode == null) return currentNode;
            if (index == s.length()) return currentNode;
            return get(currentNode.next[s.charAt(index) - 'A'], s, index+1);
        }

        public boolean contains(String s) {
            if (s == null) throw new IllegalArgumentException("Null string");
            TrieNode findNode = get(root, s, 0);
            if (findNode == null) return false;
            return findNode.isString;
        }

        private void collectAllWords(TrieNode node, StringBuilder currentWord, Bag<String> collected) {
            if (node == null) return;
            if (node.isString) {
                collected.add(currentWord.toString());
            }
            for (int i = 0; i < TrieNode.R; ++i) {
                if (node.next[i] != null) {
                    currentWord.append((char)(i+TrieNode.Anum));
                    collectAllWords(node.next[i], currentWord, collected);
                    currentWord.deleteCharAt(currentWord.length()-1);
                }
            }
        }

        @Override
        public Iterator<String> iterator() {
            Bag<String> words = new Bag<String>();
            StringBuilder currentWord = new StringBuilder();
            collectAllWords(root, currentWord, words);
            return words.iterator();
        }
    }

    private void findAllValidWords(BoggleBoard board, int m, int n, int row, int col, String s, SimpleTrie foundWords, TrieNode dictNode, boolean[] visited) {
        if (dictNode == null) return;
        if (s.length() >= 3 && dictNode.isString) {
            foundWords.add(s);
        }
        for (int i = 0; i < 8; ++i) {
            int newRow = row + _xDirections[i];
            int newCol = col + _yDirections[i];
            if (newRow < 0 || newRow >= m || newCol < 0 || newCol >= n) {
                continue;
            }
            if (visited[newRow*n + newCol]) continue;
            visited[newRow*n + newCol] = true;
            char nextLetter = board.getLetter(newRow, newCol);
            String nextS = s + nextLetter;
            TrieNode newNode = dictNode.next[nextLetter-'A'];
            if (nextLetter == 'Q') {
                if (newNode != null)
                    newNode = newNode.next['U'-'A'];
                nextS += 'U';
            }
            findAllValidWords(board, m, n, newRow, newCol, nextS, foundWords, newNode, visited);
            visited[newRow*n + newCol] = false;
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        SimpleTrie foundWords = new SimpleTrie();
        if (_dictTrie.root == null) return null;
        if (board == null) {
            throw new IllegalArgumentException("Null board");
        }
        int m = board.rows();
        int n = board.cols();
        boolean[] visitedPos = new boolean[m*n];
        for (int row = 0; row < m; ++row) {
            for (int col = 0; col < n; ++col) {
                char baseLetter = board.getLetter(row, col);
                String s = String.valueOf(baseLetter);
                visitedPos[row*n+ col] = true;
                TrieNode baseNode = _dictTrie.root.next[baseLetter - 'A'];
                if (baseLetter == 'Q') {
                    s += 'U';
                    baseNode = baseNode.next['U'-'A'];
                }
                findAllValidWords(board, m, n, row, col, s, foundWords, baseNode, visitedPos);
                visitedPos[row*n+ col] = false;
            }
        }
        return foundWords;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word == null) throw new IllegalArgumentException("Null word");
        if (!_dictTrie.contains(word)) return 0;
        int length = word.length();
        if (length < 3) return 0;
        else if (length <= 4) return 1;
        else if (length <= 5) return 2;
        else if (length <= 6) return 3;
        else if (length <= 7) return 5;
        else return 11;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        int cnt = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            ++cnt;
            score += solver.scoreOf(word);
        }
        StdOut.println("Count = " + cnt);
        StdOut.println("Score = " + score);
    }
}
