package eu.ibagroup.easyrpa.examples.googlesheets.sheets_manipulating.tasks;

import eu.ibagroup.easyrpa.engine.annotation.ApTaskEntry;
import eu.ibagroup.easyrpa.engine.annotation.Configuration;
import eu.ibagroup.easyrpa.engine.apflow.ApTask;
import eu.ibagroup.easyrpa.openframework.googlesheets.GoogleSheets;
import eu.ibagroup.easyrpa.openframework.googlesheets.spreadsheet.GSheet;
import eu.ibagroup.easyrpa.openframework.googlesheets.spreadsheet.Spreadsheet;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@ApTaskEntry(name = "Activate Specific Sheet")
@Slf4j
public class ActivateSpecificSheet extends ApTask {

    @Configuration(value = "spreadsheet.id")
    private String spreadsheetId;

    @Inject
    private GoogleSheets service;

    @Override
    public void execute() {
        int sheetIndex = 2;
        String sheetName = "2";

        log.info("Activate sheet with name '{}' for spreadsheet document with id: {}", sheetName, spreadsheetId);
        Spreadsheet spreadsheet = service.getSpreadsheet(spreadsheetId);
        GSheet activeGSheet = spreadsheet.getActiveSheet();

        log.info("Active sheet before any action: {}", activeGSheet.getName());

        log.info("Activate sheet.");
        activeGSheet = spreadsheet.selectSheet(sheetName);

        log.info("Active sheet after activation: {}", activeGSheet.getName());

        log.info("Active sheet using index {}.", sheetIndex);
        activeGSheet = spreadsheet.selectSheet(sheetIndex);

        log.info("Active sheet after activation: {}", activeGSheet.getName());
    }
}
