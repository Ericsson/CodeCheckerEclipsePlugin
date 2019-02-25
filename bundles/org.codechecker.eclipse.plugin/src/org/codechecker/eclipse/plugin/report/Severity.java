package org.codechecker.eclipse.plugin.report;

public enum Severity {
	STYLE(0), LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4), ANY(5);

	private final int value;

	private Severity(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}