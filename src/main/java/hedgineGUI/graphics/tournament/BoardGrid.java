package hedgineGUI.graphics.tournament;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

/**
 * Sizes the board cells to show the whole round in the visible area, picking the column count
 * that gives the largest boards. Only when boards would drop below MIN_BOARD does it scroll vertically.
 */
final class BoardGrid extends JPanel implements Scrollable {
    static final int MIN_BOARD = 150;
    private static final int GAP = 12;
    private record Fit(int columns, int board) {}

    BoardGrid() {
        super(null);
    }

    @Override public void addNotify() {
        super.addNotify();
        // Viewport height changes do not resize this panel, but they change the best fit.
        if (getParent() instanceof JViewport viewport) viewport.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { revalidate(); }
        });
    }

    /** Height of each cell's label and buttons; boards are given MIN_BOARD as preferred height. */
    private int extraHeight() {
        int extra = 0;
        for (Component cell : getComponents()) extra = Math.max(extra, cell.getPreferredSize().height - MIN_BOARD);
        return extra;
    }

    private Dimension area() {
        return getParent() instanceof JViewport viewport ? viewport.getExtentSize() : getSize();
    }

    private Fit fit(Dimension area, int extra) {
        int count = getComponentCount();
        if (count == 0 || area.width <= 0 || area.height <= 0) return new Fit(Math.max(1, Math.min(count, 2)), MIN_BOARD);
        Fit best = null;
        for (int columns = 1; columns <= count; columns++) {
            int rows = (count + columns - 1) / columns;
            int board = Math.min((area.width - GAP * (columns - 1)) / columns,
                    (area.height - GAP * (rows - 1)) / rows - extra);
            if (best == null || board > best.board()) best = new Fit(columns, board);
        }
        if (best.board() >= MIN_BOARD) return best;
        int columns = Math.max(1, Math.min(count, (area.width + GAP) / (MIN_BOARD + GAP)));
        return new Fit(columns, Math.max(1, (area.width - GAP * (columns - 1)) / columns));
    }

    @Override public void doLayout() {
        Dimension area = area();
        int extra = extraHeight();
        Fit fit = fit(area, extra);
        int cellHeight = fit.board() + extra;
        int left = Math.max(0, (area.width - fit.columns() * (fit.board() + GAP) + GAP) / 2);
        for (int i = 0; i < getComponentCount(); i++) {
            getComponent(i).setBounds(left + i % fit.columns() * (fit.board() + GAP),
                    i / fit.columns() * (cellHeight + GAP), fit.board(), cellHeight);
        }
    }

    @Override public Dimension getPreferredSize() {
        int extra = extraHeight();
        Fit fit = fit(area(), extra);
        int rows = (getComponentCount() + fit.columns() - 1) / fit.columns();
        return new Dimension(fit.columns() * (fit.board() + GAP) - GAP,
                Math.max(0, rows * (fit.board() + extra + GAP) - GAP));
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visible.height : visible.width;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
