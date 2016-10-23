package com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans;



public class RBTMSearchParams {
	
	private String id;
	private String fl;
	private String start;
	private String q;
	private String wt;
	private String rows;
	private String sort;
	private String fq;
	private String qt;
	
	public String getQt() {
		return qt;
	}

	public void setQt(String qt) {
		this.qt = qt;
	}
	
	//Default Constructor
	public RBTMSearchParams() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFl() {
		return fl;
	}

	public void setFl(String fl) {
		this.fl = fl;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getWt() {
		return wt;
	}

	public void setWt(String wt) {
		this.wt = wt;
	}

	public String getRows() {
		return rows;
	}

	public void setRows(String rows) {
		this.rows = rows;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getFq() {
		return fq;
	}

	public void setFq(String fq) {
		this.fq = fq;
	}

	@Override
	public String toString() {
		return "RBTMSearchParams [id=" + id + ", fl=" + fl + ", start=" + start
				+ ", q=" + q + ", wt=" + wt + ", rows=" + rows + ", sort="
				+ sort + ", fq=" + fq + ", qt=" + qt + "]";
	}
}
