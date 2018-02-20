package uk.ac.open.kmi.squire.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author callocca
 * 
 * @param <T>=List<TriplePath>
 *            but the next version is going to be the Query.
 */
public class TreeNodez<T> {

	private List<TreeNodez<T>> children = new ArrayList<>();

	private T data = null;

	public TreeNodez(T data) {
		if (data != null) this.data = data;
	}

	public TreeNodez(T data, List<TreeNodez<T>> children) {
		this(data);
		if (children != null) this.children.addAll(children);
	}

	public void addChild(TreeNodez<T> child) {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		this.children.add(child);// .addSibling(childNode);
	}

	public List<TreeNodez<T>> getChildren() {
		return children;
	}

	public T getData() {
		return data;
	}

	public void setChildren(ArrayList<TreeNodez<T>> children) {
		this.children = children;
	}

	public void setData(T data) {
		this.data = data;
	}
}