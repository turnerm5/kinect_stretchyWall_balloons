class KinectTracker {
  // Size of kinect image
  int kw = 640;
  int kh = 480;
  
  //depth threshold
  int threshold = 765;
  
  //how hard is someone pushing into the screen?
  float force;
  
  //Are we tracking something?
  boolean tracking;

  // location of tracked point
  PVector loc;

  // Depth data
  int[] depth;
  //track where we found the deepest value
  int deepX;
  int deepY;

  //how much does the kinect tilt?
  float deg = 0;

  //be able to get our display pixels
  PImage display;

  // correct for the kinect and projector alignment
  int leftCorrection;
  int rightCorrection;
  int topCorrection;
  int bottomCorrection;

  KinectTracker() {
    kinect.start();
    kinect.enableDepth(true);
    kinect.tilt(deg);

    // We could skip processing the grayscale image for efficiency
    // but it helps for debugging the threshold
    kinect.processDepthImage(true);
    display = createImage(kw,kh,PConstants.RGB);
    loc = new PVector(0,0);
  }

  void track() {

    // Get the raw depth as array of integers
    depth = kinect.getRawDepth();

    // Being overly cautious here
    if (depth == null) return;

    //reset our closest depth
    int depthMax = 99999;
    
    //default to false, unless we're tracking something
    tracking = false;    

    //for every value in the Kinect depth array.
    for(int x = 0; x < kw; x++) {
      for(int y = 0; y < kh; y++) {
        
        // Mirror the image
        int offset = kw-x-1+y*kw;
        
        // Grab the raw depth value
        int rawDepth = depth[offset];
        
        // Test against threshold
        if (rawDepth < threshold) {
          //if we found something, we're tracking!
          tracking = true;
          
          //if it's the closest value, remember it, and its coordinates
          if (rawDepth < depthMax) {
            depthMax = rawDepth;
            deepY = y;
            deepX = x;
          }
        }
      }
    }

    // If we found something
    if (tracking) {
      
      //correct the location point for the misalignment of the kinect and proj.
      //should we be correcting the whole image, not just the point?
      int correctedY = (int)map(deepY, offsets[0], offsets[1], 0, kh);
      int correctedX = (int)map(deepX, offsets[2], offsets[3], 0, kw);
      
      //save the location, corrected to the screen
      loc = new PVector(correctedX,correctedY);    
    }
  }

  PVector getPos() {
    loc.x = map(loc.x,0,kw,0,width);
    loc.y = map(loc.y,0,kh,0,height);
    return loc;
  }

  void display() {
    
    // Being overly cautious here
    if (depth == null) return;

    // Going to rewrite the depth image to show which pixels are in threshold
    // A lot of this is redundant, but this is just for demonstration purposes
    
    //Always reload the pixels
    display.loadPixels();
    
    for(int x = 0; x < kw; x++) {
      for(int y = 0; y < kh; y++) {
        // mirroring image
        int offset = kw-x-1+y*kw;
        // Raw depth
        int rawDepth = depth[offset];

        //What is the index of the array?
        int pix = x+y*display.width;

        if (rawDepth < threshold) {
          // A red color
          display.pixels[pix] = color(150,50,50);
        } 
        else {
          //A dark gray
          display.pixels[pix] = color(100);
        }
      }
    }
    
    //Always update the pixels at the end
    display.updatePixels();

    // Draw the image
    image(display,0,0);
  }

  void quit() {
    kinect.quit();
  }

  int getThreshold() {
    return threshold;
  }

  void setThreshold(int t) {
    threshold = t;
  }

  //remaps the force, so when you push in more, the force returned is greater
  float getForce(){
    
    //what is the range of forces that are allowed?
    int minForce = 200;
    int maxForce = 600;
    
    // how far past the trigger threshold can someone push in?
    int distancePastThreshold = 70;

    force = constrain(
      map(
        force, 
        0, distancePastThreshold, 
        minForce, maxForce
      ),
      minForce,maxForce
    );
    
    return force;
  }

  //be able to adjust our corrections
  void setOffset(int offsetIndex, int offsetChange){
    offsets[offsetIndex] += offsetChange;
    println("offsets[" + offsetIndex + "]:" + offsets[offsetIndex] );  
  }

}