package ar.edu.testdesk.model;

public record Dashboard(long cases, long activeCases, long plans, long openBugs, TestRun lastRun) {}
