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
import java.util.logging.Logger;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import chess.Board;
import chess.Move;
import utility.*;
import chess.Square;
import game.GameManager;
import game.GameStarter;
import game.Human;
import game.interfaces.GameEventListener;
import graphics.GraphicSettings;
import graphics.MenuManager;
import graphics.dialogs.InformationDialogs;
import graphics.dialogs.PromotionDialog;

/**
 * Shows the chess board on the screen. 
 * Handles clicks on it, and if the active player is a human, registers its move. 
 */
public class ChessBoardPanel extends JPanel implements GameEventListener {
	private static final Logger logger = Logger.getLogger(ChessBoardPanel.class.getName());
	private static final long serialVersionUID = 987168713547L;
	private transient GameManager gameManager;
	private transient Square selected;
	private int xDim;
	private int squareSize;
	private transient HashMap<Character, BufferedImage> images;
	
	private boolean dragged = false;
	private int dragX;
	private int dragY;
	private char draggedPiece;
	
	public ChessBoardPanel(GameManager gameManager, MenuManager menuManager) {
		images = new HashMap<>();
		loadPieces();
		selected = new Square();

		xDim = Math.min(getWidth(), getHeight());
		squareSize = xDim / 8;
		this.gameManager = gameManager;
		gameManager.addGameChangeListener(this);
		menuManager.addGameEventListener(this);
		
		setPreferredSize(new Dimension(720, 720));

		
		MouseAdapter mouseAdapter = new MouseAdapter() {
			/*@Override
			public void mouseClicked(MouseEvent e) {
				Square s = getSquare(e.getX(), e.getY());
				
				logger.info("file: " + s.getFile() + ", rank: " + s.getRank());
				handleSquareClick(s.getFile(), s.getRank());
			}*/
			
			@Override
			public void mousePressed(MouseEvent e) {
				Square s = getSquare(e.getX(), e.getY());
				
				logger.info("file: " + s.getFile() + ", rank: " + s.getRank());
				if (GraphicSettings.dragDrop) {
					draggedPiece = gameManager.getBoard().boardAt(s);
					if (draggedPiece == ' ') {
						handleSquareClick(s.getFile(), s.getRank());
						return;
					}
					if ((gameManager.getBoard().tomove() == Sides.WHITE 
									&& Character.isLowerCase(gameManager.getBoard().boardAt(s))) 
							|| (gameManager.getBoard().tomove() == Sides.BLACK 
									&& Character.isUpperCase(gameManager.getBoard().boardAt(s)))) {
						handleSquareClick(s.getFile(), s.getRank());
						return;
					}					
					
					if ((gameManager.getBoard().tomove() == Sides.WHITE && Character.isUpperCase(draggedPiece)) 
						|| (gameManager.getBoard().tomove() == Sides.BLACK && Character.isLowerCase(draggedPiece))) {

						dragged = true;
						selected = s;
						draggedPiece = gameManager.getBoard().boardAt(s);
						
					}
				}
				else {
					handleSquareClick(s.getFile(), s.getRank());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (!GraphicSettings.dragDrop || !dragged) {
					return;
				}
				Square s = getSquare(e.getX(), e.getY());
				
				logger.info("file: " + s.getFile() + ", rank: " + s.getRank());
				dragged = false;
				if (!s.equals(selected)) {
					handleSquareClick(s.getFile(), s.getRank());
					selected = new Square();
				}
				else {
					selected = new Square();
					handleSquareClick(s.getFile(), s.getRank());
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (!GraphicSettings.dragDrop || !dragged) {
					return;
				}	
				dragX = e.getX();
				dragY = e.getY();

				logger.info(Integer.toString(dragX) + ' ' + Integer.toString(dragY));
				repaint();
			}
		};

		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}
	
	private Square getSquare(int X, int Y) {
		int rank;
		char file = (char)((X - (getWidth() - xDim) / 2)/ squareSize + 'a');
		if (GraphicSettings.rotateBoard) {
			rank = Y / squareSize + 1; 
			file = (char)(7 - file + 'a' + 'a');
		}
		else {
			rank = 7 - Y / squareSize + 1; 
		}
		return new Square(file, rank);
	}
	
	void loadPieces() {
		String imagesPath = "/resources/pieces/";
		String selectionPath = "/resources/select/";
		InputStream imageStream = null;// = getClass().getResourceAsStream(resourcePath);
	
		boolean fail = false;
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "wp.png"))) throw new IOException();
			images.put('P', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "wr.png"))) throw new IOException();
			images.put('R', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "wb.png"))) throw new IOException();
			images.put('B', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "wn.png"))) throw new IOException();
			images.put('N', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try {
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "wq.png"))) throw new IOException();
			images.put('Q', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "wk.png"))) throw new IOException();
			images.put('K', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; } 
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "bp.png"))) throw new IOException();
			images.put('p', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "br.png"))) throw new IOException();
			images.put('r', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "bb.png"))) throw new IOException();
			images.put('b', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try {
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "bn.png"))) throw new IOException();
			images.put('n', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "bq.png"))) throw new IOException();
			images.put('q', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(imagesPath + "bk.png"))) throw new IOException();
			images.put('k', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); fail = true; }
		
		//selection
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(selectionPath + "blue.png"))) throw new IOException();
			images.put('S', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); }
		
		//check
		try { 
			if (null == (imageStream = getClass().getResourceAsStream(selectionPath + "magenta.png"))) throw new IOException();
			images.put('C', ImageIO.read(imageStream));
		} catch (IOException e) { e.printStackTrace(); }
		
		if (fail) {
			InformationDialogs.errorDialog(GameStarter.getMainWindow(), "Can't load all images at \"" + imagesPath + "\".\nLetters will be visible instead.");
		}
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
				
				//if the program reaches this, the active player is surely human
				Human h = (Human) (gameManager.getCurrentPlayer());
				h.makeMove(new Move(from, to, promotion));
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		xDim = Math.min(getWidth(), getHeight());
		
		squareSize = xDim / 8;
		
		if (!dragged) {
			drawBoard(g);
		}
		else {
			Board board = new Board(gameManager.getBoard());
			board.getRawBoard()[selected.getRowCoord()][selected.getColCoord()] = ' ';
			drawBoard(g, board);
			
			drawPiece(g, dragX - squareSize/2, dragY - squareSize/2, draggedPiece, getSquare(dragX, dragY).getFile(), getSquare(dragX, dragY).getFile());
			
		}
	}
	
	private void drawPiece(Graphics g, int xCoord, int yCoord, char piece, char file, int rank) {
		if (images.containsKey(piece)) {
			g.drawImage(images.get(piece).getScaledInstance(squareSize, squareSize, Image.SCALE_FAST)
					, xCoord, yCoord, squareSize, squareSize, this);
		}
		else {
			//piece image isn't available
			g.setColor(Color.RED);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString(Character.toString(piece), 
					xCoord + squareSize/2, yCoord + squareSize/2);
		}
	}
	
	private void drawBoard(Graphics g, Board board) {
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
					drawPiece(g, xCoord, yCoord, board.boardAt(file, rank), file, rank);
				}
			}
		}
	}

	private void drawBoard(Graphics g) {
		Board board = gameManager.getBoard();
		drawBoard(g, board);
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

	@Override
	public void onCheckmate(Sides won) {
		//
	}

	@Override
	public void onDraw() {
		//
	}

	@Override
	public void onStalemate() {
		//
	}

	@Override
	public void onInsufficientMaterial() {
		//
	}

	@Override
	public void onTimeIsUp(Sides won) {
		//
	}

	@Override
	public void onTimeIsUp() {
		//
	}

	@Override
	public void onResign(Sides won) {
		// 
	}
	
}
