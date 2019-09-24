# Slack Gardener

The Slack Gardener provides one primary function:

* scan all public channels, warn each inactive channel, before then subsequently archiving them.

It also has some extra things built in:

* A way to bulk import a list of users by email into a channel (`ChannelMemberInviter`)
* A way to export a list of names of channel members to a `csv` format (`ChannelMemberExportToCsv`)
* A way to bulk rename channels (`ChannelRenamer`)
* A set of `OpenFeign` implementations on top of a subset of the Slack APIs
* A profile checking functionality that messages users that don’t have certain fields filled out such as a profile picture

## Motivation

The Slack Gardener helps cultivate your slack instance, by pruning inactive channels and ensuring people's profiles are filled out.

By ensuring people's profiles are filled out, it can improve the ability of people to identify and find other people both within and outside of slack.

### Why automatically archive slack channels?

**TLDR: By pruning inactive channels, it allows people fresh to joining your slack that have questions/answers/comments to be guided towards frequently used channels rather than inactive/stale channels. This has the benefit improving the visibility of conversations and surfacing information across your organisation.**

We want to maximise the value of the network within Slack, this means we need to ensure conversations and information are easily surfaced, maximising the chance for people in the network to join in.
However to ensure this is manageable for people in the network, we don’t want to have one large channel where everyone is a member and all conversation and information is sent, as this makes it impossible for people to be able to manageably consume information.

Instead we want to adhere to a few principles:

* Maximise the amount of information and conversations being surfaced to people
* Increase the signal to noise ratio of the information and conversations
* Respect the amount of information anyone can tolerate reading
* Optimise for the whole of the global network rather than local optimisations for parts of the network

This means that there need to be a few channels with reserved purposes with high signal to noise ratio to ensure high priority information is surfaced quickly and easily across the network.

It also means that we should, as a guideline, favour broad topic channels over highly specific topic channels. Combined with the ability to join and leave any public channel, it allows people to opt into conversations and information, to the degree of which they are comfortable, whilst maximising the surfacing of conversations and information.

To achieve our goal organically we need to be able to guide the experience of using slack, without negatively impacting it i.e. we need a way to judge the fitness of a channel so we can encourage the channels we want, and discourage the ones we don't, whilst not actively giving people a negative experience on slack.

By looking at the lifecycle of a channel and what we know about the stages, we can find our measure of fitness:

```none
Channel creation

    We don’t have enough information about a channel at this stage to judge if it’ll grow to be a high member count, broad topic, with an ongoing active conversation

Active conversation

    We have some information at this stage

    We know that it’s serving its purpose, as people are using the channel for conversation and information surfacing

    We don’t know if that purpose is short/long-lived

    We don’t know if it’s around a specific topic or a broader topic

    We don’t know anything around it’s signal-to-noise ratio, but if it’s higher noise then people will naturally leave over time.

Inactive conversation

    We have lots of information at this stage

    It’s purpose may have ended

    It’s purpose was short lived, or is incredibly long-lived with long periods of inactivity

    More likely to be a specific topic than a broader topic (broad topics have a higher chance of ongoing active conversation)

    People may have moved away from this channel due to low signal-to-noise ratio

Channel archival

    It has been purposely removed from the list of active channels, slack indicates that the conversation has become stale, it is still searchable, but not to be actively participated in

Channel unarchived (go back to active conversation)

    An active intervention by someone to revive the purpose of a channel.
```

The stage with the most amount of information to act on is inactivity, and this allows us the best chance to shape the use of slack.

As we are programmatically judging a channel's fitness by a metric and not by the actual context of the channel, we want to ensure that anyone with the channel's context can stop the process entirely. So we automatically warn the channel for a period to allow humans with greater context of the channels purpose to stop the channel being archived, if no-one wants to stop the channel being archived, then we automatically archive it.

This removes conversations that have become inactive because it has either naturally finished it's purpose or failed to draw enough of a membership to actively participate, guiding users towards channels with active conversations and larger memberships.

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

Once the app is installed to the workspace, you will need to provide the Slack Gardener the below OAuth access tokens via their respective parameter keys

* OAuth Access Token - `"slack.gardener.oauth.access_token"`
* Bot User OAuth Access Token - `"slack.gardener.bot.oauth.access_token"`

## AWS Lambda Installation

Create slack dependencies

1. Create slack user (due to current limitations, users must archive channels rather than bots)
2. Create slack app using [Slack Api Console](https://api.slack.com/apps?new_app=1)
3. Grant slack app permissions (as indicated by requirements above)
4. Add bot user to slack app

Create AWS dependencies

1. Run the below commands (reviewing as necessary) to provision aws infrastructure, you may be prompted to create terraform s3 state buckets, if running for the first time, and you may see errors around creating the lambda as the lambda jar isn't present in the s3 bucket

    ```bash
    cd infra/environments/example
    terragrunt plan-all
    terragrunt apply-all
    cd ../../..
    ```

2. Change `build.gradle` bucketName references to allow `gradle` to upload the lambda jar to the correct place
3. Upload lambda jar artefact (and hash) to S3 bucket by running

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
