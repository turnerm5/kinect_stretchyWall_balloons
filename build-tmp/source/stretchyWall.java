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

// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan
// http://www.shiffman.net
// https://github.com/shiffman/libfreenect/tree/master/wrappers/java/processing




// Showing how we can farm all the kinect stuff out to a separate class
KinectTracker tracker;
// Kinect Library object
Kinect kinect;

Balloon[] balloons = new Balloon[100];
int fillColor = color(165,33,26);

public void setup() {
  size(640,480);
  kinect = new Kinect(this);
  tracker = new KinectTracker();

  for (int i = 0; i < balloons.length; i++) { 
    balloons[i] = new Balloon(random(200, 400.0f),random(50, width - 50),random(50, height - 50), fillColor);
  }
}

public void draw() {
  background(255);

  // Run the tracking analysis
  tracker.track();
  tracker.display();
  
  // Let's draw the raw location
  PVector v1 = tracker.getPos();
  
  //for every balloon 
  for (int i = 0; i < balloons.length; i++) {
    
    //add friction to our world, so they don't bounce around forever
    float c = 0.8f;
    PVector friction = balloons[i].velocity.get();
    friction.mult(-1);
    friction.normalize();
    friction.mult(c);
    

    balloons[i].applyForces(friction);
    
    if (tracker.repelling()){
      balloons[i].repel(v1); 
    }
    

    balloons[i].update();
    balloons[i].checkEdges();
    balloons[i].display();

  }

}

public void keyPressed() {
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
  
  
  public void repel(PVector finger) {
    
    PVector mouse = finger.get();
    mouse.sub(location);
    float distance = mouse.mag();
    distance = constrain(distance, 25, 500);
    //change the number here for the gravitational constant
    float grav = (-1 * mass) / ( distance * distance);
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
class KinectTracker {

  // Size of kinect image
  int kw = 640;
  int kh = 480;
  int threshold = 635;

  // Raw location
  PVector loc;

  // Interpolated location
  PVector lerpedLoc;

  Boolean repelling = false;

  // Depth data
  int[] depth;


  PImage display;

  KinectTracker() {
    kinect.start();
    kinect.enableDepth(true);
    float deg = 0;
    kinect.tilt(deg);

    // We could skip processing the grayscale image for efficiency
    // but this example is just demonstrating everything
    kinect.processDepthImage(true);

    display = createImage(kw,kh,PConstants.RGB);

    loc = new PVector(0,0);
    lerpedLoc = new PVector(0,0);
  }

  public void track() {

    repelling = false;

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
          repelling = true;
          sumX += x;
          sumY += y;
          count++;
        }
      }
    }
    // As long as we found something
    if (count != 0) {
      loc = new PVector(sumX/count,sumY/count);
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

  public Boolean repelling() {
    if (repelling) {
      return true;
    } else {
      return false;
    }
  }

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
