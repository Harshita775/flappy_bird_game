import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.util.Scanner;
import java.io.*;
 
public class FlappyBird extends JPanel implements ActionListener, KeyListener { 

    static int highscore ;
    public static void main(String []args ) throws Exception {
        int boardWidth = 360;
        int boardHieght = 640;
        JFrame frame = new JFrame("flappybird");
        frame.setSize(boardWidth , boardHieght);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FlappyBird flappybird = new FlappyBird();
        frame.add(flappybird);
        frame.pack();
        flappybird.requestFocus();
        frame.setVisible(true);

    try {
      File myObj = new File("highscore.txt");
      Scanner myReader = new Scanner(myObj);
      highscore = myReader.nextInt();
    //   System.out.println(highscore);
      myReader.close(); 
    } 
    catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
}


    int boardWidth = 360;
    int boardHieght = 640;

    // Images
    Image backgroungImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;
    
    // Bird
    int birdx = boardWidth/8;
    int birdy = boardHieght/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdx;
        int y = birdy;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipes
    int pipex = boardWidth;
    int pipey = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipex;
        int y = pipey;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game Logic
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;

    boolean gameOver = false;
    double score = 0;
    

    public FlappyBird() {

        setPreferredSize(new Dimension(boardWidth , boardHieght));
        setFocusable(true);
        addKeyListener(this);
        

        // load images
        this.backgroungImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        this.birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        this.topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        this.bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // place pipe timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        // Game Timer
        gameLoop = new Timer(1000/60 , this);
        gameLoop.start();

    }

    public void placePipes() {
        // (0-1) * pipeHeight/2 -> (0-256)
        // 0 - 128 - (0-256) --> 1/4 pipeHeight --> 3/4pipeHeight


        int randomPipeY = (int) (pipey - pipeHeight/4 - Math.random()*(pipeHeight/2));

        int openingSpace = boardHieght/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // System.out.println("draw");
        // background
        g.drawImage(backgroungImg , 0 ,0 , boardWidth, boardHieght ,null);

        // bird
        g.drawImage(bird.img , bird.x,bird.y, bird.width, bird.height ,null);

        // Pipes
        for(int i = 0;i<pipes.size();i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //  Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial" , Font.PLAIN, 32));
        if(gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
            g.drawString("High Score: " + String.valueOf((int) Math.max(score , highscore)), 10, 75);
            if(score > highscore){
                highscore = (int)score;
                try {
                    FileWriter myWriter = new FileWriter("highscore.txt");
                    myWriter.write(Integer.toString(highscore));
                    myWriter.close();
                    } 
                catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    }
                g.setFont(new Font("Arial" , Font.PLAIN, 27));
                g.setColor(Color.darkGray);
                g.drawString("Yuhuu! new high Score of: " + String.valueOf(highscore), 10, 155);
            }
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35 );
        }   
    }
    public void move() {
        // bird
        velocityY +=gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y , 0);

        // Pipes
        for(int i = 0;i<pipes.size();i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // because their are two set of pipes top and bottom..............
            }

            if(collision(bird , pipe)) {
                gameOver = true;
            }
        }

        if(bird.y > boardHieght) {
            gameOver = true;
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && // a,s top left corner doesn't reach b's top right corner
               a.x + a.width > b.x && // a,s top right pases b's top left corner
               a.y < b.y + b.height && // a,s top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;   // a,s bottom left corner passes b's top left corner

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();    
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if(gameOver) {
                bird.y = birdy;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start(); 
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    

    @Override
    public void keyReleased(KeyEvent e) {}


    
} 