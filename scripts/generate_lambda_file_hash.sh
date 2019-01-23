#!/usr/bin/env bash
openssl dgst -sha256 -binary build/libs/ee-slack-gardener-0.0.1-SNAPSHOT.jar | openssl enc -base64