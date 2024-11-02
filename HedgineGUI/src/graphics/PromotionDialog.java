package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.http.WebSocket.Listener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import org.junit.validator.PublicClassValidator;

import chess.Sides;

public class PromotionDialog extends JDialog{
	private static final long serialVersionUID = 1687874164L;
	private char selectedPiece;
	private HashMap<Character, BufferedImage>  images;
	public PromotionDialog(JFrame parent, HashMap<Character, BufferedImage> images, Sides tomove) {
		super(parent, "Pawn promotion", true);
		this.images = images;
		selectedPiece = ' '; //this space works as a cancel operation too (like on exit)
		setLayout(new FlowLayout());
		setSize(350, 90 + parent.getInsets().top + parent.getInsets().bottom);
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		if (tomove == Sides.white) {
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
		button.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedPiece = piece;
				dispose();
			}
		});		
		add(button);
	}
}