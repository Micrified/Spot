import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class GraphMenuBar extends JMenuBar {
    
    /* ******** Properties ******** */
    private GraphFrame delegate;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu helpMenu;
    
    
    /* ******** Constructor ******** */
    public GraphMenuBar(GraphFrame delegate) {

        /* Set Delegate */
        this.delegate = delegate;
        
        /* Create Menus */
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        viewMenu = new JMenu("View");
        helpMenu = new JMenu("Help");
        
        /* Create File Menu Items */
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        JMenuItem loadMenuItem = new JMenuItem("Load");
        
        /* Create Edit Menu Items */
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        JMenuItem redoMenuItem = new JMenuItem("Redo");
        
        /* Create View Menu Items */
        JMenuItem duplicateMenuItem = new JMenuItem("Duplicate");
        
        /* Create Help Menu Items */
        JMenuItem helpMenuItem = new JMenuItem("Help me!");
        
        /* Add Items to their Menus */
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(loadMenuItem);
        
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        
        viewMenu.add(duplicateMenuItem);
        
        helpMenu.add(helpMenuItem);
        
        /* Add ActionListeners for File-Menu Items */
        saveMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didSave();
            }
        });
        
        saveAsMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didSaveAs();
            }
        });
        
        loadMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didLoad();
            }
        });
        
        /* Add ActionListeners for Edit-Menu Items */
        undoMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didUndo();
            }
        });
        
        redoMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didRedo();
            }
        });
        
        /* Add ActionListeners for View-Menu Items */
        duplicateMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didDuplicate();
            }
        });
        
        /* Add ActionListeners for Help-Menu Items */
        helpMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GraphMenuBar.this.delegate.didRequestHelp();
            }
        });
        
        /* Add Menu's to the MenuBar */
        this.add(fileMenu);
        this.add(editMenu);
        this.add(viewMenu);
        this.add(helpMenu);
    }
    
    /* ******** Setters ******** */
    public void setDelegate(GraphFrame delegate){
        this.delegate = delegate;
    }
    
    /* ******** Getters ******** */
    public GraphFrame getDelegate(){
        return this.delegate;
    }
}
