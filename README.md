# Slack Gardener

The Slack Gardener provides two primary functions:

* scan all public channels, warn each inactive channel, before then subsequently archiving them.
* scan all user profiles, and inform them if they're missing certain key fields

It also provides some openfeign api implementations on top of Slack's methods api

## Motivation

The Slack Gardener helps cultivate your slack instance, by pruning inactive channels and ensuring people's profiles are filled out.

By pruning inactive channels, it allows people fresh to joining your slack that have questions/answers/comments to be guided towards frequently used channels rather than inactive/stale channels.
This has the benefit improving the visibility of conversations and surfacing information across your organisation.

By ensuring people's profiles are filled out, it can improve the ability of people to identify and find other people both within and outside of slack.

## Requirements

* Slack app will need to be created using [Slack Api Console](https://api.slack.com/apps?new_app=1)
* The slack app will need :
  * a bot user created
  * if you want to use the channel pruning functionality permissions will need to be granted for

    ```none
    channels:history
    channels:read
    channels:write
    incoming-webhook
    bot
    ```

  * if you want to use the profile pruning functionality permissions will need to be granted for

    ```none
    users:read
    users:read.email
    users.profile:read
    incoming-webhook
    bot
    ```

* A slack user will need to be created (or an existing user used) to install the app to the workspace, as well as allow the app to archive channels, as slack currently only lets users have access to this method.

Once the app is installed to the workspace, you will need to provide the Slack Gardener the below oauth access tokens via their respective parameter keys

* OAuth Access Token - "slack.gardener.oauth.access_token"
* Bot User OAuth Access Token - "slack.gardener.bot.oauth.access_token"


## AWS Lambda Installation

Create slack dependencies
1. Create slack user (due to current limitations, users must archive channels rather than bots)
2. Create slack app using [Slack Api Console](https://api.slack.com/apps?new_app=1)
3. Grant slack app permissions (as indicated by requirements above)
4. Add bot user to slack app

Create aws dependencies

1. Run the below commands (reviewing as necessary) to provision aws infrastructure, you may be prompted to create terraform s3 state buckets, if running for the first time, and you may see errors around creating the lambda as the lambda jar isn't present in the s3 bucket

    ```bash
    cd infra/environments/example
    terragrunt plan-all
    terragrunt apply-all
    cd ../../..
    ```

2. Change build.gradle bucketName references to allow gradle to upload the lambda jar to the correct place
3. Upload lambda jar artifact (and hash) to s3 bucket by running

    ```bash
    ./gradlew clean build test jar upload
    ```

4. Create SNS topic subscriptions to send emails to correct groups within your organisation for when the lambda fails (this can't be easily automated using terraform due to the asynchronous nature of confirming email subscriptions)
5. Store the slack app tokens in AWS Parameter Store

    ```bash
    pipenv run aws ssm put-parameter --name "slack.gardener.oauth.access_token" --value "xoxp-TOKEN" --type "SecureString"
    pipenv run aws ssm put-parameter --name "slack.gardener.bot.oauth.access_token" --value "xoxb-TOKEN" --type "SecureString"
    # Done via input json because the awscli v1 tries to auto-fetch any url, this apparently will be fixed in awscli v2
    pipenv run aws ssm put-parameter --cli-input-json '{
      "Name": "slack.gardener.uri",
      "Value": "https://api.slack.com",
      "Type": "String",
      "Description": "url"
    }'
    pipenv run aws ssm put-parameter --name "slack.gardener.idle.months" --value "3" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.warning.wait.weeks" --value "1" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.idle.long.years" --value "1" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.idle.long.channels" --value "annual-conference" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.warning.wait.message" --value 'Hi <!channel>. This channel has been inactive for a while, so I’d like to archive it. This will keep the list of channels smaller and help users find things more easily. If you _do not_ want this channel to be archived, just post a message and it will be left alone for a while. You can archive the channel now using the `/archive` command. If nobody posts in a few days I will come back and archive the channel for you.' --type "String"
    ```

6. Run the below commands (reviewing as necessary) to ensure the lambda jar is present in the correct s3 bucket and the lambda gets created, this should pass with no errors.

    ```bash
    cd infra/environments/example
    terragrunt plan-all
    terragrunt apply-all
    cd ../../..
    ```

## Built with

* [Kotlin](https://kotlinlang.org/)
* [JUnit 5](https://junit.org/junit5/)
* [Feign](https://github.com/OpenFeign/feign)
* [Pipenv](https://github.com/pypa/pipenv)
* [Terraform](https://terraform.io)
* [Terragrunt](https://github.com/gruntwork-io/terragrunt)
* [AWS](https://aws.amazon.com/)
* [Gradle](https://https://gradle.org/)

## Contribute

See [CONTRIBUTING.MD](CONTRIBUTING.MD)

## Credits

* Inspiration for the idea [destalinator](https://github.com/randsleadershipslack/destalinator)
* Original project engineer [Sean Reilly](https://twitter.com/seanjreilly)
