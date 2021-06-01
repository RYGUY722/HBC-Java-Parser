package marist.thesisModeler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

public class DetailWindow extends JFrame{
	
	static JSONObject obj;
	public DetailWindow(JSONObject obj) {
        super(obj.getString("filename"));
        this.obj = obj;
 
        getContentPane().setBackground(Color.WHITE);
        setSize(480, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
	
	public void drawAdvancedView(Graphics g) {
		JFrame adv = new JFrame();
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawRect(30, 50, 200, obj.getInt("linecount"));
		
		adv.setVisible(true);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		drawAdvancedView(g);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DetailWindow(obj).setVisible(true);
            }
        });
	}
	
}
