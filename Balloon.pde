class Balloon{
  
  PVector location;
  PVector velocity;
  PVector acceleration;
  float mass;
  float topspeed = 2;
  float size;
  int opacity;
  color balloonColor;
  
  Balloon(float tempM, float tempX, float tempY, color tempFillColor) {
    location = new PVector(tempX,tempY);
    velocity = new PVector(0,0);
    acceleration = new PVector(0,0);
    mass = tempM;
    size = tempM/6;
    balloonColor = tempFillColor;
    opacity = 80;
  }

  void run() {
    update();
    friction();
    checkEdges();
    display();
  }

  void friction() {
    float c = 0.8;
    PVector friction = velocity.get();
    friction.mult(-1);
    friction.normalize();
    friction.mult(c);
    applyForces(friction);
  }

  void applyForces(PVector force) {
    PVector f = force.get();
    f.div(mass);
    acceleration.add(f);
  }
  
  void update() {
    velocity.add(acceleration);
    velocity.limit(topspeed);
    location.add(velocity);
    acceleration.mult(0);
  }
  
  void display() {
    
    
    if (opacity > 80){
      opacity -= 2;
    }
    
    fill(balloonColor, opacity);
    
    noStroke();
    ellipse(location.x, location.y, size, size);
    
  }
  
  void checkEdges() {

    //slow down the balloons when they hit the wall
    float bounceFactor = .9;

    if (location.y < (size/2)) {
      location.y = (size/2); 
      velocity.y *= -bounceFactor;
      opacity = 180;
    } else if (location.y > height - (size/2)) {
      location.y = height - (size/2); 
      velocity.y *= -bounceFactor;
      opacity = 180;
    }
    
    if (location.x < (size/2)) {
      location.x = (size/2); 
      velocity.x *= -bounceFactor;
      opacity = 180;
    } else if (location.x > width- (size/2)) {
      location.x = width - (size/2); 
      velocity.x *= -bounceFactor;
      opacity = 180;
    }
  }
  
  
  void repel(PVector finger, float force) {
    PVector mouse = finger.get();
    mouse.sub(location);
    float distance = mouse.mag();
    distance = constrain(distance, 50, 800);
    //change the number here for the gravitational constant
    float grav = (force * direction * mass) / ( distance * distance );
    mouse.normalize();
    mouse.mult(grav);
    applyForces(mouse);
  }
  
  
  void changeColor() {
    if (balloonColor == color(0,150,219)){
      balloonColor = color(165,33,26);
    } else {
      balloonColor = color(0,150,219);
    }
  }
  
}