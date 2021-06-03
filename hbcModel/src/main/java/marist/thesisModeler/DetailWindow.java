package marist.thesisModeler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.json.JSONObject;

public class DetailWindow extends JFrame{
	
	static JSONObject obj;
	static boolean created;
	
	public DetailWindow(JSONObject obj) {
        super(obj.getString("filename"));
        this.obj = obj;
        created = false;
 
        getContentPane().setBackground(Color.WHITE);
        setSize(400, ((obj.getInt("linecount") * 2) + 400));
        //setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
	
	public void drawAdvancedView(Graphics g) {
		JPanel all = new JPanel();
		//all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setLayout(null);
		
		DetailBox box = new DetailBox(obj);
		box.setBounds(0, 0, 400, ((obj.getInt("linecount") * 2) + 100));
		box.paintComponent(g);
		all.add(box);
		
		
		JPanel lower = new JPanel();
		lower.setBounds(0, ((obj.getInt("linecount") * 2) + 100), 400, 300);
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel legend = new JPanel();
		legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
		ArrayList<String> references = box.getRef();
		ArrayList<Color> colors = box.getColors();
		for(int i = 0; i < references.size(); i++) {
			JLabel fLabel = new JLabel(references.get(i));
			fLabel.setForeground(colors.get(i));
			legend.add(fLabel);
		}
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane legendScroller = new JScrollPane(legend); // Locks the window size, making it scroll.
        legendScroller.setPreferredSize(new Dimension(300, 200));
        legendScroller.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab("Legend", legendScroller);
        
        JPanel methods = new JPanel();
        methods.setLayout(new BoxLayout(methods, BoxLayout.Y_AXIS));
		String fullMethods = obj.getString("method names");
		fullMethods = fullMethods.substring(1, fullMethods.length()-1);
		String[] metArr = fullMethods.split(", ");
		for(String s: metArr) {
			methods.add(new JLabel(s));
		}
		methods.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane methodScroller = new JScrollPane(methods); // Locks the window size, making it scroll.
        methodScroller.setPreferredSize(new Dimension(300, 200));
        methodScroller.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab("Methods", methodScroller);
        
        JPanel imports = new JPanel();
		imports.setLayout(new BoxLayout(imports, BoxLayout.Y_AXIS));
		String fullImports = obj.getString("imports");
		fullImports = fullImports.substring(1, fullImports.length()-1);
		fullImports.replace("\r\n", "");
		fullImports.replace("import", "");
		String[] impArr = fullImports.split(", ");
		for(String s: impArr) {
			imports.add(new JLabel(s));
		}
        imports.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane importScroller = new JScrollPane(imports); // Locks the window size, making it scroll.
        importScroller.setPreferredSize(new Dimension(300, 200));
        importScroller.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab("File Imports", importScroller);
        
        lower.add(tabs);
        all.add(lower);
        
        JScrollPane sizeLocker = new JScrollPane(all); // Locks the window size, making it scroll.
        sizeLocker.setMaximumSize(new Dimension(400, 500));
        sizeLocker.getVerticalScrollBar().setUnitIncrement(16);
        
		add(sizeLocker);
		created = true;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if(!created)
		drawAdvancedView(g);
	}
	
}

class DetailBox extends JPanel{
	
	static JSONObject obj;
	ArrayList<String> references = new ArrayList<String>();
	ArrayList<Color> colors = new ArrayList<Color>();
	
	public DetailBox(JSONObject obj) {
        super();
        this.obj = obj;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawRect(100, 50, 200, (obj.getInt("linecount") * 2));
		g2d.drawString(obj.getString("filename"), 150, (obj.getInt("linecount") + 50));
		String[] ref = obj.getString("references").split(", ");
		Random r = new Random();
		
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
					g2d.drawLine(300, linenum, 380, linenum);
				}
				left = !left;
			}
		}
	}
	
	public ArrayList<String> getRef() {
		return references;
	}
	
	public ArrayList<Color> getColors() {
		return colors;
	}
}