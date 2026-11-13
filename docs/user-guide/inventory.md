# Inventory

[User Guide](README.md) › Inventory

The **Inventory** tab lists what JAM authorizes and verifies, and who is involved. Anyone can read
it. Administrators can change the controls, participation, authorizers and watchers here; the
rest is changed in the database (see [Administration](administration.md#what-is-changed-in-the-database)).

| Page | Lists | Administrators can |
|---|---|---|
| [Controls](#controls) | The credited controls. | Add, edit and remove controls. |
| **Facilities** | Each facility, its manager and its logbooks. | — |
| **Segments** | The active RF segments and their facilities. | — |
| **Destinations** | The active beam destinations and their facilities. | — |
| [Participation](#participation) | Which controls apply to which segments and destinations. | Add or remove a control from a segment or destination. |
| [Authorizers](#authorizers) | Who may change each facility's RF or beam authorization. | Add and remove authorizers. |
| [Watchers](#watchers) | Who is emailed each facility's RF or beam authorization changes. | Add and remove watchers. |
| [Verifiers](#verifiers) | The verification teams, their members and their controls. | — |

## Controls

**Inventory › Controls** lists each credited control's name, description, documents,
verification team and verification frequency ("As Needed" when it has none). Select a team to
see its members.

To change the list, select a row and **Edit** or **Remove**, or select **Add**:

- **Name** and **Description**.
- **Documentation**: links to the control's documents, as `label|url`, separated by commas; for
  example `ASE|https://example.org/ase,Checklist|https://example.org/checklist`.
- **Team**: the verification team responsible for it.
- **Frequency**: how often it must be verified, such as "1 Year". Verifiers use it to set
  expiration dates; JAM doesn't enforce it.

> **Warning**: removing a control removes all of its verifications and their history, for every
> facility.

A new control applies nowhere until it is added on the Participation page.

## Participation

**Inventory › Participation** shows, for the facility you choose, a grid of credited controls
against RF segments (**RF Operations**) and against beam destinations (**Beam Operations**). A ✔
means the control participates: it must be verified for that segment or destination.

Administrators select a cell to add or remove the ✔:

- Adding a control creates its verification as **Not Verified**. The segment or destination
  becomes Not Verified, and so not approved, at once, until the team verifies the control. The
  director's permission stays as it was, so tell the authorizer and the team first.
- Removing a control deletes that verification and its history.

## Authorizers

**Inventory › Authorizers** lists who may change each facility's RF or beam authorization; filter
by facility, operations type or username. An administrator can change any authorization, listed
or not.

Administrators select **Add** and choose the facility, the operations type (RF or BEAM) and the
user, or select a row and **Remove**.

## Watchers

**Inventory › Watchers** lists who is emailed each change to a facility's RF or beam authorization,
including automatic reductions. Watchers need no role: anyone who wants to follow a facility, such
as operations staff, can be one. Ask an administrator to add or remove you; they do it as for
authorizers.

## Verifiers

**Inventory › Verifiers** lists each verification team, its members, and the credited controls it
is responsible for. Administrators also see the team's directory role. A team's members are the
users with that role in JLab's user directory, so membership is changed there, not in JAM. Members
can verify the team's controls and get the team's emails.
