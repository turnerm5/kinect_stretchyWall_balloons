// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan
// http://www.shiffman.net
// https://github.com/shiffman/libfreenect/tree/master/wrappers/java/processing

import org.openkinect.*;
import org.openkinect.processing.*;

// Showing how we can farm all the kinect stuff out to a separate class
KinectTracker tracker;
// Kinect Library object
Kinect kinect;

Balloon[] balloons = new Balloon[100];
color fillColor = color(165,33,26);

void setup() {
  size(640,480);
  kinect = new Kinect(this);
  tracker = new KinectTracker();

  for (int i = 0; i < balloons.length; i++) { 
    balloons[i] = new Balloon(random(200, 400.0),random(50, width - 50),random(50, height - 50), fillColor);
  }
}

void draw() {
  background(255);

  // Run the tracking analysis
  tracker.track();
  
  // Let's draw the raw location
  PVector v1 = tracker.getPos();

  //for every balloon 
  for (int i = 0; i < balloons.length; i++) {
    
    //add friction to our world, so they don't bounce around forever
    float c = 0.8;
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

void keyPressed() {
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

void stop() {
  tracker.quit();
  super.stop();
}