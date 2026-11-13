# Expirations and notifications

[User Guide](README.md) › Expirations and notifications

Every permission the director gives, and every Verified or Provisionally Verified control, has an
expiration date. JAM checks for expirations, revokes what has expired, and warns people a week
ahead.

## When JAM checks

- **Every night at midnight**, for every facility: JAM revokes what has expired, then sends the
  [upcoming expiration notices](#upcoming-expiration-notices).
- **Whenever someone opens a facility's page** on the Authorizations tab: JAM revokes what has
  expired in that facility, so the page never shows an expired permission as current.

Something that expires during the day is therefore revoked when the facility's page is next
opened, or at midnight, whichever comes first.

## What expiration does

1. Each Verified or Provisionally Verified control whose expiration has passed becomes **Not
   Verified**, with the comment "Expired". Its history gets a row modified by `jam-admin`.
2. In the facility's current authorization, JAM sets to high power RF **No**, or beam mode
   **None**:
   - each permission whose expiration has passed, with the comment "Permission automatically
     revoked due to director's authorization expiration"; and
   - each permission of a segment or destination with a control that just expired, with the
     comment "Permission automatically revoked due to credited control *control* verification
     expiration".
3. If any permission was revoked, JAM saves the result as a new authorization: its change notes
   list the segments or destinations revoked, it keeps the last authorizer's signature, and it
   is marked "Automatically reduced". Then JAM posts a log entry and sends emails, as below.

A [downgrade](verifications.md#downgrading-a-control) by a verification team revokes permission in
the same way, at once, with the comment "Permission automatically revoked due to credited control
*control* verification downgrade".

If a control expires or is downgraded where there was no permission to revoke, only the
verification changes: there is no new authorization, log entry or email.

## Emails and log entries

Each change to an authorization, whether by an authorizer or automatic, gets a log entry and
emails with a picture of the new authorization. The log entry goes to the facility's logbooks
(**Inventory › Facilities**), tagged Readme, titled "JAM: *Facility* RF Authorization Updated"
or "JAM: *Facility* Beam Authorization Updated", and posted as the authorizer, or, for an
automatic reduction, as the last authorizer. The email subjects start with "JAM: *Facility*".

| Event | Log entry | Watchers | Administrators and facility manager | Verification team |
|---|---|---|---|---|
| An authorizer saves an authorization | Yes | Yes | Yes | No |
| A downgrade revokes permission | Yes | Yes | Yes | The downgraded control's team: "… Authorization Reduced due to Control Verification Downgrade" |
| An expiration revokes permission | Yes | Yes | Yes | The team of each expired control: "… Authorization Reduced due to Control Verification Expiration" |
| Upcoming expirations (nightly) | No | No | Yes | Yes, for the team's controls |

Watchers get the RF or beam emails they are listed for (**Inventory › Watchers**). The
administrators are the users with the `jam-admin` role, and the verification team is the users
with the team's directory role.

Log entries and emails are made in the background after the change, and a failure doesn't undo
the change. **Always check JAM for the current authorization**, as each log entry says.

## Upcoming expiration notices

Each night JAM emails "JAM: *Facility* Upcoming Expiration Notice" about what will expire between
six and seven days later, so each expiration is normally announced once, a week ahead:

- to the administrators and the facility's manager: the facility's RF and beam permissions and
  control verifications that will expire;
- to each verification team: its control verifications that will expire.

The **Expiring** link on the [Verifications](verifications.md#the-verifications-tab) tab lists
everything expiring within the next seven days, and the Authorizations and Verifications pages
mark it **(Expiring Soon)**.

To keep an operation running, verify its controls again, or give a new permission, before it
expires.
