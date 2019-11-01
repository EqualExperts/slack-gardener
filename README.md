# Slack Gardener

![avatar](/docs/avatar/Avatar-256.png) [--> source](/docs/avatar)

> _TL;DR Archiving inactive channels improves the visibility of conversations and makes it easier for new joiners to find channels that are relevant to them. Ensuring complete Slack profiles facilitates people finding each other, within and outside Slack._

This app provides one **primary function**:

* Scan all public channels, warn each inactive channel, before subsequently archiving them.

It also includes some **extra features**:

* A way to message Slack users that don't have specific fields filled out such as a profile picture
* A way to bulk import a list of users by email into a channel (`ChannelMemberInviter`)
* A way to export a list of names of channel members to a `csv` format (`ChannelMemberExportToCsv`)
* A way to bulk rename channels (`ChannelRenamer`)
* A set of `OpenFeign` implementations on top of a subset of the Slack APIs

## Installation - Part 1 (Slack)

### Create the app

1. Go to [Slack Api Console](https://api.slack.com/apps?new_app=1)
2. Create a new app and give it a name
3. Scroll down to **Display Information** and fill App name, description and avatar

### Give permissions

1. In the left sidebar, navigate to **Features > OAuth & Permissions > Scopes**
2. If you want to use the *channel pruning* feature, then grant the following permissions:

    ```none
    channels:history
    channels:read
    channels:write
    incoming-webhook
    bot
    ```

3. If you want to use the *profile pruning* feature, then grant the following permissions:

    ```none
    users:read
    users:read.email
    users.profile:read
    incoming-webhook
    bot
    ```

### Assign a bot user to the app

> ℹ️ _This app needs a bot user to interact with users in a more conversational manner. You need to create one before installing the app. This is because Slack currently only lets (bot) users archive channels._

1. In the left sidebar, navigate to **Features > Bot Users**
2. Click **Add User**
3. Choose a display name (e.g. `Slack Gardener`) and a username (e.g. `slack_gardener`)
4. Leave **Always Show My Bot as Online** as `OFF`.

### Install the app

1. In the left sidebar, navigate to **Settings > Install App**
2. Click **Install App to Workplace**
3. Review the permissions you are granting to this app
4. When asked **Where should Slack Gardener post?**, select a channel used by Slack admins or the maintaining this app

> ℹ️ _This is usually an `#ask-slack`, a channel used by Slack admins to discuss Slack stuff or clarify doubts about Slack coming from anyone in your organisation. In practice this app will post to all the channels it needs._

### Save your app tokens

1. In the left sidebar, navigate to **Settings > Install App > OAuth Tokens for Your Team**
2. Save your **OAuth Access Token**, something like `xoxp-*`
3. Save your **Bot User OAuth Access Token**, something like `xoxb-*`

You will need these tokens in the next steps.

## Installation - Part 2 (AWS)

### Get access to AWS

1. Get an account to your organisation's **AWS Management Console**.
2. [Login](https://signin.aws.amazon.com/signin?redirect_uri=https%3A%2F%2Fconsole.aws.amazon.com%2Fconsole%2Fhome%3Fstate%3DhashArgs%2523%26isauthcode%3Dtrue&client_id=arn%3Aaws%3Aiam%3A%3A015428540659%3Auser%2Fhomepage&forceMobileApp=0).

### Create an S3 Bucket

1. Navigate to [S3](https://s3.console.aws.amazon.com/s3/home)
2. Click **Create bucket**
3. Give it a name (e.g. `slack-gardener-bucket`) and click **Create**

### Create an SNS topic

1. [Create it](https://docs.aws.amazon.com/sns/latest/dg/welcome.html)
2. _TODO: detail how_
3. Save the name (e.g. `slack-gardener-sns`)

### Configure the provision scripts

1. Duplicate the `/infra/environments/example/` folder and rename it (e.g. `live`)
2. Open the new folder
3. Edit `terragrunt.hcl` by replacing all placeholders:
   * `INSERT_BUCKET_STATE_NAME` is the name of the AWS S3 Bucket you created above
   * `INSERT_ACCOUNT_NUMBER` is the AWS account number used to create the S3 Bucket
4. Edit `slack_lambda/terragrunt.hcl` by replacing all placeholders:
   * `INSERT_BUCKET_ARTEFACT_NAME` is the name of the bucket for where the lambda zips will be stored (the module will create this)
   * `INSERT_LOG_BUCKET_NAME` is the name of the bucket for where the lambda log backups will be stored (the module will create this)
   * `INSERT_LAMBDA_NAME` is the desired name for the lambda  (e.g. `slack-gardener-lambda`)
   * `INSERT_LAMBDA_DISPLAY_NAME` is the desired display name for the lambda (e.g. `Slack Gardener`)
   * `INSERT_LAMBDA_DESCRIPTION` is the desired description for the lambda (e.g. `Slack Gardener Lambda warns and archives inactive slack channels`)
   * `INSERT_SNS_TOPIC` is the name of the SNS topic create above (Amazon CloudWatch will send alerts about the lambda)

### Configure the build scripts

1. In the root of this repo, duplicate `build.example.gradle` and rename it to `build.gradle`
2. Edit `build.gradle`
   * Replace all occurrences of `INSERT_BUCKET_ARTEFACT_NAME` with the value used in the steps above

### Install dependencies

```bash
brew install pipenv terraform terragrunt gradle
```

### Choose the right version of Java

1. You need Java 8 to run the next step. Check your current version with `java -version`.
   * If you have it, great, skip to the next step.
   * If not, you might want to use [`jenv`](http://www.jenv.be/) to switch Java versions:

    ```bash
    brew install jenv
    # configure jenv
    echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.bash_profile
    echo 'eval "$(jenv init -)"' >> ~/.bash_profile
    jenv enable-plugin export
    exec $SHELL -l
    # restart bash to load new configs
    source ~/.bash_profile
    # install java and add it to jenv
    brew cask install java8
    jenv add $(/usr/libexec/java_home)
    # list all java versions installed on your machine
    ls -1 /Library/Java/JavaVirtualMachines
    # add java 8 to jenv
    jenv add /Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/
    # set project version to 8
    jenv local 1.8.0.202
    ```

### Provision infra

> ℹ️ _These scripts will provision AWS infrastructure for you._

1. Run these commands

    ```bash
    cd infra/environments/<name-of-your-folder>
    terragrunt plan-all
    terragrunt apply-all
    ```

    > ℹ️ _Terraform creates everything async. Since we have some dependencies, if a child is faster than it's parent you get an error. Just run `plan-all` and `apply-all` again._

2. When it complains about a missing lambda...

    > ℹ️ _You may see errors around creating the lambda as the lambda jar isn't present in the S3 bucket._

3. Build and upload lambda jar (artifact and hash) to AWS

    ```bash
    ./gradlew clean build test jar upload
    ```

4. Repeat steps 1 and 3 until it works (good luck ☘️😅)
5. Send the Slack app tokens to AWS as parameter store

    ```bash
    pipenv run aws ssm put-parameter --name "slack.gardener.oauth.access_token" --value "xoxp-TOKEN" --type "SecureString"
    pipenv run aws ssm put-parameter --name "slack.gardener.bot.oauth.access_token" --value "xoxb-TOKEN" --type "SecureString"
    ```

6. Send other configurations to AWS as parameter store

    ```bash
    pipenv run aws ssm put-parameter --name "slack.gardener.idle.months" --value "3" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.warning.wait.weeks" --value "1" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.idle.long.years" --value "1" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.idle.long.channels" --value "annual-conference" --type "String"
    pipenv run aws ssm put-parameter --name "slack.gardener.warning.wait.message" --value 'Hi <!channel>. This channel has been inactive for a while, so I’d like to archive it. This will keep the list of channels smaller and help users find things more easily. If you _do not_ want this channel to be archived, just post a message and it will be left alone for a while. You can archive the channel now using the `/archive` command. If nobody posts in a few days I will come back and archive the channel for you.' --type "String"
    # Done via input json because the awscli v1 tries to auto-fetch any url, this apparently will be fixed in awscli v2
    pipenv run aws ssm put-parameter --cli-input-json '{
      "Name": "slack.gardener.uri",
      "Value": "https://api.slack.com",
      "Type": "String",
      "Description": "url"
    }'
    ```

7. Decide if you want the "archive inactive channels" feature and if it
   should be in dry run mode, by running the relevant commands below:

    ```bash
    # Enable
    pipenv run aws ssm put-parameter --name "slack.gardener.channel.checking" --value "true" --type "String"
    # Disable
    pipenv run aws ssm put-parameter --name "slack.gardener.channel.checking" --value "false" --type "String"
    # Dry Run On
    pipenv run aws ssm put-parameter --name "slack.channel.dryrun" --value "true" --type "String"
    # Dry Run Off
    pipenv run aws ssm put-parameter --name "slack.channel.dryrun" --value "false" --type "String"
    ```

8. Decide if you want the "enforce profile picture" feature and if it
   should be in dry run mode, by running the relevant commands below:

    ```bash
    # Enable
    pipenv run aws ssm put-parameter --name "slack.gardener.profile.checking" --value "true" --type "String"
    # Disable
    pipenv run aws ssm put-parameter --name "slack.gardener.profile.checking" --value "false" --type "String"
    # Dry Run On
    pipenv run aws ssm put-parameter --name "slack.profile.dryrun" --value "true" --type "String"
    # Dry Run Off
    pipenv run aws ssm put-parameter --name "slack.profile.dryrun" --value "false" --type "String"
    ```

## Installation - Part 3

1. CELEBRATE! 🎉🥳🎊

If you got to this point everything should be working. How can you be sure?

1. _TODO: write steps to validate installation_

## Motivation

**TL;DR Archiving inactive channels improves the visibility of conversations and makes it easier for new joiners to find channels that are relevant to them. Ensuring complete Slack profiles facilitates people finding each other, within and outside Slack.**

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

## Built with

* [Kotlin](https://kotlinlang.org/)
* [JUnit](https://junit.org/junit5/)
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
