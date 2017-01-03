
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

class GridsCanvas extends Canvas {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int width, height;

    int rows;

    int cols;
    Cell map[][];

    GridsCanvas(int w, int h, Cell[][] map) {
        setSize(width = w, height = h);
        this.map = map;
        rows = 9;
        cols = 9;
    }

    public void paint(Graphics g) {
        width = 720;
        height = 720;
       
        Graphics2D g2 = (Graphics2D) g;

        for(int j=0;j<rows;j++) {
            for (int i = 0; i < cols; i++) {
                if(map[j][i].color == 0)
                    g.setColor(Color.white);
                else if(map[j][i].color == 1)
                    g.setColor(Color.red);
                else if(map[j][i].color == 2)
                    g.setColor(java.awt.Color.blue);
                else if(map[j][i].color == 3)
                    g.setColor(java.awt.Color.black);
                else if(map[j][i].color == 5)
                	g.setColor(Color.green);
                else
                    g.setColor(new Color(230, 230, 230));
                g.fillRect(i * width / cols + 5 * (i) + 2, j * height / rows + 5 * (j) + 2, width / cols - 5, height / rows - 5);

                int hr = height / rows, wc = width/cols;

                int[][] points = new int[][]{
                        {i * wc + 5 * i, j * hr + 5 * j},
                        {(i + 1) * wc + 5 * i, j * hr + 5 * j},
                        {(i + 1) * wc + 5 * i,(j+1)*hr + 5 * j},
                        {i * wc + 5 * i, (j+1) * hr + 5 * j}
                };

                g2.setStroke(new BasicStroke(5));
                if(map[j][i].color == -1) continue;
                for(int k=0;k<4;k++) {
                    if(map[j][i].wall[k] == true)
                        g.setColor(Color.black);
                    else
                        g.setColor(Color.white);
                    int x1 = points[k][0] + 5 * (k % 2 == 0 ? (k == 0 ? 1 : -1) : 0),
                    		y1 = points[k][1] + 5 * (k % 2 == 1 ? (k == 1 ? 1 : -1) : 0),
                    		x2 = points[(k+1)%4][0] + 5 * (k % 2 == 0 ? (k == 0 ? -1 : 1) : 0),
                    		y2 = points[(k+1)%4][1] + 5 * (k % 2 == 1 ? (k == 1 ? -1 : 1) : 0);
                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }
}