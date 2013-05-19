package com.clc3.persistence.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.clc3.persistence.model.api.DatabaseEntity;

@Entity
public class SportData  implements Serializable, DatabaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -439025846500744662L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String team1;
	
	private String team2;
	
	private String result;

	public SportData(String team1, String team2, String result) {
		this.setTeam1(team1);
		this.setTeam2(team2);
		this.setResult(result);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTeam1() {
		return team1;
	}

	public void setTeam1(String team1) {
		this.team1 = team1;
	}

	public String getTeam2() {
		return team2;
	}

	public void setTeam2(String team2) {
		this.team2 = team2;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public Object getPrimaryKey() {
		return id;
	}
}
