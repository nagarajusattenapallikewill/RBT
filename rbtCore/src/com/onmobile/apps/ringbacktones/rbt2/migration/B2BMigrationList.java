package com.onmobile.apps.ringbacktones.rbt2.migration;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.onmobile.apps.ringbacktones.content.database.ID2CMigration;

@Repository(value="b2bMigrationList")
public class B2BMigrationList {
	
	private List<ID2CMigration>  migrationList = null;

	public List<ID2CMigration> getMigrationList() {
		return migrationList;
	}

	public void setMigrationList(List<ID2CMigration> migrationList) {
		this.migrationList = migrationList;
	}
	

}
