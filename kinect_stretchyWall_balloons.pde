import org.openkinect.*;
import org.openkinect.processing.*;

//turns off the Kinect sensing, uses the mouse as input
Boolean debugMode = false;

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
  background(255);

  //if we're not in debug mode
  if (!debugMode){
    
    // Run the tracking analysis
    tracker.track();
    PVector v1 = tracker.getPos();
    force = tracker.getForce();

    //for every balloon 
    for (int i = 0; i < balloons.length; i++) {
      if (tracker.tracking()){
        balloons[i].repel(v1, force); 
      }
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

}

//if we hit a key
void keyPressed() {
  
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
  if (debugMode){
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
