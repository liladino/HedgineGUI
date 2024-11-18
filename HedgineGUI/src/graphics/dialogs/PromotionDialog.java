package graphics.dialogs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import utility.Sides;

public class PromotionDialog extends JDialog{
	private static final long serialVersionUID = 1687874164L;
	private char selectedPiece;
	private transient Map<Character, BufferedImage> images;
	public PromotionDialog(JFrame parent, Map<Character, BufferedImage> images, Sides tomove) {
		super(parent, "Pawn promotion", true);
		this.images = images;
		selectedPiece = ' '; //this space works as a cancel operation too (like on exit)
		setLayout(new GridLayout(1, 4));
		setSize(400, 90 + parent.getInsets().top + parent.getInsets().bottom);
		setResizable(false);
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		if (tomove == Sides.WHITE) {
			addPieceButton('Q');
			addPieceButton('R');
			addPieceButton('B');
			addPieceButton('N');
		}
		else {	
			addPieceButton('q');
			addPieceButton('r');
			addPieceButton('b');
			addPieceButton('n');
		}
	}

	public char getSelectedPiece() {
		return Character.toLowerCase(selectedPiece);
	}
	
	private void addPieceButton(char piece) {
		JButton button = new JButton();
		ImageIcon icon = null;
		if (images.containsKey(piece)) {
			icon = new ImageIcon(images.get(piece).getScaledInstance(80, 80, Image.SCALE_SMOOTH));
			button.setIcon(icon);
		}
		else {
			button.setText(Character.toString(piece));
		}
		button.setPreferredSize(new Dimension(80, 80));
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.addActionListener(
			(ActionEvent e) -> {
				selectedPiece = piece;
				dispose();
			}
		);
		
		add(button);
	}
}