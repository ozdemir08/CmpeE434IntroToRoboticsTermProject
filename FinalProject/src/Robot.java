import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.LayoutStyle;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Robot{
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	static GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
	
	static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	static EV3MediumRegulatedMotor mediumMotor = new EV3MediumRegulatedMotor(MotorPort.B);
	
	static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
	static ColorAdapter colorAdapter = new ColorAdapter(colorSensor);
	static NXTUltrasonicSensor rightUltrasonic = new NXTUltrasonicSensor(SensorPort.S1);  // kontrol edilecek
	static NXTUltrasonicSensor leftUltrasonic = new NXTUltrasonicSensor(SensorPort.S3); 	/// kontrol edilecek.

	static MovePilot pilot;
	static float trackWidth = 11.7f;
	static Boolean reverse = false;
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);
	static SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
	static SampleProvider rightProvider = rightUltrasonic.getDistanceMode();
	static SampleProvider leftProvider = leftUltrasonic.getDistanceMode();
    static float [] sample = new float[sampleProvider.sampleSize()];
	static float [] rightSample = new float[rightProvider.sampleSize()];
	static float [] leftSample = new float[leftProvider.sampleSize()];
	public static Customer customers[] = new Customer[2];
	static DataOutputStream dataOutputStream;
	Socket client;
	
    int x, y, dir, lastGyroReset;
    int keptBall; // { 0 : 'Nothing', 1 : 'Red', 2 : 'Blue'}
    Check check;

    public Robot(Socket client) throws Exception{
        this.x = Map.MAP_SIZE / 2; 
        this.y = Map.MAP_SIZE / 2; 
        this.dir = 0;
        check = new Check();
        keptBall = 0;
		this.client = client;
		OutputStream outputStream = client.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);

        Chassis chassis = new WheeledChassis(
				new Wheel[]{
						WheeledChassis.modelWheel(leftMotor,5.592).offset(-trackWidth/2).invert(reverse),
						WheeledChassis.modelWheel(rightMotor,5.607).offset(trackWidth/2).invert(reverse)}, 
				WheeledChassis.TYPE_DIFFERENTIAL);
		
    	pilot = new MovePilot(chassis);

    	pilot.setLinearSpeed(50);
    	pilot.setLinearAcceleration(15);
    	gyroSensor.reset();
    	pilot.setAngularAcceleration(15);
    	pilot.setAngularSpeed(80);
    	reset();
    }

    public void discoverMap() throws IOException{
    	getCellColorInformation(0);

    	if(Map.map[x][y].color == 3)
    		for(int i=0;i<4;i++)
    			Map.map[x][y].wall[i] = true;
    	else
    		getWallInformation();
    	
    	dataOutputStream.writeInt(x);
    	dataOutputStream.writeInt(y);
    	dataOutputStream.writeInt(Map.map[x][y].color);
    	for(int k=0;k<4;k++)
    		dataOutputStream.writeBoolean(Map.map[x][y].wall[k]);
    	dataOutputStream.flush();
    	if(Map.map[x][y].color == 3)
    		return;
    	
//    	resetGyroWithWallsIfNecessary();
    	
    	for(int i=0;i<4;i++)
    		System.out.print(Map.map[x][y].wall[i]);
    	
    	for(int i=0;i<4;i++){
    		int nx = x + Direction.dir[dir][0];
    		int ny = y + Direction.dir[dir][1];
    		if(!Map.map[x][y].wall[dir] && Map.map[nx][ny].color == -1){
    			updateXY(1);
    			forwardACell();
    			discoverMap();
    			backwardACell();
    			updateXY(-1);
    		}
    		rotateWithOdometry(90);
    		dir = (dir+1) % 4;
    	}
    }
    
    public void resetGyroWithWallsIfNecessary() {
		int angle = 0;
    	System.out.println("asdfafsafsd: " + lastGyroReset);
    	
    	if(lastGyroReset++ < 3) return;
    	
    	if(Map.map[x][y].wall[(dir+1)%4]){
    		System.out.println("ilk ifin icine girdi");
    		rightProvider.fetchSample(rightSample, 0);
        	float prevDistance, currentDistance = rightSample[0] * 100;
        	int i = 0;
        	do{
        		sampleProvider.fetchSample(sample, 0);
            	angle = (int)sample[0];
        		turnRight(angle+8);
        		prevDistance = currentDistance;
        		Delay.msDelay(300);
        		rightProvider.fetchSample(rightSample, 0);
            	currentDistance = rightSample[0] * 100;
            	System.out.println("current distance: " + currentDistance);
            	i++;
        	}while(prevDistance > currentDistance);
        	System.out.println("" + i);
        	if(i == 1){
        		i = 0;
        		do{
        			System.out.println("-8 donmeye basladi");
        			sampleProvider.fetchSample(sample, 0);
                	angle = (int)sample[0];
            		turnLeft(angle-8);
            		System.out.println("-8 dondu");
            		prevDistance = currentDistance;
            		Delay.msDelay(300);
            		rightProvider.fetchSample(rightSample, 0);
                	currentDistance = rightSample[0] * 100;
                	i++;
            	}while(prevDistance > currentDistance);  
        		sampleProvider.fetchSample(sample, 0);
            	angle = (int)sample[0];
        		turnRight(angle+4);
        	}
        	else
        	{
        		sampleProvider.fetchSample(sample, 0);
        		angle = (int)sample[0];
        		turnLeft(angle-4);
        	}
        	System.out.println("afsdfas");
    	}
    	System.out.println("reset gyro bitti");
	}

	private void updateXY(int sign){
    	x += Direction.dir[dir][0] * sign;
    	y += Direction.dir[dir][1] * sign;
    }
    
    private int getCellColorInformation(int flag){

    	mediumMotor.rotate(-70);
    	int colorID = colorAdapter.getColorID();
    	System.out.println("" + x + " " + y);
    	int color = -1;
    	
    	if(colorID == Color.WHITE)
    		color = 0;
    	if(colorID == Color.RED)
    		color = 1;    	
    	if(colorID == Color.BLUE)
    		color = 2;
    	if(colorID == Color.BLACK)
    		color = 3;
    	
    	if(flag == 0)
    		Map.map[x][y].color = color;
    	System.out.println(color);
    	mediumMotor.rotate(70);
    	return color;
    }
    private void getWallInformation(){
    	float leftDistance, rightDistance, difference;
    	int maxDistance = 25;
    	
    	for(int i=0;i<2;i++){
	    	leftProvider.fetchSample(leftSample, 0);
	    	leftDistance = leftSample[0] * 100;
	    	
	    	rightProvider.fetchSample(rightSample, 0);
	    	rightDistance = rightSample[0] * 100;
	    	System.out.println(" " + i + "-> left: " + leftDistance + "\tright: " + rightDistance);
	    	
	    	if(leftDistance < maxDistance && rightDistance < maxDistance)
	    		difference = rightDistance % Map.cellLength - leftDistance % Map.cellLength;
	    	else if(leftDistance < maxDistance)
	    		difference = Map.cellLength - 2 * (leftDistance % Map.cellLength) - 12;
	    	else if(rightDistance < maxDistance)
	    		difference = 2 * (rightDistance % Map.cellLength) - Map.cellLength + 12;
	    	else 
	    		difference = 0;
	    	
	    	System.out.println(difference);
	    		
	    	if(rightDistance < 30)
	    		Map.map[x][y].wall[(dir+1+i)%4] = true;
	    	if(leftDistance < 30)
	    		Map.map[x][y].wall[(dir+3+i)%4] = true;
	    	
	    	rotateWithOdometry(90 * (i == 0 ? 1 : -1));
	    	if(difference != 0){
	    		pilot.travel((i == 0 ? 1 : -1) * difference / 2);
	    		check.correctTheAngle();
	    	}
    	}
    }
    
    private Cell getCellForLocalization(){
    	Cell cell = new Cell();
    	float leftDistance, rightDistance, difference;
    	int maxDistance = 25;
    	
    	for(int i=0;i<2;i++){
	    	leftProvider.fetchSample(leftSample, 0);
	    	leftDistance = leftSample[0] * 100;
	    	
	    	rightProvider.fetchSample(rightSample, 0);
	    	rightDistance = rightSample[0] * 100;
	    	System.out.println(" " + i + "-> left: " + leftDistance + "\tright: " + rightDistance);
	    	
	    	if(leftDistance < maxDistance && rightDistance < maxDistance)
	    		difference = rightDistance % Map.cellLength - leftDistance % Map.cellLength;
	    	else if(leftDistance < maxDistance)
	    		difference = Map.cellLength - 2 * (leftDistance % Map.cellLength) - 12;
	    	else if(rightDistance < maxDistance)
	    		difference = 2 * (rightDistance % Map.cellLength) - Map.cellLength + 12;
	    	else 
	    		difference = 0;
	    	
	    	System.out.println(difference);
	    		
	    	if(rightDistance < 30)
	    		cell.wall[(1+i)%4] = true;
	    	if(leftDistance < 30)
	    		cell.wall[(3+i)%4] = true;
	    	
	    	rotateWithOdometry(90 * (i == 0 ? 1 : -1));
	    	if(difference != 0){
	    		pilot.travel((i == 0 ? 1 : -1) * difference / 2);
	    		check.correctTheAngle();
	    	}
    	}
    	return cell;
    }
    
    public void rotateWithOdometry(int angle){
    	pilot.rotate(angle);
		check.correctTheAngle();
    }

    public void localize() throws IOException{
        // robot arranges its direction and centers the cell.
    	ArrayList<Pair> poss = new ArrayList<>(), newPoss = new ArrayList<>();
    	for(int i=0;i<9;i++)
    		for(int j=0;j<9;j++)
    			if(Map.map[i][j].color != -1)
    				for(int k=0;k<4;k++)
    					newPoss.add(new Pair(i,j,k));
    	
    	do{
    		poss = newPoss;
    		newPoss = new ArrayList<>();
    		Cell cell = getCellForLocalization(); // robotun su anki celli
    		cell.color = getCellColorInformation(1);
    		
    		System.out.println("cell:");
    		for(int i=0;i<4;i++)
    			System.out.println(cell.wall[i]);
    		System.out.println();
    		
    		for(int i=0;i<poss.size();i++){
    			Cell c = Map.map[poss.get(i).x][poss.get(i).y]; // su anki tahmin cell
    			if(cell.color != c.color) continue;
    			int k = 0;
    			for(k = 0;k<4;k++)
    				if(cell.wall[k] != c.wall[(k + poss.get(i).dir)%4])
    					break;
    			if(k < 4) continue;
    			newPoss.add(poss.get(i));
    		}
    		
    		sendAllMaptoData();
    		for(int i=0;i<newPoss.size();i++){
    			Pair p = newPoss.get(i);
    			dataOutputStream.writeInt(p.x);
	        	dataOutputStream.writeInt(p.y);
	        	dataOutputStream.writeInt(5);
	        	System.out.println("" + i + ": " + p.x + " " + p.y + " " + 5);
	        	for(int k=0;k<4;k++)
	        		dataOutputStream.writeBoolean(Map.map[p.x][p.y].wall[k]);
	        	dataOutputStream.flush();
	        	Delay.msDelay(500);
    		}
    			
    		
    		if(newPoss.size() <= 1) 
    			break; 
    		
    		for(int i=0;i<4;i++)    			
    			if(!cell.wall[i]){
    				System.out.println("" + i + "'de dondu yapti m.l");
    				if(i == 1 || i == 2)
    					rotateWithOdometry(90 * i);
    				else if(i == 3)
    					rotateWithOdometry(-90);
    				
    				forwardACell();
    				for(int j=0;j<newPoss.size();j++){
    					Pair c = newPoss.get(j);
    					if(Map.map[c.x][c.y].wall[c.dir + i]){
    						newPoss.remove(j);
    						j--;
    					}
    					else{
    						newPoss.get(j).x += Direction.dir[c.dir + i][0];
    						newPoss.get(j).y += Direction.dir[c.dir + i][1];
    						newPoss.get(j).dir = (c.dir + i + 4) % 4;    						
    					}    					
    				}
    				break;
    			}
    				
    	}while(newPoss.size() > 1);
    }

    public void findBalls(){
        // finds the balls by DFS and calls getBall();
        pilot.travel(Map.cellLength, true);
        int color_id;
        do{
        	color_id = colorAdapter.getColorID();
        }while(color_id != Color.RED && color_id != Color.BLUE);
        if(color_id == Color.RED)
        	keptBall = 1;
        else
        	keptBall = 2;
        
        mediumMotor.rotate(-90);
        x += Direction.dir[dir][0];
        y += Direction.dir[dir][1];
    }

    public void getBall(int ball){
    	float distanceTraveled = pilot.getMovement().getDistanceTraveled(); 
		keptBall = ball;
		pilot.stop();
		mediumMotor.rotate(-40);
		pilot.travel(3);
		mediumMotor.rotate(-50);
		pilot.travel(-distanceTraveled-3);
		mediumMotor.rotate(90);
		pilot.forward();	
    }

    public void leftBall(){
        // robot left ball by changing front side.
    }

    public void goToCustomer(int color) throws IOException{
    	
    	int tx = x, ty = y;
    	ArrayList<Pair> path = new ArrayList<>();
    
//    	path.add(new Pair(tx, ty));
    	while(customers[color-1].distance[tx][ty] != 1){
    		System.out.println("x: y:  " + tx + " " + ty);
    		for(int i=0;i<4;i++){
    			int nx = tx + Direction.dir[i][0];
    			int ny = ty + Direction.dir[i][1];
    			if(!Map.map[tx][ty].wall[i] && nx >= 0 && nx < Map.cellLength && ny >= 0 && ny < Map.cellLength && 
    					customers[color-1].distance[tx][ty] == customers[color-1].distance[nx][ny] + 1){
    				path.add(new Pair(tx, ty, i));
    				tx = nx;
    				ty = ny;
    			}    				
    		}
    	}
    	path.add(new Pair(tx, ty));
    	
    	System.out.println("Path size: " + path.size());
    	for(int i=0;i<path.size();i++){
//    		Map.map[path.get(i).x][path.get(i).y].color = color;
    		dataOutputStream.writeInt(path.get(i).x);
        	dataOutputStream.writeInt(path.get(i).y);
        	dataOutputStream.writeInt(color);
        	for(int k=0;k<4;k++)
        		dataOutputStream.writeBoolean(Map.map[path.get(i).x][path.get(i).y].wall[k]);
        	dataOutputStream.flush();
        	Delay.msDelay(500);
    	}    
    	
    	for(int i=0;i<path.size()-1;i++){
    		int difference = (path.get(i).dir - dir + 4) % 4; 
//        	System.out.println(path.get(i).dir);
        	if(difference == 1 || difference == 2){
        		rotateWithOdometry(difference * 90);
        		System.out.println("saga dondu: " + difference * 90);
        	}
        	else if(difference == 3){
        		rotateWithOdometry(-90);
        		System.out.println("sola dondu: " + 90);
        	}
        	
        	forwardACell();
        	System.out.println("ileri: ");
        	dir = path.get(i).dir;
        	x = path.get(i+1).x;
        	y = path.get(i+1).y;
        	System.out.println("" + x + " " + y + " " + dir);
    	}
    	mediumMotor.rotate(90);
    }

    public void forwardACell(){
		float angle;
		sampleProvider.fetchSample(sample, 0);
    	angle = sample[0];	
    	System.out.println(angle);
    	Delay.msDelay(100);
		pilot.travel(Map.cellLength);		
		check.correctTheAngle();    			
        //checkLocation();
    }
    
    public void backwardACell(){
		float angle;
		sampleProvider.fetchSample(sample, 0);
    	angle = sample[0];	
    	System.out.println(angle);
		Delay.msDelay(100);
		pilot.travel(-Map.cellLength);		
		check.correctTheAngle();    	
        //checkLocation();
    }
    
	static void turnLeft(int wantedAngle){
		sampleProvider.fetchSample(sample, 0);
    	float angle = sample[0];
		System.out.println("TURN LEFT " + angle);    	
    	
		pilot.rotate(185, true);

		while(angle > wantedAngle){
			sampleProvider.fetchSample(sample, 0);
	    	angle = sample[0];	    	
		}
		System.out.println("TURN LEFT cikis " + angle);
		pilot.stop();
		Delay.msDelay(100);
	}
	
	static void turnRight(int wantedAngle){
		
		sampleProvider.fetchSample(sample, 0);
    	float angle = sample[0];
    	System.out.println("TURN RIGHT " + angle);

		pilot.rotate(-185, true);

		while(angle < wantedAngle){
			sampleProvider.fetchSample(sample, 0);
	    	angle = sample[0];	    	
		}
		pilot.stop();
		Delay.msDelay(100);
		System.out.println("TURN RIGHT cikis " + angle);
	}
    
	void getBallTest(){
		
		while(Button.readButtons() != Button.ID_ESCAPE){
			Color colors = colorAdapter.getColor();
			int red = colors.getRed(), blue = colors.getBlue(), green = colors.getGreen();
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Color Sensor: " + red, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+blue, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+green, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+pilot.getMovement().getDistanceTraveled(), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 60, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			
			if(red > 10){
				getBall(1);
			}
			
			if(blue > 10){
				getBall(2);
			}
			
			
		}
	}
	
	public void reset() throws IOException{
		for(int i=0;i<Map.MAP_SIZE;i++)
			for(int j=0;j<Map.MAP_SIZE;j++)
				Map.map[i][j] = new Cell();
        customers = new Customer[2];
        sendAllMaptoData();
        gyroSensor.reset();
        lastGyroReset = 4;
        x = 4;
        y = 4;
        dir = 0;
	}

	public void saveMap() {
		try{
		    PrintWriter writer = new PrintWriter("map.txt", "UTF-8");
		    for(int i=0;i<9;i++)
		    	for(int j=0;j<9;j++){
		    		writer.print("" + i + " " + j + " " + Map.map[i][j].color + " ");
		    		for(int k=0;k<4;k++)
		    			writer.print(Map.map[i][j].wall[k] + " ");
		    		writer.println();
		    	}
		    writer.close();
		} catch (IOException e) {
		   // do something
			System.out.println("saveMap yazamiyorum dunyaya");
		}		
	}
	
	public void loadMap() throws IOException{
		Scanner in = new Scanner(new FileReader("map.txt"));
		for(int i=0;i<9;i++)
	    	for(int j=0;j<9;j++){
	    		int x = in.nextInt(), y = in.nextInt();
	    		Map.map[x][y].color = in.nextInt();
	    		for(int k=0;k<4;k++)
	    			Map.map[x][y].wall[k] = in.nextBoolean();
	    		
	    	}
		sendAllMaptoData();
		in.close();
	}
	
	private void sendAllMaptoData() throws IOException{
		for(int i=0;i<9;i++)
			for(int j=0;j<9;j++){
	    		dataOutputStream.writeInt(i);
	        	dataOutputStream.writeInt(j);
	        	dataOutputStream.writeInt(Map.map[i][j].color);
	        	for(int k=0;k<4;k++)
	        		dataOutputStream.writeBoolean(Map.map[i][j].wall[k]);
	        	dataOutputStream.flush();
			}
	}
}