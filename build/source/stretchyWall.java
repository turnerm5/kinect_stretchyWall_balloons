import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.openkinect.*; 
import org.openkinect.processing.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class stretchyWall extends PApplet {




//turns off the Kinect sensing, uses the mouse as input
Boolean debugMode = false;

// Showing how we can farm all the kinect stuff out to a separate class
KinectTracker tracker;
// Kinect Library object
Kinect kinect;


Balloon[] balloons = new Balloon[150];
int fillColor = color(165,33,26);

int direction = 1;

public void setup() {
  size(1024,768);
  
  if (!debugMode){
    kinect = new Kinect(this);
    tracker = new KinectTracker();
  }
  
  for (int i = 0; i < balloons.length; i++) { 
    balloons[i] = new Balloon(random(200, 400.0f),random(50, width - 50),random(50, height - 50), fillColor);
  }
}

public void draw() {
  background(255);

  if (!debugMode){
    
    // Run the tracking analysis
    tracker.track();
    
    // Let's draw the raw location
    PVector v1 = tracker.getPos();
    float force = tracker.getForce();
    if (tracker.tracking) {
  
      fill(100);
      noStroke();
      ellipse(v1.x, v1.y, 20, 20);
    }

    //for every balloon 
    for (int i = 0; i < balloons.length; i++) {
      if (tracker.tracking){
        balloons[i].repel(v1, force); 
      }
      balloons[i].run();
    }

  }

  if (debugMode){
    PVector mouse = new PVector(mouseX, mouseY);
    fill(100);
    noStroke();
    ellipse(mouse.x, mouse.y, 20, 20);
    
    for (int i = 0; i < balloons.length; i++) {
      balloons[i].repel(mouse, .5f); 
      balloons[i].run();
    }
  }

}
public void keyPressed() {
  
  if (!debugMode){
    //make it easy to adjust our threshold
    int t = tracker.getThreshold();
    if (key == CODED) {
      if (keyCode == UP) {
        t+=5;
        tracker.setThreshold(t);
      } 
      else if (keyCode == DOWN) {
        t-=5;
        tracker.setThreshold(t);
      }
    }
  }
  //if we hit space, change the color and direction!
  if (key == ' ') {
    direction *= -1;
    for (int i = 0; i < balloons.length; i++) {
      balloons[i].changeColor();
    }
  }
}

public void stop() {
  tracker.quit();
  super.stop();
}
class Balloon{
  
  PVector location;
  PVector velocity;
  PVector acceleration;
  float mass;
  float topspeed = 2;
  float size;
  int opacity;
  int balloonColor;
  
  Balloon(float tempM, float tempX, float tempY, int tempFillColor) {
    location = new PVector(tempX,tempY);
    velocity = new PVector(0,0);
    acceleration = new PVector(0,0);
    mass = tempM;
    size = tempM/6;
    balloonColor = tempFillColor;
    opacity = 80;
  }

  public void run() {
    update();
    friction();
    checkEdges();
    display();
  }

  public void friction() {
    float c = 0.8f;
    PVector friction = velocity.get();
    friction.mult(-1);
    friction.normalize();
    friction.mult(c);
    applyForces(friction);
  }

  public void applyForces(PVector force) {
    PVector f = force.get();
    f.div(mass);
    acceleration.add(f);
  }
  
  public void update() {
    velocity.add(acceleration);
    velocity.limit(topspeed);
    location.add(velocity);
    acceleration.mult(0);
  }
  
  public void display() {
    
    
    if (opacity > 80){
      opacity -= 2;
    }
    
    fill(balloonColor, opacity);
    
    stroke(255,20);
    ellipse(location.x, location.y, size, size);
    
  }
  
  public void checkEdges() {

    if (location.y < (size/2)) {
      location.y = (size/2); 
      velocity.y *= -.8f;
      opacity = 180;
    } else if (location.y > height - (size/2)) {
      location.y = height - (size/2); 
      velocity.y *= -.8f;
      opacity = 180;
    }
    
    if (location.x < (size/2)) {
      location.x = (size/2); 
      velocity.x *= -.8f;
      opacity = 180;
    } else if (location.x > width- (size/2)) {
      location.x = width - (size/2); 
      velocity.x *= -.8f;
      opacity = 180;
    }
  }
  
  
  public void repel(PVector finger, float force) {
    
    PVector mouse = finger.get();
    mouse.sub(location);
    float distance = mouse.mag();
    distance = constrain(distance, 25, 500);
    //change the number here for the gravitational constant
    float grav = (force * direction * mass) / ( distance * distance);
    mouse.normalize();
    mouse.mult(grav);
    acceleration.add(mouse);
  }
  
  
  public void changeColor() {
    if (balloonColor == color(0,150,219)){
      balloonColor = color(165,33,26);
    } else {
      balloonColor = color(0,150,219);
    }
  }
  
}
//Thanks to Daniel Shiffman!

class KinectTracker {

  // Size of kinect image
  int kw = 640;
  int kh = 480;
  int threshold = 635;

  // Raw location
  PVector loc;

  // Interpolated location
  PVector lerpedLoc;

  Boolean tracking = false;

  // Depth data
  int[] depth;

  float force;

  PImage display;

  KinectTracker() {
    kinect.start();
    kinect.enableDepth(true);
    float deg = 0;
    kinect.tilt(deg);

    // We could skip processing the grayscale image for efficiency
    // but this example is just demonstrating everything
    kinect.processDepthImage(false);

    display = createImage(kw,kh,PConstants.RGB);

    loc = new PVector(0,0);
    lerpedLoc = new PVector(0,0);
  }

  public void track() {

    tracking = false;

    // Get the raw depth as array of integers
    depth = kinect.getRawDepth();

    // Being overly cautious here
    if (depth == null) return;

    float sumX = 0;
    float sumY = 0;
    float count = 0;

    for(int x = 0; x < kw; x++) {
      for(int y = 0; y < kh; y++) {
        // Mirroring the image
        int offset = kw-x-1+y*kw;
        // Grabbing the raw depth
        int rawDepth = depth[offset];

        // Testing against threshold
        if (rawDepth < threshold) {
          tracking = true;
          sumX += x;
          sumY += y;
          count++;
          force += (rawDepth - threshold);
        }
      }
    }
    // As long as we found something
    if (count != 0) {
      loc = new PVector(sumX/count,sumY/count);
      force = force / count;
      loc.x = map(loc.x,0,kw,0,width);
      loc.y = map(loc.y,0,kh,0,height);
    }

    // Interpolating the location, doing it arbitrarily for now
    lerpedLoc.x = PApplet.lerp(lerpedLoc.x, loc.x, 0.9f);
    lerpedLoc.y = PApplet.lerp(lerpedLoc.y, loc.y, 0.9f);
  }

  public PVector getLerpedPos() {
    return lerpedLoc;
  }

  public PVector getPos() {
    return loc;
  }

  public float getForce(){
    //we need to determine what the second number should be.
    force = constrain(map(force, 0, 60, 0, 2),0,2);
    return force;
  }


  //need to enable
  public void display() {
    
    PImage img = kinect.getDepthImage();

    // Being overly cautious here
    if (depth == null || img == null) return;

    // Going to rewrite the depth image to show which pixels are in threshold
    // A lot of this is redundant, but this is just for demonstration purposes
    display.loadPixels();
    for(int x = 0; x < kw; x++) {
      for(int y = 0; y < kh; y++) {
        // mirroring image
        int offset = kw-x-1+y*kw;
        // Raw depth
        int rawDepth = depth[offset];

        int pix = x+y*display.width;
        if (rawDepth < threshold) {
          // A red color instead
          display.pixels[pix] = color(150,50,50);
        } 
        else {
          display.pixels[pix] = img.pixels[offset];
        }
      }
    }

    display.updatePixels();

    // Draw the image
    image(display,0,0);

  }

  public void quit() {
    kinect.quit();
  }

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(int t) {
    threshold =  t;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "stretchyWall" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
