#!/usr/bin/env bash

sbt clean update scalafmtAll compile scalafmtCheckAll coverage test it/test coverageOff coverageReport
