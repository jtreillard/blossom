package fr.blossom.core.scheduler.job;

import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Maël Gargadennnec on 09/05/2017.
 */
public class ScheduledJobServiceImpl implements ScheduledJobService {

  private final static Logger LOGGER = LoggerFactory.getLogger(ScheduledJobServiceImpl.class);
  private final Scheduler scheduler;

  public ScheduledJobServiceImpl(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public void changeState(boolean activate) {
    try {
      if (this.scheduler.isStarted()) {
        if (activate) {
          this.scheduler.start();
        } else {
          this.scheduler.standby();
        }
      }
    } catch (SchedulerException e) {
      LOGGER.error("Cannot change scheduler state", e);
    }

  }

  @Override
  public List<String> getGroups() {
    try {
      return this.scheduler.getJobGroupNames();
    } catch (SchedulerException e) {
      LOGGER.error("Cannot retrieve job group names from quartz scheduler", e);
      return Lists.newArrayList();
    }
  }


  @Override
  public SchedulerInfo getSchedulerInfo() {
    SchedulerInfo schedulerInfo = new SchedulerInfo();
    try {
      schedulerInfo.setName(this.scheduler.getSchedulerName());
      schedulerInfo.setStart(this.scheduler.getMetaData().getRunningSince());
      schedulerInfo.setPoolsize(this.scheduler.getMetaData().getThreadPoolSize());
      schedulerInfo.setStarted(this.scheduler.isStarted());
      schedulerInfo.setStandBy(this.scheduler.isInStandbyMode());
      schedulerInfo.setJobs(this.computeJobs());
      schedulerInfo.setTriggers(this.getTriggers());
    } catch (SchedulerException e) {
      LOGGER.error("Cannot retrieve scheduler info.", e);
    }

    return schedulerInfo;

  }

  private long getTriggers() throws SchedulerException {
    return this.scheduler.getTriggerGroupNames().stream().flatMap(groupName -> {
      try {
        return this.scheduler.getTriggerKeys(groupEquals(groupName)).stream();
      } catch (SchedulerException e) {
        return Stream.of();
      }
    }).count();
  }

  private long computeJobs() throws SchedulerException {
    return this.scheduler.getJobGroupNames().stream().flatMap(groupName -> {
      try {
        return this.scheduler.getJobKeys(groupEquals(groupName)).stream();
      } catch (SchedulerException e) {
        return Stream.of();
      }
    }).count();
  }

  @Override
  public List<JobInfo> getAll(String groupName) {
    List<JobInfo> jobInfos = Lists.newArrayList();
    try {
      Set<JobKey> jobKeys = this.scheduler.getJobKeys(groupEquals(groupName));
      for (JobKey jobKey : jobKeys) {
        jobInfos.add(this.getOne(jobKey));
      }
    } catch (SchedulerException e) {
      LOGGER.error("Cannot retrieve job infos for groupName " + groupName, e);
    }
    return jobInfos;
  }

  @Override
  public JobInfo getOne(JobKey jobKey) {
    try {
      JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
      List<? extends Trigger> triggers = this.scheduler.getTriggersOfJob(jobKey);
      List<JobExecutionContext> jobExecutionContexts = this.scheduler.getCurrentlyExecutingJobs()
        .stream().filter(jec -> jec.getJobDetail().getKey().equals(jobKey))
        .collect(Collectors.toList());

      JobInfo jobInfo = new JobInfo(jobKey);
      jobInfo.setDetail(jobDetail);
      jobInfo.setTriggers(triggers);
      jobInfo.setJobExecutionContexts(jobExecutionContexts);

      return jobInfo;
    } catch (SchedulerException e) {
      LOGGER.error("Cannot retrieve job infos for jobKey " + jobKey, e);
      return null;
    }
  }

  @Override
  public void execute(JobKey jobKey) {
    try {
      scheduler.triggerJob(jobKey);
    } catch (SchedulerException e) {
      LOGGER.error("Cannot execute job for jobKey " + jobKey, e);
    }
  }
}


