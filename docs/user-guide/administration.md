# Administration

[User Guide](README.md) › Administration

This page is for JAM administrators: users with the `jam-admin` role, who see the **Setup** tab.
Administrators can also do what every other role can, for every facility and team: change any
authorization and verify any control. What they do is still recorded under their own username.
See [Roles](concepts.md#roles).

## Setup tab

| Page | For |
|---|---|
| **Settings** | The app's settings; see [Settings](#settings). Select a row to edit its value. |
| **Directory Cache** | JAM caches users' names, emails and roles from the user directory (Keycloak), and clears the cache every night at midnight. Clear it, or look up a user or role, after a change in the directory, such as a new verification team member. Users must log out and in again for a change to their own roles to apply. |

## Settings

**Setup › Settings** lists the settings with their descriptions.

| Setting | Use |
|---|---|
| `USER_QUERY_URL` | Where the username fields, such as **Verified By**, look up users. |
| `COMPONENT_QUERY_URL` | Where **Edit Components** searches for components (SRM). |
| `COMPONENT_DETAIL_URL` | The link from a component's name to its details (SRM); the name is added to the end. |
| `EMAIL_ENABLED`, `EMAIL_DOMAIN_NAME` | Email settings shared with JLab's other apps, such as for the Help tab's feedback form. JAM's own emails don't use them: they are always sent, to the username followed by `@jlab.org` or to the user's email in the directory. |
| `EMAIL_TESTING_ENABLED` | Send the administrators' and teams' emails to the users with the `testlead` role instead, and not to the facility manager. Watchers' emails are sent as usual. |
| `EMAIL_SENDER_ADDRESS` | The address JAM's emails are sent from. |
| `NOTIFICATION_*` | A banner shown at the top of every page, with an optional link. |
| `HELP_*` | The contacts and documentation links on the Help tab. |
| `ADMIN_ROLE_NAME` | Leave it `jam-admin`: JAM's own pages and checks use that role name. |
| `IP_READ_*`, `SMOOTHNESS_*` | Which addresses may view pages without logging in, and where shared web resources come from. |

## Regular tasks

- **Credited controls and participation**: add or change controls on
  [Inventory › Controls](inventory.md#controls), and say where they apply on
  [Inventory › Participation](inventory.md#participation). Adding a control to a segment or
  destination makes it Not Verified at once, so plan it with its team and the authorizer.
- **Authorizers and watchers**: keep [Inventory › Authorizers](inventory.md#authorizers) and
  [Inventory › Watchers](inventory.md#watchers) current as people change jobs.
- **Verification team members**: add or remove users from the team's role in the user directory,
  then clear the directory cache.
- **Components**: list the components a verification covers; see
  [Components](verifications.md#components).

### Running the expiration check now

The [expiration check](notifications.md#when-jam-checks) for all facilities runs every night at
midnight. To run it now, for example after correcting an expiration date, open
`/jam/ajax/expiration` (at JLab, [ace.jlab.org/jam/ajax/expiration](https://ace.jlab.org/jam/ajax/expiration))
while logged in as an administrator. It answers `{"stat":"ok"}` when done. It also sends the
upcoming expiration notices again.

## What is changed in the database

JAM has no pages to change these; a developer with database access changes them:

- **Facilities**: name, URL path, manager, logbooks and order.
- **RF segments** and **beam destinations**: name, facility, current units (beam), whether active,
  and order. An inactive segment or destination is hidden everywhere and is no longer checked
  for expiration; its history is kept.
- **Verification teams**: name and directory role.

The beam modes of each facility are set in the code.

## When notifications go missing

Log entries and emails are made in the background after each change, using a screenshot service
and the logbook server. If an authorization changed but its **Log Entry** link never appears, or
the emails have no picture, tell the technical contact on the Help tab: the change itself is
saved.
