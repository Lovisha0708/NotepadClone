package mynote;

import java.io.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.undo.UndoManager;



class FileOperation
{
	mynote note;
	boolean saved;
	boolean newFileFlag;
	String FileName;
	String appName = "myNote";
	
	File file;
	JFileChooser fchooser;
	
	/*boolean isSaved(){ return this.saved; }
    void setSaved(boolean saved){ this.saved = saved; }
    String getFileName(){ return new String(this.fileName); }
    String setFileName(String fileName){ return this.fileName = fileName; }*/
	
	FileOperation(mynote note)
	{
		this.note = note;
		
		saved = true;
		newFileFlag = true;
		FileName = "Untitled";
		file = new File("C:\\Desktop\\"+FileName);
		fchooser = new JFileChooser();
		fchooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Java Source Codes(*.java)", "java"));
        fchooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Text Files(*.txt)", "txt"));
        fchooser.setCurrentDirectory(new File("C:\\Desktop\\"));
	}
	
	boolean saveFile(File temp)
	{
		FileWriter writer = null;
		try {
			writer = new FileWriter(temp);
			writer.write(note.textArea.getText());
		} catch (IOException e) {
			// TODO: handle exception
			updateStatus(temp,false);
			return false;
		}finally
		{
			try
			{
			if(writer != null)
				writer.close();
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		 updateStatus(temp, true);
	        return true;
	}
	boolean saveThisFile() {
		if(!newFileFlag) return saveFile(file);
		return saveAsFile();
	}
	boolean saveAsFile() {
		File temp;
		fchooser.setDialogTitle("Save As...");
		fchooser.setApproveButtonText("Save");
		
		while(true) {
			if(fchooser.showSaveDialog(this.note.mainFrame)!=JFileChooser.APPROVE_OPTION)
				return false;
			temp = fchooser.getSelectedFile();
			if(!temp.exists()) break;
			int x = JOptionPane.showConfirmDialog(this.note.mainFrame,
                    "<html>" +temp.getPath()+ " already exists. Do you wish to overwrite?<html>",
                    "Save As",
                    JOptionPane.YES_NO_OPTION);
            if(x == JOptionPane.YES_OPTION) break;
        }
        return saveFile(temp);
		}
	boolean confirmSave() {
		String alert = "<html>Changes have been made to the document.<br>"
				+ "Would you like to save changes?<html>";
		if(!saved) {
			int opt = JOptionPane.showConfirmDialog(
					this.note.mainFrame,alert,appName,JOptionPane.YES_NO_CANCEL_OPTION);
			if(opt==JOptionPane.CANCEL_OPTION) return false;
            else if(opt == JOptionPane.YES_OPTION && !saveThisFile()) return false;
        }
        return true;
		}
	void openFile() {
		if(!confirmSave()) return;
		fchooser.setDialogTitle("Open file...");
		fchooser.setApproveButtonText("Open");
		
		File temp;
		while(true) {
			if(fchooser.showOpenDialog(this.note.mainFrame)!=JFileChooser.APPROVE_OPTION)
				return;
			temp = fchooser.getSelectedFile();
			if(temp.exists()) break;
			JOptionPane.showMessageDialog(
					this.note.mainFrame, 
					"<html>" +temp.getName()+"<br>File not found.<html>", 
					"open",
					JOptionPane.INFORMATION_MESSAGE);
		}
		this.note.textArea.setText("");
		
		if(!openFile(temp)) {
			FileName = "Untitled";
		this.note.mainFrame.setTitle(FileName + " - " + appName);
		}
		if(!temp.canWrite()) newFileFlag = true;
	}
	boolean openFile(File temp)
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			fis =  new FileInputStream(temp);
			br = new BufferedReader(new InputStreamReader(fis));
			String text = " ";
			while(text != null)
			{
				text = br.readLine();
				if(text == null) break;
				this.note.textArea.append(text + "\n");
			}
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			updateStatus(temp,false);
			return false;
		}finally {
			try {
				br.close();
				fis.close();
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		updateStatus(temp, true);
        this.note.textArea.setCaretPosition(0);
        return true;
	}
	
	void updateStatus(File temp , boolean saved)
	{
		if(saved) {
			this.saved = true;
			FileName = temp.getName();
			if(!temp.canWrite()) {
				FileName += "(Read Only)";
                newFileFlag = true;
			}
			file = temp;
			note.mainFrame.setTitle(FileName + " " +appName);
			note.statLabel.setText("File: " +temp.getPath()+ " saved/opened successfully.");
            newFileFlag = false;
		}else {
			 note.statLabel.setText("Failed to save/open: " +temp.getPath());
	            note.mainFrame.setTitle(FileName + "* - " + appName);
		}
	}
	void newFile() {
		if(!confirmSave()) return;
		
		 this.note.textArea.setText("");
	     FileName = "Untitled";
	     file = new File(FileName);
	     saved = true;
	     newFileFlag = true;
	     this.note.mainFrame.setTitle(FileName + " - " + appName);
	}
	}
	

public class mynote{
	
	JFrame mainFrame;
	JTextArea textArea;
	JLabel statLabel;
	
	FileOperation fileOP;
	
	String fileName = "Untitled";
	String appName = "Notepad";
	
	// edit menu item
	private JMenuItem cutMI, copyMI, deleteMI, findMI,
    findNextMI, replaceMI, gotoMI, selectAllMI, undoMI, redoMI;
	
	protected UndoManager undoManager = new UndoManager();

    JColorChooser bgColorChooser=null;
    JColorChooser fgColorChooser=null;
    JDialog bgDialog=null;
    JDialog fgDialog=null;

	
	
	public mynote()
	{
		fileOP = new FileOperation(this);
		prepareGUI();
	}
	
	private void prepareGUI()
	{
		mainFrame = new JFrame(fileName+ "-" +appName);
		textArea = new JTextArea(30,70);
		statLabel = new JLabel("||    Ln 1, Col 1",JLabel.RIGHT);
		
		mainFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);
		mainFrame.add(statLabel, BorderLayout.SOUTH);
		mainFrame.add(new JLabel("  "), BorderLayout.WEST);
		mainFrame.add(new JLabel("  "), BorderLayout.EAST);
		
		//
		DocumentListener docListener = new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				fileOP.saved = false;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				fileOP.saved = false;
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				fileOP.saved = false;

			}
		};
		textArea.getDocument().addDocumentListener(docListener);
		textArea.getDocument().addUndoableEditListener(new MyUndoableEditListener());
		mainFrame.setJMenuBar(createMenuBar());
		createPopupMenu();
		
