import java.util.HashMap;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    private final HashMap<String, int[]> _nameWinLossRemain;
    private final HashMap<String, HashMap<String, Integer>> _againstMap;
    private final HashMap<String, Bag<String>> _eliminationSubSet;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        if (filename == null) throw new IllegalArgumentException("constructor argument is null");
        if (filename.length() == 0) throw new IllegalArgumentException("constructor argument is the empty string");
        _nameWinLossRemain = new HashMap<String, int[]>();
        _againstMap = new HashMap<String, HashMap<String, Integer>>();
        _eliminationSubSet = new HashMap<String, Bag<String>>();

        In in = new In(filename);
        int totalTeams = 0;
        try {
            totalTeams = Integer.parseInt(in.readLine());
            String[] numToString = new String[totalTeams];
            int filledTeams = 0;
            while (in.hasNextLine()) {
                String line = in.readLine();
                line = line.strip();
                String[] breakUp = line.split(" +");
                String team = breakUp[0];
                numToString[filledTeams] = team;
                _nameWinLossRemain.put(team, new int[]{Integer.parseInt(breakUp[1]), Integer.parseInt(breakUp[2]), Integer.parseInt(breakUp[3])});
                for (int againstNum = 0; againstNum < filledTeams; ++againstNum) {
                    int againstMatch = Integer.parseInt(breakUp[4+againstNum]);
                    populateOpponentsMap(team, numToString[againstNum], againstMatch);
                    populateOpponentsMap(numToString[againstNum], team, againstMatch);
                }
                ++filledTeams;
            }
        }
        catch (java.util.NoSuchElementException e) {
            throw new IllegalArgumentException("invalid input format in Digraph constructor", e);
        }
    }

    private void populateOpponentsMap(String team1, String team2, int againstMatches) {
        HashMap<String, Integer> team1Opps = _againstMap.get(team1);
        if (team1Opps == null) {
            team1Opps = new HashMap<String, Integer>();
            team1Opps.put(team2, againstMatches);
            _againstMap.put(team1, team1Opps);
        }
        else {
            team1Opps.put(team2, againstMatches);
        }
    }

    private void checkInvalidString(String team) {
        if (team == null)
            throw new IllegalArgumentException("Null team name");
        if (!_nameWinLossRemain.containsKey(team))
            throw new IllegalArgumentException("Not valid team name");
    }

    // number of teams
    public int numberOfTeams() {
        return _nameWinLossRemain.size();
    }

    // all teams
    public Iterable<String> teams() {
        return _nameWinLossRemain.keySet();
    }

    // number of wins for given team
    public int wins(String team) {
        checkInvalidString(team);
        return _nameWinLossRemain.get(team)[0];
    }

    // number of losses for given team
    public int losses(String team) {
        checkInvalidString(team);
        return _nameWinLossRemain.get(team)[1];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        checkInvalidString(team);
        return _nameWinLossRemain.get(team)[2];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        checkInvalidString(team1);
        checkInvalidString(team2);
        if (team1.equals(team2))
            return 0;
        return _againstMap.get(team1).get(team2);
    }

    private boolean isTriviallyEliminated(String team) {
        // Trivial Elimination
        Bag<String> subset = new Bag<String>();
        for (String testTeam : teams()) {
            if (testTeam.equals(team)) continue;
            if (wins(team) + remaining(team) < wins(testTeam)) {
                subset.add(testTeam);
            }
        }
        _eliminationSubSet.put(team, subset);
        return !subset.isEmpty();
    }

    private boolean isNonTriviallyEliminated(String team) {
        String[] numToTeam = new String[numberOfTeams()-1];
        int loc = 0;
        for (String inGraphTeam : teams()) {
            if (inGraphTeam.equals(team)) continue;
            numToTeam[loc++] = inGraphTeam;
        }
        int totalNode = numToTeam.length + numToTeam.length*(numToTeam.length-1)/2 + 2;
        // Team node = [0, numToTeam.length)
        // against games node = [numToTeam.length, numToTeam.length + numToTeam.length*(numToTeam.length-1)/2 )
        // source node = numToTeam.length + numToTeam.length*(numToTeam.length-1)/2;
        // target node = numToTeam.length + numToTeam.length*(numToTeam.length-1)/2 + 1;
        int s = totalNode - 2;
        int t = totalNode - 1;
        FlowNetwork fn = new FlowNetwork(totalNode);
        int currentTeamMaxWin = wins(team) + remaining(team);
        // node denoting games between team[i] and team[j] where j > i;
        int ijNode = numToTeam.length;
        for (int i = 0; i < numToTeam.length; ++i) {
            fn.addEdge(new FlowEdge(i, t, currentTeamMaxWin - wins(numToTeam[i])));
            for (int j = i+1; j < numToTeam.length; ++j) {
                fn.addEdge(new FlowEdge(s, ijNode, against(numToTeam[i], numToTeam[j])));
                fn.addEdge(new FlowEdge(ijNode, i, Double.POSITIVE_INFINITY));
                fn.addEdge(new FlowEdge(ijNode++, j, Double.POSITIVE_INFINITY));
            }
        }
        FordFulkerson maxFlow = new FordFulkerson(fn, s, t);
        Bag<String> subset = new Bag<String>();
        for (int i = 0; i < numToTeam.length; ++i) {
            if (maxFlow.inCut(i)) {
                subset.add(numToTeam[i]);
            }
        }
        _eliminationSubSet.put(team, subset);
        return !subset.isEmpty();
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        checkInvalidString(team);
        if (_eliminationSubSet.containsKey(team)) {
            return !_eliminationSubSet.get(team).isEmpty();
        }
        return isTriviallyEliminated(team) || isNonTriviallyEliminated(team);

    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        checkInvalidString(team);
        if (!isEliminated(team))
            return null;
        return _eliminationSubSet.get(team);
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}