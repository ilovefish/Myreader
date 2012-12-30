package com.example.Types;

import java.io.Serializable;

import com.example.ddd.R;

public class FileInfo implements Serializable{
	private String name = "";
	private boolean isLead = false;
	private String uri = "";
	private int titlePage = R.drawable.bookcase_book_nor_cover; 
	
	public int getTitlePage() {
		return titlePage;
	}
	public void setTitlePage(int titlePage) {
		this.titlePage = titlePage;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isLead() {
		return isLead;
	}
	public void setLead(boolean isLead) {
		this.isLead = isLead;
	}

}
