# JLab Authorization Manager User Guide

The JLab Authorization Manager (JAM) records the Director of Operations' **authorization** to run
each of Jefferson Lab's facilities: which **RF segments** may run high power RF, and which
**beam destinations** may take beam, in which beam mode and up to what current. It also tracks
the **credited controls** that must be in place for each segment and destination, and the
**verification** of each control by its responsible team. An operation is approved only while
the director's permission and all of its controls' verifications are current; JAM revokes
permission when either expires, or when a team downgrades a control, and tells the people
concerned by email and logbook.

At JLab, JAM is at [ace.jlab.org/jam](https://ace.jlab.org/jam). You can view it without logging
in; log in to make changes.

The [guide's website](https://jeffersonlab.github.io/jam/) has a copy for each release; pick the
version the Help tab shows. On GitHub, this guide describes the `main` branch, which may be newer
than the version you are using.

## Contents

1. [Concepts](concepts.md): facilities, RF segments and beam destinations, credited controls and
   verification teams, the director's permission, approval, expiration and roles.
2. [Authorizations](authorizations.md): reading a facility's authorization, and giving or
   changing permission. For operators and authorizers.
3. [Verifications](verifications.md): checking control status, and verifying or downgrading
   controls. For verification teams.
4. [Expirations and notifications](notifications.md): when permission and verifications expire,
   and the emails and log entries JAM sends.
5. [Inventory](inventory.md): the controls, facilities, segments, destinations, participation,
   authorizers, watchers and verification teams.
6. [Administration](administration.md): the Setup tab, settings and the administrator's tasks.
   For administrators.

## Tabs at a glance

| Tab | What it is for |
|---|---|
| **Authorizations** | Each facility's current authorization for RF and beam operations, and its history. Authorizers change it here. |
| **Verifications** | The verification status of each credited control, segment and destination. Verification teams verify controls here. |
| **Inventory** | The credited controls and where they apply, the facilities, segments and destinations, and who authorizes, watches and verifies. |
| **Setup** | Administrators only: settings and the directory cache. |
| **Help** | Version, contacts, documentation and a feedback form. |

## Quick start

**Operators**: open **Authorizations** and pick the facility. Operate a segment or destination
only if its **Auth** column shows a green thumbs-up, within the beam mode and current limit shown,
until the expiration shown. See [Reading an authorization](authorizations.md#reading-an-authorization).

**Authorizers**: on your facility's page, select **Edit Beam Operations** or **Edit RF
Operations**, set each row, write the change notes, and **Save**. See
[Changing an authorization](authorizations.md#changing-an-authorization).

**Verification teams**: on **Verifications**, open a control with its arrow (→), select the rows
you checked, choose **Edit Verification**, and set the status, dates and who verified it. See
[Verifying a control](verifications.md#verifying-a-control).

## Questions and changes

Use the feedback form on the Help tab, or contact the content and technical contacts listed
there. To suggest a change to this guide, open an issue or pull request on
[GitHub](https://github.com/JeffersonLab/jam).

JAM follows the Accelerator Safety Envelopes (ASE) and the Safety Assessment Document (SAD),
which define the credited controls; at JLab see the
[CEBAF-LERF ASE](https://jlabdoc.jlab.org/docushare/dsweb/Get/Document-187898),
[UITF ASE](https://jlabdoc.jlab.org/docushare/dsweb/Get/Document-203051) and
[SAD](https://jlabdoc.jlab.org/docushare/dsweb/Get/Document-21395). Where this guide and those
documents differ, the documents rule.
