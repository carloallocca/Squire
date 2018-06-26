package uk.ac.open.kmi.squire.fca;

public class Lattice<O, A> {

	protected Concept<O, A> bottom, top;

	public Concept<O, A> getBottom() {
		return this.bottom;
	}

	public Concept<O, A> getTop() {
		return this.top;
	}

	public void setBottom(Concept<O, A> concept) {
		this.bottom = concept;
	}

	public void setTop(Concept<O, A> concept) {
		this.top = concept;
	}

}
