{
    ReceiveTaskHandler receiveTaskHandler;
    StatefulKnowledgeSession ksession;
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , new SystemOutWorkItemHandler());
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , new SystemOutWorkItemHandler());
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , receiveTaskHandler);
    ProcessInstance processInstance = ksession.startProcess( " a string " );
    assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , new SystemOutWorkItemHandler());
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , new SystemOutWorkItemHandler());
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , receiveTaskHandler);
    assertProcessInstanceCompleted(processInstance.getId(), ksession);
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , new SystemOutWorkItemHandler());
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , new SystemOutWorkItemHandler());
    ksession.getWorkItemManager().registerWorkItemHandler( " a string " , receiveTaskHandler);
    processInstance = ksession.startProcess( " a string " );
    assertProcessInstanceCompleted(processInstance.getId(), ksession);
}    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , receiveTaskHandler);    ProcessInstance processInstance = ksession.startProcess( "  " a string "  " );    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , receiveTaskHandler);    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , receiveTaskHandler);    processInstance = ksession.startProcess( "  " a string "  " );    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , receiveTaskHandler);    ProcessInstance processInstance = ksession.startProcess( "  " a string "  " );    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , receiveTaskHandler);    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  " a string "  " , receiveTaskHandler);    processInstance = ksession.startProcess( "  " a string "  " );}    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , receiveTaskHandler);    ProcessInstance processInstance = ksession.startProcess( "  "  " a string "  "  " );    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , receiveTaskHandler);    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , new SystemOutWorkItemHandler());    ksession.getWorkItemManager().registerWorkItemHandler( "  "  " a string "  "  " , receiveTaskHandler);    processInstance = ksession.startProcess( "  "  " a string "  "  " );
