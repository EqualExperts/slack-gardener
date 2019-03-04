# Contributing to Slack Gardener

First off, thanks for taking the time to contribute!

The following is a set of mostly guidelines, not rules for contributing to Slack Gardener.
Use your best judgment, and feel free to propose changes to this document in a pull request.

#### Table Of Contents

[How Can I Contribute?](#how-can-i-contribute)

* [Reporting Bugs](#reporting-bugs)
* [Suggesting Enhancements](#suggesting-enhancements)
* [Pull Requests](#pull-requests)

[Styleguides](#styleguides)

* [Git Commit Messages](#git-commit-messages)
* [Kotlin Styleguide](#kotlin-styleguide)

## How Can I Contribute?

### Reporting Bugs

This section guides you through submitting a bug report for Slack Gardener. Following these guidelines helps maintainers and the community understand your report, reproduce the behavior, and find related reports.

Before creating bug reports, please check [this list](#before-submitting-a-bug-report) as you might find out that you don't need to create one. When you are creating a bug report, please [include as many details as possible](#how-do-i-submit-a-good-bug-report). Fill out [the required template](.github/ISSUE_TEMPLATE/bug_report.md), the information it asks for helps us resolve issues faster.

> **Note:** If you find a **Closed** issue that seems like it is the same thing that you're experiencing, open a new issue and include a link to the original issue in the body of your new one.

#### Before Submitting A Bug Report

* **Perform a [cursory search](https://github.com/EqualExperts/ee-slack-gardener/issues?&q=is%3Aissue)** to see if the problem has already been reported. If it has **and the issue is still open**, add a comment to the existing issue instead of opening a new one.

#### How Do I Submit A (Good) Bug Report?

Bugs are tracked as [GitHub issues](https://guides.github.com/features/issues/). Provide the following information by filling in [the template](.github/ISSUE_TEMPLATE/bug_report.md).

Explain the problem and include additional details to help maintainers reproduce the problem:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible. For example, start by explaining the execution environment Slack Gardener is working in, e.g. AWS Lambda/GCP Functions/Running Locally. When listing steps, **don't just say what you did, but explain how you did it**. For example, if started locally, was it via running a jar on the command line? in debug mode of IntelliJ?
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples. If you're providing snippets in the issue, use [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **If you're reporting that Slack Gardener crashed**, include logs, and if possible a stack trace. Include the logs in the issue in a [code block](https://help.github.com/articles/markdown-basics/#multiple-lines), a [file attachment](https://help.github.com/articles/file-attachments-on-issues-and-pull-requests/), or put it in a [gist](https://gist.github.com/) and provide link to that gist.
* **If the problem is related to performance or memory**, include how to reproduce in detail, as well as any tests that can reproduce the issue.

Provide more context by answering these questions:

* **Did the problem start happening recently** (e.g. after updating to a new version of Slack Gardener) or was this always a problem?
* If the problem started happening recently, **can you reproduce the problem in an older version of Slack Gardener?** What's the most recent version in which the problem doesn't happen? You can compile older git hashes of Slack Gardener from this repo, [Git Bisect](https://git-scm.com/book/en/v2/Git-Tools-Debugging-with-Git) is useful for this.
* **Can you reliably reproduce the issue?** If not, provide details about how often the problem happens and under which conditions it normally happens.

Include details about your configuration and environment:

* **Which version of Slack Gardener are you using?** Git hashes are preferred, but jar artifacts are also helpful if available
* **What's execution environment your running the Slack Gardener in**?
* **What configs are you using with the Slack Gardener (please don't include slack access tokens)?** Provide the contents of those configs, preferably in a [code block](https://help.github.com/articles/markdown-basics/#multiple-lines) or with a link to a [gist](https://gist.github.com/).

### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion for Slack Gardener, including completely new features and minor improvements to existing functionality. Following these guidelines helps maintainers and the community understand your suggestion :pencil: and find related suggestions.

Before creating enhancement suggestions, please check [this list](#before-submitting-an-enhancement-suggestion) as you might find out that you don't need to create one. When you are creating an enhancement suggestion, please [include as many details as possible](#how-do-i-submit-a-good-enhancement-suggestion). Fill in [the template](.github/ISSUE_TEMPLATE/Feature_request.md), including the steps that you imagine you would take if the feature you're requesting existed.

#### Before Submitting An Enhancement Suggestion

* **Perform a [cursory search](https://github.com/EqualExperts/ee-slack-gardener/issues?&q=is%3Aissue)** to see if the enhancement has already been suggested. If it has, add a comment to the existing issue instead of opening a new one.

#### How Do I Submit A (Good) Enhancement Suggestion?

Enhancement suggestions are tracked as [GitHub issues](https://guides.github.com/features/issues/).

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include copy/pasteable snippets which you use in those examples, as [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Explain why this enhancement would be useful** to most Slack Gardeners installations.
* **Specify which version of Slack Gardener you're using.** Git hashes are preferred
* **Specify the execution environment you're using.**

#### Local development

Slack Gardener can be developed locally. Instructions on how to setup this up are:

* Pull down the repo
* Run the below on the command line to pull down the necessary dependencies and setup the idea files

  ```bash
  ./gradlew clean build test idea
  ```

* Import into IntelliJ Idea, via normal import process

### Pull Requests

The process described here has several goals:

* Maintain and improve Slack Gardener's quality
* Fix problems that are important to users
* Engage the community in working toward the best possible Slack Gardener
* Enable a sustainable system for Slack Gardener's maintainers to review contributions

Please follow these steps to have your contribution considered by the maintainers:

1. Follow all instructions in [the template](.github/PULL_REQUEST_TEMPLATE)
2. Follow the [styleguides](#styleguides)
3. After you submit your pull request, verify that all [status checks](https://help.github.com/articles/about-status-checks/) are passing <details><summary>What if the status checks are failing?</summary>If a status check is failing, and you believe that the failure is unrelated to your change, please leave a comment on the pull request explaining why you believe the failure is unrelated. A maintainer will re-run the status check for you. If we conclude that the failure was a false positive, then we will open an issue to track that problem with our status check suite.</details>

While the prerequisites above must be satisfied prior to having your pull request reviewed, the reviewer(s) may ask you to complete additional design work, tests, or other changes before your pull request can be ultimately accepted.

## Styleguides

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Reference issues and pull requests liberally after the first line

### Kotlin Styleguide

* Slack Gardener currently uses IntelliJ IDEA's default kotlin styleguide

### Documentation Styleguide

* Use [Markdown](https://guides.github.com/features/mastering-markdown/#GitHub-flavored-markdown).
