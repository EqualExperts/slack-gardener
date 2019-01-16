#!/usr/bin/env bash
shasum -a 256 < build/libs/ee-slack-gardener-0.0.1-SNAPSHOT.jar | base64
