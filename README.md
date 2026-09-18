cis-contractor-frontend
=======================
![](https://img.shields.io/github/v/release/hmrc/cis-contractor-frontend)

A Scala/Play frontend service for the Construction Industry Scheme (CIS), enabling contractors to manage their subcontractors on the HMRC Tax Platform.

The service supports the following journeys:

* Adding subcontractors — individuals, companies, partnerships, and trusts
* Verifying subcontractor details with HMRC
* Amending existing subcontractor records
* Managing contractor details

The service is bilingual, supporting both English and Welsh.

## Running the service

Start all dependent services via Service Manager:

```shell
sm2 --start CIS_ALL
```

To run locally (default port 6998):

```shell
sbt run
```

## Testing

Run unit tests:

```shell
sbt test
```

Run integration tests:

```shell
sbt it/test
```

Check code coverage (minimum 78% statement coverage required):

```shell
sbt clean coverage test it/test coverageOff coverageReport
```

Before committing, run the full pre-commit script which formats code, compiles, runs all tests, and checks coverage:

```shell
./run_all_tests.sh
```

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