		//Adding cursor status to Status Bar
        textArea.addCaretListener(e -> {
            int ln=0, col=0, pos;
            try{
                pos = textArea.getCaretPosition();
                ln = textArea.getLineOfOffset(pos);
                col = textArea.getLineStartOffset(ln);
            }catch (Exception ex){ex.printStackTrace();}
            if(textArea.getText().length()==0){
                ln=0;
                col=0;
            }
            statLabel.setText("||    Ln " + (ln+1) + ", Col "+ (col+1));
        });

		mainFrame.pack();
		mainFrame.setLocation(500, 50);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				if(!fileOP.confirmSave()) {
					int op = JOptionPane.showConfirmDialog(
							mainFrame,
							"There are unsaved changes in your document." +
                            " Are you sure you want to exit?");
                        if(op == JOptionPane.YES_OPTION)
                        	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}else
					mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
			}
		});
		mainFrame.setVisible(true);
		
	}
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu fileM = new JMenu("File");
		fileM.setMnemonic(KeyEvent.VK_F);
		JMenu editM = new JMenu("Edit");
		editM.setMnemonic(KeyEvent.VK_E);
		JMenu formatM = new JMenu("Format");
		formatM.setMnemonic(KeyEvent.VK_O);
		JMenu viewM = new JMenu("View");
		viewM.setMnemonic(KeyEvent.VK_V);
		JMenu helpM = new JMenu("Help");
		helpM.setMnemonic(KeyEvent.VK_H);
		
		MenuItemListener miListener = new MenuItemListener();
		JMenuItem tempMI;
		
		// creating file items
		createMenuItem("New", fileM, miListener, KeyEvent.VK_N, KeyEvent.VK_N);
		createMenuItem("Open", fileM, miListener, KeyEvent.VK_O, KeyEvent.VK_O);
		createMenuItem("Save", fileM, miListener, KeyEvent.VK_S, KeyEvent.VK_S);
		createMenuItem("Save As", fileM, miListener, KeyEvent.VK_A);
        fileM.addSeparator();
        createMenuItem("Page Setup", fileM, miListener, KeyEvent.VK_U);
        createMenuItem("Print", fileM, miListener, KeyEvent.VK_P, KeyEvent.VK_P);
        fileM.addSeparator();
        createMenuItem("Exit", fileM, miListener, KeyEvent.VK_X);
        
        //creating edit items
        undoMI =  createMenuItem("Undo", editM, miListener, KeyEvent.VK_U, KeyEvent.VK_Z);
        undoMI.setEnabled(false);
        redoMI = createMenuItem("Redo", editM, miListener, KeyEvent.VK_R);
        redoMI.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK |
                        InputEvent.SHIFT_DOWN_MASK));
        redoMI.setEnabled(false);
        cutMI = createMenuItem("Cut", editM, miListener, KeyEvent.VK_T, KeyEvent.VK_X);
        copyMI = createMenuItem("Copy", editM, miListener, KeyEvent.VK_C, KeyEvent.VK_C);
        createMenuItem("Paste", editM, miListener, KeyEvent.VK_P, KeyEvent.VK_V);
        deleteMI = createMenuItem("Delete", editM, miListener, KeyEvent.VK_L);
        deleteMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        editM.addSeparator();
        findMI = createMenuItem("Find", editM, miListener, KeyEvent.VK_F, KeyEvent.VK_F);
        findNextMI = createMenuItem("Find Next", editM, miListener, KeyEvent.VK_N);
        replaceMI = createMenuItem("Replace", editM, miListener, KeyEvent.VK_R);
        gotoMI = createMenuItem("Go to", editM, miListener, KeyEvent.VK_G);
        editM.addSeparator();
        selectAllMI = createMenuItem("Select All", editM, miListener, KeyEvent.VK_A, KeyEvent.VK_A);
        createMenuItem("Time/Date", editM, miListener, KeyEvent.VK_T);
        
        //creating Format items
        JCheckBoxMenuItem wordWrapMI = new JCheckBoxMenuItem("Word Wrap", false);
        wordWrapMI.setActionCommand("Word Wrap");
        wordWrapMI.addActionListener(miListener);
        formatM.add(wordWrapMI);
        createMenuItem("Font", formatM, miListener, KeyEvent.VK_F);
        formatM.addSeparator();
        createMenuItem("Text Color", formatM, miListener, KeyEvent.VK_T);
        createMenuItem("BG Color", formatM, miListener, KeyEvent.VK_B);
        JMenu themeSubMenu = new JMenu("Theme");
        JRadioButtonMenuItem lightThemeMI = new JRadioButtonMenuItem("Light", true);
        lightThemeMI.setActionCommand("lightTheme");
        lightThemeMI.addActionListener(miListener);
        JRadioButtonMenuItem darkThemeMI = new JRadioButtonMenuItem("Dark", false);
        darkThemeMI.setActionCommand("darkTheme");
        darkThemeMI.addActionListener(miListener);
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightThemeMI);
        themeGroup.add(darkThemeMI);
        themeSubMenu.add(lightThemeMI);
        themeSubMenu.add(darkThemeMI);
        formatM.add(themeSubMenu);

        //Creating View items
        JCheckBoxMenuItem statusMI = new JCheckBoxMenuItem("View Status Bar", true);
        statusMI.setActionCommand("viewStat");
        statusMI.addActionListener(miListener);
        viewM.add(statusMI);

        //Creating Help items
        createMenuItem("Help Topic", helpM, miListener, KeyEvent.VK_H);
        createMenuItem("About myNote", helpM, miListener, KeyEvent.VK_A);  
        
        MenuListener editListener = new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				// TODO Auto-generated method stub
				if(textArea.getText().length()==0)
				{
					findMI.setEnabled(false);
					findNextMI.setEnabled(false);
                    selectAllMI.setEnabled(false);
                    replaceMI.setEnabled(false);
                    gotoMI.setEnabled(false);
                    undoMI.setEnabled(false);
                    redoMI.setEnabled(false);
				}else
				{
					findMI.setEnabled(true);
					findNextMI.setEnabled(true);
                    selectAllMI.setEnabled(true);
                    replaceMI.setEnabled(true);
                    gotoMI.setEnabled(true);
                    undoMI.setEnabled(true);
                    redoMI.setEnabled(true);
				}
				if(textArea.getSelectionStart() == textArea.getSelectionEnd())
				{
					cutMI.setEnabled(false);
					copyMI.setEnabled(false);
					deleteMI.setEnabled(false);
				}else
				{
					cutMI.setEnabled(true);
					copyMI.setEnabled(true);
					deleteMI.setEnabled(true);
				}
				
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {}
			@Override
			public void menuCanceled(MenuEvent e) {}
		};
		editM.addMenuListener(editListener);
		
		menuBar.add(fileM);
		menuBar.add(editM);
		menuBar.add(formatM);
		menuBar.add(viewM);
		menuBar.add(helpM);
		
		mainFrame.setVisible(true);
		return menuBar;

	}
	private JMenuItem createMenuItem(String name, JMenu toMenu, ActionListener miListener, int key)
	{
		JMenuItem mItem = new JMenuItem(name,key);
		mItem.setActionCommand(name);
		mItem.addActionListener(miListener);
		toMenu.add(mItem);
		return mItem;
	}
	//Method to create menu items with shortcut.
    private JMenuItem createMenuItem(String name, JMenu toMenu, ActionListener miListener,
                                     int key, int accKey){
        JMenuItem mItem = new JMenuItem(name, key);
        mItem.setActionCommand(name);
        mItem.addActionListener(miListener);
        // this line
        mItem.setAccelerator(KeyStroke.getKeyStroke(accKey, InputEvent.CTRL_DOWN_MASK));
        toMenu.add(mItem);
        return mItem;
    }	
	private void createPopupMenu() {
		final JPopupMenu popEdit = new JPopupMenu("Edit");
		
		JMenuItem cutPopMI = new JMenuItem("Cut");
        cutPopMI.setActionCommand("Cut");

        JMenuItem copyPopMI = new JMenuItem("Copy");
        copyPopMI.setActionCommand("Copy");

        JMenuItem pastePopMI = new JMenuItem("Paste");
        pastePopMI.setActionCommand("Paste");

        JMenuItem selectAllPopMI = new JMenuItem("Select All");
        selectAllPopMI.setActionCommand("Select All");

        JMenuItem deletePopMI = new JMenuItem("Delete");
        deletePopMI.setActionCommand("Delete");
        
        MenuItemListener miListener = new MenuItemListener();
        
        cutPopMI.addActionListener(miListener);
        copyPopMI.addActionListener(miListener);
        pastePopMI.addActionListener(miListener);
        selectAllPopMI.addActionListener(miListener);
        deletePopMI.addActionListener(miListener);
        
        popEdit.add(cutPopMI);
        popEdit.add(copyPopMI);
        popEdit.add(pastePopMI);
        popEdit.add(selectAllPopMI);
        popEdit.add(deletePopMI);
        
        textArea.addMouseListener(new MouseAdapter() {
        	public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e))
                    popEdit.show(mainFrame, e.getX(), e.getY());
            }
        });
        textArea.add(popEdit);
        mainFrame.setVisible(true);
	}
	
		private void goTo() {
			int ln;
			try {
				ln = textArea.getLineOfOffset(textArea.getCaretPosition())+1;
				String lnStr = JOptionPane.showInputDialog(mainFrame,
						"Enter line number:",
						"" + ln);
				if(lnStr == null) return;
				ln = Integer.parseInt(lnStr);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void bgColorDialog() {
			if(bgColorChooser == null) bgColorChooser = new JColorChooser();
			if(bgDialog == null)
				bgDialog = JColorChooser.createDialog(
						mainFrame,
						"Set text Color....",
						false, 
						bgColorChooser,
						new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								textArea.setBackground(bgColorChooser.getColor());
								
							}
						}, null);
			bgDialog.setVisible(true);
		}
		
		private void fgColorDialog() {
			if(fgColorChooser == null) fgColorChooser = new JColorChooser();
			if(fgDialog == null)
				fgDialog = JColorChooser.createDialog(
						mainFrame,
						"Set text Color....",
						false, 
						fgColorChooser,
						new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								textArea.setBackground(fgColorChooser.getColor());
								
							}
						}, null);
			fgDialog.setVisible(true);
		}

	public static void main(String[] args) {
		new mynote();

	}
	
	private class MyUndoableEditListener implements UndoableEditListener
	{

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			// TODO Auto-generated method stub
			undoManager.addEdit(e.getEdit());
		}
		
	}
	
	private class MenuItemListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			String command = e.getActionCommand();
			switch(command)
			{
			// file menu action
			case "New" -> fileOP.newFile();
			case "Open" -> fileOP.openFile();
			case "Save" -> fileOP.saveThisFile();
			case "Save As" -> fileOP.saveAsFile();
			case "Exit" -> {
				if(fileOP.confirmSave())
					System.exit(0);
			}
			
				// edit menu action
			case "Print" -> JOptionPane.showMessageDialog(
					mainFrame,
					"No printer could be detected.",
                    "Printer not found",
                    JOptionPane.INFORMATION_MESSAGE);
			case "Cut" -> textArea.cut();
			case "Copy" -> textArea.copy();
			case "Paste" -> textArea.paste();
			case "Delete" -> textArea.replaceSelection("");
			case "Select All" -> textArea.selectAll();
			// undo and redo function
			case "Undo" -> {
				if(undoManager.canUndo()) undoManager.undo();
			}
			case "Redo" -> {
				if(undoManager.canRedo()) undoManager.redo();
			}
			case "Find" -> {}
			case "Find Next" -> {}
			case "Replace" -> {}
			case "Go to" -> {
				if(textArea.getText().length()==0) return;
                goTo();
			}
		
			// format menu function
			case "WordWrap" -> {
				JCheckBoxMenuItem temp = (JCheckBoxMenuItem) e.getSource();
                textArea.setLineWrap(temp.isSelected());
			}
			case "Font" -> {}
			case "Text Color" -> fgColorDialog();
			case "Bg Color" -> bgColorDialog();
			case "Time/Date" -> textArea.insert(new Date().toString(), textArea.getSelectionStart());
			case "lightTheme" -> {
                textArea.setBackground(Color.WHITE);
                textArea.setForeground(Color.BLACK);
            }
            case "darkTheme" -> {
                textArea.setForeground(Color.LIGHT_GRAY);
                textArea.setBackground(Color.DARK_GRAY);
            }
            
            // View menu action
            case "viewStat" -> {
            	 JCheckBoxMenuItem temp = (JCheckBoxMenuItem) e.getSource();
                 statLabel.setVisible(temp.isSelected());
            }
            
            // about menu action
            case "Help Topic" -> {}
            case "About myNote" -> JOptionPane.showMessageDialog(
                    mainFrame,
                    "<html>This project has been created as my first work on AWT and Swing.<br>" +
                            "Project is still under development and improvement.<br>" +
                            "--Developed by Lovisha--<html>",
                    "About myNote",
                    JOptionPane.INFORMATION_MESSAGE);
			}
			
		}
		
	}

}
