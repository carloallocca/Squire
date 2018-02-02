/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;

/**
 *
 * @author callocca
 * 
 * @param <T>=List<TriplePath>
 *            but the next version is going to be the Query.
 */
public class TreeNode<T> {

	private T data = null;

	private ArrayList<TreeNode<T>> children = new ArrayList<>();

	public TreeNode(T data, ArrayList<TreeNode<T>> childs) {
		if (data != null) {
			this.data = data;
		}
		if (childs != null) {
			this.children.addAll(childs);
		}

	}

	public void addChild(TreeNode<T> child) {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		this.children.add(child);// .addSibling(childNode);
	}

	public ArrayList<TreeNode<T>> getChildren() {
		return children;
	}

	public T getData() {
		return data;
	}

	public void setChildren(ArrayList<TreeNode<T>> children) {
		this.children = children;
	}

	public void setData(T data) {
		this.data = data;
	}
}
