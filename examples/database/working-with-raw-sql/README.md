# Working with raw SQL queries

* ### Execute SQL query

```Java
    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {
        String oldDate = DateTime.now().minusYears(3).toString("yyyy-MM-dd");
        String SELECT_INVOICES_SQL = "SELECT * FROM invoices WHERE invoice_date < '%s';";

        log.info("Output invoices which are older than '{}'.", oldDate);
        dbService.withConnection("testdb", (c) -> {
            ResultSet rs = c.executeQuery(String.format(SELECT_INVOICES_SQL, oldDate));
            while (rs.next()) {
            int id = rs.getInt("invoice_number");
            }
        });
    }
```

* ### Execute update statement

```Java
    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {
        String oldDate = DateTime.now().minusYears(3).toString("yyyy-MM-dd");
        String UPDATE_INVOICES_SQL = "UPDATE invoices SET outdated='TRUE' WHERE invoice_date < '%s';";

        log.info("Mark outdated invoices using SQL statement.");
        int res = dbService.withConnection("testdb", (c) -> {
            return c.executeUpdate(String.format(UPDATE_INVOICES_SQL, oldDate));
        });

        log.info("'{}' records have been updated.", res);
    }
```

* ### Execute insert statement

```Java
    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {
        String INSERT_INVOICES_SQL = "INSERT INTO invoices (invoice_number, invoice_date, customer_name, amount) " +
                                     "VALUES " +
                                     "('000001', '2019-07-04', 'At&T', '5000.76'), " +
                                     "('000002', '2012-02-15', 'Apple', '12320.99'), " +
                                     "('000003', '2014-11-23', 'IBM', '600.00'), " +
                                     "('000004', '2011-01-04', 'Verizon', '138.50'), " +
                                     "('000005', '2021-09-01', 'HP', '25600.00');";

        log.info("Adding of new invoice records using SQL statement.");
        int res = dbService.withConnection("testdb", (c) -> {
            return c.executeInsert(INSERT_INVOICES_SQL);
        });

        log.info("'{}' records have been added successfully.", res);
    }
```

* ### Execute delete statement

```Java
    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {
        String DELETE_INVOICES_SQL = "DELETE FROM invoices WHERE outdated='TRUE';";

        log.info("Delete invoices marked as outdated using SQL statement.");
        int res = dbService.withConnection("testdb", (c) -> {
            return c.executeDelete(DELETE_INVOICES_SQL);
        });

        log.info("'{}' records have been deleted.", res);
    }
```

See the full source of this example for more details or check following instructions to run it.

### Running

> :warning: **To be able to build and run this example it's necessary to have an access
>to some instance of EasyRPA Control Server.**

Its a fully workable process. To play around with it and run do the following:
1. Download this example using [link][down_git_link].
2. Unpack it somewhere on local file system.
3. Specify URL to the available instance of EasyRPA Control Server in the `pom.xml` of this example:
    ```xml
    <repositories>
        <repository>
            <id>easy-rpa-repository</id>
            <url>[Replace with EasyRPA Control Server URL]/nexus/repository/easyrpa/</url>
        </repository>
    </repositories>
    ```
4. If necessary, change version of `easy-rpa-engine-parent` in the same `pom.xml` to corresponding version of
   EasyRPA Control Server:
    ```xml
    <parent>
        <groupId>eu.ibagroup</groupId>
        <artifactId>easy-rpa-engine-parent</artifactId>
        <version>[Replace with version of EasyRPA Control Server]</version>
    </parent>
    ```

5. Build it using `mvn clean install` command. This command should be run within directory of this example.
6. Run `main()` method of `WorkingWithRawSqlModule` class.

[down_git_link]: https://downgit.github.io/#/home?url=https://github.com/easyrpa/openframework/tree/main/examples/database/working-with-raw-sql
