import java.util.Iterator;
import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
    private final Digraph _G;
    private int _cachedV;
    private int _cachedW;
    private int _cachedSingleLengh;
    private int _cachedSingleAncestor;
    private Iterable<Integer> _cachedIterV;
    private Iterable<Integer> _cachedIterW;
    private BreadthFirstDirectedPaths _cachedVBFS;
    private BreadthFirstDirectedPaths _cachedWBFS;
    private int _cachedIterLengh;
    private int _cachedIterAncestor;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null)
            throw new IllegalArgumentException("Cannot initialize SAP with a null Digraph");
        _G = new Digraph(G);
        _cachedV = -1;
        _cachedW = -1;
        _cachedSingleLengh = -1;
        _cachedSingleAncestor = -1;
        _cachedIterV = null;
        _cachedIterW = null;
        _cachedIterLengh = -1;
        _cachedIterAncestor = -1;
    }

    private boolean validCheck(Integer v) {
        if (v == null) return false;
        return v >= 0 && v < _G.V();
    }

    private void doBFS(int v, int w) {
        if (!validCheck(v) || !validCheck(w)) {
            throw new IllegalArgumentException("Invalid nodes");
        }
        _cachedV = v;
        _cachedW = w;
        BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(_G, v);
        BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(_G, w);
        _cachedSingleLengh = -1;
        _cachedSingleAncestor = -1;
        for (int i = 0; i < _G.V(); ++i) {
            if (vBFS.hasPathTo(i) && wBFS.hasPathTo(i)) {
                int currentDist = vBFS.distTo(i) + wBFS.distTo(i);
                if (currentDist < _cachedSingleLengh || _cachedSingleLengh == -1) {
                    _cachedSingleLengh = currentDist;
                    _cachedSingleAncestor = i;
                }
            }
        }
    }

    private void doBFS(Iterable<Integer> v, Iterable<Integer> w, boolean vUnchanged, boolean wUnchanged) {
        if (vUnchanged && wUnchanged)
            return;
        if (!vUnchanged) {
            _cachedIterV = v;
            _cachedVBFS = new BreadthFirstDirectedPaths(_G, v);
        }
        if (!wUnchanged) {
            _cachedIterW = w;
            _cachedWBFS = new BreadthFirstDirectedPaths(_G, w);
        }
        _cachedIterLengh = -1;
        _cachedIterAncestor = -1;
        for (int i = 0; i < _G.V(); ++i) {
            if (_cachedVBFS.hasPathTo(i) && _cachedWBFS.hasPathTo(i)) {
                int currentDist = _cachedVBFS.distTo(i) + _cachedWBFS.distTo(i);
                if (currentDist < _cachedIterLengh || _cachedIterLengh == -1) {
                    _cachedIterLengh = currentDist;
                    _cachedIterAncestor = i;
                }
            }
        }
    }
 
    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (!((v == _cachedV && w == _cachedW) || (v == _cachedW && w == _cachedV))) {
            doBFS(v, w);
        }
        return _cachedSingleLengh;
    }
 
    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (!((v == _cachedV && w == _cachedW) || (v == _cachedW && w == _cachedV))) {
            doBFS(v, w);
        }
        return _cachedSingleAncestor;
    }

    private boolean compareTwoIterables(Iterable<Integer> v1, Iterable<Integer> v2) {
        if (v1 == null || v2 == null) return false;
        Iterator<Integer> iter1 = v1.iterator();
        Iterator<Integer> iter2 = v2.iterator();
        while(iter1.hasNext() && iter2.hasNext()) {
            if (iter1.next() != iter2.next())
                return false;
        }
        return (!iter1.hasNext()) && (!iter2.hasNext());
    }

    private boolean validCheck(Iterable<Integer> v) {
        if (v == null) return false;
        for (Integer n : v) {
            if (!validCheck(n))
                return false;
        }
        return true;
    }
 
    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (!validCheck(v) || !validCheck(w)) throw new IllegalArgumentException("Invalid iterables");
        if (!v.iterator().hasNext() || !w.iterator().hasNext()) return -1;
        doBFS(v,
              w,
              compareTwoIterables(v, _cachedIterV),
              compareTwoIterables(w, _cachedIterW));
        return _cachedIterLengh;
    }    
 
    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (!validCheck(v) || !validCheck(w)) throw new IllegalArgumentException("Invalid iterables");
        if (!v.iterator().hasNext() || !w.iterator().hasNext()) return -1;
        doBFS(v,
              w,
              compareTwoIterables(v, _cachedIterV),
              compareTwoIterables(w, _cachedIterW));
        return _cachedIterAncestor;
    }
 
    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int ancestor = sap.ancestor(v, w);
            int length   = sap.length(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
 }