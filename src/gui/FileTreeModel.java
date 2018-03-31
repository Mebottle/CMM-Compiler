package gui;

import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;

public class FileTreeModel extends DefaultTreeModel {
    public FileTreeModel(TreeNode root) {
        super(root);
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File currentPath = new File(System.getProperty("user.dir"));
        FileNode childFileNode = new FileNode(fileSystemView.getSystemDisplayName(currentPath), fileSystemView.getSystemIcon(currentPath), currentPath, false);
        DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(childFileNode);
        ((DefaultMutableTreeNode) root).add(childTreeNode);
    }

    @Override
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
        FileNode fileNode = (FileNode) treeNode.getUserObject();
        if (fileNode.isDummyRoot) return false;
        return fileNode.file.isFile();
    }
}