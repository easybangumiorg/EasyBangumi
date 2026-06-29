---
name: easybangumi-repository-test
description: Use when testing the Pure Pure Bangumi repository feature specifically, especially flows under com.heyanle.easybangumi4.ui.source_manage.repository, including repository add/remove, repository index refresh, repository entry rendering, script installation, and repository-specific boundary cases such as duplicate repositories, invalid jsonl lines, empty URLs, incompatible scripts, and failed downloads.
---

# Easybangumi Repository Test

## Overview

This skill is for the current repository feature only. Use it when the target is the repository page, repository manager dialog, repository index format, or repository installation behavior.

## Scope

Target code areas:

- `com.heyanle.easybangumi4.ui.source_manage.repository`
- `com.heyanle.easybangumi4.plugin.source.repository`

Primary user path:

1. `更多`
2. `番源管理`
3. `番源仓库`
4. `添加仓库`
5. Add repository URL
6. Close dialog
7. Validate rendered entries
8. Install target entry
9. Validate result dialog and downstream state

## Setup

Prefer using the common helper script from the sibling skill:

- `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh serve <mock-dir> 18080`
- `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh reverse 18080`
- `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh clear com.heyanle.easybangumi4.debug`
- `./skills/easybangumi-ui-test/scripts/easybangumi_test.sh launch com.heyanle.easybangumi4.debug`

Use repository indexes as `jsonl`, one `RepositoryEntry` per line.

## Repository Mock Rules

Each valid line should match the repository entry shape:

```json
{"key":"repo.test.success","label":"仓库测试-成功安装","version":"1.0","versionCode":100,"url":"http://127.0.0.1:18080/age.js","describe":"用于验证兼容脚本可成功安装"}
```

Useful mock variants:

- Valid compatible script entry
- Valid incompatible script entry
- Empty `url`
- Missing script target
- Mixed file with invalid line + valid line
- Empty repository file

## Test Cases

### 1. Empty State

- Precondition: app data cleared, no repository configured.
- Steps: enter `番源仓库`.
- Expect: page shows empty-state copy equivalent to `没有番剧源`.

### 2. Add Valid Repository

- Precondition: mock `repo-success.jsonl` available.
- Steps:
  1. Open `添加仓库`
  2. Input repository URL
  3. Tap `确定`
  4. Close dialog
- Expect:
  - dialog list contains the repository URL
  - repository page renders the entry from the index

### 3. Duplicate Repository URL

- Precondition: repository URL already added once.
- Steps: add the same URL again.
- Expect:
  - repository manager list still contains one URL only
  - no duplicate entry created

### 4. Successful Installation

- Precondition: repository contains a script compatible with current app lib version.
- Steps: tap `安装`.
- Expect:
  - device requests the script from mock server
  - success dialog appears
  - dialog text follows `<label> installed`

### 5. Incompatible Script

- Precondition: repository entry points to an older or unsupported script.
- Steps: tap `安装`.
- Expect:
  - download request is sent
  - install fails with compatibility error
  - result mentions unsupported `libVersion`

### 6. Empty Download URL

- Precondition: repository entry has `url: ""`.
- Steps: tap `安装`.
- Expect:
  - no HTTP request for script body is needed
  - error dialog says `Source download URL is empty`

### 7. Missing Script Target

- Precondition: repository entry points to a nonexistent script path.
- Steps: tap `安装`.
- Expect:
  - mock server receives request for missing path
  - failure dialog appears
- Note:
  current behavior may surface as parse failure such as `source metadata is incomplete` if the error page body is saved locally and parsed as a script.

### 8. Mixed jsonl Content

- Precondition: repository file contains at least one invalid line before a valid line.
- Steps: add the repository and refresh if needed.
- Expect:
  - invalid lines are ignored
  - valid lines still render
- If actual result is “no entry rendered”, inspect repository parsing and line-level error handling first.

### 9. Empty Repository File

- Precondition: repository file is empty.
- Steps: add repository and return to page.
- Expect:
  - no crash
  - page remains empty or unchanged
  - repository URL can still appear in manager list

## What To Compare Against Code

When runtime and expectation diverge, inspect these likely points first:

- repository add dedupe: `RepositoryManageViewModel.addRepository`
- index fetch and parse: `RepositoryController.fetchRepoEntries`
- install precheck: blank URL guard in `RepositoryController.installSource`
- download/install path: `RepositoryController.installSource`

## Reporting

Report repository cases in this order:

1. Blocking failures in the happy path
2. Data-loss or silent-ignore behavior
3. Error message mismatches
4. Boundary cases that passed as expected
