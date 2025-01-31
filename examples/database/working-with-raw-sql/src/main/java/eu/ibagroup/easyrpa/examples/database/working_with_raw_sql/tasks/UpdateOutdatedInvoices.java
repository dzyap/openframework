package eu.ibagroup.easyrpa.examples.database.working_with_raw_sql.tasks;

import eu.ibagroup.easyrpa.engine.annotation.ApTaskEntry;
import eu.ibagroup.easyrpa.engine.apflow.ApTask;
import eu.ibagroup.easyrpa.openframework.database.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.inject.Inject;

@ApTaskEntry(name = "Update Outdated Invoices")
@Slf4j
public class UpdateOutdatedInvoices extends ApTask {

    private final String UPDATE_INVOICES_SQL = "UPDATE invoices SET outdated='TRUE' WHERE invoice_date < '%s';";

    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {
        String oldDate = DateTime.now().minusYears(3).toString("yyyy-MM-dd");

        log.info("Mark outdated invoices using SQL statement.");
        int res = dbService.withConnection("testdb", (c) -> {
            return c.executeUpdate(String.format(UPDATE_INVOICES_SQL, oldDate));
        });

        log.info("'{}' records have been updated.", res);
    }
}
