import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import javalib.worldimages.*;

class Card {
  Integer value;
  String suit;
  int state; //0: face down 1: face up 2: hidden
  int row;
  int column;

  /* Template:
   *  fields:
   *    this.value ... -- int
   *    this.suit ... -- String
   *  methods:
   *    this.flipCard() ... -- void
   *    this.drawCard() ... -- void
   * 
   */

  Card(Integer value, String suit, int state, int row, int column) {
    this.value = value;
    this.suit = suit;
    this.state = state;
    this.row = row;
    this.column = column;
  }

  String printCard() {
    return "[Card: Val " + this.value + " Suit " + this.suit + "]";
  }

  //EFFECT: Flips this card over (updates faceUp)
  void flipCard() {
    if (state == 0) {
      state = 1;
    }
    else if (state == 1) {
      state = 0;
    }
  }

  void changePosition(int r, int c) {
    this.row = r;
    this.column = c;
  }

  boolean positionEquals(int r, int c) {
    return (this.row == r && this.column == c);
  }

  boolean sameValue(Card c) {
    return this.value == c.value;
  }

  //Draws this card onto given scene
  void drawCard(WorldScene aScene, int x, int y) {
    if (this.state == 1) {
      aScene.placeImageXY(new RectangleImage(44, 60, OutlineMode.SOLID, Color.BLUE), x, y);
      aScene.placeImageXY(new ScaleImage(new TextImage(this.suit, Color.BLACK), 1.5), (x - 4), y);
      aScene.placeImageXY(new TextImage(this.value.toString(), Color.BLACK), x - 15, y - 20);
      aScene.placeImageXY(new TextImage(this.value.toString(), Color.BLACK), x + 12, y + 20);
    }
    else if (state == 0) {
      aScene.placeImageXY(new RectangleImage(44, 60, OutlineMode.SOLID, Color.GREEN), x, y);
    }
    else {
      aScene.placeImageXY(new RectangleImage(44, 60, OutlineMode.SOLID, Color.WHITE), x, y);
    }
  }

  public void hideCard() {
    // TODO Auto-generated method stub
    this.state = 2;
  }

}

class Board {
  ArrayList<Card> cards;
  ArrayList<String> suits = new ArrayList<String>(Arrays.asList("♣", "♦", "♥", "♠"));
  Integer score;
  Integer minutes;
  Integer seconds;

  /* Template:
   *  Fields:
   *    this.cards ... -- ArrayList<Card>
   *    this.suits ... -- ArrayList<String>
   *  Methods:
   *    this.shuffleCards(Random) ... -- void
   *    this.flipCards() ... -- void
   *    this.createGrid() ... -- ArrayList<ArrayList<Card>>
   *    this.drawBoard(WorldScene, int, int, int, int) ... -- void  
   */

  Board() {
    cards = new ArrayList<Card>();
    score = 26;
    minutes = 0;
    seconds = 0;

    for (int i = 0; i < 4; i++) {
      for (int j = 1; j < 14; j++) {
        cards.add(new Card(j, suits.get(i), 0, i, j));
      }
    }
  }

  void shuffleCards(Random r) {

    for (int i = 0; i < cards.size(); i++) {
      int rand = r.nextInt(52);
      Card temp = cards.get(i);
      cards.set(i, cards.get(rand));
      cards.set(rand, temp);
    }
  }

  void flipCards() {
    for (Card c : cards) {
      c.flipCard();
    }
  }

  ArrayList<ArrayList<Card>> createGrid(ArrayList<Card> deck) {
    ArrayList<ArrayList<Card>> grid = new ArrayList<ArrayList<Card>>(4);
    for (int i = 0; i < 4; i++) {
      ArrayList<Card> row = new ArrayList<Card>(13);
      for (int j = 0; j < 13; j++) {
        deck.get(13 * i + j).changePosition(i, j);
        row.add(deck.get(13 * i + j));

      }
      grid.add(row);
    }
    return grid;
  }

  void drawBoard(WorldScene aScene, int x, int y, int width, int height) {
    ArrayList<ArrayList<Card>> grid = this.createGrid(cards);

    int xPos = x;
    int yPos = y;

    for (int i = 0; i < grid.size(); i++) {
      for (int j = 0; j < grid.get(i).size(); j++) {
        grid.get(i).get(j).drawCard(aScene, xPos + j * 50, yPos + i * 80);
      }
    }
    aScene.placeImageXY(new TextImage("Score: " + score.toString(), Color.BLACK), width - 100, height - 50);
    if (seconds < 10) {
      aScene.placeImageXY(new TextImage("Time: " + minutes + ":" + "0" + seconds, Color.BLACK), 100, height - 50);
    }
    else {
      aScene.placeImageXY(new TextImage("Time: " + minutes + ":" + seconds, Color.BLACK), 100, height - 50);
    }
  }
  
  void drawEndScene(WorldScene aScene, int width, int height) {
    aScene.placeImageXY(new TextImage("GAME OVER", Color.BLACK), width/2, height/2);
    aScene.placeImageXY(new TextImage("Score: " + score.toString(), Color.BLACK), width/2, height/2 + 50);
    if (seconds < 10) {
      aScene.placeImageXY(new TextImage("Time: " + minutes + ":" + "0" + seconds, Color.BLACK), width/2, height - 100);
    }
    else {
      aScene.placeImageXY(new TextImage("Time: " + minutes + ":" + seconds, Color.BLACK), width/2, height - 100);
    }
  }
  
