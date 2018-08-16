package de.java2db.entities;

/**
 * @author Collin Alpert
 */
public class BaseCodeAndDescriptionEntity extends BaseEntity {
	private String code;
	private String description;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Code: " + code + ", Description: " + description + ", " + super.toString();
	}
}
