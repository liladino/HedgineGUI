package graphics.dialogs;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class NewGame extends JFrame {
    public NewGame(){
        setTitle("New game");
		setSize(300 + getInsets().left + getInsets().right, 200 + getInsets().top + getInsets().bottom);
        setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}
