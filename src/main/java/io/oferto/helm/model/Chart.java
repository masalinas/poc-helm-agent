package io.oferto.helm.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Chart {
	private String name;
	private String version;
	private String app_version;
	private String description;
}
