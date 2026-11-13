# Concepts

[User Guide](README.md) › Concepts

## Facilities and operations

A **facility** is a machine or test area JAM authorizes, such as CEBAF, LERF, UITF, CMTF or VTA.
Each facility has a page on the **Authorizations** tab, a manager who is emailed about it, and the
logbooks JAM posts its changes to.

JAM authorizes two **operations types** in each facility, separately:

- **RF operations**, by **RF segment**: a part of the facility that runs RF, such as CEBAF's
  Injector, North Linac and South Linac, or a whole test facility ("Entire Facility"). The
  permission is whether the segment may run **high power RF**.
- **Beam operations**, by **beam destination**: a dump, hall or other place beam may be sent, such
  as Hall A or the Inline Dump. The permission is a **beam mode** and a **current limit**.

A facility without segments or destinations has no panel for that type; VTA and CMTF, for
example, have RF segments but no beam destinations.

### Beam modes

The beam modes depend on the facility:

| Facility | Beam modes |
|---|---|
| CEBAF | None, Tune, CW |
| LERF | None, Ceramic Viewer, Viewer Limited, High Duty Cycle, BLM Checkout, CW |
| UITF | None, Viewer Limited, Tune, CW |

**None** means no beam. Each destination has its own current units, normally µA (uA). A
destination with a beam mode but no current limit is **Dump Power Limited**: its current is limited
only by the power its dump can take.

## Credited controls

A **credited control** is an engineered or administrative control that the safety envelope (ASE)
credits for safe operation: shielding, Personnel Safety System (PSS) access and beam containment
controls, oxygen deficiency hazard (ODH) controls, barriers and so on. Each control has a
description, links to its documents, a **verification frequency** (how often it must be checked,
or "As Needed"), and one responsible **verification team**.

A control **participates** in an operation when it is required for that RF segment or beam
destination. Each pairing of a control with a segment or destination has its own
**verification**: a status, when and by whom it was verified, when it expires, comments, an
optional documentation link and, optionally, the components it covers.

### Verification status

| Status | Icon | Meaning |
|---|---|---|
| **Verified** | Green thumbs-up | The team has checked the control for this operation, until the expiration date. |
| **Provisionally Verified** | Yellow warning triangle | The control is accepted with conditions. Permission is still possible, but the authorizer must explain it in a comment. |
| **Not Verified** | Red X | The control isn't verified, or its verification expired or was downgraded. |

A segment's or destination's status is the worst status of the controls that participate in it,
and its expiration is the earliest of their expirations. A segment or destination with no
participating controls counts as Verified. A facility's status for a control is likewise the worst
of that control's verifications in the facility.

## The director's permission

The Director of Operations, or a delegate (an **authorizer**), gives each facility's
**authorization**: for each RF segment, high power RF Yes or No, and for each beam destination, a
beam mode and current limit. Each permission other than None or No has an **expiration date**
and may have a comment. Each save records a new authorization, with the **change notes**, who
authorized it and when, so the history keeps every version.

### Approved operations

An RF segment or beam destination is **approved**, shown by a green thumbs-up in the **Auth**
column of its facility's page, only while both:

1. the director's permission allows it (high power RF is Yes, or the beam mode isn't None), and
2. its credited controls are Verified or Provisionally Verified.

Operators must honor the permission as shown, and stay within its beam mode and current limit.

### Expiration and automatic reduction

Permissions and verifications expire. When a verification expires, JAM sets it to Not Verified,
and when a team downgrades one, JAM keeps the team's new status; either way, JAM sets the
permission of each segment or destination it affects to No or None. When a permission's own
expiration date passes, JAM sets it to No or None too. These **automatic reductions** are saved
as a new authorization, still signed by the last authorizer and marked "Automatically reduced",
and are announced by email and log entry. JAM never raises a permission by itself: restoring it
takes an authorizer.

Anything expiring within seven days is marked **(Expiring Soon)**. See
[Expirations and notifications](notifications.md).

## Roles

| Role | Who | Can |
|---|---|---|
| **Viewer** | Anyone, logged in or not | See every page except Setup. |
| **Operator** | Operations staff | Read and honor the authorizations. Operators need no special role. |
| **Authorizer** | Users listed as a facility's authorizers for RF or beam (**Inventory › Authorizers**) | Change that facility's RF or beam authorization. |
| **Verification team member** | Users with the team's directory role (**Inventory › Verifiers**) | Verify, or downgrade, the controls their team is responsible for. Gets the team's emails. |
| **Watcher** | Users listed as a facility's watchers for RF or beam (**Inventory › Watchers**) | Gets an email each time that authorization changes. |
| **Facility manager** | The manager of a facility (**Inventory › Facilities**) | Gets the facility's change and expiration emails. |
| **Administrator** | Users with the `jam-admin` role | Do everything above for every facility and team, edit the inventory, and use the Setup tab. |

Roles come from JLab's user directory (Keycloak); the authorizer, watcher and manager lists are
kept in JAM.
