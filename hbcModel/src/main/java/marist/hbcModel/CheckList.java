package marist.hbcModel;

import java.awt.*;
import javax.swing.*;

// These classes are used to create and render the checklist in the GUI.
class CheckListItem {

	  private String label;
	  private boolean isSelected = true;

	  public CheckListItem(String label) {
	    this.label = label;
	  }

	  public boolean isSelected() {
	    return isSelected;
	  }

	  public void setSelected(boolean isSelected) {
	    this.isSelected = isSelected;
	  }

	  @Override
	  public String toString() {
	    return label;
	  }
	}

class CheckListRenderer extends JCheckBox implements ListCellRenderer {
	  public Component getListCellRendererComponent(JList list, Object value,
	      int index, boolean isSelected, boolean hasFocus) {
	    setEnabled(list.isEnabled());
	    setSelected(((CheckListItem) value).isSelected());
	    setFont(list.getFont());
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	    setText(value.toString());
	    return this;
	  }
}

class InteractionObject {
	private String search;
	private int weight = 1;
	
	public InteractionObject(String search) {
		this.search = search;
	}
	
	public InteractionObject(String search, int weight) {
		this.search = search;
		this.weight = weight;
	}
	
	public String getTerm() {
		return this.search;
	}
	
	public int getWeight() {
		return this.weight;
	}
}