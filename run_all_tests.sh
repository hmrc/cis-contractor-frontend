#!/usr/bin/env bash

sbt clean scalafmtAll compile scalafmtCheckAll coverage test it/test coverageOff coverageReport
