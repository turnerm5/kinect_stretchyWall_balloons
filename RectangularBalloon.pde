class RectangularBalloon extends Balloon {
	
	RectangularBalloon(float tempM, float tempX, float tempY, color tempFillColor) {
  	super(tempM, tempX, tempY, tempFillColor);
  }

	void display() {
    
    
    if (opacity > 80){
      opacity -= 2;
    }
    
    fill(balloonColor, opacity);
    
    stroke(255,20);
    rectMode(CENTER);
    rect(location.x, location.y, size, size);
    
  }
  
}