package io.oferto.helm.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Release {
	private String name;
	private String namespace;
	private String revision;
	private Date updated;
	private String status;
	private String chart;
	private String app_version;
	
}
