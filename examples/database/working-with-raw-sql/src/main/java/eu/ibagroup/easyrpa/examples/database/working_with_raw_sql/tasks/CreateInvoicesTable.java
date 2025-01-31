package eu.ibagroup.easyrpa.examples.database.working_with_raw_sql.tasks;

import eu.ibagroup.easyrpa.engine.annotation.ApTaskEntry;
import eu.ibagroup.easyrpa.engine.apflow.ApTask;
import eu.ibagroup.easyrpa.openframework.database.DatabaseService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
@ApTaskEntry(name = "Create Invoices Table")
public class CreateInvoicesTable extends ApTask {

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS invoices(" +
            "id  SERIAL PRIMARY KEY, " +
            "invoice_number integer NOT NULL, " +
            "invoice_date date, " +
            "customer_name character varying(45), " +
            "amount double precision, " +
            "outdated boolean default FALSE" +
            ");";
//            ") TABLESPACE pg_default;\n" +
//            "ALTER TABLE rpa.invoices OWNER to postgres;";

    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {

        log.info("Creating of new table using SQL statement.");
        dbService.withConnection("testdb", (c) -> {
            c.executeUpdate(CREATE_TABLE);
        });

        log.info("Table is created successfully.");
    }
}
