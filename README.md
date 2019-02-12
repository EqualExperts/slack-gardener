# Slack Gardener
A slack bot that will scan all public channels, warn each inactive channel, before then subsequently archiving them.

## Motivation
The Slack Gardener helps clean up your inactive channels, so people fresh to joining your slack can have their questions/answers/comments can be guided towards frequently used channels rather than inactive/stale channels.
This has the benefit of driving your slack's conversations to common frequently used channels improving the visibility of conversations and surfacing information across your organisation.


## Requirements
- Slack app will need to be created using [Slack Api Console](https://api.slack.com/apps?new_app=1)
- The slack app will need :
    - a bot user created
    - permissions granted for 
```
channels:history
channels:read
channels:write
incoming-webhook
bot
```
- A slack user will need to be created (or an existing user used) to install the app to the workspace, as well as allow the app to archive channels, as currently slack only lets users have access to this method.

Once the app is installed to the workspace, you will need to provide the gardener the below access tokens 

* OAuth Access Token - "slack.gardener.oauth.access_token"
* Bot User OAuth Access Token - "slack.gardener.bot.oauth.access_token"


## AWS Lambda Installation

Configure app
1. Change configuration values as appropriate in src/main/kotlin/com/equalexperts/slack/gardener/AwsLambda.kt
```
val idleMonths = 3
val warningWeeks = 1
val longIdleYears = 1
val channelWhitelist = setOf("announcements", "random")
val longIdlePeriodChannels = setOf("sk-ee-trip")
val warningMessage = """Hi <!channel>.
                |This channel hasn't been used in a while, so I’d like to archive it.
                |This will keep the list of channels smaller and help users find things more easily.
                |If you _don't_ want this channel to be archived, just post a message and I'll leave it alone for a while.
                |You can archive the channel now using the `/archive` command.
                |If nobody posts in a few days I will come back and archive the channel for you.""".trimMargin().replace('\n', ' ')
```


Creating slack dependencies
1. Create slack user (due to current limitations, users must archive channels rather than bots)
2. Create slack app using [Slack Api Console](https://api.slack.com/apps?new_app=1) 
3. Grant slack app permissions (as indicated by requirements above)
4. Add bot user to slack app

Creating aws dependencies
1. Run the below commands to provision aws infrastructure, you may be prompted to create terraform s3 state buckets, if running for the first time
```
cd infra/environments/prod
terragrunt plan-all
terragrunt apply-all
```
2. Change build.gradle bucketName references to allow gradle to upload the lambda jar to the correct place
3. Upload lambda jar artifact (and hash) to s3 bucket by running
```
./gradlew clean build test jar upload
```
4. Create SNS topic subscriptions to send emails to correct groups within your organisation for when the lambda fails (this can't be easily automated using terraform due to the asynchronous nature of confirming email subscriptions)
5. Store the slack app tokens in AWS Parameter Store
```
pipenv run aws ssm put-parameter --name "slack.gardener.oauth.access_token" --value "xoxp-TOKEN" --type "SecureString"
pipenv run aws ssm put-parameter --name "slack.gardener.bot.oauth.access_token" --value "xoxb-TOKEN" --type "SecureString"
```


## Built with

- [Kotlin](https://kotlinlang.org/)
- [JUnit 5](https://junit.org/junit5/)
- [Feign](https://github.com/OpenFeign/feign)
- [Pipenv](https://github.com/pypa/pipenv)

## Contribute

See [CONTRIBUTING.MD](CONTRIBUTING.MD)

## Credits

- Inspiration for the idea [destalinator](https://github.com/randsleadershipslack/destalinator)
- Original project engineer [Sean Reilly](https://twitter.com/seanjreilly)
 
