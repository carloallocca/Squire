/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author callocca
 * @param <T>=List<TriplePath> but the next version is going to be the Query. 
 */
public class TreeNode<T> {

    private T data = null;
    private ArrayList<TreeNode> children = new ArrayList<>();
    
    

    public TreeNode(T data, ArrayList<TreeNode> childs) {
        if(data!=null){
            this.data = data;
        }
        if (childs != null) {
            this.children.addAll(childs);
        }

    }

//    public TreeNode(T data, TreeNode... children) {
//        this.data = data;
//        
//        this.children.addAll(Arrays.asList(children));
//
//    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setChildren(ArrayList<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode<T> child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);//.addSibling(childNode);
    }

//    public void addSibling(TreeNode<T> sibling) {
//        if(!(this.children==null)){
//            if(!(this.children.contains(sibling))){
//                this.children.add(sibling);//.addSibling(childNode);
//            }
//
//        }
//    }
}
