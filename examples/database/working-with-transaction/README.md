# Working with transaction

* ### Adding of new record to the table within one transaction

```Java
    @Inject
    private DatabaseService dbService;

    @Override
    public void execute() {
        List<Invoice> invoicesToAdd = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Invoice.DB_DATE_FORMAT);

        Invoice invoice = new Invoice(10001, dateFormat.parse("2021-01-22"), "Sony", 4500);
        invoice.addProduct(new Product("TV", 1));
        invoice.addProduct(new Product("Printer LX", 2));
        invoicesToAdd.add(invoice);

        log.info("Adding of new record to the table within one transaction");
        dbService.withTransaction("testdb", (c) -> {
            for (Invoice invoice : invoicesToAdd) {
                c.create(invoice);
                c.create(invoice.getProducts());
            }
        });

        log.info("Records added to the table successfully.");
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
6. Run `main()` method of `WorkingWithTransactionModule` class.

[down_git_link]: https://downgit.github.io/#/home?url=https://github.com/easyrpa/openframework/tree/main/examples/database/working-with-transaction
