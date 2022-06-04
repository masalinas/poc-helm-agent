package io.oferto.helm.tasklets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.vdurmont.semver4j.Semver;

import io.oferto.helm.model.Chart;
import io.oferto.helm.model.Release;

public class HelmTasklet implements Tasklet, InitializingBean {
	private final String HELM_COMMAND = "/opt/homebrew/bin/helm";
	private final String HELM_REPO= "chartmuseum";
	
	private String helmCommand = HELM_COMMAND;
	private String helmRepo = HELM_REPO;
	
	private static Logger LOG = LoggerFactory
		      .getLogger(HelmTasklet.class);
	
	private void configureHelmCommand(ChunkContext chunkContext) {
		String helm_command = (String) chunkContext.getStepContext()
	            .getJobParameters()
	            .get("helmCommand");
		
		if (helm_command != null)
			helmCommand = helm_command;
		
		String helm_repo = (String) chunkContext.getStepContext()
	            .getJobParameters()
	            .get("helmRepo");
		
		if (helm_repo != null)
			helmRepo = helm_repo;
		
		System.out.println("helm command: " + helmCommand + " configured with repo: " + helmRepo);
	}
	
	private String updateChartRepositories() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("sh", "-c", helmCommand + " repo update 2>&1; true");
		Process process = pb.start();
				
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
        StringBuffer output = new StringBuffer();
        String line;    
        
        while ((line = reader.readLine()) != null) {
        	output.append(line + "\n");
        }
        
        return output.toString();
	}
	
	
	private Chart[] getCharts() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("sh", "-c", helmCommand + " search repo " + helmRepo + " -o json 2>&1; true");
		Process process = pb.start();
				
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
        StringBuffer output = new StringBuffer();
        String line;    
        
        while ((line = reader.readLine()) != null) {
        	output.append(line + "\n");
        }
        
        Gson gson = new Gson();
        Chart[] charts = gson.fromJson(output.toString(), Chart[].class); 
        
        return charts;
	}
	
	
	private Release[] getReleases() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("sh", "-c", helmCommand + " list -o json 2>&1; true");
		Process process = pb.start();
				
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
        StringBuffer output = new StringBuffer();
        String line;    
        
        while ((line = reader.readLine()) != null) {
        	output.append(line + "\n");
        }
        
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Release[] releases = gson.fromJson(output.toString(), Release[].class); 
        
        return releases;
	}
	
	private String upgradeRelease(String releaseName, String chartName) throws IOException {
		ProcessBuilder pb = new ProcessBuilder("sh", "-c", helmCommand + " upgrade " + releaseName + " " + chartName + " 2>&1; true");
		Process process = pb.start();
				
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
        StringBuffer output = new StringBuffer();
        String line;    
        
        while ((line = reader.readLine()) != null) {
        	output.append(line + "\n");
        }
        
        return output.toString();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		try {	   
			String result;
			
			LOG.info("STEP00: Configure Helm Command");
			configureHelmCommand(chunkContext);
			
			LOG.info("STEP01: Updating Chart Repositories");
			result = updateChartRepositories();
			System.out.println(result);
			
			LOG.info("STEP02: Get Charts from Helm Chart Repository");
			List<Chart> charts = Arrays.asList(getCharts());
			LOG.info("Exist " + charts.size() + " Charts published");
			System.out.println();
				        
	        LOG.info("STEP03: Get Releases from Kubernetes Cluster");
	        List<Release> releases = Arrays.asList(getReleases());	        
	        LOG.info("Exist " + releases.size() + " Releases deployed");
	        System.out.println();
	        
	        LOG.info("STEP04: List release to be upgrade");
	        for(Release release : releases) {	        		        	
	        	Chart chart = charts.stream().filter(
	        			ch -> {
	        				Semver chartVersion = new Semver(ch.getApp_version());	        				        				
	        				Semver releaseVersion = new Semver(release.getApp_version());
	        				
	        				return ch.getName().split("/")[1].equals(release.getName()) &&
	        					   chartVersion.isGreaterThan(releaseVersion);
	        			})
	        		.findAny()
	        		.orElse(null);
	        	
	        	if (chart != null) {
	        		System.out.println("Updating release" + chart.getName() + " to version: " + chart.getApp_version());
	        		
	        		result = upgradeRelease(release.getName(), chart.getName());
	        		
	        		System.out.println(result);
	        		System.out.println("Release" + chart.getName() + " to version: " + chart.getApp_version() + " Upgraded");
	        	}
	        } 
		} catch (Exception ex) {
			 System.out.println(ex.getMessage());
		}
		
		return RepeatStatus.FINISHED;
	}

}
