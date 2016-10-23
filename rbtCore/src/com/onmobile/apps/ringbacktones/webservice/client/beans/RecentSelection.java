package com.onmobile.apps.ringbacktones.webservice.client.beans;

public class RecentSelection {

	private String classType = null;
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classType == null) ? 0 : classType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RecentSelection other = (RecentSelection) obj;
		if (classType == null) {
			if (other.classType != null)
				return false;
		} else if (!classType.equals(other.classType))
			return false;
		return true;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public String toString(){
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Recent Selection = [");
		strBuilder.append("ClassType = ");
		strBuilder.append(classType);
		strBuilder.append("]");
		return strBuilder.toString();
	}
	
}
