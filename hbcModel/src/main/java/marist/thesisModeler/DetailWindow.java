package marist.thesisModeler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.json.JSONObject;

public class DetailWindow extends JFrame{
	
	static JSONObject obj;
	
	public DetailWindow(JSONObject obj) {
        super(obj.getString("filename"));
        this.obj = obj;
 
        getContentPane().setBackground(Color.WHITE);
        setSize(400, ((obj.getInt("linecount") * 2) + 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
	
	public void drawAdvancedView(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawRect(100, 50, 200, (obj.getInt("linecount") * 2));
		String[] ref = obj.getString("references").split(", ");
		ArrayList<String> references = new ArrayList<String>();
		ArrayList<Color> colors = new ArrayList<Color>();
		Random r = new Random();
	    //r.setSeed(0);
		
		if(!(ref[0].equals("[]"))) {
			boolean left = true;
			for(String s : ref) {
				String pointsTo = s.split(" ")[0];
				s = s.split(" ")[1];
				
				if(pointsTo.contains("[")) {
					pointsTo = pointsTo.substring(1, pointsTo.length());
				}
				if(s.contains("]")) {
					s = s.substring(0, s.length()-1);
				}
				
				if(!(references.contains(pointsTo))) {
					references.add(pointsTo);
					Color newc = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
					while(colors.contains(newc)) {
						newc = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
					}
					colors.add(newc);
				}
				
				int linenum = Integer.parseInt(s);
				linenum *= 2;
				linenum += 50;
				
				if(left) {
					g2d.setColor(colors.get(references.indexOf(pointsTo)));
					g2d.drawLine(5, linenum, 100, linenum);
				}
				else {
					g2d.setColor(colors.get(references.indexOf(pointsTo)));
					g2d.drawLine(300, linenum, 395, linenum);
				}
				left = !left;
			}
		}
		
		JPanel legend = new JPanel();
		for(String s: references) {
			legend.add(new JLabel(s));
		}
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
		JScrollPane legendScroller = new JScrollPane(legend); // Locks the window size, making it scroll.
		legendScroller.setPreferredSize(new Dimension(250, 150));
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		drawAdvancedView(g);
	}
	
}
