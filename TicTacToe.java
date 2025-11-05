import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TicTacToe implements ActionListener {

    Random random = new Random();
    JFrame frame = new JFrame();
    JPanel title_panel = new JPanel();
    JPanel button_panel = new JPanel();
    JLabel textField = new JLabel();
    JButton[] buttons = new JButton[9];
    boolean player1_turn;

    public TicTacToe() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.getContentPane().setBackground(new Color(50, 50, 50));
        frame.setLayout(new BorderLayout());

        textField.setBackground(new Color(25, 25, 25));
        textField.setForeground(new Color(25, 255, 0));
        textField.setFont(new Font("Ink Free", Font.BOLD, 75));
        textField.setHorizontalAlignment(JLabel.CENTER);
        textField.setText("Tic-Tac-Toe");
        textField.setOpaque(true);
        textField.setBorder(new EmptyBorder(10, 10, 10, 10));

        title_panel.setLayout(new BorderLayout());
        title_panel.setBounds(0, 0, 800, 100);
        title_panel.add(textField);

        button_panel.setLayout(new GridLayout(3, 3));
        button_panel.setBackground(new Color(150, 150, 150));

        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton();
            buttons[i].setFont(new Font("MV Boli", Font.BOLD, 120));
            buttons[i].setFocusable(false);
            buttons[i].addActionListener(this);
            buttons[i].setOpaque(true);
            buttons[i].setBackground(Color.WHITE);
            button_panel.add(buttons[i]);
        }

        frame.add(title_panel, BorderLayout.NORTH);
        frame.add(button_panel);
        frame.setVisible(true);

        firstTurn();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < 9; i++) {
            if (e.getSource() == buttons[i]) {
                if (player1_turn && buttons[i].getText().equals("")) {
                    buttons[i].setForeground(Color.RED);
                    buttons[i].setText("X");
                    player1_turn = false;
                    check();

                    // If game is not over, trigger AI turn
                    if (!checkWinner("X") && !isFull()) {
                        aiMoveAsync();
                    }
                }
            }
        }
    }

    // Run AI in background via threading to prevent UI freezing
    public void aiMoveAsync() {
        textField.setText("AI thinking...");
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            int[] move = findBestMove();
            int bestMove = move[0];
            if (bestMove != -1) {
                buttons[bestMove].setForeground(Color.BLUE);
                buttons[bestMove].setText("O");
            }
            player1_turn = true;
            SwingUtilities.invokeLater(this::check);
        }).start();
    }

    public void firstTurn() {
        try {
            Thread.sleep(700);
        } catch (InterruptedException ignored) {
        }

        player1_turn = random.nextBoolean();
        if (player1_turn) {
            textField.setText("X Turn");
        } else {
            textField.setText("O Turn");
            aiMoveAsync();
        }
    }

    public void check() {
        if (checkWinner("X")) {
            textField.setText("X Wins");
            highlightWinner("X");
            scheduleReset();
        } else if (checkWinner("O")) {
            textField.setText("O Wins");
            highlightWinner("O");
            scheduleReset();
        } else if (isFull()) {
            textField.setText("Draw!");
            scheduleReset();
        } else {
            if (player1_turn) {
                textField.setText("X Turn");
            } else {
                textField.setText("O Turn");
            }
        }
    }

    // Delay 3s before resetting the game after someone wins or draws
    public void scheduleReset() {
        disableButtons();
        new javax.swing.Timer(13000, e -> resetGame()).start();
    }

    public void resetGame() {
        for (int i = 0; i < 9; i++) {
            buttons[i].setText("");
            buttons[i].setBackground(Color.WHITE);
            buttons[i].setEnabled(true);
        }
        firstTurn();
    }

    public int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int move = -1;

        for (int i = 0; i < 9; i++) {
            if (buttons[i].getText().equals("")) {
                buttons[i].setText("O");
                int score = minimax(0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                buttons[i].setText("");
                if (score > bestScore) {
                    bestScore = score;
                    move = i;
                }
            }
        }
        return new int[] { move };
    }

    public int minimax(int depth, boolean isMaximising, int alpha, int beta) {
        if (checkWinner("O"))
            return 10 - depth;
        if (checkWinner("X"))
            return depth - 10;
        if (isFull())
            return 0;

        if (isMaximising) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (buttons[i].getText().equals("")) {
                    buttons[i].setText("O");
                    int score = minimax(depth + 1, false, alpha, beta);
                    buttons[i].setText("");
                    best = Math.max(best, score);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha)
                        break;
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (buttons[i].getText().equals("")) {
                    buttons[i].setText("X");
                    int score = minimax(depth + 1, true, alpha, beta);
                    buttons[i].setText("");
                    best = Math.min(best, score);
                    beta = Math.min(beta, best);
                    if (beta <= alpha)
                        break;
                }
            }
            return best;
        }
    }

    public boolean isFull() {
        for (JButton button : buttons) {
            if (button.getText().equals(""))
                return false;
        }
        return true;
    }

    public boolean checkWinner(String player) {
        int[][] winConditions = {
                { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 },
                { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
                { 0, 4, 8 }, { 2, 4, 6 }
        };
        for (int[] cond : winConditions) {
            if (buttons[cond[0]].getText().equals(player) &&
                    buttons[cond[1]].getText().equals(player) &&
                    buttons[cond[2]].getText().equals(player)) {
                return true;
            }
        }
        return false;
    }

    //Highlight the moves of the winner

    public void highlightWinner(String player) {
        int[][] winConditions = {
                { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 },
                { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
                { 0, 4, 8 }, { 2, 4, 6 }
        };
        for (int[] cond : winConditions) {
            if (buttons[cond[0]].getText().equals(player) &&
                    buttons[cond[1]].getText().equals(player) &&
                    buttons[cond[2]].getText().equals(player)) {
                buttons[cond[0]].setBackground(Color.GREEN);
                buttons[cond[1]].setBackground(Color.GREEN);
                buttons[cond[2]].setBackground(Color.GREEN);
                disableButtons();
                break;
            }
        }
    }

    public void disableButtons() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
    }
}
