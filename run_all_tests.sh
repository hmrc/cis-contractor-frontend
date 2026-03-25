#!/usr/bin/env bash

sbt clean update scalafmtAll columnarFmt compile scalafmtCheckAll coverage test it/test coverageOff coverageReport
