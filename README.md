
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

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").