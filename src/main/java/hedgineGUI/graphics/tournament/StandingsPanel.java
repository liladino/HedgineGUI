package hedgineGUI.graphics.tournament;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import hedgineGUI.tournament.MatchupScores.Matchup;
import hedgineGUI.tournament.Resolution;

/** Tournament status line, standings, pair scores and resolution counts. */
final class StandingsPanel extends JPanel {
    private final JLabel status = new JLabel("Configure the first round");
    private final DefaultTableModel scores = readOnlyModel("Player", "Wins", "Draws", "Losses", "Points");
    private final DefaultTableModel pairScores = readOnlyModel("Player", "Opponent", "0 (LL)", "0.5 (LD)",
            "1 (WL/DD)", "1.5 (WD)", "2 (WW)", "Complete", "Incomplete");
    private final DefaultTableModel resolutions = readOnlyModel("Resolution", "This round", "Tournament");

    StandingsPanel() {
        super(new BorderLayout(8, 8));
        add(status, BorderLayout.NORTH);
        JPanel results = new JPanel(new BorderLayout(8, 8));
        JPanel standingsPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        JScrollPane playerPane = new JScrollPane(new JTable(scores));
        playerPane.setBorder(BorderFactory.createTitledBorder("Individual games"));
        standingsPanel.add(playerPane);
        JTable pairTable = new JTable(pairScores);
        pairTable.setToolTipText("One row per matchup; scores are from the Player column’s perspective.");
        pairTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < pairTable.getColumnCount(); column++)
            pairTable.getColumnModel().getColumn(column).setPreferredWidth(column < 2 ? 110 : 85);
        JScrollPane pairPane = new JScrollPane(pairTable);
        pairPane.setBorder(BorderFactory.createTitledBorder("Pair scores by matchup"));
        standingsPanel.add(pairPane);
        results.add(standingsPanel, BorderLayout.CENTER);
        JTable gameCounts = new JTable(resolutions);
        gameCounts.setFillsViewportHeight(true);
        JScrollPane countsPane = new JScrollPane(gameCounts);
        countsPane.setPreferredSize(new Dimension(350, 190));
        countsPane.setBorder(BorderFactory.createTitledBorder("Games by resolution"));
        results.add(countsPane, BorderLayout.SOUTH);
        add(results, BorderLayout.CENTER);
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    void setStatus(String text) {
        status.setText(text);
    }

    /** Values are wins, draws, losses and points, as returned by Standings. */
    void showStandings(Map<String, double[]> standings) {
        scores.setRowCount(0);
        standings.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[3], a.getValue()[3]))
                .forEach(entry -> {
                    double[] s = entry.getValue();
                    scores.addRow(new Object[]{entry.getKey(), (int)s[0], (int)s[1], (int)s[2], s[3]});
                });
    }

    void showMatchups(Map<Matchup, long[]> counts) {
        pairScores.setRowCount(0);
        counts.forEach((matchup, bins) -> pairScores.addRow(new Object[]{
                matchup.player(), matchup.opponent(), bins[0], bins[1], bins[2], bins[3], bins[4],
                bins[0] + bins[1] + bins[2] + bins[3] + bins[4], bins[5]}));
    }

    void showResolutions(List<Resolution> round, List<Resolution> tournament) {
        resolutions.setRowCount(0);
        for (Resolution resolution : Resolution.values()) {
            resolutions.addRow(new Object[]{resolution,
                    round.stream().filter(resolution::equals).count(),
                    tournament.stream().filter(resolution::equals).count()});
        }
        resolutions.addRow(new Object[]{"Total", round.size(), tournament.size()});
    }

    /** Plain-text copy of everything shown, for the results file. */
    String toText() {
        StringBuilder text = new StringBuilder(status.getText()).append("\n\n");
        text.append("Individual games (wins draws losses points)\n");
        for (int row = 0; row < scores.getRowCount(); row++)
            text.append(scores.getValueAt(row, 0)).append(": ").append(cells(scores, row, 1)).append('\n');
        text.append("\nPair scores (0 0.5 1 1.5 2 complete incomplete)\n");
        for (int row = 0; row < pairScores.getRowCount(); row++)
            text.append(pairScores.getValueAt(row, 0)).append(" vs ").append(pairScores.getValueAt(row, 1))
                    .append(": ").append(cells(pairScores, row, 2)).append('\n');
        text.append("\nGames by resolution (this round, tournament)\n");
        for (int row = 0; row < resolutions.getRowCount(); row++)
            text.append(resolutions.getValueAt(row, 0)).append(": ").append(cells(resolutions, row, 1)).append('\n');
        return text.toString();
    }

    private static String cells(DefaultTableModel model, int row, int from) {
        return IntStream.range(from, model.getColumnCount())
                .mapToObj(column -> String.valueOf(model.getValueAt(row, column)))
                .collect(Collectors.joining(" "));
    }
}
