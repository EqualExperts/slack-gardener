# Service Name: Slack Gardener

## Service Information

* Service Owner: Equal Experts Slack Admins

* Service Description: The slack gardener removes inactive channels in a slack instance. This helps improve discoverability of active channels, guiding slack users towards channels with active members. 

* Service Contact Information:

    * Slack channel: #ask-ee-slack
    * Issue escalation contact point: @adam 

* Service Dashboards:

    * [Cloud Watch Dashboard](https://eu-west-1.console.aws.amazon.com/cloudwatch/home?region=eu-west-1#dashboards:name=ee-slack-gardener-dashboard)

* Service Deployed Version Information: 

    * Build artifacts are stored in S3 are versioned for posterity
    * AWS Lambda is versioned 

## Technical information:

* Service Dependencies:

    * Slack API
    * AWS Lambda: used for app orchestration
    * AWS SSM Parameter Store: used for secret storage of API keys
    * AWS Cloud Watch Event Rules: used for triggering lambda
    * AWS Cloud Watch Metrics: used for triggering alert emails to SNS
    * AWS Cloud Watch Log Groups: Log Storage 
    * AWS SNS: Used to send metric alert emails to slack admins
    * AWS S3: Used to store app artifacts, as well as old lambda's logs

* Configuration:

    * "slack.gardener.oauth.access_token" and "slack.gardener.bot.oauth.access_token" must be both present in the AWS SSM Parameter store, as the lambda will request these at the start of execution to connect to the relevant slack api's

* Other Information:

    * Build Information: `./gradlew clean build test jar upload`
    * Deploy Information: `cd infra/environments/prod && terragrunt plan-all --terragrunt-non-interactive && terragrunt apply-all --terragrunt-non-interactive`

## Service alerts:

  * Alert name: `ALARM: "Slack Gardener" in EU (Ireland)`
      * Why do we have this alert?
        * It fires when lambda error count goes past 0
      * What does it mean when the alert fires? 
        * A lambda invocation has failed.
      * What is the impact to customers?
        * Slack channels may or may not have been processed for the day, depending on when the lambda failed.
      * What typically is the cause of the alert?
        * This is a catch all for any issue with the lambda app, so no typical cause
      * How can this alert be fixed? (A person who hasn't worked on the system should be able to follow these steps to remeadiate the alert)
        * Check this dashboard's log widget
        * Determine source of lambda failure