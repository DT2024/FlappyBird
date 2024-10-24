import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird  extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 480;
    int boardHeight = 720;

    //images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //bird class
    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 44;
    int birdHeight = 34;
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }
    
    //Pipes 
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  //scaled by 1/6
    int pipeHeight = 512;

    class Pipe{
        int x=pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;

        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }
    
    //game logic
    Bird bird;
    int velocityX=-4; //pipes movement left
    int velocityY=0; //birds movement up and down
    int gravity=1;
    ArrayList<Pipe> pipes;
    Random random = new Random(); // to give random position
    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    boolean canRestart = true;  // A flag to prevent restarting during game over sound
    double score = 0;

       // Background music clip
       Clip backgroundMusicClip;
       Clip gameOverClip; // To manage the game over sound

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        //load images
        backgroundImg = new ImageIcon(getClass().getResource("./back.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

          //bird
          bird = new Bird(birdImg);
          pipes = new ArrayList<Pipe>();
          //place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Code to be executed
              placePipes();
            }
        });
        placePipeTimer.start();

          //game timer
		gameLoop = new Timer(1000/60, this); //how long it takes to start timer, milliseconds gone between frames 
        gameLoop.start(); 

          // Start the background music
          playBackgroundMusic();
    }

     // Function to play background music
     public void playBackgroundMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("background.wav").getAbsoluteFile());
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the background music
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.out.println("Error with playing background music.");
            ex.printStackTrace();
        }
    }

    // Function to stop the background music
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop(); // Stop the music when the game is over
        }
    }
    // Function to play sound when space is pressed
    public void playJumpSound() {
        playSound("jump.wav");
    }

     // Function to play game over sound
     public void playGameOverSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("gameover.wav").getAbsoluteFile());
            gameOverClip = AudioSystem.getClip();
            gameOverClip.open(audioInputStream);

            // Disable restarting until the sound finishes
            canRestart = false;

            // Add a listener to detect when the game over sound finishes
            gameOverClip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        gameOverClip.close();  // Close the clip when finished
                        canRestart = true;     // Re-enable restarting
                    }
                }
            });

            gameOverClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.out.println("Error with playing game over sound.");
            ex.printStackTrace();
        }
    }

    // Generic function to play sound
    public void playSound(String soundFileName) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFileName).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;
    
        Pipe topPipe = new Pipe(topPipeImg);
         topPipe.y = randomPipeY;
        pipes.add(topPipe);
    
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

    public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
         //bird
         g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
        //pipes 
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

         //score
         g.setColor(Color.white);

         g.setFont(new Font("Arial", Font.PLAIN, 32));
         if (gameOver) {
             g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
         }
         else {
             g.drawString(String.valueOf((int) score), 10, 35);
         }
    }
    public void move(){
        //bird
        velocityY +=gravity;
        bird.y +=velocityY;
        bird.y =Math.max(bird.y,0);

        //pipes
        for (int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; 
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }
    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            playGameOverSound(); // Play game over sound when the game ends
        }
    }
   
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameOver) {
                // Game is ongoing, allow jump
                velocityY = -9;
                playJumpSound(); // Play sound when space is pressed
            } else if (gameOver && canRestart) {
                // Game is over, only restart if allowed (i.e., when game over sound finishes)
                // Restart game by resetting conditions
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
                playBackgroundMusic(); // Restart background music
            }
        }
        
    }
    @Override
    public void keyTyped(KeyEvent e) {
        
    }
    @Override
    public void keyReleased(KeyEvent e) {
        
    }
}
