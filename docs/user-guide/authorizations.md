# Authorizations

[User Guide](README.md) › Authorizations

The **Authorizations** tab shows each facility's current authorization. Pick the facility in the
**Auth** list on the left. Anyone can read it; authorizers and administrators change it.

## Reading an authorization

A facility's page has an **RF Operations** panel, with a row per RF segment, and a **Beam
Operations** panel, with a row per beam destination. A facility without segments or destinations
has no panel for them.

| Column | Shows |
|---|---|
| **RF Segment** or **Beam Destination** | Its name. Select it to see the verification of each of its credited controls. |
| **Auth** | Whether the operation is [approved](concepts.md#approved-operations): a green thumbs-up only when the director's permission allows it and its controls are Verified or Provisionally Verified, otherwise a red X. |
| **High Power RF** (RF) | The director's permission: Yes or No. |
| **Beam Mode** (beam) | The director's permission: a [beam mode](concepts.md#beam-modes), or None. |
| **Current Limit** (beam) | The highest current allowed, in the destination's units, or **Dump Power Limited** when the director set no limit. |
| **Comment** | The authorizer's comment on this permission. It is outlined in gold when the permission relies on a Provisionally Verified control; the comment says why that is acceptable. |
| **Expiration** | When the permission ends, with **(Expiring Soon)** in red within seven days. |
| **Credited Controls Status** | The worst status of the operation's credited controls, with **(Expiring Soon)** when the earliest verification ends within seven days. Select the icon for each control's status and expiration. |

Below each table:

- **Change Notes**: the authorizer's notes on the latest change, or, after an
  [automatic reduction](concepts.md#expiration-and-automatic-reduction), which segments or
  destinations JAM revoked.
- **Digital Signature**: who authorized it and when. After an automatic reduction it adds
  "† Automatically reduced on" with the date; the signature stays the last authorizer's.
- **Log Entry**: the logbook entry JAM posted for the latest change.
- **History**: every earlier authorization; see [History](#history).

Opening a facility's page also runs that facility's expiration check, so a permission or
verification that has just expired is revoked before the page shows it. See
[Expirations and notifications](notifications.md).

## Changing an authorization

You can change a facility's RF or beam authorization if you are one of its authorizers for that
type (**Inventory › Authorizers**) or an administrator. Log in, and the panel shows **Edit RF
Operations** or **Edit Beam Operations**.

1. Select **Edit RF Operations** or **Edit Beam Operations**. Every row becomes editable.
2. For each row, set the permission:
   - RF: **High Power RF** Yes or No.
   - Beam: the **Beam Mode**, and the **Current Limit**. Leave the limit empty for Dump Power
     Limited.
   - Choosing No or None clears and locks the row's other fields.
3. For each row with permission, set the **Expiration** date and time, and a **Comment** if
   needed.
4. In **Change Notes**, write what you changed and why. The box starts empty; your notes replace
   the previous ones.
5. Select **Save**, or **Cancel** to discard your changes.

Saving records a new authorization of every row, signed by you, so leave the rows you aren't
changing as they are. JAM checks each row with permission, and refuses the save, naming the
segment or destination, when:

- its credited controls aren't Verified or Provisionally Verified;
- its controls are Provisionally Verified and it has no comment explaining why permission is
  given anyway;
- it has no expiration date; or
- its expiration is less than one hour from now.

### After you save

The page reloads with the new authorization. Then, in the background, JAM:

- posts a log entry, titled "JAM: *Facility* RF Authorization Updated" or "JAM: *Facility* Beam
  Authorization Updated", with a picture of the new authorization, to the facility's logbooks,
  as you; the **Log Entry** link appears when it is posted;
- emails the same picture to the facility's [watchers](inventory.md#watchers) for that type, and
  to the administrators and the facility's manager.

See [Expirations and notifications](notifications.md#emails-and-log-entries).

### Restoring permission after a reduction

JAM revokes permission by itself when it expires or a credited control is downgraded or expires,
but never restores it. Once the controls are verified again, an authorizer edits the
authorization and gives permission again, with a new expiration.

## History

**History**, at the top right of each panel, lists the facility's RF or beam authorizations, the
newest first, ten per page: when each was modified and by whom, when it was authorized and by
whom, and its change notes. A row whose Modified By differs from its Authorized By is an automatic
reduction, modified by `jam-admin`.

**Destination Details** or **Segment Details** shows that authorization's rows. Rows that changed
from the authorization before it are highlighted in yellow. This page is also the picture JAM
attaches to its emails and log entries.
