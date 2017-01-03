import java.util.LinkedList;
import java.util.Queue;

public class Customer{
    public int x, y;
    public int distance[][] = new int[Map.MAP_SIZE][Map.MAP_SIZE];
    
    public Customer(int x, int y)
    {
    	this.x = x;
    	this.y = y;
    }
    
    
    void findDistances(){
    	Queue<Pair> queue = new LinkedList<Pair>();
    	queue.add(new Pair(x,y));
    	distance[x][y]=1;
    	while(!queue.isEmpty())
    	{
    		Pair current = queue.poll();
    		for(int i=0;i<4;i++)
    		{
    			int nx=current.x+Direction.dir[i][0];
    			int ny=current.y+Direction.dir[i][1];
    			if(!Map.map[current.x][current.y].wall[i] 
    					&& nx>=0 
    					&& ny>=0 
    					&& nx<Map.cellLength 
    					&& ny<Map.cellLength 
    					&& distance[nx][ny] == 0
    					&& Map.map[nx][ny].color != 3
    					&& Map.map[nx][ny].color != -1){
    				distance[nx][ny] = distance[current.x][current.y]+1;
    				queue.add(new Pair(nx,ny));    				
    			}
    		}
    	}
    }
}