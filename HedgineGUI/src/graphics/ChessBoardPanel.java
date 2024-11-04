package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
import game.GameUpdateListener;
import game.Human;

public class ChessBoardPanel extends JPanel implements GameUpdateListener {
	private static final long serialVersionUID = 987168713547L;
	private GameManager gameManager;
    private int selectedRank = -1;
    private char selectedFile= 0; 
    private int xDim;
    private int yDim;
    private int squareSize;
    private HashMap<Character, BufferedImage>  images;

	public ChessBoardPanel(GameManager gameManager, int size) {
		images = new HashMap<Character, BufferedImage>();
		loadPieces();
		
		yDim = xDim = size;
		squareSize = xDim / 8;
        this.gameManager = gameManager;
        gameManager.addGameUpdateListener(this);
        
        setPreferredSize(new Dimension(xDim, yDim));

        // Add a mouse listener to handle piece selection and movement
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int rank = 7 - e.getY() / squareSize + 1; 
                char file = (char)(e.getX() / squareSize + 'a');
                
                handleSquareClick(file, rank);
            }
        });
    }
	
	void loadPieces() {
		String imagesPath = "../resources/pieces/";
		String selectionPath = "../resources/select/";

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
    	if (c >= 'a' && c <= 'z') return Sides.black;
    	return Sides.white;
    }

    private void handleSquareClick(char file, int rank) {
    	if (!gameManager.getCurrentPlayer().isHuman()) {
    		return;
    	}
    	
        if (selectedFile == 0 && selectedRank == -1) {
        	if (gameManager.getBoard().boardAt(file, rank) != ' ' 
        			&& pieceColor(gameManager.getBoard().boardAt(file, rank)) == gameManager.getBoard().tomove()) {
        		//allow only piece selection
	        	selectedFile = file;
	        	selectedRank = rank;
	        	repaint();
	        }
        } else {
            // second click, try moving the piece
        	char promotion = ' ';
        	Square from = new Square(selectedFile, selectedRank);
        	Square to = new Square(file, rank);
        	        	
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
        	
            selectedFile = 0;
        	selectedRank = -1;
    		repaint();
    		
    		Human h = (Human) (gameManager.getCurrentPlayer());
        	h.makeMove(gameManager, new Move(from, to, promotion));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        xDim = yDim = Math.min(panelWidth, panelHeight);
        squareSize = xDim / 8;
        
        drawBoard(g);
    }

    private void drawBoard(Graphics g) {
    	
        Board board = gameManager.getBoard();

        for (int rank = 1; rank <= 8; rank++) {
            for (char file = 'a'; file <= 'h'; file++) {
            	int xCoord = (file - 'a') * squareSize + (getWidth() - xDim) / 2, yCoord = (8 - rank) * squareSize;
            	
                if ((file-'a' + rank) % 2 == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.GRAY);
                }
                g.fillRect(xCoord, yCoord, squareSize, squareSize);
                
                // Draw pieces
                if (((board.boardAt(file, rank) == 'K' && board.tomove() == Sides.white) 
                	|| (board.boardAt(file, rank) == 'k' && board.tomove() == Sides.black))
                		&& board.inCheck()){
                	//mark check
                	if (images.containsKey('C')) {
                    	g.drawImage(images.get('C'), xCoord, yCoord, squareSize, squareSize, this);
                	}
                }
                if (file == selectedFile && rank == selectedRank) {
                	//mark piece selection
                	if (images.containsKey('S')) {
                    	g.drawImage(images.get('S'), xCoord, yCoord, squareSize, squareSize, this);
                	}
                }
                
                if (board.boardAt(file, rank) != ' ') {
                	if (images.containsKey(board.boardAt(file, rank))) {
                    	g.drawImage(images.get(board.boardAt(file, rank)).getScaledInstance(squareSize, squareSize, Image.SCALE_SMOOTH)
                    			, xCoord, yCoord, squareSize, squareSize, this);
                	}
                	else {
                		g.setColor(Color.RED);
                		g.drawString(Character.toString(board.boardAt(file, rank)), 
                				xCoord + squareSize/2, yCoord + squareSize/2);
                    }
                }
            }
        }
    }

	@Override
	public void onGameStateChanged() {
		//System.out.println("Szoveg");
		repaint();
	}
    
}
