# Verifications

[User Guide](README.md) › Verifications

Each [credited control](concepts.md#credited-controls) must be verified, by its team, for each RF
segment and beam destination it participates in. The **Verifications** tab shows where things
stand, and is where teams record their checks.

## The Verifications tab

The tab has three panels, each of which can be collapsed:

- **Credited Controls**: each control, with a section per facility listing its RF segments and
  beam destinations with their status icons. The arrow (→) opens the
  [control's page](#a-controls-page) for that facility.
- **RF Segments**: each segment, with its facility, its status and its earliest control
  expiration. The arrow opens the [segment's page](#a-segments-or-destinations-page).
- **Beam Destinations**: the same for each destination.

**Filter** narrows the tab to one facility, one verification team, or both. **Expiring**, at the
top right, lists every verification that expires within seven days, by facility.

The icons are those of the [verification status](concepts.md#verification-status): a green
thumbs-up for Verified, a yellow triangle for Provisionally Verified, a red X for Not Verified.

## A control's page

A control's page (**Control Verification**) lists the control's verifications in two panels,
**RF Operations** and **Beam Operations**, one row per segment or destination. **Filter** picks the
control (listed as *team* / *control*) and, optionally, one facility. **Info** shows the control's
description, documents, team and verification frequency.

| Column | Shows |
|---|---|
| **Facility / RF Segment** or **Facility / Beam Destination** | Where the control applies. |
| **Verified** | The status icon, the verification date and who verified it. |
| **Expiration Date** | When the verification ends. |
| **Components** | The components the verification covers, each with its status in SRM and a link to it there. |
| **Comments** | The verifier's comments, and a **Documentation** link if one was given. |
| **Audit** | **History**: every change to this verification. |

Work on one control at a time from here: it lists exactly the rows your team is responsible for.

## A segment's or destination's page

A segment's or destination's page lists the verification of each control that participates in it,
as *team* / *control*, with the same columns. Its title shows its overall status and earliest
expiration. Choose another segment or destination from the list at the top. The same view opens
in a dialog when you select a segment or destination on the [Authorizations](authorizations.md)
tab.

## Verifying a control

You can edit a verification if you have the directory role of the control's verification team
(**Inventory › Verifiers**), or are an administrator. Log in first.

1. Open the control's page (or a segment's or destination's page).
2. Select the rows you checked. Hold down Ctrl or Shift (⌘ on a Mac) to select several, or use
   **Select › All**. Rows of several segments or destinations can be edited together, as long as
   they are your team's.
3. Select **Edit Verification**. The dialog lists the rows you selected, and notes when their
   current values differ.
4. Fill in:
   - **Status**: **Verified** or **Not Verified**.
   - **Verification Date**: when it was checked; **Now** fills in the current time.
   - **Verified By**: who checked it; **Me** fills in your username.
   - **Expiration Date**: when it must be checked again, normally from the control's verification
     frequency. Required for Verified, and it can't be in the past. Not Verified clears it.
   - **Comments**, and a **Documentation URL**, such as a checklist or work record.
5. Select **Save**. The same values are saved to every selected row.

Every save is kept in the row's **History**. The dialog doesn't offer Provisionally Verified.

### Downgrading a control

Saving a lower status than a row had, such as Not Verified for a Verified row, is a
**downgrade**. A downgrade revokes the director's permission at once: JAM sets each affected
segment to high power RF No, and each affected destination to beam mode None, with a comment
naming the control. It saves that as a new authorization, posts a log entry, and emails the
facility's watchers, the administrators and manager, and the control's team. See
[Expirations and notifications](notifications.md).

Downgrade a control as soon as it can no longer be relied on; don't wait for its expiration.

Verifying a control again doesn't restore permission: the facility's authorizer must
[authorize](authorizations.md#restoring-permission-after-a-reduction) it again.

### Components

A verification can list the components it covers, such as the PSS devices of a segment. The
components come from SRM, and their status icons show SRM's status: a component that isn't Ready
in SRM may need the control checked again. The components are for information; they don't change
the verification's status.

Administrators add or remove components: select one row, then **Edit Components**, search for a
component by name and **Add** it, or pick one and **Remove** it.

## Verification history

**History** in a row's **Audit** column lists every change to that verification, the newest first,
ten per page: when it was modified and by whom, the verification date, who verified it, the
status, comments and expiration. When a verification expires, JAM adds a row modified by
`jam-admin`, with Not Verified and the comment "Expired".
