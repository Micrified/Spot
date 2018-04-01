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
    
    /* ******** Constructor ******** */
    public GraphMenuBar(GraphFrame delegate) {

        /* Set Delegate */
        this.delegate = delegate;
        
        /* Create Menus */
        fileMenu = new JMenu("File");
        
        /* Create File Menu Items */
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        JMenuItem loadMenuItem = new JMenuItem("Load");
        
        /* Add Items to their Menus */
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(loadMenuItem);
        
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
        
        /* Add Menu's to the MenuBar */
        this.add(fileMenu);
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
