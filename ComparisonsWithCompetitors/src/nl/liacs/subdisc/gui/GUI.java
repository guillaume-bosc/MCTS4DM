/**
 * Convenience class to build GUI elements.
 * Any class calling these functions should implement ActionListener,
 *  if ActionListeners have to be set.
 * Also they need to be passed as (the last) parameter.
 * Typical usage: aPanel.add(GUI.getButton("Name", 'X', "KeyPressed", this));
 */

package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import nl.liacs.subdisc.gui.MiningWindow.*;

public class GUI
{
	public static final Color RED = new Color(255, 150, 166);
	// TODO remove DEFAULT_
	public static final Font DEFAULT_TEXT_FONT = new Font("Dialog", 0, 11);
	public static final Font DEFAULT_BUTTON_FONT = new Font("Dialog", 1, 11);
	public static final Dimension TEXT_FIELD_DEFAULT_SIZE = new Dimension(86, 22);
	public static final Dimension WINDOW_DEFAULT_SIZE = new Dimension(1000, 600);
	public static final Dimension ROC_WINDOW_DEFAULT_SIZE = new Dimension(600, 600);
	// button
	public static final Dimension BUTTON_DEFAULT_SIZE = new Dimension(120, 25);
	public static final Dimension BUTTON_MINIMUM_SIZE = new Dimension(75, 25);
	public static final Dimension BUTTON_MEDIUM_SIZE = new Dimension(100, 25);
	public static final Dimension BUTTON_MAXIMUM_SIZE = new Dimension(120, 25);

	public enum Event
	{
		DISABLE_ATTRIBUTE,
		ENABLE_ATTRIBUTE,
		CHANGE_ATTRIBUTE_TYPE,
		CHANGE_MISSING;
	}

	private GUI() {}; // uninstantiable class

	public static JButton buildButton(String theName, String theActionCommand, ActionListener theClass)
	{
		JButton aButton = new JButton();
		aButton.setPreferredSize(BUTTON_DEFAULT_SIZE);
		aButton.setMinimumSize(BUTTON_MINIMUM_SIZE);
		aButton.setMaximumSize(BUTTON_MAXIMUM_SIZE);
		aButton.setFont(DEFAULT_BUTTON_FONT);
		aButton.setBorder(new BevelBorder(0));
		aButton.setText(theName);
		aButton.setActionCommand(theActionCommand);
		aButton.addActionListener(theClass);
		return aButton;
	}

	public static JButton buildButton(String theName, int theMnemonic, String theActionCommand, ActionListener theClass)
	{
		// use constructor without mnemonic
		JButton aButton = GUI.buildButton(theName, theActionCommand, theClass);
		aButton.setMnemonic(theMnemonic);
		return aButton;
	}

	public static JRadioButton buildRadioButton(String theName, String theActionCommand, ActionListener theClass)
	{
		JRadioButton aRadioButton = new JRadioButton();
		aRadioButton.setText(theName);
		aRadioButton.setActionCommand(theActionCommand);
		aRadioButton.addActionListener(theClass);
		return aRadioButton;
	}

	public static JCheckBox buildCheckBox(String theName, ItemListener theClass)
	{
		JCheckBox aCheckBox = new JCheckBox(theName);
//		aCheckBox.setMnemonic(theMnemonic);
		aCheckBox.addItemListener(theClass);
		return aCheckBox;
	}

	public static JComboBox buildComboBox(Object[] theItems, String theActionCommand, ActionListener theClass)
	{
		JComboBox aComboBox = new JComboBox();
		aComboBox.setPreferredSize(TEXT_FIELD_DEFAULT_SIZE);
		aComboBox.setMinimumSize(TEXT_FIELD_DEFAULT_SIZE);
		aComboBox.setFont(DEFAULT_TEXT_FONT);

		for (Object o : theItems)
			aComboBox.addItem(o);

		aComboBox.setActionCommand(theActionCommand);
		aComboBox.addActionListener(theClass);
		return aComboBox;
	}

	public static JComboBox buildComboBox(Object[] theItems, ActionListener theClass)
	{
		JComboBox aComboBox = new JComboBox();
		aComboBox.setPreferredSize(TEXT_FIELD_DEFAULT_SIZE);
		aComboBox.setMinimumSize(TEXT_FIELD_DEFAULT_SIZE);
		aComboBox.setFont(DEFAULT_TEXT_FONT);

		for (Object o : theItems)
			aComboBox.addItem(o);

		aComboBox.addActionListener(theClass);
		return aComboBox;
	}

	public static JLabel buildLabel(String theName, Component theComponent)
	{
		JLabel aJLable = new JLabel(theName);
		aJLable.setFont(DEFAULT_TEXT_FONT);
		aJLable.setLabelFor(theComponent);
		return aJLable;
	}

	public static JMenuItem buildMenuItem(String theText, int theMnemonic, KeyStroke theAccelerator, ActionListener theClass)
	{
		JMenuItem aMenuItem = new JMenuItem(theText, theMnemonic);
		aMenuItem.setFont(GUI.DEFAULT_TEXT_FONT);
		aMenuItem.setAccelerator(theAccelerator);
		aMenuItem.addActionListener(theClass);
		return aMenuItem;
	}

	public static JTextField buildTextField(String theText)
	{
		JTextField aTextField = new JTextField();
		aTextField.setPreferredSize(TEXT_FIELD_DEFAULT_SIZE);
		aTextField.setMinimumSize(TEXT_FIELD_DEFAULT_SIZE);
		aTextField.setFont(GUI.DEFAULT_TEXT_FONT);
		aTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		aTextField.setText(theText);
		return aTextField;
	}

	// no need for null check
	public static Border buildBorder(String theTitle)
	{
		return new TitledBorder(new EtchedBorder(), theTitle, 4, 2, DEFAULT_BUTTON_FONT);
	}

	// on window opening focus on the specified JComponent
	// TODO to be used by all window classes
	public static void focusComponent(final JComponent theComponentToFocus, JFrame theFrame)
	{
		theFrame.addWindowListener(
			new WindowAdapter()
			{
				@Override
				public void windowOpened(WindowEvent e)
				{
					theComponentToFocus.requestFocusInWindow();
				}
			});
	}
}
