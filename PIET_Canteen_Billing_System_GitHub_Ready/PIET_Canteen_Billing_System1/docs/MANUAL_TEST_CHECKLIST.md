# Manual verification checklist

Use a disposable test database before testing destructive or refund flows.

- [ ] Run the migration on a copy of the existing database and verify old users, food, orders and order_items remain.
- [ ] Verify first-admin setup requires the configured token and stops working once any user exists.
- [ ] Verify correct/incorrect login, inactive account blocking, legacy-password upgrade and mandatory temporary password change.
- [ ] Verify staff cannot open admin-only screens; test direct entry points by attempting each screen with STAFF role.
- [ ] Add/edit/deactivate a menu item; verify delete is a soft deactivation so historical order items remain.
- [ ] Create a bill with 0%, partial and 100% discount; verify GST is calculated after discount and all amounts round to 2 decimals.
- [ ] Attempt to oversell stock; verify the order fails without partially inserting the order or decrementing stock.
- [ ] Complete one Cash, UPI and Card bill; verify order, order_items, stock movements and receipts.
- [ ] Double-click Complete Payment and verify only one order is created.
- [ ] Cancel/refund a completed order; verify stock is restored once and repeated refund is blocked.
- [ ] Verify reports exclude refunded orders and historical order totals do not change after GST settings change.
- [ ] Verify stock thresholds, low-stock alerts, audit records, user reset/deactivation and CSV exports.
- [ ] Create a backup, restore it to a separate test database and compare record counts.
- [ ] Reopen the packaged ZIP as an Ant project in NetBeans and build/run it with the official Connector/J JAR installed.
