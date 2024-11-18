package graphics.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import chess.Board;
import chess.Move;
import utility.*;
import chess.Square;
import game.GameManager;
import game.Human;
import game.interfaces.VisualChangeListener;
import graphics.GraphicSettings;
import graphics.MenuManager;
import graphics.dialogs.PromotionDialog;

public class ChessBoardPanel extends JPanel implements VisualChangeListener {
	private static final long serialVersionUID = 987168713547L;
	private transient GameManager gameManager;
	private transient Square selected;
	private int xDim;
	private int squareSize;
	private transient HashMap<Character, BufferedImage> images;

	public ChessBoardPanel(GameManager gameManager, MenuManager menuManager) {
		images = new HashMap<>();
		loadPieces();
		selected = new Square();

		xDim = Math.min(getWidth(), getHeight());
		squareSize = xDim / 8;
		this.gameManager = gameManager;
		gameManager.addVisualChangeListener(this);
		menuManager.addVisualChangeListener(this);
		
		setPreferredSize(new Dimension(720, 720));

		// Add a mouse listener to handle piece selection and movement
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int rank;
				char file = (char)((e.getX() - (getWidth() - xDim) / 2)/ squareSize + 'a');
				if (GraphicSettings.rotateBoard) {
					rank = e.getY() / squareSize + 1; 
					file = (char)(7 - file + 'a' + 'a');
				}
				else {
					rank = 7 - e.getY() / squareSize + 1; 
				}
				
				System.out.printf("file: %c, rank: %d%n", file, rank);
				handleSquareClick(file, rank);
			}
		});
	}
	
	void loadPieces() {
		String imagesPath = System.getProperty("user.dir") + "/resources/pieces/";
		String selectionPath = System.getProperty("user.dir") + "/resources/select/";
		System.out.println(System.getProperty("user.dir") + "/resources/select/");
		try { images.put('P', ImageIO.read(new File(imagesPath + "wp.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('R', ImageIO.read(new File(imagesPath + "wr.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('B', ImageIO.read(new File(imagesPath + "wb.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('N', ImageIO.read(new File(imagesPath + "wn.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('Q', ImageIO.read(new File(imagesPath + "wq.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('K', ImageIO.read(new File(imagesPath + "wk.png"))); } catch (IOException e) { e.printStackTrace(); } 
		try { images.put('p', ImageIO.read(new File(imagesPath + "bp.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('r', ImageIO.read(new File(imagesPath + "br.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('b', ImageIO.read(new File(imagesPath + "bb.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('n', ImageIO.read(new File(imagesPath + "bn.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('q', ImageIO.read(new File(imagesPath + "bq.png"))); } catch (IOException e) { e.printStackTrace(); }
		try { images.put('k', ImageIO.read(new File(imagesPath + "bk.png"))); } catch (IOException e) { e.printStackTrace(); }
		
		//selection
		try { images.put('S', ImageIO.read(new File(selectionPath + "blue.png"))); } catch (IOException e) { e.printStackTrace(); }
		//check
		try { images.put('C', ImageIO.read(new File(selectionPath + "magenta.png"))); } catch (IOException e) { e.printStackTrace(); }
	}
	
	Sides pieceColor(char c) {
		if (c >= 'a' && c <= 'z') return Sides.BLACK;
		return Sides.WHITE;
	}

	private void handleSquareClick(char file, int rank) {
		if (!gameManager.getCurrentPlayer().isHuman()) {
			return;
		}
		
		if (selected.isNull()) {
			if (gameManager.getBoard().boardAt(file, rank) != ' ' && 
					pieceColor(gameManager.getBoard().boardAt(file, rank)) == gameManager.getBoard().tomove()) {
				//allow only piece selection
				selected = new Square(file, rank);
				repaint();
			}
		} else {
			// second click, try moving the piece
			char promotion = ' ';
			Square from = new Square(selected);
			Square to = new Square(file, rank);
			
			if (	
				gameManager.getBoard().boardAt(from) != ' ' &&
				gameManager.getBoard().boardAt(to) != ' ' &&
				pieceColor(gameManager.getBoard().boardAt(from)) == pieceColor(gameManager.getBoard().boardAt(to))
					) {
				//same side's piece selected, move the selection
				
				if (from.equals(to)) {
					selected = new Square();
				}
				else {
					selected = new Square(file, rank);
				}
				repaint();
			}
			else {		   	
				if (Character.toLowerCase(gameManager.getBoard().boardAt(from)) == 'p' && 
						(to.getRank() == 8 || to.getRank() == 1) ) {
					//check if any promotion is legal
					char[] promPieces = {'q', 'r', 'k', 'b'};
					boolean isPromLegal = false;
					for (char c : promPieces) {
						if (gameManager.getBoard().isMoveLegal(new Move(from, to, c))){
							isPromLegal = true;
							break;
						}
					}
					if (isPromLegal) {
						//handle promotion
						PromotionDialog dialog = new PromotionDialog(
								(JFrame) SwingUtilities.getWindowAncestor(this), images, gameManager.getBoard().tomove());
						dialog.setVisible(true);
						promotion = dialog.getSelectedPiece();
					}
				}
				
				selected = new Square();
				repaint();
				
				Human h = (Human) (gameManager.getCurrentPlayer());
				h.makeMove(gameManager, new Move(from, to, promotion));
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		xDim = Math.min(getWidth(), getHeight());
		
		squareSize = xDim / 8;
		
		drawBoard(g);
	}

	private void drawBoard(Graphics g) {
		
		Board board = gameManager.getBoard();

		for (int rank = 1; rank <= 8; rank++) {
			for (char file = 'a'; file <= 'h'; file++) {
				int xCoord, yCoord;
				if (GraphicSettings.rotateBoard) {
					yCoord = (rank - 1) * squareSize;
					xCoord = (7 - file + 'a') * squareSize + (getWidth() - xDim) / 2;
				}
				else {
					yCoord = (8 - rank) * squareSize;
					xCoord = (file - 'a') * squareSize + (getWidth() - xDim) / 2;
				}
				
				if ((file-'a' + rank) % 2 == 0) {
					g.setColor(getColor(Sides.WHITE));
				} else {
					g.setColor(getColor(Sides.BLACK));
				}
				g.fillRect(xCoord, yCoord, squareSize, squareSize);
				
				// Draw pieces
				if (((board.boardAt(file, rank) == 'K' && board.tomove() == Sides.WHITE) 
					|| (board.boardAt(file, rank) == 'k' && board.tomove() == Sides.BLACK))
						&& board.inCheck()
						&& images.containsKey('C')
						){
					//mark check
					g.drawImage(images.get('C'), xCoord, yCoord, squareSize, squareSize, this);
				}
				if (file == selected.getFile() 
					&& rank == selected.getRank()
					&& images.containsKey('S')) {
					//mark piece selection
				   	g.drawImage(images.get('S'), xCoord, yCoord, squareSize, squareSize, this);
				}
				
				if (board.boardAt(file, rank) != ' ') {
					if (images.containsKey(board.boardAt(file, rank))) {
						g.drawImage(images.get(board.boardAt(file, rank)).getScaledInstance(squareSize, squareSize, Image.SCALE_SMOOTH)
								, xCoord, yCoord, squareSize, squareSize, this);
					}
					else {
						//piece image isn't available
						g.setColor(Color.RED);
						g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
						g.drawString(Character.toString(board.boardAt(file, rank)), 
								xCoord + squareSize/2, yCoord + squareSize/2);
					}
				}
			}
		}
	}
	
	private Color getColor(Sides s) {
		if (s == Sides.WHITE) {
			if (GraphicSettings.colors.get(GraphicSettings.selectedScheme) != null)
				return GraphicSettings.colors.get(GraphicSettings.selectedScheme).first;	
			return Color.white;
		}
		else {
			if (GraphicSettings.colors.get(GraphicSettings.selectedScheme) != null)
				return GraphicSettings.colors.get(GraphicSettings.selectedScheme).second;
			
			return Color.gray;
		}
	}

	@Override
	public void onGameStateChanged() {
		repaint();
	}

	@Override
	public void onGameLooksChanged() {
		repaint();
	}
	
}
