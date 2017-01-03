public class Check{
    Robot robot;
    public Check(){
    }

    public void correctTheAngle(){
    		float angle;
    		Robot.sampleProvider.fetchSample(Robot.sample, 0);
        	angle = Robot.sample[0];
        	Robot.pilot.setAngularAcceleration(40);
        	Robot.pilot.setAngularSpeed(10); // robot should turn slowly in order to stop in time.
        	int wantedAngle = (int)(angle + (angle > 0 ? 45 : -45)) / 90 * 90; // the angle that robot should be at.
        	System.out.println("check error: " + angle + " wantedAngle: " + wantedAngle);
        	if(wantedAngle < angle)		// turns robot to that angle.
        		Robot.turnLeft(wantedAngle);
        	else if(wantedAngle > angle)
        		Robot.turnRight(wantedAngle);
        	Robot.pilot.setAngularAcceleration(15);	// robot's speed turns normal.
        	Robot.pilot.setAngularSpeed(80);
    }
}