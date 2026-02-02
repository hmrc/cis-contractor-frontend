
# cis-contractor-frontend

This is the new cis-contractor-frontend repository

## Running the service

Service Manager: `sm2 --start CIS_ALL`


To start the server locally: `sbt run`

## Testing

Run unit tests with:
```shell
sbt test
```

Check code coverage with:
```shell
sbt clean coverage test it/test coverageReport
```

Run integration tests with:
```shell
sbt it/test
```

Before committing code, please ensure all tests pass and code coverage is satisfactory and the code has been formatted
using `sbt scalafmtAll`. Alternatively, you can run the following script in the root directory, which does all
these steps for you:
```shell
./run_all_tests.sh
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").