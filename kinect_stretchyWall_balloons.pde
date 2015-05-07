import org.openkinect.*;
import org.openkinect.processing.*;

//turns off the Kinect sensing, uses the mouse as input
Boolean debugMode = true;

//this will all be put in a separate class later
//what correction mode are we in?
Boolean correctionMode = false;
int currentMode = -1;
int[] offsets = {0,0,0,0};
String[] modes = {"Top", "Bottom", "Left", "Right"};

// Showing how we can farm all the kinect stuff out to a separate class
KinectTracker tracker;
// Kinect Library object
Kinect kinect;

//What is the inital mass of the mouse?
float force = 100;

//How many balloons are there?
Balloon[] balloons = new Balloon[150];

//What color are they
color fillColor = color(165,33,26);

//Should they chase the mouse, or run away? -1 = run away.
int direction = -1;

void setup() {
  size(1024,768);
  
  //if we're not in debug mode, initialize the Kinect
  if (!debugMode){
    kinect = new Kinect(this);
    tracker = new KinectTracker();
  }
  
  //create our balloons
  for (int i = 0; i < balloons.length; i++) { 
    float mass = random(200.0, 400.0);
    balloons[i] = new Balloon(
      mass,
      random(50, width - 50),
      random(50, height - 50), 
      fillColor);
  }
}

void draw() {
  background(240);


  

  //if we're not in debug mode
  if (!debugMode){
    
    // Run the tracking analysis
    tracker.track();
    
    //get the position of the point
    PVector v1 = tracker.getPos();
    force = tracker.getForce();

    //for every balloon 
    for (int i = 0; i < balloons.length; i++) {
      // only repel if we're tracking something
      if (tracker.tracking){
        balloons[i].repel(v1, force); 
      }
      //run the balloons
      balloons[i].run();
    }

  }

  

  //if we are in debug mode
  if (debugMode){
    PVector mouse = new PVector(mouseX, mouseY);
    fill(100);
    noStroke();
    ellipse(mouse.x, mouse.y, force/5, force/5);
    
    for (int i = 0; i < balloons.length; i++) {
      balloons[i].repel(mouse, force); 
      balloons[i].run();
    }
  }

  if (correctionMode){
    fill(25);
    text(modes[currentMode] + " Correction", 10, 20);
    text("Offset: " + offsets[currentMode], 10, 35);
  }
}



//if we hit a key
void keyPressed() {
  //if we hit c, toggle between correction mode
  if (key == 'c') {
    currentMode += 1;
    if (currentMode <= 3){
      correctionMode = true;
    }
    if (currentMode > 3) {
      currentMode = -1;
      correctionMode = false;
    }
  
  }

  if (correctionMode){
    if (key == CODED) {
      if (keyCode == UP || keyCode == RIGHT) {
        offsets[currentMode] += 1;
      } 
      else if (keyCode == DOWN || keyCode == LEFT) {
        offsets[currentMode] -= 1;
      }
    }
  }


  //make it easy to adjust our threshold
  if (!debugMode){
    int t = tracker.getThreshold();
    if (key == CODED) {
      if (keyCode == UP) {
        t+=1;
        println("t: "+t);
        tracker.setThreshold(t);
      } 
      else if (keyCode == DOWN) {
        t-=1;
        println("t: "+t);
        tracker.setThreshold(t);
      }
    }
  }
  
  
  //if we hit space, change the color and direction
  if (key == ' ') {
    direction *= -1;
    for (int i = 0; i < balloons.length; i++) {
      balloons[i].changeColor();
    }
  }

  
  //make it easy to adjust our force while debugging
  if (debugMode &&! correctionMode){
    if (key == CODED) {
      if (keyCode == UP) {
        force += 50;
        println("force: "+force);
      } 
      else if (keyCode == DOWN) {
        force -= 50;
        println("force: "+force);
      }
    }
  }


}

void stop() {
  tracker.quit();
  super.stop();
}
