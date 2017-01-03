import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {

	static int MAP_SIZE = 9;
    static Cell[][] map = new Cell[MAP_SIZE][MAP_SIZE];
    static int width = 800, height = 800;
    static private Frame frame;
    static private Panel controlPanel;
    static Button discoverMap, loadMap, findBalls, reset, debug, localization;
    static GridsCanvas current; 
    
    static OutputStream outputStream;
    static DataOutputStream dataOutputStream;

	public static void main(String args[]) throws InterruptedException, Exception{
		for(int i=0;i<MAP_SIZE;i++)
            for(int j=0;j<MAP_SIZE;j++)
                map[i][j] = new Cell();

        setupGui();

        
        String ip = "10.0.1.1";
		
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");
		
		InputStream inputStream = socket.getInputStream();
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		
		outputStream = socket.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);
        
        while(socket.isConnected()){
        	Cell cell = new Cell();
        	if(dataInputStream.available() != 0){
        		int x = dataInputStream.readInt();
        		int y = dataInputStream.readInt();
        		cell.color = dataInputStream.readInt();
        		cell.wall[0] = dataInputStream.readBoolean();
        		cell.wall[1] = dataInputStream.readBoolean();
        		cell.wall[2] = dataInputStream.readBoolean();
        		cell.wall[3] = dataInputStream.readBoolean();
        		System.out.println(x + " " + y + " " + cell.color);
        		for(int i=0;i<4;i++)
        			System.out.println(cell.wall[i] + " ");
        		map[x][y] = cell;

            	GridsCanvas next = new GridsCanvas(width, height, map);
            	if(current != null)
            		frame.remove(current);
                frame.add(next);
                current = next;
        	}
        }
        System.out.println("Baglanti gitti");
	}

	private static void setupGui() {
		// TODO Auto-generated method stub
		frame = new Frame("DROGBAAAA");
		GridLayout frameGridLayout = new GridLayout(1, 2);
		frameGridLayout.setHgap(50);
        frame.setLayout(frameGridLayout);
        GridsCanvas current = new GridsCanvas(width, height, map);
        frame.setSize(1600, 800);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        
        
        frame.add(current);
        
        controlPanel = new Panel();
        GridLayout grid = new GridLayout(4, 2);
        grid.setVgap(40);
        controlPanel.setLayout(grid);
        controlPanel.setSize(10, 10);
        
        discoverMap = new Button("Discover Map");
        discoverMap.setPreferredSize(new Dimension(20, 20));
        discoverMap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Discover map started");
				sendDataToRobot(0);
			}
		});
        
        loadMap = new Button("Load Map");
        loadMap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Load map started");
				sendDataToRobot(1);
			}
		});
        
        findBalls = new Button("Find Balls");
        findBalls.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Find Balls started");
				sendDataToRobot(2);
			}
		});
        
        reset = new Button("Reset");
        reset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Reset");
				sendDataToRobot(3);
			}
		});
        
        debug = new Button("Debug");
        debug.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Debug");
				sendDataToRobot(4);
			}
		});
        
        localization = new Button("Localization");
        localization.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Localization");
				sendDataToRobot(5);
			}
		});
        
        controlPanel.add(discoverMap);
        controlPanel.add(loadMap);
        controlPanel.add(findBalls);
        controlPanel.add(reset);
        controlPanel.add(debug);
        controlPanel.add(localization);
        frame.add(controlPanel);
        frame.setVisible(true);
	}
	
	private static void sendDataToRobot(int command){
		try {
			dataOutputStream.writeInt(command);
			dataOutputStream.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Discover map exception");
		}
		
	}
}
