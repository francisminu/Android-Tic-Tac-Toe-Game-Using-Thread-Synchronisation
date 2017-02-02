/*
    The below code implements a Tic Tac Toe Game played between two Threads.
    The moves to be made by the Threads are decided on random.
    The moves taken by the threads are updated on the UI Game Board by the UI Thread

 */

package com.android.minuf.cs478_project4;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Multithread extends AppCompatActivity {


    public static final int SET_CURRENT_PLAYER = 0;
    public static final int INITIALISE_ARRAY = 1;
    public static final int SET_BOARD_SELECTION_THREAD1 = 2;
    public static final int SET_BOARD_SELECTION_THREAD2 = 3;
    public static final int THREAD_ONE_HANDLER = 4;
    public static final int THREAD_TWO_HANDLER = 5;
    public static final int GAME_OVER_ENDLOOP = 6;

    public int[] winnerArrayPlayerOne = {0,0,0,0,0,0,0,0};
    public int[] winnerArrayPlayerTwo = {0,0,0,0,0,0,0,0};


    private TextView currentPlayerStatus;


    private Thread playerOneThread = new Thread(new PlayerOneThread());
    private Thread playerTwoThread = new Thread(new PlayerTwoThread());

    public Handler playerOneHandler;
    public Handler playerTwoHandler;

    public TextView[] gameBoard;
    int chances = 0;


    /* Handler created for the UI thread.
    This handler does the below duties:
    1. Update the game board with the move provided by each of the worker threads
    2. Inform each worker thread that it is their chance to make a move
    3. Display the Game Board with current status of the players
     */
    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            int value = msg.what;
            switch(value){
                case SET_CURRENT_PLAYER:{ // This case statement sets the details of the Thread that is under consideration at any time
                    currentPlayerStatus.setText((String)msg.obj);
                    break;
                }
                case SET_BOARD_SELECTION_THREAD1: // The handler of thread one sends message to this to indicate the UI thread of the move that it has decided on. UI handler then updates the position on the board corresponding to the index provided by the worker thread
                {
                    int row = (int)msg.arg1; //arg1 contains the index of the box in the board that has to be updated with the respective thread
                    int threadNumber = (int)msg.arg2; //threadNumber contains the thread that has sent the message at the respective point, if value is 1, then it is thread1 (player1) and if it is 2, thread2 (player2)
                    String gameStatus = "";
                    gameStatus = updateGameBoardAndGetStatus(row,threadNumber); // UI handler updates the board with 'X' at the index provided as arg1 by thread 1

                    if(gameStatus == "Continue") //Send messages to the handler of Thread 2 only if the Game has not been won by any of the players and there are more moves to be played.
                    {
                        chances = chances + 1;
                        try { playerOneThread.sleep(3000); }
                        catch (InterruptedException e) { System.out.println("Thread interrupted!") ; };

                        if(chances < 9) // Game is played further only if there are empty indices (boxes) to be filled on the board - 9 is the max count of moves in a game
                        {
                            Message msg1 = playerTwoHandler.obtainMessage(THREAD_TWO_HANDLER); // Handler of thread 2 is called as it is Thread 2's chance to play now
                            msg1.obj = "Thread 1 Completed"; // Thread 1 is informing thread 2 that it has completed its move
                            playerTwoHandler.sendMessage(msg1);
                        }
                        else
                        {
                            // Informs the threads 1 and 2 that the game is over and there by asks them to stop the looper
                            Message msg1 = playerOneHandler.obtainMessage(GAME_OVER_ENDLOOP);
                            playerOneHandler.sendMessage(msg1);
                            Message msg2 = playerTwoHandler.obtainMessage(GAME_OVER_ENDLOOP);
                            playerTwoHandler.sendMessage(msg2);
                            try { Thread.sleep(2000); }
                            catch (InterruptedException e) { System.out.println("Thread interrupted!") ; };
                            currentPlayerStatus.setText("GAME OVER - THE GAME IS TIED!");
                            /* Assigning 0 to the variables and arrays, at the end of a game, so that previous values are not retaines during next game */
                            chances = 0;
                            winnerArrayPlayerOne = new int[]{0,0,0,0,0,0,0,0};
                            winnerArrayPlayerTwo = new int[]{0,0,0,0,0,0,0,0};

                        }
                    }
                    else
                    {
                        // Informs the threads 1 and 2 that the game is over and there by asks them to stop the looper
                        Message msg1 = playerOneHandler.obtainMessage(GAME_OVER_ENDLOOP);
                        playerOneHandler.sendMessage(msg1);
                        Message msg2 = playerTwoHandler.obtainMessage(GAME_OVER_ENDLOOP);
                        playerTwoHandler.sendMessage(msg2);
                        try { Thread.sleep(2000); }
                        catch (InterruptedException e) { System.out.println("Thread interrupted!") ; };
                        currentPlayerStatus.setText("PLAYER 1 HAS WON THE GAME!");
                        /* Assigning 0 to the variables and arrays, at the end of a game, so that previous values are not retaines during next game */
                        chances = 0;
                        winnerArrayPlayerOne = new int[]{0,0,0,0,0,0,0,0};
                        winnerArrayPlayerTwo = new int[]{0,0,0,0,0,0,0,0};
                    }

                    break;
                }
                case SET_BOARD_SELECTION_THREAD2: // This message is called by the handler of Thread 2 to inform the move taken by it to the UI handler
                {
                    int row = (int)msg.arg1;
                    int threadNumber = (int)msg.arg2;
                    String gameStatus = "";
                    gameStatus = updateGameBoardAndGetStatus(row, threadNumber); // UI handler updates the board with 'O' at the index provided as arg1 by thread 2


                    if(gameStatus == "Continue") //Send messages to the handler of Thread 1 only if the Game has not been won by any of the players and there are more moves to be played.
                    {
                        chances = chances + 1;

                        try { playerTwoThread.sleep(3000); }
                        catch (InterruptedException e) { System.out.println("Thread interrupted!") ; };

                        if(chances < 9) // Game is played further only if there are empty indices (boxes) to be filled on the board - 9 is the max count of moves in a game
                        {
                            Message msg1 = playerOneHandler.obtainMessage(THREAD_ONE_HANDLER);// Handler of thread 1 is called as it is Thread 1's chance to play now
                            msg1.obj = "Thread 2 Completed"; // Thread 2 is informing thread 1 that it has completed its move
                            playerOneHandler.sendMessage(msg1);
                        }
                        else
                        {
                            // Informs the threads 1 and 2 that the game is over and there by asks them to stop the looper
                            Message msg1 = playerOneHandler.obtainMessage(GAME_OVER_ENDLOOP);
                            playerOneHandler.sendMessage(msg1);
                            Message msg2 = playerTwoHandler.obtainMessage(GAME_OVER_ENDLOOP);
                            playerTwoHandler.sendMessage(msg2);
                            try { Thread.sleep(2000); }
                            catch (InterruptedException e) { System.out.println("Thread interrupted!") ; };
                            currentPlayerStatus.setText("GAME OVER - THE GAME IS TIED!");
                            /* Assigning 0 to the variables and arrays, at the end of a game, so that previous values are not retaines during next game */
                            chances = 0;
                            winnerArrayPlayerOne = new int[]{0,0,0,0,0,0,0,0};
                            winnerArrayPlayerTwo = new int[]{0,0,0,0,0,0,0,0};
                        }
                    }
                    else
                    {
                        // Informs the threads 1 and 2 that the game is over and there by asks them to stop the looper
                        Message msg1 = playerOneHandler.obtainMessage(GAME_OVER_ENDLOOP);
                        playerOneHandler.sendMessage(msg1);
                        Message msg2 = playerTwoHandler.obtainMessage(GAME_OVER_ENDLOOP);
                        playerTwoHandler.sendMessage(msg2);
                        try { Thread.sleep(2000); }
                        catch (InterruptedException e) { System.out.println("Thread interrupted!") ; };
                        currentPlayerStatus.setText("PLAYER 2 HAS WON THE GAME!");
                        /* Assigning 0 to the variables and arrays, at the end of a game, so that previous values are not retaines during next game */
                        chances = 0;
                        winnerArrayPlayerOne = new int[]{0,0,0,0,0,0,0,0};
                        winnerArrayPlayerTwo = new int[]{0,0,0,0,0,0,0,0};
                    }
                    break;

                }
                case INITIALISE_ARRAY:
                {
                    initializeGameBoard(); // The values of the text views are initialized to null at the start of a game so that previous values are not retained
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multithread);

        currentPlayerStatus = (TextView) findViewById(R.id.currentPlayerStatus); // Text view which shows the current status of the game : 1. which player 2. Game: Win/Tie

        setBackgroundColors();

        Button btnNewGame = (Button) findViewById(R.id.btnNewGame);

        /* Perform the below operation on click of the button - 'New Game' */
        btnNewGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){


                if(playerOneThread.isAlive())
                {
                    initializeGameBoard(); // Initialize the board to null values when the button is clicked

                    /* The message queues of the handlers are getting cleared as New Game is to be started */
                    playerOneHandler.removeCallbacksAndMessages(null);
                    playerTwoHandler.removeCallbacksAndMessages(null);
                    uiHandler.removeCallbacksAndMessages(null);

                    // Starting Thread 1 again for new game
                    playerOneThread = new Thread(new PlayerOneThread());
                    playerOneThread.start();

                    Log.i("ThreadStatus","Thread 1 is alive");
                }
                else
                {
                    Log.i("ThreadStatus","First game");
                    initializeGameBoard(); // Initialize the board with null values (text views)
                    playerOneThread = new Thread(new PlayerOneThread());
                    playerOneThread.start(); // start thread 1
                }


                if(playerTwoThread.isAlive())
                {
                    initializeGameBoard(); // Initialize the board to null values when the button is clicked
                    /* The message queues of the handlers are getting cleared as New Game is to be started */
                    playerOneHandler.removeCallbacksAndMessages(null);
                    playerTwoHandler.removeCallbacksAndMessages(null);
                    uiHandler.removeCallbacksAndMessages(null);

                    // Starting Thread 2 again for New Game
                    playerTwoThread = new Thread(new PlayerTwoThread());
                    playerTwoThread.start();
                    //Log.i("ThreadStatus","Thread 2 is alive");
                }
                else
                {
                    initializeGameBoard(); // Initialize the board with null values (text views)
                    playerTwoThread = new Thread(new PlayerTwoThread());
                    playerTwoThread.start(); // start thread 2
                }

            }
        });
    }


    // Thread 1 corresponding to player 1
    public class PlayerOneThread implements Runnable{

        public void run(){

                // Since worker threads do not have looper by default, the looper.prepare is called
                Looper.prepare();

                /*Message msg1 = uiHandler.obtainMessage(INITIALISE_ARRAY); // When thsi message is called, UI handler initializes the text view
                uiHandler.sendMessage(msg1);*/

                // The first move is always played by Thread 1 (Player 1) and by sending the below message to UI Handler, it informs the ui handler to update the board with he below message
                Message msg = uiHandler.obtainMessage(Multithread.SET_CURRENT_PLAYER);
                String val = "PLAYER 1 IS MAKING THE FIRST MOVE!";
                msg.obj = val;
                uiHandler.sendMessage(msg);


                int index = getRandomIndex(); // Gets the index of the box where player one has to make its first move


                // Once the index is obtained, this message inidicates the UI handler of the move that has taken by Player 1 (Thread 1)
                Message mesg = uiHandler.obtainMessage(SET_BOARD_SELECTION_THREAD1);
                mesg.arg1 = index;
                mesg.arg2 = 1;
                uiHandler.sendMessage(mesg);

                // Handler for Thread 1. As the game proceeds, the UI handler would be sending messages to the playerOneHandler to inform the thread that it is it's chance to play and also indicates if the game is over or not
                playerOneHandler = new Handler(){
                    public void handleMessage(Message msg)
                    {
                        int value = msg.what;
                        switch(value){
                            case THREAD_ONE_HANDLER:{

                                Message msg1 = uiHandler.obtainMessage(Multithread.SET_CURRENT_PLAYER);
                                String val = "Player 1 is making a move!";
                                msg1.obj = val;
                                uiHandler.sendMessage(msg1);

                                int currentIndex = getRandomIndex(); // Gets the next move to be played by Thread 1

                                // Once the index is obtained, this message inidicates the UI handler of the move that has taken by Player 1 (Thread 1)
                                Message msg2 = uiHandler.obtainMessage(SET_BOARD_SELECTION_THREAD1);
                                msg2.arg1 = currentIndex;
                                msg2.arg2 = 1;
                                uiHandler.sendMessage(msg2);
                                break;
                            }
                            case GAME_OVER_ENDLOOP:{
                                playerOneHandler.getLooper().quit(); // Once the game ends, either when one of the threads win the game or when the game is tied, looper.quit() is called so that the thread 1 can stop its execution
                                break;
                            }
                        }
                    }
                };

                Looper.loop(); // Starts the looper so that all the messages in the Message Queue of playerOneHandler can be executed in sequence
        }
    }

    // Thread corresponding to Player 2
    public class PlayerTwoThread implements Runnable{

        public void run(){


                Looper.prepare(); // Preparing the looper so that it can loop through the messages of playerTwoHandler

                playerTwoHandler = new Handler(){
                    public void handleMessage(Message msg)
                    {
                        int value = msg.what;
                        switch(value){
                            case THREAD_TWO_HANDLER:{

                                //Displays the message that Player 2 is playing at the moment to the user
                                Message msg1 = uiHandler.obtainMessage(Multithread.SET_CURRENT_PLAYER);
                                String val = "Player 2 is making a move!";
                                msg1.obj = val;
                                uiHandler.sendMessage(msg1);

                                int currentIndex = getRandomIndex(); // Gets the next move to be played by Thread 2

                                // Once the index is obtained, this message informs the UI handler of the move that has taken by Player 2 (Thread 2)
                                Message msg2 = uiHandler.obtainMessage(SET_BOARD_SELECTION_THREAD2);
                                msg2.arg1 = currentIndex;
                                msg2.arg2 = 2;
                                uiHandler.sendMessage(msg2);
                                break;
                            }
                            case GAME_OVER_ENDLOOP:{
                                playerTwoHandler.getLooper().quit(); // Once the game ends, either when one of the threads win the game or when the game is tied, looper.quit() is called so that thread 1 can stop its execution
                                break;
                            }
                        }
                    }
                };

                Looper.loop(); // Starts the looper so that all the messages in the Message Queue of playerTwoHandler can be executed in sequence
        }
    }


    // Initializes the game board and the variables and arrays to 0 / null so that the game starts from scratch
    public void initializeGameBoard()
    {
        int textViewCount = 9;

        chances = 0;

        winnerArrayPlayerOne = new int[]{0,0,0,0,0,0,0,0};
        winnerArrayPlayerTwo = new int[]{0,0,0,0,0,0,0,0};

        currentPlayerStatus.setText("");

        gameBoard = new TextView[textViewCount];
        gameBoard[0] = (TextView) findViewById(R.id.box0);
        gameBoard[1] = (TextView) findViewById(R.id.box1);
        gameBoard[2] = (TextView) findViewById(R.id.box2);
        gameBoard[3] = (TextView) findViewById(R.id.box3);
        gameBoard[4] = (TextView) findViewById(R.id.box4);
        gameBoard[5] = (TextView) findViewById(R.id.box5);
        gameBoard[6] = (TextView) findViewById(R.id.box6);
        gameBoard[7] = (TextView) findViewById(R.id.box7);
        gameBoard[8] = (TextView) findViewById(R.id.box8);

        gameBoard[0].setText("");
        gameBoard[1].setText("");
        gameBoard[2].setText("");
        gameBoard[3].setText("");
        gameBoard[4].setText("");
        gameBoard[5].setText("");
        gameBoard[6].setText("");
        gameBoard[7].setText("");
        gameBoard[8].setText("");

    }

    public List<Integer> randomIndices = new ArrayList<Integer>();
    int rowVal = 0;
    String chosenIndexValue = "";

    //Returns the move to be made by a thread
    public int getRandomIndex()
    {
        while(true)
        {
            Random r = new Random();
            rowVal = r.nextInt(9-0)+0; // gets a random value between 0 and 9
            randomIndices.add(rowVal);
            /*if(!randomIndices.contains(rowVal))
                return random;*/

            chosenIndexValue = gameBoard[rowVal].getText().toString();
            if(chosenIndexValue == "") // Return the obtained index as the move to be taken by the respective player, if and only if neither of the players have put and X or 0 in the obtained location during the current game
            {
                break; // break the while loop if there is no value at the chosen index for the current game and return the value
            }

        }
        return rowVal;
    }


    // This method updates the UI. Updates the UI with X/O corresponding to the moves taken by each of the threads
    public String updateGameBoardAndGetStatus(int index,int threadNumber)
    {

        if(threadNumber == 1)
        {
            gameBoard[index].setText("X");
        }
        else
            gameBoard[index].setText("O");

        String gameStatus = checkGameStatus(index,threadNumber); // Returns the status of the game after the current game. Checks if the game has ended due to a tie/win
        return gameStatus;
    }

    // Checks the status of the game after each move by the threads
    public String checkGameStatus(int index, int thread)
    {
        String gameStatus = "Continue";

        if(thread == 1)
        {
            /* winnerArrayPlayerOne contains 8 cells with respect to row1,row2,row3,col1,col2,col3,diag1.diag2 for thread 1. Each of the values are incremented depending on
            which row/column the index belongs to. Array is updated only when Thread 1 makes a move*/
            if(index == 0)
            {
                winnerArrayPlayerOne[0] = winnerArrayPlayerOne[0] + 1;
                winnerArrayPlayerOne[3] = winnerArrayPlayerOne[3] + 1;
                winnerArrayPlayerOne[6] = winnerArrayPlayerOne[6] + 1;
            }
            else if(index == 1)
            {
                winnerArrayPlayerOne[0] = winnerArrayPlayerOne[0] + 1;
                winnerArrayPlayerOne[4] = winnerArrayPlayerOne[4] + 1;
            }
            else if(index == 2)
            {
                winnerArrayPlayerOne[0] = winnerArrayPlayerOne[0] + 1;
                winnerArrayPlayerOne[5] = winnerArrayPlayerOne[5] + 1;
                winnerArrayPlayerOne[7] = winnerArrayPlayerOne[7] + 1;
            }
            else if(index == 3)
            {
                winnerArrayPlayerOne[1] = winnerArrayPlayerOne[1] + 1;
                winnerArrayPlayerOne[3] = winnerArrayPlayerOne[3] + 1;
            }
            else if(index == 4)
            {
                winnerArrayPlayerOne[1] = winnerArrayPlayerOne[1] + 1;
                winnerArrayPlayerOne[4] = winnerArrayPlayerOne[4] + 1;
                winnerArrayPlayerOne[6] = winnerArrayPlayerOne[6] + 1;
                winnerArrayPlayerOne[7] = winnerArrayPlayerOne[7] + 1;
            }
            else if(index == 5)
            {
                winnerArrayPlayerOne[1] = winnerArrayPlayerOne[1] + 1;
                winnerArrayPlayerOne[5] = winnerArrayPlayerOne[5] + 1;
            }
            else if(index == 6)
            {
                winnerArrayPlayerOne[2] = winnerArrayPlayerOne[2] + 1;
                winnerArrayPlayerOne[3] = winnerArrayPlayerOne[3] + 1;
                winnerArrayPlayerOne[7] = winnerArrayPlayerOne[7] + 1;
            }
            else if(index == 7)
            {
                winnerArrayPlayerOne[2] = winnerArrayPlayerOne[2] + 1;
                winnerArrayPlayerOne[4] = winnerArrayPlayerOne[4] + 1;
            }
            else if(index == 8)
            {
                winnerArrayPlayerOne[2] = winnerArrayPlayerOne[2] + 1;
                winnerArrayPlayerOne[5] = winnerArrayPlayerOne[5] + 1;
                winnerArrayPlayerOne[6] = winnerArrayPlayerOne[6] + 1;
            }

            // If at any point, any of the values in winnerArrayPlayerOne equals 3, it means that the condition for winning the game is satisfied and thread 1 has won the game
            for(int i=0;i<winnerArrayPlayerOne.length;i++)
            {
                if(winnerArrayPlayerOne[i] == 3)
                {
                    //Log.i("Game Status ","Inside Thread 1");
                    gameStatus = "Win";
                    break;
                }
            }
        }
        else
        {
            /* winnerArrayPlayerOne contains 8 cells with respect to row1,row2,row3,col1,col2,col3,diag1.diag2 for thread 2. Each of the values are incremented depending on
            which row/column the index belongs to. Array is updated only when Thread 2 makes a move*/
            if(index == 0)
            {
                winnerArrayPlayerTwo[0] = winnerArrayPlayerTwo[0] + 1;
                winnerArrayPlayerTwo[3] = winnerArrayPlayerTwo[3] + 1;
                winnerArrayPlayerTwo[6] = winnerArrayPlayerTwo[6] + 1;
            }
            else if(index == 1)
            {
                winnerArrayPlayerTwo[0] = winnerArrayPlayerTwo[0] + 1;
                winnerArrayPlayerTwo[4] = winnerArrayPlayerTwo[4] + 1;
            }
            else if(index == 2)
            {
                winnerArrayPlayerTwo[0] = winnerArrayPlayerTwo[0] + 1;
                winnerArrayPlayerTwo[5] = winnerArrayPlayerTwo[5] + 1;
                winnerArrayPlayerTwo[7] = winnerArrayPlayerTwo[7] + 1;
            }
            else if(index == 3)
            {
                winnerArrayPlayerTwo[1] = winnerArrayPlayerTwo[1] + 1;
                winnerArrayPlayerTwo[3] = winnerArrayPlayerTwo[3] + 1;
            }
            else if(index == 4)
            {
                winnerArrayPlayerTwo[1] = winnerArrayPlayerTwo[1] + 1;
                winnerArrayPlayerTwo[4] = winnerArrayPlayerTwo[4] + 1;
                winnerArrayPlayerTwo[6] = winnerArrayPlayerTwo[6] + 1;
                winnerArrayPlayerTwo[7] = winnerArrayPlayerTwo[7] + 1;
            }
            else if(index == 5)
            {
                winnerArrayPlayerTwo[1] = winnerArrayPlayerTwo[1] + 1;
                winnerArrayPlayerTwo[5] = winnerArrayPlayerTwo[5] + 1;
            }
            else if(index == 6)
            {
                winnerArrayPlayerTwo[2] = winnerArrayPlayerTwo[2] + 1;
                winnerArrayPlayerTwo[3] = winnerArrayPlayerTwo[3] + 1;
                winnerArrayPlayerTwo[7] = winnerArrayPlayerTwo[7] + 1;
            }
            else if(index == 7)
            {
                winnerArrayPlayerTwo[2] = winnerArrayPlayerTwo[2] + 1;
                winnerArrayPlayerTwo[4] = winnerArrayPlayerTwo[4] + 1;
            }
            else if(index == 8)
            {
                winnerArrayPlayerTwo[2] = winnerArrayPlayerTwo[2] + 1;
                winnerArrayPlayerTwo[5] = winnerArrayPlayerTwo[5] + 1;
                winnerArrayPlayerTwo[6] = winnerArrayPlayerTwo[6] + 1;
            }

            // If at any point, any of the values in winnerArrayPlayerOne equals 3, it means that the condition for winning the game is satisfied and thread 2 has won the game
            for(int i=0;i<winnerArrayPlayerTwo.length;i++)
            {
                if(winnerArrayPlayerTwo[i] == 3)
                {
                    gameStatus = "Win";
                    break;
                }
            }
        }


        return gameStatus;
    }

    // Sets the background color as Black for the textviews in the table layout
    public void setBackgroundColors()
    {
        TextView box1 = (TextView) findViewById(R.id.box0);
        box1.setBackgroundColor(Color.BLACK);
        box1.setTextColor(Color.WHITE);

        TextView box2 = (TextView) findViewById(R.id.box1);
        box2.setBackgroundColor(Color.BLACK);
        box2.setTextColor(Color.WHITE);

        TextView box3 = (TextView) findViewById(R.id.box2);
        box3.setBackgroundColor(Color.BLACK);
        box3.setTextColor(Color.WHITE);

        TextView box4 = (TextView) findViewById(R.id.box3);
        box4.setBackgroundColor(Color.BLACK);
        box4.setTextColor(Color.WHITE);

        TextView box5 = (TextView) findViewById(R.id.box4);
        box5.setBackgroundColor(Color.BLACK);
        box5.setTextColor(Color.WHITE);

        TextView box6 = (TextView) findViewById(R.id.box5);
        box6.setBackgroundColor(Color.BLACK);
        box6.setTextColor(Color.WHITE);

        TextView box7 = (TextView) findViewById(R.id.box6);
        box7.setBackgroundColor(Color.BLACK);
        box7.setTextColor(Color.WHITE);

        TextView box8 = (TextView) findViewById(R.id.box7);
        box8.setBackgroundColor(Color.BLACK);
        box8.setTextColor(Color.WHITE);

        TextView box9 = (TextView) findViewById(R.id.box8);
        box9.setBackgroundColor(Color.BLACK);
        box9.setTextColor(Color.WHITE);
    }
}
