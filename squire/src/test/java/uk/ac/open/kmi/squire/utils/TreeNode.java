/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author callocca
 * 
 * @param <T>=List<TriplePath>
 *            but the next version is going to be the Query.
 */
public class TreeNode<T> {

	private T data = null;

	private List<TreeNode<T>> children = new ArrayList<>();

	public TreeNode(T data) {
		this(data, Collections.emptyList());
	}

	public TreeNode(T data, List<TreeNode<T>> children) {
		if (data != null) this.data = data;
		if (children != null) this.children.addAll(children);
	}

	public void addChild(TreeNode<T> child) {
		if (this.children == null) this.children = new ArrayList<>();
		this.children.add(child);// .addSibling(childNode);
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public T getData() {
		return data;
	}

	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}

	public void setData(T data) {
		this.data = data;
	}
}
