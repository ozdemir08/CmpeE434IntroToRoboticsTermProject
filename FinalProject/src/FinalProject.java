import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FinalProject 
{	
    public static Robot robot;
    static ServerSocket serverSocket;
    static Socket client;
    
	public static void main(String[] args) throws Exception{
		
		serverSocket = new ServerSocket(1234);
		System.out.println("Waiting for computer");
		client = serverSocket.accept();
		InputStream inputStream = client.getInputStream();
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		robot = new Robot(client); // Robot starts at center
		
		while(!client.isConnected());
		System.out.println("CONNECTED");
		while(client.isConnected()){
			if(dataInputStream.available() != 0){
				int command = dataInputStream.readInt();
				System.out.println("Okunan deger: " + command);
				if(command == 0){					
					robot.reset();
					robot.discoverMap();
				}
				else if(command == 1)
					robot.loadMap();
				else if(command == 2){
//					robot.resetGyroWithWallsIfNecessary();
					testGoOneCellAndGoToCustomer();
				}
				else if(command == 3)
					robot.reset();
				else if(command == 4){
					testGoOneCellAndGoToCustomer();		
				}
				else if(command == 5){
					robot.reset();
					robot.loadMap();
					robot.localize();
				}
				else
					System.out.println("Bu ne arkadasim");
			}
		}
        robot.discoverMap();
	}
	
	
	static void testGoOneCellAndGoToCustomer() throws IOException{
		robot.reset();
		robot.loadMap();
		robot.x=1;
		robot.y=1;
		robot.dir=0;
//		robot.keptBall=2; // Blue 2
		robot.findBalls();
		Robot.customers[0] = new Customer(0,0);
		Robot.customers[1] = new Customer(1,4);
		Robot.customers[robot.keptBall-1].findDistances();				
		robot.goToCustomer(robot.keptBall);
	}
}