  void checkScore () {
    score = this.numberLeft() / 2;
  }
  
  void addTime() {
    if (seconds >= 59) {
      minutes++;
      seconds = 0;
    }
    else {
      seconds++;
    }
  }

  void findAndFlip(Posn pos) {
    int row = -1;
    int column = -1;
    for (int i = 0; i < 4; i++) {
      if (pos.y > (10 + i * 80) && pos.y < (70 + i * 80)) {
        row = i;
      }
    }
    for (int i = 0; i < 13; i++) {
      if (pos.x > (18 + i * 50) && pos.x < (62 + i * 50)) {
        column = i;
      }
    }
    this.findAndFlipHelper(row, column);
  }

  void findAndFlipHelper(int row, int column) {
    for (Card c : cards) {
      if (c.positionEquals(row, column)) {
        if (c.state == 0 && this.numberUp() < 2) {
          c.flipCard();
        }
      }
    }
  }

  void allDown() {
    for (Card c : cards) {
      if (c.state == 1) {
        c.state = 0;
      }
    }
  }

  int numberUp() {
    int faceUps = 0;
    for (Card c : cards) {
      if (c.state == 1) {
        faceUps++;
      }
    }
    return faceUps;
  }
  
  int numberLeft() {
    int cardsLeft = 0;
    for (Card c : cards) {
      if (c.state != 2) {
        cardsLeft++;
      }
    }
    return cardsLeft;
  }

  void removeIfSame() {
    ArrayList<Integer> indexOfUps = new ArrayList<Integer>();
    for(int i = 0; i < this.cards.size(); i++) {
      if(this.cards.get(i).state == 1) {
        indexOfUps.add(i);
      }
    }
    
    if(indexOfUps.size() > 1) {
      if(this.cards.get(indexOfUps.get(0)).sameValue(this.cards.get(indexOfUps.get(1)))) {
        this.cards.get(indexOfUps.get(0)).hideCard();
        this.cards.get(indexOfUps.get(1)).hideCard();
      }
    }
    
    this.allDown();
  }

}

class Game extends World {
  int width;
  int height;
  Random r;
  Board b;
  int ticks1;
  int ticks2;

  /* Template:
   *  Fields:
   *    this.width ... -- int
   *    this.height ... -- int
   *    this.r ... -- random
   *    this.b ... -- board
   *  Methods:
   *    this.makeScene() ... -- WorldScene
   *    this.onKeyEvent() ... -- void
   *  Methods of Fields:
   *    this.b.shuffleCards(Random) ... -- void
   *    this.b.flipCards() ... -- void
   *    this.b.createGrid() ... -- ArrayList<ArrayList<Card>>
   *    this.b.drawBoard(WorldScene, int, int, int, int) ... -- void 
   */

  Game(int width, int height, Random r, int ticks1, int ticks2) {
    this.width = width;
    this.height = height;
    this.r = r;
    this.ticks1 = ticks1;
    this.ticks2 = ticks2;
    b = new Board();
    b.shuffleCards(this.r);
  }

  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    this.b.drawBoard(scene, 40, 40, width, height);

    return scene;
  }

  public void onKeyEvent(String key) {
    if (key.equals(" ")) {
      b.flipCards();
    }
    if (key.equals("r")) {
      //create new world scene
    }
  }

  public void onMouseReleased(Posn pos) {
    b.findAndFlip(pos);
  }

  public void onTick() {
    if (this.ticks1 == 5) {
      b.addTime();
      this.ticks1 = 0;
    }
    else {
      this.ticks1++;
    }
    
    if (b.numberUp() == 2) {
      if (this.ticks2 == 10) {
        b.removeIfSame();
        b.checkScore();
        this.ticks2 = 0;
      }
      else {
        this.ticks2++;
      }
    }
  }
  

  public WorldEnd worldEnds() {
    if (b.numberLeft() == 0) {
      return new WorldEnd(true, this.lastScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
  
  public WorldScene lastScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    this.b.drawEndScene(scene, width, height);

    return scene;
  }
  

}

class ExamplesConcentration {
  Random r;
  Card AceOfSpade;
  Board b;

  void initData() {
    r = new Random(1);
    AceOfSpade = new Card(1, "♠", 0, 0, 0);
    b = new Board();
  }

  void testFlip(Tester t) {
    this.initData();

    t.checkExpect(this.AceOfSpade.state, 0);

    this.AceOfSpade.flipCard();

    t.checkExpect(this.AceOfSpade.state, 1);

    t.checkExpect(this.b.cards.get(0).state, 0);

    b.flipCards();

    t.checkExpect(this.b.cards.get(0).state, 1);
  }

  void testShuffle(Tester t) {
    this.initData();

    t.checkExpect(this.b.cards.get(0), new Card(1, "♣", 0, 0, 0));

    b.shuffleCards(r);

    t.checkExpect(this.b.cards.get(0), new Card(2, "♣", 0, 0, 0));
  }

  void testConcentration(Tester t) {
    this.initData();
    int width = 700;
    int height = 400;
    Game newGame = new Game(width, height, this.r, 0, 0);
    double tickRate = 0.2;

    newGame.bigBang(width, height, tickRate);
  }
}
