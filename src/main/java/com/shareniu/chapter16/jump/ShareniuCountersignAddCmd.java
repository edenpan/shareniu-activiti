package com.shareniu.chapter16.jump;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;

import java.util.Date;

/**
 * @author edenpan
 */
public class ShareniuCountersignAddCmd implements Command<Void> {
    protected String executionId;
    protected String assignee;

    public Void execute(CommandContext commandContext){
        ProcessEngineConfigurationImpl pec = commandContext.getProcessEngineConfiguration();

        TaskService taskService = pec.getTaskService();
        IdGenerator idGenerator = pec.getIdGenerator();
        RuntimeService runtimeService = pec.getRuntimeService();
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        ExecutionEntity ee = (ExecutionEntity) execution;
        ExecutionEntity parent = ee.getParent();
        ExecutionEntity newExecution = parent.createExecution();
        newExecution.setActive(true);
        newExecution.setConcurrent(true);
        newExecution.setScope(true);
        Task newTask = taskService.createTaskQuery().executionId(executionId).singleResult();
        TaskEntity t = (TaskEntity) newTask;
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setCreateTime(new Date());
        taskEntity.setTaskDefinition(t.getTaskDefinition());
        taskEntity.setProcessInstanceId(t.getProcessInstanceId());
        taskEntity.setProcessDefinitionId(t.getProcessDefinitionId());
        taskEntity.setTaskDefinitionKey(t.getTaskDefinitionKey());
        taskEntity.setExecutionId(newExecution.getId());
        taskEntity.setName(newTask.getName());
        String taskId = idGenerator.getNextId();
        taskEntity.setId(taskId);
        taskEntity.setExecution(newExecution);
        taskEntity.setAssignee(assignee);
        taskService.saveTask(taskEntity);

        int loopCounter = ShareniuLoopVariableUtils.getLoopVariable(newExecution, "nrOfInstances");
        int nrOfCompletedInstances = ShareniuLoopVariableUtils.getLoopVariable(newExecution, "nrOfActiveInstances");
        ShareniuLoopVariableUtils.setLoopVariable(newExecution, "nrOfInstances", loopCounter + 1);
        ShareniuLoopVariableUtils.setLoopVariable(newExecution, "nrOfActiveInstances", nrOfCompletedInstances + 1);


        return null;
    }

    public ShareniuCountersignAddCmd(String executionId, String assignee){
        this.executionId = executionId;
        this.assignee = assignee;
    }


}
