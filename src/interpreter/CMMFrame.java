package interpreter;

import gui.*;
import org.jvnet.substance.skin.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class CMMFrame extends JFrame {
    // 菜单栏
    private JMenuBar MENU_BAR;
    // 窗体工具条
    private JToolBar TOOL_BAR;
    // 项目管理器
    private JPanel FILE_PANEL;
    // 文件浏览树
    private FileTree FILE_TREE;
    // 文本编辑器
    private JPanel editPanel;
    public final Editor editor = new Editor();
    // 控制台
    private JPanel consolePanel;
    // 控制台
    private JTextArea consoleArea;
    // 分析窗口
    private JTextArea analyseArea;
    // 分析窗口（词法分析、语法分析）
    private JPanel ANALYSE_PANEL;
    //保存和打开对话框
    private FileDialog filedialog_save, filedialog_load;
    // 编辑器上方文件名
    private JLabel editLabel;

    private GUIInputStream gis;

    private Thread thread;
    public final static Object object = new Object();

    public CMMFrame() throws NoSuchMethodException {
        super();
        createMenus();
        createToolBar();
        createFileManager();
        createEditor();
        createConsole();
        createAnalyseResultPanel();
        setLayout(null);
        setTitle("CMM解释器");
        // 设置背景颜色
        getContentPane().setBackground(new Color(47, 47, 47));
        // 设置关闭操作
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void createMenus() throws NoSuchMethodException {
        // 初始化菜单项
        MENU_BAR = new JMenuBar();

        // File
        JMenu fileMenu = MENU_BAR.add(new JMenu("File"));
        createMenuItem(fileMenu, "New", "create");
        createMenuItem(fileMenu, "Open", "open");
        createMenuItem(fileMenu, "Save", "save");
        fileMenu.addSeparator();
        createMenuItem(fileMenu, "Exit", "exit");
        fileMenu.addSeparator();

        // Edit
        JMenu editMenu = MENU_BAR.add(new JMenu("Edit"));
        createMenuItem(editMenu, "Copy", "copy");
        createMenuItem(editMenu, "Cut", "cut");
        createMenuItem(editMenu, "Paste", "paste");
        createMenuItem(editMenu, "Delete", "delete");
        editMenu.addSeparator();
        createMenuItem(editMenu, "Select All", "select all");
        editMenu.addSeparator();

        // View
        JMenu viewMenu = MENU_BAR.add(new JMenu("View"));
        createMenuItem(viewMenu, "Project View", null);
        createMenuItem(viewMenu, "Lexer View", null);
        createMenuItem(viewMenu, "Paser View", null);
        viewMenu.addSeparator();

        // Run
        JMenu runMenu = MENU_BAR.add(new JMenu("Run"));
        createMenuItem(runMenu, "run", "run");
        runMenu.addSeparator();

        // Help
        JMenu helpMenu = MENU_BAR.add(new JMenu("Help"));
        createMenuItem(helpMenu, "About", "about");
        helpMenu.addSeparator();

        // 绑定菜单栏
        setJMenuBar(MENU_BAR);
    }

    // 创建子菜单项
    private void createMenuItem(JMenu menu, String label, String method) {
        JMenuItem mi = menu.add(new JMenuItem(label));
        if (method == null) {
            mi.setEnabled(false);
            return;
        }
        switch (method) {
            case "save":
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                        InputEvent.CTRL_MASK));
                break;
            case "copy":
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                        InputEvent.CTRL_MASK));
                break;
            case "cut":
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                        InputEvent.CTRL_MASK));
                break;
            case "paste":
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                        InputEvent.CTRL_MASK));
                break;
            case "select all":
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                        InputEvent.CTRL_MASK));
                break;
        }
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                switch (method) {
                    case "save":
                        save();
                        break;
                    case "create":
                        create();
                        break;
                    case "exit":
                        exit();
                        break;
                    case "open":
                        open();
                        break;
                    case "copy":
                        copy();
                        break;
                    case "cut":
                        cut();
                        break;
                    case "paste":
                        paste();
                        break;
                    case "select all":
                        selectAll();
                        break;
                    case "delete":
                        delete();
                        break;
                    case "about":
                        about();
                        break;
                    case "run":
                        run();

                }
            }
        });
    }

    private JButton createToolBarButton(String imgFilename, String toolTipText) {
        Image image = getToolkit().getImage(imgFilename);
        image = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        JButton newButton = new JButton();
        newButton.setIcon(new ImageIcon(image));
        newButton.setToolTipText(toolTipText);
        newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return newButton;
    }

    // 创建工具栏
    private void createToolBar() {
        TOOL_BAR = new JToolBar();
        TOOL_BAR.setFloatable(false);
        TOOL_BAR.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        JButton newButton = createToolBarButton("img/new.png", "新建");
        JButton openButton = createToolBarButton("img/open.png", "打开");
        JButton saveButton = createToolBarButton("img/save.png", "保存");
        JButton exitButton = createToolBarButton("img/exit.png", "退出");
        JButton copyButton = createToolBarButton("img/copy.png", "复制");
        JButton cutButton = createToolBarButton("img/cut.png", "剪切");
        JButton pasteButton = createToolBarButton("img/paste.png", "粘贴");
        JButton deleteButton = createToolBarButton("img/delete.png", "删除");
        JButton runButton = createToolBarButton("img/run.png", "运行");
        JButton aboutButton = createToolBarButton("img/about.png", "关于");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                create();
            }
        });
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });
        pasteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paste();
            }
        });
        cutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cut();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                about();
            }
        });
        TOOL_BAR.add(newButton);
        TOOL_BAR.add(openButton);
        TOOL_BAR.add(saveButton);

        TOOL_BAR.addSeparator();
        TOOL_BAR.addSeparator();

        TOOL_BAR.add(copyButton);
        TOOL_BAR.add(cutButton);
        TOOL_BAR.add(pasteButton);
        TOOL_BAR.add(deleteButton);

        TOOL_BAR.addSeparator();
        TOOL_BAR.addSeparator();

        TOOL_BAR.add(runButton);

        TOOL_BAR.addSeparator();
        TOOL_BAR.addSeparator();

        TOOL_BAR.add(aboutButton);

        TOOL_BAR.addSeparator();
        TOOL_BAR.addSeparator();

        TOOL_BAR.add(exitButton);
        TOOL_BAR.setBounds(0, 0, 1240, 30);
        TOOL_BAR.setPreferredSize(getPreferredSize());
        add(TOOL_BAR);
    }

    private void createFileManager() {
        // 文件保存和打开对话框
        filedialog_save = new FileDialog(this, "保存文件", FileDialog.SAVE);
        filedialog_save.setVisible(false);
        filedialog_load = new FileDialog(this, "打开文件", FileDialog.LOAD);
        filedialog_load.setVisible(false);
        filedialog_save.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                filedialog_save.setVisible(false);
            }
        });
        filedialog_load.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                filedialog_load.setVisible(false);
            }
        });
        FILE_TREE = new FileTree();
        FILE_TREE.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = FILE_TREE.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        FileNode fileNode = (FileNode) node.getUserObject();
                        String filePath = fileNode.file.getPath();
                        String fileType = filePath.substring(filePath.length() - 4);
                        if (fileType.equals(".cmm")) {
                            String text = CMMFileReader.cmm2String(fileNode.file);
                            editor.setText("");
                            editor.setText(text);
                            editLabel.setText(fileNode.file.getName());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        FileTreeModel model = new FileTreeModel(new DefaultMutableTreeNode(new FileNode("root", null, null, true)));
        FILE_TREE.setModel(model);
        FILE_TREE.setCellRenderer(new FileTreeRenderer());
        // 资源管理区(文件目录浏览区)
        FILE_PANEL = new JPanel(new BorderLayout());
        JLabel fileLabel = new JLabel("Project");
        JPanel fileLabelPanel = new JPanel(new BorderLayout());
        fileLabel.setFont(new Font("幼圆", Font.BOLD, 13));
        fileLabelPanel.add(fileLabel, BorderLayout.WEST);
        fileLabelPanel.setBackground(Color.LIGHT_GRAY);
        FILE_PANEL.add(fileLabelPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(FILE_TREE);
        FILE_PANEL.add(scrollPane, BorderLayout.CENTER);
        FILE_PANEL.setBounds(0, TOOL_BAR.getHeight() + MENU_BAR.getHeight(), 195, 750
                - TOOL_BAR.getHeight() - 98);
        add(FILE_PANEL);
    }

    private void createEditor() {
        JScrollPane scrollPane = new JScrollPane(editor);
        // 添加行号组件
        TextLineNumber tln = new TextLineNumber(editor);
        scrollPane.setRowHeaderView(tln);

        // 获得默认焦点
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent evt) {
                editor.requestFocus();
            }
        });
        editPanel = new JPanel(null);
        editPanel.setBackground(getBackground());
        editPanel.setForeground(new Color(238, 238, 238));
        editLabel = new JLabel("untitled", JLabel.CENTER);
        editLabel.setFont(new Font("幼圆", Font.BOLD, 13));
        JPanel editLabelPanel = new JPanel(new BorderLayout());

        editLabelPanel.add(editLabel, BorderLayout.CENTER);
        editLabelPanel.setBackground(Color.LIGHT_GRAY);
        editPanel.add(editLabelPanel, BorderLayout.NORTH);

        editPanel.add(scrollPane, BorderLayout.CENTER);

        editLabelPanel.setBounds(0, 0, 815, 15);

        editPanel.setBounds(FILE_PANEL.getWidth(), TOOL_BAR.getHeight(), 815,
                450 - TOOL_BAR.getHeight());
        scrollPane.setBounds(5, editLabelPanel.getHeight() + 5, editPanel.getWidth() - 10, editPanel.getHeight() - editLabelPanel.getHeight());
        add(editPanel);
    }

    private void createConsole() {
        consoleArea = new JTextArea();
        consoleArea.setTabSize(4);
        consoleArea.setLineWrap(true);// 激活自动换行功能
        consoleArea.setWrapStyleWord(true);// 激活断行不断字功能

        consolePanel = new JPanel(null);
        consolePanel.setBackground(getBackground());
        consolePanel.setForeground(new Color(238, 238, 238));

        JLabel editLabel = new JLabel("Console", JLabel.CENTER);
        editLabel.setFont(new Font("幼圆", Font.BOLD, 13));
        JPanel editLabelPanel = new JPanel(new BorderLayout());
        editLabelPanel.add(editLabel, BorderLayout.CENTER);
        editLabelPanel.setBackground(Color.LIGHT_GRAY);
        editLabelPanel.setBounds(0, 0, 815, 15);
        consolePanel.add(editLabelPanel, BorderLayout.NORTH);

        JScrollPane jscrollPane = new JScrollPane(consoleArea);
        jscrollPane.setBounds(0, 15, 815, 630 - editPanel.getHeight() - 20);
        consolePanel.setBounds(FILE_PANEL.getWidth(), TOOL_BAR.getHeight() + editPanel.getHeight(), 815, 620 - editPanel.getHeight());
        consolePanel.add(jscrollPane, BorderLayout.CENTER);

        gis = new GUIInputStream(consoleArea);
        consoleArea.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    try {
                        gis.method();
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                    synchronized (object) {
                        //maybe this should only notify() as multiple threads may
                        //be waiting for input and they would now race for input
                        object.notifyAll();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });


        add(consolePanel);
    }

    private void createAnalyseResultPanel() {
        ANALYSE_PANEL = new JPanel(new BorderLayout());
        JLabel fileLabel = new JLabel("Analyse");
        JPanel fileLabelPanel = new JPanel(new BorderLayout());
        fileLabel.setFont(new Font("幼圆", Font.BOLD, 13));
        fileLabelPanel.add(fileLabel, BorderLayout.WEST);
        fileLabelPanel.setBackground(Color.LIGHT_GRAY);
        ANALYSE_PANEL.add(fileLabelPanel, BorderLayout.NORTH);

        analyseArea = new JTextArea();
        analyseArea.setTabSize(4);
        analyseArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(analyseArea);
        ANALYSE_PANEL.add(scrollPane, BorderLayout.CENTER);
        ANALYSE_PANEL.setBounds(FILE_PANEL.getWidth() + editPanel.getWidth(), TOOL_BAR.getHeight() + MENU_BAR.getHeight(), 1240 - FILE_PANEL.getWidth() - editPanel.getWidth(), 750
                - TOOL_BAR.getHeight() - 98);
        add(ANALYSE_PANEL);
    }

    // 运行：分析并运行CMM程序，显示运行结果
    public void run() {
        System.setIn(gis);
        String text = editor.getText();
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("temp.cmm"));
            pw.println(text);
            pw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        consoleArea.setText("");
        analyseArea.setText("");
        thread = new Thread() {
            public void run() {
                CMMEntry t = new CMMEntry();
                t.start("temp.cmm", consoleArea, analyseArea);
            }
        };
        thread.start();
    }

    // 新建
    private void create() {
        editor.setText(null);
        editLabel.setText("untitled");
        editLabel.repaint();

    }

    // 打开
    private void open() {
        File file = null;
        filedialog_load.setVisible(true);
        if (filedialog_load.getFile() != null) {
            file = new File(filedialog_load.getDirectory(), filedialog_load
                    .getFile());
            editor.setText(CMMFileReader.cmm2String(file));
            editLabel.setText(file.getName());
            editLabel.repaint();
        }
    }

    // 保存
    public void save() {
        File currentPath = new File(new File(this.getClass().getClassLoader().getResource("").getPath()).getParent());
        JFileChooser chooser = new JFileChooser(currentPath);
        //后缀名过滤器
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "CMM文件 (*.cmm)", "cmm");
        chooser.setFileFilter(filter);
        int option = chooser.showSaveDialog(null);
        File file = null;
        if (option == JFileChooser.APPROVE_OPTION) {    //假如用户选择了保存
            file = chooser.getSelectedFile();

            String fname = chooser.getName(file);   //从文件名输入框中获取文件名

            //假如用户填写的文件名不带我们制定的后缀名，那么我们给它添上后缀
            if (fname.indexOf(".con") == -1) {
                file = new File(chooser.getCurrentDirectory(), fname + ".cmm");
            }
        }

        if (editor.getText() != null) {
            try {
                FileWriter fw = new FileWriter(file);
                fw.write(editor.getText());
                fw.close();
            } catch (IOException e2) {
            }
        }
    }

    // 退出
    private void exit() {
        System.exit(0);
    }


    // 复制
    private void copy() {
        editor.copy();
    }

    // 剪切
    private void cut() {
        editor.cut();
    }

    // 粘贴
    private void paste() {
        editor.paste();
    }


    // 全选
    private void selectAll() {
        editor.selectAll();
    }

    // 删除
    private void delete() {
        editor.replaceSelection("");
    }

    private void about() {
        JOptionPane.showMessageDialog(new JOptionPane(),
                "\n\n\t\t\t\t          CMM解释器\n" +
                        "\t    2015302580332\t隗泽浩        \n" +
                        "\t    2015302580337\t冯文珠        \n" +
                        "\t\t\t\t          版本 1.0.0\n" +
                        "Copyright © 2017–2018 ISS Inc.        \n" +
                        "       All rights reserved.\n\n", "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // 主函数
    public static void main(String[] args) {
        try {
            //设置外观
            UIManager.setLookAndFeel(new SubstanceAutumnLookAndFeel());
            JFrame.setDefaultLookAndFeelDecorated(true);
            CMMFrame cmmFrame = new CMMFrame();
            cmmFrame.setBounds(25, 30, 1248, 700);
            cmmFrame.setResizable(true);
            cmmFrame.setVisible(true);
        } catch (UnsupportedLookAndFeelException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

}
